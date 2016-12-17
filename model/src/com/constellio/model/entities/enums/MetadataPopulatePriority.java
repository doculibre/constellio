package com.constellio.model.entities.enums;

import static java.util.Arrays.asList;

import java.util.List;

public enum MetadataPopulatePriority {

	STYLES_PROPERTIES_REGEX("styles", "properties", "regex", "plugin"),
	STYLES_REGEX_PROPERTIES("styles", "regex", "properties", "plugin"),
	PROPERTIES_STYLES_REGEX("properties", "styles", "regex", "plugin"),
	PROPERTIES_REGEX_STYLES("properties", "regex", "styles", "plugin"),
	REGEX_STYLES_PROPERTIES("regex", "styles", "properties", "plugin"),
	REGEX_PROPERTIES_STYLES("regex", "properties", "styles", "plugin");

	private List<String> priorities;

	MetadataPopulatePriority(String... priorities) {
		this.priorities = asList(priorities);
	}

	public List<String> getPrioritizedSources() {
		return priorities;
	}
}
