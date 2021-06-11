package com.constellio.app.modules.rm.services.reports.xml;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.enums.TemplateVersionType;
import com.constellio.app.modules.rm.services.reports.xml.legacy.MetadataXMLDataSourceGenerator;
import com.constellio.app.modules.rm.services.reports.xml.legacy.ReportLegacyXMLDataSourceGenerator;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;

public class XMLDataSourceGeneratorFactory {

	private final RMConfigs rmConfigs;
	private final String collection;
	private final AppLayerFactory appLayerFactory;

	public XMLDataSourceGeneratorFactory(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		rmConfigs = new RMConfigs(appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager());
	}

	public XMLDataSourceGenerator createXMLDataSourceGenerator(String schemaType, XMLDataSourceType xmlDataSourceType) {
		return createXMLDataSourceGenerator(schemaType, xmlDataSourceType, null);
	}

	public XMLDataSourceGenerator createXMLDataSourceGenerator(String schemaType, XMLDataSourceType xmlDataSourceType,
															   TemplateVersionType templateVersionType) {
		switch (xmlDataSourceType) {
			case LABEL:
				return new LabelXMLDataSourceGenerator(collection, appLayerFactory);
			case METADATA:
				return new MetadataXMLDataSourceGenerator(collection, appLayerFactory);
			case REPORT:
				if (schemaType.equals(Document.SCHEMA_TYPE) || schemaType.equals(Folder.SCHEMA_TYPE) || schemaType.equals(Task.SCHEMA_TYPE)) {
					TemplateVersionType currentTemplateVersionType =
							templateVersionType != null ? templateVersionType : rmConfigs.getTemplateVersionForReports();
					switch (currentTemplateVersionType) {
						case CONSTELLIO_5:
							return new ReportLegacyXMLDataSourceGenerator(collection, appLayerFactory);
						case CONSTELLIO_10:
							return new ReportXMLDataSourceGenerator(collection, appLayerFactory);
						default:
							throw new UnsupportedOperationException("Unknown template version type : " + templateVersionType.name());
					}
				}
				return new ReportXMLDataSourceGenerator(collection, appLayerFactory);
			default:
				throw new UnsupportedOperationException("Unknown printable type : " + xmlDataSourceType.name());
		}
	}

}
