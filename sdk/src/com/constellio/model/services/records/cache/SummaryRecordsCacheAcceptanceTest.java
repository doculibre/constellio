package com.constellio.model.services.records.cache;

import static com.constellio.model.services.records.cache.CacheConfig.permanentEssentialMetadatasCacheNotLoadedInitially;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsEssentialInSummary;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

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

public class SummaryRecordsCacheAcceptanceTest extends ConstellioTest {

	Transaction transaction;
	User adminInZeCollection, adminInAnotherCollection;

	TestRecord record1, record2, record3, record4, record;

	TestsSchemasSetup zeCollectionSchemas = new TestsSchemasSetup(zeCollection).withSecurityFlag(false);
	ZeSchemaMetadatas permanentSummaryCacheSchemaType = zeCollectionSchemas.new ZeSchemaMetadatas();

	RecordsCaches recordsCaches;
	RecordsCache zeCollectionRecordsCache;

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
				.withAStringMetadata(whichIsEssentialInSummary)
				.withANumberMetadata(whichIsEssentialInSummary)
				.withAnotherStringMetadata());

		adminInZeCollection = getModelLayerFactory().newUserServices().getUserInCollection("admin", zeCollection);

		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		cachelessRecordServices = getModelLayerFactory().newCachelessRecordServices();

		userServices = getModelLayerFactory().newUserServices();
		recordsCaches = getModelLayerFactory().getRecordsCaches();
		zeCollectionRecordsCache = getModelLayerFactory().getRecordsCaches().getCache(zeCollection);

		RecordsCache collection1Cache = getModelLayerFactory().getRecordsCaches().getCache(zeCollection);
		collection1Cache
				.configureCache(permanentEssentialMetadatasCacheNotLoadedInitially(permanentSummaryCacheSchemaType.type()));

		DataLayerSystemExtensions extensions = getDataLayerFactory().getExtensions().getSystemWideExtensions();
		queriesListener = new StatsBigVaultServerExtension();
		extensions.getBigVaultServerExtension().add(queriesListener);

	}

	@Test
	public void givenSummaryCacheThenRecordsAddedWhenGetById()
			throws Exception {

		givenTestRecords();
		resetCacheAndQueries();

		assertThat(zeCollectionRecordsCache.getSummary("1")).isNull();
		assertThat(zeCollectionRecordsCache.getSummary("1")).isNull();
		assertThat(zeCollectionRecordsCache.getSummary("2")).isNull();
		assertThat(zeCollectionRecordsCache.getSummary("3")).isNull();

		getModelLayerFactory().newRecordServices().getDocumentById("1");
		getModelLayerFactory().newRecordServices().getDocumentById("2");
		getModelLayerFactory().newRecordServices().getDocumentById("3");

		Record record1 = zeCollectionRecordsCache.getSummary("1");
		Record record2 = zeCollectionRecordsCache.getSummary("2");
		Record record3 = zeCollectionRecordsCache.getSummary("3");

		assertThat(zeCollectionRecordsCache.get("1")).isNull();
		assertThat(zeCollectionRecordsCache.get("2")).isNull();
		assertThat(zeCollectionRecordsCache.get("3")).isNull();

		assertThat(record1).isNotNull();
		assertThat(record1.isFullyLoaded()).isFalse();
		assertThat(record1.get(permanentSummaryCacheSchemaType.anotherStringMetadata())).isNull();

		assertThat(record2).isNotNull();
		assertThat(record2.isFullyLoaded()).isFalse();
		assertThat(record2.get(permanentSummaryCacheSchemaType.anotherStringMetadata())).isNull();

		assertThat(record3).isNotNull();
		assertThat(record3.isFullyLoaded()).isFalse();
		assertThat(record3.get(permanentSummaryCacheSchemaType.anotherStringMetadata())).isNull();

	}

	@Test
	public void givenSummaryCacheWhenAddUpdatingRecordsThenInsertedInCache()
			throws Exception {

		givenTestRecords();

		Record record1 = zeCollectionRecordsCache.getSummary("1");
		Record record2 = zeCollectionRecordsCache.getSummary("2");
		Record record3 = zeCollectionRecordsCache.getSummary("3");

		assertThat(zeCollectionRecordsCache.get("1")).isNull();
		assertThat(zeCollectionRecordsCache.get("2")).isNull();
		assertThat(zeCollectionRecordsCache.get("3")).isNull();

		assertThat(record1).isNotNull();
		assertThat(record1.isFullyLoaded()).isFalse();
		assertThat(record1.get(permanentSummaryCacheSchemaType.anotherStringMetadata())).isNull();

		assertThat(record2).isNotNull();
		assertThat(record2.isFullyLoaded()).isFalse();
		assertThat(record2.get(permanentSummaryCacheSchemaType.anotherStringMetadata())).isNull();

		assertThat(record3).isNotNull();
		assertThat(record3.isFullyLoaded()).isFalse();
		assertThat(record3.get(permanentSummaryCacheSchemaType.anotherStringMetadata())).isNull();

	}

	@Test
	public void givenSummaryCacheWhenSearchingRecordsWithAllFieldsThenResultsInsertedInCache()
			throws Exception {

		givenTestRecords();
		resetCacheAndQueries();

		LogicalSearchQuery query = new LogicalSearchQuery(from(permanentSummaryCacheSchemaType.type()).returnAll());
		recordsCaches.insert(zeCollection, searchServices.search(query));

		Record record1 = zeCollectionRecordsCache.getSummary("1");
		Record record2 = zeCollectionRecordsCache.getSummary("2");
		Record record3 = zeCollectionRecordsCache.getSummary("3");
		assertThat(zeCollectionRecordsCache.get("1")).isNull();
		assertThat(zeCollectionRecordsCache.get("2")).isNull();
		assertThat(zeCollectionRecordsCache.get("3")).isNull();

		assertThat(record1).isNotNull();
		assertThat(record1.isFullyLoaded()).isFalse();
		assertThat(record1.get(permanentSummaryCacheSchemaType.anotherStringMetadata())).isNull();

		assertThat(record2).isNotNull();
		assertThat(record2.isFullyLoaded()).isFalse();
		assertThat(record2.get(permanentSummaryCacheSchemaType.anotherStringMetadata())).isNull();

		assertThat(record3).isNotNull();
		assertThat(record3.isFullyLoaded()).isFalse();
		assertThat(record3.get(permanentSummaryCacheSchemaType.anotherStringMetadata())).isNull();

	}

	@Test
	public void givenNotFullyLoadedRecordThenNotInsertedUnlessForced()
			throws Exception {

		givenTestRecords();
		resetCacheAndQueries();

		LogicalSearchQuery query = new LogicalSearchQuery(from(permanentSummaryCacheSchemaType.type()).returnAll());
		query.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(permanentSummaryCacheSchemaType.stringMetadata(),
				permanentSummaryCacheSchemaType.numberMetadata()));
		List<Record> records = searchServices.search(query);

		for (Record record : records) {
			recordsCaches.insert(record);
		}

		assertThat(zeCollectionRecordsCache.get("1")).isNull();
		assertThat(zeCollectionRecordsCache.get("2")).isNull();
		assertThat(zeCollectionRecordsCache.get("3")).isNull();
		assertThat(zeCollectionRecordsCache.getSummary("1")).isNull();
		assertThat(zeCollectionRecordsCache.getSummary("2")).isNull();
		assertThat(zeCollectionRecordsCache.getSummary("3")).isNull();

		for (Record record : records) {
			recordsCaches.forceInsert(record);
		}

		Record record1 = zeCollectionRecordsCache.getSummary("1");
		Record record2 = zeCollectionRecordsCache.getSummary("2");
		Record record3 = zeCollectionRecordsCache.getSummary("3");
		assertThat(zeCollectionRecordsCache.get("1")).isNull();
		assertThat(zeCollectionRecordsCache.get("2")).isNull();
		assertThat(zeCollectionRecordsCache.get("3")).isNull();

		assertThat(record1).isNotNull();
		assertThat(record1.isFullyLoaded()).isFalse();
		assertThat(record1.get(permanentSummaryCacheSchemaType.anotherStringMetadata())).isNull();

		assertThat(record2).isNotNull();
		assertThat(record2.isFullyLoaded()).isFalse();
		assertThat(record2.get(permanentSummaryCacheSchemaType.anotherStringMetadata())).isNull();

		assertThat(record3).isNotNull();
		assertThat(record3.isFullyLoaded()).isFalse();
		assertThat(record3.get(permanentSummaryCacheSchemaType.anotherStringMetadata())).isNull();

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
		record3 = (TestRecord) transaction.add(newRecordOf("1", permanentSummaryCacheSchemaType).withTitle("a")
				.set(permanentSummaryCacheSchemaType.stringMetadata(), "1")
				.set(permanentSummaryCacheSchemaType.numberMetadata(), 2.0)
				.set(permanentSummaryCacheSchemaType.anotherStringMetadata(), "3"));

		transaction.add(newRecordOf("2", permanentSummaryCacheSchemaType).withTitle("b")
				.set(permanentSummaryCacheSchemaType.stringMetadata(), "4")
				.set(permanentSummaryCacheSchemaType.anotherStringMetadata(), "5"));

		transaction.add(newRecordOf("3", permanentSummaryCacheSchemaType).withTitle("c")
				.set(permanentSummaryCacheSchemaType.stringMetadata(), "6")
				.set(permanentSummaryCacheSchemaType.numberMetadata(), 7.0)
				.set(permanentSummaryCacheSchemaType.anotherStringMetadata(), "8"));

		transaction.add(newRecordOf("4", permanentSummaryCacheSchemaType).withTitle("d")
				.set(permanentSummaryCacheSchemaType.stringMetadata(), "9")
				.set(permanentSummaryCacheSchemaType.numberMetadata(), 10.0)
				.set(permanentSummaryCacheSchemaType.anotherStringMetadata(), "11"));

		transaction.add(newRecordOf("5", permanentSummaryCacheSchemaType).withTitle("e")
				.set(permanentSummaryCacheSchemaType.stringMetadata(), "12")
				.set(permanentSummaryCacheSchemaType.numberMetadata(), 13.0)
				.set(permanentSummaryCacheSchemaType.anotherStringMetadata(), "14"));

		recordServices.execute(transaction);

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
