package com.constellio.model.services.records.cache;

import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.RecordCacheType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesImpl;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ThirdSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import com.constellio.sdk.tests.setups.SchemaShortcuts;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromEveryTypesOfEveryCollection;
import static com.constellio.sdk.tests.TestUtils.linkEventBus;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsUnique;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class EventRecordsCache2AcceptanceTest extends ConstellioTest {

	Transaction transaction;

	TestRecord uncachedRecord1, uncachedRecord2, permanentRecord1, permanentRecord2, permanentRecord3, volatileRecord3, volatileRecord4, volatileRecord1, volatileRecord2;

	TestsSchemasSetup zeCollectionSchemas = new TestsSchemasSetup(zeCollection).withSecurityFlag(false);
	ZeSchemaMetadatas zeCollectionSchemaWithVolatileCache = zeCollectionSchemas.new ZeSchemaMetadatas();
	AnotherSchemaMetadatas zeCollectionSchemaWithPermanentCache = zeCollectionSchemas.new AnotherSchemaMetadatas();
	ThirdSchemaMetadatas zeCollectionSchemaWithSummaryPermanentCache = zeCollectionSchemas.new ThirdSchemaMetadatas();

	String anotherCollection = "anotherCollection";
	TestsSchemasSetup anotherCollectionSchemas = new TestsSchemasSetup(anotherCollection).withSecurityFlag(false);
	ZeSchemaMetadatas anotherCollectionSchemaWithoutCache = anotherCollectionSchemas.new ZeSchemaMetadatas();
	ThirdSchemaMetadatas anotherCollectionSchemaWithVolatileCache = anotherCollectionSchemas.new ThirdSchemaMetadatas();

	RecordsCaches recordsCaches;
	RecordsCaches otherInstanceRecordsCaches;
	RecordsCache zeCollectionRecordsCache;
	RecordsCache otherInstanceZeCollectionRecordsCache;
	RecordsCache anotherCollectionRecordsCache;

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

		ModelLayerFactory otherModelLayerFactory = getModelLayerFactory("other");

		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		cachelessRecordServices = getModelLayerFactory().newCachelessRecordServices();

		otherInstanceRecordServices = otherModelLayerFactory.newRecordServices();
		otherInstanceSearchServices = otherModelLayerFactory.newSearchServices();

		recordsCaches = getModelLayerFactory().getRecordsCaches();
		zeCollectionRecordsCache = getModelLayerFactory().getRecordsCaches().getCache(zeCollection);
		anotherCollectionRecordsCache = getModelLayerFactory().getRecordsCaches().getCache(anotherCollection);

		otherInstanceRecordsCaches = otherModelLayerFactory.getRecordsCaches();
		otherInstanceZeCollectionRecordsCache = otherModelLayerFactory.getRecordsCaches().getCache(zeCollection);

		linkEventBus(getDataLayerFactory(), otherModelLayerFactory.getDataLayerFactory());


		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(zeCollectionSchemaWithVolatileCache.type().getCode()).setRecordCacheType(RecordCacheType.SUMMARY_CACHED_WITH_VOLATILE);
			}
		});

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(zeCollectionSchemaWithPermanentCache.type().getCode()).setRecordCacheType(
						RecordCacheType.FULLY_CACHED);
			}
		});

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(zeCollectionSchemaWithSummaryPermanentCache.type().getCode()).setRecordCacheType(
						RecordCacheType.SUMMARY_CACHED_WITHOUT_VOLATILE);
			}
		});

		getModelLayerFactory().getMetadataSchemasManager().modify(anotherCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(anotherCollectionSchemaWithVolatileCache.type().getCode()).setRecordCacheType(RecordCacheType.SUMMARY_CACHED_WITH_VOLATILE);
			}
		});

		DataLayerSystemExtensions extensions = getDataLayerFactory().getExtensions().getSystemWideExtensions();
		queriesListener = new StatsBigVaultServerExtension();
		extensions.getBigVaultServerExtension().add(queriesListener);

		extensions = otherModelLayerFactory.getDataLayerFactory().getExtensions().getSystemWideExtensions();
		otherSystemQueriesListener = new StatsBigVaultServerExtension();
		extensions.getBigVaultServerExtension().add(otherSystemQueriesListener);

		tx = new Transaction();

		assertThat(getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchemaType(zeCollectionSchemaWithPermanentCache.typeCode()).getCacheType()).isEqualTo(RecordCacheType.FULLY_CACHED);

		assertThat(getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchemaType(zeCollectionSchemaWithSummaryPermanentCache.typeCode()).getCacheType()).isEqualTo(RecordCacheType.SUMMARY_CACHED_WITHOUT_VOLATILE);

		assertThat(getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchemaType(zeCollectionSchemaWithVolatileCache.typeCode()).getCacheType()).isEqualTo(RecordCacheType.SUMMARY_CACHED_WITH_VOLATILE);

		assertThat(otherModelLayerFactory.getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchemaType(zeCollectionSchemaWithPermanentCache.typeCode()).getCacheType()).isEqualTo(RecordCacheType.FULLY_CACHED);

		assertThat(otherModelLayerFactory.getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchemaType(zeCollectionSchemaWithSummaryPermanentCache.typeCode()).getCacheType()).isEqualTo(RecordCacheType.SUMMARY_CACHED_WITHOUT_VOLATILE);

		assertThat(otherModelLayerFactory.getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchemaType(zeCollectionSchemaWithVolatileCache.typeCode()).getCacheType()).isEqualTo(RecordCacheType.SUMMARY_CACHED_WITH_VOLATILE);

	}

	@Test
	public void whenInsertingRecordsThenInsertedInLocalVolatileCacheAndInAllPermanentCache() throws Exception {

		assertThat(recordsCaches.getRecord("p1")).isNull();
		assertThat(recordsCaches.getRecord("p2")).isNull();
		assertThat(recordsCaches.getRecord("v1")).isNull();

		assertThat(otherInstanceRecordsCaches.getRecord("p1")).isNull();
		assertThat(otherInstanceRecordsCaches.getRecord("p2")).isNull();
		assertThat(otherInstanceRecordsCaches.getRecord("v1")).isNull();

		tx.add(permanentRecord1 = newRecordOf("p1", zeCollectionSchemaWithPermanentCache).withTitle("b")
				.set(zeCollectionSchemaWithPermanentCache.stringMetadata(), "p1Code"));

		tx.add(permanentRecord2 = newRecordOf("p2", zeCollectionSchemaWithSummaryPermanentCache).withTitle("b"));

		tx.add(volatileRecord1 = newRecordOf("v1", zeCollectionSchemaWithVolatileCache).withTitle("c")
				.set(zeCollectionSchemaWithVolatileCache.stringMetadata(), "code18"));

		recordServices.execute(tx);

		assertThat(recordsCaches.getRecord("p1").isSummary()).isFalse();
		assertThat(recordsCaches.getRecord("p2")).isNull();
		assertThat(recordsCaches.getRecordSummary("p2").isSummary()).isTrue();
		assertThat(recordsCaches.getRecord("v1").isSummary()).isFalse();
		assertThat(recordsCaches.getRecordSummary("v1").isSummary()).isTrue();


		assertThat(otherInstanceRecordsCaches.getRecord("p1").isSummary()).isFalse();
		assertThat(otherInstanceRecordsCaches.getRecord("p2")).isNull();
		assertThat(otherInstanceRecordsCaches.getRecordSummary("p2").isSummary()).isTrue();
		assertThat(otherInstanceRecordsCaches.getRecord("v1")).isNull();
		assertThat(otherInstanceRecordsCaches.getRecordSummary("v1").isSummary()).isTrue();

	}

	//-----------------------------------------------------------------

	private void resetCacheAndQueries() {
		recordsCaches.invalidateVolatile();
		otherInstanceRecordsCaches.invalidateVolatile();
		queriesListener.clear();
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

		tx.add(uncachedRecord1 = newRecordOf("un1", zeCollectionSchemaWithPermanentCache).withTitle("a"));
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
