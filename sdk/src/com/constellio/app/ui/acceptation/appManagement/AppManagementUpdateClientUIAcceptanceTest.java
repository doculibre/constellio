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
