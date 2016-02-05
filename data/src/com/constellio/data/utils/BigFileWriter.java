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
