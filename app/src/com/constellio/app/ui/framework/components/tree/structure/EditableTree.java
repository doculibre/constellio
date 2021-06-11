package com.constellio.app.ui.framework.components.tree.structure;

import com.constellio.app.ui.framework.components.tree.structure.listener.treenodeisbuilding.EditableTreeNodeIsBuildingEvent;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.DefaultItemSorter;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.themes.ValoTheme;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class EditableTree extends CustomComponent {

	private static final String CSS_STYLE_DRAGGABLE = "draggable";
	private static final String TREE_NODE_PROPERTY_ID = "treeNode";

	private final HierarchicalContainer container;
	private final TreeTable treeTable;
	private ArrayList<EditableTreeNode> roots;
	private boolean isHeightIsDynamic;
	private int pageLength;
	private boolean isEditable;
	private boolean nodesAreDraggable;

	public EditableTree() {
		container = createContainer();

		treeTable = new TreeTable(null, container);
		nodesAreDraggable = false;
		stylizeTree(treeTable);

		setRoot(roots);

		setCompositionRoot(treeTable);
	}

	public EditableTree(EditableTreeNode root) {
		container = createContainer();

		treeTable = new TreeTable(null, container);
		stylizeTree(treeTable);

		setRoot(roots);

		setCompositionRoot(treeTable);
	}

	@Override
	public void attach() {
		super.attach();
		buildMainComponent();
	}

	private void buildMainComponent() {
		if (treeTable != null && isNodesAreDraggable()) {
			setDragMode(treeTable, TableDragMode.ROW);
		}
	}

	HierarchicalContainer createContainer() {
		HierarchicalContainer container = new HierarchicalContainer();
		container.addContainerProperty(TREE_NODE_PROPERTY_ID, EditableTreeNode.class, null);

		container.setItemSorter(new DefaultItemSorter() {
			@Override
			public int compare(Object o1, Object o2) {
				EditableTreeNode editableTreeNode1 = (EditableTreeNode) o1;
				EditableTreeNode editableTreeNode2 = (EditableTreeNode) o2;

				if (!editableTreeNode1.isCommitedOnce()) {
					return -1;
				}
				if (!editableTreeNode2.isCommitedOnce()) {
					return 1;
				}

				return editableTreeNode1.getCaption().compareToIgnoreCase(editableTreeNode2.getCaption());
			}
		});

		return container;
	}

	public void fillContainerWithTreeNodes(Container container, EditableTreeNode editableTreeNode) {

		if (container.containsId(editableTreeNode)) {
			container.removeItem(editableTreeNode);
		}

		Item item = container.addItem(editableTreeNode);
		item.getItemProperty(TREE_NODE_PROPERTY_ID).setValue(editableTreeNode);

		editableTreeNode.getChildren().stream().forEach(childNode -> fillContainerWithTreeNodes(container, (EditableTreeNode) childNode));
	}


	private void stylizeTree(TreeTable treeTable) {

		treeTable.setSizeFull();
		treeTable.addStyleName("structure-tree-editor");
		treeTable.addStyleName(ValoTheme.TREETABLE_BORDERLESS);
		treeTable.addStyleName(ValoTheme.TREETABLE_NO_HEADER);
		treeTable.addStyleName(ValoTheme.TREETABLE_NO_HORIZONTAL_LINES);
		treeTable.addStyleName(ValoTheme.TREETABLE_NO_VERTICAL_LINES);
		treeTable.addStyleName(ValoTheme.TREETABLE_NO_STRIPES);
		treeTable.addStyleName(ValoTheme.TREETABLE_COMPACT);

		treeTable.setColumnExpandRatio(TREE_NODE_PROPERTY_ID, 1);

		treeTable.setVisibleColumns(TREE_NODE_PROPERTY_ID);

		setPageLength(15);
		setHeightIsDynamic(true);

		adjustHeight(treeTable);
	}

	protected void adjustHeight(TreeTable table) {
		if (isHeightIsDynamic()) {
			table.setPageLength(table.getItemIds().size());
		} else {
			table.setPageLength(getPageLength());
		}

		markAsDirty();
	}

	private void expandsTreeNodesWithChildren(TreeTable treeTable, EditableTreeNode parentEditableTreeNode) {
		boolean hasChildren = treeTable.hasChildren(parentEditableTreeNode);

		treeTable.setCollapsed(parentEditableTreeNode, !hasChildren);

		if (hasChildren) {
			treeTable
					.getChildren(parentEditableTreeNode)
					.stream()
					.forEach(itemId -> {
						Item item = treeTable.getItem(itemId);
						EditableTreeNode editableTreeNode = (EditableTreeNode) (item.getItemProperty(TREE_NODE_PROPERTY_ID).getValue());
						expandsTreeNodesWithChildren(treeTable, editableTreeNode);
					});
		}
	}

	protected void addChildToParent(TreeTable treeTable, EditableTreeNode parentEditableTreeNode) {

		createChildNodeSelectedListenerOnTreeNode(parentEditableTreeNode);
		createChildNodeBuildingListenerOnTreeNode(treeTable, parentEditableTreeNode);
		createTreeNodeHasBuildedListenerOnTreeNode(parentEditableTreeNode);
		createReplaceThisTreeNodeListenerOnTreeNode(treeTable, parentEditableTreeNode);
		createRemoveThisTreeNodeListenerOnTreeNode(parentEditableTreeNode);

		treeTable.setChildrenAllowed(parentEditableTreeNode, true);

		if (parentEditableTreeNode.hasChildren()) {
			parentEditableTreeNode.getChildren().stream().forEach(childNode -> {
				treeTable.setParent(childNode, parentEditableTreeNode);

				addChildToParent(treeTable, (EditableTreeNode) childNode);
			});

			treeTable.setCollapsed(parentEditableTreeNode, false);
		}

		treeTable.setChildrenAllowed(parentEditableTreeNode, treeTable.hasChildren(parentEditableTreeNode));
	}

	private void createChildNodeBuildingListenerOnTreeNode(TreeTable treeTable, EditableTreeNode editableTreeNode) {
		editableTreeNode.addChildNodeIsBuildingListener(treeNodeIsBuildingEvent ->
				childNodeBuildingListener(treeTable, editableTreeNode, treeNodeIsBuildingEvent));
	}

	protected void childNodeBuildingListener(TreeTable treeTable, EditableTreeNode parentNode,
											 EditableTreeNodeIsBuildingEvent event) {
		EditableTreeNode newChildNode = event.getTreeNode();

		fillContainerWithTreeNodes(container, newChildNode);

		treeTable.setChildrenAllowed(parentNode, true);
		treeTable.setChildrenAllowed(newChildNode, false);


		treeTable.setParent(newChildNode, parentNode);
		treeTable.setCollapsed(parentNode, false);

		adjustHeight(treeTable);

		createChildNodeSelectedListenerOnTreeNode(newChildNode);
		createChildNodeBuildingListenerOnTreeNode(treeTable, newChildNode);
		createTreeNodeHasBuildedListenerOnTreeNode(newChildNode);
		createReplaceThisTreeNodeListenerOnTreeNode(treeTable, newChildNode);
		createRemoveThisTreeNodeListenerOnTreeNode(newChildNode);

		sortTreeNodes();
	}

	protected void createChildNodeSelectedListenerOnTreeNode(EditableTreeNode editableTreeNode) {
		if (!editableTreeNode.hasTreeNodeSelectedListeners()) {
			editableTreeNode.addTreeNodeSelectedListener(event -> {
				selectNode(event.getTreeNode());
			});
		}
	}

	protected void createTreeNodeHasBuildedListenerOnTreeNode(EditableTreeNode editableTreeNode) {
		editableTreeNode.addNodeHasBuildedListener(event -> createNode(event.getTreeNode()));
	}

	protected void createReplaceThisTreeNodeListenerOnTreeNode(TreeTable treeTable, EditableTreeNode editableTreeNode) {
		editableTreeNode.addReplaceThisTreeNodeListener(event -> {
			EditableTreeNode currentEditableTreeNode = event.getCurrentTreeNode();
			EditableTreeNode replacementEditableTreeNode = event.getReplacementTreeNode();

			replaceTreeNode(currentEditableTreeNode, replacementEditableTreeNode);
		});
	}

	protected void createRemoveThisTreeNodeListenerOnTreeNode(EditableTreeNode editableTreeNode) {
		editableTreeNode.addRemoveThisTreeNodeListener(removeThisTreeNodeEvent -> {
			EditableTreeNode nodeToRemove = removeThisTreeNodeEvent.getTreeNode();

			if (editableTreeNode.isCommitedOnce()) {
				deleteNode(removeThisTreeNodeEvent.getTreeNode());
			} else {
				removeTreeNode(editableTreeNode);
			}
		});
	}

	public void setRoot(EditableTreeNode root) {
		setRoot(Collections.singletonList(root));
	}

	public void setRoot(List<EditableTreeNode> roots) {
		if (this.roots != roots) {
			if (roots != null) {
				container.removeAllItems();
				roots.forEach(root -> {
					fillContainerWithTreeNodes(container, root);
					addChildToParent(treeTable, root);
					root.setDeletable(false);
				});
			} else {
				container.removeAllItems();
			}

			this.roots = new ArrayList<>(roots);
		}
	}

	public ArrayList<EditableTreeNode> getRoot() {
		return roots;
	}

	public void setHeightIsDynamic(boolean dynamicHeight) {
		isHeightIsDynamic = dynamicHeight;
		adjustHeight(treeTable);
	}

	public boolean isHeightIsDynamic() {
		return isHeightIsDynamic;
	}

	public void setPageLength(int pageLength) {
		this.pageLength = pageLength;
		adjustHeight(treeTable);
	}

	public int getPageLength() {
		return pageLength;
	}

	public void setEditable(boolean editable) {
		isEditable = editable;
		boolean treeNodesAreForcedNotEditable = !editable;

		if (roots != null) {
			roots.forEach(root -> setForcedNotEditableOnNodeRecursively(root, treeNodesAreForcedNotEditable));
		}
	}

	private void setForcedNotEditableOnNodeRecursively(EditableTreeNode parentNode, boolean isForceNotEditable) {
		parentNode.setForcedNotEditable(isForceNotEditable);

		Collection<?> children = container.getChildren(parentNode);
		if (children != null) {
			children.forEach(child -> setForcedNotEditableOnNodeRecursively((EditableTreeNode) child, isForceNotEditable));
		}

	}

	public boolean isEditable() {
		return isEditable;
	}

	protected void setDragMode(TreeTable treeTable, TableDragMode newDragMode) {
		DropHandler dropHandler;

		switch (newDragMode) {
			case ROW:
				dropHandler = buildSingleRowDropHandler(treeTable);
				break;
			default:
				dropHandler = null;
				break;
		}

		treeTable.setStyleName(CSS_STYLE_DRAGGABLE, dropHandler != null);
		treeTable.setDropHandler(dropHandler);
	}

	private DropHandler buildSingleRowDropHandler(TreeTable treeTable) {
		return new DropHandler() {
			public AcceptCriterion getAcceptCriterion() {
				return getDragAndDropCriterion();
			}

			public void drop(DragAndDropEvent event) {
				dropNode(treeTable, event);
			}
		};
	}

	public EditableTreeNode searchForTreeNode(Object searchedValue) {
		Optional<EditableTreeNode> potentialTreeNode = container
				.getItemIds()
				.stream().map(container::getItem)
				.map(item -> (EditableTreeNode) (item.getItemProperty(TREE_NODE_PROPERTY_ID).getValue()))
				.filter(treeNode -> treeNode.getModel() != null && treeNode.getModel().equals(searchedValue))
				.findFirst();

		if (potentialTreeNode.isPresent()) {
			return potentialTreeNode.get();
		} else {
			return null;
		}
	}

	protected void replaceTreeNode(EditableTreeNode oldNode, EditableTreeNode newNode) {

		boolean rootIsChanged = false;
		Object oldNodeParent = null;

		if (oldNode == newNode) {
			return;
		}

		if (oldNode != null) {
			rootIsChanged = container.isRoot(oldNode);

			oldNodeParent = container.getParent(oldNode);

			container.setParent(oldNode, null);
			container.removeItemRecursively(oldNode);
		}

		if (newNode != null) {
			fillContainerWithTreeNodes(container, newNode);
			container.setParent(newNode, oldNodeParent);

			addChildToParent(treeTable, newNode);
			expandsTreeNodesWithChildren(treeTable, newNode);

		} else if (oldNodeParent != null) {
			EditableTreeNode oldNodeParentEditableTreeNode = (EditableTreeNode) oldNodeParent;
			addChildToParent(treeTable, oldNodeParentEditableTreeNode);
			expandsTreeNodesWithChildren(treeTable, oldNodeParentEditableTreeNode);
		}

		if (rootIsChanged) {
			roots.set(roots.indexOf(oldNode), newNode);
			newNode.setDeletable(false);
		}

		sortTreeNodes();

		adjustHeight(treeTable);
	}

	protected void sortTreeNodes() {
		container.sort(new Object[]{TREE_NODE_PROPERTY_ID}, new boolean[]{true});
	}

	protected void removeTreeNode(EditableTreeNode nodeToRemove) {
		replaceTreeNode(nodeToRemove, null);
	}


	public void createNode(EditableTreeNode editableTreeNode) {
		//Behaviour when there is no model handling the CRUD of the nodes. Can be freely overriden
		editableTreeNode.setEditing(false);
	}

	public void selectNode(EditableTreeNode editableTreeNode) {
	}

	public void deleteNode(EditableTreeNode editableTreeNode) {
		//Behaviour when there is no model handling the CRUD of the nodes. Can be freely overriden
		removeTreeNode(editableTreeNode);
	}

	public EditableTreeNode getParentOf(EditableTreeNode node) {
		return (EditableTreeNode) treeTable.getParent(node);
	}

	protected HierarchicalContainer getContainer() {
		return container;
	}

	public void setNodesAreDraggable(boolean nodesAreDraggable) {
		this.nodesAreDraggable = nodesAreDraggable;
	}

	public boolean isNodesAreDraggable() {
		return nodesAreDraggable;
	}

	protected AcceptCriterion getDragAndDropCriterion() {
		return AcceptAll.get();
	}

	protected void dropNode(TreeTable treeTable, DragAndDropEvent event) {
	}
}
