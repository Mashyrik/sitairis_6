package com.sitairis.metadata.execution;

import com.sitairis.metadata.LibraryMetadataRegistry;
import com.sitairis.metadata.LibraryOperation;
import com.sitairis.metadata.LibraryOperationDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

public final class ReflectionLibraryActionExecutor implements LibraryActionExecutor {
    private final LibraryMetadataRegistry registry;

    public ReflectionLibraryActionExecutor(LibraryMetadataRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "Реестр метаданных обязателен.");
    }

    @Override
    public Object execute(Object target, LibraryOperation operation, Object... arguments) {
        Objects.requireNonNull(target, "Целевой объект обязателен.");
        Objects.requireNonNull(operation, "Операция обязательна.");
        LibraryOperationDescriptor descriptor =
                registry.requireOperation(target.getClass(), operation);
        try {
            return descriptor.method().invoke(target, arguments);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Нет доступа к методу сценария.", exception);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Аргументы не соответствуют методу " + descriptor.method() + ".", exception);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("Метод сценария завершился с ошибкой.", cause);
        }
    }
}
