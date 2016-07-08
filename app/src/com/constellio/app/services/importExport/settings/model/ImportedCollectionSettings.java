package com.constellio.app.services.importExport.settings.model;

import java.util.ArrayList;
import java.util.List;

public class ImportedCollectionSettings {

	String code;

	List<ImportedValueList> valueLists = new ArrayList<>();

	public ImportedCollectionSettings setCode(String code) {
		this.code = code;
		return this;
	}

	public ImportedCollectionSettings addValueList(ImportedValueList valueList) {
		if(valueList != null){
			valueLists.add(valueList);
		}
		return this;
	}

	public String getCode() {
		return code;
	}

	public List<ImportedValueList> getValueLists() {
		return valueLists;
	}
}
