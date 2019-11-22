package com.constellio.data.dao.services.idGenerator;

import com.constellio.model.services.records.RecordId;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class UUIDV1GeneratorAcceptTest {

	@Test
	public void whenGeneratingNewIdThenAlwaysDifferentAndThreadSafe()
			throws InterruptedException {

		final Set<String> synchronizedSet = java.util.Collections.synchronizedSet(new HashSet<String>());

		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < 10; i++) {
			threads.add(new Thread() {
				@Override
				public void run() {
					List<String> values = new ArrayList<String>();

					for (int i = 0; i < 1000; i++) {
						values.add(UUIDV1Generator.newRandomId());
					}

					synchronizedSet.addAll(values);
				}
			});
		}

		for (Thread t : threads) {
			t.start();
		}

		for (Thread t : threads) {
			t.join();
		}

		assertEquals(10000, synchronizedSet.size());
	}

	@Test
	public void whenGeneratingUUIDRecordIdsThenNoIntValue()
			throws InterruptedException {

		assertThat(RecordId.toIntId(UUIDV1Generator.newRandomId())).isEqualTo(0);

		for (int i = 0; i < 1_000_000; i++) {
			assertThat(RecordId.toIntId(UUIDV1Generator.newRandomId())).isEqualTo(0);
		}

	}


}
