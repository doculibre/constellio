package com.constellio.app.ui.framework.components;

import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.framework.reports.ReportWithCaptionVO;

import java.io.Serializable;
import java.util.List;

public interface NewReportPresenter extends Serializable {
	List<ReportWithCaptionVO> getSupportedReports();

	NewReportWriterFactory getReport(String report);

	Object getReportParameters(String report);


}
