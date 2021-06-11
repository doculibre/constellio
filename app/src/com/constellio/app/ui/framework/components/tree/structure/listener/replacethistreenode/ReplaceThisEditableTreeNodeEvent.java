package com.constellio.app.ui.framework.components.tree.structure.listener.replacethistreenode;

import com.constellio.app.ui.framework.components.tree.structure.EditableTreeNode;
import com.constellio.app.ui.framework.components.tree.structure.listener.EditableTreeNodeEvent;

public class ReplaceThisEditableTreeNodeEvent extends EditableTreeNodeEvent {
	public static final String EVENT_TYPE = "ReplaceThisTreeNode";

	private final EditableTreeNode replacementEditableTreeNode;

	public ReplaceThisEditableTreeNodeEvent(EditableTreeNode currentEditableTreeNode,
											EditableTreeNode replacementEditableTreeNode) {
		super(currentEditableTreeNode);
		this.replacementEditableTreeNode = replacementEditableTreeNode;
	}

	@Override
	public String getEventType() {
		return EVENT_TYPE;
	}

	public EditableTreeNode getCurrentTreeNode() {
		return getTreeNode();
	}

	public EditableTreeNode getReplacementTreeNode() {
		return replacementEditableTreeNode;
	}
}
