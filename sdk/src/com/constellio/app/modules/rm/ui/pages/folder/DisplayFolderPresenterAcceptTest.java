package com.constellio.app.modules.rm.ui.pages.folder;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.UIContext;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
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
import java.util.List;
import java.util.Locale;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
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
						.withFoldersAndContainersOfEveryStatus().withEvents()
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
				"documentInA49", "documentInA51", "documentInA53", "documentInA54").hasSize(7);

		presenter.forParams(rmRecords.folder_A51);
		assertThat(searchServices.search(presenter.getDocumentsQuery())).extracting("id").contains(
				"documentInA51").hasSize(3);

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
