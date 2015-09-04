/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.services.records.cache;

import static com.constellio.sdk.tests.TestUtils.mockManualMetadata;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.cache.RecordsCacheImplRuntimeException.RecordsCacheImplRuntimeException_CacheAlreadyConfigured;
import com.constellio.model.services.records.cache.RecordsCacheImplRuntimeException.RecordsCacheImplRuntimeException_InvalidSchemaTypeCode;
import com.constellio.sdk.tests.ConstellioTest;

public class RecordsCacheImplTest extends ConstellioTest {

	boolean givenDisabledRecordDuplications = false;
	Metadata zeTypeCodeMetadata, anotherTypeCodeMetadata, anotherTypeLegacyIdMetadata;

	List<Metadata> withoutIndexByMetadata = new ArrayList<>();
	String zeType = "zeType";
	String anotherType = "anotherType";

	RecordsCacheImpl cache;

	@Before
	public void setUp()
			throws Exception {
		cache = new RecordsCacheImpl();
		zeTypeCodeMetadata = mockManualMetadata("zeType_default_code", MetadataValueType.STRING);
		anotherTypeCodeMetadata = mockManualMetadata("anotherType_default_code", MetadataValueType.STRING);
		anotherTypeLegacyIdMetadata = mockManualMetadata("anotherType_default_legacyId", MetadataValueType.STRING);
	}

	@Test
	public void whenConfigureCachedTypeThenConfigsSavedInCache()
			throws Exception {

		cache.configureCache(CacheConfig.permanentCache(zeType, withoutIndexByMetadata));
		cache.configureCache(CacheConfig.volatileCache(anotherType, 100, withoutIndexByMetadata));

		assertThat(cache.getConfiguredCaches()).containsOnly(
				CacheConfig.permanentCache(zeType, withoutIndexByMetadata),
				CacheConfig.volatileCache(anotherType, 100, withoutIndexByMetadata)
		);

	}

	@Test(expected = RecordsCacheImplRuntimeException_InvalidSchemaTypeCode.class)
	public void whenConfigureCacheUsingAnInvalidTypeThenIllegalArgumentException()
			throws Exception {

		cache.configureCache(CacheConfig.permanentCache("zeType_default", withoutIndexByMetadata));

	}

	@Test(expected = RecordsCacheImplRuntimeException_CacheAlreadyConfigured.class)
	public void whenConfigureCacheThatIsAlreadyConfiguredThenIllegalArgumentException()
			throws Exception {

		cache.configureCache(CacheConfig.permanentCache(zeType, withoutIndexByMetadata));
		cache.configureCache(CacheConfig.permanentCache(zeType, withoutIndexByMetadata));

	}

	@Test(expected = IllegalArgumentException.class)
	public void whenConfigureCacheUsingNullThenIllegalArgumentException()
			throws Exception {

		cache.configureCache(null);

	}

	@Test
	public void givenPermanentCacheWhenInsertRecordsThenKeepAllOfThem()
			throws Exception {

		cache.configureCache(CacheConfig.permanentCache(zeType, withoutIndexByMetadata));

		Record record1 = cache.insert(newRecord(zeType, 1));
		Record record2 = cache.insert(newRecord(zeType, 2));
		Record record3 = cache.insert(newRecord(zeType, 3));

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

		cache.configureCache(CacheConfig.permanentCache(zeType, withoutIndexByMetadata));

		Record record1 = cache.insert(newRecord(zeType, 1));
		Record record2 = cache.insert(newRecord(anotherType, 2));
		Record record3 = cache.insert(newRecord(anotherType, 3));

		assertThatRecord("1").isInCache();
		assertThatRecords("2", "3").areNotInCache();

	}

	@Test
	public void whenInsertRecordsOfVolatileCachedTypesThenKeptInCache()
			throws Exception {

		cache.configureCache(CacheConfig.volatileCache(zeType, 3, withoutIndexByMetadata));

		Record record1 = cache.insert(newRecord(zeType, 1));
		Record record2 = cache.insert(newRecord(zeType, 2));
		Record record3 = cache.insert(newRecord(zeType, 3));

		assertThatRecords("1", "2", "3").areInCache();

	}

