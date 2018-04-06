package com.constellio.data.events;

public class SentEventParams {


	private Event event;

	public SentEventParams(Event event) {
		this.event = event;
	}

	public Event getEvent() {
		return event;
	}
}
