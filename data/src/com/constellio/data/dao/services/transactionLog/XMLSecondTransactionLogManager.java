package com.constellio.data.dao.services.transactionLog;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.dto.records.TransactionResponseDTO;
import com.constellio.data.dao.services.DataLayerLogger;
import com.constellio.data.dao.services.bigVault.RecordDaoException.OptimisticLocking;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.dao.services.contents.ContentDaoException.ContentDaoException_NoSuchContent;
import com.constellio.data.dao.services.contents.ContentDaoRuntimeException;
import com.constellio.data.dao.services.contents.FileSystemContentDao;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.dao.services.recovery.TransactionLogXmlRecoveryManager;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_CouldNotFlushTransaction;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_CouldNotRegroupAndMoveInVault;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_LogIsInInvalidStateCausedByPreviousException;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_NotAllLogsWereDeletedCorrectlyException;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_TransactionLogHasAlreadyBeenInitialized;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_TransactionLogIsNotInitialized;
import com.constellio.data.dao.services.transactionLog.replay.TransactionLogReplayServices;
import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.data.threads.BackgroundThreadConfiguration;
import com.constellio.data.threads.BackgroundThreadsManager;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.TimeProvider;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.constellio.data.threads.BackgroundThreadExceptionHandling.STOP;

public class XMLSecondTransactionLogManager implements SecondTransactionLogManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(XMLSecondTransactionLogManager.class);

	static final String MERGE_LOGS_ACTION = XMLSecondTransactionLogManager.class.getSimpleName() + "_mergeLogs";
	static final String BIG_LOG_TEMP_FILE = XMLSecondTransactionLogManager.class.getSimpleName() + "_bigLogTempFile";
	static final String READ_LOG = XMLSecondTransactionLogManager.class.getSimpleName() + "_readLog";

	static final String READ_TEMP_BIG_LOG = XMLSecondTransactionLogManager.class.getSimpleName() + "_readBigLog";
	static final String WRITE_TEMP_BIG_LOG = XMLSecondTransactionLogManager.class.getSimpleName() + "_readBigLog";

	static final String RECOVERY_FOLDER = XMLSecondTransactionLogManager.class.getSimpleName() + "_recoveryFolder";
	static final String RECOVERED_TLOG_INPUT = XMLSecondTransactionLogManager.class.getSimpleName() + "_recoveredTLogInput";
	static final String RECOVERED_TLOG_OUTPUT = XMLSecondTransactionLogManager.class.getSimpleName() + "_recoveredTLogOutput";

	private DataLayerConfiguration configuration;

	private File folder;

	private IOServices ioServices;

	private AtomicInteger idSequence;

	private boolean started;

	private boolean exceptionOccured;

	private RecordDao recordDao;

	private BigVaultServer bigVaultServer;

	private ContentDao contentDao;

	private BackgroundThreadsManager backgroundThreadsManager;

	private DataLayerLogger dataLayerLogger;

	private DataLayerSystemExtensions dataLayerSystemExtensions;

	private final TransactionLogXmlRecoveryManager transactionLogXmlRecoveryManager;
	private boolean automaticRegroup = true;

	public XMLSecondTransactionLogManager(DataLayerConfiguration configuration, IOServices ioServices,
										  RecordDao recordDao,
										  ContentDao contentDao, BackgroundThreadsManager backgroundThreadsManager,
										  DataLayerLogger dataLayerLogger,
										  DataLayerSystemExtensions dataLayerSystemExtensions,
										  TransactionLogXmlRecoveryManager transactionLogXmlRecoveryManager) {
		this.configuration = configuration;
		this.folder = configuration.getSecondTransactionLogBaseFolder();
		this.ioServices = ioServices;
		this.recordDao = recordDao;
		this.bigVaultServer = recordDao.getBigVaultServer();
		this.contentDao = contentDao;
		this.backgroundThreadsManager = backgroundThreadsManager;
		this.dataLayerLogger = dataLayerLogger;
		this.dataLayerSystemExtensions = dataLayerSystemExtensions;
		this.transactionLogXmlRecoveryManager = transactionLogXmlRecoveryManager;
	}

	@Override
	public void initialize() {
		if (started) {
			throw new SecondTransactionLogRuntimeException_TransactionLogHasAlreadyBeenInitialized();
		}
		getFlushedFolder().mkdirs();
		getUnflushedFolder().mkdirs();
		idSequence = newIdSequence();
		started = true;

		flushOrCancelPreparedTransactions();

		backgroundThreadsManager.configure(
				BackgroundThreadConfiguration.repeatingAction(MERGE_LOGS_ACTION, newRegroupAndMoveInVaultRunnable())
						.handlingExceptionWith(STOP).executedEvery(configuration.getSecondTransactionLogMergeFrequency()));

		if (bigVaultServer.countDocuments() == 0) {
			regroupAndMove();
			destroyAndRebuildSolrCollection();
		}
	}

	@Override
	public void destroyAndRebuildSolrCollection() {
		this.transactionLogXmlRecoveryManager.disableRollbackModeDuringSolrRestore();
		File recoveryFolder = ioServices.newTemporaryFolder(RECOVERY_FOLDER);
		try {
			List<File> tLogs = recoverTransactionLogs(recoveryFolder);
			if (!tLogs.isEmpty()) {
				clearSolrCollection();
				new TransactionLogReplayServices(newReadWriteServices(), bigVaultServer, dataLayerLogger)
						.replayTransactionLogs(tLogs);
			}

		} finally {
			ioServices.deleteQuietly(recoveryFolder);
		}
	}

	@Override
	public void transactionLOGReindexationStartStrategy() {
		regroupAndMove();
		File tlogsFolder = contentDao.getFileOf("tlogs/");
		Collection<File> tlogs = FileUtils.listFiles(tlogsFolder, new String[]{"tlog"}, false);
		String backupFolderId = "tlogs_bck/" + TimeProvider.getLocalDateTime().toString("yyyy-MM-dd-HH-mm-ss") + ".zip";

		ZipService zipService = new ZipService(ioServices);
		try {
			File zipFile = contentDao.getFileOf(backupFolderId);
			ioServices.touch(zipFile);
			zipService.zip(zipFile, new ArrayList<>(tlogs));
			ioServices.deleteQuietly(tlogsFolder);
		} catch (ZipServiceException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void transactionLOGReindexationCleanupStrategy() {
		List<String> backups;

		while ((backups = contentDao.getFolderContents("tlogs_bck")).size() > configuration
				.getSecondTransactionLogBackupCount()) {
			String deletedFolderId = null;
			LocalDateTime deletedFolderDateTime = null;

			for (String backup : backups) {
				String backupName = backup.split("/")[1];
				backupName = backupName.replace(".zip", "");
				DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss");
				LocalDateTime backupDateTime = LocalDateTime.parse(backupName, dateTimeFormatter);
				if (deletedFolderId == null || deletedFolderDateTime.isAfter(backupDateTime)) {
					deletedFolderId = backup;
					deletedFolderDateTime = backupDateTime;
				}
			}

			ioServices.deleteQuietly(contentDao.getFileOf(deletedFolderId));
		}

	}

	@Override
	public void moveLastBackupAsCurrentLog() {
		String lastTLOGBackup = getLastTLOGBackup();
		if (lastTLOGBackup != null) {
			File tlogsFolder = contentDao.getFileOf("tlogs/");
			ioServices.deleteQuietly(tlogsFolder);

			ZipService zipService = new ZipService(ioServices);
			try {
				File lastBackupFile = contentDao.getFileOf(lastTLOGBackup);
				zipService.unzip(lastBackupFile, tlogsFolder);
				ioServices.deleteQuietly(lastBackupFile);
			} catch (ZipServiceException e) {
				e.printStackTrace();
			}
		}
	}

	public String getLastTLOGBackup() {
		List<String> backups = contentDao.getFolderContents("tlogs_bck");
		String lastFolderId = null;
		LocalDateTime lastFolderDateTime = null;
		for (String backup : backups) {
			File backupFile = contentDao.getFileOf(backup);
			if (!backupFile.isDirectory()) {
				String backupName = backup.split("/")[1];
				backupName = backupName.replace(".zip", "");
				DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss");
				LocalDateTime backupDateTime = LocalDateTime.parse(backupName, dateTimeFormatter);
				if (lastFolderId == null || lastFolderDateTime.isBefore(backupDateTime)) {
					lastFolderId = backup;
					lastFolderDateTime = backupDateTime;
				}
			}
		}
		return lastFolderId;
	}

	private void clearSolrCollection() {

		ModifiableSolrParams deleteAllSolrDocumentsOfEveryConstellioCollectionsQuery = new ModifiableSolrParams();
		deleteAllSolrDocumentsOfEveryConstellioCollectionsQuery.set("q", "*:*");
		try {
			recordDao.execute(new TransactionDTO(RecordsFlushing.NOW())
					.withDeletedByQueries(deleteAllSolrDocumentsOfEveryConstellioCollectionsQuery));

		} catch (OptimisticLocking optimisticLocking) {
			throw new RuntimeException(optimisticLocking);
		}

	}

	TransactionLogReadWriteServices newReadWriteServices() {
		return new TransactionLogReadWriteServices(ioServices, configuration, dataLayerSystemExtensions);
	}

	private Runnable newRegroupAndMoveInVaultRunnable() {
		return new Runnable() {
			@Override
			public void run() {
				if (isAutomaticRegroup()) {
					regroupAndMove();
				}
			}
		};
	}

	private void flushOrCancelPreparedTransactions() {
		for (File unflushedFile : findUnflushedFiles()) {
			if (isCommitted(unflushedFile, recordDao)) {
				flush(unflushedFile.getName(), null);
			} else {
				cancel(unflushedFile.getName());
			}
		}
	}

	@Override
	public void close() {
		started = false;
	}

	public File getFlushedFolder() {
		return new File(folder, "flushed");
	}

	public File getUnflushedFolder() {
		return new File(folder, "unflushed");
	}

	private Collection<File> findUnflushedFiles() {

		IOFileFilter filter = new AbstractFileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory() || !file.getName().contains(".");
			}

		};

		return FileUtils.listFiles(getUnflushedFolder(), filter, filter);
	}

	@Override
	public void prepare(String transactionId, BigVaultServerTransaction transaction) {
		ensureStarted();
		ensureNoExceptionOccured();
		File file = new File(getUnflushedFolder(), transactionId);

		try {
			ioServices.replaceFileContent(file, newReadWriteServices().toLogEntry(transaction));
		} catch (IOException e) {
			exceptionOccured = true;
			throw new RuntimeException(e);
		}
	}

	@Override
	public void flush(String transactionId, TransactionResponseDTO transactionInfo) {
		ensureStarted();
		try {
			doFlush(transactionId);
		} catch (IOException e) {
			exceptionOccured = true;
			throw new SecondTransactionLogRuntimeException_CouldNotFlushTransaction(e);
		}
	}

	void doFlush(String transactionId)
			throws IOException {

		int nextSequentialId = idSequence.incrementAndGet();
		String fileName = "00000000000" + nextSequentialId;
		fileName = fileName.substring(fileName.length() - 12);

		Path source = FileSystems.getDefault().getPath(getUnflushedFolder().getAbsolutePath(), transactionId);
		Path target = FileSystems.getDefault().getPath(getFlushedFolder().getAbsolutePath(), fileName);
		if (!getFlushedFolder().exists()) {
			throw new RuntimeException("Flushed folder does not exist");
		}
		if (!source.toFile().exists()) {
			throw new RuntimeException("Source does not exist");
		}
		Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
	}

	private List<String> readFileToLines(File file) {
		try {
			String content = FileUtils.readFileToString(file);
			return Arrays.asList(content.split("\n"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	boolean isCommitted(File file, RecordDao recordDao) {
		List<String> lines = readFileToLines(file);
		//Skip line 0 which is the --transaction-- header

		if (lines.size() < 2) {
			return false;
		}

		String firstLine = lines.get(1);
		String[] firstLineParts = firstLine.split(" ");

		if ("addUpdate".equals(firstLineParts[0])) {
			return isUpdateCommitted(recordDao, firstLineParts);

		} else if ("delete".equals(firstLineParts[0])) {
			return isDeleteCommitted(recordDao, firstLineParts[1]);

		} else if ("deletequery".equals(firstLineParts[0])) {
			int indexOfSpace = firstLine.indexOf(" ");
			return isDeleteQueryCommitted(firstLine.substring(indexOfSpace + 1), recordDao);

		} else {
			throw new ImpossibleRuntimeException("Unknown operation " + firstLine);
		}
	}

	private boolean isDeleteCommitted(RecordDao recordDao, String firstDeletedDocumentId) {
		return recordDao.getCurrentVersion(firstDeletedDocumentId) == -1;
	}

	private boolean isUpdateCommitted(RecordDao recordDao, String[] firstLineParts) {
		String id = firstLineParts[1];
		long versionBeforeUpdate = Long.valueOf(firstLineParts[2]);
		long currentVersion = recordDao.getCurrentVersion(id);
		if (versionBeforeUpdate == -1) {
			return currentVersion != -1;
		} else {
			return (currentVersion != -1 && currentVersion != versionBeforeUpdate);
		}

	}

	private boolean isDeleteQueryCommitted(String query, RecordDao recordDao) {

		ModifiableSolrParams solrParams = new ModifiableSolrParams();
		solrParams.set("q", query);
		return recordDao.query(solrParams).getNumFound() == 0;
	}

	AtomicInteger newIdSequence() {

		List<String> transactionFiles = getFlushedTransactionsSortedByName();
		if (transactionFiles.isEmpty()) {
			return new AtomicInteger(0);
		} else {
			int lastTransaction = Integer.valueOf(transactionFiles.get(transactionFiles.size() - 1));
			return new AtomicInteger(lastTransaction);

		}
	}

	private List<String> getFlushedTransactionsSortedByName() {
		FilenameFilter filenameFilter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				try {
					Integer.valueOf(name);
					return true;
				} catch (NumberFormatException e) {
					return false;
				}
			}
		};

		String transactionFilesArray[] = getFlushedFolder().list(filenameFilter);

		List<String> transactionFiles =
				transactionFilesArray == null ? new ArrayList<String>() : Arrays.asList(transactionFilesArray);
		Collections.sort(transactionFiles);
		return transactionFiles;
	}

	@Override
	public void cancel(String transactionId) {
		File transactionFile = new File(getUnflushedFolder().getAbsolutePath(), transactionId);
		transactionFile.delete();
	}

	@Override
	public synchronized String regroupAndMove() {
		List<String> transactionLogs = getFlushedTransactionsSortedByName();
		if (transactionLogs.isEmpty()) {
			return null;
		}
		String now = TimeProvider.getLocalDateTime().toString().replace(".", "-").replace(":", "-");
		String vaultContentId = "tlogs/" + now + ".tlog";
		File tempFile = null;
		try {
			tempFile = ioServices.newTemporaryFile(BIG_LOG_TEMP_FILE);
			regroupLogsInTempFile(tempFile, transactionLogs);
			saveRegroupLogsInVault(tempFile, vaultContentId);
			deleteTransactionLogs(transactionLogs);

		} catch (ContentDaoRuntimeException | IOException e) {
			exceptionOccured = true;
			throw new SecondTransactionLogRuntimeException_CouldNotRegroupAndMoveInVault(e);
		} finally {
			ioServices.deleteQuietly(tempFile);
		}
		return vaultContentId;
	}

	private void saveRegroupLogsInVault(File bigLogFile, String vaultContentId) {
		InputStream inputStream = ioServices.newBufferedFileInputStreamWithoutExpectableFileNotFoundException(bigLogFile,
				READ_TEMP_BIG_LOG);
		try {
			contentDao.add(vaultContentId, inputStream);

			if (bigLogFile.length() != contentDao.getContentLength(vaultContentId)) {
				throw new ImpossibleRuntimeException("Copy of transactions failed");
			}

		} finally {
			ioServices.closeQuietly(inputStream);
		}
	}

	private void deleteTransactionLogs(List<String> transactionLogs) {
		for (String transactionLog : transactionLogs) {
			new File(getFlushedFolder(), transactionLog).delete();
		}
	}

	private void regroupLogsInTempFile(File tempFile, List<String> transactionLogs)
			throws IOException {
		OutputStream tempFileOutputStream = null;

		long totalLengthOfAllTransactionFiles = 0;
		try {
			tempFileOutputStream = ioServices.newBufferedFileOutputStreamWithoutExpectableFileNotFoundException(tempFile,
					WRITE_TEMP_BIG_LOG);
			for (String transactionLog : transactionLogs) {
				File transactionFile = new File(getFlushedFolder(), transactionLog);
				copyTransactionLogInRegroupedLogsFile(transactionFile, tempFileOutputStream);
				totalLengthOfAllTransactionFiles += transactionFile.length();
			}
			tempFileOutputStream.flush();

		} finally {
			ioServices.closeQuietly(tempFileOutputStream);
		}
		if (totalLengthOfAllTransactionFiles != tempFile.length()) {
			throw new ImpossibleRuntimeException("Copy of transactions failed");
		}
	}

	private List<File> recoverTransactionLogs(File tlogsFolder) {
		List<String> tlogs = contentDao.getFolderContents("/tlogs");
		Collections.sort(tlogs);

		List<File> tlogsFiles = new ArrayList<>();
		if (contentDao instanceof FileSystemContentDao) {
			File tlogsFolderInVault = ((FileSystemContentDao) contentDao).getFolder("tlogs").getParentFile();
			for (String tlog : tlogs) {
				tlogsFiles.add(new File(tlogsFolderInVault, tlog));
			}
			return tlogsFiles;
		}

		for (String tlog : tlogs) {

			InputStream inputStream;
			try {
				inputStream = contentDao.getContentInputStream(tlog, RECOVERED_TLOG_INPUT);
			} catch (ContentDaoException_NoSuchContent contentDaoException_noSuchContent) {
				throw new RuntimeException(contentDaoException_noSuchContent);
			}
			File tLogFile = new File(tlogsFolder, tlog);
			ioServices.touch(tLogFile);
			OutputStream outputStream = ioServices.newBufferedFileOutputStreamWithoutExpectableFileNotFoundException(
					tLogFile, RECOVERED_TLOG_OUTPUT);
			tlogsFiles.add(tLogFile);

			try {
				ioServices.copyAndClose(inputStream, outputStream);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		return tlogsFiles;
	}

	private void copyTransactionLogInRegroupedLogsFile(File transactionFile, OutputStream tempFileOutputStream)
			throws IOException {

		InputStream inputStream = null;

		try {
			inputStream = ioServices.newBufferedFileInputStreamWithoutExpectableFileNotFoundException(transactionFile, READ_LOG);
			ioServices.copy(inputStream, tempFileOutputStream);

		} finally {
			ioServices.closeQuietly(inputStream);
		}
	}

	public File getFolder() {
		return folder;
	}

	private void ensureStarted() {
		if (!started) {
			throw new SecondTransactionLogRuntimeException_TransactionLogIsNotInitialized();
		}
	}

	private void ensureNoExceptionOccured() {
		if (exceptionOccured) {
			throw new SecondTransactionLogRuntimeException_LogIsInInvalidStateCausedByPreviousException();
		}
	}

	synchronized public boolean isAutomaticRegroup() {
		return automaticRegroup;
	}

	public void setAutomaticRegroupAndMoveEnabled(boolean automaticMode) {
		this.automaticRegroup = automaticMode;
	}

	@Override
	public void deleteUnregroupedLog()
			throws SecondTransactionLogRuntimeException_NotAllLogsWereDeletedCorrectlyException {
		List<String> transactionFiles = getFlushedTransactionsSortedByName();

		for (String transactionLog : transactionFiles) {
			File transactionFile = new File(getFlushedFolder(), transactionLog);
			FileUtils.deleteQuietly(transactionFile);
		}
		transactionFiles = getFlushedTransactionsSortedByName();
		if (!transactionFiles.isEmpty()) {
			throw new SecondTransactionLogRuntimeException_NotAllLogsWereDeletedCorrectlyException(transactionFiles);
		}
	}

	@Override
	public void setSequence(String sequenceId, long value, TransactionResponseDTO transactionInfo) {
		ensureStarted();
		ensureNoExceptionOccured();
		String transactionId = UUIDV1Generator.newRandomId();
		File file = new File(getUnflushedFolder(), transactionId);

		try {
			ioServices.replaceFileContent(file, newReadWriteServices().toSetSequenceLogEntry(sequenceId, value));
			doFlush(transactionId);
		} catch (IOException e) {
			exceptionOccured = true;
			throw new RuntimeException(e);
		}

	}

	@Override
	public void nextSequence(String sequenceId, TransactionResponseDTO transactionInfo) {
		ensureStarted();
		ensureNoExceptionOccured();
		String transactionId = UUIDV1Generator.newRandomId();
		File file = new File(getUnflushedFolder(), transactionId);

		try {
			ioServices.replaceFileContent(file, newReadWriteServices().toNextSequenceLogEntry(sequenceId));
			doFlush(transactionId);
		} catch (IOException e) {
			exceptionOccured = true;
			throw new RuntimeException(e);
		}
	}
}
