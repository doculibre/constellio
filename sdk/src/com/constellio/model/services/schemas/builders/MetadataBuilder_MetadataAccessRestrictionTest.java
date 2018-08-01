package com.constellio.model.services.schemas.builders;

import com.constellio.model.entities.schemas.MetadataValueType;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MetadataBuilder_MetadataAccessRestrictionTest extends MetadataBuilderTest {

	@Test
	public void whenBuildingMetadataWithoutInheritanceThenSetAccessRestrictions()
			throws Exception {

		metadataWithoutInheritanceBuilder.setType(MetadataValueType.STRING).defineAccessRestrictions()
										 .withRequiredReadRole("zeRole");

		build();

		assertThat(metadataWithoutInheritance.getAccessRestrictions().getRequiredReadRoles()).containsOnly("zeRole");
	}

	@Test
	public void whenModifyingMetadataWithoutInheritanceThenSetAccessRestrictions()
			throws Exception {

		metadataWithoutInheritanceBuilder.setType(MetadataValueType.STRING).defineAccessRestrictions()
										 .withRequiredReadRole("zeRole");

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.defineAccessRestrictions().getRequiredReadRoles()).containsOnly("zeRole");
	}

	@Test
	public void whenBuildingMetadataWithInheritanceThenSetAccessRestrictionsConfiguredInInheritedMetadata()
			throws Exception {

		inheritedMetadataBuilder.setType(MetadataValueType.STRING).defineAccessRestrictions()
								.withRequiredReadRole("zeRole");

		build();

		assertThat(metadataWithInheritance.getAccessRestrictions().getRequiredReadRoles()).containsOnly("zeRole");
	}

	@Test
	public void whenModifyingMetadataWithInheritanceThenSetNull()
			throws Exception {

		metadataWithoutInheritanceBuilder.setType(MetadataValueType.STRING).defineAccessRestrictions()
										 .withRequiredReadRole("zeRole");

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.defineAccessRestrictions()).isNull();
	}
}
