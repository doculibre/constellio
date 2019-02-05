package com.constellio.app.api.extensions;

import com.constellio.app.api.extensions.params.AddComponentToSearchResultParams;
import com.constellio.app.api.extensions.params.FilterCapsuleParam;
import com.constellio.app.api.extensions.params.GetSearchResultSimpleTableWindowComponentParam;
import com.constellio.app.api.extensions.params.SearchPageConditionParam;
import com.constellio.app.api.extensions.taxonomies.GetCustomResultDisplayParam;
import com.constellio.app.api.extensions.taxonomies.UserSearchEvent;
import com.constellio.app.ui.framework.components.SearchResultDisplay;
import com.constellio.model.entities.records.wrappers.Capsule;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.vaadin.ui.Component;

import java.util.List;

public class SearchPageExtension {

	public SearchResultDisplay getCustomResultDisplayFor(GetCustomResultDisplayParam param) {
		return null;
	}

	public void notifyNewUserSearch(UserSearchEvent event) {
	}

	public Component getSimpleTableWindowComponent(GetSearchResultSimpleTableWindowComponentParam param) {
		return null;
	}

	public Capsule filter(FilterCapsuleParam param) {
		return param.getCapsule();
	}

	public LogicalSearchCondition adjustSearchPageCondition(SearchPageConditionParam param) {
		return param.getCondition();
	}

	public List<Component> addComponentToSearchResult(AddComponentToSearchResultParams addComponentToSearchResultParams) {
		return null;
	}
}
