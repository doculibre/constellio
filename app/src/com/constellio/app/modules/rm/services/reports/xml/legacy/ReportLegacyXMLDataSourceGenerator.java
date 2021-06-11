package com.constellio.app.modules.rm.services.reports.xml.legacy;

import com.constellio.app.modules.rm.services.reports.xml.XMLDataSourceGenerator;
import com.constellio.app.modules.rm.services.reports.xml.XMLDataSourceGeneratorParams;
import com.constellio.app.modules.rm.services.reports.xml.legacy.parameters.XmlReportGeneratorParameters;
import com.constellio.app.services.factories.AppLayerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ReportLegacyXMLDataSourceGenerator implements XMLDataSourceGenerator {

	private final AppLayerFactory appLayerFactory;
	private final String collection;

	public ReportLegacyXMLDataSourceGenerator(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
	}

	@Override
	public InputStream generate(XMLDataSourceGeneratorParams params) {

		XmlReportGeneratorParameters xmlGeneratorParameters = new XmlReportGeneratorParameters(1);
		xmlGeneratorParameters.setElementWithIds(params.getSchemaType(), params.getRecordIds());
		xmlGeneratorParameters.setQuery(params.getQuery());
		if (params.isXmlForTest()) {
			xmlGeneratorParameters.markAsTestXml();
		}

		AbstractXmlGenerator xmlGenerator = new PrintableReportXmlGenerator(appLayerFactory,
				collection, xmlGeneratorParameters, params.getLocale(), params.getUsername());

		String xml = xmlGenerator.generateXML();
		return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
	}

}
