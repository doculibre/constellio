package com.constellio.app.ui.framework.buttons;

import static com.constellio.app.ui.i18n.i18n.$;

import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;

public abstract class SaveButton extends IconButton {

	public static final Resource ICON_RESOURCE = new ThemeResource("images/commun/disket.gif");

	public static final String BUTTON_STYLE = "save-button";

	public SaveButton() {
		super(ICON_RESOURCE, $("save"), true);
		init();
	}

	public SaveButton(boolean iconOnly) {
		super(iconOnly ? ICON_RESOURCE : null, computeCaption($("save"), iconOnly), iconOnly, false);
		init();
	}

	public SaveButton(String caption) {
		super(null, computeCaption(caption, false), false);
		init();
	}

	private static String computeCaption(String caption, boolean iconOnly) {
		return iconOnly ? caption : $("save.icon") + " " + caption;
	}

	private void init() {
		addStyleName(BUTTON_STYLE);
	}

}