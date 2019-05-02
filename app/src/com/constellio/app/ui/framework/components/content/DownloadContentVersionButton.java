package com.constellio.app.ui.framework.components.content;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.DownloadButton;
import com.vaadin.server.Resource;

public class DownloadContentVersionButton extends DownloadButton {

	public static final String STYLE_NAME = "download-content-version-button";

	public DownloadContentVersionButton(ContentVersionVO contentVersionVO) {
		this(contentVersionVO, contentVersionVO.toString());
	}

	public DownloadContentVersionButton(ContentVersionVO contentVersionVO, String caption) {
		super(new ContentVersionVOResource(contentVersionVO), caption);
		addStyleName(STYLE_NAME);
		setSizeFull();
	}

	public DownloadContentVersionButton(RecordVO recordVO, ContentVersionVO contentVersionVO, String caption,
										UpdatableContentVersionPresenter presenter) {
		super(new UpdatableContentVersionVOResource(recordVO, contentVersionVO, presenter), caption);
		addStyleName(STYLE_NAME);
		setSizeFull();
	}

	public DownloadContentVersionButton(RecordVO recordVO, ContentVersionVO contentVersionVO, Resource icon,
										UpdatableContentVersionPresenter presenter) {
		this(recordVO, contentVersionVO, "", presenter);
		setIcon(icon);
	}

	public DownloadContentVersionButton(ContentVersionVO contentVersionVO, Resource icon) {
		this(contentVersionVO, "");
		setIcon(icon);
	}

	public DownloadContentVersionButton(RecordVO recordVO, MetadataVO metadataVO) {
		this((ContentVersionVO) recordVO.get(metadataVO));
	}
}
