package com.constellio.app.modules.es.ui.pages;

import com.constellio.app.ui.pages.base.BaseView;

public interface ConnectorReportView extends BaseView {

	//Report modes
	public static final String INDEXATION = "indexationReport";
	public static final String ERRORS = "errorsReport";
	public static final String CONNECTOR_ID = "connectorId";
	public static final String REPORT_MODE = "reportMode";

	public void filterTable();
}
