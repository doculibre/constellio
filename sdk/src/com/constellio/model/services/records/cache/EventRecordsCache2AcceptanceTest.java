package com.constellio.model.services.records.cache;

import com.constellio.data.dao.dto.records.RecordDTOMode;
import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.data.test.RandomWordsIterator;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.conf.PropertiesModelLayerConfiguration.InMemoryModelLayerConfiguration;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.RecordCacheType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordDeleteServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesImpl;
import com.constellio.model.services.records.cache.NewVolatileRecordCacheAcceptanceTest.TestHook;
import com.constellio.model.services.records.cache.eventBus.EventsBusRecordsCachesImpl;
import com.constellio.model.services.records.cache.hooks.DeterminedHookCacheInsertion;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.GetByIdCounter;
import com.constellio.sdk.tests.ModelLayerConfigurationAlteration;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ThirdSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import com.constellio.sdk.tests.setups.SchemaShortcuts;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.LongAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

import static com.constellio.data.test.RandomWordsIterator.createFor;
import static com.constellio.data.utils.Octets.megaoctets;
import static com.constellio.model.entities.schemas.RecordCacheType.SUMMARY_CACHED_WITHOUT_VOLATILE;
import static com.constellio.model.entities.schemas.RecordCacheType.SUMMARY_CACHED_WITH_VOLATILE;
import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.entities.schemas.Schemas.TITLE;
import static com.constellio.model.entities.schemas.Schemas.TITLE_CODE;
import static com.constellio.model.services.records.cache.hooks.DeterminedHookCacheInsertion.DEFAULT_INSERT;
import static com.constellio.model.services.records.cache.hooks.DeterminedHookCacheInsertion.INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT;
import static com.constellio.model.services.records.cache.hooks.DeterminedHookCacheInsertion.INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT_WITHOUT_VOLATILE;
import static com.constellio.model.services.records.cache.hooks.DeterminedHookCacheInsertion.INSERT_WITH_HOOK_REPLACING_DEFAULT_INSERT;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromEveryTypesOfEveryCollection;
import static com.constellio.sdk.tests.TestUtils.assertThatStream;
import static com.constellio.sdk.tests.TestUtils.linkEventBus;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsUnique;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
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

	EventsBusRecordsCachesImpl recordsCaches;
	EventsBusRecordsCachesImpl otherInstanceRecordsCaches;
	RecordsCache zeCollectionRecordsCache;
	RecordsCache otherInstanceZeCollectionRecordsCache;
	RecordsCache anotherCollectionRecordsCache;

	RecordServices recordServices, otherInstanceRecordServices;
	RecordServicesImpl cachelessRecordServices;
	SearchServices searchServices, otherInstanceSearchServices;

	StatsBigVaultServerExtension queriesListener;
	StatsBigVaultServerExtension otherSystemQueriesListener;

	GetByIdCounter instanceGetByIdCounter;
	GetByIdCounter otherInstanceGetByIdCounter;

	BiFunction<RecordsCaches, String, List<String>> getIdByTitle = (RecordsCaches aCache, String title) -> aCache.getRecordsByIndexedMetadata(
			zeCollectionSchemaWithVolatileCache.type(), zeCollectionSchemaWithVolatileCache.metadata("title"), title)
			.map(Record::getId).collect(toList());

	BiFunction<RecordsCaches, String, List<String>> getIdSummaryByTitle = (RecordsCaches aCache, String title) -> aCache.getRecordsSummaryByIndexedMetadata(
			zeCollectionSchemaWithVolatileCache.type(), zeCollectionSchemaWithVolatileCache.metadata("title"), title)
			.map(Record::getId).collect(toList());

	boolean useZeroPaddedIds;


	public EventRecordsCache2AcceptanceTest(String testCase) {
		this.useZeroPaddedIds = testCase.equals("zero-padded-ids");
	}

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> testCases() {
		return Arrays.asList(new Object[][]{{"zero-padded-ids"}, {"string-ids"}});
	}


	@Before
	public void setUp()
			throws Exception {

		configure(new ModelLayerConfigurationAlteration() {
			@Override
			public void alter(InMemoryModelLayerConfiguration configuration) {
				configuration.setRecordsVolatileCacheMemorySize("10M");
			}
		});

		givenCollection("zeCollection").withAllTestUsers();
		givenCollection("anotherCollection").withAllTestUsers();

		inCollection(zeCollection).giveWriteAccessTo(admin);
		inCollection(anotherCollection).giveWriteAccessTo(admin);

		defineSchemasManager()
				.using(zeCollectionSchemas.withAStringMetadata(whichIsUnique).withAnotherStringMetadata()
						.withAStringMetadataInAnotherSchema(whichIsUnique).withALargeTextMetadata());
		defineSchemasManager().using(anotherCollectionSchemas.withAStringMetadataInAnotherSchema(whichIsUnique));

		ModelLayerFactory otherModelLayerFactory = getModelLayerFactory("other");

		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		cachelessRecordServices = getModelLayerFactory().newCachelessRecordServices();

		otherInstanceRecordServices = otherModelLayerFactory.newRecordServices();
		otherInstanceSearchServices = otherModelLayerFactory.newSearchServices();

		recordsCaches = (EventsBusRecordsCachesImpl) getModelLayerFactory().getRecordsCaches();
		zeCollectionRecordsCache = getModelLayerFactory().getRecordsCaches().getCache(zeCollection);
		anotherCollectionRecordsCache = getModelLayerFactory().getRecordsCaches().getCache(anotherCollection);

		otherInstanceRecordsCaches = (EventsBusRecordsCachesImpl) otherModelLayerFactory.getRecordsCaches();
		otherInstanceZeCollectionRecordsCache = otherModelLayerFactory.getRecordsCaches().getCache(zeCollection);

		instanceGetByIdCounter = new GetByIdCounter(getClass()).listening(getModelLayerFactory());
		otherInstanceGetByIdCounter = new GetByIdCounter(getClass()).listening(otherModelLayerFactory);

		linkEventBus(getDataLayerFactory(), otherModelLayerFactory.getDataLayerFactory());

		zeCollectionSchemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(zeCollectionSchemaWithVolatileCache.type().getCode()).setRecordCacheType(RecordCacheType.SUMMARY_CACHED_WITH_VOLATILE);
				types.getSchemaType(zeCollectionSchemaWithVolatileCache.type().getCode()).getDefaultSchema().get("title").setCacheIndex(true);
			}
		});

		zeCollectionSchemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(zeCollectionSchemaWithPermanentCache.type().getCode()).setRecordCacheType(
						RecordCacheType.FULLY_CACHED);
				types.getSchemaType(zeCollectionSchemaWithPermanentCache.type().getCode()).getDefaultSchema().get("title").setCacheIndex(true);
			}
		});

		zeCollectionSchemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(zeCollectionSchemaWithSummaryPermanentCache.type().getCode()).setRecordCacheType(
						SUMMARY_CACHED_WITHOUT_VOLATILE);
				types.getSchemaType(zeCollectionSchemaWithSummaryPermanentCache.type().getCode()).getDefaultSchema().get("title").setCacheIndex(true);
			}
		});

		anotherCollectionSchemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(anotherCollectionSchemaWithVolatileCache.type().getCode()).setRecordCacheType(RecordCacheType.SUMMARY_CACHED_WITH_VOLATILE);
				types.getSchemaType(anotherCollectionSchemaWithVolatileCache.type().getCode()).getDefaultSchema().get("title").setCacheIndex(true);
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
				.getSchemaType(zeCollectionSchemaWithSummaryPermanentCache.typeCode()).getCacheType()).isEqualTo(SUMMARY_CACHED_WITHOUT_VOLATILE);

		assertThat(getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchemaType(zeCollectionSchemaWithVolatileCache.typeCode()).getCacheType()).isEqualTo(RecordCacheType.SUMMARY_CACHED_WITH_VOLATILE);

		assertThat(getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getDefaultSchema(zeCollectionSchemaWithVolatileCache.typeCode()).get("title").isCacheIndex()).isTrue();

		assertThat(otherModelLayerFactory.getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchemaType(zeCollectionSchemaWithPermanentCache.typeCode()).getCacheType()).isEqualTo(RecordCacheType.FULLY_CACHED);

		assertThat(otherModelLayerFactory.getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchemaType(zeCollectionSchemaWithSummaryPermanentCache.typeCode()).getCacheType()).isEqualTo(SUMMARY_CACHED_WITHOUT_VOLATILE);

		assertThat(otherModelLayerFactory.getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchemaType(zeCollectionSchemaWithVolatileCache.typeCode()).getCacheType()).isEqualTo(RecordCacheType.SUMMARY_CACHED_WITH_VOLATILE);

		assertThat(otherModelLayerFactory.getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getDefaultSchema(zeCollectionSchemaWithVolatileCache.typeCode()).get("title").isCacheIndex()).isTrue();
	}


	@Test
	public void test() throws Exception {

		//Équivalent de 2 unités administratives
		recordServices.add(newRecordOf(idOf(1001), zeCollectionSchemaWithPermanentCache).withTitle("1001"));
		recordServices.add(newRecordOf(idOf(100002), zeCollectionSchemaWithPermanentCache).withTitle("100002"));

		//Équivalent de 2 dossiers
		recordServices.add(newRecordOf(idOf(100003), zeCollectionSchemaWithSummaryPermanentCache).withTitle("100003"));
		recordServices.add(newRecordOf(idOf(100004), zeCollectionSchemaWithSummaryPermanentCache).withTitle("100004"));

		validateAvailableEverywhere(zeCollectionSchemaWithPermanentCache.type(), idOf(1001), idOf(100002));
		validateAvailableEverywhere(zeCollectionSchemaWithSummaryPermanentCache.type(), idOf(100003), idOf(100004));

		//On repart le système en compactant la cache
		Toggle.STRUCTURE_CACHE_BASED_ON_EXISTING_IDS.enable();
		Toggle.STRUCTURE_CACHE_BASED_ON_EXISTING_IDS_ON_DEV_STATION.enable();
		restartLayers();
		validateAvailableEverywhere(zeCollectionSchemaWithPermanentCache.type(), idOf(1001), idOf(100002));
		//validateAvailableEverywhere(zeCollectionSchemaWithSummaryPermanentCache.type(), idOf(100003), idOf(100004));

		//	assertThat(recordsCaches.getRecordsCachesDataStore().getIntIdsDataStore().getIds().size()).isEqualTo(56);
		//	assertThat(recordsCaches.getRecordsCachesDataStore().getIntIdsDataStore().getFullyCachedData()).hasSize(56);

		//Sauf qu'aucune place n'a été gardée pour 100001, car il n'était pas dans la liste
		getModelLayerFactory().newRecordServices().add(newRecordOf(idOf(100001), zeCollectionSchemaWithPermanentCache).withTitle("100001"));

		validateAvailableEverywhere(zeCollectionSchemaWithPermanentCache.type(), idOf(1001), idOf(100001), idOf(100002));
		//java.lang.IndexOutOfBoundsException: Index: 60, Size: 59

	}

	@Test
	public void test2() throws Exception {


		recordServices.add(newRecordOf(idOf(100003), zeCollectionSchemaWithSummaryPermanentCache).withTitle("100003"));
		recordServices.add(newRecordOf(idOf(100004), zeCollectionSchemaWithSummaryPermanentCache).withTitle("100004"));
		validateAvailableEverywhere(zeCollectionSchemaWithSummaryPermanentCache.type(), idOf(100003), idOf(100004));

		Toggle.STRUCTURE_CACHE_BASED_ON_EXISTING_IDS.enable();
		Toggle.STRUCTURE_CACHE_BASED_ON_EXISTING_IDS_ON_DEV_STATION.enable();
		restartLayers();

		validateAvailableEverywhere(zeCollectionSchemaWithSummaryPermanentCache.type(), idOf(100003), idOf(100004));
		getModelLayerFactory().newRecordServices().add(permanentRecord1 = newRecordOf(idOf(1009), zeCollectionSchemaWithPermanentCache).withTitle("1009"));

		validateAvailableEverywhere(zeCollectionSchemaWithPermanentCache.type(), idOf(1009));
		validateAvailableEverywhere(zeCollectionSchemaWithSummaryPermanentCache.type(), idOf(100003), idOf(100004));


	}

	public void restartLayers() {
		super.restartLayers();
		ModelLayerFactory otherModelLayerFactory = getModelLayerFactory("other");

		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		cachelessRecordServices = getModelLayerFactory().newCachelessRecordServices();

		otherInstanceRecordServices = otherModelLayerFactory.newRecordServices();
		otherInstanceSearchServices = otherModelLayerFactory.newSearchServices();

		recordsCaches = (EventsBusRecordsCachesImpl) getModelLayerFactory().getRecordsCaches();
		zeCollectionRecordsCache = getModelLayerFactory().getRecordsCaches().getCache(zeCollection);
		anotherCollectionRecordsCache = getModelLayerFactory().getRecordsCaches().getCache(anotherCollection);

		otherInstanceRecordsCaches = (EventsBusRecordsCachesImpl) otherModelLayerFactory.getRecordsCaches();
		otherInstanceZeCollectionRecordsCache = otherModelLayerFactory.getRecordsCaches().getCache(zeCollection);

		instanceGetByIdCounter = new GetByIdCounter(getClass()).listening(getModelLayerFactory());
		otherInstanceGetByIdCounter = new GetByIdCounter(getClass()).listening(otherModelLayerFactory);

		linkEventBus(getDataLayerFactory(), otherModelLayerFactory.getDataLayerFactory());
	}

	private void validateAvailableEverywhere(MetadataSchemaType schemaType, String... ids) {
		for (RecordsCaches cache : asList(recordsCaches, otherInstanceRecordsCaches)) {

			if (schemaType.getCacheType().hasPermanentCache()) {
				assertThat(cache.stream(schemaType).collect(toList())).extracting("id").containsOnly(ids);
				assertThat(cache.stream(zeCollection)
						.filter((r) -> r.getSchemaCode().startsWith(schemaType.getCode() + "_"))
						.collect(toList())).extracting("id").containsOnly(ids);
				for (String id : ids) {
					assertThat(cache.getRecordSummary(id)).isNotNull();
				}
			}

		}
	}

	@Test
	public void whenInsertingRecordsThenInsertedInLocalVolatileCacheAndInAllPermanentCache() throws Exception {

		assertThat(recordsCaches.getRecord(idOf(101))).isNull();
		assertThat(recordsCaches.getRecord(idOf(102))).isNull();
		assertThat(recordsCaches.getRecord(idOf(1))).isNull();

		assertThat(otherInstanceRecordsCaches.getRecord(idOf(101))).isNull();
		assertThat(otherInstanceRecordsCaches.getRecord(idOf(102))).isNull();
		assertThat(otherInstanceRecordsCaches.getRecord(idOf(1))).isNull();

		tx.add(permanentRecord1 = newRecordOf(idOf(101), zeCollectionSchemaWithPermanentCache).withTitle("b")
				.set(zeCollectionSchemaWithPermanentCache.stringMetadata(), "p1Code"));

		tx.add(permanentRecord2 = newRecordOf(idOf(102), zeCollectionSchemaWithSummaryPermanentCache).withTitle("c"));

		tx.add(volatileRecord1 = newRecordOf(idOf(1), zeCollectionSchemaWithVolatileCache).withTitle("d")
				.set(zeCollectionSchemaWithVolatileCache.stringMetadata(), "code18"));

		instanceGetByIdCounter.reset();
		otherInstanceGetByIdCounter.reset();

		recordServices.execute(tx);

		assertThat(instanceGetByIdCounter.newCalls()).hasSize(0);
		if (useZeroPaddedIds) {
			assertThat(otherInstanceGetByIdCounter.newIdCalled()).containsOnly(idOf(101), idOf(102), idOf(1));
		} else {
			assertThat(otherInstanceGetByIdCounter.newIdCalled()).containsOnly(idOf(101), idOf(102), idOf(1));
		}


		assertThat(recordsCaches.getRecord(idOf(101)).isSummary()).isFalse();
		assertThat(recordsCaches.getRecord(idOf(101)).getTitle()).isEqualTo("b");
		assertThat(recordsCaches.getRecord(idOf(101)).
				<String>get(zeCollectionSchemaWithPermanentCache.stringMetadata())).isEqualTo("p1Code");

		assertThat(recordsCaches.getRecord(idOf(102))).isNull();
		assertThat(recordsCaches.getRecordSummary(idOf(102)).isSummary()).isTrue();
		assertThat(recordsCaches.getRecordSummary(idOf(102)).getTitle()).isEqualTo("c");

		assertThat(recordsCaches.getRecord(idOf(1)).isSummary()).isFalse();
		assertThat(recordsCaches.getRecordSummary(idOf(1)).isSummary()).isTrue();
		assertThat(recordsCaches.getRecordSummary(idOf(1)).getTitle()).isEqualTo("d");
		assertThat(recordsCaches.getRecordSummary(idOf(1))
				.<String>get(zeCollectionSchemaWithVolatileCache.stringMetadata())).isEqualTo("code18");
		assertThat(recordsCaches.getRecordsSummaryByIndexedMetadata(zeCollectionSchemaWithPermanentCache.type(), TITLE, "b")
				.map(Record::getId).collect(toList())).containsOnly(idOf(101));
		assertThat(recordsCaches.getRecordsSummaryByIndexedMetadata(zeCollectionSchemaWithPermanentCache.type(), TITLE, "e")
				.map(Record::getId).collect(toList())).isEmpty();
		assertThat(recordsCaches.getRecordsSummaryByIndexedMetadata(zeCollectionSchemaWithSummaryPermanentCache.type(), TITLE, "c")
				.map(Record::getId).collect(toList())).containsOnly(idOf(102));
		assertThat(recordsCaches.getRecordsSummaryByIndexedMetadata(zeCollectionSchemaWithSummaryPermanentCache.type(), TITLE, "f")
				.map(Record::getId).collect(toList())).isEmpty();
		assertThat(recordsCaches.getRecordsSummaryByIndexedMetadata(zeCollectionSchemaWithVolatileCache.type(), TITLE, "d")
				.map(Record::getId).collect(toList())).containsOnly(idOf(1));
		assertThat(recordsCaches.getRecordsSummaryByIndexedMetadata(zeCollectionSchemaWithVolatileCache.type(), TITLE, "g")
				.map(Record::getId).collect(toList())).isEmpty();

		assertThat(otherInstanceRecordsCaches.getRecord(idOf(101)).isSummary()).isFalse();
		assertThat(otherInstanceRecordsCaches.getRecord(idOf(101)).getTitle()).isEqualTo("b");
		assertThat(otherInstanceRecordsCaches.getRecord(idOf(101)).
				<String>get(zeCollectionSchemaWithPermanentCache.stringMetadata())).isEqualTo("p1Code");
		assertThat(otherInstanceRecordsCaches.getRecord(idOf(102))).isNull();

		assertThat(otherInstanceRecordsCaches.getRecordSummary(idOf(102)).isSummary()).isTrue();
		assertThat(otherInstanceRecordsCaches.getRecordSummary(idOf(102)).getTitle()).isEqualTo("c");

		assertThat(otherInstanceRecordsCaches.getRecord(idOf(1))).isNull();
		assertThat(otherInstanceRecordsCaches.getRecordSummary(idOf(1)).isSummary()).isTrue();
		assertThat(otherInstanceRecordsCaches.getRecordSummary(idOf(1)).getTitle()).isEqualTo("d");
		assertThat(otherInstanceRecordsCaches.getRecordSummary(idOf(1))
				.<String>get(zeCollectionSchemaWithVolatileCache.stringMetadata())).isEqualTo("code18");
		otherInstanceRecordServices.getDocumentById(idOf(1));
		assertThat(otherInstanceRecordsCaches.getRecord(idOf(1))).isNotNull();


		assertThat(otherInstanceRecordsCaches.getRecordsSummaryByIndexedMetadata(zeCollectionSchemaWithPermanentCache.type(), TITLE, "b")
				.map(Record::getId).collect(toList())).containsOnly(idOf(101));
		assertThat(otherInstanceRecordsCaches.getRecordsSummaryByIndexedMetadata(zeCollectionSchemaWithPermanentCache.type(), TITLE, "e")
				.map(Record::getId).collect(toList())).isEmpty();
		assertThat(otherInstanceRecordsCaches.getRecordsSummaryByIndexedMetadata(zeCollectionSchemaWithSummaryPermanentCache.type(), TITLE, "c")
				.map(Record::getId).collect(toList())).containsOnly(idOf(102));
		assertThat(otherInstanceRecordsCaches.getRecordsSummaryByIndexedMetadata(zeCollectionSchemaWithSummaryPermanentCache.type(), TITLE, "f")
				.map(Record::getId).collect(toList())).isEmpty();
		assertThat(otherInstanceRecordsCaches.getRecordsSummaryByIndexedMetadata(zeCollectionSchemaWithVolatileCache.type(), TITLE, "d")
				.map(Record::getId).collect(toList())).containsOnly(idOf(1));
		assertThat(otherInstanceRecordsCaches.getRecordsSummaryByIndexedMetadata(zeCollectionSchemaWithVolatileCache.type(), TITLE, "g")
				.map(Record::getId).collect(toList())).isEmpty();

		//Update with new values
		tx = new Transaction();
		tx.add(permanentRecord1.withTitle("e")
				.set(zeCollectionSchemaWithPermanentCache.stringMetadata(), "p1Code2"));

		tx.add(permanentRecord2.withTitle("f"));

		tx.add(volatileRecord1.withTitle("g")
				.set(zeCollectionSchemaWithVolatileCache.stringMetadata(), "code12"));

		otherInstanceGetByIdCounter.reset();
		recordServices.execute(tx);
		if (useZeroPaddedIds) {
			assertThat(otherInstanceGetByIdCounter.newIdCalled()).containsOnly(idOf(101), idOf(102), idOf(1));
		} else {
			assertThat(otherInstanceGetByIdCounter.newIdCalled()).containsOnly(idOf(101), idOf(102), idOf(1));
		}


		assertThat(recordsCaches.getRecord(idOf(101)).isSummary()).isFalse();
		assertThat(recordsCaches.getRecord(idOf(101)).getTitle()).isEqualTo("e");
		assertThat(recordsCaches.getRecord(idOf(101)).
				<String>get(zeCollectionSchemaWithPermanentCache.stringMetadata())).isEqualTo("p1Code2");

		assertThat(recordsCaches.getRecord(idOf(102))).isNull();
		assertThat(recordsCaches.getRecordSummary(idOf(102)).isSummary()).isTrue();
		assertThat(recordsCaches.getRecordSummary(idOf(102)).getTitle()).isEqualTo("f");

		assertThat(recordsCaches.getRecord(idOf(1)).isSummary()).isFalse();
		assertThat(recordsCaches.getRecordSummary(idOf(1)).isSummary()).isTrue();
		assertThat(recordsCaches.getRecordSummary(idOf(1)).getTitle()).isEqualTo("g");
		assertThat(recordsCaches.getRecordSummary(idOf(1))
				.<String>get(zeCollectionSchemaWithVolatileCache.stringMetadata())).isEqualTo("code12");
		assertThat(recordsCaches.getRecordsSummaryByIndexedMetadata(zeCollectionSchemaWithPermanentCache.type(), TITLE, "b")
				.map(Record::getId).collect(toList())).isEmpty();
		assertThat(recordsCaches.getRecordsSummaryByIndexedMetadata(zeCollectionSchemaWithPermanentCache.type(), TITLE, "e")
				.map(Record::getId).collect(toList())).containsOnly(idOf(101));
		assertThat(recordsCaches.getRecordsSummaryByIndexedMetadata(zeCollectionSchemaWithSummaryPermanentCache.type(), TITLE, "c")
				.map(Record::getId).collect(toList())).isEmpty();
		assertThat(recordsCaches.getRecordsSummaryByIndexedMetadata(zeCollectionSchemaWithSummaryPermanentCache.type(), TITLE, "f")
				.map(Record::getId).collect(toList())).containsOnly(idOf(102));
		assertThat(recordsCaches.getRecordsSummaryByIndexedMetadata(zeCollectionSchemaWithVolatileCache.type(), TITLE, "d")
				.map(Record::getId).collect(toList())).isEmpty();
		assertThat(recordsCaches.getRecordsSummaryByIndexedMetadata(zeCollectionSchemaWithVolatileCache.type(), TITLE, "g")
				.map(Record::getId).collect(toList())).containsOnly(idOf(1));

		assertThat(otherInstanceRecordsCaches.getRecord(idOf(101)).isSummary()).isFalse();
		assertThat(otherInstanceRecordsCaches.getRecord(idOf(101)).getTitle()).isEqualTo("e");
		assertThat(recordsCaches.getRecord(idOf(101)).
				<String>get(zeCollectionSchemaWithPermanentCache.stringMetadata())).isEqualTo("p1Code2");
		assertThat(otherInstanceRecordsCaches.getRecord(idOf(102))).isNull();

		assertThat(otherInstanceRecordsCaches.getRecordSummary(idOf(102)).isSummary()).isTrue();
		assertThat(otherInstanceRecordsCaches.getRecordSummary(idOf(102)).getTitle()).isEqualTo("f");

		assertThat(otherInstanceRecordsCaches.getRecord(idOf(1))).isNull();
		assertThat(otherInstanceRecordsCaches.getRecordSummary(idOf(1)).isSummary()).isTrue();
		assertThat(otherInstanceRecordsCaches.getRecordSummary(idOf(1)).getTitle()).isEqualTo("g");
		assertThat(otherInstanceRecordsCaches.getRecordSummary(idOf(1))
				.<String>get(zeCollectionSchemaWithVolatileCache.stringMetadata())).isEqualTo("code12");

		assertThat(otherInstanceRecordsCaches.getRecordsSummaryByIndexedMetadata(zeCollectionSchemaWithPermanentCache.type(), TITLE, "b")
				.map(Record::getId).collect(toList())).isEmpty();
		assertThat(otherInstanceRecordsCaches.getRecordsSummaryByIndexedMetadata(zeCollectionSchemaWithPermanentCache.type(), TITLE, "e")
				.map(Record::getId).collect(toList())).containsOnly(idOf(101));
		assertThat(otherInstanceRecordsCaches.getRecordsSummaryByIndexedMetadata(zeCollectionSchemaWithSummaryPermanentCache.type(), TITLE, "c")
				.map(Record::getId).collect(toList())).isEmpty();
		assertThat(otherInstanceRecordsCaches.getRecordsSummaryByIndexedMetadata(zeCollectionSchemaWithSummaryPermanentCache.type(), TITLE, "f")
				.map(Record::getId).collect(toList())).containsOnly(idOf(102));
		assertThat(otherInstanceRecordsCaches.getRecordsSummaryByIndexedMetadata(zeCollectionSchemaWithVolatileCache.type(), TITLE, "d")
				.map(Record::getId).collect(toList())).isEmpty();
		assertThat(otherInstanceRecordsCaches.getRecordsSummaryByIndexedMetadata(zeCollectionSchemaWithVolatileCache.type(), TITLE, "g")
				.map(Record::getId).collect(toList())).containsOnly(idOf(1));

	}


	@Test
	public void whenRetrievingRecordsFromSolrThenInsertedInLocalVolatileCacheAndNotInRemoteOnes() throws Exception {

		recordServices.add(volatileRecord1 = newRecordOf(idOf(1), zeCollectionSchemaWithVolatileCache));
		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1));

		recordServices.add(volatileRecord2 = newRecordOf(idOf(2), zeCollectionSchemaWithVolatileCache));
		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2));

		recordServices.add(volatileRecord3 = newRecordOf(idOf(3), zeCollectionSchemaWithVolatileCache));
		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2), idOf(3));

		recordServices.add(volatileRecord4 = newRecordOf(idOf(4), zeCollectionSchemaWithVolatileCache));
		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2), idOf(3), idOf(4));

		assertThatStream(((RecordsCaches2Impl) otherInstanceRecordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).isEmpty();

		otherInstanceRecordServices.getDocumentById(idOf(1));
		otherInstanceRecordServices.getDocumentById(idOf(4));

		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2), idOf(3), idOf(4));

		assertThatStream(((RecordsCaches2Impl) otherInstanceRecordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(4));

	}


	@Test
	public void whenDeletingRecordsFromThenRemovedFromAllVolatileAndPermanentCaches() throws Exception {

		recordServices.add(volatileRecord1 = newRecordOf(idOf(1), zeCollectionSchemaWithVolatileCache).withTitle("A"));
		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1));

		recordServices.add(volatileRecord2 = newRecordOf(idOf(2), zeCollectionSchemaWithVolatileCache).withTitle("B"));
		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2));

		recordServices.add(volatileRecord3 = newRecordOf(idOf(3), zeCollectionSchemaWithVolatileCache).withTitle("C"));
		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2), idOf(3));

		otherInstanceRecordServices.getDocumentById(idOf(1));
		otherInstanceRecordServices.getDocumentById(idOf(2));

		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2), idOf(3));
		assertThatStream(((RecordsCaches2Impl) otherInstanceRecordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2));
		assertThatStream(recordsCaches.stream(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2), idOf(3));
		assertThatStream(otherInstanceRecordsCaches.stream(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2), idOf(3));

		//Deleting the record, it is updated on the instance volatile cache, removed remotely
		recordServices.logicallyDelete(volatileRecord2, User.GOD);

		long newVersion = volatileRecord2.getVersion();

		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2), idOf(3));

		assertThatStream(((RecordsCaches2Impl) otherInstanceRecordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1));


		assertThatStream(recordsCaches.stream(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2), idOf(3));

		assertThatStream(otherInstanceRecordsCaches.stream(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2), idOf(3));

		assertThat(recordsCaches.getRecord(idOf(2)).getVersion()).isEqualTo(newVersion);
		assertThat(recordsCaches.getRecordSummary(idOf(2)).getVersion()).isEqualTo(newVersion);
		assertThat(otherInstanceRecordsCaches.getRecordSummary(idOf(2)).getVersion()).isEqualTo(newVersion);

		assertThat(recordsCaches.getRecord(idOf(2)).<Boolean>get(Schemas.LOGICALLY_DELETED_STATUS)).isTrue();
		assertThat(recordsCaches.getRecordSummary(idOf(2)).<Boolean>get(Schemas.LOGICALLY_DELETED_STATUS)).isTrue();
		assertThat(otherInstanceRecordsCaches.getRecordSummary(idOf(2)).<Boolean>get(Schemas.LOGICALLY_DELETED_STATUS)).isTrue();

		assertThat(recordsCaches.getRecord(idOf(2)).isActive()).isFalse();
		assertThat(recordsCaches.getRecordSummary(idOf(2)).isActive()).isFalse();
		assertThat(otherInstanceRecordsCaches.getRecordSummary(idOf(2)).isActive()).isFalse();

		//Reinserting it in remote volatile cache
		otherInstanceRecordServices.getDocumentById(idOf(2));
		assertThatStream(((RecordsCaches2Impl) otherInstanceRecordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2));

		//Physically deleting the record, removed everywhere
		recordServices.physicallyDelete(volatileRecord2, User.GOD);

		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(3));

		assertThatStream(((RecordsCaches2Impl) otherInstanceRecordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1));


		assertThatStream(recordsCaches.stream(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(3));

		assertThatStream(otherInstanceRecordsCaches.stream(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(3));

	}

	@Test
	public void whenUpdatingRecordWithPermanentAndVolatileCacheThenInvalidatedFromRemoteVolatileUpdatedInRemotePermanent()
			throws Exception {


		recordServices.add(volatileRecord1 = newRecordOf(idOf(1), zeCollectionSchemaWithVolatileCache).set(TITLE_CODE, "t1-1"));
		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1));

		recordServices.add(volatileRecord2 = newRecordOf(idOf(2), zeCollectionSchemaWithVolatileCache).set(TITLE_CODE, "t2-1"));
		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2));

		recordServices.add(volatileRecord3 = newRecordOf(idOf(3), zeCollectionSchemaWithVolatileCache).set(TITLE_CODE, "t3-1"));
		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2), idOf(3));

		assertThatStream(((RecordsCaches2Impl) otherInstanceRecordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).isEmpty();

		otherInstanceRecordServices.getDocumentById(idOf(1));
		otherInstanceRecordServices.getDocumentById(idOf(2));

		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2), idOf(3));
		assertThatStream(((RecordsCaches2Impl) otherInstanceRecordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2));
		assertThatStream(recordsCaches.stream(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2), idOf(3));
		assertThatStream(otherInstanceRecordsCaches.stream(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2), idOf(3));

		for (RecordsCaches aCache : asList(recordsCaches, otherInstanceRecordsCaches)) {
			assertThat(getIdByTitle.apply(aCache, "t1-1")).containsOnly(volatileRecord1.getId());
			assertThat(getIdByTitle.apply(aCache, "t2-1")).containsOnly(volatileRecord2.getId());
			assertThat(getIdByTitle.apply(aCache, "t3-1")).containsOnly(volatileRecord3.getId());

			assertThat(getIdSummaryByTitle.apply(aCache, "t1-1")).containsOnly(volatileRecord1.getId());
			assertThat(getIdSummaryByTitle.apply(aCache, "t2-1")).containsOnly(volatileRecord2.getId());
			assertThat(getIdSummaryByTitle.apply(aCache, "t3-1")).containsOnly(volatileRecord3.getId());
		}


		//Modifying the record, it is updated on the instance volatile cache, removed remotely
		recordServices.update(volatileRecord2.set(TITLE_CODE, "t2-2"));


		for (RecordsCaches aCache : asList(recordsCaches, otherInstanceRecordsCaches)) {
			assertThat(getIdByTitle.apply(aCache, "t1-1")).containsOnly(volatileRecord1.getId());
			assertThat(getIdByTitle.apply(aCache, "t2-1")).isEmpty();
			assertThat(getIdByTitle.apply(aCache, "t2-2")).containsOnly(volatileRecord2.getId());
			assertThat(getIdByTitle.apply(aCache, "t3-1")).containsOnly(volatileRecord3.getId());

			assertThat(getIdSummaryByTitle.apply(aCache, "t1-1")).containsOnly(volatileRecord1.getId());
			assertThat(getIdSummaryByTitle.apply(aCache, "t2-1")).isEmpty();
			assertThat(getIdSummaryByTitle.apply(aCache, "t2-2")).containsOnly(volatileRecord2.getId());
			assertThat(getIdSummaryByTitle.apply(aCache, "t3-1")).containsOnly(volatileRecord3.getId());
		}

		long newVersion = volatileRecord2.getVersion();

		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2), idOf(3));

		assertThatStream(((RecordsCaches2Impl) otherInstanceRecordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1));


		assertThatStream(recordsCaches.stream(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2), idOf(3));

		assertThatStream(otherInstanceRecordsCaches.stream(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2), idOf(3));

		assertThat(recordsCaches.getRecord(idOf(2)).getVersion()).isEqualTo(newVersion);
		assertThat(recordsCaches.getRecordSummary(idOf(2)).getVersion()).isEqualTo(newVersion);
		assertThat(otherInstanceRecordsCaches.getRecordSummary(idOf(2)).getVersion()).isEqualTo(newVersion);

		assertThat(recordsCaches.getRecord(idOf(2)).getTitle()).isEqualTo("t2-2");
		assertThat(recordsCaches.getRecordSummary(idOf(2)).getTitle()).isEqualTo("t2-2");
		assertThat(otherInstanceRecordsCaches.getRecordSummary(idOf(2)).getTitle()).isEqualTo("t2-2");

		//Reinserting it in remote volatile cache
		otherInstanceRecordServices.getDocumentById(idOf(2));
		assertThatStream(((RecordsCaches2Impl) otherInstanceRecordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2));


	}


	@Test
	public void whenLogicallyThenPhysicallyDeletingRecordWithPermanentAndVolatileCacheThenInvalidatedFromRemoteVolatileUpdatedInRemotePermanent()
			throws Exception {


		recordServices.add(volatileRecord1 = newRecordOf(idOf(1), zeCollectionSchemaWithVolatileCache).set(TITLE_CODE, "t1-1"));
		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1));

		recordServices.add(volatileRecord2 = newRecordOf(idOf(2), zeCollectionSchemaWithVolatileCache).set(TITLE_CODE, "t2-1"));
		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2));

		recordServices.add(volatileRecord3 = newRecordOf(idOf(3), zeCollectionSchemaWithVolatileCache).set(TITLE_CODE, "t3-1"));
		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2), idOf(3));

		assertThatStream(((RecordsCaches2Impl) otherInstanceRecordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).isEmpty();

		otherInstanceRecordServices.getDocumentById(idOf(1));
		otherInstanceRecordServices.getDocumentById(idOf(2));

		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2), idOf(3));
		assertThatStream(((RecordsCaches2Impl) otherInstanceRecordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2));
		assertThatStream(recordsCaches.stream(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2), idOf(3));
		assertThatStream(otherInstanceRecordsCaches.stream(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2), idOf(3));

		for (RecordsCaches aCache : asList(recordsCaches, otherInstanceRecordsCaches)) {
			assertThat(getIdByTitle.apply(aCache, "t1-1")).containsOnly(volatileRecord1.getId());
			assertThat(getIdByTitle.apply(aCache, "t2-1")).containsOnly(volatileRecord2.getId());
			assertThat(getIdByTitle.apply(aCache, "t3-1")).containsOnly(volatileRecord3.getId());

			assertThat(getIdSummaryByTitle.apply(aCache, "t1-1")).containsOnly(volatileRecord1.getId());
			assertThat(getIdSummaryByTitle.apply(aCache, "t2-1")).containsOnly(volatileRecord2.getId());
			assertThat(getIdSummaryByTitle.apply(aCache, "t3-1")).containsOnly(volatileRecord3.getId());
		}


		//Modifying the record, it is updated on the instance volatile cache, removed remotely
		recordServices.logicallyDelete(volatileRecord2, User.GOD);
		recordServices.physicallyDelete(volatileRecord2, User.GOD);


		for (RecordsCaches aCache : asList(recordsCaches, otherInstanceRecordsCaches)) {
			assertThat(getIdByTitle.apply(aCache, "t1-1")).containsOnly(volatileRecord1.getId());
			assertThat(getIdByTitle.apply(aCache, "t2-1")).isEmpty();
			assertThat(getIdByTitle.apply(aCache, "t3-1")).containsOnly(volatileRecord3.getId());

			assertThat(getIdSummaryByTitle.apply(aCache, "t1-1")).containsOnly(volatileRecord1.getId());
			assertThat(getIdSummaryByTitle.apply(aCache, "t2-1")).isEmpty();
			assertThat(getIdSummaryByTitle.apply(aCache, "t3-1")).containsOnly(volatileRecord3.getId());
		}

		long newVersion = volatileRecord2.getVersion();

		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(3));

		assertThatStream(((RecordsCaches2Impl) otherInstanceRecordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1));


		assertThatStream(recordsCaches.stream(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(3));

		assertThatStream(otherInstanceRecordsCaches.stream(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(3));

		assertThat(recordsCaches.getRecord(idOf(2))).isNull();
		assertThat(recordsCaches.getRecordSummary(idOf(2))).isNull();
		assertThat(otherInstanceRecordsCaches.getRecordSummary(idOf(2))).isNull();

	}

	@Test
	public void whenDeletingTypeRecordsThenRemovedFromAllVolatileAndPermanentCaches() throws Exception {


		recordServices.add(volatileRecord1 = newRecordOf(idOf(1), zeCollectionSchemaWithVolatileCache).set(TITLE_CODE, "t1-1"));
		recordServices.add(volatileRecord2 = newRecordOf(idOf(2), zeCollectionSchemaWithVolatileCache).set(TITLE_CODE, "t2-1"));
		recordServices.add(volatileRecord3 = newRecordOf(idOf(3), zeCollectionSchemaWithVolatileCache).set(TITLE_CODE, "t3-1"));
		recordServices.add(permanentRecord1 = newRecordOf(idOf(101), zeCollectionSchemaWithPermanentCache));
		recordServices.add(permanentRecord2 = newRecordOf(idOf(102), zeCollectionSchemaWithPermanentCache));

		otherInstanceRecordServices.getDocumentById(idOf(1));
		otherInstanceRecordServices.getDocumentById(idOf(2));

		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2), idOf(3));
		assertThatStream(((RecordsCaches2Impl) otherInstanceRecordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2));
		assertThatStream(recordsCaches.stream(zeCollectionSchemaWithVolatileCache.collection())
				.map(Record::getId)).contains(idOf(1), idOf(2), idOf(3), idOf(101), idOf(102));
		assertThatStream(otherInstanceRecordsCaches.stream(zeCollectionSchemaWithVolatileCache.collection())
				.map(Record::getId)).contains(idOf(1), idOf(2), idOf(3), idOf(101), idOf(102));

		for (RecordsCaches aCache : asList(recordsCaches, otherInstanceRecordsCaches)) {
			assertThat(getIdByTitle.apply(aCache, "t1-1")).containsOnly(volatileRecord1.getId());
			assertThat(getIdByTitle.apply(aCache, "t2-1")).containsOnly(volatileRecord2.getId());
			assertThat(getIdByTitle.apply(aCache, "t3-1")).containsOnly(volatileRecord3.getId());

			assertThat(getIdSummaryByTitle.apply(aCache, "t1-1")).containsOnly(volatileRecord1.getId());
			assertThat(getIdSummaryByTitle.apply(aCache, "t2-1")).containsOnly(volatileRecord2.getId());
			assertThat(getIdSummaryByTitle.apply(aCache, "t3-1")).containsOnly(volatileRecord3.getId());
		}


		//Deleting the record, it is updated on the instance volatile cache, removed remotely

		new RecordDeleteServices(getModelLayerFactory())
				.totallyDeleteSchemaTypeRecordsSkippingValidation_WARNING_CANNOT_BE_REVERTED(zeCollectionSchemaWithVolatileCache.type());

		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).isEmpty();
		assertThatStream(((RecordsCaches2Impl) otherInstanceRecordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).isEmpty();


		assertThatStream(recordsCaches.stream(zeCollectionSchemaWithVolatileCache.collection())
				.map(Record::getId)).contains(idOf(101), idOf(102)).doesNotContain(idOf(1), idOf(2), idOf(3));
		assertThatStream(otherInstanceRecordsCaches.stream(zeCollectionSchemaWithVolatileCache.collection())
				.map(Record::getId)).contains(idOf(101), idOf(102)).doesNotContain(idOf(1), idOf(2), idOf(3));

		for (RecordsCaches aCache : asList(recordsCaches, otherInstanceRecordsCaches)) {
			assertThat(getIdByTitle.apply(aCache, "t1-1")).isEmpty();
			assertThat(getIdByTitle.apply(aCache, "t2-1")).isEmpty();
			assertThat(getIdByTitle.apply(aCache, "t3-1")).isEmpty();

			assertThat(getIdSummaryByTitle.apply(aCache, "t1-1")).isEmpty();
			assertThat(getIdSummaryByTitle.apply(aCache, "t2-1")).isEmpty();
			assertThat(getIdSummaryByTitle.apply(aCache, "t3-1")).isEmpty();
		}

	}

	@Test
	public void whenDeletingCollectionRecordsThenRemovedFromAllVolatileAndPermanentCaches() throws Exception {


		recordServices.add(volatileRecord1 = newRecordOf(idOf(1), zeCollectionSchemaWithVolatileCache));
		recordServices.add(volatileRecord2 = newRecordOf(idOf(2), zeCollectionSchemaWithVolatileCache));
		recordServices.add(volatileRecord3 = newRecordOf(idOf(3), zeCollectionSchemaWithVolatileCache));
		recordServices.add(permanentRecord1 = newRecordOf(idOf(101), zeCollectionSchemaWithPermanentCache));
		recordServices.add(permanentRecord2 = newRecordOf(idOf(102), zeCollectionSchemaWithPermanentCache));
		recordServices.add(permanentRecord1 = newRecordOf(idOf(201), anotherCollectionSchemaWithVolatileCache));
		recordServices.add(permanentRecord2 = newRecordOf(idOf(202), anotherCollectionSchemaWithVolatileCache));

		otherInstanceRecordServices.getDocumentById(idOf(1));
		otherInstanceRecordServices.getDocumentById(idOf(2));
		otherInstanceRecordServices.getDocumentById(idOf(201));
		otherInstanceRecordServices.getDocumentById(idOf(202));

		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2), idOf(3));
		assertThatStream(((RecordsCaches2Impl) otherInstanceRecordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2));

		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(anotherCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(201), idOf(202));
		assertThatStream(((RecordsCaches2Impl) otherInstanceRecordsCaches).streamVolatile(anotherCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(201), idOf(202));

		assertThatStream(recordsCaches.stream(zeCollectionSchemaWithVolatileCache.collection())
				.map(Record::getId)).contains(idOf(1), idOf(2), idOf(3), idOf(101), idOf(102));
		assertThatStream(otherInstanceRecordsCaches.stream(zeCollectionSchemaWithVolatileCache.collection())
				.map(Record::getId)).contains(idOf(1), idOf(2), idOf(3), idOf(101), idOf(102));

		assertThatStream(recordsCaches.stream(anotherCollectionSchemaWithVolatileCache.collection())
				.map(Record::getId)).contains(idOf(201), idOf(202));
		assertThatStream(otherInstanceRecordsCaches.stream(anotherCollectionSchemaWithVolatileCache.collection())
				.map(Record::getId)).contains(idOf(201), idOf(202));

		//Deleting the record, it is updated on the instance volatile cache, removed remotely

		getAppLayerFactory().getCollectionsManager().deleteCollection(zeCollection);

		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).isEmpty();
		assertThatStream(((RecordsCaches2Impl) otherInstanceRecordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).isEmpty();

		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(anotherCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).isEmpty();
		assertThatStream(((RecordsCaches2Impl) otherInstanceRecordsCaches).streamVolatile(anotherCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).isEmpty();

		assertThatStream(recordsCaches.stream().map(Record::getId)).contains(idOf(201), idOf(202)).doesNotContain(idOf(1), idOf(2), idOf(3), idOf(101), idOf(102));
		assertThatStream(otherInstanceRecordsCaches.stream().map(Record::getId)).contains(idOf(201), idOf(202)).doesNotContain(idOf(1), idOf(2), idOf(3), idOf(101), idOf(102));

		assertThatStream(recordsCaches.stream(anotherCollectionSchemaWithVolatileCache.collection())
				.map(Record::getId)).contains(idOf(201), idOf(202));
		assertThatStream(otherInstanceRecordsCaches.stream(anotherCollectionSchemaWithVolatileCache.collection())
				.map(Record::getId)).contains(idOf(201), idOf(202));

	}

	@Test
	public void givenHookRegisteredOnPermanentAndVolatileCacheThenInsertBasedOnHook() throws Exception {


		final String ID_INSERT_WITH_HOOK_REPLACING_DEFAULT_INSERT = idOf(1_000_001);
		String ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT = idOf(1_000_002);
		String ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT_WITHOUT_VOLATILE = idOf(1_000_003);
		String ID_DEFAULT_INSERT = idOf(1_000_004);

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(zeCollectionSchemaWithVolatileCache.typeCode()).setRecordCacheType(SUMMARY_CACHED_WITH_VOLATILE);
			}
		});
		getModelLayerFactory().getRecordsCaches().register(new TestHook() {

			@Override
			public DeterminedHookCacheInsertion determineCacheInsertion(Record record,
																		MetadataSchemaTypes schemaTypes) {

				if (record.getId().equals(ID_INSERT_WITH_HOOK_REPLACING_DEFAULT_INSERT)) {
					return INSERT_WITH_HOOK_REPLACING_DEFAULT_INSERT;

				} else if (record.getId().equals(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT)) {
					return INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT;

				} else if (record.getId().equals(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT_WITHOUT_VOLATILE)) {
					return INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT_WITHOUT_VOLATILE;

				} else if (record.getId().equals(ID_DEFAULT_INSERT)) {
					return DEFAULT_INSERT;
				} else {
					throw new IllegalArgumentException("Bad record id");
				}

			}
		});

		getModelLayerFactory("other").getRecordsCaches().register(new TestHook() {

			@Override
			public DeterminedHookCacheInsertion determineCacheInsertion(Record record,
																		MetadataSchemaTypes schemaTypes) {

				if (record.getId().equals(ID_INSERT_WITH_HOOK_REPLACING_DEFAULT_INSERT)) {
					return INSERT_WITH_HOOK_REPLACING_DEFAULT_INSERT;

				} else if (record.getId().equals(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT)) {
					return INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT;

				} else if (record.getId().equals(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT_WITHOUT_VOLATILE)) {
					return INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT_WITHOUT_VOLATILE;

				} else if (record.getId().equals(ID_DEFAULT_INSERT)) {
					return DEFAULT_INSERT;
				} else {
					throw new IllegalArgumentException("Bad record id");
				}

			}
		});

		Record r1, r2, r3, r4;
		recordServices.add(r1 = newRecordOf(ID_INSERT_WITH_HOOK_REPLACING_DEFAULT_INSERT, zeCollectionSchemaWithVolatileCache).set(TITLE, "val1"));
		recordServices.add(r2 = newRecordOf(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT, zeCollectionSchemaWithVolatileCache).set(TITLE, "val2"));
		recordServices.add(r3 = newRecordOf(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT_WITHOUT_VOLATILE, zeCollectionSchemaWithVolatileCache).set(TITLE, "val3"));
		recordServices.add(r4 = newRecordOf(ID_DEFAULT_INSERT, zeCollectionSchemaWithVolatileCache).set(TITLE, "val4"));

		recordsCaches.invalidateVolatile();
		otherInstanceRecordsCaches.invalidateVolatile();

		for (RecordsCaches aCache : asList(recordsCaches, otherInstanceRecordsCaches)) {
			assertThatVersion(aCache.getRecord(ID_INSERT_WITH_HOOK_REPLACING_DEFAULT_INSERT)).isEqualTo(r1.getVersion());
			assertThat(aCache.getRecord(ID_INSERT_WITH_HOOK_REPLACING_DEFAULT_INSERT).getLoadedFieldsMode()).isEqualTo(RecordDTOMode.FULLY_LOADED);
			assertThatVersion(aCache.getRecord(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT)).isEqualTo(r2.getVersion());
			assertThat(aCache.getRecord(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT).getLoadedFieldsMode()).isEqualTo(RecordDTOMode.FULLY_LOADED);
			assertThatVersion(aCache.getRecord(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT_WITHOUT_VOLATILE)).isEqualTo(r3.getVersion());
			assertThat(aCache.getRecord(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT_WITHOUT_VOLATILE).getLoadedFieldsMode()).isEqualTo(RecordDTOMode.FULLY_LOADED);
			assertThatVersion(aCache.getRecord(ID_DEFAULT_INSERT)).isNull(); //Was obtained from volatile

			assertThatVersion(aCache.getRecordSummary(ID_INSERT_WITH_HOOK_REPLACING_DEFAULT_INSERT)).isEqualTo(r1.getVersion());
			assertThat(aCache.getRecordSummary(ID_INSERT_WITH_HOOK_REPLACING_DEFAULT_INSERT).getLoadedFieldsMode()).isEqualTo(RecordDTOMode.SUMMARY);
			assertThatVersion(aCache.getRecordSummary(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT)).isEqualTo(r2.getVersion());
			assertThat(aCache.getRecordSummary(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT).getLoadedFieldsMode()).isEqualTo(RecordDTOMode.SUMMARY);
			assertThatVersion(aCache.getRecordSummary(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT_WITHOUT_VOLATILE)).isEqualTo(r3.getVersion());
			assertThat(aCache.getRecordSummary(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT_WITHOUT_VOLATILE).getLoadedFieldsMode()).isEqualTo(RecordDTOMode.SUMMARY);
			assertThatVersion(aCache.getRecordSummary(ID_DEFAULT_INSERT)).isEqualTo(r4.getVersion());

			assertThat(aCache.getCache(zeCollection).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
					.map(Record::getId).collect(toList())).isEmpty();

			assertThat(aCache.stream(zeCollection).map(Record::getId).collect(toList()))
					.contains(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT, ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT_WITHOUT_VOLATILE, ID_DEFAULT_INSERT)
					.doesNotContain(ID_INSERT_WITH_HOOK_REPLACING_DEFAULT_INSERT);
		}
		// Update records

		recordServices.add(r1.set(TITLE, "val1a"));
		recordServices.add(r2.set(TITLE, "val2b"));
		recordServices.add(r3.set(TITLE, "val3c"));
		recordServices.add(r4.set(TITLE, "val4d"));


		assertThatVersion(recordsCaches.getRecord(ID_INSERT_WITH_HOOK_REPLACING_DEFAULT_INSERT)).isEqualTo(r1.getVersion());
		assertThatVersion(recordsCaches.getRecord(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT)).isEqualTo(r2.getVersion());
		assertThatVersion(recordsCaches.getRecord(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT_WITHOUT_VOLATILE)).isEqualTo(r3.getVersion());
		assertThatVersion(recordsCaches.getRecord(ID_DEFAULT_INSERT)).isEqualTo(r4.getVersion());

		assertThat(recordsCaches.getCache(zeCollection).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId).collect(toList())).containsOnly(
				ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT,
				ID_DEFAULT_INSERT);

		assertThatVersion(otherInstanceRecordsCaches.getRecord(ID_INSERT_WITH_HOOK_REPLACING_DEFAULT_INSERT)).isEqualTo(r1.getVersion());
		assertThatVersion(otherInstanceRecordsCaches.getRecord(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT)).isEqualTo(r2.getVersion());
		assertThatVersion(otherInstanceRecordsCaches.getRecord(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT_WITHOUT_VOLATILE)).isEqualTo(r3.getVersion());
		assertThatVersion(otherInstanceRecordsCaches.getRecord(ID_DEFAULT_INSERT)).isNull();

		assertThat(otherInstanceRecordsCaches.getCache(zeCollection).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId).collect(toList())).containsOnly(
				ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT).doesNotContain(ID_DEFAULT_INSERT);


		recordsCaches.invalidateVolatile();
		otherInstanceRecordsCaches.invalidateVolatile();

		for (RecordsCaches aCache : asList(recordsCaches, otherInstanceRecordsCaches)) {
			assertThatVersion(aCache.getRecord(ID_INSERT_WITH_HOOK_REPLACING_DEFAULT_INSERT)).isEqualTo(r1.getVersion());
			assertThatVersion(aCache.getRecord(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT)).isEqualTo(r2.getVersion());
			assertThatVersion(aCache.getRecord(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT_WITHOUT_VOLATILE)).isEqualTo(r3.getVersion());
			assertThatVersion(aCache.getRecord(ID_DEFAULT_INSERT)).isNull(); //Was obtained from volatile

			assertThatVersion(aCache.getRecordSummary(ID_INSERT_WITH_HOOK_REPLACING_DEFAULT_INSERT)).isEqualTo(r1.getVersion());
			assertThatVersion(aCache.getRecordSummary(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT)).isEqualTo(r2.getVersion());
			assertThatVersion(aCache.getRecordSummary(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT_WITHOUT_VOLATILE)).isEqualTo(r3.getVersion());
			assertThatVersion(aCache.getRecordSummary(ID_DEFAULT_INSERT)).isEqualTo(r4.getVersion());
		}
		// Delete records


		recordServices.logicallyDelete(r1, User.GOD);
		recordServices.logicallyDelete(r2, User.GOD);
		recordServices.logicallyDelete(r3, User.GOD);
		recordServices.logicallyDelete(r4, User.GOD);

		recordServices.physicallyDelete(r1, User.GOD);
		recordServices.physicallyDelete(r2, User.GOD);
		recordServices.physicallyDelete(r3, User.GOD);
		recordServices.physicallyDelete(r4, User.GOD);

		for (RecordsCaches aCache : asList(recordsCaches, otherInstanceRecordsCaches)) {
			//Since it is not handled by the cache, the record does not benefit from automatic invalidation on delete
			assertThatVersion(aCache.getRecord(ID_INSERT_WITH_HOOK_REPLACING_DEFAULT_INSERT)).isNotNull();
			assertThatVersion(aCache.getRecord(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT)).isNull();
			assertThatVersion(aCache.getRecord(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT_WITHOUT_VOLATILE)).isNull();
			assertThatVersion(aCache.getRecord(ID_DEFAULT_INSERT)).isNull();

			assertThatVersion(aCache.getRecordSummary(ID_INSERT_WITH_HOOK_REPLACING_DEFAULT_INSERT)).isNotNull();
			assertThatVersion(aCache.getRecordSummary(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT)).isNull();
			assertThatVersion(aCache.getRecordSummary(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT_WITHOUT_VOLATILE)).isNull();
			assertThatVersion(aCache.getRecordSummary(ID_DEFAULT_INSERT)).isNull();

			assertThat(aCache.getCache(zeCollection).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
					.map(Record::getId).collect(toList())).isEmpty();

			assertThat(aCache.stream(zeCollection).map(Record::getId).collect(toList()))
					.doesNotContain(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT,
							ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT_WITHOUT_VOLATILE,
							ID_DEFAULT_INSERT, ID_INSERT_WITH_HOOK_REPLACING_DEFAULT_INSERT);
		}

	}

	private String idOf(int i) {

		return useZeroPaddedIds ? StringUtils.leftPad("" + (100000 + i), 11, '0') : ("" + i);
	}

	//@Test
	public void whenVolatileCacheIsFullThenExpiredIndependentlyOnEachInstance() throws Exception {


		File dictionaryFolder = getFoldersLocator().getDict();
		RandomWordsIterator iterators = createFor(new File(dictionaryFolder, "fr_FR_avec_accents.dic"));

		recordServices.add(volatileRecord1 = newRecordOf(idOf(1), zeCollectionSchemaWithVolatileCache)
				.set(zeCollectionSchemaWithVolatileCache.largeTextMetadata(), iterators.nextWordsOfLength(megaoctets(2))));
		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1));

		recordServices.add(volatileRecord1 = newRecordOf(idOf(2), zeCollectionSchemaWithVolatileCache)
				.set(zeCollectionSchemaWithVolatileCache.largeTextMetadata(), iterators.nextWordsOfLength(megaoctets(2))));
		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2));

		recordServices.add(volatileRecord1 = newRecordOf(idOf(3), zeCollectionSchemaWithVolatileCache)
				.set(zeCollectionSchemaWithVolatileCache.largeTextMetadata(), iterators.nextWordsOfLength(megaoctets(2))));
		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2), idOf(3));

		recordServices.add(volatileRecord1 = newRecordOf(idOf(4), zeCollectionSchemaWithVolatileCache)
				.set(zeCollectionSchemaWithVolatileCache.largeTextMetadata(), iterators.nextWordsOfLength(megaoctets(2))));
		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2), idOf(3), idOf(4));

		recordServices.add(volatileRecord1 = newRecordOf("v5", zeCollectionSchemaWithVolatileCache)
				.set(zeCollectionSchemaWithVolatileCache.largeTextMetadata(), iterators.nextWordsOfLength(megaoctets(2))));
		recordServices.getDocumentById(idOf(1));
		Thread.sleep(10000);
		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(3), idOf(4), "v5");

		assertThatStream(((RecordsCaches2Impl) otherInstanceRecordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).isEmpty();

		otherInstanceRecordServices.getDocumentById(idOf(1));
		otherInstanceRecordServices.getDocumentById(idOf(4));

		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(2), idOf(3), idOf(4), "v5");

		assertThatStream(((RecordsCaches2Impl) otherInstanceRecordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(4));

	}

	//-----------------------------------------------------------------

	private void resetCacheAndQueries() {
		recordsCaches.invalidateVolatile();
		otherInstanceRecordsCaches.invalidateVolatile();
		queriesListener.clear();
	}

	private void loadAllRecordsInCaches() {
		recordServices.getDocumentById(idOf(101));
		recordServices.getDocumentById(idOf(102));
		recordServices.getDocumentById(idOf(1));
		recordServices.getDocumentById(idOf(2));
		recordServices.getDocumentById(idOf(3));
		recordServices.getDocumentById(idOf(4));

		otherInstanceRecordServices.getDocumentById(idOf(101));
		otherInstanceRecordServices.getDocumentById(idOf(102));
		otherInstanceRecordServices.getDocumentById(idOf(1));
		otherInstanceRecordServices.getDocumentById(idOf(2));
		otherInstanceRecordServices.getDocumentById(idOf(3));
		otherInstanceRecordServices.getDocumentById(idOf(4));

		recordServices.getDocumentById(idOf(801));
		otherInstanceRecordServices.getDocumentById(idOf(802));
	}

	private void givenTestRecords()
			throws Exception {
		Transaction tx = new Transaction();

		tx.add(permanentRecord1 = newRecordOf(idOf(101), zeCollectionSchemaWithPermanentCache).withTitle("x")
				.set(zeCollectionSchemaWithPermanentCache.stringMetadata(), "p1Code"));

		tx.add(permanentRecord2 = newRecordOf(idOf(102), zeCollectionSchemaWithPermanentCache).withTitle("b")
				.set(zeCollectionSchemaWithPermanentCache.stringMetadata(), "p2Code"));

		tx.add(volatileRecord1 = newRecordOf(idOf(1), zeCollectionSchemaWithVolatileCache).withTitle("c")
				.set(zeCollectionSchemaWithVolatileCache.stringMetadata(), "code18"));

		tx.add(volatileRecord2 = newRecordOf(idOf(2), zeCollectionSchemaWithVolatileCache).withTitle("c")
				.set(zeCollectionSchemaWithVolatileCache.stringMetadata(), "code42"));

		tx.add(volatileRecord3 = newRecordOf(idOf(3), zeCollectionSchemaWithVolatileCache).withTitle("c")
				.set(zeCollectionSchemaWithVolatileCache.stringMetadata(), "code3"));

		tx.add(uncachedRecord1 = newRecordOf(idOf(801), zeCollectionSchemaWithPermanentCache).withTitle("a"));
		recordServices.execute(tx);
		tx = new Transaction();
		tx.add(volatileRecord4 = newRecordOf(idOf(4), anotherCollectionSchemaWithVolatileCache).withTitle("d"));
		tx.add(uncachedRecord2 = newRecordOf(idOf(802), anotherCollectionSchemaWithoutCache).withTitle("e"));

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

	private LongAssert assertThatVersion(Record record) {
		return assertThat(record == null ? null : record.getVersion());
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
