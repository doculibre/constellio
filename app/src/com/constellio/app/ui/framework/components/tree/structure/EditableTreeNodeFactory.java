package com.constellio.app.ui.framework.components.tree.structure;

import java.util.HashMap;
import java.util.Map;

public class EditableTreeNodeFactory {
	private Map<Class, EditableTreeNodeFactoryTemplate> factories;

	public EditableTreeNodeFactory() {
		registerDefaultFactories();
	}

	public <T> EditableTreeNode<T> build(T node) {
		Class clazz = node.getClass();

		return factories.containsKey(clazz) ? factories.get(clazz).build(node) : null;
	}

	public EditableTreeNode build(Class clazz) {
		EditableTreeNodeFactoryTemplate factory = getFactoryForClass(clazz);
		return factory != null ? factory.build() : null;
	}

	public EditableTreeNodeFactoryTemplate getFactoryForClass(Class clazz) {
		return factories.getOrDefault(clazz, null);
	}

	public <T> void registerCustomNodeType(Class<? extends T> clazz,
										   EditableTreeNodeFactoryTemplate<T> factory) {
		if (factories == null) {
			factories = new HashMap<>();
		}

		if (!factories.containsKey(clazz)) {
			factories.put(clazz, factory);
		}
	}

	protected void registerDefaultFactories() {
	}
}
