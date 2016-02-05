package com.constellio.app.ui.framework.buttons;

import static com.constellio.app.ui.i18n.i18n.$;

import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;

@SuppressWarnings("serial")
public abstract class EnableButton extends ConfirmDialogButton {

	public static final Resource ICON_RESOURCE = new ThemeResource("images/commun/reactiver.gif");
	
	public static final String BUTTON_STYLE = "enable-button";
	
	public EnableButton() {
		super(ICON_RESOURCE, $("enable"), true);
		init();
	}

	public EnableButton(String caption, boolean iconOnly) {
		super(ICON_RESOURCE, caption, iconOnly);
		init();
	}

	public EnableButton(String caption) {
		super(ICON_RESOURCE, caption);
		init();
	}
	
	private void init() {
		addStyleName(BUTTON_STYLE);
	}
	
	protected String getConfirmDialogMessage() {
		return $("ConfirmDialog.confirmEnable");
	}

}
