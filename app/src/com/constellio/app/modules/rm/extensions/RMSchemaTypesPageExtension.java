package com.constellio.app.modules.rm.extensions;

import com.constellio.app.api.extensions.SchemaTypesPageExtension;
import com.constellio.app.api.extensions.params.IsBuiltInMetadataAttributeModifiableParam;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;

import static com.constellio.model.entities.schemas.MetadataAttribute.REQUIRED;

public class RMSchemaTypesPageExtension extends SchemaTypesPageExtension {

	@Override
	public ExtensionBooleanResult isBuiltInMetadataAttributeModifiable(
			IsBuiltInMetadataAttributeModifiableParam param) {

		if (param.is(ContainerRecord.SCHEMA_TYPE, ContainerRecord.ADMINISTRATIVE_UNITS) && param.isAttribute(REQUIRED)) {
			return ExtensionBooleanResult.FORCE_TRUE;

		} else if (param.is(ContainerRecord.SCHEMA_TYPE, ContainerRecord.DECOMMISSIONING_TYPE) && param.isAttribute(REQUIRED)) {
			return ExtensionBooleanResult.FORCE_TRUE;

		}

		return ExtensionBooleanResult.NOT_APPLICABLE;
	}
}
