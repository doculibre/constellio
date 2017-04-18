package com.constellio.app.ui.framework.buttons;

public abstract class BadgeButton extends BaseButton {

	public BadgeButton(String caption) {
		this(caption, 0);
	}

	public BadgeButton(String caption, int count) {
		this(caption, count, true);
	}

	public BadgeButton(String caption, int count, boolean badgeVisibleWhenZero) {
		super(caption);
		setBadgeCount(count);
		setBadgeVisible(true);
		setBadgeVisibleWhenZero(badgeVisibleWhenZero);
	}

}
