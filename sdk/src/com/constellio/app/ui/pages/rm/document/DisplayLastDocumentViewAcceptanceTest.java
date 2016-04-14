package com.constellio.app.ui.pages.rm.document;

import static com.constellio.app.ui.pages.rm.document.DisplayLastDocumentViewAcceptanceTest.DocumentContextMenuAction.AUTHORIZATIONS;
import static com.constellio.app.ui.pages.rm.document.DisplayLastDocumentViewAcceptanceTest.DocumentContextMenuAction.DELETE;
import static com.constellio.app.ui.pages.rm.document.DisplayLastDocumentViewAcceptanceTest.DocumentContextMenuAction.MODIFY;
import static com.constellio.app.ui.pages.rm.document.DisplayLastDocumentViewAcceptanceTest.DocumentContextMenuAction.UPLOAD;
import static com.constellio.app.ui.pages.rm.document.DisplayLastDocumentViewAcceptanceTest.DocumentContextMenuAction.VIEW;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingType;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.tools.ButtonWebElement;
import com.constellio.app.ui.tools.RecordFormWebElement;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.security.Role;
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
	RMTestRecords records = new RMTestRecords(zeCollection);
	RMSchemasRecordsServices schemas;
	RolesManager rolesManager;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(
						records).withFoldersAndContainersOfEveryStatus()
						.withEvents()
		);
		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		recordServices = getModelLayerFactory().newRecordServices();

		rolesManager = getModelLayerFactory().getRolesManager();

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
		LocalDate nowDate = TimeProvider.getLocalDate();
		LocalDate previewReturnDate = nowDate.plusDays(15);
		borrowingServices
				.borrowFolder(folderId, nowDate, previewReturnDate, records.getChuckNorris(), records.getChuckNorris(),
						BorrowingType.BORROW);
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
		//		List<DocumentContextMenuAction> expectedActionsList = asList(actions);
		//
		//		if (expectedActionsList.contains(VIEW)) {
		//			assertThatCheckButtonState().isEqualTo(ComponentState.ENABLED);
		//		} else {
		//			assertThatCheckButtonState().isEqualTo(ComponentState.INVISIBLE);
		//		}
		//
		//		if (expectedActionsList.contains(MODIFY)) {
		//			assertThatModifyButtonState().isEqualTo(ComponentState.ENABLED);
		//		} else {
		//			assertThatModifyButtonState().isEqualTo(ComponentState.INVISIBLE);
		//		}
		//
		//		if (expectedActionsList.contains(DELETE)) {
		//			assertThatDeleteButtonState().isEqualTo(ComponentState.ENABLED);
		//		} else {
		//			assertThatDeleteButtonState().isEqualTo(ComponentState.INVISIBLE);
		//		}
		//
		//		if (expectedActionsList.contains(AUTHORIZATIONS)) {
		//			assertThatAuthorizationsButtonState().isEqualTo(ComponentState.ENABLED);
		//		} else {
		//			assertThatAuthorizationsButtonState().isEqualTo(ComponentState.INVISIBLE);
		//		}
		//
		//		if (expectedActionsList.contains(UPLOAD)) {
		//			assertThatUploadButtonState().isEqualTo(ComponentState.ENABLED);
		//		} else {
		//			assertThatUploadButtonState().isEqualTo(ComponentState.INVISIBLE);
		//		}
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
		ConstellioWebElement element = driver.find("v-tabsheet-tabs-main-component");
		List<ConstellioWebElement> listTabMenu = element.findAdaptElements(By.className("v-caption"));

		listTabMenu.get(1).click();

		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			throw new RuntimeException(e1);
		}
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
		driver.navigateTo().url(RMNavigationConfiguration.DISPLAY_DOCUMENT + "/" + id);
		getModelLayerFactory().newRecordServices().flush();

		navigateToLastViewDocuments();
	}

	private void addPoisToLastViewedDocuments() {
		String id = recordIdWithTitleInCollection("Pois - Livre de recettes", zeCollection);
		driver.navigateTo().url(RMNavigationConfiguration.DISPLAY_DOCUMENT + "/" + id);
		getModelLayerFactory().newRecordServices().flush();
		navigateToLastViewDocuments();
	}

	private void addMaisToLastViewedDocuments() {
		String id = recordIdWithTitleInCollection("Maïs - Livre de recettes", zeCollection);
		driver.navigateTo().url(RMNavigationConfiguration.DISPLAY_DOCUMENT + "/" + id);
		getModelLayerFactory().newRecordServices().flush();
		navigateToLastViewDocuments();
	}

}
