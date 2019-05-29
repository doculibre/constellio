package com.constellio.model.services.collections.exceptions;

public class NoMoreCollectionAvalibleException extends Exception {
	public NoMoreCollectionAvalibleException() {
		super("The maximum number of collection is reached");
	}
}
