package com.constellio.model.services.records.reindexing;

import static com.constellio.sdk.tests.TestUtils.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServiceAcceptanceTestSchemas;
import com.constellio.sdk.tests.ConstellioTest;

public class ReindexingSchemaTypeRecordsProviderAcceptanceTest extends ConstellioTest {

	ReindexingRecordsProvider provider;

	SearchServiceAcceptanceTestSchemas schema = new SearchServiceAcceptanceTestSchemas(zeCollection);
	SearchServiceAcceptanceTestSchemas.ZeSchemaMetadatas zeSchema = schema.new ZeSchemaMetadatas();
	SearchServiceAcceptanceTestSchemas.AnotherSchemaMetadatas anotherSchema = schema.new AnotherSchemaMetadatas();

	@Test
	public void whenIteratingMultipleTimeThenOnlyReturnSkippedRecords()
			throws Exception {

		ReindexingSchemaTypeRecordsProvider typeProvider = provider.newSchemaTypeProvider(schema.zeDefaultSchemaType(), 1);

		List<String> ids = iterateOverSkipping(typeProvider, "z3", "z4", "z7");
		assertThat(ids).isEqualTo(asList("z0", "z1", "z2", "z3", "z4", "z5", "z6", "z7", "z8", "z9"));
		assertThat(typeProvider.isRequiringAnotherIteration()).isTrue();
		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(3);

		ids = iterateOverSkipping(typeProvider, "z4");
		assertThat(ids).isEqualTo(asList("z3", "z4", "z7"));
		assertThat(typeProvider.isRequiringAnotherIteration()).isTrue();
		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(1);

		ids = iterateOverSkipping(typeProvider);
		assertThat(ids).isEqualTo(asList("z4"));
		assertThat(typeProvider.isRequiringAnotherIteration()).isFalse();
		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(0);

	}

	@Test
	public void givenTheSameQuantityOfRecordsAreSkippedInTwoIterationThenStopIterating()
			throws Exception {

		ReindexingSchemaTypeRecordsProvider typeProvider = provider.newSchemaTypeProvider(schema.zeDefaultSchemaType(), 1);

		iterateOverSkipping(typeProvider, "z3", "z4", "z7");

		iterateOverSkipping(typeProvider, "z4");
		assertThat(typeProvider.isRequiringAnotherIteration()).isTrue();
		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(1);

		iterateOverSkipping(typeProvider, "z4");
		assertThat(typeProvider.isRequiringAnotherIteration()).isFalse();
		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(1);

	}

	@Before
	public void setUp()
			throws Exception {

		defineSchemasManager().using(schema);
		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		Transaction tx = new Transaction();
		for (int i = 0; i < 10; i++) {
			tx.add(recordServices.newRecordWithSchema(zeSchema.instance(), "z" + i));
		}

		for (int i = 0; i < 10; i++) {
			tx.add(recordServices.newRecordWithSchema(anotherSchema.instance(), "a" + i));
		}

		recordServices.execute(tx);

		provider = new ReindexingRecordsProvider(getModelLayerFactory(), 5);
	}

	private List<String> iterateOverSkipping(ReindexingSchemaTypeRecordsProvider typeProvider, String... idsToSkip) {
		Iterator<Record> recordIterator = typeProvider.startNewSchemaTypeIteration();

		List<String> listOfIdsToSkip = asList(idsToSkip);

		List<String> ids = new ArrayList<>();
		while (recordIterator.hasNext()) {
			Record record = recordIterator.next();
			ids.add(record.getId());
			if (listOfIdsToSkip.contains(record.getId())) {
				typeProvider.markRecordAsSkipped(record);
			} else {
				typeProvider.markRecordAsHandled(record);
			}

		}

		typeProvider.markIterationAsFinished();
		return ids;
	}
}
