package com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes;

import com.constellio.app.services.icons.IconService;
import com.constellio.app.ui.framework.components.menuBar.BaseMenuBar;
import com.constellio.app.ui.framework.components.tree.structure.EditableTreeNode;
import com.constellio.app.ui.framework.components.tree.structure.EditableTreeNodeFactoryTemplate;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.dd.VerticalDropLocation;
import com.vaadin.ui.Component;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import org.h2.util.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class MenuDisplayConfigEditableTreeNode<T extends MenuDisplayConfigComponent> extends EditableTreeNode<T> {
	private boolean deletableEventWhenItsRoot;
	private List<EditableTreeNode> children;

	public MenuDisplayConfigEditableTreeNode(T root) {
		super(root);
		deletableEventWhenItsRoot = false;
	}

	@Override
	public String getCaption() {
		Map<Locale, String> captions = getValueFromModel(T::getCaptions,
				captionsFound -> captionsFound != null ? captionsFound : Collections.singletonMap(getCurrentLocale(), ""));
		return captions.get(getCurrentLocale());
	}

	@Override
	public Resource getIcon() {
		IconService iconService = getValueFromModel(T::getIconService);
		String iconName = getValueFromModel(T::getIconName);
		return iconService != null && !StringUtils.isNullOrEmpty(iconName) ? iconService.getIconByName(iconName) : null;
	}

	@Override
	protected boolean hasChildren() {
		return !getChildren().isEmpty();
	}

	@NotNull
	@Override
	public List<EditableTreeNode> getChildren() {
		if (children == null) {
			children = new ArrayList<>();
			final MenuDisplayConfigEditableTreeNodeFactory childrenFactory = new MenuDisplayConfigEditableTreeNodeFactory();

			List<MenuDisplayConfigComponent> childrenModels = getValueFromModel(T::getEnabledChildren, possibleChildrenModels -> possibleChildrenModels != null ? possibleChildrenModels : new ArrayList<>());
			for (MenuDisplayConfigComponent childModel : childrenModels) {
				EditableTreeNodeFactoryTemplate factory = childrenFactory.getFactoryForClass(childModel.getMainClass());
				EditableTreeNode child = factory.build(childModel);
				if (child != null) {
					children.add(child);
				}
			}
		}

		return children;
	}

	protected Locale getCurrentLocale() {
		return getValueFromModel(root -> root.getSessionContext().getCurrentLocale());
	}

	protected Component buildEnableControl(EditableTreeNode currentEditableTreeNode) {
		MenuBar menuBar = new BaseMenuBar(true, false);

		MenuItem menuItem = menuBar.addItem("", item -> fireRemoveThisTreeNodeEvent(currentEditableTreeNode));
		menuItem.setIcon(getValueFromModel(T::isEnabled) ? FontAwesome.EYE_SLASH : FontAwesome.EYE);


		return menuBar;
	}

	protected Component buildEditControl(EditableTreeNode currentEditableTreeNode) {
		MenuBar menuBar = new BaseMenuBar(true, false);

		MenuItem menuItem = menuBar.addItem("", item -> fireTreeNodeSelectedEvent(currentEditableTreeNode));
		menuItem.setIcon(FontAwesome.EDIT);


		return menuBar;
	}

	public List<EditableTreeNode> getNodesToAddAtSameHierarchicalLevel() {
		return new ArrayList<>();
	}

	public boolean isDeletableEventWhenItsRoot() {
		return deletableEventWhenItsRoot;
	}

	public void setDeletableEventWhenItsRoot(boolean deletableEventWhenItsRoot) {
		this.deletableEventWhenItsRoot = deletableEventWhenItsRoot;
	}

	@Override
	protected boolean isNodeSelectedFiredOnCaptionClick() {
		return false;
	}

	public boolean isDropManipulationAllowed(MenuDisplayConfigEditableTreeNode droppedNode,
											 VerticalDropLocation dropLocation) {
		return true;
	}

	public boolean isChildrenAllowed() {
		return true;
	}
}
