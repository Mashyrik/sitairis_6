package com.sitairis.persistence.xml;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class SaxLibraryAnalyzer {
    public List<String> findBooks(Path xmlPath, String query) {
        String normalized = query == null ? "" : query.trim().toLowerCase();
        BookSearchHandler handler = new BookSearchHandler(normalized);
        parse(xmlPath, handler);
        return handler.matches;
    }

    public LoanStatistics aggregateLoans(Path xmlPath) {
        LoanAggregationHandler handler = new LoanAggregationHandler();
        parse(xmlPath, handler);
        return new LoanStatistics(handler.total, handler.active, handler.overdue,
                handler.returned, handler.totalFine);
    }

    private void parse(Path xmlPath, DefaultHandler handler) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.newSAXParser().parse(xmlPath.toFile(), handler);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Ошибка SAX-обработки: " + exception.getMessage(), exception);
        }
    }

    private static final class BookSearchHandler extends DefaultHandler {
        private final String query;
        private final List<String> matches = new ArrayList<>();
        private final StringBuilder text = new StringBuilder();
        private boolean inBook;
        private String title;
        private String author;

        private BookSearchHandler(String query) { this.query = query; }

        @Override public void startElement(String uri, String localName, String qName, Attributes attributes) {
            text.setLength(0);
            if ("book".equals(localName)) { inBook = true; title = null; author = null; }
        }
        @Override public void characters(char[] ch, int start, int length) { text.append(ch, start, length); }
        @Override public void endElement(String uri, String localName, String qName) {
            if (inBook && "title".equals(localName)) title = text.toString().trim();
            if (inBook && "author".equals(localName)) author = text.toString().trim();
            if ("book".equals(localName)) {
                String value = (title + " " + author).toLowerCase();
                if (value.contains(query)) matches.add(title + " — " + author);
                inBook = false;
            }
            text.setLength(0);
        }
    }

    private static final class LoanAggregationHandler extends DefaultHandler {
        private final StringBuilder text = new StringBuilder();
        private int total;
        private int active;
        private int overdue;
        private int returned;
        private BigDecimal totalFine = BigDecimal.ZERO;

        @Override public void startElement(String uri, String localName, String qName, Attributes attributes) {
            text.setLength(0);
            if ("loan".equals(localName)) total++;
        }
        @Override public void characters(char[] ch, int start, int length) { text.append(ch, start, length); }
        @Override public void endElement(String uri, String localName, String qName) {
            String value = text.toString().trim();
            if ("status".equals(localName)) {
                switch (value) {
                    case "ACTIVE" -> active++;
                    case "OVERDUE" -> overdue++;
                    case "RETURNED" -> returned++;
                    default -> { }
                }
            } else if ("fineAmount".equals(localName) && !value.isEmpty()) {
                totalFine = totalFine.add(new BigDecimal(value));
            }
            text.setLength(0);
        }
    }
}
