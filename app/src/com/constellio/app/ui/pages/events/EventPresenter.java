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
package com.constellio.app.ui.pages.events;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.DateRangePanel;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.data.event.EventTypeUtils;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.ongoing.OngoingLogicalSearchConditionWithDataStoreFields;

public class EventPresenter extends SingleSchemaBasePresenter<EventView> {
	private transient RMSchemasRecordsServices rmSchemasRecordsServices;
	private transient RMEventsSearchServices rmSchemasEventsServices;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private EventCategory eventCategory;
	private String eventType;
	private String id;

	public EventPresenter(EventView view) {
		super(view, Event.DEFAULT_SCHEMA);
	}

	public RecordVODataProvider getDataProvider() {
		initParameters(view.getParameters());

		MetadataSchema schema = schema();
		RecordToVOBuilder voBuilder;
		if (EventCategory.CURRENTLY_BORROWED_DOCUMENTS.equals(eventCategory)) {

			//TODO remove and create separate table container
			voBuilder = new RecordToVOBuilder() {
				transient RMSchemasRecordsServices schemas;

				@Override
				public RecordVO build(Record record, VIEW_MODE viewMode) {
					return build(record, viewMode, view.getSessionContext());
				}

				@Override
				public RecordVO build(Record record, VIEW_MODE viewMode, MetadataSchemaVO schemaVO) {
					MetadataSchema documentSchema = schemas().defaultDocumentSchema();
					Metadata contentMetadata = documentSchema.getMetadata(Document.CONTENT);
					Content content = record.get(contentMetadata);
					LocalDateTime eventTime = content.getCheckoutDateTime().minusSeconds(1);

					Metadata eventTypeMetadata = schema().getMetadata(Event.TYPE);
					Metadata eventMetaData = Schemas.CREATED_ON;
					Metadata recordIdMetadata = schema().getMetadata(Event.RECORD_ID);
					LogicalSearchQuery query = new LogicalSearchQuery();
					String recordId = record.getId();
					query.setCondition(
							LogicalSearchQueryOperators.from(schema()).where(eventTypeMetadata).isEqualTo(
									EventType.BORROW_DOCUMENT)
									.andWhere(eventMetaData).isGreaterOrEqualThan(
									eventTime).andWhere(recordIdMetadata).isEqualTo(recordId));
					SearchServices searchServices = modelLayerFactory.newSearchServices();
					Record eventRecord = searchServices.search(query).get(0);

					return super.build(eventRecord, viewMode, schemaVO);
				}

				private RMSchemasRecordsServices schemas() {
					if (schemas == null) {
						schemas = new RMSchemasRecordsServices(collection, modelLayerFactory);
					}
					return schemas;
				}
			};
		} else {
			voBuilder = new RecordToVOBuilder();
		}
		List<String> metadataCodes = EventTypeUtils.getDisplayedMetadataCodes(schema(), getEventType());
		MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder().build(schema, VIEW_MODE.TABLE, metadataCodes);
		RecordVODataProvider eventsDataProvider = new RecordVODataProvider(schemaVO, voBuilder, modelLayerFactory) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return buildQueryFromParameters();
			}
		};
		return eventsDataProvider;
	}

	private RMSchemasRecordsServices rmSchemasRecordsServices() {
		if (rmSchemasRecordsServices == null) {
			rmSchemasRecordsServices = new RMSchemasRecordsServices(view.getCollection(), modelLayerFactory);
		}
		return rmSchemasRecordsServices;
	}

	private RMEventsSearchServices rmSchemasEventsServices() {
		if (rmSchemasEventsServices == null) {
			rmSchemasEventsServices = new RMEventsSearchServices(modelLayerFactory, view.getCollection());
		}
		return rmSchemasEventsServices;
	}

	private LogicalSearchQuery buildQueryFromParameters() {
		User currentUser = getCurrentUser();
		initParameters(view.getParameters());
		switch (this.eventCategory) {
		case EVENTS_BY_ADMINISTRATIVE_UNIT:
			return rmSchemasEventsServices().newFindEventByDateRangeAndByAdministrativeUnitIdQuery(currentUser, eventType,
					startDate,
					endDate, id);
		case EVENTS_BY_FOLDER:
			return rmSchemasEventsServices().newFindEventByDateRangeAndByFolderQuery(currentUser, eventType, startDate,
					endDate, id);
		case EVENTS_BY_USER:
			return rmSchemasEventsServices().newFindEventByDateRangeAndByUserIdQuery(currentUser, eventType, startDate,
					endDate, id);
		case CURRENTLY_BORROWED_DOCUMENTS:
			return rmSchemasEventsServices().newFindCurrentlyBorrowedDocumentsQuery(currentUser);
		case CURRENTLY_BORROWED_FOLDERS:
			return rmSchemasEventsServices().newFindCurrentlyBorrowedFoldersQuery(currentUser);

		default:
			return rmSchemasEventsServices().newFindEventByDateRangeQuery(currentUser, eventType, startDate, endDate);
		}
	}

	private void initParameters(Map<String, String> parameters) {
		String dateString = parameters.get(EventViewParameters.EVENT_START_DATE);
		if (dateString != null) {
			this.startDate = LocalDateTime.parse(dateString);
		}
		String endString = parameters.get(EventViewParameters.EVENT_END_DATE);
		if (endString != null) {
			this.endDate = LocalDateTime.parse(endString);
		}
		this.eventType = parameters.get(EventViewParameters.EVENT_TYPE);
		this.eventCategory = EventCategory.valueOf(parameters.get(EventViewParameters.EVENT_CATEGORY));
		this.id = parameters.get(EventViewParameters.BY_ID_EVENT_PARAMETER);
	}

	public String getEventType() {
		if (StringUtils.isBlank(eventType)) {
			initParameters(view.getParameters());
		}
		return eventType;
	}

	public void backButtonClick() {
		if (eventType == null) {
			initParameters(view.getParameters());
		}
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(EventViewParameters.EVENT_START_DATE, startDate);
		parameters.put(EventViewParameters.EVENT_END_DATE, endDate);
		parameters.put(EventViewParameters.BY_ID_EVENT_PARAMETER, id);
		parameters.put(EventViewParameters.EVENT_CATEGORY, eventCategory);
		view.navigateTo().showEventCategory(parameters);
	}

	public void recordLinkClicked(String recordId) {
		view.navigateTo().displaySchemaRecord(recordId);
	}

	public boolean isRecordIdMetadata(MetadataValueVO metadataValue) {
		return metadataValue.getMetadata().getCode().contains(Event.RECORD_ID);
	}

	public boolean isDeltaMetadata(MetadataValueVO metadataValue) {
		return metadataValue.getMetadata().getCode().contains(Event.DELTA);
	}

	public boolean isTitleMetadata(MetadataValueVO metadataValue) {
		return metadataValue.getMetadata().getCode().contains(Schemas.TITLE_CODE);
	}

	public EventCategory getEventCategory() {
		if (eventCategory == null) {
			initParameters(view.getParameters());
		}
		return eventCategory;
	}
}
