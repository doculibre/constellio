package com.constellio.app.modules.rm.reports.builders.administration.plan;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.modules.rm.reports.model.administration.plan.ConservationRulesReportPresenter;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.reports.ReportBuilder;
import com.constellio.app.ui.framework.reports.ReportBuilderFactory;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.factories.ModelLayerFactory;

public class ConservationRulesReportViewImpl implements ReportBuilderFactory {

	boolean byAdministrativeUnit = false;
	String administrativeUnit = null;

	public ConservationRulesReportViewImpl() {
	}

	public ConservationRulesReportViewImpl(boolean byAdministrativeUnit, String administrativeUnit) {
		this.byAdministrativeUnit = byAdministrativeUnit;
		this.administrativeUnit = administrativeUnit;
	}

	@Override
	public ReportBuilder getReportBuilder(ModelLayerFactory modelLayerFactory) {
		String collection = getSessionContext().getCurrentCollection();
		ConservationRulesReportPresenter presenter = new ConservationRulesReportPresenter(collection, modelLayerFactory,
				byAdministrativeUnit, administrativeUnit);
		return new ConservationRulesReportBuilder(presenter.build(), presenter.getFoldersLocator());
	}

	@Override
	public String getFilename() {
		return $("Report.ListRetentionRules") + ".pdf";
	}

	SessionContext getSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}
}
