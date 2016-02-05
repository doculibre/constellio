package com.constellio.app.services.appManagement;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.constellio.data.io.streamFactories.StreamFactory;

public class AppManagementServiceDownload extends Thread {
	private boolean result;
	private StreamFactory<OutputStream> destination;
	private InputStream download;
	private AppManagementService receiver;

	public AppManagementServiceDownload(InputStream download, StreamFactory<OutputStream> destination, AppManagementService receiver) {
		super();
		this.destination = destination;
		this.download = download;
		this.receiver = receiver;
	}

	@Override
	public void run() {
		result = true;
		try {
			OutputStream warFileOutput = destination.create("war upload");

			byte[] buffer = new byte[8 * 1024];
			try {
				int bytesRead;
				while ((bytesRead = download.read(buffer)) != -1) {
					warFileOutput.write(buffer, 0, bytesRead);
				}
			} finally {
				warFileOutput.close();
			}
		} catch (IOException ioe) {
			result = false;
		}
	}

	public boolean getResult() {
		return result;
	}
}
