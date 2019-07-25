package com.constellio.app.ui.framework.components;

import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.mouseover.NiceTitle;
import com.vaadin.server.Resource;
import com.vaadin.ui.themes.ValoTheme;

public class BaseMouseOverIcon extends BaseButton {

	public BaseMouseOverIcon(Resource icon, String message) {
		super("", icon, true);
		super.setIcon(icon);
//		setEnabled(false);
		addStyleName(ValoTheme.BUTTON_BORDERLESS);
		addExtension(new NiceTitle(message, true));
	}

	@Override
	protected void buttonClick(ClickEvent event) {

	}
}
