package com.constellio.app.ui.pages.search;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.components.NewReportPresenter;
import com.constellio.app.ui.framework.components.SearchResultTable;
import com.constellio.app.ui.framework.stream.DownloadStreamResource;
import com.constellio.app.ui.pages.base.ConstellioHeader;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Component;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class AdvancedSearchViewImpl extends SearchViewImpl<AdvancedSearchPresenter> implements AdvancedSearchView {

	public static final String BATCH_PROCESS_BUTTONSTYLE = "searchBatchProcessButton";
	public static final String LABELS_BUTTONSTYLE = "searchLabelsButton";

	private final ConstellioHeader header;

	public AdvancedSearchViewImpl() {
		presenter = new AdvancedSearchPresenter(this);
		presenter.resetFacetAndOrder();
		header = ConstellioUI.getCurrent().getHeader();
	}

	@Override
	protected ConstellioHeader getHeader() {
		return header;
	}

	public AdvancedSearchViewImpl(ConstellioHeader header) {
		presenter = new AdvancedSearchPresenter(this);
		presenter.resetFacetAndOrder();
		this.header = header;
	}

	@Override
	public List<Criterion> getSearchCriteria() {
		return header.getAdvancedSearchCriteria();
	}

	@Override
	public void setSearchCriteria(List<Criterion> criteria) {
		header.setAdvancedSearchCriteria(criteria);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void downloadBatchProcessingResults(final InputStream stream) {
		Resource resource = new DownloadStreamResource(new StreamResource.StreamSource() {
			@Override
			public InputStream getStream() {
				return stream;
			}
		}, "results.xls");
		Page.getCurrent().open(resource, null, false);
	}

	@Override
	public void closeBatchProcessingWindow() {
	}

	@Override
	public String getSchemaType() {
		return header.getAdvancedSearchSchemaType();
	}

	@Override
	public void setSchemaType(String schemaTypeCode) {
		header.selectAdvancedSearchSchemaType(schemaTypeCode);
	}

	@Override
	public void setSchema(String schemaCode) {
		header.selectAdvancedSearchSchema(schemaCode);
	}

	@Override
	public String getSearchExpression() {
		return header.getSearchExpression();
	}

	@Override
	protected Component buildSearchUI() {
		return null;
	}

	@Override
	protected Component buildSummary(final SearchResultTable results) {
		return results.createSummary(Collections.emptyList(), Collections.emptyList());
	}

	@Override
	public Boolean computeStatistics() {
		return presenter.computeStatistics();
	}

	@Override
	protected String getTitle() {
		return $("searchResults");
	}

	@Override
	public void fireSomeRecordsSelected() {
	}

	@Override
	public void fireNoRecordSelected() {
	}

	@Override
	public NewReportPresenter getPresenter() {
		return presenter;
	}
}
