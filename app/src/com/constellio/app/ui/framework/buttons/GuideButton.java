package com.constellio.app.ui.framework.buttons;

import com.constellio.app.ui.framework.components.mouseover.NiceTitle;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.themes.ValoTheme;

import static com.constellio.app.ui.i18n.i18n.$;

public abstract class GuideButton extends BaseButton {

	public GuideButton() {
		super($("guide"), new ThemeResource("images/icons/about.png"));

		addStyleName(ValoTheme.BUTTON_LINK);
		addStyleName("guide-button");
		addExtension(new NiceTitle($("guide.details")));

		setCaptionVisibleOnMobile(false);
	}

	@Override
	protected void buttonClick(ClickEvent event) {
		String guideUrl = getGuideUrl();
		Page.getCurrent().open(guideUrl, "_blank", false);
	}

	protected abstract String getGuideUrl();

}
