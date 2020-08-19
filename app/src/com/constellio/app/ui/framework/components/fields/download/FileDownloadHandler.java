package com.constellio.app.ui.framework.components.fields.download;

import java.util.Collection;

public interface FileDownloadHandler {

	public void downloadStarted(DownloadEvent event);

	public void downloadFinished(DownloadEvent event);

	public void downloadFailed(DownloadEvent event);

	public void onProgress(DownloadEvent event);

	public void filesQueued(Collection<TempFileDownload> pendingFileNames);

	public boolean isInterrupted();
}
