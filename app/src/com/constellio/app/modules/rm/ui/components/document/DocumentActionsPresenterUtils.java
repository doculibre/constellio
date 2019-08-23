package com.constellio.app.modules.rm.ui.components.document;

import com.constellio.app.api.extensions.params.NavigateToFromAPageParams;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.extensions.api.DocumentExtension.DocumentExtensionAddMenuItemParams;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.logging.DecommissioningLoggingService;
import com.constellio.app.modules.rm.ui.builders.DocumentToVOBuilder;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.modules.rm.util.DecommissionNavUtil;
import com.constellio.app.modules.rm.util.RMNavigationUtils;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.RecordVORuntimeException.RecordVORuntimeException_NoSuchMetadata;
import com.constellio.app.ui.framework.builders.ContentVersionToVOBuilder;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.framework.components.contextmenu.BaseContextMenu;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.DateFormatUtils;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.data.utils.TimeProvider;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.contents.ContentConversionManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.logging.SearchEventServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.security.AuthorizationsServices;
import com.vaadin.server.Resource;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import org.apache.commons.io.FilenameUtils;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItemClickEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItemClickListener;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.pages.search.SearchPresenter.CURRENT_SEARCH_EVENT;
import static com.constellio.app.ui.pages.search.SearchPresenter.SEARCH_EVENT_DWELL_TIME;
import static com.constellio.model.entities.schemas.Schemas.LEGACY_ID;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

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
	private transient RMModuleExtensions rmModuleExtensions;
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
		rmModuleExtensions = presenterUtils.appLayerFactory().getExtensions().forCollection(presenterUtils.getCollection())
				.forModule(ConstellioRMModule.ID);
	}

	public DocumentVO getRecordVO() {
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

	public void editDocumentButtonClicked(Map<String, String> params) {
		if (isEditDocumentPossible()) {

			RMNavigationUtils.navigateToEditDocument(documentVO.getId(), params,
					actionsComponent.getConstellioFactories().getAppLayerFactory(),
					actionsComponent.getSessionContext().getCurrentCollection());

			updateSearchResultClicked();
		}
	}


	public void addDocumentToDefaultFavorite() {
		Document document = rmSchemasRecordsServices.wrapDocument(presenterUtils.getRecord(documentVO.getId()));
		if (rmSchemasRecordsServices.numberOfDocumentsInFavoritesReachesLimit(getCurrentUser().getId(), 1)) {
			actionsComponent.showMessage($("DisplayDocumentView.cartCannotContainMoreThanAThousandDocuments"));
		} else {
			document.addFavorite(getCurrentUser().getId());
			try {
				presenterUtils.recordServices().update(document.getWrappedRecord(), RecordUpdateOptions.validationExceptionSafeOptions());
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void removeDocumentFromDefaultFavorites() {
		Document document = rmSchemasRecordsServices.wrapDocument(presenterUtils.getRecord(documentVO.getId()));
		document.removeFavorite(getCurrentUser().getId());
		try {
			presenterUtils.recordServices().update(document.getWrappedRecord(), RecordUpdateOptions.validationExceptionSafeOptions());
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean documentInDefaultFavorites() {
		Record record = presenterUtils.getRecord(documentVO.getId());
		Document document = rmSchemasRecordsServices.wrapDocument(record);
		return document.getFavorites().contains(getCurrentUser().getId());
	}

	public Document renameContentButtonClicked(String newName) {
		if (isEditDocumentPossible()) {
			Record record = presenterUtils.getRecord(documentVO.getId());
			Document document = new Document(record, presenterUtils.types());

			RMSchemasRecordsServices rm = new RMSchemasRecordsServices(record.getCollection(), getModelLayerFactory());
			boolean isManualEntry = rm.folder.title().getDataEntry().getType() == DataEntryType.MANUAL;
			if (document.getContent().getCurrentVersion().getFilename().equals(document.getTitle())) {
				if (isManualEntry && !rm.documentSchemaType().getDefaultSchema().getMetadata(Schemas.TITLE_CODE)
						.getPopulateConfigs().isAddOnly()) {
					document.setTitle(newName);
				}
			} else if (FilenameUtils.removeExtension(document.getContent().getCurrentVersion().getFilename())
					.equals(document.getTitle())) {
				if (isManualEntry && !rm.documentSchemaType().getDefaultSchema().getMetadata(Schemas.TITLE_CODE)
						.getPopulateConfigs().isAddOnly()) {
					document.setTitle(FilenameUtils.removeExtension(newName));
				}
			}

			document.getContent().renameCurrentVersion(newName);
			return document;
		}
		return null;
	}

	public void copyContentButtonClicked(Map<String, String> params) {
		if (isCopyDocumentPossible()) {
			boolean areSearchTypeAndSearchIdPresent = DecommissionNavUtil.areTypeAndSearchIdPresent(params);

			if (areSearchTypeAndSearchIdPresent) {
				actionsComponent.navigate().to(RMViews.class)
						.addDocumentWithContentFromDecommission(documentVO.getId(), DecommissionNavUtil.getSearchId(params), DecommissionNavUtil.getSearchType(params));
			} else if (params.get(RMViews.FAV_GROUP_ID_KEY) != null) {
				actionsComponent.navigate().to(RMViews.class).addDocumentWithContentFromFavorites(documentVO.getId(), params.get(RMViews.FAV_GROUP_ID_KEY));
			} else if (rmModuleExtensions
					.navigateToAddDocumentWhileKeepingTraceOfPreviousView(new NavigateToFromAPageParams(params, documentVO.getId()))) {
			} else {
				actionsComponent.navigate().to(RMViews.class).addDocumentWithContent(documentVO.getId());
			}
		}
	}

	protected ValidationErrors validateDeleteDocumentPossible() {
		ValidationErrors validationErrors = new ValidationErrors();
		boolean userHasDeleteAccess = getCurrentUser().hasDeleteAccess().on(currentDocument());
		if (!userHasDeleteAccess) {
			validationErrors.add(DocumentActionsPresenterUtils.class, "userDoesNotHaveDeleteAccess");
		} else {
			validationErrors = extensions.validateDeleteAuthorized(currentDocument(), getCurrentUser());
		}
		return validationErrors;
	}

	protected ValidationErrors validateDeleteDocumentPossibleExtensively() {
		ValidationErrors validationErrors = new ValidationErrors();
		validationErrors.addAll(validateDeleteDocumentPossible().getValidationErrors());
		validationErrors.addAll(presenterUtils.recordServices().validateLogicallyDeletable(currentDocument(), getCurrentUser()).getValidationErrors());
		return validationErrors;
	}

	private ComponentState getDeleteButtonState() {
		if (validateDeleteDocumentPossible().isEmpty()) {
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
			if (archivisticStatus != null && archivisticStatus.isSemiActive()) {
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

	public void deleteDocumentButtonClicked(Map<String, String> params) {
		if (validateDeleteDocumentPossibleExtensively().isEmpty()) {
			Document document = rmSchemasRecordsServices.getDocument(documentVO.getId());
			String parentId = document.getFolder();
			try {
				presenterUtils.delete(document.getWrappedRecord(), null, true, WAIT_ONE_SECOND);
			} catch (RecordServicesRuntimeException.RecordServicesRuntimeException_CannotLogicallyDeleteRecord e) {
				actionsComponent.showMessage(MessageUtils.toMessage(e));
				return;
			}
			if (parentId != null) {
				navigateToDisplayFolder(parentId, params);
			} else {
				actionsComponent.navigate().to().recordsManagement();
			}
		} else {
			MessageUtils.getCannotDeleteWindow(validateDeleteDocumentPossibleExtensively()).openWindow();
		}
	}

	public void linkToDocumentButtonClicked() {
		// TODO ZeroClipboardComponent
		actionsComponent.showMessage("Clipboard integration TODO!");
	}

	protected boolean isViewAuthorizationPossible() {
		return getCurrentUser().hasAny(RMPermissionsTo.MANAGE_DOCUMENT_AUTHORIZATIONS, RMPermissionsTo.VIEW_DOCUMENT_AUTHORIZATIONS).on(currentDocument());
	}

	protected boolean isCreateDocumentPossible() {
		return getCurrentUser().has(RMPermissionsTo.CREATE_DOCUMENTS).on(currentDocument());
	}

	private ComponentState getViewAuthorizationState() {
		return ComponentState.visibleIf(isViewAuthorizationPossible());
	}

	private ComponentState getCreateDocumentState() {
		return ComponentState.visibleIf(isCreateDocumentPossible());
	}

	private ComponentState getCopyDocumentState() {
		return ComponentState.visibleIf(isCopyDocumentPossible());
	}

	protected boolean isCopyDocumentPossible() {
		if (!rmModuleExtensions.isCopyActionPossibleOnDocument(rmSchemasRecordsServices.wrapDocument(currentDocument), currentUser)) {
			return false;
		}
		return true;
	}

	public ComponentState getCreatePDFAState() {
		return ComponentState.visibleIf(isCreatePDFAPossible());
	}

	protected boolean isCreatePDFAPossible() {
		if (!isCheckOutPossible() || !getAuthorizationServices().canWrite(getCurrentUser(), currentDocument()) ||
			getContent() == null) {
			return false;
		}

		if (!rmModuleExtensions.isCreatePDFAActionPossibleOnDocument(rmSchemasRecordsServices.wrapDocument(currentDocument()), currentUser)) {
			return false;
		}
		return true;
	}

	private ComponentState getShareDocumentState() {
		return ComponentState.visibleIf(isShareDocumentPossible());
	}

	protected boolean isShareDocumentPossible() {
		FolderStatus archivisticStatus = rmSchemasRecordsServices.wrapDocument(currentDocument()).getArchivisticStatus();

		if (!getCurrentUser().has(RMPermissionsTo.SHARE_DOCUMENT).on(currentDocument())) {
			return false;
		}
		if (archivisticStatus == null) {
			return false;
		}
		if (archivisticStatus.isInactive() &&
			!getCurrentUser().has(RMPermissionsTo.SHARE_A_INACTIVE_DOCUMENT).on(currentDocument())) {
			return false;
		}
		if (archivisticStatus.isSemiActive() &&
			!getCurrentUser().has(RMPermissionsTo.SHARE_A_SEMIACTIVE_DOCUMENT).on(currentDocument())) {
			return false;
		}
		if (isNotBlank((String) currentDocument().get(LEGACY_ID)) &&
			!getCurrentUser().has(RMPermissionsTo.SHARE_A_IMPORTED_DOCUMENT).on(currentDocument())) {
			return false;
		}

		if (!rmModuleExtensions.isShareActionPossibleOnDocument(rmSchemasRecordsServices.wrapDocument(currentDocument()), currentUser)) {
			return false;
		}
		return true;
	}

	public void addAuthorizationButtonClicked() {
		if (isViewAuthorizationPossible()) {
			actionsComponent.navigate().to().listObjectAccessAndRoleAuthorizations(documentVO.getId());
			updateSearchResultClicked();
		}
	}

	public void shareDocumentButtonClicked() {
		if (isShareDocumentPossible()) {
			actionsComponent.navigate().to().shareContent(documentVO.getId());
			updateSearchResultClicked();
		}
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
		return getCurrentUser().has(CorePermissions.DELETE_CONTENT_VERSION).on(currentDocument()) &&
			   !extensions.isModifyBlocked(currentDocument(), getCurrentUser()) &&
			   getCurrentUser().hasDeleteAccess().on(currentDocument());
	}

	public boolean isDeleteContentVersionPossible(ContentVersionVO contentVersionVO) {
		return getContent().isDeleteContentVersionPossible(contentVersionVO.getVersion()) &&
			   !extensions.isModifyBlocked(currentDocument(), getCurrentUser());
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
			event.setUserRoles(org.apache.commons.lang3.StringUtils.join(getCurrentUser().getUserRoles().toArray(), "; "));
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

	public synchronized void createPDFA(Map<String, String> params) {
		if (!isCreatePDFAPossible()) {
			return;
		}

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

					navigateToDisplayDocument(document.getId(), params);

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

	public void navigateToDisplayDocument(String documentId, Map<String, String> params) {
		RMNavigationUtils.navigateToDisplayDocument(documentId, params,
				actionsComponent.getConstellioFactories().getAppLayerFactory(),
				actionsComponent.getSessionContext().getCurrentCollection());
	}

	public void navigateToDisplayFolder(String folderId, Map<String, String> params) {
		RMNavigationUtils.navigateToDisplayFolder(folderId, params,
				actionsComponent.getConstellioFactories().getAppLayerFactory(),
				actionsComponent.getSessionContext().getCurrentCollection());
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
				presenterUtils.recordServices().update(record, new RecordUpdateOptions().setOverwriteModificationDateAndUser(false));
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
				presenterUtils.recordServices().update(record, new RecordUpdateOptions().setOverwriteModificationDateAndUser(false));
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
		} else if (isCheckOutNotPossibleDocumentDeleted()) {
			actionsComponent.showErrorMessage($("DocumentActionsComponent.cantCheckOutDocumentDeleted"));
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

	private boolean isDocumentLogicallyDeleted() {
		if (currentDocument().getId() != null) {
			return rmSchemasRecordsServices.getDocument(documentVO.getId()).isLogicallyDeletedStatus();
		} else {
			return true;
		}
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
		return !email && !isDocumentLogicallyDeleted() && (getContent() != null && !isContentCheckedOut());
	}

	protected boolean isCheckOutNotPossibleDocumentDeleted() {
		return !isEmail() && isDocumentLogicallyDeleted() && (getContent() != null && !isContentCheckedOut());
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

	private ComponentState getFinalizeButtonState() {
		return ComponentState.visibleIf(isFinalizePossible());
	}

	protected boolean isFinalizePossible() {
		boolean borrowed = isContentCheckedOut();
		boolean minorVersion;
		Content content = getContent();
		minorVersion = content != null && content.getCurrentVersion().getMinor() != 0;
		if (borrowed || !minorVersion || !extensions.isRecordModifiableBy(currentDocument(), getCurrentUser())) {
			return false;
		}

		if (!rmModuleExtensions.isFinalizeActionPossibleOnDocument(rmSchemasRecordsServices.wrapDocument(currentDocument()), currentUser)) {
			return false;
		}
		return true;
	}

	private ComponentState getPublishButtonState() {
		return ComponentState.visibleIf(isPublishPossible());
	}

	protected boolean isPublishPossible() {
		if (!rmModuleExtensions.isPublishActionPossibleOnDocument(rmSchemasRecordsServices.wrapDocument(currentDocument()), currentUser)) {
			return false;
		}
		return true;
	}

	public void updateActionsComponent() {
		RMConfigs configs = new RMConfigs(getModelLayerFactory().getSystemConfigurationsManager());

		updateBorrowedMessage();
		RecordVO documentVO = getRecordVO();
		actionsComponent.setRecordVO(documentVO);
		Boolean isLogicallyDeleted = documentVO.get(Schemas.LOGICALLY_DELETED_STATUS.getLocalCode());
		if (Boolean.TRUE.equals(isLogicallyDeleted)) {
			actionsComponent.setEditDocumentButtonState(ComponentState.INVISIBLE);
			actionsComponent.setAddDocumentButtonState(ComponentState.INVISIBLE);
			actionsComponent.setDeleteDocumentButtonState(ComponentState.INVISIBLE);
			actionsComponent.setViewAuthorizationButtonState(ComponentState.INVISIBLE);
			actionsComponent.setCreatePDFAButtonState(ComponentState.INVISIBLE);
			actionsComponent.setShareDocumentButtonState(ComponentState.INVISIBLE);
			actionsComponent.setUploadButtonState(ComponentState.INVISIBLE);
			actionsComponent.setCheckInButtonState(ComponentState.INVISIBLE);
			actionsComponent.setCheckOutButtonState(ComponentState.INVISIBLE);
			actionsComponent.setAlertWhenAvailableButtonState(ComponentState.INVISIBLE);
			actionsComponent.setFinalizeButtonState(ComponentState.INVISIBLE);
			actionsComponent.setStartWorkflowButtonState(ComponentState.INVISIBLE);
			actionsComponent.setCartButtonState(ComponentState.INVISIBLE);
			actionsComponent.setAddToOrRemoveFromSelectionButtonState(ComponentState.INVISIBLE);
			actionsComponent.setPublishButtonState(ComponentState.INVISIBLE);
			actionsComponent.setCopyDocumentButtonState(ComponentState.INVISIBLE);
			return;
		}

		actionsComponent.setEditDocumentButtonState(getEditButtonState());
		// THIS IS WHERE I SHOULD USE THE ADD DOCUMENT PERMISSION INSTEAD
		// OH MY GOD WHY ARE WE YELLING LIKE THAT ?
		actionsComponent.setAddDocumentButtonState(getCreateDocumentState());
		actionsComponent.setDeleteDocumentButtonState(getDeleteButtonState());
		actionsComponent.setViewAuthorizationButtonState(getViewAuthorizationState());
		actionsComponent.setCreatePDFAButtonState(getCreatePDFAState());
		actionsComponent.setShareDocumentButtonState(getShareDocumentState());
		actionsComponent.setUploadButtonState(getUploadButtonState());
		actionsComponent.setCheckInButtonState(getCheckInState());
		actionsComponent.setCheckOutButtonState(getCheckOutState());
		actionsComponent.setAlertWhenAvailableButtonState(getAlertWhenAvailableButtonState());
		actionsComponent.setFinalizeButtonState(getFinalizeButtonState());
		actionsComponent.setStartWorkflowButtonState(ComponentState.visibleIf(configs.areWorkflowsEnabled()));
		actionsComponent.setCopyDocumentButtonState(getCopyDocumentState());
		actionsComponent.setPublishButtonState(getPublishButtonState());
	}

	protected void updateBorrowedMessage() {
		if (isContentCheckedOut()) {
			Content content = getContent();
			String borrowDate = DateFormatUtils.format(content.getCheckoutDateTime());
			if (!isCurrentUserBorrower()) {
				String checkoutUserId = content.getCheckoutUserId();
				User user = rmSchemasRecordsServices.getUser(checkoutUserId);
				String borrowerCaption = user.getTitle();
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

	protected User getCurrentUser() {
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

	public void addToCartRequested(Cart cart) {
		if (rmSchemasRecordsServices.numberOfDocumentsInFavoritesReachesLimit(cart.getId(), 1)) {
			actionsComponent.showMessage($("DisplayDocumentView.cartCannotContainMoreThanAThousandDocuments"));
		} else {
			Document document = rmSchemasRecordsServices.getDocument(documentVO.getId());
			document.addFavorite(cart.getId());
			presenterUtils.addOrUpdate(document.getWrappedRecord(), RecordUpdateOptions.validationExceptionSafeOptions());
			actionsComponent.showMessage($("DocumentActionsComponent.addedToCart"));
		}
	}

	public Document publishButtonClicked() {
		if (!isPublishPossible()) {
			return null;
		}
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

	public void addItemsFromExtensions(final MenuItem rootItem, final BaseViewImpl view) {

		final String collection = presenterUtils.getCollection();
		final AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();

		RMModuleExtensions extensions = appLayerFactory.getExtensions().forCollection(collection)
				.forModule(ConstellioRMModule.ID);

		final Record record = currentDocument();

		extensions.addMenuBarButtons(new DocumentExtensionAddMenuItemParams() {
			@Override
			public Document getDocument() {
				RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
				return rm.wrapDocument(record);
			}

			@Override
			public RecordVO getRecordVO() {
				return documentVO;
			}

			@Override
			public BaseViewImpl getView() {
				return view;
			}

			@Override
			public User getUser() {
				return currentUser;
			}

			@Override
			public void registerMenuItem(String caption, Resource icon, final Runnable runnable) {
				MenuItem item = rootItem.addItem(caption, icon, null);
				item.setCommand(new Command() {
					@Override
					public void menuSelected(MenuItem selectedItem) {
						runnable.run();
					}
				});
			}
		});
	}

	public void addItemsFromExtensions(final BaseContextMenu menu, final BaseViewImpl view) {

		final String collection = presenterUtils.getCollection();
		final AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();

		RMModuleExtensions extensions = appLayerFactory.getExtensions().forCollection(collection)
				.forModule(ConstellioRMModule.ID);

		final Record record = currentDocument();

		extensions.addMenuBarButtons(new DocumentExtensionAddMenuItemParams() {
			@Override
			public Document getDocument() {
				RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
				return rm.wrapDocument(record);
			}

			@Override
			public RecordVO getRecordVO() {
				return documentVO;
			}

			@Override
			public BaseViewImpl getView() {
				return view;
			}

			@Override
			public User getUser() {
				return currentUser;
			}

			@Override
			public void registerMenuItem(String caption, Resource icon, final Runnable runnable) {
				ContextMenuItem item = menu.addItem(caption, icon);
				item.addItemClickListener(new ContextMenuItemClickListener() {
					@Override
					public void contextMenuItemClicked(ContextMenuItemClickEvent contextMenuItemClickEvent) {
						runnable.run();
					}
				});
			}
		});

	}

	protected void updateSearchResultClicked() {
		if (Toggle.ADVANCED_SEARCH_CONFIGS.isEnabled()) {
			ConstellioUI.getCurrent().setAttribute(SEARCH_EVENT_DWELL_TIME, System.currentTimeMillis());

			SearchEventServices searchEventServices = new SearchEventServices(presenterUtils.getCollection(),
					presenterUtils.modelLayerFactory());


			SearchEvent searchEvent = ConstellioUI.getCurrentSessionContext().getAttribute(CURRENT_SEARCH_EVENT);

			if (searchEvent != null) {
				searchEventServices.incrementClickCounter(searchEvent.getId());

				String url = null;
				try {
					url = documentVO.get("url");
				} catch (RecordVORuntimeException_NoSuchMetadata e) {
				}
				String clicks = defaultIfBlank(url, documentVO.getId());
				searchEventServices.updateClicks(searchEvent, clicks);
			}
		}
	}

	//	public void addItemsFromExtensions(final MenuItem rootItem, final BaseViewImpl view) {
	//
	//		final String collection = presenterUtils.getCollection();
	//		final AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
	//
	//		RMModuleExtensions extensions = appLayerFactory.getExtensions().forCollection(collection)
	//				.forModule(ConstellioRMModule.ID);
	//
	//		final Record record = currentDocument();
	//
	//		extensions.addMenuBarButtons(new DocumentExtensionAddMenuItemParams() {
	//
	//			@Override
	//			public Document getDocument() {
	//				RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
	//				return rm.wrapDocument(record);
	//			}
	//
	//			@Override
	//			public RecordVO getRecordVO() {
	//				return documentVO;
	//			}
	//
	//			@Override
	//			public BaseViewImpl getView() {
	//				return view;
	//			}
	//
	//			@Override
	//			public User getUser() {
	//				return getCurrentUser();
	//			}
	//
	//			@Override
	//			public void registerMenuItem(String caption, Resource icon, final Runnable runnable) {
	//
	//				MenuItem workflowItem = rootItem.addItem(caption, icon, null);
	//				workflowItem.setCommand(new Command() {
	//					@Override
	//					public void menuSelected(MenuItem selectedItem) {
	//						runnable.run();
	//					}
	//				});
	//			}
	//		});
	//	}

}
