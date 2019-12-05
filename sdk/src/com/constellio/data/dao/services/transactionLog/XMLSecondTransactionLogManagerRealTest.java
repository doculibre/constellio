package com.constellio.data.dao.services.transactionLog;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.dto.records.QueryResponseDTO;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.services.DataLayerLogger;
import com.constellio.data.dao.services.bigVault.RecordDaoException;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.bigVault.solr.SolrUtils;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.dao.services.contents.ContentDaoRuntimeException;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.dao.services.solr.ConstellioSolrInputDocument;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_CouldNotFlushTransaction;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_CouldNotRegroupAndMoveInVault;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_LogIsInInvalidStateCausedByPreviousException;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_TransactionLogHasAlreadyBeenInitialized;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_TransactionLogIsNotInitialized;
import com.constellio.data.dao.services.transactionLog.reader1.ReaderLinesIteratorV1;
import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.threads.BackgroundThreadsManager;
import com.constellio.sdk.tests.ConstellioTest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.assertj.core.api.Condition;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class XMLSecondTransactionLogManagerRealTest extends ConstellioTest {

	@Mock DataLayerConfiguration dataLayerConfiguration;

	@Mock BigVaultServer bigVaultServer;

	@Mock RecordDao recordDao;

	@Mock DataLayerSystemExtensions systemExtensions;

	LocalDateTime shishOclockLocalDateTime = new LocalDateTime().plusHours(1);
	LocalDateTime tockOClockLocalDateTime = shishOclockLocalDateTime.plusMinutes(1);
	String shishOclock = SolrUtils.convertLocalDateTimeToSolrDate(shishOclockLocalDateTime);
	String tockOClock = SolrUtils.convertLocalDateTimeToSolrDate(tockOClockLocalDateTime);

	SolrInputDocument record1, record2, record5, record3, record6, record4 = new SolrInputDocument();
	String deletedRecord6 = "deletedRecord6";
	String deletedRecord7 = "deletedRecord7";
	@Mock QueryResponseDTO queryResponseDTO;
	@Mock BackgroundThreadsManager backgroundThreadsManager;
	ContentDao contentDao;

	File baseFolder, unflushed, flushed;

	IOServices ioServices;

	XMLSecondTransactionLogManager transactionLog;

	String deleteByQueryParams;
	RecordsFlushing recordsFlushing = RecordsFlushing.LATER();

	DataLayerLogger dataLayerLogger = new DataLayerLogger();
	String firstTransactionId = "firstTransaction";
	List<SolrInputDocument> firstTransactionNewRecords = new ArrayList<>();
	List<SolrInputDocument> firstTransactionModifiedRecords = new ArrayList<>();
	List<String> firstTransactionDeletedRecords = new ArrayList<>();
	List<String> firstTransactionDeletedByQueries = new ArrayList<>();

	BigVaultServerTransaction firstTransaction = new BigVaultServerTransaction(firstTransactionId, recordsFlushing,
			firstTransactionNewRecords,
			firstTransactionModifiedRecords, firstTransactionDeletedRecords, firstTransactionDeletedByQueries);

	String secondTransactionId = "secondTransaction";
	List<SolrInputDocument> secondTransactionNewRecords = new ArrayList<>();
	List<SolrInputDocument> secondTransactionModifiedRecords = new ArrayList<>();
	List<String> secondTransactionDeletedRecords = new ArrayList<>();
	List<String> secondTransactionDeletedByQueries = new ArrayList<>();
	BigVaultServerTransaction secondTransaction = new BigVaultServerTransaction(secondTransactionId, recordsFlushing,
			secondTransactionNewRecords,
			secondTransactionModifiedRecords, secondTransactionDeletedRecords, secondTransactionDeletedByQueries);

	String expectedLogOfFirstTransaction, expectedLogOfSecondTransaction;

	File firstTransactionTempFile, secondTransactionTempFile, flushedTransaction1, flushedTransaction2, flushedTransaction42;

	@Before
	public void setUp()
			throws Exception {

		when(systemExtensions.isDocumentFieldLoggedInTransactionLog(anyString(), anyString(), anyString(), eq(true)))
				.thenReturn(true);
		when(systemExtensions.isDocumentFieldLoggedInTransactionLog(anyString(), anyString(), anyString(), eq(false)))
				.thenReturn(false);

		givenDisabledAfterTestValidations();
		withSpiedServices(ContentDao.class);

		baseFolder = newTempFolder();
		when(dataLayerConfiguration.getSecondTransactionLogBaseFolder()).thenReturn(baseFolder);
		flushed = new File(baseFolder, "flushed");
		unflushed = new File(baseFolder, "unflushed");
		ioServices = getIOLayerFactory().newIOServices();
		contentDao = getDataLayerFactory().getContentsDao();
		when(recordDao.getBigVaultServer()).thenReturn(bigVaultServer);
		when(bigVaultServer.countDocuments()).thenReturn(42L);
		transactionLog = spy(new XMLSecondTransactionLogManager(dataLayerConfiguration, ioServices, recordDao, contentDao,
				backgroundThreadsManager, dataLayerLogger, systemExtensions,
				getDataLayerFactory().getTransactionLogRecoveryManager()));
		transactionLog.initialize();

		record1 = newSolrInputDocument("record1", -1L);
		record1.setField("text_s", "aValue");
		record1.setField("date_dt", shishOclock);
		record1.setField("content_txt_fr", "ze french parsed content");
		record1.setField("content_txt_en", "ze english parsed content");

		record2 = newSolrInputDocument("record2", -1L);
		record2.setField("text_s", "anotherValue");
		record2.setField("otherfield_ss", asList(true, false));

		record3 = newSolrInputDocument("record3", 34L);
		record3.setField("text_s", "line1\nline2");

		record4 = newSolrInputDocument("record4", 45L);
		record4.setField("text_s", "value3");
		record4.setField("otherfield_ss", asList(false, true));

		record5 = newSolrInputDocument("record5", 56L);
		record5.setField("text_s", "aValue");
		record5.setField("date_dt", tockOClock);

		record6 = newSolrInputDocument("record6ZZ", 56L);
		record6.setField("text_s", "aValue");
		record6.setField("date_dt", tockOClock);

		firstTransactionNewRecords.add(record1);
		firstTransactionNewRecords.add(record2);
		firstTransactionModifiedRecords.add(record3);
		firstTransactionModifiedRecords.add(record4);
		firstTransactionModifiedRecords.add(record6);
		firstTransactionDeletedByQueries.add(deleteByQueryParams = SolrUtils.toDeleteQueries(new ModifiableSolrParams()
				.set("q", "zeQuery").add("fq", "firstFilter").add("fq", "secondFilter")));
		firstTransactionDeletedByQueries.add(SolrUtils.toDeleteQueries(new ModifiableSolrParams()
				.set("q", "anotherQuery").add("fq", "firstFilter").add("fq", "secondFilter").add("fq", "thirdFilter")));

		secondTransactionNewRecords.add(record5);
		secondTransactionModifiedRecords.add(record3);
		secondTransactionDeletedRecords.add(deletedRecord6);
		secondTransactionDeletedRecords.add(deletedRecord7);

		StringBuilder expectedLogOfFirstTransactionBuilder = new StringBuilder();
		expectedLogOfFirstTransactionBuilder.append("--transaction--\n");
		expectedLogOfFirstTransactionBuilder.append("addUpdate record1 -1\n");
		expectedLogOfFirstTransactionBuilder.append("text_s=aValue\n");
		expectedLogOfFirstTransactionBuilder.append("date_dt=" + shishOclock + "\n");
		expectedLogOfFirstTransactionBuilder.append("addUpdate record2 -1\n");
		expectedLogOfFirstTransactionBuilder.append("text_s=anotherValue\n");
		expectedLogOfFirstTransactionBuilder.append("otherfield_ss=true\n");
		expectedLogOfFirstTransactionBuilder.append("otherfield_ss=false\n");
		expectedLogOfFirstTransactionBuilder.append("addUpdate record3 34\n");
		expectedLogOfFirstTransactionBuilder.append("text_s=line1__LINEBREAK__line2\n");
		expectedLogOfFirstTransactionBuilder.append("addUpdate record4 45\n");
		expectedLogOfFirstTransactionBuilder.append("text_s=value3\n");
		expectedLogOfFirstTransactionBuilder.append("otherfield_ss=false\n");
		expectedLogOfFirstTransactionBuilder.append("otherfield_ss=true\n");
		expectedLogOfFirstTransactionBuilder.append("deletequery ((zeQuery) AND (firstFilter) AND (secondFilter))\n");
		expectedLogOfFirstTransactionBuilder
				.append("deletequery ((anotherQuery) AND (firstFilter) AND (secondFilter) AND (thirdFilter))\n");
		expectedLogOfFirstTransaction = expectedLogOfFirstTransactionBuilder.toString();

		StringBuilder expectedLogOfSecondTransactionBuilder = new StringBuilder();
		expectedLogOfSecondTransactionBuilder.append("--transaction--\n");
		expectedLogOfSecondTransactionBuilder.append("addUpdate record5 56\n");
		expectedLogOfSecondTransactionBuilder.append("text_s=aValue\n");
		expectedLogOfSecondTransactionBuilder.append("date_dt=" + tockOClock + "\n");
		expectedLogOfSecondTransactionBuilder.append("addUpdate record3 34\n");
		expectedLogOfSecondTransactionBuilder.append("text_s=line1__LINEBREAK__line2\n");
		expectedLogOfSecondTransactionBuilder.append("delete deletedRecord6 deletedRecord7\n");
		expectedLogOfSecondTransaction = expectedLogOfSecondTransactionBuilder.toString();

		firstTransactionTempFile = new File(unflushed, firstTransactionId);
		secondTransactionTempFile = new File(unflushed, secondTransactionId);
		flushedTransaction1 = new File(flushed, "000000000001");
		flushedTransaction2 = new File(flushed, "000000000002");
		flushedTransaction42 = new File(flushed, "000000000042");

		contentDao = getDataLayerFactory().getContentsDao();
	}

	private SolrInputDocument newSolrInputDocument(String id, long version) {
		SolrInputDocument solrInputDocument = new ConstellioSolrInputDocument();
		solrInputDocument.setField("id", id);
		solrInputDocument.setField("_version_", version);
		return solrInputDocument;
	}

	@Test
	public void whenPrepareLogThenCreateFileWithTransactionModifications()
			throws Exception {

		transactionLog.prepare(firstTransactionId, firstTransaction);

		File firstTransactionTempFile = new File(unflushed, firstTransactionId);

		assertThat(firstTransactionTempFile).has(content(expectedLogOfFirstTransaction));
		assertThat(flushedTransaction1).doesNotExist();
		assertThat(flushedTransaction2).doesNotExist();

	}

	@Test
	public void givenTwoTransactionArePreparedThenAreSavedInSeparateFiles()
			throws Exception {

		transactionLog.prepare(firstTransactionId, firstTransaction);
		transactionLog.prepare(secondTransactionId, secondTransaction);

		assertThat(FileUtils.readFileToString(firstTransactionTempFile)).isEqualTo(expectedLogOfFirstTransaction);
		assertThat(FileUtils.readFileToString(secondTransactionTempFile)).isEqualTo(expectedLogOfSecondTransaction);
		assertThat(flushedTransaction1).doesNotExist();
		assertThat(flushedTransaction2).doesNotExist();
	}

	@Test
	public void givenTwoPreparedTransactionsWhenATransactionIsFlushedThenAppendToLogAndKeepOtherUnflushed()
			throws Exception {

		transactionLog.prepare(firstTransactionId, firstTransaction);
		transactionLog.prepare(secondTransactionId, secondTransaction);

		transactionLog.flush(secondTransactionId, null);

		assertThat(firstTransactionTempFile).has(content(expectedLogOfFirstTransaction));
		assertThat(secondTransactionTempFile).doesNotExist();
		assertThat(flushedTransaction1).has(content(expectedLogOfSecondTransaction));
		assertThat(flushedTransaction2).doesNotExist();
	}

	@Test
	public void givenTwoPreparedTransactionsWhenTheSecondIsFlushedBeforeTheSecondThenWritenInLogFileFirst()
			throws Exception {

		transactionLog.prepare(firstTransactionId, firstTransaction);
		transactionLog.prepare(secondTransactionId, secondTransaction);

		transactionLog.flush(secondTransactionId, null);
		transactionLog.flush(firstTransactionId, null);

		assertThat(firstTransactionTempFile).doesNotExist();
		assertThat(secondTransactionTempFile).doesNotExist();
		assertThat(flushedTransaction1).has(content(expectedLogOfSecondTransaction));
		assertThat(flushedTransaction2).has(content(expectedLogOfFirstTransaction));
	}

	@Test
	public void givenTwoPreparedTransactionsWhenTheFirstIsCancelledAndTheSecondFlushedThenGetSequentialId1()
			throws Exception {

		transactionLog.prepare(firstTransactionId, firstTransaction);
		transactionLog.prepare(secondTransactionId, secondTransaction);

		transactionLog.cancel(secondTransactionId);
		transactionLog.flush(firstTransactionId, null);

		assertThat(firstTransactionTempFile).doesNotExist();
		assertThat(secondTransactionTempFile).doesNotExist();
		assertThat(flushedTransaction1).has(content(expectedLogOfFirstTransaction));
		assertThat(flushedTransaction2).doesNotExist();
	}

	@Test
	public void givenZZRecordsAreWrittenWhenWriteTransactionWithZZRecordsThenWrote()
			throws Exception {

		when(dataLayerConfiguration.isWriteZZRecords()).thenReturn(true);

		transactionLog.prepare(firstTransactionId, firstTransaction);
		transactionLog.flush(firstTransactionId, null);

		assertThat(FileUtils.readFileToString(flushedTransaction1)).contains("record6ZZ");
	}

	@Test
	public void givenAnExceptionOccurWhenFlushingThenThrowExceptionAndBlockFutureTransactions()
			throws Exception {

		transactionLog.prepare(firstTransactionId, firstTransaction);
		File firstTransactionTempFile = new File(unflushed, firstTransactionId);
		doThrow(IOException.class).when(transactionLog).doFlush(firstTransactionId);

		try {
			transactionLog.flush(firstTransactionId, null);
			fail("SecondTransactionLogHandlerRuntimeException_CouldNotFlushTransaction expected");
		} catch (SecondTransactionLogRuntimeException_CouldNotFlushTransaction e) {
			//OK
		}

		assertThat(firstTransactionTempFile).exists();

		try {
			transactionLog.prepare(secondTransactionId, secondTransaction);
			fail("SecondTransactionLogRuntimeException_LogIsInInvalidStateCausedByPreviousException expected");
		} catch (SecondTransactionLogRuntimeException_LogIsInInvalidStateCausedByPreviousException e) {
			//OK
		}

	}

	@Test
	public void givenTwoUnflushedTransactionThenOnlyFlushOnesThatWereCommitted()
			throws Exception {

		transactionLog.prepare(firstTransactionId, firstTransaction);
		transactionLog.prepare(secondTransactionId, secondTransaction);

		transactionLog = spy(new XMLSecondTransactionLogManager(dataLayerConfiguration, ioServices, recordDao, contentDao,
				backgroundThreadsManager, dataLayerLogger, systemExtensions,
				getDataLayerFactory().getTransactionLogRecoveryManager()));

		doReturn(true).when(transactionLog).isCommitted(firstTransactionTempFile, recordDao);
		doReturn(false).when(transactionLog).isCommitted(secondTransactionTempFile, recordDao);
		doReturn(new AtomicInteger(41)).when(transactionLog).newIdSequence();

		transactionLog.initialize();

		assertThat(firstTransactionTempFile).doesNotExist();
		assertThat(secondTransactionTempFile).doesNotExist();
		assertThat(flushedTransaction42).has(content(expectedLogOfFirstTransaction));
		assertThat(flushedTransaction2).doesNotExist();
	}

	@Test(expected = SecondTransactionLogRuntimeException_TransactionLogIsNotInitialized.class)
	public void givenPreparedIsCalledBeforeInitializingTheTransactionLogThenException() {

		transactionLog = spy(new XMLSecondTransactionLogManager(dataLayerConfiguration, ioServices, recordDao, contentDao,
				backgroundThreadsManager, dataLayerLogger, systemExtensions,
				getDataLayerFactory().getTransactionLogRecoveryManager()));
		transactionLog.prepare(firstTransactionId, firstTransaction);

	}

	@Test(expected = SecondTransactionLogRuntimeException_TransactionLogHasAlreadyBeenInitialized.class)
	public void givenInitializedTransactionLogWhenStartASecondTimeThenException() {

		transactionLog.initialize();

	}

	@Test
	public void givenNoFlushedFolderWhenCreateIdSequenceThenSetToZero()
			throws Exception {

		assertThat(transactionLog.newIdSequence().get()).isEqualTo(0);

	}

	@Test
	public void givenFlushedFolderWhenCreateIdSequenceThenSetToZero()
			throws Exception {

		FileUtils.write(new File(transactionLog.getFlushedFolder(), ".000110000666"), "content");
		FileUtils.write(new File(transactionLog.getFlushedFolder(), "000000000666"), "content");
		FileUtils.write(new File(transactionLog.getFlushedFolder(), "000000000042"), "content");
		FileUtils.write(new File(transactionLog.getFlushedFolder(), "thumb.db"), "content");

		assertThat(transactionLog.newIdSequence().get()).isEqualTo(666);

	}

	@Test
	public void givenUnflushedTransactionFileIsEmptyThenDeleted()
			throws Exception {

		when(recordDao.getCurrentVersion("zeRecord")).thenReturn(-1L);

		File file = new File(transactionLog.getUnflushedFolder(), UUIDV1Generator.newRandomId());
		FileUtils.touch(file);

		assertThat(transactionLog.isCommitted(file, recordDao)).isFalse();

	}

	@Test
	public void givenFirstChangeOfTransactionWasRecordAddAndRecordDoesNotExistThenNotCommitted()
			throws Exception {

		when(recordDao.getCurrentVersion("zeRecord")).thenReturn(-1L);
		SolrInputDocument zeRecord = newSolrInputDocument("zeRecord", -1L);
		BigVaultServerTransaction transaction = new BigVaultServerTransaction(RecordsFlushing.NOW())
				.setNewDocuments(asList(zeRecord));

		transactionLog.prepare(firstTransactionId, transaction);

		assertThat(transactionLog.isCommitted(firstTransactionTempFile, recordDao)).isFalse();

	}

	@Test
	public void givenFirstChangeOfTransactionWasRecordAddAndRecordDoesExistThenCommitted()
			throws Exception {

		when(recordDao.getCurrentVersion("zeRecord")).thenReturn(42L);
		SolrInputDocument zeRecord = newSolrInputDocument("zeRecord", -1L);
		BigVaultServerTransaction transaction = new BigVaultServerTransaction(RecordsFlushing.NOW())
				.setNewDocuments(asList(zeRecord));

		transactionLog.prepare(firstTransactionId, transaction);

		assertThat(transactionLog.isCommitted(firstTransactionTempFile, recordDao)).isTrue();

	}

	@Test
	public void givenFirstChangeOfTransactionWasRecordUpdateAndRecordDoesNotExistThenNotCommitted()
			throws Exception {

		when(recordDao.getCurrentVersion("zeRecord")).thenReturn(-1L);
		SolrInputDocument zeRecordUpdate = newSolrInputDocument("zeRecord", 123L);
		BigVaultServerTransaction transaction = new BigVaultServerTransaction(RecordsFlushing.NOW())
				.setUpdatedDocuments(asList(zeRecordUpdate));

		transactionLog.prepare(firstTransactionId, transaction);

		assertThat(transactionLog.isCommitted(firstTransactionTempFile, recordDao)).isFalse();

	}

	@Test
	public void givenFirstChangeOfTransactionWasRecordUpdateAndRecordExistWithSameVersionThenNotCommitted()
			throws Exception {

		SolrInputDocument zeRecordUpdate = newSolrInputDocument("zeRecord", 123L);
		when(recordDao.getCurrentVersion("zeRecord")).thenReturn(123L);
		BigVaultServerTransaction transaction = new BigVaultServerTransaction(RecordsFlushing.NOW())
				.setUpdatedDocuments(asList(zeRecordUpdate));

		transactionLog.prepare(firstTransactionId, transaction);

		assertThat(transactionLog.isCommitted(firstTransactionTempFile, recordDao)).isFalse();

	}

	@Test
	public void givenFirstChangeOfTransactionWasRecordUpdateAndRecordDoesExistWithDifferentVersionThenCommitted()
			throws Exception {

		SolrInputDocument zeRecordUpdate = newSolrInputDocument("zeRecord", 123L);
		when(recordDao.getCurrentVersion("zeRecord")).thenReturn(42L);
		BigVaultServerTransaction transaction = new BigVaultServerTransaction(RecordsFlushing.NOW())
				.setUpdatedDocuments(asList(zeRecordUpdate));

		transactionLog.prepare(firstTransactionId, transaction);

		assertThat(transactionLog.isCommitted(firstTransactionTempFile, recordDao)).isTrue();

	}

	@Test
	public void givenFirstChangeOfTransactionWasDeleteByIdAndFirstRecordExistThenNotCommitted()
			throws Exception {

		when(recordDao.getCurrentVersion("zeRecord")).thenReturn(42L);
		BigVaultServerTransaction transaction = new BigVaultServerTransaction(RecordsFlushing.NOW())
				.setDeletedRecords(asList("zeRecord"));

		transactionLog.prepare(firstTransactionId, transaction);

		assertThat(transactionLog.isCommitted(firstTransactionTempFile, recordDao)).isFalse();

	}

	@Test
	public void givenFirstChangeOfTransactionWasDeleteByIdAndFirstRecordDoesNotExistThenCommitted()
			throws Exception {

		when(recordDao.getCurrentVersion("zeRecord")).thenReturn(-1L);
		BigVaultServerTransaction transaction = new BigVaultServerTransaction(RecordsFlushing.NOW())
				.setDeletedRecords(asList("zeRecord"));

		transactionLog.prepare(firstTransactionId, transaction);

		assertThat(transactionLog.isCommitted(firstTransactionTempFile, recordDao)).isTrue();

	}

	@Test
	public void givenTransactionOfMultipleDeleteQueryAndRecordsForFirstQueryFoundThenNotCommitted()
			throws Exception {

		ArgumentCaptor<SolrParams> solrParamsArgumentCaptor = ArgumentCaptor.forClass(SolrParams.class);
		when(queryResponseDTO.getNumFound()).thenReturn(2L);
		when(recordDao.query(solrParamsArgumentCaptor.capture())).thenReturn(queryResponseDTO);
		BigVaultServerTransaction transaction = new BigVaultServerTransaction(RecordsFlushing.NOW())
				.setDeletedQueries(firstTransactionDeletedByQueries);

		transactionLog.prepare(firstTransactionId, transaction);

		assertThat(transactionLog.isCommitted(firstTransactionTempFile, recordDao)).isFalse();

		assertThat(solrParamsArgumentCaptor.getValue()).is(sameAsFirstQuery());
	}

	@Test
	public void givenTransactionOfMultipleDeleteQueryAndNoRecordForFirstQueryFoundThenCommitted()
			throws Exception {

		ArgumentCaptor<SolrParams> solrParamsArgumentCaptor = ArgumentCaptor.forClass(SolrParams.class);
		when(queryResponseDTO.getNumFound()).thenReturn(0L);
		when(recordDao.query(solrParamsArgumentCaptor.capture())).thenReturn(queryResponseDTO);
		BigVaultServerTransaction transaction = new BigVaultServerTransaction(RecordsFlushing.NOW())
				.setDeletedQueries(firstTransactionDeletedByQueries);

		transactionLog.prepare(firstTransactionId, transaction);

		assertThat(transactionLog.isCommitted(firstTransactionTempFile, recordDao)).isTrue();
		assertThat(solrParamsArgumentCaptor.getValue()).is(sameAsFirstQuery());
	}

	@Test
	public void givenTransactionOfOnlyOneDeleteQueryAndRecordFoundThenNoCommitted()
			throws Exception {

		SolrInputDocument zeRecord = newSolrInputDocument("zeRecord", 42L);
		SolrInputDocument zeRecordUpdate = newSolrInputDocument("zeRecord", 123L);
		ArgumentCaptor<SolrParams> solrParamsArgumentCaptor = ArgumentCaptor.forClass(SolrParams.class);
		when(queryResponseDTO.getNumFound()).thenReturn(2L);
		when(recordDao.query(solrParamsArgumentCaptor.capture())).thenReturn(queryResponseDTO);
		BigVaultServerTransaction transaction = new BigVaultServerTransaction(RecordsFlushing.NOW())
				.addDeletedQuery(deleteByQueryParams);

		transactionLog.prepare(firstTransactionId, transaction);

		assertThat(transactionLog.isCommitted(firstTransactionTempFile, recordDao)).isFalse();
		assertThat(solrParamsArgumentCaptor.getValue()).is(sameAsFirstQuery());

	}

	@Test
	public void givenTransactionOfOnlyOneDeleteQueryAndNoRecordFoundThenCommitted()
			throws Exception {

		SolrInputDocument zeRecord = newSolrInputDocument("zeRecord", 42L);
		SolrInputDocument zeRecordUpdate = newSolrInputDocument("zeRecord", 123L);
		ArgumentCaptor<SolrParams> solrParamsArgumentCaptor = ArgumentCaptor.forClass(SolrParams.class);
		when(queryResponseDTO.getNumFound()).thenReturn(0L);
		when(recordDao.query(solrParamsArgumentCaptor.capture())).thenReturn(queryResponseDTO);
		when(recordDao.get("zeRecord")).thenThrow(RecordDaoException.NoSuchRecordWithId.class);
		BigVaultServerTransaction transaction = new BigVaultServerTransaction(RecordsFlushing.NOW())
				.addDeletedQuery(deleteByQueryParams);

		transactionLog.prepare(firstTransactionId, transaction);

		assertThat(transactionLog.isCommitted(firstTransactionTempFile, recordDao)).isTrue();
		assertThat(solrParamsArgumentCaptor.getValue()).is(sameAsFirstQuery());
	}

	@Test
	public void whenRegroupAndMoveToContentDaoThenCreateLogFileWithCurrentTimeAsTheNameAndDeleteFiles()
			throws Exception {

		givenTimeIs(new LocalDateTime(2345, 6, 7, 8, 9, 10, 11));
		transactionLog.prepare(firstTransactionId, firstTransaction);
		transactionLog.flush(firstTransactionId, null);
		transactionLog.prepare(secondTransactionId, secondTransaction);
		transactionLog.flush(secondTransactionId, null);

		String id = transactionLog.regroupAndMove();
		assertThat(id).isEqualTo("tlogs/2345-06-07T08-09-10-011.tlog");

		String content = IOUtils.toString(getDataLayerFactory().getContentsDao().getContentInputStream(id, SDK_STREAM));
		assertThat(content).isEqualTo(expectedLogOfFirstTransaction + expectedLogOfSecondTransaction);
		assertThat(flushedTransaction1).doesNotExist();
		assertThat(flushedTransaction2).doesNotExist();
	}

	@Test
	public void givenNoFlushedTransactionsWhenRegroupAndMoveToContentDaoThenDoNothingAndReturnNull()
			throws Exception {

		givenTimeIs(new LocalDateTime(2345, 6, 7, 8, 9, 10, 11));
		transactionLog.prepare(firstTransactionId, firstTransaction);
		transactionLog.prepare(secondTransactionId, secondTransaction);

		String id = transactionLog.regroupAndMove();
		assertThat(id).isNull();

		verify(contentDao, never()).add(anyString(), any(InputStream.class));
		assertThat(flushedTransaction1).doesNotExist();
		assertThat(flushedTransaction2).doesNotExist();
	}

	@Test
	public void givenRecordDaoExceptionWhenRegroupAndMoveToContentDaoThenDoesNotDeleteFilesAndFuturePrepareFails()
			throws Exception {

		givenTimeIs(new LocalDateTime(2345, 6, 7, 8, 9, 10, 11));
		transactionLog.prepare(firstTransactionId, firstTransaction);
		transactionLog.flush(firstTransactionId, null);
		transactionLog.prepare(secondTransactionId, secondTransaction);
		transactionLog.flush(secondTransactionId, null);

		doThrow(ContentDaoRuntimeException.class).when(contentDao).add(anyString(), any(InputStream.class));

		try {
			transactionLog.regroupAndMove();
			fail("SecondTransactionLogRuntimeException_CouldNotRegroupAndMoveInVault expected");
		} catch (SecondTransactionLogRuntimeException_CouldNotRegroupAndMoveInVault e) {
			//OK
		}

		assertThat(flushedTransaction1).exists();
		assertThat(flushedTransaction2).exists();

		try {
			transactionLog.prepare(firstTransactionId, firstTransaction);
			fail("SecondTransactionLogRuntimeException_LogIsInInvalidStateCausedByPreviousException");
		} catch (SecondTransactionLogRuntimeException_LogIsInInvalidStateCausedByPreviousException e) {
			//OK
		}
	}

	@Test
	public void givenFileWithoutCarriageReturnsThenLineIteratorReturnLines()
			throws Exception {

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("line1\n");
		stringBuilder.append("line2\\nend\n");
		stringBuilder.append("line3__LINEBREAK__end");

		File file = newTempFileWithContent("zeFile", stringBuilder.toString());
		BufferedReader tLogBufferedReader = ioServices.newBufferedFileReader(file, SDK_STREAM);
		Iterator<String> lineIterator = new ReaderLinesIteratorV1(ioServices, tLogBufferedReader);

		assertThat(lineIterator.next()).isEqualTo("line1");
		assertThat(lineIterator.next()).isEqualTo("line2\\nend");
		assertThat(lineIterator.next()).isEqualTo("line3__LINEBREAK__end");
		assertThat(lineIterator.hasNext()).isFalse();

	}

	@Test
	public void givenFileWithCarriageReturnsThenLineIteratorReturnLines()
			throws Exception {

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("line1\n");
		stringBuilder.append("line2\\nend\n");
		stringBuilder.append("line3\r\nend\n");
		stringBuilder.append("line4\rend\n");
		stringBuilder.append("line5__LINEBREAK__end");

		File file = newTempFileWithContent("zeFile", stringBuilder.toString());
		BufferedReader tLogBufferedReader = ioServices.newBufferedFileReader(file, SDK_STREAM);
		Iterator<String> lineIterator = new ReaderLinesIteratorV1(ioServices, tLogBufferedReader);

		assertThat(lineIterator.next()).isEqualTo("line1");
		assertThat(lineIterator.next()).isEqualTo("line2\\nend");
		assertThat(lineIterator.next()).isEqualTo("line3__LINEBREAK__end");
		assertThat(lineIterator.next()).isEqualTo("line4end");
		assertThat(lineIterator.next()).isEqualTo("line5__LINEBREAK__end");
		assertThat(lineIterator.hasNext()).isFalse();

	}

	//TODO Test flush exception

	private Condition<? super Iterable<Map.Entry<String, String[]>>> sameAsFirstQuery() {
		return new Condition<Iterable<Map.Entry<String, String[]>>>() {
			@Override
			public boolean matches(Iterable<Map.Entry<String, String[]>> value) {
				String qValue = "";
				List<String> qValues = new ArrayList<>();
				List<String> allParamNames = new ArrayList<>();
				for (Iterator<Map.Entry<String, String[]>> iterator = value.iterator(); iterator.hasNext(); ) {
					Map.Entry<String, String[]> entry = iterator.next();
					if (qValue.isEmpty() && entry.getKey().equals("q") && entry.getValue().length > 0) {
						qValue = entry.getValue()[0];
						qValues.addAll(asList(entry.getValue()));
					}
					allParamNames.add(entry.getKey());
				}
				assertThat(allParamNames).containsOnly("q");
				assertThat(qValues).isEqualTo(singletonList(qValue));
				return true;
			}
		};
	}

	private Condition<? super File> content(final String expectedContent) {
		return new Condition<File>() {
			@Override
			public boolean matches(File value) {

				try {
					String content = FileUtils.readFileToString(value);
					assertThat(content).isEqualTo(expectedContent);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				return true;
			}
		};
	}
}
