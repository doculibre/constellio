package com.constellio.app.api.extensions.params;

import java.io.InputStream;

public class ParseEmailMessageParams {

	private final InputStream inputStream;

	private final String filename;

	public ParseEmailMessageParams(InputStream inputStream, String filename) {
		super();
		this.inputStream = inputStream;
		this.filename = filename;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public String getFilename() {
		return filename;
	}

}
