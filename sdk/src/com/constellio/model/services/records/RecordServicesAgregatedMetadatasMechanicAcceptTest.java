package com.constellio.model.services.records;

import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.sdk.tests.TestUtils.assertThatAllRecordsOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.groups.Tuple;
import org.junit.Test;

import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataNetworkLink;
import com.constellio.model.services.records.reindexing.ReindexationMode;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.MetadataSchemaTypesConfigurator;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ThirdSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;

public class RecordServicesAgregatedMetadatasMechanicAcceptTest extends ConstellioTest {

	TestsSchemasSetup schemas = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	AnotherSchemaMetadatas anotherSchema = schemas.new AnotherSchemaMetadatas();
	ThirdSchemaMetadatas thirdSchema = schemas.new ThirdSchemaMetadatas();
	RecordServices recordServices;
	RecordServicesAgregatedMetadatasAcceptTestRecords records = new RecordServicesAgregatedMetadatasAcceptTestRecords();
	SearchServices searchServices;

	public void setUpWithAgregatedSumMetadatas()
			throws Exception {
		givenBackgroundThreadsEnabled();
		defineSchemasManager().using(schemas.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder zeType = schemaTypes.getSchemaType(zeSchema.typeCode());
				MetadataSchemaTypeBuilder anotherType = schemaTypes.getSchemaType(anotherSchema.typeCode());
				MetadataSchemaTypeBuilder thirdType = schemaTypes.getSchemaType(thirdSchema.typeCode());

				MetadataBuilder zeNumber = zeType.getDefaultSchema().create("number").setType(NUMBER);
				MetadataBuilder zeRef = zeType.getDefaultSchema().create("ref").defineReferencesTo(anotherType);
				MetadataBuilder zeRefText = zeType.getDefaultSchema().create("refText");
				MetadataBuilder pctRef = zeType.getDefaultSchema().create("pct").setType(NUMBER)
						.setIncreasedDependencyLevel(true).defineDataEntry().asJexlScript(
								"if (ref.copiedThirdSchemaTypeSum > 0) {number / ref.copiedThirdSchemaTypeSum} else {0}");

				MetadataBuilder anotherSchemaSum = anotherType.getDefaultSchema().create("sum")
						.defineDataEntry().asSum(zeRef, zeNumber);
				MetadataBuilder anotherSchemaSumX10 = anotherType.getDefaultSchema().create("sumX10").setType(NUMBER)
						.defineDataEntry().asJexlScript("sum * 10");
				MetadataBuilder copiedThirdSchemaTypeSum = anotherType.getDefaultSchema().create("copiedThirdSchemaTypeSum");
				MetadataBuilder anotherSchemaRef = anotherType.getDefaultSchema().create("ref").defineReferencesTo(thirdType);
				MetadataBuilder anotherSchemaText = anotherType.getDefaultSchema().create("text").setType(STRING);

				MetadataBuilder thirdSchemaSum = thirdType.getDefaultSchema().create("sum")
						.defineDataEntry().asSum(anotherSchemaRef, anotherSchemaSum);
				MetadataBuilder thirdSchemaSumX10 = thirdType.getDefaultSchema().create("sumX10")
						.defineDataEntry().asSum(anotherSchemaRef, anotherSchemaSumX10);

				copiedThirdSchemaTypeSum.setType(NUMBER).defineDataEntry().asCopied(anotherSchemaRef, thirdSchemaSum);
				zeRefText.setType(STRING).defineDataEntry().asCopied(zeRef, anotherSchemaText);

			}
		}));
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();

	}

	public void setUpWithAgregatedReferenceCountMetadatas()
			throws Exception {
		givenBackgroundThreadsEnabled();
		defineSchemasManager().using(schemas.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder zeType = schemaTypes.getSchemaType(zeSchema.typeCode());
				MetadataSchemaTypeBuilder anotherType = schemaTypes.getSchemaType(anotherSchema.typeCode());
				MetadataSchemaTypeBuilder thirdType = schemaTypes.getSchemaType(thirdSchema.typeCode());

				MetadataBuilder zeRef = zeType.getDefaultSchema().create("ref").defineReferencesTo(anotherType);

				MetadataBuilder anotherSchemaRefCount = anotherType.getDefaultSchema().create("refCount").setType(NUMBER)
						.defineDataEntry().asReferenceCount(zeRef);
			}
		}));
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();

	}

	@Test
	public void givenMetadatasCreatingCyclicDependenciesOverSchemaTypesThenDividedByLevels()
			throws Exception {

		setUpWithAgregatedSumMetadatas();

		assertThat(getNetworkLinks()).containsOnly(
				tuple("aThirdSchemaType_default_sum", "anotherSchemaType_default_ref", 1),
				tuple("aThirdSchemaType_default_sum", "anotherSchemaType_default_sum", 1),
				tuple("aThirdSchemaType_default_sumX10", "anotherSchemaType_default_ref", 1),
				tuple("aThirdSchemaType_default_sumX10", "anotherSchemaType_default_sumX10", 1),
				tuple("anotherSchemaType_default_copiedThirdSchemaTypeSum", "aThirdSchemaType_default_sum", 1),
				tuple("anotherSchemaType_default_copiedThirdSchemaTypeSum", "anotherSchemaType_default_ref", 1),
				tuple("anotherSchemaType_default_sum", "zeSchemaType_default_ref", 1),
				tuple("anotherSchemaType_default_sum", "zeSchemaType_default_number", 1),
				tuple("anotherSchemaType_default_sumX10", "anotherSchemaType_default_sum", 1),
				tuple("zeSchemaType_default_pct", "zeSchemaType_default_ref", 2),
				tuple("zeSchemaType_default_pct", "zeSchemaType_default_number", 2),
				tuple("zeSchemaType_default_pct", "anotherSchemaType_default_copiedThirdSchemaTypeSum", 2),
				tuple("zeSchemaType_default_refText", "anotherSchemaType_default_text", 0),
				tuple("zeSchemaType_default_refText", "zeSchemaType_default_ref", 0)
		);

	}

	@Test
	public void givenAgregatedSumMetadataWhenAddingRecordsInSameTransactionThenGoodSums()
			throws Exception {
		setUpWithAgregatedSumMetadatas();

		records.setupInOneTransaction(schemas, getModelLayerFactory());

		assertThatAllRecordsOf(thirdSchema).extractingMetadatas("id", "sum", "sumX10").containsOnly(
				tuple("aThirdSchemaRecord1", 10.0, 100.0),
				tuple("aThirdSchemaRecord2", 0.0, 0.0)
		);

		assertThatAllRecordsOf(anotherSchema).extractingMetadatas("id", "sum", "sumX10", "copiedThirdSchemaTypeSum").containsOnly(
				tuple("anotherSchemaRecord1", 3.0, 30.0, 10.0),
				tuple("anotherSchemaRecord2", 7.0, 70.0, 10.0)
		);

		assertThatAllRecordsOf(zeSchema).extractingMetadatas("id", "number", "pct").containsOnly(
				tuple("zeSchemaRecord1", 1.0, 0.1),
				tuple("zeSchemaRecord2", 2.0, 0.2),
				tuple("zeSchemaRecord3", 3.0, 0.3),
				tuple("zeSchemaRecord4", 4.0, 0.4)
		);

	}

	@Test
	public void givenAgregatedSumMetadataWhenReindexingThenGoodValues()
			throws Exception {
		setUpWithAgregatedSumMetadatas();
		records.setupWithNothingComputed(schemas, getModelLayerFactory());

		assertThatAllRecordsOf(anotherSchema).extractingMetadatas("id", "sum", "sumX10", "copiedThirdSchemaTypeSum").containsOnly(
				tuple("anotherSchemaRecord1", null, null, null),
				tuple("anotherSchemaRecord2", null, null, null)
		);

		assertThatAllRecordsOf(thirdSchema).extractingMetadatas("id", "sum", "sumX10").containsOnly(
				tuple("aThirdSchemaRecord1", null, null),
				tuple("aThirdSchemaRecord2", null, null)
		);

		assertThatAllRecordsOf(zeSchema).extractingMetadatas("id", "number", "pct").containsOnly(
				tuple("zeSchemaRecord1", 1.0, null),
				tuple("zeSchemaRecord2", 2.0, null),
				tuple("zeSchemaRecord3", 3.0, null),
				tuple("zeSchemaRecord4", 4.0, null)
		);

		ReindexingServices reindexingServices = new ReindexingServices(getModelLayerFactory());
		reindexingServices.reindexCollections(ReindexationMode.RECALCULATE_AND_REWRITE);
		//waitForBatchProcess();

		assertThatAllRecordsOf(anotherSchema).extractingMetadatas("id", "sum", "sumX10", "copiedThirdSchemaTypeSum").containsOnly(
				tuple("anotherSchemaRecord1", 3.0, 30.0, 10.0),
				tuple("anotherSchemaRecord2", 7.0, 70.0, 10.0)
		);

		assertThatAllRecordsOf(thirdSchema).extractingMetadatas("id", "sum", "sumX10").containsOnly(
				tuple("aThirdSchemaRecord1", 10.0, 100.0),
				tuple("aThirdSchemaRecord2", 0.0, 0.0)
		);

		assertThatAllRecordsOf(zeSchema).extractingMetadatas("id", "number", "pct").containsOnly(
				tuple("zeSchemaRecord1", 1.0, 0.1),
				tuple("zeSchemaRecord2", 2.0, 0.2),
				tuple("zeSchemaRecord3", 3.0, 0.3),
				tuple("zeSchemaRecord4", 4.0, 0.4)
		);

	}

	@Test
	public void givenAgregatedSumMetadataWhenAddingRecordsInMultipleTransactionsThenGoodSums()
			throws Exception {
		setUpWithAgregatedSumMetadatas();

		records.setupInMultipleTransaction(schemas, getModelLayerFactory());

		assertThatAllRecordsOf(zeSchema).extractingMetadatas("id", "number", "pct").containsOnly(
				tuple("zeSchemaRecord1", 1.0, 0.1),
				tuple("zeSchemaRecord2", 2.0, 0.2),
				tuple("zeSchemaRecord3", 3.0, 0.3),
				tuple("zeSchemaRecord4", 4.0, 0.4)
		);

		assertThatAllRecordsOf(anotherSchema).extractingMetadatas("id", "sum", "sumX10", "copiedThirdSchemaTypeSum").containsOnly(
				tuple("anotherSchemaRecord1", 3.0, 30.0, 10.0),
				tuple("anotherSchemaRecord2", 7.0, 70.0, 10.0)
		);

		assertThatAllRecordsOf(thirdSchema).extractingMetadatas("id", "sum", "sumX10").containsOnly(
				tuple("aThirdSchemaRecord1", 10.0, 100.0),
				tuple("aThirdSchemaRecord2", 0.0, 0.0)
		);

	}

	@Test
	public void givenModifiedRecordNumberMetadatasThenUpdateSums()
			throws Exception {
		setUpWithAgregatedSumMetadatas();

		records.setupInOneTransaction(schemas, getModelLayerFactory());

		Transaction transaction = new Transaction();
		transaction.add(records.zeSchemaRecord1().set(zeSchema.metadata("number"), 11.0));
		recordServices.execute(transaction);
		getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();

		assertThatAllRecordsOf(zeSchema).extractingMetadatas("id", "number", "pct").containsOnly(
				tuple("zeSchemaRecord1", 11.0, 0.55),
				tuple("zeSchemaRecord2", 2.0, 0.1),
				tuple("zeSchemaRecord3", 3.0, 0.15),
				tuple("zeSchemaRecord4", 4.0, 0.2)
		);

		assertThatAllRecordsOf(anotherSchema).extractingMetadatas("id", "sum", "sumX10", "copiedThirdSchemaTypeSum").containsOnly(
				tuple("anotherSchemaRecord1", 13.0, 130.0, 20.0),
				tuple("anotherSchemaRecord2", 7.0, 70.0, 20.0)
		);

		assertThatAllRecordsOf(thirdSchema).extractingMetadatas("id", "sum", "sumX10").containsOnly(
				tuple("aThirdSchemaRecord1", 20.0, 200.0),
				tuple("aThirdSchemaRecord2", 0.0, 0.0)
		);

	}

	@Test
	public void givenRemovedRecordNumberMetadatasThenUpdateSums()
			throws Exception {
		setUpWithAgregatedSumMetadatas();

		records.setupInOneTransaction(schemas, getModelLayerFactory());

		Transaction transaction = new Transaction();
		transaction.add(records.zeSchemaRecord2().set(zeSchema.metadata("number"), null));
		recordServices.execute(transaction);
		getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();

		assertThatAllRecordsOf(zeSchema).extractingMetadatas("id", "number", "pct").containsOnly(
				tuple("zeSchemaRecord1", 1.0, 0.125),
				tuple("zeSchemaRecord2", null, 0.0),
				tuple("zeSchemaRecord3", 3.0, 0.375),
				tuple("zeSchemaRecord4", 4.0, 0.5)
		);

		assertThatAllRecordsOf(anotherSchema).extractingMetadatas("id", "sum", "sumX10", "copiedThirdSchemaTypeSum").containsOnly(
				tuple("anotherSchemaRecord1", 1.0, 10.0, 8.0),
				tuple("anotherSchemaRecord2", 7.0, 70.0, 8.0)
		);

		assertThatAllRecordsOf(thirdSchema).extractingMetadatas("id", "sum", "sumX10").containsOnly(
				tuple("aThirdSchemaRecord1", 8.0, 80.0),
				tuple("aThirdSchemaRecord2", 0.0, 0.0)
		);

	}

	@Test
	public void givenModifiedRecordReferenceMetadatasThenUpdateSums()
			throws Exception {
		setUpWithAgregatedSumMetadatas();

		records.setupInOneTransaction(schemas, getModelLayerFactory());

		Transaction transaction = new Transaction();
		transaction.add(records.zeSchemaRecord2().set(zeSchema.metadata("ref"), "anotherSchemaRecord2"));
		transaction.add(records.anotherSchemaRecord1().set(anotherSchema.metadata("ref"), "aThirdSchemaRecord2"));
		recordServices.execute(transaction);
		getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();

		assertThatAllRecordsOf(anotherSchema).extractingMetadatas("id", "sum", "sumX10", "copiedThirdSchemaTypeSum").containsOnly(
				tuple("anotherSchemaRecord1", 1.0, 10.0, 1.0),
				tuple("anotherSchemaRecord2", 9.0, 90.0, 9.0)
		);

		assertThatAllRecordsOf(thirdSchema).extractingMetadatas("id", "sum", "sumX10").containsOnly(
				tuple("aThirdSchemaRecord1", 9.0, 90.0),
				tuple("aThirdSchemaRecord2", 1.0, 10.0)
		);

		assertThatAllRecordsOf(zeSchema).extractingMetadatas("id", "number", "pct").containsOnly(
				tuple("zeSchemaRecord1", 1.0, 1.0),
				tuple("zeSchemaRecord2", 2.0, 0.2222222222222222),
				tuple("zeSchemaRecord3", 3.0, 0.3333333333333333),
				tuple("zeSchemaRecord4", 4.0, 0.4444444444444444)
		);

	}

	@Test
	public void givenRemovedRecordReferenceMetadatasThenUpdateSums()
			throws Exception {
		setUpWithAgregatedSumMetadatas();

		records.setupInOneTransaction(schemas, getModelLayerFactory());

		Transaction transaction = new Transaction();
		transaction.add(records.zeSchemaRecord2().set(zeSchema.metadata("ref"), null));
		recordServices.execute(transaction);
		getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();

		assertThatAllRecordsOf(anotherSchema).extractingMetadatas("id", "sum", "sumX10", "copiedThirdSchemaTypeSum").containsOnly(
				tuple("anotherSchemaRecord1", 1.0, 10.0, 8.0),
				tuple("anotherSchemaRecord2", 7.0, 70.0, 8.0)
		);

		assertThatAllRecordsOf(thirdSchema).extractingMetadatas("id", "sum", "sumX10").containsOnly(
				tuple("aThirdSchemaRecord1", 8.0, 80.0),
				tuple("aThirdSchemaRecord2", 0.0, 0.0)
		);

		assertThatAllRecordsOf(zeSchema).extractingMetadatas("id", "number", "pct").containsOnly(
				tuple("zeSchemaRecord1", 1.0, 0.125),
				tuple("zeSchemaRecord2", 2.0, 0.0),
				tuple("zeSchemaRecord3", 3.0, 0.375),
				tuple("zeSchemaRecord4", 4.0, 0.5)
		);

		transaction = new Transaction();
		transaction.add(records.anotherSchemaRecord1().set(anotherSchema.metadata("ref"), null));
		recordServices.execute(transaction);
		getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();

		assertThatAllRecordsOf(zeSchema).extractingMetadatas("id", "number", "pct").containsOnly(
				tuple("zeSchemaRecord1", 1.0, 0.0),
				tuple("zeSchemaRecord2", 2.0, 0.0),
				tuple("zeSchemaRecord3", 3.0, 0.42857142857142855),
				tuple("zeSchemaRecord4", 4.0, 0.5714285714285714)
		);

		assertThatAllRecordsOf(anotherSchema).extractingMetadatas("id", "sum", "sumX10", "copiedThirdSchemaTypeSum").containsOnly(
				tuple("anotherSchemaRecord1", 1.0, 10.0, null),
				tuple("anotherSchemaRecord2", 7.0, 70.0, 7.0)
		);

		assertThatAllRecordsOf(thirdSchema).extractingMetadatas("id", "sum", "sumX10").containsOnly(
				tuple("aThirdSchemaRecord1", 7.0, 70.0),
				tuple("aThirdSchemaRecord2", 0.0, 0.0)
		);

		transaction = new Transaction();
		transaction.add(records.anotherSchemaRecord2().set(anotherSchema.metadata("ref"), null));
		recordServices.execute(transaction);
		getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();

		assertThatAllRecordsOf(zeSchema).extractingMetadatas("id", "number", "pct").containsOnly(
				tuple("zeSchemaRecord1", 1.0, 0.0),
				tuple("zeSchemaRecord2", 2.0, 0.0),
				tuple("zeSchemaRecord3", 3.0, 0.0),
				tuple("zeSchemaRecord4", 4.0, 0.0)
		);

		assertThatAllRecordsOf(anotherSchema).extractingMetadatas("id", "sum", "sumX10", "copiedThirdSchemaTypeSum").containsOnly(
				tuple("anotherSchemaRecord1", 1.0, 10.0, null),
				tuple("anotherSchemaRecord2", 7.0, 70.0, null)
		);

		assertThatAllRecordsOf(thirdSchema).extractingMetadatas("id", "sum", "sumX10").containsOnly(
				tuple("aThirdSchemaRecord1", 0.0, 0.0),
				tuple("aThirdSchemaRecord2", 0.0, 0.0)
		);

	}

	@Test
	public void givenDeletedRecordThenUpdateSums()
			throws Exception {
		setUpWithAgregatedSumMetadatas();

		records.setupInOneTransaction(schemas, getModelLayerFactory());

		Transaction transaction = new Transaction();
		transaction.add(records.zeSchemaRecord2().set(zeSchema.metadata("ref"), null));
		recordServices.execute(transaction);
		getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();

		assertThatAllRecordsOf(anotherSchema).extractingMetadatas("id", "sum", "sumX10", "copiedThirdSchemaTypeSum").containsOnly(
				tuple("anotherSchemaRecord1", 1.0, 10.0, 8.0),
				tuple("anotherSchemaRecord2", 7.0, 70.0, 8.0)
		);

		assertThatAllRecordsOf(thirdSchema).extractingMetadatas("id", "sum", "sumX10").containsOnly(
				tuple("aThirdSchemaRecord1", 8.0, 80.0),
				tuple("aThirdSchemaRecord2", 0.0, 0.0)
		);

		assertThatAllRecordsOf(zeSchema).extractingMetadatas("id", "number", "pct").containsOnly(
				tuple("zeSchemaRecord1", 1.0, 0.125),
				tuple("zeSchemaRecord2", 2.0, 0.0),
				tuple("zeSchemaRecord3", 3.0, 0.375),
				tuple("zeSchemaRecord4", 4.0, 0.5)
		);

		recordServices.logicallyDelete(records.zeSchemaRecord1(), User.GOD);
		recordServices.physicallyDelete(records.zeSchemaRecord1(), User.GOD);
		getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();

		assertThatAllRecordsOf(zeSchema).extractingMetadatas("id", "number", "pct").containsOnly(
				tuple("zeSchemaRecord2", 2.0, 0.0),
				tuple("zeSchemaRecord3", 3.0, 0.42857142857142855),
				tuple("zeSchemaRecord4", 4.0, 0.5714285714285714)
		);

		assertThatAllRecordsOf(anotherSchema).extractingMetadatas("id", "sum", "sumX10", "copiedThirdSchemaTypeSum").containsOnly(
				tuple("anotherSchemaRecord1", 0.0, 00.0, 7.0),
				tuple("anotherSchemaRecord2", 7.0, 70.0, 7.0)
		);

		assertThatAllRecordsOf(thirdSchema).extractingMetadatas("id", "sum", "sumX10").containsOnly(
				tuple("aThirdSchemaRecord1", 7.0, 70.0),
				tuple("aThirdSchemaRecord2", 0.0, 0.0)
		);

	}

	@Test
	public void givenAgregatedReferenceCountWhenAddOrRemoveRecordsThenAgregatedMetadataUpdated()
			throws Exception {
		setUpWithAgregatedReferenceCountMetadatas();
		records.setupEmpty(schemas, getModelLayerFactory());

		tx = new Transaction();
		tx.add(new TestRecord(zeSchema, "zeSchemaRecord1").set("ref", "anotherSchemaRecord1"));
		tx.add(new TestRecord(zeSchema, "zeSchemaRecord2").set("ref", "anotherSchemaRecord2"));
		tx.add(new TestRecord(zeSchema, "zeSchemaRecord3").set("ref", "anotherSchemaRecord2"));
		tx.add(new TestRecord(zeSchema, "zeSchemaRecord4").set("ref", "anotherSchemaRecord2"));
		tx.add(new TestRecord(anotherSchema, "anotherSchemaRecord1"));
		tx.add(new TestRecord(anotherSchema, "anotherSchemaRecord2"));
		executeAndWait(tx);

		assertThatAllRecordsOf(anotherSchema).extractingMetadatas("id", "refCount").containsOnly(
				tuple("anotherSchemaRecord1", 1.0),
				tuple("anotherSchemaRecord2", 3.0)
		);

		recordServices.logicallyDelete(records.zeSchemaRecord4(), User.GOD);
		recordServices.physicallyDelete(records.zeSchemaRecord4(), User.GOD);
		waitForAgregatedMetadatasCalculation();

		assertThatAllRecordsOf(anotherSchema).extractingMetadatas("id", "refCount").containsOnly(
				tuple("anotherSchemaRecord1", 1.0),
				tuple("anotherSchemaRecord2", 2.0)
		);

		recordServices.update(records.zeSchemaRecord2().set(zeSchema.metadata("ref"), "anotherSchemaRecord1"));
		waitForAgregatedMetadatasCalculation();

		assertThatAllRecordsOf(anotherSchema).extractingMetadatas("id", "refCount").containsOnly(
				tuple("anotherSchemaRecord1", 2.0),
				tuple("anotherSchemaRecord2", 1.0)
		);

		recordServices.add(new TestRecord(zeSchema).set(zeSchema.metadata("ref"), "anotherSchemaRecord1"));
		waitForAgregatedMetadatasCalculation();

		assertThatAllRecordsOf(anotherSchema).extractingMetadatas("id", "refCount").containsOnly(
				tuple("anotherSchemaRecord1", 3.0),
				tuple("anotherSchemaRecord2", 1.0)
		);
	}

	private void executeAndWait(Transaction tx)
			throws RecordServicesException {
		recordServices.execute(tx);
		waitForAgregatedMetadatasCalculation();
	}

	private void waitForAgregatedMetadatasCalculation() {
		getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();
	}

	private List<Tuple> getNetworkLinks() {

		List<Tuple> tuples = new ArrayList();

		for (MetadataNetworkLink link : schemas.getTypes().getMetadataNetwork().getLinks()) {

			if (!link.getToMetadata().isGlobal()
					&& !link.getFromMetadata().isGlobal()
					&& !link.getFromMetadata().getCode().startsWith("user_")
					&& !link.getFromMetadata().getCode().startsWith("user_")
					&& !link.getFromMetadata().getCode().startsWith("temporaryRecord_")) {
				Tuple tuple = new Tuple();
				tuple.addData(link.getFromMetadata().getCode());
				tuple.addData(link.getToMetadata().getCode());
				tuple.addData(link.getLevel());
				tuples.add(tuple);
			}

		}

		return tuples;
	}

}
