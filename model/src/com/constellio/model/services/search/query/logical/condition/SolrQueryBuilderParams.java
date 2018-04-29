package com.constellio.model.services.search.query.logical.condition;

import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class SolrQueryBuilderParams {

	private boolean preferAnalyzedFields;

	private String languageCode;

	private MetadataSchemaTypes types;

	public SolrQueryBuilderParams(boolean preferAnalyzedFields, String languageCode, MetadataSchemaTypes types) {
		this.preferAnalyzedFields = preferAnalyzedFields;
		this.languageCode = languageCode;
		this.types = types;
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
}
