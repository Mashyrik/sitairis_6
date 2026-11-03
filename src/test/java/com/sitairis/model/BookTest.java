package com.sitairis.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Year;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BookTest {
    @Test
    void constructorCreatesAvailableBook() {
        Book book = new Book(1, "Чистый код", "Роберт Мартин", "ISBN-1", 2008);

        assertEquals(1, book.getId());
        assertEquals("Чистый код", book.getTitle());
        assertEquals(BookStatus.AVAILABLE, book.getStatus());
        assertTrue(book.isAvailable());
    }

    @ParameterizedTest(name = "Пустое название [{0}] отклоняется")
    @ValueSource(strings = {"", " ", "\t", "\n"})
    void constructorRejectsBlankTitles(String title) {
        assertThrows(IllegalArgumentException.class,
                () -> new Book(1, title, "Автор", "ISBN", 2020));
    }

    @Test
    void constructorRejectsNonPositiveId() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new Book(0, "Книга", "Автор", "ISBN", 2020));

        assertTrue(exception.getMessage().contains("ID"));
    }

    @Test
    void constructorRejectsFuturePublicationYear() {
        int futureYear = Year.now().getValue() + 1;

        assertThrows(IllegalArgumentException.class,
                () -> new Book(1, "Книга", "Автор", "ISBN", futureYear));
    }

    @Test
    void markAsIssuedChangesAvailability() {
        Book book = book();

        book.markAsIssued();

        assertEquals(BookStatus.ISSUED, book.getStatus());
        assertFalse(book.isAvailable());
    }

    @Test
    void issuingBookTwiceThrowsException() {
        Book book = book();
        book.markAsIssued();

        assertThrows(IllegalStateException.class, book::markAsIssued);
    }

    @Test
    void bookCanMoveBetweenLostAndAvailableStates() {
        Book book = book();

        book.markAsLost();
        assertEquals(BookStatus.LOST, book.getStatus());
        assertFalse(book.isAvailable());

        book.markAsAvailable();
        assertTrue(book.isAvailable());
    }

    private Book book() {
        return new Book(1, "Книга", "Автор", "ISBN", 2020);
    }
}
