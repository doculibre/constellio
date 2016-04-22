package com.constellio.app.modules.es.extensions;

import static com.constellio.model.entities.schemas.Schemas.TITLE;

import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class ESRecordExtension extends RecordExtension {

	ESSchemasRecordsServices es;
	MetadataSchemasManager schemasManager;
	CollectionsManager collectionsManager;

	public ESRecordExtension(ESSchemasRecordsServices es) {
		this.es = es;
		this.schemasManager = es.getModelLayerFactory().getMetadataSchemasManager();
		this.collectionsManager = es.getAppLayerFactory().getCollectionsManager();
	}

	@Override
	public void recordInModificationBeforeValidationAndAutomaticValuesCalculation(
			RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent event) {
		if (event.isSchemaType(ConnectorInstance.SCHEMA_TYPE)) {
			Record record = event.getRecord();
			final ConnectorInstance connectorInstance = es.wrapConnectorInstance(record);
			if (record.isModified(TITLE)) {
				schemasManager.modify(es.getCollection(), new MetadataSchemaTypesAlteration() {
					@Override
					public void alter(MetadataSchemaTypesBuilder types) {
						String schema = connectorInstance.getDocumentsCustomSchemaCode();
						for (MetadataSchemaTypeBuilder type : types.getTypes()) {
							if (type.hasSchema(schema)) {
								MetadataSchemaBuilder builder = type.getSchema(schema);
								for (String languageStr : collectionsManager.getCollectionLanguages(es.getCollection())) {
									builder.addLabel(Language.withCode(languageStr),
											connectorInstance.getTitle());
								}
							}
						}
					}
				});
			}
		}
	}

}
