package com.sitairis.metadata;

import com.sitairis.metadata.annotation.EntityMetadata;
import com.sitairis.metadata.annotation.ScenarioAction;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/** Scans annotations without depending on concrete library entity names. */
public final class LibraryMetadataProcessor {
    private static final int MINIMUM_CLASS_COUNT = 3;

    public LibraryMetadataRegistry scan(Class<?>... types) {
        if (types == null || types.length < MINIMUM_CLASS_COUNT) {
            throw new IllegalArgumentException(
                    "Обработчик должен сканировать минимум три класса.");
        }

        List<LibraryEntityDescriptor> entities = new ArrayList<>();
        for (Class<?> type : types) {
            if (type == null) {
                throw new IllegalArgumentException("Сканируемый класс не может быть null.");
            }
            EntityMetadata entityMetadata = type.getAnnotation(EntityMetadata.class);
            if (entityMetadata == null) {
                throw new IllegalArgumentException(
                        "Класс " + type.getName() + " не помечен @EntityMetadata.");
            }
            if (entityMetadata.name().isBlank()) {
                throw new IllegalArgumentException(
                        "Имя сущности для " + type.getName() + " не может быть пустым.");
            }

            List<LibraryOperationDescriptor> operations = new ArrayList<>();
            EnumSet<LibraryOperation> foundOperations = EnumSet.noneOf(LibraryOperation.class);
            for (Method method : type.getDeclaredMethods()) {
                ScenarioAction action = method.getAnnotation(ScenarioAction.class);
                if (action == null) {
                    continue;
                }
                validateActionMethod(method);
                if (!foundOperations.add(action.value())) {
                    throw new IllegalArgumentException(
                            "Операция '" + action.value().getDescription()
                                    + "' повторно объявлена в " + type.getName() + ".");
                }
                operations.add(new LibraryOperationDescriptor(action.value(), method));
            }
            entities.add(new LibraryEntityDescriptor(type, entityMetadata.name(), operations));
        }
        return new LibraryMetadataRegistry(entities);
    }

    private void validateActionMethod(Method method) {
        int modifiers = method.getModifiers();
        if (!Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers)) {
            throw new IllegalArgumentException(
                    "Метод сценария должен быть открытым методом экземпляра: " + method);
        }
    }
}
