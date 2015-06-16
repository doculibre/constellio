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
package com.constellio.app.ui.pages.rm.document;

import static com.constellio.app.ui.pages.rm.document.DisplayLastDocumentViewAcceptanceTest.DocumentContextMenuAction.AUTHORIZATIONS;
import static com.constellio.app.ui.pages.rm.document.DisplayLastDocumentViewAcceptanceTest.DocumentContextMenuAction.DELETE;
import static com.constellio.app.ui.pages.rm.document.DisplayLastDocumentViewAcceptanceTest.DocumentContextMenuAction.MODIFY;
import static com.constellio.app.ui.pages.rm.document.DisplayLastDocumentViewAcceptanceTest.DocumentContextMenuAction.UPLOAD;
import static com.constellio.app.ui.pages.rm.document.DisplayLastDocumentViewAcceptanceTest.DocumentContextMenuAction.VIEW;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.entities.ComponentState;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.tools.ButtonWebElement;
import com.constellio.app.ui.tools.RecordFormWebElement;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.borrowingServices.BorrowingServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

@UiTest
public class DisplayLastDocumentViewAcceptanceTest extends ConstellioTest {

	enum DocumentContextMenuAction {
		VIEW, MODIFY, DELETE, AUTHORIZATIONS, UPLOAD
	}

	RecordFormWebElement zeForm;
	RecordServices recordServices;
	ConstellioWebDriver driver;
	RMTestRecords records;
	RMSchemasRecordsServices schemas;
	RolesManager rolesManager;

	@Before
	public void setUp()
			throws Exception {

		givenCollectionWithTitle(zeCollection, "Collection de test").withConstellioRMModule().withAllTestUsers();

		recordServices = getModelLayerFactory().newRecordServices();

		rolesManager = getModelLayerFactory().getRolesManager();

		records = new RMTestRecords(zeCollection).setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus()
				.withEvents();
	}

	@Test
	/** Admin is a RGD 
	 */
	public void givenAdminThenDisplayDocumentContextMenuIsOk() {
		logAsInZeCollectionAndSetupLastViewedDocuments(admin);

		getMaisDocument().rightClick();
		assertThatEnabledActionsAre(VIEW, MODIFY, DELETE, AUTHORIZATIONS, UPLOAD);

		getMaisDocument().sendKeys(Keys.ESCAPE);
		getPoivronDocument().rightClick();
		assertThatEnabledActionsAre(VIEW, MODIFY, DELETE, AUTHORIZATIONS, UPLOAD);

		getPoivronDocument().sendKeys(Keys.ESCAPE);
		getPoisDocument().rightClick();
		assertThatEnabledActionsAre(VIEW, MODIFY, DELETE, AUTHORIZATIONS, UPLOAD);
	}

	@Test
	/** Alice is a USER
	 *  Alice can READ  
	 */
	public void givenAliceThenDisplayDocumentContextMenuIsOk() {
		logAsInZeCollectionAndSetupLastViewedDocuments(aliceWonderland);

		getMaisDocument().rightClick();
		assertThatEnabledActionsAre(UPLOAD, VIEW);

		getMaisDocument().sendKeys(Keys.ESCAPE);
		getPoivronDocument().rightClick();
		assertThatEnabledActionsAre(VIEW);

		getPoivronDocument().sendKeys(Keys.ESCAPE);
		getPoisDocument().rightClick();
		assertThatEnabledActionsAre(VIEW);
	}

	@Test
	/** Bob is a USER 
	 *  Bob can READ/WRITE in UA 30
	 */
	public void givenBobThenDisplayDocumentMenuIsOk() {
		logAsInZeCollectionAndSetupLastViewedDocuments(bobGratton);

		getPoivronDocument().rightClick();
		assertThatEnabledActionsAre(VIEW);
	}

	@Test
	/** Edouard is a USER
	 *  Edouard can READ/WRITE in UA 30
	 */
	public void givenEdouardThenDisplayDocumentMenuIsOk() {
		logAsInZeCollectionAndSetupLastViewedDocuments(edouard);

		getPoivronDocument().rightClick();
		assertThatEnabledActionsAre(VIEW);

		getPoivronDocument().sendKeys(Keys.ESCAPE);
		getPoisDocument().rightClick();
		assertThatEnabledActionsAre(VIEW);
	}

	@Test
	/** Gandalf is a MANAGER
	 *  Gandalf can READ/WRITE/DELETE in UA 30
	 */
	public void givenGandalfThenDisplayDocumentMenuIsOk() {
		logAsInZeCollectionAndSetupLastViewedDocuments(gandalf);

		getMaisDocument().rightClick();
		assertThatEnabledActionsAre(VIEW, AUTHORIZATIONS, UPLOAD, DELETE);

		getMaisDocument().sendKeys(Keys.ESCAPE);
		getPoivronDocument().rightClick();
		assertThatEnabledActionsAre(VIEW, AUTHORIZATIONS);

		getPoivronDocument().sendKeys(Keys.ESCAPE);
		getPoisDocument().rightClick();
		assertThatEnabledActionsAre(VIEW, AUTHORIZATIONS);
	}

