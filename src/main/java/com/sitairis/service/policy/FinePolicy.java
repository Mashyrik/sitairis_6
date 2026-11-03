package com.sitairis.service.policy;

import java.math.BigDecimal;

public interface FinePolicy {
    BigDecimal calculateFine(long overdueDays);
    String getName();
    String getDescription();
}
