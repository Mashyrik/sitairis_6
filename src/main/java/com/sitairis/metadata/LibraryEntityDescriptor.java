package com.sitairis.metadata;

import java.util.List;

public record LibraryEntityDescriptor(
        Class<?> type, String name, List<LibraryOperationDescriptor> operations) {

    public LibraryEntityDescriptor {
        if (type == null || name == null || name.isBlank() || operations == null) {
            throw new IllegalArgumentException("Описание сущности заполнено не полностью.");
        }
        operations = List.copyOf(operations);
    }
}
