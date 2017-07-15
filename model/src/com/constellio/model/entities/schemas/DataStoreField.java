package com.constellio.model.entities.schemas;

import java.io.Serializable;

public interface DataStoreField extends Serializable {

	String getDataStoreCode();

	String getDataStoreType();

	boolean isMultivalue();

	boolean isSearchable();

	MetadataValueType getType();

	String getCollection();

	DataStoreField getAnalyzedField(String languageCode);

	DataStoreField getSortField();

}
