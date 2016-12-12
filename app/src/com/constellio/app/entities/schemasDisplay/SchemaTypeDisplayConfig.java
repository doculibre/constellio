package com.constellio.app.entities.schemasDisplay;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.model.entities.Language;

public class SchemaTypeDisplayConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(SchemaTypeDisplayConfig.class);

	private final String collection;

	private final String schemaType;

	private final boolean manageable;

	private final boolean advancedSearch;

	private final boolean simpleSearch;

	private final Map<String, Map<Language, String>> metadataGroup;

	public SchemaTypeDisplayConfig(String collection, String schemaType, boolean manageable, boolean advancedSearch,
			boolean simpleSearch, Map<String, Map<Language, String>> metadataGroup) {
		this.collection = collection;
		this.schemaType = schemaType;
		this.manageable = manageable;
		this.advancedSearch = advancedSearch;
		this.simpleSearch = simpleSearch;
		this.metadataGroup = Collections.unmodifiableMap(metadataGroup);
	}

	public SchemaTypeDisplayConfig(String collection, String schemaType, Map<String, Map<Language, String>> metadataGroup) {
		this.collection = collection;
		this.schemaType = schemaType;
		this.manageable = false;
		this.advancedSearch = false;
		this.simpleSearch = false;
		this.metadataGroup = Collections.unmodifiableMap(metadataGroup);
	}

	public boolean isManageable() {
		return manageable;
	}

	public boolean isAdvancedSearch() {
		return advancedSearch;
	}

	public boolean isSimpleSearch() {
		return simpleSearch;
	}

	public String getCollection() {
		return collection;
	}

	public String getSchemaType() {
		return schemaType;
	}

	public Map<String, Map<Language, String>> getMetadataGroup() {
		return metadataGroup;
	}

	public SchemaTypeDisplayConfig withManageableStatus(boolean manageable) {
		return new SchemaTypeDisplayConfig(collection, schemaType, manageable, advancedSearch, simpleSearch, metadataGroup);
	}

	public SchemaTypeDisplayConfig withAdvancedSearchStatus(boolean advancedSearch) {
		return new SchemaTypeDisplayConfig(collection, schemaType, manageable, advancedSearch, simpleSearch, metadataGroup);
	}

	public SchemaTypeDisplayConfig withSimpleAndAdvancedSearchStatus(boolean status) {
		return withSimpleSearchStatus(status).withAdvancedSearchStatus(status);
	}

	public SchemaTypeDisplayConfig withSimpleSearchStatus(boolean simpleSearch) {
		return new SchemaTypeDisplayConfig(collection, schemaType, manageable, advancedSearch, simpleSearch, metadataGroup);
	}

	public SchemaTypeDisplayConfig withMetadataGroup(Map<String, Map<Language, String>> metadataGroup) {

		boolean defaultTab = false;
		for (String key : metadataGroup.keySet()) {
			if (key.startsWith("default")) {
				defaultTab = true;
			}
		}
		if (!defaultTab) {
			LOGGER.warn("It is recommended to have a metadata group starting with 'default'");
		}

		return new SchemaTypeDisplayConfig(collection, schemaType, manageable, advancedSearch, simpleSearch, metadataGroup);
	}

	public SchemaTypeDisplayConfig withNewMetadataGroup(Map<String, Map<Language, String>> newGroup) {
		Map<String, Map<Language, String>> groups = new HashMap<>();
		groups.putAll(metadataGroup);
		groups.putAll(newGroup);
		return withMetadataGroup(groups);
	}

	public String getGroupLabel(String group, Language language) {
		if ("".equals(group)) {
			return getDefaultGroup(language);
		} else {
			Map<Language, String> map = metadataGroup.get(group);
			return map == null ? null : map.get(language);
		}
	}

	public String getDefaultGroup(Language language) {
		for (Map.Entry<String, Map<Language, String>> entry : metadataGroup.entrySet()) {
			if (entry.getKey().startsWith("default:")) {
				return entry.getValue().get(language);
			}
		}
		return null;
	}
}
