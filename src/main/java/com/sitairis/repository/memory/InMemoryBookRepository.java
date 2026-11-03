package com.sitairis.repository.memory;

import com.sitairis.model.Book;
import com.sitairis.repository.BookRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class InMemoryBookRepository implements BookRepository {
    private final Map<Long, Book> books = new LinkedHashMap<>();

    @Override
    public void save(Book book) {
        if (book == null) throw new IllegalArgumentException("Книга не может быть null.");
        if (books.containsKey(book.getId())) {
            throw new IllegalArgumentException("Книга с ID " + book.getId() + " уже существует.");
        }
        books.put(book.getId(), book);
    }

    @Override public Optional<Book> findById(long id) { return Optional.ofNullable(books.get(id)); }
    @Override public List<Book> findAll() { return new ArrayList<>(books.values()); }
    @Override public long nextId() {
        return books.keySet().stream().mapToLong(Long::longValue).max().orElse(0) + 1;
    }
}
