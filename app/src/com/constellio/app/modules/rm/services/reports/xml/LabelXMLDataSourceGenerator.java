package com.constellio.app.modules.rm.services.reports.xml;

import com.constellio.app.modules.rm.services.reports.xml.legacy.LabelXmlGenerator;
import com.constellio.app.modules.rm.services.reports.xml.legacy.parameters.XmlReportGeneratorParameters;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordServices;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

class LabelXMLDataSourceGenerator implements XMLDataSourceGenerator {

	private final AppLayerFactory appLayerFactory;
	private final String collection;
	private final RecordServices recordServices;

	public LabelXMLDataSourceGenerator(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
	}

	@Override
	public InputStream generate(XMLDataSourceGeneratorParams params) {
		LabelXmlGenerator reportXmlGenerator = new LabelXmlGenerator(collection, appLayerFactory, params.getLocale(), params.getUsername());
		reportXmlGenerator.setStartingPosition(params.getStartingPosition());
		reportXmlGenerator.setElements(getRecordsFromIds(params.getRecordIds()));
		reportXmlGenerator.setNumberOfCopies(params.getNumberOfCopies());
		if (params.isXmlForTest()) {
			reportXmlGenerator.setXmlGeneratorParameters(new XmlReportGeneratorParameters().markAsTestXml());
		}

		String xml = reportXmlGenerator.generateXML();
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
