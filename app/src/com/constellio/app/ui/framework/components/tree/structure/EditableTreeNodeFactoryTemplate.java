package com.constellio.app.ui.framework.components.tree.structure;

public interface EditableTreeNodeFactoryTemplate<T> {
	EditableTreeNode<T> build(T model);

	EditableTreeNode<T> build();
}
