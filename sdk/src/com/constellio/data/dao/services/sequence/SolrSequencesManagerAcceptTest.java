package com.constellio.data.dao.services.sequence;

import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.utils.ThreadList;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.SlowTest;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class SolrSequencesManagerAcceptTest extends ConstellioTest {

	private static final String CONFIG_PATH = "/sequence.properties";

	SolrSequencesManager sequencesManager;
	SolrClient client;

	@Before
	public void setUp()
			throws Exception {
		notAUnitItest = true;

		//TODO AFTER-TEST-VALIDATION-SEQ
		givenDisabledAfterTestValidations();
		givenTransactionLogIsEnabled();
		sequencesManager = new SolrSequencesManager(getDataLayerFactory().newRecordDao(),
				getDataLayerFactory().getSecondTransactionLogManager());
		client = getDataLayerFactory().newRecordDao().getBigVaultServer().getNestedSolrServer();
	}

	@Test
	@SlowTest
	public void givenMultipleThreadsWitDifferentGeneratorThenAlwaysUniqueIds()
			throws Exception {

		final Set<Long> concurrentSet = Collections.synchronizedSet(new HashSet<Long>());
		final List<Long> concurrentList = Collections.synchronizedList(new ArrayList<Long>());

		ThreadList<Thread> threads = new ThreadList<>();

		final AtomicInteger total = new AtomicInteger();
		for (int i = 0; i < 100; i++) {
			threads.add(new Thread() {
				@Override
				public void run() {
					SequencesManager sequencesManager = new SolrSequencesManager(getDataLayerFactory().newRecordDao(), null);
					List<Long> ids = new ArrayList<Long>();
					for (int j = 0; j < 200; j++) {
						try {
							ids.add(sequencesManager.next("zeSequence"));
							total.incrementAndGet();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					concurrentSet.addAll(ids);
					concurrentList.addAll(ids);
				}
			});
		}

		threads.startAll();

		while (total.get() < 18000) {
			System.out.println(total.get() + "/20000");
			Thread.sleep(2000);
		}

		threads.joinAll();

		assertThat(concurrentList.size()).isEqualTo(20000);
		assertThat(concurrentSet.size()).isEqualTo(20000);
	}

	@Test
	public void givenMultipleThreadsWitSameGeneratorThenAlwaysUniqueIds()
			throws Exception {

		final Set<Long> concurrentSet = Collections.synchronizedSet(new HashSet<Long>());

		ThreadList<Thread> threads = new ThreadList<>();

		for (int i = 0; i < 10; i++) {
			threads.add(new Thread() {
				@Override
				public void run() {
					Set<Long> ids = new HashSet<Long>();
					for (int j = 0; j < 1000; j++) {
						concurrentSet.add(sequencesManager.next("zeSequence"));
					}
					//concurrentSet.addAll(ids);
				}
			});
		}

		threads.startAll();

		while (concurrentSet.size() < 10000) {
			System.out.println(concurrentSet.size());
			Thread.sleep(2000);
		}

		threads.joinAll();

		assertThat(concurrentSet).hasSize(10000);

	}

	@Test
	public void whenGetIdsThenZeroPadded()
			throws Exception {

		assertThat(sequencesManager.next("sequence1")).isEqualTo(1L);
		assertThat(sequencesManager.next("sequence1")).isEqualTo(2L);
		assertThat(sequencesManager.next("sequence2")).isEqualTo(1L);

	}

	@Test
	public void whenCallNextSetAndLastSequenceValueThenCorrectAnswers()
			throws Exception {

		SequencesManager sequencesManager1 = new SolrSequencesManager(getDataLayerFactory().newRecordDao(),
				getDataLayerFactory().getSecondTransactionLogManager());
		SequencesManager sequencesManager2 = new SolrSequencesManager(getDataLayerFactory().newRecordDao(),
				getDataLayerFactory().getSecondTransactionLogManager());

		assertThat(sequencesManager1.next("seq1")).isEqualTo(1L);
		assertThat(sequencesManager1.getLastSequenceValue("seq1")).isEqualTo(1L);
		assertThat(sequencesManager2.next("seq1")).isEqualTo(2L);
		assertThat(sequencesManager1.next("seq1")).isEqualTo(3L);
		assertThat(sequencesManager2.next("seq1")).isEqualTo(4L);
		assertThat(sequencesManager1.next("seq1")).isEqualTo(5L);
		assertThat(sequencesManager1.getLastSequenceValue("seq1")).isEqualTo(5L);
		assertThat(sequencesManager2.next("seq1")).isEqualTo(6L);
		assertThat(sequencesManager1.getLastSequenceValue("seq1")).isEqualTo(6L);

		assertThat(sequencesManager1.next("seq2")).isEqualTo(1L);
		assertThat(sequencesManager2.next("seq2")).isEqualTo(2L);
		assertThat(sequencesManager1.getLastSequenceValue("seq2")).isEqualTo(2L);
		assertThat(sequencesManager1.next("seq2")).isEqualTo(3L);
		assertThat(sequencesManager2.next("seq2")).isEqualTo(4L);
		assertThat(sequencesManager1.getLastSequenceValue("seq2")).isEqualTo(4L);
		sequencesManager1.set("seq2", 2L);
		assertThat(sequencesManager1.next("seq2")).isEqualTo(3L);
		assertThat(sequencesManager2.next("seq2")).isEqualTo(4L);

		assertThat(getSequenceDocument("seq1").getFieldValues("uuids_ss")).hasSize(1);
		assertThat(getSequenceDocument("seq1").getFieldValues("uuids_to_remove_ss")).hasSize(1);

		assertThat(getSequenceDocument("seq2").getFieldValues("uuids_ss")).hasSize(1);
		assertThat(getSequenceDocument("seq2").getFieldValues("uuids_to_remove_ss")).hasSize(1);

	}

	@Test
	// Confirm @SlowTest
	public void givenASystemHasFallenBetweenTheIncAndTheAddUUIDToRemoveStepThenUUIDRemovedWhenOneThousandAfterHim()
			throws Exception {

		String previousUUID = UUIDV1Generator.newRandomId();
		sequencesManager.createSequenceDocument("seq1", previousUUID);

		for (int i = 0; i < 999; i++) {
			System.out.println(i);
			assertThat(sequencesManager.next("seq1")).isEqualTo(2 + i);
			SolrDocument sequenceDocument = getSequenceDocument("seq1");
			assertThat(sequenceDocument.getFieldValues("uuids_ss")).hasSize(2 + i);
		}

		sequencesManager.next("seq1");
		SolrDocument sequenceDocument = getSequenceDocument("seq1");
		assertThat(sequenceDocument.getFieldValues("uuids_ss")).hasSize(1);

	}

	SolrDocument getSequenceDocument(String sequenceId)
			throws Exception {

		client.commit(true, true, true);

		ModifiableSolrParams solrParams = new ModifiableSolrParams();
		solrParams.set("q", "id:seq_" + sequenceId);

		QueryResponse queryResponse = client.query(solrParams);
		if (queryResponse.getResults().size() == 0) {
			return null;
		} else {
			return queryResponse.getResults().get(0);
		}
	}
}
