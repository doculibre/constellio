package com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.action;

import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.MenuDisplayConfigComponent;
import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.MenuDisplayConfigEditableTreeNode;
import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.MenuDisplayConfigEditableTreeNodeFactoryTypes;
import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.root.MenuDisplayConfigRoot;
import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.subMenu.MenuDisplayConfigSubMenu;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.tree.structure.EditableTreeNode;
import com.constellio.app.ui.framework.components.tree.structure.EditableTreeNodeFactoryType;
import com.vaadin.shared.ui.dd.VerticalDropLocation;
import com.vaadin.ui.Component;
import com.vaadin.ui.Layout;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class MenuDisplayConfigActionEditableTreeNode extends MenuDisplayConfigEditableTreeNode<MenuDisplayConfigAction> {
	public MenuDisplayConfigActionEditableTreeNode(
			MenuDisplayConfigAction root) {
		super(root);
	}

	@Override
	protected Component buildControlsLayout(EditableTreeNode currentEditableTreeNode) {
		Layout layout = new I18NHorizontalLayout();

		if (!getValueFromModel(MenuDisplayConfigAction::isAlwaysEnabled)) {
			layout.addComponents(buildEnableControl(currentEditableTreeNode));
		}

		return layout;
	}

	@NotNull
	@Override
	protected List<EditableTreeNodeFactoryType> getPossibleChildrenTypes() {
		return Collections.emptyList();
	}


	@NotNull
	@Override
	protected EditableTreeNodeFactoryType<MenuDisplayConfigAction> getCurrentTreeNodeFactoryType() {
		return MenuDisplayConfigEditableTreeNodeFactoryTypes.ACTION;
	}

	@Override
	public boolean isDropManipulationAllowed(MenuDisplayConfigEditableTreeNode droppedNode,
											 VerticalDropLocation dropLocation) {
		boolean manipulationAllowed = !dropLocation.equals(VerticalDropLocation.MIDDLE);

		MenuDisplayConfigComponent parent = getValueFromModel(MenuDisplayConfigAction::getParent);

		manipulationAllowed &= !(!parent.getMainClass().equals(MenuDisplayConfigRoot.class) && droppedNode.getModelClass().equals(MenuDisplayConfigSubMenu.class));

		return manipulationAllowed;
	}

	@Override
	public boolean isDraggable() {
		return getValueFromModel(MenuDisplayConfigAction::isEnabled);
	}

	@Override
	public boolean isChildrenAllowed() {
		return false;
	}
}
