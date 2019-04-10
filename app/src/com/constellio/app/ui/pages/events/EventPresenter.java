package com.constellio.app.ui.pages.events;

import au.com.bytecode.opencsv.CSVWriter;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.tasks.navigation.TaskViews;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.content.InputStreamWrapper;
import com.constellio.app.ui.framework.components.content.InputStreamWrapper.SimpleAction;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.data.event.EventTypeUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class EventPresenter extends SingleSchemaBasePresenter<EventView> {
	private transient RMSchemasRecordsServices rmSchemasRecordsServices;
	private transient RMEventsSearchServices rmSchemasEventsServices;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private EventCategory eventCategory;
	private String eventType;
	private String id;
	private IOServices ioServices;
	public static final String STREAM_NAME = EventPresenter.class.getName() + "-stream";
	public static final String TEMPORARY_FILE = EventPresenter.class.getName() + "-file";
	private boolean removeTabAndNewLine;

	public EventPresenter(EventView view) {
		super(view, Event.DEFAULT_SCHEMA);
		recordServices().flush();
		ioServices = view.getConstellioFactories().getModelLayerFactory().getIOServicesFactory().newIOServices();
		removeTabAndNewLine = modelLayerFactory.getSystemConfigurationsManager()
				.getValue(ConstellioEIMConfigs.REMOVE_TAB_AND_NEW_LINE_ON_DELTA_FIELD_IN_EDIT_REPORT);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.VIEW_EVENTS).onSomething();
	}

	public RecordVODataProvider getDataProvider() {
		initParameters(view.getParameters());

		RecordToVOBuilder voBuilder;

		List<String> metadataCodes = null;
		MetadataSchemaVO schemaVO = null;
		if (EventCategory.CURRENTLY_BORROWED_DOCUMENTS.equals(eventCategory)) {
			voBuilder = getRecordToVOBuilderToBorrowedDocuments();
			MetadataSchema documentDefaultSchema = schemaType(Document.SCHEMA_TYPE).getDefaultSchema();

			Metadata documentIdentifierMetadata = documentDefaultSchema.getMetadata(CommonMetadataBuilder.ID);
			Metadata titleMetadata = documentDefaultSchema.getMetadata(CommonMetadataBuilder.TITLE);

			metadataCodes = new ArrayList<>();
			metadataCodes.add(documentIdentifierMetadata.getCode());
			metadataCodes.add(titleMetadata.getCode());

			schemaVO = new MetadataSchemaToVOBuilder()
					.build(documentDefaultSchema, VIEW_MODE.TABLE, metadataCodes, view.getSessionContext(), true);

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
					.build(folderDefaultSchema, VIEW_MODE.TABLE, metadataCodes, view.getSessionContext(), true);

		} else {
			voBuilder = new RecordToVOBuilder();
		}
		if (metadataCodes == null) {
			metadataCodes = EventTypeUtils.getDisplayedMetadataCodes(defaultSchema(), getEventType());
			schemaVO = new MetadataSchemaToVOBuilder()
					.build(defaultSchema(), VIEW_MODE.TABLE, metadataCodes, view.getSessionContext());
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

	public InputStreamWrapper createInputStreamWrapper() {
		InputStreamWrapper inputStreamWrapper = new InputStreamWrapper();
		inputStreamWrapper.addSimpleAction(new SimpleAction() {
			@Override
			public void action(InputStreamWrapper inputStreamWrapper) {
				try {
					final File tempFile = generateCsvReport();
					InputStream inputStream = new FileInputStream(tempFile) {
						@Override
						public void close()
								throws IOException {
							super.close();
							ioServices.deleteQuietly(tempFile);
						}
					};

					inputStreamWrapper.setInputStream(inputStream);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		});

		return inputStreamWrapper;
	}

	public File generateCsvReport() {

		OutputStreamWriter outputStreamWriter = null;
		CSVWriter csvWriter = null;
		OutputStream byteArrayOutputStream = null;
		File temporaryFile = null;
		try {
			temporaryFile = ioServices.newTemporaryFile(TEMPORARY_FILE);
			byteArrayOutputStream = ioServices.newFileOutputStream(temporaryFile, STREAM_NAME);
			outputStreamWriter = new OutputStreamWriter(byteArrayOutputStream, CharEncoding.ISO_8859_1);
			csvWriter = new CSVWriter(outputStreamWriter, ',', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER);
			writeCsvReport(csvWriter);
			csvWriter.flush();
			ioServices.closeQuietly(csvWriter);
			ioServices.closeQuietly(outputStreamWriter);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			ioServices.closeQuietly(csvWriter);
			ioServices.closeQuietly(outputStreamWriter);
			ioServices.closeQuietly(byteArrayOutputStream);
		}

		return temporaryFile;
	}

	public void writeCsvReport(CSVWriter csvWriter) {
		RecordVODataProvider dataProvider = getDataProvider();

		String[] headerRecord = view.getTableColumn();
		csvWriter.writeNext(headerRecord);
		SearchResponseIterator<Record> searchResponseIterator = dataProvider.getIterator();
		Object[] visiblePropertyObject = view.getTableVisibleProperties();

		while (searchResponseIterator.hasNext()) {
			Record currentRecord = searchResponseIterator.next();
			RecordToVOBuilder voBuilder = new RecordToVOBuilder();
			RecordVO recordVO = voBuilder.build(currentRecord, VIEW_MODE.TABLE, view.getSessionContext());
			String[] stringArray = new String[visiblePropertyObject.length];

			int counter = 0;
			for (Object object : visiblePropertyObject) {
				MetadataVO metadataVO = (MetadataVO) object;
				Object metadataValue = recordVO.get(metadataVO);
				String valueAsString = null;

			 	if (metadataVO != null && metadataVO.getLocalCode().equals(Event.NEGATIVE_AUTHORIZATION)) {
					valueAsString = EventViewImpl.negativeAuthorizationString((Boolean) metadataValue);
				}

				if (metadataValue != null) {
					valueAsString = metadataValue.toString().trim();

					if(metadataVO != null && metadataVO.getLocalCode().equals(Event.DELTA)) {
						if (removeTabAndNewLine) {
							valueAsString = valueAsString.replaceAll('\n' + "", " ");
							valueAsString = valueAsString.replaceAll('\t' + "", " ");
						}
						valueAsString = valueAsString.replaceAll("\\[" + "", "");
						valueAsString = valueAsString.replaceAll(']' + "", "");
					}

					if (metadataVO.getType() == MetadataValueType.STRING) {
						valueAsString = "\"" + valueAsString + "\"";
					}
				}

				stringArray[counter++] = valueAsString;
			}
			csvWriter.writeNext(stringArray);
		}
	}

	private RMSchemasRecordsServices rmSchemasRecordsServices() {
		if (rmSchemasRecordsServices == null) {
			rmSchemasRecordsServices = new RMSchemasRecordsServices(view.getCollection(), appLayerFactory);
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
			case EVENTS_BY_CONTAINER:
				return rmSchemasEventsServices()
						.newFindEventByDateRangeAndByContainerQuery(currentUser, eventType, startDate,
								endDate,
								id);
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
		view.navigate().to().showEventCategory(parameters);
	}

	public boolean isRecordIdMetadata(MetadataValueVO metadataValue) {
		return metadataValue.getMetadata().getCode().contains(Event.RECORD_ID) || metadataValue.getMetadata().getCode()
				.contains(CommonMetadataBuilder.ID);
	}

	public boolean isDeltaMetadata(MetadataValueVO metadataValue) {
		return metadataValue.getMetadata().getCode().contains(Event.DELTA);
	}

	public boolean isTypeMetadata(MetadataValueVO metadataValue) {
		return metadataValue.getMetadata().getCode().contains(Event.TYPE);
	}

	public boolean isNegativeAuthorizationMetadata(MetadataValueVO metadataValue) {
		return metadataValue.getMetadata().getCode().contains(Event.NEGATIVE_AUTHORIZATION);
	}

	public boolean isTaskMetadata(MetadataValueVO metadataValue) {
		return metadataValue.getMetadata().getCode().contains(Event.TASK);
	}

	public boolean isReceiverMetadata(MetadataValueVO metadataValue) {
		return metadataValue.getMetadata().getCode().contains(Event.RECEIVER_NAME);
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

	public RecordVO getLinkedRecordVO(RecordVO eventOrRecordVO) {
		RecordVO result;
		if (eventOrRecordVO.getSchema().getTypeCode().equals(Event.SCHEMA_TYPE)) {
			try {
				String recordId = eventOrRecordVO.get(Event.RECORD_ID);
				Record linkedRecord = recordServices().getDocumentById(recordId);
				RecordToVOBuilder voBuilder = new RecordToVOBuilder();
				result = voBuilder.build(linkedRecord, VIEW_MODE.TABLE, view.getSessionContext());
			} catch (Throwable t) {
				result = null;
			}
		} else {
			result = eventOrRecordVO;
		}
		return result;
	}

	public void recordLinkClicked(RecordVO eventVO) {
		Map<String, Object> params = new HashMap<>();

		if (EventCategory.CURRENTLY_BORROWED_DOCUMENTS.equals(eventCategory)) {
			Record linkedRecord = recordServices().getDocumentById(eventVO.getId());
			if (Boolean.TRUE.equals(linkedRecord.get(Schemas.LOGICALLY_DELETED_STATUS))) {
				String schemaTypeLabel = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection)
						.getSchemaType(linkedRecord.getTypeCode()).getLabel(Language.withLocale(view.getSessionContext().getCurrentLocale())).toLowerCase();
				params.put("schemaType", schemaTypeLabel);
				final String errorMessage = $("ReferenceDisplay.cannotDisplayLogicallyDeletedRecord", params);
				view.showErrorMessage(errorMessage);
				return;
			}
			try {
				view.navigate().to(RMViews.class).displayDocument(eventVO.getId());
			} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
				return;
			}
		} else {
			String eventId = eventVO.get(Event.RECORD_ID);
			try {
				Record linkedRecord = recordServices().getDocumentById(eventId);
				String linkedRecordId = linkedRecord.getId();

				if (Boolean.TRUE.equals(linkedRecord.get(Schemas.LOGICALLY_DELETED_STATUS))) {
					String schemaTypeLabel = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection)
							.getSchemaType(linkedRecord.getTypeCode()).getLabel(Language.withLocale(view.getSessionContext().getCurrentLocale())).toLowerCase();
					params.put("schemaType", schemaTypeLabel);
					final String errorMessage = $("ReferenceDisplay.cannotDisplayLogicallyDeletedRecord", params);
					view.showErrorMessage(errorMessage);
					return;
				}

				if (getEventType().contains(EventType.DECOMMISSIONING_LIST)) {
					view.navigate().to(RMViews.class).displayDecommissioningList(linkedRecordId);
				} else if (getEventType().contains("folder")) {
					view.navigate().to(RMViews.class).displayFolder(linkedRecordId);
				} else if (getEventType().contains("document") || EventType.PDF_A_GENERATION.equals(getEventType())) {
					view.navigate().to(RMViews.class).displayDocument(linkedRecordId);
				} else if (getEventType().contains("container")) {
					view.navigate().to(RMViews.class).displayContainer(linkedRecordId);
				} else {
					view.navigate().to(TaskViews.class).displayTask(linkedRecordId);
				}
			} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
				return;
			}
		}


	}

	private RecordToVOBuilder getRecordToVOBuilderToBorrowedFolders() {
		RecordToVOBuilder voBuilder;
		voBuilder = new RecordToVOBuilder() {
			transient RMSchemasRecordsServices schemas;

			@Override
			public RecordVO build(Record record, VIEW_MODE viewMode, MetadataSchemaVO schemaVO,
								  SessionContext sessionContext) {
				MetadataSchema folderSchema = schemas().folder.schema();
				Metadata borrowDateMetadata = folderSchema.getMetadata(Folder.BORROW_DATE);
				LocalDateTime eventTime = record.get(borrowDateMetadata);

				Metadata eventTypeMetadata = schema().getMetadata(Event.TYPE);
				Metadata eventMetaData = Schemas.CREATED_ON;
				Metadata recordIdMetadata = folderSchema.getMetadata(Schemas.IDENTIFIER.getCode());
				String recordId = record.getId();

				Metadata borrowedMetadata = folderSchema.getMetadata(Folder.BORROWED);
				LocalDateTime borrowDateValue = record.get(borrowDateMetadata);

				LogicalSearchCondition logicalSearchCondition = LogicalSearchQueryOperators.from(schemas().folder.schema())
						.where(borrowedMetadata).isTrue()
						.andWhere(borrowDateMetadata).isEqualTo(
								borrowDateValue).andWhere(recordIdMetadata).isEqualTo(recordId);

				SearchServices searchServices = modelLayerFactory.newSearchServices();
				Record eventRecord = searchServices.searchSingleResult(logicalSearchCondition);

				return super.build(eventRecord, viewMode, schemaVO, sessionContext);
			}

			private RMSchemasRecordsServices schemas() {
				if (schemas == null) {
					schemas = new RMSchemasRecordsServices(collection, appLayerFactory);
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

			private RMSchemasRecordsServices schemas() {
				if (schemas == null) {
					schemas = new RMSchemasRecordsServices(collection, appLayerFactory);
				}
				return schemas;
			}
		};
		return voBuilder;
	}

}
