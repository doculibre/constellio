package com.constellio.model.services.schemas;

import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import java.io.Serializable;

public interface MetadataSchemaTypesAlteration extends Serializable {

	void alter(MetadataSchemaTypesBuilder types);

}
