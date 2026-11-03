package com.sitairis.service.policy;

import java.math.BigDecimal;

public class StandardFinePolicy implements FinePolicy {
    private static final BigDecimal DAILY_RATE = new BigDecimal("0.50");

    @Override
    public BigDecimal calculateFine(long overdueDays) {
        if (overdueDays <= 0) return BigDecimal.ZERO;
        return DAILY_RATE.multiply(BigDecimal.valueOf(overdueDays));
    }

    @Override
    public String getName() { return "Стандартная"; }

    @Override
    public String getDescription() { return "0,50 BYN за каждый день просрочки"; }
}
