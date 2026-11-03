package com.sitairis.repository;

import com.sitairis.model.Book;
import java.util.List;
import java.util.Optional;

public interface BookRepository {
    void save(Book book);
    Optional<Book> findById(long id);
    List<Book> findAll();
    long nextId();
}
