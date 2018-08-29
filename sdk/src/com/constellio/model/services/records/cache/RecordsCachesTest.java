package com.constellio.model.services.records.cache;

import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;

public class RecordsCachesTest extends ConstellioTest {

	@Mock ModelLayerFactory modelLayerFactory;

	@Test
	public void whenGetRecordsCacheThenReturnSameInstance()
			throws Exception {

		RecordsCaches allCollectionsCaches = new RecordsCachesMemoryImpl(modelLayerFactory);

		RecordsCache collection1Cache = allCollectionsCaches.getCache("collection1");
		RecordsCache collection1CacheSecondCall = allCollectionsCaches.getCache("collection1");
		RecordsCache collection2Cache = allCollectionsCaches.getCache("collection2");
		RecordsCache collection2CacheSecondCall = allCollectionsCaches.getCache("collection2");

		assertThat(collection1Cache).isSameAs(collection1CacheSecondCall);
		assertThat(collection2Cache).isSameAs(collection2CacheSecondCall);
		assertThat(collection1Cache).isNotSameAs(collection2Cache);

	}
}
