package com.constellio.app.ui.pages.management.taxonomy;

import com.constellio.app.api.extensions.taxonomies.TaxonomyManagementClassifiedType;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.Language;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TaxonomyManagementPresenterAcceptTest extends ConstellioTest {

	RecordServices recordServices;
	RMTestRecords records = new RMTestRecords(zeCollection);
	@Mock TaxonomyManagementView view;
	@Mock SessionContext sessionContext;
	@Mock RecordVO recordVO;
	@Mock UserVO userVO;
	@Mock MetadataSchemaVO metadataSchemaVO;
	TaxonomyManagementPresenter presenter;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withEvents()
		);
		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		recordServices = getModelLayerFactory().newRecordServices();

		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getSessionContext()).thenReturn(sessionContext);
		when(sessionContext.getCurrentCollection()).thenReturn(zeCollection);
		when(userVO.getId()).thenReturn(admin);
		when(userVO.getUsername()).thenReturn(admin);
		when(sessionContext.getCurrentUser()).thenReturn(userVO);
		when(sessionContext.getCurrentLocale()).thenReturn(Locale.FRENCH);

		presenter = new TaxonomyManagementPresenter(view);
	}

	@Test
	public void whenLoadingForRootThenNoCurrentConceptAndRightValues() {
		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put(TaxonomyManagementPresenter.TAXONOMY_CODE, RMTaxonomies.CLASSIFICATION_PLAN);
		String params = ParamUtils.addParams(null, paramsMap);

		presenter.forParams(params);

		assertThat(presenter.conceptId).isNull();
		assertThat(presenter.taxonomy.getCode()).isEqualTo(RMTaxonomies.CLASSIFICATION_PLAN);
		presenter.getTaxonomy().getTitle(Language.French).equals("Plan de classification");
		presenter.getTaxonomy().getTitle(Language.English).equals("File plan");
	}

	@Test
	public void whenLoadingForLevelOneConceptThenAllValuesAreRight() {
		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put(TaxonomyManagementPresenter.TAXONOMY_CODE, RMTaxonomies.CLASSIFICATION_PLAN);
		paramsMap.put(TaxonomyManagementPresenter.CONCEPT_ID, records.categoryId_X);
		String params = ParamUtils.addParams(null, paramsMap);

		presenter.forParams(params);

		assertThat(presenter.conceptId).isEqualTo(records.categoryId_X);
		assertThat(presenter.taxonomy.getCode()).isEqualTo(RMTaxonomies.CLASSIFICATION_PLAN);

		presenter.getTaxonomy().getTitle(Language.French).equals("Plan de classification");
		presenter.getTaxonomy().getTitle(Language.English).equals("File plan");
	}

	@Test
	public void whenGettingDataProvidersThenExpectedRecordsProvided() {
		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put(TaxonomyManagementPresenter.TAXONOMY_CODE, RMTaxonomies.CLASSIFICATION_PLAN);
		String params = ParamUtils.addParams(null, paramsMap);
		presenter.forParams(params);

		List<RecordVODataProvider> dataProviders = presenter.getDataProviders();

		RecordVODataProvider dataProvider = dataProviders.get(0);
		assertThat(dataProvider.getSchema().getCode()).isEqualTo(Category.DEFAULT_SCHEMA);
		assertThat(getRecordIdsFromDataProvider(dataProvider)).containsOnly(records.categoryId_X, records.categoryId_Z);
	}

	@Test
	public void givenAdministrativeUnitWhenGetClassifiedTypeThenReturnFolderTab()
			throws Exception {

		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put(TaxonomyManagementPresenter.TAXONOMY_CODE, RMTaxonomies.ADMINISTRATIVE_UNITS);
		paramsMap.put(TaxonomyManagementPresenter.CONCEPT_ID, records.unitId_12b);
		String params = ParamUtils.addParams(null, paramsMap);
		presenter.forParams(params);

		List<TaxonomyManagementClassifiedType> classifiedTypes = presenter.getClassifiedTypes();
		assertThat(classifiedTypes).extracting("countLabel").isEqualTo(asList("Nombre de dossiers",
				"Nombre de règles de conservation"));
		assertThat(classifiedTypes.get(0).getDataProvider().size()).isEqualTo(10);
		assertThat(idsOf(classifiedTypes.get(0).getDataProvider())).containsExactly(
				records.folder_B52, records.folder_B02, records.folder_B04, records.folder_B06, records.folder_B08,
				records.folder_B54, records.folder_B30, records.folder_B32, records.folder_B34, records.folder_B50);

	}

	private List<String> idsOf(RecordVODataProvider recordVODataProvider) {
		List<String> ids = new ArrayList<>();
		for (int i = 0; i < recordVODataProvider.size(); i++) {
			RecordVO recordVO = recordVODataProvider.getRecordVO(i);
			ids.add(recordVO.getId());
		}
		return ids;
	}

	@Test
	public void givenCategoryWhenGetClassifiedTypeThenReturnFolderTab()
			throws Exception {

		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put(TaxonomyManagementPresenter.TAXONOMY_CODE, RMTaxonomies.CLASSIFICATION_PLAN);
		paramsMap.put(TaxonomyManagementPresenter.CONCEPT_ID, "categoryId_Z112");
		String params = ParamUtils.addParams(null, paramsMap);
		presenter.forParams(params);

		List<TaxonomyManagementClassifiedType> classifiedTypes = presenter.getClassifiedTypes();
		assertThat(classifiedTypes).extracting("countLabel").isEqualTo(asList("Nombre de dossiers"));
		assertThat(classifiedTypes.get(0).getDataProvider().size()).isEqualTo(5);
		assertThat(idsOf(classifiedTypes.get(0).getDataProvider())).containsExactly(
				records.folder_A08, records.folder_A07, records.folder_A09, records.folder_C03, records.folder_B03);

	}

	@Test
	public void whenDeletingANotDeletableTaxonomyThenReturnValidationErrors() {
		when(recordVO.getId()).thenReturn(records.categoryId_X100);

		presenter = spy(new TaxonomyManagementPresenter(view));
		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put(TaxonomyManagementPresenter.TAXONOMY_CODE, RMTaxonomies.CLASSIFICATION_PLAN);
		paramsMap.put(TaxonomyManagementPresenter.CONCEPT_ID, records.categoryId_X);
		String params = ParamUtils.addParams(null, paramsMap);
		presenter.forParams(params);
		ValidationErrors validationErrors = new ValidationErrors();
		validationErrors.add(TaxonomyManagementPresenterAcceptTest.class, "cannotDeleteError");
		doReturn(validationErrors).when(presenter).validateDeletable(recordVO);
		doNothing().when(presenter).displayErrorWindow(validationErrors);

		presenter.deleteButtonClicked(recordVO);

		assertThat(presenter.validateDeletable(recordVO).isEmpty()).isFalse();
	}

	@Test
	public void whenDeletingADeletableTaxonomyThenOk() {
		when(recordVO.getId()).thenReturn(records.categoryId_X13);
		when(recordVO.getSchema()).thenReturn(metadataSchemaVO);
		when(metadataSchemaVO.getCode()).thenReturn(Category.DEFAULT_SCHEMA);

		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put(TaxonomyManagementPresenter.TAXONOMY_CODE, RMTaxonomies.CLASSIFICATION_PLAN);
		paramsMap.put(TaxonomyManagementPresenter.CONCEPT_ID, records.categoryId_X);
		String params = ParamUtils.addParams(null, paramsMap);
		presenter.forParams(params);

		presenter.deleteButtonClicked(recordVO);
		presenter.forParams(params);

		List<RecordVODataProvider> dataProviders = presenter.getDataProviders();

		RecordVODataProvider dataProvider = dataProviders.get(0);
		assertThat(dataProvider.getSchema().getCode()).isEqualTo(Category.DEFAULT_SCHEMA);
		assertThat(getRecordIdsFromDataProvider(dataProvider)).containsOnly(records.categoryId_X100);

		verify(view).refreshTable();
	}

	private List<String> getRecordIdsFromDataProvider(RecordVODataProvider dataProvider) {
		List<String> IDs = new ArrayList<>();
		for (RecordVO recordVO : dataProvider.listRecordVOs(0, dataProvider.size())) {
			IDs.add(recordVO.getId());
		}
		return IDs;
	}

}
