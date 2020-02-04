package com.constellio.app.ui.framework.components;

import com.constellio.app.ui.framework.buttons.IconButton;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;

public class MouseOverHelpIcon extends IconButton {
	public static final Resource RESOURCE_PATH = new ThemeResource("images/icons/information2.png");

	public MouseOverHelpIcon(String caption) {
		super(RESOURCE_PATH, caption, true, true);
	}

	@Override
	protected void buttonClick(ClickEvent event) {

	}
}
