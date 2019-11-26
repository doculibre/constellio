package com.constellio.app.ui.framework.components.content;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.DownloadLink;
import com.constellio.app.ui.framework.buttons.PdfTronContentVersionWindow;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.viewers.pdftron.PdfTronViewer;
import com.vaadin.server.Resource;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class DownloadContentVersionLink extends DownloadLink {

	public static final String STYLE_NAME = "download-content-version-link";

	private String recordId;
	private String metadataCode;
	private boolean hasRightToAnnotate;
	private ContentVersionVO contentVersionVO;

	public DownloadContentVersionLink(ContentVersionVO contentVersionVO) {
		this(contentVersionVO, contentVersionVO.toString());
	}

	public DownloadContentVersionLink(ContentVersionVO contentVersionVO, String caption) {
		super(new ContentVersionVOResource(contentVersionVO), caption);
		addStyleName(STYLE_NAME);
		setSizeFull();
	}

	public DownloadContentVersionLink(ContentVersionVO contentVersionVO, String caption, String recordId,
									  String metadataCode, boolean hasRightToAnnotate) {
		super(new ContentVersionVOResource(contentVersionVO), caption);
		this.recordId = recordId;
		this.metadataCode = metadataCode;
		this.hasRightToAnnotate = hasRightToAnnotate;
		this.contentVersionVO = contentVersionVO;
	}

	public DownloadContentVersionLink(RecordVO recordVO, ContentVersionVO contentVersionVO, String caption,
									  UpdatableContentVersionPresenter presenter) {
		super(new UpdatableContentVersionVOResource(recordVO, contentVersionVO, presenter), caption);
		addStyleName(STYLE_NAME);
		setSizeFull();
	}

	public DownloadContentVersionLink(RecordVO recordVO, ContentVersionVO contentVersionVO, Resource icon,
									  UpdatableContentVersionPresenter presenter) {
		this(recordVO, contentVersionVO, "", presenter);
		setIcon(icon);
	}

	public DownloadContentVersionLink(ContentVersionVO contentVersionVO, Resource icon) {
		this(contentVersionVO, "");
		setIcon(icon);
	}

	private boolean usePdfTron() {
		String extension = StringUtils.lowerCase(FilenameUtils.getExtension(contentVersionVO.getFileName()));
		return Arrays.asList(PdfTronViewer.SUPPORTED_EXTENTION).contains(extension);
	}

	private BaseWindow buildPdfTronWindow() {
		return new PdfTronContentVersionWindow(recordId, contentVersionVO, metadataCode, hasRightToAnnotate);
	}

	@Override
	public void click() {

		if (usePdfTron()) {

		} else {
			super.click();
		}
	}
}
