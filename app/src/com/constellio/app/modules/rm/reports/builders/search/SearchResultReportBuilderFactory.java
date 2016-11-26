package com.constellio.app.modules.rm.reports.builders.search;

import java.util.List;
import java.util.Locale;

import com.constellio.app.modules.rm.reports.model.search.SearchResultReportModel;
import com.constellio.app.modules.rm.reports.model.search.SearchResultReportPresenter;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.reports.ReportWriter;
import com.constellio.app.ui.framework.reports.ReportBuilderFactory;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class SearchResultReportBuilderFactory implements ReportBuilderFactory {
	private final List<String> selectedRecords;
	private final String schemaType;
	private final String collection;
	private final String username;
	private final String reportTitle;
	private final LogicalSearchQuery searchQuery;
	private final ModelLayerFactory modelLayerFactory;

	public SearchResultReportBuilderFactory(ModelLayerFactory modelLayerFactory, List<String> selectedRecords, String schemaType,
			String collection, String reportTitle, User user, LogicalSearchQuery searchQuery) {
		this.selectedRecords = selectedRecords;
		this.schemaType = schemaType;
		this.collection = collection;
		this.reportTitle = reportTitle;
		this.searchQuery = searchQuery;
		this.modelLayerFactory = modelLayerFactory;
		if (user != null) {
			this.username = user.getUsername();
		} else {
			username = null;
		}
	}

	@Override
	public ReportWriter getReportBuilder(ModelLayerFactory modelLayerFactory) {
		FoldersLocator folderLocator = modelLayerFactory.getFoldersLocator();
		Locale locale =ConstellioUI.getCurrentSessionContext().getCurrentLocale();
		SearchResultReportPresenter searchResultPresenter = new SearchResultReportPresenter(modelLayerFactory, selectedRecords,
				schemaType, collection, username, reportTitle, searchQuery, locale);
		return new SearchResultReportWriter(searchResultPresenter.buildModel(modelLayerFactory), folderLocator,locale);
	}

	@Override
	public String getFilename() {
		return reportTitle + "." + new SearchResultReportWriter(new SearchResultReportModel(), null, null).getFileExtension();
	}

}