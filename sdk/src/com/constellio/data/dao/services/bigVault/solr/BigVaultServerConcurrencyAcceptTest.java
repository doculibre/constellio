package com.constellio.data.dao.services.bigVault.solr;

import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.services.bigVault.solr.BigVaultException.CouldNotExecuteQuery;
import com.constellio.data.dao.services.bigVault.solr.BigVaultException.OptimisticLocking;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.solr.ConstellioSolrInputDocument;
import com.constellio.data.utils.ThreadList;
import com.constellio.sdk.tests.ConstellioTest;
import junit.framework.Assert;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.constellio.data.dao.dto.records.RecordsFlushing.NOW;
import static java.util.Arrays.asList;
import static junit.framework.Assert.fail;
import static org.assertj.core.api.Assertions.assertThat;

public class BigVaultServerConcurrencyAcceptTest extends ConstellioTest {

	private static final String transaction1 = "transaction1";
	private static final String transaction2 = "transaction2";
	private static final String transaction3 = "transaction3";
	BigVaultServer vaultServer;
	BigVaultServer anotherVaultServer;
	BigVaultServer aThirdVaultServer;
	private String gandalf = "gandalf";
	private String edouard = "edouard";
	private String dakota = "dakota";
	private List emptyList = new ArrayList<>();

	@Before

	public void setUp()
			throws Exception {
		givenDisabledAfterTestValidations();
		//		DataLayerFactory daosFactory = getDataLayerFactory();
		setupSolrServers();
		//vaultServer = daosFactory.getRecordsVaultServer();
		//anotherVaultServer = new BigVaultServer(vaultServer.getNestedSolrServer(), BigVaultLogger.disabled());
	}

	@Test
	public void givenTransactionWithOptimisticLockingExceptionWhenRetryingWithSameRecordsThenOK()
			throws Exception {

		add("Sam_Gamegie_ze_brave", "Frodon", "Gandalf");

		updateExpectingAnOptimisticLocking(inCurrentVersion("Gandalf"), inCurrentVersion("Sam_Gamegie_ze_brave"),
				inVersion("Frodon", 42L));

		try {
			updateWithCurrentVersionObtainedFromAQuery("Sam_Gamegie_ze_brave", "Gandalf");
			fail("Exception expected");
		} catch (Exception e) {
			//OK
		}

		getDataLayerFactory().newRecordDao().flush();

		updateWithCurrentVersionObtainedFromAQuery("Sam_Gamegie_ze_brave", "Gandalf");
	}

	@Test
	public void givenTransactionWithOptimisticLockingExceptionWhenRetryingWithSameRecordsUsingRealTimeGetThenOK()
			throws Exception {

		add("Sam_Gamegie_ze_brave", "Frodon", "Gandalf");

		updateExpectingAnOptimisticLocking(inCurrentVersion("Gandalf"), inCurrentVersion("Sam_Gamegie_ze_brave"),
				inVersion("Frodon", 42L));

		updateWithCurrentVersionObtainedFromARealtimeGet("Sam_Gamegie_ze_brave", "Gandalf");

	}

	@Test
	public void whenMultipleThreadsAreCommittingMassivelyThenCombined()
			throws Exception {

		vaultServer.setResilienceModeToZero();
		try {
			ThreadList<Thread> threadList = new ThreadList<>();
			final AtomicInteger errorsCounter = new AtomicInteger();
			for (int i = 1; i <= 20; i++) {
				final int threadId = i;
				threadList.add(new Thread() {
					@Override
					public void run() {
						for (int o = 1; o <= 100; o++) {
							BigVaultServerTransaction tx = new BigVaultServerTransaction(RecordsFlushing.NOW());

							SolrInputDocument doc = new SolrInputDocument();
							doc.setField("id", threadId + "-" + o);
							doc.setField("type_s", "test");
							tx.setNewDocuments(asList(doc));

							try {
								vaultServer.addAndCommit(tx);

							} catch (SolrServerException e) {
								errorsCounter.incrementAndGet();
								e.printStackTrace();
								throw new RuntimeException(e);
							} catch (IOException e) {
								errorsCounter.incrementAndGet();
								e.printStackTrace();
								throw new RuntimeException(e);
							}
						}
					}
				});
			}


			threadList.startAll();

			threadList.joinAll();

			ModifiableSolrParams params = new ModifiableSolrParams();
			params.set("q", "type_s:test");

			assertThat(errorsCounter.get()).isEqualTo(0);
			assertThat(vaultServer.query(params).getResults().getNumFound()).isEqualTo(2000);
		} finally {
			vaultServer.setResilienceModeToNormal();
		}
	}

