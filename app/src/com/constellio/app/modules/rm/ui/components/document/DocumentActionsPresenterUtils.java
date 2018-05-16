package com.constellio.app.modules.rm.ui.components.document;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.logging.DecommissioningLoggingService;
import com.constellio.app.modules.rm.ui.builders.DocumentToVOBuilder;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.ContentVersionToVOBuilder;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.DateFormatUtils;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.services.contents.ContentConversionManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.logging.SearchEventServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.security.AuthorizationsServices;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.pages.search.SearchPresenter.CURRENT_SEARCH_EVENT;
import static com.constellio.app.ui.pages.search.SearchPresenter.SEARCH_EVENT_DWELL_TIME;
import static com.constellio.model.entities.schemas.Schemas.LEGACY_ID;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class DocumentActionsPresenterUtils<T extends DocumentActionsComponent> implements Serializable {

	private static final int WAIT_ONE_SECOND = 1;

	protected SchemaPresenterUtils presenterUtils;

	protected ContentVersionToVOBuilder contentVersionVOBuilder;
	protected DocumentVO documentVO;
	protected T actionsComponent;

	private transient User currentUser;
	private Record currentDocument;
	protected transient DocumentToVOBuilder voBuilder;
	private transient RMSchemasRecordsServices rmSchemasRecordsServices;
	private transient DecommissioningLoggingService decommissioningLoggingService;
	private transient ModelLayerCollectionExtensions extensions;
	private transient LoggingServices loggingServices;

	public DocumentActionsPresenterUtils(T actionsComponent) {
		this.actionsComponent = actionsComponent;

		ConstellioFactories constellioFactories = actionsComponent.getConstellioFactories();
		SessionContext sessionContext = actionsComponent.getSessionContext();
		presenterUtils = new SchemaPresenterUtils(Document.DEFAULT_SCHEMA, constellioFactories, sessionContext);
		contentVersionVOBuilder = new ContentVersionToVOBuilder(presenterUtils.modelLayerFactory());
		loggingServices = new LoggingServices(getModelLayerFactory());

		initTransientObjects();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		rmSchemasRecordsServices = new RMSchemasRecordsServices(presenterUtils.getCollection(),
				presenterUtils.appLayerFactory());
		voBuilder = new DocumentToVOBuilder(presenterUtils.modelLayerFactory());
		decommissioningLoggingService = new DecommissioningLoggingService(presenterUtils.modelLayerFactory());
		extensions = presenterUtils.modelLayerFactory().getExtensions().forCollection(presenterUtils.getCollection());
	}

	public DocumentVO getDocumentVO() {
		return this.documentVO;
	}

	public void setRecordVO(RecordVO recordVO) {
		if (recordVO instanceof DocumentVO) {
			this.documentVO = (DocumentVO) recordVO;
		} else {
			this.documentVO = new DocumentVO(recordVO);
		}
		this.currentDocument = documentVO.getRecord();
		presenterUtils.setSchemaCode(recordVO.getSchema().getCode());
	}

	protected boolean isEditDocumentPossible() {
		return getAuthorizationServices().canWrite(getCurrentUser(), currentDocument());
	}

	ComponentState getEditButtonState() {
		Record record = currentDocument();
		User user = getCurrentUser();
		if (isNotBlank(record.<String>get(LEGACY_ID)) && !user.has(RMPermissionsTo.MODIFY_IMPORTED_DOCUMENTS).on(record)) {
			return ComponentState.INVISIBLE;
		}
		return ComponentState.visibleIf(user.hasWriteAccess().on(record)
				&& extensions.isRecordModifiableBy(record, user) && !extensions.isModifyBlocked(record, user));
	}

	public void editDocumentButtonClicked() {
		if (isEditDocumentPossible()) {
			actionsComponent.navigate().to(RMViews.class).editDocument(documentVO.getId());
			updateSearchResultClicked();
		}
	}

	public Document renameContentButtonClicked(String newName) {
		if (isEditDocumentPossible()) {
			Record record = presenterUtils.getRecord(documentVO.getId());
			Document document = new Document(record, presenterUtils.types());

			if (document.getContent().getCurrentVersion().getFilename().equals(document.getTitle())) {
				RMSchemasRecordsServices rm = new RMSchemasRecordsServices(record.getCollection(), getModelLayerFactory());
				if (rm.folder.title().getDataEntry().getType() == DataEntryType.MANUAL) {
					document.setTitle(newName);
				}
			}

			document.getContent().renameCurrentVersion(newName);
			return document;
		}
		return null;
	}

	public void copyContentButtonClicked() {
		actionsComponent.navigate().to(RMViews.class).addDocumentWithContent(documentVO.getId());
	}

	protected boolean isDeleteDocumentPossible() {
		return getCurrentUser().hasDeleteAccess().on(currentDocument()) && !extensions
				.isDeleteBlocked(currentDocument(), getCurrentUser());
	}

	protected boolean isDeleteDocumentPossibleExtensively() {
		return isDeleteDocumentPossible() && presenterUtils.recordServices()
				.isLogicallyDeletable(currentDocument(), getCurrentUser());
	}

	private ComponentState getDeleteButtonState() {

		if (isDeleteDocumentPossible()) {
			if (documentVO != null) {
				Document document = new Document(currentDocument(), presenterUtils.types());
				if (document.isPublished() && !getCurrentUser().has(RMPermissionsTo.DELETE_PUBLISHED_DOCUMENT)
						.on(currentDocument())) {
					return ComponentState.INVISIBLE;
				}

				if (getCurrentBorrowerOf(document) != null && !getCurrentUser().has(RMPermissionsTo.DELETE_BORROWED_DOCUMENT)
						.on(currentDocument())) {
					return ComponentState.INVISIBLE;
				}
			}
			FolderStatus archivisticStatus = documentVO.get(Document.FOLDER_ARCHIVISTIC_STATUS);
			if (archivisticStatus != null && archivisticStatus.isInactive()) {
				Folder parentFolder = rmSchemasRecordsServices.getFolder(currentDocument().getParentId());
				if (parentFolder.getBorrowed() != null && parentFolder.getBorrowed()) {
					return ComponentState
							.visibleIf(getCurrentUser().has(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER).on(parentFolder)
									&& getCurrentUser().has(RMPermissionsTo.DELETE_INACTIVE_DOCUMENT).on(currentDocument()));
				}
				return ComponentState
						.visibleIf(getCurrentUser().has(RMPermissionsTo.DELETE_INACTIVE_DOCUMENT).on(currentDocument()));
			}
			if (archivisticStatus != null && archivisticStatus.isInactive()) {
				Folder parentFolder = rmSchemasRecordsServices.getFolder(currentDocument().getParentId());
				if (parentFolder.getBorrowed() != null && parentFolder.getBorrowed()) {
					return ComponentState
							.visibleIf(getCurrentUser().has(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER).on(parentFolder)
									&& getCurrentUser().has(RMPermissionsTo.DELETE_SEMIACTIVE_DOCUMENT).on(currentDocument()));
				}
				return ComponentState
						.visibleIf(getCurrentUser().has(RMPermissionsTo.DELETE_SEMIACTIVE_DOCUMENT).on(currentDocument()));
			}
			return ComponentState.ENABLED;
		}
		return ComponentState.INVISIBLE;
	}

	public void deleteDocumentButtonClicked() {
		if (isDeleteDocumentPossibleExtensively()) {
			Document document = rmSchemasRecordsServices.getDocument(documentVO.getId());
			String parentId = document.getFolder();
			try {
				presenterUtils.delete(document.getWrappedRecord(), null, true, WAIT_ONE_SECOND);
			} catch (RecordServicesRuntimeException.RecordServicesRuntimeException_CannotLogicallyDeleteRecord e) {
				Content content = document.getContent();
				String checkoutUserId = content != null ? content.getCheckoutUserId() : null;

				if (checkoutUserId != null) {
					actionsComponent.showMessage($("DocumentActionsComponent.cannotBeDeleteBorrowedDocuments"));
				} else {
					actionsComponent.showMessage($("DocumentActionsComponent.cannotBeDeleted"));
				}
				return;
			}
			if (parentId != null) {
				actionsComponent.navigate().to(RMViews.class).displayFolder(parentId);
			} else {
				actionsComponent.navigate().to().recordsManagement();
			}
		} else {
			actionsComponent.showMessage($("DocumentActionsComponent.cannotBeDeleted"));
		}
	}

	public void linkToDocumentButtonClicked() {
		// TODO ZeroClipboardComponent
		actionsComponent.showMessage("Clipboard integration TODO!");
	}

	protected boolean isAddAuthorizationPossible() {
		return getCurrentUser().has(RMPermissionsTo.MANAGE_DOCUMENT_AUTHORIZATIONS).on(currentDocument());
	}

	protected boolean isCreateDocumentPossible() {
		return getCurrentUser().has(RMPermissionsTo.CREATE_DOCUMENTS).on(currentDocument());
	}

	private ComponentState getAddAuthorizationState() {
		return ComponentState.visibleIf(isAddAuthorizationPossible());
	}

	private ComponentState getCreateDocumentState() {
		return ComponentState.visibleIf(isCreateDocumentPossible());
	}

	protected boolean isShareDocumentPossible() {
		return getCurrentUser().has(RMPermissionsTo.SHARE_DOCUMENT).on(currentDocument());
	}

	public ComponentState getCreatePDFAState() {
		if (isCheckOutPossible() && getAuthorizationServices().canWrite(getCurrentUser(), currentDocument())) {
			if (getContent() != null) {
				return ComponentState.ENABLED;
			}
		}
		return ComponentState.INVISIBLE;
	}

	private ComponentState getShareDocumentState() {
		FolderStatus archivisticStatus = rmSchemasRecordsServices.wrapDocument(currentDocument()).getArchivisticStatus();
		if (isShareDocumentPossible() && archivisticStatus != null) {
			if (archivisticStatus.isInactive()) {
				return ComponentState
						.visibleIf(getCurrentUser().has(RMPermissionsTo.SHARE_A_INACTIVE_DOCUMENT).on(currentDocument()));
			}
			if (archivisticStatus.isSemiActive()) {
				return ComponentState
						.visibleIf(getCurrentUser().has(RMPermissionsTo.SHARE_A_SEMIACTIVE_DOCUMENT).on(currentDocument()));
			}
			if (isNotBlank((String) currentDocument().get(LEGACY_ID))) {
				return ComponentState
						.visibleIf(getCurrentUser().has(RMPermissionsTo.SHARE_A_IMPORTED_DOCUMENT).on(currentDocument()));
			}
			return ComponentState.ENABLED;
		}
		return ComponentState.INVISIBLE;
	}

	public void addAuthorizationButtonClicked() {
		if (isAddAuthorizationPossible()) {
			actionsComponent.navigate().to().listObjectAccessAuthorizations(documentVO.getId());
			updateSearchResultClicked();
		}
	}

	public void shareDocumentButtonClicked() {
		actionsComponent.navigate().to().shareContent(documentVO.getId());
		updateSearchResultClicked();
	}

	public void updateWindowClosed() {
		currentDocument = presenterUtils.getRecord(documentVO.getId());
		documentVO = voBuilder.build(currentDocument, VIEW_MODE.DISPLAY, actionsComponent.getSessionContext());
		updateActionsComponent();
	}

	public void uploadButtonClicked() {
		actionsComponent.openUploadWindow(false);
	}

	public boolean isDeleteContentVersionPossible() {
		return presenterUtils.getCurrentUser().has(CorePermissions.DELETE_CONTENT_VERSION).on(currentDocument());
	}

	public boolean isDeleteContentVersionPossible(ContentVersionVO contentVersionVO) {
		return getContent().isDeleteContentVersionPossible(contentVersionVO.getVersion());
	}

	public void deleteContentVersionButtonClicked(ContentVersionVO contentVersionVO) {
		if (isDeleteContentVersionPossible(contentVersionVO)) {
			String version = contentVersionVO.getVersion();

			Record record = presenterUtils.getRecord(documentVO.getId());
			Document document = new Document(record, presenterUtils.types());
			Content content = document.getContent();
			content.deleteVersion(contentVersionVO.getVersion(), presenterUtils.getCurrentUser());

			try {
				presenterUtils.recordServices().update(record);
				currentDocument = record;
				documentVO = voBuilder.build(record, VIEW_MODE.DISPLAY, actionsComponent.getSessionContext());

				ContentVersionVO currentVersionVO = buildContentVersionVO(content);
				documentVO.setContent(currentVersionVO);

				updateActionsComponent();
				actionsComponent.showMessage($("DocumentActionsComponent.contentVersionDeleted", version));

				createVersionDeletionEvent(record, version);

			} catch (RecordServicesException e) {
				actionsComponent.showErrorMessage(MessageUtils.toMessage(e));
			}
		}
	}

	public ContentVersionVO buildContentVersionVO(Content content) {
		return contentVersionVOBuilder.build(content);
	}

	private void createVersionDeletionEvent(Record record, String version) {
		SchemasRecordsServices schemasRecords = new SchemasRecordsServices(getCurrentUser().getCollection(),
				getModelLayerFactory());
		Event event = schemasRecords.newEvent();
		event.setType(EventType.DELETE_DOCUMENT);
		event.setUsername(getCurrentUser().getUsername());
		if (documentVO != null) {
			event.setUserRoles(StringUtils.join(getCurrentUser().getUserRoles().toArray(), "; "));
			event.setTitle(record.getTitle());
			event.setRecordId(documentVO.getId());
			event.setEventPrincipalPath((String) record.get(Schemas.PRINCIPAL_PATH));
		}
		event.setRecordVersion(version);
		try {
			getModelLayerFactory().newRecordServices().add(event);
		} catch (RecordServicesException e) {
			e.printStackTrace();
		}
	}

	public synchronized void createPDFA() {
		if (!documentVO.getExtension().toUpperCase().equals("PDF") && !documentVO.getExtension().toUpperCase().equals("PDFA")) {
			Record record = presenterUtils.getRecord(documentVO.getId());
			Document document = new Document(record, presenterUtils.types());
			Content content = document.getContent();
			ContentConversionManager conversionManager = new ContentConversionManager(presenterUtils.modelLayerFactory());
			if (content != null) {
				try {
					conversionManager = new ContentConversionManager(presenterUtils.modelLayerFactory());
					conversionManager.convertContentToPDFA(getCurrentUser(), content);
					presenterUtils.addOrUpdate(document.getWrappedRecord());

					decommissioningLoggingService.logPdfAGeneration(document, getCurrentUser());

					actionsComponent.navigate().to(RMViews.class).displayDocument(document.getId());
					actionsComponent.showMessage($("DocumentActionsComponent.createPDFASuccess"));
				} catch (Exception e) {
					actionsComponent.showErrorMessage(
							$("DocumentActionsComponent.createPDFAFailure") + " : " + MessageUtils.toMessage(e));
				} finally {
					conversionManager.close();
				}
			}
		} else {
			actionsComponent.showMessage($("DocumentActionsComponent.documentAllreadyPDFA"));
		}
	}

	public void checkInButtonClicked() {
		if (isCheckInPossible()) {
			actionsComponent.openUploadWindow(true);
		} else if (isCancelCheckOutPossible()) {
			Record record = presenterUtils.getRecord(documentVO.getId());
			Document document = new Document(record, presenterUtils.types());
			Content content = document.getContent();
			content.checkIn();
			getModelLayerFactory().newLoggingServices().returnRecord(record, getCurrentUser());
			try {
				presenterUtils.recordServices().update(record);
				currentDocument = record;
				documentVO = voBuilder.build(record, VIEW_MODE.DISPLAY, actionsComponent.getSessionContext());

				ContentVersionVO currentVersionVO = contentVersionVOBuilder.build(content);
				documentVO.setContent(currentVersionVO);

				updateActionsComponent();
				actionsComponent.refreshParent();
				actionsComponent.showMessage($("DocumentActionsComponent.canceledCheckOut"));
			} catch (RecordServicesException e) {
				actionsComponent.showErrorMessage(MessageUtils.toMessage(e));
			}
		}
	}

	public void finalizeButtonClicked() {
		if (isFinalizePossible()) {
			Record record = presenterUtils.getRecord(documentVO.getId());
			Document document = new Document(record, presenterUtils.types());
			Content content = document.getContent();
			content.finalizeVersion();
			try {
				presenterUtils.recordServices().update(record);
				currentDocument = record;
				documentVO = voBuilder.build(record, VIEW_MODE.DISPLAY, actionsComponent.getSessionContext());

				ContentVersionVO currentVersionVO = contentVersionVOBuilder.build(content);
				documentVO.setContent(currentVersionVO);

				updateActionsComponent();
				String newMajorVersion = content.getCurrentVersion().getVersion();
				loggingServices.finalizeDocument(record, getCurrentUser());
				actionsComponent.showMessage($("DocumentActionsComponent.finalizedVersion", newMajorVersion));
			} catch (RecordServicesException e) {
				actionsComponent.showErrorMessage(MessageUtils.toMessage(e));
			}
		}
	}

	public void checkOutButtonClicked(SessionContext sessionContext) {
		if (isCheckOutPossible()) {
			updateSearchResultClicked();
			Record record = presenterUtils.getRecord(documentVO.getId());
			Document document = new Document(record, presenterUtils.types());
			Content content = document.getContent();
			content.checkOut(presenterUtils.getCurrentUser());
			getModelLayerFactory().newLoggingServices().borrowRecord(record, getCurrentUser(), TimeProvider.getLocalDateTime());
			try {
				presenterUtils.recordServices().update(record);
				currentDocument = record;
				documentVO = voBuilder.build(record, VIEW_MODE.DISPLAY, sessionContext);

				updateActionsComponent();
				String checkedOutVersion = content.getCurrentVersion().getVersion();
				actionsComponent.showMessage($("DocumentActionsComponent.checkedOut", checkedOutVersion));
				String agentURL = ConstellioAgentUtils.getAgentURL(documentVO, documentVO.getContent(), sessionContext);
				if (agentURL != null) {
					actionsComponent.openAgentURL(agentURL);
					loggingServices.openDocument(record, currentUser);
				}
			} catch (RecordServicesException e) {
				actionsComponent.showErrorMessage(MessageUtils.toMessage(e));
			}
		}
	}

	protected Content getContent() {
		Record record = currentDocument();
		if (record == null) {
			record = presenterUtils.getRecord(documentVO.getId());
		}
		Document document = new Document(record, presenterUtils.types());
		return document.getContent();
	}

	protected boolean isCurrentUserBorrower() {
		User currentUser = presenterUtils.getCurrentUser();
		Content content = getContent();
		return content != null && currentUser.getId().equals(content.getCheckoutUserId());
	}

	protected boolean isContentCheckedOut() {
		Content content = getContent();
		return content != null && content.getCheckoutUserId() != null;
	}

	private boolean isEmail() {
		boolean email;
		if (documentVO.getContent() != null) {
			email = rmSchemasRecordsServices.isEmail(documentVO.getContent().getFileName());
		} else {
			email = false;
		}
		return email;
	}

	protected boolean isUploadPossible() {
		boolean email = isEmail();
		boolean checkedOut = isContentCheckedOut();
		boolean borrower = isCurrentUserBorrower();
		return !email && (!checkedOut || borrower);
	}

	ComponentState getUploadButtonState() {

		FolderStatus archivisticStatus = documentVO.get(Document.FOLDER_ARCHIVISTIC_STATUS);
		if (archivisticStatus != null && isUploadPossible() && getCurrentUser().hasWriteAccess().on(currentDocument())
				&& extensions.isRecordModifiableBy(currentDocument(), getCurrentUser())
				&& !extensions.isModifyBlocked(currentDocument(), getCurrentUser())) {
			if (archivisticStatus.isInactive()) {
				Folder parentFolder = rmSchemasRecordsServices.getFolder(currentDocument().getParentId());
				if (parentFolder.getBorrowed() != null && parentFolder.getBorrowed()) {
					return ComponentState
							.visibleIf(getCurrentUser().has(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER).on(parentFolder)
									&& getCurrentUser().has(RMPermissionsTo.UPLOAD_INACTIVE_DOCUMENT).on(currentDocument()));
				}
				return ComponentState
						.visibleIf(getCurrentUser().has(RMPermissionsTo.UPLOAD_INACTIVE_DOCUMENT).on(currentDocument()));
			}
			if (archivisticStatus.isSemiActive()) {
				Folder parentFolder = rmSchemasRecordsServices.getFolder(currentDocument().getParentId());
				if (parentFolder.getBorrowed() != null && parentFolder.getBorrowed()) {
					return ComponentState
							.visibleIf(getCurrentUser().has(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER).on(parentFolder)
									&& getCurrentUser().has(RMPermissionsTo.UPLOAD_SEMIACTIVE_DOCUMENT).on(currentDocument()));
				}
				return ComponentState
						.visibleIf(getCurrentUser().has(RMPermissionsTo.UPLOAD_SEMIACTIVE_DOCUMENT).on(currentDocument()));
			}
			return ComponentState.ENABLED;
		}
		return ComponentState.INVISIBLE;
	}

	protected boolean isCheckInPossible() {
		boolean email = isEmail();
		return !email && (getContent() != null && isCurrentUserBorrower());
	}

	private ComponentState getCheckInState() {
		if (getAuthorizationServices().canWrite(getCurrentUser(), currentDocument())) {
			boolean permissionToReturnOtherUsersDocuments = getCurrentUser().has(RMPermissionsTo.RETURN_OTHER_USERS_DOCUMENTS)
					.on(currentDocument());
			if (isCheckInPossible() || (permissionToReturnOtherUsersDocuments && isContentCheckedOut())) {
				return ComponentState.ENABLED;
			}
		}
		return ComponentState.INVISIBLE;
	}

	protected boolean isCheckOutPossible() {
		boolean email = isEmail();
		return !email && (getContent() != null && !isContentCheckedOut());
	}

	private ComponentState getCheckOutState() {
		if (getAuthorizationServices().canWrite(getCurrentUser(), currentDocument())) {
			if (isCheckOutPossible() && extensions.isRecordModifiableBy(currentDocument(), getCurrentUser()) && !extensions
					.isModifyBlocked(currentDocument(), getCurrentUser())) {
				return ComponentState.ENABLED;
			}
		}
		return ComponentState.INVISIBLE;
	}

	public ComponentState getAlertWhenAvailableButtonState() {
		if (!isEmail() && getContent() != null && isContentCheckedOut() && !isCurrentUserBorrower()) {
			return ComponentState.ENABLED;
		} else {
			return ComponentState.INVISIBLE;
		}
	}

	protected boolean isCancelCheckOutPossible() {
		boolean email = isEmail();
		return !email && (getContent() != null && isContentCheckedOut());
	}

	protected boolean isFinalizePossible() {
		boolean borrowed = isContentCheckedOut();
		boolean minorVersion;
		Content content = getContent();
		minorVersion = content != null && content.getCurrentVersion().getMinor() != 0;
		return !borrowed && minorVersion && extensions.isRecordModifiableBy(currentDocument(), getCurrentUser());
	}

	public void updateActionsComponent() {
		RMConfigs configs = new RMConfigs(getModelLayerFactory().getSystemConfigurationsManager());

		updateBorrowedMessage();
		DocumentVO documentVO = getDocumentVO();
		actionsComponent.setDocumentVO(documentVO);
		Boolean isLogicallyDeleted = documentVO.get(Schemas.LOGICALLY_DELETED_STATUS.getLocalCode());
		if (Boolean.TRUE.equals(isLogicallyDeleted)) {
			actionsComponent.setEditDocumentButtonState(ComponentState.INVISIBLE);
			actionsComponent.setAddDocumentButtonState(ComponentState.INVISIBLE);
			actionsComponent.setDeleteDocumentButtonState(ComponentState.INVISIBLE);
			actionsComponent.setAddAuthorizationButtonState(ComponentState.INVISIBLE);
			actionsComponent.setCreatePDFAButtonState(ComponentState.INVISIBLE);
			actionsComponent.setShareDocumentButtonState(ComponentState.INVISIBLE);
			actionsComponent.setUploadButtonState(ComponentState.INVISIBLE);
			actionsComponent.setCheckInButtonState(ComponentState.INVISIBLE);
			actionsComponent.setCheckOutButtonState(ComponentState.INVISIBLE);
			actionsComponent.setAlertWhenAvailableButtonState(ComponentState.INVISIBLE);
			actionsComponent.setFinalizeButtonVisible(false);
			actionsComponent.setStartWorkflowButtonState(ComponentState.INVISIBLE);
			actionsComponent.setCartButtonState(ComponentState.INVISIBLE);
			actionsComponent.setAddToOrRemoveFromSelectionButtonState(ComponentState.INVISIBLE);
			actionsComponent.setPublishButtonState(ComponentState.INVISIBLE);
			return;
		}

		actionsComponent.setEditDocumentButtonState(getEditButtonState());
		// THIS IS WHERE I SHOULD USE THE ADD DOCUMENT PERMISSION INSTEAD
		// OH MY GOD WHY ARE WE YELLING LIKE THAT ?
		actionsComponent.setAddDocumentButtonState(getCreateDocumentState());
		actionsComponent.setDeleteDocumentButtonState(getDeleteButtonState());
		actionsComponent.setAddAuthorizationButtonState(getAddAuthorizationState());
		actionsComponent.setCreatePDFAButtonState(getCreatePDFAState());
		actionsComponent.setShareDocumentButtonState(getShareDocumentState());
		actionsComponent.setUploadButtonState(getUploadButtonState());
		actionsComponent.setCheckInButtonState(getCheckInState());
		actionsComponent.setCheckOutButtonState(getCheckOutState());
		actionsComponent.setAlertWhenAvailableButtonState(getAlertWhenAvailableButtonState());
		actionsComponent.setFinalizeButtonVisible(isFinalizePossible());
		actionsComponent.setStartWorkflowButtonState(ComponentState.visibleIf(configs.areWorkflowsEnabled()));
	}

	protected void updateBorrowedMessage() {
		if (isContentCheckedOut()) {
			Content content = getContent();
			String borrowDate = DateFormatUtils.format(content.getCheckoutDateTime());
			if (!isCurrentUserBorrower()) {
				String borrowerCaption = SchemaCaptionUtils.getCaptionForRecordId(content.getCheckoutUserId());
				String borrowedMessageKey = "DocumentActionsComponent.borrowedByOtherUser";
				actionsComponent.setBorrowedMessage(borrowedMessageKey, borrowerCaption, borrowDate);
			} else {
				String borrowerMessageKey = "DocumentActionsComponent.borrowedByCurrentUser";
				actionsComponent.setBorrowedMessage(borrowerMessageKey, borrowDate);
			}
		} else {
			actionsComponent.setBorrowedMessage(null);
		}
	}

	ModelLayerFactory getModelLayerFactory() {
		return presenterUtils.getConstellioFactories().getModelLayerFactory();
	}

	AuthorizationsServices getAuthorizationServices() {
		return getModelLayerFactory().newAuthorizationsServices();
	}

	Record currentDocument() {
		if (currentDocument == null) {
			currentDocument = presenterUtils.toRecord(documentVO);
		}
		return currentDocument;
	}

	User getCurrentUser() {
		if (currentUser == null) {
			currentUser = presenterUtils.getCurrentUser();
		}
		return currentUser;
	}

	public String getContentTitle() {
		return getContent().getCurrentVersion().getFilename();
	}

	public boolean hasContent() {
		return getContent() != null;
	}

	public void alertWhenAvailable() {
		RMSchemasRecordsServices schemas = new RMSchemasRecordsServices(presenterUtils.getCollection(),
				presenterUtils.appLayerFactory());
		Document document = schemas.getDocument(documentVO.getId());
		List<String> usersToAlert = document.getAlertUsersWhenAvailable();
		String currentUserId = getCurrentUser().getId();
		List<String> newUsersToAlert = new ArrayList<>();
		newUsersToAlert.addAll(usersToAlert);

		String currentBorrower = getCurrentBorrowerOf(document);

		if (!newUsersToAlert.contains(currentUserId) && currentBorrower != null && !currentUserId.equals(currentBorrower)) {
			newUsersToAlert.add(currentUserId);
			document.setAlertUsersWhenAvailable(newUsersToAlert);
			presenterUtils.addOrUpdate(document.getWrappedRecord(), User.GOD);
		}
		actionsComponent.showMessage($("RMObject.createAlert"));
	}

	private String getCurrentBorrowerOf(Document document) {
		return document.getContent() == null ? null : document.getContent().getCheckoutUserId();
	}

	public void addToCartRequested(RecordVO cartVO) {
		Cart cart = rmSchemasRecordsServices.getCart(cartVO.getId()).addDocuments(Arrays.asList(documentVO.getId()));
		presenterUtils.addOrUpdate(cart.getWrappedRecord());
		actionsComponent.showMessage($("DocumentActionsComponent.addedToCart"));
	}

	public Document publishButtonClicked() {
		Record record = presenterUtils.getRecord(documentVO.getId());
		return new Document(record, presenterUtils.types()).setPublished(true);
	}

	public Document unpublishButtonClicked() {
		Record record = presenterUtils.getRecord(documentVO.getId());
		return new Document(record, presenterUtils.types()).setPublished(false);
	}

	public boolean isDocumentPublished() {
		Record record = presenterUtils.getRecord(documentVO.getId());
		return new Document(record, presenterUtils.types()).isPublished();
	}

	public void logDownload(RecordVO recordVO) {
		loggingServices.downloadDocument(rmSchemasRecordsServices.get(recordVO.getId()), getCurrentUser());
	}

	public void logOpenDocument(RecordVO recordVO) {
		loggingServices.openDocument(rmSchemasRecordsServices.get(recordVO.getId()), getCurrentUser());
	}

	protected void updateSearchResultClicked() {
		ConstellioUI.getCurrent().setAttribute(SEARCH_EVENT_DWELL_TIME, System.currentTimeMillis());

		SearchEventServices searchEventServices = new SearchEventServices(presenterUtils.getCollection(), presenterUtils.modelLayerFactory());
		Record searchEvent = ConstellioUI.getCurrent().getAttribute(CURRENT_SEARCH_EVENT);

		searchEventServices.incrementClickCounter(searchEvent.getId());
	}
}
