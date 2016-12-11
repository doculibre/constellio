package com.constellio.model.entities.schemas;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.services.schemas.SchemaUtils;

public class AllowedReferences implements Serializable {

	private final Set<String> allowedSchemas;

	private final String allowedSchemaType;
	private String typeWithAllowedSchemas;

	public AllowedReferences(String allowedSchemaType, Set<String> allowedSchemas) {
		super();
		this.allowedSchemaType = allowedSchemaType;
		if (allowedSchemas != null) {
			this.allowedSchemas = Collections.unmodifiableSet(allowedSchemas);
		} else {
			this.allowedSchemas = null;
		}
	}

	public Set<String> getAllowedSchemas() {
		return allowedSchemas;
	}

	public String getAllowedSchemaType() {
		return allowedSchemaType;
	}

	public boolean isAtLeastOneSchemaAllowed(MetadataSchemaType type) {
		if (allowedSchemas != null) {
			if (isAllowed(type.getDefaultSchema())) {
				return true;
			}
			for (MetadataSchema customSchema : type.getSchemas()) {
				if (allowedSchemas.contains(customSchema.getCode())) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isAllSchemasAllowed(MetadataSchemaType type) {
		if (allowedSchemaType != null) {
			return allowedSchemaType.equals(type.getCode());
		} else {
			boolean allAllowed = isAllowed(type.getDefaultSchema());
			for (MetadataSchema customSchema : type.getSchemas()) {
				allAllowed &= allowedSchemas.contains(customSchema.getCode());
			}
			return allAllowed;
		}
	}

	public boolean isAllowed(MetadataSchema schema) {
		if (allowedSchemas != null && !allowedSchemas.isEmpty()) {
			return allowedSchemas.contains(schema.getCode());
		} else {
			String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(schema.getCode());
			return schemaTypeCode.equals(allowedSchemaType);
		}
	}

	public boolean isAllowed(MetadataSchemaType type) {
		return type.getCode().equals(getTypeWithAllowedSchemas());
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString() {
		return "AllowedReferences [allowedSchemas=" + allowedSchemas + ", allowedSchemaType=" + allowedSchemaType + "]";
	}

	public String getTypeWithAllowedSchemas() {
		if (allowedSchemaType != null) {
			return allowedSchemaType;
		} else {
			for (String allowedSchema : allowedSchemas) {
				return new SchemaUtils().getSchemaTypeCode(allowedSchema);
			}
			return null;
		}
	}
}
