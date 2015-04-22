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
package com.constellio.app.modules.rm.constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.data.utils.KeyListMap;

public class RMPermissionsTo {

	private static List<String> ALL_PERMISSIONS = new ArrayList<>();
	private static KeyListMap<String, String> PERMISSIONS_GROUPED = new KeyListMap<>();

	private static String permission(String groupLocalCode, String permissionLocalCode) {
		String groupCode = ConstellioRMModule.ID + "." + groupLocalCode;
		String permissionCode = ConstellioRMModule.ID + "." + permissionLocalCode;
		if (ALL_PERMISSIONS.contains(permissionCode)) {
			throw new Error("Cannot add a same permission twice.");
		}
		ALL_PERMISSIONS.add(permissionCode);
		PERMISSIONS_GROUPED.add(groupCode, permissionCode);
		return permissionCode;
	}

	public static Map<String, List<String>> getGroupedPermissions() {
		return Collections.unmodifiableMap(PERMISSIONS_GROUPED.getNestedMap());
	}

	public static List<String> getAllPermissions() {
		return Collections.unmodifiableList(ALL_PERMISSIONS);
	}

	// --------------------------------------------

	// Folders
	private static final String FOLDER_GROUP = "folders";

	public static final String SHARE_A_FOLDER = permission(FOLDER_GROUP, "shareFolders");
	public static final String SHARE_A_SEMIACTIVE_FOLDER = permission(FOLDER_GROUP, "shareSemiActiveFolders");
	public static final String SHARE_A_INACTIVE_FOLDER = permission(FOLDER_GROUP, "shareInactiveFolders");

	public static final String CREATE_SUB_FOLDERS = permission(FOLDER_GROUP, "createSubFolders");
	public static final String CREATE_SUB_FOLDERS_IN_SEMIACTIVE_FOLDERS = permission(FOLDER_GROUP,
			"createSubFoldersInSemiActiveFolders");
	public static final String CREATE_SUB_FOLDERS_IN_INACTIVE_FOLDERS = permission(FOLDER_GROUP,
			"createSubFoldersInInactiveFolders");

	public static final String MODIFY_FOLDERS = permission(FOLDER_GROUP, "modifyFolders");
	public static final String MODIFY_SEMIACTIVE_FOLDERS = permission(FOLDER_GROUP, "modifySemiActiveFolders");
	public static final String MODIFY_INACTIVE_FOLDERS = permission(FOLDER_GROUP, "modifyInactiveFolders");

	public static final String DELETE_FOLDERS = permission(FOLDER_GROUP, "deleteFolders");
	public static final String DELETE_SEMIACTIVE_FOLDERS = permission(FOLDER_GROUP, "deleteSemiActiveFolders");
	public static final String DELETE_INACTIVE_FOLDERS = permission(FOLDER_GROUP, "deleteInactiveFolders");

	public static final String CREATE_FOLDERS = permission(FOLDER_GROUP, "createFolders");
	
	public static final String DUPLICATE_SEMIACTIVE_FOLDER = permission(FOLDER_GROUP, "duplicateSemiActiveFolders");
	public static final String DUPLICATE_INACTIVE_FOLDER = permission(FOLDER_GROUP, "duplicateInactiveFolders");

	// Documents
	private static final String DOCUMENT_GROUP = "documents";

	public static final String SHARE_A_DOCUMENT = permission(DOCUMENT_GROUP, "shareDocuments");
	public static final String SHARE_A_SEMIACTIVE_DOCUMENT = permission(DOCUMENT_GROUP, "shareSemiActiveDocuments");
	public static final String SHARE_A_INACTIVE_DOCUMENT = permission(DOCUMENT_GROUP, "shareInactiveDocuments");

	public static final String CREATE_DOCUMENTS = permission(DOCUMENT_GROUP, "createDocuments");
	public static final String CREATE_SEMIACTIVE_DOCUMENT = permission(DOCUMENT_GROUP, "createSemiActiveDocuments");
	public static final String CREATE_INACTIVE_DOCUMENT = permission(DOCUMENT_GROUP, "createInactiveDocuments");

	public static final String MODIFY_DOCUMENTS = permission(DOCUMENT_GROUP, "modifyDocuments");
	public static final String MODIFY_SEMIACTIVE_DOCUMENT = permission(DOCUMENT_GROUP, "modifySemiActiveDocuments");
	public static final String MODIFY_INACTIVE_DOCUMENT = permission(DOCUMENT_GROUP, "modifyInactiveDocuments");
	
	public static final String UPLOAD_SEMIACTIVE_DOCUMENT = permission(DOCUMENT_GROUP, "uploadSemiActiveDocuments");
	public static final String UPLOAD_INACTIVE_DOCUMENT = permission(DOCUMENT_GROUP, "uploadInactiveDocuments");

	public static final String DELETE_DOCUMENTS = permission(DOCUMENT_GROUP, "deleteDocuments");
	public static final String DELETE_SEMIACTIVE_DOCUMENT = permission(DOCUMENT_GROUP, "deleteSemiActiveDocuments");
	public static final String DELETE_INACTIVE_DOCUMENT = permission(DOCUMENT_GROUP, "deleteInactiveDocuments");

	public static final String RETURN_OTHER_USERS_DOCUMENTS = permission(DOCUMENT_GROUP, "returnOtherUsersDocuments");

	//Decommissioning
	private static final String DECOMMISSIONING = "decommissioning";
	public static final String MODIFY_FOLDER_DECOMMISSIONING_DATES = permission(DECOMMISSIONING, "modifyFolderDecomDate");
	public static final String EDIT_DECOMMISSIONING_LIST = permission(DECOMMISSIONING, "editDecommissioningList");
	public static final String PROCESS_DECOMMISSIONING_LIST = permission(DECOMMISSIONING, "processDecommissioningList");

	public static final String MANAGE_REPORTS = permission("management", "manageReports");
	public static final String MANAGE_DECOMMISSIONING = permission(DECOMMISSIONING, "decommissioning");
	public static final String MANAGE_CONTAINERS = permission(DECOMMISSIONING, "manageContainers");
	public static final String MANAGE_ROBOTS = permission(DECOMMISSIONING, "manageRobots");
}
