package com.constellio.app.ui.pages.search;

import com.constellio.app.ui.pages.base.BaseView;

import java.util.List;

public interface SearchView extends BaseView {
	
	void refreshSearchResultsAndFacets();

	void refreshSearchResults(boolean temporarySave);

	void refreshFacets();

	List<String> getSelectedRecordIds();

	List<String> getUnselectedRecordIds();

	void setSearchExpression(String expression);

	Boolean computeStatistics();
}
