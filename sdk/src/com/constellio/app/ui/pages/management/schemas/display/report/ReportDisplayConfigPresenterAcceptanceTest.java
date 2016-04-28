package com.constellio.app.ui.pages.management.schemas.display.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.sdk.tests.MockedNavigation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.reports.model.search.ReportTestUtils;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.ReportVO;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.reports.ReportServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.ConstellioTest;

public class ReportDisplayConfigPresenterAcceptanceTest extends ConstellioTest {
	RMTestRecords records = new RMTestRecords(zeCollection);

	ReportDisplayConfigPresenter presenter;
	ReportTestUtils reportTestUtils;
	@Mock
	ReportConfigurationView view;
	@Mock
	SessionContext session;
	@Mock
	UserVO currentUser;
	MockedNavigation navigator;
	private String zeReportTitle = "report title";
	private ReportServices reportServices;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withAllTestUsers()
		);

		navigator = new MockedNavigation();

		when(view.getSessionContext()).thenReturn(session);
		when(view.getCollection()).thenReturn(zeCollection);
		when(session.getCurrentCollection()).thenReturn(zeCollection);
		when(currentUser.getUsername()).thenReturn(admin);
		when(session.getCurrentUser()).thenReturn(currentUser);
		when(view.navigate()).thenReturn(navigator);

		presenter = new ReportDisplayConfigPresenter(view);
		Map<String, String> params = new HashMap<>();
		params.put("schemaTypeCode", Folder.SCHEMA_TYPE);
		presenter.setParameters(params);
		reportTestUtils = new ReportTestUtils(getModelLayerFactory(), zeCollection, records);
		reportServices = new ReportServices(getModelLayerFactory(), zeCollection);
	}

	@Test
	public void whenDefaultFolderReportAndChuckReportThenReturnDefaultReports() {
		reportTestUtils.addDefaultReport(zeReportTitle);
		reportTestUtils.addUserReport(zeReportTitle, chuckNorris);
		List<ReportVO> reports = presenter.getReports();
		assertThat(reports.size()).isEqualTo(1);
		reportTestUtils.validateDefaultReport(reports.get(0));
	}

	@Test
	public void whenNoDefaultFolderReportAndChuckReportThenNoReport() {
		reportTestUtils.addUserReport(zeReportTitle, chuckNorris);
		List<ReportVO> reports = presenter.getReports();
		assertThat(reports).isEmpty();
	}

	@Test
	public void whenDefaultFolderAndDefaultDocumentReportThenDefaultFolderReportForFoldersSchemaType() {
		reportTestUtils.addDefaultReport(zeReportTitle);
		reportTestUtils.addDocumentDefaultReport(zeReportTitle);
		List<ReportVO> reports = presenter.getReports();
		assertThat(reports.size()).isEqualTo(1);
		reportTestUtils.validateDefaultReport(reports.get(0));
	}

	@Test
	public void whenNewReportSavedThenSavedCorrectly() {
		String newReportTitle = "newReport";
		when(view.getSelectedReport()).thenReturn(newReportTitle);
		Metadata folderTitleMetadata = getFolderLinearSizeMetadata();
		List<MetadataVO> metadataVOList = new ArrayList();
		MetadataVO firstMD = new MetadataToVOBuilder().build(folderTitleMetadata, session);
		metadataVOList.add(firstMD);
		presenter.saveButtonClicked(metadataVOList);
		Report report = reportServices.getReport(Folder.SCHEMA_TYPE, newReportTitle);
		assertThat(report).isNotNull();
		assertThat(report.getReportedMetadata().size()).isEqualTo(1);
		assertThat(report.getReportedMetadata().get(0).getMetadataCode()).isEqualTo(folderTitleMetadata.getCode());
	}

	private Metadata getFolderLinearSizeMetadata() {
		MetadataSchemasManager metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(zeCollection);
		MetadataSchema folderDefaultSchema = types.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema();
		return folderDefaultSchema.getMetadata(Folder.LINEAR_SIZE);
	}

	@Test
	public void whenReportUpdatedThenUpdatedCorrectly() {
		reportTestUtils.addDefaultReport(zeReportTitle);
		when(view.getSelectedReport()).thenReturn(zeReportTitle);
		Metadata folderTitleMetadata = getFolderLinearSizeMetadata();
		List<MetadataVO> metadataVOList = new ArrayList();
		MetadataVO firstMD = new MetadataToVOBuilder().build(folderTitleMetadata, session);
		metadataVOList.add(firstMD);
		presenter.saveButtonClicked(metadataVOList);
		Report report = reportServices.getReport(Folder.SCHEMA_TYPE, zeReportTitle);
		assertThat(report).isNotNull();
		assertThat(report.getReportedMetadata().size()).isEqualTo(1);
		assertThat(report.getReportedMetadata().get(0).getMetadataCode()).isEqualTo(folderTitleMetadata.getCode());
	}

	@Test
	public void whenGetReportMetadataForNewReportThenNoMetadata() {
		when(view.getSelectedReport()).thenReturn(null);
		assertThat(presenter.getReportMetadatas()).isEmpty();
	}

	@Test
	public void whenGetReportMetadataForExistingReportThenReturnExistingReportMetadata() {
		reportTestUtils.addDefaultReport(zeReportTitle);
		when(view.getSelectedReport()).thenReturn(zeReportTitle);
		List<MetadataVO> reportMetadataList = presenter.getReportMetadatas();
		reportTestUtils.validateDefaultReport(reportMetadataList);
	}

}
