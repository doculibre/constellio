/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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

		assertThat(zeroPaddedSequentialUniqueIdGenerator.next()).isEqualTo("00000000001");
		assertThat(zeroPaddedSequentialUniqueIdGenerator.next()).isEqualTo("00000000002");
		assertThat(zeroPaddedSequentialUniqueIdGenerator.next()).isEqualTo("00000000003");

	}

	@Test
	public void givenZeroPaddedSequentialWithCustomBatchSizeThenOK()
			throws Exception {

		ZeroPaddedSequentialUniqueIdGenerator generator1 = new ZeroPaddedSequentialUniqueIdGenerator(
				getDataLayerFactory().getConfigManager(), 3);
		ZeroPaddedSequentialUniqueIdGenerator generator2 = new ZeroPaddedSequentialUniqueIdGenerator(
				getDataLayerFactory().getConfigManager(), 3);

		assertThat(generator1.next()).isEqualTo("00000000001");
		assertThat(generator2.next()).isEqualTo("00000000004");
		assertThat(generator1.next()).isEqualTo("00000000002");
		assertThat(generator2.next()).isEqualTo("00000000005");
		assertThat(generator1.next()).isEqualTo("00000000003");
		assertThat(generator2.next()).isEqualTo("00000000006");
		assertThat(generator2.next()).isEqualTo("00000000007");
		assertThat(generator1.next()).isEqualTo("00000000010");

	}
}
