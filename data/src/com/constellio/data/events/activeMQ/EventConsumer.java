package com.constellio.data.events.activeMQ;

import com.constellio.data.events.Event;

public interface EventConsumer extends Runnable {
	Event receiveEvent();
}
