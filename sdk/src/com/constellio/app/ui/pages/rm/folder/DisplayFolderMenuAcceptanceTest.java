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
package com.constellio.app.ui.pages.rm.folder;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.entities.ComponentState;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.tools.ButtonWebElement;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

@UiTest
public class DisplayFolderMenuAcceptanceTest extends ConstellioTest {

	RecordServices recordServices;
	ConstellioWebDriver driver;
	RMTestRecords records;
	RMSchemasRecordsServices rm;

	@Before
	public void setUp()
			throws Exception {

		givenCollectionWithTitle(zeCollection, "Collection de test").withConstellioRMModule().withAllTestUsers();

		recordServices = getModelLayerFactory().newRecordServices();

		records = new RMTestRecords(zeCollection).setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus()
				.withEvents();
	}

	@Test
	/** Alice is a USER
	 *  Alice can READ  
	 */
	public void givenAliceThenDisplayFolderMenuIsOk() {
		logAs(aliceWonderland);

		navigateToDestroyedFolderInUA10();
		assertThatAllAreInvisibleExceptPrintDisabled();

		navigateToDepositedFolderInUA30();
		assertThatAllAreInvisibleExceptPrintDisabled();

		navigateToActiveFolderInUA10();
		assertThat(getButtonState("Imprimer l'étiquette")).isSameAs(ComponentState.ENABLED);

		navigateToSemiActiveFolderInUA10();
		assertThat(getButtonState("Imprimer l'étiquette")).isSameAs(ComponentState.DISABLED);
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

		navigateToActiveFolderInUA10();
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
		logAs(bobGratton);

		navigateToDestroyedFolderInUA10();
		assertThatAllAreDisabledExceptShareInvisible();

		navigateToDepositedFolderInUA30();
		assertThatAllAreDisabledExceptShareInvisible();

		navigateToActiveFolderInUA10();
		assertThatAllAreEnabledExceptShareInvisible();

		navigateToSemiActiveFolderInUA10();
		assertThatAllAreDisabledExceptDeleteEnabledAndShareInvisible();
	}

	@Test
	/** Charles is a USER 
	 * 	Charles can READ/WRITE in UA 10 
	 */
	public void givenCharlesThenDisplayFolderMenuIsOk() {
		logAs(charlesFrancoisXavier);

		navigateToDestroyedFolderInUA10();
		assertThatAllAreDisabledExceptShareInvisible();

		navigateToActiveFolderInUA10();
		assertThatAllAreEnabledExceptShareInvisible();

		navigateToSemiActiveFolderInUA10();
		assertThatAllAreDisabledExceptDeleteEnabledAndShareInvisible();
	}

	@Test
	/** Dakota is a MANAGER
	 *  Dakota can READ/WRITE  in UA 10
	 *  Dakota can READ/WRITE/DELETE in UA 10
	 */
	public void givenDakotaThenDisplayFolderMenuIsOk() {
		logAs(dakota);

		navigateToDestroyedFolderInUA10();
		assertThatAllAreDisabled();

		navigateToActiveFolderInUA10();
		assertThatAllAreEnabled();

		navigateToSemiActiveFolderInUA10();
		assertThatAllAreDisabledExceptDeleteAndShareEnabled();
	}

	@Test
	/** Edouard is a USER, but manager in UA 11 et 12
	 *  Edouard can READ/WRITE in UA 30
	 *  Edouard can READ/WRITE/DELETE in UA 10
	 */
	public void givenEdouardThenDisplayFolderMenuIsOk() {
		logAs(edouard);

		navigateToDestroyedFolderInUA30();
		assertThatAllAreDisabledExceptShareInvisible();

		navigateToDepositedFolderInUA30();
		assertThatAllAreDisabledExceptShareInvisible();

		navigateToActiveFolderInUA12();
		assertThatAllAreEnabled();

		navigateToSemiActiveFolderInUA10();
		assertThatAllAreDisabledExceptDeleteAndShareEnabled();
	}

