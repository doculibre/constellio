package com.constellio.model.services.records.cache;

import static com.constellio.model.services.records.cache.CacheConfig.permanentCache;
import static com.constellio.model.services.records.cache.CacheConfig.volatileCache;
import static com.constellio.model.services.records.cache.InsertionReason.WAS_MODIFIED;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsUnique;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Before;
import org.junit.ComparisonFailure;
import org.junit.Test;

import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.events.EventBusManager;
import com.constellio.data.events.SDKEventBusSendingService;
import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordImplRuntimeException;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesImpl;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ThirdSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import com.constellio.sdk.tests.setups.SchemaShortcuts;

public class EventRecordsCacheAcceptanceTest extends ConstellioTest {

	Transaction transaction;
	User adminInZeCollection, adminInAnotherCollection;

	TestRecord uncachedRecord1, uncachedRecord2, permanentRecord1, permanentRecord2, volatileRecord3, volatileRecord4, volatileRecord1, volatileRecord2;

	TestsSchemasSetup zeCollectionSchemas = new TestsSchemasSetup(zeCollection).withSecurityFlag(false);
	ZeSchemaMetadatas zeCollectionSchemaWithVolatileCache = zeCollectionSchemas.new ZeSchemaMetadatas();
	AnotherSchemaMetadatas zeCollectionSchemaWithPermanentCache = zeCollectionSchemas.new AnotherSchemaMetadatas();
	ThirdSchemaMetadatas zeCollectionSchemaWithoutCache = zeCollectionSchemas.new ThirdSchemaMetadatas();

	String anotherCollection = "anotherCollection";
	TestsSchemasSetup anotherCollectionSchemas = new TestsSchemasSetup(anotherCollection).withSecurityFlag(false);
	ZeSchemaMetadatas anotherCollectionSchemaWithoutCache = anotherCollectionSchemas.new ZeSchemaMetadatas();
	ThirdSchemaMetadatas anotherCollectionSchemaWithVolatileCache = anotherCollectionSchemas.new ThirdSchemaMetadatas();

	RecordsCaches recordsCaches;
	RecordsCaches otherInstanceRecordsCaches;
	RecordsCache zeCollectionRecordsCache;
	RecordsCache anotherCollectionRecordsCache;

	UserServices userServices;

	RecordServices recordServices, otherInstanceRecordServices;
	RecordServicesImpl cachelessRecordServices;
	SearchServices searchServices;

	StatsBigVaultServerExtension queriesListener;
	StatsBigVaultServerExtension otherSystemQueriesListener;

