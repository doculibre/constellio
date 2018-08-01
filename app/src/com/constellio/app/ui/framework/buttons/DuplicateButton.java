package com.constellio.app.ui.framework.buttons;

import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;

import static com.constellio.app.ui.i18n.i18n.$;

@SuppressWarnings("serial")
public abstract class DuplicateButton extends IconButton {

	public static final Resource ICON_RESOURCE = new ThemeResource("images/icons/actions/copy.png");

	public static final String BUTTON_STYLE = "copy-button";

	public DuplicateButton() {
		super(ICON_RESOURCE, $("duplicate"), true);
		init();
	}

	public DuplicateButton(boolean iconOnly) {
		super(iconOnly ? ICON_RESOURCE : null, computeCaption($("duplicate"), iconOnly), iconOnly);
		init();
	}

	public DuplicateButton(String caption) {
		super(null, computeCaption(caption, false), false);
		init();
	}

	private static String computeCaption(String caption, boolean iconOnly) {
		return iconOnly ? caption : $("duplicate.icon") + " " + caption;
	}

	private void init() {
		addStyleName(BUTTON_STYLE);
	}

}
