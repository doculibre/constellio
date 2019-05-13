package com.constellio.model.services.records.cache.ignite;

import com.constellio.data.dao.services.cache.ConstellioCacheManager;
import com.constellio.data.dao.services.cache.ignite.ConstellioIgniteCache;
import com.constellio.data.dao.services.cache.ignite.ConstellioIgniteCacheManager;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.records.LocalisedRecordMetadataRetrieval;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.ModelLayerSystemExtensions;
import com.constellio.model.services.extensions.ModelLayerExtensions;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache.CacheConfig;
import com.constellio.model.services.records.cache.RecordsCacheImplRuntimeException.RecordsCacheImplRuntimeException_InvalidSchemaTypeCode;
import com.constellio.model.services.records.cache.ignite.RecordsCacheIgniteImpl.RecordHolder;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.entities.SearchBoost;
import com.constellio.model.services.search.query.ResultsProjection;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.IgniteTest;
import org.assertj.core.api.ListAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.constellio.data.dao.services.cache.InsertionReason.WAS_MODIFIED;
import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.entities.schemas.Schemas.TITLE;
import static com.constellio.model.services.search.query.ReturnedMetadatasFilter.idVersionSchema;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.sdk.tests.TestUtils.mockManualMetadata;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@IgniteTest
public class RecordsCacheIgniteAcceptanceTest extends ConstellioTest {

	@Mock ModelLayerSystemExtensions systemExtensions;
	@Mock ModelLayerExtensions extensions;

	@Mock User user;
	@Mock SearchBoost searchBoost;
	@Mock ResultsProjection resultsProjection;
	@Mock ModelLayerFactory modelLayerFactory;
	@Mock DataLayerFactory dataLayerFactory;
	ConstellioCacheManager recordsCacheManager;

	boolean givenDisabledRecordDuplications = false;
	Metadata zeTypeCodeMetadata, anotherTypeCodeMetadata, anotherTypeLegacyIdMetadata;

	List<Metadata> withoutIndexByMetadata = new ArrayList<>();
	String zeType = "zeType";
	String anotherType = "anotherType";

	RecordsCacheIgniteImpl cache;

	@Before
	public void setUp()
			throws Exception {
		recordsCacheManager = new ConstellioIgniteCacheManager("localhost:47500", "1.2.3.42");

		when(modelLayerFactory.getExtensions()).thenReturn(extensions);
		when(modelLayerFactory.getDataLayerFactory()).thenReturn(dataLayerFactory);
		when(dataLayerFactory.getDistributedCacheManager()).thenReturn(recordsCacheManager);
		when(extensions.getSystemWideExtensions()).thenReturn(systemExtensions);

		cache = new RecordsCacheIgniteImpl(zeCollection, modelLayerFactory);
		zeTypeCodeMetadata = mockManualMetadata("zeType_default_code", MetadataValueType.STRING);
		anotherTypeCodeMetadata = mockManualMetadata("anotherType_default_code", MetadataValueType.STRING);
		anotherTypeLegacyIdMetadata = mockManualMetadata("anotherType_default_legacyId", MetadataValueType.STRING);

		when(modelLayerFactory.getExtensions()).thenReturn(extensions);
		when(extensions.getSystemWideExtensions()).thenReturn(systemExtensions);
	}

	@After
	public void tearDown()
			throws Exception {
		for (String cacheName : recordsCacheManager.getCacheNames()) {
			ConstellioIgniteCache cache = (ConstellioIgniteCache) recordsCacheManager.getCache(cacheName);
			cache.clear();
		}
	}

