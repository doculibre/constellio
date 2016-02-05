package com.constellio.app.modules.rm.reports.builders.administration.plan;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.modules.rm.reports.model.administration.plan.UserReportPresenter;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.reports.ReportBuilder;
import com.constellio.app.ui.framework.reports.ReportBuilderFactory;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.factories.ModelLayerFactory;

public class UserReportViewImpl implements ReportBuilderFactory {

	@Override
	public ReportBuilder getReportBuilder(ModelLayerFactory modelLayerFactory) {
		String collection = getSessionContext().getCurrentCollection();
		UserReportPresenter presenter = new UserReportPresenter(collection, modelLayerFactory);
		return new UserReportBuilder(presenter.build(), presenter.getFoldersLocator());
	}

	@Override
	public String getFilename() {
		return $("Report.User") + ".pdf";
	}

	SessionContext getSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}
}
