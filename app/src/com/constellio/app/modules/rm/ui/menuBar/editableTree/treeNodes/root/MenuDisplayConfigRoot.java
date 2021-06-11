package com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.root;

import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.MenuDisplayConfigComponent;
import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.MenuDisplayConfigComponentBase;
import com.constellio.app.services.icons.IconService;
import com.constellio.app.ui.pages.base.SessionContext;

import java.util.List;
import java.util.stream.Collectors;

public class MenuDisplayConfigRoot extends MenuDisplayConfigComponentBase {
	public MenuDisplayConfigRoot(SessionContext sessionContext, IconService iconService) {
		super(new MenuDisplayConfigComponentBase(MenuDisplayConfigRoot.class, "root", sessionContext, iconService));
	}

	public MenuDisplayConfigRoot(MenuDisplayConfigRoot copy) {
		super(copy);
	}

	public MenuDisplayConfigRoot(MenuDisplayConfigComponent copy) {
		super(copy);
	}

	@Override
	public List<MenuDisplayConfigComponent> getDisabledChildren() {
		return getFlattenDisabledChildren(this);
	}

	private List<MenuDisplayConfigComponent> getFlattenDisabledChildren(MenuDisplayConfigComponent node) {
		List<MenuDisplayConfigComponent> children = node.getChildren();
		List<MenuDisplayConfigComponent> disabledChildren = children.stream().filter(child -> !child.isEnabled()).collect(Collectors.toList());
		List<MenuDisplayConfigComponent> enabledChildren = children.stream().filter(MenuDisplayConfigComponent::isEnabled).collect(Collectors.toList());

		enabledChildren.stream().flatMap(child -> getFlattenDisabledChildren(child).stream()).forEach(disabledChildren::add);

		return disabledChildren;
	}

	@Override
	public MenuDisplayConfigComponent applyModification(MenuDisplayConfigComponent copy) {
		return new MenuDisplayConfigRoot(copy);
	}
}