	@Before
	public void setUp()
			throws Exception {

		givenCollection("zeCollection").withAllTestUsers();
		givenCollection("anotherCollection").withAllTestUsers();

		inCollection(zeCollection).giveWriteAccessTo(admin);
		inCollection(anotherCollection).giveWriteAccessTo(admin);

		defineSchemasManager()
				.using(zeCollectionSchemas.withAStringMetadata(whichIsUnique).withAnotherStringMetadata()
						.withAStringMetadataInAnotherSchema(whichIsUnique));
		defineSchemasManager().using(anotherCollectionSchemas.withAStringMetadataInAnotherSchema(whichIsUnique));

		adminInZeCollection = getModelLayerFactory().newUserServices().getUserInCollection("admin", zeCollection);
		adminInAnotherCollection = getModelLayerFactory().newUserServices().getUserInCollection("admin", anotherCollection);

		ModelLayerFactory otherModelLayerFactory = getModelLayerFactory("other");

		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		cachelessRecordServices = getModelLayerFactory().newCachelessRecordServices();

		otherInstanceRecordServices = otherModelLayerFactory.newRecordServices();

		userServices = getModelLayerFactory().newUserServices();
		recordsCaches = getModelLayerFactory().getRecordsCaches();
		zeCollectionRecordsCache = getModelLayerFactory().getRecordsCaches().getCache(zeCollection);
		anotherCollectionRecordsCache = getModelLayerFactory().getRecordsCaches().getCache(anotherCollection);

		otherInstanceRecordsCaches = otherModelLayerFactory.getRecordsCaches();

		for (ModelLayerFactory aModelLayerFactory : asList(getModelLayerFactory(), otherModelLayerFactory)) {
			RecordsCache collection1Cache = aModelLayerFactory.getRecordsCaches().getCache(zeCollection);
			RecordsCache collection2Cache = aModelLayerFactory.getRecordsCaches().getCache(anotherCollection);
			collection1Cache.configureCache(volatileCache(zeCollectionSchemaWithVolatileCache.type(), 4));
			collection1Cache.configureCache(permanentCache(zeCollectionSchemaWithPermanentCache.type()));
			collection2Cache.configureCache(volatileCache(anotherCollectionSchemaWithVolatileCache.type(), 3));

		}

		DataLayerSystemExtensions extensions = getDataLayerFactory().getExtensions().getSystemWideExtensions();
		queriesListener = new StatsBigVaultServerExtension();
		extensions.getBigVaultServerExtension().add(queriesListener);

		extensions = otherModelLayerFactory.getDataLayerFactory().getExtensions().getSystemWideExtensions();
		otherSystemQueriesListener = new StatsBigVaultServerExtension();
		extensions.getBigVaultServerExtension().add(otherSystemQueriesListener);

		EventBusManager eventBusManager1 = getModelLayerFactory().getDataLayerFactory().getEventBusManager();
		EventBusManager eventBusManager2 = otherModelLayerFactory.getDataLayerFactory().getEventBusManager();
		assertThat(eventBusManager1).isNotSameAs(eventBusManager2);

		SDKEventBusSendingService sendingService1 = new SDKEventBusSendingService();
		SDKEventBusSendingService sendingService2 = new SDKEventBusSendingService();

		eventBusManager1.setEventBusSendingService(sendingService1);
		eventBusManager2.setEventBusSendingService(sendingService2);
		SDKEventBusSendingService.interconnect(sendingService1, sendingService2);
	}

	@Test
	public void givenRecordsAreNotFullyLoadedThenNotInsertedInCache()
			throws Exception {

		givenTestRecords();
		recordsCaches.invalidateAll();

		List<Record> records = searchServices.search(new LogicalSearchQuery().setCondition(
				fromAllSchemasIn(zeCollection).returnAll())
				.setReturnedMetadatas(ReturnedMetadatasFilter.idVersionSchemaTitlePath()));

		for (Record record : records) {
			assertThat(record.isFullyLoaded()).isFalse();
			recordsCaches.insert(record, WAS_MODIFIED);
		}

		assertThatRecords("un1", "p2", "v3", "v1", "v2").areNotIn(recordsCaches);
		assertThatRecords("un1", "p2", "v3", "v1", "v2").areNotIn(otherInstanceRecordsCaches);

		records = searchServices.search(new LogicalSearchQuery().setCondition(
				fromAllSchemasIn(zeCollection).returnAll()));
		for (Record record : records) {
			assertThat(record.isFullyLoaded()).isTrue();
			recordsCaches.insert(record, WAS_MODIFIED);
		}
		assertThatRecords("un1", "un2").areNotInBothCache();
		assertThatRecords("p1", "p2", "v3", "v1", "v2").areIn(recordsCaches);
		assertThatRecords("p1", "p2").areIn(otherInstanceRecordsCaches);
		assertThatRecords("v3", "v1", "v2").areNotIn(otherInstanceRecordsCaches);

		otherInstanceRecordServices.getDocumentById("v1");
		otherInstanceRecordServices.getDocumentById("v2");
		otherInstanceRecordServices.getDocumentById("v3");

		assertThatRecords("un1", "un2").areNotInBothCache();
		assertThatRecords("p1", "p2", "v1", "v2", "v3").areInBothCache();
	}