	@Test
	/** Gandalf is a MANAGER
	 *  Gandalf can READ/WRITE/DELETE in UA 10
	 *  Gandalf can READ/WRITE/DELETE in UA 30
	 */
	public void givenGandalfThenDisplayFolderMenuIsOk() {
		logAs(gandalf);

		navigateToDestroyedFolderInUA10();
		assertThatAllAreDisabled();

		navigateToDepositedFolderInUA30();
		assertThatAllAreDisabled();

		navigateToActiveFolderInUA10();
		assertThatAllAreEnabled();

		navigateToSemiActiveFolderInUA10();
		assertThatAllAreDisabledExceptDeleteAndShareEnabled();
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

		navigateToActiveFolderInUA10();
		assertThatAllAreEnabled();

		navigateToSemiActiveFolderInUA10();
		assertThatAllAreEnabled();
	}

	private void assertThatAllAreEnabled() {
		assertThat(getButtonState("Ajouter un document")).isSameAs(ComponentState.ENABLED);
		assertThat(getButtonState("Ajouter un sous-dossier")).isSameAs(ComponentState.ENABLED);
		assertThat(getButtonState("Supprimer ce dossier")).isSameAs(ComponentState.ENABLED);
		assertThat(getButtonState("Modifier la fiche dossier")).isSameAs(ComponentState.ENABLED);
		assertThat(getButtonState("Dupliquer ce dossier")).isSameAs(ComponentState.ENABLED);
		assertThat(getButtonState("Partager ce dossier")).isSameAs(ComponentState.ENABLED);
		assertThat(getButtonState("Imprimer l'étiquette")).isSameAs(ComponentState.ENABLED);
	}

	private void assertThatAllAreEnabledExceptShareInvisible() {
		assertThat(getButtonState("Ajouter un document")).isSameAs(ComponentState.ENABLED);
		assertThat(getButtonState("Ajouter un sous-dossier")).isSameAs(ComponentState.ENABLED);
		assertThat(getButtonState("Supprimer ce dossier")).isSameAs(ComponentState.ENABLED);
		assertThat(getButtonState("Modifier la fiche dossier")).isSameAs(ComponentState.ENABLED);
		assertThat(getButtonState("Dupliquer ce dossier")).isSameAs(ComponentState.ENABLED);
		assertThat(getButtonState("Partager ce dossier")).isSameAs(ComponentState.INVISIBLE);
		assertThat(getButtonState("Imprimer l'étiquette")).isSameAs(ComponentState.ENABLED);
	}

	private void assertThatAllAreDisabledExceptDeleteAndShareEnabled() {
		assertThat(getButtonState("Ajouter un document")).isSameAs(ComponentState.DISABLED);
		assertThat(getButtonState("Ajouter un sous-dossier")).isSameAs(ComponentState.DISABLED);
		assertThat(getButtonState("Supprimer ce dossier")).isSameAs(ComponentState.ENABLED);
		assertThat(getButtonState("Modifier la fiche dossier")).isSameAs(ComponentState.DISABLED);
		assertThat(getButtonState("Dupliquer ce dossier")).isSameAs(ComponentState.DISABLED);
		assertThat(getButtonState("Partager ce dossier")).isSameAs(ComponentState.ENABLED);
		assertThat(getButtonState("Imprimer l'étiquette")).isSameAs(ComponentState.DISABLED);
	}

	private void assertThatAllAreDisabled() {
		assertThat(getButtonState("Ajouter un document")).isSameAs(ComponentState.DISABLED);
		assertThat(getButtonState("Ajouter un sous-dossier")).isSameAs(ComponentState.DISABLED);
		assertThat(getButtonState("Supprimer ce dossier")).isSameAs(ComponentState.DISABLED);
		assertThat(getButtonState("Modifier la fiche dossier")).isSameAs(ComponentState.DISABLED);
		assertThat(getButtonState("Dupliquer ce dossier")).isSameAs(ComponentState.DISABLED);
		assertThat(getButtonState("Partager ce dossier")).isSameAs(ComponentState.DISABLED);
		assertThat(getButtonState("Imprimer l'étiquette")).isSameAs(ComponentState.DISABLED);
	}

