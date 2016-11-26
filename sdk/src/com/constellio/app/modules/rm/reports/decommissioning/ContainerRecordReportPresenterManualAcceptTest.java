package com.constellio.app.modules.rm.reports.decommissioning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.reports.builders.decommissioning.DocumentTransfertReportWriter;
import com.constellio.app.modules.rm.reports.model.decommissioning.DocumentReportModel;
import com.constellio.app.modules.rm.reports.model.decommissioning.DocumentReportModel.DocumentTransfertModel_Calendar;
import com.constellio.app.modules.rm.reports.model.decommissioning.DocumentReportModel.DocumentTransfertModel_Document;
import com.constellio.app.modules.rm.reports.model.decommissioning.DocumentReportModel.DocumentTransfertModel_Identification;
import com.constellio.app.modules.rm.reports.model.decommissioning.ReportBooleanField;
import com.constellio.app.reports.builders.administration.plan.ReportBuilderTestFramework;

public class ContainerRecordReportPresenterManualAcceptTest extends ReportBuilderTestFramework {

	RMTestRecords records = new RMTestRecords(zeCollection);
	ContainerRecordReportPresenter presenter;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		presenter = new ContainerRecordReportPresenter(zeCollection, getModelLayerFactory());

	}

	//@Test TODO Francis expected:<"X1[1]0"> but was:<"X1[2]0">
	public void givenContainerBac01WhenBuildingModelThenGetModel() {
		DocumentReportModel model = presenter.build(records.getContainerBac01());
		verifyBac01Model(model);
	}

	@Test
	public void test1() {
		givenContainerBac01WhenBuildingReportThenGetReportWithSentDate_BoxNumber_filingSpaceAndCode_UserNameAndEmail_SemiActivePeriod_Sort_DM_PA_2_Folders_2000_2002();

	}

	@Test
	public void test2() {
		givenContainerBac06WhenBuildingReportThenGetReportWithSentDate_BoxNumber_filingSpaceAndCode_CalendrierConservation_4_SemiActivePeriod_888_Disposition_2007_10_31_Conservation_DM_1_Folders_2000_2002();

	}

	@Test
	public void test3() {
		givenContainerBac12WhenBuildingReportThenGetReportWithSentDate_BoxNumber_filingSpaceAndCode_UnitAddress_CalendarConservation_2_SemiActivePeriod_0_Disposition_2005_10_31_Destruction_DM__PA_3_Folders_2000_2002();

	}

	//Windows doesn't open file with too long filename
	//	@Test
	public void givenContainerBac01WhenBuildingReportThenGetReportWithSentDate_BoxNumber_filingSpaceAndCode_UserNameAndEmail_SemiActivePeriod_Sort_DM_PA_2_Folders_2000_2002() {
		DocumentReportModel model = presenter.build(records.getContainerBac01());

		assertThat(model).isNotNull();

		build(new DocumentTransfertReportWriter(model, getIOLayerFactory().newIOServices(),
				getModelLayerFactory().getFoldersLocator()));

	}

	//	@Test
	public void givenContainerBac06WhenBuildingReportThenGetReportWithSentDate_BoxNumber_filingSpaceAndCode_CalendrierConservation_4_SemiActivePeriod_888_Disposition_2007_10_31_Conservation_DM_1_Folders_2000_2002() {
		DocumentReportModel model = presenter.build(records.getContainerBac06());

		assertThat(model).isNotNull();

		build(new DocumentTransfertReportWriter(model, getIOLayerFactory().newIOServices(),
				getModelLayerFactory().getFoldersLocator()));

	}

	//	@Test
	public void givenContainerBac12WhenBuildingReportThenGetReportWithSentDate_BoxNumber_filingSpaceAndCode_UnitAddress_CalendarConservation_2_SemiActivePeriod_0_Disposition_2005_10_31_Destruction_DM__PA_3_Folders_2000_2002() {
		DocumentReportModel model = presenter.build(records.getContainerBac12());

		assertThat(model).isNotNull();

		build(new DocumentTransfertReportWriter(model, getIOLayerFactory().newIOServices(),
				getModelLayerFactory().getFoldersLocator()));

	}

	private void verifyBac01Model(DocumentReportModel model) {
		verifyDocuments(model);

		verifyCalendar(model);

		verifyIdentification(model);
	}

	private void verifyDocuments(DocumentReportModel model) {
		List<DocumentTransfertModel_Document> documents = model.getDocumentList();
		assertThat(documents).isNotNull();
		assertThat(documents).isNotEmpty();

		DocumentTransfertModel_Document document1 = documents.get(0);
		assertThat(document1).isNotNull();
		assertThat(document1.getCode()).isEqualTo("X110");
		assertThat(document1.getDelayNumber()).isEqualTo("2");
		assertThat(document1.getReferenceId()).isEqualTo("C50");
		assertThat(document1.getTitle()).isEqualTo("Pois");
		assertThat(document1.getStartingYear()).isEqualTo("2000");
		assertThat(document1.getEndingYear()).isEqualTo("2001");
		// TODO assertThat(document1.getRestrictionYear()).isNotEmpty();

		DocumentTransfertModel_Document document2 = documents.get(1);
		assertThat(document2).isNotNull();
		assertThat(document2.getCode()).isEqualTo("X120");
		assertThat(document2.getDelayNumber()).isEqualTo("4");
		assertThat(document2.getReferenceId()).isEqualTo("C55");
		assertThat(document2.getTitle()).isEqualTo("Bette");
		assertThat(document2.getStartingYear()).isEqualTo("2000");
		assertThat(document2.getEndingYear()).isEqualTo("2002");
		// TODO assertThat(document2.getRestrictionYear()).isNotEmpty();
	}

	private void verifyCalendar(DocumentReportModel model) {
		DocumentTransfertModel_Calendar calendar = model.getCalendarModel();
		assertThat(calendar).isNotNull();

		assertThat(calendar.getCalendarNumber()).isEmpty();
		assertThat(calendar.getRuleNumber()).isNotNull();
		assertThat(calendar.getSemiActiveRange()).isNotNull();
		assertThat(calendar.getDispositionYear()).isNotNull();

		List<ReportBooleanField> conservationDispositions = calendar.getConservationDisposition();
		assertThat(conservationDispositions).isNotNull();
		assertThat(conservationDispositions).extracting("label", "value").contains(tuple("D", false), tuple("T", true),
				tuple("C", false));

		List<ReportBooleanField> supports = calendar.getSupports();
		assertThat(supports).isNotNull();
		assertThat(supports).extracting("label", "value").contains(tuple("DM", true), tuple("PA", true));

		assertThat(calendar.getQuantity()).isEqualTo("2");
		assertThat(calendar.getExtremeDate()).isEqualTo("2000-2002");

	}

	private void verifyIdentification(DocumentReportModel model) {
		DocumentTransfertModel_Identification identification = model.getIdentificationModel();
		assertThat(identification).isNotNull();

		assertThat(identification.getSentDate()).isEqualTo("2007-10-31");
		assertThat(identification.getBoxNumber()).isEqualTo("30_C_01");
		assertThat(identification.getOrganisationName()).isEmpty();
		assertThat(identification.getPublicOrganisationNumber()).isEmpty();
		assertThat(identification.getAdministrationAddress()).isEmpty();
		assertThat(identification.getResponsible()).isEqualTo("Bob 'Elvis' Gratton");
		assertThat(identification.getFunction()).isNotNull();
		assertThat(identification.getPhoneNumber()).isNotNull();
		assertThat(identification.getEmail()).isEqualTo("bob@doculibre.com");

	}
}