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
package com.constellio.data.io.streamFactories.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.streamFactories.CloseableStreamFactory;
import com.constellio.data.io.streamFactories.impl.CopyInputStreamFactoryRuntimeException.CannotReadInputStreamRuntime;
import com.constellio.data.io.streamFactories.impl.CopyInputStreamFactoryRuntimeException.CannotWriteInputContentInAFileRuntime;
import com.constellio.data.io.streamFactories.impl.CopyInputStreamFactoryRuntimeException.InputStreamIsNull;
import com.constellio.data.utils.Octets;

public class CopyInputStreamFactory implements CloseableStreamFactory<InputStream> {

	private static final String TEMPFILE_RESOURCE_NAME = "CopyInputStreamFactory-TempFile";
	private static final String COPY_TO_TEMPFILE_OUTPUT_STREAM = "CopyInputStreamFactory-CopyToTempFileOut";
	private static final String COPY_TO_TEMPFILE_STREAM = "CopyInputStreamFactory-CopyToTempFile";

	private final IOServices ioServices;
	private final int byteArraySize;
	File tempMemoryFile;
	byte[] receivedInputStreamBytes;
	byte[] staticBuffer;
	private long length;

	public CopyInputStreamFactory(IOServices ioServices, Octets octets) {
		this.byteArraySize = (int) octets.getOctets();
		this.ioServices = ioServices;
		this.tempMemoryFile = null;
		this.receivedInputStreamBytes = null;
		this.staticBuffer = new byte[byteArraySize];
	}

	public void saveInputStreamContent(InputStream inputStream)
			throws CopyInputStreamFactoryRuntimeException {
		if (inputStream == null) {
			throw new InputStreamIsNull();
		}
		//		byte[] buffer = new byte[byteArraySize];
		//		length = getInputStreamContent(inputStream, buffer);
		//
		//		if (length < byteArraySize) {
		//			receivedInputStreamBytes = Arrays.copyOfRange(buffer, 0, (int) length);
		//		} else {
		//			writeContentInTempFile(inputStream, buffer);
		//		}
		writeContentInTempFile(inputStream);
	}

	private int getInputStreamContent(InputStream inputStream, byte[] buffer)
			throws CopyInputStreamFactoryRuntimeException {
		try {
			int nbBuffer = inputStream.read(buffer);
			if (nbBuffer == -1) {
				throw new CannotReadInputStreamRuntime(new Exception());
			}
			return nbBuffer;
		} catch (IOException e) {
			throw new CannotReadInputStreamRuntime(e);
		}
	}

	void writeContentInTempFile(InputStream inputStream)
			throws CopyInputStreamFactoryRuntimeException {

		tempMemoryFile = ioServices.newTemporaryFile(TEMPFILE_RESOURCE_NAME);
		OutputStream fileOutputStream = null;
		try {
			fileOutputStream = ioServices.newFileOutputStream(tempMemoryFile, COPY_TO_TEMPFILE_OUTPUT_STREAM);
			IOUtils.copy(inputStream, fileOutputStream);
			ioServices.closeQuietly(fileOutputStream);
		} catch (IOException e) {
			ioServices.closeQuietly(fileOutputStream);

			tempMemoryFile.delete();
			throw new com.constellio.data.io.streamFactories.CopyInputStreamFactoryRuntimeException.CannotWriteContentInTempFile(
					tempMemoryFile.getPath(), e);

		} catch (CopyInputStreamFactoryRuntimeException e) {
			ioServices.closeQuietly(fileOutputStream);
			tempMemoryFile.delete();
			throw e;
		}
		length = tempMemoryFile.length();

	}

	void writeContentInFile(InputStream inputStream, byte[] buffer, File file)
			throws CopyInputStreamFactoryRuntimeException, FileNotFoundException {

		OutputStream tempFileOutputStream = ioServices.newBufferedFileOutputStream(file, COPY_TO_TEMPFILE_STREAM);

		try {
			tempFileOutputStream.write(buffer);
			ioServices.copyLarge(inputStream, tempFileOutputStream);
		} catch (IOException e) {
			throw new CannotWriteInputContentInAFileRuntime(e);

		} finally {
			ioServices.closeQuietly(tempFileOutputStream);
		}
	}

	@Override
	public InputStream create(String name)
			throws IOException {
		if (tempMemoryFile != null) {
			return ioServices.newBufferedFileInputStream(tempMemoryFile, name);
		} else {
			return ioServices.newBufferedByteArrayInputStream(receivedInputStreamBytes, name);
		}
	}

	@Override
	public long length() {
		return length;
	}

	@Override
	public void close() {
		ioServices.deleteQuietly(tempMemoryFile);
	}

}
