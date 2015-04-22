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
package com.constellio.app.ui.acceptation.appManagement;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.Before;
import org.openqa.selenium.By;

import com.constellio.app.services.appManagement.AppManagementService;
import com.constellio.app.ui.pages.management.app.AppManagementView;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;
import com.constellio.sdk.tests.selenium.components.vaadin.VaadinUpload;
import com.constellio.sdk.tests.selenium.conditions.ConditionWithTimeout;

@UiTest
public class AppManagementUpdateClientUIAcceptanceTest extends ConstellioTest {

	ConstellioWebDriver webDriver;

	File tempFolder;

	@Before
	public void setUp() {
		webDriver = newWebDriver(FakeSessionContext.adminInCollection(zeCollection));
		webDriver.navigateTo().appManagement();
		tempFolder = getModelLayerFactory().getConfiguration().getTempFolder();

		assertThat(getFoldersLocator().getUploadConstellioWarFile()).doesNotExist();
		//assertThat(getFoldersLocator().getWrapperDeployFolder(tempFolder)).doesNotExist();
		assertThat(getFoldersLocator().getWrapperCommandFile()).doesNotExist();

	}

	//	@Test
	//	@DoNotRunOnIntegrationServer
	public void whenUploadingnewWarFileThenUploadedInCorrectFileAndUpdateCommandWritten()
			throws Exception {

		VaadinUpload vaadinUpload = new VaadinUpload(webDriver.findElement(By.id(AppManagementView.UPLOAD_FIELD_ID)));
		vaadinUpload.uploadFile(getTestResourceFile("fakeApp.IAmAWar"));

		assertThat(getFoldersLocator().getUploadConstellioWarFile()).exists();

		ConstellioWebElement updateButton = webDriver.findElement(By.id(AppManagementView.UPDATE_BUTTON_ID));
		updateButton.click();

		webDriver.waitForCondition(new ConditionWithTimeout() {

			@Override
			protected boolean evaluate() {
				return getFoldersLocator().getWrapperCommandFile().exists();
			}
		});

		//assertThat(getFoldersLocator().getWrapperDeployFolder(tempFolder)).exists();
		//assertThat(new File(getFoldersLocator().getWrapperDeployFolder(tempFolder), "WEB-INF")).exists();
		assertThat(getFoldersLocator().getWrapperCommandFile()).hasContent(AppManagementService.UPDATE_COMMAND);
	}

	//	@Test
	public void whenRestartingApplicationThenCommandWritten()
			throws Exception {

		ConstellioWebElement updateButton = webDriver.findElement(By.id(AppManagementView.RESTART_BUTTON_ID));
		updateButton.click();

		webDriver.waitForCondition(new ConditionWithTimeout() {

			@Override
			protected boolean evaluate() {
				return getFoldersLocator().getWrapperCommandFile().exists();
			}
		});

		assertThat(getFoldersLocator().getWrapperCommandFile()).hasContent(AppManagementService.RESTART_COMMAND);
	}
}
