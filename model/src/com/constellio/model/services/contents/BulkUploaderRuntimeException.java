package com.constellio.model.services.contents;

public class BulkUploaderRuntimeException extends RuntimeException {

	public BulkUploaderRuntimeException(String key, Throwable cause) {
		super("Error uploading '" + key + "'", cause);
	}

}
