package com.constellio.app.services.importExport.settings.model;

import com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeCodeMode;
import com.constellio.model.entities.Language;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImportedValueList {

	String code;

	String codeMode;

	List<String> classifiedTypes = new ArrayList<>();

	Map<Language, String> title;

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

	public ImportedValueList setCodeMode(ValueListItemSchemaTypeCodeMode codeMode) {
		this.codeMode = codeMode.name();
		return this;
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

	public Map<Language, String> getTitle() {
		return title;
	}

	public ImportedValueList setTitle(Map<Language, String> title) {
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

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
