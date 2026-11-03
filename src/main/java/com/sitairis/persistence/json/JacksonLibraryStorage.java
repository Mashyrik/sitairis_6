package com.sitairis.persistence.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sitairis.persistence.dto.LibraryData;
import java.io.IOException;
import java.nio.file.Path;

public class JacksonLibraryStorage {
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.INDENT_OUTPUT);

    public LibraryData read(Path path) {
        try { return mapper.readValue(path.toFile(), LibraryData.class); }
        catch (IOException exception) {
            throw new IllegalArgumentException("Ошибка чтения JSON: " + exception.getMessage(), exception);
        }
    }

    public void write(LibraryData data, Path path) {
        try {
            if (path.getParent() != null) java.nio.file.Files.createDirectories(path.getParent());
            mapper.writeValue(path.toFile(), data);
        } catch (IOException exception) {
            throw new IllegalStateException("Ошибка записи JSON: " + exception.getMessage(), exception);
        }
    }
}
