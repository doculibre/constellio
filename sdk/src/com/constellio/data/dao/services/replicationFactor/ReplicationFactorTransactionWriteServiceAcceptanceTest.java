package com.constellio.data.dao.services.replicationFactor;

import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionResponseDTO;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.sdk.tests.ConstellioTest;
import com.google.common.collect.ImmutableMap;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.constellio.data.dao.services.replicationFactor.ReplicationFactorTestUtils.getLocalLogFilePath;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class ReplicationFactorTransactionWriteServiceAcceptanceTest extends ConstellioTest {

	private ReplicationFactorTransactionWriteService service;
	private Path localLogFilePath;

	@Before
	public void setUp() throws Exception {
		ContentDao contentDao = getDataLayerFactory().getContentsDao();
		ReplicationFactorLogService logService = new ReplicationFactorLogService(contentDao);
		localLogFilePath = getLocalLogFilePath(logService, contentDao);

		service = new ReplicationFactorTransactionWriteService(logService);
	}

	@Test
	public void givenRecordNotReplicatedThenLogged() throws Exception {
		Callable<Void> c1 = new Callable<Void>() {
			public Void call() {
				service.add(buildTransaction("id1", 1, false), null);
				return null;
			}
		};

		ExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		executorService.submit(c1);

		Thread.sleep(1000);

		List<String> lines = Files.readAllLines(localLogFilePath, StandardCharsets.UTF_8);
		assertThat(lines.size()).isEqualTo(1);
		assertThat(lines.get(0)).isNotEmpty();
	}

	@Test
	public void givenMultipleRecordsNotReplicatedThenAllLogged() throws Exception {
		Callable<Void> c1 = new Callable<Void>() {
			public Void call() {
				service.add(buildTransaction("id1", 1L, false), null);
				return null;
			}
		};

		Callable<Void> c2 = new Callable<Void>() {
			public Void call() {
				service.add(buildTransaction("id2", 2L, true),
						new TransactionResponseDTO(0, ImmutableMap.of("id2", 2L)));
				return null;
			}
		};

		Callable<Void> c3 = new Callable<Void>() {
			public Void call() {
				service.add(buildTransaction("id3", 3L, false), null);
				return null;
			}
		};

		ExecutorService executorService = Executors.newFixedThreadPool(3);
		executorService.invokeAll(asList(c1, c2, c3));
		Thread.sleep(1000);

		List<String> lines = Files.readAllLines(localLogFilePath, StandardCharsets.UTF_8);
		assertThat(lines.size()).isEqualTo(3);
		assertThat(lines).doesNotContain("");
	}

	private BigVaultServerTransaction buildTransaction(String id, long version, boolean add) {
		SolrInputDocument inputDocument = new SolrInputDocument();
		inputDocument.setField("id", id);
		inputDocument.setField("_version_", version);
		inputDocument.setField("title_s", "newTitle");

		BigVaultServerTransaction bigVaultServerTransaction = new BigVaultServerTransaction(RecordsFlushing.NOW());
		if (add) {
			bigVaultServerTransaction.setNewDocuments(Collections.singletonList(inputDocument));
		} else {
			bigVaultServerTransaction.setUpdatedDocuments(Collections.singletonList(inputDocument));
		}

		return bigVaultServerTransaction;
	}

}
