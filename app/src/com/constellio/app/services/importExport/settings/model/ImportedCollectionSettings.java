package com.constellio.app.services.importExport.settings.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ImportedCollectionSettings {

	String code;

	List<ImportedValueList> valueLists = new ArrayList<>();

	List<ImportedTaxonomy> taxonomies = new ArrayList<>();

	List<ImportedType> types = new ArrayList<>();

	public String getCode() {
		return code;
	}

	public ImportedCollectionSettings setCode(String code) {
		this.code = code;
		return this;
	}

	public ImportedValueList newCustomValueList(String code) {
		if (!code.startsWith("ddvUSR")) {
			code = "ddvUSR" + code;
		}

		ImportedValueList importedValueList = new ImportedValueList().setCode(code);
		addValueList(importedValueList);
		return importedValueList;
	}

	public ImportedCollectionSettings addValueList(ImportedValueList valueList) {
		if (valueList != null) {
			valueLists.add(valueList);
		}
		return this;
	}

	public List<ImportedValueList> getValueLists() {
		return valueLists;
	}

	public ImportedCollectionSettings setValueLists(List<ImportedValueList> valueLists) {
		this.valueLists = valueLists;
		return this;
	}

	public ImportedCollectionSettings addTaxonomy(ImportedTaxonomy taxonomy) {
		this.taxonomies.add(taxonomy);
		return this;
	}

	public List<ImportedTaxonomy> getTaxonomies() {
		return taxonomies;
	}

	public ImportedCollectionSettings setTaxonomies(List<ImportedTaxonomy> taxonomies) {
		this.taxonomies = taxonomies;
		return this;
	}

	public ImportedType newType(String code) {
		ImportedType type = new ImportedType().setCode(code);
		types.add(type);
		return type;
	}

	public ImportedCollectionSettings addType(ImportedType importedType) {
		types.add(importedType);
		return this;
	}

	public List<ImportedType> getTypes() {
		return types;
	}

	public ImportedCollectionSettings setTypes(List<ImportedType> types) {
		this.types = types;
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);

	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	public ImportedType getType(String code) {
		for (ImportedType importedType : types) {
			if (code.equals(importedType.getCode())) {
				return importedType;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
