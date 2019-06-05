package com.constellio.app.ui.framework.components.menuBar;

import com.vaadin.ui.MenuBar;
import com.vaadin.ui.themes.ValoTheme;

import static com.constellio.app.ui.i18n.i18n.isRightToLeft;

public class BaseMenuBar extends MenuBar {

	public BaseMenuBar() {
		this(false);
	}

	public BaseMenuBar(boolean compact) {
		if (isRightToLeft()) {
			addStyleName("menubar-rtl");
		}
		if (compact) {
			addStyleName("compact-menubar");
			addStyleName(ValoTheme.MENUBAR_BORDERLESS);
		}
	}

}
