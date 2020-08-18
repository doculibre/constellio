package com.constellio.app.ui.pages.search;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.UIContext;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.constellio.model.entities.schemas.Schemas.IS_DETACHED_AUTHORIZATIONS;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class AdvancedSearchPresenterAcceptanceTest extends ConstellioTest {

	Users users = new Users();
	@Mock
	AdvancedSearchViewImpl advancedSearchView;
	@Mock
	CoreViews navigator;
	RMTestRecords rmRecords = new RMTestRecords(zeCollection);
	RMSchemasRecordsServices schemasRecordsServices;
	AdvancedSearchPresenter presenter;
	SessionContext sessionContext;
	@Mock
	UIContext uiContext;
	RecordServices recordServices;
	LocalDateTime now = new LocalDateTime();
	LocalDateTime shishOClock = new LocalDateTime().plusDays(1);

	MetadataSchemasManager metadataSchemasManager;
	SearchServices searchServices;

	@Before
	public void setUp()
			throws Exception {
		givenBackgroundThreadsEnabled();
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(rmRecords)
						.withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent()
		);
		inCollection(zeCollection).giveWriteAccessTo(aliceWonderland);

		schemasRecordsServices = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		searchServices = getModelLayerFactory().newSearchServices();

		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		sessionContext = FakeSessionContext.chuckNorrisInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);

		when(advancedSearchView.getSessionContext()).thenReturn(sessionContext);
		when(advancedSearchView.getCollection()).thenReturn(zeCollection);
		when(advancedSearchView.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(advancedSearchView.getUIContext()).thenReturn(uiContext);

		presenter = new AdvancedSearchPresenter(advancedSearchView);
	}

	@Test
	public void givenViewIsEnteredThenAddToCartButtonOnlyShowsWhenUserHasPermission() {
		List<String> userRoles = users.aliceIn(zeCollection).getUserRoles();
		String roleCode = userRoles.get(0);
		RolesManager rolesManager = getAppLayerFactory().getModelLayerFactory().getRolesManager();

		Role role = rolesManager.getRole(zeCollection, roleCode);
		Role editedRole = role.withPermissions(new ArrayList<String>());
		rolesManager.updateRole(editedRole);

		connectWithAlice();
		assertThat(presenter.hasCurrentUserPermissionToUseCart()).isFalse();

		Role editedRole2 = editedRole.withPermissions(asList(RMPermissionsTo.USE_MY_CART));
		rolesManager.updateRole(editedRole2);

		connectWithAlice();
		assertThat(presenter.hasCurrentUserPermissionToUseCart()).isTrue();
	}

	@Test
	public void givenAdvanceSearchThenMetadataChoiceIsLimitedByUsedSchemas()
			throws RecordServicesException {
		connectWithAdmin();
		List<MetadataVO> baseMetadatas = presenter.getMetadataAllowedInAdvancedSearch(Folder.SCHEMA_TYPE);

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(Folder.SCHEMA_TYPE).createCustomSchema("customSchema").create("newSearchableMetadata")
						.setType(MetadataValueType.STRING).setSearchable(true);
			}
		});

		SchemasDisplayManager metadataSchemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();
		metadataSchemasDisplayManager
				.saveMetadata(metadataSchemasDisplayManager.getMetadata(zeCollection, "folder_customSchema_newSearchableMetadata")
						.withVisibleInAdvancedSearchStatus(true));

		assertThat(baseMetadatas).containsAll(presenter.getMetadataAllowedInAdvancedSearch(Folder.SCHEMA_TYPE));
		recordServices.add(newFolder("testFolder").changeSchemaTo("folder_customSchema"));
		recordServices.update(recordServices.getDocumentById("testFolder").set(IS_DETACHED_AUTHORIZATIONS, true));

		List<MetadataVO> newMetadatas = presenter.getMetadataAllowedInAdvancedSearch(Folder.SCHEMA_TYPE);
		newMetadatas.removeAll(baseMetadatas);
		assertThat(newMetadatas.size()).isEqualTo(1);
		assertThat(newMetadatas.get(0).getCode()).isEqualTo("folder_customSchema_newSearchableMetadata");

		connectWithBob();
		assertThat(baseMetadatas).containsAll(presenter.getMetadataAllowedInAdvancedSearch(Folder.SCHEMA_TYPE));
	}

	@Test
	public void givenAdvanceSearchWithTaxonomiesThenIsLimitedByPermission()
			throws RecordServicesException {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				MetadataSchemaTypeBuilder justeadmin = types.createNewSchemaType("justeadmin");
				justeadmin.getDefaultSchema().create("code").setType(MetadataValueType.STRING);
			}
		});

		Map<Language, String> labelTitle = new HashMap<>();
		labelTitle.put(Language.French, "justeadmin");


		MetadataSchemasManager metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		Taxonomy hiddenInHomePage = Taxonomy.createHiddenInHomePage("justeadmin", labelTitle, zeCollection,
				"justeadmin").withUserIds(asList(rmRecords.getAdmin().getId()));
		getModelLayerFactory().getTaxonomiesManager().addTaxonomy(hiddenInHomePage, metadataSchemasManager);

		recordServices.add((RecordWrapper) rm.newHierarchicalValueListItem("justeadmin_default").setCode("J01")
				.set(Schemas.TITLE, "J01"));

		connectWithAdmin();
		List<MetadataVO> baseMetadatas = presenter.getMetadataAllowedInAdvancedSearch(Folder.SCHEMA_TYPE);

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getDefaultSchema(Folder.SCHEMA_TYPE).create("newSearchableMetadata")
						.setType(MetadataValueType.REFERENCE).defineReferencesTo(types.getDefaultSchema("justeadmin"))
						.setSearchable(true);
			}
		});

		SchemasDisplayManager metadataSchemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();
		metadataSchemasDisplayManager
				.saveMetadata(metadataSchemasDisplayManager.getMetadata(zeCollection, "folder_default_newSearchableMetadata")
						.withVisibleInAdvancedSearchStatus(true));

		List<MetadataVO> newMetadatas = presenter.getMetadataAllowedInAdvancedSearch(Folder.SCHEMA_TYPE);
		newMetadatas.removeAll(baseMetadatas);
		assertThat(newMetadatas.size()).isEqualTo(1);
		assertThat(newMetadatas.get(0).getCode()).isEqualTo("folder_default_newSearchableMetadata");

		connectWithBob();
		assertThat(baseMetadatas).containsAll(presenter.getMetadataAllowedInAdvancedSearch(Folder.SCHEMA_TYPE));
	}

	@Test
	public void givenAdvanceSearchThenDoNotShowDisabledMetadatas()
			throws RecordServicesException {
		connectWithAdmin();
		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getDefaultSchema(Folder.SCHEMA_TYPE).get(Folder.BORROWED)
						.setEnabled(false);
			}
		});

		List<MetadataVO> baseMetadatas = presenter.getMetadataAllowedInAdvancedSearch(Folder.SCHEMA_TYPE);

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getDefaultSchema(Folder.SCHEMA_TYPE).get(Folder.BORROWED)
						.setEnabled(true);
			}
		});

		List<MetadataVO> newMetadatas = presenter.getMetadataAllowedInAdvancedSearch(Folder.SCHEMA_TYPE);
		newMetadatas.removeAll(baseMetadatas);
		assertThat(newMetadatas.size()).isEqualTo(1);
		assertThat(newMetadatas.get(0).getCode()).isEqualTo("folder_default_" + Folder.BORROWED);
	}

	private void connectWithAlice() {
		sessionContext = FakeSessionContext.aliceInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);
		when(advancedSearchView.getSessionContext()).thenReturn(sessionContext);
		presenter = new AdvancedSearchPresenter(advancedSearchView);
	}

	private void connectWithAdmin() {
		sessionContext = FakeSessionContext.adminInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);
		when(advancedSearchView.getSessionContext()).thenReturn(sessionContext);
		presenter = new AdvancedSearchPresenter(advancedSearchView);
	}

	private void connectWithBob() {
		sessionContext = FakeSessionContext.bobInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);
		when(advancedSearchView.getSessionContext()).thenReturn(sessionContext);
		presenter = new AdvancedSearchPresenter(advancedSearchView);
	}

	//
	private MetadataSchemaTypes getSchemaTypes() {
		return getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
	}

	private Folder newFolder(String title) {
		return schemasRecordsServices.newFolderWithId("testFolder").setTitle(title).setOpenDate(LocalDate.now())
				.setAdministrativeUnitEntered(rmRecords.unitId_10a)
				.setCategoryEntered(rmRecords.categoryId_X110)
				.setRetentionRuleEntered(rmRecords.getRule2())
				.setCopyStatusEntered(CopyType.PRINCIPAL);
	}
}