	private void updateExpectingAnOptimisticLocking(SolrInputDocument... documents) {
		try {
			update(documents);
			Assert.fail("Expected :  Optimistic Locking");
		} catch (Exception e) {
		}
	}

	private void update(SolrInputDocument... documents)
			throws BigVaultException {
		vaultServer.addAll(new BigVaultServerTransaction(NOW).setUpdatedDocuments(asList(documents)));
	}

	private SolrInputDocument inCurrentVersion(String id) {
		Long version = getVersionOf(id);
		return updateDocument(id, "a", version);
	}

	private SolrInputDocument inVersion(String id, Long version) {
		return updateDocument(id, "a", version);
	}

	private void updateWithCurrentVersionObtainedFromAQuery(String... ids) {
		List<SolrInputDocument> inputDocuments = new ArrayList<>();
		for (String id : ids) {
			Long version = getVersionOf(id);
			inputDocuments.add(updateDocument(id, "a", version));
		}
		try {
			vaultServer.addAll(new BigVaultServerTransaction(NOW).setUpdatedDocuments(inputDocuments));
		} catch (BigVaultException e) {
			throw new RuntimeException(e);
		}
	}

	private void updateWithCurrentVersionObtainedFromARealtimeGet(String... ids) {
		List<SolrInputDocument> inputDocuments = new ArrayList<>();
		for (String id : ids) {
			Long version = getRealVersionOf(id);
			inputDocuments.add(updateDocument(id, "a", version));
		}
		try {
			vaultServer.addAll(new BigVaultServerTransaction(NOW).setUpdatedDocuments(inputDocuments));
		} catch (BigVaultException e) {
			throw new RuntimeException(e);
		}
	}

	private void add(String... ids) {
		List<SolrInputDocument> inputDocuments = new ArrayList<>();
		for (String id : ids) {
			inputDocuments.add(addDocument(id, "a"));
		}
		try {
			vaultServer.addAll(new BigVaultServerTransaction(NOW).setNewDocuments(inputDocuments));
		} catch (BigVaultException e) {
			throw new RuntimeException(e);
		}
	}

	private void setupSolrServers() {

		//		DataLayerSystemExtensions extensions = new DataLayerSystemExtensions();
		DataLayerFactory daosFactory = (DataLayerFactory) getDataLayerFactory();
		BigVaultServer recordsVaultServer = daosFactory.getRecordsVaultServer();

		vaultServer = recordsVaultServer.clone();
		anotherVaultServer = recordsVaultServer.clone();
		aThirdVaultServer = recordsVaultServer.clone();
	}

