package com.constellio.app.ui.framework.buttons;

import static com.constellio.app.ui.i18n.i18n.$;

import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class BackButton extends Button {

	public static final Resource ICON_RESOURCE = new ThemeResource("images/commun/back.png");
	
	public static final String BUTTON_STYLE = "back-button";
	
	public BackButton() {
		super($("back"), ICON_RESOURCE);
		addStyleName(ValoTheme.BUTTON_BORDERLESS);
		addStyleName(BUTTON_STYLE);
	}

}
