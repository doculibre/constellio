package com.constellio.app.ui.framework.components.tree.structure.listener.treenodeisbuilding;

import com.constellio.app.ui.framework.components.tree.structure.EditableTreeNode;
import com.constellio.app.ui.framework.components.tree.structure.listener.EditableTreeNodeEvent;

public class EditableTreeNodeIsBuildingEvent extends EditableTreeNodeEvent {
	public static final String EVENT_TYPE = "TreeNodeBuilding";

	public EditableTreeNodeIsBuildingEvent(EditableTreeNode editableTreeNode) {
		super(editableTreeNode);
	}

	@Override
	public String getEventType() {
		return EVENT_TYPE;
	}
}
