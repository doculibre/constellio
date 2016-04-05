package com.constellio.model.services.records.extractions;

import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Majid on 2016-03-29.
 */
public class DefaultMetadataPopulatorFactory implements MetatdataPopulatorFactory{
    private Marshaller marshaller;
    private Unmarshaller unmarshaller;

    public DefaultMetadataPopulatorFactory()  {
    }

    private void init() throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(DefaultMetadataPopulator.class, RegexExtractor.class, MetadataToText.class);
        marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        unmarshaller = jc.createUnmarshaller();
    }

    public Marshaller getMarshaller() throws JAXBException {
        if (marshaller == null)
            init();
        return marshaller;
    }

    public Unmarshaller getUnmarshaller() throws JAXBException {
        if (unmarshaller == null)
            init();
        return unmarshaller;
    }

    @Override
    public synchronized MetadataPopulator createAnInstance(Element xmlElement) throws Exception {
        XMLOutputter xmlOutputter = new XMLOutputter();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        xmlOutputter.output(xmlElement, outputStream);

        return (DefaultMetadataPopulator) getUnmarshaller().unmarshal(new ByteArrayInputStream(outputStream.toByteArray()));

    }

    public synchronized String convertToXml(DefaultMetadataPopulator metadataPopulator) throws JAXBException, IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        getMarshaller().marshal(metadataPopulator, outputStream);
        outputStream.close();

        return new String(outputStream.toByteArray());
    }
}
