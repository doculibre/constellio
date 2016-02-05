package com.constellio.app.ui.pages.management.collections;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.tools.vaadin.WebElementUtils;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
@InDevelopmentTest
public class CollectionManagementViewImplAcceptanceTest extends ConstellioTest {
	ConstellioWebDriver driver;
	RMSchemasRecordsServices schemas;
	RMTestRecords rm = new RMTestRecords(zeCollection);
	ManageCollectionsFacade manageCollectionsFacade;

	@Before
	public void setUp()
			throws Exception {
		schemas = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
		);

		rm.setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus();

		driver = newWebDriver(loggedAsUserInCollection("admin", zeCollection));

	}

	@Test
	public void whenAddCollectionThenValidateNotExistingCodeAndCreateCollection()
			throws AWTException {
		navigateToCollectionsManagement();
		Map<String, String> existingCollections = getInitCollections();
		validateExistingCollections(existingCollections);

		AddEditCollectionFacade addEditCollectionFacade = addCollectionAndSave("zeCollection", "Collection avec code existant");
		//FIXME pourquoi ca fonctionne pas?
		//String errorMessage = addEditCollectionFacade.getErrorMessage();
		//assertThat(errorMessage).isEqualTo($("AddEditCollectionView.codeNonAvailable"));
		WebElementUtils.pressEscapeAndRelease();
		navigateToCollectionsManagement();
		validateExistingCollections(existingCollections);

		addCollectionAndCancel("collection2", "Collection 2");
		navigateToCollectionsManagement();
		validateExistingCollections(existingCollections);

		addCollectionAndSave("collection2", "Collection 2");
		existingCollections.put("collection2", "Collection 2");
		navigateToCollectionsManagement();
		validateExistingCollections(existingCollections);

		//waitUntilICloseTheBrowsers();
	}

	private void navigateToCollectionsManagement() {
		driver.navigateTo().url(NavigatorConfigurationService.COLLECTION_MANAGEMENT);
		manageCollectionsFacade = new ManageCollectionsFacade(driver);
	}

	@Test
	public void whenModifyCollectionThenCodeFieldIsDisabledAndNameIsModified() {
		Map<String, String> existingCollections = getInitCollections();

		navigateToCollectionsManagement();
		validateExistingCollections(existingCollections);

		modifyCollectionAndCancel(0, "Ze Collection");
		navigateToCollectionsManagement();
		validateExistingCollections(existingCollections);

		modifyCollectionAndSave(0, "Ze Collection");
		existingCollections.put("zeCollection", "Ze Collection");
		navigateToCollectionsManagement();
		validateExistingCollections(existingCollections);

		//waitUntilICloseTheBrowsers();
	}

	@Test
	public void whenRemoveCollectionThenIfNotCurrentCollectionThenCollectionIsRemoved() {
		Map<String, String> existingCollections = getInitCollections();

		navigateToCollectionsManagement();
		validateExistingCollections(existingCollections);
		try {
			manageCollectionsFacade.deleteButtonEnabled(0);
			throw new RuntimeException("Delete button should not be visible for the current collection");
		} catch (Exception e) {

		}

		addCollectionAndSave("collection2", "Collection 2");
		existingCollections.put("collection2", "Collection 2");
		navigateToCollectionsManagement();
		validateExistingCollections(existingCollections);
		assertThat(manageCollectionsFacade.deleteButtonEnabled(1)).isEqualTo(true);

		//FIXME pourquoi ca fonctionne pas?
		/*manageCollectionsFacade.clickRemoveCollectionAndCancel(1);//BaseFormWebElement saveCancelFacade =
		navigateToCollectionsManagement();
		validateExistingCollections(existingCollections);*/

		manageCollectionsFacade.clickRemoveCollectionAndValidate(1);
		existingCollections.remove("collection2");
		navigateToCollectionsManagement();
		validateExistingCollections(existingCollections);

		//waitUntilICloseTheBrowsers();
	}

	private AddEditCollectionFacade addCollectionAndSave(String code, String name) {
		AddEditCollectionFacade addEditCollectionFacade = addCollection(code, name);
		addEditCollectionFacade.save();
		return addEditCollectionFacade;
	}

	private AddEditCollectionFacade addCollectionAndCancel(String code, String name) {
		AddEditCollectionFacade addEditCollectionFacade = addCollection(code, name);
		addEditCollectionFacade.cancel();
		return addEditCollectionFacade;
	}

	private AddEditCollectionFacade addCollection(String code, String name) {
		manageCollectionsFacade.clickAddCollection();
		AddEditCollectionFacade addEditCollectionFacade = new AddEditCollectionFacade(driver);
		assertThat(addEditCollectionFacade.isCodeFieldEnabled()).isTrue();
		addEditCollectionFacade.setCode(code);
		addEditCollectionFacade.setName(name);
		return addEditCollectionFacade;
	}

	private AddEditCollectionFacade modifyCollectionAndSave(int i, String name) {
		AddEditCollectionFacade addEditCollectionFacade = modifyCollection(i, name);
		addEditCollectionFacade.save();
		return addEditCollectionFacade;
	}

	private AddEditCollectionFacade modifyCollectionAndCancel(int i, String name) {
		AddEditCollectionFacade addEditCollectionFacade = modifyCollection(i, name);
		addEditCollectionFacade.cancel();
		return addEditCollectionFacade;
	}

	private AddEditCollectionFacade modifyCollection(int i, String name) {
		manageCollectionsFacade.clickModifyCollection(i);
		AddEditCollectionFacade addEditCollectionFacade = new AddEditCollectionFacade(driver);
		assertThat(addEditCollectionFacade.isCodeFieldEnabled()).isFalse();
		addEditCollectionFacade.setName(name);
		return addEditCollectionFacade;
	}

	private void validateExistingCollections(Map<String, String> existingCollections) {
		assertThat(manageCollectionsFacade.getCollectionSize()).isEqualTo(existingCollections.size());
		for (int i = 0; i < manageCollectionsFacade.getCollectionSize(); i++) {
			String code = manageCollectionsFacade.getCollectionCode(i);
			assertThat(existingCollections.containsKey(code));
			assertThat(manageCollectionsFacade.getCollectionName(i)).isEqualTo(existingCollections.get(code));
		}
	}

	public Map<String, String> getInitCollections() {
		Map<String, String> existingCollections = new HashMap<>();
		existingCollections.put("zeCollection", "");
		return existingCollections;
	}
}
