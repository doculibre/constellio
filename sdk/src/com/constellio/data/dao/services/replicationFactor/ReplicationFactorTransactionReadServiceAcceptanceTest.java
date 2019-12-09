package com.constellio.data.dao.services.replicationFactor;

import com.constellio.data.dao.services.bigVault.solr.SolrUtils;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.leaderElection.ObservableLeaderElectionManager;
import com.constellio.data.dao.services.leaderElection.StandaloneLeaderElectionManager;
import com.constellio.data.dao.services.replicationFactor.ReplicationFactorTestUtils.MockReplicationFactorTransactionReadTask;
import com.constellio.data.dao.services.replicationFactor.dto.ReplicationFactorSolrInputField;
import com.constellio.data.dao.services.replicationFactor.dto.ReplicationFactorTransaction;
import com.constellio.data.dao.services.replicationFactor.dto.ReplicationFactorTransactionType;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.sdk.tests.ConstellioTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.solr.common.SolrInputDocument;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import static com.constellio.data.dao.services.replicationFactor.ReplicationFactorTestUtils.FOLDER;
import static com.constellio.data.dao.services.replicationFactor.ReplicationFactorTestUtils.PREFIX;
import static com.constellio.sdk.tests.TestUtils.linkEventBus;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// FIXME ignored until we fully support solr 8+
@Ignore
public class ReplicationFactorTransactionReadServiceAcceptanceTest extends ConstellioTest {

	private ContentDao contentDao;

	private ReplicationFactorTransactionReadService transactionReadService;
	private ObjectMapper objectMapper;
	private Path localLogFilePath;

	@Before
	public void setUp() throws Exception {
		contentDao = getDataLayerFactory().getContentsDao();
		ReplicationFactorLogService logService = new ReplicationFactorLogService(contentDao);
		transactionReadService = new ReplicationFactorTransactionReadService(getDataLayerFactory(), logService,
				getDataLayerFactory().getExtensions().getSystemWideExtensions());
		objectMapper = new ObjectMapper();

		localLogFilePath = contentDao.getFileOf(FOLDER + "/" + PREFIX + "-1.tlog").toPath();

		String content = objectMapper.writeValueAsString(buildTransaction("id1", 12345L));
		Files.write(localLogFilePath, content.concat(System.lineSeparator()).getBytes());
	}

	@Test
	public void givenTransactionInLogThenTransactionReplayed() throws Exception {
		new MockReplicationFactorTransactionReadTask(transactionReadService).run();

		assertThat(localLogFilePath.toFile().exists()).isFalse();
	}

	@Test
	public void givenTwoTransactionLogFilesThenAllTransactionReplayed() throws Exception {
		Path localLogFilePath2 = contentDao.getFileOf(FOLDER + "/" + PREFIX + "-2.tlog").toPath();

		String content = objectMapper.writeValueAsString(buildTransaction("id2", 123456L));
		Files.write(localLogFilePath2, content.concat(System.lineSeparator()).getBytes());

		new MockReplicationFactorTransactionReadTask(transactionReadService).run();

		assertThat(localLogFilePath.toFile().exists()).isFalse();
		assertThat(localLogFilePath2.toFile().exists()).isFalse();
	}

	@Test
	public void givenMultipleTransactionsOnSameRecordThenTransactionsOrderedByVersion() {
		Map<String, Set<ReplicationFactorTransaction>> transactionsByRecordIds = new LinkedHashMap<>();

		Set<ReplicationFactorTransaction> transactions = new TreeSet<>();
		transactions.add(buildTransaction("id1", 10L));
		transactionsByRecordIds.put("id1", transactions);

		transactionsByRecordIds.get("id1").add(buildTransaction("id1", 2L));
		transactionsByRecordIds.get("id1").add(buildTransaction("id1", 8L));

		transactionsByRecordIds.put("id2", new TreeSet<>(singletonList(buildTransaction("id2", 100L))));

		transactionsByRecordIds.get("id1").add(buildTransaction("id1", 5L));
		transactionsByRecordIds.get("id1").add(buildTransaction("id1", -1L));

		assertThat(new ArrayList<>(transactionsByRecordIds.keySet())).containsExactly("id1", "id2");

		List<ReplicationFactorTransaction> orderedTransactions = new ArrayList<>(transactionsByRecordIds.get("id1"));
		assertThat(orderedTransactions).extracting("version").containsExactly(-1L, 2L, 5L, 8L, 10L);
	}

	@Test
	public void givenUnorderedTransactionsThenTransactionsOrderedByTimestamp() {
		Set<ReplicationFactorTransaction> sortedTransactions = new TreeSet<>(transactionReadService.getComparator());

		ReplicationFactorTransaction transaction2 = buildTransaction("id2", 12L, 2L);
		sortedTransactions.add(transaction2);
		ReplicationFactorTransaction transaction3 = buildTransaction("id3", 13L, 3L);
		sortedTransactions.add(transaction3);
		ReplicationFactorTransaction transaction1 = buildTransaction("id1", 11L, 1L);
		sortedTransactions.add(transaction1);

		assertThat(sortedTransactions).containsExactly(transaction1, transaction2, transaction3);
	}

