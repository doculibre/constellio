package com.constellio.model.services.records.cache;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.ModelLayerSystemExtensions;
import com.constellio.model.services.extensions.ModelLayerExtensions;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordImplRuntimeException.RecordImplException_RecordIsUnmodifiable;
import com.constellio.model.services.records.cache.RecordsCacheImpl.RecordHolder;
import com.constellio.model.services.records.cache.RecordsCacheImplRuntimeException.RecordsCacheImplRuntimeException_InvalidSchemaTypeCode;
import com.constellio.model.services.search.entities.SearchBoost;
import com.constellio.model.services.search.query.ResultsProjection;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.SlowTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.data.dao.services.cache.InsertionReason.WAS_MODIFIED;
import static com.constellio.model.services.records.cache.VolatileCacheInvalidationMethod.FIFO;
import static com.constellio.sdk.tests.TestUtils.mockManualMetadata;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RecordsCacheImplTest extends ConstellioTest {

	@Mock ModelLayerSystemExtensions systemExtensions;
	@Mock ModelLayerExtensions extensions;

	@Mock User user;
	@Mock SearchBoost searchBoost;
	@Mock ResultsProjection resultsProjection;
	@Mock ModelLayerFactory modelLayerFactory;

	boolean givenDisabledRecordDuplications = false;
	Metadata zeTypeCodeMetadata, anotherTypeCodeMetadata, anotherTypeLegacyIdMetadata;

	List<Metadata> withoutIndexByMetadata = new ArrayList<>();
	String zeType = "zeType";
	String anotherType = "anotherType";

	RecordsCacheImpl cache;

	@Before
	public void setUp()
			throws Exception {
		cache = new RecordsCacheImpl(zeCollection, modelLayerFactory);
		zeTypeCodeMetadata = mockManualMetadata("zeType_default_code", MetadataValueType.STRING);
		anotherTypeCodeMetadata = mockManualMetadata("anotherType_default_code", MetadataValueType.STRING);
		anotherTypeLegacyIdMetadata = mockManualMetadata("anotherType_default_legacyId", MetadataValueType.STRING);

		when(modelLayerFactory.getExtensions()).thenReturn(extensions);
		when(extensions.getSystemWideExtensions()).thenReturn(systemExtensions);
	}

	@Test
	public void whenInsertingAPrevisoulyAddedRecordInAFullVolatileCacheThenInserted()
			throws Exception {
		cache.configureCache(CacheConfig.volatileCache(zeType, 3, withoutIndexByMetadata));

		Record record1 = newRecord(zeType, 1);
		cache.insert(record1, WAS_MODIFIED);

		Record record2 = newRecord(zeType, 2);
		cache.insert(record2, WAS_MODIFIED);

		Record record3 = newRecord(zeType, 3);
		cache.insert(record3, WAS_MODIFIED);

		Record record4 = newRecord(zeType, 4);
		cache.insert(record4, WAS_MODIFIED);

		Record record5 = newRecord(zeType, 5);
		cache.insert(record5, WAS_MODIFIED);

		cache.insert(record1, WAS_MODIFIED);
		cache.insert(record2, WAS_MODIFIED);
		cache.insert(record3, WAS_MODIFIED);
		cache.insert(record4, WAS_MODIFIED);
		cache.insert(record5, WAS_MODIFIED);

		assertThatRecords("3", "4", "5").areInCache();
		assertThatRecords("1", "2").areNotInCache();

		cache.insert(record1, WAS_MODIFIED);

		assertThatRecords("1", "4", "5").areInCache();
		assertThatRecords("2", "3").areNotInCache();
		assertThat(cache.get("1")).isNotNull();
	}

	@Test
	public void whenConfigureCachedTypeThenConfigsSavedInCache()
			throws Exception {

		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, withoutIndexByMetadata));
		cache.configureCache(CacheConfig.volatileCache(anotherType, 100, withoutIndexByMetadata));

		assertThat(cache.getConfiguredCaches()).containsOnly(
				CacheConfig.permanentCacheNotLoadedInitially(zeType, withoutIndexByMetadata),
				CacheConfig.volatileCache(anotherType, 100, withoutIndexByMetadata)
		);

	}

	@Test(expected = RecordsCacheImplRuntimeException_InvalidSchemaTypeCode.class)
	public void whenConfigureCacheUsingAnInvalidTypeThenIllegalArgumentException()
			throws Exception {

		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially("zeType_default", withoutIndexByMetadata));

	}

	@Test
	public void whenConfigureCacheThatIsAlreadyConfiguredThenNotReconfigured()
			throws Exception {

		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, withoutIndexByMetadata));
		cache.configureCache(CacheConfig.volatileCache(zeType, 100, withoutIndexByMetadata));

		assertThat(cache.getCacheConfigOf(zeType).isVolatile()).isFalse();

	}

	@Test(expected = IllegalArgumentException.class)
	public void whenConfigureCacheUsingNullThenIllegalArgumentException()
			throws Exception {

		cache.configureCache(null);

	}

	@Test
	public void givenpermanentCacheNotLoadedInitiallyWhenInsertRecordsThenKeepAllOfThem()
			throws Exception {

		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, withoutIndexByMetadata));

		Record record1 = newRecord(zeType, 1);
		cache.insert(record1, WAS_MODIFIED);

		Record record2 = newRecord(zeType, 2);
		cache.insert(record2, WAS_MODIFIED);

		Record record3 = newRecord(zeType, 3);
		cache.insert(record3, WAS_MODIFIED);

		assertThatRecords("1", "2", "3").areInCache();

		assertThat(cache.get("1").getId()).isEqualTo("1");
		assertThat(cache.get("1")).isNotSameAs(record1);

		assertThat(cache.get("2").getId()).isEqualTo("2");
		assertThat(cache.get("2")).isNotSameAs(record2);

		assertThat(cache.get("3").getId()).isEqualTo("3");
		assertThat(cache.get("3")).isNotSameAs(record3);

	}

	@Test
	public void whenInsertRecordsOfNonCachedTpesThenNotKeptInCache()
			throws Exception {

		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, withoutIndexByMetadata));

		Record record1 = newRecord(zeType, 1);
		cache.insert(record1, WAS_MODIFIED);

		Record record2 = newRecord(anotherType, 2);
		cache.insert(record2, WAS_MODIFIED);

		Record record3 = newRecord(anotherType, 3);
		cache.insert(record3, WAS_MODIFIED);

		assertThatRecord("1").isInCache();
		assertThatRecords("2", "3").areNotInCache();

	}

	@Test
	public void whenInsertRecordsOfVolatileCachedTypesThenKeptInCache()
			throws Exception {

		cache.configureCache(CacheConfig.volatileCache(zeType, 3, withoutIndexByMetadata));

		Record record1 = newRecord(zeType, 1);
		cache.insert(record1, WAS_MODIFIED);

		Record record2 = newRecord(zeType, 2);
		cache.insert(record2, WAS_MODIFIED);

		Record record3 = newRecord(zeType, 3);
		cache.insert(record3, WAS_MODIFIED);

		assertThatRecords("1", "2", "3").areInCache();

	}

	@Test
	public void givenVolatileCacheIsFullWhenInsertingANewItemThenRemoveOlder()
			throws Exception {

		cache.configureCache(CacheConfig.volatileCache(zeType, 3, withoutIndexByMetadata));

		cache.insert(newRecord(zeType, 1), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 2), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 3), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 4), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 5), WAS_MODIFIED);

		assertThatRecords("3", "4", "5").areInCache();
		assertThatRecords("1", "2").areNotInCache();

		cache.insert(newRecord(zeType, 6), WAS_MODIFIED);
		assertThatRecords("4", "5", "6").areInCache();
		assertThatRecords("1", "2", "3").areNotInCache();

		cache.insert(newRecord(zeType, 1), WAS_MODIFIED);
		assertThatRecords("1", "5", "6").areInCache();
		assertThatRecords("2", "3", "4").areNotInCache();
	}

	@Test
	public void givenVolatileCacheWhenARecordIsHitThenMovedOnTop()
			throws Exception {

		cache.configureCache(CacheConfig.volatileCache(zeType, 3, withoutIndexByMetadata));

		cache.insert(newRecord(zeType, 1), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 2), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 3), WAS_MODIFIED);

		cache.get("1");
		cache.insert(newRecord(zeType, 4), WAS_MODIFIED);
		assertThatRecords("1", "3", "4").areInCache();
		assertThatRecord("2").isNotInCache();

		cache.insert(newRecord(zeType, 5), WAS_MODIFIED);
		assertThatRecords("1", "4", "5").areInCache();
		assertThatRecords("2", "3").areNotInCache();

		cache.insert(newRecord(zeType, 6), WAS_MODIFIED);
		assertThatRecords("4", "5", "6").areInCache();
		assertThatRecords("1", "2", "3").areNotInCache();

		cache.insert(newRecord(zeType, 1), WAS_MODIFIED);
		assertThatRecords("1", "5", "6").areInCache();
		assertThatRecords("2", "3", "4").areNotInCache();

	}

	@Test
	public void givenVolatileCacheWithFirstInFirstOutInvalidationWhenARecordIsPlacedThenOldestRemoved()
			throws Exception {

		cache.configureCache(CacheConfig.volatileCache(zeType, 3, withoutIndexByMetadata, FIFO));

		cache.insert(newRecord(zeType, 1), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 2), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 3), WAS_MODIFIED);

		cache.get("1");
		cache.get("2");

		cache.insert(newRecord(zeType, 4), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 5), WAS_MODIFIED);

		assertThatRecords("3", "4", "5").areInCache();
		assertThatRecords("1", "2").areNotInCache();

		cache.get("4");
		cache.get("5");
		cache.insert(newRecord(zeType, 6), WAS_MODIFIED);

		assertThatRecords("4", "5", "6").areInCache();
		assertThatRecords("1", "2", "3").areNotInCache();

	}

	@Test
	@SlowTest
	public void givenVolatileCacheWhenTopRecordIsHitThenNotConsideredHasAHit()
			throws Exception {

		cache.configureCache(CacheConfig.volatileCache(zeType, 3, withoutIndexByMetadata));

		cache.insert(newRecord(zeType, 1), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 2), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 3), WAS_MODIFIED);

		for (int i = 0; i < 20000; i++) {
			cache.get("1");
			cache.get("2");
		}

		cache.insert(newRecord(zeType, 4), WAS_MODIFIED);
		assertThatRecords("1", "2", "4").areInCache();
		assertThatRecord("3").isNotInCache();

		cache.insert(newRecord(zeType, 5), WAS_MODIFIED);
		assertThatRecords("2", "5").areInCache();
		assertThatRecords("1", "3").areNotInCache();

		cache.insert(newRecord(zeType, 6), WAS_MODIFIED);
		assertThatRecords("4", "5", "6").areInCache();
		assertThatRecords("1", "2", "3").areNotInCache();
		assertThat(cache.volatileCaches.get(zeType).holders.size()).isEqualTo(3);
	}

	@Test
	public void givenpermanentCacheNotLoadedInitiallyWhenInsertASameRecordMultipleTimesThenOnlyAddedOnce()
			throws Exception {
		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, withoutIndexByMetadata));

		cache.insert(newRecord(zeType, 1), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 2), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 2, 2), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 2, 3), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 3), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 3, 2), WAS_MODIFIED);

		assertThatRecords("1", "2", "3").areInCache();
		assertThat(cache.permanentCaches.get(zeType).holders).hasSize(3);
		assertThat(cache.get("1").getVersion()).isEqualTo(1L);
		assertThat(cache.get("2").getVersion()).isEqualTo(3L);
		assertThat(cache.get("3").getVersion()).isEqualTo(2L);

	}

	@Test
	public void givenVolatileCacheWhenInsertASameRecordMultipleTimesThenOnlyAddedOnce()
			throws Exception {
		cache.configureCache(CacheConfig.volatileCache(zeType, 3, withoutIndexByMetadata));

		cache.insert(newRecord(zeType, 1), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 2), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 2, 2), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 2, 3), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 3), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 3, 2), WAS_MODIFIED);

		assertThatRecords("1", "2", "3").areInCache();
		assertThat(cache.volatileCaches.get(zeType).holders).hasSize(3);
		assertThat(cache.get("1").getVersion()).isEqualTo(1L);
		assertThat(cache.get("2").getVersion()).isEqualTo(3L);
		assertThat(cache.get("3").getVersion()).isEqualTo(2L);
	}

	@Test
	public void givenVolatileCacheWithRecordsWhenInvalidateAllThenAllRemovedAndCacheEmpty()
			throws Exception {

		cache.configureCache(CacheConfig.volatileCache(zeType, 4, withoutIndexByMetadata));
		cache.configureCache(CacheConfig.volatileCache(anotherType, 4, withoutIndexByMetadata));

		cache.insert(newRecord(anotherType, 10), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 1), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 2), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 3), WAS_MODIFIED);
		cache.get("2");
		cache.insert(newRecord(zeType, 3, 2), WAS_MODIFIED);
		assertThatRecords("1", "2", "3", "10").areInCache();

		cache.reloadSchemaType(zeType, true);
		assertThatRecords("1", "2", "3").areNotInCache();
		assertThatRecord("10").isInCache();
		assertThat(cache.volatileCaches.get(zeType).recordsInCache).isEqualTo(0);
		assertThat(cache.volatileCaches.get(zeType).holders).isEmpty();

	}

	@Test
	public void givenpermanentCacheNotLoadedInitiallyWithRecordsWhenInvalidateAllThenAllRemovedAndCacheEmpty()
			throws Exception {

		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, withoutIndexByMetadata));
		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(anotherType, withoutIndexByMetadata));

		cache.insert(newRecord(anotherType, 10), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 1), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 2), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 3), WAS_MODIFIED);
		cache.get("2");
		cache.insert(newRecord(zeType, 3, 2), WAS_MODIFIED);
		assertThatRecords("1", "2", "3", "10").areInCache();

		cache.reloadSchemaType(zeType, true);
		assertThatRecords("1", "2", "3").areNotInCache();
		assertThatRecord("10").isInCache();
		assertThat(cache.permanentCaches.get(zeType).holders).hasSize(3);

	}

	@Test
	public void whenInsertingMultipleRecordsThenRegistered()
			throws Exception {
		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, withoutIndexByMetadata));
		cache.configureCache(CacheConfig.volatileCache(anotherType, 3, withoutIndexByMetadata));

		cache.insert(asList(
				newRecord(anotherType, 1),
				newRecord(anotherType, 2),
				newRecord(zeType, 3),
				newRecord(zeType, 4)
		), WAS_MODIFIED);
		assertThatRecords("1", "2", "3", "4").areInCache();

		cache.insert(asList(
				newRecord(anotherType, 5),
				newRecord(anotherType, 6),
				newRecord(zeType, 7),
				newRecord(zeType, 8)
		), WAS_MODIFIED);
		assertThatRecords("2", "3", "4", "5", "6", "7", "8").areInCache();
		assertThatRecord("1").isNotInCache();

	}

	@Test
	public void givenVolatileCacheIsNotBigEnoughWhenInsertingThenInsertHasMuchRecordsHasPossible()
			throws Exception {
		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, withoutIndexByMetadata));
		cache.configureCache(CacheConfig.volatileCache(anotherType, 3, withoutIndexByMetadata));

		cache.insert(asList(
				newRecord(anotherType, 1),
				newRecord(anotherType, 2),
				newRecord(anotherType, 3),
				newRecord(anotherType, 4),
				newRecord(zeType, 5),
				newRecord(zeType, 6),
				newRecord(zeType, 7),
				newRecord(zeType, 8)
		), WAS_MODIFIED);
		assertThatRecords("2", "3", "4", "5", "6", "7", "8").areInCache();
		assertThatRecord("1").isNotInCache();

	}

	@Test
	public void givenVolatileCacheWhenInsertingNullThenDoNothing()
			throws Exception {
		cache.configureCache(CacheConfig.volatileCache(zeType, 3, withoutIndexByMetadata));

		cache.insert(newRecord(zeType, 1), WAS_MODIFIED);
		cache.insert((Record) null, WAS_MODIFIED);
		cache.insert((List<Record>) null, WAS_MODIFIED);

		assertThatRecord("1").isInCache();
	}

	@Test
	public void givenVolatileCacheWhenInsertingEmptyListThenDoNothing()
			throws Exception {
		cache.configureCache(CacheConfig.volatileCache(zeType, 3, withoutIndexByMetadata));

		cache.insert(newRecord(zeType, 1), WAS_MODIFIED);
		cache.insert(new ArrayList<Record>(), WAS_MODIFIED);

		assertThatRecord("1").isInCache();
	}

	@Test
	public void givenVolatileCacheWhenInsertingDirtyRecordThenNotInserted()
			throws Exception {
		cache.configureCache(CacheConfig.volatileCache(zeType, 3, withoutIndexByMetadata));

		Record dirtyRecord = newRecord(zeType, 2);
		when(dirtyRecord.isDirty()).thenReturn(true);

		cache.insert(newRecord(zeType, 1), WAS_MODIFIED);
		cache.insert(dirtyRecord, WAS_MODIFIED);

		assertThatRecord("1").isInCache();
		assertThatRecord("2").isNotInCache();
	}

	@Test
	public void givenVolatileCacheWhenInsertingNotFullyLoadedRecordThenNotInsertedAndInvalidateExisting()
			throws Exception {
		cache.configureCache(CacheConfig.volatileCache(zeType, 3, withoutIndexByMetadata));

		Record notFullyLoadedRecord = newRecord(zeType, 2);
		when(notFullyLoadedRecord.isFullyLoaded()).thenReturn(false);

		cache.insert(newRecord(zeType, 1), WAS_MODIFIED);
		cache.insert(notFullyLoadedRecord, WAS_MODIFIED);

		assertThatRecord("1").isInCache();
		assertThatRecord("2").isNotInCache();

		Record zeTypeRecordUpdate = newRecord(zeType, 1);
		when(zeTypeRecordUpdate.isFullyLoaded()).thenReturn(false);
		cache.insert(zeTypeRecordUpdate, WAS_MODIFIED);
		assertThatRecords("1", "2").areNotInCache();
	}

	@Test
	public void givenVolatileCacheWhenInsertingLogicallyDeletedRecordThenInserted()
			throws Exception {
		cache.configureCache(CacheConfig.volatileCache(zeType, 3, withoutIndexByMetadata));

		Record logicallyDeletedRecord = newRecord(zeType, 2);
		when(logicallyDeletedRecord.get(Schemas.LOGICALLY_DELETED_STATUS)).thenReturn(true);

		Record restoredRecord = newRecord(zeType, 3);
		when(restoredRecord.get(Schemas.LOGICALLY_DELETED_STATUS)).thenReturn(false);

		cache.insert(newRecord(zeType, 1), WAS_MODIFIED);
		cache.insert(logicallyDeletedRecord, WAS_MODIFIED);
		cache.insert(restoredRecord, WAS_MODIFIED);

		assertThatRecords("1", "2", "3").areInCache();
	}

	@Test
	public void givenVolatileCacheWhenInsertingUnsavedRecordThenNotInserted()
			throws Exception {
		cache.configureCache(CacheConfig.volatileCache(zeType, 3, withoutIndexByMetadata));

		Record dirtyRecord = newRecord(zeType, 2);
		when(dirtyRecord.isSaved()).thenReturn(false);

		cache.insert(newRecord(zeType, 1), WAS_MODIFIED);
		cache.insert(dirtyRecord, WAS_MODIFIED);

		assertThatRecord("1").isInCache();
		assertThatRecord("2").isNotInCache();
	}

	@Test
	public void givenPermanenCacheWhenInsertingNullThenDoNothing()
			throws Exception {
		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, withoutIndexByMetadata));

		cache.insert(newRecord(zeType, 1), WAS_MODIFIED);
		cache.insert((Record) null, WAS_MODIFIED);
		cache.insert((List<Record>) null, WAS_MODIFIED);

		assertThatRecord("1").isInCache();
	}

	@Test
	public void givenPermanenCacheWhenInsertingEmptyListThenDoNothing()
			throws Exception {
		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, withoutIndexByMetadata));

		cache.insert(newRecord(zeType, 1), WAS_MODIFIED);
		cache.insert(new ArrayList<Record>(), WAS_MODIFIED);

		assertThatRecord("1").isInCache();
	}

	@Test
	public void givenpermanentCacheNotLoadedInitiallyWhenInsertingLogicallyDeletedRecordThenReinserted()
			throws Exception {
		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, withoutIndexByMetadata));

		Record logicallyDeletedRecord = newRecord(zeType, 2);
		when(logicallyDeletedRecord.get(Schemas.LOGICALLY_DELETED_STATUS)).thenReturn(true);

		Record restoredRecord = newRecord(zeType, 3);
		when(restoredRecord.get(Schemas.LOGICALLY_DELETED_STATUS)).thenReturn(false);

		cache.insert(newRecord(zeType, 1), WAS_MODIFIED);
		cache.insert(logicallyDeletedRecord, WAS_MODIFIED);
		cache.insert(restoredRecord, WAS_MODIFIED);

		assertThatRecords("1", "2", "3").areInCache();
	}

	@Test
	public void givenPermanenCacheWhenInsertingDirtyRecordThenNotInserted()
			throws Exception {
		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, withoutIndexByMetadata));

		Record dirtyRecord = newRecord(zeType, 2);
		when(dirtyRecord.isDirty()).thenReturn(true);

		cache.insert(newRecord(zeType, 1), WAS_MODIFIED);
		cache.insert(dirtyRecord, WAS_MODIFIED);

		assertThatRecord("1").isInCache();
		assertThatRecord("2").isNotInCache();
	}

	@Test
	public void givenpermanentCacheNotLoadedInitiallyWhenInsertingNotFullyLoadedRecordThenNotInserted()
			throws Exception {
		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, withoutIndexByMetadata));

		Record notFullyLoadedRecord = newRecord(zeType, 2);
		when(notFullyLoadedRecord.isFullyLoaded()).thenReturn(false);

		cache.insert(newRecord(zeType, 1), WAS_MODIFIED);
		cache.insert(notFullyLoadedRecord, WAS_MODIFIED);

		assertThatRecord("1").isInCache();
		assertThatRecord("2").isNotInCache();

		Record zeTypeRecordUpdate = newRecord(zeType, 1);
		when(zeTypeRecordUpdate.isFullyLoaded()).thenReturn(false);
		cache.insert(zeTypeRecordUpdate, WAS_MODIFIED);
		assertThatRecords("1", "2").areNotInCache();
	}

	@Test
	public void givenpermanentCacheNotLoadedInitiallyWhenInsertingUnsavedRecordThenNotInserted()
			throws Exception {
		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, withoutIndexByMetadata));

		Record dirtyRecord = newRecord(zeType, 2);
		when(dirtyRecord.isSaved()).thenReturn(false);

		cache.insert(newRecord(zeType, 1), WAS_MODIFIED);
		cache.insert(dirtyRecord, WAS_MODIFIED);

		assertThatRecord("1").isInCache();
		assertThatRecord("2").isNotInCache();
	}

	@Test
	public void wheInvalidatingAnUnfoundRecordThenNothingChange()
			throws Exception {

		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, withoutIndexByMetadata));
		cache.insert(newRecord(zeType, 1), WAS_MODIFIED);

		cache.removeFromAllCaches("2");

		assertThatRecord("1").isInCache();
		assertThatRecord("2").isNotInCache();

	}

	@Test
	public void givenpermanentCacheNotLoadedInitiallyWhenInvalidatingARecordThenDiminishCounter()
			throws Exception {

		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, withoutIndexByMetadata));
		cache.insert(newRecord(zeType, 1), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 2), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 3), WAS_MODIFIED);

		cache.removeFromAllCaches("2");

		assertThatRecords("1", "3").areInCache();
		assertThatRecord("2").isNotInCache();

	}

	@Test
	public void givenVolatileCacheWhenInvalidatingARecordThenInvalidate()
			throws Exception {
		cache.configureCache(CacheConfig.volatileCache(zeType, 3, withoutIndexByMetadata));
		cache.insert(newRecord(zeType, 1), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 2), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 3), WAS_MODIFIED);

		cache.removeFromAllCaches("2");

		assertThatRecords("1", "3").areInCache();
		assertThatRecord("2").isNotInCache();
		assertThat(cache.volatileCaches.get(zeType).recordsInCache).isEqualTo(3);
		//At this step, the cache elements counter has an invalid value

		cache.insert(newRecord(zeType, 4), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 5), WAS_MODIFIED);
		assertThatRecords("3", "4", "5").areInCache();
		assertThatRecords("1", "2").isNotInCache();
		assertThat(cache.volatileCaches.get(zeType).recordsInCache).isEqualTo(3);
		//The counter has return to normal
	}

	@Test
	public void whenInvalidatingMultipleRecordsThenAllInvalidated()
			throws Exception {
		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, withoutIndexByMetadata));
		cache.configureCache(CacheConfig.volatileCache(anotherType, 3, withoutIndexByMetadata));

		cache.insert(asList(
				newRecord(anotherType, 1),
				newRecord(anotherType, 2),
				newRecord(anotherType, 3),
				newRecord(zeType, 4),
				newRecord(zeType, 5),
				newRecord(zeType, 6)
		), WAS_MODIFIED);

		cache.removeFromAllCaches(asList("1", "3", "5"));

		assertThatRecords("2", "4", "6").areInCache();
		assertThatRecords("1", "3", "5").areNotInCache();

	}

	@Test
	public void whenInvalidatingPassingNullThenDoesNothing()
			throws Exception {
		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, withoutIndexByMetadata));
		cache.configureCache(CacheConfig.volatileCache(anotherType, 3, withoutIndexByMetadata));

		cache.insert(asList(
				newRecord(anotherType, 1),
				newRecord(anotherType, 2),
				newRecord(anotherType, 3),
				newRecord(zeType, 4),
				newRecord(zeType, 5),
				newRecord(zeType, 6)
		), WAS_MODIFIED);

		cache.removeFromAllCaches((String) null);
		cache.removeFromAllCaches((List<String>) null);

		assertThatRecords("1", "2", "3", "4", "5", "6").areInCache();

	}


	@Test
	public void whenGetAllValuesOfPermanentCachesThenAllReturnedInModifiableState()
			throws Exception {
		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, withoutIndexByMetadata));
		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(anotherType, withoutIndexByMetadata));
		Record anotherTypeRecord1 = newRecord(anotherType, 1);
		Record anotherTypeRecord2 = newRecord(anotherType, 2);
		Record anotherTypeRecord3 = newRecord(anotherType, 3);
		Record zeTypeRecord4 = newRecord(zeType, 4);
		Record zeTypeRecord5 = newRecord(zeType, 5);
		Record zeTypeRecord6 = newRecord(zeType, 6);
		cache.insert(
				asList(anotherTypeRecord1, anotherTypeRecord2, anotherTypeRecord3, zeTypeRecord4, zeTypeRecord5, zeTypeRecord6),
				WAS_MODIFIED);

		assertThat(cache.getAllValues(anotherType)).extracting("id").containsOnly("1", "2", "3");
		assertThat(cache.getAllValues(zeType)).extracting("id").containsOnly("4", "5", "6");

		cache.getAllValues(anotherType).get(0).set(Schemas.TITLE, "test");
	}


	@Test
	public void whenInsertingRecordInOlderVersionThenRejected()
			throws Exception {

		givenDisabledRecordDuplications = true;

		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, asList(zeTypeCodeMetadata)));

		Record record = newRecord(zeType, 1);

		Record recordInNewVersion = newRecord(zeType, 1, 2L);

		assertThat(cache.insert(record, WAS_MODIFIED).status).isEqualTo(CacheInsertionStatus.ACCEPTED);
		assertThat(cache.get("1").getVersion()).isEqualTo(1L);

		assertThat(cache.insert(recordInNewVersion, WAS_MODIFIED).status).isEqualTo(CacheInsertionStatus.ACCEPTED);
		assertThat(cache.get("1").getVersion()).isEqualTo(2L);

		assertThat(cache.insert(record, WAS_MODIFIED).status).isEqualTo(CacheInsertionStatus.REFUSED_OLD_VERSION);
		assertThat(cache.get("1").getVersion()).isEqualTo(2L);

	}

	@Test
	public void givenCacheWithMetadataIndexesThenCanFindRecordsWithThem()
			throws Exception {

		givenDisabledRecordDuplications = true;

		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, asList(zeTypeCodeMetadata)));
		cache.configureCache(
				CacheConfig.volatileCache(anotherType, 3, asList(anotherTypeCodeMetadata, anotherTypeLegacyIdMetadata)));

		Record zeType1 = newRecord(zeType, 1);
		when(zeType1.get(zeTypeCodeMetadata)).thenReturn("leNumero1"); //Supertimor

		Record zeType18 = newRecord(zeType, 2);
		when(zeType18.get(zeTypeCodeMetadata)).thenReturn("code18");

		Record zeType42 = newRecord(zeType, 3);
		when(zeType42.get(zeTypeCodeMetadata)).thenReturn("ze42");

		Record anotherType1 = newRecord(anotherType, 4);
		when(anotherType1.get(anotherTypeCodeMetadata)).thenReturn("leNumero1"); //Supertimor
		when(anotherType1.get(anotherTypeLegacyIdMetadata)).thenReturn("123");

		Record anotherType18 = newRecord(anotherType, 5);
		when(anotherType18.get(anotherTypeCodeMetadata)).thenReturn("code18");
		when(anotherType18.get(anotherTypeLegacyIdMetadata)).thenReturn("456");

		Record anotherType42 = newRecord(anotherType, 6);
		when(anotherType42.get(anotherTypeCodeMetadata)).thenReturn("ze42");
		when(anotherType42.get(anotherTypeLegacyIdMetadata)).thenReturn("789");

		cache.insert(asList(zeType1, zeType18, zeType42, anotherType1, anotherType18, anotherType42), WAS_MODIFIED);

		assertThat(idOf(cache.getByMetadata(zeTypeCodeMetadata, "leNumero1"))).isEqualTo("1");
		assertThat(idOf(cache.getByMetadata(zeTypeCodeMetadata, "code18"))).isEqualTo("2");
		assertThat(idOf(cache.getByMetadata(zeTypeCodeMetadata, "ze42"))).isEqualTo("3");
		assertThat(idOf(cache.getByMetadata(zeTypeCodeMetadata, "666"))).isNull();

		assertThat(idOf(cache.getByMetadata(anotherTypeCodeMetadata, "leNumero1"))).isEqualTo("4");
		assertThat(idOf(cache.getByMetadata(anotherTypeCodeMetadata, "code18"))).isEqualTo("5");
		assertThat(idOf(cache.getByMetadata(anotherTypeCodeMetadata, "ze42"))).isEqualTo("6");
		assertThat(idOf(cache.getByMetadata(anotherTypeCodeMetadata, "666"))).isNull();

		assertThat(idOf(cache.getByMetadata(anotherTypeLegacyIdMetadata, "123"))).isEqualTo("4");
		assertThat(idOf(cache.getByMetadata(anotherTypeLegacyIdMetadata, "456"))).isEqualTo("5");
		assertThat(idOf(cache.getByMetadata(anotherTypeLegacyIdMetadata, "789"))).isEqualTo("6");
		assertThat(idOf(cache.getByMetadata(anotherTypeLegacyIdMetadata, "666"))).isNull();

		zeType18 = newRecord(zeType, 2, 2L);
		when(zeType18.get(zeTypeCodeMetadata)).thenReturn("666");
		cache.insert(asList(zeType18), WAS_MODIFIED);

		anotherType42 = newRecord(anotherType, 6, 2L);
		when(anotherType42.get(anotherTypeCodeMetadata)).thenReturn("ze42");
		when(anotherType42.get(anotherTypeLegacyIdMetadata)).thenReturn("666");
		cache.insert(asList(anotherType42), WAS_MODIFIED);

		anotherType1 = newRecord(anotherType, 4, 2L);
		when(anotherType1.get(anotherTypeCodeMetadata)).thenReturn(null);
		when(anotherType1.get(anotherTypeLegacyIdMetadata)).thenReturn("123");
		cache.insert(asList(anotherType1), WAS_MODIFIED);

		assertThat(idOf(cache.getByMetadata(zeTypeCodeMetadata, "leNumero1"))).isEqualTo("1");
		assertThat(idOf(cache.getByMetadata(zeTypeCodeMetadata, "code18"))).isNull();
		assertThat(idOf(cache.getByMetadata(zeTypeCodeMetadata, "ze42"))).isEqualTo("3");
		assertThat(idOf(cache.getByMetadata(zeTypeCodeMetadata, "666"))).isEqualTo("2");

		assertThat(idOf(cache.getByMetadata(anotherTypeCodeMetadata, "leNumero1"))).isNull();
		assertThat(idOf(cache.getByMetadata(anotherTypeCodeMetadata, "code18"))).isEqualTo("5");
		assertThat(idOf(cache.getByMetadata(anotherTypeCodeMetadata, "ze42"))).isEqualTo("6");
		assertThat(idOf(cache.getByMetadata(anotherTypeCodeMetadata, "666"))).isNull();

		assertThat(idOf(cache.getByMetadata(anotherTypeLegacyIdMetadata, "123"))).isEqualTo("4");
		assertThat(idOf(cache.getByMetadata(anotherTypeLegacyIdMetadata, "456"))).isEqualTo("5");
		assertThat(idOf(cache.getByMetadata(anotherTypeLegacyIdMetadata, "789"))).isNull();
		assertThat(idOf(cache.getByMetadata(anotherTypeLegacyIdMetadata, "666"))).isEqualTo("6");

		cache.removeFromAllCaches("1");
		cache.removeFromAllCaches("5");
		zeType1 = newRecord(zeType, 1);
		when(zeType1.get(zeTypeCodeMetadata)).thenReturn("supertimor"); //leNumero1
		anotherType18 = newRecord(anotherType, 5);
		when(anotherType18.get(anotherTypeCodeMetadata)).thenReturn("code18pouces");
		cache.insert(asList(zeType1), WAS_MODIFIED);
		cache.insert(asList(anotherType18), WAS_MODIFIED);

		assertThat(idOf(cache.getByMetadata(zeTypeCodeMetadata, "leNumero1"))).isNull();
		assertThat(idOf(cache.getByMetadata(zeTypeCodeMetadata, "code18"))).isNull();
		assertThat(idOf(cache.getByMetadata(zeTypeCodeMetadata, "ze42"))).isEqualTo("3");
		assertThat(idOf(cache.getByMetadata(zeTypeCodeMetadata, "666"))).isEqualTo("2");
		assertThat(idOf(cache.getByMetadata(zeTypeCodeMetadata, "supertimor"))).isEqualTo("1");

		assertThat(idOf(cache.getByMetadata(anotherTypeCodeMetadata, "leNumero1"))).isNull();
		assertThat(idOf(cache.getByMetadata(anotherTypeCodeMetadata, "code18"))).isNull();
		assertThat(idOf(cache.getByMetadata(anotherTypeCodeMetadata, "ze42"))).isEqualTo("6");
		assertThat(idOf(cache.getByMetadata(anotherTypeCodeMetadata, "666"))).isNull();
		assertThat(idOf(cache.getByMetadata(anotherTypeCodeMetadata, "code18pouces"))).isEqualTo("5");

		//Record 4 has been released during record 5 insert
		assertThat(idOf(cache.getByMetadata(anotherTypeLegacyIdMetadata, "123"))).isNull();
		assertThat(idOf(cache.getByMetadata(anotherTypeLegacyIdMetadata, "456"))).isNull();
		assertThat(idOf(cache.getByMetadata(anotherTypeLegacyIdMetadata, "789"))).isNull();
		assertThat(idOf(cache.getByMetadata(anotherTypeLegacyIdMetadata, "666"))).isEqualTo("6");

	}

	private String idOf(Record record) {
		return record == null ? null : record.getId();
	}

	//--------------------------------------

	private OngoingEntryAssertion assertThatRecord(String id) {
		return new OngoingEntryAssertion(asList(id));
	}

	private OngoingEntryAssertion assertThatRecords(String... ids) {
		return new OngoingEntryAssertion(asList(ids));
	}

	private class OngoingEntryAssertion {

		private List<String> ids;

		private OngoingEntryAssertion(List<String> ids) {
			this.ids = ids;
		}

		private void isInCache() {
			areInCache();
		}

		private void areInCache() {
			for (String id : ids) {
				boolean isCached = cache.isCached(id);
				assertThat(isCached).describedAs("Record with id '" + id + "' is expected to be in cache").isTrue();
				RecordHolder holder = cache.cacheById.get(id);
				assertThat(holder).describedAs("Record holder of '" + id + "'").isNotNull();
				assertThat(holder.getCopy()).describedAs("Cache record of '" + id + "'").isNotNull();
			}
		}

		private void isNotInCache() {
			areNotInCache();
		}

		private void areNotInCache() {
			for (String id : ids) {
				boolean isCached = cache.isCached(id);
				assertThat(isCached).describedAs("Record with id '" + id + "' is expected to not be in cache").isFalse();
			}
		}
	}

	private Record newRecord(String schemaType, int id) {
		return newRecord(schemaType, "" + id);
	}

	private Record newRecord(String schemaType, int id, long version) {
		return newRecord(schemaType, "" + id, version);
	}

	private Record newRecord(final String schemaType, final String id) {
		return newRecord(schemaType, id, 1);
	}

	private Record newRecord(final String schemaType, final String id, final long version) {
		String schema = schemaType + "_default";
		final Record record = mock(Record.class, schemaType + "-" + id);
		when(record.getId()).thenReturn(id);
		when(record.getSchemaCode()).thenReturn(schema);
		when(record.getTypeCode()).thenReturn(schemaType);
		when(record.getVersion()).thenReturn(version);
		when(record.isDirty()).thenReturn(false);
		when(record.isFullyLoaded()).thenReturn(true);
		when(record.isSaved()).thenReturn(true);
		when(record.getCopyOfOriginalRecord()).thenAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation)
					throws Throwable {
				if (givenDisabledRecordDuplications) {
					return record;
				} else {
					boolean dirty = record.isDirty();
					boolean fullyLoaded = record.isFullyLoaded();
					boolean saved = record.isSaved();
					Boolean logicallyDeleted = record.get(Schemas.LOGICALLY_DELETED_STATUS);
					Record recordCopy = newRecord(schemaType, id, version);
					when(recordCopy.isDirty()).thenReturn(dirty);
					when(recordCopy.isFullyLoaded()).thenReturn(fullyLoaded);
					when(recordCopy.isSaved()).thenReturn(saved);
					when(recordCopy.get(Schemas.LOGICALLY_DELETED_STATUS)).thenReturn(logicallyDeleted);
					return recordCopy;
				}
			}
		});

		when(record.getUnmodifiableCopyOfOriginalRecord()).thenAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation)
					throws Throwable {
				if (givenDisabledRecordDuplications) {
					return record;
				} else {
					boolean dirty = record.isDirty();
					boolean fullyLoaded = record.isFullyLoaded();
					boolean saved = record.isSaved();
					Boolean logicallyDeleted = record.get(Schemas.LOGICALLY_DELETED_STATUS);
					Record recordCopy = newRecord(schemaType, id, version);

					when(recordCopy.set(any(Metadata.class), any(Object.class)))
							.thenThrow(RecordImplException_RecordIsUnmodifiable.class);
					when(recordCopy.isDirty()).thenReturn(dirty);
					when(recordCopy.isFullyLoaded()).thenReturn(fullyLoaded);
					when(recordCopy.isSaved()).thenReturn(saved);
					when(recordCopy.get(Schemas.LOGICALLY_DELETED_STATUS)).thenReturn(logicallyDeleted);
					return recordCopy;
				}
			}
		});

		return record;
	}

	private MetadataSchemaType zeType() {
		MetadataSchemaType type = Mockito.mock(MetadataSchemaType.class);
		when(type.getCode()).thenReturn(zeType);
		return type;
	}

	private MetadataSchemaType anotherType() {
		MetadataSchemaType type = Mockito.mock(MetadataSchemaType.class);
		when(type.getCode()).thenReturn(anotherType);
		return type;
	}

	private LogicalSearchQuery query(LogicalSearchCondition condition) {
		return new LogicalSearchQuery(condition);
	}


}
