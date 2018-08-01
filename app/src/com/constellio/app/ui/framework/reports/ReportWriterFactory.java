package com.constellio.app.ui.framework.reports;

import com.constellio.model.services.factories.ModelLayerFactory;

import java.io.Serializable;

public interface ReportWriterFactory extends Serializable {
	ReportWriter getReportBuilder(ModelLayerFactory modelLayerFactory);

	String getFilename();
}
