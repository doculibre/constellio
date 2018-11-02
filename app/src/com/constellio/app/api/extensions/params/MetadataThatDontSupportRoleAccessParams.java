package com.constellio.app.api.extensions.params;

public class MetadataThatDontSupportRoleAccessParams {
	private String schema;

	public MetadataThatDontSupportRoleAccessParams(String schema) {
		this.schema = schema;
	}

	public String getSchema() {
		return schema;
	}
}
