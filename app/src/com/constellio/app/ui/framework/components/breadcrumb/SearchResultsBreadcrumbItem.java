package com.constellio.app.ui.framework.components.breadcrumb;

import static com.constellio.app.ui.i18n.i18n.$;

public class SearchResultsBreadcrumbItem implements BreadcrumbItem {
	
	private String searchId;
    
	private boolean advancedSearch;
	
	public SearchResultsBreadcrumbItem(String searchId, boolean advancedSearch) {
		this.searchId = searchId;
		this.advancedSearch = advancedSearch;
	}

	@Override
	public String getLabel() {
		return $("searchResults");
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	public String getSearchId() {
		return searchId;
	}
	
	public boolean isAdvancedSearch() {
		return advancedSearch;
	}

}
