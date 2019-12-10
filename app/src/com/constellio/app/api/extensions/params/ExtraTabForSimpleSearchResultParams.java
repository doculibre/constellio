package com.constellio.app.api.extensions.params;

import com.constellio.app.ui.pages.search.SearchPresenter;
import com.constellio.app.ui.pages.search.SearchView;

public class ExtraTabForSimpleSearchResultParams<T extends SearchPresenter<? extends SearchView>> {
	private T presenter;
	private String searchTerm;

	public ExtraTabForSimpleSearchResultParams(T presenter, String searchTerm) {
		this.presenter = presenter;
		this.searchTerm = searchTerm;
	}

	public T getPresenter() {
		return presenter;
	}

	public String getSearchTerm() {
		return searchTerm;
	}
}
