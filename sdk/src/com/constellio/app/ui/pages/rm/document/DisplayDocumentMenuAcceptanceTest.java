package com.constellio.app.ui.pages.rm.document;

import static com.constellio.app.ui.pages.rm.document.DisplayDocumentMenuAcceptanceTest.AddEditDocumentAction.DELETE;
import static com.constellio.app.ui.pages.rm.document.DisplayDocumentMenuAcceptanceTest.AddEditDocumentAction.MANAGE_AUTHORIZATIONS;
import static com.constellio.app.ui.pages.rm.document.DisplayDocumentMenuAcceptanceTest.AddEditDocumentAction.MODIFY;
import static com.constellio.app.ui.pages.rm.document.DisplayDocumentMenuAcceptanceTest.AddEditDocumentAction.SHARE;
import static com.constellio.app.ui.pages.rm.document.DisplayDocumentMenuAcceptanceTest.AddEditDocumentAction.UPLOAD;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.tools.ButtonWebElement;
import com.constellio.app.ui.tools.RecordFormWebElement;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

@UiTest
public class DisplayDocumentMenuAcceptanceTest extends ConstellioTest {

	enum AddEditDocumentAction {
		MODIFY, DELETE, MANAGE_AUTHORIZATIONS, UPLOAD, SHARE
	}

