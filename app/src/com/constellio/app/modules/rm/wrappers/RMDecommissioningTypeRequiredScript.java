package com.constellio.app.modules.rm.wrappers;

import com.constellio.model.entities.configs.SystemConfigurationScript;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMDecommissioningTypeRequiredScript implements SystemConfigurationScript<Boolean> {
	@Override
	public void onNewCollection(Boolean newValue, String collection, ModelLayerFactory modelLayerFactory) {
		if (Boolean.TRUE.equals(newValue)) {
			setContainerDecommissioningTypeRequirement(true, modelLayerFactory, collection);
		} else {
			setContainerDecommissioningTypeRequirement(false, modelLayerFactory, collection);
		}
	}

	private void setContainerDecommissioningTypeRequirement(final boolean value, ModelLayerFactory modelLayerFactory, String collection) {
		modelLayerFactory.getMetadataSchemasManager().modify(collection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {

				if (types.hasSchemaType(ContainerRecord.SCHEMA_TYPE)) {
					types.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).get(ContainerRecord.DECOMMISSIONING_TYPE)
							.setDefaultRequirement(value);
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
	public void onValueChanged(Boolean previousValue, Boolean newValue, ModelLayerFactory modelLayerFactory, String collection) {
		setContainerDecommissioningTypeRequirement(newValue, modelLayerFactory, collection);
	}
}
