package com.sitairis.metadata.execution;

import com.sitairis.metadata.LibraryOperation;
import com.sitairis.model.Book;
import com.sitairis.model.Loan;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/** Explicit alternative used to compare the solution with code without reflection. */
public final class DirectLibraryActionExecutor implements LibraryActionExecutor {
    @Override
    public Object execute(Object target, LibraryOperation operation, Object... arguments) {
        Objects.requireNonNull(target, "Целевой объект обязателен.");
        Objects.requireNonNull(operation, "Операция обязательна.");

        if (operation == LibraryOperation.ISSUE_BOOK && target instanceof Book book) {
            requireArgumentCount(arguments, 0, operation);
            book.markAsIssued();
            return null;
        }
        if (operation == LibraryOperation.RETURN_BOOK && target instanceof Loan loan) {
            requireArgumentCount(arguments, 2, operation);
            loan.recordReturn((LocalDate) arguments[0], (BigDecimal) arguments[1]);
            return null;
        }
        if (operation == LibraryOperation.CHECK_DUE_DATE && target instanceof Loan loan) {
            requireArgumentCount(arguments, 1, operation);
            return loan.isOverdue((LocalDate) arguments[0]);
        }
        throw new IllegalArgumentException(
                "Операция '" + operation.getDescription() + "' не поддерживается для "
                        + target.getClass().getSimpleName() + ".");
    }

    private void requireArgumentCount(Object[] arguments, int expected, LibraryOperation operation) {
        if (arguments == null || arguments.length != expected) {
            throw new IllegalArgumentException(
                    "Для операции '" + operation.getDescription() + "' ожидалось аргументов: "
                            + expected + ".");
        }
    }
}
