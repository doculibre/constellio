package com.constellio.app.ui.framework.components.tree.structure.listener;

public interface EditableTreeNodeEventListener<T extends EditableTreeNodeEvent> {
	void fireTreeNodeEvent(T event);
}
