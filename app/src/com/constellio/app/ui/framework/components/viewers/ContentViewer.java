package com.constellio.app.ui.framework.components.viewers;

import java.util.Arrays;

import org.apache.commons.io.FilenameUtils;

import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.viewers.audio.AudioViewer;
import com.constellio.app.ui.framework.components.viewers.document.DocumentViewer;
import com.constellio.app.ui.framework.components.viewers.image.ImageViewer;
import com.constellio.app.ui.framework.components.viewers.video.VideoViewer;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;

public class ContentViewer extends CustomComponent {
	
	private Component viewerComponent;

	public ContentViewer(RecordVO recordVO, String metadataCode, ContentVersionVO contentVersionVO) {
		if (contentVersionVO != null) {
			String filename = contentVersionVO.getFileName();
			String extension = FilenameUtils.getExtension(filename);
			
			if (Arrays.asList(ImageViewer.SUPPORTED_EXTENSIONS).contains(extension)) {
				ImageViewer imageViewer = new ImageViewer(recordVO, Document.CONTENT, contentVersionVO);
				viewerComponent = imageViewer;
			} else if (Arrays.asList(AudioViewer.SUPPORTED_EXTENSIONS).contains(extension)) {
				AudioViewer audioViewer = new AudioViewer(contentVersionVO);
				viewerComponent = audioViewer;
			} else if (Arrays.asList(VideoViewer.SUPPORTED_EXTENSIONS).contains(extension)) {
				VideoViewer videoViewer = new VideoViewer(contentVersionVO);
				viewerComponent = videoViewer;
			} else if (DocumentViewer.isSupported(filename)) {
				DocumentViewer documentViewer;
				if (recordVO instanceof DocumentVO) {
					documentViewer = new DocumentViewer(recordVO, Document.CONTENT, contentVersionVO);
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

}