	//	IGNITE : Assume that LRU works properly
	//	@Test
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
	public void whenConfigureCacheThatIsAlreadyConfiguredThenReconfigure()
			throws Exception {

		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, withoutIndexByMetadata));
		cache.configureCache(CacheConfig.volatileCache(zeType, 100, withoutIndexByMetadata));

		assertThat(cache.getCacheConfigOf(zeType).isVolatile()).isTrue();

	}

	@Test(expected = IllegalArgumentException.class)
	public void whenConfigureCacheUsingNullThenIllegalArgumentException()
			throws Exception {

		cache.configureCache(null);

	}

	@Test
	public void givenPermanentCacheNotLoadedInitiallyWhenInsertRecordsThenKeepAllOfThem()
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

	//	IGNITE : Assume that LRU works properly
	//	@Test
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

	//	@Test
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

	//	@Test
	//	@SlowTest
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
		//		IGNITE : Assume that LRU is working properly
		//		assertThat(cache.volatileCaches.get(zeType).holdersSize()).isEqualTo(3);
	}

	@Test
	public void givenPermanentCacheNotLoadedInitiallyWhenInsertASameRecordMultipleTimesThenOnlyAddedOnce()
			throws Exception {
		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, withoutIndexByMetadata));

		cache.insert(newRecord(zeType, 1), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 2), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 2, 2), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 2, 3), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 3), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 3, 2), WAS_MODIFIED);

		assertThatRecords("1", "2", "3").areInCache();
		assertThat(cache.getPermanentRecordHoldersCount(zeType)).isEqualTo(3);
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
		assertThat(cache.getVolatileRecordHoldersCount(zeType)).isEqualTo(3);
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

		cache.invalidateRecordsOfType(zeType);
		assertThatRecords("1", "2", "3").areNotInCache();
		assertThatRecord("10").isInCache();
		//		FIXME No equivalent for recordsInCache in implementation
		//		assertThat(cache.volatileCaches.get(zeType).recordsInCache).isEqualTo(0);
		//		assertThat(cache.volatileCaches.get(zeType).holdersSize()).isEqualTo(0);
		assertThat(cache.getVolatileRecordHoldersCount(zeType)).isEqualTo(0);
	}

	@Test
	public void givenPermanentCacheNotLoadedInitiallyWithRecordsWhenInvalidateAllThenAllRemovedAndCacheEmpty()
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

		cache.invalidateRecordsOfType(zeType);
		assertThatRecords("1", "2", "3").areNotInCache();
		assertThatRecord("10").isInCache();
		//		FIXME With implementation, should be 0
		//		assertThat(cache.permanentCaches.get(zeType).holdersSize()).isEqualTo(3);
		assertThat(cache.getPermanentRecordHoldersCount(zeType)).isEqualTo(0);
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
		//		IGNITE : Assume that LRU works properly
		//		assertThatRecord("1").isNotInCache();
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
		//		IGNITE : Assuming Ignite manages LRU correctly
		//		assertThatRecord("1").isNotInCache();
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
		//		when(dirtyRecord.isDirty()).thenReturn(true);
		((TestRecord) dirtyRecord).setDirty(true);

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
		//		when(notFullyLoadedRecord.isFullyLoaded()).thenReturn(false);
		((TestRecord) notFullyLoadedRecord).setFullyLoaded(false);

		cache.insert(newRecord(zeType, 1), WAS_MODIFIED);
		cache.insert(notFullyLoadedRecord, WAS_MODIFIED);

		assertThatRecord("1").isInCache();
		assertThatRecord("2").isNotInCache();

		Record zeTypeRecordUpdate = newRecord(zeType, 1);
		//		when(zeTypeRecordUpdate.isFullyLoaded()).thenReturn(false);
		((TestRecord) zeTypeRecordUpdate).setFullyLoaded(false);
		cache.insert(zeTypeRecordUpdate, WAS_MODIFIED);
		assertThatRecords("1", "2").areNotInCache();
	}

	@Test
	public void givenVolatileCacheWhenInsertingLogicallyDeletedRecordThenNotInserted()
			throws Exception {
		cache.configureCache(CacheConfig.volatileCache(zeType, 3, withoutIndexByMetadata));

		Record logicallyDeletedRecord = newRecord(zeType, 2);
		//		when(logicallyDeletedRecord.get(Schemas.LOGICALLY_DELETED_STATUS)).thenReturn(true);
		((TestRecord) logicallyDeletedRecord).setLogicallyDeleted(true);

		Record restoredRecord = newRecord(zeType, 3);
		//		when(restoredRecord.get(Schemas.LOGICALLY_DELETED_STATUS)).thenReturn(false);
		((TestRecord) restoredRecord).setLogicallyDeleted(false);

		cache.insert(newRecord(zeType, 1), WAS_MODIFIED);
		cache.insert(logicallyDeletedRecord, WAS_MODIFIED);
		cache.insert(restoredRecord, WAS_MODIFIED);

		assertThatRecords("1", "3").areInCache();
		assertThatRecord("2").isNotInCache();
	}

	@Test
	public void givenVolatileCacheWhenInsertingUnsavedRecordThenNotInserted()
			throws Exception {
		cache.configureCache(CacheConfig.volatileCache(zeType, 3, withoutIndexByMetadata));

		Record dirtyRecord = newRecord(zeType, 2);
		//		when(dirtyRecord.isSaved()).thenReturn(false);
		((TestRecord) dirtyRecord).setSaved(false);

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
	public void givenPermanentCacheNotLoadedInitiallyWhenInsertingLogicallyDeletedRecordThenNotInserted()
			throws Exception {
		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, withoutIndexByMetadata));

		Record logicallyDeletedRecord = newRecord(zeType, 2);
		//		when(logicallyDeletedRecord.get(Schemas.LOGICALLY_DELETED_STATUS)).thenReturn(true);
		((TestRecord) logicallyDeletedRecord).setLogicallyDeleted(true);

		Record restoredRecord = newRecord(zeType, 3);
		//		when(restoredRecord.get(Schemas.LOGICALLY_DELETED_STATUS)).thenReturn(false);
		((TestRecord) restoredRecord).setLogicallyDeleted(false);

		cache.insert(newRecord(zeType, 1), WAS_MODIFIED);
		cache.insert(logicallyDeletedRecord, WAS_MODIFIED);
		cache.insert(restoredRecord, WAS_MODIFIED);

		assertThatRecords("1", "3").areInCache();
		assertThatRecord("2").isNotInCache();
	}

	@Test
	public void givenPermanenCacheWhenInsertingDirtyRecordThenNotInserted()
			throws Exception {
		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, withoutIndexByMetadata));

		Record dirtyRecord = newRecord(zeType, 2);
		//		when(dirtyRecord.isDirty()).thenReturn(true);
		((TestRecord) dirtyRecord).setDirty(true);

		cache.insert(newRecord(zeType, 1), WAS_MODIFIED);
		cache.insert(dirtyRecord, WAS_MODIFIED);

		assertThatRecord("1").isInCache();
		assertThatRecord("2").isNotInCache();
	}

	@Test
	public void givenPermanentCacheNotLoadedInitiallyWhenInsertingNotFullyLoadedRecordThenNotInserted()
			throws Exception {
		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, withoutIndexByMetadata));

		Record notFullyLoadedRecord = newRecord(zeType, 2);
		//		when(notFullyLoadedRecord.isFullyLoaded()).thenReturn(false);
		((TestRecord) notFullyLoadedRecord).setFullyLoaded(false);

		cache.insert(newRecord(zeType, 1), WAS_MODIFIED);
		cache.insert(notFullyLoadedRecord, WAS_MODIFIED);

		assertThatRecord("1").isInCache();
		assertThatRecord("2").isNotInCache();

		Record zeTypeRecordUpdate = newRecord(zeType, 1);
		//		when(zeTypeRecordUpdate.isFullyLoaded()).thenReturn(false);
		((TestRecord) zeTypeRecordUpdate).setFullyLoaded(false);
		cache.insert(zeTypeRecordUpdate, WAS_MODIFIED);
		assertThatRecords("1", "2").areNotInCache();
	}

	@Test
	public void givenPermanentCacheNotLoadedInitiallyWhenInsertingUnsavedRecordThenNotInserted()
			throws Exception {
		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, withoutIndexByMetadata));

		Record dirtyRecord = newRecord(zeType, 2);
		//		when(dirtyRecord.isSaved()).thenReturn(false);
		((TestRecord) dirtyRecord).setSaved(false);

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

		cache.invalidate("2");

		assertThatRecord("1").isInCache();
		assertThatRecord("2").isNotInCache();

	}

	@Test
	public void givenPermanentCacheNotLoadedInitiallyWhenInvalidatingARecordThenDiminishCounter()
			throws Exception {

		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, withoutIndexByMetadata));
		cache.insert(newRecord(zeType, 1), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 2), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 3), WAS_MODIFIED);

		cache.invalidate("2");

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

		cache.invalidate("2");

		assertThatRecords("1", "3").areInCache();
		assertThatRecord("2").isNotInCache();
		//		assertThat(cache.volatileCaches.get(zeType).recordsInCache).isEqualTo(3);
		assertThat(cache.getVolatileRecordHoldersCount(zeType)).isEqualTo(3);
		//At this step, the cache elements counter has an invalid value

		cache.insert(newRecord(zeType, 4), WAS_MODIFIED);
		cache.insert(newRecord(zeType, 5), WAS_MODIFIED);
		assertThatRecords("3", "4", "5").areInCache();
		//		IGNITE : Assume that LRU is working properly
		//		assertThatRecords("1", "2").isNotInCache();
		//		assertThat(cache.volatileCaches.get(zeType).recordsInCache).isEqualTo(3);
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

		cache.invalidate(asList("1", "3", "5"));

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

		cache.invalidate((String) null);
		cache.invalidate((List<String>) null);

		assertThatRecords("1", "2", "3", "4", "5", "6").areInCache();

	}

	@Test
	public void whenGetCachedSearchResultForQueryThenOnlyGetCachedSearchResultsForSameQueryOfpermanentCacheNotLoadedInitially()
			throws Exception {
		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, withoutIndexByMetadata));
		cache.configureCache(CacheConfig.volatileCache(anotherType, 3, withoutIndexByMetadata));
		Record anotherTypeRecord1 = newRecord(anotherType, 1);
		Record anotherTypeRecord2 = newRecord(anotherType, 2);
		Record anotherTypeRecord3 = newRecord(anotherType, 3);
		Record zeTypeRecord4 = newRecord(zeType, 4);
		Record zeTypeRecord5 = newRecord(zeType, 5);
		Record zeTypeRecord6 = newRecord(zeType, 6);

		cache.insert(
				asList(anotherTypeRecord1, anotherTypeRecord2, anotherTypeRecord3, zeTypeRecord4, zeTypeRecord5, zeTypeRecord6),
				WAS_MODIFIED);

		assertThatQueryResults(from(anotherType()).returnAll()).isNull();
		assertThatQueryResults(from(zeType()).returnAll()).isNull();
		assertThatQueryResults(fromAllSchemasIn(zeCollection).returnAll()).isNull();

		cache.insertQueryResults(query(from(anotherType()).returnAll()), asList(anotherTypeRecord1, anotherTypeRecord2));
		cache.insertQueryResults(query(from(zeType()).returnAll()), asList(zeTypeRecord4, zeTypeRecord5, zeTypeRecord6));
		cache.insertQueryResults(query(from(zeType()).where(TITLE).isEqualTo("value1")), asList(zeTypeRecord4, zeTypeRecord6));
		cache.insertQueryResults(query(fromAllSchemasIn(zeCollection).returnAll()),
				asList(zeTypeRecord4, zeTypeRecord5, zeTypeRecord6));

		assertThatQueryResults(from(anotherType()).returnAll()).isNull();

		//List<Record> records = cache.getQueryResults(from(zeType()).returnAll()));
		assertThatQueryResults(from(zeType()).returnAll()).containsExactly(zeTypeRecord4, zeTypeRecord5, zeTypeRecord6);
		assertThatQueryResults(from(zeType()).where(TITLE).isEqualTo("value1")).containsExactly(zeTypeRecord4, zeTypeRecord6);
		assertThatQueryResults(from(zeType()).where(TITLE).isEqualTo("value2")).isNull();
		assertThatQueryResults(from(zeType()).where(TITLE.getAnalyzedField("fr")).isEqualTo("value1")).isNull();
		assertThatQueryResults(fromAllSchemasIn(zeCollection).returnAll()).isNull();

		cache.insertQueryResults(query(from(zeType()).where(TITLE).isEqualTo("value2")), new ArrayList<Record>());
		assertThatQueryResults(from(zeType()).where(TITLE).isEqualTo("value1")).containsExactly(zeTypeRecord4, zeTypeRecord6);
		assertThatQueryResults(from(zeType()).where(TITLE).isEqualTo("value2")).isEmpty();
	}

	@Test
	public void whenGetCachedSearchResultForQueryThenOnlyCacheQueriesWithoutUnsupportedFiltersAndFeatures()
			throws Exception {
		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, withoutIndexByMetadata));
		cache.configureCache(CacheConfig.volatileCache(anotherType, 3, withoutIndexByMetadata));
		Record anotherTypeRecord1 = newRecord(anotherType, 1);
		Record anotherTypeRecord2 = newRecord(anotherType, 2);
		Record anotherTypeRecord3 = newRecord(anotherType, 3);
		Record zeTypeRecord4 = newRecord(zeType, 4);
		Record zeTypeRecord5 = newRecord(zeType, 5);
		Record zeTypeRecord6 = newRecord(zeType, 6);

		LogicalSearchCondition condition = from(zeType()).returnAll();

		cache.insert(
				asList(anotherTypeRecord1, anotherTypeRecord2, anotherTypeRecord3, zeTypeRecord4, zeTypeRecord5, zeTypeRecord6),
				WAS_MODIFIED);

		//		assertThat(cache.permanentCaches.get(zeType).queryResultsSize()).isEqualTo(0);
		assertThat(cache.getQueryResultHoldersCount(zeType)).isEqualTo(0);

		cache.insertQueryResults(query(condition).setNumberOfRows(1), asList(zeTypeRecord4));
		//		assertThat(cache.permanentCaches.get(zeType).queryResultsSize()).isEqualTo(0);
		assertThat(cache.getQueryResultHoldersCount(zeType)).isEqualTo(0);

		cache.insertQueryResults(query(condition).setStartRow(4), asList(zeTypeRecord4));
		//		assertThat(cache.permanentCaches.get(zeType).queryResultsSize()).isEqualTo(0);
		assertThat(cache.getQueryResultHoldersCount(zeType)).isEqualTo(0);

		cache.insertQueryResults(query(condition).setPreferAnalyzedFields(true), asList(zeTypeRecord4));
		//		assertThat(cache.permanentCaches.get(zeType).queryResultsSize()).isEqualTo(0);
		assertThat(cache.getQueryResultHoldersCount(zeType)).isEqualTo(0);

		cache.insertQueryResults(query(condition).setHighlighting(true), asList(zeTypeRecord4));
		//		assertThat(cache.permanentCaches.get(zeType).queryResultsSize()).isEqualTo(0);
		assertThat(cache.getQueryResultHoldersCount(zeType)).isEqualTo(0);

		cache.insertQueryResults(query(condition).setResultsProjection(resultsProjection), asList(zeTypeRecord4));
		//		assertThat(cache.permanentCaches.get(zeType).queryResultsSize()).isEqualTo(0);
		assertThat(cache.getQueryResultHoldersCount(zeType)).isEqualTo(0);

		cache.insertQueryResults(query(condition).setQueryBoosts(asList(searchBoost)), asList(zeTypeRecord4));
		//		assertThat(cache.permanentCaches.get(zeType).queryResultsSize()).isEqualTo(0);
		assertThat(cache.getQueryResultHoldersCount(zeType)).isEqualTo(0);

		cache.insertQueryResults(query(condition).setFieldBoosts(asList(searchBoost)), asList(zeTypeRecord4));
		//		assertThat(cache.permanentCaches.get(zeType).queryResultsSize()).isEqualTo(0);
		assertThat(cache.getQueryResultHoldersCount(zeType)).isEqualTo(0);

		cache.insertQueryResults(query(condition).addFieldFacet("field_s"), asList(zeTypeRecord4));
		//		assertThat(cache.permanentCaches.get(zeType).queryResultsSize()).isEqualTo(0);
		assertThat(cache.getQueryResultHoldersCount(zeType)).isEqualTo(0);

		cache.insertQueryResults(query(condition).addQueryFacet("queries", "field_s:*"), asList(zeTypeRecord4));
		//		assertThat(cache.permanentCaches.get(zeType).queryResultsSize()).isEqualTo(0);
		assertThat(cache.getQueryResultHoldersCount(zeType)).isEqualTo(0);

		DataStoreField field = mock(DataStoreField.class);
		when(field.getDataStoreCode()).thenReturn("field_s");

		cache.insertQueryResults(query(condition).computeStatsOnField(field), asList(zeTypeRecord4));
		//		assertThat(cache.permanentCaches.get(zeType).queryResultsSize()).isEqualTo(0);
		assertThat(cache.getQueryResultHoldersCount(zeType)).isEqualTo(0);

		cache.insertQueryResults(query(condition).filteredWithUser(user), asList(zeTypeRecord4));
		//		assertThat(cache.permanentCaches.get(zeType).queryResultsSize()).isEqualTo(0);
		assertThat(cache.getQueryResultHoldersCount(zeType)).isEqualTo(0);

		cache.insertQueryResults(query(condition).filteredWithUserDelete(user), asList(zeTypeRecord4));
		//		assertThat(cache.permanentCaches.get(zeType).queryResultsSize()).isEqualTo(0);
		assertThat(cache.getQueryResultHoldersCount(zeType)).isEqualTo(0);

		cache.insertQueryResults(query(condition).filteredWithUserWrite(user), asList(zeTypeRecord4));
		//		assertThat(cache.permanentCaches.get(zeType).queryResultsSize()).isEqualTo(0);
		assertThat(cache.getQueryResultHoldersCount(zeType)).isEqualTo(0);

		cache.insertQueryResults(query(condition).setReturnedMetadatas(idVersionSchema()), asList(zeTypeRecord4));
		//		assertThat(cache.permanentCaches.get(zeType).queryResultsSize()).isEqualTo(0);
		assertThat(cache.getQueryResultHoldersCount(zeType)).isEqualTo(0);

		LogicalSearchQuery query = query(condition);
		query.getFacetFilters().selectedFieldFacetValue("field_s", "value");
		cache.insertQueryResults(query, asList(zeTypeRecord4));
		//		assertThat(cache.permanentCaches.get(zeType).queryResultsSize()).isEqualTo(0);
		assertThat(cache.getQueryResultHoldersCount(zeType)).isEqualTo(0);

		cache.insertQueryResults(query(condition), asList(zeTypeRecord4));
		//		assertThat(cache.permanentCaches.get(zeType).queryResultsSize()).isEqualTo(1);
		assertThat(cache.getQueryResultHoldersCount(zeType)).isEqualTo(1);
	}

	@Test
	public void whenCacheQueryResultsThenBasedOnSort()
			throws Exception {

		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, withoutIndexByMetadata));
		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(anotherType, withoutIndexByMetadata));
		Record zeTypeRecord4 = newRecord(zeType, 4);
		Record zeTypeRecord5 = newRecord(zeType, 5);
		Record zeTypeRecord6 = newRecord(zeType, 6);

		cache.insert(asList(zeTypeRecord4, zeTypeRecord5, zeTypeRecord6), WAS_MODIFIED);

		assertThatQueryResults(from(anotherType()).returnAll()).isNull();
		assertThatQueryResults(from(zeType()).returnAll()).isNull();
		assertThatQueryResults(fromAllSchemasIn(zeCollection).returnAll()).isNull();

		cache.insertQueryResults(query(from(zeType()).returnAll()).sortAsc(IDENTIFIER),
				asList(zeTypeRecord4, zeTypeRecord5, zeTypeRecord6));
		cache.insertQueryResults(query(from(zeType()).returnAll()).sortDesc(IDENTIFIER),
				asList(zeTypeRecord6, zeTypeRecord5, zeTypeRecord4));
		cache.insertQueryResults(query(from(zeType()).returnAll()).sortAsc(TITLE).sortAsc(IDENTIFIER),
				asList(zeTypeRecord5, zeTypeRecord4, zeTypeRecord6));
		cache.insertQueryResults(query(from(zeType()).returnAll()).sortAsc(TITLE).sortDesc(IDENTIFIER),
				asList(zeTypeRecord5, zeTypeRecord6, zeTypeRecord4));

		assertThatQueryResults(query(from(zeType()).returnAll()).sortAsc(IDENTIFIER))
				.containsExactly(zeTypeRecord4, zeTypeRecord5, zeTypeRecord6);
		assertThatQueryResults(query(from(zeType()).returnAll()).sortDesc(IDENTIFIER))
				.containsExactly(zeTypeRecord6, zeTypeRecord5, zeTypeRecord4);
		assertThatQueryResults(query(from(zeType()).returnAll()).sortAsc(TITLE).sortAsc(IDENTIFIER))
				.containsExactly(zeTypeRecord5, zeTypeRecord4, zeTypeRecord6);
		assertThatQueryResults(query(from(zeType()).returnAll()).sortAsc(TITLE).sortDesc(IDENTIFIER))
				.containsExactly(zeTypeRecord5, zeTypeRecord6, zeTypeRecord4);
	}

	@Test
	public void whenCacheQueryResultsThenBasedOnFreeTextSearch()
			throws Exception {

		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, withoutIndexByMetadata));
		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(anotherType, withoutIndexByMetadata));
		Record zeTypeRecord4 = newRecord(zeType, 4);
		Record zeTypeRecord5 = newRecord(zeType, 5);
		Record zeTypeRecord6 = newRecord(zeType, 6);

		cache.insert(asList(zeTypeRecord4, zeTypeRecord5, zeTypeRecord6), WAS_MODIFIED);

		assertThatQueryResults(from(anotherType()).returnAll()).isNull();
		assertThatQueryResults(from(zeType()).returnAll()).isNull();
		assertThatQueryResults(fromAllSchemasIn(zeCollection).returnAll()).isNull();

		cache.insertQueryResults(query(from(zeType()).returnAll()).setFreeTextQuery("a"),
				asList(zeTypeRecord4, zeTypeRecord5, zeTypeRecord6));
		cache.insertQueryResults(query(from(zeType()).returnAll()).setFreeTextQuery("b"),
				asList(zeTypeRecord6, zeTypeRecord4));
		cache.insertQueryResults(query(from(zeType()).returnAll()).setFreeTextQuery("c"),
				asList(zeTypeRecord5, zeTypeRecord4));
		cache.insertQueryResults(query(from(zeType()).returnAll()).setFreeTextQuery("d"),
				asList(zeTypeRecord4));

		assertThatQueryResults(query(from(zeType()).returnAll()).setFreeTextQuery("a"))
				.containsExactly(zeTypeRecord4, zeTypeRecord5, zeTypeRecord6);
		assertThatQueryResults(query(from(zeType()).returnAll()).setFreeTextQuery("b"))
				.containsExactly(zeTypeRecord6, zeTypeRecord4);
		assertThatQueryResults(query(from(zeType()).returnAll()).setFreeTextQuery("c"))
				.containsExactly(zeTypeRecord5, zeTypeRecord4);
		assertThatQueryResults(query(from(zeType()).returnAll()).setFreeTextQuery("d"))
				.containsExactly(zeTypeRecord4);
	}

	@Test
	public void whenCacheQueryResultsThenBasedOnStatusFilter()
			throws Exception {

		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, withoutIndexByMetadata));
		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(anotherType, withoutIndexByMetadata));
		Record zeTypeRecord4 = newRecord(zeType, 4);
		Record zeTypeRecord5 = newRecord(zeType, 5);
		Record zeTypeRecord6 = newRecord(zeType, 6);

		cache.insert(asList(zeTypeRecord4, zeTypeRecord5, zeTypeRecord6), WAS_MODIFIED);

		assertThatQueryResults(from(anotherType()).returnAll()).isNull();
		assertThatQueryResults(from(zeType()).returnAll()).isNull();
		assertThatQueryResults(fromAllSchemasIn(zeCollection).returnAll()).isNull();

		cache.insertQueryResults(query(from(zeType()).returnAll()).filteredByStatus(StatusFilter.ACTIVES),
				asList(zeTypeRecord4, zeTypeRecord5, zeTypeRecord6));
		cache.insertQueryResults(query(from(zeType()).returnAll()).filteredByStatus(StatusFilter.ALL),
				asList(zeTypeRecord6, zeTypeRecord4));
		cache.insertQueryResults(query(from(zeType()).returnAll()).filteredByStatus(StatusFilter.DELETED),
				asList(zeTypeRecord5, zeTypeRecord4));

		assertThatQueryResults(query(from(zeType()).returnAll()).filteredByStatus(StatusFilter.ACTIVES))
				.containsExactly(zeTypeRecord4, zeTypeRecord5, zeTypeRecord6);
		assertThatQueryResults(query(from(zeType()).returnAll()).filteredByStatus(StatusFilter.ALL))
				.containsExactly(zeTypeRecord6, zeTypeRecord4);
		assertThatQueryResults(query(from(zeType()).returnAll()).filteredByStatus(StatusFilter.DELETED))
				.containsExactly(zeTypeRecord5, zeTypeRecord4);
		assertThatQueryResults(query(from(zeType()).returnAll()))
				.containsExactly(zeTypeRecord6, zeTypeRecord4);
	}

	@Test
	public void givenCachedSearchResultsOfAQueryWhenInsertARecordWithDifferentVersionThenAllQueriesInvalidated()
			throws Exception {
		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, withoutIndexByMetadata));
		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(anotherType, withoutIndexByMetadata));
		Record anotherTypeRecord1 = newRecord(anotherType, 1);
		Record anotherTypeRecord2 = newRecord(anotherType, 2);
		Record anotherTypeRecord3 = newRecord(anotherType, 3);
		Record zeTypeRecord4 = newRecord(zeType, 4);
		Record zeTypeRecord5 = newRecord(zeType, 5);
		Record zeTypeRecord6 = newRecord(zeType, 6);

		Record zeTypeRecord5_v2 = newRecord(zeType, 5, 2);
		Record anotherTypeRecord2_v2 = newRecord(anotherType, 2, 2);

		cache.insert(
				asList(anotherTypeRecord1, anotherTypeRecord2, anotherTypeRecord3, zeTypeRecord4, zeTypeRecord5, zeTypeRecord6),
				WAS_MODIFIED);

		assertThatQueryResults(from(anotherType()).returnAll()).isNull();
		assertThatQueryResults(from(zeType()).returnAll()).isNull();
		assertThatQueryResults(fromAllSchemasIn(zeCollection).returnAll()).isNull();

		cache.insertQueryResults(query(from(zeType()).returnAll()), asList(zeTypeRecord4, zeTypeRecord5, zeTypeRecord6));
		cache.insertQueryResults(query(from(zeType()).where(TITLE).isEqualTo("value1")), asList(zeTypeRecord4, zeTypeRecord6));

		assertThatQueryResults(from(zeType()).returnAll()).containsExactly(zeTypeRecord4, zeTypeRecord5, zeTypeRecord6);
		assertThatQueryResults(from(zeType()).where(TITLE).isEqualTo("value1")).containsExactly(zeTypeRecord4, zeTypeRecord6);
		assertThatQueryResults(from(zeType()).where(TITLE).isEqualTo("value2")).isNull();

		//Inserting a record from another schema - no impacts
		cache.insert(anotherTypeRecord2, WAS_MODIFIED);
		cache.insert(anotherTypeRecord2_v2, WAS_MODIFIED);

		assertThatQueryResults(from(zeType()).returnAll()).containsExactly(zeTypeRecord4, zeTypeRecord5, zeTypeRecord6);
		assertThatQueryResults(from(zeType()).where(TITLE).isEqualTo("value1")).containsExactly(zeTypeRecord4, zeTypeRecord6);
		assertThatQueryResults(from(zeType()).where(TITLE).isEqualTo("value2")).isNull();

		//Inserting a record from the schema with same version - no impact
		cache.insert(zeTypeRecord5, WAS_MODIFIED);

		assertThatQueryResults(from(zeType()).returnAll()).containsExactly(zeTypeRecord4, zeTypeRecord5, zeTypeRecord6);
		assertThatQueryResults(from(zeType()).where(TITLE).isEqualTo("value1")).containsExactly(zeTypeRecord4, zeTypeRecord6);
		assertThatQueryResults(from(zeType()).where(TITLE).isEqualTo("value2")).isNull();

		//Inserting a record from the schema with different version - all queries invalidated
		cache.insert(zeTypeRecord5_v2, WAS_MODIFIED);

		assertThatQueryResults(from(zeType()).returnAll()).isNull();
		assertThatQueryResults(from(zeType()).where(TITLE).isEqualTo("value1")).isNull();
		assertThatQueryResults(from(zeType()).where(TITLE).isEqualTo("value2")).isNull();

	}

	@Test
	public void givenCachedSearchResultsOfAQueryWhenInvalidateARecordThenAllQueriesInvalidated()
			throws Exception {

		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, withoutIndexByMetadata));
		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(anotherType, withoutIndexByMetadata));
		Record anotherTypeRecord1 = newRecord(anotherType, 1);
		Record anotherTypeRecord2 = newRecord(anotherType, 2);
		Record anotherTypeRecord3 = newRecord(anotherType, 3);
		Record zeTypeRecord4 = newRecord(zeType, 4);
		Record zeTypeRecord5 = newRecord(zeType, 5);
		Record zeTypeRecord6 = newRecord(zeType, 6);

		Record zeTypeRecord5_v2 = newRecord(zeType, 5, 2);
		Record anotherTypeRecord2_v2 = newRecord(anotherType, 2, 2);

		cache.insert(
				asList(anotherTypeRecord1, anotherTypeRecord2, anotherTypeRecord3, zeTypeRecord4, zeTypeRecord5, zeTypeRecord6),
				WAS_MODIFIED);

		assertThatQueryResults(from(anotherType()).returnAll()).isNull();
		assertThatQueryResults(from(zeType()).returnAll()).isNull();
		assertThatQueryResults(fromAllSchemasIn(zeCollection).returnAll()).isNull();

		cache.insertQueryResults(query(from(zeType()).returnAll()), asList(zeTypeRecord4, zeTypeRecord5, zeTypeRecord6));
		cache.insertQueryResults(query(from(zeType()).where(TITLE).isEqualTo("value1")), asList(zeTypeRecord4, zeTypeRecord6));

		assertThatQueryResults(from(zeType()).returnAll()).containsExactly(zeTypeRecord4, zeTypeRecord5, zeTypeRecord6);
		assertThatQueryResults(from(zeType()).where(TITLE).isEqualTo("value1")).containsExactly(zeTypeRecord4, zeTypeRecord6);
		assertThatQueryResults(from(zeType()).where(TITLE).isEqualTo("value2")).isNull();

		//Invalidate a record from another schema - queries of ze schema not invalidated
		cache.invalidate(anotherTypeRecord2.getId());

		assertThatQueryResults(from(zeType()).returnAll()).containsExactly(zeTypeRecord4, zeTypeRecord5, zeTypeRecord6);
		assertThatQueryResults(from(zeType()).where(TITLE).isEqualTo("value1")).containsExactly(zeTypeRecord4, zeTypeRecord6);
		assertThatQueryResults(from(zeType()).where(TITLE).isEqualTo("value2")).isNull();

		//Invalidate a record from ze schema - queries of ze schema invalidated
		cache.invalidate(zeTypeRecord4.getId());

		assertThatQueryResults(from(zeType()).returnAll()).isNull();
		assertThatQueryResults(from(zeType()).where(TITLE).isEqualTo("value1")).isNull();
		assertThatQueryResults(from(zeType()).where(TITLE).isEqualTo("value2")).isNull();

	}

	@Test
	public void givenCachedSearchResultsOfAQueryWhenInvalidateAllRecordsOfTypeThenAllQueriesInvalidated()
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

		assertThatQueryResults(from(anotherType()).returnAll()).isNull();
		assertThatQueryResults(from(zeType()).returnAll()).isNull();
		assertThatQueryResults(fromAllSchemasIn(zeCollection).returnAll()).isNull();

		cache.insertQueryResults(query(from(zeType()).returnAll()), asList(zeTypeRecord4, zeTypeRecord5, zeTypeRecord6));
		cache.insertQueryResults(query(from(zeType()).where(TITLE).isEqualTo("value1")), asList(zeTypeRecord4, zeTypeRecord6));

		assertThatQueryResults(from(zeType()).returnAll()).containsExactly(zeTypeRecord4, zeTypeRecord5, zeTypeRecord6);
		assertThatQueryResults(from(zeType()).where(TITLE).isEqualTo("value1")).containsExactly(zeTypeRecord4, zeTypeRecord6);
		assertThatQueryResults(from(zeType()).where(TITLE).isEqualTo("value2")).isNull();

		//Invalidate a record from another schema - queries of ze schema not invalidated
		cache.invalidateRecordsOfType(anotherType);

		assertThatQueryResults(from(zeType()).returnAll()).containsExactly(zeTypeRecord4, zeTypeRecord5, zeTypeRecord6);
		assertThatQueryResults(from(zeType()).where(TITLE).isEqualTo("value1")).containsExactly(zeTypeRecord4, zeTypeRecord6);
		assertThatQueryResults(from(zeType()).where(TITLE).isEqualTo("value2")).isNull();

		//Invalidate a record from ze schema - queries of ze schema invalidated
		cache.invalidateRecordsOfType(zeType);

		assertThatQueryResults(from(zeType()).returnAll()).isNull();
		assertThatQueryResults(from(zeType()).where(TITLE).isEqualTo("value1")).isNull();
		assertThatQueryResults(from(zeType()).where(TITLE).isEqualTo("value2")).isNull();

	}

	@Test
	public void givenCachedSearchResultsOfAQueryWhenInvalidateAllThenAllQueriesInvalidated()
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

		assertThatQueryResults(from(anotherType()).returnAll()).isNull();
		assertThatQueryResults(from(zeType()).returnAll()).isNull();
		assertThatQueryResults(fromAllSchemasIn(zeCollection).returnAll()).isNull();

		cache.insertQueryResults(query(from(zeType()).returnAll()), asList(zeTypeRecord4, zeTypeRecord5, zeTypeRecord6));
		cache.insertQueryResults(query(from(zeType()).where(TITLE).isEqualTo("value1")), asList(zeTypeRecord4, zeTypeRecord6));

		assertThatQueryResults(from(zeType()).returnAll()).containsExactly(zeTypeRecord4, zeTypeRecord5, zeTypeRecord6);
		assertThatQueryResults(from(zeType()).where(TITLE).isEqualTo("value1")).containsExactly(zeTypeRecord4, zeTypeRecord6);
		assertThatQueryResults(from(zeType()).where(TITLE).isEqualTo("value2")).isNull();

		//Invalidate a record from another schema - queries of ze schema not invalidated
		cache.invalidateAll();

		assertThatQueryResults(from(zeType()).returnAll()).isNull();
		assertThatQueryResults(from(zeType()).where(TITLE).isEqualTo("value1")).isNull();
		assertThatQueryResults(from(zeType()).where(TITLE).isEqualTo("value2")).isNull();

	}

	@Test
	public void givenCachedSearchResultsOfAQueryWhenInsertANewRecordThenAllQueriesInvalidated()
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

		assertThatQueryResults(from(anotherType()).returnAll()).isNull();
		assertThatQueryResults(from(zeType()).returnAll()).isNull();
		assertThatQueryResults(fromAllSchemasIn(zeCollection).returnAll()).isNull();

		cache.insertQueryResults(query(from(zeType()).returnAll()), asList(zeTypeRecord4, zeTypeRecord5, zeTypeRecord6));
		cache.insertQueryResults(query(from(zeType()).where(TITLE).isEqualTo("value1")), asList(zeTypeRecord4, zeTypeRecord6));

		assertThatQueryResults(from(zeType()).returnAll()).containsExactly(zeTypeRecord4, zeTypeRecord5, zeTypeRecord6);
		assertThatQueryResults(from(zeType()).where(TITLE).isEqualTo("value1")).containsExactly(zeTypeRecord4, zeTypeRecord6);
		assertThatQueryResults(from(zeType()).where(TITLE).isEqualTo("value2")).isNull();

		//Invalidate a record from another schema - queries of ze schema not invalidated
		cache.insert(newRecord(anotherType, 7), WAS_MODIFIED);

		assertThatQueryResults(from(zeType()).returnAll()).containsExactly(zeTypeRecord4, zeTypeRecord5, zeTypeRecord6);
		assertThatQueryResults(from(zeType()).where(TITLE).isEqualTo("value1")).containsExactly(zeTypeRecord4, zeTypeRecord6);
		assertThatQueryResults(from(zeType()).where(TITLE).isEqualTo("value2")).isNull();

		//Invalidate a record from ze schema - queries of ze schema invalidated
		cache.insert(newRecord(zeType, 8), WAS_MODIFIED);

		assertThatQueryResults(from(zeType()).returnAll()).isNull();
		assertThatQueryResults(from(zeType()).where(TITLE).isEqualTo("value1")).isNull();
		assertThatQueryResults(from(zeType()).where(TITLE).isEqualTo("value2")).isNull();

	}

	@Test
	public void givenCacheWithMetadataIndexesThenCanFindRecordsWithThem()
			throws Exception {

		givenDisabledRecordDuplications = true;

		cache.configureCache(CacheConfig.permanentCacheNotLoadedInitially(zeType, asList(zeTypeCodeMetadata)));
		cache.configureCache(
				CacheConfig.volatileCache(anotherType, 3, asList(anotherTypeCodeMetadata, anotherTypeLegacyIdMetadata)));

		Record zeType1 = newRecord(zeType, 1);
		//when(zeType1.get(zeTypeCodeMetadata)).thenReturn("leNumero1"); //Supertimor
		((TestRecord) zeType1).set(zeTypeCodeMetadata, "leNumero1"); //Supertimor

		Record zeType18 = newRecord(zeType, 2);
		//		when(zeType18.get(zeTypeCodeMetadata)).thenReturn("code18");
		((TestRecord) zeType18).set(zeTypeCodeMetadata, "code18");

		Record zeType42 = newRecord(zeType, 3);
		//		when(zeType42.get(zeTypeCodeMetadata)).thenReturn("ze42");
		((TestRecord) zeType42).set(zeTypeCodeMetadata, "ze42");

		Record anotherType1 = newRecord(anotherType, 4);
		//		when(anotherType1.get(anotherTypeCodeMetadata)).thenReturn("leNumero1"); //Supertimor
		//		when(anotherType1.get(anotherTypeLegacyIdMetadata)).thenReturn("123");
		((TestRecord) anotherType1).set(anotherTypeCodeMetadata, "leNumero1"); //Supertimor
		((TestRecord) anotherType1).set(anotherTypeLegacyIdMetadata, "123");

		Record anotherType18 = newRecord(anotherType, 5);
		//		when(anotherType18.get(anotherTypeCodeMetadata)).thenReturn("code18");
		//		when(anotherType18.get(anotherTypeLegacyIdMetadata)).thenReturn("456");
		((TestRecord) anotherType18).set(anotherTypeCodeMetadata, "code18");
		((TestRecord) anotherType18).set(anotherTypeLegacyIdMetadata, "456");

		Record anotherType42 = newRecord(anotherType, 6);
		//		when(anotherType42.get(anotherTypeCodeMetadata)).thenReturn("ze42");
		//		when(anotherType42.get(anotherTypeLegacyIdMetadata)).thenReturn("789");
		((TestRecord) anotherType42).set(anotherTypeCodeMetadata, "ze42");
		((TestRecord) anotherType42).set(anotherTypeLegacyIdMetadata, "789");

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

		zeType18 = newRecord(zeType, 2);
		//		when(zeType18.get(zeTypeCodeMetadata)).thenReturn("666");
		((TestRecord) zeType18).set(zeTypeCodeMetadata, "666");
		cache.insert(asList(zeType18), WAS_MODIFIED);

		anotherType42 = newRecord(anotherType, 6);
		//		when(anotherType42.get(anotherTypeCodeMetadata)).thenReturn("ze42");
		//		when(anotherType42.get(anotherTypeLegacyIdMetadata)).thenReturn("666");
		((TestRecord) anotherType42).set(anotherTypeCodeMetadata, "ze42");
		((TestRecord) anotherType42).set(anotherTypeLegacyIdMetadata, "666");
		cache.insert(asList(anotherType42), WAS_MODIFIED);

		anotherType1 = newRecord(anotherType, 4);
		//		when(anotherType1.get(anotherTypeCodeMetadata)).thenReturn(null);
		//		when(anotherType1.get(anotherTypeLegacyIdMetadata)).thenReturn("123");
		((TestRecord) anotherType1).set(anotherTypeCodeMetadata, null);
		((TestRecord) anotherType1).set(anotherTypeLegacyIdMetadata, "123");
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

		cache.invalidate("1");
		cache.invalidate("5");
		zeType1 = newRecord(zeType, 1);
		//		when(zeType1.get(zeTypeCodeMetadata)).thenReturn("supertimor"); //leNumero1
		((TestRecord) zeType1).set(zeTypeCodeMetadata, "supertimor"); //leNumero1
		anotherType18 = newRecord(anotherType, 5);
		//		when(anotherType18.get(anotherTypeCodeMetadata)).thenReturn("code18pouces");
		((TestRecord) anotherType18).set(anotherTypeCodeMetadata, "code18pouces");
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
		//		IGNITE : Assume that LRU works properly
		//		assertThat(idOf(cache.getByMetadata(anotherTypeLegacyIdMetadata, "123"))).isNull();
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
				//				RecordHolder holder = cache.cacheById.get(id);
				RecordHolder holder = cache.permanentByIdRecordHoldersCache.get(id);
				if (holder == null) {
					holder = cache.volatileByIdRecordHoldersCache.get(id);
				}
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
		String typeCode = schemaType;
		final Record record = mock(Record.class, schemaType + "-" + id);
		when(record.getId()).thenReturn(id);
		when(record.getSchemaCode()).thenReturn(schema);
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
		//		return record;
		return new TestRecord(schema, typeCode, id, version, givenDisabledRecordDuplications);
	}

	private static class TestRecord implements Record {

		private String id;

		private String schemaCode;

		private String typeCode;

		private long version;

		private boolean givenDisabledRecordDuplications;

		private Boolean logicallyDeleted;

		private Boolean dirty;

		private Boolean fullyLoaded = true;

		private Boolean saved;

		private Map<String, Object> metadatas = new HashMap<>();

		TestRecord(String schemaCode, String typeCode, String id, long version,
				   boolean givenDisabledRecordDuplications) {
			this.id = id;
			this.schemaCode = schemaCode;
			this.typeCode = typeCode;
			this.version = version;
			this.givenDisabledRecordDuplications = givenDisabledRecordDuplications;
		}

		@Override
		public String getId() {
			return id;
		}

		public void setVersion(long version) {
			this.version = version;
		}

		public void setLogicallyDeleted(Boolean logicallyDeleted) {
			this.logicallyDeleted = logicallyDeleted;
		}

		public void setDirty(Boolean dirty) {
			this.dirty = dirty;
		}

		public void setFullyLoaded(Boolean fullyLoaded) {
			this.fullyLoaded = fullyLoaded;
		}

		public void setSaved(Boolean saved) {
			this.saved = saved;
		}

		@Override
		public String getTitle() {
			return null;
		}

		@Override
		public long getVersion() {
			return version;
		}

		@Override
		public long getDataMigrationVersion() {
			return 0;
		}

		@Override
		public String getSchemaCode() {
			return schemaCode;
		}

		@Override
		public String getTypeCode() {
			return typeCode;
		}

		@Override
		public boolean isDirty() {
			return Boolean.TRUE.equals(dirty);
		}

		@Override
		public boolean isFullyLoaded() {
			return Boolean.TRUE.equals(fullyLoaded);
		}

		@Override
		public boolean isModified(Metadata metadata) {
			return false;
		}

		@Override
		public Record set(Metadata metadata, Object value) {
			metadatas.put(metadata.getCode(), value);
			return this;
		}

		@Override
		public Record set(Metadata metadata, Locale locale, Object value) {
			metadatas.put(metadata.getCode(), value);
			return this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T get(Metadata metadata) {
			if (Schemas.LOGICALLY_DELETED_STATUS.getCode().equals(metadata.getCode())) {
				return (T) logicallyDeleted;
			}
			return (T) metadatas.get(metadata.getCode());
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T get(Metadata metadata, Locale locale) {
			if (Schemas.LOGICALLY_DELETED_STATUS.getCode().equals(metadata.getCode())) {
				return (T) logicallyDeleted;
			}
			return (T) metadatas.get(metadata.getCode());
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T get(Metadata metadata, Locale locale, LocalisedRecordMetadataRetrieval mode) {
			if (Schemas.LOGICALLY_DELETED_STATUS.getCode().equals(metadata.getCode())) {
				return (T) logicallyDeleted;
			}
			return (T) metadatas.get(metadata.getCode());
		}

		@Override
		public <T> T getNonNullValueIn(List<Metadata> metadatas) {
			return null;
		}

		@Override
		public <T> List<T> getList(Metadata metadata) {
			return null;
		}

		@Override
		public <T> List<T> getList(Metadata metadata, Locale locale, LocalisedRecordMetadataRetrieval mode) {
			return null;
		}

		@Override
		public MetadataList getModifiedMetadatas(MetadataSchemaTypes schemaTypes) {
			return null;
		}

		@Override
		public boolean isSaved() {
			return Boolean.TRUE.equals(saved) || (saved == null && version != -1);
		}

		@Override
		public String getCollection() {
			return null;
		}

		@Override
		public CollectionInfo getCollectionInfo() {
			return null;
		}

		@Override
		public String getParentId() {
			return null;
		}

		@Override
		public boolean isActive() {
			return false;
		}

		@Override
		public boolean isDisconnected() {
			return false;
		}

		@Override
		public Record getUnmodifiableCopyOfOriginalRecord() {
			Record record = this;
			if (givenDisabledRecordDuplications) {
				return record;
			} else {
				boolean dirty = record.isDirty();
				boolean fullyLoaded = record.isFullyLoaded();
				boolean saved = record.isSaved();
				Boolean logicallyDeleted = record.get(Schemas.LOGICALLY_DELETED_STATUS);
				TestRecord recordCopy = new TestRecord(schemaCode, typeCode, id, version, givenDisabledRecordDuplications);
				recordCopy.setDirty(dirty);
				recordCopy.setFullyLoaded(fullyLoaded);
				recordCopy.setSaved(saved);
				recordCopy.setLogicallyDeleted(logicallyDeleted);
				return recordCopy;
			}
		}

		@Override
		public Record getCopyOfOriginalRecord() {
			Record record = this;
			if (givenDisabledRecordDuplications) {
				return record;
			} else {
				boolean dirty = record.isDirty();
				boolean fullyLoaded = record.isFullyLoaded();
				boolean saved = record.isSaved();
				Boolean logicallyDeleted = record.get(Schemas.LOGICALLY_DELETED_STATUS);
				TestRecord recordCopy = new TestRecord(schemaCode, typeCode, id, version, givenDisabledRecordDuplications);
				recordCopy.setDirty(dirty);
				recordCopy.setFullyLoaded(fullyLoaded);
				recordCopy.setSaved(saved);
				recordCopy.setLogicallyDeleted(logicallyDeleted);
				return recordCopy;
			}
		}

		@Override
		public Record getCopyOfOriginalRecordKeepingOnly(List<Metadata> metadatas) {
			throw new RuntimeException("unsupported");
		}

		@Override
		public String getIdTitle() {
			return null;
		}

		@Override
		public String getSchemaIdTitle() {
			return null;
		}

		@Override
		public void removeAllFieldsStartingWith(String field) {
		}

		@Override
		public void markAsModified(Metadata metadata) {
		}

		@Override
		public boolean changeSchema(MetadataSchema wasSchema, MetadataSchema newSchema) {
			return false;
		}

		@Override
		public <T> void addValueToList(Metadata metadata, T value) {
		}

		@Override
		public <T> void removeValueFromList(Metadata metadata, T value) {
		}

		@Override
		public boolean isOfSchemaType(String type) {
			return false;
		}

		@Override
		public void markAsSaved(long version, MetadataSchema schema) {

		}

		public <T> List<T> getValues(Metadata metadata) {
			Object value = get(metadata);
			if (value == null) {
				return Collections.emptyList();
			} else {
				if (metadata.isMultivalue()) {
					return (List<T>) value;
				} else {
					List<T> values = asList((T) value);
					return values;
				}
			}
		}

		public <T> List<T> getValues(Metadata metadata, Locale locale, LocalisedRecordMetadataRetrieval mode) {
			Object value = get(metadata, locale);
			if (value == null) {
				return Collections.emptyList();
			} else {
				if (metadata.isMultivalue()) {
					return (List<T>) value;
				} else {
					List<T> values = asList((T) value);
					return values;
				}
			}
		}

		@Override
		public Record get() {
			return this;
		}
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

	private ListAssert<Record> assertThatQueryResults(LogicalSearchCondition condition) {
		return assertThatQueryResults(query(condition));
	}

	private ListAssert<Record> assertThatQueryResults(LogicalSearchQuery query) {
		Comparator<Record> idVersionSchemaRecordComparator = new Comparator<Record>() {
			@Override
			public int compare(Record o1, Record o2) {
				String id1 = o1.getId();
				String id2 = o2.getId();
				String schemaCode1 = o1.getSchemaCode();
				String schemaCode2 = o2.getSchemaCode();
				long version1 = o1.getVersion();
				long version2 = o2.getVersion();

				if (id1.equals(id2) && schemaCode1.equals(schemaCode2) && version1 == version2) {
					return 0;
				} else {
					return 1;
				}
			}
		};
		return assertThat(cache.getQueryResults(query)).usingElementComparator(
				idVersionSchemaRecordComparator);
	}

}
