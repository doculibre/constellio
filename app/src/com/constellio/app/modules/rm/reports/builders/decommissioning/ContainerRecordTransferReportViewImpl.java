package com.constellio.app.modules.rm.reports.builders.decommissioning;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.modules.rm.reports.decommissioning.ContainerRecordReportPresenter;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.reports.ReportBuilder;
import com.constellio.app.ui.framework.reports.ReportBuilderFactory;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.factories.ModelLayerFactory;

public class ContainerRecordTransferReportViewImpl implements ReportBuilderFactory {

	String containerId;

	public ContainerRecordTransferReportViewImpl(String containerId) {
		this.containerId = containerId;
	}

	@Override
	public ReportBuilder getReportBuilder(ModelLayerFactory modelLayerFactory) {
		String collection = getSessionContext().getCurrentCollection();
		ContainerRecordReportPresenter presenter = new ContainerRecordReportPresenter(collection, modelLayerFactory);
		ContainerRecord containerRecord = presenter.getContainerRecord(containerId);
		return new DocumentTransfertReportBuilder(presenter.build(containerRecord), presenter.getIoServices(),
				modelLayerFactory.getFoldersLocator());
	}

	@Override
	public String getFilename() {
		return $("Transfer.pdf");
	}

	SessionContext getSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}
}
