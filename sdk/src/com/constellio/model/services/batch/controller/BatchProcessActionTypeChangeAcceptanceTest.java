package com.constellio.model.services.batch.controller;

import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.batch.actions.ChangeValueOfMetadataBatchProcessAction;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.query.logical.ongoing.OngoingLogicalSearchConditionWithDataStoreFields;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.MetadataSchemaTypesConfigurator;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.Schemas.CODE;
import static com.constellio.model.entities.schemas.Schemas.TITLE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class BatchProcessActionTypeChangeAcceptanceTest extends ConstellioTest {

	TestsSchemasSetup schemas = new TestsSchemasSetup();
	TestsSchemasSetup.ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	AnotherSchemaMetadatas anotherSchema = schemas.new AnotherSchemaMetadatas();
	BatchProcessesManager batchProcessesManager;
	RecordServices recordServices;

	String type1Id = "type1";
	String type2Id = "type2";
	String type3Id = "type3";

	OngoingLogicalSearchConditionWithDataStoreFields fromZeSchemaWhereTitle;

	Metadata type, metadataA, metadataB, metadataC;

	@Test
	public void givenTypeHasLinkedSchemaWhenChangeTypeOfFolderInBatchProcessActionThenAlsoChangeSchema()
			throws Exception {

		setup(true);

		batchProcessesManager.addPendingBatchProcess(fromZeSchemaWhereTitle.isEqualTo("apple"), setTypeTo(type1Id), null);
		waitForBatchProcess();

		assertThat(record("r1").<String>get(type)).isEqualTo(type1Id);
		assertThat(record("r1").getSchemaCode()).isEqualTo("zeSchemaType_custom1");
		assertThat(record("r1").<String>get(metadataA)).isEqualTo("v2");
		assertThat(record("r1").<String>get(metadataB)).isEqualTo("v3");
		assertThat(record("r1").<String>get(metadataC)).isNull();

		assertThat(record("r2").<String>get(type)).isEqualTo(type1Id);
		assertThat(record("r2").getSchemaCode()).isEqualTo("zeSchemaType_custom1");
		assertThat(record("r2").<String>get(metadataA)).isEqualTo("v2");
		assertThat(record("r2").<String>get(metadataB)).isEqualTo("v3");
		assertThat(record("r2").<String>get(metadataC)).isNull();

		assertThat(record("r3").<String>get(type)).isNull();
		assertThat(record("r3").getSchemaCode()).isEqualTo("zeSchemaType_default");
		assertThat(record("r3").<String>get(metadataA)).isEqualTo("v1");
		assertThat(record("r3").<String>get(metadataB)).isNull();
		assertThat(record("r3").<String>get(metadataC)).isNull();

		batchProcessesManager.addPendingBatchProcess(fromZeSchemaWhereTitle.isEqualTo("apple"), setTypeTo(type2Id), null);
		waitForBatchProcess();

		assertThat(record("r1").<String>get(type)).isEqualTo(type2Id);
		assertThat(record("r1").getSchemaCode()).isEqualTo("zeSchemaType_custom2");
		assertThat(record("r1").<String>get(metadataA)).isEqualTo("v1");
		assertThat(record("r1").<String>get(metadataB)).isNull();
		assertThat(record("r1").<String>get(metadataC)).isEqualTo("v4");

		assertThat(record("r2").<String>get(type)).isEqualTo(type2Id);
		assertThat(record("r2").getSchemaCode()).isEqualTo("zeSchemaType_custom2");
		assertThat(record("r2").<String>get(metadataA)).isEqualTo("v1");
		assertThat(record("r2").<String>get(metadataB)).isNull();
		assertThat(record("r2").<String>get(metadataC)).isEqualTo("v4");

		assertThat(record("r3").<String>get(type)).isNull();
		assertThat(record("r3").getSchemaCode()).isEqualTo("zeSchemaType_default");
		assertThat(record("r3").<String>get(metadataA)).isEqualTo("v1");
		assertThat(record("r3").<String>get(metadataB)).isNull();
		assertThat(record("r3").<String>get(metadataC)).isNull();

		batchProcessesManager.addPendingBatchProcess(fromZeSchemaWhereTitle.isEqualTo("apple"), setTypeTo(type3Id), null);
		waitForBatchProcess();

		assertThat(record("r1").<String>get(type)).isEqualTo(type3Id);
		assertThat(record("r1").getSchemaCode()).isEqualTo("zeSchemaType_default");
		assertThat(record("r1").<String>get(metadataA)).isEqualTo("v1");
		assertThat(record("r1").<String>get(metadataB)).isNull();
		assertThat(record("r1").<String>get(metadataC)).isNull();

		assertThat(record("r2").<String>get(type)).isEqualTo(type3Id);
		assertThat(record("r2").getSchemaCode()).isEqualTo("zeSchemaType_default");
		assertThat(record("r2").<String>get(metadataA)).isEqualTo("v1");
		assertThat(record("r2").<String>get(metadataB)).isNull();
		assertThat(record("r2").<String>get(metadataC)).isNull();

		assertThat(record("r3").<String>get(type)).isNull();
		assertThat(record("r3").getSchemaCode()).isEqualTo("zeSchemaType_default");
		assertThat(record("r3").<String>get(metadataA)).isEqualTo("v1");
		assertThat(record("r3").<String>get(metadataB)).isNull();
		assertThat(record("r3").<String>get(metadataC)).isNull();

		batchProcessesManager.addPendingBatchProcess(fromZeSchemaWhereTitle.isEqualTo("apple"), setTypeTo(null), null);
		waitForBatchProcess();

		assertThat(record("r1").<String>get(type)).isNull();
		assertThat(record("r1").getSchemaCode()).isEqualTo("zeSchemaType_default");
		assertThat(record("r1").<String>get(metadataA)).isEqualTo("v1");
		assertThat(record("r1").<String>get(metadataB)).isNull();
		assertThat(record("r1").<String>get(metadataC)).isNull();

		assertThat(record("r2").<String>get(type)).isNull();
		assertThat(record("r2").getSchemaCode()).isEqualTo("zeSchemaType_default");
		assertThat(record("r2").<String>get(metadataA)).isEqualTo("v1");
		assertThat(record("r2").<String>get(metadataB)).isNull();
		assertThat(record("r2").<String>get(metadataC)).isNull();

		assertThat(record("r3").<String>get(type)).isNull();
		assertThat(record("r3").getSchemaCode()).isEqualTo("zeSchemaType_default");
		assertThat(record("r3").<String>get(metadataA)).isEqualTo("v1");
		assertThat(record("r3").<String>get(metadataB)).isNull();
		assertThat(record("r3").<String>get(metadataC)).isNull();

	}

	@Test
	public void givenTypeHasNoLinkedSchemaWhenChangeTypeOfFolderInBatchProcessActionThenAlsoChangeSchema()
			throws Exception {

		setup(false);

		batchProcessesManager.addPendingBatchProcess(fromZeSchemaWhereTitle.isEqualTo("apple"), setTypeTo(type1Id), null);
		waitForBatchProcess();

		assertThat(record("r1").<String>get(type)).isEqualTo(type1Id);
		assertThat(record("r1").getSchemaCode()).isEqualTo("zeSchemaType_default");
		assertThat(record("r1").<String>get(metadataA)).isEqualTo("v1");
		assertThat(record("r1").<String>get(metadataB)).isNull();
		assertThat(record("r1").<String>get(metadataC)).isNull();

		assertThat(record("r2").<String>get(type)).isEqualTo(type1Id);
		assertThat(record("r2").getSchemaCode()).isEqualTo("zeSchemaType_default");
		assertThat(record("r2").<String>get(metadataA)).isEqualTo("v1");
		assertThat(record("r2").<String>get(metadataB)).isNull();
		assertThat(record("r2").<String>get(metadataC)).isNull();

		assertThat(record("r3").<String>get(type)).isNull();
		assertThat(record("r3").getSchemaCode()).isEqualTo("zeSchemaType_default");
		assertThat(record("r3").<String>get(metadataA)).isEqualTo("v1");
		assertThat(record("r3").<String>get(metadataB)).isNull();
		assertThat(record("r3").<String>get(metadataC)).isNull();

	}

	// -------------------------------------------------------------------------------------------------------------------

	private void setup(boolean withLinkedSchema)
			throws Exception {
		prepareSystem(withZeCollection());
		defineSchemasManager().using(schemas.with(twoCustomSchemasAndType(withLinkedSchema)));
		recordServices = getModelLayerFactory().newRecordServices();
		batchProcessesManager = getModelLayerFactory().getBatchProcessesManager();
		setupTypeRecords(withLinkedSchema);

		type = zeSchema.metadata("type");
		metadataA = zeSchema.metadata("metadataA");
		metadataB = zeSchema.type().getSchema("custom1").get("metadataB");
		metadataC = zeSchema.type().getSchema("custom2").get("metadataC");
		fromZeSchemaWhereTitle = from(zeSchema.type()).where(TITLE);

		Record record1 = recordServices.newRecordWithSchema(zeSchema.instance(), "r1").set(TITLE, "apple");
		Record record2 = recordServices.newRecordWithSchema(zeSchema.instance(), "r2").set(TITLE, "apple");
		Record record3 = recordServices.newRecordWithSchema(zeSchema.instance(), "r3").set(TITLE, "orange");
		recordServices.execute(new Transaction(record1, record2, record3));
	}

	private BatchProcessAction setTypeTo(String type1Id) {
		Map<String, Object> values = new HashMap<>();
		values.put("zeSchemaType_default_type", type1Id);
		return new ChangeValueOfMetadataBatchProcessAction(values);
	}

	private void setupTypeRecords(boolean withLinkedSchema)
			throws Exception {

		Record type1 = new TestRecord(anotherSchema, type1Id).set(CODE, "code1").set(TITLE, "title1");
		Record type2 = new TestRecord(anotherSchema, type2Id).set(CODE, "code2").set(TITLE, "title2");
		Record type3 = new TestRecord(anotherSchema, type3Id).set(CODE, "code3").set(TITLE, "title3");

		if (withLinkedSchema) {
			type1.set(anotherSchema.metadata("linkedSchema"), "custom1");
			type2.set(anotherSchema.metadata("linkedSchema"), "custom2");
		}

		recordServices.execute(new Transaction(asList(type1, type2, type3)));
	}

	private MetadataSchemaTypesConfigurator twoCustomSchemasAndType(final boolean linkedSchema) {
		return new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder anotherSchema = schemaTypes.getSchemaType("anotherSchemaType");
				schemaTypes.getSchema("zeSchemaType_default").create("type").defineReferencesTo(anotherSchema);
				MetadataSchemaBuilder custom1 = schemaTypes.getSchemaType("zeSchemaType").createCustomSchema("custom1");
				MetadataSchemaBuilder custom2 = schemaTypes.getSchemaType("zeSchemaType").createCustomSchema("custom2");

				schemaTypes.getSchema("zeSchemaType_default").create("metadataA").setType(STRING).setDefaultValue("v1");
				custom1.get("metadataA").setDefaultValue("v2");
				custom1.create("metadataB").setType(STRING).setDefaultValue("v3");
				custom2.create("metadataC").setType(STRING).setDefaultValue("v4");

				anotherSchema.getDefaultSchema().create("code").setType(STRING);
				if (linkedSchema) {
					anotherSchema.getDefaultSchema().create("linkedSchema").setType(STRING);
				}
			}
		};
	}

}
