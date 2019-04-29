package com.constellio.model.services.records.cache2;

import com.constellio.sdk.tests.ConstellioTest;
import org.apache.commons.lang3.StringUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class RecordsCachesDataStoreAcceptanceTest extends ConstellioTest {

	MemoryEfficientRecordsCachesDataStore dataStore = new MemoryEfficientRecordsCachesDataStore();

	//@Test
	public void whenInsertingRecordsThenAllAccessiblesByTheirId() {

		for (int id = 1; id < 80000000; id += 8) {
			//dataStore.put(id, new ByteArrayRecordDTO(id, id + 42));
		}

		for (int id = 1; id < 80000000; id += 8) {
			assertThat(((ByteArrayRecordDTO) dataStore.get(id)).getId()).isEqualTo(StringUtils.leftPad("" + id, 11, '0'));
			assertThat(((ByteArrayRecordDTO) dataStore.get(id)).getVersion()).isEqualTo(id + 42);
			assertThat(dataStore.get(id + 1)).isNull();
			assertThat(dataStore.get(id + 2)).isNull();
			assertThat(dataStore.get(id + 3)).isNull();
			assertThat(dataStore.get(id + 4)).isNull();
			assertThat(dataStore.get(id + 5)).isNull();
			assertThat(dataStore.get(id + 6)).isNull();
			assertThat(dataStore.get(id + 7)).isNull();

		}
	}


}