	private void assertThatAllAreDisabledExceptDeleteEnabledAndShareInvisible() {
		assertThat(getButtonState("Ajouter un document")).isSameAs(ComponentState.DISABLED);
		assertThat(getButtonState("Ajouter un sous-dossier")).isSameAs(ComponentState.DISABLED);
		assertThat(getButtonState("Supprimer ce dossier")).isSameAs(ComponentState.ENABLED);
		assertThat(getButtonState("Modifier la fiche dossier")).isSameAs(ComponentState.DISABLED);
		assertThat(getButtonState("Dupliquer ce dossier")).isSameAs(ComponentState.DISABLED);
		assertThat(getButtonState("Partager ce dossier")).isSameAs(ComponentState.INVISIBLE);
		assertThat(getButtonState("Imprimer l'étiquette")).isSameAs(ComponentState.DISABLED);
	}

	private void assertThatAllAreDisabledExceptShareInvisible() {
		assertThat(getButtonState("Ajouter un document")).isSameAs(ComponentState.DISABLED);
		assertThat(getButtonState("Ajouter un sous-dossier")).isSameAs(ComponentState.DISABLED);
		assertThat(getButtonState("Supprimer ce dossier")).isSameAs(ComponentState.DISABLED);
		assertThat(getButtonState("Modifier la fiche dossier")).isSameAs(ComponentState.DISABLED);
		assertThat(getButtonState("Dupliquer ce dossier")).isSameAs(ComponentState.DISABLED);
		assertThat(getButtonState("Partager ce dossier")).isSameAs(ComponentState.INVISIBLE);
		assertThat(getButtonState("Imprimer l'étiquette")).isSameAs(ComponentState.DISABLED);
	}

	private void assertThatAllAreInvisibleExceptPrintDisabled() {
		assertThat(getButtonState("Ajouter un document")).isSameAs(ComponentState.INVISIBLE);
		assertThat(getButtonState("Ajouter un sous-dossier")).isSameAs(ComponentState.INVISIBLE);
		assertThat(getButtonState("Supprimer ce dossier")).isSameAs(ComponentState.INVISIBLE);
		assertThat(getButtonState("Modifier la fiche dossier")).isSameAs(ComponentState.INVISIBLE);
		assertThat(getButtonState("Dupliquer ce dossier")).isSameAs(ComponentState.INVISIBLE);
		assertThat(getButtonState("Partager ce dossier")).isSameAs(ComponentState.INVISIBLE);
		assertThat(getButtonState("Imprimer l'étiquette")).isSameAs(ComponentState.DISABLED);
	}

	private void logAs(String user) {
		driver = newWebDriver(loggedAsUserInCollection(user, zeCollection));
	}

	private void navigateToActiveFolderInUA10() {
		String id = recordIdWithTitleInCollection("Chien", zeCollection);
		driver.navigateTo().url(NavigatorConfigurationService.DISPLAY_FOLDER + "/" + id);
	}

	private void navigateToActiveFolderInUA12() {
		String id = recordIdWithTitleInCollection("Mangue", zeCollection);
		driver.navigateTo().url(NavigatorConfigurationService.DISPLAY_FOLDER + "/" + id);
	}

	private void navigateToDestroyedFolderInUA10() {
		String id = recordIdWithTitleInCollection("Marmotte", zeCollection);
		driver.navigateTo().url(NavigatorConfigurationService.DISPLAY_FOLDER + "/" + id);
	}

	private void navigateToSemiActiveFolderInUA10() {
		String id = recordIdWithTitleInCollection("Orange", zeCollection);
		driver.navigateTo().url(NavigatorConfigurationService.DISPLAY_FOLDER + "/" + id);
	}

	private void navigateToDestroyedFolderInUA30() {
		String id = recordIdWithTitleInCollection("Epinard", zeCollection);
		driver.navigateTo().url(NavigatorConfigurationService.DISPLAY_FOLDER + "/" + id);
	}

	private void navigateToDepositedFolderInUA30() {
		String id = recordIdWithTitleInCollection("Bette", zeCollection);
		driver.navigateTo().url(NavigatorConfigurationService.DISPLAY_FOLDER + "/" + id);
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
