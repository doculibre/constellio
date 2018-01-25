package com.constellio.app.ui.entities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.schemas.SchemaUtils;

@SuppressWarnings("serial")
public class FormMetadataSchemaVO implements Serializable {

	String code = "";;

	String localCode = "";;

	String collection = "";;

	Map<String, String> labels = new HashMap<>();

	String currentLanguageCode;

	/* Schema type properties */
	Boolean advancedSearch = null;

	Boolean simpleSearch = null;


	public FormMetadataSchemaVO(SessionContext sessionContext) {
		this.currentLanguageCode = sessionContext.getCurrentLocale().getLanguage();
	}

	public FormMetadataSchemaVO(String code, String localCode, String collection, Map<String, String> labels) {
		this.localCode = localCode;
		this.code = code;
		this.collection = collection;
		this.labels = new HashMap<>(labels);
	}

	public FormMetadataSchemaVO(String code, String localCode,String collection, Map<String, String> labels, Boolean advancedSearch) {
		this(code, localCode, collection, labels);
		this.advancedSearch = advancedSearch;
	}

	public FormMetadataSchemaVO(String code, String localCode,String collection, Map<String, String> labels, Boolean advancedSearch, Boolean simpleSearch) {
		this(code, localCode, collection, labels, advancedSearch);
		this.simpleSearch = simpleSearch;
	}

	public String getCode() {
		return code;
	}

	public String getLocalCode() {
		return localCode;
	}

	public String getCollection() {
		return collection;
	}

	public Map<String, String> getLabels() {
		return labels;
	}

	public String getLabel(String language) {
		return labels.get(language);
	}

	public void addLabel(String language, String label) {
		labels.put(language, label);
	}

	public void setLabels(Map<String, String> labels) {
		this.labels = new HashMap<>(labels);
	}

	public void setLocalCode(String code) {
		this.localCode = code;
	}

	public void setCollection(String collection) {
		this.collection = collection;
	}

	public Boolean getAdvancedSearch() { return advancedSearch; }

	public void setAdvancedSearch(Boolean advancedSearch) {	this.advancedSearch = advancedSearch; }

	public Boolean getSimpleSearch() { return simpleSearch; }

	public void setSimpleSearch(Boolean simpleSearch) {	this.simpleSearch = simpleSearch; }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + ((collection == null) ? 0 : collection.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FormMetadataSchemaVO other = (FormMetadataSchemaVO) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (collection == null) {
			if (other.collection != null)
				return false;
		} else if (!collection.equals(other.collection))
			return false;
		return true;
	}

	/**
	 * Used by Vaadin to populate the header of the column in a table (since we use MetadataVO objects as property ids).
	 *
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		String toString;
		try {
			toString = getLabel(currentLanguageCode);
		} catch (RuntimeException e) {
			toString = code;
		}
		return toString;
	}
}
