package com.constellio.model.services.records.extractions;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Created by Majid on 2016-03-29.
 */
public class DefaultMetadataPopulatorPersistenceManager implements MetadataPopulatorPersistenceManager {

    @Override
    public Element toXml(MetadataPopulator metadataPopulator) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        XMLEncoder xmlEncoder = new XMLEncoder(buffer);
        xmlEncoder.writeObject(metadataPopulator);
        xmlEncoder.close();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer.toByteArray());

        SAXBuilder saxBuilder = new SAXBuilder();
        Document document = saxBuilder.build(inputStream);
        Element element = document.getRootElement();
        element.detach();

        return element;
    }

    @Override
    public MetadataPopulator fromXML(Element xmlElement) throws Exception {
        XMLOutputter converter = new XMLOutputter();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        converter.output(xmlElement, buffer);
        buffer.close();

        XMLDecoder xmlDecoder = new XMLDecoder(new ByteArrayInputStream(buffer.toByteArray()));
        return (MetadataPopulator) xmlDecoder.readObject();
    }
}
