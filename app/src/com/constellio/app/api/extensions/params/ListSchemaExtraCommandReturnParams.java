package com.constellio.app.api.extensions.params;

import com.vaadin.server.Resource;
import com.vaadin.ui.MenuBar;

public class ListSchemaExtraCommandReturnParams {
	MenuBar.Command command;
	String caption;
	Resource ressource;

	public ListSchemaExtraCommandReturnParams(MenuBar.Command command, String caption, Resource ressource) {
		this.command = command;
		this.caption = caption;
		this.ressource = ressource;
	}

	public Resource getRessource() {
		return ressource;
	}

	public MenuBar.Command getCommand() {
		return command;
	}

	public String getCaption() {
		return caption;
	}
}
