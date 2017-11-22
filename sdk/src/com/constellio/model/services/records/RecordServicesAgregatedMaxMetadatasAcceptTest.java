
package com.constellio.model.services.records;

import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.services.records.RecordServicesAgregatedMetadatasMechanicAcceptTest.clearAggregateMetadatasThenReindexReturningQtyOfQueriesOf;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.groups.Tuple;
import org.junit.Test;

import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataNetworkLink;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.MetadataSchemaTypesConfigurator;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ThirdSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;

public class RecordServicesAgregatedMaxMetadatasAcceptTest extends ConstellioTest {

	TestsSchemasSetup schemas = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	AnotherSchemaMetadatas anotherSchema = schemas.new AnotherSchemaMetadatas();
	ThirdSchemaMetadatas thirdSchema = schemas.new ThirdSchemaMetadatas();
	RecordServicesAgregatedMetadatasAcceptTestRecords records = new RecordServicesAgregatedMetadatasAcceptTestRecords();

	@Test
	public void givenMaxOfMultiValueNumberMetadatasThenAllNumbersAreCopiedWithoutDuplicates()
			throws Exception {

		givenBackgroundThreadsEnabled();
		defineSchemasManager().using(schemas.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder zeType = schemaTypes.getSchemaType(zeSchema.typeCode());
				MetadataSchemaTypeBuilder anotherType = schemaTypes.getSchemaType(anotherSchema.typeCode());
				MetadataSchemaTypeBuilder thirdType = schemaTypes.getSchemaType(thirdSchema.typeCode());
				MetadataBuilder zeSchema_valueMetadata = zeType.getDefaultSchema().create("numberValue").setType(NUMBER)
						.setMultivalue(true);
				MetadataBuilder zeSchema_zeRef = zeType.getDefaultSchema().create("ref").defineReferencesTo(anotherType);

				MetadataBuilder anotherSchema_maxValue = anotherType.getDefaultSchema().create("maxValue")
						.setType(NUMBER).defineDataEntry().asMax(zeSchema_zeRef, zeSchema_valueMetadata);
				MetadataBuilder anotherSchema_zeRef = anotherType.getDefaultSchema().create("ref").defineReferencesTo(thirdType);
				MetadataBuilder thirdSchema_maxValue = thirdType.getDefaultSchema().create("maxValue")
						.setType(NUMBER).defineDataEntry().asMax(anotherSchema_zeRef, anotherSchema_maxValue);

			}
		}));
		Metadata zeSchema_numberValue = zeSchema.metadata("numberValue");
		Metadata anotherSchema_maxValue = anotherSchema.metadata("maxValue");
		Metadata thirdSchema_maxValue = thirdSchema.metadata("maxValue");

		assertThat(getNetworkLinks()).containsOnly(
				tuple("anotherSchemaType_default_maxValue", "zeSchemaType_default_ref", 1),
				tuple("aThirdSchemaType_default_maxValue", "anotherSchemaType_default_ref", 1),
				tuple("aThirdSchemaType_default_maxValue", "anotherSchemaType_default_maxValue", 1),
				tuple("anotherSchemaType_default_maxValue", "zeSchemaType_default_numberValue", 1));

		Transaction tx = new Transaction();

		tx.add(new TestRecord(anotherSchema, "merge1").set("ref", "merge3"));
		tx.add(new TestRecord(anotherSchema, "merge2").set("ref", "merge3"));
		tx.add(new TestRecord(thirdSchema, "merge3"));
		getModelLayerFactory().newRecordServices().execute(tx);

		tx = new Transaction();
		TestRecord r1 = new TestRecord(zeSchema, "r1").set("numberValue", asList(0.7, 1.4)).set("ref", "merge1");
		TestRecord r2 = new TestRecord(zeSchema, "r2").set("numberValue", asList(0.9, 0.5)).set("ref", "merge1");
		TestRecord r3 = new TestRecord(zeSchema, "r3").set("numberValue", asList(0.1, 1.6)).set("ref", "merge2");
		TestRecord r4 = new TestRecord(zeSchema, "r4").set("numberValue", asList(0.2, 1.5)).set("ref", "merge2");
		TestRecord r5 = new TestRecord(zeSchema, "r5").set("numberValue", asList(0.3, 1.4)).set("ref", "merge2");
		getModelLayerFactory().newRecordServices().execute(tx.addAll(r1, r2, r3, r4, r5));
		waitForBatchProcess();
		assertThat(record("merge1").get(anotherSchema_maxValue)).isEqualTo(1.4);
		assertThat(record("merge2").get(anotherSchema_maxValue)).isEqualTo(1.6);
		assertThat(record("merge3").get(thirdSchema_maxValue)).isEqualTo(1.6);

		tx = new Transaction();
		tx.add(r1.set("numberValue", asList(0.7, 2.0)));
		tx.add(r3.set("ref", "merge1"));
		getModelLayerFactory().newRecordServices().execute(tx);
		waitForBatchProcess();
		assertThat(record("merge1").get(anotherSchema_maxValue)).isEqualTo(2.0);
		assertThat(record("merge2").get(anotherSchema_maxValue)).isEqualTo(1.5);
		assertThat(record("merge3").get(thirdSchema_maxValue)).isEqualTo(2.0);

		int queries = clearAggregateMetadatasThenReindexReturningQtyOfQueriesOf(zeSchema, anotherSchema, thirdSchema);
		assertThat(record("merge1").get(anotherSchema_maxValue)).isEqualTo(2.0);
		assertThat(record("merge2").get(anotherSchema_maxValue)).isEqualTo(1.5);
		assertThat(record("merge3").get(thirdSchema_maxValue)).isEqualTo(2.0);
		assertThat(queries).isEqualTo(10);

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
