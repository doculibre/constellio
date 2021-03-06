package com.constellio.app.modules.rm.ui.pages.folder;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.modules.rm.ui.builders.DocumentToVOBuilder;
import com.constellio.app.modules.rm.ui.builders.ExternalLinkToVOBuilder;
import com.constellio.app.modules.rm.ui.builders.FolderToVOBuilder;
import com.constellio.app.modules.rm.ui.builders.UserToVOBuilder;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.ui.entities.FolderVO;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.ExternalLink;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.UIContext;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.QueryCounter;
import com.constellio.sdk.tests.SDKViewNavigation;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForUsers;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.QueryCounter.ON_COLLECTION;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class DisplayFolderPresenterAcceptTest extends ConstellioTest {

	LocalDateTime shishOClock = new LocalDateTime().plusDays(1);

	Users users = new Users();
	@Mock DisplayFolderView displayFolderView;
	SDKViewNavigation viewNavigation;
	@Mock UserCredentialVO chuckCredentialVO;
	RMTestRecords rmRecords = new RMTestRecords(zeCollection);
	SearchServices searchServices;
	DisplayFolderPresenter presenter;
	SessionContext sessionContext;
	@Mock UIContext uiContext;
	LocalDate nowDate = new LocalDate();
	RMEventsSearchServices rmEventsSearchServices;
	RolesManager rolesManager;

	RMSchemasRecordsServices rmSchemasRecordsServices;
	MetadataSchemasManager metadataSchemasManager;
	RecordServices recordServices;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(rmRecords)
						.withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent().withEvents().withDocumentsDecommissioningList()
		);

		inCollection(zeCollection).setCollectionTitleTo("Collection de test");
		inCollection(zeCollection).giveWriteAccessTo(aliceWonderland);
		rmEventsSearchServices = new RMEventsSearchServices(getModelLayerFactory(), zeCollection);

		rmSchemasRecordsServices = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		recordServices = getModelLayerFactory().newRecordServices();

		sessionContext = FakeSessionContext.forRealUserIncollection(users.chuckNorrisIn(zeCollection));
		sessionContext.setCurrentLocale(Locale.FRENCH);
		searchServices = getModelLayerFactory().newSearchServices();

		viewNavigation = new SDKViewNavigation(displayFolderView);
		when(displayFolderView.getSessionContext()).thenReturn(sessionContext);
		when(displayFolderView.getCollection()).thenReturn(zeCollection);
		when(displayFolderView.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(displayFolderView.getUIContext()).thenReturn(uiContext);
		when(displayFolderView.getFolderOrSubFolderButtonKey("DisplayFolderView.borrowedFolder"))
				.thenReturn("DisplayFolderView.borrowedFolder");

		chuckCredentialVO = new UserCredentialVO();
		chuckCredentialVO.setUsername("chuck");

		displayFolderPresenterCreation(displayFolderView, null, false);
		presenter.forParams("C30");


		rolesManager = getModelLayerFactory().getRolesManager();

		givenTimeIs(nowDate);
	}

	private DisplayFolderPresenter displayFolderPresenterCreation(DisplayFolderView displayFolderView,
																  RecordVO recordVO, boolean popup) {
		presenter = spy(new DisplayFolderPresenter(displayFolderView, recordVO, popup, false));//spy(
		doNothing().when(presenter).navigateToFolder(any(String.class));
		doNothing().when(presenter).navigateToDocument(any(RecordVO.class));

		return presenter;
	}
	@Test
	public void givenDocumentWithContentThenFindDuplicateWithPresenter() throws RecordServicesException {
		QueryCounter queryCounter = new QueryCounter(getDataLayerFactory(), ON_COLLECTION(zeCollection));
		queryCounter.reset();

		Record recordA19 = recordServices.get(rmRecords.document_A19);
		Document documentA19 = rmSchemasRecordsServices.wrapDocument(recordA19);

		String hash = documentA19.getContent().getCurrentVersion().getHash();

		List<Record> recordList = presenter.getAllRecordsWithHash(hash);

		Document document1 = rmSchemasRecordsServices.wrapDocument(recordList.get(0));
		assertThat(recordList).hasSize(1);
		assertThat(document1.getContent().getCurrentVersion().getHash()).isEqualTo(hash);

		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
	}

	@Test
	public void givenDocumentWithContentThenCannotFindWithUserWIthoutAccess() {

		Record recordA19 = recordServices.get(rmRecords.document_A19);
		Document documentA19 = rmSchemasRecordsServices.wrapDocument(recordA19);

		String hash = documentA19.getContent().getCurrentVersion().getHash();

		AuthorizationsServices authorizationsServices = getModelLayerFactory().newAuthorizationsServices();
		authorizationsServices.add(authorizationForUsers(users.bobIn(zeCollection)).on(recordA19).givingNegativeReadWriteAccess());

		SessionContext sessionContext = FakeSessionContext.forRealUserIncollection(users.bobIn(zeCollection));
		sessionContext.setCurrentLocale(Locale.FRENCH);

		when(displayFolderView.getSessionContext()).thenReturn(sessionContext);

		DisplayFolderPresenter displayFolderPresenter = displayFolderPresenterCreation(displayFolderView, null, false);

		List<Record> recordList = displayFolderPresenter.getAllRecordsWithHash(hash);

		assertThat(recordList).hasSize(0);
	}

	@Test
	public void givenFolderWithDocumentsThenFindOtherDocumentsWithSameNameWithoutAnySolrQuery() {

		QueryCounter queryCounter = new QueryCounter(getDataLayerFactory(), ON_COLLECTION(zeCollection));
		queryCounter.reset();

		SearchResponseIterator<Record> searchResponseIterator = presenter.getExistingDocumentInCurrentFolder("contrat.docx",rmRecords.folder_A01);

		assertThat(searchResponseIterator.getNumFound()).isEqualTo(1);
		List<Record> recordList = searchResponseIterator.stream().collect(Collectors.toList());

		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		Record record1 = recordList.get(0);
		assertThat(record1.getId()).isEqualTo(rmRecords.folder_A01 + "_numericContractWithDifferentCopy");
	}

	@Test
	public void whenGetTemplatesThenReturnFolderTemplates()
			throws Exception {

		List<LabelTemplate> labelTemplates = presenter.getDefaultTemplates();

		assertThat(labelTemplates).hasSize(8);

		assertThat(labelTemplates.get(0).getKey()).isEqualTo("FOLDER_RIGHT_AVERY_5159");
		assertThat(labelTemplates.get(0).getName()).isEqualTo("Code de plan justifié à droite (Avery 5159)");

		assertThat(labelTemplates.get(1).getKey()).isEqualTo("FOLDER_RIGHT_AVERY_5161");
		assertThat(labelTemplates.get(1).getName()).isEqualTo("Code de plan justifié à droite (Avery 5161)");

		assertThat(labelTemplates.get(2).getKey()).isEqualTo("FOLDER_RIGHT_AVERY_5162");
		assertThat(labelTemplates.get(2).getName()).isEqualTo("Code de plan justifié à droite (Avery 5162)");

		assertThat(labelTemplates.get(3).getKey()).isEqualTo("FOLDER_RIGHT_AVERY_5163");
		assertThat(labelTemplates.get(3).getName()).isEqualTo("Code de plan justifié à droite (Avery 5163)");

		assertThat(labelTemplates.get(4).getKey()).isEqualTo("FOLDER_LEFT_AVERY_5159");
		assertThat(labelTemplates.get(4).getName()).isEqualTo("Code de plan justifié à gauche (Avery 5159)");

		assertThat(labelTemplates.get(5).getKey()).isEqualTo("FOLDER_LEFT_AVERY_5161");
		assertThat(labelTemplates.get(5).getName()).isEqualTo("Code de plan justifié à gauche (Avery 5161)");

		assertThat(labelTemplates.get(6).getKey()).isEqualTo("FOLDER_LEFT_AVERY_5162");
		assertThat(labelTemplates.get(6).getName()).isEqualTo("Code de plan justifié à gauche (Avery 5162)");

		assertThat(labelTemplates.get(7).getKey()).isEqualTo("FOLDER_LEFT_AVERY_5163");
		assertThat(labelTemplates.get(7).getName()).isEqualTo("Code de plan justifié à gauche (Avery 5163)");
	}

	@Test
	public void givenViewIsEnteredThenAddToCartButtonOnlyShowsWhenUserHasPermission() {
		String roleCode = users.aliceIn(zeCollection).getUserRoles().get(0);
		RolesManager rolesManager = getAppLayerFactory().getModelLayerFactory().getRolesManager();

		Role role = rolesManager.getRole(zeCollection, roleCode);
		Role editedRole = role.withPermissions(new ArrayList<String>());
		rolesManager.updateRole(editedRole);

		connectWithAlice();
		assertThat(presenter.hasCurrentUserPermissionToUseCartGroup()).isFalse();

		Role editedRole2 = editedRole.withPermissions(asList(RMPermissionsTo.USE_GROUP_CART));
		rolesManager.updateRole(editedRole2);

		connectWithAlice();
		assertThat(presenter.hasCurrentUserPermissionToUseCartGroup()).isTrue();
	}

	@Test
	public void givenEventsThenEventsDataProviderReturnValidEvents()
			throws Exception {
		getDataLayerFactory().newEventsDao().flush();
		assertThat(searchServices.getResultsCount(
				rmEventsSearchServices.newFindEventByRecordIDQuery(users.adminIn(zeCollection), rmRecords.folder_A01)))
				.isEqualTo(1);
		assertThat(searchServices.getResultsCount(
				rmEventsSearchServices.newFindEventByRecordIDQuery(users.adminIn(zeCollection), rmRecords.folder_A05)))
				.isEqualTo(1);

		presenter.forParams(rmRecords.folder_A01);
		RecordVODataProvider provider = presenter.getEventsDataProvider();
		List<RecordVO> eventList = provider.listRecordVOs(0, 100);
		assertThat(eventList).hasSize(1);
	}

	@Test
	public void givenFolderWithChildDocumentsAndReferencingOtherDocumentsThenAllReturnedByQuery()
			throws Exception {

		metadataSchemasManager.modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(Folder.DEFAULT_SCHEMA).create("refToDocument")
						.defineReferencesTo(types.getSchemaType(Document.SCHEMA_TYPE));
				types.getSchema(Folder.DEFAULT_SCHEMA).create("refToDocuments")
						.defineReferencesTo(types.getSchemaType(Document.SCHEMA_TYPE)).setMultivalue(true);
			}
		});
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		Transaction tx = new Transaction();
		tx.add(rm.newDocumentWithId("documentInA48").setTitle("Document in folder A48").setFolder(rmRecords.folder_A48));
		tx.add(rm.newDocumentWithId("documentInA49").setTitle("Document in folder A49").setFolder(rmRecords.folder_A49));
		tx.add(rm.newDocumentWithId("documentInA51").setTitle("Document in folder A51").setFolder(rmRecords.folder_A51));
		tx.add(rm.newDocumentWithId("documentInA52").setTitle("Document in folder A52").setFolder(rmRecords.folder_A52));
		tx.add(rm.newDocumentWithId("documentInA53").setTitle("Document in folder A53").setFolder(rmRecords.folder_A53));
		tx.add(rm.newDocumentWithId("documentInA54").setTitle("Document in folder A54").setFolder(rmRecords.folder_A54));
		tx.add(rm.newDocumentWithId("documentInA55").setTitle("Document in folder A55").setFolder(rmRecords.folder_A55));
		Folder folder = rmRecords.getFolder_A49().set("refToDocument", "documentInA51")
				.set("refToDocuments", asList("documentInA53", "documentInA54"));
		tx.add(folder);
		recordServices.execute(tx);

		presenter.forParams(rmRecords.folder_A49);
		assertThat(searchServices.search(presenter.getDocumentsQuery())).extracting("id").contains(
				"documentInA49", "documentInA51", "documentInA53", "documentInA54").hasSize(14);

		presenter.forParams(rmRecords.folder_A51);
		assertThat(searchServices.search(presenter.getDocumentsQuery())).extracting("id").contains(
				"documentInA51").hasSize(9);
	}

	@Test
	public void whenDocumentsLinkedToFolderThenAllDocumentsProvidedWithoutQuery()
			throws Exception {
		givenConfig(ConstellioEIMConfigs.DISPLAY_ONLY_SUMMARY_METADATAS_IN_TABLES, true);

		MetadataSchema schema = rmSchemasRecordsServices.schemaType("document").getDefaultSchema();
		Metadata metadata = schema.getMetadata("linkedTo");
		User bob = users.bobIn(zeCollection);
		User charles = users.charlesIn(zeCollection);

		AdministrativeUnit au1 = rmSchemasRecordsServices.getAdministrativeUnit(rmRecords.unitId_10);
		AdministrativeUnit au2 = rmSchemasRecordsServices.newAdministrativeUnitWithId(rmRecords.unitId_20);

		Folder folder1 = newFolderInUnit(au1, "folder1");
		Folder folder2 = newFolderInUnit(au1, "folder2");
		Folder folder3 = newFolderInUnit(au1, "folder3");
		Folder folder4 = newFolderInUnit(au1, "folder4");
		Folder folder5 = newFolderInUnit(au1, "folder5");
		Folder folderZ = newFolderInUnit(au2, "folderZ");

		Document doc0 = rmSchemasRecordsServices.newDocumentWithId("doc0").setFolder(folder4).setTitle("Beta");
		Document doc1 = rmSchemasRecordsServices.newDocumentWithId("doc1").setFolder(folderZ).setTitle("Zeta");
		Document doc2 = rmSchemasRecordsServices.newDocumentWithId("doc2").setFolder(folderZ).setTitle("Gamma");
		Document doc3 = rmSchemasRecordsServices.newDocumentWithId("doc3").setFolder(folderZ).setTitle("Alpha");
		Document doc4 = rmSchemasRecordsServices.newDocumentWithId("doc4").setFolder(folder4).setTitle("Delta");

		// Setting document links to folders
		List<Folder> doc0Refs = new ArrayList<>();
		doc0Refs.add(folder4);
		doc0.set(metadata, doc0Refs);

		List<Folder> doc1Refs = new ArrayList<>();
		doc1Refs.add(folder2);
		doc1Refs.add(folder4);
		doc1.set(metadata, doc1Refs);

		List<Folder> doc2Refs = new ArrayList<>();
		doc2Refs.add(folder3);
		doc2Refs.add(folder4);
		doc2.set(metadata, doc2Refs);

		List<Folder> doc3Refs = new ArrayList<>();
		doc3Refs.add(folder3);
		doc3Refs.add(folder5);
		doc3.set(metadata, doc3Refs);

		List<Folder> doc4Refs = new ArrayList<>();
		doc4Refs.add(folder4);
		doc4.set(metadata, doc4Refs);

		recordServices.execute(new Transaction(
				folder1,
				folder2,
				folder3,
				folder4,
				folder5,
				folderZ,
				doc0,
				doc1,
				doc2,
				doc3,
				doc4
		));
		QueryCounter queryCounter = new QueryCounter(getDataLayerFactory(), DisplayFolderPresenterAcceptTest.class);

		presenter.forParams(folder1.getId());
		assertThat(searchServices.searchRecordIds(presenter.folderContentDataProvider.getQuery().getCacheableQueries().get(2)))
				.isEmpty();

		presenter.forParams(folder2.getId());
		assertThat(searchServices.searchRecordIds(presenter.folderContentDataProvider.getQuery().getCacheableQueries().get(2)))
				.containsExactly(doc1.getId());

		presenter.forParams(folder3.getId());
		assertThat(searchServices.searchRecordIds(presenter.folderContentDataProvider.getQuery().getCacheableQueries().get(2)))
				.containsExactly(doc3.getId(), doc2.getId());

		presenter.forParams(folder4.getId());
		assertThat(searchServices.searchRecordIds(presenter.folderContentDataProvider.getQuery().getCacheableQueries().get(2)))
				.containsExactly(doc0.getId(), doc4.getId(), doc2.getId(), doc1.getId()); // Alphabetically sorted

		presenter.forParams(folder5.getId());
		assertThat(searchServices.searchRecordIds(presenter.folderContentDataProvider.getQuery().getCacheableQueries().get(2)))
				.containsExactly(doc3.getId());

		assertThat(queryCounter.newQueryCalls()).isZero();
	}

	@Test
	public void whenExternalLinksLinkedToFolderThenAllExternalLinksProvidedWithoutQuery()
			throws Exception {
		givenConfig(ConstellioEIMConfigs.DISPLAY_ONLY_SUMMARY_METADATAS_IN_TABLES, true);

		AdministrativeUnit au1 = rmSchemasRecordsServices.getAdministrativeUnit(rmRecords.unitId_10);

		Folder folder1 = newFolderInUnit(au1, "folder1");
		Folder folder2 = newFolderInUnit(au1, "folder2");
		Folder folder3 = newFolderInUnit(au1, "folder3");
		Folder folder4 = newFolderInUnit(au1, "folder4");

		ExternalLink link1 = rmSchemasRecordsServices.newExternalLinkWithId("link1").setTitle("link1").setImportedOn(TimeProvider.getLocalDateTime());
		ExternalLink link2 = rmSchemasRecordsServices.newExternalLinkWithId("link2").setTitle("link2").setImportedOn(TimeProvider.getLocalDateTime());
		ExternalLink link3 = rmSchemasRecordsServices.newExternalLinkWithId("link3").setTitle("link3").setImportedOn(null);
		ExternalLink link4 = rmSchemasRecordsServices.newExternalLinkWithId("link4").setTitle("link4").setImportedOn(null);

		folder1.setExternalLinks(null);
		folder2.setExternalLinks(asList(link1.getId(), link2.getId()));
		folder3.setExternalLinks(asList(link3.getId(), link4.getId()));
		folder4.setExternalLinks(asList(link1.getId(), link2.getId(), link3.getId(), link4.getId()));

		link1.setLinkedto(folder4);
		link2.setLinkedto(folder4);
		link3.setLinkedto(folder4);
		link4.setLinkedto(folder4);

		recordServices.execute(new Transaction(
				folder1,
				folder2,
				folder3,
				folder4,
				link1,
				link2,
				link3,
				link4
		));
		QueryCounter queryCounter = new QueryCounter(getDataLayerFactory(), DisplayFolderPresenterAcceptTest.class);

		presenter.forParams(folder1.getId());
		assertThat(searchServices.searchRecordIds(presenter.folderContentDataProvider.getQuery().getCacheableQueries().get(3)))
				.isEmpty();

		presenter.forParams(folder2.getId());
		assertThat(searchServices.searchRecordIds(presenter.folderContentDataProvider.getQuery().getCacheableQueries().get(3)))
				.isEmpty();

		presenter.forParams(folder3.getId());
		assertThat(searchServices.searchRecordIds(presenter.folderContentDataProvider.getQuery().getCacheableQueries().get(3)))
				.containsExactly(link3.getId(), link4.getId());

		presenter.forParams(folder4.getId());
		assertThat(searchServices.searchRecordIds(presenter.folderContentDataProvider.getQuery().getCacheableQueries().get(3)))
				.containsExactly(link3.getId(), link4.getId()); // Alphabetically sorted

		assertThat(queryCounter.newQueryCalls()).isZero();
	}

	@Test
	public void givenSubDocumentDraggedAndDroppedInSubFolderThenParentUpdated() throws Exception {
		Document subDocument = rmSchemasRecordsServices.newDocumentWithId("abeilleDoc")
				.setFolder(rmRecords.folder_A01).setTitle("AbeilleDoc");
		Folder subFolder = rmSchemasRecordsServices.newFolderWithId("abeille2")
				.setParentFolder(rmRecords.folder_A01).setTitle("Abeille2").setOpenDate(TimeProvider.getLocalDate());
		recordServices.execute(new Transaction().addAll(subFolder, subDocument));

		Record record = recordServices.getDocumentById(subDocument.getId());
		DocumentVO documentVO = new DocumentToVOBuilder(getModelLayerFactory()).build(record, VIEW_MODE.TABLE, sessionContext);
		FolderVO subFolderVO = new FolderToVOBuilder().build(subFolder.getWrappedRecord(), VIEW_MODE.TABLE, sessionContext);
		presenter.recordsDroppedOn(Arrays.asList(documentVO), subFolderVO);
		waitForBatchProcess();

		record = recordServices.getDocumentById(subDocument.getId());
		assertThat(record.getParentId(getModelLayerFactory().getMetadataSchemasManager().getSchemaOf(record))).isEqualTo(subFolder.getId());
	}

	@Test
	public void givenSubFolderDraggedAndDroppedInAnotherSubFolderThenParentUpdated() throws Exception {
		Folder subFolder1 = rmSchemasRecordsServices.newFolderWithId("abeille1")
				.setParentFolder(rmRecords.folder_A01).setTitle("Abeille1").setOpenDate(TimeProvider.getLocalDate());
		Folder subFolder2 = rmSchemasRecordsServices.newFolderWithId("abeille2")
				.setParentFolder(rmRecords.folder_A01).setTitle("Abeille2").setOpenDate(TimeProvider.getLocalDate());
		recordServices.execute(new Transaction().addAll(subFolder1, subFolder2));

		FolderVO folderVO = new FolderToVOBuilder().build(subFolder1.getWrappedRecord(), VIEW_MODE.TABLE, sessionContext);
		FolderVO targetFolderVO = new FolderToVOBuilder().build(subFolder2.getWrappedRecord(), VIEW_MODE.TABLE, sessionContext);
		presenter.recordsDroppedOn(Arrays.asList(folderVO), targetFolderVO);
		waitForBatchProcess();

		Record record = recordServices.getDocumentById(subFolder1.getId());
		assertThat(record.getParentId(getModelLayerFactory().getMetadataSchemasManager().getSchemaOf(record))).isEqualTo(subFolder2.getId());
	}

	@Test
	public void givenSubFolderDraggedAndDroppedAndUserHasNoWriteAccessThenParentNotUpdated() throws Exception {
		User user = users.sasquatchIn(zeCollection);
		UserVO userVO = new UserToVOBuilder().build(user.getWrappedRecord(), VIEW_MODE.DISPLAY, sessionContext);
		sessionContext.setCurrentUser(userVO);

		Folder subFolder1 = rmSchemasRecordsServices.newFolderWithId("abeille1")
				.setParentFolder(rmRecords.folder_A01).setTitle("Abeille1").setOpenDate(TimeProvider.getLocalDate());
		Folder subFolder2 = rmSchemasRecordsServices.newFolderWithId("abeille2")
				.setParentFolder(rmRecords.folder_A01).setTitle("Abeille2").setOpenDate(TimeProvider.getLocalDate());
		recordServices.execute(new Transaction().addAll(subFolder1, subFolder2));

		FolderVO folderVO = new FolderToVOBuilder().build(subFolder1.getWrappedRecord(), VIEW_MODE.TABLE, sessionContext);
		FolderVO targetFolderVO = new FolderToVOBuilder().build(subFolder2.getWrappedRecord(), VIEW_MODE.TABLE, sessionContext);
		presenter.recordsDroppedOn(Arrays.asList(folderVO), targetFolderVO);
		waitForBatchProcess();

		Record record = recordServices.getDocumentById(subFolder1.getId());
		assertThat(record.getParentId(getModelLayerFactory().getMetadataSchemasManager().getSchemaOf(record))).isEqualTo("abeille2");
	}

	@Test
	public void givenExternalLinkDraggedAndDroppedInSubFolderThenParentUpdated() throws Exception {
		ExternalLink externalLink = rmSchemasRecordsServices.newExternalLinkWithId("link1").setTitle("link1").setLinkedto(rmRecords.getFolder_A01());
		Folder parentFolder = rmRecords.getFolder_A01().setExternalLinks(Collections.singletonList(externalLink.getId()));
		Folder subFolder = rmSchemasRecordsServices.newFolderWithId("abeille2")
				.setParentFolder(rmRecords.folder_A01).setTitle("Abeille2").setOpenDate(TimeProvider.getLocalDate());
		recordServices.execute(new Transaction().addAll(externalLink, parentFolder, subFolder));

		FolderVO folderVO = new FolderToVOBuilder().build(parentFolder.getWrappedRecord(), VIEW_MODE.FORM, sessionContext);
		doReturn(folderVO).when(presenter).getLazyFullFolderVO();

		RecordVO externalLinkVO = new ExternalLinkToVOBuilder(getAppLayerFactory()).build(externalLink.getWrappedRecord(), VIEW_MODE.TABLE, sessionContext);
		FolderVO targetFolderVO = new FolderToVOBuilder().build(subFolder.getWrappedRecord(), VIEW_MODE.TABLE, sessionContext);
		presenter.recordsDroppedOn(Arrays.asList(externalLinkVO), targetFolderVO);
		waitForBatchProcess();

		subFolder = rmSchemasRecordsServices.getFolder(subFolder.getId());
		assertThat(subFolder.getExternalLinks()).containsExactly(externalLink.getId());
		parentFolder = rmSchemasRecordsServices.getFolder(parentFolder.getId());
		assertThat(parentFolder.getExternalLinks()).isEmpty();
	}

	@Test
	public void givenSearchingInFolderWithDeepHierarchyThenChildrenFound() throws RecordServicesException {
		recordServices.execute(new Transaction(
				rmRecords.getFolder_A04().setParentFolder(rmRecords.folder_A03),
				rmRecords.getFolder_A03().setParentFolder(rmRecords.folder_A02),
				rmRecords.getFolder_A02().setParentFolder(rmRecords.folder_A01)
		));

		FolderVO folderVO = new FolderToVOBuilder().build(rmRecords.getFolder_A01().getWrappedRecord(), VIEW_MODE.FORM, sessionContext);
		presenter.forParams(rmRecords.folder_A01);
		doReturn(folderVO).when(presenter).getLazyFullFolderVO();

		presenter.changeFolderContentDataProvider("abeille", false);
		assertThat(getFolderContentTitle()).contains("Abeille - Document analogique avec le même exemplaire",
				"Abeille - Document contrat analogique avec un autre exemplaire",
				"Abeille - Document contrat numérique avec un autre exemplaire",
				"Abeille - Document numérique avec le même exemplaire",
				"Abeille - Document procès verbal analogique avec un autre exemplaire",
				"Abeille - Document procès verbal numérique avec un autre exemplaire",
				"Abeille - Histoire", "Abeille - Livre de recettes", "Abeille - Petit guide",
				"Abeille - Typologie");

		presenter.changeFolderContentDataProvider("abeille", true);
		assertThat(getFolderContentTitle()).contains("Abeille - Document analogique avec le même exemplaire",
				"Abeille - Document contrat analogique avec un autre exemplaire",
				"Abeille - Document contrat numérique avec un autre exemplaire",
				"Abeille - Document numérique avec le même exemplaire",
				"Abeille - Document procès verbal analogique avec un autre exemplaire",
				"Abeille - Document procès verbal numérique avec un autre exemplaire",
				"Abeille - Histoire", "Abeille - Livre de recettes", "Abeille - Petit guide",
				"Abeille - Typologie");

		presenter.changeFolderContentDataProvider("baleine", false);
		assertThat(getFolderContentTitle()).isEmpty();

		presenter.changeFolderContentDataProvider("bale*", false);
		assertThat(getFolderContentTitle()).isEmpty();

		presenter.changeFolderContentDataProvider("bale*", true);
		assertThat(getFolderContentTitle()).contains("Baleine",
				"Baleine - Document analogique avec le même exemplaire",
				"Baleine - Document contrat analogique avec un autre exemplaire",
				"Baleine - Document contrat numérique avec un autre exemplaire",
				"Baleine - Document numérique avec le même exemplaire",
				"Baleine - Document procès verbal analogique avec un autre exemplaire",
				"Baleine - Document procès verbal numérique avec un autre exemplaire",
				"Baleine - Livre de recettes", "Baleine - Petit guide", "Baleine - Typologie");

		presenter.changeFolderContentDataProvider("baleine", true);
		assertThat(getFolderContentTitle()).contains("Baleine",
				"Baleine - Document analogique avec le même exemplaire",
				"Baleine - Document contrat analogique avec un autre exemplaire",
				"Baleine - Document contrat numérique avec un autre exemplaire",
				"Baleine - Document numérique avec le même exemplaire",
				"Baleine - Document procès verbal analogique avec un autre exemplaire",
				"Baleine - Document procès verbal numérique avec un autre exemplaire",
				"Baleine - Livre de recettes", "Baleine - Petit guide", "Baleine - Typologie");

		presenter.changeFolderContentDataProvider("*", false);
		assertThat(getFolderContentTitle()).contains("Abeille - Document analogique avec le même exemplaire",
				"Abeille - Document contrat analogique avec un autre exemplaire",
				"Abeille - Document contrat numérique avec un autre exemplaire",
				"Abeille - Document numérique avec le même exemplaire",
				"Abeille - Document procès verbal analogique avec un autre exemplaire",
				"Abeille - Document procès verbal numérique avec un autre exemplaire",
				"Abeille - Histoire", "Abeille - Livre de recettes", "Abeille - Petit guide",
				"Abeille - Typologie");

		presenter.changeFolderContentDataProvider("*", true);
		assertThat(getFolderContentTitle()).contains("Aigle", "Alouette", "Baleine",
				"Abeille - Document analogique avec le même exemplaire",
				"Abeille - Document contrat analogique avec un autre exemplaire",
				"Abeille - Document contrat numérique avec un autre exemplaire",
				"Abeille - Document numérique avec le même exemplaire",
				"Abeille - Document procès verbal analogique avec un autre exemplaire",
				"Abeille - Document procès verbal numérique avec un autre exemplaire",
				"Abeille - Histoire",
				"Abeille - Livre de recettes",
				"Abeille - Petit guide",
				"Abeille - Typologie",
				"Aigle - Document analogique avec le même exemplaire",
				"Aigle - Document contrat analogique avec un autre exemplaire",
				"Aigle - Document contrat numérique avec un autre exemplaire",
				"Aigle - Document numérique avec le même exemplaire",
				"Aigle - Document procès verbal analogique avec un autre exemplaire",
				"Aigle - Document procès verbal numérique avec un autre exemplaire",
				"Aigle - Histoire",
				"Aigle - Livre de recettes",
				"Aigle - Petit guide",
				"Aigle - Typologie",
				"Alouette - Document analogique avec le même exemplaire",
				"Alouette - Document contrat analogique avec un autre exemplaire",
				"Alouette - Document contrat numérique avec un autre exemplaire",
				"Alouette - Document numérique avec le même exemplaire",
				"Alouette - Document procès verbal analogique avec un autre exemplaire",
				"Alouette - Document procès verbal numérique avec un autre exemplaire",
				"Alouette - Histoire", "Alouette - Livre de recettes",
				"Alouette - Petit guide",
				"Alouette - Typologie",
				"Baleine - Document analogique avec le même exemplaire",
				"Baleine - Document contrat analogique avec un autre exemplaire",
				"Baleine - Document contrat numérique avec un autre exemplaire",
				"Baleine - Document numérique avec le même exemplaire",
				"Baleine - Document procès verbal analogique avec un autre exemplaire",
				"Baleine - Document procès verbal numérique avec un autre exemplaire",
				"Baleine - Livre de recettes",
				"Baleine - Petit guide",
				"Baleine - Typologie");
	}

	private List<String> getFolderContentTitle() {
		return searchServices.search(presenter.folderContentDataProvider.getQuery()).stream()
				.map(Record::getTitle)
				.collect(Collectors.toList());
	}

	private MetadataSchemaTypes getSchemaTypes() {
		return getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
	}

	private void searchEmailsToSend(LogicalSearchQuery query) {
		Folder folderC30 = rmRecords.getFolder_C30();
		Metadata subjectMetadata = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getMetadata(EmailToSend.DEFAULT_SCHEMA + "_" + EmailToSend.SUBJECT);
		LogicalSearchCondition condition = from(getSchemaTypes().getSchemaType(EmailToSend.SCHEMA_TYPE))
				.where(subjectMetadata).isContainingText(folderC30.getTitle());

		query.setCondition(condition);
	}

	private void connectWithBob() {
		sessionContext = FakeSessionContext.bobInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);
		when(displayFolderView.getSessionContext()).thenReturn(sessionContext);
		presenter = displayFolderPresenterCreation(displayFolderView, null, false);
	}

	private void connectWithAlice() {
		sessionContext = FakeSessionContext.aliceInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);
		when(displayFolderView.getSessionContext()).thenReturn(sessionContext);
		presenter = displayFolderPresenterCreation(displayFolderView, null, false);
	}

	private void connectWithChuck() {
		sessionContext = FakeSessionContext.chuckNorrisInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);
		when(displayFolderView.getSessionContext()).thenReturn(sessionContext);
		presenter = displayFolderPresenterCreation(displayFolderView, null, false);
	}

	@Test
	public void test()
			throws Exception {

		displayFolderView.selectFolderContentTab();

	}


	private Folder newFolderInUnit(AdministrativeUnit unit, String title) {
		return rmSchemasRecordsServices.newFolder().setCategoryEntered(rmRecords.categoryId_X100).setTitle(title).setOpenDate(new LocalDate())
				.setRetentionRuleEntered(rmRecords.ruleId_1).setAdministrativeUnitEntered(unit);
	}

}
