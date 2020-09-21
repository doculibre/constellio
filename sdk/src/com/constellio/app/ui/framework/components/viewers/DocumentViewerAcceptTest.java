package com.constellio.app.ui.framework.components.viewers;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.builders.DocumentToVOBuilder;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.ContentVersionToVOBuilder;
import com.constellio.app.ui.framework.components.resource.ConstellioResourceHandler;
import com.constellio.app.ui.framework.components.viewers.document.DocumentViewer;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.setups.Users;
import com.constellio.sdk.tests.vaadin.FakeVaadinEngine;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Label;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;

import static org.mockito.Mockito.doReturn;

@UiTest
public class DocumentViewerAcceptTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();

	ContentManager contentManager;

	RMSchemasRecordsServices rm;
	SearchServices searchServices;
	RecordServices recordServices;

	DocumentToVOBuilder documentToVOBuilder;
	ContentVersionToVOBuilder contentVersionToVOBuilder;

	Document documentWithPercentInFileName;
	Document documentWithoutPercentInFileName;

	@Before
	public void setup() throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records).withFoldersAndContainersOfEveryStatus().withAllTest(users));
		searchServices = getModelLayerFactory().newSearchServices();
		contentManager = getModelLayerFactory().getContentManager();
		recordServices = getModelLayerFactory().newRecordServices();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		documentToVOBuilder = new DocumentToVOBuilder(getModelLayerFactory());
		contentVersionToVOBuilder = new ContentVersionToVOBuilder(getModelLayerFactory());

		documentWithPercentInFileName = createDocumentWithPercentInFileName();
		documentWithoutPercentInFileName = createDocumentWithoutPercentInFileName();

		rm.executeTransaction(new Transaction(documentWithPercentInFileName, documentWithoutPercentInFileName));
	}

	@Test
	public void withValidAndInvalidPercentEncodingInFileNameDocumentViewerStillRetreiveRessource() throws Exception {

		for (Document document : Arrays.asList(documentWithPercentInFileName, documentWithoutPercentInFileName)) {
			Content content = document.getContent();
			ContentVersion contentVersion = content.getLastMajorContentVersion();

			DocumentVO documentVO = documentToVOBuilder.build(rm.get(document.getId()), VIEW_MODE.DISPLAY, FakeSessionContext.gandalfInCollection(zeCollection));
			ContentVersionVO contentVersionVO = contentVersionToVOBuilder.build(content, contentVersion);

			try (FakeVaadinEngine fakeVaadinEngine = new FakeVaadinEngine(FakeSessionContext.gandalfInCollection(zeCollection))) {
				DocumentViewer documentViewer = new DocumentViewer(documentVO, Document.CONTENT, contentVersionVO);

				fakeVaadinEngine.show(documentViewer);

				String htmlMarkup = fakeVaadinEngine.getFlattenChildComponents(Label.class, documentViewer).get(0).getValue();

				int fileUrlBeginIndex = htmlMarkup.indexOf("file=") + "file=".length();
				int fileUrlEndIndex = htmlMarkup.indexOf("\" width=");
				String fileUrl = htmlMarkup.substring(fileUrlBeginIndex, fileUrlEndIndex);

				ConstellioResourceHandler constellioResourceHandler = new ConstellioResourceHandler();

				doReturn(fileUrl).when(fakeVaadinEngine.getVaadinServletRequest()).getRequestURI();

				constellioResourceHandler.handleRequest(
						VaadinSession.getCurrent(),
						fakeVaadinEngine.getVaadinServletRequest(),
						fakeVaadinEngine.getVaadinResponse());
			}
		}
	}

	@NotNull
	private Document createDocumentWithPercentInFileName() throws FileNotFoundException {
		Document document = rm.newDocument()
				.setFolder(rm.getFolder(records.folder_A01))
				.setTitle("Document with error in Title");

		File file = getTestResourceFile("my%File.pdf");

		ContentVersionDataSummary versionDataSummary = contentManager.upload(file);
		Content content = contentManager.createMajor(users.gandalfLeblancIn(zeCollection), file.getName(), versionDataSummary);
		document.setContent(content);

		return document;
	}

	@NotNull
	private Document createDocumentWithoutPercentInFileName() throws FileNotFoundException {
		Document document = rm.newDocument()
				.setFolder(rm.getFolder(records.folder_A01))
				.setTitle("Document with error in Title");

		File file = getTestResourceFile("myFile.pdf");

		ContentVersionDataSummary versionDataSummary = contentManager.upload(file);
		Content content = contentManager.createMajor(users.gandalfLeblancIn(zeCollection), file.getName(), versionDataSummary);
		document.setContent(content);

		return document;
	}
}
