package com.constellio.model.services.schemas.builders;

import org.junit.Test;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

public class MetadataBuilder_UniqueValueFlagTest extends MetadataBuilderTest {

	@Test
	public void givenUniqueValueFlagUndefinedOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.isUniqueValue()).isFalse();
	}

	@Test
	public void givenUniqueValueFlagUndefinedOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isUniqueValue()).isFalse();
	}

	@Test
	public void givenUniqueValueFlagSetToFalseOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setUniqueValue(false);

		build();

		assertThat(metadataWithoutInheritance.isUniqueValue()).isFalse();
	}

	@Test
	public void givenUniqueValueFlagSetToFalseOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setUniqueValue(false);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isUniqueValue()).isFalse();
	}

	@Test
	public void givenUniqueValueFlagSetToTrueOnMetadataWithoutInheritanceWhenBuildingThenUniqueValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setUniqueValue(true);

		build();

		assertThat(metadataWithoutInheritance.isUniqueValue()).isTrue();
	}

	@Test
	public void givenUniqueValueFlagSetToTrueOnMetadataWithoutInheritanceWhenModifyingThenUniqueValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setUniqueValue(true);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isUniqueValue()).isTrue();
	}

	@Test
	public void givenMutlivalueFlagOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setUniqueValue(true);

		build();

		assertThat(metadataWithInheritance.isUniqueValue()).isTrue();
	}

	@Test
	public void givenUniqueValueFlagOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setUniqueValue(true);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.isUniqueValue()).isTrue();
	}

	@Test
	public void givenTrueFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setUniqueValue(true);
		assertThat(metadataWithInheritanceBuilder.isUniqueValue()).isTrue();

	}

	@Test
	public void givenFalseFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setUniqueValue(false);
		assertThat(metadataWithInheritanceBuilder.isUniqueValue()).isFalse();

	}
}
