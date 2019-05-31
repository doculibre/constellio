package com.constellio.model.services.records.cache;

import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.RecordCacheType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordImplRuntimeException;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesImpl;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ThirdSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import com.constellio.sdk.tests.setups.SchemaShortcuts;
import org.junit.Before;
import org.junit.ComparisonFailure;
import org.junit.Test;

import java.util.List;

import static com.constellio.data.dao.services.cache.InsertionReason.WAS_MODIFIED;
import static com.constellio.model.services.records.cache.CacheConfig.permanentCache;
import static com.constellio.model.services.records.cache.CacheConfig.volatileCache;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsUnique;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class PermanentRecordsCacheAcceptanceTest extends ConstellioTest {

	Transaction transaction;
	User adminInZeCollection, adminInAnotherCollection;

	TestRecord record1, record2, record3, record4, record5, record18, record42;

	TestsSchemasSetup zeCollectionSchemas = new TestsSchemasSetup(zeCollection).withSecurityFlag(false);
	ZeSchemaMetadatas zeCollectionSchemaWithVolatileCache = zeCollectionSchemas.new ZeSchemaMetadatas();
	AnotherSchemaMetadatas zeCollectionSchemaWithPermanentCache = zeCollectionSchemas.new AnotherSchemaMetadatas();
	ThirdSchemaMetadatas zeCollectionSchemaWithoutCache = zeCollectionSchemas.new ThirdSchemaMetadatas();

	String anotherCollection = "anotherCollection";
	TestsSchemasSetup anotherCollectionSchemas = new TestsSchemasSetup(anotherCollection).withSecurityFlag(false);
	ZeSchemaMetadatas anotherCollectionSchemaWithoutCache = anotherCollectionSchemas.new ZeSchemaMetadatas();
	ThirdSchemaMetadatas anotherCollectionSchemaWithVolatileCache = anotherCollectionSchemas.new ThirdSchemaMetadatas();

	RecordsCaches recordsCaches;
	RecordsCache zeCollectionRecordsCache;
	RecordsCache anotherCollectionRecordsCache;

	UserServices userServices;

	RecordServices recordServices;
	RecordServicesImpl cachelessRecordServices;
	SearchServices searchServices;

	StatsBigVaultServerExtension queriesListener;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withAllTestUsers(),
				withCollection(anotherCollection).withAllTestUsers()
		);
		inCollection(zeCollection).giveWriteAccessTo(admin);
		inCollection(anotherCollection).giveWriteAccessTo(admin);

		defineSchemasManager()
				.using(zeCollectionSchemas.withAStringMetadata(whichIsUnique).withAnotherStringMetadata());
		defineSchemasManager().using(anotherCollectionSchemas);

		adminInZeCollection = getModelLayerFactory().newUserServices().getUserInCollection("admin", zeCollection);
		adminInAnotherCollection = getModelLayerFactory().newUserServices().getUserInCollection("admin", anotherCollection);

		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		cachelessRecordServices = getModelLayerFactory().newCachelessRecordServices();

		userServices = getModelLayerFactory().newUserServices();
		recordsCaches = getModelLayerFactory().getRecordsCaches();
		zeCollectionRecordsCache = getModelLayerFactory().getRecordsCaches().getCache(zeCollection);
		anotherCollectionRecordsCache = getModelLayerFactory().getRecordsCaches().getCache(anotherCollection);

		RecordsCache collection1Cache = getModelLayerFactory().getRecordsCaches().getCache(zeCollection);
		RecordsCache collection2Cache = getModelLayerFactory().getRecordsCaches().getCache(anotherCollection);
		collection1Cache.configureCache(volatileCache(zeCollectionSchemaWithVolatileCache.type(), 4));
		collection1Cache.configureCache(permanentCache(zeCollectionSchemaWithPermanentCache.type()));
		collection2Cache.configureCache(volatileCache(anotherCollectionSchemaWithVolatileCache.type(), 3));

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(zeCollectionSchemaWithVolatileCache.type().getCode()).setRecordCacheType(RecordCacheType.SUMMARY_CACHED_WITH_VOLATILE);
				types.getSchemaType(zeCollectionSchemaWithPermanentCache.type().getCode()).setRecordCacheType(RecordCacheType.FULLY_CACHED);
			}
		});

		DataLayerSystemExtensions extensions = getDataLayerFactory().getExtensions().getSystemWideExtensions();
		queriesListener = new StatsBigVaultServerExtension();
		extensions.getBigVaultServerExtension().add(queriesListener);

	}

	@Test
	public void givenPermanentCacheWhenInsertingARecordAndUpdatePassedRecordAndTheOnePassedToTheCacheThenDoesNotAffectTheCachedRecord()
			throws Exception {

		Transaction transaction = new Transaction();
		Record record = transaction.add(newRecordOf("1", zeCollectionSchemaWithPermanentCache).withTitle("original title"));
		record.set(Schemas.LEGACY_ID, "zeLegacyId");
		recordServices.add(record);

		recordsCaches.invalidateAll();

		recordsCaches.getCache(record.getCollection()).insert(record, WAS_MODIFIED);
		record.set(Schemas.TITLE, "modified title");
		record.set(Schemas.TITLE, "modified title");
		recordsCaches.getCache(record.getCollection()).get(record.getId()).set(Schemas.TITLE, "modified title");

		assertThat(recordsCaches.getRecord(record.getId()).<String>get(Schemas.TITLE)).isEqualTo("original title");
		assertThat(recordsCaches.getCache(record.getCollection())
				.getByMetadata(zeCollectionSchemaWithPermanentCache.metadata(Schemas.LEGACY_ID.getLocalCode()), "zeLegacyId")
				.<String>get(Schemas.TITLE)).isEqualTo("original title");
		assertThat(recordsCaches.getRecord(record.getId()).isDirty()).isFalse();
		assertThat(record.<String>get(Schemas.TITLE)).isEqualTo("modified title");
		assertThat(record.isDirty()).isTrue();
		assertThat(record.<String>get(Schemas.TITLE)).isEqualTo("modified title");
		assertThat(record.isDirty()).isTrue();

		String type = zeCollectionSchemaWithPermanentCache.typeCode();
		assertThat(recordsCaches.getCache(zeCollection).getAllValues(type)).extracting("id").containsOnly("1");
		recordsCaches.getCache(zeCollection).getAllValues(type).get(0).set(Schemas.TITLE, "test");

		assertThat(recordsCaches.getCache(zeCollection).getAllValuesInUnmodifiableState(type)).extracting("id").containsOnly("1");
		try {
			recordsCaches.getCache(zeCollection).getAllValuesInUnmodifiableState(type).get(0).set(Schemas.TITLE, "test");
			fail("Record should be unmodifiable");
		} catch (RecordImplRuntimeException.RecordImplException_RecordIsUnmodifiable e) {
			//OK
		}
	}

	@Test
	public void givenVolatileCacheWhenInsertingARecordAndUpdatePassedRecordAndTheOnePassedToTheCacheThenDoesNotAffectTheCachedRecord()
			throws Exception {

		Transaction transaction = new Transaction();
		Record record = transaction.add(newRecordOf("1", zeCollectionSchemaWithVolatileCache).withTitle("original title"));
		record.set(Schemas.LEGACY_ID, "zeLegacyId");
		recordServices.add(record);

		recordsCaches.invalidateAll();

		recordsCaches.getCache(record.getCollection()).insert(record, WAS_MODIFIED);

		record.set(Schemas.TITLE, "modified title");
		record.set(Schemas.TITLE, "modified title");
		assertThat(recordsCaches.getCache(record.getCollection()).get(record.getId())).isNotNull();
		recordsCaches.getCache(record.getCollection()).get(record.getId()).set(Schemas.TITLE, "modified title");

		assertThat(recordsCaches.getRecord(record.getId()).<String>get(Schemas.TITLE)).isEqualTo("original title");
		assertThat(recordsCaches.getCache(record.getCollection())
				.getByMetadata(zeCollectionSchemaWithVolatileCache.metadata(Schemas.LEGACY_ID.getLocalCode()), "zeLegacyId")
				.<String>get(Schemas.TITLE)).isEqualTo("original title");
		assertThat(record.<String>get(Schemas.TITLE)).isEqualTo("modified title");
		assertThat(record.isDirty()).isTrue();
		assertThat(record.<String>get(Schemas.TITLE)).isEqualTo("modified title");
		assertThat(record.isDirty()).isTrue();

	}

	@Test
	public void givenVolatileCacheWhenGetMetadataByLegacyIdThenObtainACopy()
			throws Exception {

		Transaction transaction = new Transaction();
		Record record = transaction.add(newRecordOf("1", zeCollectionSchemaWithVolatileCache).withTitle("original title"));
		record.set(Schemas.LEGACY_ID, "zeLegacyId");
		recordServices.add(record);

		recordsCaches.invalidateAll();

		recordsCaches.getCache(record.getCollection()).insert(record, WAS_MODIFIED);

		record.set(Schemas.TITLE, "modified title");
		record.set(Schemas.TITLE, "modified title");
		recordsCaches.getCache(record.getCollection()).get(record.getId()).set(Schemas.TITLE, "modified title");

		assertThat(recordsCaches.getRecord(record.getId()).<String>get(Schemas.TITLE)).isEqualTo("original title");
		assertThat(recordsCaches.getRecord(record.getId()).isDirty()).isFalse();
		assertThat(record.<String>get(Schemas.TITLE)).isEqualTo("modified title");
		assertThat(record.isDirty()).isTrue();
		assertThat(record.<String>get(Schemas.TITLE)).isEqualTo("modified title");
		assertThat(record.isDirty()).isTrue();

	}

	@Test
	public void givenRecordLogicallyDeletedThenModifiedInCache()
			throws Exception {

		givenTestRecords();
		loadAllRecordsInCaches();

		assertThatRecords("2", "3", "4").areInCache();
		assertThatRecords("1", "5").areNotInCache();

		recordServices.logicallyDelete(recordServices.getDocumentById("2"), adminInZeCollection);
		assertThatRecords("2", "3", "4").areInCache();
		assertThatRecords("1", "5").areNotInCache();

		recordServices.update(recordServices.getDocumentById("3").set(Schemas.LOGICALLY_DELETED_STATUS, true),
				adminInZeCollection);
		assertThatRecords("2", "3", "4").areInCache();
		assertThatRecords("1", "5").areNotInCache();

		recordsCaches.getCache(zeCollection).invalidateAll();

		recordServices.getDocumentById("2");
		recordServices.getDocumentById("3");
		recordServices.getDocumentById("4");
		assertThatRecords("2", "3", "4").areInCache();
		assertThatRecords("1", "5").areNotInCache();
	}

	@Test
	public void whenAddUpdateRecordsThenKeptInCache()
			throws Exception {

		givenTestRecords();
		loadAllRecordsInCaches();

		assertThatRecords("2", "3", "4").areInCache();
		assertThatRecords("1", "5").areNotInCache();

		queriesListener.clear();
		assertThatGetDocumentsByIdReturnEqualRecord(record1, record2, record3, record4, record5);
		assertThat(queriesListener.byIds).containsOnlyOnce("1", "5");

		transaction = new Transaction();
		transaction.add(record1.set(Schemas.TITLE, "a2"));
		transaction.add(record2.set(Schemas.TITLE, "b2"));
		transaction.add(record3.set(Schemas.TITLE, "c2"));
		recordServices.execute(transaction);

		recordServices.add(record4.set(Schemas.TITLE, "d2"));
		recordServices.add(record5.set(Schemas.TITLE, "e2"));

		queriesListener.clear();
		assertThatGetDocumentsByIdReturnEqualRecord(record1, record2, record3, record4, record5);
		assertThat(queriesListener.byIds).containsOnlyOnce("1", "5");
	}

	@Test
	public void whenInvalidateAllThenAllInvalidated()
			throws Exception {
		givenTestRecords();
		loadAllRecordsInCaches();

		assertThatRecords("2", "3", "4").areInCache();
		assertThatRecords("1", "5").areNotInCache();

		recordsCaches.invalidateAll();
		assertThatRecords("1", "2", "3", "4", "5").areNotInCache();
	}

	@Test
	public void whenGetRecordByIdThenReturnUseCaches()
			throws Exception {

		givenTestRecords();

		recordServices.getDocumentById("1");
		recordServices.getDocumentById("2");
		recordServices.getDocumentById("3");
		recordServices.getDocumentById("4");
		recordServices.getDocumentById("5");
		assertThat(queriesListener.byIds).containsOnlyOnce("1", "2", "3", "4", "5");
		assertThatRecords("2", "3", "4").areInCache();
		assertThatRecords("1", "5").areNotInCache();

		recordServices.getDocumentById("2");
		recordServices.getDocumentById("3");
		recordServices.getDocumentById("4");
		assertThat(queriesListener.byIds).containsOnlyOnce("2", "3", "4");
	}

	@Test
	public void whenGetRecordByMetadataThenKeptInCache()
			throws Exception {

		givenTestRecords();
		queriesListener.clear();
		Metadata stringMetadata = zeCollectionSchemaWithVolatileCache.stringMetadata();

		assertThat(recordServices.getRecordByMetadata(stringMetadata, "code3").getId()).isEqualTo("3");
		assertThat(recordServices.getRecordByMetadata(stringMetadata, "code18").getId()).isEqualTo("18");
		assertThat(recordServices.getRecordByMetadata(stringMetadata, "code42").getId()).isEqualTo("42");
		assertThat(queriesListener.queries).hasSize(0);

		assertThat(recordServices.getRecordByMetadata(stringMetadata, "code3").getId()).isEqualTo("3");
		assertThat(recordServices.getRecordByMetadata(stringMetadata, "code18").getId()).isEqualTo("18");
		assertThat(recordServices.getRecordByMetadata(stringMetadata, "code42").getId()).isEqualTo("42");
		assertThat(queriesListener.queries).hasSize(0);
	}

	@Test
	public void whenUserByUsernameThenKeptInCache()
			throws Exception {

		givenTestRecords();
		queriesListener.clear();

		int initialQueriesSize = queriesListener.queries.size();
		userServices.getUserInCollection("gandalf", zeCollection);
		assertThat(queriesListener.queries).hasSize(initialQueriesSize + 2);

		userServices.getUser("gandalf");
		assertThat(queriesListener.queries).hasSize(initialQueriesSize + 2);

		userServices.getUserInCollection("gandalf", zeCollection);
		assertThat(queriesListener.queries).hasSize(initialQueriesSize + 2);
	}

	@Test
	public void whenGetRecordByIdForUserThenKeptInCache()
			throws Exception {

		givenTestRecords();

		recordServices.getDocumentById("1", adminInZeCollection);
		recordServices.getDocumentById("2", adminInZeCollection);
		recordServices.getDocumentById("3", adminInZeCollection);
		recordServices.getDocumentById("4", adminInAnotherCollection);
		recordServices.getDocumentById("5", adminInAnotherCollection);
		recordServices.getDocumentById("2", adminInZeCollection);
		recordServices.getDocumentById("3", adminInZeCollection);
		recordServices.getDocumentById("4", adminInAnotherCollection);
		assertThatRecords("2", "3", "4").areInCache();
		assertThatRecords("1", "5").areNotInCache();
		assertThat(queriesListener.byIds).containsOnlyOnce("2", "3", "4");
	}

	@Test
	public void givenASchemaTypeIsNotCachedThenCanUpdateWithDelayedFlushing()
			throws Exception {

		givenTestRecords();

		Transaction transaction = new Transaction().setRecordFlushing(RecordsFlushing.LATER());
		transaction.update(record1.withTitle("modified1"));
		cachelessRecordServices.execute(transaction);
		cachelessRecordServices.flush();

		//Can add withing 2 seconds for a record
		transaction = new Transaction().setRecordFlushing(RecordsFlushing.WITHIN_SECONDS(2));
		transaction.update(record1.withTitle("modified2"));
		cachelessRecordServices.execute(transaction);
		cachelessRecordServices.flush();

	}

	@Test
	public void whenInsertingCacheRecordWithEmptyValueThenRetrievedAsNull()
			throws Exception {

		givenTestRecords();

		Transaction transaction = new Transaction();
		transaction.update(newRecordOf("zeUltimateRecordWithEmptyValue", zeCollectionSchemaWithVolatileCache).set(
				zeCollectionSchemaWithVolatileCache.anotherStringMetadata(), ""));
		cachelessRecordServices.execute(transaction);

		Record record = getModelLayerFactory().newRecordServices().getDocumentById("zeUltimateRecordWithEmptyValue");
		assertThat(record.<String>get(zeCollectionSchemaWithVolatileCache.anotherStringMetadata())).isNull();

	}

	//	@Test
	//	public void whenGetRecordsByIdThenKeptInCache()
	//			throws Exception {
	//
	//		givenTestRecords();
	//		assertThatRecords("1", "2", "3", "4", "5").areNotInCache();
	//
	//		assertThat(recordServices.getRecordsById(zeCollection, asList("1", "2", "3"))).extracting("id")
	//				.containsOnly("1", "2", "3");
	//		assertThat(recordServices.getRecordsById(anotherCollection, asList("4", "5"))).extracting("id")
	//				.containsOnly("4", "5");
	//
	//		assertThatRecords("2", "3", "4").areInCache();
	//		assertThatRecords("1", "5").areNotInCache();
	//		assertThat(queriesListener.byIds).containsOnlyOnce("1", "2", "3", "4", "5").hasSize(5);
	//
	//		assertThat(recordServices.getRecordsById(zeCollection, asList("1", "2", "3"))).extracting("id")
	//				.containsOnly("1", "2", "3");
	//		assertThat(recordServices.getRecordsById(anotherCollection, asList("4", "5"))).extracting("id")
	//				.containsOnly("4", "5");
	//		assertThat(queriesListener.byIds).containsOnlyOnce("2", "3", "4").hasSize(7);
	//
	//		resetCacheAndQueries();
	//		assertThat(recordServices.getRecordsById(zeCollection, asList("1", "2"))).extracting("id")
	//				.containsOnly("1", "2");
	//		assertThatRecord("2").isInCache();
	//		assertThatRecords("1", "3").areNotInCache();
	//		assertThat(queriesListener.byIds).isEqualTo(asList("1", "2"));
	//
	//		assertThat(recordServices.getRecordsById(zeCollection, asList("1", "2", "3"))).extracting("id")
	//				.containsOnly("1", "2", "3");
	//		assertThatRecords("2", "3").areInCache();
	//		assertThatRecords("1").areNotInCache();
	//		assertThat(queriesListener.byIds).isEqualTo(asList("1", "2", "1", "3"));
	//
	//		try {
	//			recordServices.getRecordsById(anotherCollection, asList("1"));
	//		} catch (RecordServicesRuntimeException e) {
	//
	//		}
	//
	//	}

	//-----------------------------------------------------------------

	private void resetCacheAndQueries() {
		recordsCaches.invalidateAll();
		queriesListener.clear();
		assertThatRecords("1", "2", "3", "4", "5").areNotInCache();
	}

	private void loadAllRecordsInCaches() {
		recordServices.getDocumentById("1");
		recordServices.getDocumentById("2");
		recordServices.getDocumentById("3");
		recordServices.getDocumentById("4");
		recordServices.getDocumentById("5");
	}

	private void givenTestRecords()
			throws Exception {
		Transaction transaction = new Transaction();
		record1 = (TestRecord) transaction.add(newRecordOf("1", zeCollectionSchemaWithoutCache).withTitle("a"));
		record2 = (TestRecord) transaction.add(newRecordOf("2", zeCollectionSchemaWithPermanentCache).withTitle("b"));
		record3 = (TestRecord) transaction.add(newRecordOf("3", zeCollectionSchemaWithVolatileCache).withTitle("c")
				.set(zeCollectionSchemaWithVolatileCache.stringMetadata(), "code3"));
		transaction.add(newRecordOf("18", zeCollectionSchemaWithVolatileCache).withTitle("c")
				.set(zeCollectionSchemaWithVolatileCache.stringMetadata(), "code18"));
		transaction.add(newRecordOf("42", zeCollectionSchemaWithVolatileCache).withTitle("c")
				.set(zeCollectionSchemaWithVolatileCache.stringMetadata(), "code42"));
		recordServices.execute(transaction);
		recordServices.add(record4 = newRecordOf("4", anotherCollectionSchemaWithVolatileCache).withTitle("d"));
		recordServices.add(record5 = newRecordOf("5", anotherCollectionSchemaWithoutCache).withTitle("e"));

		resetCacheAndQueries();
	}

	@Test
	public void testZeTestUtilityMethods()
			throws Exception {

		Transaction transaction = new Transaction();
		TestRecord record1 = (TestRecord) transaction.add(newRecordOf("1", zeCollectionSchemaWithoutCache).withTitle("a"));
		TestRecord record2 = (TestRecord) transaction
				.add(newRecordOf("2", zeCollectionSchemaWithPermanentCache).withTitle("b"));
		TestRecord record3 = (TestRecord) transaction
				.add(newRecordOf("3", zeCollectionSchemaWithPermanentCache).withTitle("c"));
		recordServices.execute(transaction);

		resetCacheAndQueries();

		recordServices.getDocumentById("2");
		recordServices.getDocumentById("3");

		assertThatRecord("2").isInCache();
		assertThatRecords("2", "3").areInCache();

		try {
			assertThatRecords("1", "2", "3").areInCache();
			fail("Exception expected");
		} catch (ComparisonFailure e) {
			//OK
		}

		try {
			assertThatRecords("2", "3", "1").areInCache();
			fail("Exception expected");
		} catch (ComparisonFailure e) {
			//OK
		}

		try {
			assertThatRecords("1", "inexistentRecord").areInCache();
			fail("Exception expected");
		} catch (ComparisonFailure e) {
			//OK
		}

		try {
			assertThatRecords("2", "inexistentRecord").areInCache();
			fail("Exception expected");
		} catch (ComparisonFailure e) {
			//OK
		}

		assertThatRecord("1").isNotInCache();
		assertThatRecords("1", "4").areNotInCache();
		assertThatRecords("1", "inexistentRecord").areNotInCache();

		try {
			assertThatRecords("2", "3").areNotInCache();
			fail("Exception expected");
		} catch (ComparisonFailure e) {
			//OK
		}

		try {
			assertThatRecords("1", "2").areNotInCache();
			fail("Exception expected");
		} catch (ComparisonFailure e) {
			//OK
		}

		try {
			assertThatRecords("2", "3", "1").areNotInCache();
			fail("Exception expected");
		} catch (ComparisonFailure e) {
			//OK
		}

		assertThat(queriesListener.queries).isNotEmpty();

		try {
			recordServices.getDocumentById("42");
		} catch (NoSuchRecordWithId e) {
			//OK
		}
		try {
			recordServices.getDocumentById("is");
		} catch (NoSuchRecordWithId e) {
			//OK
		}
		try {
			recordServices.getDocumentById("magic");
		} catch (NoSuchRecordWithId e) {
			//OK
		}

		assertThat(queriesListener.byIds).containsOnlyOnce("42", "is", "magic");
		queriesListener.clear();
		assertThat(queriesListener.queries).isEmpty();
		assertThat(queriesListener.byIds).isEmpty();

		Record modifiedRecord1 = record1.getCopyOfOriginalRecord().set(Schemas.TITLE, "modified title");
		recordServices.update(modifiedRecord1);

		assertThatGetDocumentsByIdReturnEqualRecord(modifiedRecord1);

		try {
			assertThatGetDocumentsByIdReturnEqualRecord(record1);
		} catch (ComparisonFailure e) {
			//OK
		}

		assertThat(recordServices.getDocumentById(record1.getId()).getVersion())
				.isNotEqualTo(record1.getVersion()).isEqualTo(modifiedRecord1.getVersion());
	}

	private void assertThatGetDocumentsByIdReturnEqualRecord(Record... records) {
		for (Record record : records) {
			Record returnedRecord = recordServices.getDocumentById(record.getId());
			assertThat(returnedRecord.getVersion()).isEqualTo(record.getVersion());
		}
	}

	private void getRecordsById(String collection, List<String> ids) {

	}

	private OngoingEntryAssertion assertThatRecord(String id) {
		return new OngoingEntryAssertion(asList(id));
	}

	private OngoingEntryAssertion assertThatRecords(String... ids) {
		return new OngoingEntryAssertion(asList(ids));
	}

	private TestRecord newRecordOf(String id, SchemaShortcuts schema) {
		return new TestRecord(schema, id);
	}

	private TestRecord newRecordOf(SchemaShortcuts schema) {
		return new TestRecord(schema);
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
				boolean isCached = recordsCaches.isCached(id);
				assertThat(isCached).describedAs("Record with id '" + id + "' is expected to be in cache").isTrue();
			}
		}

		private void isNotInCache() {
			areNotInCache();
		}

		private void areNotInCache() {
			for (String id : ids) {
				boolean isCached = recordsCaches.isCached(id);
				assertThat(isCached).describedAs("Record with id '" + id + "' is expected to not be in cache").isFalse();
			}
		}
	}

}
