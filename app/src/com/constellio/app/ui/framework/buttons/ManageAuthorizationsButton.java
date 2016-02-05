package com.constellio.app.ui.framework.buttons;

import static com.constellio.app.ui.i18n.i18n.$;

import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;

@SuppressWarnings("serial")
public abstract class ManageAuthorizationsButton extends IconButton {

	public static final Resource ICON_RESOURCE = new ThemeResource("images/commun/add_authorization.png");

	public static final String BUTTON_STYLE = "edit-button";

	public ManageAuthorizationsButton() {
		super(ICON_RESOURCE, $("addAuthorization"), true);
		init();
	}

	public ManageAuthorizationsButton(String caption, boolean iconOnly) {
		super(ICON_RESOURCE, caption, iconOnly);
		init();
	}

	public ManageAuthorizationsButton(String caption) {
		super(ICON_RESOURCE, caption);
		init();
	}

	private void init() {
		addStyleName(BUTTON_STYLE);
	}

}
