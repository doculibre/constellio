package com.constellio.app.ui.pages.rm.folder;

import static com.constellio.app.ui.pages.rm.folder.DisplayFolderMenuAcceptanceTest.AddEditFolderAction.CREATE_DOCUMENT;
import static com.constellio.app.ui.pages.rm.folder.DisplayFolderMenuAcceptanceTest.AddEditFolderAction.CREATE_SUB_FOLDER;
import static com.constellio.app.ui.pages.rm.folder.DisplayFolderMenuAcceptanceTest.AddEditFolderAction.DELETE;
import static com.constellio.app.ui.pages.rm.folder.DisplayFolderMenuAcceptanceTest.AddEditFolderAction.DUPLICATE;
import static com.constellio.app.ui.pages.rm.folder.DisplayFolderMenuAcceptanceTest.AddEditFolderAction.MANAGE_AUTHORIZATIONS;
import static com.constellio.app.ui.pages.rm.folder.DisplayFolderMenuAcceptanceTest.AddEditFolderAction.MODIFY;
import static com.constellio.app.ui.pages.rm.folder.DisplayFolderMenuAcceptanceTest.AddEditFolderAction.PRINT_LABELS;
import static com.constellio.app.ui.pages.rm.folder.DisplayFolderMenuAcceptanceTest.AddEditFolderAction.SHARE;
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
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.tools.ButtonWebElement;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

@UiTest
public class DisplayFolderMenuAcceptanceTest extends ConstellioTest {

	enum AddEditFolderAction {
		MODIFY, DELETE, MANAGE_AUTHORIZATIONS, DUPLICATE, PRINT_LABELS, CREATE_SUB_FOLDER, CREATE_DOCUMENT, SHARE
	}

