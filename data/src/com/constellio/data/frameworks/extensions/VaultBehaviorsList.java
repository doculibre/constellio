package com.constellio.data.frameworks.extensions;

import com.constellio.data.frameworks.extensions.ExtensionUtils.BooleanCaller;

import java.util.Iterator;
import java.util.List;

public class VaultBehaviorsList<T> extends PriorityOrderedList<T> {


	public List<T> getExtensions() {
		return getItems();
	}

	public Boolean getBooleanValue(Boolean defaultValue, BooleanCaller<T> caller) {
		return ExtensionUtils.getBooleanValue(this, defaultValue, caller);
	}


	public void remove(Class<?> clazz) {
		Iterator<OrderedItems<T>> iterator = items.iterator();
		while (iterator.hasNext()) {
			if (clazz.isAssignableFrom(iterator.next().behavior.getClass())) {
				iterator.remove();
			}
		}

	}
}
