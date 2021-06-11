package com.constellio.model.services.schemas.builders;

import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.ConstellioTest;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MetadataBuilderWithPrepareAcceptanceTest extends ConstellioTest {
	public static final String METADATA_NAME1 = "metadataName1";
	public static final String METADATA_NAME2 = "ametadataName2";

	private MetadataSchemaTypesBuilder typesBuilder;
	private MetadataSchemasManager metadataSchemasManager;

	@Before
	public void setUp() {
		prepareSystem(withZeCollection());

		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		typesBuilder = getMetadataSchemaTypesBuilder();
	}

	@Test
	public void givenNewSchemaAndNewSchemaThenIdCorrespond() throws Exception {
		MetadataSchemaBuilder type1DefaultSchema = typesBuilder.createNewSchemaTypeWithSecurity("type1").getDefaultSchema();
		type1DefaultSchema.createUndeletable("randomName1").setType(MetadataValueType.STRING);
		type1DefaultSchema.createUndeletable("randomName2").setType(MetadataValueType.STRING);
		type1DefaultSchema.createUndeletable(METADATA_NAME1).setType(MetadataValueType.STRING);


		MetadataSchemasManager metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchemaTypes metadataSchemaTypes1 = metadataSchemasManager.saveUpdateSchemaTypes(typesBuilder);

		MetadataSchema metadataSchemaType1DefaultSchema = metadataSchemaTypes1.getSchema(type1DefaultSchema.getCode());

		int defaultSchemaMetadataName1_1 = metadataSchemaType1DefaultSchema.getMetadata(METADATA_NAME1).getId();

		MetadataSchemaTypesBuilder typesBuilder2 = getMetadataSchemaTypesBuilder();
		MetadataSchemaBuilder metadataSchemaBuilder2 = typesBuilder2.getSchema(type1DefaultSchema.getCode());
		metadataSchemaBuilder2.createUndeletable("randomName3").setType(MetadataValueType.STRING);
		metadataSchemaBuilder2.createUndeletable("randomName4").setType(MetadataValueType.STRING);
		metadataSchemaBuilder2.createUndeletable("randomName5").setType(MetadataValueType.STRING);
		metadataSchemaBuilder2.createUndeletable(METADATA_NAME2).setType(MetadataValueType.STRING);

		MetadataSchemaBuilder customSchemaType1 = typesBuilder2.getSchemaType("type1").createCustomSchema("customType1");
		customSchemaType1.createUndeletable("randomName6").setType(MetadataValueType.STRING);
		customSchemaType1.createUndeletable("randomName7").setType(MetadataValueType.STRING);

		MetadataSchemaTypes metadataSchemaTypes2 = metadataSchemasManager.saveUpdateSchemaTypes(typesBuilder2);

		int defaultSchemaMetadataName1_2 = metadataSchemaTypes2.getSchema(type1DefaultSchema.getCode()).getMetadata(METADATA_NAME1).getId();
		int inheritedMetadataId1 = metadataSchemaTypes2.getSchema(customSchemaType1.getCode()).getMetadata(METADATA_NAME1).getId();
		int defaultSchemaMetadataName2Id1 = metadataSchemaTypes2.getSchema(type1DefaultSchema.getCode()).getMetadata(METADATA_NAME2).getId();
		int inheritedMetadataName2Id2 = metadataSchemaTypes2.getSchema(customSchemaType1.getCode()).getMetadata(METADATA_NAME2).getId();

		assertThat(defaultSchemaMetadataName1_1).isEqualTo(defaultSchemaMetadataName1_2);
		assertThat(defaultSchemaMetadataName1_1).isEqualTo(inheritedMetadataId1);
		assertThat(defaultSchemaMetadataName1_1).isNotEqualTo(defaultSchemaMetadataName2Id1);
		assertThat(defaultSchemaMetadataName2Id1).isEqualTo(inheritedMetadataName2Id2);
	}

	@NotNull
	private MetadataSchemaTypesBuilder getMetadataSchemaTypesBuilder() {
		return metadataSchemasManager.modify(zeCollection);
	}
}
