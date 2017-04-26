package com.constellio.app.ui.framework.components.content;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.DownloadLink;
import com.vaadin.server.Resource;

public class DownloadContentVersionLink extends DownloadLink {
	
	public static final String STYLE_NAME = "download-content-version-link";

	public DownloadContentVersionLink(ContentVersionVO contentVersionVO) {
		this(contentVersionVO, contentVersionVO.toString());
	}

	public DownloadContentVersionLink(ContentVersionVO contentVersionVO, String caption) {
		super(new ContentVersionVOResource(contentVersionVO), caption);
		addStyleName(STYLE_NAME);
		setSizeFull();
	}

	public DownloadContentVersionLink(ContentVersionVO contentVersionVO, Resource icon) {
		this(contentVersionVO, "");
		setIcon(icon);
	}

	public DownloadContentVersionLink(RecordVO recordVO, MetadataVO metadataVO) {
		this((ContentVersionVO) recordVO.get(metadataVO));
	}
}
