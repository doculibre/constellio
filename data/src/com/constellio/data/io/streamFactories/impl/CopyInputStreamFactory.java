package com.constellio.data.io.streamFactories.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.streamFactories.CloseableStreamFactory;
import com.constellio.data.io.streamFactories.StreamFactoryWithFilename;
import com.constellio.data.io.streamFactories.impl.CopyInputStreamFactoryRuntimeException.InputStreamIsNull;
import com.constellio.data.utils.Octets;

public class CopyInputStreamFactory implements CloseableStreamFactory<InputStream>, StreamFactoryWithFilename<InputStream> {

	private static final String TEMPFILE_RESOURCE_NAME = "CopyInputStreamFactory-TempFile";
	private static final String COPY_TO_TEMPFILE_OUTPUT_STREAM = "CopyInputStreamFactory-CopyToTempFileOut";

	private String filename;
	private final IOServices ioServices;
	File tempMemoryFile;
	private long length;

	public CopyInputStreamFactory(IOServices ioServices, Octets octets) {
		this.ioServices = ioServices;
	}

	public void saveInputStreamContent(InputStream inputStream, String filename)
			throws CopyInputStreamFactoryRuntimeException {
		this.filename = filename;
		if (inputStream == null) {
			throw new InputStreamIsNull();
		}

		writeContentInTempFile(inputStream);
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

	public File getTempFile() {
		return tempMemoryFile;
	}

	@Override
	public InputStream create(String name)
			throws IOException {
		return ioServices.newBufferedFileInputStream(tempMemoryFile, name);
	}

	@Override
	public long length() {
		return length;
	}

	@Override
	public void close() {
		ioServices.deleteQuietly(tempMemoryFile);
	}

	@Override
	public String getFilename() {
		return filename;
	}
}
