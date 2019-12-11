package com.constellio.app.modules.rm.configScripts;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.configs.AbstractSystemConfigurationScript;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class EnableOrDisableTypeRestrictionInFolderScript extends AbstractSystemConfigurationScript<Boolean> {
	@Override
	public void onNewCollection(Boolean newValue, String collection, ModelLayerFactory modelLayerFactory) {
		onValueChanged(null, newValue, modelLayerFactory, collection);
	}

	@Override
	public void validate(Boolean newValue, ValidationErrors errors) {

	}

	@Override
	public void onValueChanged(Boolean previousValue, Boolean newValue, ModelLayerFactory modelLayerFactory) {
		if (newValue != previousValue) {
			CollectionsListManager collectionManager = modelLayerFactory.getCollectionsListManager();
			for (String collection : collectionManager.getCollectionsExcludingSystem()) {
				onValueChanged(previousValue, newValue, modelLayerFactory, collection);
			}
		}
	}

	@Override
	public void onValueChanged(Boolean previousValue, Boolean newValue, ModelLayerFactory modelLayerFactory,
							   String collection) {
		modelLayerFactory.getMetadataSchemasManager().modify(collection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				MetadataSchemaBuilder folderSchema = types.getDefaultSchema(Folder.SCHEMA_TYPE);
				folderSchema.getMetadata(Folder.ALLOWED_DOCUMENT_TYPES).setSystemReserved(!newValue);
				folderSchema.getMetadata(Folder.ALLOWED_FOLDER_TYPES).setSystemReserved(!newValue);
			}
		});
	}
}
