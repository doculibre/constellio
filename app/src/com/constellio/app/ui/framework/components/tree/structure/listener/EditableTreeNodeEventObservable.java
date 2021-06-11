package com.constellio.app.ui.framework.components.tree.structure.listener;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

public class EditableTreeNodeEventObservable<T extends EditableTreeNodeEvent> {

	public List<EditableTreeNodeEventListener<T>> getListeners() {
		return listeners;
	}

	private final List<EditableTreeNodeEventListener<T>> listeners;

	public EditableTreeNodeEventObservable() {
		this.listeners = new ArrayList<>();
	}

	public <TListener extends EditableTreeNodeEventListener<T>> void addListener(TListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public <TListener extends EditableTreeNodeEventListener<T>> void removeListener(TListener listener) {
		if (listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}

	public void fire(T event) {
		Iterator<EditableTreeNodeEventListener<T>> iter = listeners.iterator();

		while (iter.hasNext()) {
			try {
				EditableTreeNodeEventListener<T> listener = iter.next();

				listener.fireTreeNodeEvent(event);
			} catch (ConcurrentModificationException exception) {
				break;
			}
		}
	}
}
