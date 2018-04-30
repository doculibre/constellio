package com.constellio.data.events;

public interface EventBusListener {

	void onEventReceived(Event event);

}
