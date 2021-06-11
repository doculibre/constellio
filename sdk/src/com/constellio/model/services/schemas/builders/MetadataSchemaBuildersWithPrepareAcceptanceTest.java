package com.constellio.model.services.schemas.builders;

import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.ConstellioTest;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MetadataSchemaBuildersWithPrepareAcceptanceTest extends ConstellioTest {
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


		MetadataSchemasManager metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchemaTypes metadataSchemaTypes1 = metadataSchemasManager.saveUpdateSchemaTypes(typesBuilder);

		MetadataSchema metadataSchemaType1DefaultSchema = metadataSchemaTypes1.getSchema(type1DefaultSchema.getCode());

		int type1DefaultSchemaId1 = metadataSchemaType1DefaultSchema.getId();

		MetadataSchemaTypesBuilder typesBuilder2 = getMetadataSchemaTypesBuilder();
		MetadataSchemaBuilder customSchemaType1 = typesBuilder2.getSchemaType("type1").createCustomSchema("customType1");
		MetadataSchemaTypes metadataSchemaTypes2 = metadataSchemasManager.saveUpdateSchemaTypes(typesBuilder2);

		int type1DefaultSchemaId2 = metadataSchemaTypes2.getSchema(type1DefaultSchema.getCode()).getId();
		int customSchemaType1_2 = metadataSchemaTypes2.getSchema(customSchemaType1.getCode()) .getId();

		assertThat(type1DefaultSchemaId1).isEqualTo(type1DefaultSchemaId2);
		assertThat(type1DefaultSchemaId1).isNotEqualTo(customSchemaType1_2);
	}

	@NotNull
	private MetadataSchemaTypesBuilder getMetadataSchemaTypesBuilder() {
		return metadataSchemasManager.modify(zeCollection);
	}
}
