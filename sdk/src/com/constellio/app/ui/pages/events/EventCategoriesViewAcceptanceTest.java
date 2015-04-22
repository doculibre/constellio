/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.pages.events;

import static com.constellio.app.ui.i18n.i18n.$;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.AuthorizationDetails;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.setups.Users;

@UiTest
@InDevelopmentTest
public class EventCategoriesViewAcceptanceTest extends ConstellioTest {
	ConstellioWebDriver driver;
	EventcategoriesFacade1 eventCategoriesFacade;

	RecordServices recordServices;
	private RMTestRecords records;
	RMSchemasRecordsServices schemas;
	LoggingServices loggingServices;
	AuthorizationsServices authorizationsServices;

	LocalDateTime testDate = new LocalDateTime().minusDays(1);
	Users users;

	@Before
	public void setUp()
			throws Exception {

		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers().andUsersWithReadAccess(
				"admin").withConstellioRMModule();
		schemas = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		loggingServices = getModelLayerFactory().newLoggingServices();
		authorizationsServices = getModelLayerFactory().newAuthorizationsServices();

		records = new RMTestRecords(zeCollection).setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus();
		users = records.getUsers();
		driver = newWebDriver(loggedAsUserInCollection("admin", zeCollection));

		eventCategoriesFacade = new EventcategoriesFacade1(driver);

	}

	private Folder createFolder(AdministrativeUnit administrativeUnit) {
		Folder folder = schemas.newFolder();
		folder.setAdministrativeUnitEntered(administrativeUnit.getId());
		folder.setFilingSpaceEntered(records.filingId_A);
		folder.setCategoryEntered(records.categoryId_X110);
		folder.setTitle("Ze folder");
		folder.setOpenDate(new LocalDate());
		folder.setCloseDateEntered(new LocalDate());
		return folder;
	}

	@Test
	public void navigateToEachCategoryAndReturn()
			throws Exception {
		navigateToEventsList();
		navigateToCategoryAndReturn(CategoriesConsts.SYSTEM_USAGE_CATEGORY_TITLE);
		navigateToCategoryAndReturn(CategoriesConsts.USERS_AND_GROUPS_ADD_OR_REMOVE_CATEGORY_TITLE);
		navigateToCategoryAndReturn(CategoriesConsts.FOLDERS_AND_DOCUMENTS_CREATION_CATEGORY_TITLE);
		navigateToCategoryAndReturn(CategoriesConsts.FOLDERS_AND_DOCUMENTS_MODIFICATION_CATEGORY_TITLE);
		navigateToCategoryAndReturn(CategoriesConsts.FOLDERS_AND_DOCUMENTS_DELETION_CATEGORY_TITLE);
		navigateToCategoryAndReturn(CategoriesConsts.CURRENTLY_BORROWED_DOCUMENTS_CATEGORY_TITLE);
		navigateToCategoryAndReturn(CategoriesConsts.DOCUMENTS_BORROW_OR_RETURN_CATEGORY_TITLE);
		navigateToCategoryAndReturn(CategoriesConsts.BY_ADMINISTRATIVE_UNIT_CATEGORY_TITLE);
		navigateToCategoryAndReturn(CategoriesConsts.BY_FOLDER_CATEGORY_TITLE);
		navigateToCategoryAndReturn(CategoriesConsts.BY_USER_CATEGORY_TITLE);
		navigateToCategoryAndReturn(CategoriesConsts.DECOMMISSIONING_CATEGORY_TITLE);
	}

	@Test
	public void validateSystemUsageDefaultView()
			throws Exception {
		navigateToSystemUsageEventCategory();
		BaseEventCategoryFacade systemUsageEventCategoryFacade = new BaseEventCategoryFacade(driver);
		validateAllStatValuesAreEmpty(systemUsageEventCategoryFacade);
		assertThat(systemUsageEventCategoryFacade.getStatsCount()).isEqualTo(1);
		assertThat(systemUsageEventCategoryFacade.getStatCaption(CategoriesConsts.OPENED_SESSIONS_INDEX_IN_SYSTEM_USAGE_CATEGORY))
				.isEqualTo($("ListEventsView.openedSessions"));
	}

