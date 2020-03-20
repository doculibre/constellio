package com.constellio.data.dao.services.replicationFactor;

import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.dao.services.replicationFactor.ReplicationFactorTransactionReadService.ReplicationFactorTransactionReadTask;

import java.io.FileNotFoundException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class ReplicationFactorTestUtils {

	static final String FOLDER = "replicationFactor";
	static final String PREFIX = "degradedStateTransactions";

	static void deleteAllLogFiles() throws Exception {
		Files.walkFileTree(Paths.get("."), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path filePath, BasicFileAttributes attributes) {
				if (filePath.toFile().getName().startsWith(PREFIX)) {
					try {
						Files.delete(filePath);
					} catch (Exception ignored) {
					}
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}

	static void clearLogFile(Path logFilePath) throws Exception {
		Files.write(logFilePath, "".getBytes());
	}

	static Path getLocalLogFilePath(ReplicationFactorLogService service) throws Exception {
		final List<Path> paths = new ArrayList<>();

		service.writeLineToLocalLog("");
		Files.walkFileTree(Paths.get("."), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path filePath, BasicFileAttributes attributes) {
				if (filePath.toFile().getName().startsWith(PREFIX)) {
					paths.add(filePath);
					return FileVisitResult.TERMINATE;
				}
				return FileVisitResult.CONTINUE;
			}
		});

		return !paths.isEmpty() ? paths.get(0) : null;
	}

	static Path getLocalLogFilePath(ReplicationFactorLogService service, ContentDao contentDao) throws Exception {
		Path localLogFilePath = null;

		service.writeLineToLocalLog("_");
		for (String filename : contentDao.getFolderContents("replicationFactor")) {
			if (filename.contains(FOLDER.concat("/").concat(PREFIX).concat("-"))) {
				localLogFilePath = contentDao.getFileOf(filename).toPath();
			}
		}

		if (localLogFilePath == null) {
			throw new FileNotFoundException();
		}

		clearLogFile(localLogFilePath);
		return localLogFilePath;
	}

	static class MockReplicationFactorTransactionReadTask extends ReplicationFactorTransactionReadTask {

		MockReplicationFactorTransactionReadTask(ReplicationFactorTransactionReadService enclosing) {
			enclosing.super();
		}

		@Override
		boolean isSolrCloudOnline() {
			return true;
		}
	}

}
