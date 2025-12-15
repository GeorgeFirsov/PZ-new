package ru.mtuci.coursemanagement.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

@RestController
public class XmlController {

    @PostMapping(
            value = "/api/xml/parse",
            consumes = {MediaType.TEXT_XML_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    public String parse(@RequestBody String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();

        f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        f.setFeature("http://xml.org/sax/features/external-general-entities", false);
        f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        f.setXIncludeAware(false);
        f.setExpandEntityReferences(false);

        DocumentBuilder b = f.newDocumentBuilder();
        Document doc = b.parse(new InputSource(new StringReader(xml)));

        if (doc.getDocumentElement() == null) return "";
        return doc.getDocumentElement().getTextContent();
    }
}