	@Test
	public void validateUsersAndGroupsAddOrRemoveDefaultView()
			throws Exception {
		navigateToUsersAndGroupsAddOrRemove();
		BaseEventCategoryFacade systemUsageEventCategoryFacade = new BaseEventCategoryFacade(driver);
		validateAllStatValuesAreEmpty(systemUsageEventCategoryFacade);
		assertThat(systemUsageEventCategoryFacade.getStatsCount()).isEqualTo(4);
		assertThat(systemUsageEventCategoryFacade
				.getStatCaption(CategoriesConsts.ADD_USER_STAT_INDEX_IN_USERS_AND_GROUPS_ADD_OR_REMOVE_CATEGORY))
				.isEqualTo($("ListEventsView.createdUsersEvent"));
		assertThat(systemUsageEventCategoryFacade
				.getStatCaption(CategoriesConsts.REMOVE_USER_STAT_INDEX_IN_USERS_AND_GROUPS_ADD_OR_REMOVE_CATEGORY))
				.isEqualTo($("ListEventsView.deletedUsersEvent"));
		assertThat(systemUsageEventCategoryFacade
				.getStatCaption(CategoriesConsts.ADD_GROUP_STAT_INDEX_IN_USERS_AND_GROUPS_ADD_OR_REMOVE_CATEGORY))
				.isEqualTo($("ListEventsView.createdGroupsEvent"));
		assertThat(
				systemUsageEventCategoryFacade
						.getStatCaption(CategoriesConsts.REMOVE_GROUP_STAT_INDEX_IN_USERS_AND_GROUPS_ADD_OR_REMOVE_CATEGORY))
				.isEqualTo(
						$("ListEventsView.deletedGroupsEvent"));
	}

	@Test
	public void validateFoldersAndDocumentsCreationDefaultView()
			throws Exception {
		navigateToFoldersAndDocumentsCreation();
		BaseEventCategoryFacade systemUsageEventCategoryFacade = new BaseEventCategoryFacade(driver);
		validateAllStatValuesAreEmpty(systemUsageEventCategoryFacade);
		assertThat(systemUsageEventCategoryFacade.getStatsCount()).isEqualTo(2);
		assertThat(systemUsageEventCategoryFacade
				.getStatCaption(CategoriesConsts.FOLDERS_CREATION_IN_FOLDERS_AND_DOCUMENTS_CREATION_CATEGORY))
				.isEqualTo($("ListEventsView.foldersCreation"));
		assertThat(systemUsageEventCategoryFacade
				.getStatCaption(CategoriesConsts.DOCUMENTS_CREATION_IN_FOLDERS_AND_DOCUMENTS_CREATION_CATEGORY))
				.isEqualTo($("ListEventsView.documentsCreation"));
	}

	@Test
	public void validateFoldersAndDocumentsModificationDefaultView()
			throws Exception {
		navigateToFoldersAndDocumentsModification();
		BaseEventCategoryFacade systemUsageEventCategoryFacade = new BaseEventCategoryFacade(driver);
		validateAllStatValuesAreEmpty(systemUsageEventCategoryFacade);
		assertThat(systemUsageEventCategoryFacade.getStatsCount()).isEqualTo(2);
		assertThat(systemUsageEventCategoryFacade.getStatCaption(
				CategoriesConsts.FOLDERS_MODIFICATION_IN_FOLDERS_AND_DOCUMENTS_MODIFICATION_CATEGORY))
				.isEqualTo($("ListEventsView.foldersModification"));
		assertThat(systemUsageEventCategoryFacade.getStatCaption(
				CategoriesConsts.DOCUMENTS_MODIFICATION_IN_FOLDERS_AND_DOCUMENTS_MODIFICATION_CATEGORY)).isEqualTo(
				$("ListEventsView.documentsModification"));
	}

