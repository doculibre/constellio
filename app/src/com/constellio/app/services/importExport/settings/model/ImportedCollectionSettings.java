package com.constellio.app.services.importExport.settings.model;

import java.util.ArrayList;
import java.util.List;

public class ImportedCollectionSettings {

	String code;

	List<ImportedValueList> valueLists = new ArrayList<>();

	List<ImportedTaxonomy> taxonomies = new ArrayList<>();

	List<ImportedType> types = new ArrayList<>();

	public ImportedCollectionSettings setCode(String code) {
		this.code = code;
		return this;
	}

	public String getCode() {
		return code;
	}

	public ImportedCollectionSettings addValueList(ImportedValueList valueList) {
		if(valueList != null){
			valueLists.add(valueList);
		}
		return this;
	}

	public List<ImportedValueList> getValueLists() {
		return valueLists;
	}

	public ImportedCollectionSettings addTaxonomy(ImportedTaxonomy taxonomy){
		this.taxonomies.add(taxonomy);
		return this;
	}

	public List<ImportedTaxonomy> getTaxonomies(){
		return taxonomies;
	}

	public ImportedCollectionSettings addType(ImportedType importedType) {
		types.add(importedType);
		return this;
	}

	public ImportedCollectionSettings setTypes(List<ImportedType> types){
		this.types = types;
		return  this;
	}

	public List<ImportedType> getTypes(){
		return types;
	}

	public ImportedCollectionSettings setValueLists(List<ImportedValueList> valueLists) {
		this.valueLists = valueLists;
		return this;
	}

	public ImportedCollectionSettings setTaxonomies(List<ImportedTaxonomy> taxonomies) {
		this.taxonomies = taxonomies;
		return this;
	}
}
