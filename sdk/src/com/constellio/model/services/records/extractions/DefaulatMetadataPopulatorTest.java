package com.constellio.model.services.records.extractions;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Majid on 2016-03-29.
 */

public class DefaulatMetadataPopulatorTest {
    @Test
    public void whenSavingRegexMetadataPopulatorIntoXMLAndLoadingItBackThenTheyAreEqual() throws Exception {
        MetadataPopulatorPersistenceManager metadataPopulatorXMLSerializer = new DefaultMetadataPopulatorPersistenceManager();

        DefaultMetadataPopulator metadataPopulator = new DefaultMetadataPopulator(
                new RegexExtractor("regex pattern", false, "toReplaceValue"), new MetadataToText("test"));
        XMLOutputter converter = new XMLOutputter();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        converter.output(metadataPopulatorXMLSerializer.toXml(metadataPopulator), buffer);
        String xml = new String(buffer.toByteArray());
        System.out.println(xml);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        //parsing xml as we do in com.constellio.model.services.schemas.xml.MetadataSchemaXMLWriter2
        SAXBuilder saxBuilder = new SAXBuilder();
        Document document = saxBuilder.build(inputStream);
        Element element = document.getRootElement();

        Element populateConfigsElement = new Element("populateConfigs");
        element.detach();
        populateConfigsElement.addContent(element);

        MetadataPopulator returnedObject = new DefaultMetadataPopulatorPersistenceManager().fromXML(element);

        assertThat(returnedObject).isEqualTo(metadataPopulator);
    }
}