	@Test
	public void givenPermanentCacheWhenInsertingARecordAndUpdatePassedRecordAndTheOnePassedToTheCacheThenDoesNotAffectTheCachedRecord()
			throws Exception {

		Transaction transaction = new Transaction();
		Record record = transaction.add(newRecordOf("un1", zeCollectionSchemaWithPermanentCache).withTitle("original title"));
		record.set(Schemas.LEGACY_ID, "zeLegacyId");
		recordServices.add(record);

		recordsCaches.invalidateAll();

		recordsCaches.getCache(record.getCollection()).insert(record, WAS_MODIFIED);
		record.set(Schemas.TITLE, "modified title");
		record.set(Schemas.TITLE, "modified title");
		recordsCaches.getCache(record.getCollection()).get(record.getId()).set(Schemas.TITLE, "modified title");

		assertThat(recordsCaches.getRecord(record.getId()).get(Schemas.TITLE)).isEqualTo("original title");
		assertThat(recordsCaches.getCache(record.getCollection())
				.getByMetadata(zeCollectionSchemaWithPermanentCache.metadata(Schemas.LEGACY_ID.getLocalCode()), "zeLegacyId")
				.get(Schemas.TITLE)).isEqualTo("original title");
		assertThat(recordsCaches.getRecord(record.getId()).isDirty()).isFalse();
		assertThat(record.get(Schemas.TITLE)).isEqualTo("modified title");
		assertThat(record.isDirty()).isTrue();
		assertThat(record.get(Schemas.TITLE)).isEqualTo("modified title");
		assertThat(record.isDirty()).isTrue();

		String type = zeCollectionSchemaWithPermanentCache.typeCode();
		assertThat(recordsCaches.getCache(zeCollection).getAllValues(type)).extracting("id").containsOnly("un1");
		recordsCaches.getCache(zeCollection).getAllValues(type).get(0).set(Schemas.TITLE, "test");

		assertThat(recordsCaches.getCache(zeCollection).getAllValuesInUnmodifiableState(type)).extracting("id")
				.containsOnly("un1");
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
		Record record = transaction.add(newRecordOf("un1", zeCollectionSchemaWithVolatileCache).withTitle("original title"));
		record.set(Schemas.LEGACY_ID, "zeLegacyId");
		recordServices.add(record);

		recordsCaches.invalidateAll();

		recordsCaches.getCache(record.getCollection()).insert(record, WAS_MODIFIED);

		record.set(Schemas.TITLE, "modified title");
		record.set(Schemas.TITLE, "modified title");
		recordsCaches.getCache(record.getCollection()).get(record.getId()).set(Schemas.TITLE, "modified title");

		assertThat(recordsCaches.getRecord(record.getId()).get(Schemas.TITLE)).isEqualTo("original title");
		assertThat(recordsCaches.getCache(record.getCollection())
				.getByMetadata(zeCollectionSchemaWithVolatileCache.metadata(Schemas.LEGACY_ID.getLocalCode()), "zeLegacyId")
				.get(Schemas.TITLE)).isEqualTo("original title");
		assertThat(record.get(Schemas.TITLE)).isEqualTo("modified title");
		assertThat(record.isDirty()).isTrue();
		assertThat(record.get(Schemas.TITLE)).isEqualTo("modified title");
		assertThat(record.isDirty()).isTrue();

	}

	@Test
	public void givenVolatileCacheWhenGetMetadataByLegacyIdThenObtainACopy()
			throws Exception {

		Transaction transaction = new Transaction();
		Record record = transaction.add(newRecordOf("un1", zeCollectionSchemaWithVolatileCache).withTitle("original title"));
		record.set(Schemas.LEGACY_ID, "zeLegacyId");
		recordServices.add(record);

		recordsCaches.invalidateAll();

		recordsCaches.getCache(record.getCollection()).insert(record, WAS_MODIFIED);

		record.set(Schemas.TITLE, "modified title");
		record.set(Schemas.TITLE, "modified title");
		recordsCaches.getCache(record.getCollection()).get(record.getId()).set(Schemas.TITLE, "modified title");

		assertThat(recordsCaches.getRecord(record.getId()).get(Schemas.TITLE)).isEqualTo("original title");
		assertThat(recordsCaches.getRecord(record.getId()).isDirty()).isFalse();
		assertThat(record.get(Schemas.TITLE)).isEqualTo("modified title");
		assertThat(record.isDirty()).isTrue();
		assertThat(record.get(Schemas.TITLE)).isEqualTo("modified title");
		assertThat(record.isDirty()).isTrue();

	}

