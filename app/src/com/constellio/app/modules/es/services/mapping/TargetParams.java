package com.constellio.app.modules.es.services.mapping;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.constellio.model.entities.schemas.MetadataValueType;

public class TargetParams implements Serializable {
	private String code;
	private String label;
	private MetadataValueType type;
	private boolean multivalue = true;
	private boolean searchable;
	private boolean advancedSearch;
	private boolean searchResults;
	private final Set<String> customFlags;
	private final boolean existing;

	public TargetParams(boolean existing) {
		this.existing = existing;
		customFlags = new HashSet<>();
	}

	public TargetParams() {
		this(false);
	}

	public TargetParams(String code, String label, MetadataValueType type, boolean existing) {
		this(existing);
		this.code = code;
		this.label = label;
		this.type = type;
	}

	public TargetParams(String code, String label, MetadataValueType type) {
		this(code, label, type, false);
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public TargetParams withCode(String code) {
		setCode(code);
		return this;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public TargetParams withLabel(String label) {
		setLabel(label);
		return this;
	}

	public MetadataValueType getType() {
		return type;
	}

	public void setType(MetadataValueType type) {
		this.type = type;
	}

	public TargetParams withType(MetadataValueType type) {
		setType(type);
		return this;
	}

	public boolean isMultivalue() {
		return multivalue;
	}

	public void setMultivalue(boolean multivalue) {
		this.multivalue = multivalue;
	}

	public TargetParams withMultivalue(boolean multivalue) {
		setMultivalue(multivalue);
		return this;
	}

	public boolean isSearchable() {
		return searchable;
	}

	public void setSearchable(boolean searchable) {
		this.searchable = searchable;
	}

	public TargetParams withSearchable(boolean searchable) {
		setSearchable(searchable);
		return this;
	}

	public boolean isAdvancedSearch() {
		return advancedSearch;
	}

	public void setAdvancedSearch(boolean advancedSearch) {
		this.advancedSearch = advancedSearch;
	}

	public TargetParams withAdvancedSearch(boolean advancedSearch) {
		setAdvancedSearch(advancedSearch);
		return this;
	}

	public boolean isSearchResults() {
		return searchResults;
	}

	public void setSearchResults(boolean searchResults) {
		this.searchResults = searchResults;
	}

	public TargetParams withSearchResults(boolean searchResults) {
		setSearchResults(searchResults);
		return this;
	}

	public TargetParams setCustomFlag(String flag) {
		customFlags.add(flag);
		return this;
	}

	public TargetParams unsetCustomFlag(String flag) {
		customFlags.remove(flag);
		return this;
	}

	public boolean hasCustomFlag(String flag) {
		return customFlags.contains(flag);
	}

	public boolean isExisting() {
		return existing;
	}

	public boolean isValid() {
		return code != null && label != null && type != null;
	}
}
