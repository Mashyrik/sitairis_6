package com.sitairis.service.observer;

import com.sitairis.model.Loan;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public final class LibraryEvent {
    private final LibraryEventType type;
    private final Loan loan;
    private final String message;
    private final BigDecimal fineAmount;
    private final LocalDateTime timestamp;

    public LibraryEvent(LibraryEventType type, Loan loan, String message) {
        this(type, loan, message, BigDecimal.ZERO);
    }

    public LibraryEvent(LibraryEventType type, Loan loan, String message, BigDecimal fineAmount) {
        this.type = Objects.requireNonNull(type, "Тип события обязателен.");
        this.loan = Objects.requireNonNull(loan, "Выдача обязательна для события.");
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Сообщение события не может быть пустым.");
        }
        this.message = message.trim();
        this.fineAmount = fineAmount != null && fineAmount.signum() > 0
                ? fineAmount : BigDecimal.ZERO;
        this.timestamp = LocalDateTime.now();
    }

    public LibraryEventType getType() { return type; }
    public Loan getLoan() { return loan; }
    public String getMessage() { return message; }
    public BigDecimal getFineAmount() { return fineAmount; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
