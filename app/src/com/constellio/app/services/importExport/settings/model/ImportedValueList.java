package com.constellio.app.services.importExport.settings.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ImportedValueList {

	String code;

	String codeMode;

	List<String> classifiedTypes = new ArrayList<>();

	String title;

	Boolean hierarchical;

	public String getCode() {
		return code;
	}

	public ImportedValueList setCode(String code) {
		this.code = code;
		return this;
	}

	public String getCodeMode() {
		return codeMode;
	}

	public ImportedValueList setCodeMode(String codeMode) {
		this.codeMode = codeMode;
		return this;
	}

	public List<String> getClassifiedTypes() {
		return classifiedTypes;
	}

	public ImportedValueList setClassifiedTypes(List<String> classifiedTypes) {
		this.classifiedTypes = classifiedTypes;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public ImportedValueList setTitle(String title) {
		this.title = title;
		return this;
	}

	public ImportedValueList setHierarchical(boolean hierarchical) {
		this.hierarchical = hierarchical;
		return this;
	}

	public Boolean getHierarchical() {
		return hierarchical;
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);

	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}
}
