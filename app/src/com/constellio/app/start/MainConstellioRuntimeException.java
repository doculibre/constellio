package com.constellio.app.start;

@SuppressWarnings("serial")
public class MainConstellioRuntimeException extends RuntimeException {

	public MainConstellioRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public MainConstellioRuntimeException(String message) {
		super(message);
	}

	public MainConstellioRuntimeException(Throwable cause) {
		super(cause);
	}

}