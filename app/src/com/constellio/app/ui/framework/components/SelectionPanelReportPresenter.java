package com.constellio.app.ui.framework.components;

import com.constellio.app.modules.rm.reports.builders.search.SearchResultReportParameters;
import com.constellio.app.modules.rm.reports.builders.search.SearchResultReportWriterFactory;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.framework.reports.ReportWithCaptionVO;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.reports.ReportServices;

import java.util.ArrayList;
import java.util.List;

public abstract class SelectionPanelReportPresenter implements NewReportPresenter {

	private AppLayerFactory appLayerFactory;
	private String collection;
	private User user;

	public SelectionPanelReportPresenter(AppLayerFactory appLayerFactory, String collection, User user) {
		this.appLayerFactory = appLayerFactory;
		this.collection = collection;
		this.user = user;
	}

	public List<ReportWithCaptionVO> getSupportedReports() {
		ArrayList<ReportWithCaptionVO> supportedReports = new ArrayList<>();
		ReportServices reportServices = new ReportServices(appLayerFactory.getModelLayerFactory(), collection);
		List<String> userReports = reportServices.getUserReportTitles(user, getSelectedSchemaType());
		if (userReports != null) {
			for (String reportTitle : userReports) {
				supportedReports.add(new ReportWithCaptionVO(reportTitle, reportTitle));
			}
		}
		return supportedReports;
	}

	public NewReportWriterFactory getReport(String report) {
		return new SearchResultReportWriterFactory(appLayerFactory);
	}

	public Object getReportParameters(String report) {
		return new SearchResultReportParameters(getSelectedRecordIds(), getSelectedSchemaType(),
				collection, report, user, null);
	}

	public abstract String getSelectedSchemaType();

	public abstract List<String> getSelectedRecordIds();
}
