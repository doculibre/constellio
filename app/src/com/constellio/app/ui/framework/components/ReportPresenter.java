package com.constellio.app.ui.framework.components;

import com.constellio.app.ui.framework.reports.NewReportWriterFactory;

import java.io.Serializable;
import java.util.List;

public interface ReportPresenter extends Serializable {
	List<String> getSupportedReports();

	NewReportWriterFactory getReport(String report);
}
