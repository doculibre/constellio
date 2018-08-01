package com.constellio.app.ui.pages.management.printableReport;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.management.Report.AddEditPrintableReportPresenter;
import com.constellio.app.ui.pages.management.Report.AddEditPrintableReportView;
import com.constellio.app.ui.pages.management.Report.PrintableReportListPossibleType;
import com.constellio.model.entities.records.Transaction;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AddEditPrintableReportPresenterAcceptanceTest extends ConstellioTest {
	private AddEditPrintableReportPresenter presenter;
	private RMSchemasRecordsServices rm;
	@Mock
	AddEditPrintableReportView viewMock;
	@Mock
	Navigation navigator;


	@Before
	public void setUp() {
		prepareSystem(
				withZeCollection().withConstellioESModule().withConstellioRMModule().withAllTestUsers()
		);

		navigator = new MockedNavigation();
		viewMock = mock(AddEditPrintableReportView.class);
		ConstellioFactories factories = getConstellioFactories();
		when(viewMock.getCollection()).thenReturn(zeCollection);
		when(viewMock.getConstellioFactories()).thenReturn(factories);
		when(viewMock.getSessionContext()).thenReturn(FakeSessionContext.adminInCollection(zeCollection));
		when(viewMock.navigate()).thenReturn(navigator);
		presenter = new AddEditPrintableReportPresenter(viewMock);
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
	}

	@Test
	public void testGetRecordVO() throws Exception {
		Transaction transaction = new Transaction();
		String titleForFolder = "title 1";
		String reportTypeForFolder = PrintableReportListPossibleType.FOLDER.getSchemaType();
		String schemaForFolder = Folder.DEFAULT_SCHEMA;

		PrintableReport report = rm.newPrintableReport();
		report.setTitle(titleForFolder)
				.set(PrintableReport.RECORD_TYPE, reportTypeForFolder)
				.set(PrintableReport.RECORD_SCHEMA, schemaForFolder);
		transaction.add(report);
		getModelLayerFactory().newRecordServices().execute(transaction);

		RecordVO recordVO = presenter.getRecordVO(report.getId());

		assertThat(recordVO.getTitle()).isEqualTo(report.getTitle());
		assertThat(recordVO.<String>get(PrintableReport.RECORD_TYPE)).isEqualTo(report.get(PrintableReport.RECORD_TYPE));
		assertThat(recordVO.<String>get(PrintableReport.RECORD_SCHEMA)).isEqualTo(report.get(PrintableReport.RECORD_SCHEMA));
		assertThat(recordVO.getId()).isEqualTo(report.getId());
	}
}
