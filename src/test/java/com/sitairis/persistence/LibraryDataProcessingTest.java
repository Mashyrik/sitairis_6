package com.sitairis.persistence;

import static org.junit.jupiter.api.Assertions.*;

import com.sitairis.persistence.json.JacksonLibraryStorage;
import com.sitairis.persistence.xml.DomLibraryEditor;
import com.sitairis.persistence.xml.SaxLibraryAnalyzer;
import com.sitairis.persistence.xml.XmlValidator;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LibraryDataProcessingTest {
    private final Path resources = Path.of("src", "main", "resources", "data");

    @Test void validatesPositiveAndNegativeDocuments() {
        XmlValidator validator = new XmlValidator(resources.resolve("library.xsd"));
        assertTrue(validator.validate(resources.resolve("library.xml")).valid());
        assertFalse(validator.validate(resources.resolve("invalid-email.xml")).valid());
        assertFalse(validator.validate(resources.resolve("invalid-reference.xml")).valid());
    }

    @Test void performsSaxSearchAndAggregation() {
        SaxLibraryAnalyzer analyzer = new SaxLibraryAnalyzer();
        assertEquals(2, analyzer.findBooks(resources.resolve("library.xml"), "java").size());
        var statistics = analyzer.aggregateLoans(resources.resolve("library.xml"));
        assertEquals(15, statistics.total());
        assertEquals(3, statistics.active());
        assertEquals(2, statistics.overdue());
        assertEquals(10, statistics.returned());
        assertEquals("36.00", statistics.totalFine().setScale(2).toPlainString());
    }

    @Test void changesDocumentWithDomAndKeepsItValid(@TempDir Path tempDir) {
        Path changed = tempDir.resolve("changed.xml");
        new DomLibraryEditor().returnLoan(resources.resolve("library.xml"), changed,
                4, "2026-11-01", "4.50");
        assertTrue(new XmlValidator(resources.resolve("library.xsd")).validate(changed).valid());
        assertTrue(javaRead(changed).contains("<returnDate>2026-11-01</returnDate>"));
    }

    @Test void jacksonRoundTripRestoresEquivalentObject(@TempDir Path tempDir) {
        JacksonLibraryStorage storage = new JacksonLibraryStorage();
        var original = storage.read(resources.resolve("library.json"));
        Path copy = tempDir.resolve("copy.json");
        storage.write(original, copy);
        assertEquals(original, storage.read(copy));
        assertEquals(15, original.readers().size());
        assertEquals(15, original.books().size());
        assertEquals(15, original.loans().size());
    }

    private String javaRead(Path path) {
        try { return java.nio.file.Files.readString(path); }
        catch (java.io.IOException exception) { throw new AssertionError(exception); }
    }
}
