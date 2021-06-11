package com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.subMenu;

import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.MenuDisplayConfigEditableTreeNode;
import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.MenuDisplayConfigEditableTreeNodeFactoryTypes;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.tree.structure.EditableTreeNode;
import com.constellio.app.ui.framework.components.tree.structure.EditableTreeNodeFactoryType;
import com.vaadin.shared.ui.dd.VerticalDropLocation;
import com.vaadin.ui.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class MenuDisplayConfigSubMenuEditableTreeNode extends MenuDisplayConfigEditableTreeNode<MenuDisplayConfigSubMenu> {
	public MenuDisplayConfigSubMenuEditableTreeNode(MenuDisplayConfigSubMenu menuDisplayConfigSubMenu) {
		super(menuDisplayConfigSubMenu);
		setDeletableEventWhenItsRoot(true);
	}

	@Override
	protected List<EditableTreeNodeFactoryType> getPossibleChildrenTypes() {
		return Collections.emptyList();
	}

	@NotNull
	@Override
	protected List<EditableTreeNodeFactoryType> getDroppableChildrenTypes() {
		return Collections.singletonList(MenuDisplayConfigEditableTreeNodeFactoryTypes.ACTION);
	}

	@NotNull
	@Override
	protected EditableTreeNodeFactoryType<MenuDisplayConfigSubMenu> getCurrentTreeNodeFactoryType() {
		return MenuDisplayConfigEditableTreeNodeFactoryTypes.SUB_MENU;
	}

	@Override
	protected Component buildControlsLayout(EditableTreeNode currentEditableTreeNode) {
		I18NHorizontalLayout layout = new I18NHorizontalLayout();

		layout.addComponents(buildEditControl(currentEditableTreeNode), buildDeleteControl(currentEditableTreeNode));

		return layout;
	}

	@Override
	public boolean isDraggable() {
		return true;
	}

	@Override
	public boolean isDropManipulationAllowed(MenuDisplayConfigEditableTreeNode droppedNode,
											 VerticalDropLocation dropLocation) {
		boolean manipoulationAllowed;

		switch (dropLocation) {
			case TOP:
				manipoulationAllowed = true;
				break;
			case BOTTOM:
				manipoulationAllowed = !hasChildren();
				break;
			case MIDDLE:
				manipoulationAllowed = getDroppableChildrenTypes().stream().anyMatch(type -> type.getSourceClass().equals(droppedNode.getModelClass()));
				break;
			default:
				manipoulationAllowed = false;
		}

		return manipoulationAllowed;
	}
}
