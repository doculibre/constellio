package com.constellio.app.api.extensions.params;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.ModifiableStructure;

public class ConvertStructureToMapParams {

	ModifiableStructure structure;
	Metadata metadata;

	public ConvertStructureToMapParams(ModifiableStructure structure, Metadata metadata) {
		this.structure = structure;
		this.metadata = metadata;
	}

	public ModifiableStructure getStructure() {
		return structure;
	}

	public Metadata getMetadata() {
		return metadata;
	}
}
