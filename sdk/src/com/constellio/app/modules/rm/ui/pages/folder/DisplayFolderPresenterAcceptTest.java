package com.constellio.app.modules.rm.ui.pages.folder;

import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingType;
import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.UIContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.security.Role;
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
import com.constellio.sdk.tests.SDKViewNavigation;
import com.constellio.sdk.tests.setups.Users;
import org.apache.commons.lang3.StringEscapeUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.*;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

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

	private DisplayFolderPresenter displayFolderPresenterCreation(DisplayFolderView displayFolderView, RecordVO recordVO, boolean popup) {
		presenter = spy(new DisplayFolderPresenter(displayFolderView, recordVO, popup));//spy(
		doNothing().when(presenter).navigateToFolder(any(String.class));
		doNothing().when(presenter).navigateToDocument(any(RecordVO.class));

		return presenter;
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
	public void givenBorrowFolderWhenReturnItThenOk()
			throws Exception {
		displayFolderView.navigate().to(RMViews.class).displayFolder("C30");
		LocalDate borrowingLocalDate = nowDate;
		presenter
				.borrowFolder(borrowingLocalDate, nowDate, rmRecords.getChuckNorris().getId(),
						BorrowingType.BORROW, null);

		nowDate = nowDate.plusDays(5);
		givenTimeIs(nowDate);
		presenter.forParams("C30");
		presenter.returnFolder(nowDate.minusDays(1), borrowingLocalDate);
		recordServices.flush();

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
		presenter.forParams("C30");
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
	public void givenFolderWhenAddToDefaultFavoritesThenIsAdded() {
		Map<String, String> params = new HashMap<>();
		params.put("id", "C30");
		presenter.forParams("C30");
		presenter.addToDefaultFavorite();
		assertThat(presenter.inDefaultFavorites()).isTrue();
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
	public void givenBorrowedFolderWhenRemindingReturnThenOk()
			throws Exception {

		givenTimeIs(shishOClock);
		presenter.forParams("C30");
		presenter.borrowFolder(nowDate, nowDate, rmRecords.getChuckNorris().getId(), BorrowingType.BORROW, null);
		Folder folderC30 = rmRecords.getFolder_C30();

		presenter.forParams("C30");
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
		assertThat(emailToSend.getParameters()).containsOnly(
				"previewReturnDate:" + folderC30.getBorrowPreviewReturnDate(),
				"borrower:chuck",
				"borrowedFolderTitle:Haricot",
				"title:Rappel pour retourner le dossier \"Haricot\"",
				"constellioURL:http://localhost:8080/constellio/",
				"recordURL:http://localhost:8080/constellio/#!displayFolder/C30"
		);
		assertThat(emailToSend.getFrom()).isEqualTo(null);
		verify(displayFolderView).showMessage($("DisplayFolderView.reminderEmailSent"));
	}

	@Test
	public void whenAlertWhenAvailableThenOk()
			throws Exception {

		presenter.forParams("C30");
		presenter.borrowFolder(nowDate, nowDate, rmRecords.getCharles_userInA().getId(), BorrowingType.BORROW, null);
		presenter.alertWhenAvailable();

		connectWithBob();
		presenter.forParams("C30");
		presenter.alertWhenAvailable();

		connectWithAlice();
		presenter.forParams("C30");
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

		presenter.forParams("C30");
		presenter.borrowFolder(nowDate, nowDate, rmRecords.getCharles_userInA().getId(), BorrowingType.BORROW, null);

		presenter.forParams("C30");
		presenter.alertWhenAvailable();
		presenter.alertWhenAvailable();
		assertThat(folderC30.getAlertUsersWhenAvailable()).containsOnly(rmRecords.getAlice().getId());
		connectWithBob();

		presenter.forParams("C30");
		presenter.alertWhenAvailable();
		presenter.alertWhenAvailable();

		folderC30 = rmRecords.getFolder_C30();
		assertThat(folderC30.getAlertUsersWhenAvailable()).containsOnly(rmRecords.getBob_userInAC().getId());
	}

	@Test
	public void givenTwoUsersToAlertWhenReturnFolderThenOneEmailToSend()
			throws Exception {

		presenter.forParams("C30");
		presenter.borrowFolder(nowDate, nowDate, rmRecords.getCharles_userInA().getId(), BorrowingType.BORROW, null);

		presenter.forParams("C30");
		presenter.alertWhenAvailable();

		connectWithBob();

		presenter.forParams("C30");
		presenter.alertWhenAvailable();

		connectWithChuck();

		presenter.forParams("C30");
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

		presenter.forParams("C30");
		presenter.borrowFolder(nowDate, nowDate, rmRecords.getCharles_userInA().getId(), BorrowingType.BORROW, null);

		connectWithBob();
		presenter.forParams("C30");
		presenter.alertWhenAvailable();

		connectWithChuck();
		givenTimeIs(shishOClock);
		presenter.forParams("C30");
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
		final String subject = "Le dossier demandé est disponible: " + folderC30.getTitle();
		assertThat(emailToSend.getSubject()).isEqualTo(subject);
		assertThat(emailToSend.getTemplate()).isEqualTo(RMEmailTemplateConstants.ALERT_AVAILABLE_ID);
		assertThat(emailToSend.getError()).isNull();
		assertThat(emailToSend.getTryingCount()).isEqualTo(0);
		assertThat(emailToSend.getParameters()).hasSize(6);
		assertThat(emailToSend.getParameters().get(0)).isEqualTo("subject" + EmailToSend.PARAMETER_SEPARATOR + StringEscapeUtils.escapeHtml4(subject));
		assertThat(emailToSend.getParameters().get(1))
				.isEqualTo("returnDate" + EmailToSend.PARAMETER_SEPARATOR + shishOClock.toString("yyyy-MM-dd  HH:mm:ss"));
		assertThat(emailToSend.getParameters().get(2))
				.isEqualTo("title" + EmailToSend.PARAMETER_SEPARATOR + folderC30.getTitle());
		assertThat(emailToSend.getParameters().get(3))
				.isEqualTo("constellioURL" + EmailToSend.PARAMETER_SEPARATOR + "http://localhost:8080/constellio/");
		assertThat(emailToSend.getParameters().get(4))
				.isEqualTo(
						"recordURL" + EmailToSend.PARAMETER_SEPARATOR + "http://localhost:8080/constellio/#!displayFolder/C30");
		assertThat(emailToSend.getParameters().get(5))
				.isEqualTo("recordType" + EmailToSend.PARAMETER_SEPARATOR + "dossier");
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
}
