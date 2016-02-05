package com.constellio.model.entities.enums;

import static java.util.Arrays.asList;

import java.util.List;

public enum TitleMetadataPopulatePriority {

	STYLES_PROPERTIES_FILENAME("styles", "properties", "filename"),
	STYLES_FILENAME_PROPERTIES("styles", "filename", "properties"),
	PROPERTIES_STYLES_FILENAME("properties", "styles", "filename"),
	PROPERTIES_FILENAME_STYLES("properties", "filename", "styles"),
	FILENAME("filename");

	private List<String> priorities;

	TitleMetadataPopulatePriority(String... priorities) {
		this.priorities = asList(priorities);
	}

	public List<String> getPrioritizedSouces() {
		return priorities;
	}
}
