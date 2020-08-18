package com.constellio.app.ui.pages.search;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.importExport.settings.SettingsImportServices;
import com.constellio.app.services.importExport.settings.model.ImportedSettings;
import com.constellio.app.ui.entities.SearchResultVO;
import com.constellio.app.ui.framework.data.SearchResultVODataProvider;
import com.constellio.app.ui.pages.search.SearchPresenter.SortOrder;
import com.constellio.data.utils.KeySetMap;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.enums.SearchSortType;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Capsule;
import com.constellio.model.entities.records.wrappers.CapsuleLanguage;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.FakeUIContext;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class SimpleSearchPresenterAcceptanceTest extends ConstellioTest {

	String retentionRuleFacetId = "retentionRuleFacetId";
	String archivisticStatusFacetId = "archivisticStatusFacetId";
	String typeFacetId = "typeFacetId";

	LocalDateTime threeYearsAgo = new LocalDateTime().minusYears(3);

	LogicalSearchQuery allFolders;
	LogicalSearchQuery allFoldersAndDocuments;

	RMSchemasRecordsServices rm;
	Users users = new Users();
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;
	SearchServices searchServices;
	SearchPresenterService searchPresenterService;
	SchemasRecordsServices schemasRecordsServices;
	MetadataSchemaTypes types;

	@Mock SimpleSearchViewImpl view;
	SimpleSearchPresenter simpleSearchPresenter;

	long allFolderDocumentsContainersCount;

	long allFolderDocumentsContainersCountWithRetentionRule1;

	long allActiveFolderDocumentsContainersCountWithRetentionRule1;

	long foldersCount, documentsCount, containersCount;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
				.withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent());

		when(view.getUIContext()).thenReturn(new FakeUIContext());
		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.getSessionContext()).thenReturn(FakeSessionContext.gandalfInCollection(zeCollection));
		when(view.getCollection()).thenReturn(zeCollection);
		simpleSearchPresenter = new SimpleSearchPresenter(view);
		schemasRecordsServices = new SchemasRecordsServices(zeCollection, getModelLayerFactory());
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);

		allFolders = new LogicalSearchQuery(from(rm.folderSchemaType()).returnAll());
		allFoldersAndDocuments = new LogicalSearchQuery(from(asList(rm.folderSchemaType(), rm.documentSchemaType())).returnAll());

		clearExistingFacets();

		recordServices.add(rm.newFacetQuery(typeFacetId).setOrder(0).setTitle("Type")
				.withQuery("schema_s:folder*", "Dossiers")
				.withQuery("schema_s:document*", "Documents")
				.withQuery("schema_s:containerRecord*", "Contenants"));

		recordServices.add(rm.newFacetField(retentionRuleFacetId).setOrder(1).setTitle("Règles de conservations")
				.setFieldDataStoreCode("retentionRuleId_s"));
		recordServices.add(rm.newFacetField(archivisticStatusFacetId).setOrder(2).setTitle("Archivistic status")
				.setFieldDataStoreCode("archivisticStatus_s"));

		allFolderDocumentsContainersCount = searchServices.getResultsCount(
				from(asList(rm.folderSchemaType(), rm.documentSchemaType(), rm.containerRecord.schemaType())).returnAll());

		allFolderDocumentsContainersCountWithRetentionRule1 = searchServices.getResultsCount(
				from(asList(rm.folderSchemaType(), rm.documentSchemaType(), rm.containerRecord.schemaType()))
						.where(rm.folder.retentionRule()).isEqualTo(records.ruleId_1));

		allActiveFolderDocumentsContainersCountWithRetentionRule1 = searchServices.getResultsCount(
				from(asList(rm.folderSchemaType(), rm.documentSchemaType(), rm.containerRecord.schemaType()))
						.where(rm.folder.retentionRule()).isEqualTo(records.ruleId_1)
						.andWhere(rm.folder.archivisticStatus()).isEqualTo(FolderStatus.ACTIVE));

		foldersCount = searchServices.getResultsCount(from(rm.folderSchemaType()).returnAll());
		documentsCount = searchServices.getResultsCount(from(rm.documentSchemaType()).returnAll());
		containersCount = searchServices.getResultsCount(from(rm.containerRecord.schemaType()).returnAll());
	}

	@Test
	public void givenSelectedFieldFacetThenAppliedToSearchResults()
			throws Exception {

		assertThat(searchServices.getResultsCount(simpleSearchPresenter.getSearchQuery()))
				.isEqualTo(allFolderDocumentsContainersCount);

		simpleSearchPresenter.facetValueSelected(retentionRuleFacetId, records.ruleId_1);
		assertThat(searchServices.getResultsCount(simpleSearchPresenter.getSearchQuery()))
				.isEqualTo(allFolderDocumentsContainersCountWithRetentionRule1);

		simpleSearchPresenter.facetValueSelected(archivisticStatusFacetId, FolderStatus.ACTIVE.getCode());
		assertThat(searchServices.getResultsCount(simpleSearchPresenter.getSearchQuery()))
				.isEqualTo(allActiveFolderDocumentsContainersCountWithRetentionRule1);

		simpleSearchPresenter.facetValueDeselected(archivisticStatusFacetId, FolderStatus.ACTIVE.getCode());
		assertThat(searchServices.getResultsCount(simpleSearchPresenter.getSearchQuery()))
				.isEqualTo(allFolderDocumentsContainersCountWithRetentionRule1);
	}

	@Test
	public void givenSelectedFieldsFacetAndPushApplyButtonThenAppliedToSearchResults()
			throws Exception {

		assertThat(searchServices.getResultsCount(simpleSearchPresenter.getSearchQuery()))
				.isEqualTo(allFolderDocumentsContainersCount);

		final KeySetMap testKeySetMap = new KeySetMap();
		testKeySetMap.add(retentionRuleFacetId, records.ruleId_1);

		simpleSearchPresenter.facetValuesChanged(testKeySetMap);
		assertThat(searchServices.getResultsCount(simpleSearchPresenter.getSearchQuery()))
				.isEqualTo(allFolderDocumentsContainersCountWithRetentionRule1);

		testKeySetMap.add(archivisticStatusFacetId, FolderStatus.ACTIVE.getCode());
		simpleSearchPresenter.facetValuesChanged(testKeySetMap);
		assertThat(searchServices.getResultsCount(simpleSearchPresenter.getSearchQuery()))
				.isEqualTo(allActiveFolderDocumentsContainersCountWithRetentionRule1);

		testKeySetMap.remove(archivisticStatusFacetId, FolderStatus.ACTIVE.getCode());
		simpleSearchPresenter.facetValueDeselected(archivisticStatusFacetId, FolderStatus.ACTIVE.getCode());
		assertThat(searchServices.getResultsCount(simpleSearchPresenter.getSearchQuery()))
				.isEqualTo(allFolderDocumentsContainersCountWithRetentionRule1);
	}

	@Test
	public void givenSelectedQueryFacetThenAppliedToSearchResults()
			throws Exception {

		assertThat(searchServices.getResultsCount(simpleSearchPresenter.getSearchQuery()))
				.isEqualTo(foldersCount + documentsCount + containersCount);

		simpleSearchPresenter.facetValueSelected(typeFacetId, "schema_s:folder*");
		assertThat(searchServices.getResultsCount(simpleSearchPresenter.getSearchQuery()))
				.isEqualTo(foldersCount);

		simpleSearchPresenter.facetValueSelected(typeFacetId, "schema_s:document*");
		assertThat(searchServices.getResultsCount(simpleSearchPresenter.getSearchQuery()))
				.isEqualTo(foldersCount + documentsCount);

		simpleSearchPresenter.facetValueDeselected(typeFacetId, "schema_s:folder*");
		assertThat(searchServices.getResultsCount(simpleSearchPresenter.getSearchQuery()))
				.isEqualTo(documentsCount);
	}

	@Test
	public void givenSelectedQueryFacetsThenAppliedToSearchResults()
			throws Exception {

		assertThat(searchServices.getResultsCount(simpleSearchPresenter.getSearchQuery()))
				.isEqualTo(foldersCount + documentsCount + containersCount);

		final KeySetMap testKeySetMap = new KeySetMap();
		testKeySetMap.add(typeFacetId, "schema_s:folder*");
		testKeySetMap.add(typeFacetId, "schema_s:document*");

		simpleSearchPresenter.facetValuesChanged(testKeySetMap);

		assertThat(searchServices.getResultsCount(simpleSearchPresenter.getSearchQuery()))
				.isEqualTo(foldersCount + documentsCount);

		testKeySetMap.remove(typeFacetId, "schema_s:folder*");

		simpleSearchPresenter.facetValuesChanged(testKeySetMap);
		assertThat(searchServices.getResultsCount(simpleSearchPresenter.getSearchQuery()))
				.isEqualTo(documentsCount);
	}

	@Test
	public void givenSearchOrderAscByIDThenOK()
			throws Exception {
		givenConfig(ConstellioEIMConfigs.SEARCH_SORT_TYPE, SearchSortType.ID_ASC);
		simpleSearchPresenter = new SimpleSearchPresenter(view);
		assertThat(simpleSearchPresenter.getSortOrder()).isEqualTo(SortOrder.ASCENDING);
		assertThat(simpleSearchPresenter.getSortCriterion()).isEqualTo(Schemas.IDENTIFIER.getCode());
		SearchResultVODataProvider searchResults = simpleSearchPresenter.getSearchResults(true);
		List<SearchResultVO> result = searchResults.listSearchResultVOs(0, searchResults.size());
		List<String> resultsIds = getRecordsIds(result);
		assertThat(result.size()).isGreaterThan(1);
		List<String> orderedResults = new ArrayList<>(resultsIds);
		Collections.sort(orderedResults, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		assertThat(resultsIds).containsExactlyElementsOf(orderedResults);
	}

	@Test
	public void givenSearchOrderDescByIDThenOK()
			throws Exception {
		givenConfig(ConstellioEIMConfigs.SEARCH_SORT_TYPE, SearchSortType.ID_DES);
		simpleSearchPresenter = new SimpleSearchPresenter(view);
		assertThat(simpleSearchPresenter.getSortOrder()).isEqualTo(SortOrder.DESCENDING);
		assertThat(simpleSearchPresenter.getSortCriterion()).isEqualTo(Schemas.IDENTIFIER.getCode());
		SearchResultVODataProvider searchResults = simpleSearchPresenter.getSearchResults(true);
		List<SearchResultVO> result = searchResults.listSearchResultVOs(0, searchResults.size());
		List<String> resultsIds = getRecordsIds(result);
		assertThat(result.size()).isGreaterThan(1);
		List<String> orderedResults = new ArrayList<>(resultsIds);
		Collections.sort(orderedResults, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o2.compareTo(o1);
			}
		});
		assertThat(resultsIds).containsExactlyElementsOf(orderedResults);
	}

	@Test
	public void givenASearchOrderInConfigThenSearchFollowsTheOrder()
			throws Exception {
		givenConfig(ConstellioEIMConfigs.SEARCH_SORT_TYPE, SearchSortType.RELEVENCE);
		simpleSearchPresenter = new SimpleSearchPresenter(view);
		assertThat(simpleSearchPresenter.getSortOrder()).isEqualTo(SortOrder.DESCENDING);
		assertThat(simpleSearchPresenter.getSortCriterion()).isNull();

		givenConfig(ConstellioEIMConfigs.SEARCH_SORT_TYPE, SearchSortType.CREATION_DATE_ASC);
		simpleSearchPresenter = new SimpleSearchPresenter(view);
		assertThat(simpleSearchPresenter.getSortOrder()).isEqualTo(SortOrder.ASCENDING);
		assertThat(simpleSearchPresenter.getSortCriterion()).isEqualTo(Schemas.CREATED_ON.getCode());

		givenConfig(ConstellioEIMConfigs.SEARCH_SORT_TYPE, SearchSortType.CREATION_DATE_DES);
		simpleSearchPresenter = new SimpleSearchPresenter(view);
		assertThat(simpleSearchPresenter.getSortOrder()).isEqualTo(SortOrder.DESCENDING);
		assertThat(simpleSearchPresenter.getSortCriterion()).isEqualTo(Schemas.CREATED_ON.getCode());

		givenConfig(ConstellioEIMConfigs.SEARCH_SORT_TYPE, SearchSortType.MODIFICATION_DATE_ASC);
		simpleSearchPresenter = new SimpleSearchPresenter(view);
		assertThat(simpleSearchPresenter.getSortOrder()).isEqualTo(SortOrder.ASCENDING);
		assertThat(simpleSearchPresenter.getSortCriterion()).isEqualTo(Schemas.MODIFIED_ON.getCode());

		givenConfig(ConstellioEIMConfigs.SEARCH_SORT_TYPE, SearchSortType.MODIFICATION_DATE_DES);
		simpleSearchPresenter = new SimpleSearchPresenter(view);
		assertThat(simpleSearchPresenter.getSortOrder()).isEqualTo(SortOrder.DESCENDING);
		assertThat(simpleSearchPresenter.getSortCriterion()).isEqualTo(Schemas.MODIFIED_ON.getCode());

		givenConfig(ConstellioEIMConfigs.SEARCH_SORT_TYPE, SearchSortType.PATH_ASC);
		simpleSearchPresenter = new SimpleSearchPresenter(view);
		assertThat(simpleSearchPresenter.getSortOrder()).isEqualTo(SortOrder.ASCENDING);
		assertThat(simpleSearchPresenter.getSortCriterion()).isEqualTo(Schemas.PATH.getCode());

		givenConfig(ConstellioEIMConfigs.SEARCH_SORT_TYPE, SearchSortType.PATH_DES);
		simpleSearchPresenter = new SimpleSearchPresenter(view);
		assertThat(simpleSearchPresenter.getSortOrder()).isEqualTo(SortOrder.DESCENDING);
		assertThat(simpleSearchPresenter.getSortCriterion()).isEqualTo(Schemas.PATH.getCode());
	}

	@Test
	public void givenAMetadataIsSortableAndAtLeastInOneTypeThenAllowedInSort()
			throws Exception {
		SettingsImportServices services = new SettingsImportServices(getAppLayerFactory());
		ImportedSettings settings = new ImportedSettings();
		settings.newCollectionSettings(zeCollection).newType(Folder.SCHEMA_TYPE).getDefaultSchema()
				.newMetadata("USRnouvelleMetadataTest").setType(STRING).setLabel("Nouvelle metadata test")
				.setSortable(true);
		services.importSettings(settings);

		simpleSearchPresenter = new SimpleSearchPresenter(view);
		assertThat(simpleSearchPresenter.getMetadataAllowedInSort()).extracting("localCode")
				.contains(rm.defaultFolderSchema().getMetadata("USRnouvelleMetadataTest").getLocalCode());
	}

	@Test
	public void givenIDIsSetToNotSortableThenNotAllowedInSort()
			throws Exception {
		MetadataSchemasManager schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		schemasManager.modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getDefaultSchema(Folder.SCHEMA_TYPE).getMetadata("id").setSortable(false);
				types.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).getMetadata("id").setSortable(false);
				types.getDefaultSchema(Document.SCHEMA_TYPE).getMetadata("id").setSortable(false);
				types.getDefaultSchema(Task.SCHEMA_TYPE).getMetadata("id").setSortable(false);
				types.getDefaultSchema(StorageSpace.SCHEMA_TYPE).get("id").setSortable(false);
			}
		});

		simpleSearchPresenter = new SimpleSearchPresenter(view);
		assertThat(simpleSearchPresenter.getMetadataAllowedInSort()).extracting("localCode")
				.doesNotContain(rm.defaultFolderSchema().getMetadata("id").getLocalCode());
	}

	@Test
	public void testSearchCapsuleWithOneKeywords()
			throws Exception {

		Toggle.ADVANCED_SEARCH_CONFIGS.enable();

		String html = "<h1>test</h1>";
		List<String> keywords = asList("keyword1", "keyword2", "keyword3");
		String code = "code";

		CapsuleLanguage frenchCapsuleLanguage = schemasRecordsServices.getCapsuleLanguageWithCode("fr");

		Capsule capsule = schemasRecordsServices
				.newCapsule()
				.setCode(code)
				.setLanguage(frenchCapsuleLanguage)
				.setHTML(html)
				.setKeywords(keywords);
		Transaction transaction = new Transaction();
		transaction.add(capsule);
		getModelLayerFactory().newRecordServices().execute(transaction);

		simpleSearchPresenter.setSearchExpression("keyword4");
		Capsule returnedCapsule = simpleSearchPresenter.getCapsuleForCurrentSearch();
		assertThat(returnedCapsule).isNull();

		simpleSearchPresenter.setSearchExpression("keyword1");
		returnedCapsule = simpleSearchPresenter.getCapsuleForCurrentSearch();
		assertThat(returnedCapsule).isNotNull();
		assertThat(returnedCapsule.getCode()).isEqualTo(code);
		assertThat(returnedCapsule.getHTML()).isEqualTo(html);
		assertThat(returnedCapsule.getKeywords()).containsOnly(keywords.toArray(new String[0]));
	}

	@Test
	public void givenCreatedSavedSearchThenCreatedProperly() {
		simpleSearchPresenter.saveSearch("New Title", false,
				asList(users.aliceIn(zeCollection).getId(), users.bobIn(zeCollection).getId()), users.aliceIn(zeCollection).getUserGroups());

		SavedSearch savedSearch =
				rm.wrapSavedSearch(searchServices.getAllRecords(types.getSchemaType(SavedSearch.SCHEMA_TYPE)).get(0));

		assertThat(savedSearch.getTitle()).isEqualTo("New Title");
		assertThat(savedSearch.isPublic()).isFalse();
		assertThat(savedSearch.getSharedUsers()).containsOnly(users.aliceIn(zeCollection).getId(), users.bobIn(zeCollection).getId());
		assertThat(savedSearch.getSharedGroups())
				.containsOnly(users.aliceIn(zeCollection).getUserGroups().toArray(new String[0]));
	}

	private List<String> getRecordsIds(List<SearchResultVO> records) {
		List<String> returnList = new ArrayList<>();
		for (SearchResultVO record : records) {
			returnList.add(record.getRecordVO().getId());
		}
		return returnList;
	}

	// ---------------------------------------

	private void clearExistingFacets() {
		for (Record facetRecord : searchServices.search(new LogicalSearchQuery(from(rm.facetSchemaType()).returnAll()))) {
			recordServices.logicallyDelete(facetRecord, User.GOD);
			recordServices.physicallyDelete(facetRecord, User.GOD);
		}
	}
}
