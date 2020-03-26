package com.constellio.app.api.extensions.params;

import com.constellio.model.entities.schemas.MetadataSchemaType;

public class IsRecordExportableParams {
	private MetadataSchemaType schemaType;

	public IsRecordExportableParams(MetadataSchemaType schemaType) {
		this.schemaType = schemaType;
	}

	public MetadataSchemaType getSchemaType() {
		return schemaType;
	}
}
