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
package com.constellio.model.entities;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.constellio.data.utils.KeyListMap;

public class CorePermissions {

	private static List<String> ALL_PERMISSIONS = new ArrayList<>();
	private static KeyListMap<String, String> PERMISSIONS_GROUPED = new KeyListMap<>();

	private static String permission(String groupLocalCode, String permissionLocalCode) {
		String groupCode = "core." + groupLocalCode;
		String permissionCode = "core." + permissionLocalCode;
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

	public static final String DELETE_CONTENT_VERSION = permission("system", "deleteContentVersion");

	public static final String VIEW_EVENTS = permission("system", "viewEvents");

	public static final String MANAGE_TAXONOMIES = permission("management.collection", "manageTaxonomies");
	public static final String MANAGE_VALUELIST = permission("management.collection", "manageValueList");
	public static final String MANAGE_METADATASCHEMAS = permission("management.collection", "manageMetadataSchemas");
	public static final String MANAGE_SECURITY = permission("management.collection", "manageSecurity");
	//public static final String MANAGE_ROLES = permission("management.collection", "manageRoles");
	public static final String MANAGE_METADATAEXTRACTOR = permission("management.collection", "manageMetadataExtractor");
	public static final String MANAGE_CONNECTORS = permission("management.collection", "manageConnectors");
	public static final String MANAGE_SEARCHENGINE = permission("management.collection", "manageSearchEngine");
	public static final String MANAGE_TRASH = permission("management.collection", "manageTrash");
	public static final List<String> COLLECTION_MANAGEMENT_PERMISSIONS = asList(MANAGE_TAXONOMIES, MANAGE_VALUELIST,
			MANAGE_METADATASCHEMAS, MANAGE_SECURITY,
			MANAGE_METADATAEXTRACTOR, MANAGE_CONNECTORS, MANAGE_SEARCHENGINE, MANAGE_TRASH);

	public static final String MANAGE_SYSTEM_CONFIGURATION = permission("management.system", "manageSystemConfiguration");
	public static final String MANAGE_SYSTEM_GROUPS = permission("management.system", "manageSystemGroups");
	public static final String MANAGE_SYSTEM_USERS = permission("management.system", "manageSystemUsers");
	public static final String MANAGE_SYSTEM_COLLECTIONS = permission("management.system", "manageSystemCollections");
	public static final String MANAGE_SYSTEM_MODULES = permission("management.system", "manageSystemModules");
	public static final String MANAGE_SYSTEM_DATA_IMPORTS = permission("management.system", "manageSystemDataImports");
	public static final String MANAGE_SYSTEM_SERVERS = permission("management.system", "manageSystemServers");
	public static final String MANAGE_SYSTEM_UPDATES = permission("management.system", "manageSystemUpdates");
	public static final String MANAGE_LDAP = permission("management.system", "ldapConfigurationManagement");
	public static final List<String> SYSTEM_MANAGEMENT_PERMISSIONS = asList(MANAGE_SYSTEM_CONFIGURATION, MANAGE_SYSTEM_GROUPS,
			MANAGE_SYSTEM_USERS, MANAGE_SYSTEM_COLLECTIONS, MANAGE_SYSTEM_MODULES, MANAGE_SYSTEM_DATA_IMPORTS,
			MANAGE_SYSTEM_SERVERS, MANAGE_SYSTEM_UPDATES, MANAGE_LDAP);

}
