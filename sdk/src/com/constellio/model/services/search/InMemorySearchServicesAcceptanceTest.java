package com.constellio.model.services.search;

import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.utils.TimeProvider;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.cache.CacheConfig;
import com.constellio.model.services.records.cache.RecordsCache;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.DataStoreFieldLogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.DataStoreFilters;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.ongoing.OngoingLogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import org.assertj.core.api.ListAssert;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.entities.schemas.Schemas.TITLE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.*;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMultivalue;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

public class InMemorySearchServicesAcceptanceTest extends ConstellioTest {

	LocalDateTime NOW = TimeProvider.getLocalDateTime();

	RecordServices recordServices;
	SearchServices searchServices;
	FreeTextSearchServices freetext;
	InMemorySearchServices inMemorySearchServices;
	RecordsCache zeCollectionCaches;
	RecordDao recordDao;

	SearchServiceAcceptanceTestSchemas schemas = new SearchServiceAcceptanceTestSchemas(zeCollection);
	SearchServiceAcceptanceTestSchemas.ZeSchemaMetadatas defaultSchemaOfType1 = schemas.new ZeSchemaMetadatas();
	SearchServiceAcceptanceTestSchemas.ZeCustomSchemaMetadatas customSchemaOfType1 = schemas.new ZeCustomSchemaMetadatas();
	SearchServiceAcceptanceTestSchemas.AnotherSchemaMetadatas defaultSchemaOfType2 = schemas.new AnotherSchemaMetadatas();
	SearchServiceAcceptanceTestSchemas.ThirdSchemaMetadatas defaultSchemaOfType3 = schemas.new ThirdSchemaMetadatas();

	SearchServiceAcceptanceTestSchemas otherCollectionSchemas = new SearchServiceAcceptanceTestSchemas("collection2");
	SearchServiceAcceptanceTestSchemas.OtherSchemaMetadatasInCollection2 schemaInOtherCollection = otherCollectionSchemas.new OtherSchemaMetadatasInCollection2();

	LogicalSearchCondition condition;

	Transaction transaction;

	@Before
	public void setUp() {
		Toggle.QUERY_EXECUTION_IN_CACHE.disable();
		prepareSystem(
				withZeCollection(),
				withCollection("collection2")
		);
		//givenCollection(zeCollection, Arrays.asList(Language.French.getCode(), Language.English.getCode()));
		recordServices = getModelLayerFactory().newRecordServices();
		recordDao = spy(getDataLayerFactory().newRecordDao());
		searchServices = new SearchServices(recordDao, getModelLayerFactory());
		zeCollectionCaches = getModelLayerFactory().getRecordsCaches().getCache(zeCollection);
		inMemorySearchServices = new InMemorySearchServices(getModelLayerFactory());
		transaction = new Transaction();
		Toggle.QUERY_EXECUTION_IN_CACHE.enable();
	}

	@Test
	public void givenSingleSchemaTypeQueryThenOnlyExecutableInCacheIfSchemaTypeHasPermanentCache()
			throws Exception {

		defineSchemasManager().using(schemas);
		zeCollectionCaches.configureCache(CacheConfig.permanentCache(defaultSchemaOfType1.type()));
		zeCollectionCaches.configureCache(CacheConfig.volatileCache(defaultSchemaOfType2.type(), 100));

		assertExecutableInCache(from(defaultSchemaOfType1.type()).where(TITLE).isEqualTo("test"));
		assertNotExecutableInCache(from(defaultSchemaOfType2.type()).where(TITLE).isEqualTo("test"));
		assertNotExecutableInCache(from(defaultSchemaOfType3.type()).where(TITLE).isEqualTo("test"));
	}
	@Test
	public void givenMultipleSchemaTypeQueryThenOnlyExecutableInCacheIfSchemaTypeHasPermanentCache()
			throws Exception {

		defineSchemasManager().using(schemas);
		zeCollectionCaches.configureCache(CacheConfig.permanentCache(defaultSchemaOfType1.type()));
		zeCollectionCaches.configureCache(CacheConfig.volatileCache(defaultSchemaOfType2.type(), 100));

		assertExecutableInCache(from(defaultSchemaOfType1.type()).where(TITLE).isEqualTo("test"));
		assertNotExecutableInCache(from(defaultSchemaOfType2.type()).where(TITLE).isEqualTo("test"));
		assertNotExecutableInCache(from(defaultSchemaOfType3.type()).where(TITLE).isEqualTo("test"));
	}

