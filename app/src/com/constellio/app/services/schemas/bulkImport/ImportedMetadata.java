package com.constellio.app.services.schemas.bulkImport;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.MetadataValueType;

@SuppressWarnings("serial")
public class ImportedMetadata implements Serializable {
	String schemaTypeCode;
	String schemaCode;
	String localCode;
	MetadataValueType valueType;
	String reference;
	Map<Language, String> labels;
	boolean required;
	boolean multivalue;
	boolean searchable;
	boolean sortable;
	boolean advancedSearch;
	boolean facet;
	boolean highlight;
	boolean autocomplete;
	boolean enabled;
	String metadataGroup;
	MetadataInputType input;
	boolean global = false;
	boolean inherited = false;
	boolean newMetadata = false;
	String copyMetadata;
	String usingReference;
	String code;
	String calculator;
	boolean displayInAllSchemas;

	public ImportedMetadata(String schemaTypeCode, String schemaCode, String localCode, MetadataValueType type, boolean required,
			String reference,
			String label, boolean searchable, boolean multivalue, boolean sortable, boolean advancedSearch, boolean facet,
			MetadataInputType input, boolean highlight, boolean autocomplete, boolean enabled, String metadataGroup,
			String copyMetadata, String usingReference, String calculator, boolean displayInAllSchemas) {
		super();

		if (localCode.contains("USR")) {
			this.localCode = localCode.split("USR")[1];
		} else {
			this.localCode = localCode;
		}

		this.schemaTypeCode = schemaTypeCode;
		this.schemaCode = schemaCode;
		this.valueType = type;
		this.required = required;
		this.labels = new HashMap<>();
		for(Language language : Language.getAvailableLanguages()){
			if(!language.equals(Language.UNKNOWN)){
				this.labels.put(language, label);
			}
		}
		this.multivalue = multivalue;
		this.searchable = searchable;
		this.sortable = sortable;
		this.advancedSearch = advancedSearch;
		this.facet = facet;
		this.reference = reference;
		this.input = input;
		this.highlight = highlight;
		this.autocomplete = autocomplete;
		this.enabled = enabled;
		this.metadataGroup = metadataGroup;
		this.copyMetadata = copyMetadata;
		this.usingReference = usingReference;
		this.calculator = calculator;
		this.displayInAllSchemas = displayInAllSchemas;
	}

	public String getCopyMetadata() {
		return copyMetadata;
	}

	public String getUsingReference() {
		return usingReference;
	}

	public String getLocalCode() {
		return localCode;
	}

	public boolean isSearchable() {
		return searchable;
	}

	public boolean isSortable() {
		return sortable;
	}

	public boolean isRequired() {
		return required;
	}

	public boolean isAdvancedSearch() {
		return advancedSearch;
	}

	public boolean isFacet() {
		return facet;
	}

	public String getReference() {
		return reference;
	}

	public MetadataInputType getInput() {
		return input;
	}

	public MetadataValueType getValueType() {
		return valueType;
	}

	public boolean isMultivalue() {
		return multivalue;
	}

	public boolean isHighlight() {
		return highlight;
	}

	public boolean isAutocomplete() {
		return autocomplete;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public Map<Language, String> getLabels() {
		return labels;
	}

	public String getMetadataGroup() {
		return this.metadataGroup;
	}

	public void setLocalCode(String localcode) {
		this.localCode = localcode;
	}

	public void setSearchable(boolean searchable) {
		this.searchable = searchable;
	}

	public void setSortable(boolean sortable) {
		this.sortable = sortable;
	}

	public void setAdvancedSearch(boolean advancedSearch) {
		this.advancedSearch = advancedSearch;
	}

	public void setFacet(boolean facet) {
		this.facet = facet;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public void setInput(MetadataInputType input) {
		this.input = input;
	}

	public void setMultivalue(boolean multivalue) {
		this.multivalue = multivalue;
	}

	public void setLabels(Map<Language, String> labels) {
		this.labels = labels;
	}

	public void addLabel(Language language, String label) {
		this.labels.put(language, label);
	}

	public void setHighlight(boolean highlight) {
		this.highlight = highlight;
	}

	public void setAutocomplete(boolean autocomplete) {
		this.autocomplete = autocomplete;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setMetadataGroup(String metadataGroup) {
		this.metadataGroup = metadataGroup;
	}

	public boolean isGlobal() {
		return global;
	}

	public void setGlobal(boolean global) {
		this.global = global;
	}

	public boolean isNewMetadata() {
		return newMetadata;
	}

	public void setNewMetadata(boolean newMetadata) {
		this.newMetadata = newMetadata;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCalculator() {
		return calculator;
	}

	public void setCalculator(String calculator) {
		this.calculator = calculator;
	}

	public boolean isDisplayInAllSchemas() {
		return displayInAllSchemas;
	}
}
