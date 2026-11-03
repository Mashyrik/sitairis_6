package com.sitairis.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sitairis.metadata.annotation.EntityMetadata;
import com.sitairis.metadata.annotation.ScenarioAction;
import com.sitairis.model.Book;
import com.sitairis.model.Loan;
import com.sitairis.model.Reader;
import org.junit.jupiter.api.Test;

class LibraryMetadataProcessorTest {
    private final LibraryMetadataProcessor processor = new LibraryMetadataProcessor();

    @Test
    void scansThreeAnnotatedDomainClassesAndTheirOperations() {
        LibraryMetadataRegistry registry = processor.scan(Reader.class, Book.class, Loan.class);

        assertEquals(3, registry.getEntities().size());
        assertEquals("Читатель", registry.requireEntity(Reader.class).name());
        assertEquals("markAsIssued", registry
                .requireOperation(Book.class, LibraryOperation.ISSUE_BOOK).method().getName());
        assertEquals("recordReturn", registry
                .requireOperation(Loan.class, LibraryOperation.RETURN_BOOK).method().getName());
        assertEquals("isOverdue", registry
                .requireOperation(Loan.class, LibraryOperation.CHECK_DUE_DATE).method().getName());
    }

    @Test
    void rejectsScanOfFewerThanThreeClasses() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> processor.scan(Reader.class, Book.class));

        assertTrue(exception.getMessage().contains("минимум три"));
    }

    @Test
    void rejectsClassWithoutEntityAnnotation() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> processor.scan(Reader.class, Book.class, String.class));

        assertTrue(exception.getMessage().contains("не помечен"));
    }

    @Test
    void rejectsNonPublicScenarioMethod() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> processor.scan(Reader.class, Book.class, InvalidEntity.class));

        assertTrue(exception.getMessage().contains("открытым методом экземпляра"));
    }

    @EntityMetadata(name = "Некорректная сущность")
    private static class InvalidEntity {
        @ScenarioAction(LibraryOperation.CHECK_DUE_DATE)
        private boolean hiddenAction() {
            return false;
        }
    }
}
