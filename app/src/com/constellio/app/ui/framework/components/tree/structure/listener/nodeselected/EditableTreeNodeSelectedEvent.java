package com.constellio.app.ui.framework.components.tree.structure.listener.nodeselected;

import com.constellio.app.ui.framework.components.tree.structure.EditableTreeNode;
import com.constellio.app.ui.framework.components.tree.structure.listener.EditableTreeNodeEvent;

public class EditableTreeNodeSelectedEvent extends EditableTreeNodeEvent {

	public static final String EVENT_TYPE = "TreeNodeSelected";

	public EditableTreeNodeSelectedEvent(EditableTreeNode editableTreeNode) {
		super(editableTreeNode);
	}

	@Override
	public String getEventType() {
		return EVENT_TYPE;
	}
}
