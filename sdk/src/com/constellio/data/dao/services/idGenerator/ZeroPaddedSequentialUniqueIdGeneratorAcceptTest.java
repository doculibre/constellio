package com.constellio.data.dao.services.idGenerator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.constellio.data.utils.ThreadList;
import com.constellio.sdk.tests.ConstellioTest;

public class ZeroPaddedSequentialUniqueIdGeneratorAcceptTest extends ConstellioTest {

	ZeroPaddedSequentialUniqueIdGenerator zeroPaddedSequentialUniqueIdGenerator;

	@Before
	public void setUp()
			throws Exception {
		notAUnitItest = true;
		zeroPaddedSequentialUniqueIdGenerator = new ZeroPaddedSequentialUniqueIdGenerator(
				getDataLayerFactory().getConfigManager(),
				10);
	}

	@Test
	public void givenMultipleThreadsWitDifferentGeneratorThenAlwaysUniqueIds()
			throws Exception {

		final Set<String> concurrentSet = Collections.synchronizedSet(new HashSet<String>());
		final List<String> concurrentList = Collections.synchronizedList(new ArrayList<String>());

		ThreadList<Thread> threads = new ThreadList<>();

		for (int i = 0; i < 154; i++) {
			threads.add(new Thread() {
				@Override
				public void run() {
					ZeroPaddedSequentialUniqueIdGenerator generator = new ZeroPaddedSequentialUniqueIdGenerator(
							getDataLayerFactory().getConfigManager());
					Set<String> ids = new HashSet<String>();
					for (int j = 0; j < 1000; j++) {
						ids.add(generator.next());
					}
					concurrentSet.addAll(ids);
					concurrentList.addAll(ids);
				}
			});
		}

		threads.startAll();
		threads.joinAll();

		System.out.println(concurrentList.size());
		assertThat(concurrentSet).hasSize(154000);
	}

	@Test
	public void givenMultipleThreadsWitSameGeneratorThenAlwaysUniqueIds()
			throws Exception {

		final Set<String> concurrentSet = Collections.synchronizedSet(new HashSet<String>());

		ThreadList<Thread> threads = new ThreadList<>();

		for (int i = 0; i < 10; i++) {
			threads.add(new Thread() {
				@Override
				public void run() {
					Set<String> ids = new HashSet<String>();
					for (int j = 0; j < 1000; j++) {
						ids.add(zeroPaddedSequentialUniqueIdGenerator.next());
					}
					concurrentSet.addAll(ids);
				}
			});
		}

		threads.startAll();
		threads.joinAll();

		assertThat(concurrentSet).hasSize(10000);

	}

	@Test
	public void whenGetIdsThenZeroPadded()
			throws Exception {

		assertThat(zeroPaddedSequentialUniqueIdGenerator.next()).isEqualTo("00000001001");
		assertThat(zeroPaddedSequentialUniqueIdGenerator.next()).isEqualTo("00000001002");
		assertThat(zeroPaddedSequentialUniqueIdGenerator.next()).isEqualTo("00000001003");

	}

	@Test
	public void givenZeroPaddedSequentialWithCustomBatchSizeThenOK()
			throws Exception {

		ZeroPaddedSequentialUniqueIdGenerator generator1 = new ZeroPaddedSequentialUniqueIdGenerator(
				getDataLayerFactory().getConfigManager(), 3);
		ZeroPaddedSequentialUniqueIdGenerator generator2 = new ZeroPaddedSequentialUniqueIdGenerator(
				getDataLayerFactory().getConfigManager(), 3);

		assertThat(generator1.next()).isEqualTo("00000001001");
		assertThat(generator2.next()).isEqualTo("00000001004");
		assertThat(generator1.next()).isEqualTo("00000001002");
		assertThat(generator2.next()).isEqualTo("00000001005");
		assertThat(generator1.next()).isEqualTo("00000001003");
		assertThat(generator2.next()).isEqualTo("00000001006");
		assertThat(generator2.next()).isEqualTo("00000001007");
		assertThat(generator1.next()).isEqualTo("00000001010");

	}
}
