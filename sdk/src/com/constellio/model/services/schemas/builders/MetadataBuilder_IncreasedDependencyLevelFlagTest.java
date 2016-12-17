package com.constellio.model.services.schemas.builders;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MetadataBuilder_IncreasedDependencyLevelFlagTest extends MetadataBuilderTest {

	@Test
	public void givenReversedDependencyFlagUndefinedOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.isIncreasedDependencyLevel()).isFalse();
	}

	@Test
	public void givenReversedDependencyFlagUndefinedOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isIncreasedDependencyLevel()).isFalse();
	}

	@Test
	public void givenReversedDependencyFlagSetToFalseOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setIncreasedDependencyLevel(false);

		build();

		assertThat(metadataWithoutInheritance.isIncreasedDependencyLevel()).isFalse();
	}

	@Test
	public void givenReversedDependencyFlagSetToFalseOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setIncreasedDependencyLevel(false);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isIncreasedDependencyLevel()).isFalse();
	}

	@Test
	public void givenReversedDependencyFlagSetToTrueOnMetadataWithoutInheritanceWhenBuildingThenReversedDependency()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setIncreasedDependencyLevel(true);

		build();

		assertThat(metadataWithoutInheritance.isIncreasedDependencyLevel()).isTrue();
	}

	@Test
	public void givenReversedDependencyFlagSetToTrueOnMetadataWithoutInheritanceWhenModifyingThenReversedDependency()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setIncreasedDependencyLevel(true);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isIncreasedDependencyLevel()).isTrue();
	}

	@Test
	public void givenMutlivalueFlagOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setIncreasedDependencyLevel(true);

		build();

		assertThat(metadataWithInheritance.isIncreasedDependencyLevel()).isTrue();
	}

	@Test
	public void givenReversedDependencyFlagOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setIncreasedDependencyLevel(true);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.isIncreasedDependencyLevel()).isTrue();
	}

	@Test
	public void givenTrueFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setIncreasedDependencyLevel(true);
		assertThat(metadataWithInheritanceBuilder.isIncreasedDependencyLevel()).isTrue();

	}

	@Test
	public void givenFalseFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setIncreasedDependencyLevel(false);
		assertThat(metadataWithInheritanceBuilder.isIncreasedDependencyLevel()).isFalse();

	}
}
