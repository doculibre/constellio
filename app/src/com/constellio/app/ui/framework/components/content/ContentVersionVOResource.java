package com.constellio.app.ui.framework.components.content;

import java.io.InputStream;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.vaadin.server.StreamResource;

public class ContentVersionVOResource extends StreamResource {

	private static final String STREAM_NAME = "ContentVersionVOResource-InputStream";

	public ContentVersionVOResource(final ContentVersionVO contentVersionVO) {
		super(new StreamSource() {
			@Override
			public InputStream getStream() {
				return contentVersionVO.getInputStreamProvider().getInputStream(STREAM_NAME);
			}
		}, contentVersionVO.getFileName());
	}

}
