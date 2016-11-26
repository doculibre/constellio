package com.constellio.app.modules.robots.reports;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import com.constellio.app.modules.robots.model.DryRunRobotAction;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.reports.ReportWriter;
import com.constellio.app.ui.framework.reports.ReportBuilderFactory;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.factories.ModelLayerFactory;

public class DryRunReportBuilderFactory implements ReportBuilderFactory {
	private List<DryRunRobotAction> dryRunRobotActions;
	private SessionContext sessionContext;

	@Deprecated
	public DryRunReportBuilderFactory(List<DryRunRobotAction> dryRunRobotActions) {
		this.dryRunRobotActions = dryRunRobotActions;
		this.sessionContext = ConstellioUI.getCurrentSessionContext();
	}

	public DryRunReportBuilderFactory(List<DryRunRobotAction> dryRunRobotActions, SessionContext sessionContext) {
		this.dryRunRobotActions = dryRunRobotActions;
		this.sessionContext = sessionContext;
	}

	@Override
	public ReportWriter getReportBuilder(ModelLayerFactory modelLayerFactory) {
		DryRunReportPresenter dryRunReportPresenter = new DryRunReportPresenter(modelLayerFactory, dryRunRobotActions, sessionContext);
		return new DryRunReportWriter(dryRunReportPresenter.buildModel(), null);
	}

	@Override
	public String getFilename() {
		return $("DryRunReport.filename") + "." + new DryRunReportWriter(new DryRunReportModel(), null)
				.getFileExtension();
	}

}