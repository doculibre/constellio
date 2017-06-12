package com.constellio.app.modules.rm.ui.components.document.newFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Locale;

import org.apache.commons.io.input.ReaderInputStream;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Content;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.setups.Users;

public class NewFilePresenterAcceptTest extends ConstellioTest {

	Users users = new Users();
	@Mock NewFileWindow view;
	@Mock CoreViews navigator;
	@Mock DocumentVO documentVO;
	RMTestRecords rmRecords = new RMTestRecords(zeCollection);
	RMSchemasRecordsServices schemasRecordsServices;
	NewFilePresenter presenter;
	SessionContext sessionContext;
	RecordServices recordServices;
	LocalDateTime now = new LocalDateTime();
	LocalDateTime shishOClock = new LocalDateTime().plusDays(1);

	MetadataSchemasManager metadataSchemasManager;
	SearchServices searchServices;
	ContentManager contentManager;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(rmRecords)
						.withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent()
		);
		inCollection(zeCollection).giveWriteAccessTo(aliceWonderland);

		schemasRecordsServices = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		searchServices = getModelLayerFactory().newSearchServices();
		contentManager = getModelLayerFactory().getContentManager();

		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		sessionContext = FakeSessionContext.chuckNorrisInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);

		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.getSessionContext()).thenReturn(sessionContext);

		presenter = new NewFilePresenter(view);
	}

	@Test
	public void givenTemplateAndFileNameAndDocumentTypeWhenNewFileThenNewFileWithTemplateContent()
			throws Exception {

		Content content = givenTemplateAndFileName();
		givenDocumentTypeWithTemplateAndLinkedToEmailSchema(content);
		presenter.documentTypeIdSet(rmRecords.documentTypeId_1);

		presenter.newFileNameSubmitted();

		assertThat(content.getCurrentVersion().getFilename()).isEqualTo("test.docx");
		assertThat(presenter.getFilename()).isEqualTo("newName.docx");
		assertThat(presenter.getFileContent().getCurrentVersion().getFilename()).isEqualTo("newName.docx");
		verify(view).notifyNewFileCreated(presenter.getFileContent(), presenter.getDocumentTypeId());
	}

	private void givenDocumentTypeWithTemplateAndLinkedToEmailSchema(Content content1)
			throws RecordServicesException {
		DocumentType documentType1 = schemasRecordsServices.getDocumentType(rmRecords.documentTypeId_1);
		documentType1.setLinkedSchema(Email.SCHEMA);
		documentType1.setTemplates(Arrays.asList(content1));
		recordServices.update(documentType1);
	}

	private Content givenTemplateAndFileName() {
		Content content1 = contentManager.createMinor(rmRecords.getAdmin(), "test.docx", textContent("content"));
		when(view.getTemplate()).thenReturn(content1);
		when(view.getFileName()).thenReturn("newName");
		return content1;
	}

	private ContentVersionDataSummary textContent(String text) {
		Reader reader = new StringReader(text);
		InputStream inputStream = new ReaderInputStream(reader);
		ContentVersionDataSummary contentVersionDataSummary = contentManager.upload(inputStream);
		return contentVersionDataSummary;

	}
}
