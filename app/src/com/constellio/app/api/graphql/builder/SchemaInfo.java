package com.constellio.app.api.graphql.builder;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class SchemaInfo {
	private String keyword;
	private String type;
	private List<SchemaInfoField> fields;

	@Builder
	@Data
	public static class SchemaInfoField {
		private String name;
		private List<SchemaInfoFieldParameter> parameters;
		private String type;
		private boolean required;
		private boolean multivalue;
	}

	@Builder
	@Data
	public static class SchemaInfoFieldParameter {
		private String name;
		private String type;
		private boolean required;
		private boolean multivalue;
	}

}
