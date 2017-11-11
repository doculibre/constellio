package com.constellio.model.services.records;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.MetadataSchemaTypesConfigurator;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;

public class RecordServicesAgregatedUnionMetadatasAcceptTest extends ConstellioTest {

	TestsSchemasSetup schemas = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	AnotherSchemaMetadatas anotherSchema = schemas.new AnotherSchemaMetadatas();
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
				MetadataBuilder valueMetadata = zeType.getDefaultSchema().create("stringValue").setType(STRING);
				MetadataBuilder zeRef = zeType.getDefaultSchema().create("ref").defineReferencesTo(anotherType);
				anotherType.getDefaultSchema().create("stringValuesUnion").setType(STRING).setMultivalue(true)
						.defineDataEntry().asUnion(zeRef, valueMetadata);

			}
		}));

		Transaction tx = new Transaction();

		tx.add(new TestRecord(anotherSchema, "merge1"));
		tx.add(new TestRecord(anotherSchema, "merge2"));
		getModelLayerFactory().newRecordServices().execute(tx);

		tx = new Transaction();
		tx.add(new TestRecord(zeSchema).set("stringValue", "value1").set("ref", "merge1"));
		tx.add(new TestRecord(zeSchema).set("stringValue", "value2").set("ref", "merge1"));
		tx.add(new TestRecord(zeSchema).set("stringValue", "value3").set("ref", "merge2"));
		tx.add(new TestRecord(zeSchema).set("stringValue", "value4").set("ref", "merge2"));
		tx.add(new TestRecord(zeSchema).set("stringValue", "value4").set("ref", "merge2"));
		getModelLayerFactory().newRecordServices().execute(tx);
		waitForBatchProcess();

		assertThat(record("merge1").getList(anotherSchema.metadata("stringValuesUnion"))).containsOnly("value1", "value2");
		assertThat(record("merge2").getList(anotherSchema.metadata("stringValuesUnion"))).containsOnly("value3", "value4");
	}

}
