package com.constellio.app.ui.framework.components.menuBar;

import com.vaadin.ui.MenuBar;

import static com.constellio.app.ui.i18n.i18n.isRightToLeft;

public class BaseMenuBar extends MenuBar {

	public BaseMenuBar() {
		if (isRightToLeft()) {
			addStyleName("menubar-rtl");
		}
	}

}
