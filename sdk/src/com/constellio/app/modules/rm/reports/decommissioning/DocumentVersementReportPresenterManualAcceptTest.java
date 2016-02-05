package com.constellio.app.modules.rm.reports.decommissioning;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.reports.builders.decommissioning.DocumentVersementReportBuilder;
import com.constellio.app.modules.rm.reports.model.decommissioning.DocumentReportModel;
import com.constellio.app.reports.builders.administration.plan.ReportBuilderTestFramework;

public class DocumentVersementReportPresenterManualAcceptTest extends ReportBuilderTestFramework {

	RMTestRecords records = new RMTestRecords(zeCollection);
	ContainerRecordReportPresenter presenter;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withAllTestUsers().withConstellioRMModule().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		presenter = new ContainerRecordReportPresenter(zeCollection, getModelLayerFactory());

	}

	// TODO Benoit
	@Test
	public void givenContainerBac01WhenBuildingReportThenGetReportWithSentDate_BoxNumber_filingSpaceAndCode_UserNameAndEmail_Sort_2_Folders_2000_2002_DM_PA() {
		DocumentReportModel model = presenter.build(records.getContainerBac01());

		build(new DocumentVersementReportBuilder(model, getIOLayerFactory().newIOServices(),
				getModelLayerFactory().getFoldersLocator()));
	}

	@Test
	public void givenContainerBac05WhenBuildingReportThenGetReportWithSentDate_BoxNumber_filingSpaceAndCode_Sort_6_Fodlers_2000_2002_DM_PA() {
		DocumentReportModel model = presenter.build(records.getContainerBac05());

		build(new DocumentVersementReportBuilder(model, getIOLayerFactory().newIOServices(),
				getModelLayerFactory().getFoldersLocator()));
	}
}