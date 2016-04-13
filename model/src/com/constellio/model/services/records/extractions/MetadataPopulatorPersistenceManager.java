package com.constellio.model.services.records.extractions;

import org.jdom2.Element;

/**
 * Created by Majid on 2016-03-29.
 */
public interface MetadataPopulatorPersistenceManager {

    Element toXml(MetadataPopulator metadataPopulator) throws Exception;
    MetadataPopulator fromXML(Element xmlElement) throws Exception;
}
