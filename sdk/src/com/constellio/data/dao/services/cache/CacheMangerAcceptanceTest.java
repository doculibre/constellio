package com.constellio.data.dao.services.cache;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;

public class CacheMangerAcceptanceTest extends ConstellioTest {
	private IgniteConfigManager manager;

	public CacheMangerAcceptanceTest() {
	}
	
	@Before
	public void setup() {
		manager = getDataLayerFactory().getCacheConfigManager();
	}

	@Test
	public void insertDataInCacheThenClear() {
		manager.getManagerCache().put("key", "value");
		manager.getRecordCache().put("key", "value");

		getCurrentTestSession().getFactoriesTestFeatures().afterTest();

		String valueManager = (String) manager.getManagerCache().get("key");
		String valueRecord = (String) manager.getRecordCache().get("key");

		assertThat(valueManager).isNull();
		assertThat(valueRecord).isNull();
	}

}
