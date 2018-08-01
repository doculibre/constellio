package com.constellio.model.services.schemas.builders;

import org.junit.Test;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

public class MetadataBuilder_UnmodifiableFlagTest extends MetadataBuilderTest {

	@Test
	public void givenUnmodifiableFlagUndefinedOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.isUnmodifiable()).isFalse();
	}

	@Test
	public void givenUnmodifiableFlagUndefinedOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isUnmodifiable()).isFalse();
	}

	@Test
	public void givenUnmodifiableFlagSetToFalseOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setUnmodifiable(false);

		build();

		assertThat(metadataWithoutInheritance.isUnmodifiable()).isFalse();
	}

	@Test
	public void givenUnmodifiableFlagSetToFalseOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setUnmodifiable(false);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isUnmodifiable()).isFalse();
	}

	@Test
	public void givenUnmodifiableFlagSetToTrueOnMetadataWithoutInheritanceWhenBuildingThenUnmodifiable()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setUnmodifiable(true);

		build();

		assertThat(metadataWithoutInheritance.isUnmodifiable()).isTrue();
	}

	@Test
	public void givenUnmodifiableFlagSetToTrueOnMetadataWithoutInheritanceWhenModifyingThenUnmodifiable()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setUnmodifiable(true);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isUnmodifiable()).isTrue();
	}

	@Test
	public void givenMutlivalueFlagOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setUnmodifiable(true);

		build();

		assertThat(metadataWithInheritance.isUnmodifiable()).isTrue();
	}

	@Test
	public void givenUnmodifiableFlagOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setUnmodifiable(true);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.isUnmodifiable()).isTrue();
	}

	@Test
	public void givenTrueFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setUnmodifiable(true);
		assertThat(metadataWithInheritanceBuilder.isUnmodifiable()).isTrue();

	}

	@Test
	public void givenFalseFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setUnmodifiable(false);
		assertThat(metadataWithInheritanceBuilder.isUnmodifiable()).isFalse();

	}
}
