package com.constellio.data.dao.services.sequence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import com.constellio.data.utils.ThreadList;
import com.constellio.sdk.tests.ConstellioTest;

public class SolrSequenceManagerAcceptTest extends ConstellioTest {

	private static final String CONFIG_PATH = "/sequence.properties";

	SequencesManager sequencesManager;

	@Before
	public void setUp()
			throws Exception {
		notAUnitItest = true;
		sequencesManager = getDataLayerFactory().getSequencesManager();
	}

	@Test
	public void givenMultipleThreadsWitDifferentGeneratorThenAlwaysUniqueIds1()
			throws Exception {
		givenMultipleThreadsWitDifferentGeneratorThenAlwaysUniqueIds();
	}

	@Test
	public void givenMultipleThreadsWitDifferentGeneratorThenAlwaysUniqueIds2()
			throws Exception {
		givenMultipleThreadsWitDifferentGeneratorThenAlwaysUniqueIds();
	}

	@Test
	public void givenMultipleThreadsWitDifferentGeneratorThenAlwaysUniqueIds3()
			throws Exception {
		givenMultipleThreadsWitDifferentGeneratorThenAlwaysUniqueIds();
	}

	@Test
	public void givenMultipleThreadsWitDifferentGeneratorThenAlwaysUniqueIds4()
			throws Exception {
		givenMultipleThreadsWitDifferentGeneratorThenAlwaysUniqueIds();
	}

	@Test
	public void givenMultipleThreadsWitDifferentGeneratorThenAlwaysUniqueIds5()
			throws Exception {
		givenMultipleThreadsWitDifferentGeneratorThenAlwaysUniqueIds();
	}

	@Test
	public void givenMultipleThreadsWitDifferentGeneratorThenAlwaysUniqueIds6()
			throws Exception {
		givenMultipleThreadsWitDifferentGeneratorThenAlwaysUniqueIds();
	}

	@Test
	public void givenMultipleThreadsWitDifferentGeneratorThenAlwaysUniqueIds7()
			throws Exception {
		givenMultipleThreadsWitDifferentGeneratorThenAlwaysUniqueIds();
	}

	@Test
	public void givenMultipleThreadsWitDifferentGeneratorThenAlwaysUniqueIds8()
			throws Exception {
		givenMultipleThreadsWitDifferentGeneratorThenAlwaysUniqueIds();
	}

	@Test
	public void givenMultipleThreadsWitDifferentGeneratorThenAlwaysUniqueIds9()
			throws Exception {
		givenMultipleThreadsWitDifferentGeneratorThenAlwaysUniqueIds();
	}

	@Test
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
					SequencesManager sequencesManager = new SolrSequencesManager(getDataLayerFactory().newRecordDao());
					List<Long> ids = new ArrayList<Long>();
					for (int j = 0; j < 2000; j++) {
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

		while (total.get() < 200000) {
			System.out.println(total.get());
			Thread.sleep(2000);
		}

		threads.joinAll();

		assertThat(concurrentList.size()).isEqualTo(200000);
		assertThat(concurrentSet.size()).isEqualTo(200000);
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
	public void givenZeroPaddedSequentialWithCustomBatchSizeThenOK()
			throws Exception {

		SequencesManager sequencesManager1 = new SolrSequencesManager(getDataLayerFactory().newRecordDao());
		SequencesManager sequencesManager2 = new SolrSequencesManager(getDataLayerFactory().newRecordDao());

		assertThat(sequencesManager1.next("seq1")).isEqualTo(1L);
		assertThat(sequencesManager2.next("seq1")).isEqualTo(2L);
		assertThat(sequencesManager1.next("seq1")).isEqualTo(3L);
		assertThat(sequencesManager2.next("seq1")).isEqualTo(4L);
		assertThat(sequencesManager1.next("seq1")).isEqualTo(5L);
		assertThat(sequencesManager2.next("seq1")).isEqualTo(6L);

		assertThat(sequencesManager1.next("seq2")).isEqualTo(1L);
		assertThat(sequencesManager2.next("seq2")).isEqualTo(2L);
		assertThat(sequencesManager1.next("seq2")).isEqualTo(3L);
		assertThat(sequencesManager2.next("seq2")).isEqualTo(4L);
		assertThat(sequencesManager1.next("seq2")).isEqualTo(5L);
		assertThat(sequencesManager2.next("seq2")).isEqualTo(6L);

	}
}
