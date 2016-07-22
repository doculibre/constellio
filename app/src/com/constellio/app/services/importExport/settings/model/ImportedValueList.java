package com.constellio.app.services.importExport.settings.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportedValueList {

	String code;

	String codeMode;

	List<String> classifiedTypes = new ArrayList<>();

	Map<String, String> titles = new HashMap<>();

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

	public Map<String, String> getTitles() {
		return titles;
	}

	public ImportedValueList setTitles(Map<String, String> titles) {
		this.titles = titles;
		return this;
	}

	public ImportedValueList setHierarchical(boolean hierarchical) {
		this.hierarchical = hierarchical;
		return this;
	}

	public Boolean getHierarchical() {
		return hierarchical;
	}

}
