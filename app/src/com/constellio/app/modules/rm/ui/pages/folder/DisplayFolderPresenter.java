package com.constellio.app.modules.rm.ui.pages.folder;

import com.constellio.app.api.extensions.taxonomies.FolderDeletionEvent;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.enums.DefaultTabInFolderDisplay;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingType;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.modules.rm.ui.builders.DocumentToVOBuilder;
import com.constellio.app.modules.rm.ui.builders.FolderToVOBuilder;
import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentBreadcrumbTrail;
import com.constellio.app.modules.rm.ui.components.content.ConstellioAgentClickHandler;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.ui.entities.FolderVO;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.modules.rm.wrappers.*;
import com.constellio.app.modules.tasks.TasksPermissionsTo;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.Workflow;
import com.constellio.app.modules.tasks.navigation.TaskViews;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.modules.tasks.services.WorkflowServices;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.ContentVersionVO.InputStreamProvider;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.EventToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.*;
import com.constellio.model.entities.structures.EmailAddress;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.contents.ContentFactory;
import com.constellio.model.services.contents.icap.IcapException;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.security.AuthorizationsServices;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class DisplayFolderPresenter extends SingleSchemaBasePresenter<DisplayFolderView> {

	private static Logger LOGGER = LoggerFactory.getLogger(DisplayFolderPresenter.class);
	private RecordVODataProvider documentsDataProvider;
	private RecordVODataProvider tasksDataProvider;
	private RecordVODataProvider subFoldersDataProvider;
	private RecordVODataProvider eventsDataProvider;
	private MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder();
	private FolderToVOBuilder folderVOBuilder;
	private DocumentToVOBuilder documentVOBuilder;
	private SchemaPresenterUtils schemaPresenterUtils;
	private FolderVO folderVO;

	private transient RMConfigs rmConfigs;
	private transient RMSchemasRecordsServices rmSchemasRecordsServices;
	private transient BorrowingServices borrowingServices;
	private transient MetadataSchemasManager metadataSchemasManager;
	private transient RecordServices recordServices;
	private transient ModelLayerCollectionExtensions extensions;

	Boolean allItemsSelected = false;

	Boolean allItemsDeselected = false;

	public DisplayFolderPresenter(DisplayFolderView view) {
		super(view, Folder.DEFAULT_SCHEMA);

		ConstellioFactories constellioFactories = view.getConstellioFactories();
		SessionContext sessionContext = view.getSessionContext();
		schemaPresenterUtils = new SchemaPresenterUtils(Folder.DEFAULT_SCHEMA, constellioFactories, sessionContext);
		initTransientObjects();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, appLayerFactory);
		borrowingServices = new BorrowingServices(collection, modelLayerFactory);
		folderVOBuilder = new FolderToVOBuilder();
		documentVOBuilder = new DocumentToVOBuilder(modelLayerFactory);
		metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		recordServices = modelLayerFactory.newRecordServices();
		extensions = modelLayerFactory.getExtensions().forCollection(collection);
		rmConfigs = new RMConfigs(modelLayerFactory.getSystemConfigurationsManager());
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
		this.folderVO = folderVOBuilder.build(record, VIEW_MODE.DISPLAY, view.getSessionContext());
		setSchemaCode(record.getSchemaCode());
		view.setRecord(folderVO);

		MetadataSchema documentsSchema = getDocumentsSchema();
		MetadataSchemaVO documentsSchemaVO = schemaVOBuilder.build(documentsSchema, VIEW_MODE.TABLE, view.getSessionContext());
		documentsDataProvider = new RecordVODataProvider(
				documentsSchemaVO, documentVOBuilder, modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return getDocumentsQuery();
			}
		};

		MetadataSchemaVO foldersSchemaVO = schemaVOBuilder.build(defaultSchema(), VIEW_MODE.TABLE, view.getSessionContext());
		subFoldersDataProvider = new RecordVODataProvider(
				foldersSchemaVO, folderVOBuilder, modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return getSubFoldersQuery();
			}
		};

		MetadataSchemaVO tasksSchemaVO = schemaVOBuilder.build(getTasksSchema(), VIEW_MODE.TABLE, view.getSessionContext());
		tasksDataProvider = new RecordVODataProvider(
				tasksSchemaVO, folderVOBuilder, modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return getTasksQuery();
			}
		};

		eventsDataProvider = getEventsDataProvider();

		computeAllItemsSelected();
	}

	private LogicalSearchQuery getDocumentsQuery() {
		Record record = getRecord(folderVO.getId());
		MetadataSchemaType documentsSchemaType = getDocumentsSchemaType();
		MetadataSchema documentsSchema = getDocumentsSchema();
		Metadata folderMetadata = documentsSchema.getMetadata(Document.FOLDER);
		LogicalSearchQuery query = new LogicalSearchQuery();

		LogicalSearchCondition condition = from(documentsSchemaType).where(folderMetadata).is(record);
		query.setCondition(condition);
		query.filteredWithUser(getCurrentUser());
		query.filteredByStatus(StatusFilter.ACTIVES);
		query.sortAsc(Schemas.TITLE);
		return query;
	}

	private LogicalSearchQuery getSubFoldersQuery() {
		Record record = getRecord(folderVO.getId());
		MetadataSchemaType foldersSchemaType = getFoldersSchemaType();
		MetadataSchema foldersSchema = getFoldersSchema();
		Metadata parentFolderMetadata = foldersSchema.getMetadata(Folder.PARENT_FOLDER);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(from(foldersSchemaType).where(parentFolderMetadata).is(record));
		query.filteredWithUser(getCurrentUser());
		query.filteredByStatus(StatusFilter.ACTIVES);
		query.sortAsc(Schemas.TITLE);
		return query;
	}

	private LogicalSearchQuery getTasksQuery() {
		TasksSchemasRecordsServices tasks = new TasksSchemasRecordsServices(collection, appLayerFactory);
		Metadata taskFolderMetadata = tasks.userTask.schema().getMetadata(RMTask.LINKED_FOLDERS);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(from(tasks.userTask.schemaType()).where(taskFolderMetadata).is(folderVO.getId()));
		query.filteredByStatus(StatusFilter.ACTIVES);
		query.filteredWithUser(getCurrentUser());
		query.sortDesc(Schemas.MODIFIED_ON);
		return query;
	}

	public void selectInitialTabForUser() {
		SystemConfigurationsManager systemConfigurationsManager = modelLayerFactory.getSystemConfigurationsManager();
		RMConfigs rmConfigs = new RMConfigs(systemConfigurationsManager);

		String userDefaultTabInFolderDisplayCode = getCurrentUser().getDefaultTabInFolderDisplay();
		String configDefaultTabInFolderDisplayCode = rmConfigs.getDefaultTabInFolderDisplay();
		String defaultTabInFolderDisplayCode = StringUtils.isNotBlank(userDefaultTabInFolderDisplayCode) ?
				userDefaultTabInFolderDisplayCode :
				configDefaultTabInFolderDisplayCode;
		if (isNotBlank(defaultTabInFolderDisplayCode)) {
			if (DefaultTabInFolderDisplay.METADATA.getCode().equals(defaultTabInFolderDisplayCode)) {
				view.selectMetadataTab();
			} else if (DefaultTabInFolderDisplay.CONTENT.getCode().equals(defaultTabInFolderDisplayCode)) {
				view.selectFolderContentTab();
			}
		}
	}

	public int getFolderContentCount() {
		return subFoldersDataProvider.size() + documentsDataProvider.size();
	}

	public int getTaskCount() {
		return tasksDataProvider.size();
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
		parameters.put(RMTask.LINKED_FOLDERS, asList(folderVO.getId()));
		Workflow workflow = new TasksSchemasRecordsServices(view.getCollection(), appLayerFactory).getWorkflow(record.getId());
		new WorkflowServices(view.getCollection(), appLayerFactory).start(workflow, getCurrentUser(), parameters);
	}

	@Override
	protected boolean hasRestrictedRecordAccess(String params, User user, Record restrictedRecord) {
		return user.hasReadAccess().on(restrictedRecord);
	}

	@Override
	protected List<String> getRestrictedRecordIds(String params) {
		return asList(folderVO.getId());
	}

	private void disableMenuItems(Folder folder) {

		RMConfigs rmConfigs = new RMConfigs(modelLayerFactory.getSystemConfigurationsManager());

		User user = getCurrentUser();
		view.setLogicallyDeletable(getDeleteButtonState(user, folder));
		view.setEditButtonState(getEditButtonState(user, folder));
		view.setMoveInFolderState(getMoveInFolderButtonState(user, folder));
		view.setAddSubFolderButtonState(getAddFolderButtonState(user, folder));
		view.setAddDocumentButtonState(getAddDocumentButtonState(user, folder));
		view.setDuplicateFolderButtonState(getDuplicateFolderButtonState(user, folder));
		view.setAuthorizationButtonState(getAuthorizationButtonState(user, folder));
		view.setShareFolderButtonState(getShareButtonState(user, folder));
		view.setPrintButtonState(getPrintButtonState(user, folder));
		view.setBorrowButtonState(getBorrowButtonState(user, folder));
		view.setReturnFolderButtonState(getReturnFolderButtonState(user, folder));
		view.setReminderReturnFolderButtonState(getReminderReturnFolderButtonState(user, folder));
		view.setAlertWhenAvailableButtonState(getAlertWhenAvailableButtonState(user, folder));
		view.setBorrowedMessage(getBorrowMessageState(folder));
		view.setStartWorkflowButtonState(ComponentState.visibleIf(rmConfigs.areWorkflowsEnabled()));
	}

	String getBorrowMessageState(Folder folder) {
		String borrowedMessage = null;
		if (folder.getBorrowed() != null && folder.getBorrowed()) {
			String borrowUserEntered = folder.getBorrowUserEntered();
			if (borrowUserEntered != null) {
				String userTitle = rmSchemasRecordsServices.getUser(borrowUserEntered).getTitle();
				LocalDateTime borrowDateTime = folder.getBorrowDate();
				LocalDate borrowDate = borrowDateTime != null ? borrowDateTime.toLocalDate() : null;
				borrowedMessage = $("DisplayFolderView.borrowedFolder", userTitle, borrowDate);
			} else {
				borrowedMessage = $("DisplayFolderView.borrowedByNullUserFolder");
			}
		} else if (folder.getContainer() != null) {
			try {
				ContainerRecord containerRecord = rmSchemasRecordsServices.getContainerRecord(folder.getContainer());
				boolean borrowed = Boolean.TRUE.equals(containerRecord.getBorrowed());
				String borrower = containerRecord.getBorrower();
				if (borrowed && borrower != null) {
					String userTitle = rmSchemasRecordsServices.getUser(borrower).getTitle();
					LocalDate borrowDate = containerRecord.getBorrowDate();
					borrowedMessage = $("DisplayFolderView.borrowedContainer", userTitle, borrowDate);
				} else if (borrowed) {
					borrowedMessage = $("DisplayFolderView.borrowedByNullUserContainer");
				}
			} catch (Exception e) {
				LOGGER.error("Could not find linked container");
			}
		}
		return borrowedMessage;
	}

	protected ComponentState getBorrowButtonState(User user, Folder folder) {
		try {
			borrowingServices.validateCanBorrow(user, folder, null);
			return ComponentState
					.visibleIf(user.hasAll(RMPermissionsTo.BORROW_FOLDER, RMPermissionsTo.BORROWING_FOLDER_DIRECTLY).on(folder));
		} catch (Exception e) {
			return ComponentState.INVISIBLE;
		}
	}

	private ComponentState getReturnFolderButtonState(User user, Folder folder) {
		try {
			borrowingServices.validateCanReturnFolder(user, folder);
			return ComponentState
					.visibleIf(user.hasAll(RMPermissionsTo.BORROW_FOLDER, RMPermissionsTo.BORROWING_FOLDER_DIRECTLY).on(folder));
		} catch (Exception e) {
			return ComponentState.INVISIBLE;
		}
	}

	protected ComponentState getReminderReturnFolderButtonState(User user, Folder folder) {
		return isBorrowedByOtherUser(user, folder);
	}

	protected ComponentState getAlertWhenAvailableButtonState(User user, Folder folder) {
		return isBorrowedByOtherUser(user, folder);
	}

	private ComponentState isBorrowedByOtherUser(User currentUser, Folder folder) {
		Boolean borrowed = folder.getBorrowed();
		if ((borrowed != null && borrowed) && borrowed && !isCurrentUserBorrower(currentUser, folder)) {
			return ComponentState.ENABLED;
		} else {
			return ComponentState.INVISIBLE;
		}
	}

	private boolean isCurrentUserBorrower(User currentUser, Folder folder) {
		return currentUser.getId().equals(folder.getBorrowUserEntered());
	}

	ComponentState getPrintButtonState(User user, Folder folder) {
		AuthorizationsServices authorizationsServices = modelLayerFactory.newAuthorizationsServices();
		if (authorizationsServices.canRead(user, folder.getWrappedRecord())) {
			if (folder.getPermissionStatus().isInactive()) {
				if (folder.getBorrowed() != null && folder.getBorrowed()) {
					return ComponentState.visibleIf(user.has(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER).on(folder) && user
							.has(RMPermissionsTo.MODIFY_INACTIVE_FOLDERS).on(folder));
				}
				return ComponentState.visibleIf(user.has(RMPermissionsTo.MODIFY_INACTIVE_FOLDERS).on(folder));
			}
			if (folder.getPermissionStatus().isSemiActive()) {
				if (folder.getBorrowed() != null && folder.getBorrowed()) {
					return ComponentState.visibleIf(user.has(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER).on(folder) && user
							.has(RMPermissionsTo.MODIFY_SEMIACTIVE_FOLDERS).on(folder));
				}
				return ComponentState.visibleIf(user.has(RMPermissionsTo.MODIFY_SEMIACTIVE_FOLDERS).on(folder));
			}
			return ComponentState.ENABLED;
		}
		return ComponentState.INVISIBLE;
	}

	private ComponentState getDuplicateFolderButtonState(User user, Folder folder) {
		AuthorizationsServices authorizationsServices = modelLayerFactory.newAuthorizationsServices();
		if (authorizationsServices.canWrite(user, folder.getWrappedRecord())) {
			if (folder.getPermissionStatus().isInactive()) {
				return ComponentState.visibleIf(user.has(RMPermissionsTo.DUPLICATE_INACTIVE_FOLDER).on(folder));
			}
			if (folder.getPermissionStatus().isSemiActive()) {
				return ComponentState.visibleIf(user.has(RMPermissionsTo.DUPLICATE_SEMIACTIVE_FOLDER).on(folder));
			}
			return ComponentState.ENABLED;
		}
		return ComponentState.INVISIBLE;
	}

	private ComponentState getAuthorizationButtonState(User user, Folder folder) {
		return ComponentState.visibleIf(user.has(RMPermissionsTo.MANAGE_FOLDER_AUTHORIZATIONS).on(folder));
	}

	ComponentState getShareButtonState(User user, Folder folder) {
		if (user.has(RMPermissionsTo.SHARE_FOLDER).on(folder)) {
			if (folder.getPermissionStatus().isInactive()) {
				return ComponentState.visibleIf(user.has(RMPermissionsTo.SHARE_A_INACTIVE_FOLDER).on(folder));
			}
			if (folder.getPermissionStatus().isSemiActive()) {
				return ComponentState.visibleIf(user.has(RMPermissionsTo.SHARE_A_SEMIACTIVE_FOLDER).on(folder));
			}
			if (isNotBlank(folder.getLegacyId())) {
				return ComponentState.visibleIf(user.has(RMPermissionsTo.SHARE_A_IMPORTED_FOLDER).on(folder));
			}
			return ComponentState.ENABLED;
		}
		return ComponentState.INVISIBLE;
	}

	ComponentState getDeleteButtonState(User user, Folder folder) {
		if (user.hasDeleteAccess().on(folder)) {
			if (folder.getPermissionStatus().isInactive()) {
				if (folder.getBorrowed() != null && folder.getBorrowed()) {
					return ComponentState.visibleIf(user.has(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER).on(folder) && user
							.has(RMPermissionsTo.DELETE_INACTIVE_FOLDERS).on(folder));
				}
				return ComponentState.visibleIf(user.has(RMPermissionsTo.DELETE_INACTIVE_FOLDERS).on(folder));
			}
			if (folder.getPermissionStatus().isSemiActive()) {
				if (folder.getBorrowed() != null && folder.getBorrowed()) {
					return ComponentState.visibleIf(user.has(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER).on(folder) && user
							.has(RMPermissionsTo.DELETE_SEMIACTIVE_FOLDERS).on(folder));
				}
				return ComponentState.visibleIf(user.has(RMPermissionsTo.DELETE_SEMIACTIVE_FOLDERS).on(folder));
			}
			return ComponentState.ENABLED;
		}
		return ComponentState.INVISIBLE;
	}

	ComponentState getMoveInFolderButtonState(User user, Folder folder) {
		//		return getEditButtonState(user, folder);
		return ComponentState.INVISIBLE;
	}

	ComponentState getEditButtonState(User user, Folder folder) {
		if (isNotBlank(folder.getLegacyId()) && !user.has(RMPermissionsTo.MODIFY_IMPORTED_FOLDERS).on(folder)) {
			return ComponentState.INVISIBLE;
		}
		return ComponentState.visibleIf(user.hasWriteAccess().on(folder)
				&& extensions.isRecordModifiableBy(folder.getWrappedRecord(), user));

	}

	ComponentState getAddFolderButtonState(User user, Folder folder) {
		if (user.hasWriteAccess().on(folder) &&
				user.hasAll(RMPermissionsTo.CREATE_SUB_FOLDERS, RMPermissionsTo.CREATE_FOLDERS).on(folder)) {
			if (folder.getPermissionStatus().isInactive()) {
				if (folder.getBorrowed() != null && folder.getBorrowed()) {
					return ComponentState.visibleIf(user.has(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER).on(folder) && user
							.has(RMPermissionsTo.CREATE_SUB_FOLDERS_IN_INACTIVE_FOLDERS).on(folder));
				}
				return ComponentState.visibleIf(user.has(RMPermissionsTo.CREATE_SUB_FOLDERS_IN_INACTIVE_FOLDERS).on(folder));
			}
			if (folder.getPermissionStatus().isSemiActive()) {
				if (folder.getBorrowed() != null && folder.getBorrowed()) {
					return ComponentState.visibleIf(user.has(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER).on(folder) && user
							.has(RMPermissionsTo.CREATE_SUB_FOLDERS_IN_SEMIACTIVE_FOLDERS).on(folder));
				}
				return ComponentState.visibleIf(user.has(RMPermissionsTo.CREATE_SUB_FOLDERS_IN_SEMIACTIVE_FOLDERS).on(folder));
			}
			return ComponentState.ENABLED;
		}
		return ComponentState.INVISIBLE;
	}

	ComponentState getAddDocumentButtonState(User user, Folder folder) {
		if (user.hasWriteAccess().on(folder) &&
				user.has(RMPermissionsTo.CREATE_DOCUMENTS).on(folder)) {
			if (folder.getPermissionStatus().isInactive()) {
				if (folder.getBorrowed() != null && folder.getBorrowed()) {
					return ComponentState.visibleIf(user.has(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER).on(folder) && user
							.has(RMPermissionsTo.CREATE_INACTIVE_DOCUMENT).on(folder));
				}
				return ComponentState.visibleIf(user.has(RMPermissionsTo.CREATE_INACTIVE_DOCUMENT).on(folder));
			}
			if (folder.getPermissionStatus().isSemiActive()) {
				if (folder.getBorrowed() != null && folder.getBorrowed()) {
					return ComponentState.visibleIf(user.has(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER).on(folder) && user
							.has(RMPermissionsTo.CREATE_SEMIACTIVE_DOCUMENT).on(folder));
				}
				return ComponentState.visibleIf(user.has(RMPermissionsTo.CREATE_SEMIACTIVE_DOCUMENT).on(folder));
			}
			return ComponentState.ENABLED;
		}
		return ComponentState.INVISIBLE;
	}

	private MetadataSchemaType getFoldersSchemaType() {
		return schemaType(Folder.SCHEMA_TYPE);
	}

	private MetadataSchemaType getDocumentsSchemaType() {
		return schemaType(Document.SCHEMA_TYPE);
	}

	private MetadataSchema getFoldersSchema() {
		return schema(Folder.DEFAULT_SCHEMA);
	}

	private MetadataSchema getDocumentsSchema() {
		return schema(Document.DEFAULT_SCHEMA);
	}

	private MetadataSchema getTasksSchema() {
		return schema(Task.DEFAULT_SCHEMA);
	}

	public void viewAssembled() {
		view.setFolderContent(Arrays.asList(subFoldersDataProvider, documentsDataProvider));
		view.setTasks(tasksDataProvider);
		view.setEvents(eventsDataProvider);

		RMSchemasRecordsServices schemas = new RMSchemasRecordsServices(collection, appLayerFactory);
		Folder folder = schemas.wrapFolder(toRecord(folderVO));
		disableMenuItems(folder);
		modelLayerFactory.newLoggingServices().logRecordView(folder.getWrappedRecord(), getCurrentUser());
	}

	public void backButtonClicked() {
		view.navigate().to().previousView();
	}

	public void addDocumentButtonClicked() {
		view.navigate().to(RMViews.class).addDocument(folderVO.getId());
	}

	public void addSubFolderButtonClicked() {
		view.navigate().to(RMViews.class).addFolder(folderVO.getId());
	}

	public void editFolderButtonClicked() {
		view.navigate().to(RMViews.class).editFolder(folderVO.getId());
	}

	public void deleteFolderButtonClicked(String reason) {
		String parentId = folderVO.get(Folder.PARENT_FOLDER);
		Record record = toRecord(folderVO);

		if (recordServices.isLogicallyDeletable(record, getCurrentUser())) {
			appLayerFactory.getExtensions().forCollection(collection)
					.notifyFolderDeletion(new FolderDeletionEvent(rmSchemasRecordsServices.wrapFolder(record)));
			delete(record, reason, false);
			if (parentId != null) {
				view.navigate().to(RMViews.class).displayFolder(parentId);
			} else {
				view.navigate().to().home();
			}
		} else {
			view.showErrorMessage($("ListSchemaRecordsView.cannotDelete"));
		}
	}

	public void duplicateFolderButtonClicked() {
		Folder folder = rmSchemasRecordsServices().getFolder(folderVO.getId());
		view.navigate().to(RMViews.class).duplicateFolder(folder.getId(), false);
	}

	public void duplicateStructureButtonClicked() {
		Folder folder = rmSchemasRecordsServices().getFolder(folderVO.getId());
		view.navigate().to(RMViews.class).duplicateFolder(folder.getId(), true);
	}

	public void linkToFolderButtonClicked() {
		// TODO ZeroClipboardComponent
		view.showMessage("Clipboard integration TODO!");
	}

	public void addAuthorizationButtonClicked() {
		view.navigate().to().listObjectAccessAuthorizations(folderVO.getId());
	}

	public void shareFolderButtonClicked() {
		view.navigate().to().shareContent(folderVO.getId());
	}

	public void editDocumentButtonClicked(RecordVO recordVO) {
		view.navigate().to(RMViews.class).editDocument(recordVO.getId());
	}

	public void downloadDocumentButtonClicked(RecordVO recordVO) {
		ContentVersionVO contentVersionVO = recordVO.get(Document.CONTENT);
		view.downloadContentVersion(recordVO, contentVersionVO);
	}

	public void displayDocumentButtonClicked(RecordVO record) {
		view.navigate().to(RMViews.class).displayDocument(record.getId());
	}

	public void documentClicked(RecordVO recordVO) {
		ContentVersionVO contentVersionVO = recordVO.get(Document.CONTENT);
		if (contentVersionVO == null) {
			view.navigate().to(RMViews.class).displayDocument(recordVO.getId());
			return;
		}
		String agentURL = ConstellioAgentUtils.getAgentURL(recordVO, contentVersionVO);
		if (agentURL != null) {
			//			view.openAgentURL(agentURL);
			new ConstellioAgentClickHandler().handleClick(agentURL, recordVO, contentVersionVO);
		} else {
			view.navigate().to(RMViews.class).displayDocument(recordVO.getId());
		}
	}

	public void subFolderClicked(RecordVO subFolderVO) {
		view.navigate().to(RMViews.class).displayFolder(subFolderVO.getId());
	}

	public void taskClicked(RecordVO taskVO) {
		view.navigate().to(TaskViews.class).displayTask(taskVO.getId());
	}

	private DecommissioningService decommissioningService() {
		return new DecommissioningService(getCurrentUser().getCollection(), appLayerFactory);
	}

	private RMSchemasRecordsServices rmSchemasRecordsServices() {
		return new RMSchemasRecordsServices(getCurrentUser().getCollection(), appLayerFactory);
	}

	private boolean documentExists(String fileName) {
		Record record = getRecord(folderVO.getId());

		MetadataSchemaType documentsSchemaType = getDocumentsSchemaType();
		MetadataSchema documentsSchema = getDocumentsSchema();
		Metadata folderMetadata = documentsSchema.getMetadata(Document.FOLDER);
		Metadata titleMetadata = documentsSchema.getMetadata(Schemas.TITLE.getCode());
		LogicalSearchQuery query = new LogicalSearchQuery();
		LogicalSearchCondition parentCondition = from(documentsSchemaType).where(folderMetadata).is(record)
				.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull();
		query.setCondition(parentCondition.andWhere(titleMetadata).is(fileName));

		SearchServices searchServices = modelLayerFactory.newSearchServices();
		return searchServices.query(query).getNumFound() > 0;
	}

	public void contentVersionUploaded(ContentVersionVO uploadedContentVO) {
		view.selectFolderContentTab();
		String fileName = uploadedContentVO.getFileName();
		if (!documentExists(fileName)) {
			try {
				if(Boolean.TRUE.equals(uploadedContentVO.hasFoundDuplicate())) {
					RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
					LogicalSearchQuery duplicateDocumentsQuery = new LogicalSearchQuery().setCondition(LogicalSearchQueryOperators.from(rm.documentSchemaType())
							.where(rm.document.content()).is(ContentFactory.isHash(uploadedContentVO.getHash())))
							.filteredWithUser(getCurrentUser());
					List<Document> duplicateDocuments = rm.searchDocuments(duplicateDocumentsQuery);
					if(duplicateDocuments != null && duplicateDocuments.size() > 0) {
						view.showMessage($("ContentManager.hasFoundDuplicate"));
					}
				}
				uploadedContentVO.setMajorVersion(true);
				Record newRecord;
				if (rmSchemasRecordsServices().isEmail(fileName)) {
					InputStreamProvider inputStreamProvider = uploadedContentVO.getInputStreamProvider();
					InputStream in = inputStreamProvider.getInputStream(DisplayFolderPresenter.class + ".contentVersionUploaded");
					Document document = rmSchemasRecordsServices.newEmail(fileName, in);
					newRecord = document.getWrappedRecord();
				} else {
					Document document = rmSchemasRecordsServices.newDocument();
					newRecord = document.getWrappedRecord();
				}
				DocumentVO documentVO = documentVOBuilder.build(newRecord, VIEW_MODE.FORM, view.getSessionContext());
				documentVO.setFolder(folderVO);
				documentVO.setTitle(fileName);
				documentVO.setContent(uploadedContentVO);

				schemaPresenterUtils.setSchemaCode(newRecord.getSchemaCode());
				newRecord = schemaPresenterUtils.toRecord(documentVO);

				schemaPresenterUtils.addOrUpdate(newRecord);
				documentsDataProvider.fireDataRefreshEvent();
				view.refreshFolderContentTab();
			} catch (final IcapException e) {
				view.showErrorMessage(e.getMessage());
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			} finally {
				view.clearUploadField();
			}
		}
	}

	public boolean borrowFolder(LocalDate borrowingDate, LocalDate previewReturnDate, String userId, BorrowingType borrowingType,
			LocalDate returnDate) {
		boolean borrowed;
		String errorMessage = borrowingServices
				.validateBorrowingInfos(userId, borrowingDate, previewReturnDate, borrowingType, returnDate);
		if (errorMessage != null) {
			view.showErrorMessage($(errorMessage));
			borrowed = false;
		} else {
			Record record = recordServices().getDocumentById(userId);
			User borrowerEntered = wrapUser(record);
			try {
				borrowingServices
						.borrowFolder(folderVO.getId(), borrowingDate, previewReturnDate, getCurrentUser(), borrowerEntered,
								borrowingType, true);
				view.navigate().to(RMViews.class).displayFolder(folderVO.getId());
				borrowed = true;
			} catch (RecordServicesException e) {
				LOGGER.error(e.getMessage(), e);
				view.showErrorMessage($("DisplayFolderView.cannotBorrowFolder"));
				borrowed = false;
			}
		}
		if (returnDate != null) {
			return returnFolder(returnDate, borrowingDate);
		}
		return borrowed;
	}

	public boolean returnFolder(LocalDate returnDate) {
		LocalDateTime borrowDateTime = folderVO.getBorrowDate();
		LocalDate borrowDate = borrowDateTime != null ? borrowDateTime.toLocalDate() : null;
		return returnFolder(returnDate, borrowDate);
	}

	protected boolean returnFolder(LocalDate returnDate, LocalDate borrowingDate) {
		String errorMessage = borrowingServices.validateReturnDate(returnDate, borrowingDate);
		if (errorMessage != null) {
			view.showErrorMessage($(errorMessage));
			return false;
		}
		try {
			borrowingServices.returnFolder(folderVO.getId(), getCurrentUser(), returnDate, true);
			view.navigate().to(RMViews.class).displayFolder(folderVO.getId());
			return true;
		} catch (RecordServicesException e) {
			view.showErrorMessage($("DisplayFolderView.cannotReturnFolder"));
			return false;
		}
	}

	private EmailToSend newEmailToSend() {
		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(getCurrentUser().getCollection());
		MetadataSchema schema = types.getSchemaType(EmailToSend.SCHEMA_TYPE).getDefaultSchema();
		Record emailToSendRecord = recordServices.newRecordWithSchema(schema);
		return new EmailToSend(emailToSendRecord, types);
	}

	public void reminderReturnFolder() {

		try {
			EmailToSend emailToSend = newEmailToSend();
			User borrower = rmSchemasRecordsServices.getUser(folderVO.getBorrowUserId());
			EmailAddress borrowerAddress = new EmailAddress(borrower.getTitle(), borrower.getEmail());
			emailToSend.setTo(Arrays.asList(borrowerAddress));
			emailToSend.setSendOn(TimeProvider.getLocalDateTime());
			emailToSend.setSubject($("DisplayFolderView.returnFolderReminder") + folderVO.getTitle());
			emailToSend.setTemplate(RMEmailTemplateConstants.REMIND_BORROW_TEMPLATE_ID);
			List<String> parameters = new ArrayList<>();
			String previewReturnDate = folderVO.getPreviewReturnDate().toString();
			parameters.add("previewReturnDate" + EmailToSend.PARAMETER_SEPARATOR + previewReturnDate);
			parameters.add("borrower" + EmailToSend.PARAMETER_SEPARATOR + borrower.getUsername());
			String borrowedFolderTitle = folderVO.getTitle();
			parameters.add("borrowedFolderTitle" + EmailToSend.PARAMETER_SEPARATOR + borrowedFolderTitle);
			parameters.add("title" + EmailToSend.PARAMETER_SEPARATOR + $("DisplayFolderView.returnFolderReminder") + " \""
					+ folderVO.getTitle() + "\"");
			emailToSend.setParameters(parameters);

			recordServices.add(emailToSend);
			view.showMessage($("DisplayFolderView.reminderEmailSent"));
		} catch (RecordServicesException e) {
			LOGGER.error("DisplayFolderView.cannotSendEmail", e);
			view.showMessage($("DisplayFolderView.cannotSendEmail"));
		}
	}

	public void alertWhenAvailable() {
		try {
			RMSchemasRecordsServices schemas = new RMSchemasRecordsServices(view.getCollection(), appLayerFactory);
			Folder folder = schemas.getFolder(folderVO.getId());
			List<String> usersToAlert = folder.getAlertUsersWhenAvailable();
			String currentUserId = getCurrentUser().getId();
			if (!currentUserId.equals(folder.getBorrowUser()) && !currentUserId.equals(folder.getBorrowUserEntered())) {
				List<String> newUsersToAlert = new ArrayList<>();
				newUsersToAlert.addAll(usersToAlert);
				if (!newUsersToAlert.contains(currentUserId)) {
					newUsersToAlert.add(currentUserId);
					folder.setAlertUsersWhenAvailable(newUsersToAlert);
					addOrUpdate(folder.getWrappedRecord());
				}
			}
			view.showMessage($("RMObject.createAlert"));
		} catch (Exception e) {
			LOGGER.error("RMObject.cannotCreateAlert", e);
			view.showErrorMessage($("RMObject.cannotCreateAlert"));
		}
	}

	public List<LabelTemplate> getCustomTemplates() {
		return appLayerFactory.getLabelTemplateManager().listExtensionTemplates(Folder.SCHEMA_TYPE);
	}

	public List<LabelTemplate> getDefaultTemplates() {
		return appLayerFactory.getLabelTemplateManager().listTemplates(Folder.SCHEMA_TYPE);
	}

	public Date getPreviewReturnDate(Date borrowDate, Object borrowingTypeValue) {
		BorrowingType borrowingType;
		Date previewReturnDate = TimeProvider.getLocalDate().toDate();
		if (borrowDate != null && borrowingTypeValue != null) {
			borrowingType = (BorrowingType) borrowingTypeValue;
			if (borrowingType == BorrowingType.BORROW) {
				int addDays = rmConfigs.getBorrowingDurationDays();
				previewReturnDate = LocalDate.fromDateFields(borrowDate).plusDays(addDays).toDate();
			} else {
				previewReturnDate = borrowDate;
			}
		}
		return previewReturnDate;
	}

	boolean isDocument(RecordVO record) {
		return record.getSchema().getCode().startsWith("document");
	}

	public boolean canModifyDocument(RecordVO record) {
		boolean hasContent = record.get(Document.CONTENT) != null;
		boolean hasAccess = getCurrentUser().hasWriteAccess().on(getRecord(record.getId()));
		return hasContent && hasAccess;
	}

	public void addToCartRequested(RecordVO recordVO) {
		Cart cart = rmSchemasRecordsServices.getCart(recordVO.getId()).addFolders(Arrays.asList(folderVO.getId()));
		addOrUpdate(cart.getWrappedRecord());
		view.showMessage($("DisplayFolderView.addedToCart"));
	}

	public RecordVODataProvider getOwnedCartsDataProvider() {
		final MetadataSchemaVO cartSchemaVO = schemaVOBuilder
				.build(rmSchemasRecordsServices.cartSchema(), VIEW_MODE.TABLE, view.getSessionContext());
		return new RecordVODataProvider(cartSchemaVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(
						from(rmSchemasRecordsServices.cartSchema()).where(rmSchemasRecordsServices.cartOwner())
								.isEqualTo(getCurrentUser().getId())).sortAsc(Schemas.TITLE);
			}
		};
	}

	public RecordVODataProvider getSharedCartsDataProvider() {
		final MetadataSchemaVO cartSchemaVO = schemaVOBuilder
				.build(rmSchemasRecordsServices.cartSchema(), VIEW_MODE.TABLE, view.getSessionContext());
		return new RecordVODataProvider(cartSchemaVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(
						from(rmSchemasRecordsServices.cartSchema()).where(rmSchemasRecordsServices.cartSharedWithUsers())
								.isContaining(asList(getCurrentUser().getId()))).sortAsc(Schemas.TITLE);
			}
		};
	}

	public void parentFolderButtonClicked(String parentId)
			throws RecordServicesException {
		RMSchemasRecordsServices rmSchemas = new RMSchemasRecordsServices(collection, appLayerFactory);

		String currentFolderId = folderVO.getId();
		if (isNotBlank(parentId)) {
			try {
				recordServices.update(rmSchemas.getFolder(currentFolderId).setParentFolder(parentId));
				view.navigate().to(RMViews.class).displayFolder(currentFolderId);
			} catch (RecordServicesException.ValidationException e) {
				view.showErrorMessage($(e.getErrors()));
			}
		}
	}

	public void createNewCartAndAddToItRequested(String title) {
		Cart cart = rmSchemasRecordsServices.newCart();
		cart.setTitle(title);
		cart.setOwner(getCurrentUser());
		try {
			cart.addFolders(Arrays.asList(folderVO.getId()));
			recordServices().execute(new Transaction(cart.getWrappedRecord()).setUser(getCurrentUser()));
			view.showMessage($("DisplayFolderView.addedToCart"));
		} catch (RecordServicesException e) {
			e.printStackTrace();
		}
	}

	public RecordVODataProvider getEventsDataProvider() {
		final MetadataSchemaVO eventSchemaVO = schemaVOBuilder
				.build(rmSchemasRecordsServices.eventSchema(), VIEW_MODE.TABLE, view.getSessionContext());
		return new RecordVODataProvider(eventSchemaVO, new EventToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				RMEventsSearchServices rmEventsSearchServices = new RMEventsSearchServices(modelLayerFactory, collection);
				return rmEventsSearchServices.newFindEventByRecordIDQuery(getCurrentUser(), folderVO.getId());
			}
		};
	}

	protected boolean hasCurrentUserPermissionToViewEvents() {
		return getCurrentUser().has(CorePermissions.VIEW_EVENTS).on(toRecord(folderVO));
	}

	public void refreshEvents() {
		modelLayerFactory.getDataLayerFactory().newEventsDao().flush();
		view.setEvents(getEventsDataProvider());
	}

	public boolean hasCurrentUserPermissionToUseCart() {
		return getCurrentUser().has(RMPermissionsTo.USE_CART).globally();
	}

	public boolean hasPermissionToStartWorkflow() {
		return getCurrentUser().has(TasksPermissionsTo.START_WORKFLOWS).globally();
	}

	public boolean isSelected(RecordVO recordVO) {
		String recordId = recordVO.getId();
		SessionContext sessionContext = view.getSessionContext();
		return sessionContext.getSelectedRecordIds().contains(recordId);
	}

	public void recordSelectionChanged(RecordVO recordVO, Boolean selected) {
		String recordId = recordVO.getId();
		SessionContext sessionContext = view.getSessionContext();
		if (selected) {
			sessionContext.addSelectedRecordId(recordId, recordVO.getSchema().getTypeCode());
		} else {
			sessionContext.removeSelectedRecordId(recordId, recordVO.getSchema().getTypeCode());
		}
	}

	void computeAllItemsSelected() {
		SessionContext sessionContext = view.getSessionContext();
		List<String> selectedRecordIds = sessionContext.getSelectedRecordIds();
		SearchServices searchServices = modelLayerFactory.newSearchServices();

		List<String> subFolderIds = searchServices.searchRecordIds(getSubFoldersQuery());
		for (String subFolderId : subFolderIds) {
			if (!selectedRecordIds.contains(subFolderId)) {
				allItemsSelected = false;
				return;
			}
		}
		List<String> documentIds = searchServices.searchRecordIds(getDocumentsQuery());
		for (String documentId : documentIds) {
			if (!selectedRecordIds.contains(documentId)) {
				allItemsSelected = false;
				return;
			}
		}
		allItemsSelected = !subFolderIds.isEmpty() || !documentIds.isEmpty();
	}

	boolean isAllItemsSelected() {
		return allItemsSelected;
	}

	boolean isAllItemsDeselected() {
		return allItemsDeselected;
	}

	void selectAllClicked() {
		allItemsSelected = true;
		allItemsDeselected = false;

		SessionContext sessionContext = view.getSessionContext();
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		List<String> subFolderIds = searchServices.searchRecordIds(getSubFoldersQuery());
		for (String subFolderId : subFolderIds) {
			sessionContext.addSelectedRecordId(subFolderId, Folder.SCHEMA_TYPE);
		}
		List<String> documentIds = searchServices.searchRecordIds(getDocumentsQuery());
		for (String documentId : documentIds) {
			sessionContext.addSelectedRecordId(documentId, Document.SCHEMA_TYPE);
		}
	}

	void deselectAllClicked() {
		allItemsSelected = false;
		allItemsDeselected = true;

		SessionContext sessionContext = view.getSessionContext();
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		List<String> subFolderIds = searchServices.searchRecordIds(getSubFoldersQuery());
		for (String subFolderId : subFolderIds) {
			sessionContext.removeSelectedRecordId(subFolderId, Folder.SCHEMA_TYPE);
		}
		List<String> documentIds = searchServices.searchRecordIds(getDocumentsQuery());
		for (String documentId : documentIds) {
			sessionContext.removeSelectedRecordId(documentId, Document.SCHEMA_TYPE);
		}
	}

}
