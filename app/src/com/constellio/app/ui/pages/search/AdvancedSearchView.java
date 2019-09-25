package com.constellio.app.ui.pages.search;

import com.constellio.app.ui.framework.components.NewReportPresenter;
import com.constellio.app.ui.pages.search.criteria.Criterion;

import java.io.InputStream;
import java.util.List;

public interface AdvancedSearchView extends SearchView {

	String SEARCH_TYPE = "advancedSearch";

	List<Criterion> getSearchCriteria();

	String getSchemaType();

	void setSchemaType(String schemaTypeCode);

	void setSchema(String schemaCode);

	String getSearchExpression();

	void setSearchCriteria(List<Criterion> criteria);

	void downloadBatchProcessingResults(InputStream inputStream);

	void closeBatchProcessingWindow();

	List<String> getUnselectedRecordIds();

	void fireSomeRecordsSelected();

    void fireNoRecordSelected();

	NewReportPresenter getPresenter();
}
