package com.constellio.data.dao.services.replicationFactor;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.dao.services.bigVault.solr.SolrCloudUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.setups.Users;
import org.apache.commons.io.input.NullInputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@UiTest
public class TransactionLogReplicationFactorManagerAcceptanceTest extends ConstellioTest {

	// NOTE tests can only be performed with a solr cloud setup

	private RMTestRecords records = new RMTestRecords(zeCollection);
	private Users users = new Users();

	private RecordServices recordServices;
	private RMSchemasRecordsServices rm;

	private Path localLogFilePath;

	@Before
	public void setUp() throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
				.withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent()
				.withDocumentsDecommissioningList());

		recordServices = getModelLayerFactory().newRecordServices();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		if (!SolrCloudUtils.isOnline(getDataLayerFactory().getRecordsVaultServer().getNestedSolrServer())) {
			fail("Solr cloud must be stable");
		}

		getDataLayerFactory().getContentsDao()
				.add("replicationFactor/degradedStateTransactions-1.tlog", new NullInputStream(0));
		localLogFilePath = getDataLayerFactory().getContentsDao()
				.getFileOf("replicationFactor/degradedStateTransactions-1.tlog").toPath();
	}

	@After
	public void cleanUp() throws Exception {
		closeManager();
	}

	@Test
	public void givenNonExistingRecordAndAddTransactionThenRecordCreated() throws Exception {
		byte[] content = Files.readAllBytes(getTestResourceFile("add.txt").toPath());
		Files.write(localLogFilePath, content);

		waitUntilRecordExists("00000000428");

		Record record = recordServices.getDocumentById("00000000428");
		assertThat(record).isNotNull();
		assertThat(record.getTitle()).isEqualTo("Abeille3");

		assertThat(Files.exists(localLogFilePath)).isFalse();
	}

	@Test
	public void givenNonExistingRecordAndNoAddTransactionThenAllTransactionsIgnored() throws Exception {
		byte[] content = Files.readAllBytes(getTestResourceFile("update.txt").toPath());
		Files.write(localLogFilePath, content);

		waitUntilRecordExists("00000000422");

		try {
			recordServices.getDocumentById("00000000415");
			fail();
		} catch (NoSuchRecordWithId ignored) {
		}

		assertThat(Files.exists(localLogFilePath)).isFalse();
	}

	@Test
	public void givenExistingFileAndVersionLowerThanTransactionVersionThenTransactionIsIgnored() throws Exception {
		byte[] content = Files.readAllBytes(getTestResourceFile("update2.txt").toPath());
		Files.write(localLogFilePath, content);

		waitUntilRecordExists("00000000422");

		Record record1 = recordServices.getDocumentById("A01");
		assertThat(record1.getTitle()).isEqualTo("Abeille222");
		assertThat(rm.wrapFolder(record1).getMediumTypes()).hasSize(2);

		assertThat(Files.exists(localLogFilePath)).isFalse();
	}

	@Test
	public void givenMultipleTransactionsInLogThenAllTransactionsReplayed() throws Exception {
		byte[] content = Files.readAllBytes(getTestResourceFile("addUpdate.txt").toPath());
		Files.write(localLogFilePath, content);

		waitUntilRecordExists("00000000422");

		Record record = recordServices.getDocumentById("00000000422");
		assertThat(record).isNotNull();
		assertThat(record.getTitle()).isEqualTo("Aigle2");

		Record record1 = recordServices.getDocumentById("00000000428");
		assertThat(record1).isNotNull();
		assertThat(record1.getTitle()).isEqualTo("Abeille3");

		Record record2 = recordServices.getDocumentById("A01");
		assertThat(record2).isNotNull();
		assertThat(record2.getTitle()).isEqualTo("AbeilleAbeille");

		Record record3 = recordServices.getDocumentById("A02");
		assertThat(record3).isNotNull();
		assertThat(record3.getTitle()).isEqualTo("AigleAigle");

		assertThat(Files.exists(localLogFilePath)).isFalse();
	}

	@Test
	public void givenExistingRecordAndDeleteTransactionThenRecordDeleted() throws Exception {
		byte[] content = Files.readAllBytes(getTestResourceFile("delete.txt").toPath());
		Files.write(localLogFilePath, content);

		waitUntilRecordExists("00000000417");

		for (String id : asList("00000000101", "00000000099", "00000000100", "A01_paperContractWithDifferentCopy",
				"A01_paperProcesWithDifferentCopy", "A01_numericDocumentWithSameCopy", "A01_paperDocumentWithSameCopy",
				"A01_numericProcesWithDifferentCopy", "00000000098", "A01_numericContractWithDifferentCopy")) {
			try {
				recordServices.getDocumentById(id);
				fail();
			} catch (NoSuchRecordWithId ignored) {
			}
		}
	}

	@Test
	public void givenMultipleLogFilesThenAllTransactionReplayedInCorrectOrder() throws Exception {
		getDataLayerFactory().getContentsDao().add("replicationFactor/degradedStateTransactions-2.tlog",
				new NullInputStream(0));
		Path localLogFilePath2 = getDataLayerFactory().getContentsDao()
				.getFileOf("replicationFactor/degradedStateTransactions-2.tlog").toPath();

		byte[] content = Files.readAllBytes(getTestResourceFile("multiple1.txt").toPath());
		byte[] content2 = Files.readAllBytes(getTestResourceFile("multiple2.txt").toPath());

		Executors.newFixedThreadPool(2).invokeAll(asList(
				buildWrite(localLogFilePath, content),
				buildWrite(localLogFilePath2, content2)));

		waitUntilRecordExists("00000000422");

		Folder folder = rm.wrapFolder(recordServices.getDocumentById("00000000415"));
		assertThat(folder.getTitle()).isEqualTo("abeille222");
		assertThat(folder.getDescription()).isEqualTo("Test");

		assertThat(Files.exists(localLogFilePath)).isFalse();
		assertThat(Files.exists(localLogFilePath2)).isFalse();
	}

	private void waitUntilRecordExists(String recordId) {
		Record record = null;
		long start = System.currentTimeMillis();
		do {
			try {
				record = recordServices.getDocumentById(recordId);
				if (System.currentTimeMillis() - start > 15000L) {
					fail();
				}
			} catch (NoSuchRecordWithId ignored) {
			}
		} while (record == null);
	}

	private Callable<Void> buildWrite(final Path path, final byte[] content) {
		return new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				try {
					Files.write(path, content);
				} catch (Exception ignored) {
				}
				return null;
			}
		};
	}

	private void closeManager() {
		getDataLayerFactory().getTransactionLogReplicationFactorManager().close();
	}

}