	@Test
	public void givenVolatileCacheIsFullWhenInsertingANewItemThenRemoveOlder()
			throws Exception {

		cache.configureCache(CacheConfig.volatileCache(zeType, 3, withoutIndexByMetadata));

		cache.insert(newRecord(zeType, 1));
		cache.insert(newRecord(zeType, 2));
		cache.insert(newRecord(zeType, 3));
		cache.insert(newRecord(zeType, 4));
		cache.insert(newRecord(zeType, 5));

		assertThatRecords("3", "4", "5").areInCache();
		assertThatRecords("1", "2").areNotInCache();

		cache.insert(newRecord(zeType, 6));
		assertThatRecords("4", "5", "6").areInCache();
		assertThatRecords("1", "2", "3").areNotInCache();

		cache.insert(newRecord(zeType, 1));
		assertThatRecords("1", "5", "6").areInCache();
		assertThatRecords("2", "3", "4").areNotInCache();
	}

	@Test
	public void givenVolatileCacheWhenARecordIsHitThenMovedOnTop()
			throws Exception {

		cache.configureCache(CacheConfig.volatileCache(zeType, 3, withoutIndexByMetadata));

		cache.insert(newRecord(zeType, 1));
		cache.insert(newRecord(zeType, 2));
		cache.insert(newRecord(zeType, 3));

		cache.get("1");
		cache.insert(newRecord(zeType, 4));
		assertThatRecords("1", "3", "4").areInCache();
		assertThatRecord("2").isNotInCache();

		cache.insert(newRecord(zeType, 5));
		assertThatRecords("1", "4", "5").areInCache();
		assertThatRecords("2", "3").areNotInCache();

		cache.insert(newRecord(zeType, 6));
		assertThatRecords("4", "5", "6").areInCache();
		assertThatRecords("1", "2", "3").areNotInCache();

		cache.insert(newRecord(zeType, 1));
		assertThatRecords("1", "5", "6").areInCache();
		assertThatRecords("2", "3", "4").areNotInCache();

	}

	@Test
	public void givenPermanentCacheWhenInsertASameRecordMultipleTimesThenOnlyAddedOnce()
			throws Exception {
		cache.configureCache(CacheConfig.permanentCache(zeType, withoutIndexByMetadata));

		cache.insert(newRecord(zeType, 1));
		cache.insert(newRecord(zeType, 2));
		cache.insert(newRecord(zeType, 2, 2));
		cache.insert(newRecord(zeType, 2, 3));
		cache.insert(newRecord(zeType, 3));
		cache.insert(newRecord(zeType, 3, 2));

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

		cache.insert(newRecord(zeType, 1));
		cache.insert(newRecord(zeType, 2));
		cache.insert(newRecord(zeType, 2, 2));
		cache.insert(newRecord(zeType, 2, 3));
		cache.insert(newRecord(zeType, 3));
		cache.insert(newRecord(zeType, 3, 2));

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

		cache.insert(newRecord(anotherType, 10));
		cache.insert(newRecord(zeType, 1));
		cache.insert(newRecord(zeType, 2));
		cache.insert(newRecord(zeType, 3));
		cache.get("2");
		cache.insert(newRecord(zeType, 3, 2));
		assertThatRecords("1", "2", "3", "10").areInCache();

		cache.invalidateRecordsOfType(zeType);
		assertThatRecords("1", "2", "3").areNotInCache();
		assertThatRecord("10").isInCache();
		assertThat(cache.volatileCaches.get(zeType).recordsInCache).isEqualTo(0);
		assertThat(cache.volatileCaches.get(zeType).holders).isEmpty();

	}

	@Test
	public void givenPermanentCacheWithRecordsWhenInvalidateAllThenAllRemovedAndCacheEmpty()
			throws Exception {

		cache.configureCache(CacheConfig.permanentCache(zeType, withoutIndexByMetadata));
		cache.configureCache(CacheConfig.permanentCache(anotherType, withoutIndexByMetadata));

		cache.insert(newRecord(anotherType, 10));
		cache.insert(newRecord(zeType, 1));
		cache.insert(newRecord(zeType, 2));
		cache.insert(newRecord(zeType, 3));
		cache.get("2");
		cache.insert(newRecord(zeType, 3, 2));
		assertThatRecords("1", "2", "3", "10").areInCache();

		cache.invalidateRecordsOfType(zeType);
		assertThatRecords("1", "2", "3").areNotInCache();
		assertThatRecord("10").isInCache();
		assertThat(cache.permanentCaches.get(zeType).holders).hasSize(3);

	}

