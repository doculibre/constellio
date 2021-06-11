package com.constellio.app.events;

public class EventArgs<T> {
	private T sender;

	public EventArgs(T sender) {
		this.sender = sender;
	}

	public T getSender() {
		return sender;
	}
}
