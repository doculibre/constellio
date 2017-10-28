package com.constellio.app.api.extensions;

import com.constellio.app.api.extensions.params.GetSearchResultSimpleTableWindowComponentParam;
import com.constellio.app.api.extensions.taxonomies.GetCustomResultDisplayParam;
import com.constellio.app.api.extensions.taxonomies.UserSearchEvent;
import com.constellio.app.ui.framework.components.SearchResultDisplay;
import com.vaadin.ui.Component;

public class SearchPageExtension {

	public SearchResultDisplay getCustomResultDisplayFor(GetCustomResultDisplayParam param) {
		return null;
	}

	public void notifyNewUserSearch(UserSearchEvent event) {
	}
	
	public Component getSimpleTableWindowComponent(GetSearchResultSimpleTableWindowComponentParam param) {
		return null;
	}
	
}
