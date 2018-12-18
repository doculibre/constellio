package com.constellio.app.modules.rm.extensions.params;

import com.constellio.model.entities.schemas.Metadata;

public class RMSchemaTypesPageExtensionExclusionByPropertyParams {
	private Metadata metadata;

	public RMSchemaTypesPageExtensionExclusionByPropertyParams(Metadata metadata) {
		this.metadata = metadata;
	}

	public Metadata getMetadata() {
		return metadata;
	}
}
