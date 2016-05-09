package com.constellio.app.modules.rm.ui.pages.decommissioning;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.allConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningListParams;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningSearchConditionFactory;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.pages.search.AdvancedSearchCriteriaComponent.SearchCriteriaPresenter;
import com.constellio.app.ui.pages.search.SearchPresenter;
import com.constellio.app.ui.pages.search.criteria.ConditionBuilder;
import com.constellio.app.ui.pages.search.criteria.ConditionException;
import com.constellio.app.ui.pages.search.criteria.ConditionException.ConditionException_EmptyCondition;
import com.constellio.app.ui.pages.search.criteria.ConditionException.ConditionException_TooManyClosedParentheses;
import com.constellio.app.ui.pages.search.criteria.ConditionException.ConditionException_UnclosedParentheses;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class DecommissioningBuilderPresenter extends SearchPresenter<DecommissioningBuilderView>
		implements SearchCriteriaPresenter {
	private static final Logger LOGGER = LoggerFactory.getLogger(DecommissioningBuilderPresenter.class);

	private transient LogicalSearchCondition condition;
	private transient RMSchemasRecordsServices rmRecordServices;
	private transient DecommissioningService decommissioningService;

	SearchType searchType;
	String adminUnitId;
	boolean displayResults;
	int pageNumber;

	public DecommissioningBuilderPresenter(DecommissioningBuilderView view) {
		super(view);
	}

	@Override
	public DecommissioningBuilderPresenter forRequestParameters(String params) {
		String[] parts = params.split("/", 3);

		if (parts.length > 1) {
			searchType = SearchType.valueOf(parts[0]);
			SavedSearch search = getSavedSearch(parts[2]);
			setSavedSearch(search);
			this.displayResults = true;
		} else {
			searchType = SearchType.valueOf(params);
			view.setCriteriaSchemaType(getSchemaType());
			view.addEmptyCriterion();
			view.addEmptyCriterion();
			this.displayResults = false;
			pageNumber = 1;
		}
		return this;
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST).onSomething();
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
		view.setAdministrativeUnit(this.adminUnitId);
	}

	public SearchType getSearchType() {
		return searchType;
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
		DecommissioningService decommissioningService = new DecommissioningService(view.getCollection(), modelLayerFactory);
		params.setAdministrativeUnit(adminUnitId);
		params.setSearchType(searchType);
		try {
			DecommissioningList decommissioningList = decommissioningService.createDecommissioningList(params, getCurrentUser());
			if (decommissioningList.getDecommissioningListType().isFolderList()) {
				view.navigate().to(RMViews.class).displayDecommissioningList(decommissioningList.getId());
			} else {
				view.navigate().to(RMViews.class).displayDocumentDecommissioningList(decommissioningList.getId());
			}
		} catch (Exception e) {
			view.showErrorMessage($("DecommissioningBuilderView.unableToSave"));
		}
	}

	public List<SelectItemVO> getAdministrativeUnits() {
		User currentUser = presenterService().getCurrentUser(view.getSessionContext());
		List<SelectItemVO> results = new ArrayList<>();
		List<String> conceptIds = decommissioningService().getAdministrativeUnitsForUser(currentUser);
		List<Record> concepts = recordServices().getRecordsById(currentUser.getCollection(), conceptIds);
		for (Record record : concepts) {
			results.add(new SelectItemVO(record.getId(), (String) record.get(Schemas.TITLE)));
		}
		return results;
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
	public MetadataVO getMetadataVO(String metadataCode) {
		return super.getMetadataVO(metadataCode);
	}

	@Override
	protected boolean saveSearch(String title, boolean publicAccess) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<MetadataVO> getMetadataAllowedInSort() {
		return getMetadataAllowedInSort(getSchemaType());
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

	void buildSearchCondition()
			throws ConditionException {
		List<Criterion> criteria = view.getSearchCriteria();
		if (criteria.isEmpty()) {
			condition = selectByDecommissioningStatus();
		} else {
			condition = allConditions(selectByDecommissioningStatus(), selectByAdvancedSearchCriteria(criteria));
		}
	}

	private String getSchemaType() {
		return searchType.isFolderSearch() ? Folder.SCHEMA_TYPE : Document.SCHEMA_TYPE;
	}

	private LogicalSearchCondition selectByDecommissioningStatus() {
		return new DecommissioningSearchConditionFactory(view.getCollection(), modelLayerFactory)
				.bySearchType(searchType, adminUnitId);
	}

	private LogicalSearchCondition selectByAdvancedSearchCriteria(List<Criterion> criteria)
			throws ConditionException {
		MetadataSchemaType type = searchType.isFolderSearch() ?
				rmRecordServices().folderSchemaType() : rmRecordServices().documentSchemaType();
		return new ConditionBuilder(type).build(criteria);
	}

	private DecommissioningService decommissioningService() {
		if (decommissioningService == null) {
			decommissioningService = new DecommissioningService(view.getCollection(), modelLayerFactory);
		}
		return decommissioningService;
	}

	private RMSchemasRecordsServices rmRecordServices() {
		if (rmRecordServices == null) {
			rmRecordServices = new RMSchemasRecordsServices(view.getCollection(), modelLayerFactory);
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

	protected void saveTemporarySearch(boolean refreshPage) {
		Record tmpSearchRecord = getTemporarySearchRecord();
		if (tmpSearchRecord == null) {
			tmpSearchRecord = recordServices().newRecordWithSchema(schema(SavedSearch.DEFAULT_SCHEMA));
		}

		SavedSearch search = new SavedSearch(tmpSearchRecord, types())
				.setTitle("temporaryDecommission")
				.setSearchType(DecommissioningBuilderView.SEARCH_TYPE)
				.setUser(getCurrentUser().getId())
				.setPublic(false)
				.setTemporary(true)
				.setSchemaFilter(getSchemaType())
				.setFreeTextSearch(adminUnitId)
				.setAdvancedSearch(view.getSearchCriteria())
				.setPageNumber(pageNumber)
				.setSelectedFacets(this.getFacetSelections().getNestedMap());
		try {
			recordServices().update(search);
			if (refreshPage) {
				view.navigate().to(RMViews.class).decommissioningListBuilderReplay(searchType.name(), search.getId());
			}
		} catch (RecordServicesException e) {
			LOGGER.info("TEMPORARY SAVE ERROR", e);
		}
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
}
