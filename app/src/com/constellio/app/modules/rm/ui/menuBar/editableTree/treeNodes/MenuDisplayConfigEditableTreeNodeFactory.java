package com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes;

import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.action.MenuDisplayConfigAction;
import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.root.MenuDisplayConfigRoot;
import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.subMenu.MenuDisplayConfigSubMenu;
import com.constellio.app.ui.framework.components.tree.structure.EditableTreeNodeFactory;

public class MenuDisplayConfigEditableTreeNodeFactory extends EditableTreeNodeFactory {
	@Override
	protected void registerDefaultFactories() {
		registerCustomNodeType(MenuDisplayConfigSubMenu.class, MenuDisplayConfigEditableTreeNodeFactoryTypes.SUB_MENU);
		registerCustomNodeType(MenuDisplayConfigAction.class, MenuDisplayConfigEditableTreeNodeFactoryTypes.ACTION);
		registerCustomNodeType(MenuDisplayConfigRoot.class, MenuDisplayConfigEditableTreeNodeFactoryTypes.ROOT);
	}
}
