package com.constellio.app.modules.rm.ui.pages.document;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import org.apache.commons.lang3.ObjectUtils;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.builders.DocumentToVOBuilder;
import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentBreadcrumbTrail;
import com.constellio.app.modules.rm.ui.components.document.DocumentActionsPresenterUtils;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.Workflow;
import com.constellio.app.modules.tasks.navigation.TaskViews;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.modules.tasks.services.WorkflowServices;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.ContentVersionToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class DisplayDocumentPresenter extends SingleSchemaBasePresenter<DisplayDocumentView> {

	protected DocumentToVOBuilder voBuilder;
	protected ContentVersionToVOBuilder contentVersionVOBuilder;
	protected DocumentActionsPresenterUtils<DisplayDocumentView> presenterUtils;
	private MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder();
	private RecordVODataProvider tasksDataProvider;
	private RMSchemasRecordsServices rm;

	public DisplayDocumentPresenter(final DisplayDocumentView view) {
		super(view);
		presenterUtils = new DocumentActionsPresenterUtils<DisplayDocumentView>(view) {
			@Override
			public void updateActionsComponent() {
				super.updateActionsComponent();
				view.refreshMetadataDisplay();
				updateContentVersions();
			}
		};
		contentVersionVOBuilder = new ContentVersionToVOBuilder(modelLayerFactory);
		voBuilder = new DocumentToVOBuilder(modelLayerFactory);
		rm = new RMSchemasRecordsServices(collection,appLayerFactory);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	public void forParams(String params) {
		String id = params;
		String taxonomyCode = view.getUIContext().getAttribute(FolderDocumentBreadcrumbTrail.TAXONOMY_CODE);
		view.setTaxonomyCode(taxonomyCode);
		
		Record record = getRecord(id);
		final DocumentVO documentVO = voBuilder.build(record, VIEW_MODE.DISPLAY, view.getSessionContext());
		view.setDocumentVO(documentVO);
		presenterUtils.setRecordVO(documentVO);
		ModelLayerFactory modelLayerFactory = view.getConstellioFactories().getModelLayerFactory();
		User user = getCurrentUser();
		modelLayerFactory.newLoggingServices().logRecordView(record, user);

		MetadataSchemaVO tasksSchemaVO = schemaVOBuilder.build(getTasksSchema(), VIEW_MODE.TABLE, view.getSessionContext());
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
				query.sortDesc(Schemas.MODIFIED_ON);
				return query;
			}
		};
	}

	public int getTaskCount() {
		return tasksDataProvider.size();
	}

	public void backgroundViewMonitor() {
		DocumentVO documentVO = presenterUtils.getDocumentVO();
		try {
			ContentVersionVO contentVersionVO = documentVO.getContent();
			String contentVersionNumber = contentVersionVO != null ? contentVersionVO.getVersion() : null;
			String checkoutUserId = contentVersionVO != null ? contentVersionVO.getCheckoutUserId() : null;
			Long length = contentVersionVO != null ? contentVersionVO.getLength() : null;
			Record currentRecord = getRecord(documentVO.getId());
			Document currentDocument = new Document(currentRecord, types());
			Content currentContent = currentDocument.getContent();
			ContentVersion currentContentVersion =
					currentContent != null ? currentContent.getCurrentVersionSeenBy(getCurrentUser()) : null;
			String currentContentVersionNumber = currentContentVersion != null ? currentContentVersion.getVersion() : null;
			String currentCheckoutUserId = currentContent != null ? currentContent.getCheckoutUserId() : null;
			Long currentLength = currentContentVersion != null ? currentContentVersion.getLength() : null;
			if (ObjectUtils.notEqual(contentVersionNumber, currentContentVersionNumber)
					|| ObjectUtils.notEqual(checkoutUserId, currentCheckoutUserId)
					|| ObjectUtils.notEqual(length, currentLength)) {
				documentVO = voBuilder.build(currentRecord, VIEW_MODE.DISPLAY, view.getSessionContext());
				view.setDocumentVO(documentVO);
				presenterUtils.setRecordVO(documentVO);
				presenterUtils.updateActionsComponent();
			}
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
		view.setPublishButtons(presenterUtils.isDocumentPublished());
	}

	public RecordVODataProvider getWorkflows() {
		MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder().build(
				schema(Workflow.DEFAULT_SCHEMA), VIEW_MODE.TABLE, view.getSessionContext());

		return new RecordVODataProvider(schemaVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return new WorkflowServices(view.getCollection(), appLayerFactory).getWorkflowsQuery();
			}
		};
	}

	public void workflowStartRequested(RecordVO record) {
		Map<String, List<String>> parameters = new HashMap<>();
		parameters.put(RMTask.LINKED_DOCUMENTS, asList(presenterUtils.getDocumentVO().getId()));
		Workflow workflow = new TasksSchemasRecordsServices(view.getCollection(), appLayerFactory).getWorkflow(record.getId());
		new WorkflowServices(view.getCollection(), appLayerFactory).start(workflow, getCurrentUser(), parameters);
	}

	private void updateContentVersions() {
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
		view.navigateTo().previousView();
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
		presenterUtils.editDocumentButtonClicked();
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
		presenterUtils.createPDFA();

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
		}
	}

	public boolean hasCurrentUserPermissionToUseCart() {
		return getCurrentUser().has(RMPermissionsTo.USE_CART).globally();
	}
}
