package com.sitairis.metadata.execution;

import com.sitairis.metadata.LibraryOperation;

public interface LibraryActionExecutor {
    Object execute(Object target, LibraryOperation operation, Object... arguments);
}
