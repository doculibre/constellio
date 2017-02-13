package com.constellio.app.ui.framework.buttons;

public abstract class BadgeButton extends BaseButton {
	
	public static final String STYLE_NAME = "button-badge";
	
	private String textCaption;
	
	private int count;
	
	private boolean badgeVisible = true;
	
	private boolean badgeVisibleWhenZero = true;

	public BadgeButton(String caption) {
		this(caption, 0);
	}

	public BadgeButton(String caption, int count) {
		this(caption, count, true);
	}

	public BadgeButton(String caption, int count, boolean badgeVisibleWhenZero) {
		super(null);
		
		this.textCaption = caption;
		this.count = count;
		this.badgeVisibleWhenZero = badgeVisibleWhenZero;
		
		setCaptionAsHtml(true);
		adjustCaption();
	}
	
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
		adjustCaption();
	}

	public boolean isBadgeVisible() {
		return badgeVisible;
	}

	public void setBadgeVisible(boolean badgeVisible) {
		this.badgeVisible = badgeVisible;
		adjustCaption();
	}

	public boolean isBadgeVisibleWhenZero() {
		return badgeVisibleWhenZero;
	}

	public void setBadgeVisibleWhenZero(boolean badgeVisibleWhenZero) {
		this.badgeVisibleWhenZero = badgeVisibleWhenZero;
		adjustCaption();
	}
	
	private void adjustCaption() {
		if (badgeVisible && (count > 0 || badgeVisibleWhenZero)) {
			super.setCaption("<span class=\"badge\" data-badge=\"" + count + "\">" + textCaption + "</span>");
		} else {
			super.setCaption(textCaption);
		}
	}

	@Override
	public void setCaption(String caption) {
		super.setCaption(caption);
		adjustCaption();
	}

}