	@Test
	public void givenRecordLogicallyDeletedThenModifiedInCache()
			throws Exception {

		givenTestRecords();
		loadAllRecordsInCaches();

		assertThatRecords("p2", "v3", "v4").areIn(recordsCaches);
		assertThatRecords("p2", "v3", "v4").areIn(otherInstanceRecordsCaches);
		assertThatRecords("un1", "un2").areNotIn(recordsCaches);
		assertThatRecords("un1", "un2").areNotIn(otherInstanceRecordsCaches);

		recordServices.logicallyDelete(recordServices.getDocumentById("p2"), adminInZeCollection);
		assertThatRecords("p2", "v3", "v4").areIn(recordsCaches);
		assertThatRecords("p2", "v3", "v4").areIn(otherInstanceRecordsCaches);
		assertThatRecords("un1", "un2").areNotIn(recordsCaches);
		assertThatRecords("un1", "un2").areNotIn(otherInstanceRecordsCaches);

		recordServices.update(recordServices.getDocumentById("v3").set(Schemas.LOGICALLY_DELETED_STATUS, true),
				adminInZeCollection);
		assertThatRecords("p2", "v3", "v4").areIn(recordsCaches);
		assertThatRecords("p2", "v4").areIn(otherInstanceRecordsCaches);
		assertThatRecords("un1", "un2").areNotIn(recordsCaches);
		assertThatRecords("un1", "un2", "v3").areNotIn(otherInstanceRecordsCaches);

		recordsCaches.getCache(zeCollection).invalidateAll();
		otherInstanceRecordsCaches.getCache(zeCollection).invalidateAll();
		recordsCaches.getCache(anotherCollection).invalidateAll();
		otherInstanceRecordsCaches.getCache(anotherCollection).invalidateAll();
		assertThatRecords("p2", "v3", "v4").areNotInBothCache();

		recordServices.getDocumentById("p2");
		recordServices.getDocumentById("v3");
		recordServices.getDocumentById("v4");
		assertThatRecords("p2").areInBothCache();
		assertThatRecords("v3", "v4").areOnlyIn(recordsCaches);
	}

	@Test
	public void whenAddUpdateRecordsThenKeptInCache()
			throws Exception {

		givenTestRecords();
		loadAllRecordsInCaches();

		assertThatRecords("p2", "v3", "v4").areInBothCache();
		assertThatRecords("un1", "un2").areNotInBothCache();

		queriesListener.clear();
		assertThatGetDocumentsByIdReturnEqualRecord(uncachedRecord1, permanentRecord2, volatileRecord3, volatileRecord4,
				uncachedRecord2);
		assertThat(queriesListener.byIds).containsOnlyOnce("un1", "un2");

		transaction = new Transaction();
		transaction.add(uncachedRecord1.set(Schemas.TITLE, "a2"));
		transaction.add(permanentRecord2.set(Schemas.TITLE, "b2"));
		transaction.add(volatileRecord3.set(Schemas.TITLE, "c2"));
		recordServices.execute(transaction);

		recordServices.add(volatileRecord4.set(Schemas.TITLE, "d2"));
		recordServices.add(uncachedRecord2.set(Schemas.TITLE, "e2"));

		queriesListener.clear();
		assertThatGetDocumentsByIdReturnEqualRecord(uncachedRecord1, permanentRecord2, volatileRecord3, volatileRecord4,
				uncachedRecord2);
		assertThat(queriesListener.byIds).containsOnlyOnce("un1", "un2");
	}

	@Test
	public void whenInvalidateAllThenAllInvalidated()
			throws Exception {
		givenTestRecords();
		loadAllRecordsInCaches();

		assertThatRecords("p2", "v3", "v4").areInBothCache();
		assertThatRecords("un1", "un2").areNotInBothCache();

		recordsCaches.invalidateAll();
		assertThatRecords("un1", "p2", "v3", "v4", "un2").areNotInBothCache();
	}

