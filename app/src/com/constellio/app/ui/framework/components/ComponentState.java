package com.constellio.app.ui.framework.components;

public enum ComponentState {
	ENABLED(true, true), DISABLED(true, false), INVISIBLE(false, false);

	private final boolean visible;
	private final boolean enabled;

	ComponentState(boolean visible, boolean enabled) {
		this.visible = visible;
		this.enabled = enabled;
	}

	public boolean isVisible() {
		return visible;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public static ComponentState enabledIf(boolean state) {
		return state ? ENABLED : DISABLED;
	}

	public static ComponentState visibleIf(boolean state) {
		return state ? ENABLED : INVISIBLE;
	}
}
