package com.constellio.app.modules.rm.ui.pages.decommissioning;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.allConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.enums.DecommissioningType;
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
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class AddExistingContainerPresenter extends SearchPresenter<AddExistingContainerView>
		implements SearchCriteriaPresenter {
	private transient LogicalSearchCondition condition;
	private transient RMSchemasRecordsServices rmRecordServices;
	private transient RMConfigs rmConfigs;

	String recordId;
	String adminUnitId;
	DecommissioningType decommissioningType;

	public AddExistingContainerPresenter(AddExistingContainerView view) {
		super(view);
	}

	@Override
	public AddExistingContainerPresenter forRequestParameters(String params) {
		recordId = params;
		DecommissioningList decommissioningList = rmRecordServices().getDecommissioningList(recordId);
		adminUnitId = decommissioningList.getAdministrativeUnit();
		decommissioningType = decommissioningList.getDecommissioningListType().getDecommissioningType();
		view.setCriteriaSchemaType(ContainerRecord.SCHEMA_TYPE);
		view.addEmptyCriterion();
		view.addEmptyCriterion();
		return this;
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	@Override
	protected List<String> getRestrictedRecordIds(String params) {
		return Arrays.asList(params);
	}

	@Override
	protected boolean hasRestrictedRecordAccess(String params, User user, Record restrictedRecord) {
		return user.has(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST).on(restrictedRecord);
	}

	@Override
	public boolean mustDisplayResults() {
		return false;
	}

	@Override
	public int getPageNumber() {
		return 1;
	}

	public void backButtonClicked() {
		view.navigateTo().displayDecommissioningList(recordId);
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
			view.navigateTo().displayDecommissioningList(recordId);
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
			rmRecordServices = new RMSchemasRecordsServices(view.getCollection(), modelLayerFactory);
		}
		return rmRecordServices;
	}

	private RMConfigs rmConfigs() {
		if (rmConfigs == null) {
			rmConfigs = new RMConfigs(modelLayerFactory.getSystemConfigurationsManager());
		}
		return rmConfigs;
	}
}
