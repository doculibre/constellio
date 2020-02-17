package com.constellio.app.modules.rm.services.menu;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingType;
import com.constellio.app.modules.rm.services.menu.FolderMenuItemServices.FolderMenuItemActionType;
import com.constellio.app.modules.rm.ui.pages.folder.DisplayFolderViewImpl;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemActionState;
import com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus;
import com.constellio.app.services.menu.MenuItemServices;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.enums.ParsingBehavior;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.*;
import java.util.stream.Collectors;

import static com.constellio.sdk.tests.FakeSessionContext.forRealUserIncollection;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class FolderMenuItemServicesAcceptanceTest extends ConstellioTest {

	private Users users = new Users();
	private RMTestRecords rmRecords = new RMTestRecords(zeCollection);
	private RecordServices recordServices;
	private RMSchemasRecordsServices rm;
	private BorrowingServices borrowingServices;
	private Map<String, MenuItemAction> menuItemActionByType;
	private MenuItemServices menuItemServices;

	private RolesManager rolesManager;
	private LocalDate nowDate = new LocalDate();

	@Mock SessionContext sessionContext;
	@Mock DisplayFolderViewImpl displayFolderView;

	@Before
	public void setUp() {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(rmRecords)
						.withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent()
		);
		givenConfig(ConstellioEIMConfigs.DEFAULT_PARSING_BEHAVIOR, ParsingBehavior.SYNC_PARSING_FOR_ALL_CONTENTS);

		inCollection(zeCollection).giveWriteAccessTo(aliceWonderland);

		recordServices = getModelLayerFactory().newRecordServices();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		rolesManager = getModelLayerFactory().getRolesManager();
		borrowingServices = new BorrowingServices(zeCollection, getModelLayerFactory());
		menuItemServices = new MenuItemServices(zeCollection, getAppLayerFactory());

		sessionContext = forRealUserIncollection(users.chuckNorrisIn(zeCollection));
		when(displayFolderView.getSessionContext()).thenReturn(sessionContext);
	}

	@Test
	public void givenSemiActiveBorrowedFolderAndRemovedPermissionToModifySemiActiveBorrowedFolderAndGivenBackThenOk()
			throws Exception {

		givenRemovedPermissionToModifyBorrowedFolder(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER);

		borrowingServices.borrowFolder(rmRecords.folder_C30, nowDate, nowDate.plusDays(15),
				users.chuckNorrisIn(zeCollection), users.chuckNorrisIn(zeCollection), BorrowingType.BORROW, true);

		MenuItemActionState deleteButtonState = menuItemServices.getStateForAction(
				FolderMenuItemActionType.FOLDER_DELETE.name(), recordServices.getDocumentById(rmRecords.folder_C30),
				getMenuItemActionBehaviorParams());
		assertThat(deleteButtonState.getStatus()).isEqualTo(MenuItemActionStateStatus.HIDDEN);

		MenuItemActionState editButtonState = menuItemServices.getStateForAction(
				FolderMenuItemActionType.FOLDER_EDIT.name(), recordServices.getDocumentById(rmRecords.folder_C30),
				getMenuItemActionBehaviorParams());
		assertThat(editButtonState.getStatus()).isEqualTo(MenuItemActionStateStatus.HIDDEN);

		MenuItemActionState addFolderButtonState = menuItemServices.getStateForAction(
				FolderMenuItemActionType.FOLDER_ADD_SUBFOLDER.name(), recordServices.getDocumentById(rmRecords.folder_C30),
				getMenuItemActionBehaviorParams());
		assertThat(addFolderButtonState.getStatus()).isEqualTo(MenuItemActionStateStatus.HIDDEN);

		MenuItemActionState addDocumentButtonState = menuItemServices.getStateForAction(
				FolderMenuItemActionType.FOLDER_ADD_DOCUMENT.name(), recordServices.getDocumentById(rmRecords.folder_C30),
				getMenuItemActionBehaviorParams());
		assertThat(addDocumentButtonState.getStatus()).isEqualTo(MenuItemActionStateStatus.HIDDEN);

		MenuItemActionState printButtonState = menuItemServices.getStateForAction(
				FolderMenuItemActionType.FOLDER_PRINT_LABEL.name(), recordServices.getDocumentById(rmRecords.folder_C30),
				getMenuItemActionBehaviorParams());
		assertThat(printButtonState.getStatus()).isEqualTo(MenuItemActionStateStatus.HIDDEN);

		givenNoRemovedPermissionsToModifyBorrowedFolder();

		deleteButtonState = menuItemServices.getStateForAction(
				FolderMenuItemActionType.FOLDER_DELETE.name(), recordServices.getDocumentById(rmRecords.folder_C30),
				getMenuItemActionBehaviorParams());
		assertThat(deleteButtonState.getStatus()).isEqualTo(MenuItemActionStateStatus.VISIBLE);

		editButtonState = menuItemServices.getStateForAction(
				FolderMenuItemActionType.FOLDER_EDIT.name(), recordServices.getDocumentById(rmRecords.folder_C30),
				getMenuItemActionBehaviorParams());
		assertThat(editButtonState.getStatus()).isEqualTo(MenuItemActionStateStatus.VISIBLE);

		addFolderButtonState = menuItemServices.getStateForAction(
				FolderMenuItemActionType.FOLDER_ADD_SUBFOLDER.name(), recordServices.getDocumentById(rmRecords.folder_C30),
				getMenuItemActionBehaviorParams());
		assertThat(addFolderButtonState.getStatus()).isEqualTo(MenuItemActionStateStatus.VISIBLE);

		addDocumentButtonState = menuItemServices.getStateForAction(
				FolderMenuItemActionType.FOLDER_ADD_DOCUMENT.name(), recordServices.getDocumentById(rmRecords.folder_C30),
				getMenuItemActionBehaviorParams());
		assertThat(addDocumentButtonState.getStatus()).isEqualTo(MenuItemActionStateStatus.VISIBLE);

		printButtonState = menuItemServices.getStateForAction(
				FolderMenuItemActionType.FOLDER_PRINT_LABEL.name(), recordServices.getDocumentById(rmRecords.folder_C30),
				getMenuItemActionBehaviorParams());
		assertThat(printButtonState.getStatus()).isEqualTo(MenuItemActionStateStatus.VISIBLE);
	}

	@Test
	public void givenInactiveBorrowedFolderAndRemovedPermissionToModifyInactiveBorrwedFolderAndGivenBackThenOk()
			throws Exception {

		givenRemovedPermissionToModifyBorrowedFolder(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER);

		borrowingServices.borrowFolder(rmRecords.folder_C50, nowDate, nowDate.plusDays(15),
				users.chuckNorrisIn(zeCollection), users.chuckNorrisIn(zeCollection), BorrowingType.BORROW, true);

		MenuItemActionState deleteButtonState = menuItemServices.getStateForAction(
				FolderMenuItemActionType.FOLDER_DELETE.name(), recordServices.getDocumentById(rmRecords.folder_C50),
				getMenuItemActionBehaviorParams());
		assertThat(deleteButtonState.getStatus()).isEqualTo(MenuItemActionStateStatus.HIDDEN);

		MenuItemActionState editButtonState = menuItemServices.getStateForAction(
				FolderMenuItemActionType.FOLDER_EDIT.name(), recordServices.getDocumentById(rmRecords.folder_C50),
				getMenuItemActionBehaviorParams());
		assertThat(editButtonState.getStatus()).isEqualTo(MenuItemActionStateStatus.HIDDEN);

		MenuItemActionState addFolderButtonState = menuItemServices.getStateForAction(
				FolderMenuItemActionType.FOLDER_ADD_SUBFOLDER.name(), recordServices.getDocumentById(rmRecords.folder_C50),
				getMenuItemActionBehaviorParams());
		assertThat(addFolderButtonState.getStatus()).isEqualTo(MenuItemActionStateStatus.HIDDEN);

		MenuItemActionState addDocumentButtonState = menuItemServices.getStateForAction(
				FolderMenuItemActionType.FOLDER_ADD_DOCUMENT.name(), recordServices.getDocumentById(rmRecords.folder_C50),
				getMenuItemActionBehaviorParams());
		assertThat(addDocumentButtonState.getStatus()).isEqualTo(MenuItemActionStateStatus.HIDDEN);

		MenuItemActionState printButtonState = menuItemServices.getStateForAction(
				FolderMenuItemActionType.FOLDER_PRINT_LABEL.name(), recordServices.getDocumentById(rmRecords.folder_C50),
				getMenuItemActionBehaviorParams());
		assertThat(printButtonState.getStatus()).isEqualTo(MenuItemActionStateStatus.HIDDEN);

		givenNoRemovedPermissionsToModifyBorrowedFolder();

		deleteButtonState = menuItemServices.getStateForAction(
				FolderMenuItemActionType.FOLDER_DELETE.name(), recordServices.getDocumentById(rmRecords.folder_C50),
				getMenuItemActionBehaviorParams());
		assertThat(deleteButtonState.getStatus()).isEqualTo(MenuItemActionStateStatus.VISIBLE);

		editButtonState = menuItemServices.getStateForAction(
				FolderMenuItemActionType.FOLDER_EDIT.name(), recordServices.getDocumentById(rmRecords.folder_C50),
				getMenuItemActionBehaviorParams());
		assertThat(editButtonState.getStatus()).isEqualTo(MenuItemActionStateStatus.VISIBLE);

		addFolderButtonState = menuItemServices.getStateForAction(
				FolderMenuItemActionType.FOLDER_ADD_SUBFOLDER.name(), recordServices.getDocumentById(rmRecords.folder_C50),
				getMenuItemActionBehaviorParams());
		assertThat(addFolderButtonState.getStatus()).isEqualTo(MenuItemActionStateStatus.VISIBLE);

		addDocumentButtonState = menuItemServices.getStateForAction(
				FolderMenuItemActionType.FOLDER_ADD_DOCUMENT.name(), recordServices.getDocumentById(rmRecords.folder_C50),
				getMenuItemActionBehaviorParams());
		assertThat(addDocumentButtonState.getStatus()).isEqualTo(MenuItemActionStateStatus.VISIBLE);

		printButtonState = menuItemServices.getStateForAction(
				FolderMenuItemActionType.FOLDER_PRINT_LABEL.name(), recordServices.getDocumentById(rmRecords.folder_C50),
				getMenuItemActionBehaviorParams());
		assertThat(printButtonState.getStatus()).isEqualTo(MenuItemActionStateStatus.VISIBLE);
	}

	@Test
	public void givenImportedFolderAndRemovedPermissionToShareImportedFolderAndGivenBackThenOk()
			throws Exception {
		recordServices.update(record("A16").set(Schemas.LEGACY_ID, "ChatLegacy"));

		givenRemovedPermissionToShareImportedFolder();

		MenuItemActionState shareButtonState = menuItemServices.getStateForAction(
				FolderMenuItemActionType.FOLDER_SHARE.name(), recordServices.getDocumentById(rmRecords.folder_A16),
				getMenuItemActionBehaviorParams());
		assertThat(shareButtonState.getStatus()).isEqualTo(MenuItemActionStateStatus.HIDDEN);

		givenNoRemovedPermissionsToShareImportedFolder();

		shareButtonState = menuItemServices.getStateForAction(
				FolderMenuItemActionType.FOLDER_SHARE.name(), recordServices.getDocumentById(rmRecords.folder_A16),
				getMenuItemActionBehaviorParams());
		assertThat(shareButtonState.getStatus()).isEqualTo(MenuItemActionStateStatus.VISIBLE);
	}

	@Test
	public void givenNoBorrowedFolderThenRemiderButtonIsNotVisible() {
		MenuItemActionState state = menuItemServices.getStateForAction(
				FolderMenuItemActionType.FOLDER_RETURN_REMAINDER.name(), recordServices.getDocumentById(rmRecords.folder_C30),
				getMenuItemActionBehaviorParams());
		assertThat(state.getStatus()).isEqualTo(MenuItemActionStateStatus.HIDDEN);
	}

	@Test
	public void givenBorrowedFolderAndBorrowerThenReminderButtonIsNotVisible() throws Exception {
		borrowingServices.borrowFolder(rmRecords.folder_C30, nowDate, nowDate.plusDays(15),
				users.chuckNorrisIn(zeCollection), users.chuckNorrisIn(zeCollection), BorrowingType.BORROW, true);

		MenuItemActionState state = menuItemServices.getStateForAction(
				FolderMenuItemActionType.FOLDER_RETURN_REMAINDER.name(), recordServices.getDocumentById(rmRecords.folder_C30),
				getMenuItemActionBehaviorParams());
		assertThat(state.getStatus()).isEqualTo(MenuItemActionStateStatus.HIDDEN);
	}

	@Test
	public void givenBorrowedFolderAndAnotherUserThenReminderButtonIsVisible() throws Exception {
		borrowingServices.borrowFolder(rmRecords.folder_C30, nowDate, nowDate.plusDays(15),
				users.chuckNorrisIn(zeCollection), users.chuckNorrisIn(zeCollection), BorrowingType.BORROW, true);

		connectAsBob(rm.getFolder(rmRecords.folder_C30));

		MenuItemActionState state = menuItemServices.getStateForAction(
				FolderMenuItemActionType.FOLDER_RETURN_REMAINDER.name(), recordServices.getDocumentById(rmRecords.folder_C30),
				getMenuItemActionBehaviorParams(users.bobIn(zeCollection)));
		assertThat(state.getStatus()).isEqualTo(MenuItemActionStateStatus.VISIBLE);
	}

	@Test
	public void givenNoBorrowedFolderThenAlertButtonIsNotVisible() {
		MenuItemActionState state = menuItemServices.getStateForAction(
				FolderMenuItemActionType.FOLDER_AVAILABLE_ALERT.name(), recordServices.getDocumentById(rmRecords.folder_C30),
				getMenuItemActionBehaviorParams());
		assertThat(state.getStatus()).isEqualTo(MenuItemActionStateStatus.HIDDEN);
	}

	@Test
	public void givenBorrowedFolderAndBorrowerThenAlertButtonIsNotVisible() throws Exception {
		borrowingServices.borrowFolder(rmRecords.folder_C30, nowDate, nowDate.plusDays(15),
				users.chuckNorrisIn(zeCollection), users.chuckNorrisIn(zeCollection), BorrowingType.BORROW, true);

		MenuItemActionState state = menuItemServices.getStateForAction(
				FolderMenuItemActionType.FOLDER_AVAILABLE_ALERT.name(), recordServices.getDocumentById(rmRecords.folder_C30),
				getMenuItemActionBehaviorParams());
		assertThat(state.getStatus()).isEqualTo(MenuItemActionStateStatus.HIDDEN);
	}

	@Test
	public void givenBorrowedFolderAndAnotherUserThenAlertButtonIsVisible() throws Exception {
		borrowingServices.borrowFolder(rmRecords.folder_C30, nowDate, nowDate.plusDays(15),
				users.chuckNorrisIn(zeCollection), users.chuckNorrisIn(zeCollection), BorrowingType.BORROW, true);

		connectAsBob(rm.getFolder(rmRecords.folder_C30));

		MenuItemActionState state = menuItemServices.getStateForAction(
				FolderMenuItemActionType.FOLDER_AVAILABLE_ALERT.name(), recordServices.getDocumentById(rmRecords.folder_C30),
				getMenuItemActionBehaviorParams(users.bobIn(zeCollection)));
		assertThat(state.getStatus()).isEqualTo(MenuItemActionStateStatus.VISIBLE);
	}

	@Test
	public void givenFolderInDecommissioningListThenCannotBorrow() {
		MenuItemActionState state = menuItemServices.getStateForAction(
				FolderMenuItemActionType.FOLDER_BORROW.name(), recordServices.getDocumentById(rmRecords.folder_A48),
				getMenuItemActionBehaviorParams());
		assertThat(state.getStatus()).isEqualTo(MenuItemActionStateStatus.HIDDEN);

		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		recordServices.physicallyDeleteNoMatterTheStatus(rmRecords.getList10().getWrappedRecord(), User.GOD,
				new RecordPhysicalDeleteOptions());
		recordServices.physicallyDeleteNoMatterTheStatus(rmRecords.getList17().getWrappedRecord(), User.GOD,
				new RecordPhysicalDeleteOptions());

		state = menuItemServices.getStateForAction(
				FolderMenuItemActionType.FOLDER_BORROW.name(), recordServices.getDocumentById(rmRecords.folder_A48),
				getMenuItemActionBehaviorParams());
		assertThat(state.getStatus()).isEqualTo(MenuItemActionStateStatus.VISIBLE);
	}

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

	private void givenRemovedPermissionToShareImportedFolder() {

		for (Role role : rolesManager.getAllRoles(zeCollection)) {
			List<String> roles = role.getOperationPermissions();
			List<String> newRoles = new ArrayList<>(roles);
			newRoles.remove(RMPermissionsTo.SHARE_A_IMPORTED_FOLDER);
			role = role.withPermissions(newRoles);
			rolesManager.updateRole(role);
			Role updatedRole = rolesManager.getRole(zeCollection, role.getCode());
			assertThat(updatedRole.getOperationPermissions()).doesNotContain(RMPermissionsTo.SHARE_A_IMPORTED_FOLDER);
		}
	}

	private void givenNoRemovedPermissionsToShareImportedFolder() {

		for (Role role : rolesManager.getAllRoles(zeCollection)) {
			List<String> roles = role.getOperationPermissions();
			List<String> newRoles = new ArrayList<>(roles);
			newRoles.add(RMPermissionsTo.SHARE_A_IMPORTED_FOLDER);
			role = role.withPermissions(newRoles);
			rolesManager.updateRole(role);
			Role updatedRole = rolesManager.getRole(zeCollection, role.getCode());
			assertThat(updatedRole.getOperationPermissions()).contains(RMPermissionsTo.SHARE_A_IMPORTED_FOLDER);
		}
	}

	private void retrieveMenuItemActions(Folder folder) {
		List<MenuItemAction> menuItemActions = menuItemServices.getActionsForRecord(folder.getWrappedRecord(),
				Collections.emptyList(), getMenuItemActionBehaviorParams());

		menuItemActionByType = menuItemActions.stream().collect(Collectors.toMap(MenuItemAction::getType, m -> m));
	}

	private MenuItemActionBehaviorParams getMenuItemActionBehaviorParams() {
		return getMenuItemActionBehaviorParams(users.chuckNorrisIn(zeCollection));
	}

	private MenuItemActionBehaviorParams getMenuItemActionBehaviorParams(User user) {
		return new MenuItemActionBehaviorParams() {
			@Override
			public BaseView getView() {
				return displayFolderView;
			}

			@Override
			public Map<String, String> getFormParams() {
				return Collections.emptyMap();
			}

			@Override
			public User getUser() {
				return user;
			}
		};
	}

	private void connectAsBob(Folder folder) {
		sessionContext = FakeSessionContext.bobInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);
		when(displayFolderView.getSessionContext()).thenReturn(sessionContext);

		retrieveMenuItemActions(folder);
	}

}
