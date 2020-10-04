package com.constellio.app.api.extensions;

import com.constellio.app.api.extensions.params.AvailableActionsParam;
import com.vaadin.ui.Button;
import com.vaadin.ui.themes.ValoTheme;

@Deprecated
public class SelectionPanelExtension {

	public static final String BUTTON_STYLE = "header-selection-panel-action-button";

	public void addAvailableActions(AvailableActionsParam param) {
	}

	public static void setStyles(Button button) {
		button.addStyleName(BUTTON_STYLE);
		button.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		button.removeStyleName(ValoTheme.BUTTON_LINK);
	}
}
