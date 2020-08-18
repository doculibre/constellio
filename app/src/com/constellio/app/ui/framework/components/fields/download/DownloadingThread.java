package com.constellio.app.ui.framework.components.fields.download;

import com.vaadin.ui.UI;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.atomic.AtomicBoolean;

public class DownloadingThread extends Thread {

	final private FileDownloadHandler fileDownloadHandler;
	final private UI baseFileDownloadUi;
	final private URLConnection con;
	final private TempFileDownload tempFileDownload;
	final private UI baseDownloadFieldUi;
	final private AtomicBoolean pageEnded = new AtomicBoolean(false);

	final static long TRANSFER_BYTES_AT_ONCE = 1024 * 1024;

	DownloadingThread(FileDownloadHandler fileDownloadHandler,
					  URLConnection con,
					  TempFileDownload tempFileDownload,
					  UI baseDownloadFieldUi, UI baseFileDownloadUi) {
		this.fileDownloadHandler = fileDownloadHandler;
		this.baseFileDownloadUi = baseFileDownloadUi;
		this.con = con;
		this.tempFileDownload = tempFileDownload;
		this.baseDownloadFieldUi = baseDownloadFieldUi;
	}

	public void setPageEnded(boolean flag) {
		pageEnded.set(flag);
	}

	private boolean checkValidityAndAccess(Runnable run) {
		boolean result = false;

		if (baseDownloadFieldUi.isAttached() && baseFileDownloadUi.isAttached()) {
			baseFileDownloadUi.access(run);
			result = true;
		}
		return result;
	}

	@Override
	public void run() {

		try {
			BufferedInputStream in = new BufferedInputStream(con.getInputStream());

			long fileSize = tempFileDownload.getLength();
			FileOutputStream fos = new FileOutputStream(tempFileDownload.getTempFile());
			ReadableByteChannel rbc = Channels.newChannel(in);

			for (long offset = 0; offset < fileSize && pageEnded.get() == false; offset += TRANSFER_BYTES_AT_ONCE) {

				fos.getChannel().transferFrom(rbc, offset, TRANSFER_BYTES_AT_ONCE);
				final long progress = offset + TRANSFER_BYTES_AT_ONCE;

				Runnable tmp = () -> {
					fileDownloadHandler.onProgress(new DownloadEvent(tempFileDownload, progress));
				};

				if (checkValidityAndAccess(tmp) == false) {
					pageEnded.set(true);
				}
			}

			if (pageEnded.get() == false) {
				baseDownloadFieldUi.access(() -> {
					fileDownloadHandler.downloadFinished(new DownloadEvent(tempFileDownload, fileSize));
				});
			} else {
				checkValidityAndAccess(() -> {
					fileDownloadHandler.downloadFailed(
							new DownloadEvent(tempFileDownload, new Exception("Undefined error on download.")));
				});
			}

		} catch (Exception ex) {
			checkValidityAndAccess(() -> {
				fileDownloadHandler.downloadFailed(
						new DownloadEvent(tempFileDownload, ex));
			});
		}
	}
}
