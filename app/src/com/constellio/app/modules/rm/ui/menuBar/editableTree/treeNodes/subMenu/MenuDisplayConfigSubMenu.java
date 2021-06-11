package com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.subMenu;

import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.MenuDisplayConfigComponent;
import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.MenuDisplayConfigComponentBase;
import com.constellio.app.services.icons.IconService;
import com.constellio.app.ui.pages.base.SessionContext;

public class MenuDisplayConfigSubMenu extends MenuDisplayConfigComponentBase {
	public MenuDisplayConfigSubMenu(String code, SessionContext sessionContext, IconService iconService) {
		super(new MenuDisplayConfigComponentBase(MenuDisplayConfigSubMenu.class, code, sessionContext, iconService));
	}

	public MenuDisplayConfigSubMenu(MenuDisplayConfigSubMenu copy) {
		super(copy);
	}

	public MenuDisplayConfigSubMenu(MenuDisplayConfigComponent copy) {
		super(copy);
	}

	@Override
	public MenuDisplayConfigComponent applyModification(MenuDisplayConfigComponent copy) {
		return new MenuDisplayConfigSubMenu(copy);
	}
}
