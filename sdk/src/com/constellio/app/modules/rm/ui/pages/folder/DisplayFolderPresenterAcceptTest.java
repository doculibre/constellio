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
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.reports.model.labels.LabelsReportLayout;
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
	RMTestRecords records = new RMTestRecords(zeCollection);
	SearchServices searchServices;
	DisplayFolderPresenter presenter;
	SessionContext sessionContext;
	LocalDateTime nowDateTime = new LocalDateTime();
	RMEventsSearchServices rmEventsSearchServices;
	RolesManager rolesManager;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withEvents()
		);

		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

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

		presenter.borrowFolder(nowDateTime.minusHours(1).toDate(), records.getChuckNorris().getId());

		Folder folderC30 = records.getFolder_C30();
		assertThat(folderC30.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
		assertThat(folderC30.getBorrowed()).isNull();
		assertThat(folderC30.getBorrowDate()).isNull();
		assertThat(folderC30.getBorrowReturnDate()).isNull();
		assertThat(folderC30.getBorrowUser()).isNull();
		assertThat(folderC30.getBorrowUserEntered()).isNull();
		assertThat(
				searchServices.getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(records.getAdmin())))
				.isEqualTo(0);
	}

	@Test
	public void givenInvalidUserThenDoNotBorrow()
			throws Exception {

		displayFolderView.navigateTo().displayFolder("C30");

		presenter.borrowFolder(nowDateTime.minusHours(1).toDate(), null);

		Folder folderC30 = records.getFolder_C30();
		assertThat(folderC30.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
		assertThat(folderC30.getBorrowed()).isNull();
		assertThat(folderC30.getBorrowDate()).isNull();
		assertThat(folderC30.getBorrowReturnDate()).isNull();
		assertThat(folderC30.getBorrowUser()).isNull();
		assertThat(folderC30.getBorrowUserEntered()).isNull();
		assertThat(
				searchServices.getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(records.getAdmin())))
				.isEqualTo(0);
	}

	@Test
	public void whenBorrowFolderThenOk()
			throws Exception {

		displayFolderView.navigateTo().displayFolder("C30");

		presenter.borrowFolder(nowDateTime.toDate(), records.getChuckNorris().getId());

		Folder folderC30 = records.getFolder_C30();
		assertThat(folderC30.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
		assertThat(folderC30.getBorrowed()).isTrue();
		assertThat(folderC30.getBorrowDate()).isEqualTo(nowDateTime);
		assertThat(folderC30.getBorrowReturnDate()).isNull();
		assertThat(folderC30.getBorrowUser()).isEqualTo(records.getChuckNorris().getId());
		assertThat(folderC30.getBorrowUserEntered()).isEqualTo(records.getChuckNorris().getId());
		assertThat(
				searchServices.getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(records.getAdmin())))
				.isEqualTo(1);
		assertThat(presenter.getBorrowMessageState(folderC30)).isEqualTo("DisplayFolderview.borrowedFolder");
	}

	@Test
	public void givenBorrowFolderWhenReturnItThenOk()
			throws Exception {
		displayFolderView.navigateTo().displayFolder("C30");
		presenter.borrowFolder(nowDateTime.toDate(), records.getChuckNorris().getId());

		givenTimeIs(nowDateTime.plusDays(1));
		presenter.returnFolder();

		Folder folderC30 = records.getFolder_C30();
		assertThat(folderC30.getBorrowed()).isNull();
		assertThat(folderC30.getBorrowDate()).isNull();
		assertThat(folderC30.getBorrowReturnDate()).isNull();
		assertThat(folderC30.getBorrowUser()).isNull();
		assertThat(folderC30.getBorrowUserEntered()).isNull();
		assertThat(
				searchServices.getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(records.getAdmin())))
				.isEqualTo(0);
		Thread.sleep(1000);
		List<Record> records = searchServices.search(rmEventsSearchServices.newFindReturnedFoldersByDateRangeQuery(
				this.records.getAdmin(),
				nowDateTime.minusDays(1), nowDateTime.plusDays(1)));
		assertThat(records).hasSize(2);
		Event event = new Event(records.get(0), getSchemaTypes());
		assertThat(event.getUsername()).isEqualTo(this.records.getChuckNorris().getUsername());
		assertThat(event.getType()).isEqualTo(EventType.RETURN_FOLDER);
		assertThat(event.getCreatedOn()).isEqualTo(nowDateTime.plusDays(1));
		assertThat(presenter.getBorrowMessageState(folderC30)).isNull();
	}

	@Test
	public void givenSemiACtiveBorrowedFolderAndRemovedPermissionToModifySemiActiveBorrwedFolderAndGivenBackThenOk()
			throws Exception {

		givenRemovedPermissionToModifyBorrowedFolder(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER);
		displayFolderView.navigateTo().displayFolder("C30");
		presenter.borrowFolder(nowDateTime.toDate(), records.getChuckNorris().getId());

		displayFolderView.navigateTo().displayFolder("C30");
		assertThat(presenter.getDeleteButtonState(records.getChuckNorris(), records.getFolder_C30()).isVisible()).isFalse();
		assertThat(presenter.getEditButtonState(records.getChuckNorris(), records.getFolder_C30()).isVisible()).isFalse();
		assertThat(presenter.getAddFolderButtonState(records.getChuckNorris(), records.getFolder_C30()).isVisible()).isFalse();
		assertThat(presenter.getAddDocumentButtonState(records.getChuckNorris(), records.getFolder_C30()).isVisible()).isFalse();
		assertThat(presenter.getPrintButtonState(records.getChuckNorris(), records.getFolder_C30()).isVisible()).isFalse();

		givenNoRemovedPermissionsToModifyBorrowedFolder();
		displayFolderView.navigateTo().displayFolder("C30");

		displayFolderView.navigateTo().displayFolder("C30");
		assertThat(presenter.getDeleteButtonState(records.getChuckNorris(), records.getFolder_C30()).isVisible()).isTrue();
		assertThat(presenter.getEditButtonState(records.getChuckNorris(), records.getFolder_C30()).isVisible()).isTrue();
		assertThat(presenter.getAddFolderButtonState(records.getChuckNorris(), records.getFolder_C30()).isVisible()).isTrue();
		assertThat(presenter.getAddDocumentButtonState(records.getChuckNorris(), records.getFolder_C30()).isVisible()).isTrue();
		assertThat(presenter.getPrintButtonState(records.getChuckNorris(), records.getFolder_C30()).isVisible()).isTrue();
	}

	@Test
	public void givenInactiveBorrowedFolderAndRemovedPermissionToModifyInactiveBorrwedFolderAndGivenBackThenOk()
			throws Exception {

		presenter.forParams("C50");
		givenRemovedPermissionToModifyBorrowedFolder(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER);
		displayFolderView.navigateTo().displayFolder("C50");
		presenter.borrowFolder(nowDateTime.toDate(), records.getChuckNorris().getId());

		displayFolderView.navigateTo().displayFolder("C50");
		assertThat(presenter.getDeleteButtonState(records.getChuckNorris(), records.getFolder_C50()).isVisible()).isFalse();
		assertThat(presenter.getEditButtonState(records.getChuckNorris(), records.getFolder_C50()).isVisible()).isFalse();
		assertThat(presenter.getAddFolderButtonState(records.getChuckNorris(), records.getFolder_C50()).isVisible()).isFalse();
		assertThat(presenter.getAddDocumentButtonState(records.getChuckNorris(), records.getFolder_C50()).isVisible()).isFalse();
		assertThat(presenter.getPrintButtonState(records.getChuckNorris(), records.getFolder_C50()).isVisible()).isFalse();

		givenNoRemovedPermissionsToModifyBorrowedFolder();
		displayFolderView.navigateTo().displayFolder("C50");

		displayFolderView.navigateTo().displayFolder("C50");
		assertThat(presenter.getDeleteButtonState(records.getChuckNorris(), records.getFolder_C50()).isVisible()).isTrue();
		assertThat(presenter.getEditButtonState(records.getChuckNorris(), records.getFolder_C50()).isVisible()).isTrue();
		assertThat(presenter.getAddFolderButtonState(records.getChuckNorris(), records.getFolder_C50()).isVisible()).isTrue();
		assertThat(presenter.getAddDocumentButtonState(records.getChuckNorris(), records.getFolder_C50()).isVisible()).isTrue();
		assertThat(presenter.getPrintButtonState(records.getChuckNorris(), records.getFolder_C50()).isVisible()).isTrue();
	}

	@Test
	public void whenGetTemplatesThenReturnFolderTemplates()
			throws Exception {

		List<LabelTemplate> labelTemplates = presenter.getTemplates();

		assertThat(labelTemplates).hasSize(2);

		assertThat(labelTemplates.get(0).getSchemaType()).isEqualTo(Folder.SCHEMA_TYPE);
		assertThat(labelTemplates.get(0).getColumns()).isEqualTo(30);
		assertThat(labelTemplates.get(0).getLines()).isEqualTo(11);
		assertThat(labelTemplates.get(0).getKey()).isEqualTo("FOLDER_LEFT_AVERY_5159");
		assertThat(labelTemplates.get(0).getName()).isEqualTo("Code de plan justifié à gauche");
		assertThat(labelTemplates.get(0).getLabelsReportLayout()).isEqualTo(LabelsReportLayout.AVERY_5159);
		assertThat(labelTemplates.get(0).getFields()).hasSize(6);

		assertThat(labelTemplates.get(1).getSchemaType()).isEqualTo(Folder.SCHEMA_TYPE);
		assertThat(labelTemplates.get(1).getColumns()).isEqualTo(30);
		assertThat(labelTemplates.get(1).getLines()).isEqualTo(11);
		assertThat(labelTemplates.get(1).getKey()).isEqualTo("FOLDER_RIGHT_AVERY_5159");
		assertThat(labelTemplates.get(1).getName()).isEqualTo("Code de plan justifié à droite");
		assertThat(labelTemplates.get(1).getLabelsReportLayout()).isEqualTo(LabelsReportLayout.AVERY_5159);
		assertThat(labelTemplates.get(1).getFields()).hasSize(6);
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
