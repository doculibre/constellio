package com.constellio.model.entities.schemas;

import java.io.Serializable;

public interface DataStoreField extends Serializable {

	String getDataStoreCode();

	String getLocalCode();

	String getSecondaryLanguageDataStoreCode(String language);

	String getDataStoreType();

	boolean isMultivalue();

	boolean isSearchable();

	MetadataValueType getType();

	String getCollection();

	DataStoreField getAnalyzedField(String languageCode);

	DataStoreField getSecondaryLanguageField(String languageCode);

	DataStoreField getSortField();

	boolean isUniqueValue();

	boolean isCacheIndex();
}
