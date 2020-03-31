package com.constellio.data.dao.services.transactionLog;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.dto.records.TransactionResponseDTO;
import com.constellio.data.dao.dto.sql.RecordTransactionSqlDTO;
import com.constellio.data.dao.dto.sql.TransactionSqlDTO;
import com.constellio.data.dao.services.DataLayerLogger;
import com.constellio.data.dao.services.bigVault.RecordDaoException.OptimisticLocking;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.dao.services.leaderElection.ObservableLeaderElectionManager;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.dao.services.recovery.TransactionLogRecovery;
import com.constellio.data.dao.services.sql.SqlRecordDaoFactory;
import com.constellio.data.dao.services.sql.SqlRecordDaoType;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_CouldNotFlushTransaction;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_LogIsInInvalidStateCausedByPreviousException;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_NotAllLogsWereDeletedCorrectlyException;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_TransactionLogHasAlreadyBeenInitialized;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_TransactionLogIsNotInitialized;
import com.constellio.data.dao.services.transactionLog.replay.SqlTransactionLogReplayServices;
import com.constellio.data.dao.services.transactionLog.sql.TransactionDocumentLogContent;
import com.constellio.data.dao.services.transactionLog.sql.TransactionLogContent;
import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.threads.BackgroundThreadConfiguration;
import com.constellio.data.threads.BackgroundThreadsManager;
import com.constellio.data.utils.AsyncTaskRegrouper;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.TimeProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.constellio.data.threads.BackgroundThreadExceptionHandling.CONTINUE;
import static com.constellio.data.utils.LangUtils.executeInParallelUntilSupplierReturnsNull;
import static com.constellio.data.utils.LangUtils.newBatchSupplier;
import static org.joda.time.Duration.standardSeconds;

