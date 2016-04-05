package com.constellio.model.services.records.extractions;

import org.jdom2.Element;

import javax.xml.bind.JAXBException;
import java.io.IOException;

/**
 * Created by Majid on 2016-03-29.
 */
public interface MetatdataPopulatorFactory {

    MetadataPopulator createAnInstance(Element xmlElement) throws Exception;
}
