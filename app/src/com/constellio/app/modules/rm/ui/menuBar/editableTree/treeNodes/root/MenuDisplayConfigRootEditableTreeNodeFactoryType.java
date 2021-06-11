package com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.root;

import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.MenuDisplayConfigEditableTreeNodeFactoryType;
import com.constellio.app.ui.framework.components.tree.structure.EditableTreeNode;
import com.vaadin.server.Resource;

public class MenuDisplayConfigRootEditableTreeNodeFactoryType extends MenuDisplayConfigEditableTreeNodeFactoryType<MenuDisplayConfigRoot> {
	public MenuDisplayConfigRootEditableTreeNodeFactoryType() {
		super(MenuDisplayConfigRoot.class);
	}

	@Override
	public String getCaption() {
		return "Root node factory. This caption is never supposed to be shown";
	}

	@Override
	public Resource getIcon() {
		return null;
	}

	@Override
	public EditableTreeNode<MenuDisplayConfigRoot> build(MenuDisplayConfigRoot node) {
		return new MenuDisplayConfigRootEditableTreeNode(node);
	}

	@Override
	public EditableTreeNode<MenuDisplayConfigRoot> build() {
		return build(null);
	}
}
