package com.constellio.model.services.records.reindexing;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServiceAcceptanceTestSchemas;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class ReindexingSchemaTypeRecordsProviderAcceptanceTest extends ConstellioTest {

	ReindexingRecordsProvider provider;

	SearchServiceAcceptanceTestSchemas schema = new SearchServiceAcceptanceTestSchemas(zeCollection);
	SearchServiceAcceptanceTestSchemas.ZeSchemaMetadatas zeSchema = schema.new ZeSchemaMetadatas();
	SearchServiceAcceptanceTestSchemas.AnotherSchemaMetadatas anotherSchema = schema.new AnotherSchemaMetadatas();

	@Test
	public void givenNoRecordByRecordsOnLastIterationWhenIteratingMultipleTimeThenOnlyReturnSkippedRecords()
			throws Exception {


		ReindexingSchemaTypeRecordsProvider typeProvider = provider.newSchemaTypeProvider(schema.zeDefaultSchemaType(), 0, 1);
		typeProvider.selfParentReference = true;
		List<String> ids = iterateOverSkipping(typeProvider, "z3", "z4", "z7");
		assertThat(ids).isEqualTo(asList("z0", "z1", "z2", "z3", "z4", "z5", "z6", "z7", "z8", "z9"));
		assertThat(typeProvider.isRequiringAnotherIteration()).isTrue();
		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(3);

		ids = iterateOverSkipping(typeProvider, "z4");
		assertThat(ids).isEqualTo(asList("z3", "z4", "z7"));
		assertThat(typeProvider.isRequiringAnotherIteration()).isTrue();
		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(1);

		ids = iterateOverSkipping(typeProvider, "nothing");
		assertThat(ids).isEqualTo(asList("z4"));
		assertThat(typeProvider.isRequiringAnotherIteration()).isFalse();
		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(0);
	}

	@Test
	public void whenIteratingMultipleTimeThenOnlyReturnSkippedRecords()
			throws Exception {

		ReindexingSchemaTypeRecordsProvider typeProvider = provider.newSchemaTypeProvider(schema.zeDefaultSchemaType(), 0, 1000);

		List<String> ids = iterateOverSkipping(typeProvider, "z3", "z4", "z7");
		assertThat(ids).isEqualTo(asList("z0", "z1", "z2", "z3", "z4", "z5", "z6", "z7", "z8", "z9"));
		assertThat(typeProvider.isRequiringAnotherIteration()).isTrue();
		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(3);

		ids = iterateOverSkipping(typeProvider, "z4");
		assertThat(ids).containsOnly("z3", "z4", "z7");
		assertThat(typeProvider.isRequiringAnotherIteration()).isTrue();
		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(1);

		ids = iterateOverSkipping(typeProvider, "nothing");
		assertThat(ids).isEqualTo(asList("z4"));
		assertThat(typeProvider.isRequiringAnotherIteration()).isFalse();
		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(0);
	}
	//
	//	@Test
	//	public void whenIteratingMultipleTimeAndOnMultipleLevelsThenAlwaysBasedOnReverseOfPreviousOrderIfSameRecordsAreSkipped()
	//			throws Exception {
	//
	//		ReindexingSchemaTypeRecordsProvider typeProvider = provider.newSchemaTypeProvider(schema.zeDefaultSchemaType(), 0);
	//
	//		List<String> ids = iterateOverSkipping(typeProvider, "z3", "z4", "z7");
	//		assertThat(ids).isEqualTo(asList("z0", "z1", "z2", "z3", "z4", "z5", "z6", "z7", "z8", "z9"));
	//		assertThat(typeProvider.isRequiringAnotherIteration()).isTrue();
	//		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(3);
	//
	//		ids = iterateOverSkipping(typeProvider, "z4");
	//		assertThat(ids).isEqualTo(asList("z3", "z4", "z7"));
	//		assertThat(typeProvider.isRequiringAnotherIteration()).isTrue();
	//		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(1);
	//
	//		ids = iterateOverSkipping(typeProvider, "nothing");
	//		assertThat(ids).isEqualTo(asList("z4"));
	//		assertThat(typeProvider.isRequiringAnotherIteration()).isFalse();
	//		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(0);
	//
	//		//Iterating over the same schematype, a dependency level above
	//		typeProvider = provider.newSchemaTypeProvider(schema.zeDefaultSchemaType(), 1);
	//
	//		ids = iterateOver(typeProvider);
	//		assertThat(ids).isEqualTo(asList("z4"));
	//		assertThat(typeProvider.isRequiringAnotherIteration()).isTrue();
	//		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(3);
	//
	//		ids = iterateOver(typeProvider);
	//		assertThat(ids).isEqualTo(asList("z7", "z3"));
	//		assertThat(typeProvider.isRequiringAnotherIteration()).isTrue();
	//		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(1);
	//
	//		ids = iterateOver(typeProvider);
	//		assertThat(ids).isEqualTo(asList("z9", "z8", "z6", "z5", "z2", "z1", "z0"));
	//		assertThat(typeProvider.isRequiringAnotherIteration()).isFalse();
	//		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(0);
	//
	//		typeProvider = provider.newSchemaTypeProvider(schema.zeDefaultSchemaType(), 2);
	//
	//		ids = iterateOver(typeProvider);
	//		assertThat(ids).isEqualTo(asList("z0", "z1", "z2", "z5", "z6", "z8", "z9"));
	//		assertThat(typeProvider.isRequiringAnotherIteration()).isTrue();
	//		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(3);
	//
	//		ids = iterateOver(typeProvider);
	//		assertThat(ids).isEqualTo(asList("z3", "z7"));
	//		assertThat(typeProvider.isRequiringAnotherIteration()).isTrue();
	//		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(1);
	//
	//		ids = iterateOver(typeProvider);
	//		assertThat(ids).isEqualTo(asList("z4"));
	//		assertThat(typeProvider.isRequiringAnotherIteration()).isFalse();
	//		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(0);
	//
	//	}

	@Test
	public void whenIteratingMultipleTimeAndOnOddLevelThenAlwaysBasedOnReverseOfPreviousOrderIfSameRecordsAreSkipped()
			throws Exception {

		ReindexingSchemaTypeRecordsProvider typeProvider = provider.newSchemaTypeProvider(schema.zeDefaultSchemaType(), 0, 1000);
		List<String> ids = iterateOver(typeProvider);
		assertThat(ids).isEqualTo(asList("z0", "z1", "z2", "z3", "z4", "z5", "z6", "z7", "z8", "z9"));

		typeProvider = provider.newSchemaTypeProvider(schema.zeDefaultSchemaType(), 1, 1000);
		ids = iterateOverSkipping(typeProvider, skipping("z8").after("z2"), skipping("z8", "z2").after("z1"));
		assertThat(ids).isEqualTo(asList("z9", "z8", "z7", "z6", "z5", "z4", "z3", "z2", "z1", "z0"));
		assertThat(typeProvider.isRequiringAnotherIteration()).isTrue();
		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(2);

		ids = iterateOverSkipping(typeProvider, skipping("z8").after("z2"));
		assertThat(ids).containsOnly("z8", "z2");
		assertThat(typeProvider.isRequiringAnotherIteration()).isTrue();
		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(1);

		//		ids = iterateOver(typeProvider);
		//		assertThat(ids).isEqualTo(asList("z8"));
		//		assertThat(typeProvider.isRequiringAnotherIteration()).isFalse();
		//		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(0);
	}

	@Test
	public void givenNoRecordByRecordsOnLastIterationWhenIteratingMultipleTimeAndOnOddLevelThenAlwaysBasedOnReverseOfPreviousOrderIfSameRecordsAreSkipped()
			throws Exception {

		ReindexingSchemaTypeRecordsProvider typeProvider = provider.newSchemaTypeProvider(schema.zeDefaultSchemaType(), 0, 1);
		typeProvider.selfParentReference = true;
		List<String> ids = iterateOver(typeProvider);
		assertThat(ids).isEqualTo(asList("z0", "z1", "z2", "z3", "z4", "z5", "z6", "z7", "z8", "z9"));

		typeProvider = provider.newSchemaTypeProvider(schema.zeDefaultSchemaType(), 1, 1);
		typeProvider.selfParentReference = true;
		ids = iterateOverSkipping(typeProvider, skipping("z8").after("z2"), skipping("z8", "z2").after("z1"));
		assertThat(ids).isEqualTo(asList("z9", "z8", "z7", "z6", "z5", "z4", "z3", "z2", "z1", "z0"));
		assertThat(typeProvider.isRequiringAnotherIteration()).isTrue();
		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(2);

		ids = iterateOverSkipping(typeProvider, skipping("z8").after("z2"));
		assertThat(ids).isEqualTo(asList("z8", "z2"));
		assertThat(typeProvider.isRequiringAnotherIteration()).isTrue();
		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(1);

		ids = iterateOver(typeProvider);
		assertThat(ids).isEqualTo(asList("z8"));
		assertThat(typeProvider.isRequiringAnotherIteration()).isFalse();
		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(0);
	}

	@Test
	public void givenTheSameQuantityOfRecordsAreSkippedInTwoIterationThenStopIterating()
			throws Exception {

		ReindexingSchemaTypeRecordsProvider typeProvider = provider.newSchemaTypeProvider(schema.zeDefaultSchemaType(), 0, 1000);

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
				typeProvider.markRecordAsSkipped(record.getId());
			} else {
				typeProvider.markRecordAsHandled(record);
			}

		}

		typeProvider.markIterationAsFinished();
		return ids;
	}

	private List<String> iterateOver(ReindexingSchemaTypeRecordsProvider typeProvider) {
		Iterator<Record> recordIterator = typeProvider.startNewSchemaTypeIteration();

		List<String> ids = new ArrayList<>();
		while (recordIterator.hasNext()) {
			Record record = recordIterator.next();
			ids.add(record.getId());
			typeProvider.markRecordAsHandled(record);

		}

		typeProvider.markIterationAsFinished();
		return ids;
	}

	private List<String> iterateOverSkipping(ReindexingSchemaTypeRecordsProvider typeProvider,
											 SkippingInstruction... skipped) {
		Iterator<Record> recordIterator = typeProvider.startNewSchemaTypeIteration();

		List<String> ids = new ArrayList<>();
		while (recordIterator.hasNext()) {
			Record record = recordIterator.next();
			ids.add(record.getId());
			typeProvider.markRecordAsHandled(record);

			for (SkippingInstruction skippingInstruction : skipped) {
				if (skippingInstruction.afterId.equals(record.getId())) {
					for (String skippedId : skippingInstruction.skippedIds) {
						typeProvider.markRecordAsSkipped(skippedId);
					}
				}
			}

		}

		typeProvider.markIterationAsFinished();
		return ids;
	}

	private static SkippingInstruction skipping(String... records) {
		return new SkippingInstruction(asList(records));
	}

	private static class SkippingInstruction {
		List<String> skippedIds = new ArrayList<>();

		String afterId;

		public SkippingInstruction(List<String> skippedIds) {
			this.skippedIds = skippedIds;
			this.afterId = afterId;
		}

		public SkippingInstruction after(String recordId) {
			this.afterId = recordId;
			return this;
		}
	}
}