	@Test
	/** Chuck is a RGD
	 */
	public void givenChuckThenDisplayDocumentMenuIsOk() {
		logAsInZeCollectionAndSetupLastViewedDocuments(chuckNorris);

		getMaisDocument().rightClick();
		assertThatEnabledActionsAre(VIEW, MODIFY, DELETE, AUTHORIZATIONS, UPLOAD);

		getMaisDocument().sendKeys(Keys.ESCAPE);
		getPoivronDocument().rightClick();
		assertThatEnabledActionsAre(VIEW, MODIFY, DELETE, AUTHORIZATIONS, UPLOAD);

		getPoivronDocument().sendKeys(Keys.ESCAPE);
		getPoisDocument().rightClick();
		assertThatEnabledActionsAre(VIEW, MODIFY, DELETE, AUTHORIZATIONS, UPLOAD);
	}

	@Test
	/** Chuck is a RGD
	 */
	public void givenSemiActiveBorrowedFolderAndThenDisplayDocumentContextMenuIsOk()
			throws Exception {
		logAsInZeCollectionAndSetupLastViewedDocuments(chuckNorris);

		getMaisDocument().rightClick();
		assertThatEnabledActionsAre(VIEW, MODIFY, DELETE, AUTHORIZATIONS, UPLOAD);
		getMaisDocument().sendKeys(Keys.ESCAPE);

		givenRemovedPermissionToModifyBorrowedFolder();
		givenBorrowedFolderC32ByChuck("C32");

		assertThat(records.getChuckNorris().has(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER).on(records.getFolder_C32()))
				.isFalse();

		navigateToLastViewDocuments();
		getMaisDocument().rightClick();
		assertThatEnabledActionsAre(VIEW, AUTHORIZATIONS);
	}

	@Test
	/** Chuck is a RGD
	 */
	public void givenInactiveBorrowedFolderAndThenDisplayDocumentContextMenuIsOk()
			throws Exception {
		logAsInZeCollectionAndSetupLastViewedDocuments(chuckNorris);

		getPoisDocument().rightClick();
		assertThatEnabledActionsAre(VIEW, MODIFY, DELETE, AUTHORIZATIONS, UPLOAD);
		getPoisDocument().sendKeys(Keys.ESCAPE);

		givenRemovedPermissionToModifyBorrowedFolder();
		givenBorrowedFolderC32ByChuck("C50");

		assertThat(records.getChuckNorris().has(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER).on(records.getFolder_C50()))
				.isFalse();

		navigateToLastViewDocuments();
		getPoisDocument().rightClick();
		assertThatEnabledActionsAre(VIEW, AUTHORIZATIONS);
	}

	//----------------------------------------------------------------------------------------

	private void givenBorrowedFolderC32ByChuck(String folderId)
			throws RecordServicesException {
		BorrowingServices borrowingServices = new BorrowingServices(zeCollection, getModelLayerFactory());
		Date previewReturnDate = TimeProvider.getLocalDateTime().plusDays(15).toDate();
		borrowingServices.borrowFolder(folderId, previewReturnDate, records.getChuckNorris(), records.getChuckNorris());
	}

	private void givenRemovedPermissionToModifyBorrowedFolder() {

		for (Role role : rolesManager.getAllRoles(zeCollection)) {
			List<String> roles = role.getOperationPermissions();
			List<String> newRoles = new ArrayList<>(roles);
			newRoles.remove(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER);
			newRoles.remove(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER);
			role = role.withPermissions(newRoles);
			rolesManager.updateRole(role);
			Role updatedRole = rolesManager.getRole(zeCollection, role.getCode());
			assertThat(updatedRole.getOperationPermissions()).doesNotContain(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER);
			assertThat(updatedRole.getOperationPermissions()).doesNotContain(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER);
		}
	}

	private void assertThatEnabledActionsAre(DocumentContextMenuAction... actions) {
		List<DocumentContextMenuAction> expectedActionsList = asList(actions);

		if (expectedActionsList.contains(VIEW)) {
			assertThatCheckButtonState().isEqualTo(ComponentState.ENABLED);
		} else {
			assertThatCheckButtonState().isEqualTo(ComponentState.INVISIBLE);
		}

		if (expectedActionsList.contains(MODIFY)) {
			assertThatModifyButtonState().isEqualTo(ComponentState.ENABLED);
		} else {
			assertThatModifyButtonState().isEqualTo(ComponentState.INVISIBLE);
		}

		if (expectedActionsList.contains(DELETE)) {
			assertThatDeleteButtonState().isEqualTo(ComponentState.ENABLED);
		} else {
			assertThatDeleteButtonState().isEqualTo(ComponentState.INVISIBLE);
		}

		if (expectedActionsList.contains(AUTHORIZATIONS)) {
			assertThatAuthorizationsButtonState().isEqualTo(ComponentState.ENABLED);
		} else {
			assertThatAuthorizationsButtonState().isEqualTo(ComponentState.INVISIBLE);
		}

		if (expectedActionsList.contains(UPLOAD)) {
			assertThatUploadButtonState().isEqualTo(ComponentState.ENABLED);
		} else {
			assertThatUploadButtonState().isEqualTo(ComponentState.INVISIBLE);
		}
	}

