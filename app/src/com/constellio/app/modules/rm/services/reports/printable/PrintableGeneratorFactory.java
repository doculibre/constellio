package com.constellio.app.modules.rm.services.reports.printable;

import com.constellio.app.modules.rm.services.reports.xml.XMLDataSourceType;
import com.constellio.app.services.factories.AppLayerFactory;

public class PrintableGeneratorFactory {

	private final String collection;
	private final AppLayerFactory appLayerFactory;

	public PrintableGeneratorFactory(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
	}

	public PrintableGenerator createPrintableGenerator(XMLDataSourceType xmlDataSourceType) {
		switch (xmlDataSourceType) {
			case REPORT:
				return new ReportPrintableGenerator(collection, appLayerFactory);
			case LABEL:
				return new LabelPrintableGenerator(collection, appLayerFactory);
			case METADATA:
				return new MetadataPrintableGenerator(collection, appLayerFactory);
			default:
				throw new UnsupportedOperationException("Unknown PrintableType : " + xmlDataSourceType.name());
		}
	}

}
