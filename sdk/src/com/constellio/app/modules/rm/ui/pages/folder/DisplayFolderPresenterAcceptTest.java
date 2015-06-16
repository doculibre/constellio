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
package com.constellio.app.modules.rm.ui.pages.folder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;

public class DisplayFolderPresenterAcceptTest extends ConstellioTest {

	@Mock DisplayFolderView displayFolderView;
	@Mock ConstellioNavigator navigator;
	@Mock UserCredentialVO chuckCredentialVO;
	private final String chuckId = "00000000013";
	RMTestRecords rm;
	SearchServices searchServices;
	DisplayFolderPresenter presenter;
	SessionContext sessionContext;
	LocalDateTime nowDateTime = new LocalDateTime();
	RMEventsSearchServices rmEventsSearchServices;
	RolesManager rolesManager;

	@Before
	public void setUp()
			throws Exception {

		givenCollectionWithTitle(zeCollection, "Collection de test").withConstellioRMModule().withAllTestUsers();

		rm = new RMTestRecords(zeCollection).setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus()
				.withEvents();
		rmEventsSearchServices = new RMEventsSearchServices(getModelLayerFactory(), zeCollection);

		sessionContext = FakeSessionContext.chuckNorrisInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);
		searchServices = getModelLayerFactory().newSearchServices();

		when(displayFolderView.getSessionContext()).thenReturn(sessionContext);
		when(displayFolderView.getCollection()).thenReturn(zeCollection);
		when(displayFolderView.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(displayFolderView.navigateTo()).thenReturn(navigator);
		chuckCredentialVO = new UserCredentialVO();
		chuckCredentialVO.setUsername("chuck");

		presenter = spy(new DisplayFolderPresenter(displayFolderView));
		presenter.forParams("C30");

		rolesManager = getModelLayerFactory().getRolesManager();

		givenTimeIs(nowDateTime);
	}

	@Test
	public void givenInvalidDateThenDoNotBorrow()
			throws Exception {

		displayFolderView.navigateTo().displayFolder("C30");

		presenter.borrowFolder(nowDateTime.minusHours(1).toDate(), chuckId);

		Folder folderC30 = rm.getFolder_C30();
		assertThat(folderC30.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
		assertThat(folderC30.getBorrowed()).isNull();
		assertThat(folderC30.getBorrowDate()).isNull();
		assertThat(folderC30.getBorrowReturnDate()).isNull();
		assertThat(folderC30.getBorrowUser()).isNull();
		assertThat(folderC30.getBorrowUserEntered()).isNull();
		assertThat(searchServices.getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(rm.getAdmin())))
				.isEqualTo(0);
	}

	@Test
	public void givenInvalidUserThenDoNotBorrow()
			throws Exception {

		displayFolderView.navigateTo().displayFolder("C30");

		presenter.borrowFolder(nowDateTime.minusHours(1).toDate(), null);

		Folder folderC30 = rm.getFolder_C30();
		assertThat(folderC30.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
		assertThat(folderC30.getBorrowed()).isNull();
		assertThat(folderC30.getBorrowDate()).isNull();
		assertThat(folderC30.getBorrowReturnDate()).isNull();
		assertThat(folderC30.getBorrowUser()).isNull();
		assertThat(folderC30.getBorrowUserEntered()).isNull();
		assertThat(searchServices.getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(rm.getAdmin())))
				.isEqualTo(0);
	}

	@Test
	public void whenBorrowFolderThenOk()
			throws Exception {

		displayFolderView.navigateTo().displayFolder("C30");

		presenter.borrowFolder(nowDateTime.toDate(), chuckId);

		Folder folderC30 = rm.getFolder_C30();
		assertThat(folderC30.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
		assertThat(folderC30.getBorrowed()).isTrue();
		assertThat(folderC30.getBorrowDate()).isEqualTo(nowDateTime);
		assertThat(folderC30.getBorrowReturnDate()).isNull();
		assertThat(folderC30.getBorrowUser()).isEqualTo(rm.getChuckNorris().getId());
		assertThat(folderC30.getBorrowUserEntered()).isEqualTo(rm.getChuckNorris().getId());
		assertThat(searchServices.getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(rm.getAdmin())))
				.isEqualTo(1);
		assertThat(presenter.getBorrowMessageState(folderC30)).isEqualTo("DisplayFolderview.borrowedFolder");
	}