	@Test
	public void whenGetRecordByIdThenReturnUseCaches()
			throws Exception {

		givenTestRecords();

		recordServices.getDocumentById("un1");
		otherInstanceRecordServices.getDocumentById("p1");
		recordServices.getDocumentById("p2");
		otherInstanceRecordServices.getDocumentById("p2");
		otherInstanceRecordServices.getDocumentById("v1");
		otherInstanceRecordServices.getDocumentById("v2");
		recordServices.getDocumentById("v2");
		recordServices.getDocumentById("v3");
		recordServices.getDocumentById("v4");
		recordServices.getDocumentById("un2");
		assertThat(queriesListener.byIds).containsOnlyOnce("un1", "p2", "v3", "v4", "un2");
		assertThat(otherSystemQueriesListener.byIds).containsOnlyOnce("p1", "v1", "v2");
		assertThatRecords("p1", "p2").areInBothCache();
		assertThatRecords("v1").areOnlyIn(otherInstanceRecordsCaches);
		assertThatRecords("v2").areInBothCache();
		assertThatRecords("v3", "v4").areOnlyIn(recordsCaches);
		assertThatRecords("un1", "un2").areNotInBothCache();

	}

	@Test
	public void whenGetRecordByMetadataOnVolatileCachesThenKeptInCacheWithoutBeingSent()
			throws Exception {

		givenTestRecords();
		queriesListener.clear();
		Metadata stringMetadata = zeCollectionSchemaWithVolatileCache.stringMetadata();

		assertThat(queriesListener.queries).hasSize(0);
		assertThat(otherSystemQueriesListener.queries).hasSize(0);

		assertThat(recordServices.getRecordByMetadata(stringMetadata, "code3").getId()).isEqualTo("v3");
		assertThat(otherInstanceRecordServices.getRecordByMetadata(stringMetadata, "code18").getId()).isEqualTo("v1");
		assertThat(recordServices.getRecordByMetadata(stringMetadata, "code42").getId()).isEqualTo("v2");
		assertThat(queriesListener.queries).hasSize(2);
		assertThat(otherSystemQueriesListener.queries).hasSize(1);

		assertThat(recordServices.getRecordByMetadata(stringMetadata, "code3").getId()).isEqualTo("v3");
		assertThat(otherInstanceRecordServices.getRecordByMetadata(stringMetadata, "code18").getId()).isEqualTo("v1");
		assertThat(recordServices.getRecordByMetadata(stringMetadata, "code42").getId()).isEqualTo("v2");
		assertThat(queriesListener.queries).hasSize(2);
		assertThat(otherSystemQueriesListener.queries).hasSize(1);

		assertThat(otherInstanceRecordServices.getRecordByMetadata(stringMetadata, "code3").getId()).isEqualTo("v3");
		assertThat(recordServices.getRecordByMetadata(stringMetadata, "code18").getId()).isEqualTo("v1");
		assertThat(otherInstanceRecordServices.getRecordByMetadata(stringMetadata, "code42").getId()).isEqualTo("v2");
		assertThat(queriesListener.queries).hasSize(3);
		assertThat(otherSystemQueriesListener.queries).hasSize(3);

		assertThat(otherInstanceRecordServices.getRecordByMetadata(stringMetadata, "code3").getId()).isEqualTo("v3");
		assertThat(otherInstanceRecordServices.getRecordByMetadata(stringMetadata, "code18").getId()).isEqualTo("v1");
		assertThat(otherInstanceRecordServices.getRecordByMetadata(stringMetadata, "code42").getId()).isEqualTo("v2");
		assertThat(queriesListener.queries).hasSize(3);
		assertThat(otherSystemQueriesListener.queries).hasSize(3);

		assertThat(otherInstanceRecordServices.getRecordByMetadata(stringMetadata, "code3").getId()).isEqualTo("v3");
		assertThat(otherInstanceRecordServices.getRecordByMetadata(stringMetadata, "code18").getId()).isEqualTo("v1");
		assertThat(otherInstanceRecordServices.getRecordByMetadata(stringMetadata, "code42").getId()).isEqualTo("v2");
		assertThat(queriesListener.queries).hasSize(3);
		assertThat(otherSystemQueriesListener.queries).hasSize(3);
	}

