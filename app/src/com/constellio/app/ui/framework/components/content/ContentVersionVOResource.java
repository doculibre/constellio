package com.constellio.app.ui.framework.components.content;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.vaadin.server.StreamResource;

import java.io.InputStream;

public class ContentVersionVOResource extends StreamResource {

	private static final String STREAM_NAME = "ContentVersionVOResource-InputStream";

	public ContentVersionVOResource(final ContentVersionVO contentVersionVO) {
		super(new StreamSource() {
			@Override
			public InputStream getStream() {
				return contentVersionVO.getInputStreamProvider().getInputStream(STREAM_NAME);
			}
		}, getFilename(contentVersionVO));
	}

	private static String getFilename(ContentVersionVO contentVersionVO) {
		String filename = contentVersionVO.getFileName();
		filename = filename.replace("%", "");
		return filename;
	}

}