	RecordServices recordServices;
	ConstellioWebDriver driver;
	RMTestRecords records = new RMTestRecords(zeCollection);

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(
						records).withFoldersAndContainersOfEveryStatus().withEvents()
		);
		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		recordServices = getModelLayerFactory().newRecordServices();

	}

	@Test
	/** Alice is a USER
	 *  Alice can READ
	 */
	public void givenAliceThenDisplayFolderMenuIsOk() {
		logAs(aliceWonderland);

		navigateToDestroyedFolderInUA10();
		assertThatAllAreDisabled();

		navigateToDepositedFolderInUA30();
		assertThatAllAreDisabled();

		navigateToAFolderInUA10();
		assertThatOnlyAvailableActionsAre(PRINT_LABELS, SHARE);

		navigateToSemiActiveFolderInUA10();
		assertThatOnlyAvailableActionsAre(SHARE);
	}

	@Test
	/** Admin is a RGD
	 */
	public void givenAdminThenDisplayFolderMenuIsOk() {
		logAs(admin);

		navigateToDestroyedFolderInUA10();
		assertThatAllAreEnabled();

		navigateToDepositedFolderInUA30();
		assertThatAllAreEnabled();

		navigateToAFolderInUA10();
		assertThatAllAreEnabled();

		navigateToSemiActiveFolderInUA10();
		assertThatAllAreEnabled();
	}

	@Test
	/** Bob is a USER
	 *  Bob can READ/WRITE in UA 10
	 *  Bob can READ/WRITE in UA 30
	 */
	public void givenBobThenDisplayFolderMenuIsOk() {
		givenUserRoleHas(RMPermissionsTo.SHARE_A_INACTIVE_FOLDER);
		logAs(bobGratton);

		navigateToDestroyedFolderInUA10();
		assertThatOnlyAvailableActionsAre(SHARE);

		navigateToDepositedFolderInUA30();
		assertThatOnlyAvailableActionsAre(SHARE);

		navigateToAFolderInUA10();
		assertThatOnlyAvailableActionsAre(MODIFY, DUPLICATE, PRINT_LABELS, CREATE_SUB_FOLDER, CREATE_DOCUMENT, SHARE);

		navigateToSemiActiveFolderInUA10();
		assertThatOnlyAvailableActionsAre(SHARE);
	}

	@Test
	/** Bob is a USER
	 *  Bob can READ/WRITE in UA 10
	 *  Bob can READ/WRITE in UA 30
	 */
	public void givenUserWithoutShareSemiActiveBobThenDisplayFolderMenuIsOk() {
		givenUserRoleHasNo(RMPermissionsTo.SHARE_A_SEMIACTIVE_FOLDER);
		logAs(bobGratton);

		navigateToDestroyedFolderInUA10();
		assertThatAllAreDisabled();

		navigateToDepositedFolderInUA30();
		assertThatAllAreDisabled();

		navigateToAFolderInUA10();
		assertThatOnlyAvailableActionsAre(MODIFY, DUPLICATE, PRINT_LABELS, CREATE_SUB_FOLDER, CREATE_DOCUMENT, SHARE);

		navigateToSemiActiveFolderInUA10();
		assertThatOnlyAvailableActionsAre();
	}

	@Test
	/** Charles is a USER
	 * 	Charles can READ/WRITE in UA 10
	 */
	public void givenCharlesThenDisplayFolderMenuIsOk() {
		logAs(charlesFrancoisXavier);

		navigateToDestroyedFolderInUA10();
		assertThatAllAreDisabled();

		navigateToAFolderInUA10();
		assertThatOnlyAvailableActionsAre(MODIFY, DUPLICATE, PRINT_LABELS, CREATE_SUB_FOLDER, CREATE_DOCUMENT, SHARE);

		navigateToSemiActiveFolderInUA10();
		assertThatOnlyAvailableActionsAre(SHARE);
	}

	@Test
	/** Dakota is a MANAGER
	 *  Dakota can READ/WRITE/DELETE  in UA 10, 11, 12
	 */
	public void givenDakotaThenDisplayFolderMenuIsOk() {
		logAs(dakota);

		navigateToDestroyedFolderInUA10();
		assertThatOnlyAvailableActionsAre(MANAGE_AUTHORIZATIONS);
		//assertThatAllAreDisabled();

		navigateToAFolderInUA10();
		assertThatAllAreEnabled();

		navigateToSemiActiveFolderInUA10();
		assertThatOnlyAvailableActionsAre(DELETE, MANAGE_AUTHORIZATIONS, SHARE);
	}

	@Test
	/** Edouard is a USER, but manager in UA 11, 12
	 *  Edouard can READ/WRITE/DELETE in UA 11,12
	 *  Edouard can READ/WRITE in UA 30
	 */
	public void givenEdouardThenDisplayFolderMenuIsOk() {
		logAs(edouard);

		navigateToDestroyedFolderInUA30();
		assertThatAllAreDisabled();

		navigateToDepositedFolderInUA30();
		assertThatAllAreDisabled();

		navigateToAFolderInUA12();
		assertThatAllAreEnabled();

		navigateToSemiActiveFolderInUA10();
		assertThatOnlyAvailableActionsAre(DELETE, MANAGE_AUTHORIZATIONS, SHARE);
	}

	@Test
	/** Gandalf is a MANAGER
	 *  Gandalf can READ/WRITE/DELETE in UA 10
	 *  Gandalf can READ/WRITE/DELETE in UA 30
	 */
	public void givenGandalfThenDisplayFolderMenuIsOk() {
		logAs(gandalf);

		navigateToDestroyedFolderInUA10();
		assertThatOnlyAvailableActionsAre(MANAGE_AUTHORIZATIONS);

		navigateToDepositedFolderInUA30();
		assertThatOnlyAvailableActionsAre(MANAGE_AUTHORIZATIONS);

		navigateToAFolderInUA10();
		assertThatAllAreEnabled();

		navigateToSemiActiveFolderInUA10();
		assertThatOnlyAvailableActionsAre(DELETE, MANAGE_AUTHORIZATIONS, SHARE);
	}

	@Test
	/** Chuck is a RGD
	 */
	public void givenChuckThenDisplayFolderMenuIsOk() {
		logAs(chuckNorris);

		navigateToDestroyedFolderInUA10();
		assertThatAllAreEnabled();

		navigateToDepositedFolderInUA30();
		assertThatAllAreEnabled();

		navigateToAFolderInUA10();
		assertThatAllAreEnabled();

		navigateToSemiActiveFolderInUA10();
		assertThatAllAreEnabled();
	}

	private void navigateToSemiActiveFolderInUA10() {
		String id = recordIdWithTitleInCollection("Orange", zeCollection);
		driver.navigateTo().url(RMNavigationConfiguration.DISPLAY_FOLDER + "/" + id);
	}

	private void navigateToSemiActiveFolderInUA11() {
		String id = recordIdWithTitleInCollection("Poire", zeCollection);
		driver.navigateTo().url(RMNavigationConfiguration.DISPLAY_FOLDER + "/" + id);
	}

	private void navigateToSemiActiveFolderInUA12() {
		String id = recordIdWithTitleInCollection("Pomme", zeCollection);
		driver.navigateTo().url(RMNavigationConfiguration.DISPLAY_FOLDER + "/" + id);
	}

	private void navigateToSemiActiveFolderInUA30() {
		String id = recordIdWithTitleInCollection("Laitue", zeCollection);
		driver.navigateTo().url(RMNavigationConfiguration.DISPLAY_FOLDER + "/" + id);
	}

	private void navigateToDestroyedFolderInUA30() {
		String id = recordIdWithTitleInCollection("Epinard", zeCollection);
		driver.navigateTo().url(RMNavigationConfiguration.DISPLAY_FOLDER + "/" + id);
	}

	private void navigateToDestroyedFolderInUA10() {
		String id = recordIdWithTitleInCollection("Souris", zeCollection);
		driver.navigateTo().url(RMNavigationConfiguration.DISPLAY_FOLDER + "/" + id);
	}

	private void navigateToDestroyedFolderInUA11() {
		String id = recordIdWithTitleInCollection("Cerise", zeCollection);
		driver.navigateTo().url(RMNavigationConfiguration.DISPLAY_FOLDER + "/" + id);
	}

	private void navigateToDestroyedFolderInUA12() {
		String id = recordIdWithTitleInCollection("Avocat", zeCollection);
		driver.navigateTo().url(RMNavigationConfiguration.DISPLAY_FOLDER + "/" + id);
	}

	private void navigateToDepositedFolderInUA30() {
		String id = recordIdWithTitleInCollection("Pois", zeCollection);
		driver.navigateTo().url(RMNavigationConfiguration.DISPLAY_FOLDER + "/" + id);
	}

	private void navigateToAFolderInUA10() {
		String id = recordIdWithTitleInCollection("Abeille", zeCollection);
		driver.navigateTo().url(RMNavigationConfiguration.DISPLAY_FOLDER + "/" + id);
	}

	private void navigateToAFolderInUA11() {
		String id = recordIdWithTitleInCollection("Abricot", zeCollection);
		driver.navigateTo().url(RMNavigationConfiguration.DISPLAY_FOLDER + "/" + id);
	}

	private void navigateToAFolderInUA12() {
		String id = recordIdWithTitleInCollection("Banane", zeCollection);
		driver.navigateTo().url(RMNavigationConfiguration.DISPLAY_FOLDER + "/" + id);
	}

	private void navigateToAFolderInUA30() {
		String id = recordIdWithTitleInCollection("Chou", zeCollection);
		driver.navigateTo().url(RMNavigationConfiguration.DISPLAY_FOLDER + "/" + id);
	}

	private void assertThatOnlyAvailableActionsAre(AddEditFolderAction... expectedActions) {
		List<AddEditFolderAction> expectedActionsList = asList(expectedActions);

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
			assertThatAuthorisationButtonState().isEqualTo(ComponentState.ENABLED);
		} else {
			assertThatAuthorisationButtonState().isEqualTo(ComponentState.INVISIBLE);
		}

		if (expectedActionsList.contains(DUPLICATE)) {
			assertThatDuplicateButtonState().isEqualTo(ComponentState.ENABLED);
		} else {
			assertThatDuplicateButtonState().isEqualTo(ComponentState.INVISIBLE);
		}

		if (expectedActionsList.contains(PRINT_LABELS)) {
			assertThatPrintLabelButtonState().isEqualTo(ComponentState.ENABLED);
		} else {
			assertThatPrintLabelButtonState().isEqualTo(ComponentState.INVISIBLE);
		}

		if (expectedActionsList.contains(CREATE_SUB_FOLDER)) {
			assertThatAddSubFolderButtonState().isEqualTo(ComponentState.ENABLED);
		} else {
			assertThatAddSubFolderButtonState().isEqualTo(ComponentState.INVISIBLE);
		}

		if (expectedActionsList.contains(CREATE_DOCUMENT)) {
			assertThatAddDocumentButtonState().isEqualTo(ComponentState.ENABLED);
		} else {
			assertThatAddDocumentButtonState().isEqualTo(ComponentState.INVISIBLE);
		}

		if (expectedActionsList.contains(AddEditFolderAction.SHARE)) {
			assertThatShareButtonState().isEqualTo(ComponentState.ENABLED);
		} else {
			assertThatShareButtonState().isEqualTo(ComponentState.INVISIBLE);
		}
	}

	private void assertThatAllAreEnabled() {
		assertThatOnlyAvailableActionsAre(MODIFY, DELETE, MANAGE_AUTHORIZATIONS, SHARE, DUPLICATE, PRINT_LABELS,
				CREATE_SUB_FOLDER, CREATE_DOCUMENT);
	}

	private void assertThatAllAreDisabled() {
		assertThatOnlyAvailableActionsAre();
	}

	private org.assertj.core.api.ObjectAssert<ComponentState> assertThatAddDocumentButtonState() {
		return assertThatButtonState("Ajouter un document");
	}

	private org.assertj.core.api.ObjectAssert<ComponentState> assertThatAddSubFolderButtonState() {
		return assertThatButtonState("Ajouter un sous-dossier");
	}

	private org.assertj.core.api.ObjectAssert<ComponentState> assertThatDeleteButtonState() {
		return assertThatButtonState("Supprimer ce dossier");
	}

	private org.assertj.core.api.ObjectAssert<ComponentState> assertThatModifyButtonState() {
		return assertThatButtonState("Modifier la fiche dossier");
	}

	private org.assertj.core.api.ObjectAssert<ComponentState> assertThatDuplicateButtonState() {
		return assertThatButtonState("Dupliquer ce dossier");
	}

	private org.assertj.core.api.ObjectAssert<ComponentState> assertThatAuthorisationButtonState() {
		return assertThatButtonState("Autorisations");
	}

	private org.assertj.core.api.ObjectAssert<ComponentState> assertThatShareButtonState() {
		return assertThatButtonState("Partager ce dossier");
	}

	private org.assertj.core.api.ObjectAssert<ComponentState> assertThatPrintLabelButtonState() {
		return assertThatButtonState("Imprimer l'étiquette");
	}

	private org.assertj.core.api.ObjectAssert<ComponentState> assertThatButtonState(String label) {
		return assertThat(getButtonState(label)).describedAs(label);
	}

	private void logAs(String user) {
		driver = newWebDriver(loggedAsUserInCollection(user, zeCollection));
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
}
