package com.constellio.model.services.search;

import com.constellio.data.extensions.AfterQueryParams;
import com.constellio.data.extensions.BigVaultServerExtension;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.SlowTest;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.services.search.RecordStreamUtils.recordIds;
import static com.constellio.model.services.search.SolrFieldsComparator.asc;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static com.constellio.model.services.search.query.logical.QueryExecutionMethod.USE_SOLR;
import static com.constellio.sdk.tests.TestUtils.assertThatRecords;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SlowTest
public class SearchServiceStreamingAcceptanceTest extends ConstellioTest {

	SearchServiceAcceptanceTestSchemas schema = new SearchServiceAcceptanceTestSchemas(zeCollection);
	SearchServiceAcceptanceTestSchemas.ZeSchemaMetadatas zeSchema = schema.new ZeSchemaMetadatas();
	SearchServiceAcceptanceTestSchemas.ZeCustomSchemaMetadatas zeCustomSchema = schema.new ZeCustomSchemaMetadatas();
	SearchServiceAcceptanceTestSchemas.AnotherSchemaMetadatas anotherSchema = schema.new AnotherSchemaMetadatas();

	RecordServices recordServices;
	SearchServices searchServices;

	LogicalSearchQuery query;


	AtomicInteger documentsCounter = new AtomicInteger();
	AtomicInteger fieldsCounter = new AtomicInteger();


	@Before
	public void setUp() throws Exception {

		prepareSystem(
				withZeCollection().withAllTestUsers().withTasksModule()
		);

		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();

		defineSchemasManager().using(schema.withAStringMetadata().withANumberMetadata());

		Transaction tx = new Transaction();

		tx.add(recordServices.newRecordWithSchema(zeSchema.instance(), "id1")
				.set(zeSchema.stringMetadata(), "Value 1"))
				.set(zeSchema.numberMetadata(), 42.42);

		tx.add(recordServices.newRecordWithSchema(zeSchema.instance(), "id2")
				.set(zeSchema.stringMetadata(), "Value 2"))
				.set(zeSchema.numberMetadata(), 1.1);

		tx.add(recordServices.newRecordWithSchema(zeSchema.instance(), "id3")
				.set(zeSchema.stringMetadata(), "Value 3"))
				.set(zeSchema.numberMetadata(), 66.6);

		tx.add(recordServices.newRecordWithSchema(zeSchema.instance(), "id4")
				.set(zeSchema.stringMetadata(), "Value 4"))
				.set(zeSchema.numberMetadata(), 49.0);


		recordServices.execute(tx);

		query = new LogicalSearchQuery(from(zeSchema.instance()).returnAll());
		query.setQueryExecutionMethod(USE_SOLR);

		getDataLayerFactory().getExtensions().getSystemWideExtensions().bigVaultServerExtension
				.add(new BigVaultServerExtension() {
					@Override
					public void afterQuery(AfterQueryParams params) {
						documentsCounter.addAndGet(params.getReturnedResultsCount());

						String fl = params.getSolrParams().get("fl");
						if (fl != null) {
							fieldsCounter.addAndGet(fl.split(",").length);
						}

					}
				});
	}

	private void assertThatLoadedRecordsCountIs(int expected) {
		assertThat(documentsCounter.get()).isEqualTo(expected);
		documentsCounter.set(0);
	}


	private void assertThatLoadedFieldsCountIs(int expected) {
		assertThat(fieldsCounter.get()).isEqualTo(expected);
		documentsCounter.set(0);
	}

