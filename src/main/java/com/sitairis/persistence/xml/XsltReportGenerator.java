package com.sitairis.persistence.xml;

import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.XMLConstants;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class XsltReportGenerator {
    public void transform(Path xml, Path xslt, Path html) {
        try {
            if (html.getParent() != null) Files.createDirectories(html.getParent());
            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            var transformer = factory.newTransformer(new StreamSource(xslt.toFile()));
            transformer.transform(new StreamSource(xml.toFile()), new StreamResult(html.toFile()));
        } catch (Exception exception) {
            throw new IllegalStateException("Ошибка XSLT-преобразования: " + exception.getMessage(), exception);
        }
    }
}
