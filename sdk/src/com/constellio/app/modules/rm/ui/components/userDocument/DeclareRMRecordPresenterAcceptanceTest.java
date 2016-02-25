package com.constellio.app.modules.rm.ui.components.userDocument;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.ui.entities.FolderVO;
import com.constellio.app.modules.rm.ui.pages.userDocuments.DeclareRMRecordPresenter;
import com.constellio.app.modules.rm.ui.pages.userDocuments.DeclareRMRecordView;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.UserDocumentVO;
import com.constellio.app.ui.framework.builders.UserDocumentToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.setups.Users;

public class DeclareRMRecordPresenterAcceptanceTest extends ConstellioTest {

	@Mock DeclareRMRecordView declareRMRecordView;

	private Users users;
	private RMSchemasRecordsServices rm;
	private RecordServices recordServices;
	private SessionContext sessionContext = FakeSessionContext.gandalfInCollection(zeCollection);
	private RMTestRecords records;

	@Before
	public void setup(){
		users = new Users();
		records = new RMTestRecords(zeCollection);
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent());

		ConstellioFactories constellioFactories = getConstellioFactories();
		when(declareRMRecordView.getConstellioFactories()).thenReturn(constellioFactories);
		when(declareRMRecordView.getSessionContext()).thenReturn(sessionContext);
		when(declareRMRecordView.getCollection()).thenReturn(zeCollection);

