package com.constellio.app.events;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

public class EventObservable<T extends EventArgs> {
	private final List<EventListener<T>> listeners;

	public EventObservable() {
		this.listeners = new ArrayList<>();
	}

	public <TListener extends EventListener<T>> void addListener(TListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public <TListener extends EventListener<T>> void removeListener(TListener listener) {
		listeners.remove(listener);
	}

	public void fire(T event) {
		Iterator<EventListener<T>> iter = listeners.iterator();

		while (iter.hasNext()) {
			try {
				EventListener<T> listener = iter.next();

				listener.eventFired(event);
			} catch (ConcurrentModificationException exception) {
				break;
			}
		}
	}
}
