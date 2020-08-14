package com.constellio.data.utils.dev;

import com.constellio.data.services.tenant.TenantLocal;
import com.constellio.data.utils.ImpossibleRuntimeException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Toggle {

	public static boolean OFF_HEAP_ADDRESS_VALIDATOR = false;

	public static AvailableToggle PUBLIC_TOKENS = newToggle(false);

	public static AvailableToggle TEMP_FRANCIS = newToggle(true);


	public static AvailableToggle EXPORT_SAVESTATES_USING_WITH_FAILSAFE = newToggle(true);

	public static AvailableToggle SDK_CACHE_INTEGRITY_VALIDATION = newToggle(false);
	public static AvailableToggle SDK_REQUEST_CACHE_VALIDATION = newToggle();

	public static AvailableToggle ROLES_WITH_NEW_7_2_PERMISSIONS = newToggle();
	public static AvailableToggle USE_LATEST_BATCH_INSTEAD_OF_EXCEPTION = newToggle();

	public static AvailableToggle NEW_USERCREDENTIAL_SERVICES = newToggle();
	public static AvailableToggle AGREGATED_METADATAS = newToggle();
	public static AvailableToggle ADD_EXISTING_TASK = newToggle();
	public static AvailableToggle SIMULATE_CONNECTOR_DOWNLOAD_CONTENT = newToggle();
	public static AvailableToggle FORCE_ROLLBACK = newToggle();
	public static AvailableToggle LOST_PRIVATE_KEY = newToggle();
	public static AvailableToggle EXTERNAL_TOKENS_DISABLED = newToggle();
	public static AvailableToggle MARKED_RECORDS_IN_SAVESTATES_DISABLED = newToggle();
	public static AvailableToggle NO_WEBSERVICES_PASSWORDS_VALIDATIONS = newToggle();
	public static AvailableToggle STORAGE_SPACE_CAPACITIY_VALIDATION = newToggle(true);
	public static AvailableToggle ALERT_USERS_EMAIL = newToggle(true);
	public static AvailableToggle CACHES_ENABLED = newToggle(true);

	public static AvailableToggle LOG_REQUEST_CACHE = newToggle(false);
	public static AvailableToggle TEST_REQUEST_CACHE = newToggle(false);

	public static AvailableToggle RESTRICT_METADATAS_TO_THOSE_OF_SCHEMAS_WITH_RECORDS = newToggle(true);

	public static AvailableToggle FASTER_REINDEXING = newToggle(false);

	public static AvailableToggle RETENTION_RULE_DATE_TYPES = newToggle(false);

	public static AvailableToggle FOLDER_SUMMARY_CONFIG = newToggle(false);

	public static AvailableToggle ADVANCED_SEARCH_CONFIGS = newToggle(false);

	public static AvailableToggle NEW_TAXONOMY_SERVICES = newToggle(false);

	public static AvailableToggle AUDIT_EVENTS = newToggle(true);

	public static AvailableToggle PUTS_AFTER_SOLR_QUERY = newToggle(false);

	public static AvailableToggle ALL_CONNECTORS_DISABLED = newToggle(false);
	public static AvailableToggle LOG_CONNECTOR_INFOS_WARNINGS_ERRORS_IN_CONSOLE = newToggle(true);
	public static AvailableToggle LOG_CONVERSION_FILENAME_AND_SIZE = newToggle(false);

	public static AvailableToggle EVENT_BUS_RECORDS_CACHE = newToggle(true);

	public static AvailableToggle MULTI_LINGUAL = newToggle(true);

	public static AvailableToggle FORCE_MULTIPLE_CONTAINERS_VIEW_TO_DISPLAY = newToggle(false);

	public static AvailableToggle SHOW_CAPSULES_FOR_ALL_LANGUAGES = newToggle(false);

	public static AvailableToggle DANGER_DANGER_DANGER___ALLOW_UPDATE_TO_OLDER_VERSION___DANGER_DANGER_DANGER = newToggle(false);

	public static AvailableToggle SHOW_UNREAD_TASKS = newToggle(true);

	public static AvailableToggle RETURN_EMPTY_VALUE_WHEN_KEY_IS_MISSING = newToggle(false);

	public static AvailableToggle SEARCH_RESULTS_VIEWER = newToggle(true);

	public static AvailableToggle ALLOWS_CREATION_OF_RECORDS_WITH_NON_PADDED_ID = newToggle(false);

	public static AvailableToggle USE_CACHE_FOR_QUERY_EXECUTION = newToggle(true);

	public static AvailableToggle USE_BYTE_ARRAY_DTOS_FOR_SUMMARY_CACHE = newToggle(true);

	public static AvailableToggle VALIDATE_CACHE_EXECUTION_SERVICE_USING_SOLR = newToggle(false);

	//For test puroposes only
	public static AvailableToggle USE_ONLY_SUMMARY_SOLR_RECORD_DTO = newToggle(false);

	//Debug the dtos!
	public static AvailableToggle VALIDATE_BYTE_ARRAY_DTOS_AFTER_CREATION = newToggle(false);
	public static AvailableToggle DEBUG_DTOS = newToggle(false);

	public static AvailableToggle MIGRATING_LEGACY_SAVESTATE = newToggle(false);

	public static AvailableToggle TRI_LEVEL_CACHE_LOCKING_Mecanism = newToggle(false);

	public static AvailableToggle USE_MMAP_WITHMAP_DB_FOR_LOADING = newToggle(false);
	public static AvailableToggle USE_MMAP_WITHMAP_DB_FOR_RUNTIME = newToggle(false);

	public static AvailableToggle USE_FILESYSTEM_DB_FOR_LARGE_METADATAS_CACHE = newToggle(true);


	public static AvailableToggle NO_TAXONOMIES_CACHE_INVALIDATION = newToggle(true);

	public static AvailableToggle PERFORMANCE_TESTING = newToggle(false);

	public static AvailableToggle CONTENT_CONVERSION = newToggle(true);

	public static AvailableToggle OLD_DELETE_UNUSED_CONTENT_METHOD = newToggle(false);

	public static AvailableToggle TRY_USING_NEW_CACHE_BASED_TAXONOMIES_SEARCH_SERVICES_QUERY_HANDLER = newToggle(true);
	public static AvailableToggle FORCE_USING_NEW_CACHE_BASED_TAXONOMIES_SEARCH_SERVICES_QUERY_HANDLER = newToggle(false);

	public static AvailableToggle DEBUG_TAXONOMY_RECORDS_HOOK = newToggle(false);

	public static AvailableToggle DETACHABLE_RECORDS = newToggle(true);

	public static AvailableToggle DEBUG_SOLR_TIMINGS = newToggle(false);

	public static AvailableToggle STRUCTURE_CACHE_BASED_ON_EXISTING_IDS = newToggle(true);
	public static AvailableToggle STRUCTURE_CACHE_BASED_ON_EXISTING_IDS_ON_DEV_STATION = newToggle(false);

	public static AvailableToggle ENABLE_PDFTRON_TRIAL = newToggle(false);

	public static AvailableToggle USE_MEMORY_STRING_ID_MAPPING = newToggle(false);
	public static AvailableToggle DOCUMENT_RETENTION_RULES = newToggle(false);

	public static AvailableToggle COUNT_CACHE_FILESYSTEM_METADATA_USAGE = newToggle(false);

	public static AvailableToggle TIFF_VIEWER = newToggle(false);
	public static AvailableToggle SDK_PANEL_LOG_CALLS = newToggle(false);

	public static AvailableToggle IGNORE_CONFIGS_WHEN_SYNCHRONIZING_AZURE_RELATED_USERS_AND_GROUPS = newToggle(false);

	public static AvailableToggle PUT_IN_CONTAINER_ACTION = newToggle(false);

	public static AvailableToggle ENABLE_OFFICE365_EXCLUSIVE = newToggle(false);

	public static AvailableToggle ENABLE_SIGNATURE = newToggle(false);

	public static AvailableToggle ENABLE_CLOUD_SYSADMIN_FEATURES = newToggle(false);

	public static AvailableToggle VALIDATE_USER_COLLECTIONS = newToggle(true);


	// ------------------------------------------------

	private static Map<String, AvailableToggle> toggleMap = new HashMap<>();

	static {
		for (Field field : Toggle.class.getDeclaredFields()) {
			if (AvailableToggle.class.equals(field.getType())) {
				try {
					AvailableToggle availableToggle = (AvailableToggle) field.get(null);
					availableToggle.id = field.getName();
					toggleMap.put(field.getName(), availableToggle);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	public static void toggleAll() {
		for (AvailableToggle toggle : toggles) {
			toggle.enable();
		}
	}

	public static AvailableToggle getToggle(String id) {
		return toggleMap.get(id);
	}

	private static List<AvailableToggle> toggles;

	private static AvailableToggle newToggle(boolean value) {
		if (toggles == null) {
			toggles = new ArrayList<>();
		}
		AvailableToggle toggle = new AvailableToggle();
		toggle.enabled.set(value);
		toggle.defaultValue = value;
		toggles.add(toggle);
		return toggle;
	}

	private static AvailableToggle newToggle() {
		if (toggles == null) {
			toggles = new ArrayList<>();
		}
		AvailableToggle toggle = new AvailableToggle();
		toggles.add(toggle);
		return toggle;
	}

	public static void resetToggles() {
		for (AvailableToggle toggle : toggles) {
			toggle.disable();
		}
	}

	public static List<AvailableToggle> getAllAvailable() {
		return toggles;
	}

	public static class AvailableToggle {

		private final TenantLocal<Boolean> enabled = new TenantLocal<>();
		private boolean defaultValue;

		private String id;

		public boolean isEnabled() {
			return getIsEnabledAndInitToDefaultIfNull();
		}

		public void ensureDisabled() {
			if (getIsEnabledAndInitToDefaultIfNull()) {
				throw new ImpossibleRuntimeException("Unsupported with toggle '" + id + "'");
			}
		}

		public void reset() {
			enabled.set(defaultValue);
		}

		public void ensureEnabled() {
			if (!getIsEnabledAndInitToDefaultIfNull()) {
				throw new ImpossibleRuntimeException("Only supported with toggle '" + id + "'");
			}
		}

		public boolean enable() {
			boolean oldValue = getIsEnabledAndInitToDefaultIfNull();
			enabled.set(true);
			return oldValue;
		}

		public void set(boolean value) {
			this.enabled.set(value);
		}

		public boolean disable() {
			boolean oldValue = getIsEnabledAndInitToDefaultIfNull();
			enabled.set(false);
			return oldValue;
		}

		public String getId() {
			return id;
		}

		private boolean getIsEnabledAndInitToDefaultIfNull() {
			if (enabled.get() == null) {
				enabled.set(defaultValue);
			}
			return enabled.get();
		}
	}
}
