package com.constellio.app.ui.pages.management.configs;

import com.vaadin.ui.ComboBox;

public class BaseComboBox extends ComboBox {
	public static final String COMBO_BOX_STYLE = "v-filterselect-suggestmenu";

	public BaseComboBox() {
		addStyleName(COMBO_BOX_STYLE);
	}
}
