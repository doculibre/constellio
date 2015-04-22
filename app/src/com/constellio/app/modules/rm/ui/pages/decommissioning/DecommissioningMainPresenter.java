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

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningSearchConditionFactory;
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
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class DecommissioningMainPresenter extends SingleSchemaBasePresenter<DecommissioningMainView> {
	public static final String CREATE = "create";
	public static final String GENERATED = "generated";
	public static final String PROCESSED = "processed";

	private transient RMSchemasRecordsServices rmRecordServices;

	public DecommissioningMainPresenter(DecommissioningMainView view) {
		super(view, DecommissioningList.DEFAULT_SCHEMA);
	}

	public List<String> getTabs() {
		// TODO: There are more
		// TODO: Only display applicable tabs
		return Arrays.asList(CREATE, GENERATED, PROCESSED);
	}

	public void tabSelected(String tabId) {
		switch (tabId) {
		case CREATE:
			view.displayListCreation();
			break;
		case GENERATED:
			view.displayEditableTable(getGeneratedLists());
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
				MetadataSchema schema = rmRecordServices().defaultDecommissioningListSchema();
				LogicalSearchCondition condition = from(schema)
						.where(schema.getMetadata(DecommissioningList.PROCESSING_DATE)).isNull();
				return new LogicalSearchQuery(condition);
			}
		});
	}

	RecordVODataProvider getProcessedLists() {
		// TODO: Quite not this...
		return buildDataProvider(new Factory<LogicalSearchQuery>() {
			@Override
			public LogicalSearchQuery get() {
				MetadataSchema schema = rmRecordServices().defaultDecommissioningListSchema();
				LogicalSearchCondition condition = from(schema)
						.where(schema.getMetadata(DecommissioningList.PROCESSING_DATE)).isNotNull();
				return new LogicalSearchQuery(condition);
			}
		});
	}

	private RMSchemasRecordsServices rmRecordServices() {
		if (rmRecordServices == null) {
			rmRecordServices = new RMSchemasRecordsServices(view.getCollection(), modelLayerFactory);
		}
		return rmRecordServices;
	}

	private RecordVODataProvider buildDataProvider(final Factory<LogicalSearchQuery> factory) {
		MetadataSchema schema = rmRecordServices().defaultDecommissioningListSchema();
		MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder().build(schema, VIEW_MODE.TABLE);
		return new RecordVODataProvider(schemaVO, new RecordToVOBuilder(), modelLayerFactory) {
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
