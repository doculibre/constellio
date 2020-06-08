package com.constellio.data.dao.services.replicationFactor;

import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.dao.services.replicationFactor.dto.ReplicationFactorTransaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.input.NullInputStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static com.constellio.data.dao.services.replicationFactor.utils.ReplicationFactorTransactionParser.toTransaction;
import static java.nio.file.StandardOpenOption.CREATE;

@Slf4j
class ReplicationFactorLogService {

	private ContentDao contentDao;

	private final File localLogFile;

	public final static String ROOT_FOLDER = "replicationFactor";
	private final static String MERGED_LOG_FILE = ROOT_FOLDER.concat("/degradedStateTransactions.tlog");

	private final static String LOCAL_LOG_FILE_PREFIX = "degradedStateTransactions-";

	ReplicationFactorLogService(ContentDao contentDao) {
		this.contentDao = contentDao;

		String uuid = UUID.randomUUID().toString();
		String filename = ROOT_FOLDER + "/" + LOCAL_LOG_FILE_PREFIX + uuid + ".tlog";

		contentDao.add(filename, new NullInputStream(0));

		localLogFile = contentDao.getFileOf(filename);
	}

	File mergeAllLogFiles() {
		if (!contentDao.isDocumentExisting(MERGED_LOG_FILE)) {
			contentDao.add(MERGED_LOG_FILE, new NullInputStream(0));
		}

		File mergedLogFile = contentDao.getFileOf(MERGED_LOG_FILE);
		try (OutputStream out = new BufferedOutputStream(new FileOutputStream(mergedLogFile, true))) {
			for (String filename : contentDao.getFolderContents("replicationFactor")) {
				if (filename.contains(LOCAL_LOG_FILE_PREFIX)) {
					try {
						File file = contentDao.getFileOf(filename);
						Files.copy(file.toPath(), out);
						contentDao.delete(Collections.singletonList(filename));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
			return mergedLogFile;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	void writeLineToLocalLog(String line) throws IOException {
		synchronized (localLogFile) {
			if (!localLogFile.exists()) {
				localLogFile.createNewFile();
			}
			Files.write(localLogFile.toPath(), line.concat(System.lineSeparator()).getBytes(), CREATE, StandardOpenOption.APPEND);
		}
	}

	void removeLinesFromMergedLog(int lineCount, Set<String> replayedTransactionIds) {
		if (!contentDao.isDocumentExisting(MERGED_LOG_FILE)) {
			throw new RuntimeException("Merged log file not found");
		}

		File mergedLogFile = contentDao.getFileOf(MERGED_LOG_FILE);

		LineIterator lineIterator = null;
		try {
			String content = "";
			lineIterator = new LineIterator(new FileReader(mergedLogFile));

			// skip lines to remove
			for (int i = 1; i <= lineCount; i++) {
				if (lineIterator.hasNext()) {
					String currentLine = lineIterator.nextLine();
					ReplicationFactorTransaction currentTransaction = toTransaction(currentLine);
					if (!replayedTransactionIds.contains(currentTransaction.getId())) {
						// keep non replayed transaction
						content = content.concat(currentLine).concat(System.lineSeparator());
					}
				}
			}

			// read remaining lines
			while (lineIterator.hasNext()) {
				content = content.concat(lineIterator.nextLine()).concat(System.lineSeparator());
			}

			// overwrite file
			Files.write(mergedLogFile.toPath(), content.getBytes());
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			LineIterator.closeQuietly(lineIterator);
		}
	}

}
