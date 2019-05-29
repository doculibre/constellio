package com.constellio.model.services.schemas;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;

public interface MetadataSchemaProvider {

	MetadataSchema get(byte collectionId, short typeId, short schemaId);

	Metadata getMetadata(byte collectionId, short typeId, short metadataId);

	MetadataSchemaType get(byte collectionId, short typeId);
}
