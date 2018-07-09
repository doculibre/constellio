package com.constellio.data.utils.dev;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.data.utils.ImpossibleRuntimeException;

public class Toggle {

	public static AvailableToggle DEMO_FEATURES = newToggle();
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
	public static AvailableToggle NO_WEBSERVICES_PASSWORDS_VALIDATIONS = newToggle();
	public static AvailableToggle STORAGE_SPACE_CAPACITIY_VALIDATION = newToggle(true);
	public static AvailableToggle ALERT_USERS_EMAIL = newToggle(true);
	public static AvailableToggle CACHES_ENABLED = newToggle(true);

	public static AvailableToggle LOG_REQUEST_CACHE = newToggle(false);
	public static AvailableToggle TEST_REQUEST_CACHE = newToggle(false);

	public static AvailableToggle RESTRICT_METADATAS_TO_THOSE_OF_SCHEMAS_WITH_RECORDS = newToggle(true);

	public static AvailableToggle FASTER_REINDEXING = newToggle(false);

	public static AvailableToggle RETENTION_RULE_DATE_TYPES = newToggle(false);

	public static AvailableToggle ADVANCED_SEARCH_CONFIGS = newToggle(false);

	public static AvailableToggle NEW_TAXONOMY_SERVICES = newToggle(false);

	public static AvailableToggle AUDIT_EVENTS = newToggle(true);

	public static AvailableToggle PUTS_AFTER_SOLR_QUERY = newToggle(false);

	public static AvailableToggle ALL_CONNECTORS_DISABLED = newToggle(false);
	public static AvailableToggle GET_ALL_VALUES_USING_NEW_CACHE_METHOD = newToggle(true);
	public static AvailableToggle LOG_CONNECTOR_INFOS_WARNINGS_ERRORS_IN_CONSOLE = newToggle(true);
	public static AvailableToggle LOG_CONVERSION_FILENAME_AND_SIZE = newToggle(false);

	public static AvailableToggle EVENT_BUS_RECORDS_CACHE = newToggle(true);

	public static AvailableToggle FORCE_MULTIPLE_CONTAINERS_VIEW_TO_DISPLAY = newToggle(false);

	public static AvailableToggle QUERY_EXECUTION_IN_CACHE = newToggle(false);
	public static AvailableToggle VALIDATE_QUERY_EXECUTION_IN_CACHE = newToggle(true);

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
		toggle.enabled = value;
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

		private boolean enabled;

		private String id;

		public boolean isEnabled() {
			return enabled;
		}

		public void ensureDisabled() {
			if (enabled) {
				throw new ImpossibleRuntimeException("Unsupported with toggle '" + id + "'");
			}
		}

		public void ensureEnabled() {
			if (!enabled) {
				throw new ImpossibleRuntimeException("Only supported with toggle '" + id + "'");
			}
		}

		public void enable() {
			enabled = true;
		}

		public void disable() {
			enabled = false;
		}

		public String getId() {
			return id;
		}
	}
}