	private org.assertj.core.api.ObjectAssert<ComponentState> assertThatCheckButtonState() {
		return assertThatButtonState("Consulter la fiche de ce document");
	}

	private org.assertj.core.api.ObjectAssert<ComponentState> assertThatModifyButtonState() {
		return assertThatButtonState("Éditer la fiche de ce document");
	}

	private org.assertj.core.api.ObjectAssert<ComponentState> assertThatDeleteButtonState() {
		return assertThatButtonState("Supprimer ce document");
	}

	private org.assertj.core.api.ObjectAssert<ComponentState> assertThatAuthorizationsButtonState() {
		return assertThatButtonState("Autorisations");
	}

	private org.assertj.core.api.ObjectAssert<ComponentState> assertThatUploadButtonState() {
		return assertThatButtonState("Téléverser");
	}

	private org.assertj.core.api.ObjectAssert<ComponentState> assertThatButtonState(String label) {
		return assertThat(getButtonState(label)).describedAs(label);
	}

	//-------------------------------------------------------------------------------------------------------------

	//Semi-Actif Document
	private ConstellioWebElement getMaisDocument() {
		List<ConstellioWebElement> listRows = getListRows();
		for (ConstellioWebElement maisDocument : listRows) {
			if (maisDocument.getText().contains("Maïs")) {
				return maisDocument;
			}
		}
		return null;
	}

	//Versé Document
	private ConstellioWebElement getPoisDocument() {
		List<ConstellioWebElement> listRows = getListRows();
		for (ConstellioWebElement poisDocument : listRows) {
			if (poisDocument.getText().contains("Pois")) {
				return poisDocument;
			}
		}
		return null;
	}

	//Destroyed Document
	private ConstellioWebElement getPoivronDocument() {
		List<ConstellioWebElement> listRows = getListRows();
		for (ConstellioWebElement poivronDocument : listRows) {
			if (poivronDocument.getText().contains("Poivron")) {
				return poivronDocument;
			}
		}
		return null;
	}

	private void navigateToLastViewDocuments() {
		driver.navigateTo().url(NavigatorConfigurationService.HOME);
		List<ConstellioWebElement> listTabMenu = driver.findAdaptElements(By.className("v-caption"));
		listTabMenu.get(2).clickAndWaitForElementRefresh(driver.findElement(By.className("record-table")));
	}

	private List<ConstellioWebElement> getListRows() {
		driver.waitUntilElementExist(By.className("record-table"));
		ConstellioWebElement recordTableElement = driver.findElement(By.className("record-table"));
		ConstellioWebElement tableElement = recordTableElement.findElement(By.className("v-table-body"));
		return tableElement.findAdaptElements(By.tagName("tr"));
	}

	private ComponentState getButtonState(String buttonName) {
		driver.waitUntilElementExist(By.className("v-context-menu"));
		ConstellioWebElement contextMenu = driver.findElement(By.className("v-context-menu"));
		List<ConstellioWebElement> listButtonActionMenu = contextMenu.findAdaptElements(By.className("v-context-submenu"));
		for (ConstellioWebElement buttonElement : listButtonActionMenu) {
			if (buttonElement.getText().contains(buttonName)) {
				ButtonWebElement currentButton = new ButtonWebElement(buttonElement);
				if (currentButton.isEnabled()) {
					return ComponentState.ENABLED;
				} else {
					return ComponentState.DISABLED;
				}
			}
		}
		return ComponentState.INVISIBLE;
	}

	private void logAsInZeCollectionAndSetupLastViewedDocuments(String user) {
		driver = newWebDriver(loggedAsUserInCollection(user, zeCollection));

		addPoivronToLastViewedDocuments();
		addPoisToLastViewedDocuments();
		addMaisToLastViewedDocuments();
	}

	private void addPoivronToLastViewedDocuments() {
		String id = recordIdWithTitleInCollection("Poivron - Livre de recettes", zeCollection);
		driver.navigateTo().url(NavigatorConfigurationService.DISPLAY_DOCUMENT + "/" + id);
		getModelLayerFactory().newRecordServices().flush();

		navigateToLastViewDocuments();
	}

	private void addPoisToLastViewedDocuments() {
		String id = recordIdWithTitleInCollection("Pois - Livre de recettes", zeCollection);
		driver.navigateTo().url(NavigatorConfigurationService.DISPLAY_DOCUMENT + "/" + id);
		getModelLayerFactory().newRecordServices().flush();
		navigateToLastViewDocuments();
	}

	private void addMaisToLastViewedDocuments() {
		String id = recordIdWithTitleInCollection("Maïs - Livre de recettes", zeCollection);
		driver.navigateTo().url(NavigatorConfigurationService.DISPLAY_DOCUMENT + "/" + id);
		getModelLayerFactory().newRecordServices().flush();
		navigateToLastViewDocuments();
	}

}
