package com.constellio.app.ui.framework.buttons;

import com.vaadin.ui.themes.ValoTheme;

import static com.constellio.app.ui.i18n.i18n.$;

public abstract class SearchButton extends BaseButton {

	public static final String STYLE_NAME = "search-button";

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

}
