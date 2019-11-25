package com.constellio.app.modules.rm;

import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.model.entities.configs.SystemConfigurationScript;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.model.entities.schemas.RecordCacheType.ONLY_VOLATILE;
import static com.constellio.model.entities.schemas.RecordCacheType.SUMMARY_CACHED_WITH_VOLATILE;

public class RMDocumentSummaryCacheEnabledScript implements SystemConfigurationScript<Boolean> {
	@Override
	public void onNewCollection(Boolean newValue, String collection, ModelLayerFactory modelLayerFactory) {
		setValue(newValue, collection, modelLayerFactory);
	}

	private void setValue(Boolean value, String collection, ModelLayerFactory modelLayerFactory) {
		modelLayerFactory.getMetadataSchemasManager().modify(collection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				if (types.hasSchemaType(Document.SCHEMA_TYPE)) {
					types.getSchemaType(Document.SCHEMA_TYPE).setRecordCacheType(
							Boolean.TRUE.equals(value) ? SUMMARY_CACHED_WITH_VOLATILE : ONLY_VOLATILE);
				}
			}
		});
	}

	@Override
	public void validate(Boolean newValue, ValidationErrors errors) {

	}

	@Override
	public void onValueChanged(Boolean previousValue, Boolean newValue, ModelLayerFactory modelLayerFactory) {
	}

	@Override
	public void onValueChanged(Boolean previousValue, Boolean newValue, ModelLayerFactory modelLayerFactory,
							   String collection) {
		setValue(newValue, collection, modelLayerFactory);
	}
}
