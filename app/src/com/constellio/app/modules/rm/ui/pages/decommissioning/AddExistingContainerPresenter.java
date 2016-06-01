package com.constellio.app.modules.rm.ui.pages.decommissioning;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.allConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.enums.DecommissioningType;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
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
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class AddExistingContainerPresenter extends SearchPresenter<AddExistingContainerView>
		implements SearchCriteriaPresenter {
	private static final Logger LOGGER = LoggerFactory.getLogger(AddExistingContainerPresenter.class);

	private transient LogicalSearchCondition condition;
	private transient RMSchemasRecordsServices rmRecordServices;
	private transient RMConfigs rmConfigs;

	String recordId;
	String adminUnitId;
	int pageNumber;
	boolean displayResults;
	DecommissioningType decommissioningType;

	public AddExistingContainerPresenter(AddExistingContainerView view) {
		super(view);
	}

	@Override
	public AddExistingContainerPresenter forRequestParameters(String params) {
		String[] parts = params.split("/", 3);

		if (parts.length > 1) {
			recordId = parts[0];
		} else {
			recordId = params;
		}

		DecommissioningList decommissioningList = rmRecordServices().getDecommissioningList(recordId);
		adminUnitId = decommissioningList.getAdministrativeUnit();
		decommissioningType = decommissioningList.getDecommissioningListType().getDecommissioningType();
		view.setCriteriaSchemaType(ContainerRecord.SCHEMA_TYPE);

		if (parts.length > 1) {
			SavedSearch search = getSavedSearch(parts[2]);
			setSavedSearch(search);
			this.displayResults = true;
		} else {
			view.addEmptyCriterion();
			view.addEmptyCriterion();
			this.displayResults = false;
			pageNumber = 1;
		}

		return this;
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	@Override
	protected List<String> getRestrictedRecordIds(String params) {
		String[] parts = params.split("/", 3);
		String restricted;
		if (parts.length > 1) {
			restricted = parts[0];
		} else {
			restricted = params;
		}

		return Arrays.asList(restricted);
	}

	@Override
	protected boolean hasRestrictedRecordAccess(String params, User user, Record restrictedRecord) {
		return user.has(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST).on(restrictedRecord);
	}

	@Override
	public boolean mustDisplayResults() {
		return displayResults;
	}

	@Override
	public int getPageNumber() {
		return pageNumber;
	}

	private void setSavedSearch(SavedSearch search) {
		List<Criterion> criteria = search.getAdvancedSearch();
		if (criteria.isEmpty()) {
			view.addEmptyCriterion();
			view.addEmptyCriterion();
		} else {
			view.setSearchCriteria(criteria);
		}
		this.pageNumber = search.getPageNumber();
		this.setFacetSelections(search.getSelectedFacets());
	}

	@Override
	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public void backButtonClicked() {
		view.navigate().to(RMViews.class).displayDecommissioningList(recordId);
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

	@Override
	public void suggestionSelected(String suggestion) {
		// Do nothing
	}

	@Override
	public void addCriterionRequested() {
		view.addEmptyCriterion();
	}

	@Override
	public List<MetadataVO> getMetadataAllowedInCriteria() {
		return getMetadataAllowedInAdvancedSearch(ContainerRecord.SCHEMA_TYPE);
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
		return getMetadataAllowedInSort(ContainerRecord.SCHEMA_TYPE);
	}

	public void containerAdditionRequested(List<String> selectedRecordIds) {
		DecommissioningList decommissioningList = rmRecordServices().getDecommissioningList(recordId);
		List<ContainerRecord> containers = rmRecordServices().wrapContainerRecords(
				recordServices().getRecordsById(view.getCollection(), selectedRecordIds));
		decommissioningList.addContainerDetailsFrom(containers);

		try {
			recordServices().add(decommissioningList, getCurrentUser());
			view.navigate().to(RMViews.class).displayDecommissioningList(recordId);
		} catch (Exception e) {
			view.showErrorMessage($("AddExistingContainerView.failedToSave"));
		}
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

	private void buildSearchCondition()
			throws ConditionException {
		List<Criterion> criteria = view.getSearchCriteria();
		if (criteria.isEmpty()) {
			condition = selectByDecommissioningListProperties();
		} else {
			condition = allConditions(selectByDecommissioningListProperties(), selectByAdvancedSearchCriteria(criteria));
		}
	}

	private LogicalSearchCondition selectByDecommissioningListProperties() {
		if (rmConfigs().areMixedContainersAllowed()) {
			return from(rmRecordServices().containerRecordSchemaType())
					.where(rmRecordServices().containerDecommissioningType()).isEqualTo(decommissioningType);
		}
		return from(rmRecordServices().containerRecordSchemaType())
				.where(rmRecordServices().containerAdministrativeUnit()).isEqualTo(adminUnitId)
				.andWhere(rmRecordServices().containerDecommissioningType()).isEqualTo(decommissioningType);
	}

	private LogicalSearchCondition selectByAdvancedSearchCriteria(List<Criterion> criteria)
			throws ConditionException {
		return new ConditionBuilder(rmRecordServices().containerRecordSchemaType()).build(criteria);
	}

	private RMSchemasRecordsServices rmRecordServices() {
		if (rmRecordServices == null) {
			rmRecordServices = new RMSchemasRecordsServices(view.getCollection(), appLayerFactory);
		}
		return rmRecordServices;
	}

	private RMConfigs rmConfigs() {
		if (rmConfigs == null) {
			rmConfigs = new RMConfigs(modelLayerFactory.getSystemConfigurationsManager());
		}
		return rmConfigs;
	}

	protected void saveTemporarySearch(boolean refreshPage) {
		Record tmpSearchRecord = getTemporarySearchRecord();
		if (tmpSearchRecord == null) {
			tmpSearchRecord = recordServices().newRecordWithSchema(schema(SavedSearch.DEFAULT_SCHEMA));
		}

		SavedSearch search = new SavedSearch(tmpSearchRecord, types())
				.setTitle("temporaryContainer")
				.setSearchType(AddExistingContainerView.SEARCH_TYPE)
				.setUser(getCurrentUser().getId())
				.setPublic(false)
				.setTemporary(true)
				.setAdvancedSearch(view.getSearchCriteria())
				.setPageNumber(pageNumber)
				.setSelectedFacets(this.getFacetSelections().getNestedMap());
		try {
			recordServices().update(search);
			if (refreshPage) {
				view.navigate().to(RMViews.class).searchContainerForDecommissioningListReplay(recordId, search.getId());
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
					.andWhere(schema.getMetadata(SavedSearch.SEARCH_TYPE)).isEqualTo(AddExistingContainerView.SEARCH_TYPE));
		} catch (Exception e) {
			//TODO exception
			e.printStackTrace();
		}

		return null;
	}
}
