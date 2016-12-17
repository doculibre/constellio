package com.constellio.app.ui.pages.search;

import java.io.InputStream;
import java.util.List;

import com.constellio.app.ui.pages.search.criteria.Criterion;

public interface AdvancedSearchView extends SearchView {
	String SEARCH_TYPE = "advancedSearch";

	List<Criterion> getSearchCriteria();

	String getSchemaType();

	void setSchemaType(String schemaTypeCode);

	String getSearchExpression();

	void setSearchCriteria(List<Criterion> criteria);

	void downloadBatchProcessingResults(InputStream inputStream);

	void closeBatchProcessingWindow();
}
