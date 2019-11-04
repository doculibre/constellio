package com.constellio.model.services.records;

import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
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
import org.assertj.core.api.Assertions;
import org.junit.Test;

import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.services.records.RecordServicesAgregatedMetadatasMechanicAcceptTest.clearAggregateMetadatasThenReindexReturningQtyOfQueriesOf;
import static com.constellio.sdk.tests.TestUtils.getNetworkLinksOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

public class RecordServicesAgregatedRefCountMetadatasAcceptTest extends ConstellioTest {

	TestsSchemasSetup schemas = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	AnotherSchemaMetadatas anotherSchema = schemas.new AnotherSchemaMetadatas();
	ThirdSchemaMetadatas thirdSchema = schemas.new ThirdSchemaMetadatas();
	RecordServicesAgregatedMetadatasAcceptTestRecords records = new RecordServicesAgregatedMetadatasAcceptTestRecords();

	@Test
	public void givenReferenceCountAggregatedMetadatasThenCalculatedAccurately()
			throws Exception {

		givenBackgroundThreadsEnabled();
		defineSchemasManager().using(schemas.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder zeType = schemaTypes.getSchemaType(zeSchema.typeCode());
				MetadataSchemaTypeBuilder anotherType = schemaTypes.getSchemaType(anotherSchema.typeCode());
				MetadataSchemaTypeBuilder thirdType = schemaTypes.getSchemaType(thirdSchema.typeCode());
				MetadataBuilder zeSchema_zeRef = zeType.getDefaultSchema().create("ref").defineReferencesTo(anotherType);

				MetadataBuilder anotherSchema_refCount = anotherType.getDefaultSchema().create("refCount")
						.setType(NUMBER).defineDataEntry().asReferenceCount(zeSchema_zeRef);
				MetadataBuilder anotherSchema_zeRef = anotherType.getDefaultSchema().create("ref").defineReferencesTo(thirdType);
				MetadataBuilder thirdSchema_refCount = thirdType.getDefaultSchema().create("refCount")
						.setType(NUMBER).defineDataEntry().asReferenceCount(anotherSchema_zeRef);

			}
		}));
		Metadata anotherSchema_refCount = anotherSchema.metadata("refCount");
		Metadata thirdSchema_refCount = thirdSchema.metadata("refCount");

		assertThat(getNetworkLinksOf(zeCollection)).containsOnly(
				Assertions.tuple("group_default_ancestors", "group_default_parent", 0),
				Assertions.tuple("group_default_ancestors", "group_default_ancestors", 0),
				tuple("aThirdSchemaType_default_refCount", "anotherSchemaType_default_ref", 1),
				tuple("aThirdSchemaType_default_refCount", "anotherSchemaType_default_ref", 1),
				tuple("anotherSchemaType_default_refCount", "zeSchemaType_default_ref", 1),
				tuple("anotherSchemaType_default_refCount", "zeSchemaType_default_ref", 1));

		Transaction tx = new Transaction();

		tx.add(new TestRecord(anotherSchema, "merge1").set("ref", "merge3"));
		tx.add(new TestRecord(anotherSchema, "merge2").set("ref", "merge3"));
		tx.add(new TestRecord(thirdSchema, "merge3"));
		getModelLayerFactory().newRecordServices().execute(tx);

		tx = new Transaction();
		TestRecord r1 = new TestRecord(zeSchema, "r1").set("ref", "merge1");
		TestRecord r2 = new TestRecord(zeSchema, "r2").set("ref", "merge1");
		TestRecord r3 = new TestRecord(zeSchema, "r3").set("ref", "merge2");
		TestRecord r4 = new TestRecord(zeSchema, "r4").set("ref", "merge2");
		TestRecord r5 = new TestRecord(zeSchema, "r5").set("ref", "merge2");
		getModelLayerFactory().newRecordServices().execute(tx.addAll(r1, r2, r3, r4, r5));
		waitForBatchProcess();
		assertThat(record("merge1").<Double>get(anotherSchema_refCount)).isEqualTo(2.0);
		assertThat(record("merge2").<Double>get(anotherSchema_refCount)).isEqualTo(3.0);
		assertThat(record("merge3").<Double>get(thirdSchema_refCount)).isEqualTo(2.0);

		tx = new Transaction();
		tx.add(r3.set("ref", "merge1"));
		getModelLayerFactory().newRecordServices().execute(tx);
		waitForBatchProcess();
		assertThat(record("merge1").<Double>get(anotherSchema_refCount)).isEqualTo(3.0);
		assertThat(record("merge2").<Double>get(anotherSchema_refCount)).isEqualTo(2.0);
		assertThat(record("merge3").<Double>get(thirdSchema_refCount)).isEqualTo(2.0);

		int nbQueries = clearAggregateMetadatasThenReindexReturningQtyOfQueriesOf(zeSchema, anotherSchema, thirdSchema);
		assertThat(record("merge1").<Double>get(anotherSchema_refCount)).isEqualTo(3.0);
		assertThat(record("merge2").<Double>get(anotherSchema_refCount)).isEqualTo(2.0);
		assertThat(record("merge3").<Double>get(thirdSchema_refCount)).isEqualTo(2.0);
		assertThat(nbQueries).isEqualTo(2);

		tx = new Transaction();
		tx.add(r4.set("ref", "merge1"));
		tx.add(r5.set("ref", "merge1"));
		getModelLayerFactory().newRecordServices().execute(tx);
		waitForBatchProcess();
		assertThat(record("merge1").<Double>get(anotherSchema_refCount)).isEqualTo(5.0);
		assertThat(record("merge2").<Double>get(anotherSchema_refCount)).isEqualTo(0.0);
		assertThat(record("merge3").<Double>get(thirdSchema_refCount)).isEqualTo(2.0);

	}

}