	@Test
	public void givenMultipleMetadataInQueryThenOnlyExecutableInCacheIfSchemaTypeHasPermanentCache()
			throws Exception {

		defineSchemasManager().using(schemas.withAStringMetadata());
		zeCollectionCaches.configureCache(CacheConfig.permanentCache(defaultSchemaOfType1.type()));

		assertExecutableInCache(from(defaultSchemaOfType1.type()).
				whereAny(defaultSchemaOfType1.stringMetadata(), TITLE).isEqualTo("test"));

	}
	@Test
	public void multiplesMetadaThenFindAccurateRecords()
			throws Exception {

		defineSchemasManager().using(schemas.withAStringMetadata());
		zeCollectionCaches.configureCache(CacheConfig.permanentCache(defaultSchemaOfType1.type()));
		transaction.add(new TestRecord(defaultSchemaOfType1, "record1").
				set(TITLE, "test").set(defaultSchemaOfType1.stringMetadata(),"test1"));
		transaction.add(new TestRecord(defaultSchemaOfType1, "record2").
				set(TITLE, "test1").set(defaultSchemaOfType1.stringMetadata(),"test"));
		recordServices.execute(transaction);
		assertThatResultsIds(from(defaultSchemaOfType1.type()).
				whereAny(defaultSchemaOfType1.stringMetadata(), TITLE).isEqualTo("test")).containsOnly("record1");
		assertThatResultsIds(from(defaultSchemaOfType1.type()).
				whereAny(defaultSchemaOfType1.stringMetadata(), TITLE).isEqualTo("test")).containsOnly("record2");

	}
	@Test
	public void givenMultipleTypeSchemaInQueryThenNotExecutableInCache()
			throws Exception {

		defineSchemasManager().using(schemas.withAStringMetadata());
		zeCollectionCaches.configureCache(CacheConfig.permanentCache(defaultSchemaOfType1.type()));
		zeCollectionCaches.configureCache(CacheConfig.permanentCache(defaultSchemaOfType2.type()));

		assertExecutableInCache(from(defaultSchemaOfType1.type(),defaultSchemaOfType2.type()).
				whereAny(defaultSchemaOfType1.stringMetadata(), TITLE).isEqualTo("test"));

	}
	@Test
	public void givenNullTypeSchemaInQueryThenNotExecutableInCache()
			throws Exception {

		defineSchemasManager().using(schemas.withAStringMetadata());
		MetadataSchemaType type = null;
		zeCollectionCaches.configureCache(CacheConfig.permanentCache(type));

		assertExecutableInCache(from(type).
				whereAny(defaultSchemaOfType1.stringMetadata(), TITLE).isEqualTo("test"));

	}

	@Test
	public void givenMultiplefieldInInQueryThenOnlyExecutableInCacheIfSchemaTypeHasPermanentCache()
			throws Exception {

		defineSchemasManager().using(schemas.withAStringMetadata());
		zeCollectionCaches.configureCache(CacheConfig.permanentCache(defaultSchemaOfType1.type()));

		assertThat(from(defaultSchemaOfType1.type()).
				whereAny(defaultSchemaOfType1.stringMetadata(), TITLE).isEqualTo("test"));

	}



	@Test
	public void givenIsEqualConditionThenFindAccurateRecords()
			throws Exception {

		defineSchemasManager().using(schemas);
		zeCollectionCaches.configureCache(CacheConfig.permanentCache(defaultSchemaOfType1.type()));

		transaction.add(new TestRecord(defaultSchemaOfType1, "record1").set(TITLE, "Chat"));
		transaction.add(new TestRecord(defaultSchemaOfType1, "record2").set(TITLE, "Chien"));
		transaction.add(new TestRecord(defaultSchemaOfType1, "record3").set(TITLE, "Crocodile"));
		recordServices.execute(transaction);

		assertThatResultsIds(from(defaultSchemaOfType1.type()).where(TITLE).isEqualTo("Chat")).containsOnly("record1");
		assertThatResultsIds(from(defaultSchemaOfType1.type()).where(TITLE).isEqualTo("Chien")).containsOnly("record2");
		assertThatResultsIds(from(defaultSchemaOfType1.type()).where(TITLE).isEqualTo("Crocodile")).containsOnly("record3");
		assertThatResultsIds(from(defaultSchemaOfType1.type()).where(TITLE).isEqualTo("Mouette")).isEmpty();
	}


