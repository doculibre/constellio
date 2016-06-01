package com.constellio.app.modules.rm.extensions.api.reports;

import com.constellio.app.modules.rm.model.enums.DecommissioningType;
import com.constellio.app.modules.rm.reports.builders.decommissioning.ContainerRecordReportViewImpl;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.ui.framework.reports.ReportBuilderFactory;
import com.constellio.data.utils.Provider;

public class RMReportBuilderFactories {

	ContainerRecordReportFactoryProvider transferContainerRecordReportProvider = new ContainerRecordReportFactoryProvider();

	public ReportBuilderFactory build(ContainerRecordReportFactoryParams params) {
		return transferContainerRecordReportProvider.get(params);
	}

	public RMReportBuilderFactories register(ContainerRecordReportFactoryProvider transferContainerRecordReportProvider) {
		this.transferContainerRecordReportProvider = transferContainerRecordReportProvider;
		return this;
	}

	public static class ContainerRecordReportFactoryParams extends BaseSingleRecordReportFactoryParams<ContainerRecord> {

		public ContainerRecordReportFactoryParams(ContainerRecord containerRecord) {
			super(containerRecord);
		}

		public boolean isTransfer() {
			return getWrappedRecord().getDecommissioningType() == DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE;
		}
	}

	public static class ContainerRecordReportFactoryProvider
			implements Provider<ContainerRecordReportFactoryParams, ReportBuilderFactory> {
		public ReportBuilderFactory get(ContainerRecordReportFactoryParams params) {
			return new ContainerRecordReportViewImpl(params.getWrappedRecord().getId(), params.isTransfer());
		}
	}

}
