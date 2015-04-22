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
package com.constellio.app.modules.rm.ui.components.document;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.IOException;
import java.io.Serializable;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.builders.DocumentToVOBuilder;
import com.constellio.app.modules.rm.ui.entities.ComponentState;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.ContentVersionToVOBuilder;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.DateFormatUtils;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.security.AuthorizationsServices;

public class DocumentActionsComponentPresenter<T extends DocumentActionsComponent> implements Serializable {

	protected SchemaPresenterUtils presenterUtils;

	protected DocumentToVOBuilder voBuilder = new DocumentToVOBuilder();

	protected ContentVersionToVOBuilder contentVersionVOBuilder = new ContentVersionToVOBuilder();

	protected DocumentVO documentVO;

	protected T actionsComponent;

	private transient RMSchemasRecordsServices rmSchemasRecordsServices;

	public DocumentActionsComponentPresenter(T actionsComponent) {
		this.actionsComponent = actionsComponent;

		ConstellioFactories constellioFactories = actionsComponent.getConstellioFactories();
		SessionContext sessionContext = actionsComponent.getSessionContext();
		presenterUtils = new SchemaPresenterUtils(Document.DEFAULT_SCHEMA, constellioFactories, sessionContext);

		initTransientObjects();
	}

	protected void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	protected void initTransientObjects() {
		rmSchemasRecordsServices = new RMSchemasRecordsServices(presenterUtils.getCollection(),
				presenterUtils.modelLayerFactory());
	}

	public void setRecordVO(RecordVO recordVO) {
		if (recordVO instanceof DocumentVO) {
			this.documentVO = (DocumentVO) recordVO;
		} else {
			this.documentVO = new DocumentVO(recordVO);
		}
	}

	public void backButtonClicked() {
		String parentId = documentVO.get(Document.FOLDER);
		if (parentId != null) {
			actionsComponent.navigateTo().displayFolder(parentId);
		} else {
			actionsComponent.navigateTo().recordsManagement();
		}
	}

	protected boolean isEditDocumentPossible() {
		return getAuthorizationServices().canWrite(getCurrentUser(), currentDocument());
	}
	
	private ComponentState getEditButtonState() {
		Folder parentFolder = rmSchemasRecordsServices.getFolder(currentDocument().getParentId());
		
		if(isEditDocumentPossible()) {
			
			if(parentFolder.getArchivisticStatus().isInactive()) {
				return ComponentState.enabledIf(getCurrentUser().has(RMPermissionsTo.MODIFY_INACTIVE_DOCUMENT).on(currentDocument()));
			} else { 
			
				if(parentFolder.getArchivisticStatus().isSemiActive()){
					return ComponentState.enabledIf(getCurrentUser().has(RMPermissionsTo.MODIFY_SEMIACTIVE_DOCUMENT).on(currentDocument()));	
				} 
				
				return ComponentState.ENABLED;
			}
		} 
		return ComponentState.INVISIBLE;
	}

	public void editDocumentButtonClicked() {
		if (isEditDocumentPossible()) {
			actionsComponent.navigateTo().editDocument(documentVO.getId());
		}
	}

	protected boolean isDeleteDocumentPossible() {
		return getCurrentUser().has(RMPermissionsTo.DELETE_DOCUMENTS).on(currentDocument());

	}

	private ComponentState getDeleteButtonState() {
		Folder parentFolder = rmSchemasRecordsServices.getFolder(currentDocument().getParentId());
		if (isDeleteDocumentPossible()) {
			
			if(parentFolder.getArchivisticStatus().isInactive()) {
				return ComponentState.enabledIf(getCurrentUser().has(RMPermissionsTo.DELETE_INACTIVE_DOCUMENT).on(currentDocument()));
			} else { 
			
				if(parentFolder.getArchivisticStatus().isSemiActive()){
					return ComponentState.enabledIf(getCurrentUser().has(RMPermissionsTo.DELETE_SEMIACTIVE_DOCUMENT).on(currentDocument()));	
				} 
				return ComponentState.ENABLED;
			}
		}
		return ComponentState.INVISIBLE;
	}

	public void deleteDocumentButtonClicked() {
		if (isDeleteDocumentPossible()) {
			Document document = rmSchemasRecordsServices.getDocument(documentVO.getId());
			String parentId = document.getFolder();
			presenterUtils.delete(document.getWrappedRecord(), null);
			if (parentId != null) {
				actionsComponent.navigateTo().displayFolder(parentId);
			} else {
				actionsComponent.navigateTo().recordsManagement();
			}
		}
	}

	public void linkToDocumentButtonClicked() {
		// TODO ZeroClipboardComponent
		actionsComponent.showMessage("Clipboard integration TODO!");
	}

	protected boolean isAddAuthorizationPossible() {
		return getCurrentUser().has(RMPermissionsTo.SHARE_A_DOCUMENT).on(currentDocument());
	}

	private ComponentState getAddAuthorizationState() {
		Folder parentFolder = rmSchemasRecordsServices.getFolder(currentDocument().getParentId());
		
		if(isAddAuthorizationPossible()) {
			
			if(parentFolder.getArchivisticStatus().isInactive()) {
				return ComponentState.enabledIf(getCurrentUser().has(RMPermissionsTo.SHARE_A_INACTIVE_DOCUMENT).on(currentDocument()));
			} else { 
				
				if(parentFolder.getArchivisticStatus().isSemiActive()) {
					return ComponentState.enabledIf(getCurrentUser().has(RMPermissionsTo.SHARE_A_SEMIACTIVE_DOCUMENT).on(currentDocument()));
				}
				
				return ComponentState.ENABLED;
			}
		} 
		return ComponentState.INVISIBLE;
	}

