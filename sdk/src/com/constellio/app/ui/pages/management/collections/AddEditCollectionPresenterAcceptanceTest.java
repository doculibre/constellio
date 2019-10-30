package com.constellio.app.ui.pages.management.collections;

import com.constellio.app.modules.es.ConstellioESModule;
import com.constellio.app.modules.restapi.ConstellioRestApiModule;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.robots.ConstellioRobotsModule;
import com.constellio.app.modules.tasks.TaskModule;
import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.ui.framework.data.CollectionVODataProvider.CollectionVO;
import com.constellio.app.ui.pages.management.collections.AddEditCollectionPresenterException.AddEditCollectionPresenterException_CodeCodeChangeForbidden;
import com.constellio.app.ui.pages.management.collections.AddEditCollectionPresenterException.AddEditCollectionPresenterException_CodeShouldNotContainDash;
import com.constellio.app.ui.pages.management.collections.AddEditCollectionPresenterException.AddEditCollectionPresenterException_CodeUnAvailable;
import com.constellio.app.ui.pages.management.collections.AddEditCollectionPresenterException.AddEditCollectionPresenterException_MustSelectAtLeastOneModule;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.modules.Module;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class AddEditCollectionPresenterAcceptanceTest extends ConstellioTest {
	RMTestRecords records = new RMTestRecords(zeCollection);

	AddEditCollectionPresenter presenterRelatedToNewCollection;
	AddEditCollectionPresenter presenterRelatedToZeCollection;
	@Mock
	AddEditCollectionView view;
	MockedNavigation navigator;
	private CollectionsManager collectionsManager;
	private ConstellioModulesManager modulesManager;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records).withFoldersAndContainersOfEveryStatus()
		);

		navigator = new MockedNavigation();

		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.getSessionContext()).thenReturn(FakeSessionContext.adminInCollection(zeCollection));
		when(view.navigate()).thenReturn(navigator);

		presenterRelatedToNewCollection = new AddEditCollectionPresenter(view, null);
		presenterRelatedToZeCollection = new AddEditCollectionPresenter(view, zeCollection);
		collectionsManager = getAppLayerFactory()
				.getCollectionsManager();
		modulesManager = getAppLayerFactory().getModulesManager();
	}

	@Test
	public void whenGetCollectionVOThenOk()
			throws Exception {

		givenUnsavedCollectionWhenGetCollectionVOThenOk();
		givenExistingCollectionWhenGetCollectionAfterModulesModificationVOThenOk();
	}

	private void givenExistingCollectionWhenGetCollectionAfterModulesModificationVOThenOk() throws Exception {
		CollectionVO zeCollectionVO = presenterRelatedToZeCollection.getCollectionVO();
		Collection zeCollectionRecord = collectionsManager.getCollection(zeCollection);
		assertThat(zeCollectionVO.getCode()).isEqualTo(zeCollectionRecord.getCode());
		assertThat(zeCollectionVO.getName()).isEqualTo(zeCollectionRecord.getName());
		List<String> enabledModulesForZeCollection = asList(ConstellioRMModule.ID, TaskModule.ID);
		assertThat(zeCollectionVO.getModules()).containsExactlyElementsOf(enabledModulesForZeCollection);

		Module esModule = modulesManager.getInstalledModule(ConstellioESModule.ID);
		modulesManager
				.installValidModuleAndGetInvalidOnes(esModule, getModelLayerFactory().getCollectionsListManager());
		modulesManager.enableValidModuleAndGetInvalidOnes(zeCollection, esModule);
		zeCollectionVO = presenterRelatedToZeCollection.getCollectionVO();
		assertThat(zeCollectionVO.getCode()).isEqualTo(zeCollectionRecord.getCode());
		assertThat(zeCollectionVO.getName()).isEqualTo(zeCollectionRecord.getName());
		//enabledModulesForZeCollection = asList(ConstellioRMModule.ID, TaskModule.ID, ConstellioESModule.ID);
		assertThat(zeCollectionVO.getModules()).containsOnlyOnce(ConstellioRMModule.ID, TaskModule.ID, ConstellioESModule.ID);
	}

	private void givenUnsavedCollectionWhenGetCollectionVOThenOk() {
		CollectionVO newCollectionVo = presenterRelatedToNewCollection.getCollectionVO();
		assertThat(newCollectionVo.getCode()).isNull();
		assertThat(newCollectionVo.getName()).isNull();
		assertThat(newCollectionVo.getSupportedLanguages())
				.containsOnly(getModelLayerFactory().getConfiguration().getMainDataLanguage());
		assertThat(newCollectionVo.getModules()).isEmpty();
	}

	@Test
	public void whenIsModuleSelectedThenOK()
			throws Exception {
		CollectionVO newCollectionVo = presenterRelatedToNewCollection.getCollectionVO();
		assertThat(presenterRelatedToNewCollection.isModuleSelected(ConstellioRMModule.ID, newCollectionVo)).isFalse();
		CollectionVO zeCollectionVO = presenterRelatedToZeCollection.getCollectionVO();
		assertThat(presenterRelatedToZeCollection.isModuleSelected(ConstellioRMModule.ID, zeCollectionVO)).isTrue();
	}

	@Test
	public void whenGetEnabledModulesThenOk()
			throws Exception {
		assertThat(presenterRelatedToNewCollection.getEnabledModules(zeCollection)).containsOnly(ConstellioRMModule.ID,
				TaskModule.ID);
		assertThat(presenterRelatedToNewCollection.getEnabledModules("inexistingCollection")).isEmpty();
	}

	@Test
	public void whenGetAvailableModulesThenOk()
			throws Exception {
		assertThat(presenterRelatedToNewCollection.getAvailableModules()).containsOnly(ConstellioRMModule.ID,
				TaskModule.ID, ConstellioESModule.ID, ConstellioRobotsModule.ID, ConstellioRestApiModule.ID);
	}

	@Test
	public void whenCreateCollectionThenOk()
			throws Exception {
		CollectionVO entity = new CollectionVO();
		entity.setCode("zeCode");
		entity.setName("zeName");
		entity.setSupportedLanguages(new HashSet<>(Arrays.asList(Language.French.getCode(), Language.English.getCode())));
		entity.setModules(new HashSet<>(asList(ConstellioRobotsModule.ID)));
		presenterRelatedToNewCollection.createCollection(entity);
		Collection createdCollection = collectionsManager.getCollection("zeCode");
		assertThat(createdCollection.getCode()).isEqualTo("zeCode");
		assertThat(createdCollection.getName()).isEqualTo("zeName");
		assertThat(createdCollection.getLanguages()).containsOnly(Language.French.getCode(), Language.English.getCode());
		assertThat(modulesManager.getEnabledModules("zeCode")).extracting("id").containsOnly(ConstellioRobotsModule.ID);
	}

	@Test
	public void whenUpdateCollectionThenOk()
			throws Exception {
		Collection zeCollectionRecord = collectionsManager.getCollection(zeCollection);
		assertThat(zeCollectionRecord.getLanguages()).containsOnly(Language.French.getCode(), Language.English.getCode());
		CollectionVO zeCollectionVO = presenterRelatedToZeCollection.getCollectionVO();
		Set<String> enabledModulesForZeCollection = new HashSet<>(
				asList(ConstellioRMModule.ID, TaskModule.ID, ConstellioESModule.ID));
		zeCollectionVO.setModules(enabledModulesForZeCollection);
		zeCollectionVO.setName("newName");
		zeCollectionVO.setSupportedLanguages(new HashSet<>(Arrays.asList(Language.French.getCode())));

		presenterRelatedToZeCollection.updateCollection(zeCollectionVO);

		zeCollectionRecord = collectionsManager.getCollection(zeCollection);
		assertThat(zeCollectionRecord.getCode()).isEqualTo(zeCollectionVO.getCode());
		assertThat(zeCollectionRecord.getName()).isEqualTo("newName");
		assertThat(modulesManager.getEnabledModules(zeCollectionVO.getCode())).extracting("id")
				.containsOnly(ConstellioRMModule.ID, TaskModule.ID, ConstellioESModule.ID);
		//language is not modified
		assertThat(zeCollectionRecord.getLanguages()).containsOnly(Language.French.getCode(), Language.English.getCode());
	}

	@Test(expected = AddEditCollectionPresenterException_CodeCodeChangeForbidden.class)
	public void givenCollectionWithModifiedCodeWhenSaveButtonClickedThenFailure()
			throws Exception {
		CollectionVO collectionVO = presenterRelatedToZeCollection.getCollectionVO();
		collectionVO.setCode("newCode");
		Set<String> enabledModulesForZeCollection = new HashSet<>(
				asList(TaskModule.ID));
		collectionVO.setModules(enabledModulesForZeCollection);
		presenterRelatedToZeCollection.saveButtonClicked(collectionVO);
	}

	@Test(expected = AddEditCollectionPresenterException_CodeShouldNotContainDash.class)
	public void givenCollectionWithCodeIncludingDashWhenSaveButtonClickedThenFailure()
			throws Exception {
		CollectionVO collectionVO = presenterRelatedToNewCollection.getCollectionVO();
		collectionVO.setCode("new-Code");
		Set<String> enabledModulesForZeCollection = new HashSet<>(
				asList(TaskModule.ID));
		collectionVO.setModules(enabledModulesForZeCollection);
		presenterRelatedToZeCollection.saveButtonClicked(collectionVO);
	}

	@Test(expected = AddEditCollectionPresenterException_CodeUnAvailable.class)
	public void givenNewCollectionWithExistingCodeWhenSaveButtonClickedThenFailure()
			throws Exception {
		CollectionVO collectionVO = presenterRelatedToNewCollection.getCollectionVO();
		collectionVO.setCode(zeCollection);
		Set<String> enabledModulesForZeCollection = new HashSet<>(
				asList(TaskModule.ID));
		collectionVO.setModules(enabledModulesForZeCollection);
		presenterRelatedToNewCollection.saveButtonClicked(collectionVO);
	}

	@Test(expected = AddEditCollectionPresenterException_MustSelectAtLeastOneModule.class)
	public void givenCollectionWithNoModuleWhenSaveButtonClickedThenFailure()
			throws Exception {
		CollectionVO collectionVO = presenterRelatedToNewCollection.getCollectionVO();
		collectionVO.setCode("newCode");
		presenterRelatedToNewCollection.saveButtonClicked(collectionVO);
	}

}
