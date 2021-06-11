package com.constellio.app.ui.framework.components.tree.structure;

import com.vaadin.server.Resource;

public abstract class EditableTreeNodeFactoryType<T> implements EditableTreeNodeFactoryTemplate<T> {

	private final Class sourceClass;

	protected EditableTreeNodeFactoryType(Class<T> sourceClass) {
		this.sourceClass = sourceClass;
	}

	public Class getSourceClass() {
		return sourceClass;
	}

	public abstract String getCaption();

	public abstract Resource getIcon();
}
