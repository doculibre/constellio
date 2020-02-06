package com.constellio.model.services.records.cache;

import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.QueryExecutionMethod;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.QueryCounter;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static com.constellio.model.entities.schemas.RecordCacheType.SUMMARY_CACHED_WITHOUT_VOLATILE;
import static com.constellio.model.services.records.cache.ByteArrayRecordDTO.MAIN_SORT_UNDEFINED;
import static com.constellio.model.services.search.LogicalSearchQueryExecutorInCache.NORMALIZED_SORTS_THRESHOLD;
import static com.constellio.model.services.search.LogicalSearchQueryExecutorInCache.MAIN_SORT_THRESHOLD;
import static com.constellio.model.services.search.LogicalSearchQueryExecutorInCache.UNNORMALIZED_SORTS_THRESHOLD;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.sdk.tests.QueryCounter.ON_SCHEMA_TYPES;
import static com.constellio.sdk.tests.TestUtils.englishMessages;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsAvailableInSummary;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSortable;
import static org.apache.ignite.internal.util.lang.GridFunc.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class MainSortValueRecordCacheAcceptanceTest extends ConstellioTest {

	String anotherCollection = "anotherCollection";

	Transaction transaction;
	TestRecord record1, record2, record3, record4, record5, record18, record42;

	TestsSchemasSetup zeCollectionSchemas = new TestsSchemasSetup(zeCollection).withSecurityFlag(false);
	ZeSchemaMetadatas zeCollectionSchemaType1 = zeCollectionSchemas.new ZeSchemaMetadatas();
	AnotherSchemaMetadatas zeCollectionSchemaType2 = zeCollectionSchemas.new AnotherSchemaMetadatas();

	TestsSchemasSetup anotherCollectionSchemas = new TestsSchemasSetup(anotherCollection).withSecurityFlag(false);
	ZeSchemaMetadatas anotherCollectionSchemaType1 = anotherCollectionSchemas.new ZeSchemaMetadatas();
	AnotherSchemaMetadatas anotherCollectionSchemaType2 = anotherCollectionSchemas.new AnotherSchemaMetadatas();

	RecordsCaches recordsCaches;

	UserServices userServices;
	RecordServices recordServices;
	SearchServices searchServices;

	StatsBigVaultServerExtension queriesListener;

	QueryCounter queryCounter;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withAllTestUsers(),
				withCollection(anotherCollection).withAllTestUsers()
		);
		defineSchemasManager().using(zeCollectionSchemas
				.withAStringMetadata(whichIsSortable, whichIsAvailableInSummary)
				.withADateTimeMetadata(whichIsSortable, whichIsAvailableInSummary)
				.withAnotherStringMetadata());
		defineSchemasManager().using(anotherCollectionSchemas);

		zeCollectionSchemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeCollectionSchemas.zeDefaultSchemaCode()).get("title").setSortable(true);

				types.getSchema(zeCollectionSchemas.anotherDefaultSchemaCode()).get("title").setSortable(true);
			}
		});

		anotherCollectionSchemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(anotherCollectionSchemas.zeDefaultSchemaCode()).get("title").setSortable(true);

				types.getSchema(anotherCollectionSchemas.anotherDefaultSchemaCode()).get("title").setSortable(true);
			}
		});

		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();

		userServices = getModelLayerFactory().newUserServices();
		recordsCaches = getModelLayerFactory().getRecordsCaches();

		zeCollectionSchemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(zeCollectionSchemaType1.typeCode()).setRecordCacheType(SUMMARY_CACHED_WITHOUT_VOLATILE);
				types.getSchemaType(zeCollectionSchemaType2.typeCode()).setRecordCacheType(SUMMARY_CACHED_WITHOUT_VOLATILE);
			}
		});

		anotherCollectionSchemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(anotherCollectionSchemaType1.typeCode()).setRecordCacheType(SUMMARY_CACHED_WITHOUT_VOLATILE);
				types.getSchemaType(anotherCollectionSchemaType2.typeCode()).setRecordCacheType(SUMMARY_CACHED_WITHOUT_VOLATILE);
			}
		});

		queryCounter = new QueryCounter(getDataLayerFactory(), ON_SCHEMA_TYPES(zeCollectionSchemaType1.typeCode()));

	}

	@Test
	public void givenNewRecordsAddedThenNoSortValueUntilDetermined() throws Exception {

		Transaction tx = new Transaction();
		String id1 = tx.add(recordServices.newRecordWithSchema(zeCollectionSchemaType1.instance())
				.set(Schemas.TITLE, "Value 1")).getId();
		String id4 = tx.add(recordServices.newRecordWithSchema(zeCollectionSchemaType1.instance())
				.set(Schemas.TITLE, "Value 34")).getId();
		String id5 = tx.add(recordServices.newRecordWithSchema(zeCollectionSchemaType2.instance())
				.set(Schemas.TITLE, "ValUe 230")).getId();
		String id7 = tx.add(recordServices.newRecordWithSchema(zeCollectionSchemaType2.instance())
				.set(Schemas.TITLE, "Value 10000")).getId();
		recordServices.execute(tx);

		tx = new Transaction();
		String id2 = tx.add(recordServices.newRecordWithSchema(anotherCollectionSchemaType1.instance())
				.set(Schemas.TITLE, "Value 2")).getId();
		;
		String id3 = tx.add(recordServices.newRecordWithSchema(anotherCollectionSchemaType1.instance())
				.set(Schemas.TITLE, "Valué 3")).getId();
		;
		String id6 = tx.add(recordServices.newRecordWithSchema(anotherCollectionSchemaType2.instance())
				.set(Schemas.TITLE, "Value 231")).getId();
		;
		String id8 = tx.add(recordServices.newRecordWithSchema(anotherCollectionSchemaType2.instance())
				.set(Schemas.TITLE, "Value 100000")).getId();
		;
		recordServices.execute(tx);

		for (String id : asList(id1, id2, id3, id4, id5, id6, id7, id8)) {
			assertThat(recordServices.getDocumentById(id).getRecordDTO().getMainSortValue()).isEqualTo(MAIN_SORT_UNDEFINED);
		}

		recordsCaches.updateRecordsMainSortValue();

		assertThat(recordServices.realtimeGetRecordSummaryById(id1).getRecordDTO().getMainSortValue()).isEqualTo(1);
		assertThat(recordServices.realtimeGetRecordSummaryById(id2).getRecordDTO().getMainSortValue()).isEqualTo(3);
		assertThat(recordServices.realtimeGetRecordSummaryById(id3).getRecordDTO().getMainSortValue()).isEqualTo(5);
		assertThat(recordServices.realtimeGetRecordSummaryById(id4).getRecordDTO().getMainSortValue()).isEqualTo(7);
		assertThat(recordServices.realtimeGetRecordSummaryById(id5).getRecordDTO().getMainSortValue()).isEqualTo(9);
		assertThat(recordServices.realtimeGetRecordSummaryById(id6).getRecordDTO().getMainSortValue()).isEqualTo(11);
		assertThat(recordServices.realtimeGetRecordSummaryById(id7).getRecordDTO().getMainSortValue()).isEqualTo(13);
		assertThat(recordServices.realtimeGetRecordSummaryById(id8).getRecordDTO().getMainSortValue()).isEqualTo(15);


		//Given an other metadata is modified, the records keeps it's order
		recordServices.update(recordServices.getDocumentById(id4).set(zeCollectionSchemaType1.stringMetadata(), "test"));
		assertThat(recordServices.realtimeGetRecordSummaryById(id4).getRecordDTO().getMainSortValue()).isEqualTo(7);

		//Given the title metadata is modified (even for a similar value), the records lose it's order
		recordServices.update(recordServices.getDocumentById(id4).set(Schemas.TITLE, "Value 34b"));
		assertThat(recordServices.realtimeGetRecordSummaryById(id4).getRecordDTO().getMainSortValue()).isEqualTo(MAIN_SORT_UNDEFINED);

		recordsCaches.updateRecordsMainSortValue();
		assertThat(recordServices.realtimeGetRecordSummaryById(id4).getRecordDTO().getMainSortValue()).isEqualTo(7);
	}


	@Test
	public void whenSortingByTitleAscendinglyThenUseSortValue() throws Exception {

		System.out.println("generating random numbers...");
		List<Integer> randomNumbers = LangUtils.getIntValuesInRandomOrder(MAIN_SORT_THRESHOLD);

		String[] expectedIds = new String[randomNumbers.size()];

		System.out.println("saving records...");
		Transaction tx = new Transaction();
		tx.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
		for (int randomNumber : randomNumbers) {
			String id = tx.add(recordServices.newRecordWithSchema(zeCollectionSchemaType1.instance())
					.set(Schemas.TITLE, "A somewhat long title 1 : " + randomNumber)).getId();
			expectedIds[randomNumber] = id;
		}

		recordServices.execute(tx);
		recordsCaches.updateRecordsMainSortValue();

		System.out.println("querying...");
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(from(zeCollectionSchemaType1.type()).where(Schemas.TITLE).isNotNull());
		query.setQueryExecutionMethod(QueryExecutionMethod.USE_CACHE);
		query.sortAsc(Schemas.TITLE);

		long start = new Date().getTime();
		List<String> results = searchServices.searchRecordIds(query);
		long end = new Date().getTime();
		assertThat(results).isEqualTo(asList(expectedIds));
		System.out.println("query took " + (end - start) + "ms");

		//In this case, query execution time is an accurate indicator that the sort value was used. Otherwise, it would take ~25 sec instead of ~250ms
		assertThat(end - start).isLessThan(1000);
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);

		recordServices.add(recordServices.newRecordWithSchema(zeCollectionSchemaType1.instance())
				.set(Schemas.TITLE, "One record too much"));

		searchServices.searchRecordIds(query);
		assertThat(queryCounter.newQueryCalls()).isEqualTo(2);

	}

	@Test
	public void whenSortingBySortableNormalizedMetadataAscendinglyThenOnlyRunnedInCacheIfRecordSizeLesserOrEqualToThreshold()
			throws Exception {

		System.out.println("generating random numbers...");
		List<Integer> randomNumbers = LangUtils.getIntValuesInRandomOrder(NORMALIZED_SORTS_THRESHOLD);

		String[] expectedIds = new String[randomNumbers.size()];

		System.out.println("saving records...");
		Transaction tx = new Transaction();
		tx.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
		for (int randomNumber : randomNumbers) {
			String id = tx.add(recordServices.newRecordWithSchema(zeCollectionSchemaType1.instance())
					.set(zeCollectionSchemaType1.stringMetadata(), "A somewhat long title 1 : " + randomNumber)).getId();
			expectedIds[randomNumber] = id;
		}

		recordServices.execute(tx);
		recordsCaches.updateRecordsMainSortValue();

		System.out.println("querying...");
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(from(zeCollectionSchemaType1.type()).where(zeCollectionSchemaType1.stringMetadata()).isNotNull());
		query.setQueryExecutionMethod(QueryExecutionMethod.USE_CACHE);
		query.sortAsc(zeCollectionSchemaType1.stringMetadata());

		long start = new Date().getTime();
		List<String> results = searchServices.searchRecordIds(query);
		long end = new Date().getTime();
		assertThat(results).isEqualTo(asList(expectedIds));
		assertThat(queryCounter.newQueryCalls()).isZero();
		System.out.println("query took " + (end - start) + "ms");

		//In this case, query execution time is an accurate indicator that the sort value was used. Otherwise, it would take ~25 sec instead of ~250ms
		assertThat(end - start).isLessThan(1000);

		recordServices.add(recordServices.newRecordWithSchema(zeCollectionSchemaType1.instance())
				.set(zeCollectionSchemaType1.stringMetadata(), "One record too much"));

		searchServices.searchRecordIds(query);
		assertThat(queryCounter.newQueryCalls()).isEqualTo(1);

	}


	@Test
	public void whenSortingBySortableUnnormalizedMetadataAscendinglyThenOnlyRunnedInCacheIfRecordSizeLesserOrEqualToThreshold()
			throws Exception {

		System.out.println("generating random numbers...");
		List<Integer> randomNumbers = LangUtils.getIntValuesInRandomOrder(UNNORMALIZED_SORTS_THRESHOLD);

		LocalDateTime base = LocalDateTime.now();
		String[] expectedIds = new String[randomNumbers.size()];

		System.out.println("saving records...");
		Transaction tx = new Transaction();
		tx.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
		for (int randomNumber : randomNumbers) {
			String id = tx.add(recordServices.newRecordWithSchema(zeCollectionSchemaType1.instance())
					.set(zeCollectionSchemaType1.dateTimeMetadata(), base.plusSeconds(randomNumber))).getId();
			expectedIds[randomNumber] = id;
		}

		recordServices.execute(tx);
		recordsCaches.updateRecordsMainSortValue();

		System.out.println("querying...");
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(from(zeCollectionSchemaType1.type()).where(zeCollectionSchemaType1.dateTimeMetadata()).isNotNull());
		query.setQueryExecutionMethod(QueryExecutionMethod.USE_CACHE);
		query.sortAsc(zeCollectionSchemaType1.dateTimeMetadata());

		long start = new Date().getTime();
		List<String> results = searchServices.searchRecordIds(query);
		long end = new Date().getTime();
		assertThat(results).isEqualTo(asList(expectedIds));
		assertThat(queryCounter.newQueryCalls()).isZero();
		System.out.println("query took " + (end - start) + "ms");

		//In this case, query execution time is an accurate indicator that the sort value was used. Otherwise, it would take ~25 sec instead of ~250ms
		assertThat(end - start).isLessThan(1000);
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);

		recordServices.add(recordServices.newRecordWithSchema(zeCollectionSchemaType1.instance())
				.set(zeCollectionSchemaType1.dateTimeMetadata(), base));

		searchServices.searchRecordIds(query);
		assertThat(queryCounter.newQueryCalls()).isEqualTo(1);

	}

	@After
	public void tearDown() throws Exception {

		if (!failureDetectionTestWatcher.isFailed()) {
			RecordsCache2IntegrityDiagnosticService service = new RecordsCache2IntegrityDiagnosticService(getModelLayerFactory());
			ValidationErrors errors = service.validateIntegrity(false, true);
			//List<String> messages = englishMessages(errors).stream().map((s) -> substringBefore(s, " :")).collect(toList());

			List<String> messages = englishMessages(errors);
			assertThat(messages).isEmpty();
		}

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
		return StringUtils.leftPad("" + intId, 11, "0");
	}

	private RecordImpl getCustomlyLoaded(int id) {
		return (RecordImpl) searchServices.search(new LogicalSearchQuery().setCondition(
				fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER).isEqualTo(id(id)))
				.setReturnedMetadatas(ReturnedMetadatasFilter.idVersionSchemaTitlePath())).get(0);

	}
}
