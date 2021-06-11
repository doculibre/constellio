package com.constellio.model.services.records.reindexing;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.reindexing.ReindexingServices.LevelReindexingContext;
import com.constellio.model.services.search.SearchServiceAcceptanceTestSchemas;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.constellio.model.services.records.reindexing.ReindexationMode.RECALCULATE_AND_REWRITE;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class ReindexingSchemaTypeRecordsProviderIntIdsAcceptanceTest extends ConstellioTest {


	SearchServiceAcceptanceTestSchemas schema = new SearchServiceAcceptanceTestSchemas(zeCollection);
	SearchServiceAcceptanceTestSchemas.ZeSchemaMetadatas zeSchema = schema.new ZeSchemaMetadatas();
	SearchServiceAcceptanceTestSchemas.AnotherSchemaMetadatas anotherSchema = schema.new AnotherSchemaMetadatas();


	@Test
	public void givenNoRecordByRecordsOnLastIterationWhenIteratingMultipleTimeThenOnlyReturnSkippedRecords()
			throws Exception {


		LevelReindexingContext context = new LevelReindexingContext(null, null, null, 0, null, new ReindexationParams(RECALCULATE_AND_REWRITE));
		ReindexingSchemaTypeRecordsProvider typeProvider = new ReindexingSchemaTypeRecordsProvider(getModelLayerFactory(), 5, context, schema.zeDefaultSchemaType(), 1);
		List<String> ids = iterateOverSkipping(typeProvider, z3Id, z4Id, z7Id);
		assertThat(ids).isEqualTo(asList(z0Id, z1Id, z2Id, z3Id, z4Id, z5Id, z6Id, z7Id, z8Id, z9Id));
		assertThat(typeProvider.isRequiringAnotherIteration()).isTrue();
		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(3);

		ids = iterateOverSkipping(typeProvider, z4Id);
		assertThat(ids).isEqualTo(asList(z3Id, z4Id, z7Id));
		assertThat(typeProvider.isRequiringAnotherIteration()).isTrue();
		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(1);

		ids = iterateOverSkipping(typeProvider, "nothing");
		assertThat(ids).isEqualTo(asList(z4Id));
		assertThat(typeProvider.isRequiringAnotherIteration()).isFalse();
		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(0);
	}

	@Test
	public void whenIteratingMultipleTimeThenOnlyReturnSkippedRecords()
			throws Exception {

		LevelReindexingContext context = new LevelReindexingContext(null, null, null, 0, null, new ReindexationParams(RECALCULATE_AND_REWRITE));
		ReindexingSchemaTypeRecordsProvider typeProvider = new ReindexingSchemaTypeRecordsProvider(getModelLayerFactory(), 5, context, schema.zeDefaultSchemaType(), 1000);

		List<String> ids = iterateOverSkipping(typeProvider, z3Id, z4Id, z7Id);
		assertThat(ids).isEqualTo(asList(z0Id, z1Id, z2Id, z3Id, z4Id, z5Id, z6Id, z7Id, z8Id, z9Id));
		assertThat(typeProvider.isRequiringAnotherIteration()).isTrue();
		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(3);

		ids = iterateOverSkipping(typeProvider, z4Id);
		assertThat(ids).containsOnly(z3Id, z4Id, z7Id);
		assertThat(typeProvider.isRequiringAnotherIteration()).isTrue();
		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(1);

		ids = iterateOverSkipping(typeProvider, "nothing");
		assertThat(ids).isEqualTo(asList(z4Id));
		assertThat(typeProvider.isRequiringAnotherIteration()).isFalse();
		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(0);
	}

	@Test
	public void givenDependenciesBetweenReindexedRecordsWhenIteratingThenStartWithRecordsDependentOfRecordsWithSuperiorIds()
			throws Exception {

		Transaction tx = new Transaction();

		tx.add(record(z3Id).set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), z4Id));
		tx.add(record(z4Id).set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), z2Id));
		tx.add(record(z5Id).set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), z1Id));
		tx.add(record(z6Id).set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), z7Id));

		getModelLayerFactory().newRecordServices().execute(tx);

		LevelReindexingContext context = new LevelReindexingContext(null, null, null, 0, null, new ReindexationParams(RECALCULATE_AND_REWRITE));
		ReindexingSchemaTypeRecordsProvider typeProvider = new ReindexingSchemaTypeRecordsProvider(getModelLayerFactory(), 5, context, schema.zeDefaultSchemaType(), 1000);

		List<String> ids = iterateOver(typeProvider);
		assertThat(ids).isEqualTo(asList(z2Id, z4Id,z1Id,z7Id));
		assertThat(typeProvider.isRequiringAnotherIteration()).isTrue();
		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(0);

		ids = iterateOver(typeProvider);
		assertThat(ids).isEqualTo(asList(z0Id, z3Id, z5Id, z6Id, z8Id, z9Id));
		assertThat(typeProvider.isRequiringAnotherIteration()).isFalse();
		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(0);

	}


	@Test
	public void whenIteratingMultipleTimeAndOnOddLevelThenAlwaysBasedOnReverseOfPreviousOrderIfSameRecordsAreSkipped()
			throws Exception {

		LevelReindexingContext context = new LevelReindexingContext(null, null, null, 0, null, new ReindexationParams(RECALCULATE_AND_REWRITE));
		ReindexingSchemaTypeRecordsProvider typeProvider = new ReindexingSchemaTypeRecordsProvider(getModelLayerFactory(), 5, context, schema.zeDefaultSchemaType(),  1000);
		List<String> ids = iterateOver(typeProvider);
		assertThat(ids).isEqualTo(asList(z0Id, z1Id, z2Id, z3Id, z4Id, z5Id, z6Id, z7Id, z8Id, z9Id));

		context = new LevelReindexingContext(null, null, null, 1, null, new ReindexationParams(RECALCULATE_AND_REWRITE));
		typeProvider = new ReindexingSchemaTypeRecordsProvider(getModelLayerFactory(), 5, context, schema.zeDefaultSchemaType(),  1000);
		ids = iterateOverSkipping(typeProvider, skipping(z8Id).after(z2Id), skipping(z8Id, z2Id).after(z1Id));
		assertThat(ids).isEqualTo(asList(z9Id, z8Id, z7Id, z6Id, z5Id, z4Id, z3Id, z2Id, z1Id, z0Id));
		assertThat(typeProvider.isRequiringAnotherIteration()).isTrue();
		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(2);

		ids = iterateOverSkipping(typeProvider, skipping(z8Id).after(z2Id));
		assertThat(ids).containsOnly(z8Id, z2Id);
		assertThat(typeProvider.isRequiringAnotherIteration()).isTrue();
		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(1);

		//		ids = iterateOver(typeProvider);
		//		assertThat(ids).isEqualTo(asList(z8Id));
		//		assertThat(typeProvider.isRequiringAnotherIteration()).isFalse();
		//		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(0);
	}

	@Test
	public void givenNoRecordByRecordsOnLastIterationWhenIteratingMultipleTimeAndOnOddLevelThenAlwaysBasedOnReverseOfPreviousOrderIfSameRecordsAreSkipped()
			throws Exception {

		LevelReindexingContext context = new LevelReindexingContext(null, null, null, 0, null, new ReindexationParams(RECALCULATE_AND_REWRITE));
		ReindexingSchemaTypeRecordsProvider typeProvider = new ReindexingSchemaTypeRecordsProvider(getModelLayerFactory(), 5, context, schema.zeDefaultSchemaType(), 1);
		List<String> ids = iterateOver(typeProvider);
		assertThat(ids).isEqualTo(asList(z0Id, z1Id, z2Id, z3Id, z4Id, z5Id, z6Id, z7Id, z8Id, z9Id));

		context = new LevelReindexingContext(null, null, null, 1, null, new ReindexationParams(RECALCULATE_AND_REWRITE));
		typeProvider = new ReindexingSchemaTypeRecordsProvider(getModelLayerFactory(), 5, context, schema.zeDefaultSchemaType(),  1);
		ids = iterateOverSkipping(typeProvider, skipping(z8Id).after(z2Id), skipping(z8Id, z2Id).after(z1Id));
		assertThat(ids).isEqualTo(asList(z9Id, z8Id, z7Id, z6Id, z5Id, z4Id, z3Id, z2Id, z1Id, z0Id));
		assertThat(typeProvider.isRequiringAnotherIteration()).isTrue();
		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(2);

		ids = iterateOverSkipping(typeProvider, skipping(z8Id).after(z2Id));
		assertThat(ids).isEqualTo(asList(z8Id, z2Id));
		assertThat(typeProvider.isRequiringAnotherIteration()).isTrue();
		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(1);

		ids = iterateOver(typeProvider);
		assertThat(ids).isEqualTo(asList(z8Id));
		assertThat(typeProvider.isRequiringAnotherIteration()).isFalse();
		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(0);
	}

	@Test
	public void givenTheSameQuantityOfRecordsAreSkippedInTwoIterationThenStopIterating()
			throws Exception {

		LevelReindexingContext context = new LevelReindexingContext(null, null, null, 0, null, new ReindexationParams(RECALCULATE_AND_REWRITE));
		ReindexingSchemaTypeRecordsProvider typeProvider = new ReindexingSchemaTypeRecordsProvider(getModelLayerFactory(), 5, context, schema.zeDefaultSchemaType(), 1000);

		iterateOverSkipping(typeProvider, z3Id, z4Id, z7Id);

		iterateOverSkipping(typeProvider, z4Id);
		assertThat(typeProvider.isRequiringAnotherIteration()).isTrue();
		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(1);

		iterateOverSkipping(typeProvider, z4Id);
		assertThat(typeProvider.isRequiringAnotherIteration()).isFalse();
		assertThat(typeProvider.getSkippedRecordsCount()).isEqualTo(1);
	}

	String z0Id, z1Id, z2Id, z3Id, z4Id, z5Id, z6Id, z7Id, z8Id, z9Id;


	@Before
	public void setUp()
			throws Exception {

		defineSchemasManager().using(schema.withAParentReferenceFromZeSchemaToZeSchema());
		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		Transaction tx = new Transaction();
		String[] ids = new String[10];
		for (int i = 0; i < 10; i++) {
			ids[i] = tx.add(recordServices.newRecordWithSchema(zeSchema.instance())).getId();
		}
		z0Id = ids[0];
		z1Id = ids[1];
		z2Id = ids[2];
		z3Id = ids[3];
		z4Id = ids[4];
		z5Id = ids[5];
		z6Id = ids[6];
		z7Id = ids[7];
		z8Id = ids[8];
		z9Id = ids[9];

		for (int i = 0; i < 10; i++) {
			tx.add(recordServices.newRecordWithSchema(anotherSchema.instance()));
		}

		recordServices.execute(tx);

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
				typeProvider.markRecordAsHandledSoon(record);
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
			typeProvider.markRecordAsHandledSoon(record);

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
			typeProvider.markRecordAsHandledSoon(record);

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
