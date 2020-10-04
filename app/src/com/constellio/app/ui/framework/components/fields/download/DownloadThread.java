package com.constellio.app.ui.framework.components.fields.download;

import com.vaadin.server.ErrorMessage;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.atomic.AtomicBoolean;

public class DownloadThread extends Thread {

	final private ProgressBar fileDownloadHandler;
	final private URLConnection con;
	final private TempFileDownload tempFileDownload;
	final private BaseDownloadField baseDownloadField;
	final private UI baseDownloadFieldUi;
	final private AtomicBoolean pageEnded = new AtomicBoolean(false);

	final static long TRANSFER_BYTES_AT_ONCE = 1024 * 1024;

	DownloadThread(ProgressBar fileDownloadHandler,
				   URLConnection con,
				   TempFileDownload tempFileDownload,
				   BaseDownloadField baseDownloadField) {
		this.fileDownloadHandler = fileDownloadHandler;
		this.con = con;
		this.tempFileDownload = tempFileDownload;
		this.baseDownloadFieldUi = baseDownloadField.getUI();
		this.baseDownloadField = baseDownloadField;
	}

	public void setPageEnded(boolean flag) {
		pageEnded.set(flag);
	}

	private boolean checkValidityAndAccess(Runnable run) {
		boolean result = false;

		if (baseDownloadFieldUi.isAttached() && baseDownloadFieldUi.isAttached()) {
			baseDownloadFieldUi.access(run);
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
					fileDownloadHandler.setValue(progress * 1.0f / fileSize);
				};

				if (checkValidityAndAccess(tmp) == false) {
					pageEnded.set(true);
				}
			}

			if (pageEnded.get() == false) {
				baseDownloadFieldUi.access(() -> {
					fileDownloadHandler.setValue(1.0f);
					baseDownloadField.setValue(tempFileDownload);
				});
			} else {
				checkValidityAndAccess(() -> {
					fileDownloadHandler.setComponentError(new ErrorMessage() {
						@Override
						public ErrorLevel getErrorLevel() {
							return null;
						}

						@Override
						public String getFormattedHtmlMessage() {
							return "Undefined error on download.";
						}
					});
				});
			}

		} catch (Exception ex) {
			checkValidityAndAccess(() -> {
				fileDownloadHandler.setComponentError(new ErrorMessage() {
					@Override
					public ErrorLevel getErrorLevel() {
						return null;
					}

					@Override
					public String getFormattedHtmlMessage() {
						return ex.getMessage();
					}
				});
			});
		}
	}
}
