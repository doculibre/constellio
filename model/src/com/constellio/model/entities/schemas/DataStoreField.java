package com.constellio.model.entities.schemas;

public interface DataStoreField {

	String getDataStoreCode();

	String getDataStoreType();

	boolean isMultivalue();

	boolean isSearchable();

	MetadataValueType getType();

	String getCollection();

	DataStoreField getAnalyzedField(String languageCode);

}
