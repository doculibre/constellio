package com.constellio.app.ui.framework.components.content;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.DownloadLink;
import com.constellio.app.ui.framework.buttons.PdfTronContentVersionWindow;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.viewers.pdftron.PdfTronViewer;
import com.constellio.data.utils.dev.Toggle;
import com.vaadin.server.Resource;
import com.vaadin.ui.UI;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class DownloadContentVersionLink extends DownloadLink {

	public static final String STYLE_NAME = "download-content-version-link";

	private String recordId;
	private String metadataCode;
	private Boolean isReadonly = null;
	private ContentVersionVO contentVersionVO;

	public DownloadContentVersionLink(ContentVersionVO contentVersionVO) {
		this(contentVersionVO, contentVersionVO.toString());
	}

	public DownloadContentVersionLink(ContentVersionVO contentVersionVO, String caption) {
		super(new ContentVersionVOResource(contentVersionVO), caption);
		addStyleName(STYLE_NAME);
		setSizeFull();
		this.contentVersionVO = contentVersionVO;
	}

	public DownloadContentVersionLink(ContentVersionVO contentVersionVO, String caption, String recordId,
									  String metadataCode, boolean isReadonly) {
		super(new ContentVersionVOResource(contentVersionVO), caption);
		this.recordId = recordId;
		this.metadataCode = metadataCode;
		this.isReadonly = isReadonly;
		this.contentVersionVO = contentVersionVO;
	}

	public DownloadContentVersionLink(RecordVO recordVO, ContentVersionVO contentVersionVO, String caption,
									  UpdatableContentVersionPresenter presenter) {
		this(recordVO, contentVersionVO, caption, presenter, null, false);
	}

	public DownloadContentVersionLink(RecordVO recordVO, ContentVersionVO contentVersionVO, String caption,
									  UpdatableContentVersionPresenter presenter, String metadataCode,
									  boolean isReadonly) {
		super(new UpdatableContentVersionVOResource(recordVO, contentVersionVO, presenter), caption);
		addStyleName(STYLE_NAME);
		setSizeFull();
		this.contentVersionVO = contentVersionVO;
		this.recordId = recordVO != null ? recordVO.getId() : null;
		this.isReadonly = isReadonly;
		this.metadataCode = metadataCode;
	}

	public DownloadContentVersionLink(RecordVO recordVO, ContentVersionVO contentVersionVO, Resource icon,
									  UpdatableContentVersionPresenter presenter, String metadataCode,
									  boolean isReadonly) {
		this(recordVO, contentVersionVO, "", presenter);
		setIcon(icon);
		this.contentVersionVO = contentVersionVO;
		this.recordId = recordVO != null ? recordVO.getId() : null;
		this.isReadonly = isReadonly;
		this.metadataCode = metadataCode;
	}


	public DownloadContentVersionLink(RecordVO recordVO, ContentVersionVO contentVersionVO, Resource icon,
									  String metadataCode, boolean isReadonly) {
		this(contentVersionVO, "");
		setIcon(icon);
		this.contentVersionVO = contentVersionVO;
		this.recordId = recordVO != null ? recordVO.getId() : null;
		this.isReadonly = isReadonly;
		this.metadataCode = metadataCode;
	}


	private boolean usePdfTron() {
		// contentVersion is only set in pdfTron constructor.
		if (this.contentVersionVO == null) {
			return false;
		}

		String extension = StringUtils.lowerCase(FilenameUtils.getExtension(contentVersionVO.getFileName()));
		return this.contentVersionVO.getContentId() != null && this.recordId != null && this.metadataCode != null && isReadonly != null && Arrays.asList(PdfTronViewer.SUPPORTED_EXTENTION).contains(extension)
			   && (Toggle.ENABLE_PDFTRON_TRIAL.isEnabled() || StringUtils.isNotBlank(PdfTronViewer.getPdfTronKey()));
	}

	private BaseWindow buildPdfTronWindow() {
		return new PdfTronContentVersionWindow(recordId, contentVersionVO, metadataCode, isReadonly, PdfTronViewer.getPdfTronKey());
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void buttonClick(ClickEvent event) {
		if (usePdfTron()) {
			UI.getCurrent().addWindow(buildPdfTronWindow());
		} else {
			super.buttonClick(event);
		}
	}
}