	@Test
	public void validateFoldersAndDocumentsDeletionDefaultView()
			throws Exception {
		navigateToFoldersAndDocumentsDeletion();
		BaseEventCategoryFacade systemUsageEventCategoryFacade = new BaseEventCategoryFacade(driver);
		validateAllStatValuesAreEmpty(systemUsageEventCategoryFacade);
		assertThat(systemUsageEventCategoryFacade.getStatsCount()).isEqualTo(2);
		assertThat(systemUsageEventCategoryFacade
				.getStatCaption(CategoriesConsts.FOLDERS_DELETION_IN_FOLDERS_AND_DOCUMENTS_DELETION_CATEGORY))
				.isEqualTo($("ListEventsView.foldersDeletion"));
		assertThat(systemUsageEventCategoryFacade
				.getStatCaption(CategoriesConsts.DOCUMENTS_DELETION_IN_FOLDERS_AND_DOCUMENTS_DELETION_CATEGORY))
				.isEqualTo($("ListEventsView.documentsDeletion"));
	}

	@Test
	public void whenOpenSessionThenStatsUpdated()
			throws Exception {
		Map<String, String> expectedEventValuesMap = openAdminSession();
		recordServices.flush();
		navigateToEventsList();
		int openSessionCountInSystemUsage = getStatValueInEventCategory(
				CategoriesConsts.OPENED_SESSIONS_INDEX_IN_SYSTEM_USAGE_CATEGORY, CategoriesConsts.SYSTEM_USAGE_CATEGORY_TITLE);
		assertThat(openSessionCountInSystemUsage).isEqualTo(1);
		int openSessionCountInByAdmin = getStatValueInEventCategory(
				CategoriesConsts.OPEN_SESSION_STAT_INDEX_IN_BY_USER_CATEGORY, CategoriesConsts.BY_USER_CATEGORY_TITLE,
				users.admin().getUsername());
		assertThat(openSessionCountInByAdmin).isEqualTo(1);
		this.eventCategoriesFacade.loadEvent(CategoriesConsts.SYSTEM_USAGE_CATEGORY_TITLE,
				CategoriesConsts.OPENED_SESSIONS_INDEX_IN_SYSTEM_USAGE_CATEGORY);
		validateEventViewValues(expectedEventValuesMap);
		waitUntilICloseTheBrowsers();
	}

	private Map<String, String> openAdminSession() {
		User adminUser = users.adminIn(zeCollection);
		adminUser.setLastIPAddress("192.168.1.1");
		loggingServices.login(adminUser);
		Map<String, String> expectedEventValuesMap = new HashMap<>();
		expectedEventValuesMap.put("IP", "192.168.1.1");
		expectedEventValuesMap.put("Utilisateur", adminUser.getUsername());
		expectedEventValuesMap.put("Rôles (utilisateur)", "RGD");
		return expectedEventValuesMap;
	}

	@Test
	public void whenCloseSessionThenStatsUpdated()
			throws Exception {
		recordServices.flush();
		navigateToEventsList();
		int openSessionCountInSystemUsage = getStatValueInEventCategory(
				CategoriesConsts.OPENED_SESSIONS_INDEX_IN_SYSTEM_USAGE_CATEGORY, CategoriesConsts.SYSTEM_USAGE_CATEGORY_TITLE);
		assertThat(openSessionCountInSystemUsage).isEqualTo(1);
		int openSessionCountInByUser = getStatValueInEventCategory(
				CategoriesConsts.OPEN_SESSION_STAT_INDEX_IN_BY_USER_CATEGORY, CategoriesConsts.BY_USER_CATEGORY_TITLE);
		assertThat(openSessionCountInByUser).isEqualTo(1);
	}

	@Test
	public void whenViewFolderThenStatsUpdated()
			throws Exception {
		recordServices.flush();
		navigateToEventsList();
	}

	@Test
	public void whenCreateFolderThenStatsUpdated()
			throws Exception {
		recordServices.flush();
		navigateToEventsList();
	}

	@Test
	public void whenModifyFolderThenStatsUpdated()
			throws Exception {
		recordServices.flush();
		navigateToEventsList();
	}

	@Test
	public void whenDeleteFolderThenStatsUpdated()
			throws Exception {
		recordServices.flush();
		navigateToEventsList();
	}

	@Test
	public void whenBorrowFolderThenStatsUpdated()
			throws Exception {
		recordServices.flush();
		navigateToEventsList();
	}

