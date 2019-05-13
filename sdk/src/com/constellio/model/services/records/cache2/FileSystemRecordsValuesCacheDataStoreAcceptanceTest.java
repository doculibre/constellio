package com.constellio.model.services.records.cache2;

import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class FileSystemRecordsValuesCacheDataStoreAcceptanceTest extends ConstellioTest {

	@Test
	public void whenAddingAndUpdatingDataToCacheThenKept() {

		FileSystemRecordsValuesCacheDataStore dataStore = new FileSystemRecordsValuesCacheDataStore(
				new File(newTempFolder(), "test.db"));


		dataStore.saveIntKey(123, new byte[]{42, 34, 34, -1});
		dataStore.saveStringKey("bob", new byte[]{4, 2, 4, 2});

		assertThat(dataStore.loadStringKey("123")).isNull();
		assertThat(dataStore.loadIntKey(123)).isEqualTo(new byte[]{42, 34, 34, -1});
		assertThat(dataStore.loadStringKey("bob")).isEqualTo(new byte[]{4, 2, 4, 2});


		dataStore.saveIntKey(123, new byte[]{33, 22, 11, 99});
		dataStore.saveStringKey("bob", new byte[]{1, 2, 3, -4});


		assertThat(dataStore.loadStringKey("123")).isNull();
		assertThat(dataStore.loadIntKey(123)).isEqualTo(new byte[]{33, 22, 11, 99});
		assertThat(dataStore.loadStringKey("bob")).isEqualTo(new byte[]{1, 2, 3, -4});

		dataStore.removeIntKey(123);
		dataStore.removeStringKey("bob");

		assertThat(dataStore.loadStringKey("123")).isNull();
		assertThat(dataStore.loadIntKey(123)).isNull();
		assertThat(dataStore.loadStringKey("bob")).isNull();

	}

}
