package com.constellio.app.ui.framework.components.viewers;

import java.util.Arrays;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.viewers.audio.AudioViewer;
import com.constellio.app.ui.framework.components.viewers.document.DocumentViewer;
import com.constellio.app.ui.framework.components.viewers.image.ImageViewer;
import com.constellio.app.ui.framework.components.viewers.image.TiffImageViewer;
import com.constellio.app.ui.framework.components.viewers.video.VideoViewer;
import com.constellio.app.ui.util.ComponentTreeUtils;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Window;

public class ContentViewer extends CustomComponent {

	private Component viewerComponent;

	public ContentViewer(RecordVO recordVO, String metadataCode, ContentVersionVO contentVersionVO) {
		setId(UUID.randomUUID().toString());
		if (contentVersionVO != null) {
			String fileName = contentVersionVO.getFileName();
			String extension = StringUtils.lowerCase(FilenameUtils.getExtension(fileName));

			if (Arrays.asList(ImageViewer.SUPPORTED_EXTENSIONS).contains(extension)) {
				ImageViewer imageViewer = new ImageViewer(recordVO, Document.CONTENT, contentVersionVO);
				viewerComponent = imageViewer;
			} else if (Arrays.asList(TiffImageViewer.SUPPORTED_EXTENSIONS).contains(extension)) {
				TiffImageViewer tiffImageViewer = new TiffImageViewer(recordVO, Document.CONTENT, contentVersionVO);
				viewerComponent = tiffImageViewer;
			} else if (Arrays.asList(AudioViewer.SUPPORTED_EXTENSIONS).contains(extension)) {
				AudioViewer audioViewer = new AudioViewer(contentVersionVO);
				viewerComponent = audioViewer;
			} else if (Arrays.asList(VideoViewer.SUPPORTED_EXTENSIONS).contains(extension)) {
				VideoViewer videoViewer = new VideoViewer(contentVersionVO);
				viewerComponent = videoViewer;
			} else if (DocumentViewer.isSupported(fileName)) {
				DocumentViewer documentViewer;
				if (recordVO instanceof DocumentVO) {
					documentViewer = new DocumentViewer(recordVO, Document.CONTENT, contentVersionVO);
				} else if (metadataCode != null) {
					documentViewer = new DocumentViewer(recordVO, metadataCode, contentVersionVO);
				} else {
					documentViewer = null;
				}
				viewerComponent = documentViewer;
			}
		}
		if (viewerComponent == null) {
			setVisible(false);
		} else {
			setCompositionRoot(viewerComponent);
		}
	}

	public boolean isViewerComponentVisible() {
		return isVisible() && viewerComponent != null && viewerComponent.isVisible();
	}

	@Override
	public void attach() {
		super.attach();
//		if (ComponentTreeUtils.findParent(this, Window.class) != null) {
//			String js = "document.getElementById('" + getId() + "').style.position='fixed';";
//			js += "document.getElementById('" + getId() + "').style.width=calc(100% - 850px);";
//			JavaScript.getCurrent().execute(js);
//		}
	}

}
