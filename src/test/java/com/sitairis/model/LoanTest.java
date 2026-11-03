package com.sitairis.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoanTest {
    private Book book;
    private Reader reader;

    @BeforeEach
    void setUp() {
        book = new Book(1, "Книга", "Автор", "ISBN", 2020);
        reader = new Reader(1, "Иван Иванов", "reader@example.com", "+375290000000");
    }

    @Test
    void constructorCalculatesDueDate() {
        Loan loan = loan(14);

        assertEquals(LocalDate.of(2026, 1, 15), loan.getDueDate());
        assertEquals(LoanStatus.ACTIVE, loan.getStatus());
        assertTrue(loan.isActive());
    }

    @Test
    void constructorRejectsZeroLoanPeriod() {
        assertThrows(IllegalArgumentException.class, () -> loan(0));
    }

    @Test
    void constructorRejectsMissingReader() {
        assertThrows(IllegalArgumentException.class,
                () -> new Loan(1, null, book, LocalDate.of(2026, 1, 1), 7));
    }

    @Test
    void overdueBoundaryStartsAfterDueDate() {
        Loan loan = loan(7);

        assertFalse(loan.isOverdue(LocalDate.of(2026, 1, 8)));
        assertTrue(loan.isOverdue(LocalDate.of(2026, 1, 9)));
        assertEquals(1, loan.getDaysOverdue(LocalDate.of(2026, 1, 9)));
    }

    @Test
    void renewExtendsDueDate() {
        Loan loan = loan(7);

        loan.renew(3);

        assertEquals(LocalDate.of(2026, 1, 11), loan.getDueDate());
    }

    @Test
    void returnClosesLoanAndMakesBookAvailable() {
        book.markAsIssued();
        Loan loan = loan(7);

        loan.recordReturn(LocalDate.of(2026, 1, 10), new BigDecimal("1.00"));

        assertEquals(LoanStatus.RETURNED, loan.getStatus());
        assertEquals(new BigDecimal("1.00"), loan.getFineAmount());
        assertTrue(book.isAvailable());
        assertFalse(loan.isActive());
    }

    @Test
    void returnedLoanCannotBeRenewedOrReturnedAgain() {
        Loan loan = loan(7);
        loan.recordReturn(LocalDate.of(2026, 1, 8), BigDecimal.ZERO);

        assertThrows(IllegalStateException.class, () -> loan.renew(1));
        assertThrows(IllegalStateException.class,
                () -> loan.recordReturn(LocalDate.of(2026, 1, 9), BigDecimal.ZERO));
    }

    private Loan loan(int days) {
        return new Loan(1, reader, book, LocalDate.of(2026, 1, 1), days);
    }
}
