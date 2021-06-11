package com.constellio.app.events;

public interface EventListener<T extends EventArgs> {
	void eventFired(T args);
}
