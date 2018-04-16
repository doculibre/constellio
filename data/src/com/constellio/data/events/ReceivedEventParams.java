package com.constellio.data.events;

public class ReceivedEventParams {

	private Event event;

	private boolean remoteEvent;

	public ReceivedEventParams(Event event, boolean remoteEvent) {
		this.event = event;
		this.remoteEvent = remoteEvent;
	}

	public Event getEvent() {
		return event;
	}

	public boolean isRemoteEvent() {
		return remoteEvent;
	}
}
