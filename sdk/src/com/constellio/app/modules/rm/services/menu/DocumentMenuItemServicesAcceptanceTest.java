package com.constellio.app.modules.rm.services.menu;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.menu.DocumentMenuItemServices.DocumentMenuItemActionType;
import com.constellio.app.modules.rm.ui.pages.document.DisplayDocumentViewImpl;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemActionState;
import com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus;
import com.constellio.app.services.menu.MenuItemServices;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.enums.ParsingBehavior;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.setups.Users;
import org.apache.commons.lang3.StringEscapeUtils;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static com.constellio.app.modules.rm.services.menu.DocumentMenuItemServices.DocumentMenuItemActionType.DOCUMENT_OPEN;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.FakeSessionContext.forRealUserIncollection;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class DocumentMenuItemServicesAcceptanceTest extends ConstellioTest {

	private Users users = new Users();
	private RMTestRecords rmRecords = new RMTestRecords(zeCollection);
	private RecordServices recordServices;
	private SearchServices searchServices;
	private RMSchemasRecordsServices rm;
	private MetadataSchemaTypes schemaTypes;
	private Map<String, MenuItemAction> menuItemActionByType;
	private MenuItemServices menuItemServices;
	private MenuItemActionBehaviorParams menuItemActionBehaviorParams;

	private LocalDateTime now = new LocalDateTime();
	private LocalDateTime shishOClock = new LocalDateTime(2016, 4, 3, 1, 2, 3);

	@Mock SessionContext sessionContext;
	@Mock DisplayDocumentViewImpl displayDocumentView;

	@Before
	public void setUp() {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(rmRecords)
						.withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent()
		);

		givenConfig(RMConfigs.AGENT_ENABLED, false);
		givenConfig(ConstellioEIMConfigs.DEFAULT_PARSING_BEHAVIOR, ParsingBehavior.SYNC_PARSING_FOR_ALL_CONTENTS);

		inCollection(zeCollection).giveWriteAccessTo(aliceWonderland);

		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		schemaTypes = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		menuItemServices = new MenuItemServices(zeCollection, getAppLayerFactory());

		sessionContext = forRealUserIncollection(users.adminIn(zeCollection));
		when(displayDocumentView.getSessionContext()).thenReturn(sessionContext);

		menuItemActionBehaviorParams = getMenuItemActionBehaviorParams(null);
	}

	@Test
	public void givenDocumentWithContentWhenCreatePDFAThenOk() {

		Content initialContent = rmRecords.getDocumentWithContent_A19().getContent();
		String initialHash = initialContent.getCurrentVersion().getHash();
		String initialOlderVersionHash = initialContent.getHistoryVersions().get(0).getHash();
		assertThat(rmRecords.getDocumentWithContent_A19().getContent().getHistoryVersions()).hasSize(1);

		retrieveMenuItemActions(rm.getDocument(rmRecords.document_A19));
		MenuItemAction action = menuItemActionByType.get(DocumentMenuItemActionType.DOCUMENT_CREATE_PDF.name());

		assertThat(action.getState().getStatus()).isEqualTo(MenuItemActionStateStatus.VISIBLE);

		clickAction(action);

		Content modifiedContent = rmRecords.getDocumentWithContent_A19().getContent();

		assertThat(modifiedContent.getCurrentVersion().getMimetype())
				.isEqualTo("application/pdf");
		assertThat(modifiedContent.getCurrentVersion().getHash())
				.isNotEqualTo(initialHash)
				.isNotEqualTo(initialOlderVersionHash);
		assertThat(modifiedContent.getCurrentVersion().getFilename())
				.isEqualTo("Chevreuil.pdf");

		assertThat(modifiedContent.getHistoryVersions()).hasSize(2);
		assertThat(modifiedContent.getHistoryVersions().get(0).getMimetype())
				.isEqualTo("application/vnd.oasis.opendocument.text");
		assertThat(modifiedContent.getHistoryVersions().get(0).getHash())
				.isEqualTo(initialOlderVersionHash);
		assertThat(modifiedContent.getHistoryVersions().get(0).getFilename())
				.isEqualTo("Chevreuil.odt");
		assertThat(modifiedContent.getHistoryVersions().get(1).getMimetype())
				.isEqualTo("application/vnd.oasis.opendocument.text");
		assertThat(modifiedContent.getHistoryVersions().get(1).getHash())
				.isEqualTo(initialHash);
		assertThat(modifiedContent.getHistoryVersions().get(1).getFilename())
				.isEqualTo("Chevreuil.odt");
	}

	@Test
	public void givenCheckedOutDocumentWhenCreatePDFAThenItIsNotVisible() {
		assertThat(rmRecords.getDocumentWithContent_A19().getContent().getHistoryVersions()).hasSize(1);

		retrieveMenuItemActions(rm.getDocument(rmRecords.document_A19));
		MenuItemAction action = menuItemActionByType.get(DocumentMenuItemActionType.DOCUMENT_CHECK_OUT.name());
		clickAction(action);

		MenuItemActionState state = menuItemServices.getStateForAction(
				DocumentMenuItemActionType.DOCUMENT_CREATE_PDF.name(),
				recordServices.getDocumentById(rmRecords.document_A19),
				menuItemActionBehaviorParams);

		assertThat(state.getStatus()).isEqualTo(MenuItemActionStateStatus.HIDDEN);
	}

	@Test
	public void givenCheckoutDocumentAndCurrentBorrowerThenAlertButtonIsNotVisible() {
		retrieveMenuItemActions(rm.getDocument(rmRecords.document_A19));
		MenuItemAction action = menuItemActionByType.get(DocumentMenuItemActionType.DOCUMENT_CHECK_OUT.name());
		clickAction(action);

		MenuItemActionState state = menuItemServices.getStateForAction(
				DocumentMenuItemActionType.DOCUMENT_AVAILABLE_ALERT.name(),
				recordServices.getDocumentById(rmRecords.document_A19),
				menuItemActionBehaviorParams);

		assertThat(state.getStatus()).isEqualTo(MenuItemActionStateStatus.HIDDEN);
	}

	@Test
	public void givenCheckoutDocumentAndAnotherUserThenAlertButtonIsVisible() {
		retrieveMenuItemActions(rm.getDocument(rmRecords.document_A19));
		MenuItemAction action = menuItemActionByType.get(DocumentMenuItemActionType.DOCUMENT_CHECK_OUT.name());
		clickAction(action);

		connectAsBob(rm.getDocument(rmRecords.document_A19));

		MenuItemActionState state = menuItemServices.getStateForAction(
				DocumentMenuItemActionType.DOCUMENT_AVAILABLE_ALERT.name(),
				recordServices.getDocumentById(rmRecords.document_A19),
				menuItemActionBehaviorParams);

		assertThat(state.getStatus()).isEqualTo(MenuItemActionStateStatus.VISIBLE);
	}

	@Test
	public void whenAlertWhenAvailableThenOk() {
		retrieveMenuItemActions(rm.getDocument(rmRecords.document_A19));
		clickAction(menuItemActionByType.get(DocumentMenuItemActionType.DOCUMENT_CHECK_OUT.name()));
		clickAction(menuItemActionByType.get(DocumentMenuItemActionType.DOCUMENT_AVAILABLE_ALERT.name()));

		connectAsBob(rm.getDocument(rmRecords.document_A19));
		clickAction(menuItemActionByType.get(DocumentMenuItemActionType.DOCUMENT_AVAILABLE_ALERT.name()));

		connectAsAlice(rm.getDocument(rmRecords.document_A19));
		clickAction(menuItemActionByType.get(DocumentMenuItemActionType.DOCUMENT_AVAILABLE_ALERT.name()));

		Document document = rmRecords.getDocumentWithContent_A19();
		assertThat(document.getAlertUsersWhenAvailable()).containsOnly(
				rmRecords.getBob_userInAC().getId(),
				rmRecords.getAlice().getId());
	}

	@Test
	public void givenSomeUsersToAlertWhenAlertWhenAvailableClickedManyTimeThenAlertOnceToEachUser() {

		retrieveMenuItemActions(rm.getDocument(rmRecords.document_A19));
		clickAction(menuItemActionByType.get(DocumentMenuItemActionType.DOCUMENT_CHECK_OUT.name()));

		clickAction(menuItemActionByType.get(DocumentMenuItemActionType.DOCUMENT_AVAILABLE_ALERT.name()));
		clickAction(menuItemActionByType.get(DocumentMenuItemActionType.DOCUMENT_AVAILABLE_ALERT.name()));

		connectAsBob(rm.getDocument(rmRecords.document_A19));
		clickAction(menuItemActionByType.get(DocumentMenuItemActionType.DOCUMENT_AVAILABLE_ALERT.name()));
		clickAction(menuItemActionByType.get(DocumentMenuItemActionType.DOCUMENT_AVAILABLE_ALERT.name()));

		Document document = rmRecords.getDocumentWithContent_A19();
		assertThat(document.getAlertUsersWhenAvailable()).containsOnly(rmRecords.getBob_userInAC().getId());
	}

	@Test
	public void givenUserToAlertWhenReturnDocumentThenEmailToSendIsCreated() throws Exception {
		LogicalSearchCondition condition = from(schemaTypes.getSchemaType(EmailToSend.SCHEMA_TYPE))
				.where(rm.emailToSend.template()).isEqualTo(RMEmailTemplateConstants.ALERT_AVAILABLE_ID);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);
		long numberOfPreExistingEmails = searchServices.getResultsCount(query);
		givenTimeIs(now);

		retrieveMenuItemActions(rm.getDocument(rmRecords.document_A19));
		clickAction(menuItemActionByType.get(DocumentMenuItemActionType.DOCUMENT_CHECK_OUT.name()));

		givenTimeIs(shishOClock);
		connectAsBob(rm.getDocument(rmRecords.document_A19));
		clickAction(menuItemActionByType.get(DocumentMenuItemActionType.DOCUMENT_AVAILABLE_ALERT.name()));
		List<String> usersToAlert = rmRecords.getDocumentWithContent_A19().getAlertUsersWhenAvailable();

		Content content = rmRecords.getDocumentWithContent_A19().getContent().checkIn();
		Document document = rmRecords.getDocumentWithContent_A19().setContent(content);
		recordServices.update(document.getWrappedRecord());
		recordServices.flush();

		Document documentWithContentA19 = rmRecords.getDocumentWithContent_A19();
		List<Record> emailToSendRecords = searchServices.search(query);

		assertThat(emailToSendRecords).hasSize(1 + (int) numberOfPreExistingEmails);
		EmailToSend emailToSend = new EmailToSend(emailToSendRecords.get(0), schemaTypes);
		assertThat(emailToSend.getTo()).hasSize(usersToAlert.size());
		assertThat(emailToSend.getTo().get(0).getName()).isEqualTo(users.bobIn(zeCollection).getTitle());
		assertThat(emailToSend.getTo().get(0).getEmail()).isEqualTo(users.bobIn(zeCollection).getEmail());
		assertThat(emailToSend.getSubject()).isEqualTo("Le document demandé est disponible: " + documentWithContentA19
				.getTitle());
		assertThat(emailToSend.getTemplate()).isEqualTo(RMEmailTemplateConstants.ALERT_AVAILABLE_ID);
		assertThat(emailToSend.getError()).isNull();
		assertThat(emailToSend.getTryingCount()).isEqualTo(0);
		assertThat(emailToSend.getParameters()).containsOnly("subject:" + StringEscapeUtils.escapeHtml4("Le document demandé est disponible: Chevreuil.odt"),
				"returnDate:2016-04-03  01:02:03", "title:Chevreuil.odt", "constellioURL:http://localhost:8080/constellio/",
				"recordURL:http://localhost:8080/constellio/#!displayDocument/docA19", "recordType:document");

		assertThat(rmRecords.getDocumentWithContent_A19().getAlertUsersWhenAvailable()).isEmpty();
	}

	@Test
	public void givenSingleDocumentWhenCreateTaskThenDocumentIsAutomaticallyLinkedToTask() {
		//todo: verif que la métadonnée "dossiers liés" contient le document sélectionné
		assertThat(false).isTrue();
	}

	@Test
	public void givenSingleFolderWhenCreateTaskThenFolderIsAutomaticallyLinkedToTask() {
		//todo: verif que la métadonnée "dossiers liés" contient le dossier sélectionné
		assertThat(false).isTrue();
	}

	private void retrieveMenuItemActions(Document document) {
		List<MenuItemAction> menuItemActions = menuItemServices.getActionsForRecord(document.getWrappedRecord(),
				Collections.singletonList(DOCUMENT_OPEN.name()), menuItemActionBehaviorParams);

		menuItemActionByType = menuItemActions.stream().collect(Collectors.toMap(MenuItemAction::getType, m -> m));
	}

	private MenuItemActionBehaviorParams getMenuItemActionBehaviorParams(User user) {
		return new MenuItemActionBehaviorParams() {
			@Override
			public BaseView getView() {
				return displayDocumentView;
			}

			@Override
			public Map<String, String> getFormParams() {
				return Collections.emptyMap();
			}

			@Override
			public User getUser() {
				return user != null ? user : users.adminIn(zeCollection);
			}
		};
	}

	private void connectAsBob(Document document) {
		sessionContext = FakeSessionContext.bobInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);
		when(displayDocumentView.getSessionContext()).thenReturn(sessionContext);

		menuItemActionBehaviorParams = getMenuItemActionBehaviorParams(users.bobIn(zeCollection));
		retrieveMenuItemActions(document);
	}

	private void connectAsAlice(Document document) {
		sessionContext = FakeSessionContext.aliceInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);
		when(displayDocumentView.getSessionContext()).thenReturn(sessionContext);

		menuItemActionBehaviorParams = getMenuItemActionBehaviorParams(users.aliceIn(zeCollection));
		retrieveMenuItemActions(document);
	}

	private void clickAction(MenuItemAction action) {
		action.getCommand().accept(Collections.emptyList());
	}

}
