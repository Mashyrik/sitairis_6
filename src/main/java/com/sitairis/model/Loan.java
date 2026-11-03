package com.sitairis.model;

import com.sitairis.metadata.LibraryOperation;
import com.sitairis.metadata.annotation.EntityMetadata;
import com.sitairis.metadata.annotation.ScenarioAction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@EntityMetadata(name = "Выдача")
public class Loan {
    private final long id;
    private final Reader reader;
    private final Book book;
    private final LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private LoanStatus status;
    private BigDecimal fineAmount;

    public Loan(long id, Reader reader, Book book, LocalDate issueDate, int loanDays) {
        if (id <= 0) throw new IllegalArgumentException("ID выдачи должен быть положительным числом.");
        if (reader == null) throw new IllegalArgumentException("Читатель не может быть null.");
        if (book == null) throw new IllegalArgumentException("Книга не может быть null.");
        if (loanDays <= 0) throw new IllegalArgumentException("Срок выдачи должен быть больше 0 дней.");
        this.id = id;
        this.reader = reader;
        this.book = book;
        this.issueDate = issueDate != null ? issueDate : LocalDate.now();
        this.dueDate = this.issueDate.plusDays(loanDays);
        this.status = LoanStatus.ACTIVE;
        this.fineAmount = BigDecimal.ZERO;
    }

    public long getId() { return id; }
    public Reader getReader() { return reader; }
    public Book getBook() { return book; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public LoanStatus getStatus() { return status; }
    public BigDecimal getFineAmount() { return fineAmount; }

    public boolean isActive() {
        return status == LoanStatus.ACTIVE || status == LoanStatus.OVERDUE;
    }

    @ScenarioAction(LibraryOperation.CHECK_DUE_DATE)
    public boolean isOverdue(LocalDate checkDate) {
        LocalDate date = checkDate != null ? checkDate : LocalDate.now();
        return (returnDate != null ? returnDate : date).isAfter(dueDate);
    }

    public long getDaysOverdue(LocalDate checkDate) {
        LocalDate date = returnDate != null ? returnDate
                : (checkDate != null ? checkDate : LocalDate.now());
        return date.isAfter(dueDate) ? ChronoUnit.DAYS.between(dueDate, date) : 0;
    }

    public void markOverdue() {
        if (status == LoanStatus.ACTIVE) status = LoanStatus.OVERDUE;
    }

    public void renew(int additionalDays) {
        if (status == LoanStatus.RETURNED) {
            throw new IllegalStateException("Нельзя продлить уже закрытую выдачу.");
        }
        if (additionalDays <= 0) {
            throw new IllegalArgumentException("Количество дополнительных дней должно быть положительным.");
        }
        dueDate = dueDate.plusDays(additionalDays);
        if (status == LoanStatus.OVERDUE && !isOverdue(LocalDate.now())) status = LoanStatus.ACTIVE;
    }

    @ScenarioAction(LibraryOperation.RETURN_BOOK)
    public void recordReturn(LocalDate actualReturnDate, BigDecimal fine) {
        if (status == LoanStatus.RETURNED) throw new IllegalStateException("Выдача уже возвращена ранее.");
        returnDate = actualReturnDate != null ? actualReturnDate : LocalDate.now();
        fineAmount = fine != null && fine.signum() > 0 ? fine : BigDecimal.ZERO;
        status = LoanStatus.RETURNED;
        book.markAsAvailable();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Loan loan = (Loan) object;
        return id == loan.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        String returned = returnDate != null ? ", Возвращена: " + returnDate : "";
        String fine = fineAmount.signum() > 0 ? String.format(", Штраф: %.2f BYN", fineAmount) : "";
        return String.format("[Выдача #%d] Читатель: %s (ID: %d) | Книга: \"%s\" (ID: %d) | "
                        + "Выдана: %s | Срок: %s | Статус: %s%s%s",
                id, reader.getFullName(), reader.getId(), book.getTitle(), book.getId(),
                issueDate, dueDate, status.getDescription(), returned, fine);
    }
}
