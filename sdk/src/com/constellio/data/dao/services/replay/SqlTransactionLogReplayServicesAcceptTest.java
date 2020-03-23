package com.constellio.data.dao.services.replay;

import com.constellio.data.conf.PropertiesDataLayerConfiguration.InMemoryDataLayerConfiguration;
import com.constellio.data.conf.SecondTransactionLogType;
import com.constellio.data.dao.services.sql.MicrosoftSqlTransactionDao;
import com.constellio.data.dao.services.transactionLog.KafkaTransactionLogManagerAcceptTest;
import com.constellio.data.dao.services.transactionLog.SqlServerTransactionLogManager;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServices;
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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSearchable;
import static org.assertj.core.api.Assertions.assertThat;

public class SqlTransactionLogReplayServicesAcceptTest extends ConstellioTest {

	private LocalDateTime shishOClock = new LocalDateTime();
	private LocalDateTime shishOClockPlus1Hour = shishOClock.plusHours(1);
	private LocalDateTime shishOClockPlus2Hour = shishOClock.plusHours(2);
	private LocalDateTime shishOClockPlus3Hour = shishOClock.plusHours(3);
	private LocalDateTime shishOClockPlus4Hour = shishOClock.plusHours(4);

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaTransactionLogManagerAcceptTest.class);

	Users users = new Users();

	TestsSchemasSetup schemas = new TestsSchemasSetup();
	ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();

	SqlServerTransactionLogManager log;

	ReindexingServices reindexServices;
	RecordServices recordServices;
	MicrosoftSqlTransactionDao microsoftSqlTransactionDao;

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
				configuration.setSecondTransactionLogMode(SecondTransactionLogType.SQL_SERVER);
				configuration.setSecondTransactionLogMergeFrequency(5);
				configuration.setSecondTransactionLogBackupCount(3);
				configuration.setReplayTransactionStartVersion(0L);
				configuration.setMicrosoftSqlServerUrl("jdbc:sqlserver://localhost:1433");
				configuration.setMicrosoftSqlServerDatabase("constellio");
				configuration.setMicrosoftSqlServeruser("sa");
				configuration.setMicrosoftSqlServerpassword("ncix123$");
				configuration.setMicrosoftSqlServerencrypt(false);
				configuration.setMicrosoftSqlServertrustServerCertificate(false);
				configuration.setMicrosoftSqlServerloginTimeout(15);
			}
		});

		givenCollection(zeCollection).withAllTestUsers();

		defineSchemasManager().using(schemas.withAStringMetadata().withATitle().withAContentMetadata(whichIsSearchable).withAMultivaluedLargeTextMetadata().withABooleanMetadata());

		reindexServices = getModelLayerFactory().newReindexingServices();
		recordServices = getModelLayerFactory().newRecordServices();
		log = (SqlServerTransactionLogManager) getDataLayerFactory().getSecondTransactionLogManager();
	}


	@Test
	public void givenRecordWithParsedContentWithMultipleTypesOfLinebreakThenCanReplayWithoutProblems()
			throws Exception {

		User admin = getModelLayerFactory().newUserServices().getUserInCollection("admin", zeCollection);
		ContentManager contentManager = getModelLayerFactory().getContentManager();
		ContentVersionDataSummary data = contentManager
				.upload(getTestResourceInputStreamFactory("guide.pdf").create(SDK_STREAM));

		Record record1 = new TestRecord(zeSchema, "zeRecord");
		record1.set(zeSchema.stringMetadata(), "Guide d'architecture");
		record1.set(zeSchema.contentMetadata(), contentManager.createMajor(admin, "guide.pdf", data));
		recordServices.add(record1);

		log.regroupAndMove();
		log.destroyAndRebuildSolrCollection();

		Content content = recordServices.getDocumentById("zeRecord").get(zeSchema.contentMetadata());
		assertThat(content.getCurrentVersion().getHash()).isEqualTo("RKG3TTTTF7XBGN4T5G4OGWIYFAIQO3NR");
	}

	@Test
	public void givenRecordWithParsedContentWithMultipleTypesOfLinebreakWithAnUpdateThenCanReplayWithoutProblems()
			throws Exception {

		User admin = getModelLayerFactory().newUserServices().getUserInCollection("admin", zeCollection);
		ContentManager contentManager = getModelLayerFactory().getContentManager();
		ContentVersionDataSummary data = contentManager
				.upload(getTestResourceInputStreamFactory("guide.pdf").create(SDK_STREAM));

		Record record1 = new TestRecord(zeSchema, "zeRecord");
		record1.set(zeSchema.stringMetadata(), "Guide d'architecture");
		record1.set(zeSchema.contentMetadata(), contentManager.createMajor(admin, "guide.pdf", data));
		recordServices.add(record1);

		record1.set(zeSchema.stringMetadata(), "Guide d'architecture logiciel");
		recordServices.update(record1);

		log.regroupAndMove();
		log.destroyAndRebuildSolrCollection();

		String content = recordServices.getDocumentById("zeRecord").get(zeSchema.stringMetadata());
		assertThat(content).isEqualTo("Guide d'architecture logiciel");
	}

	@Test
	public void givenRecordWithParsedContentWithMultipleValuesWithAnUpdateThenCanReplayWithoutProblems()
			throws Exception {

		User admin = getModelLayerFactory().newUserServices().getUserInCollection("admin", zeCollection);
		ContentManager contentManager = getModelLayerFactory().getContentManager();
		ContentVersionDataSummary data = contentManager
				.upload(getTestResourceInputStreamFactory("guide.pdf").create(SDK_STREAM));

		Record record1 = new TestRecord(zeSchema, "zeRecord");
		record1.set(zeSchema.stringMetadata(), "Guide d'architecture");
		record1.set(zeSchema.contentMetadata(), contentManager.createMajor(admin, "guide.pdf", data));
		record1.set(zeSchema.booleanMetadata(), true);
		record1.set(zeSchema.title(), "zeRecord");
		record1.set(zeSchema.multivaluedLargeTextMetadata(), Arrays.asList(new String[]{"PDF", "DOCX", "TXT"}));
		recordServices.add(record1);

		record1.set(zeSchema.stringMetadata(), "Guide d'architecture logiciel");
		record1.set(zeSchema.multivaluedLargeTextMetadata(), Arrays.asList(new String[]{"PDF", "DOCX"}));
		recordServices.update(record1);

		log.regroupAndMove();
		log.destroyAndRebuildSolrCollection();

		String content = recordServices.getDocumentById("zeRecord").get(zeSchema.stringMetadata());
		assertThat(content).isEqualTo("Guide d'architecture logiciel");
	}


}
