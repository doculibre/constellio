package com.constellio.app.ui.framework.components.fields.download;

public class DownloadEvent {

	final static public long INVALID_SIZE = -1;

	final private TempFileDownload fileDownload;

	final private long progressDownload;

	final private Exception exception;

	DownloadEvent(TempFileDownload fileDl, long progress) {
		fileDownload = fileDl;
		progressDownload = progress;
		exception = null;
	}

	DownloadEvent(TempFileDownload fileDl) {
		fileDownload = fileDl;
		progressDownload = INVALID_SIZE;
		exception = null;
	}

	DownloadEvent(TempFileDownload fileDl, Exception ex) {
		fileDownload = fileDl;
		progressDownload = INVALID_SIZE;
		exception = ex;
	}

	public final long getProgressDownload() {
		return progressDownload;
	}

	public final TempFileDownload getTempFileDownload() {
		return fileDownload;
	}

	public final Exception getException() {
		return exception;
	}

}
