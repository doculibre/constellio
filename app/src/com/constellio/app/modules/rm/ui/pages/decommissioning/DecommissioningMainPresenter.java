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

import java.util.List;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecomissioningListQueryFactory;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningSearchConditionFactory;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningSecurityService;
import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.data.utils.Factory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class DecommissioningMainPresenter extends SingleSchemaBasePresenter<DecommissioningMainView> {
	public static final String CREATE = "create";
	public static final String GENERATED = "generated";
	public static final String PENDING_VALIDATION = "pendingValidation";
	public static final String TO_VALIDATE = "toValidate";
	public static final String PENDING_APPROVAL = "pendingApproval";
	public static final String PROCESSED = "processed";

	private transient RMSchemasRecordsServices rmRecordServices;

	public DecommissioningMainPresenter(DecommissioningMainView view) {
		super(view, DecommissioningList.DEFAULT_SCHEMA);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return securityService().hasAccessToDecommissioningMainPage(user);
	}

	public List<String> getTabs() {
		return securityService().getVisibleTabsInDecommissioningMainPage(getCurrentUser());
	}

	public void tabSelected(String tabId) {
		switch (tabId) {
		case CREATE:
			view.displayListCreation();
			break;
		case GENERATED:
			view.displayEditableTable(getGeneratedLists());
			break;
		case PENDING_VALIDATION:
			view.displayReadOnlyTable(getListsPendingValidation());
			break;
		case TO_VALIDATE:
			view.displayReadOnlyTable(getListsToValidate());
			break;
		case PENDING_APPROVAL:
			view.displayReadOnlyTable(getListsPendingApproval());
			break;
		case PROCESSED:
			view.displayReadOnlyTable(getProcessedLists());
			break;
		default:
			throw new RuntimeException("BUG: Unknown tabId + " + tabId);
		}
	}

	public List<SearchType> getCriteriaForFoldersWithoutPlanifiedDate() {
		return DecommissioningSearchConditionFactory.availableCriteriaForFoldersWithoutPlanifiedDate();
	}

	public List<SearchType> getCriteriaForFoldersWithPlanifiedDate() {
		return DecommissioningSearchConditionFactory.availableCriteriaForFoldersWithPlanifiedDate();
	}

	public void creationRequested(SearchType type) {
		view.navigateTo().decommissioningListBuilder(type.toString());
	}

	public void displayButtonClicked(RecordVO entity) {
		view.navigateTo().displayDecommissioningList(entity.getId());
	}

	public void editButtonClicked(RecordVO entity) {
		view.navigateTo().editDecommissioningList(entity.getId());
	}

	public void deleteButtonClicked(RecordVO entity) {
		Record record = getRecord(entity.getId());
		delete(record);
		view.reloadCurrentTab();
	}

	RecordVODataProvider getGeneratedLists() {
		return buildDataProvider(new Factory<LogicalSearchQuery>() {
			@Override
			public LogicalSearchQuery get() {
				return queryFactory().getGeneratedListsQuery(getCurrentUser());
			}
		});
	}

	RecordVODataProvider getListsPendingValidation() {
		return buildDataProvider(new Factory<LogicalSearchQuery>() {
			@Override
			public LogicalSearchQuery get() {
				return queryFactory().getListsPendingValidationQuery(getCurrentUser());
			}
		});
	}

	RecordVODataProvider getListsToValidate() {
		return buildDataProvider(new Factory<LogicalSearchQuery>() {
			@Override
			public LogicalSearchQuery get() {
				return queryFactory().getListsToValidateQuery(getCurrentUser());
			}
		});
	}

	RecordVODataProvider getListsPendingApproval() {
		return buildDataProvider(new Factory<LogicalSearchQuery>() {
			@Override
			public LogicalSearchQuery get() {
				return queryFactory().getListsPendingApprovalQuery(getCurrentUser());
			}
		});
	}

	RecordVODataProvider getProcessedLists() {
		return buildDataProvider(new Factory<LogicalSearchQuery>() {
			@Override
			public LogicalSearchQuery get() {
				return queryFactory().getProcessedListsQuery(getCurrentUser());
			}
		});
	}

	private RMSchemasRecordsServices rmRecordServices() {
		if (rmRecordServices == null) {
			rmRecordServices = new RMSchemasRecordsServices(view.getCollection(), modelLayerFactory);
		}
		return rmRecordServices;
	}

	private DecomissioningListQueryFactory queryFactory() {
		return new DecomissioningListQueryFactory(collection, modelLayerFactory);
	}

	private DecommissioningSecurityService securityService() {
		return new DecommissioningSecurityService(collection, modelLayerFactory);
	}

	private RecordVODataProvider buildDataProvider(final Factory<LogicalSearchQuery> factory) {
		MetadataSchema schema = rmRecordServices().defaultDecommissioningListSchema();
		MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder().build(schema, VIEW_MODE.TABLE, view.getSessionContext());
		return new RecordVODataProvider(schemaVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return factory.get();
			}
		};
	}

	public void backButtonClicked() {
		view.navigateTo().archivesManagement();
	}
}
