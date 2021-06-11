package com.constellio.app.ui.framework.buttons;

import com.vaadin.server.Resource;
import com.vaadin.ui.themes.ValoTheme;

import static com.constellio.app.ui.i18n.i18n.$;

public abstract class SelectButton extends BaseButton {
	public SelectButton() {
		this(true);
	}

	public SelectButton(boolean primary) {
		this($("select"), null, primary);
	}

	public SelectButton(String caption) {
		this(caption, null, true);
	}

	public SelectButton(String caption, Resource icon, boolean primary) {
		super(caption, icon);
		if (primary) {
			addStyleName(ValoTheme.BUTTON_PRIMARY);
		}
	}

	protected abstract void buttonClick(ClickEvent event);
}
