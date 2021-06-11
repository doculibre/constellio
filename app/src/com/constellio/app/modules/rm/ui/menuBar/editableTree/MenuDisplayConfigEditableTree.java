package com.constellio.app.modules.rm.ui.menuBar.editableTree;

import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.MenuDisplayConfigComponent;
import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.MenuDisplayConfigEditableTreeNode;
import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.MenuDisplayConfigEditableTreeNodeFactory;
import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.MenuDisplayConfigEditableTreeNodeFactoryTypes;
import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.root.MenuDisplayConfigRoot;
import com.constellio.app.ui.framework.components.tree.structure.EditableTree;
import com.constellio.app.ui.framework.components.tree.structure.EditableTreeNode;
import com.constellio.app.ui.framework.components.tree.structure.EditableTreeNodeFactoryTemplate;
import com.constellio.app.ui.framework.components.tree.structure.listener.treenodeisbuilding.EditableTreeNodeIsBuildingEvent;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.event.dd.acceptcriteria.ServerSideCriterion;
import com.vaadin.shared.ui.dd.VerticalDropLocation;
import com.vaadin.ui.AbstractSelect.AbstractSelectTargetDetails;
import com.vaadin.ui.TreeTable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class MenuDisplayConfigEditableTree extends EditableTree {
	private final MenuDisplayConfigEditableTreeNode root;

	public MenuDisplayConfigEditableTree(
			Function<EditableTreeNodeFactoryTemplate<MenuDisplayConfigRoot>, EditableTreeNode<MenuDisplayConfigRoot>> rootEditableTreeNodeFactory) {
		this((MenuDisplayConfigEditableTreeNode) rootEditableTreeNodeFactory.apply(MenuDisplayConfigEditableTreeNodeFactoryTypes.ROOT));
	}

	public MenuDisplayConfigEditableTree(MenuDisplayConfigEditableTreeNode root) {
		super(root);
		this.root = root;
	}

	@Override
	public void attach() {
		super.attach();

		buildMainComponent();
	}

	private void buildMainComponent() {
		rebuildRoots(root);
	}

	@Override
	public boolean isNodesAreDraggable() {
		return true;
	}

	@Override
	protected void addChildToParent(TreeTable treeTable, EditableTreeNode parentEditableTreeNode) {
		super.addChildToParent(treeTable, parentEditableTreeNode);
		MenuDisplayConfigEditableTreeNode treeNode = (MenuDisplayConfigEditableTreeNode) parentEditableTreeNode;

		getContainer().setChildrenAllowed(treeNode, treeNode.isChildrenAllowed());

	}

	@Override
	protected void childNodeBuildingListener(TreeTable treeTable, EditableTreeNode parentNode,
											 EditableTreeNodeIsBuildingEvent event) {
		Class rootClass = event.getTreeNode().getModelClass();

		newNode(rootClass, (MenuDisplayConfigComponent) parentNode.getModel(), newNodeRoot -> {
			MenuDisplayConfigEditableTreeNodeFactory menuDisplayConfigEditableTreeNodeFactory = new MenuDisplayConfigEditableTreeNodeFactory();
			if (newNodeRoot != null) {
				EditableTreeNodeFactoryTemplate factory = menuDisplayConfigEditableTreeNodeFactory.getFactoryForClass(newNodeRoot.getMainClass());

				if (factory != null) {
					rebuildRoots((MenuDisplayConfigEditableTreeNode) MenuDisplayConfigEditableTreeNodeFactoryTypes.ROOT.build((MenuDisplayConfigRoot) root.getModel()));
				}
			}
		});
	}

	public void newNode(Class<? extends MenuDisplayConfigComponent> nodeTypeRequired, MenuDisplayConfigComponent parent,
						Consumer<MenuDisplayConfigComponent> newNodeCallback) {
	}

	@Override
	public void deleteNode(EditableTreeNode editableTreeNode) {
		MenuDisplayConfigComponent nodeToDelete = (MenuDisplayConfigComponent) editableTreeNode.getModel();
		deleteNode(nodeToDelete, this::rebuildIfRoot);
	}

	public void deleteNode(MenuDisplayConfigComponent nodeToDelete,
						   Consumer<MenuDisplayConfigComponent> highestParentToRefreshCallback) {
		highestParentToRefreshCallback.accept(nodeToDelete.getParent());
	}


	@Override
	public void selectNode(EditableTreeNode editableTreeNode) {
		editNode((MenuDisplayConfigComponent) editableTreeNode.getModel(), this::rebuildIfRoot);
	}

	public void editNode(MenuDisplayConfigComponent nodeToEdit,
						 Consumer<MenuDisplayConfigComponent> highestParentToRefreshCallback) {
	}

	@Override
	protected void sortTreeNodes() {
	}

	public void moveNode(MenuDisplayConfigComponent nodeToMove, MenuDisplayConfigComponent newParent,
						 MenuDisplayConfigComponent sibling, boolean insertBefore,
						 Consumer<MenuDisplayConfigComponent> nodeToRefreshCallback) {

	}

	private void rebuildIfRoot(MenuDisplayConfigComponent highestParentToRefresh) {
		EditableTreeNode highestTreeNodeToRefresh = searchForTreeNode(highestParentToRefresh);

		if (getContainer().isRoot(highestTreeNodeToRefresh)) {
			rebuildRoots((MenuDisplayConfigEditableTreeNode) MenuDisplayConfigEditableTreeNodeFactoryTypes.ROOT.build((MenuDisplayConfigRoot) highestParentToRefresh));
		}
	}

	private void rebuildRoots(MenuDisplayConfigEditableTreeNode root) {
		List<MenuDisplayConfigEditableTreeNode> nodesAtRoot = new ArrayList<>(root.getNodesToAddAtSameHierarchicalLevel());
		nodesAtRoot.add(0, root);

		List<EditableTreeNode> editableTreeNodesAtRoot = new ArrayList<>();
		nodesAtRoot.stream().forEach(editableTreeNodesAtRoot::add);
		setRoot(editableTreeNodesAtRoot);

		nodesAtRoot.stream().filter(nodeAtRoot -> nodeAtRoot.isDeletableEventWhenItsRoot()).forEach(subMenu ->
				subMenu.setDeletable(true));
	}

	@Override
	protected AcceptCriterion getDragAndDropCriterion() {
		return new ServerSideCriterion() {
			@Override
			public boolean accept(DragAndDropEvent dragEvent) {
				Transferable transferable = dragEvent.getTransferable();
				MenuDisplayConfigEditableTreeNode treeNode = (MenuDisplayConfigEditableTreeNode) transferable.getData("itemId");
				if (treeNode == null) {
					treeNode = (MenuDisplayConfigEditableTreeNode) transferable.getData("editableTreeNode");
				}

				if (treeNode == null) {
					return false;
				}

				AbstractSelectTargetDetails targetDetails = (AbstractSelectTargetDetails) dragEvent.getTargetDetails();
				MenuDisplayConfigEditableTreeNode target = (MenuDisplayConfigEditableTreeNode) targetDetails.getItemIdOver();
				VerticalDropLocation dropLocation = targetDetails.getDropLocation();

				return treeNode != target ? target.isDropManipulationAllowed(treeNode, dropLocation) : false;
			}
		};
	}

	@Override
	protected void dropNode(TreeTable treeTable, DragAndDropEvent event) {

		Transferable t = event.getTransferable();
		MenuDisplayConfigEditableTreeNode droppedNode = (MenuDisplayConfigEditableTreeNode) t.getData("itemId");
		if (droppedNode == null) {
			droppedNode = (MenuDisplayConfigEditableTreeNode) t.getData("editableTreeNode");
		}

		if (droppedNode == null) {
			return;
		}

		AbstractSelectTargetDetails target = (AbstractSelectTargetDetails)
				event.getTargetDetails();

		MenuDisplayConfigEditableTreeNode targetItemId = (MenuDisplayConfigEditableTreeNode) target.getItemIdOver();
		VerticalDropLocation location = target.getDropLocation();

		MenuDisplayConfigComponent droppedNodeComponent = (MenuDisplayConfigComponent) droppedNode.getModel();
		MenuDisplayConfigComponent targetComponent = (MenuDisplayConfigComponent) targetItemId.getModel();
		MenuDisplayConfigComponent parentNodeComponent;
		boolean insertBefore;

		if (location == VerticalDropLocation.MIDDLE) {
			parentNodeComponent = targetComponent;
			insertBefore = false;
		} else {
			boolean targetComponentIsRoot = targetComponent.getParent() == null;

			if (targetComponentIsRoot) {
				parentNodeComponent = (MenuDisplayConfigComponent) root.getModel();
				targetComponent = parentNodeComponent;
				insertBefore = false;
			} else {
				parentNodeComponent = targetComponent.getParent();
				insertBefore = location == VerticalDropLocation.TOP;
			}
		}

		moveNode(droppedNodeComponent, parentNodeComponent, targetComponent, insertBefore, componentToRefresh -> {
			EditableTreeNode highestTreeNodeToRefresh = searchForTreeNode(componentToRefresh);

			if (getContainer().isRoot(highestTreeNodeToRefresh)) {
				rebuildRoots((MenuDisplayConfigEditableTreeNode) MenuDisplayConfigEditableTreeNodeFactoryTypes.ROOT.build((MenuDisplayConfigRoot) componentToRefresh));
			}
		});
	}

	public MenuDisplayConfigComponent getRootComponent() {
		return (MenuDisplayConfigComponent) root.getModel();
	}
}
