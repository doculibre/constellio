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
import com.constellio.app.ui.tools.RecordFormWebElement;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

@UiTest
public class DisplayDocumentMenuAcceptanceTest extends ConstellioTest {

	RecordFormWebElement zeForm;
	RecordServices recordServices;
	ConstellioWebDriver driver;
	RMTestRecords records;
	RMSchemasRecordsServices schemas;

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
		logAsInZeCollection(aliceWonderland);

		navigateToADocumentInUA10();
		assertThatAllAreInvisibleExceptUploadEnabled();
		
		navigateToSemiActiveDocumentInUA10();
		assertThatAllAreInvisibleExceptUploadDisabled();
		
		navigateToDestroyedDocumentInUA30();
		assertThatAllAreInvisibleExceptUploadDisabled();
		
		navigateToDepositedFolderInUA30();
		assertThatAllAreInvisibleExceptUploadDisabled();
	}

	@Test
	/** Admin is a RGD 
	 */
	public void givenAdminThenDisplayDocumentMenuIsOk() {
		logAsInZeCollection(admin);

		navigateToADocumentInUA10();
		assertThatAllAreEnabled();
		
		navigateToSemiActiveDocumentInUA10();
		assertThatAllAreEnabled();
		
		navigateToDestroyedDocumentInUA30();
		assertThatAllAreEnabled();
		
		navigateToDepositedFolderInUA30();
		assertThatAllAreEnabled();
	}

	@Test
	/** Bob is a USER 
	 *  Bob can READ/WRITE in UA 10
	 *  Bob can READ/WRITE in UA 30
	 */
	public void givenBobThenDisplayDocumentMenuIsOk() {
		logAsInZeCollection(bobGratton);

		navigateToADocumentInUA10();
		assertThatAllAreEnabledExceptShareInvisible();
		
		navigateToSemiActiveDocumentInUA10();
		assertThatAllAreEnabledExceptEditDisabledAndShareInvisible();
		
		navigateToDestroyedDocumentInUA30();
		assertThatAllAreDisabledExceptShareInvisible();
		
		navigateToDepositedFolderInUA30();
		assertThatAllAreDisabledExceptShareInvisible();
	}

	@Test
	/** Charles is a USER 
	 * 	Charles can READ/WRITE in UA 10 
	 */
	public void givenCharlesThenDisplayDocumentMenuIsOk() {
		logAsInZeCollection(charlesFrancoisXavier);

		navigateToADocumentInUA10();
		assertThatAllAreEnabledExceptShareInvisible();
		
		navigateToSemiActiveDocumentInUA10();
		assertThatAllAreEnabledExceptEditDisabledAndShareInvisible();
		
		navigateToDestroyedDocumentInUA30();
		assertThatAllAreDisabledExceptEditAndShareInvisible();
		
		navigateToDepositedFolderInUA30();
		assertThatAllAreDisabledExceptShareInvisible();
	}

	@Test
	/** Dakota is a MANAGER
	 *  Dakota can READ/WRITE  in UA 10
	 *  Dakota can READ/WRITE/DELETE in UA 10
	 */
	public void givenDakotaThenDisplayDocumentMenuIsOk() {
		logAsInZeCollection(dakota);

		navigateToADocumentInUA10();
		assertThatAllAreEnabled();
		
		navigateToSemiActiveDocumentInUA10();
		assertThatAllAreEnabledExceptEditDisabled();
		
		navigateToDestroyedDocumentInUA30();
		assertThatAllAreDisabledExceptEditInvisible();
		
		navigateToDepositedFolderInUA30();
		assertThatAllAreDisabled();
	}

	@Test
	/** Edouard is a USER, but manager in UA 11, 12
	 *  Edouard can READ/WRITE/DELETE in UA 30
	 *  Edouard can READ/WRITE in UA 10
	 */
	public void givenEdouardThenDisplayDocumentMenuIsOk() {
		logAsInZeCollection(edouard);

		navigateToADocumentInUA12();
		assertThatAllAreEnabled();
		
		navigateToSemiActiveDocumentInUA10();
		assertThatAllAreEnabledExceptEditDisabled();
		
		navigateToDestroyedDocumentInUA30();
		assertThatAllAreDisabledExceptShareInvisible();
		
		navigateToDepositedFolderInUA30();
		assertThatAllAreDisabledExceptEditAndShareInvisible();
	}

	@Test
	/** Gandalf is a MANAGER
	 *  Gandalf can READ/WRITE/DELETE in UA 10
	 *  Gandalf can READ/WRITE/DELETE in UA 30
	 */
	public void givenGandalfThenDisplayDocumentMenuIsOk() {
		logAsInZeCollection(gandalf);

		navigateToADocumentInUA10();
		assertThatAllAreEnabled();
		
		navigateToSemiActiveDocumentInUA10();
		assertThatAllAreEnabledExceptEditDisabled();
		
		navigateToDestroyedDocumentInUA30();
		assertThatAllAreDisabled();
		
		navigateToDepositedFolderInUA30();
		assertThatAllAreDisabled();
	}

	@Test
	/** Chuck is a RGD
	 */
	public void givenChuckThenDisplayDocumentMenuIsOk() {
		logAsInZeCollection(chuckNorris);

		navigateToADocumentInUA10();
		assertThatAllAreEnabled();
		
		navigateToSemiActiveDocumentInUA10();
		assertThatAllAreEnabled();
		
		navigateToDestroyedDocumentInUA30();
		assertThatAllAreEnabled();
		
		navigateToDepositedFolderInUA30();
		assertThatAllAreEnabled();
	}
	

	private void assertThatAllAreEnabled() {
		assertThat(getButtonState("Éditer la fiche du document")).isSameAs(ComponentState.ENABLED);
		assertThat(getButtonState("Supprimer ce document")).isSameAs(ComponentState.ENABLED);
		assertThat(getButtonState("Partager ce document")).isSameAs(ComponentState.ENABLED);
		assertThat(getButtonState("Téléverser")).isSameAs(ComponentState.ENABLED);
	}

	private void assertThatAllAreEnabledExceptShareInvisible() {
		assertThat(getButtonState("Éditer la fiche du document")).isSameAs(ComponentState.ENABLED);
		assertThat(getButtonState("Supprimer ce document")).isSameAs(ComponentState.ENABLED);
		assertThat(getButtonState("Partager ce document")).isSameAs(ComponentState.INVISIBLE);
		assertThat(getButtonState("Téléverser")).isSameAs(ComponentState.ENABLED);
	}
	
	private void assertThatAllAreDisabledExceptEditInvisible() {
		assertThat(getButtonState("Éditer la fiche du document")).isSameAs(ComponentState.INVISIBLE);
		assertThat(getButtonState("Supprimer ce document")).isSameAs(ComponentState.DISABLED);
		assertThat(getButtonState("Partager ce document")).isSameAs(ComponentState.DISABLED);
		assertThat(getButtonState("Téléverser")).isSameAs(ComponentState.DISABLED);
	}
	
	private void assertThatAllAreDisabled() {
		assertThat(getButtonState("Éditer la fiche du document")).isSameAs(ComponentState.DISABLED);
		assertThat(getButtonState("Supprimer ce document")).isSameAs(ComponentState.DISABLED);
		assertThat(getButtonState("Partager ce document")).isSameAs(ComponentState.DISABLED);
		assertThat(getButtonState("Téléverser")).isSameAs(ComponentState.DISABLED);
	}
	
	private void assertThatAllAreDisabledExceptEditAndShareInvisible() {
		assertThat(getButtonState("Éditer la fiche du document")).isSameAs(ComponentState.INVISIBLE);
		assertThat(getButtonState("Supprimer ce document")).isSameAs(ComponentState.DISABLED);
		assertThat(getButtonState("Partager ce document")).isSameAs(ComponentState.INVISIBLE);
		assertThat(getButtonState("Téléverser")).isSameAs(ComponentState.DISABLED);
	}
	
	private void assertThatAllAreDisabledExceptShareInvisible() {
		assertThat(getButtonState("Éditer la fiche du document")).isSameAs(ComponentState.DISABLED);
		assertThat(getButtonState("Supprimer ce document")).isSameAs(ComponentState.DISABLED);
		assertThat(getButtonState("Partager ce document")).isSameAs(ComponentState.INVISIBLE);
		assertThat(getButtonState("Téléverser")).isSameAs(ComponentState.DISABLED);
	}
	
	private void assertThatAllAreEnabledExceptEditDisabled() {
		assertThat(getButtonState("Éditer la fiche du document")).isSameAs(ComponentState.DISABLED);
		assertThat(getButtonState("Supprimer ce document")).isSameAs(ComponentState.ENABLED);
		assertThat(getButtonState("Partager ce document")).isSameAs(ComponentState.ENABLED);
		assertThat(getButtonState("Téléverser")).isSameAs(ComponentState.ENABLED);
	}
	
	private void assertThatAllAreEnabledExceptEditDisabledAndShareInvisible() {
		assertThat(getButtonState("Éditer la fiche du document")).isSameAs(ComponentState.DISABLED);
		assertThat(getButtonState("Supprimer ce document")).isSameAs(ComponentState.ENABLED);
		assertThat(getButtonState("Partager ce document")).isSameAs(ComponentState.INVISIBLE);
		assertThat(getButtonState("Téléverser")).isSameAs(ComponentState.ENABLED);
	}

	private void assertThatAllAreInvisibleExceptUploadEnabled() {
		assertThat(getButtonState("Éditer la fiche du document")).isSameAs(ComponentState.INVISIBLE);
		assertThat(getButtonState("Supprimer ce document")).isSameAs(ComponentState.INVISIBLE);
		assertThat(getButtonState("Partager ce document")).isSameAs(ComponentState.INVISIBLE);
		assertThat(getButtonState("Téléverser")).isSameAs(ComponentState.ENABLED);
	}
	
	public void assertThatAllAreInvisibleExceptUploadDisabled() {
		assertThat(getButtonState("Éditer la fiche du document")).isSameAs(ComponentState.INVISIBLE);
		assertThat(getButtonState("Supprimer ce document")).isSameAs(ComponentState.INVISIBLE);
		assertThat(getButtonState("Partager ce document")).isSameAs(ComponentState.INVISIBLE);
		assertThat(getButtonState("Téléverser")).isSameAs(ComponentState.DISABLED);
	}
	
	public void assertThatAllAreInvisibleExceptShareEnabled() { 
		assertThat(getButtonState("Éditer la fiche du document")).isSameAs(ComponentState.INVISIBLE);
		assertThat(getButtonState("Supprimer ce document")).isSameAs(ComponentState.INVISIBLE);
		assertThat(getButtonState("Partager ce document")).isSameAs(ComponentState.ENABLED);
		assertThat(getButtonState("Téléverser")).isSameAs(ComponentState.INVISIBLE);
	}

	private void logAsInZeCollection(String user) {
		driver = newWebDriver(loggedAsUserInCollection(user, zeCollection));
	}
	
	private void navigateToSemiActiveDocumentInUA10() {
		String id = recordIdWithTitleInCollection("Orange - Petit guide", zeCollection);
		driver.navigateTo().url(NavigatorConfigurationService.DISPLAY_DOCUMENT + "/" + id);
	}
	
	private void navigateToDestroyedDocumentInUA30() {
		String id = recordIdWithTitleInCollection("Epinard - Livre de recettes", zeCollection);
		driver.navigateTo().url(NavigatorConfigurationService.DISPLAY_DOCUMENT + "/" + id);
	}

	private void navigateToDepositedFolderInUA30() {
		String id = recordIdWithTitleInCollection("Lynx - Livre de recettes", zeCollection);
		driver.navigateTo().url(NavigatorConfigurationService.DISPLAY_DOCUMENT + "/" + id);
	}

	private void navigateToADocumentInUA10() {
		String id = recordIdWithTitleInCollection("Abeille - Livre de recettes", zeCollection);
		driver.navigateTo().url(NavigatorConfigurationService.DISPLAY_DOCUMENT + "/" + id);
	}

	private void navigateToADocumentInUA12() {
		String id = recordIdWithTitleInCollection("Mangue - Livre de recettes", zeCollection);
		driver.navigateTo().url(NavigatorConfigurationService.DISPLAY_DOCUMENT + "/" + id);
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
