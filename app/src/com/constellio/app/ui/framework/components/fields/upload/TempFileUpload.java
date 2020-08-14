package com.constellio.app.ui.framework.components.fields.upload;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.Serializable;

public class TempFileUpload implements Serializable {

	private String fileName;

	private String mimeType;

	private long length;

	private File tempFile;

	public TempFileUpload(String fileName, String mimeType, long length, File tempFile) {
		super();
		this.fileName = fileName;
		this.mimeType = mimeType;
		this.length = length;
		this.tempFile = tempFile;
	}

	public void replaceTempFile(File newFile) {
		delete();

		this.length = newFile.length();
		this.tempFile = newFile;
	}

	public final String getFileName() {
		return fileName;
	}

	public final String getMimeType() {
		return mimeType;
	}

	public final long getLength() {
		return length;
	}

	public final File getTempFile() {
		return tempFile;
	}

	public void delete() {
		FileUtils.deleteQuietly(tempFile);
	}

}