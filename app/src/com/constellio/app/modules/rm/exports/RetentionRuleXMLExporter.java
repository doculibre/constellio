/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.exports;

import static com.constellio.model.entities.schemas.Schemas.CODE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.io.FileUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.SAXException;

import com.constellio.app.modules.rm.exports.RetentionRuleXMLExporterRuntimeException.RetentionRuleXMLExporterRuntimeException_InvalidFile;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.data.io.services.facades.FileService;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class RetentionRuleXMLExporter {

	private static final String W3C_XML_SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema";

	List<RetentionRule> rules;

	File exportFile;

	String collection;

	ModelLayerFactory modelLayerFactory;

	RMSchemasRecordsServices rm;

	public RetentionRuleXMLExporter(List<RetentionRule> rules, File exportFile, String collection,
			ModelLayerFactory modelLayerFactory) {
		this.rules = rules;
		this.exportFile = exportFile;
		this.collection = collection;
		this.modelLayerFactory = modelLayerFactory;
		this.rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
	}

	public void run() {

		Document document = new Document();
		Element rowset = new Element("ROWSET");
		document.setRootElement(rowset);
		for (RetentionRule rule : rules) {

			Element row = new Element("ROW");
			rowset.addContent(row);

			//Element numRegle = new Element("NUMREGLE");
			row.addContent(new Element("NUMREGLE").setText(rule.getCode()));

		}

		XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
		try {
			FileService fileService = modelLayerFactory.getIOServicesFactory().newFileService();
			fileService.replaceFileContent(exportFile, xmlOutput.outputString(document));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static RetentionRuleXMLExporter forAllApprovedRulesInCollection(String collection,
			File exportFile, ModelLayerFactory modelLayerFactory) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		List<Record> records = searchServices.search(new LogicalSearchQuery()
				.setCondition(from(rm.retentionRuleSchemaType()).where(rm.retentionRuleApproved()).isTrue())
				.sortAsc(CODE));

		return new RetentionRuleXMLExporter(rm.wrapRetentionRules(records), exportFile, collection, modelLayerFactory);
	}

	public static void validate(File xmlFile) {

		try {

			File schemaFile = new File(new FoldersLocator().getModuleResourcesFolder("rm"), "RetentionRuleExport.xsd");
			Source xmlFileSource = new StreamSource(xmlFile);
			SchemaFactory schemaFactory = SchemaFactory
					.newInstance(W3C_XML_SCHEMA_NS_URI);
			Schema schema = schemaFactory.newSchema(schemaFile);
			Validator validator = schema.newValidator();
			validator.validate(xmlFileSource);

		} catch (SAXException | IOException e) {
			String content = "";

			try {
				content = FileUtils.readFileToString(xmlFile);
			} catch (IOException e2) {
				e.printStackTrace();
			}

			throw new RetentionRuleXMLExporterRuntimeException_InvalidFile(content, e);
		}
	}
}
