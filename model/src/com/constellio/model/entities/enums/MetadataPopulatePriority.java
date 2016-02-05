package com.constellio.model.entities.enums;

import static java.util.Arrays.asList;

import java.util.List;

public enum MetadataPopulatePriority {

	STYLES_PROPERTIES_REGEX("styles", "properties", "regex"),
	STYLES_REGEX_PROPERTIES("styles", "regex", "properties"),
	PROPERTIES_STYLES_REGEX("properties", "styles", "regex"),
	PROPERTIES_REGEX_STYLES("properties", "regex", "styles"),
	REGEX_STYLES_PROPERTIES("regex", "styles", "properties"),
	REGEX_PROPERTIES_STYLES("regex", "properties", "styles");

	private List<String> priorities;

	MetadataPopulatePriority(String... priorities) {
		this.priorities = asList(priorities);
	}

	public List<String> getPrioritizedSources() {
		return priorities;
	}
}
