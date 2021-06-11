package com.constellio.app.services.actionDisplayManager;

import com.constellio.model.entities.EnumWithSmallCode;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class MenuDisplayItem implements Serializable {
	public static final String PARENT_CODE_PREFIX = "parentSubCodePrefix_";

	enum Type implements EnumWithSmallCode {
		CONTAINER("C"),
		MENU("M");

		private String code;

		Type(String code) {
			this.code = code;
		}

		@Override
		public String getCode() {
			return code;
		}
	}

	private Type type;
	private String code;
	private String parentCode;
	private String icon;
	private String i18nKey;
	private boolean active;
	private boolean alwaysActive;

	public MenuDisplayItem(String code) {
		this(code, null, null);
	}

	public MenuDisplayItem(String code, String icon, String i18nKey) {
		this(code, icon, i18nKey, true);
	}

	public MenuDisplayItem(String code, String icon, String i18nKey, boolean active) {
		this(code, icon, i18nKey, active, null, false);
	}

	public MenuDisplayItem(String code, String icon, String i18nKey, String parentCode) {
		this(code, icon, i18nKey, true, parentCode, false);
	}

	public MenuDisplayItem(String code, String icon, String i18nKey, boolean active, String parentCode,
						   boolean alwaysActive) {
		setType();
		this.code = code;
		this.parentCode = parentCode;
		this.active = active;
		this.icon = icon;
		this.i18nKey = i18nKey;
		this.alwaysActive = alwaysActive;
	}

	public String getI18nKey() {
		return i18nKey;
	}

	private void setType() {
		if (this instanceof MenuDisplayContainer) {
			this.type = Type.CONTAINER;
		} else {
			this.type = Type.MENU;
		}
	}

	public boolean isContainer() {
		return type == Type.CONTAINER;
	}

	public boolean isOfficiallyActive() {
		return this.alwaysActive || this.active;
	}

	public MenuDisplayItem newMenuDisplayWithParentCode(String parentCode) {
		if (this.isContainer()) {
			throw new IllegalArgumentException("Cannot create MenuDisplayItem from container");
		}
		return new MenuDisplayItem(this.code, this.icon, this.i18nKey, this.active, parentCode, this.alwaysActive);
	}

	public MenuDisplayItem newMenuDisplayItemWithIcon(String icon) {
		if (this.isContainer()) {
			throw new IllegalArgumentException("Cannot create MenuDisplayItem from container");
		}
		return new MenuDisplayItem(this.code, icon, this.i18nKey, this.active, this.parentCode, this.alwaysActive);
	}

	public MenuDisplayItem newMenuDisplayItemWithActive(boolean active) {
		if (this.isContainer()) {
			throw new IllegalArgumentException("Cannot create MenuDisplayItem from container");
		}
		return new MenuDisplayItem(this.code, this.icon, i18nKey, active, this.parentCode, this.alwaysActive);
	}

	public MenuDisplayItem newMenuDisplayItemWithAlwaysActive(boolean alwaysActive) {
		if (this.isContainer()) {
			throw new IllegalArgumentException("Cannot create MenuDisplayItem from container");
		}
		return new MenuDisplayItem(this.code, this.icon, i18nKey, this.active, this.parentCode, alwaysActive);
	}

	public MenuDisplayItem newMenuDisplayItemWithI18nKey(String i18nKey) {
		if (this.isContainer()) {
			throw new IllegalArgumentException("Cannot create MenuDisplayItem from container");
		}
		return new MenuDisplayItem(this.code, this.icon, i18nKey, this.active, this.parentCode, this.alwaysActive);
	}

	private boolean isEqualIncludeNull(String term1, String term2) {

		if (term1 == null && term2 == null) {
			return true;
		}

		if (term1 == null && term2 != null || term1 != null && term2 == null) {
			return false;
		}

		return term1.equals(term2);
	}

	public MenuDisplayContainer getMenuDisplayContainer() {
		if (this instanceof MenuDisplayContainer) {
			return (MenuDisplayContainer) this;
		} else {
			throw new IllegalStateException("Has to be other type MenuDisplayContainer");
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		MenuDisplayItem that = (MenuDisplayItem) o;
		return active == that.active && alwaysActive == that.alwaysActive
			   && type == that.type && isEqualIncludeNull(code, that.code)
			   && isEqualIncludeNull(parentCode, that.parentCode)
			   && isEqualIncludeNull(icon, that.icon) && isEqualIncludeNull(i18nKey, that.i18nKey);
	}
}
