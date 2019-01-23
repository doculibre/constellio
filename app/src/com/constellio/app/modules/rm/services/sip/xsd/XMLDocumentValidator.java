package com.constellio.app.modules.rm.services.sip.xsd;

import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.frameworks.validation.ValidationErrors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jdom2.Document;
import org.jdom2.transform.JDOMSource;
import org.xml.sax.SAXException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XMLDocumentValidator {

	private static String INVALID_XML_FILE = "invalidXmlFile";


	private static Map<String, Schema> schemas = new HashMap<>();

	public void validate(String filePath, Document document, ValidationErrors errors, List<String> schemaFilenames) {

		try {
			Schema schema = getXMLSchema(schemaFilenames);

			Validator validator = schema.newValidator();
			validator.validate(new JDOMSource(document));
		} catch (SAXException e) {

			Map<String, Object> params = new HashMap<>();
			params.put("file", filePath);
			params.put("schemas", StringUtils.join(schemaFilenames));
			params.put("exception", ExceptionUtils.getStackTrace(e));

			errors.add(XMLDocumentValidator.class, INVALID_XML_FILE, params);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected Schema getXMLSchema(List<String> schemaFilenames) throws SAXException {
		String key = StringUtils.join(schemaFilenames);
		Schema schema = schemas.get(key);

		if (schema == null) {
			try {
				SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
				schemaFactory.setResourceResolver(new XSDResourceResolver());
				List<Source> schemaSources = new ArrayList<Source>();
				List<InputStream> inputStreams = new ArrayList<>();
				try {
					for (String schemaFilename : schemaFilenames) {
						InputStream schemaFileInputStream = null;

						schemaFileInputStream = new FileInputStream(new File(new FoldersLocator().getModuleResourcesFolder("rm"), "SIPArchiveGenerator/" + schemaFilename));

						inputStreams.add(schemaFileInputStream);
						schemaSources.add(new StreamSource(schemaFileInputStream));
					}
					schema = schemaFactory.newSchema(schemaSources.toArray(new Source[0]));
				} finally {
//					for (InputStream in : inputStreams) {
					//						IOUtils.closeQuietly(in);
					//					}
				}
				//schemas.put(key, schema);
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		return schema;
	}

}