	@Test
	public void whenGetRecordByMetadataOnPermanentCachesThenKeptInCacheAndSent()
			throws Exception {

		givenTestRecords();
		queriesListener.clear();
		Metadata stringMetadata = zeCollectionSchemaWithPermanentCache.stringMetadata();

		assertThat(queriesListener.queries).hasSize(0);
		assertThat(otherSystemQueriesListener.queries).hasSize(0);

		assertThat(otherInstanceRecordServices.getRecordByMetadata(stringMetadata, "p1Code").getId()).isEqualTo("p1");
		assertThat(recordServices.getRecordByMetadata(stringMetadata, "p2Code").getId()).isEqualTo("p2");
		assertThat(queriesListener.queries).hasSize(1);
		assertThat(otherSystemQueriesListener.queries).hasSize(1);

		assertThat(recordServices.getRecordByMetadata(stringMetadata, "p1Code").getId()).isEqualTo("p1");
		assertThat(otherInstanceRecordServices.getRecordByMetadata(stringMetadata, "p2Code").getId()).isEqualTo("p2");
		assertThat(queriesListener.queries).hasSize(1);
		assertThat(otherSystemQueriesListener.queries).hasSize(1);

		assertThat(otherInstanceRecordServices.getRecordByMetadata(stringMetadata, "p1Code").getId()).isEqualTo("p1");
		assertThat(otherInstanceRecordServices.getRecordByMetadata(stringMetadata, "p2Code").getId()).isEqualTo("p2");
		assertThat(queriesListener.queries).hasSize(1);
		assertThat(otherSystemQueriesListener.queries).hasSize(1);

		assertThat(otherInstanceRecordServices.getRecordByMetadata(stringMetadata, "p1Code").getId()).isEqualTo("p1");
		assertThat(otherInstanceRecordServices.getRecordByMetadata(stringMetadata, "p2Code").getId()).isEqualTo("p2");
		assertThat(queriesListener.queries).hasSize(1);
		assertThat(otherSystemQueriesListener.queries).hasSize(1);
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

		recordServices.getDocumentById("un1", adminInZeCollection);
		otherInstanceRecordServices.getDocumentById("un1", adminInAnotherCollection);
		recordServices.getDocumentById("p1", adminInZeCollection);
		otherInstanceRecordServices.getDocumentById("p2", adminInZeCollection);
		recordServices.getDocumentById("v3", adminInZeCollection);
		recordServices.getDocumentById("v4", adminInAnotherCollection);
		otherInstanceRecordServices.getDocumentById("v1", adminInZeCollection);
		otherInstanceRecordServices.getDocumentById("v2", adminInAnotherCollection);
		assertThatRecords("p1", "p2").areInBothCache();
		assertThatRecords("v3", "v4").areIn(recordsCaches);
		assertThatRecords("v1", "v2").areNotIn(recordsCaches);
		assertThatRecords("v3", "v4").areNotIn(otherInstanceRecordsCaches);
		assertThatRecords("v1", "v2").areIn(otherInstanceRecordsCaches);

		assertThatRecords("un1", "un2").areNotInBothCache();
	}

	@Test
	public void givenASchemaTypeIsNotCachedThenCanUpdateWithDelayedFlushing()
			throws Exception {

		givenTestRecords();

		Transaction transaction = new Transaction().setRecordFlushing(RecordsFlushing.LATER());
		transaction.update(uncachedRecord1.withTitle("modified1"));
		cachelessRecordServices.execute(transaction);
		cachelessRecordServices.flush();

		//Can add withing 2 seconds for a record
		transaction = new Transaction().setRecordFlushing(RecordsFlushing.WITHIN_SECONDS(2));
		transaction.update(uncachedRecord1.withTitle("modified2"));
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
		assertThat(record.get(zeCollectionSchemaWithVolatileCache.anotherStringMetadata())).isNull();

	}

