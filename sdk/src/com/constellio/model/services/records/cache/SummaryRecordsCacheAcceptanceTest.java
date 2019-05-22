package com.constellio.model.services.records.cache;

import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesImpl;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import com.constellio.sdk.tests.setups.SchemaShortcuts;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.constellio.data.dao.services.cache.InsertionReason.WAS_OBTAINED;
import static com.constellio.model.services.records.cache.CacheConfig.permanentEssentialMetadatasCacheNotLoadedInitially;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsEssentialInSummary;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsUnique;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class SummaryRecordsCacheAcceptanceTest extends ConstellioTest {

	Transaction transaction;
	User adminInZeCollection, adminInAnotherCollection;

	TestRecord record1, record2, record3, record4, record;

	TestsSchemasSetup zeCollectionSchemas = new TestsSchemasSetup(zeCollection).withSecurityFlag(false);
	ZeSchemaMetadatas schemaType = zeCollectionSchemas.new ZeSchemaMetadatas();

	RecordsCaches recordsCaches;
	RecordsCache recordsCache;

	UserServices userServices;

	RecordServices recordServices;
	RecordServicesImpl cachelessRecordServices;
	SearchServices searchServices;

	StatsBigVaultServerExtension queriesListener;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withAllTestUsers()
		);
		inCollection(zeCollection).giveWriteAccessTo(admin);

		defineSchemasManager().using(zeCollectionSchemas
				.withAStringMetadata(whichIsEssentialInSummary, whichIsUnique)
				.withANumberMetadata(whichIsEssentialInSummary)
				.withAnotherStringMetadata());

		adminInZeCollection = getModelLayerFactory().newUserServices().getUserInCollection("admin", zeCollection);

		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		cachelessRecordServices = getModelLayerFactory().newCachelessRecordServices();

		userServices = getModelLayerFactory().newUserServices();
		recordsCaches = getModelLayerFactory().getRecordsCaches();
		recordsCache = getModelLayerFactory().getRecordsCaches().getCache(zeCollection);

		RecordsCache collection1Cache = getModelLayerFactory().getRecordsCaches().getCache(zeCollection);
		collection1Cache
				.configureCache(permanentEssentialMetadatasCacheNotLoadedInitially(schemaType.type()));

		DataLayerSystemExtensions extensions = getDataLayerFactory().getExtensions().getSystemWideExtensions();
		queriesListener = new StatsBigVaultServerExtension();
		extensions.getBigVaultServerExtension().add(queriesListener);

	}

	@Test
	public void givenSummaryCacheThenRecordsAddedWhenGetById()
			throws Exception {

		givenTestRecords();
		resetCacheAndQueries();

		assertThat(recordsCache.getSummary("1")).isNull();
		assertThat(recordsCache.getSummary("1")).isNull();
		assertThat(recordsCache.getSummary("2")).isNull();
		assertThat(recordsCache.getSummary("3")).isNull();

		getModelLayerFactory().newRecordServices().getDocumentById("1");
		assertThat(recordsCache.getSummary("1")).isNotNull();
		assertThat(recordsCache.getSummary("2")).isNull();
		assertThat(recordsCache.getSummary("3")).isNull();

		getModelLayerFactory().newRecordServices().getDocumentById("2");
		getModelLayerFactory().newRecordServices().getDocumentById("3");

		assertThatTheThreeRecordsAreInCache();

	}

	@Test
	public void givenRecordsInSummaryCacheThenOnlyRetrievedByMetadataIfUsingGetSummaryByMetadata()
			throws Exception {

		givenTestRecords();

		assertThat(recordsCache.getByMetadata(schemaType.stringMetadata(), "1")).isNull();
		assertThat(recordsCache.getSummaryByMetadata(schemaType.stringMetadata(), "1").getId()).isEqualTo("1");

		assertThat(recordsCache.getByMetadata(schemaType.stringMetadata(), "4")).isNull();
		assertThat(recordsCache.getSummaryByMetadata(schemaType.stringMetadata(), "4").getId()).isEqualTo("2");

		assertThat(recordsCache.getByMetadata(schemaType.stringMetadata(), "6")).isNull();
		assertThat(recordsCache.getSummaryByMetadata(schemaType.stringMetadata(), "6").getId()).isEqualTo("3");

	}

	@Test
	public void givenSummaryCacheWhenAddUpdatingRecordsThenInsertedInCache()
			throws Exception {

		givenTestRecords();

		assertThatTheThreeRecordsAreInCache();
	}

	@Test
	public void givenSummaryCacheWhenInsertingFullyLoadedRecordsThenInsertedInCache()
			throws Exception {

		givenTestRecords();
		resetCacheAndQueries();

		LogicalSearchQuery query = new LogicalSearchQuery(from(schemaType.type()).returnAll());
		recordsCaches.insert(zeCollection, searchServices.search(query), WAS_OBTAINED);

		assertThatTheThreeRecordsAreInCache();

	}

	@Test
	public void givenNotFullyLoadedRecordThenNotInsertedUnlessForced()
			throws Exception {

		givenTestRecords();
		resetCacheAndQueries();

		LogicalSearchQuery query = new LogicalSearchQuery(from(schemaType.type()).returnAll());
		query.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(schemaType.stringMetadata(),
				schemaType.numberMetadata()));
		List<Record> records = searchServices.search(query);

		for (Record record : records) {
			recordsCaches.insert(record, WAS_OBTAINED);
		}

		assertThat(recordsCache.get("1")).isNull();
		assertThat(recordsCache.get("2")).isNull();
		assertThat(recordsCache.get("3")).isNull();
		assertThat(recordsCache.getSummary("1")).isNull();
		assertThat(recordsCache.getSummary("2")).isNull();
		assertThat(recordsCache.getSummary("3")).isNull();

		for (Record record : records) {
			recordsCaches.insert(record, WAS_OBTAINED);
		}

		assertThatTheThreeRecordsAreInCache();
	}

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
		record3 = (TestRecord) transaction.add(newRecordOf("1", schemaType).withTitle("a")
				.set(schemaType.stringMetadata(), "1")
				.set(schemaType.numberMetadata(), 2.0)
				.set(schemaType.anotherStringMetadata(), "3"));

		transaction.add(newRecordOf("2", schemaType).withTitle("b")
				.set(schemaType.stringMetadata(), "4")
				.set(schemaType.anotherStringMetadata(), "5"));

		transaction.add(newRecordOf("3", schemaType).withTitle("c")
				.set(schemaType.stringMetadata(), "6")
				.set(schemaType.numberMetadata(), 7.0)
				.set(schemaType.anotherStringMetadata(), "8"));

		recordServices.execute(transaction);

	}

	private void assertThatTheThreeRecordsAreInCache() {

		Record record1 = recordsCache.getSummary("1");
		Record record2 = recordsCache.getSummary("2");
		Record record3 = recordsCache.getSummary("3");

		assertThat(recordsCache.get("1")).isNull();
		assertThat(recordsCache.get("2")).isNull();
		assertThat(recordsCache.get("3")).isNull();

		assertThat(record1).isNotNull();
		assertThat(record1.isFullyLoaded()).isFalse();
		assertThat(record1.<String>get(schemaType.anotherStringMetadata())).isNull();

		assertThat(record2).isNotNull();
		assertThat(record2.isFullyLoaded()).isFalse();
		assertThat(record2.<String>get(schemaType.anotherStringMetadata())).isNull();

		assertThat(record3).isNotNull();
		assertThat(record3.isFullyLoaded()).isFalse();
		assertThat(record3.<String>get(schemaType.anotherStringMetadata())).isNull();

		assertThat(recordsCache.getByMetadata(schemaType.stringMetadata(), "1")).isNull();
		assertThat(recordsCache.getSummaryByMetadata(schemaType.stringMetadata(), "1").getId()).isEqualTo("1");

		assertThat(recordsCache.getByMetadata(schemaType.stringMetadata(), "4")).isNull();
		assertThat(recordsCache.getSummaryByMetadata(schemaType.stringMetadata(), "4").getId()).isEqualTo("2");

		assertThat(recordsCache.getByMetadata(schemaType.stringMetadata(), "6")).isNull();
		assertThat(recordsCache.getSummaryByMetadata(schemaType.stringMetadata(), "6").getId()).isEqualTo("3");

		assertThat(record1.<String>get(schemaType.stringMetadata())).isEqualTo("1");
		assertThat(record2.<String>get(schemaType.stringMetadata())).isEqualTo("4");
		assertThat(record3.<String>get(schemaType.stringMetadata())).isEqualTo("6");

		assertThat(record1.<Double>get(schemaType.numberMetadata())).isEqualTo(2.0);
		assertThat(record2.<Double>get(schemaType.numberMetadata())).isNull();
		assertThat(record3.<Double>get(schemaType.numberMetadata())).isEqualTo(7.0);

		assertThat(record1.<String>get(schemaType.anotherStringMetadata())).isNull();
		assertThat(record2.<String>get(schemaType.anotherStringMetadata())).isNull();
		assertThat(record3.<String>get(schemaType.anotherStringMetadata())).isNull();

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
