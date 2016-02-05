package com.constellio.app.ui.framework.buttons;

import static com.constellio.app.ui.i18n.i18n.$;

import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;

@SuppressWarnings("serial")
public abstract class EditSchemaButton extends IconButton {

	public static final Resource ICON_RESOURCE = new ThemeResource("images/commun/modifier_schema.gif");

	public static final String BUTTON_STYLE = "edit-button";

	public EditSchemaButton() {
		super(ICON_RESOURCE, $("edit"), true);
		init();
	}

	public EditSchemaButton(boolean iconOnly) {
		super(iconOnly ? ICON_RESOURCE : null, computeCaption($("edit"), iconOnly), iconOnly);
		init();
	}

	public EditSchemaButton(String caption) {
		super(null, computeCaption(caption, false), false);
		init();
	}

	private static String computeCaption(String caption, boolean iconOnly) {
		return iconOnly ? caption : $("edit.icon") + " " + caption;
	}

	private void init() {
		addStyleName(BUTTON_STYLE);
	}

}
