
package com.constellio.model.services.records;

import static com.constellio.data.dao.dto.records.RecordsFlushing.NOW;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.services.records.reindexing.ReindexationMode.RECALCULATE_AND_REWRITE;
import static com.constellio.sdk.tests.QueryCounter.ACCEPT_ALL;
import static com.constellio.sdk.tests.TestUtils.solrInputDocumentRemovingMetadatas;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.groups.Tuple;
import org.junit.Test;

import com.constellio.data.dao.services.bigVault.solr.BigVaultException;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataNetworkLink;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
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

		int queries = clearAllAggregatedValuesThenReindexReturningQtyOfQueries();
		assertThat(record("merge1").getList(anotherSchema_stringValuesUnion))
				.containsOnly("value1new", "value2", "value3", "value5");
		assertThat(record("merge2").getList(anotherSchema_stringValuesUnion)).containsOnly("value4");
		assertThat(record("merge3").getList(thirdSchema_stringValuesUnion))
				.containsOnly("value1new", "value2", "value3", "value4", "value5");
		//assertThat(queries).isEqualTo(42);

	}

	//@Test
	public void givenAUnionAggregatedMetadataInAHierarchyThenAccurateValues()
			throws Exception {

		givenBackgroundThreadsEnabled();
		defineSchemasManager().using(schemas.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder zeType = schemaTypes.getSchemaType(zeSchema.typeCode());
				MetadataSchemaTypeBuilder anotherType = schemaTypes.getSchemaType(anotherSchema.typeCode());
				MetadataBuilder zeType_parent = zeType.createMetadata("parent").defineChildOfRelationshipToType(zeType);
				MetadataBuilder zeType_values = zeType.createMetadata("value").setType(STRING).setMultivalue(true);
				MetadataBuilder anotherType_parent = zeType.createMetadata("parent").defineChildOfRelationshipToType(anotherType);
				MetadataBuilder anotherType_values = zeType.createMetadata("values").setType(STRING).setMultivalue(true);

				MetadataBuilder zeType_agregated = zeType.createMetadata("agregated")
						.setType(STRING).setMultivalue(true).defineDataEntry()
						.asUnion(anotherType_parent, anotherType_values);

			}
		}));

		assertThat(getNetworkLinks()).containsOnly(
				tuple("anotherSchemaType_default_stringValuesUnion", "zeSchemaType_default_ref", 1),
				tuple("aThirdSchemaType_default_stringValuesUnion", "anotherSchemaType_default_ref", 1),
				tuple("aThirdSchemaType_default_stringValuesUnion", "anotherSchemaType_default_stringValuesUnion", 1),
				tuple("anotherSchemaType_default_stringValuesUnion", "zeSchemaType_default_stringValue", 1));

		Transaction tx = new Transaction();

		tx.add(new TestRecord(anotherSchema, "merge1").set("ref", "merge3"));
		tx.add(new TestRecord(anotherSchema, "merge2").set("ref", "merge3"));
		tx.add(new TestRecord(thirdSchema, "merge3"));
		getModelLayerFactory().newRecordServices().execute(tx);
		Metadata anotherSchema_stringValuesUnion = anotherSchema.metadata("stringValuesUnion");
		Metadata thirdSchema_stringValuesUnion = thirdSchema.metadata("stringValuesUnion");

		tx = new Transaction();
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

		tx = new Transaction();
		tx.add(r1.set("stringValue", "value1new"));
		tx.add(r3.set("ref", "merge1"));
		getModelLayerFactory().newRecordServices().execute(tx);
		waitForBatchProcess();
		assertThat(record("merge1").getList(anotherSchema_stringValuesUnion)).containsOnly("value1new", "value2", "value3");
		assertThat(record("merge2").getList(anotherSchema_stringValuesUnion)).containsOnly("value4");
		assertThat(record("merge3").getList(thirdSchema_stringValuesUnion))
				.containsOnly("value1new", "value2", "value3", "value4");

		int queries = clearAllAggregatedValuesThenReindexReturningQtyOfQueries();
		assertThat(record("merge1").getList(anotherSchema_stringValuesUnion)).containsOnly("value1new", "value2", "value3");
		assertThat(record("merge2").getList(anotherSchema_stringValuesUnion)).containsOnly("value4");
		assertThat(record("merge3").getList(thirdSchema_stringValuesUnion))
				.containsOnly("value1new", "value2", "value3", "value4");
		//assertThat(queries).isEqualTo(42);

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

	private int clearAllAggregatedValuesThenReindexReturningQtyOfQueries() {

		BigVaultServerTransaction transaction = new BigVaultServerTransaction(NOW()).setUpdatedDocuments(asList(
				solrInputDocumentRemovingMetadatas("merge1", anotherSchema.metadata("stringValuesUnion")),
				solrInputDocumentRemovingMetadatas("merge2", anotherSchema.metadata("stringValuesUnion"))
		));
		try {
			getDataLayerFactory().newRecordDao().getBigVaultServer().addAll(transaction);
		} catch (BigVaultException e) {
			throw new RuntimeException(e);
		}

		assertThat(record("merge1").getList(anotherSchema.metadata("stringValuesUnion"))).isEmpty();
		assertThat(record("merge2").getList(anotherSchema.metadata("stringValuesUnion"))).isEmpty();

		QueryCounter queryCounter = new QueryCounter(getDataLayerFactory(), ACCEPT_ALL);

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
