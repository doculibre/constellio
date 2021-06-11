package com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.root;

import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.MenuDisplayConfigComponent;
import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.MenuDisplayConfigEditableTreeNode;
import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.MenuDisplayConfigEditableTreeNodeFactoryTypes;
import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.subMenu.MenuDisplayConfigSubMenu;
import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.subMenu.MenuDisplayConfigSubMenuEditableTreeNode;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.menuBar.BaseMenuBar;
import com.constellio.app.ui.framework.components.tree.structure.EditableTreeNode;
import com.constellio.app.ui.framework.components.tree.structure.EditableTreeNodeFactoryType;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.dd.VerticalDropLocation;
import com.vaadin.ui.Component;
import com.vaadin.ui.MenuBar;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class MenuDisplayConfigRootEditableTreeNode extends MenuDisplayConfigEditableTreeNode<MenuDisplayConfigRoot> {
	private final MenuDisplayConfigSubMenuEditableTreeNode disabledActionNode;

	public MenuDisplayConfigRootEditableTreeNode(MenuDisplayConfigRoot menuBarRoot) {
		super(menuBarRoot);

		disabledActionNode = new MenuDisplayConfigSubMenuEditableTreeNode(new MenuDisplayConfigSubMenu("rootInnactive", menuBarRoot.getSessionContext(), menuBarRoot.getIconService()) {
			@Override
			public List<MenuDisplayConfigComponent> getEnabledChildren() {
				return menuBarRoot.getDisabledChildren();
			}

			@Override
			public Map<Locale, String> getCaptions() {
				Map<Locale, String> captions = super.getCaptions();
				captions.put(getSessionContext().getCurrentLocale(), $("MenuDisplayConfigViewImpl.node.root.disabledActions.caption"));

				return captions;
			}
		}) {
			@Override
			protected Component buildControlsLayout(EditableTreeNode currentEditableTreeNode) {
				return new I18NHorizontalLayout();
			}

			@Override
			public boolean isDropManipulationAllowed(MenuDisplayConfigEditableTreeNode droppedNode,
													 VerticalDropLocation dropLocation) {
				return dropLocation.equals(VerticalDropLocation.TOP);
			}

			@Override
			public boolean isDraggable() {
				return false;
			}
		};

		disabledActionNode.setDeletableEventWhenItsRoot(false);
	}

	@NotNull
	@Override
	public Component buildAddControl() {
		MenuBar addMenuBar = new BaseMenuBar(true, false);
		addMenuBar.addItem("", FontAwesome.PLUS, menuBarItem -> {
			EditableTreeNode newChildEditableTreeNode = MenuDisplayConfigEditableTreeNodeFactoryTypes.SUB_MENU.build();
			newChildEditableTreeNode.setEditing(true);

			fireChildNodeIsBuildingEvent(newChildEditableTreeNode);
		});

		return addMenuBar;
	}

	@Override
	protected boolean hasChildren() {
		return true;
	}

	@Override
	public String getCaption() {
		return $("MenuDisplayConfigViewImpl.node.root.enabledActions.caption");
	}

	@Override
	public List<EditableTreeNode> getNodesToAddAtSameHierarchicalLevel() {
		return Collections.singletonList(disabledActionNode);
	}

	@NotNull
	@Override
	protected List<EditableTreeNodeFactoryType> getPossibleChildrenTypes() {
		return Collections.singletonList(MenuDisplayConfigEditableTreeNodeFactoryTypes.SUB_MENU);
	}

	@NotNull
	@Override
	protected List<EditableTreeNodeFactoryType> getDroppableChildrenTypes() {
		return Arrays.asList(
				MenuDisplayConfigEditableTreeNodeFactoryTypes.SUB_MENU,
				MenuDisplayConfigEditableTreeNodeFactoryTypes.ACTION
		);
	}

	@NotNull
	@Override
	protected EditableTreeNodeFactoryType<MenuDisplayConfigRoot> getCurrentTreeNodeFactoryType() {
		return MenuDisplayConfigEditableTreeNodeFactoryTypes.ROOT;
	}

	@Override
	public boolean isDropManipulationAllowed(MenuDisplayConfigEditableTreeNode droppedNode,
											 VerticalDropLocation dropLocation) {
		return dropLocation.equals(VerticalDropLocation.MIDDLE);
	}

	@Override
	public boolean isDraggable() {
		return false;
	}
}
