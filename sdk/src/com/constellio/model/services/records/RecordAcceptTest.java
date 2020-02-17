package com.constellio.model.services.records;

import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.structures.MapStringListStringStructureFactory;
import com.constellio.model.entities.structures.MapStringStringStructure;
import com.constellio.model.entities.structures.MapStringStringStructureFactory;
import com.constellio.model.services.schemas.MetadataListFilter;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.MetadataSchemaTypesConfigurator;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ThirdSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import org.junit.Test;

import java.util.ArrayList;

import static com.constellio.model.entities.schemas.MetadataValueType.ENUM;
import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.MetadataValueType.STRUCTURE;
import static com.constellio.sdk.tests.TestUtils.assertThatRecord;
import static com.constellio.sdk.tests.TestUtils.mockManualMetadata;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class RecordAcceptTest extends ConstellioTest {

	TestsSchemasSetup setup = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = setup.new ZeSchemaMetadatas();
	AnotherSchemaMetadatas anotherSchema = setup.new AnotherSchemaMetadatas();
	ThirdSchemaMetadatas thirdSchema = setup.new ThirdSchemaMetadatas();

	@Test
	public void whenChangingSchemasWithoutLosingMetadataValuesThenReturnFalse()
			throws Exception {

		defineSchemasManager().using(setup.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {

				MetadataSchemaTypeBuilder anotherSchemaTypeBuilder = schemaTypes.getSchemaType("anotherSchemaType");
				MetadataSchemaTypeBuilder aThirdSchemaTypeBuilder = schemaTypes.getSchemaType("aThirdSchemaType");

				MetadataSchemaBuilder schema1 = schemaTypes.getSchemaType("zeSchemaType").createCustomSchema("schema1");
				MetadataSchemaBuilder schema2 = schemaTypes.getSchemaType("zeSchemaType").createCustomSchema("schema2");

				schema1.create("meta1").setType(STRING);
				schema1.create("meta2").setType(STRING);
				schema1.create("meta4").setType(STRING);
				schema1.create("meta5").setType(STRING);
				schema1.create("meta6").setType(STRING).setMultivalue(true);
				schema1.create("meta7").setType(STRING).setMultivalue(true);
				schema1.create("meta8").setType(REFERENCE).defineReferencesTo(anotherSchemaTypeBuilder);
				schema1.create("meta9").setType(REFERENCE).defineReferencesTo(anotherSchemaTypeBuilder);
				schema1.create("meta10").setType(ENUM).defineAsEnum(FolderStatus.class);
				schema1.create("meta11").setType(ENUM).defineAsEnum(CopyType.class);
				schema1.create("meta12").setType(STRUCTURE).defineStructureFactory(MapStringStringStructureFactory.class);
				schema1.create("meta13").setType(STRUCTURE).defineStructureFactory(MapStringStringStructureFactory.class);

				schema2.create("meta2").setType(STRING);
				schema2.create("meta3").setType(STRING);
				schema2.create("meta4").setType(MetadataValueType.BOOLEAN);
				schema2.create("meta5").setType(STRING).setMultivalue(true);
				schema2.create("meta6").setType(STRING);
				schema2.create("meta7").setType(STRING).setMultivalue(true);
				schema2.create("meta8").setType(REFERENCE).defineReferencesTo(anotherSchemaTypeBuilder);
				schema2.create("meta9").setType(REFERENCE).defineReferencesTo(aThirdSchemaTypeBuilder);
				schema2.create("meta10").setType(ENUM).defineAsEnum(FolderStatus.class);
				schema2.create("meta11").setType(ENUM).defineAsEnum(FolderStatus.class);
				schema2.create("meta12").setType(STRUCTURE).defineStructureFactory(MapStringListStringStructureFactory.class);
				schema2.create("meta13").setType(STRUCTURE).defineStructureFactory(MapStringStringStructureFactory.class);

			}
		}));

		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(anotherSchema, "anotherSchemaRecordId"));
		transaction.add(new TestRecord(thirdSchema, "thirdSchemaRecordId"));
		recordServices.execute(transaction);

		MetadataSchema schema1 = setup.getTypes().getSchema("zeSchemaType_schema1");
		MetadataSchema schema2 = setup.getTypes().getSchema("zeSchemaType_schema2");

		Record record = new RecordImpl(schema1, "zeId");

		record.set(schema1.get("meta2"), "23");
		record.set(schema1.get("meta7"), asList("78", "89"));
		record.set(schema1.get("meta8"), "anotherSchemaRecordId");
		record.set(schema1.get("meta10"), FolderStatus.ACTIVE);
		record.set(schema1.get("meta13"), new MapStringStringStructure().with("key3", "value3").with("key4", "value4"));

		assertThat(record.changeSchema(schema1, schema2)).isFalse();

	}

	@Test
	public void whenChangingSchemasThenKeepSameMetadata()
			throws Exception {

		defineSchemasManager().using(setup.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {

				MetadataSchemaTypeBuilder anotherSchemaTypeBuilder = schemaTypes.getSchemaType("anotherSchemaType");
				MetadataSchemaTypeBuilder aThirdSchemaTypeBuilder = schemaTypes.getSchemaType("aThirdSchemaType");

				MetadataSchemaBuilder schema1 = schemaTypes.getSchemaType("zeSchemaType").createCustomSchema("schema1");
				MetadataSchemaBuilder schema2 = schemaTypes.getSchemaType("zeSchemaType").createCustomSchema("schema2");

				schema1.create("meta1").setType(STRING);
				schema1.create("meta2").setType(STRING);
				schema1.create("meta4").setType(STRING);
				schema1.create("meta5").setType(STRING);
				schema1.create("meta6").setType(STRING).setMultivalue(true);
				schema1.create("meta7").setType(STRING).setMultivalue(true);
				schema1.create("meta8").setType(REFERENCE).defineReferencesTo(anotherSchemaTypeBuilder);
				schema1.create("meta9").setType(REFERENCE).defineReferencesTo(anotherSchemaTypeBuilder);
				schema1.create("meta10").setType(ENUM).defineAsEnum(FolderStatus.class);
				schema1.create("meta11").setType(ENUM).defineAsEnum(CopyType.class);
				schema1.create("meta12").setType(STRUCTURE).defineStructureFactory(MapStringStringStructureFactory.class);
				schema1.create("meta13").setType(STRUCTURE).defineStructureFactory(MapStringStringStructureFactory.class);

				schema2.create("meta2").setType(STRING);
				schema2.create("meta3").setType(STRING);
				schema2.create("meta4").setType(MetadataValueType.BOOLEAN);
				schema2.create("meta5").setType(STRING).setMultivalue(true);
				schema2.create("meta6").setType(STRING);
				schema2.create("meta7").setType(STRING).setMultivalue(true);
				schema2.create("meta8").setType(REFERENCE).defineReferencesTo(anotherSchemaTypeBuilder);
				schema2.create("meta9").setType(REFERENCE).defineReferencesTo(aThirdSchemaTypeBuilder);
				schema2.create("meta10").setType(ENUM).defineAsEnum(FolderStatus.class);
				schema2.create("meta11").setType(ENUM).defineAsEnum(FolderStatus.class);
				schema2.create("meta12").setType(STRUCTURE).defineStructureFactory(MapStringListStringStructureFactory.class);
				schema2.create("meta13").setType(STRUCTURE).defineStructureFactory(MapStringStringStructureFactory.class);

			}
		}));

		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(anotherSchema, "anotherSchemaRecordId"));
		transaction.add(new TestRecord(thirdSchema, "thirdSchemaRecordId"));
		recordServices.execute(transaction);

		MetadataSchema schema1 = setup.getTypes().getSchema("zeSchemaType_schema1");
		MetadataSchema schema2 = setup.getTypes().getSchema("zeSchemaType_schema2");

		Metadata inexistentMetadata1InSchema2 = mockManualMetadata("zeSchemaType_schema1_meta1", STRING);
		Metadata inexistentMetadata3InSchema1 = mockManualMetadata("type_schema1_meta3", STRING);

		Record record = new RecordImpl(schema1, "zeId");

		record.set(schema1.get("meta1"), "12");
		record.set(schema1.get("meta2"), "23");
		record.set(schema1.get("meta4"), "34");
		record.set(schema1.get("meta5"), "45");
		record.set(schema1.get("meta6"), asList("56", "67"));
		record.set(schema1.get("meta7"), asList("78", "89"));
		record.set(schema1.get("meta8"), "anotherSchemaRecordId");
		record.set(schema1.get("meta9"), "anotherSchemaRecordId");
		record.set(schema1.get("meta10"), FolderStatus.ACTIVE);
		record.set(schema1.get("meta11"), CopyType.PRINCIPAL);
		record.set(schema1.get("meta12"), new MapStringStringStructure().with("key1", "value1").with("key2", "value2"));
		record.set(schema1.get("meta13"), new MapStringStringStructure().with("key3", "value3").with("key4", "value4"));

		assertThat(record.changeSchema(schema1, schema2)).isTrue();

		assertThatRecord(record)
				.hasNoMetadataValue(inexistentMetadata1InSchema2)
				.hasMetadataValue(schema2.get("meta2"), "23")
				.hasNoMetadataValue(schema2.get("meta3"))
				.hasNoMetadataValue(schema2.get("meta4"))
				.hasNoMetadataValue(schema2.get("meta5"))
				.hasNoMetadataValue(schema2.get("meta6"))
				.hasMetadataValue(schema2.get("meta7"), asList("78", "89"))
				.hasMetadataValue(schema2.get("meta8"), "anotherSchemaRecordId")
				.hasNoMetadataValue(schema2.get("meta9"))
				.hasMetadataValue(schema2.get("meta10"), FolderStatus.ACTIVE)
				.hasNoMetadataValue(schema2.get("meta11"))
				.hasNoMetadataValue(schema2.get("meta12"))
				.hasMetadataValue(schema2.get("meta13"),
						new MapStringStringStructure().with("key3", "value3").with("key4", "value4"));

		assertThat(record.getModifiedMetadatas(setup.getTypes()).only(startingWithMeta)).extracting("localCode")
				.containsOnly("meta7", "meta8", "meta10", "meta13", "meta2");
		record.set(schema2.get("meta3"), "34");
		record.changeSchema(schema2, schema2);

		assertThatRecord(record)
				.hasNoMetadataValue(inexistentMetadata1InSchema2)
				.hasNoMetadataValue(schema1.get("meta1"))
				.hasMetadataValue(schema2.get("meta2"), "23")
				.hasMetadataValue(schema2.get("meta3"), "34")
				.hasMetadataValue(schema2.get("meta7"), asList("78", "89"))
				.hasMetadataValue(schema2.get("meta8"), "anotherSchemaRecordId");

		assertThat(record.changeSchema(schema2, schema1)).isTrue();

		assertThat(record.getModifiedMetadatas(setup.getTypes()).only(startingWithMeta)).extracting("localCode")
				.containsOnly("meta8", "meta10", "meta13", "meta2", "meta7");
		assertThatRecord(record)
				.hasNoMetadataValue(schema1.get("meta1"))
				.hasMetadataValue(schema1.get("meta2"), "23")
				.hasNoMetadataValue(inexistentMetadata3InSchema1)
				.hasMetadataValue(schema2.get("meta7"), asList("78", "89"))
				.hasMetadataValue(schema2.get("meta8"), "anotherSchemaRecordId")
				.hasMetadataValue(schema2.get("meta10"), FolderStatus.ACTIVE);
	}

	@Test
	public void givenValueIsEqualToDefaultValueWhenChangingSchemaThenSetToNewDefaultValue() {

		defineSchemasManager().using(setup.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {

				MetadataSchemaBuilder schema1 = schemaTypes.getSchemaType("zeSchemaType").createCustomSchema("schema1");
				MetadataSchemaBuilder schema2 = schemaTypes.getSchemaType("zeSchemaType").createCustomSchema("schema2");

				schema1.create("meta1").setType(STRING).setDefaultValue("A");
				schema1.create("meta2").setType(STRING).setDefaultValue("A");
				schema1.create("meta3").setType(STRING).setDefaultValue("A");
				schema1.create("meta4").setType(NUMBER).setDefaultValue(1.0);
				schema1.create("meta5").setType(NUMBER).setDefaultValue(1.0);
				schema1.create("meta6").setType(NUMBER).setDefaultValue(1.0);
				schema1.create("meta7").setType(STRING).setDefaultValue("A");
				schema1.create("meta8").setType(STRING).setMultivalue(true).setDefaultValue(asList("value1", "value2"));
				schema1.create("meta9").setType(STRING).setMultivalue(true).setDefaultValue(asList("value1", "value2"));
				schema1.create("meta10").setType(STRING).setMultivalue(true).setDefaultValue(asList("value1", "value2"));
				schema1.create("meta11").setType(STRING).setMultivalue(true).setDefaultValue(asList("value1", "value2"));

				schema2.create("meta1").setType(STRING).setDefaultValue("B");
				schema2.create("meta2").setType(STRING).setDefaultValue("B");
				schema2.create("meta3").setType(STRING).setDefaultValue("B");
				schema2.create("meta4").setType(NUMBER).setDefaultValue(2.0);
				schema2.create("meta5").setType(NUMBER).setDefaultValue(2.0);
				schema2.create("meta6").setType(NUMBER).setDefaultValue(2.0);
				schema2.create("meta7").setType(STRING).setDefaultValue("B");
				schema2.create("meta8").setType(STRING).setMultivalue(true).setDefaultValue(asList("value3", "value4"));
				schema2.create("meta9").setType(STRING).setMultivalue(true).setDefaultValue(asList("value3", "value4"));
				schema2.create("meta10").setType(STRING).setMultivalue(true).setDefaultValue(asList("value3", "value4"));
				schema2.create("meta11").setType(STRING).setMultivalue(true).setDefaultValue(asList("value3", "value4"));

			}
		}));

		MetadataSchema schema1 = setup.getTypes().getSchema("zeSchemaType_schema1");
		MetadataSchema schema2 = setup.getTypes().getSchema("zeSchemaType_schema2");

		Record record = new RecordImpl(schema1, "zeId");

		record.set(schema1.get("meta1"), "A");
		record.set(schema1.get("meta2"), "custom");
		record.set(schema1.get("meta3"), null);
		record.set(schema1.get("meta4"), 1.0);
		record.set(schema1.get("meta5"), 42.666);
		record.set(schema1.get("meta6"), null);
		record.set(schema1.get("meta7"), " ");
		record.set(schema1.get("meta8"), asList("value1", "value2"));
		record.set(schema1.get("meta9"), asList("value5", "value6"));
		record.set(schema1.get("meta10"), new ArrayList<>());
		record.set(schema1.get("meta11"), null);

		assertThat(record.changeSchema(schema1, schema2)).isFalse();

		assertThatRecord(record)
				.hasMetadataValue(schema2.get("meta1"), "B")
				.hasMetadataValue(schema2.get("meta2"), "custom")
				.hasMetadataValue(schema2.get("meta3"), "B")
				.hasMetadataValue(schema2.get("meta4"), 2.0)
				.hasMetadataValue(schema2.get("meta5"), 42.666)
				.hasMetadataValue(schema2.get("meta6"), 2.0)
				.hasMetadataValue(schema2.get("meta7"), "B")
				.hasMetadataValue(schema2.get("meta8"), asList("value3", "value4"))
				.hasMetadataValue(schema2.get("meta9"), asList("value5", "value6"))
				.hasMetadataValue(schema2.get("meta10"), asList("value3", "value4"))
				.hasMetadataValue(schema2.get("meta11"), asList("value3", "value4"));

		assertThat(record.changeSchema(schema2, schema1)).isFalse();

		assertThatRecord(record)
				.hasMetadataValue(schema2.get("meta1"), "A")
				.hasMetadataValue(schema2.get("meta2"), "custom")
				.hasMetadataValue(schema2.get("meta3"), "A")
				.hasMetadataValue(schema2.get("meta4"), 1.0)
				.hasMetadataValue(schema2.get("meta5"), 42.666)
				.hasMetadataValue(schema2.get("meta6"), 1.0)
				.hasMetadataValue(schema2.get("meta7"), "A")
				.hasMetadataValue(schema2.get("meta8"), asList("value1", "value2"))
				.hasMetadataValue(schema2.get("meta9"), asList("value5", "value6"))
				.hasMetadataValue(schema2.get("meta10"), asList("value1", "value2"))
				.hasMetadataValue(schema2.get("meta11"), asList("value1", "value2"));

	}

	MetadataListFilter startingWithMeta = new MetadataListFilter() {
		@Override
		public boolean isReturned(Metadata metadata) {
			return metadata.getLocalCode().startsWith("meta");
		}
	};

}
