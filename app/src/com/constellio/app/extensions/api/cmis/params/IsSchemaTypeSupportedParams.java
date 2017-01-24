package com.constellio.app.extensions.api.cmis.params;

import com.constellio.model.entities.schemas.MetadataSchemaType;

public class IsSchemaTypeSupportedParams {

	MetadataSchemaType schemaType;

	public IsSchemaTypeSupportedParams(MetadataSchemaType schemaType) {
		this.schemaType = schemaType;
	}

	public MetadataSchemaType getSchemaType() {
		return schemaType;
	}
}
