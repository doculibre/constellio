package com.constellio.app.modules.rm.constants;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.model.entities.Permissions;

import java.util.List;

public class RMPermissionsTo {
	public static Permissions PERMISSIONS = new Permissions(ConstellioRMModule.ID);

	private static String permission(String group, String permission) {
		return PERMISSIONS.add(group, permission);
	}

	// --------------------------------------------

	// Folders
	private static final String FOLDER_GROUP = "folders";

	public static final String MANAGE_FOLDER_AUTHORIZATIONS = permission(FOLDER_GROUP, "manageFolderAuthorizations");
	public static final String VIEW_FOLDER_AUTHORIZATIONS = permission(FOLDER_GROUP, "viewFolderAuthorizations");

	public static final String SHARE_FOLDER = permission(FOLDER_GROUP, "shareFolders");
	public static final String SHARE_A_SEMIACTIVE_FOLDER = permission(FOLDER_GROUP, "shareSemiActiveFolders");
	public static final String SHARE_A_INACTIVE_FOLDER = permission(FOLDER_GROUP, "shareInactiveFolders");
	public static final String SHARE_A_IMPORTED_FOLDER = permission(FOLDER_GROUP, "shareImportedFolders");

	public static final String CREATE_SUB_FOLDERS = permission(FOLDER_GROUP, "createSubFolders");
	public static final String CREATE_SUB_FOLDERS_IN_SEMIACTIVE_FOLDERS = permission(FOLDER_GROUP,
			"createSubFoldersInSemiActiveFolders");
	public static final String CREATE_SUB_FOLDERS_IN_INACTIVE_FOLDERS = permission(FOLDER_GROUP,
			"createSubFoldersInInactiveFolders");
	public static final String BORROW_FOLDER = permission(FOLDER_GROUP, "borrowFolder");

	public static final String MODIFY_SEMIACTIVE_FOLDERS = permission(FOLDER_GROUP, "modifySemiActiveFolders");
	public static final String MODIFY_INACTIVE_FOLDERS = permission(FOLDER_GROUP, "modifyInactiveFolders");
	public static final String MODIFY_IMPORTED_FOLDERS = permission(FOLDER_GROUP, "modifyImportedFolders");

	public static final String DELETE_SEMIACTIVE_FOLDERS = permission(FOLDER_GROUP, "deleteSemiActiveFolders");
	public static final String DELETE_INACTIVE_FOLDERS = permission(FOLDER_GROUP, "deleteInactiveFolders");

	public static final String CREATE_FOLDERS = permission(FOLDER_GROUP, "createFolders");

	public static final String DUPLICATE_SEMIACTIVE_FOLDER = permission(FOLDER_GROUP, "duplicateSemiActiveFolders");
	public static final String DUPLICATE_INACTIVE_FOLDER = permission(FOLDER_GROUP, "duplicateInactiveFolders");

	public static final String MODIFY_SEMIACTIVE_BORROWED_FOLDER = permission(FOLDER_GROUP,
			"modifySemiActiveBorrowedFolder");
	public static final String MODIFY_INACTIVE_BORROWED_FOLDER = permission(FOLDER_GROUP,
			"modifyInactiveBorrowedFolder");
	public static final String MODIFY_OPENING_DATE_FOLDER = permission(FOLDER_GROUP,
			"modifyOpeningDateFolder");
	public static final String BORROWING_FOLDER_DIRECTLY = permission(FOLDER_GROUP, "borrowingFolderDirectly");

	public static final String BORROWING_REQUEST_ON_FOLDER = permission(FOLDER_GROUP, "borrowingRequestOnFolder");

	public static final String REACTIVATION_REQUEST_ON_FOLDER = permission(FOLDER_GROUP, "reactivationRequestOnFolder");

	public static final String MANAGE_REQUEST_ON_FOLDER = permission(FOLDER_GROUP, "manageRequestOnFolder");

	public static final String RETURN_OTHER_USERS_FOLDERS = permission(FOLDER_GROUP, "returnOtherUsersFolders");

	// Documents
	private static final String DOCUMENT_GROUP = "documents";

	public static final String MANAGE_DOCUMENT_AUTHORIZATIONS = permission(DOCUMENT_GROUP, "manageDocumentAuthorizations");
	public static final String VIEW_DOCUMENT_AUTHORIZATIONS = permission(DOCUMENT_GROUP, "viewDocumentAuthorizations");
	public static final String SHARE_DOCUMENT = permission(DOCUMENT_GROUP, "shareDocuments");
	public static final String SHARE_A_SEMIACTIVE_DOCUMENT = permission(DOCUMENT_GROUP, "shareSemiActiveDocuments");
	public static final String SHARE_A_INACTIVE_DOCUMENT = permission(DOCUMENT_GROUP, "shareInactiveDocuments");
	public static final String SHARE_A_IMPORTED_DOCUMENT = permission(DOCUMENT_GROUP, "shareImportedDocuments");

