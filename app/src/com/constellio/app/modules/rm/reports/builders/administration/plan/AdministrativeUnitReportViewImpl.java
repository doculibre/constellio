package com.constellio.app.modules.rm.reports.builders.administration.plan;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.modules.rm.reports.model.administration.plan.AdministrativeUnitReportPresenter;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.reports.ReportBuilder;
import com.constellio.app.ui.framework.reports.ReportBuilderFactory;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.factories.ModelLayerFactory;

public class AdministrativeUnitReportViewImpl implements ReportBuilderFactory {

	boolean withUsers = false;

	public AdministrativeUnitReportViewImpl() {
	}

	public AdministrativeUnitReportViewImpl(boolean withUsers) {
		this.withUsers = withUsers;
	}

	@Override
	public ReportBuilder getReportBuilder(ModelLayerFactory modelLayerFactory) {
		String collection = getSessionContext().getCurrentCollection();
		AdministrativeUnitReportPresenter presenter = new AdministrativeUnitReportPresenter(collection, modelLayerFactory,
				withUsers);
		return new AdministrativeUnitReportBuilder(presenter.build(), presenter.getFoldersLocator());
	}

	@Override
	public String getFilename() {
		return $("Report.AdministrativeUnits") + ".pdf";
	}

	SessionContext getSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}
}
