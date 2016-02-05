package com.constellio.model.services.search.query.logical.condition;

public class SolrQueryBuilderParams {

	private boolean preferAnalyzedFields;

	private String languageCode;

	public SolrQueryBuilderParams(boolean preferAnalyzedFields, String languageCode) {
		this.preferAnalyzedFields = preferAnalyzedFields;
		this.languageCode = languageCode;
	}

	public boolean isPreferAnalyzedFields() {
		return preferAnalyzedFields;
	}

	public String getLanguageCode() {
		return languageCode;
	}
}
