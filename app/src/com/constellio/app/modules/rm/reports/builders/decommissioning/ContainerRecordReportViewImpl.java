package com.constellio.app.modules.rm.reports.builders.decommissioning;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.modules.rm.reports.decommissioning.ContainerRecordReportPresenter;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.reports.ReportBuilder;
import com.constellio.app.ui.framework.reports.ReportBuilderFactory;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.factories.ModelLayerFactory;

public class ContainerRecordReportViewImpl implements ReportBuilderFactory {

	protected String containerId;
	protected boolean transfer;

	public ContainerRecordReportViewImpl(String containerId, boolean transfer) {
		this.containerId = containerId;
		this.transfer = transfer;
	}

	@Override
	public ReportBuilder getReportBuilder(ModelLayerFactory modelLayerFactory) {
		String collection = getSessionContext().getCurrentCollection();
		if (transfer) {
			ContainerRecordReportPresenter presenter = new ContainerRecordReportPresenter(collection, modelLayerFactory);
			ContainerRecord containerRecord = presenter.getContainerRecord(containerId);
			return new DocumentTransfertReportBuilder(presenter.build(containerRecord), presenter.getIoServices(),
					modelLayerFactory.getFoldersLocator());
		} else {
			ContainerRecordReportPresenter presenter = new ContainerRecordReportPresenter(collection, modelLayerFactory);
			ContainerRecord containerRecord = presenter.getContainerRecord(containerId);
			return new DocumentVersementReportBuilder(presenter.build(containerRecord), presenter.getIoServices(),
					modelLayerFactory.getFoldersLocator());
		}
	}

	@Override
	public String getFilename() {
		if (transfer) {
			return $("Transfer.pdf");
		} else {
			return $("Deposit.pdf");
		}
	}

	protected SessionContext getSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}
}
