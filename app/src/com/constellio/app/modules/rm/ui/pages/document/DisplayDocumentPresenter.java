package com.constellio.app.modules.rm.ui.pages.document;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.modules.rm.ui.builders.DocumentToVOBuilder;
import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentBreadcrumbTrail;
import com.constellio.app.modules.rm.ui.components.document.DocumentActionsPresenterUtils;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
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
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.FunctionLogicalSearchQuerySort;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQuerySort;
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
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public class DisplayDocumentPresenter extends SingleSchemaBasePresenter<DisplayDocumentView> {

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

	private String lastKnownContentVersionNumber;
	private String lastKnownCheckoutUserId;
	private Long lastKnownLength;
	private Document document;

	public DisplayDocumentPresenter(final DisplayDocumentView view, RecordVO recordVO, boolean popup) {
		super(view);
		presenterUtils = new DocumentActionsPresenterUtils<DisplayDocumentView>(view) {
			@Override
			public void updateActionsComponent() {
				super.updateActionsComponent();
				view.refreshMetadataDisplay();
				updateContentVersions();
			}
		};
		trashServices = new TrashServices(appLayerFactory.getModelLayerFactory(), collection);
		contentVersionVOBuilder = new ContentVersionToVOBuilder(modelLayerFactory);
		voBuilder = new DocumentToVOBuilder(modelLayerFactory);
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);

		if (recordVO != null) {
			forParams(recordVO.getId());
		}
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	public void forParams(String params) {
		String id = params;
		String taxonomyCode = view.getUIContext().getAttribute(FolderDocumentBreadcrumbTrail.TAXONOMY_CODE);
		view.setTaxonomyCode(taxonomyCode);

		this.record = getRecord(id);

		Record record = getRecord(id);
		document = rm.wrapDocument(record);
		hasWriteAccess = getCurrentUser().hasWriteAccess().on(record);

		final DocumentVO documentVO = voBuilder.build(record, VIEW_MODE.DISPLAY, view.getSessionContext());
		view.setDocumentVO(documentVO);
		presenterUtils.setRecordVO(documentVO);
		ModelLayerFactory modelLayerFactory = view.getConstellioFactories().getModelLayerFactory();
		User user = getCurrentUser();
		modelLayerFactory.newLoggingServices().logRecordView(record, user);

		MetadataSchemaVO tasksSchemaVO = schemaVOBuilder
				.build(getTasksSchema(), VIEW_MODE.TABLE, Arrays.asList(STARRED_BY_USERS), view.getSessionContext(), true);
		tasksDataProvider = new RecordVODataProvider(
				tasksSchemaVO, voBuilder, modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
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
		eventsDataProvider = getEventsDataProvider();

		ContentVersionVO contentVersionVO = documentVO.getContent();
		lastKnownContentVersionNumber = contentVersionVO != null ? contentVersionVO.getVersion() : null;
		lastKnownCheckoutUserId = contentVersionVO != null ? contentVersionVO.getCheckoutUserId() : null;
		lastKnownLength = contentVersionVO != null ? contentVersionVO.getLength() : null;
	}

	public int getTaskCount() {
		return tasksDataProvider.size();
	}

	public List<LabelTemplate> getDefaultTemplates() {
		return view.getConstellioFactories().getAppLayerFactory().getLabelTemplateManager().listTemplates(Document.SCHEMA_TYPE);
	}

	public List<LabelTemplate> getCustomTemplates() {
		return view.getConstellioFactories().getAppLayerFactory().getLabelTemplateManager().listExtensionTemplates(ContainerRecord.SCHEMA_TYPE);
	}

	public RecordVO getDocument() {
		return new RecordToVOBuilder()
				.build(document.getWrappedRecord(), VIEW_MODE.DISPLAY, view.getSessionContext());
	}

	public void backgroundViewMonitor() {
		clearRequestCache();
		DocumentVO documentVO = presenterUtils.getDocumentVO();
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
				view.setDocumentVO(documentVO);
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
		DocumentVO documentVO = presenterUtils.getDocumentVO();
		return Arrays.asList(documentVO.getId());
	}

	public void viewAssembled() {
		presenterUtils.updateActionsComponent();
		view.setTasks(tasksDataProvider);
		view.setEvents(eventsDataProvider);
		view.setPublishButtons(presenterUtils.isDocumentPublished());
	}

	public RecordVODataProvider getWorkflows() {
		MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder().build(
				schema(BetaWorkflow.DEFAULT_SCHEMA), VIEW_MODE.TABLE, view.getSessionContext());

		return new RecordVODataProvider(schemaVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return new BetaWorkflowServices(view.getCollection(), appLayerFactory).getWorkflowsQuery();
			}
		};
	}

	public void workflowStartRequested(RecordVO record) {
		Map<String, List<String>> parameters = new HashMap<>();
		parameters.put(RMTask.LINKED_DOCUMENTS, asList(presenterUtils.getDocumentVO().getId()));
		BetaWorkflow workflow = new TasksSchemasRecordsServices(view.getCollection(), appLayerFactory)
				.getBetaWorkflow(record.getId());
		new BetaWorkflowServices(view.getCollection(), appLayerFactory).start(workflow, getCurrentUser(), parameters);
	}

	public void updateContentVersions() {
		List<ContentVersionVO> contentVersionVOs = new ArrayList<ContentVersionVO>();
		DocumentVO documentVO = presenterUtils.getDocumentVO();
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

	public void editDocumentButtonClicked() {
		if (view.isInWindow()) {
			view.editInWindow();
		} else {
			presenterUtils.editDocumentButtonClicked();
		}
	}

	public void deleteDocumentButtonClicked() {
		presenterUtils.deleteDocumentButtonClicked();
	}

	public void linkToDocumentButtonClicked() {
		presenterUtils.linkToDocumentButtonClicked();
	}

	public void addAuthorizationButtonClicked() {
		presenterUtils.addAuthorizationButtonClicked();
	}

	public void shareDocumentButtonClicked() {
		presenterUtils.shareDocumentButtonClicked();
	}

	public void createPDFAButtonClicked() {
		if (!presenterUtils.getDocumentVO().getExtension().toUpperCase().equals("PDF") && !presenterUtils.getDocumentVO()
				.getExtension().toUpperCase().equals("PDFA")) {
			presenterUtils.createPDFA();
		} else {
			this.view.showErrorMessage($("DocumentActionsComponent.documentAllreadyPDFA"));
		}
	}

	public void uploadButtonClicked() {
		presenterUtils.uploadButtonClicked();
	}

	public void checkInButtonClicked() {
		presenterUtils.checkInButtonClicked();
	}

	public void alertWhenAvailableClicked() {
		presenterUtils.alertWhenAvailable();
	}

	public void checkOutButtonClicked() {
		presenterUtils.checkOutButtonClicked(view.getSessionContext());
	}

	public void finalizeButtonClicked() {
		presenterUtils.finalizeButtonClicked();
	}

	public void updateWindowClosed() {
		presenterUtils.updateWindowClosed();
	}

	public String getDocumentTitle() {
		DocumentVO documentVO = presenterUtils.getDocumentVO();
		return documentVO.getTitle();
	}

	public void copyContentButtonClicked() {
		presenterUtils.copyContentButtonClicked();
	}

	public String getContentTitle() {
		return presenterUtils.getContentTitle();
	}

	public void renameContentButtonClicked(String newContentTitle) {
		Document document = presenterUtils.renameContentButtonClicked(newContentTitle);
		if (document != null) {
			addOrUpdate(document.getWrappedRecord());
			view.navigate().to(RMViews.class).displayDocument(document.getId());
		}
	}

	public RecordVODataProvider getOwnedCartsDataProvider() {
		final MetadataSchemaVO cartSchemaVO = schemaVOBuilder.build(rm.cartSchema(), VIEW_MODE.TABLE, view.getSessionContext());
		return new RecordVODataProvider(cartSchemaVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(from(rm.cartSchema()).where(rm.cartOwner())
						.isEqualTo(getCurrentUser().getId())).sortAsc(Schemas.TITLE);
			}
		};
	}

	public RecordVODataProvider getSharedCartsDataProvider() {
		final MetadataSchemaVO cartSchemaVO = schemaVOBuilder.build(rm.cartSchema(), VIEW_MODE.TABLE, view.getSessionContext());
		return new RecordVODataProvider(cartSchemaVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(from(rm.cartSchema()).where(rm.cartSharedWithUsers())
						.isContaining(asList(getCurrentUser().getId()))).sortAsc(Schemas.TITLE);
			}
		};
	}

	public boolean hasContent() {
		return presenterUtils.hasContent();
	}

	public void taskClicked(RecordVO taskVO) {
		view.navigate().to(TaskViews.class).displayTask(taskVO.getId());
	}

	public void addToCartRequested(RecordVO recordVO) {
		presenterUtils.addToCartRequested(recordVO);
	}

	public InputStream getSignatureInputStream(String certificate, String password) {
		// TODO: Sign the file
		ContentVersionVO content = presenterUtils.getDocumentVO().getContent();
		return modelLayerFactory.getContentManager().getContentInputStream(content.getHash(), content.getFileName());
	}

	public void publishButtonClicked() {
		updateAndRefresh(presenterUtils.publishButtonClicked());
	}

	public boolean isLogicallyDeleted() {
		return document == null || document.isLogicallyDeletedStatus();
	}

	public void unpublishButtonClicked() {
		updateAndRefresh(presenterUtils.unpublishButtonClicked());
	}

	public String getPublicLink() {
		String url = modelLayerFactory.getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.CONSTELLIO_URL);
		return url + "dl?id=" + presenterUtils.getDocumentVO().getId();
	}

	private void updateAndRefresh(Document document) {
		if (document != null) {
			addOrUpdate(document.getWrappedRecord());
			view.navigate().to(RMViews.class).displayDocument(document.getId());
		}
	}

	public void createNewCartAndAddToItRequested(String title) {
		Cart cart = rm.newCart();
		cart.setTitle(title);
		cart.setOwner(getCurrentUser());
		try {
			cart.addDocuments(Arrays.asList(presenterUtils.getDocumentVO().getId()));
			recordServices().execute(new Transaction(cart.getWrappedRecord()).setUser(getCurrentUser()));
			view.showMessage($("DocumentActionsComponent.addedToCart"));
		} catch (RecordServicesException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public boolean hasWritePermission() {
		return hasWriteAccess;
	}

	public boolean hasCurrentUserPermissionToPublishOnCurrentDocument() {
		return getCurrentUser().has(RMPermissionsTo.PUBLISH_AND_UNPUBLISH_DOCUMENTS)
				.on(getRecord(presenterUtils.getDocumentVO().getId()));
	}

	public boolean hasCurrentUserPermissionToUseCart() {
		return getCurrentUser().has(RMPermissionsTo.USE_CART).globally();
	}

	public RecordVODataProvider getEventsDataProvider() {
		final RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		final MetadataSchemaVO eventSchemaVO = schemaVOBuilder
				.build(rm.eventSchema(), VIEW_MODE.TABLE, view.getSessionContext());
		return new RecordVODataProvider(eventSchemaVO, new EventToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				RMEventsSearchServices rmEventsSearchServices = new RMEventsSearchServices(modelLayerFactory, collection);
				LogicalSearchQuery query = rmEventsSearchServices
						.newFindEventByRecordIDQuery(getCurrentUser(), presenterUtils.getDocumentVO().getId());
				return query == null ? null : rmEventsSearchServices.exceptEventTypes(query,
						asList(EventType.OPEN_DOCUMENT, EventType.DOWNLOAD_DOCUMENT, EventType.UPLOAD_DOCUMENT,
								EventType.SHARE_DOCUMENT, EventType.FINALIZE_DOCUMENT));
			}
		};
	}

	protected boolean hasCurrentUserPermissionToViewEvents() {
		return getCurrentUser().has(CorePermissions.VIEW_EVENTS).on(getRecord(presenterUtils.getDocumentVO().getId()));
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

	private void addStarredSortToQuery(LogicalSearchQuery query) {
		Metadata metadata = types().getSchema(Task.DEFAULT_SCHEMA).getMetadata(STARRED_BY_USERS);
		LogicalSearchQuerySort sortField = new FunctionLogicalSearchQuerySort(
				"termfreq(" + metadata.getDataStoreCode() + ",\'" + getCurrentUser().getId() + "\')", false);
		query.sortFirstOn(sortField);
	}
}
