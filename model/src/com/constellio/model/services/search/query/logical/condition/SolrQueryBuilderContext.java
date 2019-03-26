package com.constellio.model.services.search.query.logical.condition;

import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.List;

public class SolrQueryBuilderContext {

	private boolean preferAnalyzedFields;

	private List<String> languages;

	private String languageCode;

	private MetadataSchemaTypes types;

	private List<MetadataSchemaType> searchedSchemaTypes;

	private String collection;

	public SolrQueryBuilderContext(boolean preferAnalyzedFields, List<String> languages, String languageCode,
								   MetadataSchemaTypes types,
								   List<MetadataSchemaType> searchedSchemaTypes, String collection) {
		this.preferAnalyzedFields = preferAnalyzedFields;
		this.languages = languages;
		this.languageCode = languageCode;
		this.types = types;
		this.searchedSchemaTypes = searchedSchemaTypes;
		this.collection = collection;
	}

	public List<String> getLanguages() {
		return languages;
	}

	public String getCollection() {
		return collection;
	}

	public MetadataSchemaTypes getTypes() {
		return types;
	}

	public boolean isPreferAnalyzedFields() {
		return preferAnalyzedFields;
	}

	public String getLanguageCode() {
		return languageCode;
	}

	public boolean isMultilingual(String schemaTypeCode, DataStoreField field) {
		return types == null ? false : types.getSchemaType(schemaTypeCode).isMultilingualMetadata(field.getLocalCode());
	}

	public boolean isSecondaryLanguage() {
		return types != null && languageCode != null && types != null
			   && !languageCode.equals(types.getCollectionInfo().getMainSystemLanguage().getCode());
	}

	public List<MetadataSchemaType> getSearchedSchemaTypes() {
		return searchedSchemaTypes;
	}


}
