package com.constellio.app.modules.robots.reports;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import com.constellio.app.modules.robots.model.DryRunRobotAction;
import com.constellio.app.ui.framework.reports.ReportBuilder;
import com.constellio.app.ui.framework.reports.ReportBuilderFactory;
import com.constellio.model.services.factories.ModelLayerFactory;

public class DryRunReportBuilderFactory implements ReportBuilderFactory {
	private List<DryRunRobotAction> dryRunRobotActions;

	public DryRunReportBuilderFactory(List<DryRunRobotAction> dryRunRobotActions) {
		this.dryRunRobotActions = dryRunRobotActions;
	}

	@Override
	public ReportBuilder getReportBuilder(ModelLayerFactory modelLayerFactory) {
		DryRunReportPresenter dryRunReportPresenter = new DryRunReportPresenter(modelLayerFactory, dryRunRobotActions);
		return new DryRunReportBuilder(dryRunReportPresenter.buildModel(), null);
	}

	@Override
	public String getFilename() {
		return $("DryRunReport.filename") + "." + new DryRunReportBuilder(new DryRunReportModel(), null)
				.getFileExtension();
	}

}