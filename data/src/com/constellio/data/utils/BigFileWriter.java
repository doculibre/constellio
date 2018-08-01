package com.constellio.data.utils;

import java.io.*;

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

	public static void main(String argv[])
			throws Exception {
		FileOutputStream fos = new FileOutputStream(new File("/Users/francisbaril/Downloads/file2.bigf"));
		BigFileWriter writer = new BigFileWriter(fos);

		writer.write(new File(
				"/Users/francisbaril/IdeaProjects/constellio-dev/constellio/sdk/sdk-resources/com/constellio/model/services/contents/ContentManagementAcceptTest-pdf1.pdf"));

		writer.write(new File(
				"/Users/francisbaril/IdeaProjects/constellio-dev/constellio/sdk/sdk-resources/com/constellio/model/services/contents/ContentManagementAcceptTest-pdf2.pdf"));

		writer.write(new File(
				"/Users/francisbaril/IdeaProjects/constellio-dev/constellio/sdk/sdk-resources/com/constellio/model/services/contents/ContentManagementAcceptTest-pdf3.pdf"));

		fos.close();
	}
}
