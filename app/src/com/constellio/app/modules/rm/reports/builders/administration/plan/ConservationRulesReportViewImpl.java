package com.constellio.app.modules.rm.reports.builders.administration.plan;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.modules.rm.reports.model.administration.plan.ConservationRulesReportPresenter;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.reports.ReportWriter;
import com.constellio.app.ui.framework.reports.ReportWriterFactory;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.factories.ModelLayerFactory;

public class ConservationRulesReportViewImpl implements ReportWriterFactory {

	boolean byAdministrativeUnit = false;
	String administrativeUnit = null;

	public ConservationRulesReportViewImpl() {
	}

	public ConservationRulesReportViewImpl(boolean byAdministrativeUnit, String administrativeUnit) {
		this.byAdministrativeUnit = byAdministrativeUnit;
		this.administrativeUnit = administrativeUnit;
	}

	@Override
	public ReportWriter getReportBuilder(ModelLayerFactory modelLayerFactory) {
		String collection = getSessionContext().getCurrentCollection();
		ConservationRulesReportPresenter presenter = new ConservationRulesReportPresenter(collection, modelLayerFactory,
				byAdministrativeUnit, administrativeUnit);
		return new ConservationRulesReportWriter(presenter.build(), presenter.getFoldersLocator());
	}

	@Override
	public String getFilename() {
		return $("Report.ListRetentionRules") + ".pdf";
	}

	SessionContext getSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}
}
