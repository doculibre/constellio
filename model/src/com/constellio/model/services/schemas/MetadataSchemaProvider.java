package com.constellio.model.services.schemas;

import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;

public interface MetadataSchemaProvider {

	MetadataSchema get(byte collectionId, short typeId, short schemaId);

	MetadataSchemaType get(byte collectionId, short typeId);
}
