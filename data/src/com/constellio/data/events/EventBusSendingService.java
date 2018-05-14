package com.constellio.data.events;

/**
 * Have two responsibilities :
 * - send event remotely to other nodes
 * - receive events, dispatching them to the event receiver
 */
public abstract class EventBusSendingService {

	protected boolean paused;

	EventReceiver eventReceiver;
	EventDataSerializer eventDataSerializer;

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
}
