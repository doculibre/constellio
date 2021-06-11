package com.constellio.app.ui.framework.components.tree.structure.listener.removethistreenode;

import com.constellio.app.ui.framework.components.tree.structure.EditableTreeNode;
import com.constellio.app.ui.framework.components.tree.structure.listener.EditableTreeNodeEvent;

public class RemoveThisEditableTreeNodeEvent extends EditableTreeNodeEvent {
	public static final String EVENT_TYPE = "RemoveThisTreeNode";

	public RemoveThisEditableTreeNodeEvent(EditableTreeNode editableTreeNode) {
		super(editableTreeNode);
	}

	@Override
	public String getEventType() {
		return EVENT_TYPE;
	}
}
