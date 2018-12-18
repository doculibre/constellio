package com.constellio.app.ui.framework.components;

import com.constellio.app.ui.framework.components.mouseover.NiceTitle;
import com.vaadin.data.Property;
import com.vaadin.server.Extension;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;

import static com.constellio.app.ui.i18n.i18n.$;

public class BaseMouseOverIcon extends Button {

	private BaseMouseOverIcon() {
	}

	public BaseMouseOverIcon(Resource icon, String message) {
		super();
		super.setIcon(icon);
		setEnabled(false);
		addStyleName(ValoTheme.BUTTON_BORDERLESS);
		addExtension(new NiceTitle(this, message, true));
	}
}
