package com.constellio.app.services.menu;

import com.constellio.app.ui.framework.buttons.ConfirmDialogButton.DialogMode;
import com.vaadin.server.Resource;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.function.Consumer;

@Data
@Builder
public class MenuItemAction {
	private String type;
	private MenuItemActionState state;
	private String caption;
	private Resource icon;
	private Consumer<List<String>> command;
	private int group;
	private int priority;
	private int recordsLimit;
	private String confirmMessage;
	private DialogMode dialogMode;

}
