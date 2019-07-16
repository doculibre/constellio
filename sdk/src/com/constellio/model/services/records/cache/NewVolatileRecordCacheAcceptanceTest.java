package com.constellio.model.services.records.cache;

import com.constellio.data.dao.dto.records.RecordDTOMode;
import com.constellio.data.dao.services.cache.InsertionReason;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.Provider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.cache2.DeterminedHookCacheInsertion;
import com.constellio.model.services.records.cache2.HookCacheInsertionResponse;
import com.constellio.model.services.records.cache2.HookCachePresence;
import com.constellio.model.services.records.cache2.RecordsCachesHook;
import com.constellio.model.services.records.cache2.RemoteCacheAction;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.QueryCounter;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.LongAssert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.constellio.data.dao.services.cache.InsertionReason.WAS_MODIFIED;
import static com.constellio.data.dao.services.cache.InsertionReason.WAS_OBTAINED;
import static com.constellio.model.entities.schemas.RecordCacheType.SUMMARY_CACHED_WITH_VOLATILE;
import static com.constellio.model.entities.schemas.Schemas.TITLE;
import static com.constellio.model.services.records.cache.CacheInsertionStatus.ACCEPTED;
import static com.constellio.model.services.records.cache.CacheInsertionStatus.REFUSED_OLD_VERSION;
import static com.constellio.model.services.records.cache2.DeterminedHookCacheInsertion.DEFAULT_INSERT;
import static com.constellio.model.services.records.cache2.DeterminedHookCacheInsertion.INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT;
import static com.constellio.model.services.records.cache2.DeterminedHookCacheInsertion.INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT_WITHOUT_VOLATILE;
import static com.constellio.model.services.records.cache2.DeterminedHookCacheInsertion.INSERT_WITH_HOOK_REPLACING_DEFAULT_INSERT;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.sdk.tests.QueryCounter.ON_SCHEMA_TYPES;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsEssentialInSummary;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NewVolatileRecordCacheAcceptanceTest extends ConstellioTest {

	String anotherCollection = "anotherCollection";

	Transaction transaction;
	TestRecord record1, record2, record3, record4, record5, record18, record42;

	TestsSchemasSetup zeCollectionSchemas = new TestsSchemasSetup(zeCollection).withSecurityFlag(false);
	ZeSchemaMetadatas zeCollectionSchemaType1 = zeCollectionSchemas.new ZeSchemaMetadatas();
	AnotherSchemaMetadatas zeCollectionSchemaType2 = zeCollectionSchemas.new AnotherSchemaMetadatas();

	TestsSchemasSetup anotherCollectionSchemas = new TestsSchemasSetup(anotherCollection).withSecurityFlag(false);
	ZeSchemaMetadatas anotherCollectionSchemaType1 = anotherCollectionSchemas.new ZeSchemaMetadatas();

	RecordsCaches cache;

	UserServices userServices;
	RecordServices recordServices;
	SearchServices searchServices;

	StatsBigVaultServerExtension queriesListener;

	boolean useZeroPaddedIds;

	QueryCounter queryCounter;

	public NewVolatileRecordCacheAcceptanceTest(String testCase) {
		this.useZeroPaddedIds = testCase.equals("zero-padded-ids");
	}

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> testCases() {
		return Arrays.asList(new Object[][]{{"zero-padded-ids"}, {"string-ids"}});
	}

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withAllTestUsers(),
				withCollection(anotherCollection).withAllTestUsers()
		);
		defineSchemasManager().using(zeCollectionSchemas.withAStringMetadata(whichIsEssentialInSummary).withAnotherStringMetadata());
		defineSchemasManager().using(anotherCollectionSchemas);

		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();

		userServices = getModelLayerFactory().newUserServices();
		cache = getModelLayerFactory().getRecordsCaches();

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(zeCollectionSchemaType1.typeCode()).setRecordCacheType(SUMMARY_CACHED_WITH_VOLATILE);
				types.getSchemaType(zeCollectionSchemaType2.typeCode()).setRecordCacheType(SUMMARY_CACHED_WITH_VOLATILE);
				types.getSchemaType(anotherCollectionSchemaType1.typeCode()).setRecordCacheType(SUMMARY_CACHED_WITH_VOLATILE);
			}
		});

		queryCounter = new QueryCounter(getDataLayerFactory(), ON_SCHEMA_TYPES(zeCollectionSchemaType1.typeCode()));

	}

	@Test
	public void whenInsertingSummaryLoadedRecordThenPermanentSummaryUpdatedVolatileInvalidated()
			throws Exception {

		cacheIntegrityCheckedAfterTest = false;

		Record record = newZeCollectionType1Record(1234).set(TITLE, "val1")
				.set(zeCollectionSchemaType1.stringMetadata(), "val2")
				.set(zeCollectionSchemaType1.anotherStringMetadata(), "val3");
		recordServices.add(record);

		//Record is found in volatile cache in it's full state
		Record fullRecordFromCache = cache.getRecord(id(1234));
		assertThat(fullRecordFromCache.<String>get(TITLE)).isEqualTo("val1");
		assertThat(fullRecordFromCache.<String>get(zeCollectionSchemaType1.stringMetadata())).isEqualTo("val2");
		assertThat(fullRecordFromCache.<String>get(zeCollectionSchemaType1.anotherStringMetadata())).isEqualTo("val3");
		assertThat(fullRecordFromCache.isSummary()).isFalse();

		Record summaryRecordFromCache = cache.getRecordSummary(id(1234));
		assertThat(summaryRecordFromCache.<String>get(TITLE)).isEqualTo("val1");
		assertThat(summaryRecordFromCache.<String>get(zeCollectionSchemaType1.stringMetadata())).isEqualTo("val2");
		assertThat(summaryRecordFromCache.<String>get(zeCollectionSchemaType1.anotherStringMetadata())).isNull();
		assertThat(summaryRecordFromCache.isSummary()).isTrue();

		long version = cache.getRecord(id(1234)).getVersion();

		Record partiallyLoadedRecord = cache.getRecordSummary(id(1234));
		partiallyLoadedRecord.markAsSaved(version - 1000, zeCollectionSchemaType1.instance());

		assertThat(cache.insert(partiallyLoadedRecord, WAS_OBTAINED)).isEqualTo(REFUSED_OLD_VERSION);
		assertThat(cache.insert(partiallyLoadedRecord, WAS_MODIFIED)).isEqualTo(REFUSED_OLD_VERSION);

		//Record is found in volatile cache in it's full state
		fullRecordFromCache = cache.getRecord(id(1234));
		assertThat(fullRecordFromCache.<String>get(TITLE)).isEqualTo("val1");
		assertThat(fullRecordFromCache.<String>get(zeCollectionSchemaType1.stringMetadata())).isEqualTo("val2");
		assertThat(fullRecordFromCache.<String>get(zeCollectionSchemaType1.anotherStringMetadata())).isEqualTo("val3");
		assertThat(fullRecordFromCache.isSummary()).isFalse();

		summaryRecordFromCache = cache.getRecordSummary(id(1234));
		assertThat(summaryRecordFromCache.<String>get(TITLE)).isEqualTo("val1");
		assertThat(summaryRecordFromCache.<String>get(zeCollectionSchemaType1.stringMetadata())).isEqualTo("val2");
		assertThat(summaryRecordFromCache.<String>get(zeCollectionSchemaType1.anotherStringMetadata())).isNull();
		assertThat(summaryRecordFromCache.isSummary()).isTrue();

		//Updating the record using a fully loaded record, both volatile and summary permanent are updated
		recordServices.update(fullRecordFromCache.set(zeCollectionSchemaType1.stringMetadata(), "val4"));
		fullRecordFromCache = cache.getRecord(id(1234));
		assertThat(fullRecordFromCache.<String>get(TITLE)).isEqualTo("val1");
		assertThat(fullRecordFromCache.<String>get(zeCollectionSchemaType1.stringMetadata())).isEqualTo("val4");
		assertThat(fullRecordFromCache.<String>get(zeCollectionSchemaType1.anotherStringMetadata())).isEqualTo("val3");
		assertThat(fullRecordFromCache.isSummary()).isFalse();

		summaryRecordFromCache = cache.getRecordSummary(id(1234));
		assertThat(summaryRecordFromCache.<String>get(TITLE)).isEqualTo("val1");
		assertThat(summaryRecordFromCache.<String>get(zeCollectionSchemaType1.stringMetadata())).isEqualTo("val4");
		assertThat(summaryRecordFromCache.<String>get(zeCollectionSchemaType1.anotherStringMetadata())).isNull();
		assertThat(summaryRecordFromCache.isSummary()).isTrue();

		//Currently, it is not possible to execute transaction using records that are not fully loaded

		try {
			recordServices.update(getPartiallyLoaded(1234).set(zeCollectionSchemaType1.stringMetadata(), "val5"));
			fail("Exception expected");
		} catch (ImpossibleRuntimeException e) {
			//OK
		}


		try {
			recordServices.update(summaryRecordFromCache.set(zeCollectionSchemaType1.stringMetadata(), "val5"));
			fail("Exception expected");
		} catch (ImpossibleRuntimeException e) {
			//OK
		}

		//Inserting a new state of the record using a summary record, summary permanent is updated, remove from volatile

		summaryRecordFromCache.set(zeCollectionSchemaType1.stringMetadata(), "val6");
		summaryRecordFromCache.markAsSaved(summaryRecordFromCache.getVersion() + 1000, zeCollectionSchemaType1.instance());
		assertThat(cache.insert(summaryRecordFromCache, WAS_MODIFIED)).isEqualTo(ACCEPTED);
		fullRecordFromCache = cache.getRecord(id(1234));
		assertThat(fullRecordFromCache).isNull();

		summaryRecordFromCache = cache.getRecordSummary(id(1234));
		assertThat(summaryRecordFromCache.<String>get(TITLE)).isEqualTo("val1");
		assertThat(summaryRecordFromCache.<String>get(zeCollectionSchemaType1.stringMetadata())).isEqualTo("val6");
		assertThat(summaryRecordFromCache.<String>get(zeCollectionSchemaType1.anotherStringMetadata())).isNull();
		assertThat(summaryRecordFromCache.isSummary()).isTrue();

	}

	@Test
	public void whenInsertingFullyCachedRecordsThenFullVersionObtainedEvenIfAskedForSummary()
			throws Exception {

		recordServices.add(newZeCollectionType1Record(1234).set(TITLE, "val1")
				.set(zeCollectionSchemaType1.stringMetadata(), "val2")
				.set(zeCollectionSchemaType1.anotherStringMetadata(), "val3"));

		recordServices.add(newZeCollectionType1Record(2345).set(TITLE, "valA")
				.set(zeCollectionSchemaType1.stringMetadata(), "valAA")
				.set(zeCollectionSchemaType1.anotherStringMetadata(), "valAAA"));

		queryCounter.reset();

		// Get by id

		Record recordFromCache = cache.getRecord(id(1234));
		assertThat(recordFromCache.isSummary()).isFalse();
		assertThat(recordFromCache.<String>get(TITLE)).isEqualTo("val1");
		assertThat(recordFromCache.<String>get(zeCollectionSchemaType1.stringMetadata())).isEqualTo("val2");
		assertThat(recordFromCache.<String>get(zeCollectionSchemaType1.anotherStringMetadata())).isEqualTo("val3");
		assertThat(recordFromCache.isSummary()).isFalse();

		recordFromCache = cache.getRecordSummary(id(1234));
		assertThat(recordFromCache.isSummary()).isTrue();
		assertThat(recordFromCache.<String>get(TITLE)).isEqualTo("val1");
		assertThat(recordFromCache.<String>get(zeCollectionSchemaType1.stringMetadata())).isEqualTo("val2");
		assertThat(recordFromCache.<String>get(zeCollectionSchemaType1.anotherStringMetadata())).isNull();
		assertThat(recordFromCache.isSummary()).isTrue();


		recordFromCache = cache.getCache(zeCollection).get(id(1234));
		assertThat(recordFromCache.isSummary()).isFalse();
		assertThat(recordFromCache.<String>get(TITLE)).isEqualTo("val1");
		assertThat(recordFromCache.<String>get(zeCollectionSchemaType1.stringMetadata())).isEqualTo("val2");
		assertThat(recordFromCache.<String>get(zeCollectionSchemaType1.anotherStringMetadata())).isEqualTo("val3");
		assertThat(recordFromCache.isSummary()).isFalse();

		recordFromCache = cache.getCache(zeCollection).getSummary(id(1234));
		assertThat(recordFromCache.isSummary()).isTrue();
		assertThat(recordFromCache.<String>get(TITLE)).isEqualTo("val1");
		assertThat(recordFromCache.<String>get(zeCollectionSchemaType1.stringMetadata())).isEqualTo("val2");
		assertThat(recordFromCache.<String>get(zeCollectionSchemaType1.anotherStringMetadata())).isNull();

		// Get by metadata
		try {
			recordFromCache = cache.getCache(zeCollection).getByMetadata(zeCollectionSchemaType1.stringMetadata(), "val2");
			fail("Exception expected");
		} catch (ImpossibleRuntimeException e) {
			//OK
		}
		recordFromCache = cache.getCache(zeCollection).getSummaryByMetadata(zeCollectionSchemaType1.stringMetadata(), "val2");
		assertThat(recordFromCache.isSummary()).isTrue();
		assertThat(recordFromCache.<String>get(TITLE)).isEqualTo("val1");
		assertThat(recordFromCache.<String>get(zeCollectionSchemaType1.stringMetadata())).isEqualTo("val2");
		assertThat(recordFromCache.<String>get(zeCollectionSchemaType1.anotherStringMetadata())).isNull();
		assertThat(recordFromCache.isSummary()).isTrue();

		recordFromCache = cache.getCache(zeCollection).getSummaryByMetadata(zeCollectionSchemaType1.stringMetadata(), "valAA");
		assertThat(recordFromCache.isSummary()).isTrue();
		assertThat(recordFromCache.<String>get(TITLE)).isEqualTo("valA");
		assertThat(recordFromCache.<String>get(zeCollectionSchemaType1.stringMetadata())).isEqualTo("valAA");
		assertThat(recordFromCache.<String>get(zeCollectionSchemaType1.anotherStringMetadata())).isNull();
		assertThat(recordFromCache.isSummary()).isTrue();

		assertThat(cache.getCache(zeCollection).getAllValues(zeCollectionSchemaType1.typeCode())).hasSize(2);
		assertThat(cache.getCache(zeCollection).
				getAllValuesInUnmodifiableState(zeCollectionSchemaType1.typeCode())).hasSize(2);

		assertThat(cache.stream(zeCollectionSchemaType1.type()).count()).isEqualTo(2);

		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
	}

	@Test
	public void whenInsertingRecordsThenLastVersionInCache()
			throws Exception {

		Record record = newZeCollectionType1Record(1234).set(TITLE, "val1")
				.set(zeCollectionSchemaType1.stringMetadata(), "val2")
				.set(zeCollectionSchemaType1.anotherStringMetadata(), "val3");
		recordServices.add(record);

		Record recordFromCache = cache.getRecord(id(1234));
		assertThat(recordFromCache.isSummary()).isFalse();
		assertThat(recordFromCache.<String>get(TITLE)).isEqualTo("val1");
		assertThat(recordFromCache.<String>get(zeCollectionSchemaType1.stringMetadata())).isEqualTo("val2");
		assertThat(recordFromCache.<String>get(zeCollectionSchemaType1.anotherStringMetadata())).isEqualTo("val3");
		assertThat(recordFromCache.isSummary()).isFalse();

		record.set(TITLE, "val4")
				.set(zeCollectionSchemaType1.stringMetadata(), "val5")
				.set(zeCollectionSchemaType1.anotherStringMetadata(), "val6");
		recordServices.add(record);
		assertThat(((RecordImpl) record).getRecordDTO().getLoadingMode()).isEqualTo(RecordDTOMode.FULLY_LOADED);

		recordFromCache = cache.getRecord(id(1234));
		assertThat(recordFromCache.isSummary()).isFalse();
		assertThat(recordFromCache.<String>get(TITLE)).isEqualTo("val4");
		assertThat(recordFromCache.<String>get(zeCollectionSchemaType1.stringMetadata())).isEqualTo("val5");
		assertThat(recordFromCache.<String>get(zeCollectionSchemaType1.anotherStringMetadata())).isEqualTo("val6");
		assertThat(recordFromCache.isSummary()).isFalse();

		recordServices.logicallyDelete(record, User.GOD);

		record = cache.getRecord(id(1234));
		assertThat(record.isSummary()).isFalse();
		assertThat(record.<String>get(TITLE)).isEqualTo("val4");
		assertThat(record.<String>get(zeCollectionSchemaType1.stringMetadata())).isEqualTo("val5");
		assertThat(record.<String>get(zeCollectionSchemaType1.anotherStringMetadata())).isEqualTo("val6");
		assertThat(record.isSummary()).isFalse();

		recordServices.physicallyDelete(record, User.GOD);

		recordFromCache = cache.getRecord(id(1234));
		assertThat(recordFromCache).isNull();

	}


	@Test
	public void givenHookRegisteredOnPermanentAndVolatileCacheThenInsertBasedOnHook() throws Exception {


		final String ID_INSERT_WITH_HOOK_REPLACING_DEFAULT_INSERT = id(1_000_001);
		String ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT = id(1_000_002);
		String ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT_WITHOUT_VOLATILE = id(1_000_003);
		String ID_DEFAULT_INSERT = id(1_000_004);

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(zeCollectionSchemaType1.typeCode()).setRecordCacheType(SUMMARY_CACHED_WITH_VOLATILE);
			}
		});
		getModelLayerFactory().getRecordsCaches().register(new TestHook() {
			@Override
			public List<String> getHookSchemaTypes(MetadataSchemaTypes schemaTypes) {
				return Arrays.asList(zeCollectionSchemaType1.typeCode());
			}

			@Override
			public DeterminedHookCacheInsertion determineCacheInsertion(Record record, MetadataSchemaType schemaType,
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
		recordServices.add(r1 = newZeCollectionType1Record(ID_INSERT_WITH_HOOK_REPLACING_DEFAULT_INSERT).set(TITLE, "val1"));
		recordServices.add(r2 = newZeCollectionType1Record(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT).set(TITLE, "val2"));
		recordServices.add(r3 = newZeCollectionType1Record(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT_WITHOUT_VOLATILE).set(TITLE, "val3"));
		recordServices.add(r4 = newZeCollectionType1Record(ID_DEFAULT_INSERT).set(TITLE, "val4"));

		assertThatVersion(cache.getRecord(ID_INSERT_WITH_HOOK_REPLACING_DEFAULT_INSERT)).isEqualTo(r1.getVersion());
		assertThatVersion(cache.getRecord(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT)).isEqualTo(r2.getVersion());
		assertThatVersion(cache.getRecord(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT_WITHOUT_VOLATILE)).isEqualTo(r3.getVersion());
		assertThatVersion(cache.getRecord(ID_DEFAULT_INSERT)).isEqualTo(r4.getVersion());

		assertThat(cache.getCache(zeCollection).streamVolatile(zeCollectionSchemaType1.type())
				.map(Record::getId).collect(toList())).containsOnly(
				ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT,
				ID_DEFAULT_INSERT);

		cache.invalidateVolatile();

		assertThatVersion(cache.getRecord(ID_INSERT_WITH_HOOK_REPLACING_DEFAULT_INSERT)).isEqualTo(r1.getVersion());
		assertThatVersion(cache.getRecord(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT)).isEqualTo(r2.getVersion());
		assertThatVersion(cache.getRecord(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT_WITHOUT_VOLATILE)).isEqualTo(r3.getVersion());
		assertThatVersion(cache.getRecord(ID_DEFAULT_INSERT)).isNull(); //Was obtained from volatile

		assertThatVersion(cache.getRecordSummary(ID_INSERT_WITH_HOOK_REPLACING_DEFAULT_INSERT)).isEqualTo(r1.getVersion());
		assertThatVersion(cache.getRecordSummary(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT)).isEqualTo(r2.getVersion());
		assertThatVersion(cache.getRecordSummary(ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT_WITHOUT_VOLATILE)).isEqualTo(r3.getVersion());
		assertThatVersion(cache.getRecordSummary(ID_DEFAULT_INSERT)).isEqualTo(r4.getVersion());

		assertThat(cache.getCache(zeCollection).streamVolatile(zeCollectionSchemaType1.type())
				.map(Record::getId).collect(toList())).isEmpty();

		assertThat(cache.stream(zeCollection).map(Record::getId).collect(toList())).containsOnly(
				ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT,
				ID_INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT_WITHOUT_VOLATILE,
				ID_DEFAULT_INSERT);
	}

	public static abstract class TestHook implements RecordsCachesHook {

		Map<String, Record> cache = new HashMap<>();

		@Override
		public HookCacheInsertionResponse insert(Record record, MetadataSchemaTypes recordSchemaTypes,
												 InsertionReason reason) {
			cache.put(record.getId(), record);
			return new HookCacheInsertionResponse(CacheInsertionStatus.ACCEPTED, RemoteCacheAction.INSERT);
		}

		@Override
		public Record getById(String id, Provider<String, MetadataSchemaType> schemaTypeProviderByCollection) {
			return cache.get(id);
		}

		@Override
		public HookCachePresence isRestrictedToHookCache(String id, boolean integerId,
														 Optional<MetadataSchemaType> metadataSchemaType) {
			return HookCachePresence.CAN_BE_FOUND_IN_THIS_HOOK_CACHE;
		}
	}

	private Record newZeCollectionType1Record(String id) {
		return getModelLayerFactory().newRecordServices().newRecordWithSchema(zeCollectionSchemaType1.instance(), id);
	}

	private Record newZeCollectionType1Record(int intId) {
		return getModelLayerFactory().newRecordServices().newRecordWithSchema(zeCollectionSchemaType1.instance(), id(intId));
	}

	private Record newZeCollectionType2Record(int intId) {
		return getModelLayerFactory().newRecordServices().newRecordWithSchema(zeCollectionSchemaType2.instance(), id(intId));
	}

	private Record newAnotherCollectionType1Record(int intId) {
		return getModelLayerFactory().newRecordServices().newRecordWithSchema(anotherCollectionSchemaType1.instance(), id(intId));
	}

	private String id(int intId) {
		return useZeroPaddedIds ? StringUtils.leftPad("" + intId, 11, "0") : "" + intId;
	}

	private RecordImpl getPartiallyLoaded(int id) {
		return (RecordImpl) searchServices.search(new LogicalSearchQuery().setCondition(
				fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER).isEqualTo(id(id)))
				.setReturnedMetadatas(ReturnedMetadatasFilter.idVersionSchemaTitlePath())).get(0);

	}

	private LongAssert assertThatVersion(Record record) {
		return assertThat(record == null ? null : record.getVersion());
	}
}
