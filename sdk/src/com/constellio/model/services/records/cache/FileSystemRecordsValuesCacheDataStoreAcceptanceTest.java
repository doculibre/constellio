package com.constellio.model.services.records.cache;

import com.constellio.model.services.records.cache.dataStore.FileSystemRecordsValuesCacheDataStore;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class FileSystemRecordsValuesCacheDataStoreAcceptanceTest extends ConstellioTest {

	FileSystemRecordsValuesCacheDataStore dataStore;

	@Before
	public void setUp() throws Exception {
		dataStore = new FileSystemRecordsValuesCacheDataStore(new File(newTempFolder(), "test.db"));
	}

	@After
	public void tearDown() throws Exception {
		dataStore.close();
	}

	@Test
	public void whenAddingAndUpdatingDataToCacheThenKept() {


		dataStore.saveIntKey(123, new byte[]{42, 34, 34, -1});
		dataStore.saveStringKey("bob", new byte[]{4, 2, 4, 2});

		try {
			assertThat(dataStore.loadStringKey("123")).isNull();
			//Null or exception expected
		} catch (IllegalStateException e) {
			//OK
		}
		assertThat(dataStore.loadIntKey(123)).isEqualTo(new byte[]{42, 34, 34, -1});
		assertThat(dataStore.loadStringKey("bob")).isEqualTo(new byte[]{4, 2, 4, 2});


		dataStore.saveIntKey(123, new byte[]{33, 22, 11, 99});
		dataStore.saveStringKey("bob", new byte[]{1, 2, 3, -4});

		try {
			assertThat(dataStore.loadStringKey("123")).isNull();
			//Null or exception expected
		} catch (IllegalStateException e) {
			//OK
		}
		assertThat(dataStore.loadIntKey(123)).isEqualTo(new byte[]{33, 22, 11, 99});
		assertThat(dataStore.loadStringKey("bob")).isEqualTo(new byte[]{1, 2, 3, -4});

		dataStore.removeIntKey(123);
		dataStore.removeStringKey("bob");

		try {
			assertThat(dataStore.loadStringKey("123")).isNull();
			//Null or exception expected
		} catch (IllegalStateException e) {
			//OK
		}
		try {
			assertThat(dataStore.loadIntKey(123)).isNull();
			//Null or exception expected
		} catch (IllegalStateException e) {
			//OK
		}
		try {
			assertThat(dataStore.loadStringKey("bob")).isNull();
			//Null or exception expected
		} catch (IllegalStateException e) {
			//OK
		}

	}


}
