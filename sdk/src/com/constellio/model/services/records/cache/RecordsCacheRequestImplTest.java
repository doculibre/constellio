package com.constellio.model.services.records.cache;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RecordsCacheRequestImplTest extends ConstellioTest {

	String record1Id = "record1Id";
	String record2Id = "record2Id";
	String record3Id = "record3Id";

	String record1Code = "record1Code";
	String record2Code = "record2Code";
	String record3Code = "record3Code";

	@Mock Record record1;
	@Mock Record record2;
	@Mock Record record3;

	//	@Mock Record copyOfRecord1;
	//	@Mock Record copyOfRecord2;
	//	@Mock Record volatileRecord3;

	Metadata codeMetadata;
	Metadata titleMetadata;

	@Mock RecordsCache nestedCache;

	RecordsCacheRequestImpl cache;

	@Before
	public void setUp()
			throws Exception {
		cache = new RecordsCacheRequestImpl("zeCache", nestedCache);
		when(nestedCache.get(record1Id)).thenReturn(record1);
		when(nestedCache.get(record2Id)).thenReturn(record2);
		when(nestedCache.get(record3Id)).thenReturn(record3);

		when(record1.getCopyOfOriginalRecord()).thenReturn(record1);
		when(record2.getCopyOfOriginalRecord()).thenReturn(record2);
		when(record3.getCopyOfOriginalRecord()).thenReturn(record3);

		when(record1.getId()).thenReturn(record1Id);
		when(record2.getId()).thenReturn(record2Id);
		when(record3.getId()).thenReturn(record3Id);

		when(record1.getTypeCode()).thenReturn("zeType");
		when(record2.getTypeCode()).thenReturn("zeType");
		when(record3.getTypeCode()).thenReturn("zeType");
		when(record1.getSchemaCode()).thenReturn("zeType_default");
		when(record2.getSchemaCode()).thenReturn("zeType_default");
		when(record3.getSchemaCode()).thenReturn("zeType_default");

		when(record1.isSaved()).thenReturn(true);
		when(record2.isSaved()).thenReturn(true);
		when(record3.isSaved()).thenReturn(true);

		when(record1.isFullyLoaded()).thenReturn(true);
		when(record2.isFullyLoaded()).thenReturn(true);
		when(record3.isFullyLoaded()).thenReturn(true);

		codeMetadata = TestUtils.mockMetadata("zeType_default_code");
		titleMetadata = TestUtils.mockMetadata("zeType_default_title");

		when(record1.get(codeMetadata)).thenReturn(record1Code);
		when(record2.get(codeMetadata)).thenReturn(record2Code);
		when(record3.get(codeMetadata)).thenReturn(record3Code);

		when(nestedCache.getByMetadata(codeMetadata, record1Code)).thenReturn(record1);
		when(nestedCache.getByMetadata(codeMetadata, record2Code)).thenReturn(record2);
		when(nestedCache.getByMetadata(codeMetadata, record3Code)).thenReturn(record3);
	}

	@Test
	public void whenSameGetByIdCalledTwoTimeThenOnlyGettedOnceFromNestedCache()
			throws Exception {

		assertThat(cache.get(record1Id)).isEqualTo(record1);
		assertThat(cache.get(record1Id)).isEqualTo(record1);

		verify(nestedCache, times(1)).get(record1Id);

	}

	@Test
	public void whenVariousGetByIdThenOnlyGettedOnceFromNestedCache()
			throws Exception {

		assertThat(cache.get(record1Id)).isEqualTo(record1);
		assertThat(cache.get(record2Id)).isEqualTo(record2);
		assertThat(cache.get("inexistentRecord")).isNull();
		assertThat(cache.get(record2Id)).isEqualTo(record2);
		assertThat(cache.get("inexistentRecord")).isNull();
		assertThat(cache.get(record3Id)).isEqualTo(record3);
		assertThat(cache.get(record3Id)).isEqualTo(record3);

		InOrder inOrder = inOrder(nestedCache);
		inOrder.verify(nestedCache).get(record1Id);
		inOrder.verify(nestedCache).get(record2Id);
		inOrder.verify(nestedCache, times(2)).get("inexistentRecord");
		inOrder.verify(nestedCache).get(record3Id);
		inOrder.verifyNoMoreInteractions();

	}

	@Test
	public void whenGetSameRecordByMetadataTwiceThenOnlyGettedOnceFromNestedCache()
			throws Exception {

		assertThat(cache.getByMetadata(codeMetadata, record1Code)).isEqualTo(record1);
		assertThat(cache.getByMetadata(codeMetadata, record1Code)).isEqualTo(record1);

		verify(nestedCache, times(1)).getByMetadata(codeMetadata, record1Code);

	}

	@Test
	public void whenVariousGetSameRecordByMetadataThenOnlyGettedOnceFromNestedCache()
			throws Exception {

		assertThat(cache.getByMetadata(codeMetadata, record1Code)).isEqualTo(record1);
		assertThat(cache.getByMetadata(codeMetadata, record2Code)).isEqualTo(record2);
		assertThat(cache.getByMetadata(codeMetadata, "inexistentRecord")).isNull();
		assertThat(cache.getByMetadata(codeMetadata, record2Code)).isEqualTo(record2);
		assertThat(cache.getByMetadata(codeMetadata, "inexistentRecord")).isNull();
		assertThat(cache.getByMetadata(titleMetadata, record3Code)).isNull();
		assertThat(cache.getByMetadata(codeMetadata, record3Code)).isEqualTo(record3);
		assertThat(cache.getByMetadata(codeMetadata, record3Code)).isEqualTo(record3);

		InOrder inOrder = inOrder(nestedCache);
		inOrder.verify(nestedCache).getByMetadata(codeMetadata, record1Code);
		inOrder.verify(nestedCache).getByMetadata(codeMetadata, record2Code);
		inOrder.verify(nestedCache, times(2)).getByMetadata(codeMetadata, "inexistentRecord");
		inOrder.verify(nestedCache).getByMetadata(titleMetadata, record3Code);
		inOrder.verify(nestedCache).getByMetadata(codeMetadata, record3Code);
		inOrder.verifyNoMoreInteractions();

	}

	//	@Test
	//	public void whenInvalidatingAllThenRetrieveFromNestedCacheNextTime()
	//			throws Exception {
	//
	//		assertThat(cache.getByMetadata(codeMetadata, record1Code)).isEqualTo(record1);
	//		assertThat(cache.get(record2Id)).isEqualTo(record2);
	//		assertThat(cache.getByMetadata(codeMetadata, "inexistentRecord")).isNull();
	//
	//		cache.inva();
	//
	//		assertThat(cache.getByMetadata(codeMetadata, record1Code)).isEqualTo(record1);
	//		assertThat(cache.get(record2Id)).isEqualTo(record2);
	//		assertThat(cache.getByMetadata(codeMetadata, "inexistentRecord")).isNull();
	//
	//		InOrder inOrder = inOrder(nestedCache);
	//		inOrder.verify(nestedCache).getByMetadata(codeMetadata, record1Code);
	//		inOrder.verify(nestedCache).get(record2Id);
	//		inOrder.verify(nestedCache).getByMetadata(codeMetadata, "inexistentRecord");
	//		inOrder.verify(nestedCache).getByMetadata(codeMetadata, record1Code);
	//		inOrder.verify(nestedCache).get(record2Id);
	//		inOrder.verify(nestedCache).getByMetadata(codeMetadata, "inexistentRecord");
	//		inOrder.verifyNoMoreInteractions();
	//
	//	}

	@Test
	public void whenInvalidatingSameSchemaTypeThenRetrieveFromNestedCacheNextTime()
			throws Exception {

		assertThat(cache.getByMetadata(codeMetadata, record1Code)).isEqualTo(record1);
		assertThat(cache.get(record2Id)).isEqualTo(record2);
		assertThat(cache.getByMetadata(codeMetadata, "inexistentRecord")).isNull();

		cache.reloadSchemaType("zeType");

		assertThat(cache.getByMetadata(codeMetadata, record1Code)).isEqualTo(record1);
		assertThat(cache.get(record2Id)).isEqualTo(record2);
		assertThat(cache.getByMetadata(codeMetadata, "inexistentRecord")).isNull();

		InOrder inOrder = inOrder(nestedCache);
		inOrder.verify(nestedCache).getByMetadata(codeMetadata, record1Code);
		inOrder.verify(nestedCache).get(record2Id);
		inOrder.verify(nestedCache).getByMetadata(codeMetadata, "inexistentRecord");
		inOrder.verify(nestedCache).reloadSchemaType("zeType");
		inOrder.verify(nestedCache).getByMetadata(codeMetadata, record1Code);
		inOrder.verify(nestedCache).get(record2Id);
		inOrder.verify(nestedCache).getByMetadata(codeMetadata, "inexistentRecord");
		inOrder.verifyNoMoreInteractions();

	}

	@Test
	public void whenInvalidatingOtherSchemaTypeThenNotRetrievedFromNestedCacheNextTime()
			throws Exception {

		assertThat(cache.getByMetadata(codeMetadata, record1Code)).isEqualTo(record1);
		assertThat(cache.get(record2Id)).isEqualTo(record2);
		assertThat(cache.getByMetadata(codeMetadata, "inexistentRecord")).isNull();

		cache.reloadSchemaType("otherType");
		assertThat(cache.getByMetadata(codeMetadata, record1Code)).isEqualTo(record1);
		//	assertThat(cache.get(record2Id)).isEqualTo(permanentRecord2);

		InOrder inOrder = inOrder(nestedCache);

		inOrder.verify(nestedCache).getByMetadata(codeMetadata, record1Code);
		inOrder.verify(nestedCache).get(record2Id);
		inOrder.verify(nestedCache).getByMetadata(codeMetadata, "inexistentRecord");
		inOrder.verify(nestedCache).reloadSchemaType("otherType");
		inOrder.verifyNoMoreInteractions();

	}

	@Test
	public void whenInvalidatingRecordThenRetrieveFromNestedCacheNextTime()
			throws Exception {

		assertThat(cache.getByMetadata(codeMetadata, record1Code)).isEqualTo(record1);
		assertThat(cache.get(record2Id)).isEqualTo(record2);
		assertThat(cache.get(record3Id)).isEqualTo(record3);
		assertThat(cache.getByMetadata(codeMetadata, "inexistentRecord")).isNull();

		cache.removeFromAllCaches(record1Id);
		cache.removeFromAllCaches(record2Id);

		assertThat(cache.getByMetadata(codeMetadata, record1Code)).isEqualTo(record1);
		assertThat(cache.get(record2Id)).isEqualTo(record2);
		assertThat(cache.get(record3Id)).isEqualTo(record3);
		assertThat(cache.getByMetadata(codeMetadata, "inexistentRecord")).isNull();

		InOrder inOrder = inOrder(nestedCache);
		inOrder.verify(nestedCache).getByMetadata(codeMetadata, record1Code);
		inOrder.verify(nestedCache).get(record2Id);
		inOrder.verify(nestedCache).get(record3Id);
		inOrder.verify(nestedCache).getByMetadata(codeMetadata, "inexistentRecord");
		inOrder.verify(nestedCache).getByMetadata(codeMetadata, record1Code);
		inOrder.verify(nestedCache).get(record2Id);
		inOrder.verify(nestedCache).getByMetadata(codeMetadata, "inexistentRecord");
		inOrder.verifyNoMoreInteractions();

	}

}