	@Test
	public void whenInsertingMultipleRecordsThenRegistered()
			throws Exception {
		cache.configureCache(CacheConfig.permanentCache(zeType, withoutIndexByMetadata));
		cache.configureCache(CacheConfig.volatileCache(anotherType, 3, withoutIndexByMetadata));

		cache.insert(asList(
				newRecord(anotherType, 1),
				newRecord(anotherType, 2),
				newRecord(zeType, 3),
				newRecord(zeType, 4)
		));
		assertThatRecords("1", "2", "3", "4").areInCache();

		cache.insert(asList(
				newRecord(anotherType, 5),
				newRecord(anotherType, 6),
				newRecord(zeType, 7),
				newRecord(zeType, 8)
		));
		assertThatRecords("2", "3", "4", "5", "6", "7", "8").areInCache();
		assertThatRecord("1").isNotInCache();

	}

	@Test
	public void givenVolatileCacheIsNotBigEnoughWhenInsertingThenInsertHasMuchRecordsHasPossible()
			throws Exception {
		cache.configureCache(CacheConfig.permanentCache(zeType, withoutIndexByMetadata));
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
		));
		assertThatRecords("2", "3", "4", "5", "6", "7", "8").areInCache();
		assertThatRecord("1").isNotInCache();

	}

	@Test
	public void givenVolatileCacheWhenInsertingNullThenDoNothing()
			throws Exception {
		cache.configureCache(CacheConfig.volatileCache(zeType, 3, withoutIndexByMetadata));

		cache.insert(newRecord(zeType, 1));
		cache.insert((Record) null);
		cache.insert((List<Record>) null);

		assertThatRecord("1").isInCache();
	}

	@Test
	public void givenVolatileCacheWhenInsertingEmptyListThenDoNothing()
			throws Exception {
		cache.configureCache(CacheConfig.volatileCache(zeType, 3, withoutIndexByMetadata));

		cache.insert(newRecord(zeType, 1));
		cache.insert(new ArrayList<Record>());

		assertThatRecord("1").isInCache();
	}

	@Test
	public void givenVolatileCacheWhenInsertingDirtyRecordThenNotInserted()
			throws Exception {
		cache.configureCache(CacheConfig.volatileCache(zeType, 3, withoutIndexByMetadata));

		Record dirtyRecord = newRecord(zeType, 2);
		when(dirtyRecord.isDirty()).thenReturn(true);

		cache.insert(newRecord(zeType, 1));
		cache.insert(dirtyRecord);

		assertThatRecord("1").isInCache();
		assertThatRecord("2").isNotInCache();
	}

	@Test
	public void givenVolatileCacheWhenInsertingNotFullyLoadedRecordThenNotInsertedAndInvalidateExisting()
			throws Exception {
		cache.configureCache(CacheConfig.volatileCache(zeType, 3, withoutIndexByMetadata));

		Record notFullyLoadedRecord = newRecord(zeType, 2);
		when(notFullyLoadedRecord.isFullyLoaded()).thenReturn(false);

		cache.insert(newRecord(zeType, 1));
		cache.insert(notFullyLoadedRecord);

		assertThatRecord("1").isInCache();
		assertThatRecord("2").isNotInCache();

		Record zeTypeRecordUpdate = newRecord(zeType, 1);
		when(zeTypeRecordUpdate.isFullyLoaded()).thenReturn(false);
		cache.insert(zeTypeRecordUpdate);
		assertThatRecords("1", "2").areNotInCache();
	}

	@Test
	public void givenVolatileCacheWhenInsertingLogicallyDeletedRecordThenNotInserted()
			throws Exception {
		cache.configureCache(CacheConfig.volatileCache(zeType, 3, withoutIndexByMetadata));

		Record logicallyDeletedRecord = newRecord(zeType, 2);
		when(logicallyDeletedRecord.get(Schemas.LOGICALLY_DELETED_STATUS)).thenReturn(true);

		Record restoredRecord = newRecord(zeType, 3);
		when(restoredRecord.get(Schemas.LOGICALLY_DELETED_STATUS)).thenReturn(false);

		cache.insert(newRecord(zeType, 1));
		cache.insert(logicallyDeletedRecord);
		cache.insert(restoredRecord);

		assertThatRecords("1", "3").areInCache();
		assertThatRecord("2").isNotInCache();
	}

	@Test
	public void givenVolatileCacheWhenInsertingUnsavedRecordThenNotInserted()
			throws Exception {
		cache.configureCache(CacheConfig.volatileCache(zeType, 3, withoutIndexByMetadata));

		Record dirtyRecord = newRecord(zeType, 2);
		when(dirtyRecord.isSaved()).thenReturn(false);

		cache.insert(newRecord(zeType, 1));
		cache.insert(dirtyRecord);

		assertThatRecord("1").isInCache();
		assertThatRecord("2").isNotInCache();
	}

	@Test
	public void givenPermanenCacheWhenInsertingNullThenDoNothing()
			throws Exception {
		cache.configureCache(CacheConfig.permanentCache(zeType, withoutIndexByMetadata));

		cache.insert(newRecord(zeType, 1));
		cache.insert((Record) null);
		cache.insert((List<Record>) null);

		assertThatRecord("1").isInCache();
	}

	@Test
	public void givenPermanenCacheWhenInsertingEmptyListThenDoNothing()
			throws Exception {
		cache.configureCache(CacheConfig.permanentCache(zeType, withoutIndexByMetadata));

		cache.insert(newRecord(zeType, 1));
		cache.insert(new ArrayList<Record>());

		assertThatRecord("1").isInCache();
	}

	@Test
	public void givenPermanentCacheWhenInsertingLogicallyDeletedRecordThenNotInserted()
			throws Exception {
		cache.configureCache(CacheConfig.permanentCache(zeType, withoutIndexByMetadata));

		Record logicallyDeletedRecord = newRecord(zeType, 2);
		when(logicallyDeletedRecord.get(Schemas.LOGICALLY_DELETED_STATUS)).thenReturn(true);

		Record restoredRecord = newRecord(zeType, 3);
		when(restoredRecord.get(Schemas.LOGICALLY_DELETED_STATUS)).thenReturn(false);

		cache.insert(newRecord(zeType, 1));
		cache.insert(logicallyDeletedRecord);
		cache.insert(restoredRecord);

		assertThatRecords("1", "3").areInCache();
		assertThatRecord("2").isNotInCache();
	}

	@Test
	public void givenPermanenCacheWhenInsertingDirtyRecordThenNotInserted()
			throws Exception {
		cache.configureCache(CacheConfig.permanentCache(zeType, withoutIndexByMetadata));

		Record dirtyRecord = newRecord(zeType, 2);
		when(dirtyRecord.isDirty()).thenReturn(true);

		cache.insert(newRecord(zeType, 1));
		cache.insert(dirtyRecord);

		assertThatRecord("1").isInCache();
		assertThatRecord("2").isNotInCache();
	}

	@Test
	public void givenPermanentCacheWhenInsertingNotFullyLoadedRecordThenNotInserted()
			throws Exception {
		cache.configureCache(CacheConfig.permanentCache(zeType, withoutIndexByMetadata));

		Record notFullyLoadedRecord = newRecord(zeType, 2);
		when(notFullyLoadedRecord.isFullyLoaded()).thenReturn(false);

		cache.insert(newRecord(zeType, 1));
		cache.insert(notFullyLoadedRecord);

		assertThatRecord("1").isInCache();
		assertThatRecord("2").isNotInCache();

		Record zeTypeRecordUpdate = newRecord(zeType, 1);
		when(zeTypeRecordUpdate.isFullyLoaded()).thenReturn(false);
		cache.insert(zeTypeRecordUpdate);
		assertThatRecords("1", "2").areNotInCache();
	}

	@Test
	public void givenPermanentCacheWhenInsertingUnsavedRecordThenNotInserted()
			throws Exception {
		cache.configureCache(CacheConfig.permanentCache(zeType, withoutIndexByMetadata));

		Record dirtyRecord = newRecord(zeType, 2);
		when(dirtyRecord.isSaved()).thenReturn(false);

		cache.insert(newRecord(zeType, 1));
		cache.insert(dirtyRecord);

		assertThatRecord("1").isInCache();
		assertThatRecord("2").isNotInCache();
	}

	@Test
	public void wheInvalidatingAnUnfoundRecordThenNothingChange()
			throws Exception {

		cache.configureCache(CacheConfig.permanentCache(zeType, withoutIndexByMetadata));
		cache.insert(newRecord(zeType, 1));

		cache.invalidate("2");

		assertThatRecord("1").isInCache();
		assertThatRecord("2").isNotInCache();

	}

	@Test
	public void givenPermanentCacheWhenInvalidatingARecordThenDiminishCounter()
			throws Exception {

		cache.configureCache(CacheConfig.permanentCache(zeType, withoutIndexByMetadata));
		cache.insert(newRecord(zeType, 1));
		cache.insert(newRecord(zeType, 2));
		cache.insert(newRecord(zeType, 3));

		cache.invalidate("2");

		assertThatRecords("1", "3").areInCache();
		assertThatRecord("2").isNotInCache();

	}

	@Test
	public void givenVolatileCacheWhenInvalidatingARecordThenInvalidate()
			throws Exception {
		cache.configureCache(CacheConfig.volatileCache(zeType, 3, withoutIndexByMetadata));
		cache.insert(newRecord(zeType, 1));
		cache.insert(newRecord(zeType, 2));
		cache.insert(newRecord(zeType, 3));

		cache.invalidate("2");

		assertThatRecords("1", "3").areInCache();
		assertThatRecord("2").isNotInCache();
		assertThat(cache.volatileCaches.get(zeType).recordsInCache).isEqualTo(3);
		//At this step, the cache elements counter has an invalid value

		cache.insert(newRecord(zeType, 4));
		cache.insert(newRecord(zeType, 5));
		assertThatRecords("3", "4", "5").areInCache();
		assertThatRecords("1", "2").isNotInCache();
		assertThat(cache.volatileCaches.get(zeType).recordsInCache).isEqualTo(3);
		//The counter has return to normal
	}

	@Test
	public void whenInvalidatingMultipleRecordsThenAllInvalidated()
			throws Exception {
		cache.configureCache(CacheConfig.permanentCache(zeType, withoutIndexByMetadata));
		cache.configureCache(CacheConfig.volatileCache(anotherType, 3, withoutIndexByMetadata));

		cache.insert(asList(
				newRecord(anotherType, 1),
				newRecord(anotherType, 2),
				newRecord(anotherType, 3),
				newRecord(zeType, 4),
				newRecord(zeType, 5),
				newRecord(zeType, 6)
		));

		cache.invalidate(asList("1", "3", "5"));

		assertThatRecords("2", "4", "6").areInCache();
		assertThatRecords("1", "3", "5").areNotInCache();

	}

	@Test
	public void whenInvalidatingPassingNullThenDoesNothing()
			throws Exception {
		cache.configureCache(CacheConfig.permanentCache(zeType, withoutIndexByMetadata));
		cache.configureCache(CacheConfig.volatileCache(anotherType, 3, withoutIndexByMetadata));

		cache.insert(asList(
				newRecord(anotherType, 1),
				newRecord(anotherType, 2),
				newRecord(anotherType, 3),
				newRecord(zeType, 4),
				newRecord(zeType, 5),
				newRecord(zeType, 6)
		));

		cache.invalidate((String) null);
		cache.invalidate((List<String>) null);

		assertThatRecords("1", "2", "3", "4", "5", "6").areInCache();

	}

	@Test
	public void givenCacheWithMetadataIndexesThenCanFindRecordsWithThem()
			throws Exception {

		givenDisabledRecordDuplications = true;

		cache.configureCache(CacheConfig.permanentCache(zeType, asList(zeTypeCodeMetadata)));
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

		cache.insert(asList(zeType1, zeType18, zeType42, anotherType1, anotherType18, anotherType42));

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
		when(zeType18.get(zeTypeCodeMetadata)).thenReturn("666");
		cache.insert(asList(zeType18));

		anotherType42 = newRecord(anotherType, 6);
		when(anotherType42.get(anotherTypeCodeMetadata)).thenReturn("ze42");
		when(anotherType42.get(anotherTypeLegacyIdMetadata)).thenReturn("666");
		cache.insert(asList(anotherType42));

		anotherType1 = newRecord(anotherType, 4);
		when(anotherType1.get(anotherTypeCodeMetadata)).thenReturn(null);
		when(anotherType1.get(anotherTypeLegacyIdMetadata)).thenReturn("123");
		cache.insert(asList(anotherType1));

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
		when(zeType1.get(zeTypeCodeMetadata)).thenReturn("supertimor"); //leNumero1
		anotherType18 = newRecord(anotherType, 5);
		when(anotherType18.get(anotherTypeCodeMetadata)).thenReturn("code18pouces");
		cache.insert(asList(zeType1));
		cache.insert(asList(anotherType18));

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
					return newRecord(schemaType, id, version);
				}
			}
		});
		return record;
	}

}
