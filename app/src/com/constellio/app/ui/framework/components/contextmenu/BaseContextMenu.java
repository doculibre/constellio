package com.constellio.app.ui.framework.components.contextmenu;

import com.vaadin.server.Resource;
import org.vaadin.peter.contextmenu.ContextMenu;

import static com.constellio.app.ui.i18n.i18n.isRightToLeft;

public class BaseContextMenu extends ContextMenu {

	@Override
	public ContextMenuItem addItem(String caption) {
		return adjustStyleName(super.addItem(caption));
	}

	@Override
	public ContextMenuItem addItem(Resource icon) {
		return adjustStyleName(super.addItem(icon));
	}

	@Override
	public ContextMenuItem addItem(String caption, Resource icon) {
		return adjustStyleName(super.addItem(caption, icon));
	}

	private ContextMenuItem adjustStyleName(ContextMenuItem item) {
		if (isRightToLeft()) {
			item.addStyleName("context-menu-item-rtl");
		}
		return item;
	}

}
