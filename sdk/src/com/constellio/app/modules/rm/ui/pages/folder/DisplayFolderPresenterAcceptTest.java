package com.constellio.app.modules.rm.ui.pages.folder;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingType;
import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.UIContext;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.SDKViewNavigation;
import com.constellio.sdk.tests.setups.Users;

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

		rmSchemasRecordsServices = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
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

		chuckCredentialVO = new UserCredentialVO();
		chuckCredentialVO.setUsername("chuck");

		presenter = new DisplayFolderPresenter(displayFolderView);//spy(
		Map<String, String> params = new HashMap<>();
		params.put("id", "C30");
		presenter.forParams(ParamUtils.addParams("", params));

		rolesManager = getModelLayerFactory().getRolesManager();

		givenTimeIs(nowDate);
	}

	@Test
	public void givenInvalidPreviewReturnDateThenDoNotBorrow()
			throws Exception {

		displayFolderView.navigate().to(RMViews.class).displayFolder("C30");

		presenter.borrowFolder(nowDate, nowDate.minusDays(1), rmRecords.getChuckNorris().getId(),
				BorrowingType.BORROW, null);

		Folder folderC30 = rmRecords.getFolder_C30();
		assertThat(folderC30.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
		assertThat(folderC30.getBorrowed()).isNull();
		assertThat(folderC30.getBorrowDate()).isNull();
		assertThat(folderC30.getBorrowReturnDate()).isNull();
		assertThat(folderC30.getBorrowUser()).isNull();
		assertThat(folderC30.getBorrowUserEntered()).isNull();
		assertThat(folderC30.getBorrowType()).isNull();
		assertThat(
				searchServices.getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(rmRecords.getAdmin())))
				.isEqualTo(0);
	}

	@Test
	public void givenInvalidBorrowingDateThenDoNotBorrow()
			throws Exception {

		displayFolderView.navigate().to(RMViews.class).displayFolder("C30");

		presenter.borrowFolder(nowDate.plusDays(1), nowDate.plusDays(15), rmRecords.getChuckNorris().getId(),
				BorrowingType.BORROW, null);

		Folder folderC30 = rmRecords.getFolder_C30();
		assertThat(folderC30.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
		assertThat(folderC30.getBorrowed()).isNull();
		assertThat(folderC30.getBorrowDate()).isNull();
		assertThat(folderC30.getBorrowReturnDate()).isNull();
		assertThat(folderC30.getBorrowUser()).isNull();
		assertThat(folderC30.getBorrowUserEntered()).isNull();
		assertThat(folderC30.getBorrowType()).isNull();
		assertThat(
				searchServices.getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(rmRecords.getAdmin())))
				.isEqualTo(0);
	}

	@Test
	public void givenInvalidUserThenDoNotBorrow()
			throws Exception {

		displayFolderView.navigate().to(RMViews.class).displayFolder("C30");

		presenter.borrowFolder(nowDate, nowDate.minusDays(1), null, BorrowingType.BORROW, null);

		Folder folderC30 = rmRecords.getFolder_C30();
		assertThat(folderC30.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
		assertThat(folderC30.getBorrowed()).isNull();
		assertThat(folderC30.getBorrowDate()).isNull();
		assertThat(folderC30.getBorrowReturnDate()).isNull();
		assertThat(folderC30.getBorrowUser()).isNull();
		assertThat(folderC30.getBorrowUserEntered()).isNull();
		assertThat(folderC30.getBorrowType()).isNull();
		assertThat(
				searchServices.getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(rmRecords.getAdmin())))
				.isEqualTo(0);
	}

	@Test
	public void whenBorrowFolderThenOk()
			throws Exception {

		displayFolderView.navigate().to(RMViews.class).displayFolder("C30");

		User chuck = rmRecords.getChuckNorris();

		presenter.borrowFolder(nowDate, nowDate, chuck.getId(), BorrowingType.BORROW, null);

		Folder folderC30 = rmRecords.getFolder_C30();
		assertThat(folderC30.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
		assertThat(folderC30.getBorrowed()).isTrue();
		assertThat(folderC30.getBorrowDate().toDate()).isEqualTo(nowDate.toDate());
		assertThat(folderC30.getBorrowReturnDate()).isNull();
		assertThat(folderC30.getBorrowUser()).isEqualTo(rmRecords.getChuckNorris().getId());
		assertThat(folderC30.getBorrowUserEntered()).isEqualTo(rmRecords.getChuckNorris().getId());
		assertThat(folderC30.getBorrowType()).isEqualTo(BorrowingType.BORROW);
		assertThat(
				searchServices.getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(rmRecords.getAdmin())))
				.isEqualTo(1);
		assertThat(presenter.getBorrowMessageState(folderC30))
				.isEqualTo("Dossier emprunté par " + chuck.getTitle() + " le " + nowDate);
	}

	@Test
	//FIXME Ugly sleep
	public void givenBorrowFolderWhenReturnItThenOk()
			throws Exception {
		displayFolderView.navigate().to(RMViews.class).displayFolder("C30");
		LocalDate borrowingLocalDate = nowDate;
		presenter
				.borrowFolder(borrowingLocalDate, nowDate, rmRecords.getChuckNorris().getId(),
						BorrowingType.BORROW, null);

		nowDate = nowDate.plusDays(5);
		givenTimeIs(nowDate);
		Map<String, String> params = new HashMap<>();
		params.put("id", "C30");
		presenter.forParams(ParamUtils.addParams("", params));
		presenter.returnFolder(nowDate.minusDays(1), borrowingLocalDate);
		recordServices.flush();
		//		Thread.sleep(1000);

		Folder folderC30 = rmRecords.getFolder_C30();
		assertThat(folderC30.getBorrowed()).isNull();
		assertThat(folderC30.getBorrowDate()).isNull();
		assertThat(folderC30.getBorrowReturnDate()).isNull();
		assertThat(folderC30.getBorrowUser()).isNull();
		assertThat(folderC30.getBorrowUserEntered()).isNull();
		assertThat(folderC30.getBorrowType()).isNull();
		assertThat(
				searchServices
						.getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(rmRecords.getChuckNorris())))
				.isEqualTo(0);
		List<Record> records = searchServices.search(rmEventsSearchServices.newFindEventByDateRangeAndByUserIdQuery(
				this.rmRecords.getChuckNorris(), EventType.RETURN_FOLDER,
				nowDate.minusDays(1).toDateTimeAtStartOfDay().toLocalDateTime(),
				nowDate.plusDays(1).toDateTimeAtStartOfDay().toLocalDateTime(), this.rmRecords.getChuckNorris().getId()));
		assertThat(records).hasSize(1);
		Event event = new Event(records.get(0), getSchemaTypes());
		assertThat(event.getUsername()).isEqualTo(this.rmRecords.getChuckNorris().getUsername());
		assertThat(event.getType()).isEqualTo(EventType.RETURN_FOLDER);
		assertThat(presenter.getBorrowMessageState(folderC30)).isNull();
		//TODO Francis
		assertThat(event.getCreatedOn().toLocalDate()).isEqualTo(nowDate.minusDays(1));

	}

	@Test
	public void givenBorrowFolderWhenReturnItWithAInvalideReturnDateItThenDoNotReturnIt()
			throws Exception {
		displayFolderView.navigate().to(RMViews.class).displayFolder("C30");
		LocalDate borrowingDate = nowDate;
		User chuck = rmRecords.getChuckNorris();
		presenter.borrowFolder(nowDate, nowDate, chuck.getId(), BorrowingType.BORROW, null);

		givenTimeIs(nowDate.plusDays(1));
		Map<String, String> params = new HashMap<>();
		params.put("id", "C30");
		presenter.forParams(ParamUtils.addParams("", params));
		presenter.returnFolder(nowDate.minusDays(2), borrowingDate);

		Folder folderC30 = rmRecords.getFolder_C30();
		assertThat(folderC30.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
		assertThat(folderC30.getBorrowed()).isTrue();
		assertThat(folderC30.getBorrowDate().toDate()).isEqualTo(nowDate.toDate());
		assertThat(folderC30.getBorrowReturnDate()).isNull();
		assertThat(folderC30.getBorrowUser()).isEqualTo(chuck.getId());
		assertThat(folderC30.getBorrowUserEntered()).isEqualTo(chuck.getId());
		assertThat(folderC30.getBorrowType()).isEqualTo(BorrowingType.BORROW);
		assertThat(
				searchServices.getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(rmRecords.getAdmin())))
				.isEqualTo(1);
		assertThat(presenter.getBorrowMessageState(folderC30)).isEqualTo(
				"Dossier emprunté par " + chuck.getTitle() + " le " + nowDate);
	}

	@Test
	public void givenSemiACtiveBorrowedFolderAndRemovedPermissionToModifySemiActiveBorrwedFolderAndGivenBackThenOk()
			throws Exception {

		givenRemovedPermissionToModifyBorrowedFolder(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER);
		displayFolderView.navigate().to(RMViews.class).displayFolder("C30");
		presenter.borrowFolder(nowDate, nowDate, rmRecords.getChuckNorris().getId(), BorrowingType.BORROW, null);

		displayFolderView.navigate().to(RMViews.class).displayFolder("C30");
		assertThat(presenter.getDeleteButtonState(rmRecords.getChuckNorris(), rmRecords.getFolder_C30()).isVisible()).isFalse();
		assertThat(presenter.getEditButtonState(rmRecords.getChuckNorris(), rmRecords.getFolder_C30()).isVisible()).isFalse();
		assertThat(presenter.getAddFolderButtonState(rmRecords.getChuckNorris(), rmRecords.getFolder_C30()).isVisible())
				.isFalse();
		assertThat(presenter.getAddDocumentButtonState(rmRecords.getChuckNorris(), rmRecords.getFolder_C30()).isVisible())
				.isFalse();
		assertThat(presenter.getPrintButtonState(rmRecords.getChuckNorris(), rmRecords.getFolder_C30()).isVisible()).isFalse();

		givenNoRemovedPermissionsToModifyBorrowedFolder();
		displayFolderView.navigate().to(RMViews.class).displayFolder("C30");

		displayFolderView.navigate().to(RMViews.class).displayFolder("C30");
		assertThat(presenter.getDeleteButtonState(rmRecords.getChuckNorris(), rmRecords.getFolder_C30()).isVisible()).isTrue();
		assertThat(presenter.getEditButtonState(rmRecords.getChuckNorris(), rmRecords.getFolder_C30()).isVisible()).isTrue();
		assertThat(presenter.getAddFolderButtonState(rmRecords.getChuckNorris(), rmRecords.getFolder_C30()).isVisible()).isTrue();
		assertThat(presenter.getAddDocumentButtonState(rmRecords.getChuckNorris(), rmRecords.getFolder_C30()).isVisible())
				.isTrue();
		assertThat(presenter.getPrintButtonState(rmRecords.getChuckNorris(), rmRecords.getFolder_C30()).isVisible()).isTrue();
	}

	@Test
	public void givenInactiveBorrowedFolderAndRemovedPermissionToModifyInactiveBorrwedFolderAndGivenBackThenOk()
			throws Exception {

		Map<String, String> params = new HashMap<>();
		params.put("id", "C50");
		presenter.forParams(ParamUtils.addParams("", params));
		givenRemovedPermissionToModifyBorrowedFolder(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER);
		displayFolderView.navigate().to(RMViews.class).displayFolder("C50");
		presenter.borrowFolder(nowDate, nowDate, rmRecords.getChuckNorris().getId(), BorrowingType.BORROW, null);

		displayFolderView.navigate().to(RMViews.class).displayFolder("C50");
		assertThat(presenter.getDeleteButtonState(rmRecords.getChuckNorris(), rmRecords.getFolder_C50()).isVisible()).isFalse();
		assertThat(presenter.getEditButtonState(rmRecords.getChuckNorris(), rmRecords.getFolder_C50()).isVisible()).isFalse();
		assertThat(presenter.getAddFolderButtonState(rmRecords.getChuckNorris(), rmRecords.getFolder_C50()).isVisible())
				.isFalse();
		assertThat(presenter.getAddDocumentButtonState(rmRecords.getChuckNorris(), rmRecords.getFolder_C50()).isVisible())
				.isFalse();
		assertThat(presenter.getPrintButtonState(rmRecords.getChuckNorris(), rmRecords.getFolder_C50()).isVisible()).isFalse();

		givenNoRemovedPermissionsToModifyBorrowedFolder();
		displayFolderView.navigate().to(RMViews.class).displayFolder("C50");

		displayFolderView.navigate().to(RMViews.class).displayFolder("C50");
		assertThat(presenter.getDeleteButtonState(rmRecords.getChuckNorris(), rmRecords.getFolder_C50()).isVisible()).isTrue();
		assertThat(presenter.getEditButtonState(rmRecords.getChuckNorris(), rmRecords.getFolder_C50()).isVisible()).isTrue();
		assertThat(presenter.getAddFolderButtonState(rmRecords.getChuckNorris(), rmRecords.getFolder_C50()).isVisible()).isTrue();
		assertThat(presenter.getAddDocumentButtonState(rmRecords.getChuckNorris(), rmRecords.getFolder_C50()).isVisible())
				.isTrue();
		assertThat(presenter.getPrintButtonState(rmRecords.getChuckNorris(), rmRecords.getFolder_C50()).isVisible()).isTrue();
	}

	@Test
	public void whenGetTemplatesThenReturnFolderTemplates()
			throws Exception {

		List<LabelTemplate> labelTemplates = presenter.getTemplates();

		assertThat(labelTemplates).hasSize(6);

		assertThat(labelTemplates.get(0).getKey()).isEqualTo("FOLDER_RIGHT_AVERY_5159");
		assertThat(labelTemplates.get(0).getName()).isEqualTo("Code de plan justifié à droite (Avery 5159)");

		assertThat(labelTemplates.get(1).getKey()).isEqualTo("FOLDER_RIGHT_AVERY_5161");
		assertThat(labelTemplates.get(1).getName()).isEqualTo("Code de plan justifié à droite (Avery 5161)");

		assertThat(labelTemplates.get(2).getKey()).isEqualTo("FOLDER_RIGHT_AVERY_5163");
		assertThat(labelTemplates.get(2).getName()).isEqualTo("Code de plan justifié à droite (Avery 5163)");

		assertThat(labelTemplates.get(3).getKey()).isEqualTo("FOLDER_LEFT_AVERY_5159");
		assertThat(labelTemplates.get(3).getName()).isEqualTo("Code de plan justifié à gauche (Avery 5159)");

		assertThat(labelTemplates.get(4).getKey()).isEqualTo("FOLDER_LEFT_AVERY_5161");
		assertThat(labelTemplates.get(4).getName()).isEqualTo("Code de plan justifié à gauche (Avery 5161)");

		assertThat(labelTemplates.get(5).getKey()).isEqualTo("FOLDER_LEFT_AVERY_5163");
		assertThat(labelTemplates.get(5).getName()).isEqualTo("Code de plan justifié à gauche (Avery 5163)");

	}

	@Test
	public void givenBorrowedFolderWhenRemindingReturnThenOk()
			throws Exception {

		givenTimeIs(shishOClock);
		Map<String, String> params = new HashMap<>();
		params.put("id", "C30");
		presenter.forParams(ParamUtils.addParams("", params));
		presenter.borrowFolder(nowDate, nowDate, rmRecords.getChuckNorris().getId(), BorrowingType.BORROW, null);
		Folder folderC30 = rmRecords.getFolder_C30();

		presenter.forParams(ParamUtils.addParams("", params));
		presenter.reminderReturnFolder();

		Metadata subjectMetadata = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getMetadata(EmailToSend.DEFAULT_SCHEMA + "_" + EmailToSend.SUBJECT);
		LogicalSearchCondition condition = from(getSchemaTypes().getSchemaType(EmailToSend.SCHEMA_TYPE))
				.where(subjectMetadata).isContainingText($("DisplayFolderView.returnFolderReminder") + folderC30.getTitle());
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);
		List<Record> emailToSendRecords = searchServices.search(query);

		assertThat(emailToSendRecords).hasSize(1);
		EmailToSend emailToSend = new EmailToSend(emailToSendRecords.get(0), getSchemaTypes());
		assertThat(emailToSend.getSendOn()).isEqualTo(shishOClock);
		assertThat(emailToSend.getSubject()).isEqualTo($("DisplayFolderView.returnFolderReminder") + folderC30.getTitle());
		assertThat(emailToSend.getTemplate()).isEqualTo(RMEmailTemplateConstants.REMIND_BORROW_TEMPLATE_ID);
		assertThat(emailToSend.getTo().get(0).getEmail())
				.isEqualTo(rmSchemasRecordsServices.getUser(folderC30.getBorrowUser()).getEmail());
		assertThat(emailToSend.getTo().get(0).getName())
				.isEqualTo(rmSchemasRecordsServices.getUser(folderC30.getBorrowUser()).getTitle());
		assertThat(emailToSend.getError()).isNull();
		assertThat(emailToSend.getTryingCount()).isEqualTo(0);
		assertThat(emailToSend.getParameters()).hasSize(4);
		assertThat(emailToSend.getParameters().get(0)).isEqualTo("previewReturnDate:" + folderC30.getBorrowPreviewReturnDate());
		assertThat(emailToSend.getParameters().get(1))
				.isEqualTo("borrower:" + chuckNorris);
		assertThat(emailToSend.getParameters().get(2)).isEqualTo("borrowedFolderTitle:" + folderC30.getTitle());
		assertThat(emailToSend.getParameters().get(3))
				.isEqualTo("title:" + $("DisplayFolderView.returnFolderReminder") + " \"" + folderC30.getTitle() + "\"");
		assertThat(emailToSend.getFrom()).isEqualTo(null);
		verify(displayFolderView).showMessage($("DisplayFolderView.reminderEmailSent"));
	}

	//
	@Test
	public void givenNoBorrowedFolderThenRemiderButtonIsNotVisible()
			throws Exception {

		User currentUser = rmRecords.getChuckNorris();

		Map<String, String> params = new HashMap<>();
		params.put("id", "C30");
		presenter.forParams(ParamUtils.addParams("", params));
		assertThat(presenter.getReminderReturnFolderButtonState(currentUser, rmRecords.getFolder_C30()).isVisible()).isFalse();
	}

	@Test
	public void givenBorrowedFolderAndBorrowerThenReminderButtonIsNotVisible()
			throws Exception {

		User currentUser = rmRecords.getChuckNorris();

		Map<String, String> params = new HashMap<>();
		params.put("id", "C30");
		presenter.forParams(ParamUtils.addParams("", params));
		presenter.borrowFolder(nowDate, nowDate, rmRecords.getChuckNorris().getId(), BorrowingType.BORROW, null);
		assertThat(presenter.getReminderReturnFolderButtonState(currentUser, rmRecords.getFolder_C30()).isVisible()).isFalse();
	}

	@Test
	public void givenBorrowedFolderAndAnotherUserThenReminderButtonIsVisible()
			throws Exception {

		User currentUser = rmRecords.getChuckNorris();
		Map<String, String> params = new HashMap<>();
		params.put("id", "C30");
		presenter.forParams(ParamUtils.addParams("", params));
		presenter.borrowFolder(nowDate, nowDate, currentUser.getId(), BorrowingType.BORROW, null);

		sessionContext = FakeSessionContext.bobInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);
		when(displayFolderView.getSessionContext()).thenReturn(sessionContext);
		currentUser = rmRecords.getBob_userInAC();
		presenter = new DisplayFolderPresenter(displayFolderView);
		presenter.forParams(ParamUtils.addParams("", params));

		assertThat(presenter.getAlertWhenAvailableButtonState(currentUser, rmRecords.getFolder_C30()).isVisible()).isTrue();
	}
	//

	@Test
	public void givenNoBorrowedFolderThenAlertButtonIsNotVisible()
			throws Exception {

		User currentUser = rmRecords.getChuckNorris();

		Map<String, String> params = new HashMap<>();
		params.put("id", "C30");
		presenter.forParams(ParamUtils.addParams("", params));
		assertThat(presenter.getAlertWhenAvailableButtonState(currentUser, rmRecords.getFolder_C30()).isVisible()).isFalse();
	}

	@Test
	public void givenBorrowedFolderAndBorrowerThenAlertButtonIsNotVisible()
			throws Exception {

		User currentUser = rmRecords.getChuckNorris();

		Map<String, String> params = new HashMap<>();
		params.put("id", "C30");
		presenter.forParams(ParamUtils.addParams("", params));
		presenter.borrowFolder(nowDate, nowDate, rmRecords.getChuckNorris().getId(), BorrowingType.BORROW, null);
		assertThat(presenter.getAlertWhenAvailableButtonState(currentUser, rmRecords.getFolder_C30()).isVisible()).isFalse();
	}

	@Test
	public void givenBorrowedFolderAndAnotherUserThenAlertButtonIsVisible()
			throws Exception {

		User currentUser = rmRecords.getChuckNorris();
		Map<String, String> params = new HashMap<>();
		params.put("id", "C30");
		presenter.forParams(ParamUtils.addParams("", params));
		presenter.borrowFolder(nowDate, nowDate, currentUser.getId(), BorrowingType.BORROW, null);

		sessionContext = FakeSessionContext.bobInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);
		when(displayFolderView.getSessionContext()).thenReturn(sessionContext);
		currentUser = rmRecords.getBob_userInAC();
		presenter = new DisplayFolderPresenter(displayFolderView);
		presenter.forParams(ParamUtils.addParams("", params));

		assertThat(presenter.getAlertWhenAvailableButtonState(currentUser, rmRecords.getFolder_C30()).isVisible()).isTrue();
	}

	@Test
	public void whenAlertWhenAvailableThenOk()
			throws Exception {

		Map<String, String> params = new HashMap<>();
		params.put("id", "C30");
		presenter.forParams(ParamUtils.addParams("", params));
		presenter.borrowFolder(nowDate, nowDate, rmRecords.getCharles_userInA().getId(), BorrowingType.BORROW, null);
		presenter.alertWhenAvailable();

		connectWithBob();
		presenter.forParams(ParamUtils.addParams("", params));
		presenter.alertWhenAvailable();

		connectWithAlice();
		presenter.forParams(ParamUtils.addParams("", params));
		presenter.alertWhenAvailable();

		Folder folderC30 = rmRecords.getFolder_C30();
		assertThat(folderC30.getAlertUsersWhenAvailable()).containsOnly(
				rmRecords.getAlice().getId(),
				rmRecords.getBob_userInAC().getId());
	}

	@Test
	public void givenSomeUsersToAlertWhenAlertWhenAvailableClickedManyTimeThenAlertOnceToEachUser()
			throws Exception {

		Folder folderC30 = rmRecords.getFolder_C30();
		recordServices.update(folderC30.setAlertUsersWhenAvailable(asList(users.aliceIn(zeCollection).getId())));
		assertThat(folderC30.getAlertUsersWhenAvailable()).containsOnly(rmRecords.getAlice().getId());

		Map<String, String> params = new HashMap<>();
		params.put("id", "C30");
		presenter.forParams(ParamUtils.addParams("", params));
		presenter.borrowFolder(nowDate, nowDate, rmRecords.getCharles_userInA().getId(), BorrowingType.BORROW, null);

		presenter.forParams(ParamUtils.addParams("", params));
		presenter.alertWhenAvailable();
		presenter.alertWhenAvailable();
		assertThat(folderC30.getAlertUsersWhenAvailable()).containsOnly(rmRecords.getAlice().getId());
		connectWithBob();

		presenter.forParams(ParamUtils.addParams("", params));
		presenter.alertWhenAvailable();
		presenter.alertWhenAvailable();

		folderC30 = rmRecords.getFolder_C30();
		assertThat(folderC30.getAlertUsersWhenAvailable()).containsOnly(rmRecords.getBob_userInAC().getId());
	}

	@Test
	public void givenTwoUsersToAlertWhenReturnFolderThenOneEmailToSend()
			throws Exception {

		Map<String, String> params = new HashMap<>();
		params.put("id", "C30");
		presenter.forParams(ParamUtils.addParams("", params));
		presenter.borrowFolder(nowDate, nowDate, rmRecords.getCharles_userInA().getId(), BorrowingType.BORROW, null);

		presenter.forParams(ParamUtils.addParams("", params));
		presenter.alertWhenAvailable();

		connectWithBob();

		presenter.forParams(ParamUtils.addParams("", params));
		presenter.alertWhenAvailable();

		connectWithChuck();

		presenter.forParams(ParamUtils.addParams("", params));
		presenter.returnFolder(nowDate);
		recordServices.flush();

		LogicalSearchQuery query = new LogicalSearchQuery();
		searchEmailsToSend(query);
		List<Record> emailToSendRecords = searchServices.search(query);

		assertThat(emailToSendRecords.size()).isEqualTo(1);

		EmailToSend emailToSend = rmSchemasRecordsServices.wrapEmailToSend(emailToSendRecords.get(0));
		assertThat(emailToSend.getTo()).extracting("email")
				.containsOnly(rmRecords.getBob_userInAC().getEmail());

		Folder folder = rmSchemasRecordsServices.getFolder("C30");
		assertThat(folder.getAlertUsersWhenAvailable()).isEmpty();
	}

	@Test
	public void givenUserToAlertWhenReturnFolderThenEmailToSendIsCreated()
			throws Exception {

		Map<String, String> params = new HashMap<>();
		params.put("id", "C30");
		presenter.forParams(ParamUtils.addParams("", params));
		presenter.borrowFolder(nowDate, nowDate, rmRecords.getCharles_userInA().getId(), BorrowingType.BORROW, null);

		connectWithBob();
		presenter.forParams(ParamUtils.addParams("", params));
		presenter.alertWhenAvailable();

		connectWithChuck();
		givenTimeIs(shishOClock);
		presenter.forParams(ParamUtils.addParams("", params));
		presenter.returnFolder(shishOClock.toLocalDate());

		Metadata subjectMetadata = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getMetadata(EmailToSend.DEFAULT_SCHEMA + "_" + EmailToSend.SUBJECT);
		Metadata sendOnMetadata = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getMetadata(EmailToSend.DEFAULT_SCHEMA + "_" + EmailToSend.SEND_ON);
		User chuck = rmRecords.getChuckNorris();
		Folder folderC30 = rmRecords.getFolder_C30();
		LogicalSearchCondition condition = from(getSchemaTypes().getSchemaType(EmailToSend.SCHEMA_TYPE))
				.where(subjectMetadata).isContainingText(folderC30.getTitle())
				.andWhere(sendOnMetadata).is(shishOClock);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);
		List<Record> emailToSendRecords = searchServices.search(query);

		assertThat(emailToSendRecords).hasSize(1);
		EmailToSend emailToSend = new EmailToSend(emailToSendRecords.get(0), getSchemaTypes());
		assertThat(emailToSend.getTo()).hasSize(1);
		assertThat(emailToSend.getTo().get(0).getName()).isEqualTo(users.bobIn(zeCollection).getTitle());
		assertThat(emailToSend.getTo().get(0).getEmail()).isEqualTo(users.bobIn(zeCollection).getEmail());
		assertThat(emailToSend.getSubject()).isEqualTo("Alerte lorsque le folder est disponible " + folderC30.getTitle());
		assertThat(emailToSend.getTemplate()).isEqualTo(RMEmailTemplateConstants.ALERT_AVAILABLE_ID);
		assertThat(emailToSend.getError()).isNull();
		assertThat(emailToSend.getTryingCount()).isEqualTo(0);
		assertThat(emailToSend.getParameters()).hasSize(2);
		assertThat(emailToSend.getParameters().get(0)).isEqualTo("returnDate" + EmailToSend.PARAMETER_SEPARATOR + shishOClock);
		assertThat(emailToSend.getParameters().get(1))
				.isEqualTo("title" + EmailToSend.PARAMETER_SEPARATOR + folderC30.getTitle());
	}

	//
	private void givenRemovedPermissionToModifyBorrowedFolder(String permission) {

		for (Role role : rolesManager.getAllRoles(zeCollection)) {
			List<String> roles = role.getOperationPermissions();
			List<String> newRoles = new ArrayList<>(roles);
			newRoles.remove(permission);
			role = role.withPermissions(newRoles);
			rolesManager.updateRole(role);
			Role updatedRole = rolesManager.getRole(zeCollection, role.getCode());
			assertThat(updatedRole.getOperationPermissions()).doesNotContain(permission);
		}
	}

	private void givenNoRemovedPermissionsToModifyBorrowedFolder() {

		for (Role role : rolesManager.getAllRoles(zeCollection)) {
			List<String> roles = role.getOperationPermissions();
			List<String> newRoles = new ArrayList<>(roles);
			newRoles.add(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER);
			newRoles.add(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER);
			role = role.withPermissions(newRoles);
			rolesManager.updateRole(role);
			Role updatedRole = rolesManager.getRole(zeCollection, role.getCode());
			assertThat(updatedRole.getOperationPermissions()).contains(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER);
			assertThat(updatedRole.getOperationPermissions()).contains(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER);
		}
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
		presenter = new DisplayFolderPresenter(displayFolderView);
	}

	private void connectWithAlice() {
		sessionContext = FakeSessionContext.aliceInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);
		when(displayFolderView.getSessionContext()).thenReturn(sessionContext);
		presenter = new DisplayFolderPresenter(displayFolderView);
	}

	private void connectWithChuck() {
		sessionContext = FakeSessionContext.chuckNorrisInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);
		when(displayFolderView.getSessionContext()).thenReturn(sessionContext);
		presenter = new DisplayFolderPresenter(displayFolderView);
	}
}
