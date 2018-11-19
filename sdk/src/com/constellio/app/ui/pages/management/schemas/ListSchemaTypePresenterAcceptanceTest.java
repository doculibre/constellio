package com.constellio.app.ui.pages.management.schemas;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class ListSchemaTypePresenterAcceptanceTest extends ConstellioTest {

	@Mock MetadataSchemaTypeVO schemaVO;
	ListSchemaTypePresenter presenter;
	@Mock ListSchemaTypeViewImpl view;

	RMTestRecords records = new RMTestRecords(zeCollection);

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
		byte[] preSaveFile = Files.readAllBytes(file.toPath());

		Assertions.assertThat(preSaveFile).isEqualTo(byteArrayOutputStream.toByteArray());
	}
}
