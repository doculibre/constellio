package com.constellio.app.ui.pages.management.schemas;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class ListSchemaTypePresenterAcceptanceTest extends ConstellioTest {

	@Mock MetadataSchemaTypeVO schemaVO;
	ListSchemaTypePresenter presenter;
	@Mock ListSchemaTypeViewImpl view;

	RMTestRecords records = new RMTestRecords(zeCollection);

	public static final String INPUT_STREAM_NAME = "inputstream_" + ListSchemaTypePresenterAcceptanceTest.class.getName();

	@Before
	public void setUp() {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		when(view.getSessionContext()).thenReturn(FakeSessionContext.adminInCollection(zeCollection));
		presenter = spy(new ListSchemaTypePresenter(view));
	}

	//@Test
	public void whenEditButtonClickedThenNavigateToEditSchemas()
			throws Exception {
		when(schemaVO.getCode()).thenReturn("zeId");
		presenter.editButtonClicked(schemaVO);
		//verify(view.navigateTo(), times(1)).editSchema("zeId");
	}


	@Test
	public void whenWriteSchemaTypeExcelReportOnStreamTest()
			throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = presenter.writeSchemaTypeExcelReportOnStream(Folder.SCHEMA_TYPE);

		File file = getTestResourceFile("folder.xls");
		IOServices ioServices = getModelLayerFactory().getIOServicesFactory().newIOServices();
		InputStream inputStream = ioServices.newFileInputStream(file, INPUT_STREAM_NAME);

		try {
			HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
			HSSFWorkbook generatedPreSave = new HSSFWorkbook(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));

			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
				HSSFSheet presaveSheet = workbook.getSheetAt(i);
				HSSFSheet generatedSheet = generatedPreSave.getSheetAt(i);

				generatedSheet.getSheetName().equals(presaveSheet.getSheetName());

				List<String> preSaveData = excelSheetToListOfCellAsStringValue(presaveSheet);
				List<String> generatedData = excelSheetToListOfCellAsStringValue(generatedSheet);

				Assertions.assertThat(generatedData).isEqualTo(preSaveData);
			}

		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}

	}

	private List<String> excelSheetToListOfCellAsStringValue(HSSFSheet sheet) {
		//Iterate through each rows one by one
		Iterator<Row> rowIterator = sheet.iterator();
		ArrayList contentAsList = new ArrayList();

		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			//For each row, iterate through all the columns
			Iterator<Cell> cellIterator = row.cellIterator();

			while (cellIterator.hasNext()) {
				Cell cell = cellIterator.next();
				//Check the cell type and format accordingly
				contentAsList.add(cell.getStringCellValue());
			}
		}

		return contentAsList;
	}
}
