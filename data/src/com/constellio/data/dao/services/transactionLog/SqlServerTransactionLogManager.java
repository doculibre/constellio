package com.constellio.data.dao.services.transactionLog;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.dto.records.TransactionResponseDTO;
import com.constellio.data.dao.dto.records.TransactionSqlDTO;
import com.constellio.data.dao.services.DataLayerLogger;
import com.constellio.data.dao.services.bigVault.RecordDaoException.OptimisticLocking;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.dao.services.recovery.TransactionLogRecoveryManager;
import com.constellio.data.dao.services.sql.SqlRecordDao;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_CouldNotFlushTransaction;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_LogIsInInvalidStateCausedByPreviousException;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_NotAllLogsWereDeletedCorrectlyException;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_TransactionLogHasAlreadyBeenInitialized;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_TransactionLogIsNotInitialized;
import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.threads.BackgroundThreadConfiguration;
import com.constellio.data.threads.BackgroundThreadsManager;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.constellio.data.threads.BackgroundThreadExceptionHandling.STOP;

public class SqlServerTransactionLogManager implements SecondTransactionLogManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(XMLSecondTransactionLogManager.class);

	static final String MERGE_LOGS_ACTION = XMLSecondTransactionLogManager.class.getSimpleName() + "_mergeLogs";
	static final String BIG_LOG_TEMP_FILE = XMLSecondTransactionLogManager.class.getSimpleName() + "_bigLogTempFile";
	static final String READ_LOG = XMLSecondTransactionLogManager.class.getSimpleName() + "_readLog";

	static final String READ_TEMP_BIG_LOG = XMLSecondTransactionLogManager.class.getSimpleName() + "_readBigLog";
	static final String WRITE_TEMP_BIG_LOG = XMLSecondTransactionLogManager.class.getSimpleName() + "_readBigLog";

	static final String RECOVERY_FOLDER = XMLSecondTransactionLogManager.class.getSimpleName() + "_recoveryFolder";
	static final String RECOVERED_TLOG_INPUT = XMLSecondTransactionLogManager.class.getSimpleName() + "_recoveredTLogInput";
	static final String RECOVERED_TLOG_OUTPUT = XMLSecondTransactionLogManager.class.getSimpleName() + "_recoveredTLogOutput";
	private static final Map<String,BigVaultServerTransaction > transactionContentMap = new HashMap<>();

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

	private SqlRecordDao sqlRecordDao;

	private final TransactionLogRecoveryManager transactionLogRecoveryManager;

	private boolean automaticRegroup = true;

	public SqlServerTransactionLogManager(DataLayerConfiguration configuration, IOServices ioServices,
										  RecordDao recordDao,SqlRecordDao sqlRecordDao,
										  ContentDao contentDao, BackgroundThreadsManager backgroundThreadsManager,
										  DataLayerLogger dataLayerLogger,
										  DataLayerSystemExtensions dataLayerSystemExtensions,
										  TransactionLogRecoveryManager transactionLogRecoveryManager) {
		this.configuration = configuration;
		this.folder = configuration.getSecondTransactionLogBaseFolder();
		this.ioServices = ioServices;
		this.recordDao = recordDao;
		this.bigVaultServer = recordDao.getBigVaultServer();
		this.contentDao = contentDao;
		this.sqlRecordDao = sqlRecordDao;
		this.backgroundThreadsManager = backgroundThreadsManager;
		this.dataLayerLogger = dataLayerLogger;
		this.dataLayerSystemExtensions = dataLayerSystemExtensions;
		this.transactionLogRecoveryManager = transactionLogRecoveryManager;
	}


	@Override
	public void initialize() {

		if (started) {
			throw new SecondTransactionLogRuntimeException_TransactionLogHasAlreadyBeenInitialized();
		}

		getUnflushedFolder().mkdirs();
		//idSequence = newIdSequence();
		started = true;

		flushOrCancelPreparedTransactions();

		backgroundThreadsManager.configure(
				BackgroundThreadConfiguration.repeatingAction(MERGE_LOGS_ACTION, newRegroupAndMoveInVaultRunnable())
						.handlingExceptionWith(STOP).executedEvery(configuration.getSecondTransactionLogMergeFrequency()));

//		if (bigVaultServer.countDocuments() == 0) {
//			regroupAndMoveInVault();
//			destroyAndRebuildSolrCollection();
//		}
	}

	@Override
	public void prepare(String transactionId, BigVaultServerTransaction transaction) {
		ensureStarted();
		ensureNoExceptionOccured();
		File file = new File(getUnflushedFolder(), transactionId);
		try {
			ioServices.replaceFileContent(file, newReadWriteServices().toLogEntry(transaction));

			newReadWriteServices().toLogEntry(transaction);

			transactionContentMap.put(transactionId, transaction);
		} catch (IOException e) {
			exceptionOccured = true;
			throw new RuntimeException(e);
		}
	}

	@Override
	public void flush(String transactionId, TransactionResponseDTO transactionInfo) {
		ensureStarted();
		try {

			doFlush(transactionId, transactionInfo);
		} catch (IOException e) {
			exceptionOccured = true;
			throw new SecondTransactionLogRuntimeException_CouldNotFlushTransaction(e);
		}
	}

	 void doFlush(String transactionId, TransactionResponseDTO transactionInfo)
			throws IOException {

		Path source = FileSystems.getDefault().getPath(getUnflushedFolder().getAbsolutePath(), transactionId);

		String content = toJson(transactionContentMap.remove(transactionId));

		AtomicInteger atomicTries = new AtomicInteger(0);
		do {
			try {
				Integer logVersion = getLogVersion();
				sqlRecordDao.insert(createTransactionSqlDto(transactionId, transactionInfo, content,logVersion));
				break;
			} catch (SQLException e) {
				atomicTries.getAndIncrement();
				if (atomicTries.get() > 2) {
					exceptionOccured = true;
					throw new RuntimeException(e);
				}
			}
		}
		while(true);
		if (!source.toFile().exists()) {
			throw new RuntimeException("Source does not exist");
		}

		Files.delete(source);
	}

	@Override
	public void cancel(String transactionId) {
		File transactionFile = new File(getUnflushedFolder().getAbsolutePath(), transactionId);
		transactionFile.delete();
		transactionContentMap.remove(transactionId);
	}

	@Override
	public void setSequence(String sequenceId, long value, TransactionResponseDTO transactionInfo) {
		String data = getTransactionLogReadWriteServices().toSetSequenceLogEntry(sequenceId, value);
		//sendTransaction(transactionInfo, data);
	}

	@Override
	public void nextSequence(String sequenceId, TransactionResponseDTO transactionInfo) {
		String data = getTransactionLogReadWriteServices().toNextSequenceLogEntry(sequenceId);
		//sendTransaction(transactionInfo, data);
	}

	@Override
	public String regroupAndMoveInVault() {
		return null;
	}

	@Override
	public void destroyAndRebuildSolrCollection() {


	}

	@Override
	public void moveTLOGToBackup() {
		//table and schema backup is set up in SQL Server
	}

	@Override
	public void deleteLastTLOGBackup() {

		//table and schema backup is set up in SQL Server
	}

	@Override
	public void setAutomaticRegroupAndMoveInVaultEnabled(boolean enabled) {

		//table and schema backup is set up in SQL Server
	}

	@Override
	public void deleteUnregroupedLog()
			throws SecondTransactionLogRuntimeException_NotAllLogsWereDeletedCorrectlyException {

	}

	@Override
	public void moveLastBackupAsCurrentLog() {

		//table and schema backup is set up in SQL Server
	}

	@Override
	public void close() {

		started = false;
	}

	public File getUnflushedFolder() {
		return new File(folder, "unflushed");
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
	private Collection<File> findUnflushedFiles() {

		IOFileFilter filter = new AbstractFileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory() || !file.getName().contains(".");
			}

		};

		return FileUtils.listFiles(getUnflushedFolder(), filter, filter);
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

	private List<String> readFileToLines(File file) {
		try {
			String content = FileUtils.readFileToString(file, Charset.defaultCharset());
			return Arrays.asList(content.split("\n"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Runnable newRegroupAndMoveInVaultRunnable() {
		return new Runnable() {
			@Override
			public void run() {
				if (isAutomaticRegroup()) {
					regroupAndMoveInVault();
				}
			}
		};
	}

	synchronized public boolean isAutomaticRegroup() {
		return automaticRegroup;
	}

	private List<File> recoverTransactionLogs(File tlogsFolder) {
//		List<String> tlogs = contentDao.getFolderContents("/tlogs");
//		Collections.sort(tlogs);
//
//		List<File> tlogsFiles = new ArrayList<>();
//		if (contentDao instanceof FileSystemContentDao) {
//			File tlogsFolderInVault = ((FileSystemContentDao) contentDao).getFolder("tlogs").getParentFile();
//			for (String tlog : tlogs) {
//				tlogsFiles.add(new File(tlogsFolderInVault, tlog));
//			}
//			return tlogsFiles;
//		}
//
//		for (String tlog : tlogs) {
//
//			InputStream inputStream;
//			try {
//				inputStream = contentDao.getContentInputStream(tlog, RECOVERED_TLOG_INPUT);
//			} catch (ContentDaoException_NoSuchContent contentDaoException_noSuchContent) {
//				throw new RuntimeException(contentDaoException_noSuchContent);
//			}
//			File tLogFile = new File(tlogsFolder, tlog);
//			ioServices.touch(tLogFile);
//			OutputStream outputStream = ioServices.newBufferedFileOutputStreamWithoutExpectableFileNotFoundException(
//					tLogFile, RECOVERED_TLOG_OUTPUT);
//			tlogsFiles.add(tLogFile);
//
//			try {
//				ioServices.copyAndClose(inputStream, outputStream);
//			} catch (IOException e) {
//				throw new RuntimeException(e);
//			}
//		}
//
//		return tlogsFiles;
		return null;
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

	private TransactionLogReadWriteServices getTransactionLogReadWriteServices() {
		return new TransactionLogReadWriteServices(null, configuration, dataLayerSystemExtensions);
	}

	private TransactionSqlDTO createTransactionSqlDto(String transactionId,
													  TransactionResponseDTO transactionInfo, String content, int version){
		transactionInfo = transactionInfo == null ?
						  new TransactionResponseDTO( 0, new HashMap<>()): transactionInfo;
		return new TransactionSqlDTO(transactionId, new java.sql.Date((new Date()).getTime()),
				version, transactionInfo.getNewDocumentVersions().toString(), content);
	}

	private String toJson(BigVaultServerTransaction bigVaultServerTransaction) throws JsonProcessingException {

		if(bigVaultServerTransaction == null){
			return "{}";
		}
		ObjectWriter ow =new ObjectMapper().writer().withDefaultPrettyPrinter();

		return ow.writeValueAsString(bigVaultServerTransaction);
	}

	private int getLogVersion() throws SQLException {
		return sqlRecordDao.getCurrentVersion();
	}
}
