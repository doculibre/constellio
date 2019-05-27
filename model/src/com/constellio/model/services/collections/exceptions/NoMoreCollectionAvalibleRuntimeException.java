package com.constellio.model.services.collections.exceptions;

public class NoMoreCollectionAvalibleRuntimeException extends RuntimeException {
	public NoMoreCollectionAvalibleRuntimeException() {
		super("The maximum number of collection is reached");
	}
}
