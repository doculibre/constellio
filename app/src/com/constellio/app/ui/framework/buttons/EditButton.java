package com.constellio.app.ui.framework.buttons;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;

import static com.constellio.app.ui.i18n.i18n.$;

@SuppressWarnings("serial")
public abstract class EditButton extends IconButton {

	public static final Resource ICON_RESOURCE = new ThemeResource("images/icons/actions/notebook_edit.png");

	public static final String BUTTON_STYLE = "edit-button";

	public EditButton() {
		super(ICON_RESOURCE, $("edit"), true);
		init();
	}

	public EditButton(boolean iconOnly) {
		super(iconOnly ? ICON_RESOURCE : null, computeCaption($("edit"), iconOnly), iconOnly);
		init();
	}

	public EditButton(String caption) {
		super(FontAwesome.EDIT, caption, false);
		init();
	}

	private static String computeCaption(String caption, boolean iconOnly) {
		return iconOnly ? caption : $("edit.icon") + " " + caption;
	}

	private void init() {
		addStyleName(BUTTON_STYLE);
	}

}
