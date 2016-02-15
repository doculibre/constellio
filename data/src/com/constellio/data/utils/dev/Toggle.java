package com.constellio.data.utils.dev;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.constellio.data.utils.ImpossibleRuntimeException;

public class Toggle {

	public static AvailableToggle NEW_USERCREDENTIAL_SERVICES = newToggle();
	public static AvailableToggle AGREGATED_METADATAS = newToggle();

	// ------------------------------------------------

	public static void toggleAll() {
		for (AvailableToggle toggle : toggles) {
			toggle.enable();
		}
	}

	public static AvailableToggle getToggle(String id) {
		for (Field field : Toggle.class.getDeclaredFields()) {
			if (field.getName().equals(id)) {
				try {
					return (AvailableToggle) field.get(null);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return null;
	}

	private static List<AvailableToggle> toggles;

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

		public boolean isEnabled() {
			return enabled;
		}

		public void ensureDisabled() {
			if (!enabled) {
				throw new ImpossibleRuntimeException("This feature is not supported with this toggle");
			}
		}

		public void ensureEnabled() {
			if (enabled) {
				throw new ImpossibleRuntimeException("This feature is not available");
			}
		}

		public void enable() {
			enabled = true;
		}

		public void disable() {
			enabled = false;
		}
	}
}
