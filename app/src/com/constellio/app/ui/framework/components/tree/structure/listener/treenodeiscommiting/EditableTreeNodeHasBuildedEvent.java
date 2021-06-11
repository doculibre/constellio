package com.constellio.app.ui.framework.components.tree.structure.listener.treenodeiscommiting;

import com.constellio.app.ui.framework.components.tree.structure.EditableTreeNode;
import com.constellio.app.ui.framework.components.tree.structure.listener.EditableTreeNodeEvent;

public class EditableTreeNodeHasBuildedEvent extends EditableTreeNodeEvent {
	public static final String EVENT_TYPE = "TreeNodeHasBuilded";

	public EditableTreeNodeHasBuildedEvent(EditableTreeNode editableTreeNode) {
		super(editableTreeNode);
	}

	@Override
	public String getEventType() {
		return EVENT_TYPE;
	}
}
