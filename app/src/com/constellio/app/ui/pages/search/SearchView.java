package com.constellio.app.ui.pages.search;

import java.util.List;

import com.constellio.app.ui.framework.data.SearchResultVODataProvider;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.model.frameworks.validation.ValidationException;

public interface SearchView extends BaseView {

	void refreshSearchResultsAndFacets();

	SearchResultVODataProvider refreshSearchResults(boolean temporarySave, boolean includeFacets);

	void refreshFacets(SearchResultVODataProvider dataProvider);

	List<String> getSelectedRecordIds();

	List<String> getUnselectedRecordIds();

	void setSearchExpression(String expression);

	Boolean computeStatistics();

    void fireSomeRecordsSelected();

	void fireNoRecordSelected();
	
}
