package com.sitairis.repository;

import com.sitairis.model.Reader;
import java.util.List;
import java.util.Optional;

public interface ReaderRepository {
    void save(Reader reader);
    Optional<Reader> findById(long id);
    List<Reader> findAll();
    long nextId();
}