	@Test
	public void testDeathStarInvulnerability()
			throws Exception {

		vaultServer.getNestedSolrServer().add(addDocument(dakota, "A"));
		vaultServer.getNestedSolrServer().add(addDocument(edouard, "A"));
		vaultServer.getNestedSolrServer().add(addDocument(sasquatch, "A"));
		vaultServer.softCommit();

		//	assertThat(vaultServer.getNestedSolrServer()).isNotSameAs(anotherVaultServer.getNestedSolrServer());

		SolrInputDocument firstServerUpdatedDocument = updateDocument(dakota, "B",
				getVersionOfDocumentOnServer(dakota, vaultServer));
		SolrInputDocument firstServerUpdatedDocument2 = updateDocument(sasquatch, "B",
				getVersionOfDocumentOnServer(sasquatch, vaultServer));
		vaultServer.verifyTransactionOptimisticLocking(-1, transaction1,
				asList(firstServerUpdatedDocument, firstServerUpdatedDocument2));

		aThirdVaultServer.softCommit();
		SolrInputDocument secondServerUpdatedDocument = updateDocument(dakota, "C",
				getVersionOfDocumentOnServer(dakota, anotherVaultServer));
		SolrInputDocument secondServerUpdatedDocument2 = updateDocument(edouard, "C",
				getVersionOfDocumentOnServer(edouard, anotherVaultServer));
		try {
			anotherVaultServer
					.verifyTransactionOptimisticLocking(-1, transaction2,
							asList(secondServerUpdatedDocument2, secondServerUpdatedDocument));
			fail("Should throw an exception, since the first client has not finished the transaction");
		} catch (Exception e) {
			//OK
		}
		vaultServer.processChanges(new BigVaultServerTransaction(transaction1, RecordsFlushing.LATER(), emptyList,
				asList(firstServerUpdatedDocument, firstServerUpdatedDocument2), emptyList, emptyList));
		vaultServer.softCommit();

		assertThat(getValueOf(dakota)).isEqualTo("B");

		firstServerUpdatedDocument = updateDocument(dakota, "D",
				getVersionOfDocumentOnServer(dakota, vaultServer));
		firstServerUpdatedDocument2 = updateDocument(edouard, "D",
				getVersionOfDocumentOnServer(edouard, vaultServer));
		vaultServer.verifyTransactionOptimisticLocking(-1, transaction3,
				asList(firstServerUpdatedDocument, firstServerUpdatedDocument2));
		vaultServer.processChanges(new BigVaultServerTransaction(transaction3, RecordsFlushing.LATER(), emptyList,
				asList(firstServerUpdatedDocument, firstServerUpdatedDocument2), emptyList, emptyList));
		vaultServer.softCommit();
		assertThat(getValueOf(dakota)).isEqualTo("D");
		assertThat(getValueOf(edouard)).isEqualTo("D");
	}

	@Test
	public void givenLockIsNotDeletedThenAutomaticallyDeletedAfterAGivenTime()
			throws Exception {

		int theDelayBeforeAutomaticRemoval = 66;
		LocalDateTime lockOClock = new LocalDateTime();
		LocalDateTime lockRemovalOClock = lockOClock.plusSeconds(66);
		LocalDateTime oneSecondBeforeRemovalOClock = lockOClock.minusSeconds(1);

		givenTimeIs(lockOClock);
		vaultServer.getNestedSolrServer().add(addDocument(dakota, "A"));
		vaultServer.getNestedSolrServer().add(addDocument(edouard, "A"));
		vaultServer.softCommit();

		//	assertThat(vaultServer.getNestedSolrServer()).isNotSameAs(anotherVaultServer.getNestedSolrServer());

		SolrInputDocument firstServerUpdatedDocument = updateDocument(dakota, "B",
				getVersionOfDocumentOnServer(dakota, vaultServer));
		vaultServer.verifyTransactionOptimisticLocking(-1, transaction1, asList(firstServerUpdatedDocument));
		vaultServer.softCommit();
		assertThat(containsLockFor(dakota, vaultServer)).isTrue();

		vaultServer.removeLockWithAgeGreaterThan(theDelayBeforeAutomaticRemoval);
		vaultServer.softCommit();
		assertThat(containsLockFor(dakota, vaultServer)).isTrue();

		givenTimeIs(oneSecondBeforeRemovalOClock);
		vaultServer.removeLockWithAgeGreaterThan(theDelayBeforeAutomaticRemoval);
		vaultServer.softCommit();
		assertThat(containsLockFor(dakota, vaultServer)).isTrue();

		givenTimeIs(lockRemovalOClock);
		vaultServer.removeLockWithAgeGreaterThan(theDelayBeforeAutomaticRemoval);
		vaultServer.softCommit();
		assertThat(containsLockFor(dakota, vaultServer)).isFalse();

	}

