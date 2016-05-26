package com.constellio.app.modules.rm.extensions.api;

import com.constellio.app.extensions.ModuleExtensions;
import com.constellio.app.modules.rm.extensions.api.reports.RMReportBuilderFactories;

public class RMModuleExtensions implements ModuleExtensions {

	RMReportBuilderFactories rmReportBuilderFactories;

	public RMModuleExtensions() {
		this.rmReportBuilderFactories = new RMReportBuilderFactories();
	}

	public RMReportBuilderFactories getReportBuilderFactories() {
		return rmReportBuilderFactories;
	}
}
