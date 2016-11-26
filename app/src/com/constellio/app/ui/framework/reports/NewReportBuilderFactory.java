package com.constellio.app.ui.framework.reports;

import java.io.Serializable;

public interface NewReportBuilderFactory<P> extends Serializable {
	ReportWriter getReportBuilder(P parameters);

	String getFilename(P parameters);
}
