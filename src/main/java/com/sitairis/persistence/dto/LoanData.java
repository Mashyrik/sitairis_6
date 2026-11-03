package com.sitairis.persistence.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanData(long id, long readerId, long bookId, LocalDate issueDate,
                       LocalDate dueDate, LocalDate returnDate, String status,
                       BigDecimal fineAmount) {
}
