package com.constellio.data.dao.services.transactionLog;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.dto.records.QueryResponseDTO;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionResponseDTO;
import com.constellio.data.dao.dto.sql.RecordTransactionSqlDTO;
import com.constellio.data.dao.dto.sql.TransactionSqlDTO;
import com.constellio.data.dao.services.DataLayerLogger;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.bigVault.solr.SolrUtils;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.dao.services.leaderElection.LeaderElectionManager;
import com.constellio.data.dao.services.leaderElection.ObservableLeaderElectionManager;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.dao.services.solr.ConstellioSolrInputDocument;
import com.constellio.data.dao.services.sql.MicrosoftSqlTransactionDao;
import com.constellio.data.dao.services.sql.SqlRecordDaoFactory;
import com.constellio.data.dao.services.sql.SqlRecordDaoType;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_CouldNotFlushTransaction;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_LogIsInInvalidStateCausedByPreviousException;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_TransactionLogHasAlreadyBeenInitialized;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_TransactionLogIsNotInitialized;
import com.constellio.data.dao.services.transactionLog.sql.TransactionLogContent;
import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.threads.BackgroundThreadsManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.io.FileUtils;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.assertj.core.api.Condition;
import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class SqlServerTransactionLogManagerRealTest extends ConstellioTest {

	@Mock DataLayerConfiguration dataLayerConfiguration;

	@Mock BigVaultServer bigVaultServer;

	@Mock RecordDao recordDao;

	@Mock DataLayerSystemExtensions systemExtensions;

	@Mock MicrosoftSqlTransactionDao sqlTransactionDao;

	LocalDateTime shishOclockLocalDateTime = new LocalDateTime().plusHours(1);
	LocalDateTime tockOClockLocalDateTime = shishOclockLocalDateTime.plusMinutes(1);
	String shishOclock = SolrUtils.convertLocalDateTimeToSolrDate(shishOclockLocalDateTime);
	String tockOClock = SolrUtils.convertLocalDateTimeToSolrDate(tockOClockLocalDateTime);

	SolrInputDocument record1, record2, record5, record3, record6,record1Mod, record4 = new SolrInputDocument();
	String deletedRecord6 = "deletedRecord6";
	String deletedRecord7 = "deletedRecord7";
	@Mock QueryResponseDTO queryResponseDTO;
	@Mock BackgroundThreadsManager backgroundThreadsManager;
	ContentDao contentDao;
	SqlRecordDaoFactory sqlRecordDaoFactory;

	File baseFolder, unflushed, flushed;

	IOServices ioServices;

	SqlServerTransactionLogManager transactionLog;

	String deleteByQueryParams;
	RecordsFlushing recordsFlushing = RecordsFlushing.LATER();

	DataLayerLogger dataLayerLogger = new DataLayerLogger();
	String firstTransactionId = "firstTransaction";
	List<SolrInputDocument> firstTransactionNewRecords = new ArrayList<>();
	List<SolrInputDocument> firstTransactionModifiedRecords = new ArrayList<>();
	List<String> firstTransactionDeletedRecords = new ArrayList<>();
	List<String> firstTransactionDeletedByQueries = new ArrayList<>();
	TransactionResponseDTO transactionResponseDTO1 = new TransactionResponseDTO(0, new HashMap<>());

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

	String expectedLogOfFirstTransaction, expectedLogOfSecondTransaction, expectedLogOfFirstTransactionUnflushed;

	File firstTransactionTempFile, secondTransactionTempFile;
	ObservableLeaderElectionManager electionManager;

	@Before
	public void setUp()
			throws Exception {

		when(systemExtensions.isDocumentFieldLoggedInTransactionLog(anyString(), anyString(), anyString(), eq(true)))
				.thenReturn(true);
		when(systemExtensions.isDocumentFieldLoggedInTransactionLog(anyString(), anyString(), anyString(), eq(false)))
				.thenReturn(false);

		givenDisabledAfterTestValidations();
		withSpiedServices(ContentDao.class);
		electionManager=new ObservableLeaderElectionManager(new LeaderElectionManager() {
			@Override
			public boolean isCurrentNodeLeader() {
				return true;
			}

			@Override
			public void initialize() {

			}

			@Override
			public void close() {

			}
		});

		baseFolder = newTempFolder();
		when(dataLayerConfiguration.getSecondTransactionLogBaseFolder()).thenReturn(baseFolder);
		flushed = new File(baseFolder, "flushed");
		unflushed = new File(baseFolder, "unflushed");
		ioServices = getIOLayerFactory().newIOServices();
		contentDao = getDataLayerFactory().getContentsDao();
		sqlRecordDaoFactory = getDataLayerFactory().getSqlRecordDao();
		when(recordDao.getBigVaultServer()).thenReturn(bigVaultServer);
		when(bigVaultServer.countDocuments()).thenReturn(42L);
		transactionLog = spy(new SqlServerTransactionLogManager(dataLayerConfiguration, ioServices, recordDao, sqlRecordDaoFactory, contentDao,
				backgroundThreadsManager, dataLayerLogger, systemExtensions,
				getDataLayerFactory().getTransactionLogXmlRecoveryManager(),electionManager));
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

		record1Mod = newSolrInputDocument("record1", 56L);
		record1Mod.setField("text_s", "bValue");
		record1Mod.setField("date_dt", tockOClock);

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
		secondTransactionModifiedRecords.add(record1Mod);
		secondTransactionDeletedRecords.add(deletedRecord6);
		secondTransactionDeletedRecords.add(deletedRecord7);

		expectedLogOfFirstTransaction = buildTransactionExampleString();

		expectedLogOfSecondTransaction = buildSecondTransactionExampleString();

		expectedLogOfFirstTransactionUnflushed = buildFirstTransactionUnflushedExampleString();

		firstTransactionTempFile = new File(unflushed, firstTransactionId);
		secondTransactionTempFile = new File(unflushed, secondTransactionId);

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

		assertThat(firstTransactionTempFile).has(content(expectedLogOfFirstTransactionUnflushed));

	}

	@Test
	public void givenZZRecordsAreWrittenWhenWriteTransactionWithZZRecordsThenWrote()
			throws Exception {

		when(dataLayerConfiguration.isWriteZZRecords()).thenReturn(true);

		transactionLog.prepare(firstTransactionId, firstTransaction);
		transactionLog.flush(firstTransactionId, null);

		TransactionSqlDTO transactionSqlDTO = (TransactionSqlDTO) sqlRecordDaoFactory.getRecordDao(SqlRecordDaoType.TRANSACTIONS).get(firstTransactionId);
		assertThat(transactionSqlDTO).isNotNull();
		assertThat(transactionSqlDTO.getContent()).contains("record6ZZ");
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
	public void givenAnExceptionOccurWhenFlushingThenThrowExceptionAndBlockFutureTransactions()
			throws Exception {

		transactionLog.prepare(firstTransactionId, firstTransaction);
		File firstTransactionTempFile = new File(unflushed, firstTransactionId);
		doThrow(RuntimeException.class).when(transactionLog).doFlush(firstTransactionId, null);

		try {
			transactionLog.flush(firstTransactionId, null);
			fail("SecondTransactionLogRuntimeException_CouldNotFlushTransaction expected");
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
	public void givenAnSqlExceptionOccuredOnlyOnceWhenFlushingThenSucceeds()
			throws Exception {

		doThrow(new SQLException()).doNothing().when(sqlTransactionDao).insert("");

		transactionLog.tryThreeTimes(() -> {
			sqlTransactionDao.insert("");
			return true;
		});

		//OK
	}

	@Test
	public void givenAnSqlExceptionOccuredOnlyTwiceWhenFlushingThenSucceeds()
			throws Exception {

		doThrow(new SQLException()).doThrow(new SQLException()).doNothing().when(sqlTransactionDao).insert("");

		transactionLog.tryThreeTimes(() -> {
			sqlTransactionDao.insert("");
			return true;
		});

		//OK
	}

	@Test(expected = RuntimeException.class)
	public void givenAnSqlExceptionOccuredThreeTimeWhenFlushingThenThrowRuntimeException()
			throws Exception {

		doThrow(new SQLException()).doThrow(new SQLException()).doThrow(new SQLException()).when(sqlTransactionDao).insert("");

		transactionLog.tryThreeTimes(() -> {
			sqlTransactionDao.insert("");
			return true;
		});

		//OK
	}

	@Test(expected = SecondTransactionLogRuntimeException_TransactionLogIsNotInitialized.class)
	public void givenPreparedIsCalledBeforeInitializingTheTransactionLogThenException() {

		transactionLog = spy(new SqlServerTransactionLogManager(dataLayerConfiguration, ioServices, recordDao, sqlRecordDaoFactory, contentDao,
				backgroundThreadsManager, dataLayerLogger, systemExtensions,
				getDataLayerFactory().getTransactionLogXmlRecoveryManager(),electionManager));
		transactionLog.prepare(firstTransactionId, firstTransaction);

	}

	@Test(expected = SecondTransactionLogRuntimeException_TransactionLogHasAlreadyBeenInitialized.class)
	public void givenInitializedTransactionLogWhenStartASecondTimeThenException() {

		transactionLog.initialize();

	}

	@Test
	public void whenFlushingContentIsCreated()
			throws Exception {

		givenTimeIs(new LocalDateTime(2345, 6, 7, 8, 9, 10, 11));
		transactionLog.prepare(firstTransactionId, firstTransaction);
		transactionLog.flush(firstTransactionId, null);
		transactionLog.prepare(secondTransactionId, secondTransaction);
		transactionLog.flush(secondTransactionId, null);

		String id = transactionLog.regroupAndMove();

		RecordTransactionSqlDTO transactionLogs1 = (RecordTransactionSqlDTO) getDataLayerFactory().getSqlRecordDao().getRecordDao(SqlRecordDaoType.RECORDS).get("record1");
		RecordTransactionSqlDTO transactionLogs2 = (RecordTransactionSqlDTO) getDataLayerFactory().getSqlRecordDao().getRecordDao(SqlRecordDaoType.RECORDS).get("record5");

		ObjectMapper objectMapper = new ObjectMapper();
		String transactionLogs1Content = transactionLogs1.getContent();
		String transactionLogs2Content = transactionLogs2.getContent();

		assertThat(transactionLogs1Content+transactionLogs2Content).isEqualTo(buildRecord1And5ExampleString());

	}

	@Test
	public void whenFlushingContentIsCreatedContentIsSameWhenDeserialized()
			throws Exception {

		givenTimeIs(new LocalDateTime(2345, 6, 7, 8, 9, 10, 11));
		transactionLog.prepare(firstTransactionId, firstTransaction);
		transactionLog.flush(firstTransactionId, null);

		TransactionSqlDTO transactionLogs1 = (TransactionSqlDTO) getDataLayerFactory().getSqlRecordDao().getRecordDao(SqlRecordDaoType.TRANSACTIONS).get(firstTransactionId);

		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		ObjectMapper objectMapper = new ObjectMapper();

		//String transactionLogSerialized1 = ow.writeValueAsString(transactionLogs1);
		TransactionLogContent transactionLogDeserialized = objectMapper.readValue(transactionLogs1.getContent(), TransactionLogContent.class);

		assertThat(transactionLogDeserialized.getUpdatedDocuments().size()).isEqualTo(2);
		assertThat(transactionLogDeserialized.getNewDocuments().size()).isEqualTo(2);
		assertThat(transactionLogDeserialized.getDeletedQueries().size()).isEqualTo(2);
		assertThat(transactionLogDeserialized.getDeletedRecords().size()).isEqualTo(0);
	}

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


	@After
	public void tearDown() throws Exception {

		transactionLog.deleteAllTransactionsAndRecords();
	}

	private String buildTransactionExampleString() {

		StringBuilder sb = new StringBuilder();

		sb.append("{\r\n");
		sb.append("  \"transactionId\" : \"firstTransaction\",\r\n");
		sb.append("  \"newDocuments\" : [ {\r\n");
		sb.append("    \"id\" : \"record1\",\r\n");
		sb.append("    \"version\" : -1,\r\n");
		sb.append("    \"fields\" : {\r\n");
		sb.append("      \"date_dt\" : \""+shishOclock+"\",\r\n");
		sb.append("      \"text_s\" : \"aValue\"\r\n");
		sb.append("    }\r\n");
		sb.append("  }, {\r\n");
		sb.append("    \"id\" : \"record2\",\r\n");
		sb.append("    \"version\" : -1,\r\n");
		sb.append("    \"fields\" : {\r\n");
		sb.append("      \"text_s\" : \"anotherValue\",\r\n");
		sb.append("      \"otherfield_ss\" : \"false\"\r\n");
		sb.append("    }\r\n");
		sb.append("  } ],\r\n");
		sb.append("  \"updatedDocuments\" : [ {\r\n");
		sb.append("    \"id\" : \"record3\",\r\n");
		sb.append("    \"version\" : 34,\r\n");
		sb.append("    \"fields\" : {\r\n");
		sb.append("      \"text_s\" : \"line1__LINEBREAK__line2\"\r\n");
		sb.append("    }\r\n");
		sb.append("  }, {\r\n");
		sb.append("    \"id\" : \"record4\",\r\n");
		sb.append("    \"version\" : 45,\r\n");
		sb.append("    \"fields\" : {\r\n");
		sb.append("      \"text_s\" : \"value3\",\r\n");
		sb.append("      \"otherfield_ss\" : \"true\"\r\n");
		sb.append("    }\r\n");
		sb.append("  } ],\r\n");
		sb.append("  \"deletedRecords\" : [ ],\r\n");
		sb.append("  \"deletedQueries\" : [ \"((zeQuery) AND (firstFilter) AND (secondFilter))\", \"((anotherQuery) AND (firstFilter) AND (secondFilter) AND (thirdFilter))\" ]\r\n");
		sb.append("}");


		return sb.toString();
	}


	private String buildSecondTransactionExampleString() {


		StringBuilder sb = new StringBuilder();

		sb.append("{\r\n");
		sb.append("  \"transactionId\" : \"secondTransaction\",\r\n");
		sb.append("  \"newDocuments\" : [ {\r\n");
		sb.append("    \"id\" : \"record5\",\r\n");
		sb.append("    \"version\" : 56,\r\n");
		sb.append("    \"fields\" : {\r\n");
		sb.append("      \"date_dt\" : \""+tockOClock+"\",\r\n");
		sb.append("      \"text_s\" : \"aValue\"\r\n");
		sb.append("    }\r\n");
		sb.append("  } ],\r\n");
		sb.append("  \"updatedDocuments\" : [ {\r\n");
		sb.append("    \"id\" : \"record3\",\r\n");
		sb.append("    \"version\" : 34,\r\n");
		sb.append("    \"fields\" : {\r\n");
		sb.append("      \"text_s\" : \"line1__LINEBREAK__line2\"\r\n");
		sb.append("    }\r\n");
		sb.append("  } ],\r\n");
		sb.append("  \"deletedRecords\" : [ \"deletedRecord6\", \"deletedRecord7\" ],\r\n");
		sb.append("  \"deletedQueries\" : [ ]\r\n");
		sb.append("}");

		return sb.toString();
	}

	private String buildRecord1And5ExampleString(){

		StringBuilder sb = new StringBuilder();

		sb.append("[{\r\n");
		sb.append("  \"id\" : \"record1\",\r\n");
		sb.append("  \"version\" : \"-1\",\r\n");
		sb.append("  \"fields\" : {\r\n");
		sb.append("    \"date_dt\" : \""+shishOclock+"\",\r\n");
		sb.append("    \"text_s\" : \"aValue\"\r\n");
		sb.append("  }\r\n");
		sb.append("},{\r\n");
		sb.append("  \"id\" : \"record1\",\r\n");
		sb.append("  \"version\" : \"56\",\r\n");
		sb.append("  \"fields\" : {\r\n");
		sb.append("    \"date_dt\" : \""+tockOClock+"\",\r\n");
		sb.append("    \"text_s\" : \"bValue\"\r\n");
		sb.append("  }\r\n");
		sb.append("}][{\r\n");
		sb.append("  \"id\" : \"record5\",\r\n");
		sb.append("  \"version\" : \"56\",\r\n");
		sb.append("  \"fields\" : {\r\n");
		sb.append("    \"date_dt\" : \""+tockOClock+"\",\r\n");
		sb.append("    \"text_s\" : \"aValue\"\r\n");
		sb.append("  }\r\n");
		sb.append("}]");

		return sb.toString();
	}

	private String buildFirstTransactionUnflushedExampleString() {


		StringBuilder sb = new StringBuilder();

		sb.append("--transaction--\n");
		sb.append("addUpdate record1 -1\n");
		sb.append("text_s=aValue\n");
		sb.append("date_dt="+shishOclock+"\n");
		sb.append("addUpdate record2 -1\n");
		sb.append("text_s=anotherValue\n");
		sb.append("otherfield_ss=true\n");
		sb.append("otherfield_ss=false\n");
		sb.append("addUpdate record3 34\n");
		sb.append("text_s=line1__LINEBREAK__line2\n");
		sb.append("addUpdate record4 45\n");
		sb.append("text_s=value3\n");
		sb.append("otherfield_ss=false\n");
		sb.append("otherfield_ss=true\n");
		sb.append("deletequery ((zeQuery) AND (firstFilter) AND (secondFilter))\n");
		sb.append("deletequery ((anotherQuery) AND (firstFilter) AND (secondFilter) AND (thirdFilter))\n");

		return sb.toString();
	}

	private String completeTLOG() throws SQLException {
		List<RecordTransactionSqlDTO> transactionLogs = getDataLayerFactory().getSqlRecordDao().getRecordDao(SqlRecordDaoType.RECORDS).getAll();
		int currentVersion = getDataLayerFactory().getSqlRecordDao().getRecordDao(SqlRecordDaoType.TRANSACTIONS).getCurrentVersion();

		return String.join("\n", transactionLogs.stream().filter(x -> x.getLogVersion() == currentVersion)
				.map(x -> x.getContent()).collect(Collectors.toList()));
	}

}
