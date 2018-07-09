package com.constellio.model.extensions.events.records;

import com.constellio.model.entities.records.Record;

public interface ConsumableEvent {

	public boolean isConsumed();

	public void consume();
}
