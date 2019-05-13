package com.constellio.model.services.records.cache;

import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordImplRuntimeException;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
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
import org.junit.Before;
import org.junit.ComparisonFailure;
import org.junit.Test;

import java.util.List;

import static com.constellio.data.dao.services.cache.InsertionReason.WAS_MODIFIED;
import static com.constellio.data.dao.services.cache.InsertionReason.WAS_OBTAINED;
import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.entities.schemas.Schemas.TITLE;
import static com.constellio.model.services.records.cache.CacheConfig.permanentCache;
import static com.constellio.model.services.records.cache.CacheConfig.volatileCache;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromEveryTypesOfEveryCollection;
import static com.constellio.sdk.tests.TestUtils.linkEventBus;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsUnique;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class EventRecordsCacheAcceptanceTest extends ConstellioTest {

	Transaction transaction;
	User adminInZeCollection, adminInAnotherCollection;

	TestRecord uncachedRecord1, uncachedRecord2, permanentRecord1, permanentRecord2, permanentRecord3, volatileRecord3, volatileRecord4, volatileRecord1, volatileRecord2;

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
	RecordsCache otherInstanceZeCollectionRecordsCache;
	RecordsCache anotherCollectionRecordsCache;

	UserServices userServices;

	RecordServices recordServices, otherInstanceRecordServices;
	RecordServicesImpl cachelessRecordServices;
	SearchServices searchServices, otherInstanceSearchServices;

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
		otherInstanceSearchServices = otherModelLayerFactory.newSearchServices();

		userServices = getModelLayerFactory().newUserServices();
		recordsCaches = getModelLayerFactory().getRecordsCaches();
		zeCollectionRecordsCache = getModelLayerFactory().getRecordsCaches().getCache(zeCollection);
		anotherCollectionRecordsCache = getModelLayerFactory().getRecordsCaches().getCache(anotherCollection);

		otherInstanceRecordsCaches = otherModelLayerFactory.getRecordsCaches();
		otherInstanceZeCollectionRecordsCache = otherModelLayerFactory.getRecordsCaches().getCache(zeCollection);

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

		linkEventBus(getDataLayerFactory(), otherModelLayerFactory.getDataLayerFactory());
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
			recordsCaches.insert(record, WAS_OBTAINED);
		}

		assertThatRecords("un1", "p2", "v3", "v1", "v2").areNotIn(recordsCaches);
		assertThatRecords("un1", "p2", "v3", "v1", "v2").areNotIn(otherInstanceRecordsCaches);

		records = searchServices.search(new LogicalSearchQuery().setCondition(
				fromAllSchemasIn(zeCollection).returnAll()));
		for (Record record : records) {
			assertThat(record.isFullyLoaded()).isTrue();
			recordsCaches.insert(record, WAS_OBTAINED);
		}
		assertThatRecords("un1", "un2").areNotInBothCache();
		assertThatRecords("p1", "p2", "v3", "v1", "v2").areOnlyIn(recordsCaches);

		otherInstanceRecordServices.getDocumentById("v1");
		otherInstanceRecordServices.getDocumentById("v2");
		otherInstanceRecordServices.getDocumentById("v3");

		assertThatRecords("v1", "v2", "v3").areInBothCache();
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
		record.set(TITLE, "modified title");
		record.set(TITLE, "modified title");
		recordsCaches.getCache(record.getCollection()).get(record.getId()).set(TITLE, "modified title");

		assertThat(recordsCaches.getRecord(record.getId()).<String>get(TITLE)).isEqualTo("original title");
		assertThat(recordsCaches.getCache(record.getCollection())
				.getByMetadata(zeCollectionSchemaWithPermanentCache.metadata(Schemas.LEGACY_ID.getLocalCode()), "zeLegacyId")
				.<String>get(TITLE)).isEqualTo("original title");
		assertThat(recordsCaches.getRecord(record.getId()).isDirty()).isFalse();
		assertThat(record.<String>get(TITLE)).isEqualTo("modified title");
		assertThat(record.isDirty()).isTrue();
		assertThat(record.<String>get(TITLE)).isEqualTo("modified title");
		assertThat(record.isDirty()).isTrue();

		String type = zeCollectionSchemaWithPermanentCache.typeCode();
		assertThat(recordsCaches.getCache(zeCollection).getAllValues(type)).extracting("id").containsOnly("un1");
		recordsCaches.getCache(zeCollection).getAllValues(type).get(0).set(TITLE, "test");

		assertThat(recordsCaches.getCache(zeCollection).getAllValuesInUnmodifiableState(type)).extracting("id")
				.containsOnly("un1");
		try {
			recordsCaches.getCache(zeCollection).getAllValuesInUnmodifiableState(type).get(0).set(TITLE, "test");
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

		record.set(TITLE, "modified title");
		record.set(TITLE, "modified title");
		recordsCaches.getCache(record.getCollection()).get(record.getId()).set(TITLE, "modified title");

		assertThat(recordsCaches.getRecord(record.getId()).<String>get(TITLE)).isEqualTo("original title");
		assertThat(recordsCaches.getCache(record.getCollection())
				.getByMetadata(zeCollectionSchemaWithVolatileCache.metadata(Schemas.LEGACY_ID.getLocalCode()), "zeLegacyId")
				.<String>get(TITLE)).isEqualTo("original title");
		assertThat(record.<String>get(TITLE)).isEqualTo("modified title");
		assertThat(record.isDirty()).isTrue();
		assertThat(record.<String>get(TITLE)).isEqualTo("modified title");
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

		recordsCaches.getCache(record.getCollection()).insert(record, WAS_OBTAINED);

		record.set(TITLE, "modified title");
		record.set(TITLE, "modified title");
		recordsCaches.getCache(record.getCollection()).get(record.getId()).set(TITLE, "modified title");

		assertThat(recordsCaches.getRecord(record.getId()).<String>get(TITLE)).isEqualTo("original title");
		assertThat(recordsCaches.getRecord(record.getId()).isDirty()).isFalse();
		assertThat(record.<String>get(TITLE)).isEqualTo("modified title");
		assertThat(record.isDirty()).isTrue();
		assertThat(record.<String>get(TITLE)).isEqualTo("modified title");
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

		long versionBeforeLogicallyDelete = recordServices.getDocumentById("p2").getVersion();
		recordServices.logicallyDelete(recordServices.getDocumentById("p2"), adminInZeCollection);
		assertThatRecords("p2", "v3", "v4").areIn(recordsCaches);
		assertThatRecords("p2", "v3", "v4").areIn(otherInstanceRecordsCaches);
		assertThatRecords("un1", "un2").areNotIn(recordsCaches);
		assertThatRecords("un1", "un2").areNotIn(otherInstanceRecordsCaches);
		assertThat(otherInstanceRecordsCaches.getRecord("p2").getVersion())
				.isEqualTo(recordsCaches.getRecord("p2").getVersion())
				.isNotEqualTo(versionBeforeLogicallyDelete);

		recordServices.logicallyDelete(recordServices.getDocumentById("v3"), adminInZeCollection);

		assertThatRecords("p2", "v3", "v4").areIn(recordsCaches);
		assertThatRecords("p2", "v4").areIn(otherInstanceRecordsCaches);
		assertThatRecords("un1", "un2").areNotIn(recordsCaches);
		assertThatRecords("un1", "un2", "v3").areNotIn(otherInstanceRecordsCaches);

	}

	@Test
	public void whenInvalidateRecordsThenInvalidatedEverywhere()
			throws Exception {

		givenTestRecords();
		loadAllRecordsInCaches();

		assertThatRecords("p1", "p2", "v1", "v2", "v3", "v4").areInBothCache();

		recordsCaches.getCache(zeCollection).invalidate(asList("p1", "v1"));
		assertThatRecords("p1", "v1").areNotInBothCache();

		recordsCaches.getCache(zeCollection).invalidate(asList("v3"));
		assertThatRecords("p1", "v1", "v3").areNotInBothCache();

	}

	@Test
	public void whenInvalidateSchemaTypeRecordsThenInvalidatedEverywhere()
			throws Exception {

		givenTestRecords();
		loadAllRecordsInCaches();

		assertThatRecords("p1", "p2", "v1", "v2", "v3", "v4").areInBothCache();

		recordsCaches.getCache(zeCollection).invalidateRecordsOfType(zeCollectionSchemaWithVolatileCache.typeCode());
		assertThatRecords("p1", "p2", "v4").areInBothCache(); //v4 is in an other collection
		assertThatRecords("v1", "v2", "v3").areNotInBothCache();

	}

	@Test
	public void givenRecordPhysicallyDeletedThenModifiedInCache()
			throws Exception {

		givenTestRecords();
		loadAllRecordsInCaches();

		assertThatRecords("p2", "v3", "v4").areIn(recordsCaches);
		assertThatRecords("p2", "v3", "v4").areIn(otherInstanceRecordsCaches);
		assertThatRecords("un1", "un2").areNotIn(recordsCaches);
		assertThatRecords("un1", "un2").areNotIn(otherInstanceRecordsCaches);

		recordServices.physicallyDeleteNoMatterTheStatus(recordServices.getDocumentById("p2"), adminInZeCollection,
				new RecordPhysicalDeleteOptions());
		assertThatRecords("v3", "v4", "p1").areInBothCache();
		assertThatRecords("un1", "un2", "p2").areNotInBothCache();

		recordServices.physicallyDeleteNoMatterTheStatus(recordServices.getDocumentById("v3"), adminInZeCollection,
				new RecordPhysicalDeleteOptions());

		assertThatRecords("v4", "p1").areInBothCache();
		assertThatRecords("un1", "un2", "v3", "p2").areNotInBothCache();

	}

	@Test
	public void givenRecordIsModifiedThenModifiedInCacheLocallyAndRemotelyIfPermanent()
			throws Exception {

		givenTestRecords();
		loadAllRecordsInCaches();

		assertThatRecords("p2", "v3", "v4").areIn(recordsCaches);
		assertThatRecords("p2", "v3", "v4").areIn(otherInstanceRecordsCaches);
		assertThatRecords("un1", "un2").areNotIn(recordsCaches);
		assertThatRecords("un1", "un2").areNotIn(otherInstanceRecordsCaches);

		long versionBeforeModification = recordServices.getDocumentById("p2").getVersion();
		recordServices.update(recordServices.getDocumentById("p2").set(TITLE, "test"), adminInZeCollection);
		assertThatRecords("p2", "v3", "v4").areIn(recordsCaches);
		assertThatRecords("p2", "v3", "v4").areIn(otherInstanceRecordsCaches);
		assertThatRecords("un1", "un2").areNotIn(recordsCaches);
		assertThatRecords("un1", "un2").areNotIn(otherInstanceRecordsCaches);
		assertThat(otherInstanceRecordsCaches.getRecord("p2").getVersion())
				.isEqualTo(recordsCaches.getRecord("p2").getVersion())
				.isNotEqualTo(versionBeforeModification);

		recordServices.update(recordServices.getDocumentById("v3").set(TITLE, "test"), adminInZeCollection);

		assertThatRecords("p2", "v3", "v4").areIn(recordsCaches);
		assertThatRecords("p2", "v4").areIn(otherInstanceRecordsCaches);
		assertThatRecords("un1", "un2").areNotIn(recordsCaches);
		assertThatRecords("un1", "un2", "v3").areNotIn(otherInstanceRecordsCaches);

	}

	@Test
	public void givenRecordWhichIsNotFullyLoadedIsModifiedThenUpdatedInCacheIfPermanentInvalidatedIfVolatile()
			throws Exception {

		givenTestRecords();
		loadAllRecordsInCaches();

		assertThatRecords("p2", "v3", "v4").areIn(recordsCaches);
		assertThatRecords("p2", "v3", "v4").areIn(otherInstanceRecordsCaches);
		assertThatRecords("un1", "un2").areNotIn(recordsCaches);
		assertThatRecords("un1", "un2").areNotIn(otherInstanceRecordsCaches);

		long versionBeforeModification = recordServices.getDocumentById("p2").getVersion();
		recordServices.update(getPartiallyLoadedRecord("p2").set(TITLE, "test"), adminInZeCollection);

		assertThatRecords("p2", "v3", "v4").areIn(recordsCaches);
		assertThatRecords("p2", "v3", "v4").areIn(otherInstanceRecordsCaches);
		assertThatRecords("un1", "un2").areNotIn(recordsCaches);
		assertThatRecords("un1", "un2").areNotIn(otherInstanceRecordsCaches);

		assertThat(recordsCaches.getRecord("p2").<String>get(zeCollectionSchemaWithPermanentCache.stringMetadata())).isEqualTo("p2Code");
		assertThat(otherInstanceRecordsCaches.getRecord("p2").<String>get(zeCollectionSchemaWithPermanentCache.stringMetadata()))
				.isEqualTo("p2Code");
		assertThat(recordsCaches.getRecord("p2").<String>get(TITLE)).isEqualTo("test");
		assertThat(otherInstanceRecordsCaches.getRecord("p2").<String>get(TITLE)).isEqualTo("test");
		assertThat(otherInstanceRecordsCaches.getRecord("p2").getVersion())
				.isEqualTo(recordsCaches.getRecord("p2").getVersion())
				.isNotEqualTo(versionBeforeModification);

		recordServices.update(getPartiallyLoadedRecord("v3").set(TITLE, "test"), adminInZeCollection);

		assertThatRecords("p2", "v4").areInBothCache();
		assertThatRecords("un1", "un2", "v3").areNotInBothCache();

	}

	@Test
	public void whenInvalidateAllThenOnlyInvalidateLocally()
			throws Exception {

		givenTestRecords();
		loadAllRecordsInCaches();

		recordsCaches.getCache(zeCollection).invalidateAll();
		recordsCaches.getCache(anotherCollection).invalidateAll();
		assertThatRecords("p2", "v3", "v4").areOnlyIn(otherInstanceRecordsCaches);
		otherInstanceRecordsCaches.getCache(zeCollection).invalidateAll();
		otherInstanceRecordsCaches.getCache(anotherCollection).invalidateAll();
		assertThatRecords("p2", "v3", "v4").areNotInBothCache();

		recordServices.getDocumentById("p2");
		recordServices.getDocumentById("v3");
		recordServices.getDocumentById("v4");
		assertThatRecords("p2", "v3", "v4").areOnlyIn(recordsCaches);
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
		transaction.add(uncachedRecord1.set(TITLE, "a2"));
		transaction.add(permanentRecord2.set(TITLE, "b2"));
		transaction.add(volatileRecord3.set(TITLE, "c2"));
		recordServices.execute(transaction);

		recordServices.add(volatileRecord4.set(TITLE, "d2"));
		recordServices.add(uncachedRecord2.set(TITLE, "e2"));

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
		otherInstanceRecordsCaches.invalidateAll();
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
		assertThat(otherSystemQueriesListener.byIds).containsOnlyOnce("p1", "p2", "v1", "v2");
		assertThatRecords("p1").isOnlyIn(otherInstanceRecordsCaches);
		assertThatRecords("p2").areInBothCache();
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
		assertThat(queriesListener.queries).hasSize(2);
		assertThat(otherSystemQueriesListener.queries).hasSize(2);

		assertThat(otherInstanceRecordServices.getRecordByMetadata(stringMetadata, "p1Code").getId()).isEqualTo("p1");
		assertThat(otherInstanceRecordServices.getRecordByMetadata(stringMetadata, "p2Code").getId()).isEqualTo("p2");
		assertThat(queriesListener.queries).hasSize(2);
		assertThat(otherSystemQueriesListener.queries).hasSize(2);

		assertThat(otherInstanceRecordServices.getRecordByMetadata(stringMetadata, "p1Code").getId()).isEqualTo("p1");
		assertThat(otherInstanceRecordServices.getRecordByMetadata(stringMetadata, "p2Code").getId()).isEqualTo("p2");
		assertThat(queriesListener.queries).hasSize(2);
		assertThat(otherSystemQueriesListener.queries).hasSize(2);
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
		assertThatRecords("p1", "v3", "v4").areIn(recordsCaches);
		assertThatRecords("p2", "v1", "v2").areNotIn(recordsCaches);
		assertThatRecords("p1", "v3", "v4").areNotIn(otherInstanceRecordsCaches);
		assertThatRecords("p2", "v1", "v2").areIn(otherInstanceRecordsCaches);

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
		assertThat(record.<String>get(zeCollectionSchemaWithVolatileCache.anotherStringMetadata())).isNull();

	}

	@Test
	public void givenQueriesThenInvalidatedLocallyAndRemotelyWhenARecordIsModified()
			throws Exception {

		givenTestRecords();
		LogicalSearchQuery query = LogicalSearchQuery.query(from(zeCollectionSchemaWithPermanentCache.type())
				.where(zeCollectionSchemaWithPermanentCache.stringMetadata()).isEqualTo("p2Code"));

		assertThat(searchServices.cachedSearch(query)).extracting("id").containsOnly("p2");
		assertThat(searchServices.cachedSearch(query)).extracting("id").containsOnly("p2");
		assertThat(queriesListener.queries).hasSize(1);
		assertThat(otherSystemQueriesListener.queries).hasSize(0);
		assertThat(otherInstanceSearchServices.cachedSearch(query)).extracting("id").containsOnly("p2");
		assertThat(otherInstanceSearchServices.cachedSearch(query)).extracting("id").containsOnly("p2");
		assertThat(queriesListener.queries).hasSize(1);
		assertThat(otherSystemQueriesListener.queries).hasSize(1);

		recordsCaches.insert(record("p2"), WAS_OBTAINED);

		assertThat(searchServices.cachedSearch(query)).extracting("id").containsOnly("p2");
		assertThat(otherInstanceSearchServices.cachedSearch(query)).extracting("id").containsOnly("p2");
		assertThat(queriesListener.queries).hasSize(1);
		assertThat(otherSystemQueriesListener.queries).hasSize(1);

		recordServices.update(record("p2").set(zeCollectionSchemaWithPermanentCache.stringMetadata(), "newP2Code"));
		queriesListener.queries.clear();
		otherSystemQueriesListener.queries.clear();

		assertThat(searchServices.cachedSearch(query)).extracting("id").isEmpty();
		assertThat(otherInstanceSearchServices.cachedSearch(query)).extracting("id").isEmpty();
		assertThat(queriesListener.queries).hasSize(1);
		assertThat(otherSystemQueriesListener.queries).hasSize(1);

		assertThat(searchServices.cachedSearch(query)).extracting("id").isEmpty();
		assertThat(otherInstanceSearchServices.cachedSearch(query)).extracting("id").isEmpty();
		assertThat(queriesListener.queries).hasSize(1);
		assertThat(otherSystemQueriesListener.queries).hasSize(1);

	}

	@Test
	public void givenQueriesResultIdsThenInvalidatedLocallyAndRemotelyWhenARecordIsModified()
			throws Exception {

		givenTestRecords();
		LogicalSearchQuery query = LogicalSearchQuery.query(from(zeCollectionSchemaWithPermanentCache.type())
				.where(zeCollectionSchemaWithPermanentCache.stringMetadata()).isEqualTo("p2Code"));

		assertThat(searchServices.cachedSearchRecordIds(query)).containsOnly("p2");
		assertThat(searchServices.cachedSearchRecordIds(query)).containsOnly("p2");
		assertThat(queriesListener.queries).hasSize(1);
		assertThat(otherSystemQueriesListener.queries).hasSize(0);
		assertThat(otherInstanceSearchServices.cachedSearchRecordIds(query)).containsOnly("p2");
		assertThat(otherInstanceSearchServices.cachedSearchRecordIds(query)).containsOnly("p2");
		assertThat(queriesListener.queries).hasSize(1);
		assertThat(otherSystemQueriesListener.queries).hasSize(1);

		recordsCaches.insert(record("p2"), WAS_OBTAINED);
		queriesListener.queries.clear();
		otherSystemQueriesListener.queries.clear();

		assertThat(searchServices.cachedSearchRecordIds(query)).containsOnly("p2");
		assertThat(otherInstanceSearchServices.cachedSearchRecordIds(query)).containsOnly("p2");
		assertThat(queriesListener.queries).hasSize(0);
		assertThat(otherSystemQueriesListener.queries).hasSize(0);

		recordServices.update(record("p2").set(zeCollectionSchemaWithPermanentCache.stringMetadata(), "newP2Code"));
		queriesListener.queries.clear();
		otherSystemQueriesListener.queries.clear();

		assertThat(searchServices.cachedSearchRecordIds(query)).isEmpty();
		assertThat(otherInstanceSearchServices.cachedSearchRecordIds(query)).isEmpty();
		assertThat(queriesListener.queries).hasSize(1);
		assertThat(otherSystemQueriesListener.queries).hasSize(1);

		assertThat(searchServices.cachedSearchRecordIds(query)).isEmpty();
		assertThat(otherInstanceSearchServices.cachedSearchRecordIds(query)).isEmpty();
		assertThat(queriesListener.queries).hasSize(1);
		assertThat(otherSystemQueriesListener.queries).hasSize(1);

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

		assertThatRecords("p2", "v3").isOnlyIn(recordsCaches);

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

		Record modifiedRecord1 = record1.getCopyOfOriginalRecord().set(TITLE, "modified title");
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

	@Test
	public void givenRecordsModifiedThenCachedSearchResultsAreInvalidated()
			throws Exception {

		Transaction tx = new Transaction();
		tx.add(permanentRecord1 = newRecordOf("p1", zeCollectionSchemaWithPermanentCache).withTitle("1"));
		tx.add(permanentRecord2 = newRecordOf("p2", zeCollectionSchemaWithPermanentCache).withTitle("2"));
		tx.add(permanentRecord3 = newRecordOf("p3", zeCollectionSchemaWithPermanentCache).withTitle("3"));
		recordServices.execute(tx);

		LogicalSearchQuery query = new LogicalSearchQuery(from(zeCollectionSchemaWithPermanentCache.type()).returnAll()).sortAsc(TITLE);

		assertThat(searchServices.cachedSearch(query)).extracting("title").containsOnly("1", "2", "3");
		assertThat(searchServices.cachedSearch(query)).extracting("title").containsOnly("1", "2", "3");

		assertThat(otherInstanceSearchServices.cachedSearch(query)).extracting("title").containsOnly("1", "2", "3");
		assertThat(otherInstanceSearchServices.cachedSearch(query)).extracting("title").containsOnly("1", "2", "3");

		recordServices.update(permanentRecord2.set(TITLE, "4"));

		assertThat(searchServices.cachedSearch(query)).extracting("title").containsOnly("1", "3", "4");
		assertThat(searchServices.cachedSearch(query)).extracting("title").containsOnly("1", "3", "4");

		assertThat(otherInstanceSearchServices.cachedSearch(query)).extracting("title").containsOnly("1", "3", "4");
		assertThat(otherInstanceSearchServices.cachedSearch(query)).extracting("title").containsOnly("1", "3", "4");

	}


	//-----------------------------------------------------------------

	private void resetCacheAndQueries() {
		recordsCaches.invalidateAll();
		otherInstanceRecordsCaches.invalidateAll();
		queriesListener.clear();
		assertThatRecords("un1", "p2", "v3", "v4", "un2").areNotInBothCache();
	}

	private void loadAllRecordsInCaches() {
		recordServices.getDocumentById("p1");
		recordServices.getDocumentById("p2");
		recordServices.getDocumentById("v1");
		recordServices.getDocumentById("v2");
		recordServices.getDocumentById("v3");
		recordServices.getDocumentById("v4");

		otherInstanceRecordServices.getDocumentById("p1");
		otherInstanceRecordServices.getDocumentById("p2");
		otherInstanceRecordServices.getDocumentById("v1");
		otherInstanceRecordServices.getDocumentById("v2");
		otherInstanceRecordServices.getDocumentById("v3");
		otherInstanceRecordServices.getDocumentById("v4");

		recordServices.getDocumentById("un1");
		otherInstanceRecordServices.getDocumentById("un2");
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

	private Record getPartiallyLoadedRecord(String id) {
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		LogicalSearchQuery query = new LogicalSearchQuery(fromEveryTypesOfEveryCollection().where(IDENTIFIER).isEqualTo(id));
		query.setReturnedMetadatas(ReturnedMetadatasFilter.idVersionSchemaTitle());
		return searchServices.search(query).get(0);
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

		private void isOnlyIn(RecordsCaches aRecordsCaches) {
			areOnlyIn(aRecordsCaches);
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
