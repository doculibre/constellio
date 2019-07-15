package com.constellio.model.services.records;

import com.constellio.data.dao.services.bigVault.solr.BigVaultException;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.model.entities.enums.MemoryConsumptionLevel;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.cache2.RecordsCache2IntegrityDiagnosticService;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.QueryCounter;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.MetadataSchemaTypesConfigurator;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ThirdSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import com.constellio.sdk.tests.setups.SchemaShortcuts;
import org.apache.solr.common.SolrInputDocument;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.data.dao.dto.records.RecordsFlushing.NOW;
import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.services.records.reindexing.ReindexationMode.RECALCULATE_AND_REWRITE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQuery.query;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.sdk.tests.QueryCounter.ON_SCHEMA_TYPES;
import static com.constellio.sdk.tests.TestUtils.assertThatAllRecordsOf;
import static com.constellio.sdk.tests.TestUtils.assertThatRecords;
import static com.constellio.sdk.tests.TestUtils.getNetworkLinksOf;
import static com.constellio.sdk.tests.TestUtils.solrInputDocumentRemovingMetadatas;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@RunWith(Parameterized.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RecordServicesAgregatedMetadatasMechanicAcceptTest extends ConstellioTest {

	String testCase;
	static String givenOptimizedForMemoryUsage = "givenOptimizedForMemoryUsage";
	static String givenOptimizedForPerformance = "givenOptimizedForPerformance";

	TestsSchemasSetup schemas = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	AnotherSchemaMetadatas anotherSchema = schemas.new AnotherSchemaMetadatas();
	ThirdSchemaMetadatas thirdSchema = schemas.new ThirdSchemaMetadatas();
	RecordServices recordServices;
	RecordServicesAgregatedMetadatasAcceptTestRecords records = new RecordServicesAgregatedMetadatasAcceptTestRecords();
	SearchServices searchServices;

	public RecordServicesAgregatedMetadatasMechanicAcceptTest(String testCase) {
		this.testCase = testCase;
	}

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> testCases() {
		return Arrays.asList(new Object[][]{{givenOptimizedForMemoryUsage}, {givenOptimizedForPerformance}});
	}

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

		if (testCase.equals(givenOptimizedForMemoryUsage)) {
			givenConfig(ConstellioEIMConfigs.MEMORY_CONSUMPTION_LEVEL, MemoryConsumptionLevel.LESS_MEMORY_CONSUMPTION);
		} else {
			givenConfig(ConstellioEIMConfigs.MEMORY_CONSUMPTION_LEVEL, MemoryConsumptionLevel.BETTER_PERFORMANCE);
		}

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

		assertThat(getNetworkLinksOf(zeCollection)).containsOnly(
				tuple("group_default_ancestors", "group_default_parent", 0),
				tuple("group_default_ancestors", "group_default_ancestors", 0),
				tuple("aThirdSchemaType_default_sum", "anotherSchemaType_default_ref", 1),
				tuple("aThirdSchemaType_default_sum", "anotherSchemaType_default_sum", 1),
				tuple("aThirdSchemaType_default_sumX10", "anotherSchemaType_default_ref", 1),
				tuple("aThirdSchemaType_default_sumX10", "anotherSchemaType_default_sumX10", 1),//Changed from 3 to 1
				tuple("anotherSchemaType_default_copiedThirdSchemaTypeSum", "aThirdSchemaType_default_sum", 2),
				//Changed from 1 to 2
				tuple("anotherSchemaType_default_copiedThirdSchemaTypeSum", "anotherSchemaType_default_ref", 0),
				//Changed from 1 to 0
				tuple("anotherSchemaType_default_sum", "zeSchemaType_default_ref", 1),
				tuple("anotherSchemaType_default_sum", "zeSchemaType_default_number", 1),
				tuple("anotherSchemaType_default_sumX10", "anotherSchemaType_default_sum", 1), //Changed from 2 to 1
				tuple("zeSchemaType_default_pct", "zeSchemaType_default_ref", 0),//Changed from 2 to 0
				tuple("zeSchemaType_default_pct", "zeSchemaType_default_number", 0),//Changed from 2 to 0
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

		int nbQueries = clearAggregateMetadatasThenReindexReturningQtyOfQueriesOf(zeSchema, anotherSchema, thirdSchema);

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
		assertThatAllRecordsOf(thirdSchema).extractingMetadatas("id", "sum", "sumX10").containsOnly(
				tuple("aThirdSchemaRecord1", 10.0, 100.0),
				tuple("aThirdSchemaRecord2", 0.0, 0.0)
		);

		assertThat(nbQueries).isEqualTo(13);
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

	@Test
	public void givenAUnionAggregatedMetadataInAHierarchyThenAccurateValues()
			throws Exception {

		givenBackgroundThreadsEnabled();
		defineSchemasManager().using(schemas.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder zeType = schemaTypes.getSchemaType(zeSchema.typeCode());
				MetadataBuilder zeType_parent = zeType.createMetadata("parent").defineChildOfRelationshipToType(zeType);
				MetadataBuilder zeType_values = zeType.createMetadata("value").setType(STRING);
				MetadataBuilder zeType_agregated = zeType.createMetadata("agregated").setType(STRING).setMultivalue(true);
				zeType_agregated.defineDataEntry().asUnion(zeType_parent, zeType_values, zeType_agregated);

			}
		}));
		getDataLayerFactory().getDataLayerLogger().setMonitoredIds(asList("lvl0"));

		Metadata parent = zeSchema.metadata("parent");
		Metadata value = zeSchema.metadata("value");
		Metadata agregated = zeSchema.metadata("agregated");

		assertThat(getNetworkLinksOf(zeCollection)).containsOnly(
				tuple("group_default_ancestors", "group_default_parent", 0),
				tuple("group_default_ancestors", "group_default_ancestors", 0),
				tuple("zeSchemaType_default_agregated", "zeSchemaType_default_parent", 1),
				tuple("zeSchemaType_default_agregated", "zeSchemaType_default_value", 1),
				tuple("zeSchemaType_default_agregated", "zeSchemaType_default_agregated", 1));

		Transaction tx = new Transaction();
		TestRecord r0 = (TestRecord) tx.add(new TestRecord(zeSchema, "lvl0").set(value, "v0").set(parent, null));
		TestRecord r1 = (TestRecord) tx.add(new TestRecord(zeSchema, "lvl1").set(value, "v1").set(parent, "lvl0"));
		TestRecord r2 = (TestRecord) tx.add(new TestRecord(zeSchema, "lvl2").set(value, "v2").set(parent, "lvl1"));
		TestRecord r3 = (TestRecord) tx.add(new TestRecord(zeSchema, "lvl3").set(value, "v3").set(parent, "lvl2"));
		TestRecord r4 = (TestRecord) tx.add(new TestRecord(zeSchema, "lvl4").set(value, "v4").set(parent, "lvl3"));
		TestRecord r5 = (TestRecord) tx.add(new TestRecord(zeSchema, "lvl5").set(value, "v5").set(parent, "lvl4"));
		TestRecord r6 = (TestRecord) tx.add(new TestRecord(zeSchema, "lvl6").set(value, "v6").set(parent, "lvl5"));
		TestRecord r7 = (TestRecord) tx.add(new TestRecord(zeSchema, "lvl7").set(value, "v7").set(parent, "lvl6"));
		TestRecord r8 = (TestRecord) tx.add(new TestRecord(zeSchema, "lvl8").set(value, "v8").set(parent, "lvl7"));
		TestRecord r9 = (TestRecord) tx.add(new TestRecord(zeSchema, "lvl9").set(value, "v9").set(parent, "lvl8"));
		getModelLayerFactory().newRecordServices().execute(tx);
		waitForBatchProcess();

		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		assertThatRecords(searchServices.search(query(from(zeSchema.type()).returnAll())))
				.extractingMetadatas(IDENTIFIER, agregated).containsOnly(
				tuple("lvl0", asList("v1", "v2", "v3", "v4", "v5", "v6", "v7", "v8", "v9")),
				tuple("lvl1", asList("v2", "v3", "v4", "v5", "v6", "v7", "v8", "v9")),
				tuple("lvl2", asList("v3", "v4", "v5", "v6", "v7", "v8", "v9")),
				tuple("lvl3", asList("v4", "v5", "v6", "v7", "v8", "v9")),
				tuple("lvl4", asList("v5", "v6", "v7", "v8", "v9")),
				tuple("lvl5", asList("v6", "v7", "v8", "v9")),
				tuple("lvl6", asList("v7", "v8", "v9")),
				tuple("lvl7", asList("v8", "v9")),
				tuple("lvl8", asList("v9")),
				tuple("lvl9", new ArrayList<>())

		);

		int nbQueries = clearAggregateMetadatasThenReindexReturningQtyOfQueriesOf(zeSchema, anotherSchema, thirdSchema);
		assertThat(nbQueries).isEqualTo(7);

		assertThatRecords(searchServices.search(query(from(zeSchema.type()).returnAll())))
				.extractingMetadatas(IDENTIFIER, agregated).containsOnly(
				tuple("lvl0", asList("v1", "v2", "v3", "v4", "v5", "v6", "v7", "v8", "v9")),
				tuple("lvl1", asList("v2", "v3", "v4", "v5", "v6", "v7", "v8", "v9")),
				tuple("lvl2", asList("v3", "v4", "v5", "v6", "v7", "v8", "v9")),
				tuple("lvl3", asList("v4", "v5", "v6", "v7", "v8", "v9")),
				tuple("lvl4", asList("v5", "v6", "v7", "v8", "v9")),
				tuple("lvl5", asList("v6", "v7", "v8", "v9")),
				tuple("lvl6", asList("v7", "v8", "v9")),
				tuple("lvl7", asList("v8", "v9")),
				tuple("lvl8", asList("v9")),
				tuple("lvl9", new ArrayList<>())

		);
	}

	@Test
	public void givenAUnionAggregatedMetadataWithAComplexHierarchyThenAccurateValues()
			throws Exception {

		givenBackgroundThreadsEnabled();
		defineSchemasManager().using(schemas.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder zeType = schemaTypes.getSchemaType(zeSchema.typeCode());
				MetadataBuilder zeType_parent = zeType.createMetadata("parent").defineReferencesTo(zeType);
				MetadataBuilder zeType_values = zeType.createMetadata("value").setType(STRING);
				MetadataBuilder zeType_agregated = zeType.createMetadata("agregated").setType(STRING).setMultivalue(true);
				zeType_agregated.defineDataEntry().asUnion(zeType_parent, zeType_values, zeType_agregated);

			}
		}));

		Metadata parent = zeSchema.metadata("parent");
		Metadata value = zeSchema.metadata("value");
		Metadata agregated = zeSchema.metadata("agregated");

		assertThat(getNetworkLinksOf(zeCollection)).containsOnly(
				tuple("group_default_ancestors", "group_default_parent", 0),
				tuple("group_default_ancestors", "group_default_ancestors", 0),
				tuple("zeSchemaType_default_agregated", "zeSchemaType_default_parent", 1),
				tuple("zeSchemaType_default_agregated", "zeSchemaType_default_value", 1),
				tuple("zeSchemaType_default_agregated", "zeSchemaType_default_agregated", 1));

		Transaction tx = new Transaction();
		TestRecord r1 = (TestRecord) tx.add(new TestRecord(zeSchema, "r1").set(value, "1").set(parent, null));
		TestRecord r2 = (TestRecord) tx.add(new TestRecord(zeSchema, "r2").set(value, "2").set(parent, "r4"));
		TestRecord r3 = (TestRecord) tx.add(new TestRecord(zeSchema, "r3").set(value, "3").set(parent, "r1"));
		TestRecord r4 = (TestRecord) tx.add(new TestRecord(zeSchema, "r4").set(value, "4").set(parent, "r8"));
		TestRecord r5 = (TestRecord) tx.add(new TestRecord(zeSchema, "r5").set(value, "5").set(parent, "r2"));
		TestRecord r6 = (TestRecord) tx.add(new TestRecord(zeSchema, "r6").set(value, "6").set(parent, "r5"));
		TestRecord r7 = (TestRecord) tx.add(new TestRecord(zeSchema, "r7").set(value, "7").set(parent, "r3"));
		TestRecord r8 = (TestRecord) tx.add(new TestRecord(zeSchema, "r8").set(value, "8").set(parent, "r7"));
		getModelLayerFactory().newRecordServices().execute(tx);
		waitForBatchProcess();

		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		assertThatRecords(searchServices.search(query(from(zeSchema.type()).returnAll())))
				.extractingMetadatas(IDENTIFIER, agregated).containsOnly(
				tuple("r1", asList("2", "3", "4", "5", "6", "7", "8")),
				tuple("r2", asList("5", "6")),
				tuple("r3", asList("2", "4", "5", "6", "7", "8")),
				tuple("r4", asList("2", "5", "6")),
				tuple("r5", asList("6")),
				tuple("r6", new ArrayList<>()),
				tuple("r7", asList("2", "4", "5", "6", "8")),
				tuple("r8", asList("2", "4", "5", "6"))

		);

		int nbQueries = clearAggregateMetadatasThenReindexReturningQtyOfQueriesOf(zeSchema, anotherSchema, thirdSchema);
		assertThat(nbQueries).isEqualTo(7);

		assertThatRecords(searchServices.search(query(from(zeSchema.type()).returnAll())))
				.extractingMetadatas(IDENTIFIER, agregated).containsOnly(
				tuple("r1", asList("2", "3", "4", "5", "6", "7", "8")),
				tuple("r2", asList("5", "6")),
				tuple("r3", asList("2", "4", "5", "6", "7", "8")),
				tuple("r4", asList("2", "5", "6")),
				tuple("r5", asList("6")),
				tuple("r6", new ArrayList<>()),
				tuple("r7", asList("2", "4", "5", "6", "8")),
				tuple("r8", asList("2", "4", "5", "6"))

		);
	}

	public static int clearAggregateMetadatasThenReindexReturningQtyOfQueriesOf(SchemaShortcuts... schemas) {

		String[] schemaTypes = new String[schemas.length];
		for (int i = 0; i < schemas.length; i++) {
			schemaTypes[i] = SchemaUtils.getSchemaTypeCode(schemas[i].code());
		}

		return clearAggregateMetadatasThenReindexReturningQtyOfQueriesOf(schemaTypes);
	}

	public static int clearAggregateMetadatasThenReindexReturningQtyOfQueriesOf(String... schemaTypes) {
		ModelLayerFactory modelLayerFactory = ConstellioTest.getInstance().getModelLayerFactory();

		SearchServices searchServices = modelLayerFactory.newSearchServices();

		Set<String> collectionsToInvalidateCache = new HashSet<>();

		for (String collection : modelLayerFactory.getCollectionsListManager().getCollectionsExcludingSystem()) {
			List<SolrInputDocument> documents = new ArrayList<>();
			for (Record record : searchServices.search(new LogicalSearchQuery(fromAllSchemasIn(collection).returnAll()))) {
				MetadataSchema schema = modelLayerFactory.getMetadataSchemasManager().getSchemaOf(record);
				if (!schema.getMetadatas().onlyAggregations().isEmpty()) {
					collectionsToInvalidateCache.add(collection);
					documents.add(solrInputDocumentRemovingMetadatas(record.getId(), schema.getMetadatas().onlyAggregations()));
				}
			}

			try {
				modelLayerFactory.getDataLayerFactory().getRecordsVaultServer().addAll(new BigVaultServerTransaction(NOW())
						.setUpdatedDocuments(documents));
			} catch (BigVaultException e) {
				throw new RuntimeException(e);
			}
		}

		for (String collection : collectionsToInvalidateCache) {
			modelLayerFactory.getRecordsCaches().getCache(collection).reloadAllSchemaTypes();
		}

		try {
			new RecordsCache2IntegrityDiagnosticService(modelLayerFactory).validateIntegrity(false, false).throwIfNonEmpty();
		} catch (ValidationException e) {
			throw new RuntimeException(e);
		}

		QueryCounter queryCounter = new QueryCounter(modelLayerFactory.getDataLayerFactory(), ON_SCHEMA_TYPES(schemaTypes));

		ReindexingServices reindexingServices = new ReindexingServices(modelLayerFactory);
		reindexingServices.reindexCollections(RECALCULATE_AND_REWRITE);

		return queryCounter.newQueryCalls();
	}

}
