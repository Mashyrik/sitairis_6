package com.sitairis.model;

public enum LoanStatus {
    ACTIVE("Активна"),
    OVERDUE("Просрочена"),
    RETURNED("Закрыта");

    private final String description;

    LoanStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
