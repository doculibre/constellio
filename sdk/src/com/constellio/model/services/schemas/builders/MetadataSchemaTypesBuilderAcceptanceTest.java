package com.constellio.model.services.schemas.builders;

import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.ConstellioTest;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MetadataSchemaTypesBuilderAcceptanceTest extends ConstellioTest {
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
		typesBuilder.createNewSchemaTypeWithSecurity("type1");

		MetadataSchemasManager metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchemaTypes metadataSchemaTypes1 = metadataSchemasManager.saveUpdateSchemaTypes(typesBuilder);

		int id1 = metadataSchemaTypes1.getSchemaType("type1").getId();

		MetadataSchemaTypesBuilder typesBuilder2 = getMetadataSchemaTypesBuilder();
		typesBuilder2.createNewSchemaTypeWithSecurity("type2");
		MetadataSchemaTypes metadataSchemaTypes2 = metadataSchemasManager.saveUpdateSchemaTypes(typesBuilder2);


		int id2 = metadataSchemaTypes2.getSchemaType("type2").getId();
		int id3 = metadataSchemaTypes2.getSchemaType("type1").getId();

		assertThat(id1).isEqualTo(id3);
		assertThat(id1).isNotEqualTo(id2);
	}

	@NotNull
	private MetadataSchemaTypesBuilder getMetadataSchemaTypesBuilder() {
		return metadataSchemasManager.modify(zeCollection);
	}
}
