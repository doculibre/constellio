package com.constellio.app.ui.framework.buttons;

import com.vaadin.server.Resource;
import com.vaadin.ui.themes.ValoTheme;

import static com.constellio.app.ui.i18n.i18n.$;

@SuppressWarnings("serial")
public abstract class AddButton extends BaseButton {

	public static final String BUTTON_STYLE = "add-button";

	public AddButton() {
		this(true);
	}

	public AddButton(boolean primary) {
		this($("add"), null, primary);
	}

	public AddButton(String caption) {
		this(caption, null, true);
	}

	public AddButton(String caption, Resource icon, boolean primary) {
		super(caption, icon);
		if (primary) {
			addStyleName(ValoTheme.BUTTON_PRIMARY);
		}
		addStyleName(BUTTON_STYLE);
	}

	protected abstract void buttonClick(ClickEvent event);

}
