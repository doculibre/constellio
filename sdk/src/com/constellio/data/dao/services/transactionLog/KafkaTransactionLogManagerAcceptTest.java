package com.constellio.data.dao.services.transactionLog;

import com.constellio.data.conf.PropertiesDataLayerConfiguration.InMemoryDataLayerConfiguration;
import com.constellio.data.conf.SecondTransactionLogType;
import com.constellio.data.utils.ThreadList;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.DataLayerConfigurationAlteration;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSearchable;

public class KafkaTransactionLogManagerAcceptTest extends ConstellioTest {

	private LocalDateTime shishOClock = new LocalDateTime();
	private LocalDateTime shishOClockPlus1Hour = shishOClock.plusHours(1);
	private LocalDateTime shishOClockPlus2Hour = shishOClock.plusHours(2);
	private LocalDateTime shishOClockPlus3Hour = shishOClock.plusHours(3);
	private LocalDateTime shishOClockPlus4Hour = shishOClock.plusHours(4);

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaTransactionLogManagerAcceptTest.class);

	Users users = new Users();

	TestsSchemasSetup schemas = new TestsSchemasSetup();
	ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();

	KafkaTransactionLogManager log;

	ReindexingServices reindexServices;
	RecordServices recordServices;

	private AtomicInteger index = new AtomicInteger(-1);
	private AtomicInteger titleIndex = new AtomicInteger(0);
	private List<String> recordTextValues = new ArrayList<>();

	@Before
	public void setUp()
			throws Exception {
		// givenHashingEncodingIs(BASE64_URL_ENCODED);
		givenBackgroundThreadsEnabled();
		//withSpiedServices(SecondTransactionLogManager.class);

		configure(new DataLayerConfigurationAlteration() {
			@Override
			public void alter(InMemoryDataLayerConfiguration configuration) {
				configuration.setSecondTransactionLogEnabled(true);
				configuration.setSecondTransactionLogMode(SecondTransactionLogType.KAFKA);
				configuration.setSecondTransactionLogMergeFrequency(Duration.standardMinutes(5));
				configuration.setSecondTransactionLogBackupCount(3);
				configuration.setReplayTransactionStartVersion(0L);
				configuration.setKafkaServers("localhost:9092");
				configuration.setKafkaTopic("constellio-topic5");
			}
		});

		givenCollection(zeCollection).withAllTestUsers();

		defineSchemasManager().using(schemas.withAStringMetadata().withAContentMetadata(whichIsSearchable));

		reindexServices = getModelLayerFactory().newReindexingServices();
		recordServices = getModelLayerFactory().newRecordServices();
		log = (KafkaTransactionLogManager) getDataLayerFactory().getSecondTransactionLogManager();
	}

	// @LoadTest
	@Test
	public void whenMultipleThreadsAreAdding5000RecordsThenAllRecordsAreLogged()
			throws Exception {
		runAdding(2000000);
		//		assertThat(log.isLastFlushFailed()).isFalse();

		//log.destroyAndRebuildSolrCollection();
	}

	private void runAdding(final int nbRecordsToAdd)
			throws Exception {
		final ThreadList<Thread> threads = new ThreadList<>();
		for (int i = 0; i < 10; i++) {

			threads.add(new Thread(String.valueOf(i)) {
				@Override
				public void run() {
					int arrayIndex;

					while ((arrayIndex = index.incrementAndGet()) < nbRecordsToAdd) {
						if ((arrayIndex + 1) % 500 == 0) {
							System.out.println((arrayIndex + 1) + " / " + nbRecordsToAdd + " (Thread numero : " + getName() + ")");
						}
						Record record = new TestRecord(zeSchema);

						String title = "The Hobbit - Episode " + (arrayIndex + 1) + " of " + nbRecordsToAdd;
						record.set(zeSchema.stringMetadata(), title);
						try {
							recordServices.add(record);
						} catch (RecordServicesException e) {
							throw new RuntimeException(e);
						}
					}
				}
			});
		}
		threads.startAll();
		threads.joinAll();

		// int i = 0;
		// while (log.getFlushedFolder().list().length != 0) {
		// Thread.sleep(100);
		// i++;
		// if (i > 300) {
		// fail("Never committed");
		// }
		// }
		//
		// Thread.sleep(100);
		// if (log.getUnflushedFolder().list().length != 0) {
		// throw new RuntimeException("Unflushed folder not empty");
		// }
		// if (log.getFlushedFolder().list().length != 0) {
		// throw new RuntimeException("Flushed folder not empty");
		// }
		// if (log.getUnflushedFolder().list().length != 0) {
		// throw new RuntimeException("Unflushed folder not empty");
		// }
		//
		// List<String> stringMetadataLines = new ArrayList<>();
		// List<String> transactionLogs = getDataLayerFactory().getContentsDao().getFolderContents("tlogs");
		//
		// for (String id : transactionLogs) {
		// InputStream logStream = getDataLayerFactory().getContentsDao().getContentInputStream(id, SDK_STREAM);
		// for (String line : IOUtils.readLines(logStream)) {
		// stringMetadataLines.add(line);
		// }
		// }
		//
		// for (String value : recordTextValues) {
		// assertThat(stringMetadataLines).contains(zeSchema.stringMetadata().getDataStoreCode() + "=" + value);
		// }
		//
		// verify(log, atLeast(500)).prepare(anyString(), any(BigVaultServerTransaction.class));
		// reset(log);
		//
		// RecordDao recordDao = getDataLayerFactory().newRecordDao();
		// SolrSDKToolsServices solrSDKTools = new SolrSDKToolsServices(recordDao);
		// VaultSnapshot beforeRebuild = solrSDKTools.snapshot();
		//
		// alterSomeDocuments();
		//
		// log.destroyAndRebuildSolrCollection();
		//
		// VaultSnapshot afterRebuild = solrSDKTools.snapshot();
		// solrSDKTools.ensureSameSnapshots("vault altered", beforeRebuild, afterRebuild);
		//
		// for (String text : recordTextValues) {
		// assertThat(getRecordsByStringMetadata(text)).hasSize(1);
		// }
		//
		// verify(log, never()).prepare(anyString(), any(BigVaultServerTransaction.class));
	}
}
