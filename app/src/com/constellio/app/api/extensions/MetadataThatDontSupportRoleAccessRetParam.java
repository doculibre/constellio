package com.constellio.app.api.extensions;

public class MetadataThatDontSupportRoleAccessRetParam {
	private String metadataSchemaType;
	private String metadataSchema;
	private String metadataCode;

	public MetadataThatDontSupportRoleAccessRetParam(String metadataSchemaType, String metadataSchema, String metadataCode) {
		this.metadataSchemaType = metadataSchemaType;
		this.metadataSchema = metadataSchema;
		this.metadataCode = metadataCode;
	}

	public String getMetadataSchemaType() {
		return metadataSchemaType;
	}

	public String getMetadataSchema() {
		return metadataSchema;
	}

	public String getMetadataCode() {
		return metadataCode;
	}
}
