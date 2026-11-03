package com.sitairis.persistence.xml;

import java.nio.file.Path;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.xml.sax.SAXException;

public class XmlValidator {
    private final Schema schema;

    public XmlValidator(Path xsdPath) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            schema = factory.newSchema(xsdPath.toFile());
        } catch (SAXException exception) {
            throw new IllegalArgumentException("Не удалось загрузить XSD: " + exception.getMessage(), exception);
        }
    }

    public XmlValidationResult validate(Path xmlPath) {
        try {
            var validator = schema.newValidator();
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            validator.validate(new StreamSource(xmlPath.toFile()));
            return new XmlValidationResult(true, "Документ соответствует XSD");
        } catch (Exception exception) {
            return new XmlValidationResult(false, exception.getMessage());
        }
    }
}
