package com.constellio.model.services.schemas;

import java.io.Serializable;

import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public interface MetadataSchemaTypesAlteration extends Serializable {

	void alter(MetadataSchemaTypesBuilder types);

}
