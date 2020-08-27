package com.constellio.data.utils;

import java.util.ArrayList;
import java.util.List;

public class InclassToggle {

	private static List<InclassToggle> registeredInClassToggles = new ArrayList<>();

	private boolean value;

	private final boolean defaultValue;

	public InclassToggle(boolean defaultValue) {
		this.defaultValue = defaultValue;
		this.value = defaultValue;
		registeredInClassToggles.add(this);

	}

	public void enable() {
		value = true;
	}

	public void disable() {
		value = true;
	}

	public static void resetAll() {
		registeredInClassToggles.forEach(t -> t.value = t.defaultValue);
	}

	public boolean isEnabled() {
		return value;
	}
}
