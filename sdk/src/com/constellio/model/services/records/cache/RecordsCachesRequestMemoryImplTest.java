package com.constellio.model.services.records.cache;

import com.constellio.model.entities.records.Record;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class RecordsCachesRequestMemoryImplTest extends ConstellioTest {

	@Mock RecordsCaches nestedCache;
	@Mock RecordsCache nestedZeCollectionCache;
	@Mock ModelLayerFactory modelLayerFactory;

	@Mock Record zeRecord;

	@Before
	public void setUp()
			throws Exception {
		when(modelLayerFactory.getBottomRecordsCaches()).thenReturn(nestedCache);
		when(nestedCache.getCache(zeCollection)).thenReturn(nestedZeCollectionCache);
	}

	@Test
	public void givenEmptyCacheWhenGetRecordByIdOnASpecificCollectionThenCallNestedCache()
			throws Exception {

		when(nestedZeCollectionCache.get("zeRecord")).thenReturn(zeRecord);

		RecordsCachesRequestMemoryImpl recordsCachesRequest = new RecordsCachesRequestMemoryImpl(modelLayerFactory, "zeCache");
		assertThat(recordsCachesRequest.getCache(zeCollection).get("zeRecord")).isEqualTo(zeRecord);

	}

	@Test
	public void givenEmptyCacheWhenGetRecordByIdOnAllCollectionThenCallNestedCache()
			throws Exception {

		when(nestedCache.getRecord("zeRecord", null, null)).thenReturn(zeRecord);

		RecordsCachesRequestMemoryImpl recordsCachesRequest = new RecordsCachesRequestMemoryImpl(modelLayerFactory, "zeCache");
		assertThat(recordsCachesRequest.getRecord("zeRecord")).isEqualTo(zeRecord);

	}
}