	private boolean containsLockFor(String id, BigVaultServer solrServer)
			throws CouldNotExecuteQuery {

		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "id:lock__" + id);
		return solrServer.query(params).getResults().size() == 1;
	}

	private SolrInputDocument addLock(String id) {
		SolrInputDocument doc = new SolrInputDocument();
		doc.setField("id", lockId(id));
		doc.setField("type_s", "lock");
		doc.setField("_version_", "-1");
		return doc;
	}

	private String lockId(String id) {
		return id + "_lock";
	}

	@Test
	public void givenOptimisticLockingExceptionThenNoChangesToRecord()
			throws Exception {

		vaultServer.addAll(new BigVaultServerTransaction(NOW).setNewDocuments(
				asList(addDocument(gandalf, "A"), addDocument(edouard, "A"), addDocument(dakota, "A"))));

		Long gandalfVersion = getVersionOf(gandalf);
		Long edouardVersion = getVersionOf(edouard);

		try {
			vaultServer.addAll(new BigVaultServerTransaction(NOW).setUpdatedDocuments(
					asList(updateDocument(gandalf, "B", 42L), updateDocument(edouard, "A", edouardVersion))));

			fail("Expected OptimisticLocking");
		} catch (OptimisticLocking e) {
		}

		vaultServer.addAll(new BigVaultServerTransaction(NOW)
				.setUpdatedDocuments(asList(updateDocument(edouard, "B", edouardVersion))));
		edouardVersion = getVersionOf(edouard);

		assertThat(getValueOf(gandalf)).isEqualTo("A");
		assertThat(getValueOf(edouard)).isEqualTo("B");
		assertThat(getVersionOf(gandalf)).isEqualTo(gandalfVersion);
		assertThat(getVersionOf(edouard)).isEqualTo(edouardVersion);

		Long dakotaVersion = getVersionOf(dakota);
		vaultServer.addAll(new BigVaultServerTransaction(NOW)
				.setUpdatedDocuments(asList(updateDocument(dakota, "B", dakotaVersion))));
		Thread.sleep(5000);
		assertThat(getValueOf(gandalf)).isEqualTo("A");
		assertThat(getValueOf(edouard)).isEqualTo("B");
		assertThat(getValueOf(dakota)).isEqualTo("B");
		assertThat(getVersionOf(gandalf)).isEqualTo(gandalfVersion);
		assertThat(getVersionOf(edouard)).isEqualTo(edouardVersion);
		assertThat(getVersionOf(dakota)).isNotEqualTo(dakotaVersion);
	}

	// Confirm @SlowTest
	@Test
	public void givenAThreadCausingOptimisticLockingAndAnotherAddingWithoutProblemsThenOnlySecondRecordIsModified()
			throws Exception {

		vaultServer.addAll(new BigVaultServerTransaction(NOW).setNewDocuments(
				asList(addDocument(gandalf, "0"), addDocument(edouard, "0"), addDocument(dakota, "0"))));

		final int numberOfIterations = 1000;

		final AtomicBoolean expectedException = new AtomicBoolean(false);

		Thread thread1 = new Thread() {
			@Override
			public void run() {
				for (int i = 0; i < numberOfIterations; i++) {
					System.out.println("Iteration #" + i);
					try {
						vaultServer.addAll(new BigVaultServerTransaction(NOW).setUpdatedDocuments(asList(
								updateDocument(gandalf, "8888", 42L), updateDocument(edouard, "8888", 42L),
								updateDocument(dakota, "8888", 42L))));
						expectedException.set(true);
						break;
					} catch (BigVaultException e) {
						//OK
					}
				}
			}
		};
		Thread thread2 = new Thread() {
			@Override
			public void run() {
				for (int i = 0; i < numberOfIterations; i++) {
					boolean executed = false;
					while (!executed) {
						try {
							Long gandalfVersion = getVersionOf(gandalf);
							Integer gandalfValue = Integer.valueOf(getValueOf(gandalf));
							Long edouardVersion = getVersionOf(edouard);
							Integer edouardValue = Integer.valueOf(getValueOf(edouard));
							Long dakotaVersion = getVersionOf(dakota);
							Integer dakotaValue = Integer.valueOf(getValueOf(dakota));
							SolrInputDocument gandalfUpdate = updateDocument(gandalf, "" + (gandalfValue + 1), gandalfVersion);
							SolrInputDocument edouardUpdate = updateDocument(edouard, "" + (edouardValue + 1), edouardVersion);
							SolrInputDocument dakotaUpdate = updateDocument(dakota, "" + (dakotaValue + 1), dakotaVersion);
							vaultServer.addAll(new BigVaultServerTransaction(NOW).setUpdatedDocuments(
									asList(gandalfUpdate, edouardUpdate, dakotaUpdate)));
							executed = true;

						} catch (BigVaultException e) {
							vaultServer.removeLockWithAgeGreaterThan(2);
							e.printStackTrace();

						}
					}
				}
			}
		};
		thread1.start();
		thread2.start();

		thread1.join();
		thread2.join();

		assertThat(expectedException.get()).isFalse();

		assertThat(getValueOf(gandalf)).isEqualTo("" + numberOfIterations);
		assertThat(getValueOf(edouard)).isEqualTo("" + numberOfIterations);
		assertThat(getValueOf(dakota)).isEqualTo("" + numberOfIterations);
	}

	// Confirm @SlowTest
	@Test
	public void givenFlushIsNowWhenTwoThreadsAreModifyingTheSameDocumentThenNoConflict()
			throws Exception {
		givenTwoThreadsAreModifyingTheSameDocumentThenNoConflict(NOW());
	}

	// Confirm @SlowTest
	@Test
	public void givenFlushIsWithin2SecWhenTwoThreadsAreModifyingTheSameDocumentThenNoConflict()
			throws Exception {
		givenTwoThreadsAreModifyingTheSameDocumentThenNoConflict(RecordsFlushing.LATER);
	}

	private void givenTwoThreadsAreModifyingTheSameDocumentThenNoConflict(final RecordsFlushing recordsFlushing)
			throws BigVaultException, InterruptedException, IOException, SolrServerException {
		vaultServer.addAll(new BigVaultServerTransaction(NOW).setNewDocuments(
				asList(addDocument(gandalf, "0"), addDocument(edouard, "0"), addDocument(dakota, "0"))));

		final int numberOfIterations = 100;

		final AtomicInteger problems = new AtomicInteger();

		Thread thread1 = new Thread() {
			@Override
			public void run() {
				for (int i = 0; i < numberOfIterations; i++) {
					System.out.println(i);
					boolean retry = true;
					while (retry) {
						try {
							Long gandalfVersion = getVersionOf(gandalf);
							Integer gandalfValue = Integer.valueOf(getValueOf(gandalf));
							Long edouardVersion = getVersionOf(edouard);
							Integer edouardValue = Integer.valueOf(getValueOf(edouard));
							Long dakotaVersion = getVersionOf(dakota);
							Integer dakotaValue = Integer.valueOf(getValueOf(dakota));
							SolrInputDocument gandalfUpdate = updateDocument(gandalf, "" + (gandalfValue + 1), gandalfVersion);
							SolrInputDocument edouardUpdate = updateDocument(edouard, "" + (edouardValue + 1), edouardVersion);
							SolrInputDocument dakotaUpdate = updateDocument(dakota, "" + (dakotaValue + 1), dakotaVersion);
							vaultServer.addAll(new BigVaultServerTransaction(recordsFlushing).setUpdatedDocuments(
									asList(gandalfUpdate, edouardUpdate, dakotaUpdate)));
							retry = false;
						} catch (BigVaultException e) {
							problems.addAndGet(1);
							try {
								vaultServer.flush();
							} catch (IOException e1) {
								throw new RuntimeException(e1);
							} catch (SolrServerException e1) {
								throw new RuntimeException(e1);
							}
							retry = true;
						}
					}
				}
			}
		};
		Thread thread2 = new Thread() {
			@Override
			public void run() {
				for (int i = 0; i < numberOfIterations; i++) {
					System.out.println(i);
					boolean retry = true;
					while (retry) {
						try {
							Long gandalfVersion = getVersionOf(gandalf);
							Integer gandalfValue = Integer.valueOf(getValueOf(gandalf));
							Long edouardVersion = getVersionOf(edouard);
							Integer edouardValue = Integer.valueOf(getValueOf(edouard));
							Long dakotaVersion = getVersionOf(dakota);
							Integer dakotaValue = Integer.valueOf(getValueOf(dakota));
							SolrInputDocument gandalfUpdate = updateDocument(gandalf, "" + (gandalfValue + 1), gandalfVersion);
							SolrInputDocument edouardUpdate = updateDocument(edouard, "" + (edouardValue + 1), edouardVersion);
							SolrInputDocument dakotaUpdate = updateDocument(dakota, "" + (dakotaValue + 1), dakotaVersion);
							vaultServer.addAll(new BigVaultServerTransaction(recordsFlushing).setUpdatedDocuments(
									asList(gandalfUpdate, edouardUpdate, dakotaUpdate)));
							retry = false;
						} catch (BigVaultException e) {
							problems.addAndGet(1);
							try {
								vaultServer.flush();
							} catch (IOException e1) {
								throw new RuntimeException(e1);
							} catch (SolrServerException e1) {
								throw new RuntimeException(e1);
							}
							retry = true;
						}
					}
				}
			}
		};
		thread1.start();
		thread2.start();

		thread1.join();
		thread2.join();

		vaultServer.softCommit();

		assertThat(getValueOf(gandalf)).isEqualTo("" + (numberOfIterations * 2));

		assertThat(getValueOf(edouard)).isEqualTo("" + (numberOfIterations * 2));

		assertThat(getValueOf(dakota)).isEqualTo("" + (numberOfIterations * 2));

		assertThat(problems.get()).isGreaterThan(100)
				.describedAs("The test passed, but there wasn't enought conflict to prove it is correct");
	}

	boolean isExistingOnServer(String id, BigVaultServer solrServer)
			throws SolrServerException, CouldNotExecuteQuery {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "id:" + id);
		return solrServer.query(params).getResults().size() == 1;
	}

	Long getVersionOfDocumentOnServer(String id, BigVaultServer solrServer)
			throws SolrServerException, CouldNotExecuteQuery {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "id:" + id);
		return (Long) solrServer.query(params).getResults().get(0).getFieldValue("_version_");
	}

	String getValueOf(String id) {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "id:" + id);
		try {
			return (String) vaultServer.querySingleResult(params).getFieldValue("aField_s");
		} catch (BigVaultException e) {
			throw new RuntimeException(e);
		}
	}

	Long getRealVersionOf(String id) {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "id:" + id);
		try {
			return (Long) vaultServer.realtimeGet(id, false).getFieldValue("_version_");
		} catch (BigVaultException e) {
			throw new RuntimeException(e);
		}
	}

	Long getVersionOf(String id) {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "id:" + id);
		try {
			return (Long) vaultServer.querySingleResult(params).getFieldValue("_version_");
		} catch (BigVaultException e) {
			throw new RuntimeException(e);
		}
	}

	SolrInputDocument addDocument(String id, String value) {
		SolrInputDocument doc = new ConstellioSolrInputDocument();
		doc.setField("id", id);
		doc.setField("aField_s", value);
		return doc;
	}

	SolrInputDocument updateDocument(String id, String newValue, Long version) {
		SolrInputDocument doc = new ConstellioSolrInputDocument();
		doc.setField("id", id);
		doc.setField("_version_", version);
		Map<String, String> maps = new HashMap<>();
		maps.put("set", newValue);
		doc.setField("aField_s", maps);
		return doc;
	}

	SolrInputDocument updateDocument(String id, String newValue) {
		SolrInputDocument doc = new ConstellioSolrInputDocument();
		doc.setField("id", id);
		Map<String, String> maps = new HashMap<>();
		maps.put("set", newValue);
		doc.setField("aField_s", maps);
		return doc;
	}

}
