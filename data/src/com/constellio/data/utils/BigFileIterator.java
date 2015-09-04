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
