package com.constellio.model.entities.records;

import com.constellio.model.entities.Language;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParsedContent {

	private String parsedContent;

	private String language;

	private String mimeType;

	private Map<String, String> normalizedPropertyNames;
	private Map<String, Object> properties;
	private Map<String, List<String>> styles;
	private long length;

	private String description;

	private String title;

	private Dimension dimension;

	public ParsedContent(String parsedContent, String language, String mimeType, long length,
						 Map<String, Object> properties, Map<String, List<String>> styles,
						 Dimension dimension) {
		this.parsedContent = parsedContent;
		this.language = language;
		this.mimeType = mimeType;
		this.length = length;
		this.properties = Collections.unmodifiableMap(properties);
		this.normalizedPropertyNames = normalizePropertyNames(properties);
		this.styles = Collections.unmodifiableMap(styles);
		this.dimension = dimension;
	}

	private Map<String, String> normalizePropertyNames(Map<String, Object> properties) {

		Map<String, String> normalizedProperties = new HashMap<>();

		for (String property : properties.keySet()) {
			normalizedProperties.put(normalize(property), property);
		}

		return normalizedProperties;
	}

	private String normalize(String property) {
		return property.toLowerCase().replace("list:", "").replace("dc:", "");
	}

	public static ParsedContent unparsable(String mimeType, long length) {
		Map<String, Object> emptyPropertiesMap = Collections.emptyMap();
		Map<String, List<String>> emptyStylesMap = Collections.emptyMap();
		return new ParsedContent("", Language.UNKNOWN.getCode(), mimeType, length, emptyPropertiesMap, emptyStylesMap, new Dimension());
	}

	public String getParsedContent() {
		return parsedContent;
	}

	public String getLanguage() {
		return language;
	}

	public String getMimeType() {
		return mimeType;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	public Map<String, List<String>> getStyles() {
		return styles;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public Object getNormalizedProperty(String normalizedProperty) {
		String property = normalizedPropertyNames.get(normalizedProperty.toLowerCase());
		return property == null ? null : properties.get(property);
	}

	public String getMimetypeWithoutCharset() {
		if (mimeType != null && mimeType.indexOf(";") != -1) {
			return mimeType.substring(0, mimeType.indexOf(";"));
		} else {
			return mimeType;
		}
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Dimension getDimension() {
		return dimension;
	}
}
