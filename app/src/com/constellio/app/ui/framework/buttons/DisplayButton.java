package com.constellio.app.ui.framework.buttons;

import static com.constellio.app.ui.i18n.i18n.$;

import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;

@SuppressWarnings("serial")
public abstract class DisplayButton extends IconButton {

	public static final Resource ICON_RESOURCE = new ThemeResource("images/icons/actions/document_view.png");
	
	public static final String BUTTON_STYLE = "display-button";
	
	public DisplayButton() {
		super(ICON_RESOURCE, $("display"), true);
		init();
	}
	
	public DisplayButton(String caption, boolean iconOnly) {
		super(ICON_RESOURCE, caption, iconOnly);
		init();
	}

	public DisplayButton(String caption) {
		super(ICON_RESOURCE, caption);
		init();
	}
	
	private void init() {
		addStyleName(BUTTON_STYLE);
	}

}
