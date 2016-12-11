package com.constellio.model.services.records;

import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.services.search.query.logical.LogicalSearchQuery.query;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.TestUtils.assertThatAllRecordsOf;
import static com.constellio.sdk.tests.TestUtils.assertThatRecords;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.ListAssert;
import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataNetworkLink;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestUtils.RecordsAssert;
import com.constellio.sdk.tests.schemas.MetadataSchemaTypesConfigurator;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ThirdSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;

public class RecordServicesAgregatedMetadatasAcceptTest extends ConstellioTest {

	TestsSchemasSetup schemas = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	AnotherSchemaMetadatas anotherSchema = schemas.new AnotherSchemaMetadatas();
	ThirdSchemaMetadatas thirdSchema = schemas.new ThirdSchemaMetadatas();
	RecordServices recordServices;
	RecordServicesAgregatedMetadatasAcceptTestRecords records = new RecordServicesAgregatedMetadatasAcceptTestRecords();
	SearchServices searchServices;

	@Before
	public void setUp()
			throws Exception {

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

	@Test
	public void givenMetadatasCreatingCyclicDependenciesOverSchemaTypesThenDividedByLevels()
			throws Exception {

		//schemas.getTypes().get
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
		records.setupInOneTransaction(schemas, getModelLayerFactory());

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
				tuple("aThirdSchemaRecord2", null, null)
		);

	}

	@Test
	public void givenAgregatedSumMetadataWhenAddingRecordsInMultipleTransactionsThenGoodSums()
			throws Exception {
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
				tuple("aThirdSchemaRecord2", null, null)
		);

	}

	@Test
	public void givenModifiedRecordNumberMetadatasThenUpdateSums()
			throws Exception {
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
				tuple("aThirdSchemaRecord2", null, null)
		);

	}

	@Test
	public void givenRemovedRecordNumberMetadatasThenUpdateSums()
			throws Exception {
		records.setupInOneTransaction(schemas, getModelLayerFactory());

		Transaction transaction = new Transaction();
		transaction.add(records.zeSchemaRecord2().set(zeSchema.metadata("number"), null));
		recordServices.execute(transaction);
		getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();

		assertThatAllRecordsOf(zeSchema).extractingMetadatas("id", "number", "pct").containsOnly(
				tuple("zeSchemaRecord1", 1.0, 0.125),
				tuple("zeSchemaRecord2", null, null),
				tuple("zeSchemaRecord3", 3.0, 0.375),
				tuple("zeSchemaRecord4", 4.0, 0.5)
		);

		assertThatAllRecordsOf(anotherSchema).extractingMetadatas("id", "sum", "sumX10", "copiedThirdSchemaTypeSum").containsOnly(
				tuple("anotherSchemaRecord1", 1.0, 10.0, 8.0),
				tuple("anotherSchemaRecord2", 7.0, 70.0, 8.0)
		);

		assertThatAllRecordsOf(thirdSchema).extractingMetadatas("id", "sum", "sumX10").containsOnly(
				tuple("aThirdSchemaRecord1", 8.0, 80.0),
				tuple("aThirdSchemaRecord2", null, null)
		);

	}

	@Test
	public void givenModifiedRecordReferenceMetadatasThenUpdateSums()
			throws Exception {
		records.setupInOneTransaction(schemas, getModelLayerFactory());

		Transaction transaction = new Transaction();
		transaction.add(records.zeSchemaRecord2().set(zeSchema.metadata("ref"), "anotherSchemaRecord2"));
		transaction.add(records.anotherSchemaRecord1().set(anotherSchema.metadata("ref"), "aThirdSchemaRecord2"));
		recordServices.execute(transaction);
		getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();

		assertThatAllRecordsOf(zeSchema).extractingMetadatas("id", "number", "pct").containsOnly(
				tuple("zeSchemaRecord1", 1.0, 0.1),
				tuple("zeSchemaRecord2", 2.0, 0.2),
				tuple("zeSchemaRecord3", 3.0, 0.3),
				tuple("zeSchemaRecord4", 4.0, 0.4)
		);

		assertThatAllRecordsOf(anotherSchema).extractingMetadatas("id", "sum", "sumX10", "copiedThirdSchemaTypeSum").containsOnly(
				tuple("anotherSchemaRecord1", 1.0, 10.0, 1.0),
				tuple("anotherSchemaRecord2", 9.0, 90.0, 9.0)
		);

		assertThatAllRecordsOf(thirdSchema).extractingMetadatas("id", "sum", "sumX10").containsOnly(
				tuple("aThirdSchemaRecord1", 9.0, 90.0),
				tuple("aThirdSchemaRecord2", 1.0, 10.0)
		);

	}

	@Test
	public void givenRemovedRecordReferenceMetadatasThenUpdateSums()
			throws Exception {
		records.setupInOneTransaction(schemas, getModelLayerFactory());

		Transaction transaction = new Transaction();
		transaction.add(records.zeSchemaRecord2().set(zeSchema.metadata("ref"), null));
		transaction.add(records.anotherSchemaRecord1().set(anotherSchema.metadata("ref"), null));
		recordServices.execute(transaction);
		getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();

		assertThatAllRecordsOf(zeSchema).extractingMetadatas("id", "number", "pct").containsOnly(
				tuple("zeSchemaRecord1", 1.0, 0.125),
				tuple("zeSchemaRecord2", 2.0, null),
				tuple("zeSchemaRecord3", 3.0, 0.375),
				tuple("zeSchemaRecord4", 4.0, 0.5)
		);

		assertThatAllRecordsOf(anotherSchema).extractingMetadatas("id", "sum", "sumX10", "copiedThirdSchemaTypeSum").containsOnly(
				tuple("anotherSchemaRecord1", 1.0, 10.0, 7.0),
				tuple("anotherSchemaRecord2", 7.0, 70.0, 7.0)
		);

		assertThatAllRecordsOf(thirdSchema).extractingMetadatas("id", "sum", "sumX10").containsOnly(
				tuple("aThirdSchemaRecord1", 8.0, 80.0),
				tuple("aThirdSchemaRecord2", null, null)
		);

		transaction = new Transaction();
		transaction.add(records.anotherSchemaRecord1().set(anotherSchema.metadata("ref"), null));
		recordServices.execute(transaction);
		getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();

		assertThatAllRecordsOf(zeSchema).extractingMetadatas("id", "number", "pct").containsOnly(
				tuple("zeSchemaRecord1", 1.0, null),
				tuple("zeSchemaRecord2", 2.0, null),
				tuple("zeSchemaRecord3", 3.0, null),
				tuple("zeSchemaRecord4", 4.0, null)
		);

		assertThatAllRecordsOf(anotherSchema).extractingMetadatas("id", "sum", "sumX10", "copiedThirdSchemaTypeSum").containsOnly(
				tuple("anotherSchemaRecord1", 1.0, 10.0, null),
				tuple("anotherSchemaRecord2", 7.0, 70.0, null)
		);

		assertThatAllRecordsOf(thirdSchema).extractingMetadatas("id", "sum", "sumX10").containsOnly(
				tuple("aThirdSchemaRecord1", null, null),
				tuple("aThirdSchemaRecord2", null, null)
		);

	}

	private List<Tuple> getNetworkLinks() {

		List<Tuple> tuples = new ArrayList();

		for (MetadataNetworkLink link : schemas.getTypes().getMetadataNetwork().getLinks()) {

			if (!link.getToMetadata().isGlobal()
					&& !link.getFromMetadata().isGlobal()
					&& !link.getFromMetadata().getCode().startsWith("user_")
					&& !link.getFromMetadata().getCode().startsWith("user_")) {
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
