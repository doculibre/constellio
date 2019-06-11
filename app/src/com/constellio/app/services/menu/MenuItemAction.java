package com.constellio.app.services.menu;

import com.constellio.app.ui.framework.buttons.ConfirmDialogButton.DialogMode;
import com.vaadin.server.Resource;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MenuItemAction {
	private String type;
	private MenuItemActionState state;
	private String reason;
	private String caption;
	private Resource icon;
	private Class button;
	private Runnable command;
	private int group;
	private int priority;
	private String confirmMessage;
	private DialogMode dialogMode;
}
