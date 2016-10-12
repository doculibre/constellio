package com.constellio.model.services.contents;

public class BulkUploaderRuntimeException extends RuntimeException {

	private String key;

	public BulkUploaderRuntimeException(String key, Throwable cause) {
		super("Error uploading '" + key + "'", cause);
		this.key = key;
	}

	public String getKey() {
		return key;
	}
}
