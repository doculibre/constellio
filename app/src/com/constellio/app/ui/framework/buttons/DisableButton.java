package com.constellio.app.ui.framework.buttons;

import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;

import static com.constellio.app.ui.i18n.i18n.$;

@SuppressWarnings("serial")
public abstract class DisableButton extends ConfirmDialogButton {

	public static final Resource ICON_RESOURCE = new ThemeResource("images/commun/desactiverRouge.gif");

	public static final String BUTTON_STYLE = "disable-button";

	public DisableButton() {
		super(ICON_RESOURCE, $("disable"), true);
		init();
	}

	public DisableButton(String caption, boolean iconOnly) {
		super(ICON_RESOURCE, caption, iconOnly);
		init();
	}

	public DisableButton(String caption) {
		super(ICON_RESOURCE, caption);
		init();
	}

	private void init() {
		addStyleName(BUTTON_STYLE);
	}

	protected String getConfirmDialogMessage() {
		return $("ConfirmDialog.confirmDisable");
	}

}