	@Test
	public void whenReturnFolderThenStatsUpdated()
			throws Exception {
		recordServices.flush();
		navigateToEventsList();
	}

	@Test
	public void whenViewDocumentThenStatsUpdated()
			throws Exception {
		recordServices.flush();
		navigateToEventsList();
	}

	@Test
	public void whenCreateDocumentThenStatsUpdated()
			throws Exception {
		recordServices.flush();
		navigateToEventsList();
	}

	@Test
	public void whenModifyDocumentThenStatsUpdated()
			throws Exception {
		recordServices.flush();
		navigateToEventsList();
	}

	@Test
	public void whenDeleteDocumentThenStatsUpdated()
			throws Exception {
		recordServices.flush();
		navigateToEventsList();
	}

	@Test
	public void whenBorrowDocumentThenStatsUpdated()
			throws Exception {
		recordServices.flush();
		navigateToEventsList();
	}

	@Test
	public void whenReturnDocumentThenStatsUpdated()
			throws Exception {
		recordServices.flush();
		navigateToEventsList();
	}

	@Test
	public void whenAddUserThenStatsUpdated()
			throws Exception {
		Map<String, String> expectedEventValuesMap = addUserClaraByAdminInAdminUnit11();
		recordServices.flush();
		navigateToEventsList();
		assertThat(getAddUserStatInUsersAndGroupsAddOrRemoveView()).isEqualTo(1);
		assertThat(getAddUserStatInByUserView(users.admin().getUsername())).isEqualTo(1);
		this.eventCategoriesFacade.loadEvent(
				CategoriesConsts.USERS_AND_GROUPS_ADD_OR_REMOVE_CATEGORY_TITLE,
				CategoriesConsts.ADD_USER_STAT_INDEX_IN_BY_ADMINISTRATIVE_UNIT_CATEGORY);
		validateEventViewValues(expectedEventValuesMap);
	}

	@Test
	public void whenModifyUserThenStatsUpdated()
			throws Exception {
		recordServices.flush();
		navigateToEventsList();
	}

	@Test
	public void whenDeleteUserThenStatsUpdated()
			throws Exception {
		recordServices.flush();
		navigateToEventsList();
	}

	@Test
	public void whenAddGroupThenStatsUpdated()
			throws Exception {
		recordServices.flush();
		navigateToEventsList();
	}

	@Test
	public void whenModifyGroupThenStatsUpdated()
			throws Exception {
		recordServices.flush();
		navigateToEventsList();
	}

	@Test
	public void whenDeleteGroupThenStatsUpdated()
			throws Exception {
		recordServices.flush();
		navigateToEventsList();
	}

	@Test
	public void whenGrantFolderPermissionThenStatsUpdated()
			throws Exception {
		recordServices.flush();
		navigateToEventsList();
	}

	@Test
	public void whenModifyFolderPermissionThenStatsUpdated()
			throws Exception {
		recordServices.flush();
		navigateToEventsList();
	}

	@Test
	public void whenDeleteFolderPermissionThenStatsUpdated()
			throws Exception {
		recordServices.flush();
		navigateToEventsList();
	}

	@Test
	public void whenGrantDocumentPermissionThenStatsUpdated()
			throws Exception {
		recordServices.flush();
		navigateToEventsList();
	}

	@Test
	public void whenModifyDocumentPermissionThenStatsUpdated()
			throws Exception {
		recordServices.flush();
		navigateToEventsList();
	}

	@Test
	public void whenDeleteDocumentPermissionThenStatsUpdated()
			throws Exception {
		recordServices.flush();
		navigateToEventsList();
	}

	@Test
	public void whenRelocateFolderThenStatsUpdated()
			throws Exception {
		recordServices.flush();
		navigateToEventsList();
	}

	@Test
	public void whenDepositFolderThenStatsUpdated()
			throws Exception {
		recordServices.flush();
		navigateToEventsList();
	}

	@Test
	public void whenDestructFolderThenStatsUpdated()
			throws Exception {
		recordServices.flush();
		navigateToEventsList();
	}

