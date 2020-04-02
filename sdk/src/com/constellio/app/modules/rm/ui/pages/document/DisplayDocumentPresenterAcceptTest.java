package com.constellio.app.modules.rm.ui.pages.document;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.UIContext;
import com.constellio.model.entities.enums.ParsingBehavior;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class DisplayDocumentPresenterAcceptTest extends ConstellioTest {

	Users users = new Users();
	@Mock DisplayDocumentView displayDocumentView;
	@Mock CoreViews navigator;
	RMTestRecords rmRecords = new RMTestRecords(zeCollection);
	RMSchemasRecordsServices schemasRecordsServices;
	DisplayDocumentPresenter presenter;
	SessionContext sessionContext;
	@Mock UIContext uiContext;
	RecordServices recordServices;

	MetadataSchemasManager metadataSchemasManager;
	SearchServices searchServices;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(rmRecords)
						.withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent()
		);
		givenConfig(ConstellioEIMConfigs.DEFAULT_PARSING_BEHAVIOR, ParsingBehavior.SYNC_PARSING_FOR_ALL_CONTENTS);

		inCollection(zeCollection).giveWriteAccessTo(aliceWonderland);

		schemasRecordsServices = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		searchServices = getModelLayerFactory().newSearchServices();

		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		sessionContext = FakeSessionContext.chuckNorrisInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);

		when(displayDocumentView.getSessionContext()).thenReturn(sessionContext);
		when(displayDocumentView.getCollection()).thenReturn(zeCollection);
		when(displayDocumentView.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(displayDocumentView.navigateTo()).thenReturn(navigator);
		when(displayDocumentView.getUIContext()).thenReturn(uiContext);

		presenter = new DisplayDocumentPresenter(displayDocumentView, null, false, false);
	}

	@Test
	public void givenDocumentWithoutContentWhenCreatePDFAThenItIsNotVisible()
			throws Exception {

		String docId = "docNoContent";
		String docTitle = "Document Without Content";
		Document document = schemasRecordsServices.newDocumentWithId(docId);
		document.setFolder(rmRecords.folder_C30);
		document.setTitle(docTitle);
		recordServices.add(document);

		presenter.forParams(docId);
		// FIXME
		//assertThat(presenter.presenterUtils.getCreatePDFAState().isVisible()).isFalse();
	}

	@Test
	public void givenDeletedDocumentThenIsLogicallyDeletedTrue() {
		recordServices.logicallyDelete(rmRecords.getDocumentWithContent_A19().getWrappedRecord(), User.GOD);

		presenter.forParams(rmRecords.document_A19);
		assertThat(presenter.isLogicallyDeleted()).isTrue();
	}

	@Test
	public void givenNoCheckoutDocumentThenAlertButtonIsNotVisible()
			throws Exception {

		presenter.forParams(rmRecords.document_A19);
		// FIXME
		//assertThat(presenter.presenterUtils.getAlertWhenAvailableButtonState().isVisible()).isFalse();
	}

	private void connectAsSasquatch() {
		sessionContext = FakeSessionContext.sasquatchInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);
		when(displayDocumentView.getSessionContext()).thenReturn(sessionContext);
		presenter = new DisplayDocumentPresenter(displayDocumentView, null, false, false);
	}

	@Test
	public void givenEventsThenEventsDataProviderReturnValidEvents()
			throws Exception {
		RMSchemasRecordsServices rmSchemasRecordsServices = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		RMEventsSearchServices rmEventsSearchServices = new RMEventsSearchServices(getModelLayerFactory(), zeCollection);
		Transaction transaction = new Transaction();
		transaction.add(rmSchemasRecordsServices.newEvent().setRecordId(rmRecords.document_A19)
				.setTitle(rmRecords.document_A19).setUsername(users.adminIn(zeCollection).getUsername())
				.setType(EventType.MODIFY_DOCUMENT)
				.setCreatedOn(LocalDateTime.now()));
		transaction.add(rmSchemasRecordsServices.newEvent().setRecordId(rmRecords.document_A49)
				.setTitle(rmRecords.document_A49).setUsername(users.adminIn(zeCollection).getUsername())
				.setType(EventType.MODIFY_DOCUMENT)
				.setCreatedOn(LocalDateTime.now()));
		recordServices.execute(transaction);

		getDataLayerFactory().newEventsDao().flush();
		assertThat(searchServices.getResultsCount(
				rmEventsSearchServices.newFindEventByRecordIDQuery(users.adminIn(zeCollection), rmRecords.document_A19)))
				.isEqualTo(1);
		assertThat(searchServices.getResultsCount(
				rmEventsSearchServices.newFindEventByRecordIDQuery(users.adminIn(zeCollection), rmRecords.document_A19)))
				.isEqualTo(1);

		presenter.forParams(rmRecords.document_A19);
		RecordVODataProvider provider = presenter.getEventsDataProvider();
		List<RecordVO> eventList = provider.listRecordVOs(0, 100);
		assertThat(eventList).hasSize(1);
	}

	@Test
	public void givenViewIsEnteredThenAddToCartButtonOnlyShowsWhenUserHasPermission() {
		String roleCode = users.aliceIn(zeCollection).getUserRoles().get(0);
		RolesManager rolesManager = getAppLayerFactory().getModelLayerFactory().getRolesManager();

		Role role = rolesManager.getRole(zeCollection, roleCode);
		Role editedRole = role.withPermissions(new ArrayList<String>());
		rolesManager.updateRole(editedRole);

		connectWithAlice();
		assertThat(presenter.hasCurrentUserPermissionToUseMyCart()).isFalse();
		assertThat(presenter.hasCurrentUserPermissionToUseCartGroup()).isFalse();

		Role editedRole2 = editedRole.withPermissions(asList(RMPermissionsTo.USE_MY_CART, RMPermissionsTo.USE_GROUP_CART));
		rolesManager.updateRole(editedRole2);

		connectWithAlice();
		assertThat(presenter.hasCurrentUserPermissionToUseMyCart()).isTrue();
	}

	private void connectWithAlice() {
		sessionContext = FakeSessionContext.aliceInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);
		when(displayDocumentView.getSessionContext()).thenReturn(sessionContext);
		presenter = new DisplayDocumentPresenter(displayDocumentView, null, false, false);
	}

	@Test
	public void givenDocumentThenPublishAndUnpublishButtonsOnlyShowWhenUserHasTheRightPermission()
			throws Exception {
		RolesManager manager = getModelLayerFactory().getRolesManager();
		Role zeNewRole = new Role(zeCollection, "zeNewRoleWithPublishPermission",
				asList(RMPermissionsTo.PUBLISH_AND_UNPUBLISH_DOCUMENTS));
		manager.addRole(zeNewRole);
		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		recordServices.update(users.sasquatchIn(zeCollection).setUserRoles(asList("zeNewRoleWithPublishPermission")));

		assertThat(users.sasquatchIn(zeCollection).getAllRoles()).containsOnly("zeNewRoleWithPublishPermission");

		connectAsSasquatch();
		presenter.forParams(rmRecords.document_A19);
		assertThat(presenter.hasCurrentUserPermissionToPublishOnCurrentDocument()).isTrue();

		manager.updateRole(zeNewRole.withPermissions(new ArrayList<String>()));
		assertThat(manager.getRole(zeCollection, "zeNewRoleWithPublishPermission")
				.hasOperationPermission(RMPermissionsTo.PUBLISH_AND_UNPUBLISH_DOCUMENTS)).isFalse();
		connectAsSasquatch();
		presenter.forParams(rmRecords.document_A19);
		assertThat(presenter.hasCurrentUserPermissionToPublishOnCurrentDocument()).isFalse();
	}
}
