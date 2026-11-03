package com.sitairis.metadata;

import java.util.List;

public final class LibraryMetadataRegistry {
    private final List<LibraryEntityDescriptor> entities;

    LibraryMetadataRegistry(List<LibraryEntityDescriptor> entities) {
        this.entities = List.copyOf(entities);
    }

    public List<LibraryEntityDescriptor> getEntities() {
        return entities;
    }

    public LibraryEntityDescriptor requireEntity(Class<?> type) {
        return entities.stream()
                .filter(entity -> entity.type().equals(type))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Класс " + type.getName() + " не зарегистрирован в метаданных."));
    }

    public LibraryOperationDescriptor requireOperation(Class<?> type, LibraryOperation operation) {
        return requireEntity(type).operations().stream()
                .filter(descriptor -> descriptor.operation() == operation)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Операция '" + operation.getDescription() + "' не объявлена для "
                                + type.getSimpleName() + "."));
    }
}