	@Test
	public void givenDistributedModeThenOnlyMasterIsRunningTask() {
		ModelLayerFactory modelLayerFactory1 = getModelLayerFactory();
		ModelLayerFactory modelLayerFactory2 = getModelLayerFactory("other-instance");
		linkEventBus(modelLayerFactory1, modelLayerFactory2, 1);

		ReplicationFactorTransactionReadService transactionReadService2 =
				new ReplicationFactorTransactionReadService(modelLayerFactory2.getDataLayerFactory(),
						new ReplicationFactorLogService(modelLayerFactory2.getDataLayerFactory().getContentsDao()),
						modelLayerFactory2.getDataLayerFactory().getExtensions().getSystemWideExtensions());
		transactionReadService2.start();

		transactionReadService.start();

		if (modelLayerFactory2.getDataLayerFactory().getLeaderElectionService().isCurrentNodeLeader()) {
			assertThat(transactionReadService.executor).isNull();
			assertThat(transactionReadService2.executor).isNotNull();
		} else {
			assertThat(transactionReadService.executor).isNotNull();
			assertThat(transactionReadService2.executor).isNull();
		}
	}

	@Test
	public void givenNodeIsLeaderThenExecutorIsStarted() {
		DataLayerFactory dataLayerFactory = spy(getModelLayerFactory().getDataLayerFactory());

		ObservableLeaderElectionManager leaderElectionManager = new ObservableLeaderElectionManager(new StandaloneLeaderElectionManager());
		when(dataLayerFactory.getLeaderElectionService()).thenReturn(leaderElectionManager);

		transactionReadService = new ReplicationFactorTransactionReadService(dataLayerFactory,
				new ReplicationFactorLogService(dataLayerFactory.getContentsDao()),
				dataLayerFactory.getExtensions().getSystemWideExtensions());
		transactionReadService.start();

		assertThat(transactionReadService.executor).isNotNull();
	}

	@Test
	public void givenNodeIsNotLeaderThenExecutorIsNotStarted() {
		DataLayerFactory dataLayerFactory = spy(getModelLayerFactory().getDataLayerFactory());

		StandaloneLeaderElectionManager electionManager = new StandaloneLeaderElectionManager();
		electionManager.setLeader(false);
		ObservableLeaderElectionManager leaderElectionManager = new ObservableLeaderElectionManager(electionManager);
		when(dataLayerFactory.getLeaderElectionService()).thenReturn(leaderElectionManager);

		transactionReadService = new ReplicationFactorTransactionReadService(dataLayerFactory,
				new ReplicationFactorLogService(dataLayerFactory.getContentsDao()),
				dataLayerFactory.getExtensions().getSystemWideExtensions());
		transactionReadService.start();

		assertThat(transactionReadService.executor).isNull();
	}

	@Test
	public void givenLeaderChangeThenExecutorHasCorrectBehavior() {
		DataLayerFactory dataLayerFactory = spy(getModelLayerFactory().getDataLayerFactory());

		ObservableLeaderElectionManager leaderElectionManager = new ObservableLeaderElectionManager(new StandaloneLeaderElectionManager());
		when(dataLayerFactory.getLeaderElectionService()).thenReturn(leaderElectionManager);

		transactionReadService = spy(new ReplicationFactorTransactionReadService(dataLayerFactory,
				new ReplicationFactorLogService(dataLayerFactory.getContentsDao()),
				dataLayerFactory.getExtensions().getSystemWideExtensions()));
		when(transactionReadService.createReplicationFactorReadTask())
				.thenReturn(new MockReplicationFactorTransactionReadTask(transactionReadService));

		transactionReadService.start();

		transactionReadService.onLeaderStatusChanged(false);
		verify(transactionReadService).stop();

		long start = System.currentTimeMillis();
		do {
			if (System.currentTimeMillis() - start > 15000L) {
				fail();
			}
		} while (!transactionReadService.executor.isTerminated());

		assertThat(transactionReadService.executor.isTerminated()).isTrue();

		transactionReadService.onLeaderStatusChanged(true);
		verify(transactionReadService, times(2)).start();

		start = System.currentTimeMillis();
		do {
			if (System.currentTimeMillis() - start > 15000L) {
				fail();
			}
		} while (transactionReadService.executor.isTerminated());

		assertThat(transactionReadService.executor.isTerminated()).isFalse();
	}

	private ReplicationFactorTransaction buildTransaction(String id, long version) {
		long timestamp = TimeProvider.getLocalDateTime().toDateTime(DateTimeZone.UTC).getMillis();
		return buildTransaction(id, version, timestamp);
	}

	private ReplicationFactorTransaction buildTransaction(String id, long version, long timestamp) {
		SolrInputDocument inputDocument = new SolrInputDocument();
		inputDocument.setField("id", id);
		inputDocument.setField("_version_", version);
		inputDocument.setField("title_s", "newTitle");

		return ReplicationFactorTransaction.builder()
				.id(UUID.randomUUID().toString())
				.recordId(id)
				.type(ReplicationFactorTransactionType.UPDATE)
				.version(version)
				.timestamp(timestamp)
				.fields(toSolrInputFields(SolrUtils.getFields(inputDocument)))
				.build();
	}

	private Map<String, ReplicationFactorSolrInputField> toSolrInputFields(
			Map<String, org.apache.solr.common.SolrInputField> fields) {
		Map<String, ReplicationFactorSolrInputField> convertedFields = new HashMap<>();
		for (Map.Entry<String, org.apache.solr.common.SolrInputField> entry : fields.entrySet()) {
			ReplicationFactorSolrInputField convertedField = new ReplicationFactorSolrInputField(entry.getValue().getName(), entry.getValue().getValue());
			convertedFields.put(entry.getKey(), convertedField);
		}
		return convertedFields;
	}

}