	@Test
	public void givenBorrowFolderWhenReturnItThenOk()
			throws Exception {

		displayFolderView.navigateTo().displayFolder("C30");
		presenter.borrowFolder(nowDateTime.toDate(), chuckId);

		givenTimeIs(nowDateTime.plusDays(1));
		presenter.returnFolder();

		Folder folderC30 = rm.getFolder_C30();
		assertThat(folderC30.getBorrowed()).isNull();
		assertThat(folderC30.getBorrowDate()).isNull();
		assertThat(folderC30.getBorrowReturnDate()).isNull();
		assertThat(folderC30.getBorrowUser()).isNull();
		assertThat(folderC30.getBorrowUserEntered()).isNull();
		assertThat(searchServices.getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(rm.getAdmin())))
				.isEqualTo(0);
		Thread.sleep(1000);
		List<Record> records = searchServices.search(rmEventsSearchServices.newFindReturnedFoldersByDateRangeQuery(rm.getAdmin(),
				nowDateTime.minusDays(1), nowDateTime.plusDays(1)));
		assertThat(records).hasSize(2);
		Event event = new Event(records.get(1), getSchemaTypes());
		assertThat(event.getUsername()).isEqualTo(rm.getChuckNorris().getUsername());
		assertThat(event.getType()).isEqualTo(EventType.RETURN_FOLDER);
		assertThat(event.getCreatedOn()).isEqualTo(nowDateTime.plusDays(1));
		assertThat(presenter.getBorrowMessageState(folderC30)).isNull();
	}

	@Test
	public void givenSemiACtiveBorrowedFolderAndRemovedPermissionToModifySemiActiveBorrwedFolderAndGivenBackThenOk()
			throws Exception {

		givenRemovedPermissionToModifyBorrowedFolder(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER);
		displayFolderView.navigateTo().displayFolder("C30");
		presenter.borrowFolder(nowDateTime.toDate(), chuckId);

		displayFolderView.navigateTo().displayFolder("C30");
		assertThat(presenter.getDeleteButtonState(rm.getChuckNorris(), rm.getFolder_C30()).isVisible()).isFalse();
		assertThat(presenter.getEditButtonState(rm.getChuckNorris(), rm.getFolder_C30()).isVisible()).isFalse();
		assertThat(presenter.getAddFolderButtonState(rm.getChuckNorris(), rm.getFolder_C30()).isVisible()).isFalse();
		assertThat(presenter.getAddDocumentButtonState(rm.getChuckNorris(), rm.getFolder_C30()).isVisible()).isFalse();
		assertThat(presenter.getPrintButtonState(rm.getChuckNorris(), rm.getFolder_C30()).isVisible()).isFalse();

		givenNoRemovedPermissionsToModifyBorrowedFolder();
		displayFolderView.navigateTo().displayFolder("C30");

		displayFolderView.navigateTo().displayFolder("C30");
		assertThat(presenter.getDeleteButtonState(rm.getChuckNorris(), rm.getFolder_C30()).isVisible()).isTrue();
		assertThat(presenter.getEditButtonState(rm.getChuckNorris(), rm.getFolder_C30()).isVisible()).isTrue();
		assertThat(presenter.getAddFolderButtonState(rm.getChuckNorris(), rm.getFolder_C30()).isVisible()).isTrue();
		assertThat(presenter.getAddDocumentButtonState(rm.getChuckNorris(), rm.getFolder_C30()).isVisible()).isTrue();
		assertThat(presenter.getPrintButtonState(rm.getChuckNorris(), rm.getFolder_C30()).isVisible()).isTrue();
	}

	@Test
	public void givenInactiveBorrowedFolderAndRemovedPermissionToModifyInactiveBorrwedFolderAndGivenBackThenOk()
			throws Exception {

		presenter.forParams("C50");
		givenRemovedPermissionToModifyBorrowedFolder(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER);
		displayFolderView.navigateTo().displayFolder("C50");
		presenter.borrowFolder(nowDateTime.toDate(), chuckId);

		displayFolderView.navigateTo().displayFolder("C50");
		assertThat(presenter.getDeleteButtonState(rm.getChuckNorris(), rm.getFolder_C50()).isVisible()).isFalse();
		assertThat(presenter.getEditButtonState(rm.getChuckNorris(), rm.getFolder_C50()).isVisible()).isFalse();
		assertThat(presenter.getAddFolderButtonState(rm.getChuckNorris(), rm.getFolder_C50()).isVisible()).isFalse();
		assertThat(presenter.getAddDocumentButtonState(rm.getChuckNorris(), rm.getFolder_C50()).isVisible()).isFalse();
		assertThat(presenter.getPrintButtonState(rm.getChuckNorris(), rm.getFolder_C50()).isVisible()).isFalse();

		givenNoRemovedPermissionsToModifyBorrowedFolder();
		displayFolderView.navigateTo().displayFolder("C50");

		displayFolderView.navigateTo().displayFolder("C50");
		assertThat(presenter.getDeleteButtonState(rm.getChuckNorris(), rm.getFolder_C50()).isVisible()).isTrue();
		assertThat(presenter.getEditButtonState(rm.getChuckNorris(), rm.getFolder_C50()).isVisible()).isTrue();
		assertThat(presenter.getAddFolderButtonState(rm.getChuckNorris(), rm.getFolder_C50()).isVisible()).isTrue();
		assertThat(presenter.getAddDocumentButtonState(rm.getChuckNorris(), rm.getFolder_C50()).isVisible()).isTrue();
		assertThat(presenter.getPrintButtonState(rm.getChuckNorris(), rm.getFolder_C50()).isVisible()).isTrue();
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

	private MetadataSchemaTypes getSchemaTypes() {
		return getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
	}
}