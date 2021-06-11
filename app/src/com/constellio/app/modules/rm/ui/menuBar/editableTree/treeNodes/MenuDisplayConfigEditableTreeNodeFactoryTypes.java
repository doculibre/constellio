package com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes;

import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.action.MenuDisplayConfigActionEditableTreeNodeFactoryType;
import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.root.MenuDisplayConfigRootEditableTreeNodeFactoryType;
import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.subMenu.MenuDisplayConfigSubMenuEditableTreeNodeFactoryType;

public class MenuDisplayConfigEditableTreeNodeFactoryTypes {
	public static final MenuDisplayConfigRootEditableTreeNodeFactoryType ROOT = new MenuDisplayConfigRootEditableTreeNodeFactoryType();
	public static final MenuDisplayConfigSubMenuEditableTreeNodeFactoryType SUB_MENU = new MenuDisplayConfigSubMenuEditableTreeNodeFactoryType();
	public static final MenuDisplayConfigActionEditableTreeNodeFactoryType ACTION = new MenuDisplayConfigActionEditableTreeNodeFactoryType();
}