	@Test
	public void whenComputingMaxAndMinOfFieldThenOnlyLoadOneRecordFromSolr()
			throws Exception {

		Toggle.VALIDATE_CACHE_EXECUTION_SERVICE_USING_SOLR.disable();

		assertThatRecords(searchServices.stream(query).collect(Collectors.toList()))
				.extractingMetadatas("id", "stringMetadata", "numberMetadata")
				.containsOnly(
						tuple("id1", "Value 1", 42.42),
						tuple("id2", "Value 2", 1.1),
						tuple("id3", "Value 3", 66.6),
						tuple("id4", "Value 4", 49.0)
				);

		assertThatLoadedRecordsCountIs(4);

		assertThatRecords(searchServices.stream(query).sorted(asc(IDENTIFIER)).collect(Collectors.toList()))
				.extractingMetadatas("id", "stringMetadata", "numberMetadata")
				.containsExactly(
						tuple("id1", "Value 1", 42.42),
						tuple("id2", "Value 2", 1.1),
						tuple("id3", "Value 3", 66.6),
						tuple("id4", "Value 4", 49.0)
				);
		assertThatLoadedRecordsCountIs(4);

		assertThat(searchServices.streamFromSolr(query).max(asc(zeSchema.numberMetadata())).get().getId()).isEqualTo("id3");
		assertThatLoadedRecordsCountIs(1);

		assertThat(searchServices.streamFromSolr(query).min(asc(zeSchema.numberMetadata())).get().getId()).isEqualTo("id2");
		assertThatLoadedRecordsCountIs(1);

		assertThat(searchServices.streamFromSolr(query).filter(where(zeSchema.numberMetadata()).isEqualTo(66.6))
				.findFirst().get().getId()).isEqualTo("id3");
		assertThatLoadedRecordsCountIs(1);

		assertThat(searchServices.streamFromSolr(query).filter(where(zeSchema.numberMetadata()).isGreaterThan(2))
				.collect(recordIds())).containsOnly("id1", "id3", "id4");
		assertThatLoadedRecordsCountIs(3);

		assertThat(searchServices.stream(query).filter(where(zeSchema.numberMetadata()).isEqualTo(66.6).negate())
				.findFirst().get().getId()).isEqualTo("id1");
		assertThatLoadedRecordsCountIs(1);

		assertThat(searchServices.stream(query).filter(where(zeSchema.numberMetadata()).isEqualTo(66.6).negate())
				.collect(recordIds())).containsOnly("id1", "id2", "id4");
		assertThatLoadedRecordsCountIs(3);

		assertThat(searchServices.stream(query).filter(where(zeSchema.numberMetadata()).isEqualTo(66.6).negate())
				.count()).isEqualTo(3);
		assertThatLoadedRecordsCountIs(0);

		assertThat(searchServices.stream(query).filter(where(zeSchema.numberMetadata()).isEqualTo(66.6).negate())
				.distinct().count()).isEqualTo(3);
		assertThatLoadedRecordsCountIs(0);

		//Testing allMatch
		assertThat(searchServices.stream(query).allMatch(where(zeSchema.numberMetadata()).isGreaterThan(1.0))).isTrue();
		assertThat(searchServices.stream(query).allMatch(where(zeSchema.numberMetadata()).isGreaterThan(2.0))).isFalse();
		assertThat(searchServices.stream(query).allMatch(where(zeSchema.numberMetadata()).isGreaterThan(100.0))).isFalse();
		assertThat(searchServices.stream(query).allMatch(where(zeSchema.numberMetadata()).isGreaterThan(100.0))).isFalse();
		assertThat(searchServices.stream(query)
				.filter(where(zeSchema.numberMetadata()).isEqualTo(66.6))
				.allMatch(where(zeSchema.numberMetadata()).isGreaterThan(2.0))).isTrue();
		assertThat(searchServices.stream(query)
				.filter(where(zeSchema.numberMetadata()).isEqualTo(66.6))
				.allMatch(where(zeSchema.numberMetadata()).isGreaterThan(100.0))).isFalse();
		assertThat(searchServices.stream(query)
				.filter(where(zeSchema.numberMetadata()).isEqualTo(77.7))
				.allMatch(where(zeSchema.numberMetadata()).isGreaterThan(100.0))).isTrue();
		assertThatLoadedRecordsCountIs(0);

		assertThat(searchServices.stream(query).allMatch(where(zeSchema.numberMetadata()).isGreaterThan(1.0))).isTrue();
		assertThat(searchServices.stream(query).allMatch(where(zeSchema.numberMetadata()).isGreaterThan(2.0))).isFalse();
		assertThatLoadedRecordsCountIs(0);


	}

}
