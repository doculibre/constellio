package com.constellio.app.api.extensions;

import com.constellio.app.api.extensions.params.AvailableActionsParam;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.vaadin.ui.Button;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Created by Constellio on 2017-02-14.
 */
public class SelectionPanelExtension {

	public static final String BUTTON_STYLE = "header-selection-panel-action-button";

	public void addAvailableActions(AvailableActionsParam param) {
	}

	public static void setStyles(Button button) {
		button.addStyleName(BUTTON_STYLE);
		button.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		button.removeStyleName(ValoTheme.BUTTON_LINK);
		if (button instanceof WindowButton) {
			((WindowButton) button).setZIndex(BaseWindow.OVER_ADVANCED_SEARCH_FORM_Z_INDEX);
		}
	}
}
