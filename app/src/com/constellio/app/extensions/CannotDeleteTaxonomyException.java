package com.constellio.app.extensions;

public class CannotDeleteTaxonomyException extends RuntimeException {
	public CannotDeleteTaxonomyException(String message) {
		super(message);
	}
}
