package com.sitairis.service.observer;

import java.math.BigDecimal;

public class StatisticsObserver implements LibraryObserver {
    private int issuedCount;
    private int returnedCount;
    private int overdueCount;
    private int renewedCount;
    private BigDecimal totalFines = BigDecimal.ZERO;

    @Override
    public void onEvent(LibraryEvent event) {
        switch (event.getType()) {
            case BOOK_ISSUED -> issuedCount++;
            case BOOK_RETURNED -> {
                returnedCount++;
                totalFines = totalFines.add(event.getFineAmount());
            }
            case LOAN_OVERDUE -> overdueCount++;
            case LOAN_RENEWED -> renewedCount++;
        }
    }

    @Override
    public String getName() { return "Сбор статистики"; }

    public int getIssuedCount() { return issuedCount; }
    public int getReturnedCount() { return returnedCount; }
    public int getOverdueCount() { return overdueCount; }
    public int getRenewedCount() { return renewedCount; }
    public BigDecimal getTotalFines() { return totalFines; }

    public String getSummary() {
        return String.format(
                "Выдач: %d, возвратов: %d, просрочек: %d, продлений: %d, штрафов: %.2f BYN",
                issuedCount, returnedCount, overdueCount, renewedCount, totalFines);
    }
}