	//	@Test
	//	public void whenGetRecordsByIdThenKeptInCache()
	//			throws Exception {
	//
	//		givenTestRecords();
	//		assertThatRecords("un1", "p2", "v3", "v4", "un2").areNotInBothCache();
	//
	//		assertThat(recordServices.getRecordsById(zeCollection, asList("un1", "p2", "v3"))).extracting("id")
	//				.containsOnly("un1", "p2", "v3");
	//		assertThat(recordServices.getRecordsById(anotherCollection, asList("v4", "un2"))).extracting("id")
	//				.containsOnly("v4", "un2");
	//
	//		assertThatRecords("p2", "v3", "v4").areInBothCache();
	//		assertThatRecords("un1", "un2").areNotInBothCache();
	//		assertThat(queriesListener.byIds).containsOnlyOnce("un1", "p2", "v3", "v4", "un2").hasSize(5);
	//
	//		assertThat(recordServices.getRecordsById(zeCollection, asList("un1", "p2", "v3"))).extracting("id")
	//				.containsOnly("un1", "p2", "v3");
	//		assertThat(recordServices.getRecordsById(anotherCollection, asList("v4", "un2"))).extracting("id")
	//				.containsOnly("v4", "un2");
	//		assertThat(queriesListener.byIds).containsOnlyOnce("p2", "v3", "v4").hasSize(7);
	//
	//		resetCacheAndQueries();
	//		assertThat(recordServices.getRecordsById(zeCollection, asList("un1", "p2"))).extracting("id")
	//				.containsOnly("un1", "p2");
	//		assertThatRecord("p2").isInCache();
	//		assertThatRecords("un1", "v3").areNotInBothCache();
	//		assertThat(queriesListener.byIds).isEqualTo(asList("un1", "p2"));
	//
	//		assertThat(recordServices.getRecordsById(zeCollection, asList("un1", "p2", "v3"))).extracting("id")
	//				.containsOnly("un1", "p2", "v3");
	//		assertThatRecords("p2", "v3").areInBothCache();
	//		assertThatRecords("un1").areNotInBothCache();
	//		assertThat(queriesListener.byIds).isEqualTo(asList("un1", "p2", "un1", "v3"));
	//
	//		try {
	//			recordServices.getRecordsById(anotherCollection, asList("un1"));
	//		} catch (RecordServicesRuntimeException e) {
	//
	//		}
	//
	//	}

	//-----------------------------------------------------------------

	private void resetCacheAndQueries() {
		recordsCaches.invalidateAll();
		queriesListener.clear();
		assertThatRecords("un1", "p2", "v3", "v4", "un2").areNotInBothCache();
	}

	private void loadAllRecordsInCaches() {
		recordServices.getDocumentById("un1");
		recordServices.getDocumentById("p1");
		recordServices.getDocumentById("p2");
		recordServices.getDocumentById("v1");
		recordServices.getDocumentById("v2");
		recordServices.getDocumentById("v3");
		recordServices.getDocumentById("v4");

		otherInstanceRecordServices.getDocumentById("v1");
		otherInstanceRecordServices.getDocumentById("v2");
		otherInstanceRecordServices.getDocumentById("v3");
		otherInstanceRecordServices.getDocumentById("v4");
		recordServices.getDocumentById("un2");
	}

	private void givenTestRecords()
			throws Exception {
		Transaction tx = new Transaction();

		tx.add(permanentRecord1 = newRecordOf("p1", zeCollectionSchemaWithPermanentCache).withTitle("x")
				.set(zeCollectionSchemaWithPermanentCache.stringMetadata(), "p1Code"));

		tx.add(permanentRecord2 = newRecordOf("p2", zeCollectionSchemaWithPermanentCache).withTitle("b")
				.set(zeCollectionSchemaWithPermanentCache.stringMetadata(), "p2Code"));

		tx.add(volatileRecord1 = newRecordOf("v1", zeCollectionSchemaWithVolatileCache).withTitle("c")
				.set(zeCollectionSchemaWithVolatileCache.stringMetadata(), "code18"));

		tx.add(volatileRecord2 = newRecordOf("v2", zeCollectionSchemaWithVolatileCache).withTitle("c")
				.set(zeCollectionSchemaWithVolatileCache.stringMetadata(), "code42"));

		tx.add(volatileRecord3 = newRecordOf("v3", zeCollectionSchemaWithVolatileCache).withTitle("c")
				.set(zeCollectionSchemaWithVolatileCache.stringMetadata(), "code3"));

		tx.add(uncachedRecord1 = newRecordOf("un1", zeCollectionSchemaWithoutCache).withTitle("a"));
		recordServices.execute(tx);
		tx = new Transaction();
		tx.add(volatileRecord4 = newRecordOf("v4", anotherCollectionSchemaWithVolatileCache).withTitle("d"));
		tx.add(uncachedRecord2 = newRecordOf("un2", anotherCollectionSchemaWithoutCache).withTitle("e"));

		recordServices.execute(tx);

		resetCacheAndQueries();
	}

