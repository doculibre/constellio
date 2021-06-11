package com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.subMenu;

import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.MenuDisplayConfigEditableTreeNodeFactoryType;
import com.constellio.app.ui.framework.components.tree.structure.EditableTreeNode;
import com.vaadin.server.Resource;

public class MenuDisplayConfigSubMenuEditableTreeNodeFactoryType extends MenuDisplayConfigEditableTreeNodeFactoryType<MenuDisplayConfigSubMenu> {

	public MenuDisplayConfigSubMenuEditableTreeNodeFactoryType() {
		super(MenuDisplayConfigSubMenu.class);
	}

	@Override
	public String getCaption() {
		return "Sous Menu";
	}

	@Override
	public Resource getIcon() {
		return null;
	}

	@Override
	public EditableTreeNode<MenuDisplayConfigSubMenu> build(MenuDisplayConfigSubMenu node) {
		return new MenuDisplayConfigSubMenuEditableTreeNode(node);
	}

	@Override
	public EditableTreeNode<MenuDisplayConfigSubMenu> build() {
		return build(null);
	}
}
