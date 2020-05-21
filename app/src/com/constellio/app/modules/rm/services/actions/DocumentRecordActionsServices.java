package com.constellio.app.modules.rm.services.actions;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.DateFormatUtils;
import com.constellio.data.io.ConversionManager;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.security.AuthorizationsServices;
import org.apache.commons.io.FilenameUtils;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class DocumentRecordActionsServices {

	private RMSchemasRecordsServices rm;
	private RMModuleExtensions rmModuleExtensions;
	private AuthorizationsServices authorizationService;
	private String collection;
	private RecordServices recordServices;
	private transient ModelLayerCollectionExtensions modelLayerCollectionExtensions;

	public DocumentRecordActionsServices(String collection, AppLayerFactory appLayerFactory) {
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.collection = collection;
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.authorizationService = appLayerFactory.getModelLayerFactory().newAuthorizationsServices();
		this.modelLayerCollectionExtensions = appLayerFactory.getModelLayerFactory().getExtensions().forCollection(collection);
		this.rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection).forModule(ConstellioRMModule.ID);
	}

	public String getBorrowedMessage(Record record, User user) {
		String borrowedMessage;
		Document document = rm.wrapDocument(record);
		Content content = document.getContent();
		if (content != null && content.getCheckoutUserId() != null) {
			String borrowDate = DateFormatUtils.format(content.getCheckoutDateTime());
			String checkoutUserId = content.getCheckoutUserId();
			if (!user.getId().equals(checkoutUserId)) {
				String borrowerCaption = rm.getUser(checkoutUserId).getTitle();
				String borrowedMessageKey = "DocumentActionsComponent.borrowedByOtherUser";
				borrowedMessage = $(borrowedMessageKey, borrowerCaption, borrowDate);
			} else {
				String borrowedMessageKey = "DocumentActionsComponent.borrowedByCurrentUser";
				borrowedMessage = $(borrowedMessageKey, borrowDate);
			}
		} else {
			borrowedMessage = null;
		}
		return borrowedMessage;
	}

	public boolean isMoveActionPossible(Record record, User user) {
		return hasUserWriteAccess(record, user) && isEditActionPossible(record, user) &&
			   rmModuleExtensions.isMoveActionPossibleOnDocument(rm.wrapDocument(record), user);
	}

	public boolean isDisplayActionPossible(Record record, User user) {
		return hasUserReadAccess(record, user) &&
			   rmModuleExtensions.isDisplayActionPossibleOnDocument(rm.wrapDocument(record), user);
	}

	public boolean isOpenActionPossible(Record record, User user) {
		Document document = rm.wrapDocument(record);

		return hasUserReadAccess(record, user) && document.getContent() != null
			   && rmModuleExtensions.isOpenActionPossibleOnDocument(rm.wrapDocument(record), user);
	}

	public boolean isEditActionPossible(Record record, User user) {
		return hasUserWriteAccess(record, user) &&
			   !record.isLogicallyDeleted() &&
			   rmModuleExtensions.isEditActionPossibleOnDocument(rm.wrapDocument(record), user);
	}

	public boolean isDownloadActionPossible(Record record, User user) {
		Document document = rm.wrapDocument(record);
		return hasUserReadAccess(record, user) && document.hasContent() &&
			   rmModuleExtensions.isDownloadActionPossibleOnDocument(rm.wrapDocument(record), user);
	}

	public boolean isCopyActionPossible(Record record, User user) {
		return user.hasReadAccess().on(record) &&
			   !record.isLogicallyDeleted() &&
			   rmModuleExtensions.isCopyActionPossibleOnDocument(rm.wrapDocument(record), user);
	}

	public boolean isCreateSipActionPossible(Record record, User user) {
		return hasUserReadAccess(record, user) && user.has(RMPermissionsTo.GENERATE_SIP_ARCHIVES).globally() &&
			   !record.isLogicallyDeleted() &&
			   rmModuleExtensions.isCreateSipActionPossibleOnDocument(rm.wrapDocument(record), user);
	}

	public boolean isSendEmailActionPossible(Record record, User user) {
		Document document = rm.wrapDocument(record);
		return hasUserReadAccess(record, user) && document.hasContent() &&
			   !record.isLogicallyDeleted() &&
			   rmModuleExtensions.isSendEmailActionPossibleOnDocument(rm.wrapDocument(record), user);
	}

	public boolean isUnPublishActionPossible(Record record, User user) {
		Document document = rm.wrapDocument(record);
		return user.has(RMPermissionsTo.PUBLISH_AND_UNPUBLISH_DOCUMENTS)
					   .on(record) && rmModuleExtensions.isUnPublishActionPossibleOnDocument(document, user)
			   && document.isPublished();
	}


	public boolean isGetPublicLinkActionPossible(Record record, User user) {
		Document document = rm.wrapDocument(record);

		return rmModuleExtensions.isGetPublicLinkActionPossibleOnDocument(document, user) &&
			   document.isPublished();
	}

	public boolean isPublishActionPossible(Record record, User user) {
		Document document = rm.wrapDocument(record);
		return user.has(RMPermissionsTo.PUBLISH_AND_UNPUBLISH_DOCUMENTS)
					   .on(record) && rmModuleExtensions.isPublishActionPossibleOnDocument(document, user) &&
			   !record.isLogicallyDeleted() &&
			   document.hasContent() &&
			   !document.isPublished();
	}


	public boolean isPrintLabelActionPossible(Record record, User user) {
		Document document = rm.wrapDocument(record);
		return user.hasReadAccess().on(record) &&
			   !record.isLogicallyDeleted() &&
			   rmModuleExtensions.isPrintLabelActionPossibleOnDocument(document, user);
	}

	public boolean canDeleteDocuments(List<String> ids, User user) {
		for (Record record : recordServices.getRecordsById(collection, ids)) {
			if (!record.getSchemaCode().startsWith(Document.SCHEMA_TYPE)) {
				continue;
			}

			if (!isDeleteActionPossible(record, user)) {
				return false;
			}
		}
		return true;
	}

	public boolean isDeleteActionPossible(Record record, User user) {
		Document document = rm.wrapDocument(record);

		if (!document.isLogicallyDeletedStatus()
			&& user.hasDeleteAccess().on(record) &&
			rmModuleExtensions.isDeleteActionPossbileOnDocument(rm.wrapDocument(record), user)) {
			if (document.isPublished() && !user.has(RMPermissionsTo.DELETE_PUBLISHED_DOCUMENT)
					.on(record)) {
				return false;
			}

			if (getCurrentBorrowerOf(document) != null && !user.has(RMPermissionsTo.DELETE_BORROWED_DOCUMENT)
					.on(record)) {
				return false;
			}
			FolderStatus archivisticStatus = document.getArchivisticStatus();
			if (archivisticStatus != null && archivisticStatus.isInactive()) {
				Folder parentFolder = rm.getFolder(document.getFolder());
				if (parentFolder.getBorrowed() != null && parentFolder.getBorrowed()) {
					return user.has(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER).on(parentFolder)
						   && user.has(RMPermissionsTo.DELETE_INACTIVE_DOCUMENT).on(record);
				}
				return user.has(RMPermissionsTo.DELETE_INACTIVE_DOCUMENT).on(record);
			}
			if (archivisticStatus != null && archivisticStatus.isInactive()) {
				Folder parentFolder = rm.getFolder(document.getFolder());
				if (parentFolder.getBorrowed() != null && parentFolder.getBorrowed()) {
					return user.has(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER).on(parentFolder)
						   && user.has(RMPermissionsTo.DELETE_SEMIACTIVE_DOCUMENT).on(record);
				}
				return user.has(RMPermissionsTo.DELETE_SEMIACTIVE_DOCUMENT).on(record);
			}
			return true;
		}
		return false;
	}

	public boolean isCreatePdfActionPossible(Record record, User user) {
		Document document = rm.getDocument(record.getId());

		if ((!isCheckOutPossible(document) && !isEmailConvertibleToPDF(document)) ||
			document.getContent() == null ||
			!isEditActionPossible(record, user) ||
			record.isLogicallyDeleted()) {
			return false;
		}

		return rmModuleExtensions.isCreatePDFAActionPossibleOnDocument(rm.wrapDocument(record), user);
	}

	private boolean isEmailConvertibleToPDF(Document document) {
		boolean emailConvertibleToPDF;
		if (isEmail(document)) {
			String extension = FilenameUtils.getExtension(document.getContent().getCurrentVersion().getFilename()).toLowerCase();
			ConversionManager conversionManager = rm.getModelLayerFactory().getDataLayerFactory().getConversionManager();
			emailConvertibleToPDF = conversionManager.isSupportedExtension(extension);
		} else {
			emailConvertibleToPDF = false;
		}
		return emailConvertibleToPDF;
	}

	public boolean isAddToCartActionPossible(Record record, User user) {
		return user.hasReadAccess().on(record) &&
			   !record.isLogicallyDeleted() &&
			   (hasUserPermissionToUseCart(user) || hasUserPermissionToUseMyCart(user)) &&
			   rmModuleExtensions.isAddToCartActionPossibleOnDocument(rm.wrapDocument(record), user);
	}

	private boolean hasUserPermissionToUseCart(User user) {
		return user.has(RMPermissionsTo.USE_GROUP_CART).globally();
	}

	private boolean hasUserPermissionToUseMyCart(User user) {
		return user.has(RMPermissionsTo.USE_MY_CART).globally();
	}

	public boolean isAddToSelectionActionPossible(Record record, User user, SessionContext sessionContext) {
		return hasUserReadAccess(record, user) &&
			   !record.isLogicallyDeleted() &&
			   rmModuleExtensions.isAddRemoveToSelectionActionPossibleOnDocument(rm.wrapDocument(record), user);
	}

	public boolean isRemoveToSelectionActionPossible(Record record, User user) {
		return hasUserReadAccess(record, user)
			   && rmModuleExtensions.isAddRemoveToSelectionActionPossibleOnDocument(rm.wrapDocument(record), user);
	}

	private boolean isUploadPossible(Document document, User user) {
		boolean email = isEmail(document);
		boolean checkedOut = isContentCheckedOut(document);
		boolean borrower = isCurrentUserBorrower(user, document.getContent());
		return !email && (!checkedOut || borrower);
	}

	protected boolean isCurrentUserBorrower(User currentUser, Content content) {
		return content != null && currentUser.getId().equals(content.getCheckoutUserId());
	}

	public boolean isUploadActionPossible(Record record, User user) {
		Document document = rm.wrapDocument(record);

		if (!rmModuleExtensions.isUploadActionPossibleOnDocument(rm.wrapDocument(record), user)
			|| !isEditActionPossible(record, user)) {
			return false;
		}

		FolderStatus archivisticStatus = document.getArchivisticStatus();
		if (archivisticStatus != null && isUploadPossible(document, user) && user.hasWriteAccess().on(record)
			&& modelLayerCollectionExtensions.isRecordModifiableBy(record, user)
			&& !modelLayerCollectionExtensions.isModifyBlocked(record, user)) {
			if (archivisticStatus.isInactive()) {
				Folder parentFolder = rm.getFolder(document.getFolder());
				if (parentFolder.getBorrowed() != null && parentFolder.getBorrowed()) {
					return user.has(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER).on(parentFolder)
						   && user.has(RMPermissionsTo.UPLOAD_INACTIVE_DOCUMENT).on(record);
				}
				return (user.has(RMPermissionsTo.UPLOAD_INACTIVE_DOCUMENT).on(record));
			}
			if (archivisticStatus.isSemiActive()) {
				Folder parentFolder = rm.getFolder(document.getFolder());
				if (parentFolder.getBorrowed() != null && parentFolder.getBorrowed()) {
					return user.has(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER).on(parentFolder)
						   && user.has(RMPermissionsTo.UPLOAD_SEMIACTIVE_DOCUMENT).on(record);
				}
				return user.has(RMPermissionsTo.UPLOAD_SEMIACTIVE_DOCUMENT).on(record);
			}
			return true;
		}
		return false;
	}

	public boolean isCheckInActionPossible(Record record, User user) {
		if (user.hasWriteAccess().on(record)) {
			boolean permissionToReturnOtherUsersDocuments = user.has(RMPermissionsTo.RETURN_OTHER_USERS_DOCUMENTS)
					.on(record);
			Document document = rm.wrapDocument(record);
			if (isCheckInPossible(user, document) || (permissionToReturnOtherUsersDocuments && isContentCheckedOut(document))) {
				return true;
			}
		}
		return false;
	}

	private boolean isCheckInPossible(User user, Document document) {
		boolean email = isEmail(document);
		return !email && document.getContent() != null && isCurrentUserBorrower(user, document.getContent());
	}

	public boolean isCheckOutActionPossible(Record record, User user) {
		Document document = rm.wrapDocument(record);

		if (user.hasWriteAccess().on(record)) {
			if (isCheckOutPossible(document) && modelLayerCollectionExtensions.isRecordModifiableBy(record, user) && !modelLayerCollectionExtensions
					.isModifyBlocked(record, user)) {
				return true;
			}
		}
		return false;
	}

	public boolean isCheckOutActionNotPossibleDocumentDeleted(Record record, User user) {
		Document document = rm.wrapDocument(record);

		if (user.hasWriteAccess().on(record)) {
			if (isCheckOutNotPossibleDocumentDeleted(document) && modelLayerCollectionExtensions.isRecordModifiableBy(record, user) && !modelLayerCollectionExtensions
					.isModifyBlocked(record, user)) {
				return true;
			}
		}
		return false;
	}

	public boolean isGenerateReportActionPossible(Record record, User user) {
		return user.hasReadAccess().on(record) &&
			   !record.isLogicallyDeleted() &&
			   rmModuleExtensions.isGenerateReportActionPossibleOnDocument(rm.wrapDocument(record), user);
	}

	public boolean isAddAuthorizationActionPossible(Record record, User user) {
		return user.has(RMPermissionsTo.SHARE_DOCUMENT).on(record) &&
			   !record.isLogicallyDeleted() &&
			   rmModuleExtensions.isAddAuthorizationActionPossibleOnDocument(rm.wrapDocument(record), user);
	}

	public boolean isUnshareActionPossible(Record record, User user) {
		return (authorizationService.itemIsSharedByUser(record, user) ||
				(user.hasAny(RMPermissionsTo.MANAGE_SHARE, RMPermissionsTo.MANAGE_DOCUMENT_AUTHORIZATIONS).on(record) && authorizationService.isRecordShared(record)))
			   && !record.isLogicallyDeleted();
	}

	public boolean isManageAuthorizationActionPossible(Record record, User user) {
		return user.has(RMPermissionsTo.MANAGE_DOCUMENT_AUTHORIZATIONS).on(record) &&
			   user.hasWriteAndDeleteAccess().on(record) &&
			   !record.isLogicallyDeleted() &&
			   rmModuleExtensions.isManageAuthorizationActionPossibleOnDocument(rm.wrapDocument(record), user);
	}

	public boolean isConsultLinkActionPossible(Record record, User user) {
		return user.hasReadAccess().on(record)
			   && rmModuleExtensions.isConsultLinkActionPossibleOnDocument(rm.wrapDocument(record), user);
	}

	public boolean isFinalizeActionPossible(Record record, User user) {
		Document document = rm.wrapDocument(record);

		boolean borrowed = isContentCheckedOut(document.getContent());
		boolean minorVersion;
		Content content = document.getContent();
		minorVersion = content != null && content.getCurrentVersion().getMinor() != 0;
		if (borrowed || !minorVersion || !hasUserWriteAccess(record, user)) {
			return false;
		}

		return rmModuleExtensions.isFinalizeActionPossibleOnDocument(document, user) && isEditActionPossible(record, user);
	}

	public boolean isAvailableAlertActionPossible(Record record, User user) {
		Document document = rm.wrapDocument(record);
		Content content = document.getContent();
		return !isEmail(document) && content != null && content.getCheckoutUserId() != null &&
			   !user.getId().equals(content.getCheckoutUserId());
	}

	public boolean isContentCheckedOut(Content content) {
		return content != null && content.getCheckoutUserId() != null;
	}

	private boolean isCheckOutPossible(Document document) {
		boolean email = isEmail(document);
		return document.getContent() != null &&
			   !document.isLogicallyDeletedStatus() &&
			   !email && !isContentCheckedOut(document.getContent()) &&
			   !isDocumentLogicallyDeleted(document);
	}

	public boolean isCurrentBorrower(Record record, User user) {
		Document document = rm.wrapDocument(record);
		Content content = document.getContent();
		return isContentCheckedOut(document.getContent()) &&
			   content.getCheckoutUserId() != null && user.getId().equals(content.getCheckoutUserId());
	}

	private boolean isCheckOutNotPossibleDocumentDeleted(Document document) {
		boolean email = isEmail(document);
		return document.getContent() != null &&
			   !document.isLogicallyDeletedStatus() &&
			   !email && !isContentCheckedOut(document.getContent()) &&
			   isDocumentLogicallyDeleted(document);
	}

	private boolean isDocumentLogicallyDeleted(Document document) {
		if (document.getId() != null) {
			return rm.getDocument(document.getId()).isLogicallyDeletedStatus();
		} else {
			return true;
		}
	}

	private boolean isContentCheckedOut(Document document) {
		return isContentCheckedOut(document.getContent());
	}

	private String getCurrentBorrowerOf(Document document) {
		return document.getContent() == null ? null : document.getContent().getCheckoutUserId();
	}

	public boolean isCancelCheckOutPossible(Document document) {
		boolean email = isEmail(document);
		return !email && (document.getContent() != null && isContentCheckedOut(document));
	}


	private boolean isEmail(Document document) {
		boolean email;
		if (document.getContent() != null && document.getContent().getCurrentVersion() != null) {
			email = rm.isEmail(document.getContent().getCurrentVersion().getFilename());
		} else {
			email = false;
		}
		return email;
	}

	private boolean hasUserWriteAccess(Record record, User user) {
		return user.hasWriteAccess().on(record);
	}

	private boolean hasUserReadAccess(Record record, User user) {
		return user.hasReadAccess().on(record);
	}

	public boolean isCreateTaskActionPossible(Record record, User user) {
		return hasUserReadAccess(record, user);
	}
}
