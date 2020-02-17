package com.constellio.data.dao.services.idGenerator;

import com.constellio.model.conf.PropertiesModelLayerConfiguration.InMemoryModelLayerConfiguration;
import com.constellio.model.services.records.RecordId;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.ModelLayerConfigurationAlteration;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class UUIDV1GeneratorAcceptTest extends ConstellioTest {

	@Before
	public void setUp() throws Exception {
		configure(new ModelLayerConfigurationAlteration() {
			@Override
			public void alter(InMemoryModelLayerConfiguration configuration) {
				configuration.setPersistStringRecordIdLegacyMapping(false);
			}
		});
	}

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

		getModelLayerFactory();

		Map<Integer, String> mapping = new HashMap<>();

		for (int i = 0; i < 100_000; i++) {

			String stringId = UUIDV1Generator.newRandomId();
			int intId = RecordId.toIntId(stringId);


			mapping.put(intId, stringId);
		}

		assertThat(mapping).hasSize(100_000);

		for (Map.Entry<Integer, String> entry : mapping.entrySet()) {
			assertThat(RecordId.id(entry.getKey()).stringValue()).isEqualTo(entry.getValue());
		}

	}


}
