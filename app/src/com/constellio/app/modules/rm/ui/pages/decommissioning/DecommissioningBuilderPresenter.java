package com.constellio.app.modules.rm.ui.pages.decommissioning;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.extensions.api.DecommissioningBuilderPresenterExtension;
import com.constellio.app.modules.rm.extensions.api.DecommissioningBuilderPresenterExtension.AddAdditionalSearchFiltersParams;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.model.enums.FolderMediaType;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningListParams;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningSearchConditionFactory;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailStatus;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.pages.search.AdvancedSearchCriteriaComponent.SearchCriteriaPresenter;
import com.constellio.app.ui.pages.search.SearchCriteriaPresenterUtils;
import com.constellio.app.ui.pages.search.SearchPresenter;
import com.constellio.app.ui.pages.search.criteria.ConditionBuilder;
import com.constellio.app.ui.pages.search.criteria.ConditionException;
import com.constellio.app.ui.pages.search.criteria.ConditionException.ConditionException_EmptyCondition;
import com.constellio.app.ui.pages.search.criteria.ConditionException.ConditionException_TooManyClosedParentheses;
import com.constellio.app.ui.pages.search.criteria.ConditionException.ConditionException_UnclosedParentheses;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.rometools.utils.Strings;
import com.vaadin.ui.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.data.dao.services.cache.InsertionReason.WAS_MODIFIED;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.allConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public class DecommissioningBuilderPresenter extends SearchPresenter<DecommissioningBuilderView>
		implements SearchCriteriaPresenter {
	private static final Logger LOGGER = LoggerFactory.getLogger(DecommissioningBuilderPresenter.class);

	private transient LogicalSearchCondition condition;
	private transient RMSchemasRecordsServices rmRecordServices;
	private transient DecommissioningService decommissioningService;

	private RMModuleExtensions rmModuleExtensions = appCollectionExtentions.forModule(ConstellioRMModule.ID);

	SearchType searchType;
	String adminUnitId;
	String decommissioningListId;
	boolean displayResults;
	boolean addMode;
	int pageNumber;

	public DecommissioningBuilderPresenter(DecommissioningBuilderView view) {
		super(view);
	}

	@Override
	public DecommissioningBuilderPresenter forRequestParameters(String params) {
		String[] parts = params.split("/", 3);
		List<String> partsList = Arrays.asList(parts);
		if (partsList.contains("new")) {
			view.getUIContext().clearAttribute(DecommissioningBuilderViewImpl.SAVE_SEARCH_DECOMMISSIONING);
			view.getUIContext().clearAttribute(DecommissioningBuilderViewImpl.DECOMMISSIONING_BUILDER_TYPE);
		}

		String saveSearchFromSession = view.getUIContext().getAttribute(DecommissioningBuilderViewImpl.SAVE_SEARCH_DECOMMISSIONING);

		if (!addMode) {
			if (saveSearchFromSession == null || Strings.isBlank(saveSearchFromSession)) {
				searchType = SearchType.valueOf(parts[0]);
				view.setCriteriaSchemaType(getSchemaType());
				view.addEmptyCriterion();
				view.addEmptyCriterion();
				this.displayResults = false;
				pageNumber = 1;
			} else {
				searchType = SearchType.valueOf(parts[0]);
				SavedSearch savedSearch = getSavedSearch(saveSearchFromSession);
				setSavedSearch(savedSearch);
				view.setExtraParameters(searchType.toString(), savedSearch.getId());
				this.displayResults = true;
			}
		} else if (parts.length > 2) {
			searchType = SearchType.valueOf(parts[0]);
			SavedSearch search = getSavedSearch(parts[2]);

			setSavedSearch(search);
			this.displayResults = true;
			view.getUIContext().setAttribute(DecommissioningBuilderViewImpl.SAVE_SEARCH_DECOMMISSIONING, search.getId());
			view.getUIContext().setAttribute(DecommissioningBuilderViewImpl.DECOMMISSIONING_BUILDER_TYPE, searchType.toString());
			view.setExtraParameters(searchType.toString(), search.getId());

		} else {

			if (saveSearchFromSession != null) {
				searchType = SearchType.valueOf(params);
				SavedSearch savedSearch = getSavedSearch(saveSearchFromSession);
				setSavedSearch(savedSearch);
				this.displayResults = true;
				view.setExtraParameters(searchType.toString(), savedSearch.getId());
			} else {
				searchType = SearchType.valueOf(params);
				view.setCriteriaSchemaType(getSchemaType());
				view.addEmptyCriterion();
				view.addEmptyCriterion();
				this.displayResults = false;
				pageNumber = 1;
			}
		}

		return this;
	}

	public void forParams(String params) {
		List<String> parts = asList(params.split("/", 3));
		Map<String, String> paramsMap = ParamUtils.getParamsMap(params);
		if (parts.size() == 3 && parts.get(1).equals("id")) {
			addMode = false;
			decommissioningListId = parts.get(2);
			DecommissioningList decommissioningList = rmRecordServices().getDecommissioningList(decommissioningListId);
			administrativeUnitSelected(decommissioningList.getAdministrativeUnit());
			view.setAdministrativeUnit(decommissioningList.getAdministrativeUnit());
		} else {
			addMode = true;
		}
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		String[] parts = params.split("/", 3);
		if (SearchType.transfer.equals(SearchType.valueOf(parts[0]))) {
			return user.has(RMPermissionsTo.CREATE_DECOMMISSIONING_LIST).onSomething() ||
				   user.has(RMPermissionsTo.CREATE_TRANSFER_DECOMMISSIONING_LIST).globally();
		} else {
			return user.has(RMPermissionsTo.CREATE_DECOMMISSIONING_LIST).onSomething();
		}
	}

	@Override
	public boolean mustDisplayResults() {
		return this.displayResults;
	}

	@Override
	public int getPageNumber() {
		return pageNumber;
	}

	@Override
	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	@Override
	public void suggestionSelected(String suggestion) {
		// Do nothing
	}

	private void setSavedSearch(SavedSearch search) {
		view.setCriteriaSchemaType(search.getSearchType());
		List<Criterion> criteria = search.getAdvancedSearch();
		if (criteria.isEmpty()) {
			view.addEmptyCriterion();
			view.addEmptyCriterion();
		} else {
			view.setSearchCriteria(criteria);
		}
		this.pageNumber = search.getPageNumber();
		this.setFacetSelections(search.getSelectedFacets());
		this.adminUnitId = search.getFreeTextSearch();
		setSelectedPageLength(search.getPageLength());
		view.setAdministrativeUnit(this.adminUnitId);
	}

	public SearchType getSearchType() {
		return searchType;
	}

	public DecommissioningList getDecommissioningList() {
		return decommissioningListId == null ? null : rmRecordServices().getDecommissioningList(decommissioningListId);
	}

	public void searchRequested() {
		try {
			buildSearchCondition();
			resetFacetSelection();

			view.refreshSearchResultsAndFacets();

		} catch (ConditionException_EmptyCondition e) {
			view.showErrorMessage($("AdvancedSearchView.emptyCondition"));
		} catch (ConditionException_TooManyClosedParentheses e) {
			view.showErrorMessage($("AdvancedSearchView.tooManyClosedParentheses"));
		} catch (ConditionException_UnclosedParentheses e) {
			view.showErrorMessage($("AdvancedSearchView.unclosedParentheses"));
		} catch (ConditionException e) {
			throw new RuntimeException("BUG: Uncaught ConditionException", e);
		}
	}

	public void decommissioningListCreationRequested(DecommissioningListParams params) {
		DecommissioningService decommissioningService = new DecommissioningService(view.getCollection(), appLayerFactory);
		params.setAdministrativeUnit(adminUnitId);
		params.setSearchType(searchType);
		try {
			if (params.getSelectedRecordIds() != null && params.getSelectedRecordIds().size() > 1000) {
				view.showErrorMessage($("DecommissioningBuilderView.cannotBuildADecommissioningListWithMoreThan1000Records"));
			} else {
				DecommissioningList decommissioningList = decommissioningService
						.createDecommissioningList(params, getCurrentUser());
				if (decommissioningList.getDecommissioningListType().isFolderList()) {
					view.navigate().to(RMViews.class).displayDecommissioningList(decommissioningList.getId());
				} else {
					view.navigate().to(RMViews.class).displayDocumentDecommissioningList(decommissioningList.getId());
				}
			}
		} catch (Exception e) {
			LOGGER.error("Error while creating decommissioning list", e);
			view.showErrorMessage($("DecommissioningBuilderView.unableToSave"));
		}
	}

	public void addToListButtonClicked(List<String> selected) {
		try {
			DecommissioningList decommissioningList = rmRecordServices().getDecommissioningList(decommissioningListId);
			Set<String> allIds = new HashSet<>(decommissioningList.getFolders());
			if (decommissioningList.getFolders() != null) {
				allIds = new HashSet<>(decommissioningList.getFolders());
			} else {
				allIds = new HashSet<String>();
			}
			allIds.addAll(selected);

			if (allIds.size() > 1000) {
				view.showErrorMessage($("DecommissioningBuilderView.cannotBuildADecommissioningListWithMoreThan1000Records"));
			} else {

				if (decommissioningList.getDecommissioningListType().isFolderList()) {
					if (isDecommissioningListWithSelectedFolders()) {
						decommissioningList.addFolderDetailsFor(FolderDetailStatus.SELECTED,
								rmRecordServices.getFolders(selected).toArray(new Folder[0]));
					} else {
						decommissioningList.addFolderDetailsFor(FolderDetailStatus.INCLUDED,
								rmRecordServices.getFolders(selected).toArray(new Folder[0]));
					}
					decommissioningList
							.addContainerDetailsFromFolders(rmRecordServices.getFolders(selected).toArray(new Folder[0]));
					recordServices().update(decommissioningList.getWrappedRecord());
					view.navigate().to(RMViews.class).displayDecommissioningList(decommissioningList.getId());
				} else {
					decommissioningList.addDocuments(selected.toArray(new String[0]));
					recordServices().update(decommissioningList.getWrappedRecord());
					view.navigate().to(RMViews.class).displayDocumentDecommissioningList(decommissioningList.getId());
				}
			}
		} catch (Exception e) {
			LOGGER.error("Error while creating decommissioning list", e);
			view.showErrorMessage($("DecommissioningBuilderView.unableToSave"));
		}
	}


	@Override
	public void addCriterionRequested() {
		view.addEmptyCriterion();
	}

	public void administrativeUnitSelected(String adminUnitId) {
		this.adminUnitId = adminUnitId;
	}

	@Override
	public List<MetadataVO> getMetadataAllowedInCriteria() {
		return getMetadataAllowedInAdvancedSearch(getSchemaType());
	}

	@Override
	public Map<String, String> getMetadataSchemasList(String schemaTypeCode) {
		SearchCriteriaPresenterUtils searchCriteriaPresenterUtils = new SearchCriteriaPresenterUtils(ConstellioUI.getCurrentSessionContext());
		return searchCriteriaPresenterUtils.getMetadataSchemasList(schemaTypeCode);
	}

	@Override
	public MetadataVO getMetadataVO(String metadataCode) {
		return super.getMetadataVO(metadataCode);
	}

	@Override
	protected boolean saveSearch(String title, boolean publicAccess, List<String> sharedUsers,
								 List<String> sharedGroups) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<MetadataVO> getMetadataAllowedInSort() {
		return getMetadataAllowedInSort(getSchemaType());
	}

	@Override
	public boolean isPreferAnalyzedFields() {
		return false;
	}

	@Override
	protected LogicalSearchCondition getSearchCondition() {
		if (condition == null) {
			try {
				buildSearchCondition();
			} catch (ConditionException e) {
				throw new RuntimeException("Unexpected exception (should be unreachable)", e);
			}
		}
		return condition;
	}

	protected LogicalSearchCondition buildSearchCondition()
			throws ConditionException {
		List<Criterion> criteria = view.getSearchCriteria();

		if (criteria.isEmpty()) {
			condition = selectByDecommissioningStatus();
		} else {
			condition = allConditions(selectByDecommissioningStatus(), selectByAdvancedSearchCriteria(criteria));
		}
		if (searchType.isFolderSearch()) {
			condition = condition.andWhere(rmRecordServices().folder.borrowed()).isFalseOrNull();
		}

		if (!getCurrentUser().has(RMPermissionsTo.CREATE_DECOMMISSIONING_LIST).onSomething() &&
			getCurrentUser().has(RMPermissionsTo.CREATE_TRANSFER_DECOMMISSIONING_LIST).globally()) {
			if (searchType.isFolderSearch()) {
				condition = condition.andWhere(rmRecordServices().folder.mediaType()).isEqualTo(FolderMediaType.ANALOG);
			}
		}

		for (DecommissioningBuilderPresenterExtension extension : rmModuleExtensions.getDecommissioningBuilderPresenterExtensions()) {
			condition = extension.addAdditionalSearchFilters(new AddAdditionalSearchFiltersParams(searchType, condition));
		}

		if(!((boolean) modelLayerFactory.getSystemConfigurationsManager().getValue(RMConfigs.SUB_FOLDER_DECOMMISSIONING))
				&& getSchemaType() != null && getSchemaType().equals(Folder.SCHEMA_TYPE)) {
			condition = condition.andWhere(rmRecordServices().folder.parentFolder()).isNull();
		}

		return condition;
	}

	//	protected List<String> getFoldersAlreadyInNonProcessedDecommissioningLists() {
	//		RMSchemasRecordsServices rm = rmRecordServices();
	//		Set<String> foldersToHide = new HashSet<>();
	//		List<DecommissioningList> decommissioningLists = rm.searchDecommissioningLists(where(rm.decommissioningList.status()).isNotEqual(DecomListStatus.PROCESSED)
	//				.andWhere(rm.decommissioningList.folders()).isNotNull());
	//		for(DecommissioningList list: decommissioningLists) {
	//			foldersToHide.addAll(list.getFolders());
	//		}
	//		return new ArrayList<>(foldersToHide);
	//	}
	//
	//	protected List<String> getDocumentsAlreadyInNonProcessedDecommissioningLists() {
	//		RMSchemasRecordsServices rm = rmRecordServices();
	//		Set<String> documentsToHide = new HashSet<>();
	//		List<DecommissioningList> decommissioningLists = rm.searchDecommissioningLists(where(rm.decommissioningList.status()).isNotEqual(DecomListStatus.PROCESSED)
	//				.andWhere(rm.decommissioningList.documents()).isNotNull());
	//		for(DecommissioningList list: decommissioningLists) {
	//			documentsToHide.addAll(list.getFolders());
	//		}
	//		return new ArrayList<>(documentsToHide);
	//	}

	private String getSchemaType() {
		return searchType.isFolderSearch() ? Folder.SCHEMA_TYPE : Document.SCHEMA_TYPE;
	}

	private LogicalSearchCondition selectByDecommissioningStatus() {
		return new DecommissioningSearchConditionFactory(view.getCollection(), appLayerFactory)
				.bySearchType(searchType, adminUnitId);
	}

	private LogicalSearchCondition selectByAdvancedSearchCriteria(List<Criterion> criteria)
			throws ConditionException {
		String languageCode = searchServices().getLanguageCode(view.getCollection());
		MetadataSchemaType type = searchType.isFolderSearch() ?
								  rmRecordServices().folder.schemaType() : rmRecordServices().documentSchemaType();
		return new ConditionBuilder(type, languageCode).build(criteria);
	}

	private DecommissioningService decommissioningService() {
		if (decommissioningService == null) {
			decommissioningService = new DecommissioningService(view.getCollection(), appLayerFactory);
		}
		return decommissioningService;
	}

	private RMSchemasRecordsServices rmRecordServices() {
		if (rmRecordServices == null) {
			rmRecordServices = new RMSchemasRecordsServices(view.getCollection(), appLayerFactory);
		}
		return rmRecordServices;
	}

	public static class SelectItemVO implements Serializable {
		private final String id;
		private final String label;

		public SelectItemVO(String id, String label) {
			this.id = id;
			this.label = label;
		}

		public String getId() {
			return id;
		}

		public String getLabel() {
			return label;
		}
	}

	protected SavedSearch saveTemporarySearch(boolean refreshPage) {
		Record tmpSearchRecord = getTemporarySearchRecord();
		if (tmpSearchRecord == null) {
			tmpSearchRecord = recordServices().newRecordWithSchema(schema(SavedSearch.DEFAULT_SCHEMA));
		} else {
			SavedSearch savedSearch = new SavedSearch(tmpSearchRecord, types());
			if (!savedSearch.isTemporary()) {
				tmpSearchRecord = recordServices()
						.newRecordWithSchema(schema(SavedSearch.DEFAULT_SCHEMA));
			}
		}

		SavedSearch search = new SavedSearch(tmpSearchRecord, types())
				.setTitle($("SearchView.savedSearch.temporaryDecommission"))
				.setSearchType(DecommissioningBuilderView.SEARCH_TYPE)
				.setUser(getCurrentUser().getId())
				.setPublic(false)
				.setTemporary(true)
				.setSchemaFilter(getSchemaType())
				.setFreeTextSearch(adminUnitId)
				.setAdvancedSearch(view.getSearchCriteria())
				.setPageNumber(pageNumber)
				.setSelectedFacets(this.getFacetSelections().getNestedMap())
				.setPageLength(getSelectedPageLength());
		search.getWrappedRecord().markAsSaved(99, search.getSchema());
		modelLayerFactory.getRecordsCaches().getCache(view.getCollection()).insert(search.getWrappedRecord(), WAS_MODIFIED);
		//recordServices().update(search);

		view.getUIContext().setAttribute(DecommissioningBuilderViewImpl.SAVE_SEARCH_DECOMMISSIONING, search.getId());
		view.getUIContext().setAttribute(DecommissioningBuilderViewImpl.DECOMMISSIONING_BUILDER_TYPE, searchType.toString());

		if (refreshPage) {
			view.navigate().to(RMViews.class).decommissioningListBuilderReplay(searchType.name(), search.getId());
		}
		return search;
	}

	protected boolean isDecommissioningListWithSelectedFolders() {
		return new RMConfigs(modelLayerFactory.getSystemConfigurationsManager()).isDecommissioningListWithSelectedFolders();
	}

	@Override
	public Record getTemporarySearchRecord() {
		MetadataSchema schema = schema(SavedSearch.DEFAULT_SCHEMA);
		try {
			return searchServices().searchSingleResult(from(schema)
					.where(schema.getMetadata(SavedSearch.USER)).isEqualTo(getCurrentUser().getId())
					.andWhere(schema.getMetadata(SavedSearch.TEMPORARY)).isEqualTo(true)
					.andWhere(schema.getMetadata(SavedSearch.SEARCH_TYPE)).isEqualTo(DecommissioningBuilderView.SEARCH_TYPE));
		} catch (Exception e) {
			//TODO exception
			e.printStackTrace();
		}

		return null;
	}

	public boolean isAddMode() {
		return addMode;
	}

	@Override
	public Component getExtensionComponentForCriterion(Criterion criterion) {
		AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollection(view.getCollection());
		return extensions.getComponentForCriterion(criterion);
	}
}
