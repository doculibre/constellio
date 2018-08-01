package com.constellio.app.modules.rm;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.configs.SystemConfigurationScript;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMDocumentRetentionRulesScript implements SystemConfigurationScript<Boolean> {
	@Override
	public void onNewCollection(Boolean newValue, String collection, ModelLayerFactory modelLayerFactory) {
		if (Boolean.TRUE.equals(newValue)) {
			setFolderMainCopyRuleRequirement(false, modelLayerFactory, collection);
		}
	}

	private void setFolderMainCopyRuleRequirement(final boolean value, ModelLayerFactory modelLayerFactory,
												  String collection) {
		modelLayerFactory.getMetadataSchemasManager().modify(collection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {

				if (types.hasSchemaType(Folder.SCHEMA_TYPE)) {
					types.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema().get(Folder.MAIN_COPY_RULE)
							.setDefaultRequirement(!value);
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
		setFolderMainCopyRuleRequirement(newValue, modelLayerFactory, collection);
	}
}
