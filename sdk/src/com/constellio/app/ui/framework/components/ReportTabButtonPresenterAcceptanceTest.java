package com.constellio.app.ui.framework.components;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.enums.TemplateVersionType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.reports.printable.PrintableExtension;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.management.Report.PrintableReportListPossibleType;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReportTabButtonPresenterAcceptanceTest extends ConstellioTest {
	private static final String STREAMID = ReportTabButtonPresenterAcceptanceTest.class.getName() + "-Stream";
	private RMTestRecords records = new RMTestRecords(zeCollection);
	private SessionContext sessionContext;
	private ReportTabButtonPresenter presenter;
	private RMSchemasRecordsServices rm;
	private ReportTabButton view;
	private File jasperFile;
	private IOServices ioServices;
	private ContentManager contentManager;

	@Before
	public void SetUp() {
		prepareSystem(
				withZeCollection().withConstellioESModule().withTasksModule().withConstellioRMModule().withAllTestUsers()
						.withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withEvents().withDocumentsHavingContent()
		);
		sessionContext = FakeSessionContext.adminInCollection(zeCollection);
		view = mock(ReportTabButton.class);
		when(view.getFactory()).thenReturn(getAppLayerFactory());
		when(view.getSessionContext()).thenReturn(sessionContext);
		when(view.getCollection()).thenReturn(zeCollection);
		presenter = new ReportTabButtonPresenter(view);
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		contentManager = getModelLayerFactory().getContentManager();
		jasperFile = getTestResourceFile("jasperfile.jrxml");
		ioServices = getModelLayerFactory().getIOServicesFactory().newIOServices();
	}

	@Test
	public void checkIfGetAllGeneralSchemaReturnTheCorrectSchema() {
		RecordToVOBuilder builder = new RecordToVOBuilder();
		RecordVO[] allRecord = new RecordVO[]{
				builder.build(records.getFolder_A01().getWrappedRecord(), RecordVO.VIEW_MODE.DISPLAY, sessionContext),
				builder.build(records.getFolder_A42().getWrappedRecord(), RecordVO.VIEW_MODE.DISPLAY, sessionContext),
				builder.build(records.getDocumentWithContent_A19().getWrappedRecord(), RecordVO.VIEW_MODE.DISPLAY, sessionContext),
				};
		presenter.setRecordVoList(allRecord);
		List<PrintableReportListPossibleType> generalSchema = presenter.getAllGeneralSchema();
		assertThat(generalSchema).containsOnly(PrintableReportListPossibleType.FOLDER, PrintableReportListPossibleType.DOCUMENT);

		Task task = rm.newRMTask();

		RecordVO[] onlyRecordWithTask = new RecordVO[]{
				builder.build(task.getWrappedRecord(), RecordVO.VIEW_MODE.DISPLAY, sessionContext)
		};
		presenter = new ReportTabButtonPresenter(view);
		presenter.setRecordVoList(onlyRecordWithTask);
		List<PrintableReportListPossibleType> generalSchemaWithOnlyTask = presenter.getAllGeneralSchema();
		assertThat(generalSchemaWithOnlyTask).containsOnly(PrintableReportListPossibleType.TASK);
	}

	@Test
	public void checkIfGetAllCustomSchemaReturnTheCorrectSchemaForFolder() throws Exception {
		final String customSchemaName = "CustomSchemaName";
		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(Folder.SCHEMA_TYPE).createCustomSchema(customSchemaName);
			}
		});
		RecordToVOBuilder builder = new RecordToVOBuilder();
		Folder record = records.getFolder_C30();
		Transaction t = new Transaction();
		t.add(record);
		getModelLayerFactory().newRecordServices().execute(t);
		RecordVO[] allRecord = new RecordVO[]{
				builder.build(records.getFolder_A01().getWrappedRecord(), RecordVO.VIEW_MODE.DISPLAY, sessionContext),
				builder.build(records.getFolder_A42().getWrappedRecord(), RecordVO.VIEW_MODE.DISPLAY, sessionContext),
				builder.build(record.getWrappedRecord(), RecordVO.VIEW_MODE.DISPLAY, sessionContext),
				};
		presenter.setRecordVoList(allRecord);

		List<MetadataSchemaVO> metadataSchemaVOS = presenter.getAllCustomSchema(PrintableReportListPossibleType.FOLDER);
		assertThat(metadataSchemaVOS).hasSize(1).extracting("code").containsOnly(Folder.DEFAULT_SCHEMA);
	}

	@Test
	public void checkIfGetAllReportReturnsCorrectReport() throws Exception {
		InputStream jasperInputStream = null;

		try {
			jasperInputStream = ioServices.newFileInputStream(jasperFile, STREAMID);

			ContentVersionDataSummary newFileVersion = contentManager
					.upload(jasperInputStream, "test.jasper").getContentVersionDataSummary();

			Content jasperFileContent = contentManager.createSystemContent("jasperFile.jasper", newFileVersion);

			PrintableReport defaultFolderReport = rm.newPrintableReport();
			PrintableReport defaultDocumentReport = rm.newPrintableReport();
			defaultDocumentReport.setTitle("default document report").set(PrintableReport.RECORD_TYPE, Document.SCHEMA_TYPE)
					.set(PrintableReport.RECORD_SCHEMA, Document.DEFAULT_SCHEMA)
					.set(PrintableReport.JASPERFILE, jasperFileContent)
					.set(PrintableReport.SUPPORTED_EXTENSIONS, Arrays.asList(PrintableExtension.PDF))
					.set(PrintableReport.TEMPLATE_VERSION, TemplateVersionType.CONSTELLIO_5);
			defaultFolderReport.setTitle("default folder report").set(PrintableReport.RECORD_TYPE, Folder.SCHEMA_TYPE)
					.set(PrintableReport.RECORD_SCHEMA, Folder.DEFAULT_SCHEMA)
					.set(PrintableReport.JASPERFILE, jasperFileContent)
					.set(PrintableReport.SUPPORTED_EXTENSIONS, Arrays.asList(PrintableExtension.PDF))
					.set(PrintableReport.TEMPLATE_VERSION, TemplateVersionType.CONSTELLIO_5);

			Transaction t = new Transaction();
			t.addAll(defaultFolderReport, defaultDocumentReport);
			getModelLayerFactory().newRecordServices().execute(t);

			presenter = new ReportTabButtonPresenter(view);

			MetadataSchemaToVOBuilder builder = new MetadataSchemaToVOBuilder();
			List<RecordVO> availableReports = presenter
					.getAllAvailableReport(builder.build(rm.defaultFolderSchema(), RecordVO.VIEW_MODE.DISPLAY, sessionContext));
			assertThat(availableReports).extracting("id").contains(defaultFolderReport.getId());
		} finally {
			ioServices.closeQuietly(jasperInputStream);
		}
	}

	@Test
	public void checkIfThereIsReportThatBothAreReturned() throws Exception {
		InputStream jasperInputStream = null;

		try {
			jasperInputStream = ioServices.newFileInputStream(jasperFile, STREAMID);

			ContentVersionDataSummary newFileVersion = contentManager
					.upload(jasperInputStream, "test.jasper").getContentVersionDataSummary();

			Content jasperFileContent = contentManager.createSystemContent("jasperFile.jasper", newFileVersion);

			PrintableReport report1 = rm.newPrintableReport();
			PrintableReport report2 = rm.newPrintableReport();
			report1.setTitle("report 1").set(PrintableReport.RECORD_TYPE, Folder.SCHEMA_TYPE)
					.set(PrintableReport.RECORD_SCHEMA, Folder.DEFAULT_SCHEMA)
					.set(PrintableReport.JASPERFILE, jasperFileContent)
					.set(PrintableReport.SUPPORTED_EXTENSIONS, Arrays.asList(PrintableExtension.PDF))
					.set(PrintableReport.TEMPLATE_VERSION, TemplateVersionType.CONSTELLIO_5);
			report2.setTitle("report 2").set(PrintableReport.RECORD_TYPE, Folder.SCHEMA_TYPE)
					.set(PrintableReport.RECORD_SCHEMA, Folder.DEFAULT_SCHEMA)
					.set(PrintableReport.JASPERFILE, jasperFileContent)
					.set(PrintableReport.SUPPORTED_EXTENSIONS, Arrays.asList(PrintableExtension.PDF))
					.set(PrintableReport.TEMPLATE_VERSION, TemplateVersionType.CONSTELLIO_5);

			Transaction t = new Transaction();
			t.addAll(report1, report2);
			getModelLayerFactory().newRecordServices().execute(t);

			presenter = new ReportTabButtonPresenter(view);

			MetadataSchemaToVOBuilder builder = new MetadataSchemaToVOBuilder();
			List<RecordVO> availableReports = presenter
					.getAllAvailableReport(builder.build(rm.defaultFolderSchema(), RecordVO.VIEW_MODE.DISPLAY, sessionContext));
			assertThat(availableReports).extracting("id").contains(report1.getId(), report2.getId());
		} finally {
			ioServices.closeQuietly(jasperInputStream);
		}
	}

}
