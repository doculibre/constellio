package com.constellio.model.services.schemas;

import com.constellio.data.dao.dto.records.RecordDTOMode;
import com.constellio.data.dao.dto.records.SolrRecordDTO;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.SchemasSetup;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class RecordsModificationBuilderAcceptTest extends ConstellioTest {

	RecordsModificationBuilder builder;

	@Mock MetadataSchemasManager metadataSchemasManager;
	MetadataSchemaTypes types;

	MetadataSchemaType type1;
	MetadataSchemaType type2;
	MetadataSchemaType type3;
	MetadataSchemaType type4;

	Metadata type1M1;
	Metadata type1Custom1M3;
	Metadata type1Custom2M4;
	Metadata type1M2;
	Metadata type2M1;
	Metadata type2M2;
	Metadata type2M3;
	Metadata type3M1;
	Metadata type4M1;

	RecordImpl aRecordOfType1WithModifiedMetadataM1;
	RecordImpl aRecordOfType1WithModifiedMetadataM2;
	RecordImpl aRecordOfType1WithModifiedCustom1MetadataM3;
	RecordImpl aRecordOfType1WithModifiedCustom2MetadataM3;

	RecordImpl aRecordOfType2WithModifiedMetadataM1AndM2;
	RecordImpl aRecordOfType2WithModifiedMetadataM3;

	RecordImpl aRecordOfType3WithModifiedMetadataM1;
	RecordImpl aRecordOfType3WithoutModifiedMetadata;
	RecordImpl aRecordOfType4WithModifiedMetadataM1;

	@Mock RecordServices recordServices;
	@Mock Record aRecordOfType5WithoutModifiedMetadata;
	List<Record> records;

	@Before
	public void setUp()
			throws Exception {

		builder = new RecordsModificationBuilder(recordServices);

		defineSchemaTypes();

		aRecordOfType1WithModifiedMetadataM1 = savedRecordWithModifiedMetadatas("type1_default",
				"aRecordOfType1WithModifiedMetadataM1", type1M1);
		aRecordOfType1WithModifiedMetadataM2 = savedRecordWithModifiedMetadatas("type1_default",
				"aRecordOfType1WithModifiedMetadataM2", type1M2);
		aRecordOfType1WithModifiedCustom1MetadataM3 = savedRecordWithModifiedMetadatas("type1_custom1",
				"aRecordOfType1WithModifiedCustom1MetadataM3", type1Custom1M3);
		aRecordOfType1WithModifiedCustom2MetadataM3 = savedRecordWithModifiedMetadatas("type1_custom2",
				"aRecordOfType1WithModifiedCustom2MetadataM4", type1Custom2M4);

		aRecordOfType2WithModifiedMetadataM1AndM2 = savedRecordWithModifiedMetadatas("type2_default",
				"aRecordOfType2WithModifiedMetadataM1AndM2", type2M1, type2M2);
		aRecordOfType2WithModifiedMetadataM3 = savedRecordWithModifiedMetadatas("type2_default",
				"aRecordOfType2WithModifiedMetadataM3", type2M3);

		aRecordOfType3WithModifiedMetadataM1 = savedRecordWithModifiedMetadatas("type3_default",
				"aRecordOfType3WithModifiedMetadataM1", type3M1);
		aRecordOfType3WithoutModifiedMetadata = savedRecordWithModifiedMetadatas("type3_default",
				"aRecordOfType3WithoutModifiedMetadata");
		aRecordOfType4WithModifiedMetadataM1 = savedRecordWithModifiedMetadatas("type4_default",
				"aRecordOfType4WithModifiedMetadataM1", type4M1);
		aRecordOfType5WithoutModifiedMetadata = savedRecordWithModifiedMetadatas("type5_default",
				"aRecordOfType5WithoutModifiedMetadata");

		records = Arrays.asList(aRecordOfType1WithModifiedMetadataM1, aRecordOfType1WithModifiedMetadataM2,
				aRecordOfType1WithModifiedCustom1MetadataM3, aRecordOfType1WithModifiedCustom2MetadataM3,
				aRecordOfType2WithModifiedMetadataM1AndM2, aRecordOfType2WithModifiedMetadataM3,
				aRecordOfType3WithModifiedMetadataM1, aRecordOfType3WithoutModifiedMetadata,
				aRecordOfType4WithModifiedMetadataM1, aRecordOfType5WithoutModifiedMetadata);

	}

	@Test
	public void whenBuildRecordModificationsThenBuildModificationsForEachTypes()
			throws Exception {

		List<RecordsModification> recordsModifications = builder.build(new Transaction(records), types);

		assertThat(recordsModifications).hasSize(4);
		assertThat(recordsModifications.get(0).getMetadataSchemaType()).isEqualTo(type1);
		assertThat(recordsModifications.get(0).getModifiedMetadatas())
				.containsOnly(type1M1, type1M2, type1Custom1M3, type1Custom2M4);
		assertThat(recordsModifications.get(0).getRecords()).containsOnly(aRecordOfType1WithModifiedMetadataM1,
				aRecordOfType1WithModifiedMetadataM2, aRecordOfType1WithModifiedCustom1MetadataM3,
				aRecordOfType1WithModifiedCustom2MetadataM3);

		assertThat(recordsModifications.get(1).getMetadataSchemaType()).isEqualTo(type3);
		assertThat(recordsModifications.get(1).getModifiedMetadatas()).containsOnly(type3M1);
		assertThat(recordsModifications.get(1).getRecords()).containsOnly(aRecordOfType3WithModifiedMetadataM1);

		assertThat(recordsModifications.get(2).getMetadataSchemaType()).isEqualTo(type2);
		assertThat(recordsModifications.get(2).getModifiedMetadatas()).containsOnly(type2M1, type2M2, type2M3);
		assertThat(recordsModifications.get(2).getRecords()).containsOnly(aRecordOfType2WithModifiedMetadataM1AndM2,
				aRecordOfType2WithModifiedMetadataM3);

		assertThat(recordsModifications.get(3).getMetadataSchemaType()).isEqualTo(type4);
		assertThat(recordsModifications.get(3).getModifiedMetadatas()).containsOnly(type4M1);
		assertThat(recordsModifications.get(3).getRecords()).containsOnly(aRecordOfType4WithModifiedMetadataM1);
	}

	private RecordImpl savedRecordWithModifiedMetadatas(String schema, String id, Metadata... metadatas) {

		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("collection_s", zeCollection);
		fields.put("schema_s", "zeSchema_default");

		RecordImpl record = spy(new TestRecord(schema, "zeCollection", id));
		record.refresh(1, new SolrRecordDTO(id, 1, null, fields, RecordDTOMode.FULLY_LOADED));

		for (Metadata metadata : metadatas) {
			record.set(metadata, "aNewValue");
		}

		return record;
	}

	private void defineSchemaTypes() {
		RecordsModificationsBuilderAcceptSetup setup = new RecordsModificationsBuilderAcceptSetup("zeCollection");
		define(metadataSchemasManager).using(setup);
		types = metadataSchemasManager.getSchemaTypes("zeCollection");

		doReturn(Arrays.asList("type5", "type1", "type3", "type2", "type4", "type6")).when(
				types).getSchemaTypesSortedByDependency();

		type1 = metadataSchemasManager.getSchemaTypes("zeCollection").getSchemaType("type1");
		type2 = metadataSchemasManager.getSchemaTypes("zeCollection").getSchemaType("type2");
		type3 = metadataSchemasManager.getSchemaTypes("zeCollection").getSchemaType("type3");
		type4 = metadataSchemasManager.getSchemaTypes("zeCollection").getSchemaType("type4");

		type1M1 = type1.getMetadata("type1_default_m1");
		type1M2 = type1.getMetadata("type1_default_m2");
		type1Custom1M3 = type1.getMetadata("type1_custom1_m3");
		type1Custom2M4 = type1.getMetadata("type1_custom2_m4");
		type2M1 = type2.getMetadata("type2_default_m1");
		type2M2 = type2.getMetadata("type2_default_m2");
		type2M3 = type2.getMetadata("type2_default_m3");
		type3M1 = type3.getMetadata("type3_default_m1");
		type4M1 = type4.getMetadata("type4_default_m1");

	}

	protected static class RecordsModificationsBuilderAcceptSetup extends SchemasSetup {

		public RecordsModificationsBuilderAcceptSetup(String collection) {
			super(collection);
		}

		@Override
		public void setUp() {

			MetadataSchemaTypeBuilder type1 = typesBuilder.createNewSchemaType("type1");
			MetadataSchemaTypeBuilder type2 = typesBuilder.createNewSchemaType("type2");
			MetadataSchemaTypeBuilder type3 = typesBuilder.createNewSchemaType("type3");
			MetadataSchemaTypeBuilder type4 = typesBuilder.createNewSchemaType("type4");
			MetadataSchemaTypeBuilder type5 = typesBuilder.createNewSchemaType("type5");
			MetadataSchemaTypeBuilder type6 = typesBuilder.createNewSchemaType("type6");
			type1.getDefaultSchema().create("m1").setType(MetadataValueType.STRING);
			type1.getDefaultSchema().create("m2").setType(MetadataValueType.STRING);
			type1.createCustomSchema("custom1").create("m3").setType(MetadataValueType.STRING);
			type1.createCustomSchema("custom2").create("m4").setType(MetadataValueType.STRING);
			type2.getDefaultSchema().create("m1").setType(MetadataValueType.STRING);
			type2.getDefaultSchema().create("m2").setType(MetadataValueType.STRING);
			type2.getDefaultSchema().create("m3").setType(MetadataValueType.STRING);
			type3.getDefaultSchema().create("m1").setType(MetadataValueType.STRING);
			type4.getDefaultSchema().create("m1").setType(MetadataValueType.STRING);
			type5.getDefaultSchema().create("m1").setType(MetadataValueType.STRING);
			type6.getDefaultSchema().create("m1").setType(MetadataValueType.STRING);

		}
	}
}
