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

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BigFileWriter {

	String encoding;

	DataOutputStream outputStream;

	byte[] buffer = new byte[4096];

	public BigFileWriter(OutputStream outputStream, String encoding) {
		this.outputStream = new DataOutputStream(outputStream);
		this.encoding = encoding;
	}

	public BigFileWriter(OutputStream outputStream) {
		this(outputStream, "UTF-8");
	}

	public void write(File file)
			throws IOException {
		byte[] fileNameBytes = file.getName().getBytes();

		outputStream.writeInt(fileNameBytes.length);
		outputStream.write(fileNameBytes);
		outputStream.writeInt((int) file.length());

		InputStream in = new BufferedInputStream(new FileInputStream(file));
		try {
			copyStream(in);
		} finally {
			in.close();
		}

	}

	public void copyStream(InputStream input)
			throws IOException {
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			outputStream.write(buffer, 0, n);
		}
	}
}
