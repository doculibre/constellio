package com.constellio.app.modules.rm.reports.builders.decommissioning;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.modules.rm.reports.model.decommissioning.DecommissioningListReportPresenter;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.reports.ReportBuilder;
import com.constellio.app.ui.framework.reports.ReportBuilderFactory;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.factories.ModelLayerFactory;

public class DecommissioningListReportViewImpl implements ReportBuilderFactory {

	private String decommissioningListId;

	public DecommissioningListReportViewImpl(String decommissioningListId) {
		this.decommissioningListId = decommissioningListId;
	}

	@Override
	public ReportBuilder getReportBuilder(ModelLayerFactory modelLayerFactory) {
		String collection = getSessionContext().getCurrentCollection();
		DecommissioningListReportPresenter presenter = new DecommissioningListReportPresenter(collection, modelLayerFactory,
				decommissioningListId);
		return new DecommissioningListReportBuilder(presenter.build(), presenter.getFoldersLocator());
	}

	@Override
	public String getFilename() {
		return $("Report.DecommissioningList") + ".pdf";
	}

	SessionContext getSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}
}
