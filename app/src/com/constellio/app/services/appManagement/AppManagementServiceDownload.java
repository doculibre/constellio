/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
