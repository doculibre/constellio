package com.constellio.app.api.extensions.params;

import com.constellio.app.ui.entities.SearchResultVO;

public class AddComponentToSearchResultParams {
	private SearchResultVO searchResultVO;

	public AddComponentToSearchResultParams(SearchResultVO searchResultVO) {
		this.searchResultVO = searchResultVO;
	}

	public SearchResultVO getSearchResultVO() {
		return searchResultVO;
	}

}
