package com.constellio.app.entities.modules;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class MigrationHelper {
	protected SchemaDisplayConfig order(String collection, AppLayerFactory appLayerFactory, String type,
			SchemaDisplayConfig schema, String... localCodes) {

		MetadataSchemaTypes schemaTypes = appLayerFactory.getModelLayerFactory()
				.getMetadataSchemasManager().getSchemaTypes(collection);

		List<String> visibleMetadataCodes = new ArrayList<>();
		for (String localCode : localCodes) {
			visibleMetadataCodes.add(schema.getSchemaCode() + "_" + localCode);
		}
		List<String> metadataCodes = new ArrayList<>();
		metadataCodes.addAll(visibleMetadataCodes);
		List<String> otherMetadatas = new ArrayList<>();
		List<String> retrievedMetadataCodes;
		if ("form".equals(type)) {
			retrievedMetadataCodes = schema.getFormMetadataCodes();
		} else {
			retrievedMetadataCodes = schema.getDisplayMetadataCodes();
		}
		for (String retrievedMetadataCode : retrievedMetadataCodes) {
			int index = visibleMetadataCodes.indexOf(retrievedMetadataCode);
			if (index != -1) {
				metadataCodes.set(index, retrievedMetadataCode);
			} else if (!schemaTypes.getMetadata(retrievedMetadataCode).isSystemReserved()) {
				otherMetadatas.add(retrievedMetadataCode);
			}
		}
		SchemaDisplayConfig newSchema;
		if ("form".equals(type)) {
			metadataCodes.addAll(otherMetadatas);
			newSchema = schema.withFormMetadataCodes(metadataCodes);

			SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();
			for (String invisible : otherMetadatas) {
				manager.saveMetadata(manager.getMetadata(collection, invisible).withInputType(MetadataInputType.HIDDEN));
			}
		} else {
			newSchema = schema.withDisplayMetadataCodes(metadataCodes);
		}
		return newSchema;
	}
}