public class SqlServerTransactionLogManager implements SecondTransactionLogManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(XMLSecondTransactionLogManager.class);

	static final String MERGE_LOGS_ACTION = XMLSecondTransactionLogManager.class.getSimpleName() + "_trToRecords";
	static final String BIG_LOG_TEMP_FILE = XMLSecondTransactionLogManager.class.getSimpleName() + "_bigLogTempFile";
	static final String READ_LOG = XMLSecondTransactionLogManager.class.getSimpleName() + "_readLog";

	static final String READ_TEMP_BIG_LOG = XMLSecondTransactionLogManager.class.getSimpleName() + "_readBigLog";
	static final String WRITE_TEMP_BIG_LOG = XMLSecondTransactionLogManager.class.getSimpleName() + "_readBigLog";

	static final String RECOVERY_FOLDER = XMLSecondTransactionLogManager.class.getSimpleName() + "_recoveryFolder";
	static final String RECOVERED_TLOG_INPUT = XMLSecondTransactionLogManager.class.getSimpleName() + "_recoveredTLogInput";
	static final String RECOVERED_TLOG_OUTPUT = XMLSecondTransactionLogManager.class.getSimpleName() + "_recoveredTLogOutput";
	private static final Map<String, String> transactionContentMap = new HashMap<>();

	private DataLayerConfiguration configuration;

	private File folder;

	private IOServices ioServices;

	private boolean started;

	private boolean exceptionOccured;

	private RecordDao recordDao;

	private BigVaultServer bigVaultServer;

	private ContentDao contentDao;

	private BackgroundThreadsManager backgroundThreadsManager;

	private DataLayerLogger dataLayerLogger;

	private DataLayerSystemExtensions dataLayerSystemExtensions;

	private SqlRecordDaoFactory sqlRecordDaoFactory;

	private final TransactionLogRecovery transactionLogSqlRecoveryManager;

	private boolean automaticRegroup = true;

	private ObservableLeaderElectionManager leaderElectionManager;

	private Integer currentLogVersion = null;

	private AsyncTaskRegrouper<TransactionToSend> asyncTaskRegrouper;

	private AtomicLong loggedOperationsCount = new AtomicLong();

	public SqlServerTransactionLogManager(DataLayerConfiguration configuration, IOServices ioServices,
										  RecordDao recordDao, SqlRecordDaoFactory sqlRecordDaoFactory,
										  ContentDao contentDao, BackgroundThreadsManager backgroundThreadsManager,
										  DataLayerLogger dataLayerLogger,
										  DataLayerSystemExtensions dataLayerSystemExtensions,
										  TransactionLogRecovery transactionLogSqlRecoveryManager,
										  ObservableLeaderElectionManager isCurrentNodeLeader) {
		this.configuration = configuration;
		this.folder = configuration.getSecondTransactionLogBaseFolder();
		this.ioServices = ioServices;
		this.recordDao = recordDao;
		this.bigVaultServer = recordDao.getBigVaultServer();
		this.contentDao = contentDao;
		this.sqlRecordDaoFactory = sqlRecordDaoFactory;
		this.backgroundThreadsManager = backgroundThreadsManager;
		this.dataLayerLogger = dataLayerLogger;
		this.dataLayerSystemExtensions = dataLayerSystemExtensions;
		this.transactionLogSqlRecoveryManager = transactionLogSqlRecoveryManager;
		this.leaderElectionManager = isCurrentNodeLeader;
	}


	@Override
	public void initialize() {

		if (started) {
			throw new SecondTransactionLogRuntimeException_TransactionLogHasAlreadyBeenInitialized();
		}

		if (configuration.isAsyncSQLSecondTransactionLogInsertion()) {
			this.asyncTaskRegrouper = new AsyncTaskRegrouper<>(standardSeconds(10), this::bulkLogInsert);
			this.asyncTaskRegrouper.setQueueCapacity(1000);
			this.asyncTaskRegrouper.start();
		}

		getUnflushedFolder().mkdirs();
		//idSequence = newIdSequence();
		started = true;

		tryThreeTimes(() -> {
			getLogVersion();
			return true;
		});

		flushOrCancelPreparedTransactions();

		backgroundThreadsManager.configure(
				BackgroundThreadConfiguration.repeatingAction(MERGE_LOGS_ACTION, newRegroupAndMoveInVaultRunnable())
						.handlingExceptionWith(CONTINUE).executedEvery(configuration.getSecondTransactionLogMergeFrequency()));

		//		if (bigVaultServer.countDocuments() == 0) {
		//			regroupAndMove();
		//			destroyAndRebuildSolrCollection();
		//		}
	}


	@Override
	public void prepare(String transactionId, BigVaultServerTransaction transaction) {
		ensureStarted();
		ensureNoExceptionOccured();
		File file = new File(getUnflushedFolder(), transactionId);

		String contentUnflushed = newReadWriteServices().toLogEntry(transaction);
		writeBuffered(file, contentUnflushed);
		String content = newReadWriteSqlServices().toLogEntry(transaction);

		synchronized (transactionContentMap) {
			transactionContentMap.put(transactionId, content);
		}

	}

	private void writeBuffered(File file, String content) {
		try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
			bufferedWriter.write(content);
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
		} catch (Exception e) {
			exceptionOccured = true;
			throw new SecondTransactionLogRuntimeException_CouldNotFlushTransaction(e);
		}
	}

	void doFlush(String transactionId, TransactionResponseDTO transactionInfo)
			throws IOException {

		Path source = FileSystems.getDefault().getPath(getUnflushedFolder().getAbsolutePath(), transactionId);

		String content = null;
		synchronized (transactionContentMap) {
			content = transactionContentMap.remove(transactionId);
		}
		if (asyncTaskRegrouper == null) {
			final String finalContent = content;
			tryThreeTimes(() -> {

				sqlRecordDaoFactory.getRecordDao(SqlRecordDaoType.TRANSACTIONS).insert(
						createTransactionSqlDto(transactionId, transactionInfo, finalContent, getLogVersion())
				);
				return true;
			});

			if (!source.toFile().exists()) {
				throw new RuntimeException("Source does not exist");
			}
			loggedOperationsCount.incrementAndGet();
			Files.delete(source);
		} else {
			asyncTaskRegrouper.addAsync(new TransactionToSend(transactionId, content, transactionInfo), () -> {
				try {
					Files.delete(source);
				} catch (IOException e) {
					e.printStackTrace();
					exceptionOccured = true;
				}
			});
		}
	}

	@AllArgsConstructor
	private static class TransactionToSend {
		String transactionId;
		String content;
		TransactionResponseDTO response;

	}

	private void bulkLogInsert(List<TransactionToSend> transactionToSends) {
		try {
			int logVersion = getLogVersion();
			List<TransactionSqlDTO> dtos = transactionToSends.stream()
					.map(tx -> createTransactionSqlDto(tx.transactionId, tx.response, tx.content, logVersion))
					.collect(Collectors.toList());
			tryThreeTimes(() -> {
				sqlRecordDaoFactory.getRecordDao(SqlRecordDaoType.TRANSACTIONS).insertBulk(dtos);
				return true;
			});
			loggedOperationsCount.addAndGet(dtos.size());
		} catch (SQLException e) {
			e.printStackTrace();
			exceptionOccured = true;
		}

	}

	@Override
	public void cancel(String transactionId) {
		File transactionFile = new File(getUnflushedFolder().getAbsolutePath(), transactionId);
		transactionFile.delete();

		synchronized (transactionContentMap) {
			transactionContentMap.remove(transactionId);
		}
	}

	@Override
	public void setSequence(String sequenceId, long value, TransactionResponseDTO transactionInfo) {

	}

	@Override
	public void nextSequence(String sequenceId, TransactionResponseDTO transactionInfo) {

	}

	private boolean isCurrentNodeLeader() {

		try {
			if (this.leaderElectionManager != null) {
				return this.leaderElectionManager.isCurrentNodeLeader();
			}
		} catch (NullPointerException nullEx) {
			//leaderElectionManager might be null and not instantiated during Constellio reboot.
			return false;
		} finally {
			return true;
		}
	}

	@Override
	public synchronized String regroupAndMove() {

		int converted = 0;
		if (configuration.isReplaySQLSecondTransactionLogDuringOfficeHours() || !isOfficeHours()) {

			//build record on json

			try {

				LOGGER.info("Retrieving next batch...");
				//get transactions
				List<TransactionSqlDTO> transactionsToConvert = tryThreeTimesReturnList(() ->
						sqlRecordDaoFactory.getRecordDao(SqlRecordDaoType.TRANSACTIONS).getAll(1000, true));

				converted = transactionsToConvert.size();
				if (transactionsToConvert.size() == 0) {
					//end
					return "" + converted;
				}

				final List<RecordTransactionSqlDTO> recordsToinsert = extractRecordsFromTransaction(transactionsToConvert, getLogVersion(), false);
				final List<RecordTransactionSqlDTO> recordsToUpdate = extractRecordsFromTransaction(transactionsToConvert, getLogVersion(), true);
				final List<String> recordsToDelete = extractRemoveRecordsFromTransaction(transactionsToConvert);
				final List<RecordTransactionSqlDTO> victims = new ArrayList<>();
				LOGGER.info("Replay of " + transactionsToConvert.size() + " transactions...");

				//save new records
				if (!recordsToinsert.isEmpty()) {
					if (recordsToinsert.size() < 100) {
						LOGGER.info("Inserting " + recordsToinsert.size() + " records");
						tryThreeTimes(() -> {
							sqlRecordDaoFactory.getRecordDao(SqlRecordDaoType.RECORDS).insertBulk((List) recordsToinsert);
							return true;
						});
					} else {
						LOGGER.info("Inserting " + recordsToinsert.size() + " records in parallel");
						executeInParallelUntilSupplierReturnsNull(4, newBatchSupplier(recordsToinsert, 100), (batch) -> {
							tryThreeTimes(() -> {
								sqlRecordDaoFactory.getRecordDao(SqlRecordDaoType.RECORDS).insertBulk((List) batch);

								return true;
							});
						});
					}
				}

				//save update records
				if (!recordsToUpdate.isEmpty()) {
					if (hasSameRecordTwice(recordsToUpdate) || recordsToUpdate.size() < 100) {
						LOGGER.info("Updating " + recordsToUpdate.size() + " records");
						tryThreeTimes(() -> {
							try {
								sqlRecordDaoFactory.getRecordDao(SqlRecordDaoType.RECORDS).updateBulk(recordsToUpdate);
							} catch (SQLException sqlEx) {
								LOGGER.warn("Error while updating records", sqlEx);
								if (sqlEx instanceof BatchUpdateException) {
									victims.addAll(recordsToUpdate);
								}
							}
							return true;
						});
					} else {
						LOGGER.info("Updating " + recordsToUpdate.size() + " records in parallel");
						executeInParallelUntilSupplierReturnsNull(4, newBatchSupplier(recordsToUpdate, 100), (batch) -> {
							tryThreeTimes(() -> {
								try {
									sqlRecordDaoFactory.getRecordDao(SqlRecordDaoType.RECORDS).updateBulk(batch);
								} catch (SQLException sqlEx) {
									LOGGER.warn("Error while updating records", sqlEx);
									if (sqlEx instanceof BatchUpdateException) {
										victims.addAll(batch);
									}
								}
								return true;
							});
						});
					}

				}

				//remove deleted records
				if (!recordsToDelete.isEmpty()) {
					LOGGER.info("Deleting " + recordsToDelete.size() + " records in parallel");
					tryThreeTimes(() -> {
						sqlRecordDaoFactory.getRecordDao(SqlRecordDaoType.RECORDS).deleteAll(recordsToDelete);
						return true;
					});
				}


				//Remove transactions
				for (RecordTransactionSqlDTO victim : victims) {
					transactionsToConvert.removeIf(tr -> tr.getId().equals(victim.getId()));
				}

				final String[] transactionsToRemove = transactionsToConvert.stream().map(x -> x.getId()).toArray(String[]::new);

				tryThreeTimes(() -> {
					sqlRecordDaoFactory.getRecordDao(SqlRecordDaoType.TRANSACTIONS).deleteAll(transactionsToRemove);
					return true;
				});
				LOGGER.info("Replay of " + transactionsToConvert.size() + " transactions done!");
			} catch (Exception ex) {
				//exceptionOccured = true;
				throw new RuntimeException(ex);
			}
		}
		return "" + converted;
	}

	private boolean hasSameRecordTwice(List<RecordTransactionSqlDTO> recordsToUpdate) {
		Set<String> ids = new HashSet<>();

		for (RecordTransactionSqlDTO dto : recordsToUpdate) {
			if (!ids.add(dto.getId())) {
				return true;
			}
		}
		return false;
	}

	private boolean isOfficeHours() {
		LocalDateTime time = TimeProvider.getLocalDateTime();
		return time.getHourOfDay() < 18 && time.getHourOfDay() > 7
			   && time.getDayOfWeek() != DateTimeConstants.SATURDAY
			   && time.getDayOfWeek() != DateTimeConstants.SUNDAY;
	}

	private List<String> extractRemoveRecordsFromTransaction(List<TransactionSqlDTO> transactions) {
		ObjectMapper objectMapper = new ObjectMapper();
		List<String> documentsToDelete = transactions.stream().flatMap(x -> {
			try {
				if (x == null || x.getContent() == null) {
					return new ArrayList<String>().stream();
				}
				TransactionLogContent content = objectMapper.readValue(x.getContent(), TransactionLogContent.class);
				return content.getDeletedRecords().stream();
			} catch (IOException e) {
				e.printStackTrace();
				return new ArrayList<String>().stream();
			}
		}).collect(Collectors.toList());
		return documentsToDelete;
	}

	private List<RecordTransactionSqlDTO> extractRecordsFromTransaction(List<TransactionSqlDTO> transactions,
																		int logVersion, boolean isUpdate)
			throws IOException, SQLException {
		List<RecordTransactionSqlDTO> records = new ArrayList<>();

		for (TransactionSqlDTO transactionSql : transactions) {
			String jsonContent = transactionSql.getContent();
			if (jsonContent != null && !"".equals(jsonContent)) {
				TransactionLogContent transaction = new JsonTransactionLogParser().parse(jsonContent);

				if (isUpdate) {
					records.addAll(mapUpdateBigVaultTransactionToRecordTransaction(transaction, logVersion));
				} else {
					records.addAll(mapNewBigVaultTransactionToRecordTransaction(transaction, logVersion));
				}
			}
		}
		return records;
	}

	private List<RecordTransactionSqlDTO> mapNewBigVaultTransactionToRecordTransaction(
			TransactionLogContent bigVaultServerTransaction, int logVersion)
			throws SQLException {

		String solrVersion = this.recordDao.getBigVaultServer().getVersion();

		//new documents
		Map<String, RecordTransactionSqlDTO> records = bigVaultServerTransaction.getNewDocuments().stream().map(x -> {
			try {
				return new RecordTransactionSqlDTO(x.getId(), logVersion, solrVersion, toJsonArray(x));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			return new RecordTransactionSqlDTO();
		}).collect(Collectors.toMap(e -> e.getRecordId(), e -> e));

		return records.values().stream().collect(Collectors.toList());
	}

	private List<RecordTransactionSqlDTO> mapUpdateBigVaultTransactionToRecordTransaction(
			TransactionLogContent bigVaultServerTransaction, int logVersion)
			throws SQLException {

		String solrVersion = this.recordDao.getBigVaultServer().getVersion();

		//update documents
		Map<String, RecordTransactionSqlDTO> recordsUpdate = bigVaultServerTransaction.getUpdatedDocuments().stream().map(x -> {
			try {
				return new RecordTransactionSqlDTO(x.getId(), logVersion, solrVersion, toJson(x));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			return new RecordTransactionSqlDTO();
		}).collect(Collectors.toMap(e -> e.getRecordId(), e -> e));

		return recordsUpdate.values().stream().collect(Collectors.toList());
	}

	@Override
	public void destroyAndRebuildSolrCollection() {

		this.transactionLogSqlRecoveryManager.disableRollbackModeDuringSolrRestore();
		try {
			long recordsCount = this.sqlRecordDaoFactory.getRecordDao(SqlRecordDaoType.RECORDS).getTableCount();
			for (long i = 0; i < recordsCount; i = i + 1000) {
				List<RecordTransactionSqlDTO> tRecords = this.sqlRecordDaoFactory.getRecordDao(SqlRecordDaoType.RECORDS).getAll(1000, true);
				if (!tRecords.isEmpty()) {
					clearSolrCollection();
					new SqlTransactionLogReplayServices(newReadWriteSqlServices(), bigVaultServer, dataLayerLogger)
							.replayTransactionLogs(tRecords);

					this.sqlRecordDaoFactory.getRecordDao(SqlRecordDaoType.RECORDS).deleteAll(tRecords.stream().map(x -> x.getId()).collect(Collectors.toList()));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		}

	}


	@Override
	public void transactionLOGReindexationStartStrategy() {
		//table and schema backup is set up in SQL Server

		if (isCurrentNodeLeader()) {
			tryThreeTimes(() -> {
				this.currentLogVersion =
						sqlRecordDaoFactory.getRecordDao(SqlRecordDaoType.TRANSACTIONS).increaseVersion();
				return true;
			});
		}

	}

	@Override
	public void transactionLOGReindexationCleanupStrategy() {

		final String solrVersion = this.recordDao.getBigVaultServer().getVersion();
		if (isCurrentNodeLeader()) {
			tryThreeTimes(() -> {
				int version = sqlRecordDaoFactory.getRecordDao(SqlRecordDaoType.TRANSACTIONS).getCurrentVersion();

				sqlRecordDaoFactory.getRecordDao(SqlRecordDaoType.RECORDS).deleteAllByLogVersion(version);
				sqlRecordDaoFactory.getRecordDao(SqlRecordDaoType.TRANSACTIONS).deleteAllByLogVersion(version);
				sqlRecordDaoFactory.getRecordDao(SqlRecordDaoType.RECORDS).insert(
						new RecordTransactionSqlDTO("reindexation_v" + version + "_solrv_" + solrVersion,
								version, solrVersion, "Reindexation end")
				);
				return true;
			});
		}
	}

	@Override
	public void setAutomaticRegroupAndMoveEnabled(boolean automaticMode) {
		this.automaticRegroup = automaticMode;
	}

	@Override
	public void deleteUnregroupedLog()
			throws SecondTransactionLogRuntimeException_NotAllLogsWereDeletedCorrectlyException {

		List<TransactionSqlDTO> list = tryThreeTimesReturnList(() ->
				sqlRecordDaoFactory.getRecordDao(SqlRecordDaoType.TRANSACTIONS).getAll());

		for (TransactionSqlDTO transactionLog : list) {
			tryThreeTimes(() -> {
				sqlRecordDaoFactory.getRecordDao(SqlRecordDaoType.TRANSACTIONS).delete(transactionLog.getId());
				return true;
			});
		}

		list = tryThreeTimesReturnList(() ->
				sqlRecordDaoFactory.getRecordDao(SqlRecordDaoType.TRANSACTIONS).getAll());


		if (!list.isEmpty()) {
			throw new SecondTransactionLogRuntimeException_NotAllLogsWereDeletedCorrectlyException(
					list.stream().map(x -> x.getId()).collect(Collectors.toList()));
		}
	}

	@Override
	public void moveLastBackupAsCurrentLog() {

		//table and schema backup is set up in SQL Server
	}

	@Override
	public boolean isAlive() {
		return !this.exceptionOccured && started;
	}

	@Override
	public long getLoggedTransactionCount() {
		return loggedOperationsCount.get();
	}

	@Override
	public void close() {
		if (asyncTaskRegrouper != null) {
			asyncTaskRegrouper.close();
		}
		started = false;
	}

	public void deleteAllTransactionsAndRecords() throws SQLException {
		sqlRecordDaoFactory.getRecordDao(SqlRecordDaoType.TRANSACTIONS).deleteAll();

		sqlRecordDaoFactory.getRecordDao(SqlRecordDaoType.RECORDS).deleteAll();

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

				boolean finished = false;
				while (!finished && isAutomaticRegroup() && isCurrentNodeLeader()) {
					String regrouped = regroupAndMove();
					finished = !"1000".equals(regrouped);
				}
			}
		};
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

	synchronized public boolean isAutomaticRegroup() {
		return automaticRegroup;
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

	TransactionLogSqlReadWriteServices newReadWriteSqlServices() {
		return new TransactionLogSqlReadWriteServices(configuration, dataLayerSystemExtensions);
	}

	TransactionLogReadWriteServices newReadWriteServices() {
		return new TransactionLogReadWriteServices(ioServices, configuration, dataLayerSystemExtensions);
	}

	private TransactionLogSqlReadWriteServices getTransactionLogReadWriteServices() {
		return new TransactionLogSqlReadWriteServices(configuration, dataLayerSystemExtensions);
	}

	private TransactionSqlDTO createTransactionSqlDto(String transactionId,
													  TransactionResponseDTO transactionInfo, String content,
													  int version) {
		transactionInfo = transactionInfo == null ?
						  new TransactionResponseDTO(0, new HashMap<>()) : transactionInfo;
		return new TransactionSqlDTO(transactionId, new java.sql.Date((new Date()).getTime()),
				version, transactionInfo.getNewDocumentVersions().toString(), content);
	}

	private String toJson(BigVaultServerTransaction bigVaultServerTransaction) throws JsonProcessingException {

		if (bigVaultServerTransaction == null) {
			return "{}";
		}
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

		return ow.writeValueAsString(bigVaultServerTransaction);
	}

	private String toJson(TransactionDocumentLogContent inputDocument) throws JsonProcessingException {

		if (inputDocument == null) {
			return "{}";
		}
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

		return ow.writeValueAsString(inputDocument);
	}

	private String toJsonArray(TransactionDocumentLogContent inputDocument) throws JsonProcessingException {

		if (inputDocument == null) {
			return "[]";
		}
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

		return "[" + ow.writeValueAsString(inputDocument) + "]";
	}

	private int getLogVersion() throws SQLException {
		if (this.currentLogVersion == null) {
			this.currentLogVersion = sqlRecordDaoFactory
					.getRecordDao(SqlRecordDaoType.TRANSACTIONS).getCurrentVersion();
		}
		return this.currentLogVersion;
	}

	public long getTableTransactionCount() throws SQLException {
		return sqlRecordDaoFactory
				.getRecordDao(SqlRecordDaoType.TRANSACTIONS).getTableCount();
	}

	//Command pattern

	void tryThreeTimes(Callable func) {
		AtomicInteger atomicTries = new AtomicInteger(0);
		do {
			try {
				func.call();
				break;
			} catch (SQLException e) {
				atomicTries.getAndIncrement();
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) {
					exceptionOccured = true;
					throw new RuntimeException(e);
				}
				if (atomicTries.get() > 2) {
					exceptionOccured = true;
					throw new RuntimeException(e);
				}
			} catch (Exception e) {
				throw new RuntimeException();
			}
		}
		while (true);
	}

	List<TransactionSqlDTO> tryThreeTimesReturnList(Callable<List<TransactionSqlDTO>> func) {

		List<TransactionSqlDTO> transactionsToConvert = null;
		AtomicInteger atomicTries = new AtomicInteger(0);
		do {
			try {
				transactionsToConvert = func.call();
				break;
			} catch (SQLException e) {
				atomicTries.getAndIncrement();
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) {
					exceptionOccured = true;
					throw new RuntimeException(e);
				}
				if (atomicTries.get() > 2) {
					exceptionOccured = true;
					throw new RuntimeException(e);
				}
			} catch (Exception e) {
				throw new RuntimeException();
			}
		}
		while (true);

		return transactionsToConvert;
	}
}
