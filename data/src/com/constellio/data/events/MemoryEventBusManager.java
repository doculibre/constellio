package com.constellio.data.events;

public class MemoryEventBusManager extends EventBusManager {
	@Override
	protected void handleSending(Event event) {
		receive(event);
	}
}
