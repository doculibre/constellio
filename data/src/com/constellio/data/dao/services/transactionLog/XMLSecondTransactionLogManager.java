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
package com.constellio.data.dao.services.transactionLog;

import static com.constellio.data.threads.BackgroundThreadExceptionHandling.STOP;

import java.io.BufferedReader;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.bigVault.RecordDaoException.OptimisticLocking;
import com.constellio.data.dao.services.bigVault.solr.BigVaultException;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.bigVault.solr.SolrUtils;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.dao.services.contents.ContentDaoException.ContentDaoException_NoSuchContent;
import com.constellio.data.dao.services.contents.ContentDaoRuntimeException;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.dao.services.solr.ConstellioSolrInputDocument;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_CouldNotFlushTransaction;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_CouldNotRegroupAndMoveInVault;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_LogIsInInvalidStateCausedByPreviousException;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_TransactionLogHasAlreadyBeenInitialized;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_TransactionLogIsNotInitialized;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.threads.BackgroundThreadConfiguration;
import com.constellio.data.threads.BackgroundThreadsManager;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.KeyListMap;
import com.constellio.data.utils.LazyIterator;
import com.constellio.data.utils.TimeProvider;

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
	static final String RECOVERED_TLOG_REPLAY = XMLSecondTransactionLogManager.class.getSimpleName() + "_recoveredTLogReplay";

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

	public XMLSecondTransactionLogManager(DataLayerConfiguration configuration, IOServices ioServices, RecordDao recordDao,
			ContentDao contentDao, BackgroundThreadsManager backgroundThreadsManager) {
		this.configuration = configuration;
		this.folder = configuration.getSecondTransactionLogBaseFolder();
		this.ioServices = ioServices;
		this.recordDao = recordDao;
		this.bigVaultServer = recordDao.getBigVaultServer();
		this.contentDao = contentDao;
		this.backgroundThreadsManager = backgroundThreadsManager;
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
			destroyAndRebuildSolrCollection();
		}
	}

	@Override
	public void destroyAndRebuildSolrCollection() {
		File recoveryFolder = ioServices.newTemporaryFolder(RECOVERY_FOLDER);
		try {
			List<File> tLogs = recoverTransactionLogs(recoveryFolder);
			if (!tLogs.isEmpty()) {
				clearSolrCollection();
				replayTransactionLogs(tLogs);
			}

		} finally {
			ioServices.deleteQuietly(recoveryFolder);
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

	private void replayTransactionLogs(List<File> tLogs) {
		for (File tLog : tLogs) {
			replayTransactionLog(tLog);
		}
	}

	private void replayTransactionLog(File tLog) {
		BufferedReader tLogReader = ioServices.newFileReader(tLog, RECOVERED_TLOG_REPLAY);
		try {
			Iterator<BigVaultServerTransaction> operationsIterator = newTransactionsIterator(tLogReader);

			while (operationsIterator.hasNext()) {
				try {
					bigVaultServer.addAll(operationsIterator.next());
					bigVaultServer.softCommit();
				} catch (BigVaultException | SolrServerException | IOException e) {
					throw new RuntimeException(e);
				}

			}

		} finally {
			ioServices.closeQuietly(tLogReader);
		}
	}

	private Iterator<BigVaultServerTransaction> newTransactionsIterator(final BufferedReader tLogReader) {

		final Iterator<List<String>> transactionLinesIterator = newTransactionsLinesIterator(tLogReader);

		return new LazyIterator<BigVaultServerTransaction>() {
			@Override
			protected BigVaultServerTransaction getNextOrNull() {
				if (!transactionLinesIterator.hasNext()) {
					return null;
				}
				BigVaultServerTransaction transaction = new BigVaultServerTransaction(RecordsFlushing.NOW());

				List<String> currentAddUpdateLines = new ArrayList<>();
				for (String line : transactionLinesIterator.next()) {
					if (isFirstLineOfOperation(line) && !currentAddUpdateLines.isEmpty()) {
						addOperationToTransaction(transaction, currentAddUpdateLines);
						currentAddUpdateLines.clear();
					}
					currentAddUpdateLines.add(line);
				}

				if (!currentAddUpdateLines.isEmpty()) {
					addOperationToTransaction(transaction, currentAddUpdateLines);
				}

				return transaction;
			}

		};
	}

	private void addOperationToTransaction(BigVaultServerTransaction transaction, List<String> currentAddUpdateLines) {
		String firstLine = currentAddUpdateLines.get(0);

		if (firstLine.startsWith("addUpdate ")) {

			String[] firstLineParts = firstLine.split(" ");
			String id = firstLineParts[1];
			String version = firstLineParts[2];
			KeyListMap<String, Object> fieldValues = new KeyListMap<>();
			for (int i = 1; i < currentAddUpdateLines.size(); i++) {
				String line = currentAddUpdateLines.get(i);
				int indexOfEqualSign = line.indexOf("=");
				String field = line.substring(0, indexOfEqualSign);
				String value = line.substring(indexOfEqualSign + 1);
				Object convertedValue = convertValueForLogReplay(field, value);

				fieldValues.add(field, convertedValue);
			}
			SolrInputDocument document = buildAddUpdateDocument(id, fieldValues);
			if (version.equals("-1")) {
				transaction.getNewDocuments().add(document);
			} else {
				transaction.getUpdatedDocuments().add(document);
			}

		} else if (firstLine.startsWith("delete ")) {
			int index = firstLine.indexOf(" ");
			List<String> ids = Arrays.asList(firstLine.substring(index).split(" "));
			transaction.getDeletedRecords().addAll(ids);

		} else if (firstLine.startsWith("deletequery ")) {
			int index = firstLine.indexOf(" ");
			String query = firstLine.substring(index);
			transaction.getDeletedQueries().add(query);
		}

	}

	private SolrInputDocument buildAddUpdateDocument(String id, KeyListMap<String, Object> fieldValues) {
		SolrInputDocument inputDocument = new ConstellioSolrInputDocument();
		inputDocument.setField("id", id);
		for (Map.Entry<String, List<Object>> entry : fieldValues.getMapEntries()) {
			String fieldName = entry.getKey();
			String atomicOperation = null;
			int indexOfSpace = fieldName.indexOf(" ");
			if (indexOfSpace != -1) {
				atomicOperation = fieldName.substring(0, indexOfSpace);
				fieldName = fieldName.substring(indexOfSpace + 1);
			}
			Object value = entry.getValue();
			if (!SolrUtils.isMultivalue(fieldName)) {
				value = entry.getValue().get(0);
			}
			if (atomicOperation != null) {
				Map<String, Object> setValue = new HashMap<>();
				setValue.put(atomicOperation, value);
				inputDocument.setField(fieldName, setValue);
			} else {
				inputDocument.setField(fieldName, value);
			}
		}
		return inputDocument;
	}

	private Iterator<List<String>> newTransactionsLinesIterator(final BufferedReader tLogReader) {
		return new LazyIterator<List<String>>() {

			@Override
			protected List<String> getNextOrNull() {
				List<String> currentLines = new ArrayList<>();
				try {

					while (true) {
						String line = tLogReader.readLine();
						if (line != null && (!isFirstLineOfTransaction(line) || currentLines.isEmpty())) {
							currentLines.add(line);
						} else {
							break;
						}
					}

					return currentLines.isEmpty() ? null : currentLines;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}

	private Object convertValueForLogReplay(String field, String value) {

		if (value == null || value.isEmpty()) {
			return SolrUtils.NULL_STRING;
		}

		//		} else if (field.endsWith("da") || field.endsWith("das")) {
		//			if (value.contains("T")) {
		//				LocalDateTime dateTime = new LocalDateTime(value.replace("Z", ""));
		//				return dateTime;
		//			} else {
		//				return new LocalDate(value.replace("Z", ""));
		//			}
		//
		//		} else if (field.endsWith("dt") || field.endsWith("dts")) {
		//			return new LocalDateTime(value.replace("Z", ""));
		//
		//		}

		return value.replace("__LINEBREAK__", "\n");
	}

	private boolean isFirstLineOfOperation(String line) {
		return line.startsWith("addUpdate ") || line.startsWith("delete ") || line.startsWith("deletequery ");
	}

	private boolean isFirstLineOfTransaction(String line) {
		return line.startsWith("--transaction--");
	}

	private List<File> recoverTransactionLogs(File tlogsFolder) {
		List<String> tlogs = contentDao.getFolderContents("/tlogs");
		List<File> tlogsFiles = new ArrayList<>();

		Collections.sort(tlogs);

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

	private Runnable newRegroupAndMoveInVaultRunnable() {
		return new Runnable() {
			@Override
			public void run() {
				regroupAndMoveInVault();
			}
		};
	}

	private void flushOrCancelPreparedTransactions() {
		for (File unflushedFile : findUnflushedFiles()) {
			if (isCommitted(unflushedFile, recordDao)) {
				flush(unflushedFile.getName());
			} else {
				cancel(unflushedFile.getName());
			}
		}
	}

	@Override
	public void close() {

	}

	private String toLogEntry(BigVaultServerTransaction transaction) {

		StringBuilder stringBuilder = new StringBuilder("--transaction--\n");

		for (SolrInputDocument solrDocument : transaction.getNewDocuments()) {
			appendAddUpdateSolrDocument(stringBuilder, solrDocument);
		}
		for (SolrInputDocument solrDocument : transaction.getUpdatedDocuments()) {
			appendAddUpdateSolrDocument(stringBuilder, solrDocument);
		}
		appendDeletedRecords(stringBuilder, transaction.getDeletedRecords());
		for (String deletedByQuery : transaction.getDeletedQueries()) {
			appendDeletedByQuery(stringBuilder, deletedByQuery);
		}

		return stringBuilder.toString();
	}

	private void appendDeletedByQuery(StringBuilder stringBuilder, String deletedByQuery) {
		stringBuilder.append("deletequery " + deletedByQuery + "\n");
	}

	private String correct(Object value) {
		if (value == null) {
			return SolrUtils.NULL_STRING;

		} else if (value instanceof LocalDateTime || value instanceof LocalDate) {
			return value.toString().replace("Z", "");

		} else {
			return value.toString().replace("\n", "__LINEBREAK__");
		}
	}

	private void appendDeletedRecords(StringBuilder stringBuilder, List<String> deletedDocumentIds) {
		if (!deletedDocumentIds.isEmpty()) {
			stringBuilder.append("delete");
			for (String deletedDocumentId : deletedDocumentIds) {
				stringBuilder.append(" ");
				stringBuilder.append(deletedDocumentId);
			}
			stringBuilder.append("\n");
		}
	}

	private void appendAddUpdateSolrDocument(StringBuilder stringBuilder, SolrInputDocument document) {
		String id = (String) document.getFieldValue("id");
		Object version = document.getFieldValue("_version_");
		stringBuilder.append("addUpdate ");
		stringBuilder.append(id);
		stringBuilder.append(" ");
		stringBuilder.append(version == null ? "-1" : version);
		stringBuilder.append("\n");
		for (String name : document.getFieldNames()) {
			if (!name.equals("id") && !name.equals("_version_")) {
				Collection<Object> value = removeEmptyStrings(document.getFieldValues(name));

				if (value.isEmpty()) {
					appendValue(stringBuilder, name, "");
				} else {
					for (Object item : value) {

						String fieldLogName = name;
						if (item instanceof Map) {
							Map<String, Object> mapItemValue = ((Map<String, Object>) item);
							String firstKey = mapItemValue.keySet().iterator().next();
							Object mapValue = mapItemValue.get(firstKey);
							fieldLogName = firstKey + " " + name;

							if (mapValue instanceof Collection) {
								Collection<Object> mapValueList = removeEmptyStrings((Collection) mapValue);
								if (mapValueList.isEmpty()) {
									appendValue(stringBuilder, fieldLogName, "");
								} else {
									for (Object mapValueListItem : mapValueList) {
										appendValue(stringBuilder, fieldLogName, mapValueListItem);
									}
								}
							} else {
								appendValue(stringBuilder, fieldLogName, mapValue);
							}

						} else {
							appendValue(stringBuilder, fieldLogName, item);
						}

					}
				}
			}
		}
	}

	private Collection<Object> removeEmptyStrings(Collection collection) {
		List<Object> values = new ArrayList<>();

		for (Object item : collection) {
			if (!"".equals(item)) {
				values.add(item);
			}
		}

		return values;
	}

	private void appendValue(StringBuilder stringBuilder, String fieldLogName, Object item) {
		//if (!"".equals(item)) {
		stringBuilder.append(fieldLogName);
		stringBuilder.append("=");
		stringBuilder.append(correct(item));
		stringBuilder.append("\n");
		//}
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
			ioServices.replaceFileContent(file, toLogEntry(transaction));
		} catch (IOException e) {
			exceptionOccured = true;
			throw new RuntimeException(e);
		}
	}

	@Override
	public void flush(String transactionId) {
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

	boolean isCommitted(File file, RecordDao recordDao) {
		List<String> lines = ioServices.readFileToLinesWithoutExpectableIOException(file);
		//Skip line 0 which is the --transaction-- header
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
		List<String> transactionFiles = Arrays.asList(getFlushedFolder().list(filenameFilter));
		Collections.sort(transactionFiles);
		return transactionFiles;
	}

	@Override
	public void cancel(String transactionId) {
		File transactionFile = new File(getUnflushedFolder().getAbsolutePath(), transactionId);
		transactionFile.delete();
	}

	@Override
	public synchronized String regroupAndMoveInVault() {
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
			System.out
					.println("Saving temp file '" + bigLogFile.getName() + "' in content dao using id '" + vaultContentId + "'");
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
			System.out.println("Regrouped all logs in temp file (size of " + tempFile.length() + "o)");

		} finally {
			ioServices.closeQuietly(tempFileOutputStream);
		}
		if (totalLengthOfAllTransactionFiles != tempFile.length()) {
			throw new ImpossibleRuntimeException("Copy of transactions failed");
		}
	}

	private void copyTransactionLogInRegroupedLogsFile(File transactionFile, OutputStream tempFileOutputStream)
			throws IOException {

		System.out
				.println("Regrouping log '" + transactionFile.getName() + "' of " + transactionFile.length() + "o in temp file");
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

}
