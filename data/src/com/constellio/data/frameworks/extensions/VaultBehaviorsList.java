package com.constellio.data.frameworks.extensions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.constellio.data.frameworks.extensions.ExtensionUtils.BooleanCaller;

public class VaultBehaviorsList<T> implements Iterable<T> {

	List<OrderedExtension<T>> extensions = new ArrayList<>();

	public void add(T extension) {
		add(100, extension);
	}

	public void add(int priority, T extension) {
		extensions.add(new OrderedExtension<>(extension, priority));
		Collections.sort(extensions);
	}

	public List<T> getExtensions() {
		List<T> returnedBehaviors = new ArrayList<>();
		for (OrderedExtension<T> extension : extensions) {
			returnedBehaviors.add(extension.behavior);
		}

		return returnedBehaviors;
	}

	public Boolean getBooleanValue(Boolean defaultValue, BooleanCaller<T> caller) {
		return ExtensionUtils.getBooleanValue(this, defaultValue, caller);
	}

	@Override
	public Iterator<T> iterator() {
		return getExtensions().iterator();
	}

	public void remove(Class<?> clazz) {
		Iterator<OrderedExtension<T>> iterator = extensions.iterator();
		while (iterator.hasNext()) {
			if (clazz.isAssignableFrom(iterator.next().behavior.getClass())) {
				iterator.remove();
			}
		}

	}
}
