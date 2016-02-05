package com.constellio.data.dao.services.transactionLog.reader1;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.constellio.data.utils.LazyIterator;

public class ReaderTransactionLinesIteratorV1 extends LazyIterator<List<String>> {

	Iterator<String> linesIterator;

	public ReaderTransactionLinesIteratorV1(Iterator<String> linesIterator) {
		this.linesIterator = linesIterator;
	}

	@Override
	protected List<String> getNextOrNull() {
		List<String> currentLines = new ArrayList<>();

		while (linesIterator.hasNext()) {
			String line = linesIterator.next();
			if (line != null && line.startsWith("__LINEBREAK__")) {
				int lastLineIndex = currentLines.size() - 1;
				String lastLine = currentLines.get(lastLineIndex);
				currentLines.set(lastLineIndex, lastLine + line);

			} else if (line != null && (!isFirstLineOfTransaction(line) || currentLines.isEmpty())) {
				currentLines.add(line);

			} else {
				break;
			}
		}

		return currentLines.isEmpty() ? null : currentLines;
	}

	private boolean isFirstLineOfTransaction(String line) {
		return line.startsWith("--transaction--");
	}

}
