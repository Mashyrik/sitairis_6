package com.sitairis.model;

import com.sitairis.metadata.LibraryOperation;
import com.sitairis.metadata.annotation.EntityMetadata;
import com.sitairis.metadata.annotation.ScenarioAction;
import java.util.Objects;

@EntityMetadata(name = "Книга")
public class Book {
    private final long id;
    private final String title;
    private final String author;
    private final String isbn;
    private final int publicationYear;
    private BookStatus status;

    public Book(long id, String title, String author, String isbn, int publicationYear) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID книги должен быть положительным числом.");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Название книги не может быть пустым.");
        }
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Автор книги не может быть пустым.");
        }
        int currentYear = java.time.Year.now().getValue();
        if (publicationYear < 1450 || publicationYear > currentYear) {
            throw new IllegalArgumentException("Год издания должен быть от 1450 до " + currentYear + ".");
        }
        this.id = id;
        this.title = title.trim();
        this.author = author.trim();
        this.isbn = isbn == null || isbn.isBlank() ? "N/A" : isbn.trim();
        this.publicationYear = publicationYear;
        this.status = BookStatus.AVAILABLE;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public int getPublicationYear() { return publicationYear; }
    public BookStatus getStatus() { return status; }

    public boolean isAvailable() {
        return status == BookStatus.AVAILABLE;
    }

    @ScenarioAction(LibraryOperation.ISSUE_BOOK)
    public void markAsIssued() {
        if (!isAvailable()) {
            throw new IllegalStateException("Книга '" + title + "' уже выдана или недоступна.");
        }
        status = BookStatus.ISSUED;
    }

    public void markAsAvailable() { status = BookStatus.AVAILABLE; }
    public void markAsLost() { status = BookStatus.LOST; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Book book = (Book) object;
        return id == book.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return String.format("[ID: %d] \"%s\" - %s (%d г.), ISBN: %s, Статус: %s",
                id, title, author, publicationYear, isbn, status.getDescription());
    }
}
