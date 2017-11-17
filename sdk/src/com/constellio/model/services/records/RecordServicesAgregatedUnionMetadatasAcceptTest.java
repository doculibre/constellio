
package com.constellio.model.services.records;

import static com.constellio.data.dao.dto.records.RecordsFlushing.NOW;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.services.records.reindexing.ReindexationMode.RECALCULATE_AND_REWRITE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQuery.query;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.QueryCounter.ON_SCHEMA_TYPES;
import static com.constellio.sdk.tests.TestUtils.assertThatRecords;
import static com.constellio.sdk.tests.TestUtils.solrInputDocumentRemovingMetadatas;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.groups.Tuple;
import org.junit.Test;

import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataNetworkLink;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.QueryCounter;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.MetadataSchemaTypesConfigurator;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ThirdSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;

public class RecordServicesAgregatedUnionMetadatasAcceptTest extends ConstellioTest {

	TestsSchemasSetup schemas = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	AnotherSchemaMetadatas anotherSchema = schemas.new AnotherSchemaMetadatas();
	ThirdSchemaMetadatas thirdSchema = schemas.new ThirdSchemaMetadatas();
	RecordServicesAgregatedMetadatasAcceptTestRecords records = new RecordServicesAgregatedMetadatasAcceptTestRecords();

	@Test
	public void givenUnionOfSingleValueStringMetadatasThenAllStringsAreCopiedWithoutDuplicates()
			throws Exception {

		givenBackgroundThreadsEnabled();
		defineSchemasManager().using(schemas.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder zeType = schemaTypes.getSchemaType(zeSchema.typeCode());
				MetadataSchemaTypeBuilder anotherType = schemaTypes.getSchemaType(anotherSchema.typeCode());
				MetadataSchemaTypeBuilder thirdType = schemaTypes.getSchemaType(thirdSchema.typeCode());
				MetadataBuilder zeSchema_value1 = zeType.createMetadata("stringValue1").setType(STRING);
				MetadataBuilder zeSchema_Value2 = zeType.createMetadata("stringValue2").setType(STRING).setMultivalue(true);
				MetadataBuilder zeSchema_zeRef = zeType.createMetadata("ref").defineReferencesTo(anotherType);
				MetadataBuilder anotherSchema_stringValuesUnion = anotherType.createMetadata("stringValuesUnion").setType(STRING)
						.setMultivalue(true).defineDataEntry().asUnion(zeSchema_zeRef, zeSchema_value1, zeSchema_Value2);
				MetadataBuilder anotherSchema_zeRef = anotherType.createMetadata("ref").defineReferencesTo(thirdType);
				MetadataBuilder anotherSchema_value = anotherType.createMetadata("stringValue").setType(STRING)
						.setMultivalue(true);
				thirdType.createMetadata("stringValuesUnion").setType(STRING).setMultivalue(true)
						.defineDataEntry().asUnion(anotherSchema_zeRef, anotherSchema_stringValuesUnion, anotherSchema_value);

			}
		}));

		assertThat(getNetworkLinks()).containsOnly(
				tuple("anotherSchemaType_default_stringValuesUnion", "zeSchemaType_default_ref", 1),
				tuple("aThirdSchemaType_default_stringValuesUnion", "anotherSchemaType_default_ref", 1),
				tuple("aThirdSchemaType_default_stringValuesUnion", "anotherSchemaType_default_stringValuesUnion", 1),
				tuple("aThirdSchemaType_default_stringValuesUnion", "anotherSchemaType_default_stringValue", 1),
				tuple("anotherSchemaType_default_stringValuesUnion", "zeSchemaType_default_stringValue1", 1),
				tuple("anotherSchemaType_default_stringValuesUnion", "zeSchemaType_default_stringValue2", 1)
		);

		Transaction tx = new Transaction();

		tx.add(new TestRecord(anotherSchema, "merge1").set("ref", "merge3"));
		tx.add(new TestRecord(anotherSchema, "merge2").set("ref", "merge3"));
		tx.add(new TestRecord(thirdSchema, "merge3"));
		getModelLayerFactory().newRecordServices().execute(tx);
		Metadata anotherSchema_stringValuesUnion = anotherSchema.metadata("stringValuesUnion");
		Metadata thirdSchema_stringValuesUnion = thirdSchema.metadata("stringValuesUnion");

		TestRecord r1 = new TestRecord(zeSchema, "r1").set("stringValue1", "value1").set("ref", "merge1");
		TestRecord r2 = new TestRecord(zeSchema, "r2").set("stringValue2", asList("value2")).set("ref", "merge1");
		TestRecord r3 = new TestRecord(zeSchema, "r3").set("stringValue1", "value3").set("stringValue2", asList("value5"))
				.set("ref", "merge2");
		TestRecord r4 = new TestRecord(zeSchema, "r4").set("stringValue2", asList("value4")).set("ref", "merge2");
		TestRecord r5 = new TestRecord(zeSchema, "r5").set("stringValue1", "value4").set("ref", "merge2");
		getModelLayerFactory().newRecordServices().execute(new Transaction(r1, r2, r3, r4, r5));
		waitForBatchProcess();
		assertThat(record("merge1").getList(anotherSchema_stringValuesUnion)).containsOnly("value1", "value2");
		assertThat(record("merge2").getList(anotherSchema_stringValuesUnion))
				.containsOnly("value3", "value4", "value4", "value5");
		assertThat(record("merge3").getList(thirdSchema_stringValuesUnion))
				.containsOnly("value1", "value2", "value3", "value4", "value5");

		tx = new Transaction();
		tx.add(r1.set("stringValue1", "value1new"));
		tx.add(r3.set("ref", "merge1"));
		getModelLayerFactory().newRecordServices().execute(tx);
		waitForBatchProcess();
		assertThat(record("merge1").getList(anotherSchema_stringValuesUnion))
				.containsOnly("value1new", "value2", "value3", "value5");
		assertThat(record("merge2").getList(anotherSchema_stringValuesUnion)).containsOnly("value4");
		assertThat(record("merge3").getList(thirdSchema_stringValuesUnion))
				.containsOnly("value1new", "value2", "value3", "value4", "value5");

		BigVaultServer bigVaultServer = getDataLayerFactory().newRecordDao().getBigVaultServer();
		bigVaultServer.addAll(new BigVaultServerTransaction(NOW()).setUpdatedDocuments(asList(
				solrInputDocumentRemovingMetadatas("merge1", anotherSchema.metadata("stringValuesUnion")),
				solrInputDocumentRemovingMetadatas("merge2", anotherSchema.metadata("stringValuesUnion"))
		)));
		int queries = reindexReturningQtyOfQueries();
		assertThat(record("merge1").getList(anotherSchema_stringValuesUnion))
				.containsOnly("value1new", "value2", "value3", "value5");
		assertThat(record("merge2").getList(anotherSchema_stringValuesUnion)).containsOnly("value4");
		assertThat(record("merge3").getList(thirdSchema_stringValuesUnion))
				.containsOnly("value1new", "value2", "value3", "value4", "value5");
		//assertThat(queries).isEqualTo(42);

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

		Metadata parent = zeSchema.metadata("parent");
		Metadata value = zeSchema.metadata("value");
		Metadata agregated = zeSchema.metadata("agregated");

		assertThat(getNetworkLinks()).containsOnly(
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

		BigVaultServer bigVaultServer = getDataLayerFactory().newRecordDao().getBigVaultServer();
		bigVaultServer.addAll(new BigVaultServerTransaction(NOW()).setUpdatedDocuments(asList(
				solrInputDocumentRemovingMetadatas("lvl0", agregated),
				solrInputDocumentRemovingMetadatas("lvl1", agregated),
				solrInputDocumentRemovingMetadatas("lvl2", agregated),
				solrInputDocumentRemovingMetadatas("lvl3", agregated),
				solrInputDocumentRemovingMetadatas("lvl4", agregated),
				solrInputDocumentRemovingMetadatas("lvl5", agregated),
				solrInputDocumentRemovingMetadatas("lvl6", agregated),
				solrInputDocumentRemovingMetadatas("lvl7", agregated),
				solrInputDocumentRemovingMetadatas("lvl8", agregated),
				solrInputDocumentRemovingMetadatas("lvl9", agregated)
		)));
		assertThat(reindexReturningQtyOfQueries()).isEqualTo(82);

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
				MetadataBuilder zeType_parent = zeType.createMetadata("parent").defineChildOfRelationshipToType(zeType);
				MetadataBuilder zeType_values = zeType.createMetadata("value").setType(STRING);
				MetadataBuilder zeType_agregated = zeType.createMetadata("agregated").setType(STRING).setMultivalue(true);
				zeType_agregated.defineDataEntry().asUnion(zeType_parent, zeType_values, zeType_agregated);

			}
		}));

		Metadata parent = zeSchema.metadata("parent");
		Metadata value = zeSchema.metadata("value");
		Metadata agregated = zeSchema.metadata("agregated");

		assertThat(getNetworkLinks()).containsOnly(
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

		BigVaultServer bigVaultServer = getDataLayerFactory().newRecordDao().getBigVaultServer();
		bigVaultServer.addAll(new BigVaultServerTransaction(NOW()).setUpdatedDocuments(asList(
				solrInputDocumentRemovingMetadatas("r1", agregated),
				solrInputDocumentRemovingMetadatas("r2", agregated),
				solrInputDocumentRemovingMetadatas("r3", agregated),
				solrInputDocumentRemovingMetadatas("r4", agregated),
				solrInputDocumentRemovingMetadatas("r5", agregated),
				solrInputDocumentRemovingMetadatas("r6", agregated),
				solrInputDocumentRemovingMetadatas("r7", agregated),
				solrInputDocumentRemovingMetadatas("r8", agregated)
		)));
		assertThat(reindexReturningQtyOfQueries()).isEqualTo(66);

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

	@Test
	public void givenRecordAndTheirUnionsCreatedInSameTransactionThenOk()
			throws Exception {

		givenBackgroundThreadsEnabled();
		defineSchemasManager().using(schemas.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder zeType = schemaTypes.getSchemaType(zeSchema.typeCode());
				MetadataSchemaTypeBuilder anotherType = schemaTypes.getSchemaType(anotherSchema.typeCode());
				MetadataSchemaTypeBuilder thirdType = schemaTypes.getSchemaType(thirdSchema.typeCode());
				MetadataBuilder zeSchema_valueMetadata = zeType.getDefaultSchema().create("stringValue").setType(STRING);
				MetadataBuilder zeSchema_zeRef = zeType.getDefaultSchema().create("ref").defineReferencesTo(anotherType);

				MetadataBuilder anotherSchema_stringValuesUnion = anotherType.getDefaultSchema().create("stringValuesUnion")
						.setType(STRING).setMultivalue(true)
						.defineDataEntry().asUnion(zeSchema_zeRef, zeSchema_valueMetadata);
				MetadataBuilder anotherSchema_zeRef = anotherType.getDefaultSchema().create("ref").defineReferencesTo(thirdType);
				MetadataBuilder thirdSchema_stringValuesUnion = thirdType.getDefaultSchema().create("stringValuesUnion")
						.setType(STRING).setMultivalue(true)
						.defineDataEntry().asUnion(anotherSchema_zeRef, anotherSchema_stringValuesUnion);

			}
		}));
		Metadata anotherSchema_stringValuesUnion = anotherSchema.metadata("stringValuesUnion");
		Metadata thirdSchema_stringValuesUnion = thirdSchema.metadata("stringValuesUnion");

		assertThat(getNetworkLinks()).containsOnly(
				tuple("anotherSchemaType_default_stringValuesUnion", "zeSchemaType_default_ref", 1),
				tuple("aThirdSchemaType_default_stringValuesUnion", "anotherSchemaType_default_ref", 1),
				tuple("aThirdSchemaType_default_stringValuesUnion", "anotherSchemaType_default_stringValuesUnion", 1),
				tuple("anotherSchemaType_default_stringValuesUnion", "zeSchemaType_default_stringValue", 1));

		Transaction tx = new Transaction();

		tx.add(new TestRecord(anotherSchema, "merge1").set("ref", "merge3"));
		tx.add(new TestRecord(anotherSchema, "merge2").set("ref", "merge3"));
		tx.add(new TestRecord(thirdSchema, "merge3"));

		TestRecord r1 = (TestRecord) tx.add(new TestRecord(zeSchema, "r1").set("stringValue", "value1").set("ref", "merge1"));
		TestRecord r2 = (TestRecord) tx.add(new TestRecord(zeSchema, "r2").set("stringValue", "value2").set("ref", "merge1"));
		TestRecord r3 = (TestRecord) tx.add(new TestRecord(zeSchema, "r3").set("stringValue", "value3").set("ref", "merge2"));
		TestRecord r4 = (TestRecord) tx.add(new TestRecord(zeSchema, "r4").set("stringValue", "value4").set("ref", "merge2"));
		TestRecord r5 = (TestRecord) tx.add(new TestRecord(zeSchema, "r5").set("stringValue", "value4").set("ref", "merge2"));
		getModelLayerFactory().newRecordServices().execute(tx);
		waitForBatchProcess();
		assertThat(record("merge1").getList(anotherSchema_stringValuesUnion)).containsOnly("value1", "value2");
		assertThat(record("merge2").getList(anotherSchema_stringValuesUnion)).containsOnly("value3", "value4");
		assertThat(record("merge3").getList(thirdSchema_stringValuesUnion))
				.containsOnly("value1", "value2", "value3", "value4");

	}

	private int reindexReturningQtyOfQueries() {

		QueryCounter queryCounter = new QueryCounter(getDataLayerFactory(),
				ON_SCHEMA_TYPES(zeSchema.typeCode(), anotherSchema.typeCode()));

		ReindexingServices reindexingServices = new ReindexingServices(getModelLayerFactory());
		reindexingServices.reindexCollections(RECALCULATE_AND_REWRITE);

		return queryCounter.newQueryCalls();
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
