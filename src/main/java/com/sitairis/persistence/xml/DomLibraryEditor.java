package com.sitairis.persistence.xml;

import java.nio.file.Path;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class DomLibraryEditor {
    public static final String NS = "http://sitairis.com/library";

    public void addBook(Path input, Path output, long id, String title, String author,
                        String isbn, int year) {
        Document document = read(input);
        if (findById(document, "book", id) != null) {
            throw new IllegalArgumentException("Книга с ID " + id + " уже существует");
        }
        Element books = (Element) document.getElementsByTagNameNS(NS, "books").item(0);
        Element book = document.createElementNS(NS, "book");
        book.setAttribute("id", String.valueOf(id));
        append(document, book, "title", title);
        append(document, book, "author", author);
        append(document, book, "isbn", isbn);
        append(document, book, "publicationYear", String.valueOf(year));
        append(document, book, "status", "AVAILABLE");
        books.appendChild(book);
        write(document, output);
    }

    public void returnLoan(Path input, Path output, long loanId, String returnDate,
                           String fineAmount) {
        Document document = read(input);
        Element loan = findById(document, "loan", loanId);
        if (loan == null) throw new IllegalArgumentException("Выдача с ID " + loanId + " не найдена");
        if ("RETURNED".equals(childText(loan, "status"))) {
            throw new IllegalStateException("Выдача уже закрыта");
        }
        setOptionalReturnDate(loan, returnDate);
        setChildText(loan, "status", "RETURNED");
        setChildText(loan, "fineAmount", fineAmount);
        long bookId = Long.parseLong(childText(loan, "bookId"));
        Element book = findById(document, "book", bookId);
        if (book != null) setChildText(book, "status", "AVAILABLE");
        write(document, output);
    }

    private Document read(Path path) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            return factory.newDocumentBuilder().parse(path.toFile());
        } catch (Exception exception) {
            throw new IllegalArgumentException("Ошибка DOM-чтения: " + exception.getMessage(), exception);
        }
    }

    private void write(Document document, Path path) {
        try {
            if (path.getParent() != null) java.nio.file.Files.createDirectories(path.getParent());
            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            var transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(new DOMSource(document), new StreamResult(path.toFile()));
        } catch (Exception exception) {
            throw new IllegalStateException("Ошибка DOM-записи: " + exception.getMessage(), exception);
        }
    }

    private Element findById(Document document, String tag, long id) {
        NodeList nodes = document.getElementsByTagNameNS(NS, tag);
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            if (String.valueOf(id).equals(element.getAttribute("id"))) return element;
        }
        return null;
    }

    private void append(Document document, Element parent, String name, String value) {
        Element child = document.createElementNS(NS, name);
        child.setTextContent(value);
        parent.appendChild(child);
    }

    private String childText(Element parent, String name) {
        return parent.getElementsByTagNameNS(NS, name).item(0).getTextContent().trim();
    }

    private void setChildText(Element parent, String name, String value) {
        NodeList nodes = parent.getElementsByTagNameNS(NS, name);
        if (nodes.getLength() > 0) nodes.item(0).setTextContent(value);
        else append(parent.getOwnerDocument(), parent, name, value);
    }

    private void setOptionalReturnDate(Element loan, String value) {
        NodeList nodes = loan.getElementsByTagNameNS(NS, "returnDate");
        if (nodes.getLength() > 0) {
            nodes.item(0).setTextContent(value);
            return;
        }
        Element returnDate = loan.getOwnerDocument().createElementNS(NS, "returnDate");
        returnDate.setTextContent(value);
        loan.insertBefore(returnDate, loan.getElementsByTagNameNS(NS, "status").item(0));
    }
}