	@Test
	public void testZeTestUtilityMethods()
			throws Exception {

		Transaction transaction = new Transaction();
		TestRecord record1 = (TestRecord) transaction.add(newRecordOf("un1", zeCollectionSchemaWithoutCache).withTitle("a"));
		TestRecord record2 = (TestRecord) transaction
				.add(newRecordOf("p2", zeCollectionSchemaWithPermanentCache).withTitle("b"));
		TestRecord record3 = (TestRecord) transaction
				.add(newRecordOf("v3", zeCollectionSchemaWithPermanentCache).withTitle("c"));
		recordServices.execute(transaction);

		resetCacheAndQueries();

		recordServices.getDocumentById("p2");
		recordServices.getDocumentById("v3");

		assertThatRecord("p2").isIn(recordsCaches);
		assertThatRecord("p2").isIn(otherInstanceRecordsCaches);
		assertThatRecords("p2", "v3").areInBothCache();

		try {
			assertThatRecords("un1", "p2", "v3").areInBothCache();
			fail("Exception expected");
		} catch (ComparisonFailure e) {
			//OK
		}

		try {
			assertThatRecords("p2", "v3", "un1").areInBothCache();
			fail("Exception expected");
		} catch (ComparisonFailure e) {
			//OK
		}

		try {
			assertThatRecords("un1", "inexistentRecord").areInBothCache();
			fail("Exception expected");
		} catch (ComparisonFailure e) {
			//OK
		}

		try {
			assertThatRecords("p2", "inexistentRecord").areInBothCache();
			fail("Exception expected");
		} catch (ComparisonFailure e) {
			//OK
		}

		assertThatRecord("un1").isNotIn(recordsCaches);
		assertThatRecord("un1").isNotIn(recordsCaches);
		assertThatRecords("un1", "v4").areNotInBothCache();
		assertThatRecords("un1", "inexistentRecord").areNotInBothCache();

		try {
			assertThatRecords("p2", "v3").areNotInBothCache();
			fail("Exception expected");
		} catch (ComparisonFailure e) {
			//OK
		}

		try {
			assertThatRecords("un1", "p2").areNotInBothCache();
			fail("Exception expected");
		} catch (ComparisonFailure e) {
			//OK
		}

		try {
			assertThatRecords("p2", "v3", "un1").areNotInBothCache();
			fail("Exception expected");
		} catch (ComparisonFailure e) {
			//OK
		}

		assertThat(queriesListener.queries).isNotEmpty();

		try {
			recordServices.getDocumentById("v2");
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

		assertThat(queriesListener.byIds).containsOnlyOnce("v2", "is", "magic");
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

		private void isIn(RecordsCaches recordsCaches) {
			areIn(recordsCaches);
		}

		private void areInBothCache() {
			areIn(recordsCaches);
			areIn(otherInstanceRecordsCaches);
		}

		private void areIn(RecordsCaches recordsCaches) {
			for (String id : ids) {
				boolean isCached = recordsCaches.isCached(id);
				assertThat(isCached).describedAs("Record with id '" + id + "' is expected to be in cache").isTrue();
			}
		}

		private void areOnlyIn(RecordsCaches aRecordsCaches) {
			if (recordsCaches == aRecordsCaches) {
				areIn(recordsCaches);
				areNotIn(otherInstanceRecordsCaches);
			} else {
				areIn(otherInstanceRecordsCaches);
				areNotIn(recordsCaches);
			}

		}

		private void areNotInBothCache() {
			areNotIn(recordsCaches);
			areNotIn(otherInstanceRecordsCaches);
		}

		private void isNotIn(RecordsCaches recordsCaches) {
			areNotIn(recordsCaches);
		}

		private void areNotIn(RecordsCaches recordsCaches) {
			for (String id : ids) {
				boolean isCached = recordsCaches.isCached(id);
				assertThat(isCached).describedAs("Record with id '" + id + "' is expected to not be in cache").isFalse();
			}
		}
	}

}
