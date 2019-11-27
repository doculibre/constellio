package com.constellio.data.dao.services.replicationFactor;

import com.constellio.data.dao.services.bigVault.solr.SolrUtils;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.dao.services.replicationFactor.ReplicationFactorTestUtils.MockReplicationFactorTransactionReadTask;
import com.constellio.data.dao.services.replicationFactor.dto.ReplicationFactorSolrInputField;
import com.constellio.data.dao.services.replicationFactor.dto.ReplicationFactorTransaction;
import com.constellio.data.dao.services.replicationFactor.dto.ReplicationFactorTransactionType;
import com.constellio.data.utils.TimeProvider;
import com.constellio.sdk.tests.ConstellioTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.solr.common.SolrInputDocument;
import org.joda.time.DateTimeZone;
import org.junit.Before;
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
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

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
