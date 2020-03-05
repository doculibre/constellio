package com.constellio.data.events;

import com.constellio.data.events.activeMQ.AbstractConsumer;

/**
 * Have two responsibilities :
 * - send event remotely to other nodes
 * - receive events, dispatching them to the event receiver
 */
public abstract class EventBusSendingService {

	protected boolean paused;

	EventReceiver eventReceiver;
	EventDataSerializer eventDataSerializer;

	public EventReceiver getEventReceiver() {
		return eventReceiver;
	}

	public EventDataSerializer getEventDataSerializer() {
		return eventDataSerializer;
	}

	public final EventBusSendingService setEventReceiver(EventReceiver eventReceiver) {
		this.eventReceiver = eventReceiver;
		return this;
	}

	public final EventBusSendingService setEventDataSerializer(EventDataSerializer eventDataSerializer) {
		this.eventDataSerializer = eventDataSerializer;
		return this;
	}

	public abstract void sendRemotely(Event event);

	public void start(boolean paused) {
		this.paused = paused;
	}

	public void close() {
	}

	public void pause() {
		this.paused = true;
	}

	public void resume() {
		this.paused = false;
	}

	public AbstractConsumer getConsumer(EventBusListener listener, String topic, String busName) {
		return null;
	}
}