	@Test
	public void givenIsEqualConditionOnMultivalueNumberMetadataThenFindAccurateRecords()
			throws Exception {

		defineSchemasManager().using(schemas.withANumberMetadata(whichIsMultivalue));
		zeCollectionCaches.configureCache(CacheConfig.permanentCache(defaultSchemaOfType1.type()));

		Metadata numberMetadata = defaultSchemaOfType1.numberMetadata();
		transaction.add(new TestRecord(defaultSchemaOfType1, "record1").set(numberMetadata, asList(1, 52)));
		transaction.add(new TestRecord(defaultSchemaOfType1, "record2").set(numberMetadata, asList(1.5)));
		transaction.add(new TestRecord(defaultSchemaOfType1, "record3").set(numberMetadata, asList(2.0)));
		recordServices.execute(transaction);
		assertThatResultsIds(from(defaultSchemaOfType1.type()).where(numberMetadata).isEqualTo(1)).containsOnly("record1");
		assertThatResultsIds(from(defaultSchemaOfType1.type()).where(numberMetadata).isEqualTo(1.0)).containsOnly("record1");
		assertThatResultsIds(from(defaultSchemaOfType1.type()).where(numberMetadata).isEqualTo(1.5)).containsOnly("record2");
		assertThatResultsIds(from(defaultSchemaOfType1.type()).where(numberMetadata).isEqualTo(2)).containsOnly("record3");
		assertThatResultsIds(from(defaultSchemaOfType1.type()).where(numberMetadata).isEqualTo(2.0)).containsOnly("record3");
		assertThatResultsIds(from(defaultSchemaOfType1.type()).where(numberMetadata).isEqualTo(52)).containsOnly("record1");
		assertThatResultsIds(from(defaultSchemaOfType1.type()).where(numberMetadata).isEqualTo(52.0)).containsOnly("record1");
		assertThatResultsIds(from(defaultSchemaOfType1.type()).where(numberMetadata).isEqualTo(42)).isEmpty();
	}
	@Test
	public void givenNotConditionThenNotExecutableInCacheIfSchemaTypeHasPermanentCache()
			throws Exception {

		defineSchemasManager().using(schemas.withAStringMetadata());
		zeCollectionCaches.configureCache(CacheConfig.permanentCache(defaultSchemaOfType1.type()));
		transaction.add(new TestRecord(defaultSchemaOfType1, "record1").set(TITLE, "TEST"));
		recordServices.execute(transaction);
		assertNotExecutableInCache(from(defaultSchemaOfType1.type()).where(not(where(TITLE).isEqualTo("test"))));
		//assertThatResultsIds(not(from(defaultSchemaOfType1.type()).where(TITLE).isEqualTo("TEST"))).containsOnly("record1");

	}
	@Test
	public void givenAndConditionThenExecutableInCacheAndReturnSameResultsThanSolr()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata());
		zeCollectionCaches.configureCache(CacheConfig.permanentCache(defaultSchemaOfType1.type()));
		transaction.add(new TestRecord(defaultSchemaOfType1, "record1").
				set(TITLE, "testTitle1").set(defaultSchemaOfType1.stringMetadata(),"metada1"));
		transaction.add(new TestRecord(defaultSchemaOfType1, "record2").
				set(TITLE, "testTitle1").set(defaultSchemaOfType1.stringMetadata(),"metada2"));
		transaction.add(new TestRecord(defaultSchemaOfType1, "record3").
				set(TITLE, "testTitle2").set(defaultSchemaOfType1.stringMetadata(),"metada1"));
		transaction.add(new TestRecord(defaultSchemaOfType1, "record4").
				set(TITLE, "testTitle2").set(defaultSchemaOfType1.stringMetadata(),"metada2"));
		recordServices.execute(transaction);

		/*assertExecutableInCache(from(defaultSchemaOfType1.type()).whereAllConditions(
				where(TITLE).isEqualTo("testTitle1"),
				where(defaultSchemaOfType1.stringMetadata()).isEqualTo("metada1")
		));*/
		assertThatResultsIds(from(defaultSchemaOfType1.type()).whereAllConditions(
				where(TITLE).isEqualTo("testTitle1"),
				where(defaultSchemaOfType1.stringMetadata()).isEqualTo("metada1"))).containsOnly("record1");
		assertThatResultsIds(from(defaultSchemaOfType1.type()).whereAnyCondition(
				where(TITLE).isEqualTo("testTitle1"),
				where(defaultSchemaOfType1.stringMetadata()).isEqualTo("metada1"))).containsOnly("record4");




	}

	@Test
	public void givenConditionOnMultipleFieldsThenExecutableInCacheAndReturnSameResultsThanSolr()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata());
		zeCollectionCaches.configureCache(CacheConfig.permanentCache(defaultSchemaOfType1.type()));
		transaction.add(new TestRecord(defaultSchemaOfType1, "record1").
				set(TITLE, "value1").set(defaultSchemaOfType1.stringMetadata(),"value1"));
		transaction.add(new TestRecord(defaultSchemaOfType1, "record2").
				set(TITLE, "value1").set(defaultSchemaOfType1.stringMetadata(),"value2"));
		transaction.add(new TestRecord(defaultSchemaOfType1, "record3").
				set(TITLE, "value2").set(defaultSchemaOfType1.stringMetadata(),"value1"));
		transaction.add(new TestRecord(defaultSchemaOfType1, "record4").
				set(TITLE, "value2").set(defaultSchemaOfType1.stringMetadata(),"value2"));
		recordServices.execute(transaction);

		/*assertExecutableInCache(from(defaultSchemaOfType1.type()).whereAllConditions(
				where(TITLE).isEqualTo("testTitle1"),
				where(defaultSchemaOfType1.stringMetadata()).isEqualTo("metada1")
		));*/
		assertThatResultsIds(from(defaultSchemaOfType1.type()).whereAll(TITLE, defaultSchemaOfType1.stringMetadata())
				.isEqualTo("value1")).containsOnly("record1");
		assertThatResultsIds(from(defaultSchemaOfType1.type()).whereAny(TITLE, defaultSchemaOfType1.stringMetadata())
						.isEqualTo("value1")).containsOnly("record4");

	}
	@Test
	public void afficher()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata());
		zeCollectionCaches.configureCache(CacheConfig.permanentCache(defaultSchemaOfType1.type()));
		transaction.add(new TestRecord(defaultSchemaOfType1, "record1").
				set(TITLE, "value1").set(defaultSchemaOfType1.stringMetadata(),"value1"));
		recordServices.execute(transaction);
		LogicalSearchQuery query= new LogicalSearchQuery(from(defaultSchemaOfType1.type()).whereAny(TITLE, defaultSchemaOfType1.stringMetadata())
				.isEqualTo("value1"));
		System.out.println("test:" + query.getDataStore());



	}

	//@Test
	public void givenOrConditionThenNotExecutableInCacheIfSchemaTypeHasPermanentCache()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata());
		zeCollectionCaches.configureCache(CacheConfig.permanentCache(defaultSchemaOfType1.type()));
		transaction.add(new TestRecord(defaultSchemaOfType1, "record1").
				set(TITLE, "testTitle1").set(defaultSchemaOfType1.stringMetadata(),"metada1"));
		transaction.add(new TestRecord(defaultSchemaOfType1, "record2").
				set(TITLE, "testTitle2").set(defaultSchemaOfType1.stringMetadata(),"metada2"));
		recordServices.execute(transaction);
		assertNotExecutableInCache(from(defaultSchemaOfType1.type()).whereAllConditions(
				where(TITLE).isEqualTo("testTitle1"),
				where(defaultSchemaOfType1.stringMetadata()).isEqualTo("metada1")));

		//assertThatResultsIds(from(defaultSchemaOfType1.type()).where(numberMetadata).isEqualTo(1)).containsOnly("record1");


	}

	private ListAssert<Object> assertThatResultsIds(LogicalSearchCondition condition) {
		return assertThat(search(new LogicalSearchQuery(condition)).getRecords()).extracting("id");
	}
	private ListAssert<Record> assertThatResults(LogicalSearchCondition condition) {
		return assertThat(search(new LogicalSearchQuery(condition)).getRecords());
	}

	private SPEQueryResponse search(LogicalSearchCondition condition) {
		return search(new LogicalSearchQuery(condition));
	}

	private SPEQueryResponse search(LogicalSearchQuery query) {

		assertThat(inMemorySearchServices.isExecutableInCache(query)).isTrue();
		return searchServices.query(query);
	}

	private void assertNotExecutableInCache(LogicalSearchCondition condition) {
		assertNotExecutableInCache(new LogicalSearchQuery(condition));
	}

	private void assertExecutableInCache(LogicalSearchCondition condition) {
		assertExecutableInCache(new LogicalSearchQuery(condition));
	}

	private void assertNotExecutableInCache(LogicalSearchQuery query) {
		assertThat(inMemorySearchServices.isExecutableInCache(query)).isFalse();
	}

	private void assertExecutableInCache(LogicalSearchQuery query) {
		assertThat(inMemorySearchServices.isExecutableInCache(query)).isTrue();
	}
}
