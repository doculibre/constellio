package com.constellio.model.entities;

import com.constellio.data.utils.dev.Toggle;
import com.constellio.data.utils.dev.Toggle.AvailableToggle;

import java.util.List;

public class CorePermissions {
	public static Permissions PERMISSIONS = new Permissions("core");

	private static String permission(String group, String permission, AvailableToggle toggle) {
		if (toggle.isEnabled()) {
			return PERMISSIONS.add(group, permission);
		} else {
			return PERMISSIONS.getId() + "." + permission;
		}
	}

	private static String permission(String group, String permission) {
		return PERMISSIONS.add(group, permission);
	}

	// System
	public static final String SYSTEM = "system";

	public static final String DELETE_CONTENT_VERSION = permission(SYSTEM, "deleteContentVersion");
	public static final String VIEW_EVENTS = permission(SYSTEM, "viewEvents");
	public static final String MODIFY_PUBLIC_SAVED_SEARCH = permission(SYSTEM, "modifyPublicSavedSearch");
	public static final String DELETE_PUBLIC_SAVED_SEARCH = permission(SYSTEM, "deletePublicSavedSearch");
	public static final String BATCH_PROCESS = permission(SYSTEM, "batchProcess");
	public static final String VIEW_LOGIN_NOTIFICATION_ALERT = permission(SYSTEM, "viewLoginNotificationAlert");
	// Collection management
	public static final String COLLECTION_MANAGEMENT = "management.collection";

	public static final String MANAGE_FACETS = permission(COLLECTION_MANAGEMENT, "manageFacets");
	public static final String MANAGE_TAXONOMIES = permission(COLLECTION_MANAGEMENT, "manageTaxonomies");
	public static final String MANAGE_VALUELIST = permission(COLLECTION_MANAGEMENT, "manageValueList");
	public static final String MANAGE_METADATASCHEMAS = permission(COLLECTION_MANAGEMENT, "manageMetadataSchemas");
	public static final String MANAGE_SECURITY = permission(COLLECTION_MANAGEMENT, "manageSecurity");
	public static final String MANAGE_SEARCH_BOOST = permission(COLLECTION_MANAGEMENT, "manageSearchBoost");
	public static final String MANAGE_METADATAEXTRACTOR = permission(COLLECTION_MANAGEMENT, "manageMetadataExtractor");
	public static final String MANAGE_CONNECTORS = permission(COLLECTION_MANAGEMENT, "manageConnectors");
	//public static final String MANAGE_SEARCHENGINE = permission(COLLECTION_MANAGEMENT, "manageSearchEngine");
	public static final String MANAGE_TRASH = permission(COLLECTION_MANAGEMENT, "manageTrash");
	public static final String MANAGE_EMAIL_SERVER = permission(COLLECTION_MANAGEMENT, "manageEmailServer");
	public static final String MANAGE_EXCEL_REPORT = permission(COLLECTION_MANAGEMENT, "manageExcelReport");
	//public static final String MANAGE_SEARCH_CONFIG = permission(COLLECTION_MANAGEMENT, "manageSearchConfiguration");

	public static final String ACCESS_SEARCH_CAPSULE = permission(COLLECTION_MANAGEMENT, "accessSearchCapsule",
			Toggle.ADVANCED_SEARCH_CONFIGS);
	public static final String MANAGE_SYNONYMS = permission(COLLECTION_MANAGEMENT, "manageSynonyms", Toggle.ADVANCED_SEARCH_CONFIGS);
	public static final String EXCLUDE_AND_RAISE_SEARCH_RESULT = permission(COLLECTION_MANAGEMENT, "excludeAndRaiseOnSearchResult",
			Toggle.ADVANCED_SEARCH_CONFIGS);
	public static final String DELETE_CORRECTION_SUGGESTION = permission(COLLECTION_MANAGEMENT, "deleteCorrectorSuggestion", Toggle.ADVANCED_SEARCH_CONFIGS);
	public static final String MANAGE_THESAURUS = permission(COLLECTION_MANAGEMENT, "manageThesaurus", Toggle.ADVANCED_SEARCH_CONFIGS);

	public static final List<String> COLLECTION_MANAGEMENT_PERMISSIONS = PERMISSIONS.getGroup(COLLECTION_MANAGEMENT);

	// System management
	public static final String SYSTEM_MANAGEMENT = "management.system";

	public static final String MANAGE_SYSTEM_CONFIGURATION = permission(SYSTEM_MANAGEMENT, "manageSystemConfiguration");
	public static final String MANAGE_SYSTEM_GROUPS = permission(SYSTEM_MANAGEMENT, "manageSystemGroups");
	public static final String MANAGE_SYSTEM_GROUPS_ACTIVATION = permission(SYSTEM_MANAGEMENT, "manageSystemGroupsActivation");
	//	public static final String MANAGE_SYSTEM_USERS_ACTIVATION = permission(SYSTEM_MANAGEMENT, "manageSystemUsersActivation");
	public static final String MANAGE_SYSTEM_USERS = permission(SYSTEM_MANAGEMENT, "manageSystemUsers");
	public static final String MANAGE_SYSTEM_COLLECTIONS = permission(SYSTEM_MANAGEMENT, "manageSystemCollections");
	//public static final String MANAGE_SYSTEM_MODULES = permission(SYSTEM_MANAGEMENT, "manageSystemModules");
	public static final String MANAGE_SYSTEM_DATA_IMPORTS = permission(SYSTEM_MANAGEMENT, "manageSystemDataImports");
	//public static final String MANAGE_SYSTEM_SERVERS = permission(SYSTEM_MANAGEMENT, "manageSystemServers");
	public static final String MANAGE_SYSTEM_UPDATES = permission(SYSTEM_MANAGEMENT, "manageSystemUpdates");
	public static final String MANAGE_LDAP = permission(SYSTEM_MANAGEMENT, "ldapConfigurationManagement");
	public static final String MANAGE_LABELS = permission(SYSTEM_MANAGEMENT, "manageLabels");
	public static final String MANAGE_PRINTABLE_REPORT = permission(SYSTEM_MANAGEMENT, "managePrintableReport");
	public static final String VIEW_SYSTEM_BATCH_PROCESSES = permission(SYSTEM_MANAGEMENT, "viewSystemBatchProcesses");

	public static final List<String> SYSTEM_MANAGEMENT_PERMISSIONS = PERMISSIONS.getGroup(SYSTEM_MANAGEMENT);

	public static final String OTHERS = "others";
	public static final String USE_EXTERNAL_APIS_FOR_COLLECTION = permission(OTHERS, "useExternalAPIS");

	public static final String ACCESS_TEMPORARY_RECORD = permission(SYSTEM_MANAGEMENT, "managerTemporaryRecords");
	public static final String SEE_ALL_TEMPORARY_RECORD = permission(SYSTEM_MANAGEMENT, "seeAllTemporaryRecords");
	public static final String ACCESS_DELETE_ALL_TEMPORARY_RECORD = permission(SYSTEM_MANAGEMENT, "accessDeleteAllTemporaryRecords");
	public static final String VIEW_SYSTEM_STATE = permission(SYSTEM_MANAGEMENT, "viewSystemState");
}
