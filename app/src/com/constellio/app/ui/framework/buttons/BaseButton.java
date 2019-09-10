package com.constellio.app.ui.framework.buttons;

import com.constellio.app.ui.framework.components.mouseover.NiceTitle;
import com.constellio.app.ui.util.ResponsiveUtils;
import com.vaadin.server.Extension;
import com.vaadin.server.Page;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("serial")
public abstract class BaseButton extends Button implements Clickable, BrowserWindowResizeListener {

	private String textCaption;

	private int badgeCount;

	private boolean badgeVisible = false;

	private boolean badgeVisibleWhenZero = true;

	private NiceTitle responsiveNiceTitle;

	private boolean captionVisibleOnMobile = true;
	
	public BaseButton() {
		this(null, null);
	}

	public BaseButton(String caption) {
		this(caption, null);
	}

	public BaseButton(String caption, Resource icon) {
		this(caption, icon, false);
	}

	public BaseButton(String caption, Resource icon, boolean iconOnly) {
		setCaptionAsHtml(true);
		setCaption(caption);
		setIcon(icon);
		if (iconOnly) {
			addStyleName(ValoTheme.BUTTON_ICON_ONLY);
		}

		addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				BaseButton.this.buttonClick(event);
			}
		});
	}

	@Override
	public void attach() {
		super.attach();
		Page.getCurrent().addBrowserWindowResizeListener(this);
		computeResponsive();
	}

	@Override
	public void detach() {
		Page.getCurrent().removeBrowserWindowResizeListener(this);
		super.detach();
	}

	public int getBadgeCount() {
		return badgeCount;
	}

	public void setBadgeCount(int badgeCount) {
		this.badgeCount = badgeCount;
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

	public boolean isCaptionVisibleOnMobile() {
		return captionVisibleOnMobile;
	}

	public void setCaptionVisibleOnMobile(boolean captionVisibleOnMobile) {
		this.captionVisibleOnMobile = captionVisibleOnMobile;
	}

	private void adjustCaption() {
		if (badgeVisible && (badgeCount > 0 || badgeVisibleWhenZero)) {
			StringBuilder sb = new StringBuilder();
			sb.append("<span class=\"button-badge\" data-badge=\"" + badgeCount + "\">");
			if (captionVisibleOnMobile || getIcon() == null || ResponsiveUtils.isDesktop()) {
				sb.append(textCaption);
			}
			sb.append("</span>");
			super.setCaption(sb.toString());
		} else {
			if (captionVisibleOnMobile || getIcon() == null || ResponsiveUtils.isDesktop()) {
				super.setCaption(textCaption);
			}
		}
	}

	@Override
	public void setCaption(String caption) {
		super.setCaption(caption);
		this.textCaption = caption;
		adjustCaption();
		if (responsiveNiceTitle != null && getExtensions().contains(responsiveNiceTitle)) {
			removeExtension(responsiveNiceTitle);
		}
		responsiveNiceTitle = new NiceTitle(textCaption);
	}

	@Override
	public void addExtension(Extension extension) {
		super.addExtension(extension);
	}

	private void computeResponsive() {
		if (getIcon() != null && textCaption != null) {
			if (!ResponsiveUtils.isDesktop() && StringUtils.isNotBlank(super.getCaption())) {
				super.setCaption("");
				adjustCaption();
				if (responsiveNiceTitle != null) {
					addExtension(responsiveNiceTitle);
				}
			} else if (ResponsiveUtils.isDesktop() && StringUtils.isBlank(super.getCaption())) {
				super.setCaption(textCaption);
				adjustCaption();
				if (responsiveNiceTitle != null && getExtensions().contains(responsiveNiceTitle)) {
					removeExtension(responsiveNiceTitle);
					responsiveNiceTitle = new NiceTitle(textCaption);
				}
			}
		}
	}

	@Override
	public void browserWindowResized(BrowserWindowResizeEvent event) {
		computeResponsive();
	}

	protected abstract void buttonClick(ClickEvent event);

}
