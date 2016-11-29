package com.constellio.app.modules.rm.extensions.api.reports;

import com.constellio.app.modules.rm.model.enums.DecommissioningType;
import com.constellio.app.modules.rm.reports.builders.administration.plan.AdministrativeUnitReportParameters;
import com.constellio.app.modules.rm.reports.builders.decommissioning.ContainerRecordReportFactory;
import com.constellio.app.modules.rm.reports.builders.decommissioning.ContainerRecordReportParameters;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.framework.reports.ReportWriterFactory;
import com.constellio.data.frameworks.extensions.SingleValueExtension;
import com.constellio.data.utils.Provider;

import static com.constellio.data.frameworks.extensions.SingleValueExtension.DEFAULT_VALUE;

public class RMReportBuilderFactories {

	public SingleValueExtension<NewReportWriterFactory<ContainerRecordReportParameters>> transferContainerRecordBuilderFactory = new SingleValueExtension<>();

	public SingleValueExtension<NewReportWriterFactory<AdministrativeUnitReportParameters>> administrativeUnitRecordBuilderFactory = new SingleValueExtension<>();

	public RMReportBuilderFactories(AppLayerFactory appLayerFactory) {
		//TODO Nicolas : Déplacer ce register dans le plugin des rapports
		transferContainerRecordBuilderFactory.register(DEFAULT_VALUE, new ContainerRecordReportFactory(appLayerFactory));

		//completed
		//administrativeUnitRecordBuilderFactory.register(DEFAULT_VALUE, new AdministrativeUnitReportFactory(appLayerFactory));
	}

	/**
	 * Use public attributes instead
	 */
	@Deprecated
	public RMReportBuilderFactories register(
			Provider<ContainerRecordReportFactoryParams, ReportWriterFactory> transferContainerRecordReportProvider) {
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
}
