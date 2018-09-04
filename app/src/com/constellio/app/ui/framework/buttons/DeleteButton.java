package com.constellio.app.ui.framework.buttons;

import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;

import static com.constellio.app.ui.i18n.i18n.$;

@SuppressWarnings("serial")
public abstract class DeleteButton extends ConfirmDialogButton {
	public static final Resource ICON_RESOURCE = new ThemeResource("images/icons/actions/delete.png");
	public static final String BUTTON_STYLE = "deleteLogically-button";
	public static final String CAPTION = "delete";

	public DeleteButton() {
		super(ICON_RESOURCE, $(CAPTION), true);
		init();
	}

	public DeleteButton(boolean iconOnly) {
		super(iconOnly ? ICON_RESOURCE : null, computeCaption($(CAPTION), iconOnly), iconOnly);
		init();
	}

	public DeleteButton(Resource icon, String caption, boolean iconOnly) {
		super(icon, caption, iconOnly);
		init();
	}

	public DeleteButton(String caption) {
		super(null, computeCaption(caption, false), false);
		init();
	}

	public DeleteButton(String caption, boolean iconOnly) {
		super(iconOnly ? ICON_RESOURCE : null, computeCaption(caption, iconOnly), iconOnly);
		init();
	}

	public static String computeCaption(String caption, boolean iconOnly) {
		return iconOnly ? caption : $("delete.icon") + " " + caption;
	}

	private void init() {
		addStyleName(BUTTON_STYLE);
	}

	protected String getConfirmDialogMessage() {
		return $("ConfirmDialog.confirmDelete");
	}

}
