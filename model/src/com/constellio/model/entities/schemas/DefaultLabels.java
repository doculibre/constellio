package com.constellio.model.entities.schemas;

import com.constellio.model.entities.Language;

import java.util.HashMap;
import java.util.Map;

public class DefaultLabels {

	Map<String, DefaultSchemaTypeLabels> defaultSchemaTypeLabels = new HashMap<>();

	public String getDefaultSchemaTypeLabel(String schemaType, Language language) {
		return null;
	}

	public String getDefaultSchemaLabel(String schemaType, String schema, Language language) {
		return null;
	}

	public String getDefaultMetadataLabel(String schemaType, String metadata, Language language) {
		return null;
	}

	private static class DefaultSchemaTypeLabels {
		Map<Language, String> defaultSchemaTypeLabels = new HashMap<>();
		Map<String, Map<Language, String>> defaultSchemasLabels = new HashMap<>();
		Map<String, Map<Language, String>> defaultMetadataLabels = new HashMap<>();

	}

}
