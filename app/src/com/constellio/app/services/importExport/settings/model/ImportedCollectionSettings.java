package com.constellio.app.services.importExport.settings.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ImportedCollectionSettings {

	String code;

	List<ImportedValueList> valueLists = new ArrayList<>();

	List<ImportedTaxonomy> taxonomies = new ArrayList<>();

	List<ImportedType> types = new ArrayList<>();

	Map<String, ImportedType> typesMap = new HashMap<>();

	public String getCode() {
		return code;
	}

	public ImportedCollectionSettings setCode(String code) {
		this.code = code;
		return this;
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

	public ImportedCollectionSettings addType(ImportedType importedType) {
		types.add(importedType);
		typesMap.put(importedType.getCode(), importedType);
		return this;
	}

	public List<ImportedType> getTypes() {
		return types;
	}

	public ImportedCollectionSettings setTypes(List<ImportedType> types) {
		this.types = types;
		for(ImportedType type : types){
			typesMap.put(type.getCode(), type);
		}
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
		return typesMap.get(code);
	}
}
