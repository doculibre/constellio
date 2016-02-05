package com.constellio.app.ui.pages.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.data.event.EventTypeUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

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

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.VIEW_EVENTS).globally();
	}

	public RecordVODataProvider getDataProvider() {
		initParameters(view.getParameters());

		MetadataSchema schema = schema();
		RecordToVOBuilder voBuilder;

		List<String> metadataCodes = null;
		MetadataSchemaVO schemaVO = null;
		if (EventCategory.CURRENTLY_BORROWED_DOCUMENTS.equals(eventCategory)) {
			voBuilder = getRecordToVOBuilderToBorrowedDocuments();
		} else if (EventCategory.CURRENTLY_BORROWED_FOLDERS.equals(eventCategory) || getEventType()
				.equals(EventType.CURRENTLY_BORROWED_FOLDERS) || getEventType()
				.equals(EventType.LATE_BORROWED_FOLDERS)) {
			voBuilder = getRecordToVOBuilderToBorrowedFolders();

			MetadataSchema folderDefaultSchema = schemaType(Folder.SCHEMA_TYPE).getDefaultSchema();

			Metadata borrowUserEnteredMetadata = folderDefaultSchema.getMetadata(Folder.BORROW_USER_ENTERED);
			Metadata borrowDateMetadata = folderDefaultSchema.getMetadata(Folder.BORROW_DATE);
			Metadata borrowPreviewReturnDateMetadata = folderDefaultSchema.getMetadata(Folder.BORROW_PREVIEW_RETURN_DATE);
			Metadata folderIdentifierMetadata = folderDefaultSchema.getMetadata(CommonMetadataBuilder.ID);
			Metadata titleMetadata = folderDefaultSchema.getMetadata(CommonMetadataBuilder.TITLE);

			metadataCodes = new ArrayList<>();
			metadataCodes.add(borrowUserEnteredMetadata.getCode());
			metadataCodes.add(borrowDateMetadata.getCode());
			metadataCodes.add(borrowPreviewReturnDateMetadata.getCode());
			metadataCodes.add(folderIdentifierMetadata.getCode());
			metadataCodes.add(titleMetadata.getCode());

			schemaVO = new MetadataSchemaToVOBuilder()
					.build(folderDefaultSchema, VIEW_MODE.TABLE, metadataCodes, view.getSessionContext());

		} else {
			voBuilder = new RecordToVOBuilder();
		}
		if (metadataCodes == null) {
			metadataCodes = EventTypeUtils.getDisplayedMetadataCodes(defaultSchema(), getEventType());
			schemaVO = new MetadataSchemaToVOBuilder().build(defaultSchema(), VIEW_MODE.TABLE, metadataCodes);
		}
		RecordVODataProvider eventsDataProvider = new RecordVODataProvider(schemaVO, voBuilder, modelLayerFactory,
				view.getSessionContext()) {
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
			return rmSchemasEventsServices().newFindEventByDateRangeAndByAdministrativeUnitQuery(currentUser, eventType,
					startDate,
					endDate, id);
		case EVENTS_BY_FOLDER:
			return rmSchemasEventsServices()
					.newFindEventByDateRangeAndByAdministrativeUnitQuery(currentUser, eventType, startDate,
							endDate,
							id);//newFindEventByDateRangeAndByFolderQuery(currentUser, eventType, startDate, endDate, id);
		case EVENTS_BY_USER:
			if (eventType.equals(EventType.CURRENTLY_BORROWED_FOLDERS)) {
				return rmSchemasEventsServices().newFindCurrentlyBorrowedFoldersByUser(currentUser, id);
			} else if (eventType.equals(EventType.LATE_BORROWED_FOLDERS)) {
				return rmSchemasEventsServices().newFindLateBorrowedFoldersByUserAndDateRangeQuery(currentUser, id);
			} else {
				return rmSchemasEventsServices().newFindEventByDateRangeAndByUserIdQuery(currentUser, eventType, startDate,
						endDate, id);
			}
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

	public boolean isRecordIdMetadata(MetadataValueVO metadataValue) {
		return metadataValue.getMetadata().getCode().contains(Event.RECORD_ID) || metadataValue.getMetadata().getCode()
				.contains(CommonMetadataBuilder.ID);
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

	public String getRecordLinkTitle(MetadataValueVO metadataValue, RecordVO recordVO) {
		String recordId = metadataValue.getValue().toString();
		String title = recordVO.get(Schemas.TITLE.getLocalCode());
		if (title == null) {
			return recordId;
		} else {
			return recordId + " : " + title;
		}
	}

	public void recordLinkClicked(MetadataValueVO metadataValue) {
		String recordId = metadataValue.getValue().toString();
		try {
			recordServices().getDocumentById(recordId);
			if (getEventType().contains(EventType.DECOMMISSIONING_LIST)) {
				view.navigateTo().displayDecommissioningList(recordId);
			} else if (getEventType().contains("folder")) {
				view.navigateTo().displayFolder(recordId);
			} else if (getEventType().contains("document")) {
				view.navigateTo().displayDocument(recordId);
			} else {
				view.navigateTo().displayTask(recordId);
			}
		} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
			return;
		}
	}

	private RecordToVOBuilder getRecordToVOBuilderToBorrowedFolders() {
		RecordToVOBuilder voBuilder;
		voBuilder = new RecordToVOBuilder() {
			transient RMSchemasRecordsServices schemas;

			@Override
			public RecordVO build(Record record, VIEW_MODE viewMode, MetadataSchemaVO schemaVO,
					SessionContext sessionContext) {
				MetadataSchema folderSchema = schemas().defaultFolderSchema();
				Metadata borrowDateMetadata = folderSchema.getMetadata(Folder.BORROW_DATE);
				LocalDateTime eventTime = record.get(borrowDateMetadata);

				Metadata eventTypeMetadata = schema().getMetadata(Event.TYPE);
				Metadata eventMetaData = Schemas.CREATED_ON;
				Metadata recordIdMetadata = folderSchema.getMetadata(Schemas.IDENTIFIER.getCode());
				String recordId = record.getId();

				Metadata borrowedMetadata = folderSchema.getMetadata(Folder.BORROWED);
				LocalDateTime borrowDateValue = record.get(borrowDateMetadata);

				LogicalSearchCondition logicalSearchCondition = LogicalSearchQueryOperators.from(schemas().defaultFolderSchema())
						.where(borrowedMetadata).isTrue()
						.andWhere(borrowDateMetadata).isEqualTo(
								borrowDateValue).andWhere(recordIdMetadata).isEqualTo(recordId);

				SearchServices searchServices = modelLayerFactory.newSearchServices();
				Record eventRecord = searchServices.searchSingleResult(logicalSearchCondition);

				return super.build(eventRecord, viewMode, schemaVO, sessionContext);
			}

			private RMSchemasRecordsServices schemas() {
				if (schemas == null) {
					schemas = new RMSchemasRecordsServices(collection, modelLayerFactory);
				}
				return schemas;
			}
		};
		return voBuilder;
	}

	private RecordToVOBuilder getRecordToVOBuilderToBorrowedDocuments() {
		//TODO remove and create separate table container
		RecordToVOBuilder voBuilder;
		voBuilder = new RecordToVOBuilder() {
			transient RMSchemasRecordsServices schemas;

			@Override
			public RecordVO build(Record record, VIEW_MODE viewMode, MetadataSchemaVO schemaVO,
					SessionContext sessionContext) {
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

				return super.build(eventRecord, viewMode, schemaVO, sessionContext);
			}

			private RMSchemasRecordsServices schemas() {
				if (schemas == null) {
					schemas = new RMSchemasRecordsServices(collection, modelLayerFactory);
				}
				return schemas;
			}
		};
		return voBuilder;
	}
}
