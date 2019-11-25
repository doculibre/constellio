package com.constellio.app.modules.rm.ui.pages.document;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.modules.rm.ui.builders.DocumentToVOBuilder;
import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentContainerBreadcrumbTrail;
import com.constellio.app.modules.rm.ui.components.document.DocumentActionsPresenterUtils;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.ui.pages.extrabehavior.SecurityWithNoUrlParamSupport;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.modules.rm.util.RMNavigationUtils;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.tasks.TasksPermissionsTo;
import com.constellio.app.modules.tasks.model.wrappers.BetaWorkflow;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.navigation.TaskViews;
import com.constellio.app.modules.tasks.services.BetaWorkflowServices;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.ContentVersionToVOBuilder;
import com.constellio.app.ui.framework.builders.EventToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.FunctionLogicalSearchQuerySort;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQuerySort;
import com.constellio.model.services.search.query.logical.QueryExecutionMethod;
import com.constellio.model.services.trash.TrashServices;
import com.vaadin.ui.Button;
import org.apache.commons.lang3.ObjectUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.modules.tasks.model.wrappers.Task.STARRED_BY_USERS;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public class DisplayDocumentPresenter extends SingleSchemaBasePresenter<DisplayDocumentView> implements SecurityWithNoUrlParamSupport {
	private transient RecordServices recordServices;

	protected DocumentToVOBuilder voBuilder;
	protected ContentVersionToVOBuilder contentVersionVOBuilder;
	protected DocumentActionsPresenterUtils<DisplayDocumentView> presenterUtils;
	private MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder();
	private RecordVODataProvider tasksDataProvider;
	private RecordVODataProvider eventsDataProvider;
	private RMSchemasRecordsServices rm;
	private boolean hasWriteAccess;
	private TrashServices trashServices;
	private Record record;
	private MetadataSchemaVO tasksSchemaVO;

	private String lastKnownContentVersionNumber;
	private String lastKnownCheckoutUserId;
	private Long lastKnownLength;
	private Document document;
	private DocumentVO documentVO;
	private Map<String, String> params = null;
	private boolean nestedView;
	private boolean inWindow;


	public DisplayDocumentPresenter(final DisplayDocumentView view, RecordVO recordVO, final boolean nestedView,
									final boolean inWindow) {
		super(view);
		this.nestedView = nestedView;
		this.inWindow = inWindow;
		initTransientObjects();
		presenterUtils = new DocumentActionsPresenterUtils<DisplayDocumentView>(view) {
			@Override
			public void updateActionsComponent() {
				super.updateActionsComponent();
				if (nestedView) {
					if (inWindow) {
						view.setDisplayDocumentButtonState(ComponentState.INVISIBLE);
					} else {
						view.setDisplayDocumentButtonState(ComponentState.ENABLED);
					}
				} else {
					view.setDisplayDocumentButtonState(ComponentState.INVISIBLE);
				}
				Content content = getContent();
				if (content != null) {
					ContentVersionVO contentVersionVO = contentVersionVOBuilder.build(content);
					view.setDownloadDocumentButtonState(ComponentState.ENABLED);
					String agentURL = ConstellioAgentUtils.getAgentURL(documentVO, contentVersionVO);
					view.setOpenDocumentButtonState(agentURL != null ? ComponentState.ENABLED : ComponentState.INVISIBLE);
				} else {
					view.setDownloadDocumentButtonState(ComponentState.INVISIBLE);
					view.setOpenDocumentButtonState(ComponentState.INVISIBLE);
				}
				view.refreshMetadataDisplay();
				updateContentVersions();
			}
		};
		trashServices = new TrashServices(appLayerFactory.getModelLayerFactory(), collection);
		contentVersionVOBuilder = new ContentVersionToVOBuilder(modelLayerFactory);
		voBuilder = new DocumentToVOBuilder(modelLayerFactory);
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		if (recordVO != null && params == null) {
			forParams(recordVO.getId());
		}
	}

	public boolean userHasPermissionToEditOtherUserAnnotation() {
		return getCurrentUser().has(RMPermissionsTo.EDIT_ALL_ANNOTATION).on(documentVO.getRecord());
	}

	public String getFavGroupId() {
		if (params != null) {
			return params.get(RMViews.FAV_GROUP_ID_KEY);
		} else {
			return null;
		}
	}

	public boolean isInWindow() {
		return inWindow;
	}

	private void initTransientObjects() {
		recordServices = modelLayerFactory.newRecordServices();
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public Record getRecord() {
		return record;
	}

	public boolean hasPageAccess(User user) {
		if (record == null) {
			return false;
		}

		if (!hasPageAccess(record.getId(), user)) {
			return false;
		} else {
			return hasRestrictedRecordAccess(record.getId(), user, record);
		}
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	public Map<String, String> getParams() {
		return params;
	}

	private String extractIdFromParams(String params) {
		if (params.contains("id")) {
			this.params = ParamUtils.getParamsMap(params);
			return this.params.get("id");
		} else {
			return params;
		}
	}

	public void forParams(String params) {
		String id = extractIdFromParams(params);

		view.getSessionContext().addVisited(id);

		String taxonomyCode = view.getUIContext().getAttribute(FolderDocumentContainerBreadcrumbTrail.TAXONOMY_CODE);
		view.setTaxonomyCode(taxonomyCode);

		this.record = getRecord(id);

		Record record = getRecord(id);
		document = rm.wrapDocument(record);
		hasWriteAccess = getCurrentUser().hasWriteAccess().on(record);

		documentVO = voBuilder.build(record, VIEW_MODE.DISPLAY, view.getSessionContext());
		view.setRecordVO(documentVO);
		presenterUtils.setRecordVO(documentVO);
		ModelLayerFactory modelLayerFactory = view.getConstellioFactories().getModelLayerFactory();
		User user = getCurrentUser();
		modelLayerFactory.newLoggingServices().logRecordView(record, user);

		tasksSchemaVO = schemaVOBuilder.build(getTasksSchema(), VIEW_MODE.TABLE, Arrays.asList(STARRED_BY_USERS), view.getSessionContext(), true);
		tasksDataProvider = new RecordVODataProvider(
				tasksSchemaVO, voBuilder, modelLayerFactory, view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				TasksSchemasRecordsServices tasks = new TasksSchemasRecordsServices(collection, appLayerFactory);
				Metadata taskDocumentMetadata = tasks.userTask.schema().getMetadata(RMTask.LINKED_DOCUMENTS);
				LogicalSearchQuery query = new LogicalSearchQuery();
				query.setCondition(from(tasks.userTask.schemaType()).where(taskDocumentMetadata).is(documentVO.getId()));
				query.filteredByStatus(StatusFilter.ACTIVES);
				query.filteredWithUser(getCurrentUser());
				addStarredSortToQuery(query);
				query.sortDesc(Schemas.MODIFIED_ON);
				return query;
			}

			@Override
			protected void clearSort(LogicalSearchQuery query) {
				super.clearSort(query);
				addStarredSortToQuery(query);
			}
		};

		ContentVersionVO contentVersionVO = documentVO.getContent();
		lastKnownContentVersionNumber = contentVersionVO != null ? contentVersionVO.getVersion() : null;
		lastKnownCheckoutUserId = contentVersionVO != null ? contentVersionVO.getCheckoutUserId() : null;
		lastKnownLength = contentVersionVO != null ? contentVersionVO.getLength() : null;
	}

	public int getTaskCount() {
		LogicalSearchQuery query = new LogicalSearchQuery(tasksDataProvider.getQuery());
		query.setQueryExecutionMethod(QueryExecutionMethod.USE_CACHE);
		return (int) searchServices().getResultsCount(query);
	}

	public List<LabelTemplate> getDefaultTemplates() {
		return view.getConstellioFactories().getAppLayerFactory().getLabelTemplateManager().listTemplates(Document.SCHEMA_TYPE);
	}

	public List<LabelTemplate> getCustomTemplates() {
		return view.getConstellioFactories().getAppLayerFactory().getLabelTemplateManager().listExtensionTemplates(Document.SCHEMA_TYPE);
	}

	public RecordVO getDocumentVO() {
		return new RecordToVOBuilder()
				.build(document.getWrappedRecord(), VIEW_MODE.DISPLAY, view.getSessionContext());
	}

	public void backgroundViewMonitor() {
		clearRequestCache();
		DocumentVO documentVO = presenterUtils.getRecordVO();
		try {
			ContentVersionVO contentVersionVO = documentVO.getContent();
			Record currentRecord = getRecord(documentVO.getId());
			Document currentDocument = new Document(currentRecord, types());
			Content currentContent = currentDocument.getContent();
			ContentVersion currentContentVersion =
					currentContent != null ? currentContent.getCurrentVersionSeenBy(getCurrentUser()) : null;
			String currentContentVersionNumber = currentContentVersion != null ? currentContentVersion.getVersion() : null;
			String currentCheckoutUserId = currentContent != null ? currentContent.getCheckoutUserId() : null;
			Long currentLength = currentContentVersion != null ? currentContentVersion.getLength() : null;
			if (ObjectUtils.notEqual(lastKnownContentVersionNumber, currentContentVersionNumber)
				|| ObjectUtils.notEqual(lastKnownCheckoutUserId, currentCheckoutUserId)
				|| ObjectUtils.notEqual(lastKnownLength, currentLength)) {
				documentVO = voBuilder.build(currentRecord, VIEW_MODE.DISPLAY, view.getSessionContext());
				view.setRecordVO(documentVO);
				presenterUtils.setRecordVO(documentVO);
				presenterUtils.updateActionsComponent();
				if ((lastKnownCheckoutUserId != null && currentCheckoutUserId == null)
					|| ObjectUtils.notEqual(lastKnownLength, currentLength)) {
					view.refreshContentViewer();
				}
			}

			contentVersionVO = documentVO.getContent();
			lastKnownContentVersionNumber = contentVersionVO != null ? contentVersionVO.getVersion() : null;
			lastKnownCheckoutUserId = contentVersionVO != null ? contentVersionVO.getCheckoutUserId() : null;
			lastKnownLength = contentVersionVO != null ? contentVersionVO.getLength() : null;
		} catch (NoSuchRecordWithId e) {
			view.invalidate();
		}
	}

	@Override
	protected boolean hasRestrictedRecordAccess(String params, User user, Record restrictedRecord) {
		return user.hasReadAccess().on(restrictedRecord);
	}

	private MetadataSchema getTasksSchema() {
		return schema(Task.DEFAULT_SCHEMA);
	}

	@Override
	protected List<String> getRestrictedRecordIds(String params) {
		DocumentVO documentVO = presenterUtils.getRecordVO();
		return Arrays.asList(documentVO == null ? extractIdFromParams(params) : documentVO.getId());
	}

	public void viewAssembled() {
		presenterUtils.updateActionsComponent();
		view.setTasks(tasksDataProvider);
		view.setPublishButtons(presenterUtils.isDocumentPublished());
	}

	public RecordVODataProvider getWorkflows() {
		MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder().build(
				schema(BetaWorkflow.DEFAULT_SCHEMA), VIEW_MODE.TABLE, view.getSessionContext());

		return new RecordVODataProvider(schemaVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				return new BetaWorkflowServices(view.getCollection(), appLayerFactory).getWorkflowsQuery();
			}
		};
	}

	public void workflowStartRequested(RecordVO record) {
		Map<String, List<String>> parameters = new HashMap<>();
		parameters.put(RMTask.LINKED_DOCUMENTS, asList(presenterUtils.getRecordVO().getId()));
		BetaWorkflow workflow = new TasksSchemasRecordsServices(view.getCollection(), appLayerFactory)
				.getBetaWorkflow(record.getId());
		new BetaWorkflowServices(view.getCollection(), appLayerFactory).start(workflow, getCurrentUser(), parameters);
	}

	public void updateContentVersions() {
		List<ContentVersionVO> contentVersionVOs = new ArrayList<ContentVersionVO>();
		DocumentVO documentVO = presenterUtils.getRecordVO();
		Record record = getRecord(documentVO.getId());
		Document document = new Document(record, types());

		Content content = document.getContent();
		if (content != null) {
			for (ContentVersion contentVersion : content.getHistoryVersions()) {
				ContentVersionVO contentVersionVO = contentVersionVOBuilder.build(content, contentVersion);
				contentVersionVOs.add(contentVersionVO);
			}
			ContentVersion currentVersion = content.getCurrentVersionSeenBy(getCurrentUser());
			ContentVersionVO currentVersionVO = contentVersionVOBuilder.build(content, currentVersion);
			contentVersionVOs.remove(currentVersionVO);
			contentVersionVOs.add(currentVersionVO);
		}
		Collections.reverse(contentVersionVOs);
		view.setContentVersions(contentVersionVOs);
	}

	public void backButtonClicked() {
		view.navigate().to().previousView();
	}

	public boolean isDeleteContentVersionPossible() {
		return presenterUtils.isDeleteContentVersionPossible();
	}

	public boolean isDeleteContentVersionPossible(ContentVersionVO contentVersionVO) {
		return presenterUtils.isDeleteContentVersionPossible(contentVersionVO);
	}

	public void deleteContentVersionButtonClicked(ContentVersionVO contentVersionVO) {
		presenterUtils.deleteContentVersionButtonClicked(contentVersionVO);
	}

	public void displayDocumentButtonClicked() {
		if (view.isInWindow() || nestedView) {
			view.openInWindow();
		} else {
			view.navigate().to(RMViews.class).displayDocument(documentVO.getId());
		}
	}

	public void openDocumentButtonClicked() {
		String agentURL = ConstellioAgentUtils.getAgentURL(documentVO, documentVO.getContent());
		view.openAgentURL(agentURL);
	}

	public void editDocumentButtonClicked() {
		if (view.isInWindow() || nestedView) {
			view.editInWindow();
		} else {
			presenterUtils.editDocumentButtonClicked(params);
		}
	}

	public void updateWindowClosed() {
		presenterUtils.updateWindowClosed();
	}

	public String getDocumentTitle() {
		DocumentVO documentVO = presenterUtils.getRecordVO();
		return documentVO.getTitle();
	}

	public void copyContentButtonClicked() {
		presenterUtils.copyContentButtonClicked(params);
	}

	public String getContentTitle() {
		return presenterUtils.getContentTitle();
	}

	public boolean hasContent() {
		return presenterUtils.hasContent();
	}

	public void taskClicked(RecordVO taskVO) {
		view.navigate().to(TaskViews.class).displayTask(taskVO.getId());
	}

	public InputStream getSignatureInputStream(String certificate, String password) {
		// TODO: Sign the file
		ContentVersionVO content = presenterUtils.getRecordVO().getContent();
		return modelLayerFactory.getContentManager().getContentInputStream(content.getHash(), content.getFileName());
	}

	public boolean isLogicallyDeleted() {
		return document == null || document.isLogicallyDeletedStatus();
	}

	public String getPublicLink() {
		String url = modelLayerFactory.getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.CONSTELLIO_URL);
		return url + "dl?id=" + presenterUtils.getRecordVO().getId();
	}

	private void updateAndRefresh(Document document) {
		if (document != null) {
			addOrUpdate(document.getWrappedRecord());
			RMNavigationUtils.navigateToDisplayDocument(document.getId(), params, appLayerFactory, collection);
		}
	}

	public boolean hasWritePermission() {
		return hasWriteAccess;
	}

	public boolean hasCurrentUserPermissionToPublishOnCurrentDocument() {
		return getCurrentUser().has(RMPermissionsTo.PUBLISH_AND_UNPUBLISH_DOCUMENTS)
				.on(getRecord(presenterUtils.getRecordVO().getId()));
	}

	public boolean hasCurrentUserPermissionToUseCartGroup() {
		return getCurrentUser().has(RMPermissionsTo.USE_GROUP_CART).globally();
	}


	public boolean hasCurrentUserPermissionToUseMyCart() {
		return getCurrentUser().has(RMPermissionsTo.USE_MY_CART).globally();
	}

	public RecordVODataProvider getEventsDataProvider() {
		final RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		final MetadataSchemaVO eventSchemaVO = schemaVOBuilder
				.build(rm.eventSchema(), VIEW_MODE.TABLE, view.getSessionContext());
		return new RecordVODataProvider(eventSchemaVO, new EventToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				RMEventsSearchServices rmEventsSearchServices = new RMEventsSearchServices(modelLayerFactory, collection);
				LogicalSearchQuery query = rmEventsSearchServices
						.newFindEventByRecordIDQuery(getCurrentUser(), presenterUtils.getRecordVO().getId());
				return query == null ? null : rmEventsSearchServices.exceptEventTypes(query,
						asList(EventType.OPEN_DOCUMENT, EventType.DOWNLOAD_DOCUMENT, EventType.UPLOAD_DOCUMENT,
								EventType.SHARE_DOCUMENT, EventType.FINALIZE_DOCUMENT));
			}
		};
	}

	protected boolean hasCurrentUserPermissionToViewEvents() {
		return getCurrentUser().has(CorePermissions.VIEW_EVENTS).on(getRecord(presenterUtils.getRecordVO().getId()));
	}

	protected boolean hasCurrentUserPermissionToViewFileSystemName() {
		return getCurrentUser().has(RMPermissionsTo.VIEW_SYSTEM_FILENAME).globally();
	}

	public void refreshEvents() {
		//modelLayerFactory.getDataLayerFactory().newEventsDao().flush();
		view.setEvents(getEventsDataProvider());
	}

	public boolean hasPermissionToStartWorkflow() {
		return getCurrentUser().has(TasksPermissionsTo.START_WORKFLOWS).globally();
	}

	public AppLayerFactory getAppLayerFactory() {
		return appLayerFactory;
	}

	public void updateTaskStarred(boolean isStarred, String taskId, RecordVODataProvider dataProvider) {
		TasksSchemasRecordsServices taskSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
		Task task = taskSchemas.getTask(taskId);
		if (isStarred) {
			task.addStarredBy(getCurrentUser().getId());
		} else {
			task.removeStarredBy(getCurrentUser().getId());
		}
		try {
			recordServices().update(task);
		} catch (RecordServicesException e) {
			e.printStackTrace();
		}
		dataProvider.fireDataRefreshEvent();
	}

	public List<Button> getButtonsFromExtension() {
		return appLayerFactory.getExtensions().forCollection(collection)
				.getDocumentViewButtonExtension(this.record, getCurrentUser());
	}

	public void navigateToSelf() {
		RMNavigationUtils.navigateToDisplayDocument(this.record.getId(), params, appLayerFactory, collection);
	}

	private void addStarredSortToQuery(LogicalSearchQuery query) {
		Metadata metadata = types().getSchema(Task.DEFAULT_SCHEMA).getMetadata(STARRED_BY_USERS);
		LogicalSearchQuerySort sortField = new FunctionLogicalSearchQuerySort(
				"termfreq(" + metadata.getDataStoreCode() + ",\'" + getCurrentUser().getId() + "\')", false);
		query.sortFirstOn(sortField);
	}

	public AppLayerFactory getApplayerFactory() {
		return appLayerFactory;
	}

	public MetadataSchemaVO getSchema() {
		return new MetadataSchemaToVOBuilder().build(schema(Cart.DEFAULT_SCHEMA), RecordVO.VIEW_MODE.TABLE, view.getSessionContext());
	}

	public void tasksTabSelected() {
		view.setTasks(tasksDataProvider);
	}

	public void eventsTabSelected() {
		view.setEvents(getEventsDataProvider());
	}
}
