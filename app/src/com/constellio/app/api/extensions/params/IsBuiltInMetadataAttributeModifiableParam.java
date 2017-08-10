package com.constellio.app.api.extensions.params;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataAttribute;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;

public abstract class IsBuiltInMetadataAttributeModifiableParam {

	public abstract Metadata getMetadata();

	public abstract MetadataSchema getSchema();

	public abstract MetadataSchemaType getSchemaType();

	public abstract MetadataAttribute getMetadataAttribute();

	public boolean is(String typeCode, String metadataCode) {
		return getSchemaType().getCode().equals(typeCode) && getMetadata().getLocalCode().equals(metadataCode);
	}

	public boolean isAttribute(MetadataAttribute attribute) {
		return getMetadataAttribute().equals(attribute);
	}

}
