package com.constellio.model.services.records.aggregations;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
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
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class LogicalAndMetadaAggregationHandlerAcceptanceTest extends ConstellioTest {

	private ZeSchemaMetadatas zeSchema;
	private AnotherSchemaMetadatas anotherSchema;
	private ThirdSchemaMetadatas thirdSchema;

	private RMSchemasRecordsServices rm;
	private RecordServices recordServices;

	@Before
	public void setUp() throws Exception {
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();

		TestsSchemasSetup schemas = new TestsSchemasSetup(zeCollection);
		zeSchema = schemas.new ZeSchemaMetadatas();
		anotherSchema = schemas.new AnotherSchemaMetadatas();
		thirdSchema = schemas.new ThirdSchemaMetadatas();

		final String zeSchemaTypeCode = zeSchema.typeCode();
		final String anotherSchemaTypeCode = anotherSchema.typeCode();
		final String thirdSchemaTypeCode = thirdSchema.typeCode();

		//prepareSystem(withZeCollection().withConstellioRMModule());
		givenBackgroundThreadsEnabled();
		defineSchemasManager().using(schemas.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder zeType = schemaTypes.getSchemaType(zeSchemaTypeCode);
				MetadataSchemaTypeBuilder anotherType = schemaTypes.getSchemaType(anotherSchemaTypeCode);
				MetadataSchemaTypeBuilder thirdType = schemaTypes.getSchemaType(thirdSchemaTypeCode);

				MetadataBuilder anotherType_ref = anotherType.createMetadata("ref").defineReferencesTo(zeType);
				MetadataBuilder anotherType_boolean = anotherType.createMetadata("boolean").setType(BOOLEAN)
						.setMultivalue(false);
				MetadataBuilder thirdType_ref = thirdType.createMetadata("ref").defineReferencesTo(zeType);
				MetadataBuilder thirdType_boolean = thirdType.createMetadata("boolean").setType(BOOLEAN)
						.setMultivalue(false);

				Map<MetadataBuilder, List<MetadataBuilder>> metadatasByRefMetadata = new HashMap<>();
				metadatasByRefMetadata.put(anotherType_ref, singletonList(anotherType_boolean));
				metadatasByRefMetadata.put(thirdType_ref, singletonList(thirdType_boolean));
				zeType.createMetadata("boolean").defineDataEntry().asAggregatedAnd(metadatasByRefMetadata);
			}
		}));

		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeSchema, "zeSchema1"));
		transaction.add(new TestRecord(anotherSchema, "anotherSchema1").set("ref", "zeSchema1"));
		transaction.add(new TestRecord(thirdSchema, "thirdSchema1").set("ref", "zeSchema1"));
		recordServices.execute(transaction);
	}

	@Test
	public void givenAnotherSchemaIsTrueAndThirdSchemaIsTrueThenZeSchemaIsTrue() throws Exception {
		Record record = recordServices.getDocumentById("anotherSchema1");
		record.set(anotherSchema.metadata("boolean"), true);

		Transaction transaction = new Transaction();
		transaction.add(rm.get("anotherSchema1").set(anotherSchema.metadata("boolean"), true));
		transaction.add(rm.get("thirdSchema1").set(thirdSchema.metadata("boolean"), true));
		recordServices.execute(transaction);
		waitForBatchProcess();

		assertThat(rm.get("zeSchema1").<Boolean>get(zeSchema.metadata("boolean"))).isTrue();
	}

	@Test
	public void givenAnotherSchemaIsFalseAndThirdSchemaIsTrueThenZeSchemaIsFalse() throws Exception {
		Transaction transaction = new Transaction();
		transaction.add(recordServices.getDocumentById("anotherSchema1").set(anotherSchema.metadata("boolean"), false));
		transaction.add(recordServices.getDocumentById("thirdSchema1").set(thirdSchema.metadata("boolean"), true));
		recordServices.execute(transaction);
		waitForBatchProcess();

		assertThat(recordServices.getDocumentById("zeSchema1").<Boolean>get(zeSchema.metadata("boolean"))).isFalse();
	}

	@Test
	public void givenAnotherSchemaIsTrueAndThirdSchemaIsFalseThenZeSchemaIsFalse() throws Exception {
		Transaction transaction = new Transaction();
		transaction.add(recordServices.getDocumentById("anotherSchema1").set(anotherSchema.metadata("boolean"), true));
		transaction.add(recordServices.getDocumentById("thirdSchema1").set(thirdSchema.metadata("boolean"), false));
		recordServices.execute(transaction);
		waitForBatchProcess();

		assertThat(recordServices.getDocumentById("zeSchema1").<Boolean>get(zeSchema.metadata("boolean"))).isFalse();
	}

	@Test
	public void givenAnotherSchemaIsTrueAndAnotherSchemaIsFalseAndThirdSchemaIsTrueThenZeSchemaIsFalse()
			throws Exception {
		Transaction transaction = new Transaction();
		transaction.add(recordServices.getDocumentById("anotherSchema1").set(anotherSchema.metadata("boolean"), true));
		transaction.add(new TestRecord(anotherSchema, "anotherSchema2").set("ref", "zeSchema1")
				.set(anotherSchema.metadata("boolean"), false));
		transaction.add(recordServices.getDocumentById("thirdSchema1").set(thirdSchema.metadata("boolean"), true));
		recordServices.execute(transaction);
		waitForBatchProcess();

		assertThat(recordServices.getDocumentById("zeSchema1").<Boolean>get(zeSchema.metadata("boolean"))).isFalse();
	}

}
