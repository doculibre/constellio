package com.constellio.app.modules.rm.reports.builders.decommissioning;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.modules.rm.reports.builders.decommissioning.ContainerRecordReportFactory.ContainerRecordReportParameters;
import com.constellio.app.modules.rm.reports.decommissioning.ContainerRecordReportPresenter;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.framework.reports.ReportWriter;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.factories.ModelLayerFactory;

public class ContainerRecordReportFactory implements NewReportWriterFactory<ContainerRecordReportParameters> {

	protected ModelLayerFactory modelLayerFactory;
	protected AppLayerFactory appLayerFactory;

	public ContainerRecordReportFactory(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
	}

	@Override
	public ReportWriter getReportBuilder(ContainerRecordReportParameters parameters) {
		String collection = getSessionContext().getCurrentCollection();
		if (parameters.transfer) {
			ContainerRecordReportPresenter presenter = new ContainerRecordReportPresenter(collection, modelLayerFactory);
			ContainerRecord containerRecord = presenter.getContainerRecord(parameters.containerId);
			return new DocumentTransfertReportWriter(presenter.build(containerRecord), presenter.getIoServices(),
					modelLayerFactory.getFoldersLocator());
		} else {
			ContainerRecordReportPresenter presenter = new ContainerRecordReportPresenter(collection, modelLayerFactory);
			ContainerRecord containerRecord = presenter.getContainerRecord(parameters.containerId);
			return new DocumentVersementReportWriter(presenter.build(containerRecord), presenter.getIoServices(),
					modelLayerFactory.getFoldersLocator());
		}
	}

	@Override
	public String getFilename(ContainerRecordReportParameters parameters) {
		if (parameters.transfer) {
			return $("Transfer.pdf");
		} else {
			return $("Deposit.pdf");
		}
	}

	public static class ContainerRecordReportParameters {
		String containerId;
		boolean transfer;

		public ContainerRecordReportParameters(String containerId, boolean transfer) {
			this.containerId = containerId;
			this.transfer = transfer;
		}

		public String getContainerId() {
			return containerId;
		}

		public boolean isTransfer() {
			return transfer;
		}
	}

	protected SessionContext getSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}
}
