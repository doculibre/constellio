package com.constellio.data.dao.services.transactionLog.reader1;

import java.io.BufferedReader;
import java.io.IOException;

import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.LazyIterator;

public class ReaderLinesIteratorV1 extends LazyIterator<String> {

	boolean wasCarriageReturn = false;
	char[] buffer = new char[1];
	char carriageReturnChar = '\r';
	char lineFeedChar = '\n';
	BufferedReader tLogBufferedReader;
	IOServices ioServices;

	public ReaderLinesIteratorV1(IOServices ioServices, BufferedReader tLogBufferedReader) {
		this.ioServices = ioServices;
		this.tLogBufferedReader = tLogBufferedReader;
	}

	@Override
	protected String getNextOrNull() {

		String nextLine;
		try {
			nextLine = getNextLine();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		if (nextLine == null) {
			ioServices.closeQuietly(tLogBufferedReader);
		}

		return nextLine;
	}

	private String getNextLine()
			throws IOException {
		StringBuilder stringBuilder = new StringBuilder();

		wasCarriageReturn = false;
		int response;
		while (1 == (response = tLogBufferedReader.read(buffer))) {
			if (buffer[0] == carriageReturnChar) {
				wasCarriageReturn = true;

			} else if (buffer[0] == lineFeedChar && wasCarriageReturn) {
				stringBuilder.append("__LINEBREAK__");

			} else if (buffer[0] == lineFeedChar) {
				break;

			} else {
				wasCarriageReturn = false;
				stringBuilder.append(buffer[0]);
			}
		}
		if (response != 1 && stringBuilder.length() == 0) {
			return null;
		}
		return stringBuilder.toString();
	}
}
