package com.sitairis.persistence.xml;

import java.math.BigDecimal;

public record LoanStatistics(int total, int active, int overdue, int returned,
                             BigDecimal totalFine) {
}