		recordServices = getModelLayerFactory().newRecordServices();
		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void givenADocumentInCollectionWhenAddingItWithAnotherNameThenConstellioReportTheDocumentIsADuplicate() throws RecordServicesException {
		//given
		uploadARecord(rm.newDocument().setFolder(records.folder_A03), "DocumentV1.odt");

		String newFile = "DocumentV1-copy.odt";
		String zeId = "zeId";
		UserDocument newUserDocument = rm.newUserDocumentWithId(zeId);
		uploadARecord(newUserDocument, newFile);

		UserDocumentToVOBuilder voBuilder = new UserDocumentToVOBuilder();
		UserDocumentVO documentVO = (UserDocumentVO)voBuilder.build(newUserDocument.getWrappedRecord(), VIEW_MODE.FORM, sessionContext);
		when(declareRMRecordView.getUserDocumentVO()).thenReturn(documentVO);

		//when
		new DeclareRMRecordPresenter(declareRMRecordView);

		//then
		ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
		verify(declareRMRecordView).setDuplicates(captor.capture());
		Map<DocumentVO, ContentVersionVO> duplicates = captor.getValue();
		assertThat(duplicates).hasSize(1);

		Map<DocumentVO, Double> emptyMap = Collections.emptyMap();
		verify(declareRMRecordView).setSimilarDocuments(emptyMap);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void givenADocumentInCollectionWhenAddingJustWhiteSpaceThenTheNewDocumentIsDetectedAs100PercentSimilarDoc() throws RecordServicesException {
		//given
		Document aDoc = rm.newDocument().setFolder(records.folder_A03);
		uploadARecord(aDoc, "DocumentV1.odt");

		String newFile = "DocumentV2-addedWhiteSpace.odt";
		String zeId = "zeId";
		UserDocument newUserDocument = rm.newUserDocumentWithId(zeId);
		uploadARecord(newUserDocument, newFile);

		UserDocumentToVOBuilder voBuilder = new UserDocumentToVOBuilder();
		UserDocumentVO documentVO = (UserDocumentVO)voBuilder.build(newUserDocument.getWrappedRecord(), VIEW_MODE.FORM, sessionContext);
		when(declareRMRecordView.getUserDocumentVO()).thenReturn(documentVO);

		//when
		new DeclareRMRecordPresenter(declareRMRecordView);

		//then
		Map<DocumentVO, ContentVersionVO> emptyMap = Collections.emptyMap();
		verify(declareRMRecordView).setDuplicates(emptyMap);

		ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
		verify(declareRMRecordView).setSimilarDocuments(captor.capture());
		Map<DocumentVO, Double> similarDocuments = captor.getValue();

		assertThat(similarDocuments).hasSize(1);
		Entry<DocumentVO, Double> aSimilarDocWithScore = similarDocuments.entrySet().iterator().next();
		assertThat(aSimilarDocWithScore.getKey().getId()).isEqualTo(aDoc.getId());
		assertThat(aSimilarDocWithScore.getValue()).isGreaterThan(0.99999999999);

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void givenMultipDocumentsInFoldersWhenAskingForDocumentFolderThenSystemSuggestionIsCorrect() throws RecordServicesException {
		//given
		uploadARecord(rm.newDocument().setFolder(records.folder_A01), "contrat1.docx");
		uploadARecord(rm.newDocument().setFolder(records.folder_A01), "contrat2.docx");
		uploadARecord(rm.newDocument().setFolder(records.folder_A02), "proces1.docx");
		uploadARecord(rm.newDocument().setFolder(records.folder_A02), "proces2.docx");
		uploadARecord(rm.newDocument().setFolder(records.folder_A03), "rapport1.docx");
		uploadARecord(rm.newDocument().setFolder(records.folder_A03), "rapport2.docx");
		uploadARecord(rm.newDocument().setFolder(records.folder_A03), "rapport3.docx");

		//when
		UserDocument aContrat;
		uploadARecord(aContrat = rm.newUserDocumentWithId("contrat"), "contrat3.docx");

		UserDocumentToVOBuilder voBuilder = new UserDocumentToVOBuilder();
		UserDocumentVO documentVO = (UserDocumentVO)voBuilder.build(aContrat.getWrappedRecord(), VIEW_MODE.FORM, sessionContext);
		when(declareRMRecordView.getUserDocumentVO()).thenReturn(documentVO);

		new DeclareRMRecordPresenter(declareRMRecordView);

		//then
		Map<DocumentVO, ContentVersionVO> emptyMap = Collections.emptyMap();
		verify(declareRMRecordView).setDuplicates(emptyMap);

		ArgumentCaptor<Map> captorSimilarDoc = ArgumentCaptor.forClass(Map.class);
		verify(declareRMRecordView).setSimilarDocuments(captorSimilarDoc.capture());
		Map<DocumentVO, Double> similarDocuments = captorSimilarDoc.getValue();

		assertThat(similarDocuments.size()).isEqualTo(2);

		ArgumentCaptor<Map> captorFolder = ArgumentCaptor.forClass(Map.class);
		verify(declareRMRecordView).setSuggestedFolders(captorFolder.capture());
		Map<FolderVO, Double> suggestedFolder = captorFolder.getValue();

		assertThat(suggestedFolder.size()).isEqualTo(1);
		Entry<FolderVO, Double> theSuggestedFolder = suggestedFolder.entrySet().iterator().next();

		assertThat(theSuggestedFolder.getKey().getId()).isEqualTo(records.folder_A01);
		assertThat(theSuggestedFolder.getValue()).isCloseTo(1.0, within(0.001));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void givenADocument_A_InCollectionWhenAddingASentenceToItAndUploadThenDocument_A_IsFoundAsASimilarDoc() throws RecordServicesException {
		//given
		Document aDoc = rm.newDocument().setFolder(records.folder_A03);
		uploadARecord(aDoc, "DocumentV1.odt");

		String newFile = "DocumentV2-withSomeModification.odt";
		String zeId = "zeId";
		UserDocument newUserDocument = rm.newUserDocumentWithId(zeId);
		uploadARecord(newUserDocument, newFile);

		UserDocumentToVOBuilder voBuilder = new UserDocumentToVOBuilder();
		UserDocumentVO documentVO = (UserDocumentVO)voBuilder.build(newUserDocument.getWrappedRecord(), VIEW_MODE.FORM, sessionContext);
		when(declareRMRecordView.getUserDocumentVO()).thenReturn(documentVO);

		//when
		new DeclareRMRecordPresenter(declareRMRecordView);

		//then
		Map<DocumentVO, ContentVersionVO> emptyMap = Collections.emptyMap();
		verify(declareRMRecordView).setDuplicates(emptyMap);

		ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
		verify(declareRMRecordView).setSimilarDocuments(captor.capture());
		Map<DocumentVO, Double> similarDocuments = captor.getValue();

		assertThat(similarDocuments).hasSize(1);
		Entry<DocumentVO, Double> aSimilarDocWithScore = similarDocuments.entrySet().iterator().next();
		assertThat(aSimilarDocWithScore.getKey().getId()).isEqualTo(aDoc.getId());
	}

	private void uploadARecord(RecordWrapper recordWrapper, String newFile) throws RecordServicesException {
		User user = users.gandalfIn("zeCollection");

		ContentManager cm = getModelLayerFactory().getContentManager();
		ContentVersionDataSummary version = cm.upload(getTestResourceInputStream(newFile));
		Content content = cm.createMinor(user, newFile, version);
		recordWrapper.setTitle(newFile);
		if (recordWrapper instanceof Document)
			((Document)recordWrapper).setContent(content);
		else if (recordWrapper instanceof UserDocument){
			((UserDocument)recordWrapper).setUser(user);
			((UserDocument)recordWrapper).setContent(content);
		}

		recordServices.add(recordWrapper);
	}
}
