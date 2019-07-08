package com.constellio.app.modules.rm.extensions.api.reports;

import com.constellio.app.modules.rm.model.enums.DecommissioningType;
import com.constellio.app.modules.rm.reports.builders.administration.plan.AdministrativeUnitExcelReportParameters;
import com.constellio.app.modules.rm.reports.builders.administration.plan.AdministrativeUnitReportParameters;
import com.constellio.app.modules.rm.reports.builders.administration.plan.AvailableSpaceReportParameters;
import com.constellio.app.modules.rm.reports.builders.administration.plan.ClassificationReportPlanParameters;
import com.constellio.app.modules.rm.reports.builders.administration.plan.ConservationRulesReportParameters;
import com.constellio.app.modules.rm.reports.builders.administration.plan.UserReportParameters;
import com.constellio.app.modules.rm.reports.builders.decommissioning.ContainerRecordReportParameters;
import com.constellio.app.modules.rm.reports.builders.decommissioning.DecommissioningListExcelReportParameters;
import com.constellio.app.modules.rm.reports.builders.decommissioning.DecommissioningListReportParameters;
import com.constellio.app.modules.rm.reports.builders.search.stats.StatsReportParameters;
import com.constellio.app.modules.rm.reports.factories.ExampleReportParameters;
import com.constellio.app.modules.rm.reports.factories.ExampleReportWithoutRecordsParameters;
import com.constellio.app.modules.rm.reports.factories.labels.LabelsReportParameters;
import com.constellio.app.modules.rm.services.decommissioning.DocumentDecommissioningCertificateParams;
import com.constellio.app.modules.rm.services.decommissioning.FolderDecommissioningCertificateParams;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.framework.reports.ReportWriterFactory;
import com.constellio.data.frameworks.extensions.SingleValueExtension;
import com.constellio.data.utils.Provider;

public class RMReportBuilderFactories {

	public SingleValueExtension<NewReportWriterFactory<ContainerRecordReportParameters>> transferContainerRecordBuilderFactory = new SingleValueExtension<>();

	public SingleValueExtension<NewReportWriterFactory<AdministrativeUnitReportParameters>> administrativeUnitRecordBuilderFactory = new SingleValueExtension<>();

	public SingleValueExtension<NewReportWriterFactory<ClassificationReportPlanParameters>> classifcationPlanRecordBuilderFactory = new SingleValueExtension<>();

	public SingleValueExtension<NewReportWriterFactory<ConservationRulesReportParameters>> conservationRulesRecordBuilderFactory = new SingleValueExtension<>();

	public SingleValueExtension<NewReportWriterFactory<UserReportParameters>> userRecordBuilderFactory = new SingleValueExtension<>();

	public SingleValueExtension<NewReportWriterFactory<DecommissioningListReportParameters>> decommissioningListBuilderFactory = new SingleValueExtension<>();

	public SingleValueExtension<NewReportWriterFactory<LabelsReportParameters>> labelsBuilderFactory = new SingleValueExtension<>();

	public SingleValueExtension<NewReportWriterFactory<ExampleReportParameters>> exampleBuilderFactory = new SingleValueExtension<>();

	public SingleValueExtension<NewReportWriterFactory<ExampleReportWithoutRecordsParameters>> exampleWithoutRecordsBuilderFactory = new SingleValueExtension<>();

	public SingleValueExtension<NewReportWriterFactory<FolderDecommissioningCertificateParams>> folderDecommissioningCertificateFactory = new SingleValueExtension<>();

	public SingleValueExtension<NewReportWriterFactory<DocumentDecommissioningCertificateParams>> documentDecommissioningCertificateFactory = new SingleValueExtension<>();

	public SingleValueExtension<NewReportWriterFactory<StatsReportParameters>> statsBuilderFactory = new SingleValueExtension<>();

	public SingleValueExtension<NewReportWriterFactory<AvailableSpaceReportParameters>> availableSpaceBuilderFactory = new SingleValueExtension<>();

	public SingleValueExtension<NewReportWriterFactory<AdministrativeUnitExcelReportParameters>> administrativeUnitExcelBuilderFactory = new SingleValueExtension<>();

	public SingleValueExtension<NewReportWriterFactory<DecommissioningListExcelReportParameters>> decommissioningListExcelBuilderFactory = new SingleValueExtension<>();

	public RMReportBuilderFactories(AppLayerFactory appLayerFactory) {
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
