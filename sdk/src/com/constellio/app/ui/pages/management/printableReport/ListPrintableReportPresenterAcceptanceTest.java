package com.constellio.app.ui.pages.management.printableReport;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.management.Report.ListPrintableReportPresenter;
import com.constellio.app.ui.pages.management.Report.ListPrintableReportView;
import com.constellio.app.ui.pages.management.Report.ListPrintableReportViewImpl;
import com.constellio.app.ui.pages.management.Report.PrintableReportListPossibleType;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ListPrintableReportPresenterAcceptanceTest extends ConstellioTest {
    private ListPrintableReportPresenter presenter;
    private RMSchemasRecordsServices rm;
    @Mock
    ListPrintableReportView viewMock;
    @Mock
    Navigation navigator;

    @Before
    public void setUp() {
        prepareSystem(
                withZeCollection().withConstellioESModule().withConstellioRMModule().withAllTestUsers()
        );

        navigator = new MockedNavigation();
        viewMock = mock(ListPrintableReportViewImpl.class);
        ConstellioFactories factories = getConstellioFactories();
        when(viewMock.getCollection()).thenReturn(zeCollection);
        when(viewMock.getConstellioFactories()).thenReturn(factories);
        when(viewMock.getSessionContext()).thenReturn(FakeSessionContext.adminInCollection(zeCollection));
        when(viewMock.navigate()).thenReturn(navigator);
        presenter = new ListPrintableReportPresenter(viewMock);
        rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
    }

    @Test
    public void testGettingPrintableReportDataProviderForFolder() throws Exception {
        //Prepare data
        Transaction transaction = new Transaction();
        //Content jasperFileContentForFolder1 = mock(Content.class);
        String titleForFolder1 = "title for folder 1";
        String reportTypeForFolder1 = PrintableReportListPossibleType.FOLDER.toString();
        String schemaForFolder1 = Folder.DEFAULT_SCHEMA;

        //Content jasperFileContentForFolder2 = mock(Content.class);
        String titleForFolder2 = "title for folder 2";
        String reportTypeForFolder2 = PrintableReportListPossibleType.FOLDER.toString();
        String schemaForFolder2 = Folder.SCHEMA_TYPE + "_meeting";

        //Content jasperFileContentForFolder3 = mock(Content.class);
        String titleForFolder3 = "title for folder 3";
        String reportTypeForFolder3 = PrintableReportListPossibleType.FOLDER.toString();
        String schemaForFolder3 = Folder.SCHEMA_TYPE + "_employee";


        PrintableReport report1 = rm.newPrintableReport();
        report1.setTitle(titleForFolder1)
                .set(PrintableReport.RECORD_TYPE, reportTypeForFolder1)
                .set(PrintableReport.RECORD_SCHEMA, schemaForFolder1);

        PrintableReport report2 = rm.newPrintableReport();
        report2.setTitle(titleForFolder2)
                .set(PrintableReport.RECORD_TYPE, reportTypeForFolder2)
                .set(PrintableReport.RECORD_SCHEMA, schemaForFolder2);

        PrintableReport report3 = rm.newPrintableReport();
        report3.setTitle(titleForFolder3)
                .set(PrintableReport.RECORD_TYPE, reportTypeForFolder3)
                .set(PrintableReport.RECORD_SCHEMA, schemaForFolder3);

        transaction.addAll(report1, report2, report3);
        getModelLayerFactory().newRecordServices().execute(transaction);

        RecordVODataProvider folderRecordVODataProvider = presenter.getPrintableReportFolderDataProvider();
        List<RecordVO> recordVOList = folderRecordVODataProvider.listRecordVOs(0, folderRecordVODataProvider.size());

        RecordVO firstLabel = recordVOList.get(0);
        assertThat(firstLabel.getTitle()).isEqualTo(titleForFolder1);
        //assertThat(firstLabel.get(PrintableReport.JASPERFILE)).isEqualTo(jasperFileContentForFolder1);
        assertThat(firstLabel.get(PrintableReport.RECORD_TYPE)).isEqualTo(reportTypeForFolder1);
        assertThat(firstLabel.get(PrintableReport.RECORD_SCHEMA)).isEqualTo(schemaForFolder1);

        RecordVO secondLabel = recordVOList.get(1);
        assertThat(secondLabel.getTitle()).isEqualTo(titleForFolder2);
        //assertThat(secondLabel.get(PrintableReport.JASPERFILE)).isEqualTo(jasperFileContentForFolder2);
        assertThat(secondLabel.get(PrintableReport.RECORD_TYPE)).isEqualTo(reportTypeForFolder2);
        assertThat(secondLabel.get(PrintableReport.RECORD_SCHEMA)).isEqualTo(schemaForFolder2);

        RecordVO thirdLabel = recordVOList.get(2);
        assertThat(thirdLabel.getTitle()).isEqualTo(titleForFolder3);
        //assertThat(thirdLabel.get(PrintableReport.JASPERFILE)).isEqualTo(jasperFileContentForFolder3);
        assertThat(thirdLabel.get(PrintableReport.RECORD_TYPE)).isEqualTo(reportTypeForFolder3);
        assertThat(thirdLabel.get(PrintableReport.RECORD_SCHEMA)).isEqualTo(schemaForFolder3);
    }

    @Test
    public void testGettingPrintableReportDataProviderForDocument() throws Exception {
        //Prepare data
        Transaction transaction = new Transaction();
        //Content jasperFileContentForFolder1 = mock(Content.class);
        String titleForFolder1 = "title for folder 1";
        String reportTypeForFolder1 = PrintableReportListPossibleType.DOCUMENT.toString();
        String schemaForFolder1 = Document.DEFAULT_SCHEMA;

        //Content jasperFileContentForFolder2 = mock(Content.class);
        String titleForFolder2 = "title for folder 2";
        String reportTypeForFolder2 = PrintableReportListPossibleType.DOCUMENT.toString();
        String schemaForFolder2 = Document.SCHEMA_TYPE + "_eleve";

        //Content jasperFileContentForFolder3 = mock(Content.class);
        String titleForFolder3 = "title for folder 3";
        String reportTypeForFolder3 = PrintableReportListPossibleType.DOCUMENT.toString();
        String schemaForFolder3 = Document.SCHEMA_TYPE + "_employe";


        PrintableReport report1 = rm.newPrintableReport();
        report1.setTitle(titleForFolder1)
                .set(PrintableReport.RECORD_TYPE, reportTypeForFolder1)
                .set(PrintableReport.RECORD_SCHEMA, schemaForFolder1);

        PrintableReport report2 = rm.newPrintableReport();
        report2.setTitle(titleForFolder2)
                .set(PrintableReport.RECORD_TYPE, reportTypeForFolder2)
                .set(PrintableReport.RECORD_SCHEMA, schemaForFolder2);

        PrintableReport report3 = rm.newPrintableReport();
        report3.setTitle(titleForFolder3)
                .set(PrintableReport.RECORD_TYPE, reportTypeForFolder3)
                .set(PrintableReport.RECORD_SCHEMA, schemaForFolder3);

        transaction.addAll(report1, report2, report3);
        getModelLayerFactory().newRecordServices().execute(transaction);

        RecordVODataProvider documentDataProvider = presenter.getPrintableReportDocumentDataProvider();
        List<RecordVO> recordVOList = documentDataProvider.listRecordVOs(0, documentDataProvider.size());

        RecordVO firstLabel = recordVOList.get(0);
        assertThat(firstLabel.getTitle()).isEqualTo(titleForFolder1);
        //assertThat(firstLabel.get(PrintableReport.JASPERFILE)).isEqualTo(jasperFileContentForFolder1);
        assertThat(firstLabel.get(PrintableReport.RECORD_TYPE)).isEqualTo(reportTypeForFolder1);
        assertThat(firstLabel.get(PrintableReport.RECORD_SCHEMA)).isEqualTo(schemaForFolder1);

        RecordVO secondLabel = recordVOList.get(1);
        assertThat(secondLabel.getTitle()).isEqualTo(titleForFolder2);
        //assertThat(secondLabel.get(PrintableReport.JASPERFILE)).isEqualTo(jasperFileContentForFolder2);
        assertThat(secondLabel.get(PrintableReport.RECORD_TYPE)).isEqualTo(reportTypeForFolder2);
        assertThat(secondLabel.get(PrintableReport.RECORD_SCHEMA)).isEqualTo(schemaForFolder2);

        RecordVO thirdLabel = recordVOList.get(2);
        assertThat(thirdLabel.getTitle()).isEqualTo(titleForFolder3);
        //assertThat(thirdLabel.get(PrintableReport.JASPERFILE)).isEqualTo(jasperFileContentForFolder3);
        assertThat(thirdLabel.get(PrintableReport.RECORD_TYPE)).isEqualTo(reportTypeForFolder3);
        assertThat(thirdLabel.get(PrintableReport.RECORD_SCHEMA)).isEqualTo(schemaForFolder3);
    }

    @Test
    public void testGettingPrintableReportDataProviderForTask() throws Exception {
        //Prepare data
        Transaction transaction = new Transaction();
        //Content jasperFileContentForFolder1 = mock(Content.class);
        String titleForFolder1 = "title for folder 1";
        String reportTypeForFolder1 = PrintableReportListPossibleType.TASK.toString();
        String schemaForFolder1 = Task.DEFAULT_SCHEMA;

        //Content jasperFileContentForFolder2 = mock(Content.class);
        String titleForFolder2 = "title for folder 2";
        String reportTypeForFolder2 = PrintableReportListPossibleType.TASK.toString();
        String schemaForFolder2 = Task.SCHEMA_TYPE + "_todo";

        //Content jasperFileContentForFolder3 = mock(Content.class);
        String titleForFolder3 = "title for folder 3";
        String reportTypeForFolder3 = PrintableReportListPossibleType.TASK.toString();
        String schemaForFolder3 = Task.SCHEMA_TYPE + "_done";


        PrintableReport report1 = rm.newPrintableReport();
        report1.setTitle(titleForFolder1)
                .set(PrintableReport.RECORD_TYPE, reportTypeForFolder1)
                .set(PrintableReport.RECORD_SCHEMA, schemaForFolder1);

        PrintableReport report2 = rm.newPrintableReport();
        report2.setTitle(titleForFolder2)
                .set(PrintableReport.RECORD_TYPE, reportTypeForFolder2)
                .set(PrintableReport.RECORD_SCHEMA, schemaForFolder2);

        PrintableReport report3 = rm.newPrintableReport();
        report3.setTitle(titleForFolder3)
                .set(PrintableReport.RECORD_TYPE, reportTypeForFolder3)
                .set(PrintableReport.RECORD_SCHEMA, schemaForFolder3);

        transaction.addAll(report1, report2, report3);
        getModelLayerFactory().newRecordServices().execute(transaction);

        RecordVODataProvider taskRecordVODataProvider = presenter.getPrintableReportTaskDataProvider();
        List<RecordVO> recordVOList = taskRecordVODataProvider.listRecordVOs(0, taskRecordVODataProvider.size());

        RecordVO firstLabel = recordVOList.get(0);
        assertThat(firstLabel.getTitle()).isEqualTo(titleForFolder1);
        //assertThat(firstLabel.get(PrintableReport.JASPERFILE)).isEqualTo(jasperFileContentForFolder1);
        assertThat(firstLabel.get(PrintableReport.RECORD_TYPE)).isEqualTo(reportTypeForFolder1);
        assertThat(firstLabel.get(PrintableReport.RECORD_SCHEMA)).isEqualTo(schemaForFolder1);

        RecordVO secondLabel = recordVOList.get(1);
        assertThat(secondLabel.getTitle()).isEqualTo(titleForFolder2);
        //assertThat(secondLabel.get(PrintableReport.JASPERFILE)).isEqualTo(jasperFileContentForFolder2);
        assertThat(secondLabel.get(PrintableReport.RECORD_TYPE)).isEqualTo(reportTypeForFolder2);
        assertThat(secondLabel.get(PrintableReport.RECORD_SCHEMA)).isEqualTo(schemaForFolder2);

        RecordVO thirdLabel = recordVOList.get(2);
        assertThat(thirdLabel.getTitle()).isEqualTo(titleForFolder3);
        //assertThat(thirdLabel.get(PrintableReport.JASPERFILE)).isEqualTo(jasperFileContentForFolder3);
        assertThat(thirdLabel.get(PrintableReport.RECORD_TYPE)).isEqualTo(reportTypeForFolder3);
        assertThat(thirdLabel.get(PrintableReport.RECORD_SCHEMA)).isEqualTo(schemaForFolder3);
    }

    @Test
    public void testRemoveItems() throws Exception {
        Transaction transaction = new Transaction();
        RecordServices recordServices = getModelLayerFactory().newRecordServices();

        PrintableReport reportToRemoveByIndex = rm.newPrintableReport();
        reportToRemoveByIndex.setTitle("testToRemoveByIndex")
                .set(PrintableReport.RECORD_TYPE, PrintableReportListPossibleType.FOLDER.toString())
                .set(PrintableReport.RECORD_SCHEMA, Folder.DEFAULT_SCHEMA);

        PrintableReport reportToRemoveById = rm.newPrintableReport();
        reportToRemoveById.setTitle("testToRemoveById")
                .set(PrintableReport.RECORD_TYPE, PrintableReportListPossibleType.FOLDER.toString())
                .set(PrintableReport.RECORD_SCHEMA, Folder.DEFAULT_SCHEMA);


        transaction.addAll(reportToRemoveById, reportToRemoveByIndex);
        getModelLayerFactory().newRecordServices().execute(transaction);

        RecordVODataProvider recordVODataProvider = presenter.getPrintableReportFolderDataProvider();
        List<RecordVO> recordVOS = recordVODataProvider.listRecordVOs(0, recordVODataProvider.size());

        presenter.removeRecord(0 + "", PrintableReportListPossibleType.FOLDER);
        try {
            recordServices.getDocumentById(recordVOS.get(0).getId());
            fail();
        } catch (RecordServicesRuntimeException.NoSuchRecordWithId e) { /* OK ! */}


        presenter.removeRecord(reportToRemoveById.getId(), PrintableReportListPossibleType.FOLDER);
        try {
            recordServices.getDocumentById(reportToRemoveById.getId());
            fail();
        } catch (RecordServicesRuntimeException.NoSuchRecordWithId e) { /* OK ! */}
    }
}
