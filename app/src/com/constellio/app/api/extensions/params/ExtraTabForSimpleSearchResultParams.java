package com.constellio.app.api.extensions.params;

public class ExtraTabForSimpleSearchResultParams {
	private String searchTerm;

	public ExtraTabForSimpleSearchResultParams(String searchTerm) {
		this.searchTerm = searchTerm;
	}

	public String getSearchTerm() {
		return searchTerm;
	}
}
