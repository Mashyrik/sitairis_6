package com.sitairis.service;

import com.sitairis.persistence.dto.LibraryData;
import com.sitairis.persistence.json.JacksonLibraryStorage;
import com.sitairis.persistence.xml.DomLibraryEditor;
import com.sitairis.persistence.xml.SaxLibraryAnalyzer;
import com.sitairis.persistence.xml.XmlValidationResult;
import com.sitairis.persistence.xml.XmlValidator;
import com.sitairis.persistence.xml.XsltReportGenerator;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

/** Coordinates XML and JSON operations of the library application. */
public final class LibraryDataExchangeService implements LibraryDataExchange {
    private final Path resources;
    private final Path outputDirectory;

    public LibraryDataExchangeService() {
        this(Path.of("src", "main", "resources", "data"),
                Path.of("target", "library-data-output"));
    }

    public LibraryDataExchangeService(Path resources, Path outputDirectory) {
        this.resources = resources;
        this.outputDirectory = outputDirectory;
    }

    @Override
    public void demonstrate(PrintStream output) {
        try {
            Files.createDirectories(outputDirectory);
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Не удалось создать каталог результатов.", exception);
        }

        Path xml = resources.resolve("library.xml");
        XmlValidator validator = new XmlValidator(resources.resolve("library.xsd"));

        output.println("=== XSD-валидация ===");
        printValidation(output, xml, validator.validate(xml));
        Path invalidEmail = resources.resolve("invalid-email.xml");
        printValidation(output, invalidEmail, validator.validate(invalidEmail));
        Path invalidReference = resources.resolve("invalid-reference.xml");
        printValidation(output, invalidReference, validator.validate(invalidReference));

        output.println("\n=== SAX-поиск и агрегация ===");
        SaxLibraryAnalyzer sax = new SaxLibraryAnalyzer();
        var foundBooks = sax.findBooks(xml, "Java");
        var statistics = sax.aggregateLoans(xml);
        output.println("Найденные книги по запросу \"Java\": " + foundBooks);
        output.println("Всего выдач: " + statistics.total());
        output.println("Активных: " + statistics.active());
        output.println("Просроченных: " + statistics.overdue());
        output.println("Возвращённых: " + statistics.returned());
        output.println("Общая сумма штрафов: " + statistics.totalFine() + " BYN");

        output.println("\n=== DOM-изменение ===");
        Path changedXml = outputDirectory.resolve("library-updated.xml");
        new DomLibraryEditor().returnLoan(xml, changedXml, 4, "2026-11-01", "4.50");
        output.println("Выдача №4 закрыта, документ сохранён в " + changedXml);
        printValidation(output, changedXml, validator.validate(changedXml));

        output.println("\n=== XSLT-отчёт ===");
        Path report = outputDirectory.resolve("library-report.html");
        new XsltReportGenerator().transform(
                xml, resources.resolve("library-report.xsl"), report);
        output.println("HTML-отчёт сохранён в " + report);

        output.println("\n=== Jackson JSON round-trip ===");
        JacksonLibraryStorage json = new JacksonLibraryStorage();
        LibraryData original = json.read(resources.resolve("library.json"));
        Path roundTrip = outputDirectory.resolve("library-roundtrip.json");
        json.write(original, roundTrip);
        LibraryData restored = json.read(roundTrip);
        output.println("Исходный JSON: читателей = " + original.readers().size()
                + ", книг = " + original.books().size()
                + ", выдач = " + original.loans().size());
        output.println("Восстановленный JSON: читателей = " + restored.readers().size()
                + ", книг = " + restored.books().size()
                + ", выдач = " + restored.loans().size());
        if (!original.equals(restored)) {
            throw new IllegalStateException("JSON round-trip не совпал с исходными данными.");
        }
        output.println("JSON round-trip: OK. Файл сохранён в " + roundTrip);
    }

    private void printValidation(
            PrintStream output, Path path, XmlValidationResult result) {
        output.println((result.valid() ? "VALID: " : "INVALID: ") + path);
        if (!result.valid()) {
            output.println("  Причина: " + result.message());
        }
    }
}
