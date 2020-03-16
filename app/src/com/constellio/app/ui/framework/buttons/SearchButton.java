package com.constellio.app.ui.framework.buttons;

import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;

import static com.constellio.app.ui.i18n.i18n.$;

public abstract class SearchButton extends BaseButton {

	public static final String STYLE_NAME = "search-button";

	public static final String ICON_ONLY_STYLE_NAME = STYLE_NAME + "-icon-only";
	
	public SearchButton() {
		super($("search"));
		init();
	}

	public SearchButton(String caption) {
		super(caption);
		init();
	}

	private void init() {
		addStyleName(STYLE_NAME);
		addStyleName(ValoTheme.BUTTON_PRIMARY);
	}

	public boolean isIconOnly() {
		return StringUtils.contains(getStyleName(), ICON_ONLY_STYLE_NAME);
	}

	public void setIconOnly(boolean iconOnly) {
		if (iconOnly) {
			addStyleName(ICON_ONLY_STYLE_NAME);
		} else {
			removeStyleName(ICON_ONLY_STYLE_NAME);
		}
	}

}
