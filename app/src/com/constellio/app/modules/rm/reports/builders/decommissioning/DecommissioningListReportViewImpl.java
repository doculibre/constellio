package com.constellio.app.modules.rm.reports.builders.decommissioning;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.modules.rm.reports.builders.decommissioning.DecommissioningListReportViewImpl.DecommissioningListReportFactoryParameters;
import com.constellio.app.modules.rm.reports.model.decommissioning.DecommissioningListReportPresenter;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.reports.NewReportBuilderFactory;
import com.constellio.app.ui.framework.reports.ReportBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.factories.ModelLayerFactory;

public class DecommissioningListReportViewImpl implements NewReportBuilderFactory<DecommissioningListReportFactoryParameters> {

	protected ModelLayerFactory modelLayerFactory;

	public DecommissioningListReportViewImpl(AppLayerFactory appLayerFactory) {
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
	}

	@Override
	public ReportBuilder getReportBuilder(DecommissioningListReportFactoryParameters parameters) {
		String collection = getSessionContext().getCurrentCollection();
		DecommissioningListReportPresenter presenter = new DecommissioningListReportPresenter(collection, modelLayerFactory,
				parameters.decommissioningListId);
		return new DecommissioningListReportBuilder(presenter.build(), presenter.getFoldersLocator());
	}

	@Override
	public String getFilename(DecommissioningListReportFactoryParameters parameters) {
		return $("Report.DecommissioningList") + ".pdf";
	}

	SessionContext getSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}

	public static class DecommissioningListReportFactoryParameters {

		String decommissioningListId;

		public DecommissioningListReportFactoryParameters(String decommissioningListId) {
			this.decommissioningListId = decommissioningListId;
		}

		public String getDecommissioningListId() {
			return decommissioningListId;
		}
	}
}
