package com.constellio.app.ui.framework.components.tree.structure.listener;

import com.constellio.app.ui.framework.components.tree.structure.EditableTreeNode;

public abstract class EditableTreeNodeEvent {

	private final EditableTreeNode editableTreeNode;

	public EditableTreeNodeEvent(EditableTreeNode editableTreeNode) {
		this.editableTreeNode = editableTreeNode;
	}

	public abstract String getEventType();

	public EditableTreeNode getTreeNode() {
		return editableTreeNode;
	}
}
