package com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes;

import com.constellio.app.ui.framework.components.tree.structure.EditableTreeNodeFactoryType;

public abstract class MenuDisplayConfigEditableTreeNodeFactoryType<T extends MenuDisplayConfigComponent> extends EditableTreeNodeFactoryType<T> {

	protected MenuDisplayConfigEditableTreeNodeFactoryType(Class<T> sourceClass) {
		super(sourceClass);
	}
}
