package com.constellio.app.ui.framework.buttons;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;

import static com.constellio.app.ui.i18n.i18n.$;

@SuppressWarnings("serial")
public abstract class DisplayButton extends IconButton {

	public static final Resource ICON_RESOURCE = new ThemeResource("images/icons/actions/document_view.png");

	public static final String BUTTON_STYLE = "display-button";

	public DisplayButton() {
		super(getIcon(true), $("display"), true);
		init();
	}

	public DisplayButton(String caption, boolean iconOnly) {
		super(getIcon(iconOnly), caption, iconOnly);
		init();
	}

	public DisplayButton(String caption) {
		super(getIcon(false), caption);
		init();
	}

	private static Resource getIcon(boolean iconOnly) {
		Resource icon;
		if (iconOnly) {
			icon = ICON_RESOURCE;
		} else {
			icon = FontAwesome.SEARCH;
		}
		return icon;
	}

	private void init() {
		addStyleName(BUTTON_STYLE);
	}

}
