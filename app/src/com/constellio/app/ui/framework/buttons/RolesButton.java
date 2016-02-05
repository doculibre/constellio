package com.constellio.app.ui.framework.buttons;

import static com.constellio.app.ui.i18n.i18n.$;

import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;

public abstract class RolesButton extends IconButton {
	public static final Resource ICON_RESOURCE = new ThemeResource("images/commun/crown.png");
	public static final String BUTTON_STYLE = "roles-button";

	public RolesButton() {
		super(ICON_RESOURCE, $("roles"), true);
		init();
	}

	public RolesButton(boolean iconOnly) {
		super(iconOnly ? ICON_RESOURCE : null, computeCaption($("roles"), iconOnly), iconOnly);
		init();
	}

	public RolesButton(String caption) {
		super(null, computeCaption(caption, false), false);
		init();
	}

	private static String computeCaption(String caption, boolean iconOnly) {
		return iconOnly ? caption : $("roles.icon") + " " + caption;
	}

	private void init() {
		addStyleName(BUTTON_STYLE);
	}

}
