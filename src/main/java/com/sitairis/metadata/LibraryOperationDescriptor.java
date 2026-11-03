package com.sitairis.metadata;

import java.lang.reflect.Method;

public record LibraryOperationDescriptor(LibraryOperation operation, Method method) {
    public LibraryOperationDescriptor {
        if (operation == null || method == null) {
            throw new IllegalArgumentException("Операция и метод обязательны.");
        }
    }
}
