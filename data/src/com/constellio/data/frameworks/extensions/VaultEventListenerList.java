package com.constellio.data.frameworks.extensions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VaultEventListenerList<T> {

	boolean locked;

	List<OrderedExtension<T>> extensions = new ArrayList<>();

	public void add(VaultEventListener<T> extension) {
		add(100, extension);
	}

	public void add(int priority, VaultEventListener<T> extension) {
		if (locked) {
			throw new RuntimeException("Cannot add listener to a locked listener list");
		}
		extensions.add(new OrderedExtension<>(extension, priority));
		Collections.sort(extensions);
	}

	public void notify(T event) {
		RuntimeException firstException = null;
		for (OrderedExtension<T> extension : extensions) {
			try {
				extension.extension.notify(event);
			} catch (Exception e) {
				if (firstException == null) {
					firstException = new RuntimeException("Event listener thrown an exception", e);
				}
			}
		}
		if (firstException != null) {
			throw firstException;
		}
	}

	public void lock() {
		locked = true;
	}

	private static class OrderedExtension<T> implements Comparable<OrderedExtension<T>> {

		VaultEventListener<T> extension;

		int priority;

		private OrderedExtension(VaultEventListener<T> extension, int priority) {
			this.extension = extension;
			this.priority = priority;
		}

		@Override
		public int compareTo(OrderedExtension<T> other) {
			return new Integer(priority).compareTo(other.priority);
		}
	}
}
