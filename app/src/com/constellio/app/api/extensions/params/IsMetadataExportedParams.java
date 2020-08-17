package com.constellio.app.api.extensions.params;

import com.constellio.model.entities.schemas.Metadata;

public class IsMetadataExportedParams {
	private Metadata metadata;

	public IsMetadataExportedParams(Metadata metadata) {
		this.metadata = metadata;
	}

	public Metadata getMetadata() {
		return metadata;
	}
}
