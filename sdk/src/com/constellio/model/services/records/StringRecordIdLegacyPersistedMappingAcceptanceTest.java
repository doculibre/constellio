package com.constellio.model.services.records;

import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.data.dao.dto.records.StringRecordId;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class StringRecordIdLegacyPersistedMappingAcceptanceTest extends ConstellioTest {

	@Test
	public void whenInsertingIdsThenPersisted() {

		ConfigManager configManager = getDataLayerFactory().getConfigManager();

		AtomicInteger runnableCalled = new AtomicInteger();
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				runnableCalled.incrementAndGet();
			}
		};

		StringRecordIdLegacyPersistedMapping mapping = new StringRecordIdLegacyPersistedMapping(configManager, runnable);
		int mappedId1 = mapping.getIntId("anId");
		int mappedId2 = mapping.getIntId("anotherId");
		int mappedId3 = mapping.getIntId("thirdId");
		assertThat(mapping.getIntId("anId")).isEqualTo(mappedId1);
		assertThat(mapping.getIntId("anotherId")).isEqualTo(mappedId2);
		assertThat(mapping.getIntId("thirdId")).isEqualTo(mappedId3);

		StringRecordIdLegacyPersistedMapping mapping2 = new StringRecordIdLegacyPersistedMapping(configManager, runnable);
		assertThat(mapping2.getIntId("anId")).isEqualTo(mappedId1);
		assertThat(mapping2.getIntId("anotherId")).isEqualTo(mappedId2);
		assertThat(mapping2.getIntId("thirdId")).isEqualTo(mappedId3);

		assertThat(runnableCalled.get()).isEqualTo(0);

		assertThat(mapping2.getStringId(-666)).isEqualTo(StringRecordId.INVALID_ID);

		assertThat(runnableCalled.get()).isEqualTo(1);
	}

	@Test
	public void whenInsertingZZRecordsThenNotPersisted() {
		prepareSystem(withZeCollection());

		int mappedId1 = RecordId.toId("00000123456ZZ").intValue();
		int mappedId2 = RecordId.toId("00000234567ZZ").intValue();

		assertThat(mappedId1).isEqualTo(123456);
		assertThat(mappedId2).isEqualTo(234567);

		RecordId.toId("4fef2850-8907-11e8-a6e9-005056ab92ad").intValue();
	}
}
