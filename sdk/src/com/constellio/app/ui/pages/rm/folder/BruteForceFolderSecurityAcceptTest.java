package com.constellio.app.ui.pages.rm.folder;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.ui.tools.ButtonWebElement;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.SlowTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;
import org.junit.Test;
import org.openqa.selenium.By;

import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@UiTest
public class BruteForceFolderSecurityAcceptTest extends ConstellioTest {

	SearchServices searchServices;
	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;
	ConstellioWebDriver driver;

	private String[] folderButtons = new String[]{"Ajouter un document", "Ajouter un sous-dossier", "Modifier la fiche dossier",
												  "Partager ce dossier"};

	private String[] documentButtons = new String[]{"Éditer la fiche du document", "Partager ce document"};

	private void prepare()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(
						records).withFoldersAndContainersOfEveryStatus()
						.withEvents()
		);
		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		recordServices = getModelLayerFactory().newRecordServices();

		searchServices = getModelLayerFactory().newSearchServices();

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
	}

	private boolean isEnabled() {
		String parameterValue = getCurrentTestSession().getProperty("bruteForceRMSecurityTest");
		return parameterValue != null && "true".equals(parameterValue.toLowerCase().trim());
	}

	@Test
	// Confirm @SlowTest
	public void validateUserCanClickOnAllEnabledButtonsOfEveryFolders()
			throws Exception {
		if (!isEnabled()) {
			return;
		}
		prepare();
		LogicalSearchQuery allFoldersQuery = new LogicalSearchQuery()
				.setCondition(from(rm.folderSchemaType()).returnAll());

		List<String> folders = searchServices.searchRecordIds(allFoldersQuery);
		List<User> users = getModelLayerFactory().newUserServices().getAllUsersInCollection(zeCollection);

		int progression = 0;
		int total = folders.size() * users.size();

		for (User user : users) {

			logAs(user.getUsername());
			LogicalSearchQuery allFoldersWithReadQuery = new LogicalSearchQuery()
					.setCondition(from(rm.folderSchemaType()).returnAll())
					.filteredWithUser(user);
			List<String> foldersWithReadAccess = searchServices.searchRecordIds(allFoldersWithReadQuery);

			for (String folderId : folders) {

				System.out.println("" + (++progression) + " / " + total);
				driver.navigateTo().url(RMNavigationConfiguration.DISPLAY_FOLDER + "/" + folderId);
				if (foldersWithReadAccess.contains(folderId)) {
					assertThat(isOnHomePage()).describedAs("User " + user.getUsername()
														   + " should be able to view folder '" + folderId + "'")
							.isFalse();

					for (String folderButton : folderButtons) {
						ButtonWebElement button = getButtonIfEnabled(folderButton);
						if (button != null) {
							if (!clickValidateNotHomePageAndReturn(button)) {
								fail("User " + user.getUsername() + " can click on the button '" + folderButton + "' on folder '"
									 + folderId + "', but has no access to the page");
							}
						}
					}

				} else {
					assertThat(isOnHomePage()).describedAs("User " + user.getUsername()
														   + " should not be able to view folder '" + folderId + "'")
							.isTrue();
				}
			}
		}
	}

	@Test
	// Confirm @SlowTest
	public void validateUserCanClickOnAllEnabledButtonsOfEveryDocuments()
			throws Exception {
		if (!isEnabled()) {
			return;
		}
		prepare();
		LogicalSearchQuery allDocumentsQuery = new LogicalSearchQuery()
				.setCondition(from(rm.documentSchemaType()).returnAll());

		List<String> documents = searchServices.searchRecordIds(allDocumentsQuery);
		List<User> users = getModelLayerFactory().newUserServices().getAllUsersInCollection(zeCollection);

		int progression = 0;
		int total = documents.size() * users.size();

		for (User user : users) {

			logAs(user.getUsername());
			LogicalSearchQuery allDocumentsWithReadQuery = new LogicalSearchQuery()
					.setCondition(from(rm.documentSchemaType()).returnAll())
					.filteredWithUser(user);
			List<String> documentsWithReadAccess = searchServices.searchRecordIds(allDocumentsWithReadQuery);

			for (String documentId : documents) {

				System.out.println("" + (++progression) + " / " + total);
				driver.navigateTo().url(RMNavigationConfiguration.DISPLAY_DOCUMENT + "/" + documentId);
				if (documentsWithReadAccess.contains(documentId)) {
					assertThat(isOnHomePage()).describedAs("User " + user.getUsername()
														   + " should be able to view document '" + documentId + "'")
							.isFalse();

					for (String folderButton : documentButtons) {
						ButtonWebElement button = getButtonIfEnabled(folderButton);
						if (button != null) {
							if (!clickValidateNotHomePageAndReturn(button)) {
								fail("User " + user.getUsername() + " can click on the button '" + folderButton
									 + "' on document '"
									 + documentId + "', but has no access to the page");
							}
						}
					}

				} else {
					assertThat(isOnHomePage()).describedAs("User " + user.getUsername()
														   + " should not be able to view document '" + documentId + "'")
							.isTrue();
				}
			}
		}
	}

	private boolean clickValidateNotHomePageAndReturn(ButtonWebElement button) {
		String currentUrl = driver.getCurrentPage();
		button.clickAndWaitForPageReload();
		boolean homePage = isOnHomePage();
		driver.navigateTo().url(currentUrl);
		return !homePage;
	}

	private boolean isOnHomePage() {
		return driver.getCurrentUrl().endsWith("/constellio/#!/lastViewedFolders");
	}

	private void logAs(String user) {
		driver = newWebDriver(loggedAsUserInCollection(user, zeCollection));
	}

	private ButtonWebElement getButtonIfEnabled(String buttonName) {
		List<ConstellioWebElement> listButtonActionMenu = driver.findAdaptElements(By.className("action-menu-button"));
		for (ConstellioWebElement buttonElement : listButtonActionMenu) {
			if (buttonElement.getText().contains(buttonName)) {
				ButtonWebElement currentButton = new ButtonWebElement(buttonElement);
				if (currentButton.isEnabled()) {
					return currentButton;
				} else {
					return null;
				}
			}
		}
		return null;
	}

}
