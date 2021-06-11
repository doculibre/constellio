package com.constellio.app.modules.rm.services.reports.xml.legacy;

import com.constellio.app.modules.rm.services.reports.xml.XMLDataSourceGenerator;
import com.constellio.app.modules.rm.services.reports.xml.XMLDataSourceGeneratorParams;
import com.constellio.app.modules.rm.services.reports.xml.legacy.parameters.XmlReportGeneratorParameters;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordServices;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MetadataXMLDataSourceGenerator implements XMLDataSourceGenerator {

	private final AppLayerFactory appLayerFactory;
	private final String collection;
	private final RecordServices recordServices;

	public MetadataXMLDataSourceGenerator(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
	}

	@Override
	public InputStream generate(XMLDataSourceGeneratorParams params) {
		XmlReportGeneratorParameters xmlGeneratorParameters = new XmlReportGeneratorParameters(1);
		if (params.getSchemaType() != null) {
			xmlGeneratorParameters.setElementWithIds(params.getSchemaType(), params.getRecordIds());
		} else {
			xmlGeneratorParameters.setRecordsElements(getRecordsFromIds(params.getRecordIds()));
		}
		if (params.isXmlForTest()) {
			xmlGeneratorParameters.markAsTestXml();
		}
		XmlReportGenerator xmlReportGenerator =
				new XmlReportGenerator(appLayerFactory, collection, xmlGeneratorParameters, params.getLocale());

		String xml = xmlReportGenerator.generateXML();
		return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
	}

	private Record[] getRecordsFromIds(List<String> recordIds) {
		List<Record> recordList = new ArrayList<>();
		for (String recordId : recordIds) {
			recordList.add(recordServices.getDocumentById(recordId));
		}
		return recordList.toArray(new Record[0]);
	}

}
