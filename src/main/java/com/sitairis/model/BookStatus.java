package com.sitairis.model;

public enum BookStatus {
    AVAILABLE("Доступна"),
    ISSUED("Выдана"),
    LOST("Утеряна");

    private final String description;

    BookStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
