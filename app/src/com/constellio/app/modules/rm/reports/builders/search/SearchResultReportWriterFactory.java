package com.constellio.app.modules.rm.reports.builders.search;

import java.util.List;
import java.util.Locale;

import com.constellio.app.modules.rm.reports.model.search.SearchResultReportModel;
import com.constellio.app.modules.rm.reports.model.search.SearchResultReportPresenter;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.framework.reports.ReportWriter;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class SearchResultReportWriterFactory implements NewReportWriterFactory<SearchResultReportParameters> {

	protected AppLayerFactory appLayerFactory;

	public SearchResultReportWriterFactory(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
	}

	@Override
	public ReportWriter getReportBuilder(SearchResultReportParameters parameters) {
		FoldersLocator folderLocator = appLayerFactory.getModelLayerFactory().getFoldersLocator();
		Locale locale = ConstellioUI.getCurrentSessionContext().getCurrentLocale();
		SearchResultReportPresenter searchResultPresenter = new SearchResultReportPresenter(appLayerFactory,
				parameters.getSelectedRecords(),
				parameters.getSchemaType(), parameters.getCollection(), parameters.getUsername(), parameters.getReportTitle(),
				parameters.getSearchQuery(), locale);
		return new SearchResultReportWriter(searchResultPresenter.buildModel(appLayerFactory.getModelLayerFactory()),
				folderLocator, locale);
	}

	@Override
	public String getFilename(SearchResultReportParameters parameters) {
		return parameters.getReportTitle() + "." + new SearchResultReportWriter(new SearchResultReportModel(), null, null)
				.getFileExtension();
	}

}