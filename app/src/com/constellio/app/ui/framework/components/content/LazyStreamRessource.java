package com.constellio.app.ui.framework.components.content;

import com.vaadin.server.StreamResource;

import java.io.InputStream;

public class LazyStreamRessource extends StreamResource {
	private static final String STREAM_NAME = "ContentVersionVOResource-InputStream";
	private boolean actionDone = false;

	public LazyStreamRessource(final InputStreamWrapper inputStreamWrapper, String title) {
		super(new StreamSource() {
			@Override
			public InputStream getStream() {
				inputStreamWrapper.doActionIfNotAlreadyDone();
				return inputStreamWrapper.getInputStream();
			}
		}, title);
	}

}
