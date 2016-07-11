package com.constellio.app.services.importExport.settings.model;

import java.util.ArrayList;
import java.util.List;

public class ImportedCollectionSettings {

	String code;

	List<ImportedValueList> valueLists = new ArrayList<>();

	List<ImportedTaxonomy> taxonomies = new ArrayList<>();

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

}