	public static final String CREATE_DOCUMENTS = permission(DOCUMENT_GROUP, "createDocuments");
	public static final String CREATE_SEMIACTIVE_DOCUMENT = permission(DOCUMENT_GROUP, "createSemiActiveDocuments");
	public static final String CREATE_INACTIVE_DOCUMENT = permission(DOCUMENT_GROUP, "createInactiveDocuments");

	public static final String MODIFY_SEMIACTIVE_DOCUMENT = permission(DOCUMENT_GROUP, "modifySemiActiveDocuments");
	public static final String MODIFY_INACTIVE_DOCUMENT = permission(DOCUMENT_GROUP, "modifyInactiveDocuments");
	public static final String MODIFY_IMPORTED_DOCUMENTS = permission(DOCUMENT_GROUP, "modifyImportedDocuments");

	public static final String UPLOAD_SEMIACTIVE_DOCUMENT = permission(DOCUMENT_GROUP, "uploadSemiActiveDocuments");
	public static final String UPLOAD_INACTIVE_DOCUMENT = permission(DOCUMENT_GROUP, "uploadInactiveDocuments");

	public static final String DELETE_SEMIACTIVE_DOCUMENT = permission(DOCUMENT_GROUP, "deleteSemiActiveDocuments");
	public static final String DELETE_INACTIVE_DOCUMENT = permission(DOCUMENT_GROUP, "deleteInactiveDocuments");
	public static final String DELETE_BORROWED_DOCUMENT = permission(DOCUMENT_GROUP, "deleteBorrowedDocuments");
	public static final String DELETE_PUBLISHED_DOCUMENT = permission(DOCUMENT_GROUP, "deletePublishedDocuments");

	public static final String RETURN_OTHER_USERS_DOCUMENTS = permission(DOCUMENT_GROUP, "returnOtherUsersDocuments");

	public static final String PUBLISH_AND_UNPUBLISH_DOCUMENTS = permission(DOCUMENT_GROUP, "publishAndUnpublishDocuments");

	public static final String VIEW_SYSTEM_FILENAME = permission(DOCUMENT_GROUP, "viewSystemFilename");

	// Decommissioning
	private static final String DECOMMISSIONING = "decommissioning";

	public static final String MODIFY_FOLDER_DECOMMISSIONING_DATES = permission(DECOMMISSIONING, "modifyFolderDecomDate");
	public static final String EDIT_DECOMMISSIONING_LIST = permission(DECOMMISSIONING, "editDecommissioningList");
	public static final String PROCESS_DECOMMISSIONING_LIST = permission(DECOMMISSIONING, "processDecommissioningList");

	public static final String MANAGE_REPORTS = permission("management", "manageReports");
	public static final String APPROVE_DECOMMISSIONING_LIST = permission(DECOMMISSIONING, "decommissioning");
	public static final String MANAGE_CONTAINERS = permission(DECOMMISSIONING, "manageContainers");
	public static final String DELETE_CONTAINERS = permission(DECOMMISSIONING, "deleteContainers");
	public static final String DISPLAY_CONTAINERS = permission(DECOMMISSIONING, "displayContainers");

	public static final String BORROW_CONTAINER = permission(DECOMMISSIONING, "borrowContainer");
	public static final String BORROWING_REQUEST_ON_CONTAINER = permission(DECOMMISSIONING, "borrowingRequestOnContainer");
	public static final String MANAGE_REQUEST_ON_CONTAINER = permission(DECOMMISSIONING, "manageRequestOnContainer");

	public static final String CREATE_TRANSFER_DECOMMISSIONING_LIST = permission(DECOMMISSIONING, "createActiveFolderToSemiActiveDecommissioningList");
	public static final String EDIT_TRANSFER_DECOMMISSIONING_LIST = permission(DECOMMISSIONING, "editActiveFolderToSemiActiveDecommissioningList");
	public static final String GENERATE_SIP_ARCHIVES = permission(DECOMMISSIONING, "generateSIPArchives");
	//BagInfo
	public static final String MANAGE_BAG_INFO = permission(DECOMMISSIONING, "manageBagInfo");

	// Cart
	private static final String CART_GROUP = "carts";

	public static final String USE_CART = permission(CART_GROUP, "useCart");

	// RM Module management
	private static final String RM_MANAGEMENT = "rmManagement";

	public static final String MANAGE_UNIFORMSUBDIVISIONS = permission(RM_MANAGEMENT, "manageUniformSubdivisions");
	public static final String MANAGE_RETENTIONRULE = permission(RM_MANAGEMENT, "manageRetentionRule");
	public static final String MANAGE_CLASSIFICATION_PLAN = permission(RM_MANAGEMENT, "manageClassificationPlan");
	public static final String MANAGE_STORAGE_SPACES = permission(RM_MANAGEMENT, "manageStorageSpaces");


	public static final List<String> RM_COLLECTION_MANAGEMENT_PERMISSIONS = PERMISSIONS.getGroup(RM_MANAGEMENT);
}
