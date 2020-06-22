package com.constellio.app.services.sip.xsd;

import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.model.frameworks.validation.ValidationErrors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class XMLDocumentValidator {

	private static final String INVALID_XML_FILE = "invalidXmlFile";


	private static Map<String, Schema> schemas = new HashMap<>();

	public void validate(String filePath, Document document, ValidationErrors errors, List<String> schemaFilenames) {

		try {
			Schema schema = getXMLSchema(schemaFilenames);

			Validator validator = schema.newValidator();
			validator.validate(new JDOMSource(document));
		} catch (SAXException e) {

			StringWriter stringWriter = new StringWriter();

			XMLOutputter output = new XMLOutputter();
			output.setFormat(Format.getPrettyFormat());
			try {
				output.output(document, stringWriter);
			} catch (IOException e1) {
				throw new ImpossibleRuntimeException(e1);
			}

			Map<String, Object> params = new HashMap<>();
			params.put("file", filePath);
			params.put("schemas", StringUtils.join(schemaFilenames));
			params.put("exception", ExceptionUtils.getStackTrace(e));
			params.put("xml", stringWriter.toString());

			errors.add(XMLDocumentValidator.class, INVALID_XML_FILE, params);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void validate(Document document, List<String> schemaFilenames)
			throws XMLDocumentValidatorException {

		try {
			Schema schema = getXMLSchema(schemaFilenames);

			Validator validator = schema.newValidator();
			validator.validate(new JDOMSource(document));
		} catch (SAXException e) {


			StringWriter stringWriter = new StringWriter();

			XMLOutputter output = new XMLOutputter();
			output.setFormat(Format.getPrettyFormat());
			try {
				output.output(document, stringWriter);
			} catch (IOException e1) {
				throw new ImpossibleRuntimeException(e1);
			}

			throw new XMLDocumentValidatorException(stringWriter.toString(), schemaFilenames, e);

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
				for (String schemaFilename : schemaFilenames) {
					InputStream schemaFileInputStream = new FileInputStream(new File(new FoldersLocator().getModuleResourcesFolder("rm"), "SIPArchiveGenerator/" + schemaFilename));
					inputStreams.add(schemaFileInputStream);
					schemaSources.add(new StreamSource(schemaFileInputStream));
				}
				schema = schemaFactory.newSchema(schemaSources.toArray(new Source[0]));
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		return schema;
	}

	public static void main(String[] args) throws Exception {
		String path = args[0];
		List<String> xsds = new ArrayList<>(asList(args));
		xsds.remove(0);
		XMLDocumentValidator validator = new XMLDocumentValidator();

		Document document = new SAXBuilder().build(new File(path));
		validator.validate(document, xsds);
	}
}
