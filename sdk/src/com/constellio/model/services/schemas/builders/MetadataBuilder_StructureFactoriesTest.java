package com.constellio.model.services.schemas.builders;

import static com.constellio.model.entities.schemas.MetadataValueType.CONTENT;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.constellio.model.services.contents.ContentFactory;
import com.constellio.model.services.schemas.testimpl.TestStructureFactory1;

public class MetadataBuilder_StructureFactoriesTest extends MetadataBuilderTest {

	@Test
	public void givenRecordMetadataStructureFactoryDefinedInInheritedMetadataWhenBuildingThenInherited()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).defineStructureFactory(TestStructureFactory1.class);

		build();

		assertThat(inheritedMetadata.getStructureFactory().getClass()).isEqualTo(TestStructureFactory1.class);
		assertThat(metadataWithInheritance.getStructureFactory().getClass()).isEqualTo(TestStructureFactory1.class);

	}

	@Test
	public void givenRecordMetadataStructureFactoryDefinedInInheritedMetadataWhenModifyingThenInherited()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).defineStructureFactory(TestStructureFactory1.class);

		buildAndModify();

		assertThat(inheritedMetadataBuilder.getStructureFactory()).isEqualTo(TestStructureFactory1.class);
		assertThat(metadataWithInheritanceBuilder.getStructureFactory()).isEqualTo(TestStructureFactory1.class);

	}

	@Test
	public void givenContentTypeMetadataThenHasContentInfoFactory()
			throws Exception {
		inheritedMetadataBuilder.setType(CONTENT);

		buildAndModify();

		assertThat(inheritedMetadataBuilder.getStructureFactory()).isEqualTo(ContentFactory.class);
		assertThat(metadataWithInheritanceBuilder.getStructureFactory()).isEqualTo(ContentFactory.class);

	}

}
