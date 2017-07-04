package com.constellio.app.api.extensions.params;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;

public interface GetAvailableExtraMetadataAttributesParam {

	Metadata getMetadata();

	MetadataSchema getSchema();

	MetadataSchemaType getSchemaType();

}
