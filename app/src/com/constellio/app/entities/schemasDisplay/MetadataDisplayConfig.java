package com.constellio.app.entities.schemasDisplay;

import com.constellio.app.entities.schemasDisplay.enums.MetadataDisplayType;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.model.entities.Language;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MetadataDisplayConfig implements Serializable {

	private final String metadataCode;

	private final String collection;

	private final boolean visibleInAdvancedSearch;

	private final MetadataInputType inputType;

	private final MetadataDisplayType displayType;

	private final String metadataGroupCode;

	private final boolean highlight;

	private final Map<Language, String> helpMessages;

	public MetadataDisplayConfig(String collection, String metadataCode, boolean visibleInAdvancedSearch,
								 MetadataInputType inputType, boolean highlight, String metadataGroupCode,
								 MetadataDisplayType displayType, Map<Language, String> helpMessages) {
		this.collection = collection;
		this.metadataCode = metadataCode;
		this.visibleInAdvancedSearch = visibleInAdvancedSearch;
		this.inputType = inputType;
		this.highlight = highlight;
		this.metadataGroupCode = metadataGroupCode;
		this.displayType = displayType;
		this.helpMessages = helpMessages == null ? new HashMap<>() : helpMessages;
	}

	public boolean isVisibleInAdvancedSearch() {
		return visibleInAdvancedSearch;
	}

	public boolean isHighlight() {
		return highlight;
	}

	public MetadataInputType getInputType() {
		return inputType;
	}

	public MetadataDisplayType getDisplayType() {
		return displayType;
	}

	public String getMetadataCode() {
		return metadataCode;
	}

	public String getCollection() {
		return collection;
	}

	public String getMetadataGroupCode() {
		return metadataGroupCode;
	}

	public String getHelpMessage(Language language) {
		String helpMessage = helpMessages.get(language);

		return helpMessage == null ? "" : helpMessage;
	}

	public String getFrenchHelpMessage() {
		return getHelpMessage(Language.French);
	}

	public String getEnglishHelpMessage() {
		return getHelpMessage(Language.English);
	}

	public Map<Language, String> getHelpMessages() {
		return helpMessages;
	}

	public MetadataDisplayConfig withVisibleInAdvancedSearchStatus(boolean visibleInAdvancedSearch) {
		return new MetadataDisplayConfig(collection, metadataCode, visibleInAdvancedSearch, inputType, highlight,
				metadataGroupCode, displayType, helpMessages);
	}

	public MetadataDisplayConfig withHighlightStatus(boolean highlight) {
		return new MetadataDisplayConfig(collection, metadataCode, visibleInAdvancedSearch, inputType, highlight,
				metadataGroupCode, displayType, helpMessages);
	}

	public MetadataDisplayConfig withInputType(MetadataInputType inputType) {
		return new MetadataDisplayConfig(collection, metadataCode, visibleInAdvancedSearch, inputType, highlight,
				metadataGroupCode, displayType, helpMessages);
	}

	public MetadataDisplayConfig withDisplayType(MetadataDisplayType displayType) {
		return new MetadataDisplayConfig(collection, metadataCode, visibleInAdvancedSearch, inputType, highlight,
				metadataGroupCode, displayType, helpMessages);
	}

	public MetadataDisplayConfig withMetadataGroup(String metadataGroupCode) {
		return new MetadataDisplayConfig(collection, metadataCode, visibleInAdvancedSearch, inputType, highlight,
				metadataGroupCode, displayType, helpMessages);
	}

	public static MetadataDisplayConfig inheriting(String metadataCode, MetadataDisplayConfig inheritance) {
		return new MetadataDisplayConfig(inheritance.collection, metadataCode, inheritance.visibleInAdvancedSearch,
				inheritance.inputType, inheritance.highlight, inheritance.metadataGroupCode, inheritance.displayType, inheritance.helpMessages);
	}

	public MetadataDisplayConfig withCode(String code) {
		return new MetadataDisplayConfig(collection, code, visibleInAdvancedSearch, inputType, highlight,
				metadataGroupCode, displayType, helpMessages);
	}

	public MetadataDisplayConfig withHelpMessages(Map<Language, String> helpMessages) {
		return new MetadataDisplayConfig(collection, metadataCode, visibleInAdvancedSearch, inputType, highlight,
				metadataGroupCode, displayType, helpMessages);
	}

	public MetadataDisplayConfig withFrenchHelpMessage(String frenchHelpMessage) {
		Map<Language, String> helpMessages = new HashMap<>(this.helpMessages);
		helpMessages.put(Language.French, frenchHelpMessage);

		return new MetadataDisplayConfig(collection, metadataCode, visibleInAdvancedSearch, inputType, highlight,
				metadataGroupCode, displayType, helpMessages);
	}

	public MetadataDisplayConfig withEnglishHelpMessage(String englishHelpMessage) {
		Map<Language, String> helpMessages = new HashMap<>(this.helpMessages);
		helpMessages.put(Language.English, englishHelpMessage);

		return new MetadataDisplayConfig(collection, metadataCode, visibleInAdvancedSearch, inputType, highlight,
				metadataGroupCode, displayType, helpMessages);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof MetadataDisplayConfig)) {
			return false;
		}

		MetadataDisplayConfig that = (MetadataDisplayConfig) o;

		if (visibleInAdvancedSearch != that.visibleInAdvancedSearch) {
			return false;
		}
		if (highlight != that.highlight) {
			return false;
		}
		if (metadataCode != null ? !metadataCode.equals(that.metadataCode) : that.metadataCode != null) {
			return false;
		}
		if (collection != null ? !collection.equals(that.collection) : that.collection != null) {
			return false;
		}
		if (inputType != that.inputType) {
			return false;
		}
		if (displayType != that.displayType) {
			return false;
		}
		if (!this.helpMessages.equals(that.helpMessages)) {
			return false;
		}

		return metadataGroupCode != null ? metadataGroupCode.equals(that.metadataGroupCode) : that.metadataGroupCode == null;
	}

	@Override
	public int hashCode() {
		int result = metadataCode != null ? metadataCode.hashCode() : 0;
		result = 31 * result + (collection != null ? collection.hashCode() : 0);
		result = 31 * result + (visibleInAdvancedSearch ? 1 : 0);
		result = 31 * result + (inputType != null ? inputType.hashCode() : 0);
		result = 31 * result + (displayType != null ? displayType.hashCode() : 0);
		result = 31 * result + (metadataGroupCode != null ? metadataGroupCode.hashCode() : 0);
		result = 31 * result + (highlight ? 1 : 0);
		result = 31 * result + (helpMessages != null ? helpMessages.hashCode() : 0);
		return result;
	}
}
