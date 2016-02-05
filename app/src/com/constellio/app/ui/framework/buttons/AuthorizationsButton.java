package com.constellio.app.ui.framework.buttons;

import static com.constellio.app.ui.i18n.i18n.$;

import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;

public abstract class AuthorizationsButton extends IconButton {
	public static final Resource ICON_RESOURCE = new ThemeResource("images/commun/key.png");
	public static final String BUTTON_STYLE = "permissions-button";

	public AuthorizationsButton() {
		super(ICON_RESOURCE, $("permissions"), true);
		init();
	}

	public AuthorizationsButton(boolean iconOnly) {
		super(iconOnly ? ICON_RESOURCE : null, computeCaption($("permissions"), iconOnly), iconOnly);
		init();
	}

	public AuthorizationsButton(String caption) {
		super(null, computeCaption(caption, false), false);
		init();
	}

	private static String computeCaption(String caption, boolean iconOnly) {
		return iconOnly ? caption : $("permissions.icon") + " " + caption;
	}

	private void init() {
		addStyleName(BUTTON_STYLE);
	}

}
