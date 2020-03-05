package com.constellio.data.events;

public interface EventBusListener extends AutoCloseable {

	void onEventReceived(Event event);

}
