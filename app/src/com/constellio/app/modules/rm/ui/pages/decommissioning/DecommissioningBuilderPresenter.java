/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.ui.pages.decommissioning;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.allConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningListParams;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningSearchConditionFactory;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.FilingSpace;
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
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class DecommissioningBuilderPresenter extends SearchPresenter<DecommissioningBuilderView>
		implements SearchCriteriaPresenter {
	private transient LogicalSearchCondition condition;
	private transient RMSchemasRecordsServices rmRecordServices;
	SearchType searchType;
	String filingSpaceId;
	String adminUnitId;

	public DecommissioningBuilderPresenter(DecommissioningBuilderView view) {
		super(view);
	}

	@Override
	public DecommissioningBuilderPresenter forRequestParameters(String params) {
		searchType = SearchType.valueOf(params);
		view.setCriteriaSchemaType(Folder.SCHEMA_TYPE);
		view.addEmptyCriterion();
		view.addEmptyCriterion();
		return this;
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(RMPermissionsTo.MANAGE_DECOMMISSIONING).globally();
	}

	@Override
	public boolean mustDisplayResults() {
		return false;
	}

	@Override
	public int getPageNumber() {
		return 1;
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
		params.setFilingSpace(filingSpaceId);
		params.setAdministrativeUnit(adminUnitId);
		params.setSearchType(searchType);
		try {
			DecommissioningList decommissioningList = decommissioningService.createDecommissioningList(params, getCurrentUser());
			view.navigateTo().displayDecommissioningList(decommissioningList.getId());
		} catch (Exception e) {
			view.showErrorMessage($("DecommissioningBuilderView.unableToSave"));
		}
	}

	public List<SelectItemVO> getUserFilingSpaces() {
		List<String> filingSpacesId = new DecommissioningService(collection, modelLayerFactory)
				.getUserFilingSpaces(getCurrentUser());
		List<FilingSpace> filingSpaces = new RMSchemasRecordsServices(collection, modelLayerFactory).getFilingSpaces(
				filingSpacesId);

		List<SelectItemVO> results = new ArrayList<>();
		for (FilingSpace filingSpace : filingSpaces) {
			results.add(new SelectItemVO(filingSpace.getId(), (String) filingSpace.getTitle()));
		}
		return results;
	}

	public List<SelectItemVO> getAdministrativeUnits() {
		MetadataSchema schema = rmRecordServices().administrativeUnitSchema();
		Metadata filingSpaces = schema.getMetadata(AdministrativeUnit.FILING_SPACES);

		LogicalSearchQuery query = new LogicalSearchQuery(
				from(schema).where(filingSpaces).isEqualTo(filingSpaceId));

		List<SelectItemVO> results = new ArrayList<>();
		for (Record record : searchServices().search(query)) {
			results.add(new SelectItemVO(record.getId(), (String) record.get(Schemas.TITLE)));
		}
		return results;
	}

	@Override
	public void addCriterionRequested() {
		view.addEmptyCriterion();
	}

	public void filingSpaceSelected(String filingSpaceId) {
		this.filingSpaceId = filingSpaceId;
		view.updateAdministrativeUnits();
	}

	public void administrativeUnitSelected(String adminUnitId) {
		this.adminUnitId = adminUnitId;
	}

	@Override
	public List<MetadataVO> getMetadatasAllowedInCriteria() {
		return getMetadatasAllowedInAdvancedSearch(Folder.SCHEMA_TYPE);
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

	private LogicalSearchCondition selectByDecommissioningStatus() {
		DecommissioningSearchConditionFactory factory = new DecommissioningSearchConditionFactory(
				view.getCollection(), modelLayerFactory);

		return factory.bySearchType(searchType, filingSpaceId, adminUnitId);
	}

	private LogicalSearchCondition selectByAdvancedSearchCriteria(List<Criterion> criteria)
			throws ConditionException {
		return new ConditionBuilder(rmRecordServices().folderSchemaType()).build(criteria);
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
}
