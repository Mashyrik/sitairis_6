package com.sitairis.repository.memory;

import com.sitairis.model.Reader;
import com.sitairis.repository.ReaderRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class InMemoryReaderRepository implements ReaderRepository {
    private final Map<Long, Reader> readers = new LinkedHashMap<>();

    @Override
    public void save(Reader reader) {
        if (reader == null) throw new IllegalArgumentException("Читатель не может быть null.");
        if (readers.containsKey(reader.getId())) {
            throw new IllegalArgumentException("Читатель с ID " + reader.getId() + " уже существует.");
        }
        readers.put(reader.getId(), reader);
    }

    @Override public Optional<Reader> findById(long id) { return Optional.ofNullable(readers.get(id)); }
    @Override public List<Reader> findAll() { return new ArrayList<>(readers.values()); }
    @Override public long nextId() {
        return readers.keySet().stream().mapToLong(Long::longValue).max().orElse(0) + 1;
    }
}
