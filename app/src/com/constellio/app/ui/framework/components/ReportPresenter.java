package com.constellio.app.ui.framework.components;

import java.io.Serializable;
import java.util.List;

import com.constellio.app.ui.framework.reports.ReportWriterFactory;

public interface ReportPresenter extends Serializable {
	List<String> getSupportedReports();

	ReportWriterFactory getReport(String report);
}
