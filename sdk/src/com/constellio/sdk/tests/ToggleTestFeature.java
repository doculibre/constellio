package com.constellio.sdk.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.internal.AssumptionViolatedException;

import com.constellio.data.utils.dev.Toggle;
import com.constellio.data.utils.dev.Toggle.AvailableToggle;

public class ToggleTestFeature {

	Map<String, String> sdkProperties;

	private static List<AvailableToggle> enabledToggles = null;

	public ToggleTestFeature(Map<String, String> sdkProperties) {
		this.sdkProperties = sdkProperties;
		if (enabledToggles == null) {
			String togglePropertyValue = sdkProperties.get("toggles");
			loadToggles(togglePropertyValue);
		}

		for (AvailableToggle toggle : enabledToggles) {
			toggle.enable();
		}
	}

	private static void loadToggles(String togglePropertyValue) {

		enabledToggles = new ArrayList<>();

		if ("all".equalsIgnoreCase(togglePropertyValue)) {
			enabledToggles = Toggle.getAllAvailable();

		} else if (togglePropertyValue != null) {
			for (String enabledToggleId : togglePropertyValue.split(",")) {
				AvailableToggle enabledToggle = Toggle.getToggle(enabledToggleId);
				if (enabledToggle != null) {
					enabledToggles.add(enabledToggle);
				}
			}
		}
	}

	public ToggleCondition onlyWhen(AvailableToggle toggle) {
		ToggleCondition toggleCondition = new ToggleCondition();
		toggleCondition.toggle = toggle;
		return toggleCondition;
	}

	public static class ToggleCondition {

		AvailableToggle toggle;

		public void isEnabled() {
			if (!toggle.isEnabled()) {
				throw new AssumptionViolatedException("Test is skipped because of disabled toggles");
			}

		}
	}

}
