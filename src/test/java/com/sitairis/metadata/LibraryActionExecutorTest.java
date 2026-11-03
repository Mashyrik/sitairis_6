package com.sitairis.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sitairis.metadata.execution.DirectLibraryActionExecutor;
import com.sitairis.metadata.execution.LibraryActionExecutor;
import com.sitairis.metadata.execution.ReflectionLibraryActionExecutor;
import com.sitairis.model.Book;
import com.sitairis.model.Loan;
import com.sitairis.model.LoanStatus;
import com.sitairis.model.Reader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class LibraryActionExecutorTest {
    static Stream<LibraryActionExecutor> executors() {
        var registry = new LibraryMetadataProcessor().scan(Reader.class, Book.class, Loan.class);
        return Stream.of(
                new ReflectionLibraryActionExecutor(registry),
                new DirectLibraryActionExecutor());
    }

    @ParameterizedTest
    @MethodSource("executors")
    void reflectionAndDirectSolutionsProduceTheSamePositiveResult(
            LibraryActionExecutor executor) {
        Book book = book();
        Reader reader = reader();
        Loan loan = new Loan(1, reader, book, LocalDate.of(2026, 1, 1), 7);

        executor.execute(book, LibraryOperation.ISSUE_BOOK);
        boolean overdue = (boolean) executor.execute(
                loan, LibraryOperation.CHECK_DUE_DATE, LocalDate.of(2026, 1, 10));
        executor.execute(loan, LibraryOperation.RETURN_BOOK,
                LocalDate.of(2026, 1, 10), new BigDecimal("2.50"));

        assertTrue(overdue);
        assertTrue(book.isAvailable());
        assertEquals(LoanStatus.RETURNED, loan.getStatus());
        assertEquals(new BigDecimal("2.50"), loan.getFineAmount());
    }

    @ParameterizedTest
    @MethodSource("executors")
    void reflectionAndDirectSolutionsReportUnsupportedOperation(
            LibraryActionExecutor executor) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> executor.execute(reader(), LibraryOperation.ISSUE_BOOK));

        assertTrue(exception.getMessage().contains("не "));
    }

    @Test
    void reflectionExecutorReportsWrongArgumentsClearly() {
        var registry = new LibraryMetadataProcessor().scan(Reader.class, Book.class, Loan.class);
        LibraryActionExecutor executor = new ReflectionLibraryActionExecutor(registry);
        Loan loan = new Loan(1, reader(), book(), LocalDate.of(2026, 1, 1), 7);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> executor.execute(loan, LibraryOperation.CHECK_DUE_DATE, "не дата"));

        assertTrue(exception.getMessage().contains("Аргументы не соответствуют"));
        assertFalse(loan.isOverdue(LocalDate.of(2026, 1, 5)));
    }

    private static Reader reader() {
        return new Reader(1, "Иван Иванов", "reader@example.com", "+375290000000");
    }

    private static Book book() {
        return new Book(1, "Книга", "Автор", "ISBN-1", 2020);
    }
}
