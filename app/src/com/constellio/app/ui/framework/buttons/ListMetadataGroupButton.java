package com.constellio.app.ui.framework.buttons;

import static com.constellio.app.ui.i18n.i18n.$;

import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public abstract class ListMetadataGroupButton extends IconButton {

	public static final Resource ICON_RESOURCE = new ThemeResource("images/commun/businesspeople.png");

	public static final String BUTTON_STYLE = "edit-button";

	public ListMetadataGroupButton() {
		this($("ListSchemaTypeView.buttonGroup"));
	}

	public ListMetadataGroupButton(String caption) {
		super(ICON_RESOURCE, caption);
		init();
	}

	public ListMetadataGroupButton(String caption, boolean iconOnly) {
		super(ICON_RESOURCE, caption, iconOnly);
		init();
	}

	private void init() {addStyleName(BUTTON_STYLE);}

}
