package com.constellio.app.ui.framework.data.event;

public class UnsupportedEventTypeRuntimeException extends RuntimeException{
	public UnsupportedEventTypeRuntimeException(String eventType) {
		throw new RuntimeException("Unsupported event type " + eventType);
	}
}
