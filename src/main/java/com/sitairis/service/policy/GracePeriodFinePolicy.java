package com.sitairis.service.policy;

import java.math.BigDecimal;

public class GracePeriodFinePolicy implements FinePolicy {
    private static final long GRACE_DAYS = 3;
    private static final BigDecimal DAILY_RATE = new BigDecimal("0.75");

    @Override
    public BigDecimal calculateFine(long overdueDays) {
        long chargeableDays = Math.max(0, overdueDays - GRACE_DAYS);
        return DAILY_RATE.multiply(BigDecimal.valueOf(chargeableDays));
    }

    @Override
    public String getName() { return "Льготная"; }

    @Override
    public String getDescription() { return "3 дня без штрафа, затем 0,75 BYN за день"; }
}