	private void validateEventViewValues(Map<String, String> expectedEventValuesMap) {
		EventViewFacade eventViewFacade = new EventViewFacade(driver);
		for (String key : expectedEventValuesMap.keySet()) {
			String expectedValue = expectedEventValuesMap.get(key);
			String currentValue = eventViewFacade.getFirstEventValueAtColumn(key);
			assertThat(currentValue).isEqualTo(expectedValue);
		}
	}

	private Map<String, String> addUserClaraByAdminInAdminUnit11() {
		addUserInAdminUnit("Clara", users.adminIn(zeCollection), schemas.getAdministrativeUnit(records.unitId_11));
		Map<String, String> expectedEventValuesMap = new HashMap<>();
		expectedEventValuesMap.put("", "");
		return expectedEventValuesMap;
	}

	private int getAddUserStatInByUserView(String username) {
		return getStatValueInEventCategory(CategoriesConsts.ADD_USER_STAT_INDEX_IN_BY_USER_CATEGORY,
				CategoriesConsts.BY_USER_CATEGORY_TITLE, username);
	}

	private int getAddUserStatInUsersAndGroupsAddOrRemoveView() {
		return getStatValueInEventCategory(CategoriesConsts.ADD_USER_STAT_INDEX_IN_USERS_AND_GROUPS_ADD_OR_REMOVE_CATEGORY,
				CategoriesConsts.USERS_AND_GROUPS_ADD_OR_REMOVE_CATEGORY_TITLE);
	}

	private int getStatValueInEventCategory(int statIndex, String eventCategoryTitle, String byIdValue) {
		return this.eventCategoriesFacade.getStatValue(statIndex, eventCategoryTitle, byIdValue);
	}

	private int getStatValueInEventCategory(int statIndex, String eventCategoryTitle) {
		return getStatValueInEventCategory(statIndex, eventCategoryTitle, null);
	}

	private void addUserInAdminUnit(String username, User user, AdministrativeUnit administrativeUnit) {
		User userToAdd = schemas.newUser();
		userToAdd.setUsername(username);
		addAuthorizationForUserToAccessAdministrativeUnit(userToAdd, administrativeUnit);
		loggingServices.addUser(userToAdd.getWrappedRecord(), user);
	}

	private void addAuthorizationForUserToAccessAdministrativeUnit(User userToAdd, AdministrativeUnit administrativeUnit) {
		authorizationsServices.canWrite(userToAdd, administrativeUnit.getWrappedRecord());
	}

	private Authorization newAuthorizationForUserToAccessAdministrativeUnit(User userToAdd,
			AdministrativeUnit administrativeUnit) {
		List<String> roles = Arrays.asList(Role.WRITE);
		String authorizationDetailId = userToAdd + "." + administrativeUnit;
		AuthorizationDetails detail = AuthorizationDetails
				.create(authorizationDetailId, roles, testDate.toLocalDate(), testDate.plusDays(1).toLocalDate(),
						zeCollection);
		List<String> grantedToPrincipals = new ArrayList<>();
		grantedToPrincipals.add(userToAdd.getId());
		List<String> grantedOnRecords = new ArrayList<>();
		grantedOnRecords.add(administrativeUnit.getId());
		return new Authorization(detail, grantedToPrincipals, grantedOnRecords);
	}

	private void validateAllStatValuesAreEmpty(BaseEventCategoryFacade baseEventCategoryFacade) {
		for (int value : baseEventCategoryFacade.getValues()) {
			assertThat(value).isEqualTo(0);
		}
	}

	private void navigateToSystemUsageEventCategory() {
		driver.navigateTo().url(NavigatorConfigurationService.EVENT_CATEGORY + "/" + EventCategory.SYSTEM_USAGE);
	}

	private void navigateToUsersAndGroupsAddOrRemove() {
		driver.navigateTo()
				.url(NavigatorConfigurationService.EVENT_CATEGORY + "/" + EventCategory.USERS_AND_GROUPS_ADD_OR_REMOVE);
	}

	private void navigateToFoldersAndDocumentsCreation() {
		driver.navigateTo()
				.url(NavigatorConfigurationService.EVENT_CATEGORY + "/" + EventCategory.FOLDERS_AND_DOCUMENTS_CREATION);
	}

