package com.constellio.app.ui.framework.components;

import java.io.Serializable;
import java.util.List;

import com.constellio.app.ui.framework.reports.ReportBuilderFactory;

public interface ReportPresenter extends Serializable {
	List<String> getSupportedReports();

	ReportBuilderFactory getReport(String report);
}
