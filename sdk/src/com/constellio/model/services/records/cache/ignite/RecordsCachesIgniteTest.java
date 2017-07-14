package com.constellio.model.services.records.cache.ignite;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.data.conf.CacheType;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.services.cache.ignite.ConstellioIgniteCacheManager;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache.RecordsCache;
import com.constellio.model.services.records.cache.RecordsCaches;
import com.constellio.sdk.tests.ConstellioTest;

public class RecordsCachesIgniteTest extends ConstellioTest {
	
	@Mock DataLayerConfiguration dataLayerConfiguration;

	@Mock ModelLayerFactory modelLayerFactory;
	
	@Mock DataLayerFactory dataLayerFactory;
	
	ConstellioIgniteCacheManager cacheManager;

	@Before
	public void setUp()
			throws Exception {
		when(dataLayerConfiguration.getCacheType()).thenReturn(CacheType.IGNITE);
		cacheManager = new ConstellioIgniteCacheManager("localhost:47500");
		
		when(modelLayerFactory.getDataLayerFactory()).thenReturn(dataLayerFactory);
		when(dataLayerFactory.getRecordsCacheManager()).thenReturn(cacheManager);
		when(dataLayerFactory.getDataLayerConfiguration()).thenReturn(dataLayerConfiguration);
		
		cacheManager.initialize();
	}

	@Test
	public void whenGetRecordsCacheThenReturnSameInstance()
			throws Exception {

		RecordsCaches allCollectionsCaches = new RecordsCachesIgniteImpl(modelLayerFactory);

		RecordsCache collection1Cache = allCollectionsCaches.getCache("collection1");
		RecordsCache collection1CacheSecondCall = allCollectionsCaches.getCache("collection1");
		RecordsCache collection2Cache = allCollectionsCaches.getCache("collection2");
		RecordsCache collection2CacheSecondCall = allCollectionsCaches.getCache("collection2");

		assertThat(collection1Cache).isSameAs(collection1CacheSecondCall);
		assertThat(collection2Cache).isSameAs(collection2CacheSecondCall);
		assertThat(collection1Cache).isNotSameAs(collection2Cache);

	}
}
