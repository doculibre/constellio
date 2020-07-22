package com.constellio.app.api.search;

public class isNotAuthenticatedException extends RuntimeException {
	public isNotAuthenticatedException() {
		super("Invalid serviceKey/token");
	}
}
