package com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.action;

import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.MenuDisplayConfigEditableTreeNodeFactoryType;
import com.constellio.app.ui.framework.components.tree.structure.EditableTreeNode;
import com.vaadin.server.Resource;

public class MenuDisplayConfigActionEditableTreeNodeFactoryType extends MenuDisplayConfigEditableTreeNodeFactoryType<MenuDisplayConfigAction> {
	public MenuDisplayConfigActionEditableTreeNodeFactoryType() {
		super(MenuDisplayConfigAction.class);
	}

	@Override
	public String getCaption() {
		return "Action";
	}

	@Override
	public Resource getIcon() {
		return null;
	}

	@Override
	public EditableTreeNode<MenuDisplayConfigAction> build(MenuDisplayConfigAction model) {
		return new MenuDisplayConfigActionEditableTreeNode(model);
	}

	@Override
	public EditableTreeNode<MenuDisplayConfigAction> build() {
		return build(null);
	}
}
