package com.constellio.app.ui.pages.management.printableReport;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.management.Report.ListPrintableReportPresenter;
import com.constellio.app.ui.pages.management.Report.ListPrintableReportView;
import com.constellio.app.ui.pages.management.Report.ListPrintableReportViewImpl;
import com.constellio.app.ui.pages.management.Report.PrintableReportListPossibleType;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ListPrintableReportPresenterAcceptanceTest extends ConstellioTest {

	private static final String STREAMID = ListPrintableReportPresenter.class.getName() + "-Stream";
	private ListPrintableReportPresenter presenter;
	private RMSchemasRecordsServices rm;
	@Mock
	ListPrintableReportView viewMock;
	@Mock
	Navigation navigator;

	private File jasperFile;
	private IOServices ioServices;
	private ContentManager contentManager;

	@Before
	public void setUp() {
		prepareSystem(
				withZeCollection().withConstellioESModule().withConstellioRMModule().withAllTestUsers()
		);
		jasperFile = getTestResourceFile("jasperfile.jrxml");
		ioServices = getModelLayerFactory().getIOServicesFactory().newIOServices();
		navigator = new MockedNavigation();
		viewMock = mock(ListPrintableReportViewImpl.class);
		ConstellioFactories factories = getConstellioFactories();
		when(viewMock.getCollection()).thenReturn(zeCollection);
		when(viewMock.getConstellioFactories()).thenReturn(factories);
		when(viewMock.getSessionContext()).thenReturn(FakeSessionContext.adminInCollection(zeCollection));
		when(viewMock.navigate()).thenReturn(navigator);
		presenter = new ListPrintableReportPresenter(viewMock);
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		contentManager = getModelLayerFactory().getContentManager();
	}

	@Test
	public void testGettingPrintableReportDataProviderForFolder() throws Exception {
		//Prepare data
		Transaction transaction = new Transaction();
		String titleForFolder1 = "title for folder 1";
		String reportTypeForFolder1 = PrintableReportListPossibleType.FOLDER.getSchemaType();
		String schemaForFolder1 = Folder.DEFAULT_SCHEMA;

		String titleForFolder2 = "title for folder 2";
		String reportTypeForFolder2 = PrintableReportListPossibleType.FOLDER.getSchemaType();
		String schemaForFolder2 = Folder.SCHEMA_TYPE + "_meeting";

		String titleForFolder3 = "title for folder 3";
		String reportTypeForFolder3 = PrintableReportListPossibleType.FOLDER.getSchemaType();
		String schemaForFolder3 = Folder.SCHEMA_TYPE + "_employee";

		InputStream jasperInputStream = null;

		try {
			jasperInputStream = ioServices.newFileInputStream(jasperFile, STREAMID);

			ContentVersionDataSummary newFileVersion = contentManager
					.upload(jasperInputStream, "test.jasper").getContentVersionDataSummary();

			Content jasperFileContent = contentManager.createSystemContent("jasperFile.jasper", newFileVersion);

			PrintableReport report1 = rm.newPrintableReport();
			report1.setTitle(titleForFolder1)
					.set(PrintableReport.RECORD_TYPE, reportTypeForFolder1)
					.set(PrintableReport.RECORD_SCHEMA, schemaForFolder1)
					.set(PrintableReport.JASPERFILE, jasperFileContent);

			PrintableReport report2 = rm.newPrintableReport();
			report2.setTitle(titleForFolder2)
					.set(PrintableReport.RECORD_TYPE, reportTypeForFolder2)
					.set(PrintableReport.RECORD_SCHEMA, schemaForFolder2)
					.set(PrintableReport.JASPERFILE, jasperFileContent);

			PrintableReport report3 = rm.newPrintableReport();
			report3.setTitle(titleForFolder3)
					.set(PrintableReport.RECORD_TYPE, reportTypeForFolder3)
					.set(PrintableReport.RECORD_SCHEMA, schemaForFolder3)
					.set(PrintableReport.JASPERFILE, jasperFileContent);

			transaction.addAll(report1, report2, report3);
			getModelLayerFactory().newRecordServices().execute(transaction);

			RecordVODataProvider folderRecordVODataProvider = presenter.getPrintableReportFolderDataProvider();
			List<RecordVO> recordVOList = folderRecordVODataProvider.listRecordVOs(0, folderRecordVODataProvider.size());

			RecordVO firstLabel = recordVOList.get(0);
			assertThat(firstLabel.getTitle()).isEqualTo(titleForFolder1);
			String hash = jasperFileContent.getCurrentVersion().getHash();
			assertThat(((ContentVersionVO) firstLabel.get(PrintableReport.JASPERFILE)).getHash())
					.isEqualTo(hash);
			assertThat(firstLabel.<String>get(PrintableReport.RECORD_TYPE)).isEqualTo(reportTypeForFolder1);
			assertThat(firstLabel.<String>get(PrintableReport.RECORD_SCHEMA)).isEqualTo(schemaForFolder1);

			RecordVO secondLabel = recordVOList.get(1);
			assertThat(secondLabel.getTitle()).isEqualTo(titleForFolder2);
			assertThat(((ContentVersionVO) secondLabel.get(PrintableReport.JASPERFILE)).getHash())
					.isEqualTo(hash);
			assertThat(secondLabel.<String>get(PrintableReport.RECORD_TYPE)).isEqualTo(reportTypeForFolder2);
			assertThat(secondLabel.<String>get(PrintableReport.RECORD_SCHEMA)).isEqualTo(schemaForFolder2);

			RecordVO thirdLabel = recordVOList.get(2);
			assertThat(thirdLabel.getTitle()).isEqualTo(titleForFolder3);
			assertThat(((ContentVersionVO) thirdLabel.get(PrintableReport.JASPERFILE)).getHash())
					.isEqualTo(hash);
			assertThat(thirdLabel.<String>get(PrintableReport.RECORD_TYPE)).isEqualTo(reportTypeForFolder3);
			assertThat(thirdLabel.<String>get(PrintableReport.RECORD_SCHEMA)).isEqualTo(schemaForFolder3);
		} finally {
			ioServices.closeQuietly(jasperInputStream);
		}

	}

	@Test
	public void testGettingPrintableReportDataProviderForDocument() throws Exception {
		//Prepare data
		Transaction transaction = new Transaction();
		//Content jasperFileContentForFolder1 = mock(Content.class);
		String titleForFolder1 = "title for folder 1";
		String reportTypeForFolder1 = PrintableReportListPossibleType.DOCUMENT.getSchemaType();
		String schemaForFolder1 = Document.DEFAULT_SCHEMA;

		//Content jasperFileContentForFolder2 = mock(Content.class);
		String titleForFolder2 = "title for folder 2";
		String reportTypeForFolder2 = PrintableReportListPossibleType.DOCUMENT.getSchemaType();
		String schemaForFolder2 = Document.SCHEMA_TYPE + "_eleve";

		//Content jasperFileContentForFolder3 = mock(Content.class);
		String titleForFolder3 = "title for folder 3";
		String reportTypeForFolder3 = PrintableReportListPossibleType.DOCUMENT.getSchemaType();
		String schemaForFolder3 = Document.SCHEMA_TYPE + "_employe";

		InputStream jasperInputStream = null;

		try {
			jasperInputStream = ioServices.newFileInputStream(jasperFile, STREAMID);

			ContentVersionDataSummary newFileVersion = contentManager
					.upload(jasperInputStream, "test.jasper").getContentVersionDataSummary();

			Content jasperFileContent = contentManager.createSystemContent("jasperFile.jasper", newFileVersion);

			PrintableReport report1 = rm.newPrintableReport();
			report1.setTitle(titleForFolder1)
					.set(PrintableReport.RECORD_TYPE, reportTypeForFolder1)
					.set(PrintableReport.RECORD_SCHEMA, schemaForFolder1)
					.set(PrintableReport.JASPERFILE, jasperFileContent);

			PrintableReport report2 = rm.newPrintableReport();
			report2.setTitle(titleForFolder2)
					.set(PrintableReport.RECORD_TYPE, reportTypeForFolder2)
					.set(PrintableReport.RECORD_SCHEMA, schemaForFolder2)
					.set(PrintableReport.JASPERFILE, jasperFileContent);

			PrintableReport report3 = rm.newPrintableReport();
			report3.setTitle(titleForFolder3)
					.set(PrintableReport.RECORD_TYPE, reportTypeForFolder3)
					.set(PrintableReport.RECORD_SCHEMA, schemaForFolder3)
					.set(PrintableReport.JASPERFILE, jasperFileContent);

			transaction.addAll(report1, report2, report3);
			getModelLayerFactory().newRecordServices().execute(transaction);

			RecordVODataProvider documentDataProvider = presenter.getPrintableReportDocumentDataProvider();
			List<RecordVO> recordVOList = documentDataProvider.listRecordVOs(0, documentDataProvider.size());

			RecordVO firstLabel = recordVOList.get(0);
			String hash = jasperFileContent.getCurrentVersion().getHash();
			assertThat(((ContentVersionVO) firstLabel.get(PrintableReport.JASPERFILE)).getHash())
					.isEqualTo(hash);
			assertThat(firstLabel.getTitle()).isEqualTo(titleForFolder1);
			assertThat(firstLabel.<String>get(PrintableReport.RECORD_TYPE)).isEqualTo(reportTypeForFolder1);
			assertThat(firstLabel.<String>get(PrintableReport.RECORD_SCHEMA)).isEqualTo(schemaForFolder1);

			RecordVO secondLabel = recordVOList.get(1);
			assertThat(((ContentVersionVO) secondLabel.get(PrintableReport.JASPERFILE)).getHash())
					.isEqualTo(hash);
			assertThat(secondLabel.getTitle()).isEqualTo(titleForFolder2);
			assertThat(secondLabel.<String>get(PrintableReport.RECORD_TYPE)).isEqualTo(reportTypeForFolder2);
			assertThat(secondLabel.<String>get(PrintableReport.RECORD_SCHEMA)).isEqualTo(schemaForFolder2);

			RecordVO thirdLabel = recordVOList.get(2);
			assertThat(((ContentVersionVO) thirdLabel.get(PrintableReport.JASPERFILE)).getHash())
					.isEqualTo(hash);
			assertThat(thirdLabel.getTitle()).isEqualTo(titleForFolder3);
			assertThat(thirdLabel.<String>get(PrintableReport.RECORD_TYPE)).isEqualTo(reportTypeForFolder3);
			assertThat(thirdLabel.<String>get(PrintableReport.RECORD_SCHEMA)).isEqualTo(schemaForFolder3);
		} finally {
			ioServices.closeQuietly(jasperInputStream);
		}

	}

	@Test
	public void testGettingPrintableReportDataProviderForTask() throws Exception {
		//Prepare data
		Transaction transaction = new Transaction();
		String titleForFolder1 = "title for folder 1";
		String reportTypeForFolder1 = PrintableReportListPossibleType.TASK.getSchemaType();
		String schemaForFolder1 = Task.DEFAULT_SCHEMA;

		String titleForFolder2 = "title for folder 2";
		String reportTypeForFolder2 = PrintableReportListPossibleType.TASK.getSchemaType();
		String schemaForFolder2 = Task.SCHEMA_TYPE + "_todo";

		String titleForFolder3 = "title for folder 3";
		String reportTypeForFolder3 = PrintableReportListPossibleType.TASK.getSchemaType();
		String schemaForFolder3 = Task.SCHEMA_TYPE + "_done";

		InputStream jasperInputStream = null;

		try {
			jasperInputStream = ioServices.newFileInputStream(jasperFile, STREAMID);

			ContentVersionDataSummary newFileVersion = contentManager
					.upload(jasperInputStream, "test.jasper").getContentVersionDataSummary();

			Content jasperFileContent = contentManager.createSystemContent("jasperFile.jasper", newFileVersion);

			PrintableReport report1 = rm.newPrintableReport();
			report1.setTitle(titleForFolder1)
					.set(PrintableReport.RECORD_TYPE, reportTypeForFolder1)
					.set(PrintableReport.RECORD_SCHEMA, schemaForFolder1)
					.set(PrintableReport.JASPERFILE, jasperFileContent);

			PrintableReport report2 = rm.newPrintableReport();
			report2.setTitle(titleForFolder2)
					.set(PrintableReport.RECORD_TYPE, reportTypeForFolder2)
					.set(PrintableReport.RECORD_SCHEMA, schemaForFolder2)
					.set(PrintableReport.JASPERFILE, jasperFileContent);

			PrintableReport report3 = rm.newPrintableReport();
			report3.setTitle(titleForFolder3)
					.set(PrintableReport.RECORD_TYPE, reportTypeForFolder3)
					.set(PrintableReport.RECORD_SCHEMA, schemaForFolder3)
					.set(PrintableReport.JASPERFILE, jasperFileContent);

			transaction.addAll(report1, report2, report3);
			getModelLayerFactory().newRecordServices().execute(transaction);

			RecordVODataProvider taskRecordVODataProvider = presenter.getPrintableReportTaskDataProvider();
			List<RecordVO> recordVOList = taskRecordVODataProvider.listRecordVOs(0, taskRecordVODataProvider.size());

			RecordVO firstLabel = recordVOList.get(0);
			String hash = jasperFileContent.getCurrentVersion().getHash();
			assertThat(((ContentVersionVO) firstLabel.get(PrintableReport.JASPERFILE)).getHash())
					.isEqualTo(hash);
			assertThat(firstLabel.getTitle()).isEqualTo(titleForFolder1);
			assertThat(firstLabel.<String>get(PrintableReport.RECORD_TYPE)).isEqualTo(reportTypeForFolder1);
			assertThat(firstLabel.<String>get(PrintableReport.RECORD_SCHEMA)).isEqualTo(schemaForFolder1);

			RecordVO secondLabel = recordVOList.get(1);
			assertThat(((ContentVersionVO) firstLabel.get(PrintableReport.JASPERFILE)).getHash())
					.isEqualTo(hash);
			assertThat(secondLabel.getTitle()).isEqualTo(titleForFolder2);
			assertThat(secondLabel.<String>get(PrintableReport.RECORD_TYPE)).isEqualTo(reportTypeForFolder2);
			assertThat(secondLabel.<String>get(PrintableReport.RECORD_SCHEMA)).isEqualTo(schemaForFolder2);

			RecordVO thirdLabel = recordVOList.get(2);
			assertThat(((ContentVersionVO) firstLabel.get(PrintableReport.JASPERFILE)).getHash())
					.isEqualTo(hash);
			assertThat(thirdLabel.getTitle()).isEqualTo(titleForFolder3);
			assertThat(thirdLabel.<String>get(PrintableReport.RECORD_TYPE)).isEqualTo(reportTypeForFolder3);
			assertThat(thirdLabel.<String>get(PrintableReport.RECORD_SCHEMA)).isEqualTo(schemaForFolder3);
		} finally {
			ioServices.closeQuietly(jasperInputStream);
		}
	}

	@Test
	public void testRemoveItems() throws Exception {
		Transaction transaction = new Transaction();
		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		InputStream jasperInputStream = null;

		try {
			jasperInputStream = ioServices.newFileInputStream(jasperFile, STREAMID);

			ContentVersionDataSummary newFileVersion = contentManager
					.upload(jasperInputStream, "test.jasper").getContentVersionDataSummary();

			Content jasperFileContent = contentManager.createSystemContent("jasperFile.jasper", newFileVersion);

			PrintableReport reportToRemoveByIndex = rm.newPrintableReport();
			reportToRemoveByIndex.setTitle("testToRemoveByIndex")
					.set(PrintableReport.RECORD_TYPE, PrintableReportListPossibleType.FOLDER.getSchemaType())
					.set(PrintableReport.RECORD_SCHEMA, Folder.DEFAULT_SCHEMA)
					.set(PrintableReport.JASPERFILE, jasperFileContent);

			PrintableReport reportToRemoveById = rm.newPrintableReport();
			reportToRemoveById.setTitle("testToRemoveById")
					.set(PrintableReport.RECORD_TYPE, PrintableReportListPossibleType.FOLDER.getSchemaType())
					.set(PrintableReport.RECORD_SCHEMA, Folder.DEFAULT_SCHEMA)
					.set(PrintableReport.JASPERFILE, jasperFileContent);

			transaction.addAll(reportToRemoveById, reportToRemoveByIndex);
			getModelLayerFactory().newRecordServices().execute(transaction);

			RecordVODataProvider recordVODataProvider = presenter.getPrintableReportFolderDataProvider();
			List<RecordVO> recordVOS = recordVODataProvider.listRecordVOs(0, recordVODataProvider.size());

			presenter.removeRecord(recordVOS.get(0));
			try {
				recordServices.getDocumentById(recordVOS.get(0).getId());
				fail();
			} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) { /* OK ! */}

			for (RecordVO recordVO : recordVOS) {
				if (recordVO.getId().equals(reportToRemoveById.getId())) {
					presenter.removeRecord(recordVO);
				}
			}

			try {
				recordServices.getDocumentById(reportToRemoveById.getId());
				fail();
			} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) { /* OK ! */}
		} finally {
			ioServices.closeQuietly(jasperInputStream);
		}
	}
}
