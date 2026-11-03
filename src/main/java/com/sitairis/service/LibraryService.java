package com.sitairis.service;

import com.sitairis.metadata.LibraryOperation;
import com.sitairis.metadata.execution.LibraryActionExecutor;
import com.sitairis.model.Book;
import com.sitairis.model.Loan;
import com.sitairis.model.LoanStatus;
import com.sitairis.model.Reader;
import com.sitairis.repository.BookRepository;
import com.sitairis.repository.LoanRepository;
import com.sitairis.repository.ReaderRepository;
import com.sitairis.service.notifier.Notifier;
import com.sitairis.service.observer.LibraryEvent;
import com.sitairis.service.observer.LibraryEventType;
import com.sitairis.service.observer.LibraryObserver;
import com.sitairis.service.policy.FinePolicy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class LibraryService {
    private final BookRepository bookRepository;
    private final ReaderRepository readerRepository;
    private final LoanRepository loanRepository;
    private final LibraryActionExecutor actionExecutor;
    private final List<LibraryObserver> observers = new ArrayList<>();
    private FinePolicy finePolicy;
    private Notifier notifier;

    public LibraryService(BookRepository bookRepository, ReaderRepository readerRepository,
                          LoanRepository loanRepository, FinePolicy finePolicy, Notifier notifier,
                          LibraryActionExecutor actionExecutor) {
        this.bookRepository = Objects.requireNonNull(bookRepository);
        this.readerRepository = Objects.requireNonNull(readerRepository);
        this.loanRepository = Objects.requireNonNull(loanRepository);
        this.finePolicy = Objects.requireNonNull(finePolicy);
        this.notifier = Objects.requireNonNull(notifier);
        this.actionExecutor = Objects.requireNonNull(actionExecutor);
    }

    public void attachObserver(LibraryObserver observer) {
        Objects.requireNonNull(observer, "Наблюдатель не может быть null.");
        boolean typeAlreadyRegistered = observers.stream()
                .anyMatch(existing -> existing.getClass().equals(observer.getClass()));
        if (!typeAlreadyRegistered) observers.add(observer);
    }

    public void detachObserver(LibraryObserver observer) {
        observers.remove(observer);
    }

    public List<LibraryObserver> getObservers() {
        return Collections.unmodifiableList(observers);
    }

    public void notifyObservers(LibraryEvent event) {
        Objects.requireNonNull(event, "Событие не может быть null.");
        for (LibraryObserver observer : List.copyOf(observers)) {
            observer.onEvent(event);
        }
        notifier.send(event.getLoan().getReader().getFullName(), event.getMessage());
    }

    public void setFinePolicy(FinePolicy finePolicy) {
        this.finePolicy = Objects.requireNonNull(finePolicy);
        notifier.send("Система", "Установлена политика штрафов: " + finePolicy.getName());
    }

    public void setNotifier(Notifier notifier) {
        this.notifier = Objects.requireNonNull(notifier);
        this.notifier.send("Система", "Установлен сервис оповещений: " + notifier.getName());
    }

    public String getFinePolicyDescription() {
        return finePolicy.getName() + " (" + finePolicy.getDescription() + ")";
    }

    public Book addBook(String title, String author, String isbn, int publicationYear) {
        Book book = new Book(bookRepository.nextId(), title, author, isbn, publicationYear);
        bookRepository.save(book);
        return book;
    }

    public Reader registerReader(String fullName, String email, String phone) {
        Reader reader = new Reader(readerRepository.nextId(), fullName, email, phone);
        readerRepository.save(reader);
        return reader;
    }

    public Loan issueBook(long readerId, long bookId, int loanDays, LocalDate issueDate) {
        Reader reader = requireReader(readerId);
        if (!reader.isActive()) throw new IllegalStateException("Читатель заблокирован.");
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Книга с ID " + bookId + " не найдена."));
        if (!book.isAvailable()) throw new IllegalStateException("Книга недоступна для выдачи.");

        actionExecutor.execute(book, LibraryOperation.ISSUE_BOOK);
        Loan loan = new Loan(loanRepository.nextId(), reader, book, issueDate, loanDays);
        loanRepository.save(loan);
        String message = String.format("Выдана книга \"%s\". Срок: %s",
                book.getTitle(), loan.getDueDate());
        notifyObservers(new LibraryEvent(LibraryEventType.BOOK_ISSUED, loan, message));
        return loan;
    }

    public Loan returnBook(long loanId, LocalDate returnDate) {
        Loan loan = requireLoan(loanId);
        if (!loan.isActive()) throw new IllegalStateException("Книга уже была возвращена.");

        LocalDate actualDate = returnDate != null ? returnDate : LocalDate.now();
        if (actualDate.isBefore(loan.getIssueDate())) {
            throw new IllegalArgumentException("Дата возврата не может быть раньше даты выдачи.");
        }
        long overdueDays = loan.getDaysOverdue(actualDate);
        BigDecimal fine = overdueDays > 0 ? finePolicy.calculateFine(overdueDays) : BigDecimal.ZERO;
        if (overdueDays > 0) loan.markOverdue();
        actionExecutor.execute(loan, LibraryOperation.RETURN_BOOK, actualDate, fine);

        String message = fine.signum() > 0
                ? String.format("Книга \"%s\" возвращена с просрочкой %d дн. Штраф: %.2f BYN",
                    loan.getBook().getTitle(), overdueDays, fine)
                : "Книга возвращена. Штраф отсутствует.";
        notifyObservers(new LibraryEvent(LibraryEventType.BOOK_RETURNED, loan, message, fine));
        return loan;
    }

    public Loan renewLoan(long loanId, int additionalDays) {
        Loan loan = requireLoan(loanId);
        loan.renew(additionalDays);
        String message = "Срок пользования продлён до " + loan.getDueDate();
        notifyObservers(new LibraryEvent(LibraryEventType.LOAN_RENEWED, loan, message));
        return loan;
    }

    public List<Loan> checkOverdueLoans(LocalDate checkDate) {
        LocalDate date = checkDate != null ? checkDate : LocalDate.now();
        List<Loan> newlyOverdue = loanRepository.findActiveLoans().stream()
                .filter(loan -> isOverdue(loan, date) && loan.getStatus() == LoanStatus.ACTIVE)
                .toList();
        for (Loan loan : newlyOverdue) {
            loan.markOverdue();
            long overdueDays = loan.getDaysOverdue(date);
            BigDecimal potentialFine = finePolicy.calculateFine(overdueDays);
            String message = "ВНИМАНИЕ: срок возврата истёк! Просрочка: "
                    + overdueDays + " дн., возможный штраф: " + potentialFine + " BYN";
            notifyObservers(new LibraryEvent(
                    LibraryEventType.LOAN_OVERDUE, loan, message, potentialFine));
        }
        return newlyOverdue;
    }

    public Reader setReaderActive(long readerId, boolean active) {
        Reader reader = requireReader(readerId);
        if (active) reader.unblock(); else reader.block();
        return reader;
    }

    public List<Book> getBooks() { return bookRepository.findAll(); }
    public List<Reader> getReaders() { return readerRepository.findAll(); }
    public List<Loan> getLoans() { return loanRepository.findAll(); }

    private Reader requireReader(long id) {
        return readerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Читатель с ID " + id + " не найден."));
    }

    private Loan requireLoan(long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Выдача с ID " + id + " не найдена."));
    }

    private boolean isOverdue(Loan loan, LocalDate date) {
        return (boolean) actionExecutor.execute(
                loan, LibraryOperation.CHECK_DUE_DATE, date);
    }

}
