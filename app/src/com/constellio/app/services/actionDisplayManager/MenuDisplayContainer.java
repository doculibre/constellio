package com.constellio.app.services.actionDisplayManager;

import lombok.Getter;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Getter
public class MenuDisplayContainer extends MenuDisplayItem {
	private Map<Locale, String> labels;

	public MenuDisplayContainer(String code, Map<Locale, String> displayName, String icon) {
		this(code, displayName, icon, true, false);
	}

	public MenuDisplayContainer(String code, Map<Locale, String> labels, String icon,
								boolean active, boolean alwaysActive) {
		super(code, icon, null, active, null, alwaysActive);
		this.labels = labels;
	}

	public Map<Locale, String> getLabels() {
		return Collections.unmodifiableMap(labels);
	}

	public MenuDisplayContainer newMenuDisplayContainerWithLabels(Map<Locale, String> labels) {
		return new MenuDisplayContainer(this.getCode(), labels, this.getIcon(), this.isActive(), this.isAlwaysActive());
	}

	public MenuDisplayContainer newMenuDisplayContainerWithIcon(String icon) {
		return new MenuDisplayContainer(this.getCode(), this.labels, icon, this.isActive(), this.isAlwaysActive());
	}

	public MenuDisplayContainer newMenuDisplayContainerWithActive(boolean active) {
		return new MenuDisplayContainer(this.getCode(), this.labels, this.getIcon(), active, this.isAlwaysActive());
	}

	public MenuDisplayContainer newMenuDisplayContainerWithAlwaysActive(boolean alwaysActive) {
		return new MenuDisplayContainer(this.getCode(), this.labels, this.getIcon(), this.isActive(), alwaysActive);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		MenuDisplayContainer that = (MenuDisplayContainer) o;
		return Objects.equals(labels, that.labels);
	}

	@Override
	public int hashCode() {
		return Objects.hash(labels);
	}
}
