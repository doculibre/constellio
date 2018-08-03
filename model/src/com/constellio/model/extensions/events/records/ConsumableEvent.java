package com.constellio.model.extensions.events.records;

public interface ConsumableEvent {

	public boolean isConsumed();

	public void consume();
}
