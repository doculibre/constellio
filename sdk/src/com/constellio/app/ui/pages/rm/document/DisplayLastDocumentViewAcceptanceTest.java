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
import org.openqa.selenium.Keys;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.entities.ComponentState;
import com.constellio.app.ui.tools.ButtonWebElement;
import com.constellio.app.ui.tools.RecordFormWebElement;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

@UiTest
public class DisplayLastDocumentViewAcceptanceTest extends ConstellioTest {

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
	/** Admin is a RGD 
	 */
	public void givenAdminThenDisplayDocumentContextMenuIsOk() {
		logAsInZeCollection(admin);
		
		getMaisDocument().rightClick();
		assertThatAllAreEnabled();
		
		getMaisDocument().sendKeys(Keys.ESCAPE);
		getPoivronDocument().rightClick();
		assertThatAllAreEnabled();
		
		getPoivronDocument().sendKeys(Keys.ESCAPE);
		getPoisDocument().rightClick();
		assertThatAllAreEnabled();
	}

	@Test
	/** Alice is a USER
	 *  Alice can READ  
	 */
	public void givenAliceThenDisplayDocumentContextMenuIsOk() {
		logAsInZeCollection(aliceWonderland);
		
		getMaisDocument().rightClick();
		assertThatAllAreInvisibleExceptCheckEnabled();
		
		getMaisDocument().sendKeys(Keys.ESCAPE);
		getPoivronDocument().rightClick();
		assertThatAllAreInvisibleExceptCheckEnabled();
		
		getPoivronDocument().sendKeys(Keys.ESCAPE);
		getPoisDocument().rightClick();
		assertThatAllAreInvisibleExceptCheckEnabled();
	}

	@Test
	/** Bob is a USER 
	 *  Bob can READ/WRITE in UA 30
	 */
	public void givenBobThenDisplayDocumentMenuIsOk() {
		logAsInZeCollection(bobGratton);

		getPoivronDocument().rightClick();
		assertThatAllAreInvisibleExceptCheckEnabled();
	}

	@Test
	/** Edouard is a USER
	 *  Edouard can READ/WRITE in UA 30
	 */
	public void givenEdouardThenDisplayDocumentMenuIsOk() {
		logAsInZeCollection(edouard);
		
		getPoivronDocument().rightClick();
		assertThatAllAreInvisibleExceptCheckEnabled();
		
		getPoivronDocument().sendKeys(Keys.ESCAPE);
		getPoisDocument().rightClick();
		assertThatAllAreInvisibleExceptCheckEnabled();
	}

	@Test
	/** Gandalf is a MANAGER
	 *  Gandalf can READ/WRITE/DELETE in UA 30
	 */
	public void givenGandalfThenDisplayDocumentMenuIsOk() {
		logAsInZeCollection(gandalf);
		
		getMaisDocument().rightClick();
		assertThatAllAreEnabledExceptEditInvisible();
		
		getMaisDocument().sendKeys(Keys.ESCAPE);
		getPoivronDocument().rightClick();
		assertThatAllAreInvisibleExceptCheckEnabled();
		
		getPoivronDocument().sendKeys(Keys.ESCAPE);
		getPoisDocument().rightClick();
		assertThatAllAreInvisibleExceptCheckEnabled();
	}

	@Test
	/** Chuck is a RGD
	 */
	public void givenChuckThenDisplayDocumentMenuIsOk() {
		logAsInZeCollection(chuckNorris);
		
		getMaisDocument().rightClick();
		assertThatAllAreEnabled();
		
		getMaisDocument().sendKeys(Keys.ESCAPE);
		getPoivronDocument().rightClick();
		assertThatAllAreEnabled();
		
		getPoivronDocument().sendKeys(Keys.ESCAPE);
		getPoisDocument().rightClick();
		assertThatAllAreEnabled();
	}

	private void assertThatAllAreEnabled() {
		assertThat(getButtonState("Consulter la fiche de ce document")).isSameAs(ComponentState.ENABLED);
		assertThat(getButtonState("Éditer la fiche de ce document")).isSameAs(ComponentState.ENABLED);
		assertThat(getButtonState("Supprimer ce document")).isSameAs(ComponentState.ENABLED);
		assertThat(getButtonState("Partager ce document")).isSameAs(ComponentState.ENABLED);
		assertThat(getButtonState("Téléverser")).isSameAs(ComponentState.ENABLED);
	}
	
	private void assertThatAllAreInvisibleExceptCheckEnabled() {
		assertThat(getButtonState("Consulter la fiche de ce document")).isSameAs(ComponentState.ENABLED);
		assertThat(getButtonState("Éditer la fiche de ce document")).isSameAs(ComponentState.INVISIBLE);
		assertThat(getButtonState("Supprimer ce document")).isSameAs(ComponentState.INVISIBLE);
		assertThat(getButtonState("Partager ce document")).isSameAs(ComponentState.INVISIBLE);
		assertThat(getButtonState("Téléverser")).isSameAs(ComponentState.INVISIBLE);
	}
	
	private void assertThatAllAreEnabledExceptEditInvisible() {
		assertThat(getButtonState("Consulter la fiche de ce document")).isSameAs(ComponentState.ENABLED);
		assertThat(getButtonState("Éditer la fiche de ce document")).isSameAs(ComponentState.INVISIBLE);
		assertThat(getButtonState("Supprimer ce document")).isSameAs(ComponentState.ENABLED);
		assertThat(getButtonState("Partager ce document")).isSameAs(ComponentState.ENABLED);
		assertThat(getButtonState("Téléverser")).isSameAs(ComponentState.ENABLED);
	}
	
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

	private void logAsInZeCollection(String user) {
		driver = newWebDriver(loggedAsUserInCollection(user, zeCollection));
		List<ConstellioWebElement> listTabMenu = driver.findAdaptElements(By.className("v-caption"));
		listTabMenu.get(2).clickAndWaitForElementRefresh(driver.findElement(By.className("record-table")));
	}

}