	RecordFormWebElement zeForm;
	RecordServices recordServices;
	ConstellioWebDriver driver;
	RMTestRecords records = new RMTestRecords(zeCollection);
	RMSchemasRecordsServices schemas;

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

	}

	@Test
	/** Alice is a USER
	 *  Alice can READ
	 */
	public void givenAliceThenDisplayFolderMenuIsOk() {
		logAsInZeCollection(aliceWonderland);

		navigateToADocumentInUA10();
		assertThatOnlyAvailableActionsAre(UPLOAD, SHARE);

		navigateToSemiActiveDocumentInUA10();
		assertThatOnlyAvailableActionsAre(UPLOAD, SHARE);

		navigateToDestroyedDocumentInUA30();
		assertThatOnlyAvailableActionsAre();

		navigateToDepositedFolderInUA30();
		assertThatOnlyAvailableActionsAre();
	}

	@Test
	/** Admin is a RGD
	 */
	public void givenAdminThenDisplayDocumentMenuIsOk() {
		logAsInZeCollection(admin);

		navigateToADocumentInUA10();
		assertThatOnlyAvailableActionsAre(MODIFY, DELETE, MANAGE_AUTHORIZATIONS, UPLOAD, SHARE);

		navigateToSemiActiveDocumentInUA10();
		assertThatOnlyAvailableActionsAre(MODIFY, DELETE, MANAGE_AUTHORIZATIONS, UPLOAD, SHARE);

		navigateToDestroyedDocumentInUA30();
		assertThatOnlyAvailableActionsAre(MODIFY, DELETE, MANAGE_AUTHORIZATIONS, UPLOAD, SHARE);

		navigateToDepositedFolderInUA30();
		assertThatOnlyAvailableActionsAre(MODIFY, DELETE, MANAGE_AUTHORIZATIONS, UPLOAD, SHARE);
	}

	@Test
	/** Bob is a USER
	 *  Bob can READ/WRITE in UA 10
	 *  Bob can READ/WRITE in UA 30
	 */
	public void givenBobThenDisplayDocumentMenuIsOk() {
		logAsInZeCollection(bobGratton);

		navigateToADocumentInUA10();
		assertThatOnlyAvailableActionsAre(MODIFY, UPLOAD, SHARE);

		navigateToSemiActiveDocumentInUA10();
		assertThatOnlyAvailableActionsAre(UPLOAD, SHARE);

		navigateToDestroyedDocumentInUA30();
		assertThatOnlyAvailableActionsAre();

		navigateToDepositedFolderInUA30();
		assertThatOnlyAvailableActionsAre();
	}

	@Test
	/** Bob is a USER
	 *  Bob can READ/WRITE in UA 10
	 *  Bob can READ/WRITE in UA 30
	 */
	public void givenBobWithShareInactivePermissionThenDisplayDocumentMenuIsOk() {
		givenUserRoleHas(RMPermissionsTo.SHARE_A_INACTIVE_DOCUMENT);
		logAsInZeCollection(bobGratton);

		navigateToADocumentInUA10();
		assertThatOnlyAvailableActionsAre(MODIFY, UPLOAD, SHARE);

		navigateToSemiActiveDocumentInUA10();
		assertThatOnlyAvailableActionsAre(UPLOAD, SHARE);

		navigateToDestroyedDocumentInUA30();
		assertThatOnlyAvailableActionsAre(SHARE);

		navigateToDepositedFolderInUA30();
		assertThatOnlyAvailableActionsAre(SHARE);
	}

	@Test
	/** Bob is a USER
	 *  Bob can READ/WRITE in UA 10
	 *  Bob can READ/WRITE in UA 30
	 */
	public void givenBobWithoutShareSemiActivePermissionThenDisplayDocumentMenuIsOk() {
		givenUserRoleHasNo(RMPermissionsTo.SHARE_A_SEMIACTIVE_DOCUMENT);
		logAsInZeCollection(bobGratton);

		navigateToADocumentInUA10();
		assertThatOnlyAvailableActionsAre(MODIFY, UPLOAD, SHARE);

		navigateToSemiActiveDocumentInUA10();
		assertThatOnlyAvailableActionsAre(UPLOAD);

		navigateToDestroyedDocumentInUA30();
		assertThatOnlyAvailableActionsAre();

		navigateToDepositedFolderInUA30();
		assertThatOnlyAvailableActionsAre();
	}

	private void givenUserRoleHas(String permission) {
		Role role = getModelLayerFactory().getRolesManager().getRole(zeCollection, RMRoles.USER);
		List<String> permissions = new ArrayList<>(role.getOperationPermissions());
		permissions.add(permission);
		getModelLayerFactory().getRolesManager().updateRole(role.withPermissions(permissions));
	}

	private void givenUserRoleHasNo(String permission) {
		Role role = getModelLayerFactory().getRolesManager().getRole(zeCollection, RMRoles.USER);
		List<String> permissions = new ArrayList<>(role.getOperationPermissions());
		permissions.remove(permission);
		getModelLayerFactory().getRolesManager().updateRole(role.withPermissions(permissions));
	}

	@Test
	/** Charles is a USER
	 * 	Charles can READ/WRITE in UA 10
	 */
	public void givenCharlesThenDisplayDocumentMenuIsOk() {
		logAsInZeCollection(charlesFrancoisXavier);

		navigateToADocumentInUA10();
		assertThatOnlyAvailableActionsAre(MODIFY, UPLOAD, SHARE);

		navigateToSemiActiveDocumentInUA10();
		assertThatOnlyAvailableActionsAre(UPLOAD, SHARE);

		navigateToDestroyedDocumentInUA30();
		assertThatOnlyAvailableActionsAre();

		navigateToDepositedFolderInUA30();
		assertThatOnlyAvailableActionsAre();
	}

	@Test
	/** Dakota is a MANAGER
	 *  Dakota can READ/WRITE/DELETE  in UA 10, 11, 12
	 */
	public void givenDakotaThenDisplayDocumentMenuIsOk() {
		logAsInZeCollection(dakota);

		navigateToADocumentInUA10();
		assertThatOnlyAvailableActionsAre(MODIFY, DELETE, MANAGE_AUTHORIZATIONS, UPLOAD, SHARE);

		navigateToSemiActiveDocumentInUA10();
		assertThatOnlyAvailableActionsAre(MANAGE_AUTHORIZATIONS, UPLOAD, DELETE, SHARE);

		navigateToDestroyedDocumentInUA10();
		assertThatOnlyAvailableActionsAre(MANAGE_AUTHORIZATIONS);
	}

	@Test
	/** Edouard is a USER, but manager in UA 11, 12
	 *  Edouard can READ/WRITE/DELETE in UA 11,12
	 *  Edouard can READ/WRITE in UA 30
	 */
	public void givenEdouardThenDisplayDocumentMenuIsOk() {
		logAsInZeCollection(edouard);

		navigateToADocumentInUA11();
		assertThatOnlyAvailableActionsAre(MODIFY, DELETE, MANAGE_AUTHORIZATIONS, UPLOAD, SHARE);

		navigateToADocumentInUA12();
		assertThatOnlyAvailableActionsAre(MODIFY, DELETE, MANAGE_AUTHORIZATIONS, UPLOAD, SHARE);

		navigateToADocumentInUA30();
		assertThatOnlyAvailableActionsAre(MODIFY, UPLOAD, SHARE);

		navigateToSemiActiveDocumentInUA11();
		assertThatOnlyAvailableActionsAre(DELETE, MANAGE_AUTHORIZATIONS, UPLOAD, SHARE);

		navigateToSemiActiveDocumentInUA12();
		assertThatOnlyAvailableActionsAre(DELETE, MANAGE_AUTHORIZATIONS, UPLOAD, SHARE);

		navigateToSemiActiveDocumentInUA30();
		assertThatOnlyAvailableActionsAre(UPLOAD, SHARE);

		navigateToDestroyedDocumentInUA11();
		assertThatOnlyAvailableActionsAre(MANAGE_AUTHORIZATIONS);

		navigateToDestroyedDocumentInUA12();
		assertThatOnlyAvailableActionsAre(MANAGE_AUTHORIZATIONS);

		navigateToDestroyedDocumentInUA30();
		assertThatOnlyAvailableActionsAre();

	}

	@Test
	/** Gandalf is a MANAGER
	 *  Gandalf can READ/WRITE/DELETE in UA 10
	 *  Gandalf can READ/WRITE/DELETE in UA 30
	 */
	public void givenGandalfThenDisplayDocumentMenuIsOk() {
		logAsInZeCollection(gandalf);

		navigateToADocumentInUA10();
		assertThatOnlyAvailableActionsAre(MODIFY, DELETE, MANAGE_AUTHORIZATIONS, UPLOAD, SHARE);

		navigateToADocumentInUA30();
		assertThatOnlyAvailableActionsAre(MODIFY, DELETE, MANAGE_AUTHORIZATIONS, UPLOAD, SHARE);

		navigateToSemiActiveDocumentInUA10();
		assertThatOnlyAvailableActionsAre(DELETE, MANAGE_AUTHORIZATIONS, UPLOAD, SHARE);

		navigateToSemiActiveDocumentInUA30();
		assertThatOnlyAvailableActionsAre(DELETE, MANAGE_AUTHORIZATIONS, UPLOAD, SHARE);

		navigateToDestroyedDocumentInUA10();
		assertThatOnlyAvailableActionsAre(MANAGE_AUTHORIZATIONS);

		navigateToDestroyedDocumentInUA30();
		assertThatOnlyAvailableActionsAre(MANAGE_AUTHORIZATIONS);

		navigateToDepositedFolderInUA30();
		assertThatOnlyAvailableActionsAre(MANAGE_AUTHORIZATIONS);
	}

	@Test
	/** Chuck is a RGD
	 */
	public void givenChuckThenDisplayDocumentMenuIsOk() {
		logAsInZeCollection(chuckNorris);

		navigateToADocumentInUA10();
		assertThatOnlyAvailableActionsAre(MODIFY, DELETE, MANAGE_AUTHORIZATIONS, UPLOAD, SHARE);

		navigateToADocumentInUA11();
		assertThatOnlyAvailableActionsAre(MODIFY, DELETE, MANAGE_AUTHORIZATIONS, UPLOAD, SHARE);

		navigateToADocumentInUA12();
		assertThatOnlyAvailableActionsAre(MODIFY, DELETE, MANAGE_AUTHORIZATIONS, UPLOAD, SHARE);

		navigateToADocumentInUA30();
		assertThatOnlyAvailableActionsAre(MODIFY, DELETE, MANAGE_AUTHORIZATIONS, UPLOAD, SHARE);

		navigateToSemiActiveDocumentInUA10();
		assertThatOnlyAvailableActionsAre(MODIFY, DELETE, MANAGE_AUTHORIZATIONS, UPLOAD, SHARE);

		navigateToSemiActiveDocumentInUA11();
		assertThatOnlyAvailableActionsAre(MODIFY, DELETE, MANAGE_AUTHORIZATIONS, UPLOAD, SHARE);

		navigateToSemiActiveDocumentInUA12();
		assertThatOnlyAvailableActionsAre(MODIFY, DELETE, MANAGE_AUTHORIZATIONS, UPLOAD, SHARE);

		navigateToSemiActiveDocumentInUA30();
		assertThatOnlyAvailableActionsAre(MODIFY, DELETE, MANAGE_AUTHORIZATIONS, UPLOAD, SHARE);

		navigateToDestroyedDocumentInUA10();
		assertThatOnlyAvailableActionsAre(MODIFY, DELETE, MANAGE_AUTHORIZATIONS, UPLOAD, SHARE);

		navigateToDestroyedDocumentInUA11();
		assertThatOnlyAvailableActionsAre(MODIFY, DELETE, MANAGE_AUTHORIZATIONS, UPLOAD, SHARE);

		navigateToDestroyedDocumentInUA12();
		assertThatOnlyAvailableActionsAre(MODIFY, DELETE, MANAGE_AUTHORIZATIONS, UPLOAD, SHARE);

		navigateToDestroyedDocumentInUA30();
		assertThatOnlyAvailableActionsAre(MODIFY, DELETE, MANAGE_AUTHORIZATIONS, UPLOAD, SHARE);

		navigateToDepositedFolderInUA30();
		assertThatOnlyAvailableActionsAre(MODIFY, DELETE, MANAGE_AUTHORIZATIONS, UPLOAD, SHARE);
	}

	private void logAsInZeCollection(String user) {
		driver = newWebDriver(loggedAsUserInCollection(user, zeCollection));
	}

	private void navigateToSemiActiveDocumentInUA10() {
		String id = recordIdWithTitleInCollection("Orange - Petit guide", zeCollection);
		driver.navigateTo().url(RMNavigationConfiguration.DISPLAY_DOCUMENT + "/" + id);
	}

	private void navigateToSemiActiveDocumentInUA11() {
		String id = recordIdWithTitleInCollection("Poire - Typologie", zeCollection);
		driver.navigateTo().url(RMNavigationConfiguration.DISPLAY_DOCUMENT + "/" + id);
	}

	private void navigateToSemiActiveDocumentInUA12() {
		String id = recordIdWithTitleInCollection("Pomme - Petit guide", zeCollection);
		driver.navigateTo().url(RMNavigationConfiguration.DISPLAY_DOCUMENT + "/" + id);
	}

	private void navigateToSemiActiveDocumentInUA30() {
		String id = recordIdWithTitleInCollection("Laitue - Petit guide", zeCollection);
		driver.navigateTo().url(RMNavigationConfiguration.DISPLAY_DOCUMENT + "/" + id);
	}

	private void navigateToDestroyedDocumentInUA30() {
		String id = recordIdWithTitleInCollection("Epinard - Livre de recettes", zeCollection);
		driver.navigateTo().url(RMNavigationConfiguration.DISPLAY_DOCUMENT + "/" + id);
	}

	private void navigateToDestroyedDocumentInUA10() {
		String id = recordIdWithTitleInCollection("Souris - Livre de recettes", zeCollection);
		driver.navigateTo().url(RMNavigationConfiguration.DISPLAY_DOCUMENT + "/" + id);
	}

	private void navigateToDestroyedDocumentInUA11() {
		String id = recordIdWithTitleInCollection("Cerise - Typologie", zeCollection);
		driver.navigateTo().url(RMNavigationConfiguration.DISPLAY_DOCUMENT + "/" + id);
	}

	private void navigateToDestroyedDocumentInUA12() {
		String id = recordIdWithTitleInCollection("Avocat - Petit guide", zeCollection);
		driver.navigateTo().url(RMNavigationConfiguration.DISPLAY_DOCUMENT + "/" + id);
	}

	private void navigateToDepositedFolderInUA30() {
		String id = recordIdWithTitleInCollection("Pois - Livre de recettes", zeCollection);
		driver.navigateTo().url(RMNavigationConfiguration.DISPLAY_DOCUMENT + "/" + id);
	}

	private void navigateToADocumentInUA10() {
		String id = recordIdWithTitleInCollection("Abeille - Livre de recettes", zeCollection);
		driver.navigateTo().url(RMNavigationConfiguration.DISPLAY_DOCUMENT + "/" + id);
	}

	private void navigateToADocumentInUA11() {
		String id = recordIdWithTitleInCollection("Abricot - Livre de recettes", zeCollection);
		driver.navigateTo().url(RMNavigationConfiguration.DISPLAY_DOCUMENT + "/" + id);
	}

	private void navigateToADocumentInUA12() {
		String id = recordIdWithTitleInCollection("Banane - Livre de recettes", zeCollection);
		driver.navigateTo().url(RMNavigationConfiguration.DISPLAY_DOCUMENT + "/" + id);
	}

	private void navigateToADocumentInUA30() {
		String id = recordIdWithTitleInCollection("Chou - Livre de recettes", zeCollection);
		driver.navigateTo().url(RMNavigationConfiguration.DISPLAY_DOCUMENT + "/" + id);
	}

	private org.assertj.core.api.ObjectAssert<ComponentState> assertThatModifyButtonState() {
		return assertThatButtonState("Éditer la fiche du document");
	}

	private org.assertj.core.api.ObjectAssert<ComponentState> assertThatDeleteButtonState() {
		return assertThatButtonState("Supprimer ce document");
	}

	private org.assertj.core.api.ObjectAssert<ComponentState> assertThatAuthorizationsButtonState() {
		return assertThatButtonState("Autorisations");
	}

	private org.assertj.core.api.ObjectAssert<ComponentState> assertThatShareButtonState() {
		return assertThatButtonState("Partager ce document");
	}

	private org.assertj.core.api.ObjectAssert<ComponentState> assertThatUploadButtonState() {
		return assertThatButtonState("Téléverser");
	}

	private void assertThatOnlyAvailableActionsAre(AddEditDocumentAction... expectedActions) {
		List<AddEditDocumentAction> expectedActionsList = asList(expectedActions);

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

		if (expectedActionsList.contains(MANAGE_AUTHORIZATIONS)) {
			assertThatAuthorizationsButtonState().isEqualTo(ComponentState.ENABLED);
		} else {
			assertThatAuthorizationsButtonState().isEqualTo(ComponentState.INVISIBLE);
		}

		if (expectedActionsList.contains(UPLOAD)) {
			assertThatUploadButtonState().isEqualTo(ComponentState.ENABLED);
		} else {
			assertThatUploadButtonState().isEqualTo(ComponentState.INVISIBLE);
		}

		if (expectedActionsList.contains(SHARE)) {
			assertThatShareButtonState().isEqualTo(ComponentState.ENABLED);
		} else {
			assertThatShareButtonState().isEqualTo(ComponentState.INVISIBLE);
		}
	}

	private org.assertj.core.api.ObjectAssert<ComponentState> assertThatButtonState(String label) {
		return assertThat(getButtonState(label)).describedAs(label);
	}

	private ComponentState getButtonState(String buttonName) {
		List<ConstellioWebElement> listButtonActionMenu = driver.findAdaptElements(By.className("action-menu-button"));
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

}
