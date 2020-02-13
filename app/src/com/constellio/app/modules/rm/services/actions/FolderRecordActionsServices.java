package com.constellio.app.modules.rm.services.actions;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.services.records.RecordServices;

import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotBlank;

public class FolderRecordActionsServices {

	private AppLayerFactory appLayerFactory;
	private RMSchemasRecordsServices rm;
	private ModelLayerCollectionExtensions extensions;
	private RMModuleExtensions rmModuleExtensions;
	private String collection;
	private RecordServices recordServices;
	private BorrowingServices borrowingServices;

	public FolderRecordActionsServices(String collection, AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.collection = collection;
		recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		borrowingServices = new BorrowingServices(collection, appLayerFactory.getModelLayerFactory());
		extensions = appLayerFactory.getModelLayerFactory().getExtensions().forCollection(collection);
		rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection).forModule(ConstellioRMModule.ID);
	}

	public boolean isAddDocumentActionPossible(Record record, User user) {
		Folder folder = rm.wrapFolder(record);
		if (user.hasWriteAccess().on(folder) && isEditActionPossible(record, user) &&
			rmModuleExtensions.isAddDocumentActionPossibleOnFolder(rm.wrapFolder(record), user) &&
			user.has(RMPermissionsTo.CREATE_DOCUMENTS).on(folder)) {
			if (folder.getPermissionStatus().isInactive()) {
				if (folder.getBorrowed() != null && folder.getBorrowed()) {
					return user.has(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER).on(folder) &&
						   user.has(RMPermissionsTo.CREATE_INACTIVE_DOCUMENT).on(folder);
				}
				return user.has(RMPermissionsTo.CREATE_INACTIVE_DOCUMENT).on(folder);
			}
			if (folder.getPermissionStatus().isSemiActive()) {
				if (folder.getBorrowed() != null && folder.getBorrowed()) {
					return user.has(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER).on(folder) &&
						   user.has(RMPermissionsTo.CREATE_SEMIACTIVE_DOCUMENT).on(folder);
				}
				return user.has(RMPermissionsTo.CREATE_SEMIACTIVE_DOCUMENT).on(folder);
			}
			return true;
		}
		return false;
	}

	public boolean isMoveActionPossible(Record record, User user) {
		return hasUserWriteAccess(record, user) && isEditActionPossible(record, user) &&
			   rmModuleExtensions.isMoveActionPossibleOnFolder(rm.wrapFolder(record), user);
	}

	public boolean isAddSubFolderActionPossible(Record record, User user) {
		Folder folder = rm.wrapFolder(record);
		if (user.hasWriteAccess().on(folder) && isEditActionPossible(record, user) &&
			rmModuleExtensions.isAddSubFolderActionPossibleOnFolder(rm.wrapFolder(record), user) &&
			user.hasAll(RMPermissionsTo.CREATE_SUB_FOLDERS, RMPermissionsTo.CREATE_FOLDERS).on(folder)) {
			if (folder.getPermissionStatus().isInactive()) {
				if (folder.getBorrowed() != null && folder.getBorrowed()) {
					return user.has(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER).on(folder) && user
							.has(RMPermissionsTo.CREATE_SUB_FOLDERS_IN_INACTIVE_FOLDERS).on(folder);
				}
				return user.has(RMPermissionsTo.CREATE_SUB_FOLDERS_IN_INACTIVE_FOLDERS).on(folder);
			}
			if (folder.getPermissionStatus().isSemiActive()) {
				if (folder.getBorrowed() != null && folder.getBorrowed()) {
					return user.has(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER).on(folder) && user
							.has(RMPermissionsTo.CREATE_SUB_FOLDERS_IN_SEMIACTIVE_FOLDERS).on(folder);
				}
				return user.has(RMPermissionsTo.CREATE_SUB_FOLDERS_IN_SEMIACTIVE_FOLDERS).on(folder);
			}
			return true;
		}
		return false;
	}

	public boolean isDisplayActionPossible(Record record, User user) {
		return hasUserReadAccess(record, user) &&
			   rmModuleExtensions.isDisplayActionPossibleOnFolder(rm.wrapFolder(record), user);
	}

	public boolean isEditActionPossible(Record record, User user) {
		Folder folder = rm.wrapFolder(record);
		if (isNotBlank(folder.getLegacyId()) && !user.has(RMPermissionsTo.MODIFY_IMPORTED_FOLDERS).on(folder)) {
			return false;
		}
		return hasUserWriteAccess(record, user) &&
			   !record.isLogicallyDeleted() &&
			   rmModuleExtensions.isEditActionPossibleOnFolder(folder, user) &&
			   !extensions.isModifyBlocked(folder.getWrappedRecord(), user) &&
			   extensions.isRecordModifiableBy(folder.getWrappedRecord(), user);
	}

	public boolean isDeleteActionPossible(Record record, User user) {
		Folder folder = rm.wrapFolder(record);
		if (user.hasDeleteAccess().on(record) && rmModuleExtensions.isDeleteActionPossibleOnFolder(folder, user)) {
			if (folder.getPermissionStatus().isInactive()) {
				if (folder.getBorrowed() != null && folder.getBorrowed()) {
					return user.has(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER).on(folder) && user
							.has(RMPermissionsTo.DELETE_INACTIVE_FOLDERS).on(folder);
				}
				return user.has(RMPermissionsTo.DELETE_INACTIVE_FOLDERS).on(folder);
			}
			if (folder.getPermissionStatus().isSemiActive()) {
				if (folder.getBorrowed() != null && folder.getBorrowed()) {
					return user.has(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER).on(folder) && user
							.has(RMPermissionsTo.DELETE_SEMIACTIVE_FOLDERS).on(folder);
				}
				return user.has(RMPermissionsTo.DELETE_SEMIACTIVE_FOLDERS).on(folder);
			}
			return true;
		}
		return false;
	}

	public boolean canDeleteFolders(List<String> ids, User user) {
		for (Record record : recordServices.getRecordsById(collection, ids)) {
			if (!record.getSchemaCode().startsWith(Folder.SCHEMA_TYPE)) {
				continue;
			}

			if (!isDeleteActionPossible(record, user)) {
				return false;
			}
		}
		return true;
	}

	public boolean isConsultLinkActionPossible(Record record, User user) {
		return hasUserReadAccess(record, user) && rmModuleExtensions.isConsultLinkActionPossibleOnFolder(rm.wrapFolder(record), user);
	}

	public boolean isCopyActionPossible(Record record, User user) {
		Folder folder = rm.wrapFolder(record);
		if (!hasUserReadAccess(record, user) ||
			(folder.getPermissionStatus().isInactive() && !user.has(RMPermissionsTo.DUPLICATE_INACTIVE_FOLDER).on(folder)) ||
			(folder.getPermissionStatus().isSemiActive() && !user.has(RMPermissionsTo.DUPLICATE_SEMIACTIVE_FOLDER).on(folder))) {
			return false;
		}
		return rmModuleExtensions.isCopyActionPossibleOnFolder(rm.wrapFolder(record), user);
	}

	public boolean isDownloadActionPossible(Record record, User user) {
		Folder folder = rm.wrapFolder(record);
		return hasUserReadAccess(record, user) && folder.hasContent() &&
			   rmModuleExtensions.isDownloadActionPossibleOnFolder(rm.wrapFolder(record), user);
	}

	public boolean isCreateSipActionPossible(Record record, User user) {
		return hasUserReadAccess(record, user) && user.has(RMPermissionsTo.GENERATE_SIP_ARCHIVES).globally() &&
			   !record.isLogicallyDeleted() &&
			   rmModuleExtensions.isCreateSipActionPossibleOnFolder(rm.wrapFolder(record), user);
	}

	public boolean isCreateDecommissioningListActionPossible(Record record, User user) {
		return hasUserReadAccess(record, user) && user.has(RMPermissionsTo.CREATE_DECOMMISSIONING_LIST).globally() &&
			   !record.isLogicallyDeleted() &&
			   rmModuleExtensions.isCreateDecommissioningListActionPossibleOnFolder(rm.wrapFolder(record), user);
	}

	// linkTo

	public boolean isAddAuthorizationActionPossible(Record record, User user) {
		return isEditActionPossible(record, user) &&
			   user.has(RMPermissionsTo.MANAGE_FOLDER_AUTHORIZATIONS).on(record) &&
			   user.hasWriteAndDeleteAccess().on(record) &&
			   !record.isLogicallyDeleted() &&
			   rmModuleExtensions.isAddAuthorizationActionPossibleOnFolder(rm.wrapFolder(record), user);
	}

	public boolean isShareActionPossible(Record record, User user) {
		Folder folder = rm.wrapFolder(record);
		if (!hasUserWriteAccess(record, user) || !user.has(RMPermissionsTo.SHARE_FOLDER).on(folder) ||
			(folder.getPermissionStatus().isInactive() && !user.has(RMPermissionsTo.SHARE_A_INACTIVE_FOLDER).on(folder)) ||
			(folder.getPermissionStatus().isSemiActive() && !user.has(RMPermissionsTo.SHARE_A_SEMIACTIVE_FOLDER).on(folder)) ||
			(record.isLogicallyDeleted()) ||
			(isNotBlank(folder.getLegacyId()) && !user.has(RMPermissionsTo.SHARE_A_IMPORTED_FOLDER).on(folder))) {
			return false;
		}
		return rmModuleExtensions.isShareActionPossibleOnFolder(rm.wrapFolder(record), user);
	}

	public boolean isAddToCartActionPossible(Record record, User user) {
		return hasUserReadAccess(record, user) &&
			   !record.isLogicallyDeleted() &&
			   (hasUserPermissionToUseCart(user) || hasUserPermissionToUseMyCart(user)) &&
			   rmModuleExtensions.isAddToCartActionPossibleOnFolder(rm.wrapFolder(record), user);
	}

	public boolean isBorrowActionPossible(Record record, User user) {
		Folder folder = rm.wrapFolder(record);
		try {
			borrowingServices.validateCanBorrow(user, folder, null);
		} catch (Exception e) {
			return false;
		}
		return user.hasAll(RMPermissionsTo.BORROW_FOLDER, RMPermissionsTo.BORROWING_FOLDER_DIRECTLY).on(folder) &&
			   !record.isLogicallyDeleted() &&
			   rmModuleExtensions.isBorrowingActionPossibleOnFolder(folder, user);
	}

	public boolean isBorrowRequestActionPossible(Record record, User user) {
		Folder folder = rm.wrapFolder(record);

		ContainerRecord containerRecord = null;
		if (folder.getContainer() != null) {
			containerRecord = rm.getContainerRecord(folder.getContainer());
		}

		return isFolderBorrowable(folder, containerRecord, user, collection);
	}

	private boolean isFolderBorrowable(Folder folder, ContainerRecord container, User currentUser, String collection) {
		if (folder != null) {
			try {
				this.borrowingServices.validateCanBorrow(currentUser, folder, TimeProvider.getLocalDate());
			} catch (Exception e) {
				return false;
			}
		}
		RMModuleExtensions rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection).forModule(ConstellioRMModule.ID);

		return folder != null && currentUser.hasAll(RMPermissionsTo.BORROW_FOLDER, RMPermissionsTo.BORROWING_REQUEST_ON_FOLDER)
				.on(folder)
			   && !(container != null && Boolean.TRUE.equals(container.getBorrowed())) && rmModuleExtensions.isBorrowingActionPossibleOnFolder(folder, currentUser);
	}

	public boolean isReturnActionPossible(Record record, User user) {
		Folder folder = rm.wrapFolder(record);
		try {
			borrowingServices.validateCanReturnFolder(user, folder);
		} catch (Exception e) {
			return false;
		}
		return rmModuleExtensions.isReturnActionPossibleOnFolder(rm.wrapFolder(record), user);
	}

	public boolean isReturnRequestActionPossible(Record record, User user) {
		Folder folder = rm.wrapFolder(record);
		return folder != null && Boolean.TRUE.equals(folder.getBorrowed())
			   && user.getId().equals(folder.getBorrowUserEntered());
	}

	public boolean isSendReturnReminderActionPossible(Record record, User user) {
		Folder folder = rm.wrapFolder(record);
		return Boolean.TRUE.equals(folder.getBorrowed()) &&
			   !user.getId().equals(folder.getBorrowUserEntered());
	}

	public boolean isPrintLabelActionPossible(Record record, User user) {
		Folder folder = rm.wrapFolder(record);
		if (hasUserReadAccess(record, user) && rmModuleExtensions.isPrintLabelActionPossibleOnFolder(folder, user)) {
			if (folder.getPermissionStatus().isInactive()) {
				if (folder.getBorrowed() != null && folder.getBorrowed()) {
					return user.has(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER).on(folder) &&
						   user.has(RMPermissionsTo.MODIFY_INACTIVE_FOLDERS).on(folder);
				}
				return user.has(RMPermissionsTo.MODIFY_INACTIVE_FOLDERS).on(folder);
			}
			if (folder.getPermissionStatus().isSemiActive()) {
				if (folder.getBorrowed() != null && folder.getBorrowed()) {
					return user.has(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER).on(folder) &&
						   user.has(RMPermissionsTo.MODIFY_SEMIACTIVE_FOLDERS).on(folder);
				}
				return user.has(RMPermissionsTo.MODIFY_SEMIACTIVE_FOLDERS).on(folder);
			}
			return true;
		}
		return false;
	}

	public boolean isGenerateReportActionPossible(Record record, User user) {
		return hasUserWriteAccess(record, user) &&
			   !record.isLogicallyDeleted() &&
			   rmModuleExtensions.isGenerateReportActionPossibleOnFolder(rm.wrapFolder(record), user);
	}

	/*
			linkToFolderButton = new LinkButton($("DisplayFolderView.linkToFolder")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					presenter.linkToFolderButtonClicked();
				}
			};
			linkToFolderButton.setVisible(false);
	 */

	private boolean hasUserWriteAccess(Record record, User user) {
		return user.hasWriteAccess().on(record);
	}

	private boolean hasUserReadAccess(Record record, User user) {
		return user.hasReadAccess().on(record);
	}

	private boolean hasUserPermissionToUseCart(User user) {
		return user.has(RMPermissionsTo.USE_GROUP_CART).globally();
	}

	private boolean hasUserPermissionToUseMyCart(User user) {
		return user.has(RMPermissionsTo.USE_MY_CART).globally();
	}

}