	private void navigateToFoldersAndDocumentsModification() {
		driver.navigateTo()
				.url(NavigatorConfigurationService.EVENT_CATEGORY + "/" + EventCategory.FOLDERS_AND_DOCUMENTS_MODIFICATION);
	}

	private void navigateToFoldersAndDocumentsDeletion() {
		driver.navigateTo()
				.url(NavigatorConfigurationService.EVENT_CATEGORY + "/" + EventCategory.FOLDERS_AND_DOCUMENTS_DELETION);
	}

	private void navigateToCurrentlyBorrowedDocumentsCategory() {
		driver.navigateTo().url(NavigatorConfigurationService.EVENT_CATEGORY + "/" + EventCategory.CURRENTLY_BORROWED_DOCUMENTS);
	}

	private void navigateToDocumentsBorrowOrReturnCategory() {
		driver.navigateTo().url(NavigatorConfigurationService.EVENT_CATEGORY + "/" + EventCategory.DOCUMENTS_BORROW_OR_RETURN);
	}

	private void navigateByAdministrativeUnitCategory() {
		driver.navigateTo().url(NavigatorConfigurationService.EVENT_CATEGORY + "/" + EventCategory.EVENTS_BY_ADMINISTRATIVE_UNIT);
	}

	private void navigateToByFolderCategory() {
		driver.navigateTo().url(NavigatorConfigurationService.EVENT_CATEGORY + "/" + EventCategory.EVENTS_BY_FOLDER);
	}

	private void navigateToByUserCategory() {
		driver.navigateTo().url(NavigatorConfigurationService.EVENT_CATEGORY + "/" + EventCategory.EVENTS_BY_USER);
	}

	private void navigateDecommissioningCategory() {
		driver.navigateTo().url(NavigatorConfigurationService.EVENT_CATEGORY + "/" + EventCategory.DECOMMISSIONING_EVENTS);
	}

	private void navigateToCategoryAndReturn(String categoryTitle) {
		BaseEventCategoryFacade baseEventCategoryFacade = eventCategoriesFacade.loadCategory(categoryTitle);
		baseEventCategoryFacade.returnToPreviousPage();
	}

	private void navigateToEventsList() {
		driver.navigateTo().url(NavigatorConfigurationService.EVENTS_LIST);
	}

	private RecordWrapper createDocument(String creatorUserName, LocalDateTime eventDate) {
		return createDocument(creatorUserName).setCreatedOn(eventDate);
	}

	private Event createDocument(String creatorUserName) {
		return schemas.newEvent().setUsername(creatorUserName).setType(EventType.CREATE_DOCUMENT);
	}

	private RecordWrapper createFolderEvent(String creatorUserName, LocalDateTime eventDate) {
		return createFolderEvent(creatorUserName).setCreatedOn(eventDate);
	}

	private Event createFolderEvent(String creatorUserName) {
		return schemas.newEvent().setUsername(creatorUserName).setType(EventType.CREATE_FOLDER);
	}

	private Event createFolderEvent(String creatorUserName, Folder folder) {
		String folderId = folder.getId();
		String principalPath = folder.getWrappedRecord().get(Schemas.PRINCIPAL_PATH);
		Event event = schemas.newEvent().setUsername(creatorUserName).setType(EventType.CREATE_FOLDER).setRecordId(folderId)
				.setEventPrincipalPath(principalPath);

		return event;
	}

	private void toDelete()
			throws RecordServicesException {
		//users = new Users().using(getModelLayerFactory().newUserServices());
		users = records.getUsers();
		User user = users.charlesIn(zeCollection);
		String username = user.getUsername();
		System.out.println(user.getId());//00000000006
		Transaction transaction = new Transaction();
		transaction
				.add(createDocument(username, testDate.minusDays(1)));

		transaction.add(createFolderEvent(username, testDate));
		AdministrativeUnit administrativeUnit = records.getUnit10();
		System.out.println("au :" + administrativeUnit.getId());//unitId_10
		Folder folder = createFolder(administrativeUnit);

		getModelLayerFactory().newRecordServices().execute(transaction);
		Event event = createFolderEvent(username, folder);
		transaction
				.add(event);
		recordServices.execute(transaction);
	}

}
