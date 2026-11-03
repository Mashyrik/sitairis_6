package com.sitairis.service;

import com.sitairis.model.Book;
import com.sitairis.model.Reader;
import com.sitairis.repository.BookRepository;
import com.sitairis.repository.ReaderRepository;
import java.util.Objects;

/** Fills empty repositories with data used by the console demonstration. */
public final class InitialLibraryDataSeeder {
    private final BookRepository bookRepository;
    private final ReaderRepository readerRepository;

    public InitialLibraryDataSeeder(
            BookRepository bookRepository, ReaderRepository readerRepository) {
        this.bookRepository = Objects.requireNonNull(bookRepository);
        this.readerRepository = Objects.requireNonNull(readerRepository);
    }

    public void seed() {
        if (bookRepository.findAll().isEmpty()) {
            bookRepository.save(new Book(1, "Мастер и Маргарита", "Михаил Булгаков",
                    "978-5-17-118366-6", 1967));
            bookRepository.save(new Book(2, "Чистый код", "Роберт Мартин",
                    "978-5-4461-0960-9", 2008));
            bookRepository.save(new Book(3, "Война и мир", "Лев Толстой",
                    "978-5-389-06256-6", 1869));
        }
        if (readerRepository.findAll().isEmpty()) {
            readerRepository.save(new Reader(1, "Иванов Иван Иванович",
                    "ivanov@example.com", "+375291112233"));
            readerRepository.save(new Reader(2, "Петрова Анна Сергеевна",
                    "petrova@example.com", "+375292223344"));
        }
    }
}
