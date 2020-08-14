package com.constellio.app.ui.pages.management.ExcelReport;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.reports.ReportServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;
import com.constellio.sdk.tests.schemas.FakeDataStoreTypeFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ListExcelReportPresenterAcceptanceTest extends ConstellioTest {
	ListExcelReportPresenter presenter;
	@Mock
	ListExcelReportView view;
	@Mock
	MockedNavigation navigator;

	RMTestRecords records = new RMTestRecords(zeCollection);
	RMSchemasRecordsServices rm;

	@Before
	public void setup() {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records).withFoldersAndContainersOfEveryStatus()
						.withDocumentsHavingContent().withConstellioESModule()
		);

		navigator = new MockedNavigation();
		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.getSessionContext()).thenReturn(FakeSessionContext.adminInCollection(zeCollection));
		when(view.navigate()).thenReturn(navigator);
		when(view.getCollection()).thenReturn(zeCollection);
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		presenter = new ListExcelReportPresenter(view);
	}

	@Test
	public void whenGeneratingTabCheckIfAllReportTypeAreThereTest() {
		Map<String, String> possibleReportType = presenter.initPossibleTab(Locale.FRENCH);
		assertThat(possibleReportType.keySet())
				.contains("connectorHttpDocument", "connectorLdapUserDocument", "connectorSmbDocument",
						"containerRecord", "document", "folder", "storageSpace", "userTask");
		assertThat(possibleReportType.values())
				.contains("Document sur Internet", "Utilisateur Connecteur LDAP", "Document sur un partage réseau",
						"Contenant", "Document", "Dossier", "Emplacement", "Tâche");
	}

	@Test
	public void whenGettingTheDataProviderForEachSchemaCheckIfItReturnsTheCorrectReport() {
		ReportServices reportServices = new ReportServices(getModelLayerFactory(), zeCollection);
		List<String> possibleSchema = asList("containerRecord", "document", "connectorHttpDocument", "connectorSmbDocument",
				"folder", "storageSpace", "userTask", "connectorLdapUserDocument");
		MetadataSchemaTypes metadataSchemaTypes = getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection)
				.build(new FakeDataStoreTypeFactory());
		int compteur = 0;
		for (String schema : possibleSchema) {
			String titleTest = "test " + (++compteur);
			Report report = rm.newReport();
			report.setSchemaTypeCode(schema);
			report.setLinesCount(1);
			report.setColumnsCount(10);
			report.setTitle(titleTest);
			reportServices.saveReport(records.getAdmin(), report);
			RecordVODataProvider provider = presenter.getDataProviderForSchemaType(schema);
			assertThat(provider.getRecordVO(0).getTitle()).isEqualTo(report.getTitle());
		}
	}

	@Test
	public void testRemovingAReport() {
		ReportServices reportServices = new ReportServices(getModelLayerFactory(), zeCollection);
		Report report = rm.newReport();
		report.setColumnsCount(1);
		report.setLinesCount(2);
		report.setTitle("test");
		report.setSchemaTypeCode("folder");
		reportServices.saveReport(records.getAdmin(), report);

		presenter.removeRecord(report.getId(), "folder");
		assertThat(reportServices.getRecordById(report.getId())).isNull();
	}
}
