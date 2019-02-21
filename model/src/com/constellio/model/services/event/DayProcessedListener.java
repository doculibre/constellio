package com.constellio.model.services.event;

public interface DayProcessedListener {
	 void lastDateProcessed(DayProcessedEvent dayProcessedEvent);
}
