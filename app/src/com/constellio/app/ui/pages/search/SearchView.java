package com.constellio.app.ui.pages.search;

import java.util.List;

import com.constellio.app.ui.pages.base.BaseView;

public interface SearchView extends BaseView {
	
	void refreshSearchResultsAndFacets();

	void refreshSearchResults(boolean temporarySave);

	void refreshFacets();

	List<String> getSelectedRecordIds();

	void setSearchExpression(String expression);

	Boolean computeStatistics();
}
