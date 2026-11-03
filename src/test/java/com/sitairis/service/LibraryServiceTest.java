package com.sitairis.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitairis.metadata.LibraryMetadataProcessor;
import com.sitairis.metadata.LibraryOperation;
import com.sitairis.metadata.execution.LibraryActionExecutor;
import com.sitairis.metadata.execution.ReflectionLibraryActionExecutor;
import com.sitairis.model.Book;
import com.sitairis.model.Loan;
import com.sitairis.model.LoanStatus;
import com.sitairis.model.Reader;
import com.sitairis.repository.BookRepository;
import com.sitairis.repository.LoanRepository;
import com.sitairis.repository.ReaderRepository;
import com.sitairis.service.notifier.Notifier;
import com.sitairis.service.policy.FinePolicy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LibraryServiceTest {
    @Mock private BookRepository bookRepository;
    @Mock private ReaderRepository readerRepository;
    @Mock private LoanRepository loanRepository;
    @Mock private FinePolicy finePolicy;
    @Mock private Notifier notifier;

    private LibraryService service;
    private LibraryActionExecutor actionExecutor;

    @BeforeEach
    void setUp() {
        var registry = new LibraryMetadataProcessor().scan(
                Reader.class, Book.class, Loan.class);
        actionExecutor = spy(new ReflectionLibraryActionExecutor(registry));
        service = new LibraryService(
                bookRepository, readerRepository, loanRepository, finePolicy, notifier,
                actionExecutor);
    }

    @Test
    void issueBookCreatesLoanAndNotifiesReader() {
        Reader reader = activeReader();
        Book book = availableBook();
        when(readerRepository.findById(1)).thenReturn(Optional.of(reader));
        when(bookRepository.findById(2)).thenReturn(Optional.of(book));
        when(loanRepository.nextId()).thenReturn(10L);

        Loan loan = service.issueBook(1, 2, 14, LocalDate.of(2026, 1, 1));

        assertEquals(10, loan.getId());
        assertEquals(LocalDate.of(2026, 1, 15), loan.getDueDate());
        verify(loanRepository).save(loan);
        verify(actionExecutor).execute(book, LibraryOperation.ISSUE_BOOK);
        verify(notifier).send(eq(reader.getFullName()), contains("Выдана книга"));
    }

    @Test
    void issueBookRejectsBlockedReader() {
        Reader reader = activeReader();
        reader.block();
        when(readerRepository.findById(1)).thenReturn(Optional.of(reader));

        assertThrows(IllegalStateException.class,
                () -> service.issueBook(1, 2, 14, LocalDate.of(2026, 1, 1)));
        verify(bookRepository, never()).findById(2);
        verify(loanRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void issueBookRejectsUnavailableBook() {
        Reader reader = activeReader();
        Book book = availableBook();
        book.markAsIssued();
        when(readerRepository.findById(1)).thenReturn(Optional.of(reader));
        when(bookRepository.findById(2)).thenReturn(Optional.of(book));

        assertThrows(IllegalStateException.class,
                () -> service.issueBook(1, 2, 14, LocalDate.of(2026, 1, 1)));
        verify(loanRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void returnBookUsesMockedFinePolicy() {
        Book book = availableBook();
        book.markAsIssued();
        Loan loan = new Loan(7, activeReader(), book, LocalDate.of(2026, 1, 1), 7);
        when(loanRepository.findById(7)).thenReturn(Optional.of(loan));
        when(finePolicy.calculateFine(2)).thenReturn(new BigDecimal("3.50"));

        Loan returned = service.returnBook(7, LocalDate.of(2026, 1, 10));

        assertEquals(LoanStatus.RETURNED, returned.getStatus());
        assertEquals(new BigDecimal("3.50"), returned.getFineAmount());
        verify(actionExecutor).execute(loan, LibraryOperation.RETURN_BOOK,
                LocalDate.of(2026, 1, 10), new BigDecimal("3.50"));
        verify(finePolicy).calculateFine(2);
        verify(notifier).send(eq(returned.getReader().getFullName()), contains("Штраф:"));
    }

    @Test
    void checkOverdueMarksOnlyNewlyOverdueLoan() {
        Loan activeLoan = new Loan(1, activeReader(), availableBook(),
                LocalDate.of(2026, 1, 1), 7);
        Loan currentLoan = new Loan(2, activeReader(),
                new Book(3, "Новая книга", "Автор", "ISBN-3", 2022),
                LocalDate.of(2026, 1, 10), 14);
        when(loanRepository.findActiveLoans()).thenReturn(List.of(activeLoan, currentLoan));
        when(finePolicy.calculateFine(2)).thenReturn(BigDecimal.ONE);

        List<Loan> overdue = service.checkOverdueLoans(LocalDate.of(2026, 1, 10));

        assertEquals(List.of(activeLoan), overdue);
        assertEquals(LoanStatus.OVERDUE, activeLoan.getStatus());
        assertEquals(LoanStatus.ACTIVE, currentLoan.getStatus());
        verify(actionExecutor).execute(
                activeLoan, LibraryOperation.CHECK_DUE_DATE, LocalDate.of(2026, 1, 10));
        verify(actionExecutor).execute(
                currentLoan, LibraryOperation.CHECK_DUE_DATE, LocalDate.of(2026, 1, 10));
        verify(notifier).send(eq(activeLoan.getReader().getFullName()), contains("Просрочка: 2"));
    }

    @Test
    void returnDateBeforeIssueDateIsRejected() {
        Loan loan = new Loan(7, activeReader(), availableBook(),
                LocalDate.of(2026, 1, 10), 7);
        when(loanRepository.findById(7)).thenReturn(Optional.of(loan));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.returnBook(7, LocalDate.of(2026, 1, 9)));

        assertTrue(exception.getMessage().contains("раньше даты выдачи"));
        verify(finePolicy, never()).calculateFine(org.mockito.ArgumentMatchers.anyLong());
    }

    private Reader activeReader() {
        return new Reader(1, "Иван Иванов", "reader@example.com", "+375290000000");
    }

    private Book availableBook() {
        return new Book(2, "Книга", "Автор", "ISBN-2", 2020);
    }
}
