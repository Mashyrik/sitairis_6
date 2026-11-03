package com.sitairis.metadata;

public enum LibraryOperation {
    ISSUE_BOOK("выдача книги"),
    RETURN_BOOK("возврат книги"),
    CHECK_DUE_DATE("контроль срока");

    private final String description;

    LibraryOperation(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
