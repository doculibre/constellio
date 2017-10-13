package com.constellio.app.modules.rm.services.sip.xsd;

import com.constellio.model.conf.FoldersLocator;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.JDOMSource;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class XMLDocumentValidator {
	
	private LSResourceResolver xsdResourceResolver = new XSDResourceResolver();

	public void validate(Document jdomDoc, String...schemaFilenames) {
		try {
			SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
			schemaFactory.setResourceResolver(xsdResourceResolver);
			List<Source> schemaSources = new ArrayList<Source>();
			for (String schemaFilename : schemaFilenames) {
				InputStream schemaIn = new FileInputStream(new File(new FoldersLocator().getModuleResourcesFolder("rm"), "SIPArchiveGenerator/" + schemaFilename));
				schemaSources.add(new StreamSource(schemaIn));
			}
			Schema schema = schemaFactory.newSchema(schemaSources.toArray(new Source[0]));
			Validator validator = schema.newValidator();
			validator.validate(new JDOMSource(jdomDoc));
		} catch (SAXException e) {
			XMLOutputter xml = new XMLOutputter();
            // we want to format the xml. This is used only for demonstration.
            // pretty formatting adds extra spaces and is generally not required.
            xml.setFormat(Format.getPrettyFormat());
//            try {
//				xml.output(jdomDoc, System.out);
//			} catch (IOException e1) {
//				e1.printStackTrace();
//			}
			//throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
