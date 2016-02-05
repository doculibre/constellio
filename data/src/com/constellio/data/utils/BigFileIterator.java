package com.constellio.data.utils;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class BigFileIterator extends LazyIterator<BigFileEntry> {

	String encoding;

	DataInputStream inputStream;

	public BigFileIterator(InputStream inputStream) {
		this(inputStream, "UTF-8");
	}

	public BigFileIterator(InputStream inputStream, String encoding) {
		this.inputStream = new DataInputStream(inputStream);
		this.encoding = encoding;
	}

	private String readString(int stringLength)
			throws IOException {
		byte[] stringBytes = new byte[stringLength];
		inputStream.read(stringBytes);
		return new String(stringBytes, encoding);
	}

	@Override
	protected BigFileEntry getNextOrNull() {

		try {
			int fileNameLength = inputStream.readInt();
			if (fileNameLength == -1) {
				return null;
			}
			String fileName = readString(fileNameLength);
			int contentLength = inputStream.readInt();

			byte[] content = new byte[contentLength];

			inputStream.read(content);
			return new BigFileEntry(fileName, content);

		} catch (EOFException e) {
			return null;

		} catch (IOException io) {
			throw new RuntimeException(io);

		}
	}
}
