package com.constellio.app.ui.framework.components.content;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.vaadin.server.StreamResource;

import java.io.InputStream;

public class UpdatableContentVersionVOResource extends StreamResource {

	private static final String STREAM_NAME = "ContentVersionVOResource-InputStream";

	public UpdatableContentVersionVOResource(final RecordVO recordVO, final ContentVersionVO contentVersionVO, final UpdatableContentVersionPresenter presenter) {
		super(new StreamSource() {
			@Override
			public InputStream getStream() {
				ContentVersionVO updatedContentVersionVO = presenter.getUpdatedContentVersionVO(recordVO, contentVersionVO);
				return updatedContentVersionVO.getInputStreamProvider().getInputStream(STREAM_NAME);
			}
		}, getFilename(contentVersionVO));
	}
	
	private static String getFilename(ContentVersionVO contentVersionVO) {
		String filename = contentVersionVO.getFileName();
		filename = filename.replace("%", "");
		return filename;
	}
}
