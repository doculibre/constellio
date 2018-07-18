package com.constellio.app.ui.framework.components;

import java.io.Serializable;
import java.util.List;

import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.framework.reports.ReportWithCaptionVO;

public interface NewReportPresenter extends Serializable {
	List<ReportWithCaptionVO> getSupportedReports();

	NewReportWriterFactory getReport(String report);

	Object getReportParameters(String report);


}
