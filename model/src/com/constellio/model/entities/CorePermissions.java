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

import java.util.List;

public class CorePermissions {
	public static Permissions PERMISSIONS = new Permissions("core");

	private static String permission(String group, String permission) {
		return PERMISSIONS.add(group, permission);
	}

	// System
	public static final String SYSTEM = "system";

	public static final String DELETE_CONTENT_VERSION = permission(SYSTEM, "deleteContentVersion");
	public static final String VIEW_EVENTS = permission(SYSTEM, "viewEvents");

	// Collection management
	public static final String COLLECTION_MANAGEMENT = "management.collection";

	public static final String MANAGE_TAXONOMIES = permission(COLLECTION_MANAGEMENT, "manageTaxonomies");
	public static final String MANAGE_VALUELIST = permission(COLLECTION_MANAGEMENT, "manageValueList");
	public static final String MANAGE_METADATASCHEMAS = permission(COLLECTION_MANAGEMENT, "manageMetadataSchemas");
	public static final String MANAGE_SECURITY = permission(COLLECTION_MANAGEMENT, "manageSecurity");
	public static final String MANAGE_METADATAEXTRACTOR = permission(COLLECTION_MANAGEMENT, "manageMetadataExtractor");
	public static final String MANAGE_CONNECTORS = permission(COLLECTION_MANAGEMENT, "manageConnectors");
	public static final String MANAGE_SEARCHENGINE = permission(COLLECTION_MANAGEMENT, "manageSearchEngine");
	public static final String MANAGE_TRASH = permission(COLLECTION_MANAGEMENT, "manageTrash");

	public static final List<String> COLLECTION_MANAGEMENT_PERMISSIONS = PERMISSIONS.getGroup(COLLECTION_MANAGEMENT);

	// System management
	public static final String SYSTEM_MANAGEMENT = "management.system";

	public static final String MANAGE_SYSTEM_CONFIGURATION = permission(SYSTEM_MANAGEMENT, "manageSystemConfiguration");
	public static final String MANAGE_SYSTEM_GROUPS = permission(SYSTEM_MANAGEMENT, "manageSystemGroups");
	public static final String MANAGE_SYSTEM_USERS = permission(SYSTEM_MANAGEMENT, "manageSystemUsers");
	public static final String MANAGE_SYSTEM_COLLECTIONS = permission(SYSTEM_MANAGEMENT, "manageSystemCollections");
	public static final String MANAGE_SYSTEM_MODULES = permission(SYSTEM_MANAGEMENT, "manageSystemModules");
	public static final String MANAGE_SYSTEM_DATA_IMPORTS = permission(SYSTEM_MANAGEMENT, "manageSystemDataImports");
	public static final String MANAGE_SYSTEM_SERVERS = permission(SYSTEM_MANAGEMENT, "manageSystemServers");
	public static final String MANAGE_SYSTEM_UPDATES = permission(SYSTEM_MANAGEMENT, "manageSystemUpdates");
	public static final String MANAGE_LDAP = permission(SYSTEM_MANAGEMENT, "ldapConfigurationManagement");

	public static final List<String> SYSTEM_MANAGEMENT_PERMISSIONS = PERMISSIONS.getGroup(SYSTEM_MANAGEMENT);
}
