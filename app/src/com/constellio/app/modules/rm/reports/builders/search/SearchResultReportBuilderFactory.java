/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.reports.builders.search;

import java.util.List;

import com.constellio.app.modules.rm.reports.model.search.SearchResultReportModel;
import com.constellio.app.modules.rm.reports.model.search.SearchResultReportPresenter;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.reports.ReportBuilder;
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
	public ReportBuilder getReportBuilder(ModelLayerFactory modelLayerFactory) {
		FoldersLocator folderLocator = modelLayerFactory.getFoldersLocator();
		SearchResultReportPresenter searchResultPresenter = new SearchResultReportPresenter(modelLayerFactory, selectedRecords,
				schemaType, collection, username, reportTitle, searchQuery);
		return new SearchResultReportBuilder(searchResultPresenter.buildModel(modelLayerFactory), folderLocator,
				ConstellioUI.getCurrentSessionContext().getCurrentLocale());
	}

	@Override
	public String getFilename() {
		return reportTitle + "." + new SearchResultReportBuilder(new SearchResultReportModel(), null, null).getFileExtension();
	}

}