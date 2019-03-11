package com.constellio.app.ui.framework.components.viewers.image;

import java.io.File;
import java.io.IOException;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.resource.ConstellioResourceHandler;
import com.vaadin.server.Resource;

import pl.tiffviewer.TiffViewer;

public class TiffImageViewer extends TiffViewer {

	public static String[] SUPPORTED_EXTENSIONS = {"tif", "tiff"};

	private static final int DEFAULT_WIDTH = 800;

	private static final int DEFAULT_HEIGHT = 1000;
	
	private RecordVO recordVO;

	private String metadataCode;

	private ContentVersionVO contentVersionVO;

	public TiffImageViewer(RecordVO recordVO, String metadataCode, ContentVersionVO contentVersionVO) {
		super(getTempFile());
		this.recordVO = recordVO;
		this.metadataCode = metadataCode;
		this.contentVersionVO = contentVersionVO;
		init();
	}

	public TiffImageViewer(File file) {
		super(file);
		init();
	}
	
	private static File getTempFile() {
		try {
			File tempFile = File.createTempFile(TiffImageViewer.class.getName(), ".tif");
			tempFile.deleteOnExit();
			return tempFile;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void init() {
//		setWidth(DEFAULT_WIDTH + "px");
		setWidth("100%");
		setHeight(DEFAULT_HEIGHT + "px");
		
		if (recordVO != null) {
			String version = contentVersionVO.getVersion();
			String filename = contentVersionVO.getFileName();
			Resource contentResource = ConstellioResourceHandler.createResource(recordVO.getId(), metadataCode, version, filename);
			setResource("resourceFile", contentResource);
		}
	}

}
