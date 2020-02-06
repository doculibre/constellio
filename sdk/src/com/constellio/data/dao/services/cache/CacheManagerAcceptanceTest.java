package com.constellio.data.dao.services.cache;

import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CacheManagerAcceptanceTest extends ConstellioTest {

	private ConstellioCacheManager settingsCacheManager;

	private ConstellioCacheManager recordsCacheManager;

	public CacheManagerAcceptanceTest() {
	}

	@Before
	public void setup() {
		// May or may not be Ignite
		settingsCacheManager = getDataLayerFactory().getLocalCacheManager();
		recordsCacheManager = getDataLayerFactory().getDistributedCacheManager();
	}

	private void insertDataInCacheThenClear(ConstellioCacheManager cacheManager) {
		ConstellioCache cache1 = cacheManager.getCache("cache1");
		ConstellioCache cache2 = cacheManager.getCache("cache2");

		cache1.put("key", "value1", InsertionReason.WAS_OBTAINED);
		cache2.put("key", "value2", InsertionReason.WAS_OBTAINED);

		String valueCache1 = (String) cacheManager.getCache("cache1").get("key");
		String valueCache2 = (String) cacheManager.getCache("cache2").get("key");

		assertThat(valueCache1).isEqualTo("value1");
		assertThat(valueCache2).isEqualTo("value2");

		cache1.put("key", "value1b", InsertionReason.WAS_MODIFIED);
		cache2.put("key", "value2b", InsertionReason.WAS_MODIFIED);

		valueCache1 = (String) cacheManager.getCache("cache1").get("key");
		valueCache2 = (String) cacheManager.getCache("cache2").get("key");

		assertThat(valueCache1).isEqualTo("value1b");
		assertThat(valueCache2).isEqualTo("value2b");

		for (Runnable runnable : getCurrentTestSession().getFactoriesTestFeatures().afterTest(false)) {
			runnable.run();
		}

		valueCache1 = (String) cacheManager.getCache("cache1").get("key");
		valueCache2 = (String) cacheManager.getCache("cache2").get("key");

		assertThat(valueCache1).isNull();
		assertThat(valueCache2).isNull();
	}

	@Test
	public void insertDataInSettingsCacheThenClear() {
		insertDataInCacheThenClear(settingsCacheManager);
	}

	@Test
	public void insertDataInRecordsCacheThenClear() {
		insertDataInCacheThenClear(recordsCacheManager);
	}

}
