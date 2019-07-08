package com.constellio.model.services.records.cache;

import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.data.test.RandomWordsIterator;
import com.constellio.model.conf.PropertiesModelLayerConfiguration.InMemoryModelLayerConfiguration;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.RecordCacheType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordDeleteServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesImpl;
import com.constellio.model.services.records.cache2.RecordsCaches2Impl;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.ModelLayerConfigurationAlteration;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ThirdSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import com.constellio.sdk.tests.setups.SchemaShortcuts;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.constellio.data.test.RandomWordsIterator.createFor;
import static com.constellio.data.utils.Octets.megaoctets;
import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromEveryTypesOfEveryCollection;
import static com.constellio.sdk.tests.TestUtils.assertThatStream;
import static com.constellio.sdk.tests.TestUtils.linkEventBus;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsUnique;
import static java.util.Arrays.asList;
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

		recordServices.execute(tx);

		assertThat(recordsCaches.getRecord(idOf(101)).isSummary()).isFalse();
		assertThat(recordsCaches.getRecord(idOf(102))).isNull();
		assertThat(recordsCaches.getRecordSummary(idOf(102)).isSummary()).isTrue();
		assertThat(recordsCaches.getRecord(idOf(1)).isSummary()).isFalse();
		assertThat(recordsCaches.getRecordSummary(idOf(1)).isSummary()).isTrue();


		assertThat(otherInstanceRecordsCaches.getRecord(idOf(101)).isSummary()).isFalse();
		assertThat(otherInstanceRecordsCaches.getRecord(idOf(102))).isNull();
		assertThat(otherInstanceRecordsCaches.getRecordSummary(idOf(102)).isSummary()).isTrue();
		assertThat(otherInstanceRecordsCaches.getRecord(idOf(1))).isNull();
		assertThat(otherInstanceRecordsCaches.getRecordSummary(idOf(1)).isSummary()).isTrue();

		otherInstanceRecordServices.getDocumentById(idOf(1));
		assertThat(otherInstanceRecordsCaches.getRecord(idOf(1))).isNotNull();

		//Update with new values
		tx = new Transaction();
		tx.add(permanentRecord1.withTitle("e")
				.set(zeCollectionSchemaWithPermanentCache.stringMetadata(), "p1Code"));

		tx.add(permanentRecord2.withTitle("f"));

		tx.add(volatileRecord1.withTitle("g")
				.set(zeCollectionSchemaWithVolatileCache.stringMetadata(), "code18"));

		recordServices.execute(tx);

		assertThat(recordsCaches.getRecord(idOf(101)).isSummary()).isFalse();
		assertThat(recordsCaches.getRecord(idOf(101)).getTitle()).isEqualTo("e");
		assertThat(recordsCaches.getRecord(idOf(102))).isNull();
		assertThat(recordsCaches.getRecordSummary(idOf(102)).isSummary()).isTrue();
		assertThat(recordsCaches.getRecordSummary(idOf(102)).getTitle()).isEqualTo("f");
		assertThat(recordsCaches.getRecord(idOf(1)).isSummary()).isFalse();
		assertThat(recordsCaches.getRecord(idOf(1)).getTitle()).isEqualTo("g");
		assertThat(recordsCaches.getRecordSummary(idOf(1)).isSummary()).isTrue();
		assertThat(recordsCaches.getRecordSummary(idOf(1)).getTitle()).isEqualTo("g");


		assertThat(otherInstanceRecordsCaches.getRecord(idOf(101)).isSummary()).isFalse();
		assertThat(otherInstanceRecordsCaches.getRecord(idOf(101)).getTitle()).isEqualTo("e");
		assertThat(otherInstanceRecordsCaches.getRecord(idOf(102))).isNull();
		assertThat(otherInstanceRecordsCaches.getRecordSummary(idOf(102)).isSummary()).isTrue();
		assertThat(otherInstanceRecordsCaches.getRecordSummary(idOf(102)).getTitle()).isEqualTo("f");
		assertThat(otherInstanceRecordsCaches.getRecord(idOf(1))).isNull();
		assertThat(otherInstanceRecordsCaches.getRecordSummary(idOf(1)).isSummary()).isTrue();
		assertThat(otherInstanceRecordsCaches.getRecordSummary(idOf(1)).getTitle()).isEqualTo("g");

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


		recordServices.add(volatileRecord1 = newRecordOf(idOf(1), zeCollectionSchemaWithVolatileCache));
		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1));

		recordServices.add(volatileRecord2 = newRecordOf(idOf(2), zeCollectionSchemaWithVolatileCache));
		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2));

		recordServices.add(volatileRecord3 = newRecordOf(idOf(3), zeCollectionSchemaWithVolatileCache));
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


		recordServices.add(volatileRecord1 = newRecordOf(idOf(1), zeCollectionSchemaWithVolatileCache));
		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1));

		recordServices.add(volatileRecord2 = newRecordOf(idOf(2), zeCollectionSchemaWithVolatileCache));
		assertThatStream(((RecordsCaches2Impl) recordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2));

		recordServices.add(volatileRecord3 = newRecordOf(idOf(3), zeCollectionSchemaWithVolatileCache));
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
		recordServices.update(volatileRecord2.set(Schemas.TITLE_CODE, "newValue"));

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

		assertThat(recordsCaches.getRecord(idOf(2)).getTitle()).isEqualTo("newValue");
		assertThat(recordsCaches.getRecordSummary(idOf(2)).getTitle()).isEqualTo("newValue");
		assertThat(otherInstanceRecordsCaches.getRecordSummary(idOf(2)).getTitle()).isEqualTo("newValue");

		//Reinserting it in remote volatile cache
		otherInstanceRecordServices.getDocumentById(idOf(2));
		assertThatStream(((RecordsCaches2Impl) otherInstanceRecordsCaches).streamVolatile(zeCollectionSchemaWithVolatileCache.type())
				.map(Record::getId)).containsOnly(idOf(1), idOf(2));


	}

	@Test
	public void whenDeletingTypeRecordsThenRemovedFromAllVolatileAndPermanentCaches() throws Exception {


		recordServices.add(volatileRecord1 = newRecordOf(idOf(1), zeCollectionSchemaWithVolatileCache));
		recordServices.add(volatileRecord2 = newRecordOf(idOf(2), zeCollectionSchemaWithVolatileCache));
		recordServices.add(volatileRecord3 = newRecordOf(idOf(3), zeCollectionSchemaWithVolatileCache));
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
