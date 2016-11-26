package com.constellio.app.ui.framework.components;

import java.io.Serializable;
import java.util.List;

import com.constellio.app.ui.framework.reports.NewReportWriterFactory;

public interface NewReportPresenter extends Serializable {
	List<String> getSupportedReports();

	NewReportWriterFactory getReport(String report);

	Object getReportParameters(String report);
}
