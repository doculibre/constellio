package com.constellio.model.services.schemas.builders;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MetadataBuilder_RequirementTest extends MetadataBuilderTest {

	@Test
	public void givenDefaultRequirementFlagOfMetadataWithoutInheritanceIsNullWhenBuildingThenSetToFalse() {
		metadataWithoutInheritanceBuilder.setType(STRING).setDefaultRequirement(null);

		build();

		assertThat(metadataWithoutInheritance.isDefaultRequirement()).isFalse();
	}

	@Test
	public void givenDefaultRequirementFlagOfMetadataWithoutInheritanceIsNullWhenModifyingThenSetToFalse() {
		metadataWithoutInheritanceBuilder.setType(STRING).setDefaultRequirement(null);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getDefaultRequirement()).isFalse();
	}

	@Test
	public void givenDefaultRequirementFlagOfMetadataWithoutInheritanceIsNotDefinedWhenBuildingThenSetToFalse() {
		metadataWithoutInheritanceBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.isDefaultRequirement()).isFalse();
	}

	@Test
	public void givenDefaultRequirementFlagOfMetadataWithoutInheritanceIsTrueWhenBuildingThenSetToTrue() {
		metadataWithoutInheritanceBuilder.setType(STRING).setDefaultRequirement(true);

		build();

		assertThat(metadataWithoutInheritanceBuilder.getDefaultRequirement()).isTrue();
	}

	@Test
	public void givenDefaultRequirementFlagOfMetadataWithoutInheritanceIsTrueWhenModifyingThenSetToTrue() {
		metadataWithoutInheritanceBuilder.setType(STRING).setDefaultRequirement(true);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getDefaultRequirement()).isTrue();
	}

	@Test
	public void givenDefaultRequirementFlagOfMetadataWithInheritanceIsDifferentWhenBuildingThenSetToCustomizedValue() {
		inheritedMetadataBuilder.setType(STRING).setDefaultRequirement(false);
		metadataWithInheritanceBuilder.setDefaultRequirement(true);

		build();

		assertThat(inheritedMetadata.isDefaultRequirement()).isFalse();
		assertThat(metadataWithInheritance.isDefaultRequirement()).isTrue();
	}

	@Test
	public void givenDefaultRequirementFlagOfMetadataWithInheritanceIsDifferentWhenModifyingThenSetToCustomizedValue() {
		inheritedMetadataBuilder.setType(STRING).setDefaultRequirement(false);
		metadataWithInheritanceBuilder.setDefaultRequirement(true);

		buildAndModify();

		assertThat(inheritedMetadataBuilder.getDefaultRequirement()).isFalse();
		assertThat(metadataWithInheritanceBuilder.getDefaultRequirement()).isTrue();
	}

	@Test
	public void givenDefaultRequirementFlagOfMetadataWithInheritanceIsNullWhenBuildingThenSetToInheritedValue() {
		inheritedMetadataBuilder.setType(STRING).setDefaultRequirement(false);
		metadataWithInheritanceBuilder.setDefaultRequirement(null);

		build();

		assertThat(inheritedMetadata.isDefaultRequirement()).isFalse();
		assertThat(metadataWithInheritance.isDefaultRequirement()).isFalse();
	}

	@Test
	public void givenDefaultRequirementFlagOfMetadataWithInheritanceIsNullWhenModifyingThenSetToNull() {
		inheritedMetadataBuilder.setType(STRING).setDefaultRequirement(false);
		metadataWithInheritanceBuilder.setDefaultRequirement(null);

		buildAndModify();

		assertThat(inheritedMetadataBuilder.getDefaultRequirement()).isFalse();
		assertThat(metadataWithInheritanceBuilder.getDefaultRequirement()).isNull();
	}

	@Test
	public void givenDefaultRequirementFlagOfMetadataWithInheritanceIsSameAsInheritedWhenModifyingThenSetToNull() {
		inheritedMetadataBuilder.setType(STRING).setDefaultRequirement(true);
		metadataWithInheritanceBuilder.setDefaultRequirement(true);

		buildAndModify();

		assertThat(inheritedMetadataBuilder.getDefaultRequirement()).isTrue();
		assertThat(metadataWithInheritanceBuilder.getDefaultRequirement()).isNull();
	}

}