	public void addAuthorizationButtonClicked() {
		if (isAddAuthorizationPossible()) {
			actionsComponent.navigateTo().listObjectAuthorizations(documentVO.getId());
		}
	}

	public void updateWindowClosed() {
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

				ContentVersionVO currentVersionVO = contentVersionVOBuilder.build(content);
				documentVO.setContent(currentVersionVO);

				updateActionsComponent();
				actionsComponent.showMessage($("DocumentActionsComponent.contentVersionDeleted", version));
			} catch (RecordServicesException e) {
				actionsComponent.showErrorMessage(MessageUtils.toMessage(e));
			}
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

                ContentVersionVO currentVersionVO = contentVersionVOBuilder.build(content);
                documentVO.setContent(currentVersionVO);

                updateActionsComponent();
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

				ContentVersionVO currentVersionVO = contentVersionVOBuilder.build(content);
				documentVO.setContent(currentVersionVO);

				updateActionsComponent();
				String newMajorVersion = content.getCurrentVersion().getVersion();
				actionsComponent.showMessage($("DocumentActionsComponent.finalizedVersion", newMajorVersion));
			} catch (RecordServicesException e) {
				actionsComponent.showErrorMessage(MessageUtils.toMessage(e));
			}
		}
	}

	public void checkOutButtonClicked() {
		if (isCheckOutPossible()) {
			Record record = presenterUtils.getRecord(documentVO.getId());
			Document document = new Document(record, presenterUtils.types());
			Content content = document.getContent();
			content.checkOut(presenterUtils.getCurrentUser());
			getModelLayerFactory().newLoggingServices().borrowRecord(record, getCurrentUser());
			try {
				presenterUtils.recordServices().update(record);
				updateActionsComponent();
				String checkedOutVersion = content.getCurrentVersion().getVersion();
				actionsComponent.showMessage($("DocumentActionsComponent.checkedOut", checkedOutVersion));
			} catch (RecordServicesException e) {
				actionsComponent.showErrorMessage(MessageUtils.toMessage(e));
			}
		}
	}

	protected Content getContent() {
		Record record = presenterUtils.getRecord(documentVO.getId());
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

	protected boolean isUploadPossible() {
		boolean checkedOut = isContentCheckedOut();
		boolean borrower = isCurrentUserBorrower();
		return !checkedOut || borrower;
	}
	
	private ComponentState getUploadButtonState() {
		Folder parentFolder = rmSchemasRecordsServices.getFolder(currentDocument().getParentId());
		if(isUploadPossible()) {
			
			if(parentFolder.getArchivisticStatus().isInactive()) {
				return ComponentState.enabledIf(getCurrentUser().has(RMPermissionsTo.UPLOAD_INACTIVE_DOCUMENT).on(currentDocument()));
			} else { 
			
				if(parentFolder.getArchivisticStatus().isSemiActive()){
					return ComponentState.enabledIf(getCurrentUser().has(RMPermissionsTo.UPLOAD_SEMIACTIVE_DOCUMENT).on(currentDocument()));	
				} 
				return ComponentState.ENABLED;
			}
		} 
		return ComponentState.INVISIBLE;
	}


	protected boolean isCheckInPossible() {
		return getContent() != null && isCurrentUserBorrower();
	}

	private ComponentState getCheckInState() {
		if (getAuthorizationServices().canWrite(getCurrentUser(), currentDocument())) {
						
			boolean permissionToReturnOtherUsersDocuments = getCurrentUser().has(RMPermissionsTo.RETURN_OTHER_USERS_DOCUMENTS).on(currentDocument());
			
			if (isCheckInPossible() || (permissionToReturnOtherUsersDocuments && isContentCheckedOut())) {
				return ComponentState.ENABLED;
			}
		}
		return ComponentState.INVISIBLE;
	}

	protected boolean isCheckOutPossible() {
		return getContent() != null && !isContentCheckedOut();
	}

	private ComponentState getCheckOutState() {
		if (getAuthorizationServices().canWrite(getCurrentUser(), currentDocument())) {
			
			if (isCheckOutPossible()) {
				return ComponentState.ENABLED;
			}
		}
		return ComponentState.INVISIBLE;
	}

	protected boolean isCancelCheckOutPossible() {
		return getContent() != null && isContentCheckedOut();
	}

	protected boolean isFinalizePossible() {
		boolean borrowed = isContentCheckedOut();
		boolean minorVersion;
		Content content = getContent();
		minorVersion = content != null && content.getCurrentVersion().getMinor() != 0;
		return !borrowed && minorVersion;
	}

	protected void updateActionsComponent() {
		updateBorrowedMessage();
		actionsComponent.setEditDocumentButtonState(getEditButtonState());
		actionsComponent.setDeleteDocumentButtonState(getDeleteButtonState());
		actionsComponent.setAddAuthorizationButtonState(getAddAuthorizationState());
		actionsComponent.setUploadButtonState(getUploadButtonState());
		actionsComponent.setCheckInButtonState(getCheckInState());
		actionsComponent.setCheckOutButtonState(getCheckOutState());
		actionsComponent.setFinalizeButtonVisible(isFinalizePossible());
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
		return presenterUtils.toRecord(documentVO);
	}

	User getCurrentUser() {
		return presenterUtils.getCurrentUser();
	}

}
