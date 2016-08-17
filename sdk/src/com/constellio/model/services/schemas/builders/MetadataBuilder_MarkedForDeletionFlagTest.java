package com.constellio.model.services.schemas.builders;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MetadataBuilder_MarkedForDeletionFlagTest extends MetadataBuilderTest {

	@Test
	public void givenMarkedForDeletionFlagUndefinedOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.isMarkedForDeletion()).isFalse();
	}

	@Test
	public void givenMarkedForDeletionFlagUndefinedOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isMarkedForDeletion()).isFalse();
	}

	@Test
	public void givenMarkedForDeletionFlagSetToFalseOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setMarkedForDeletion(false);

		build();

		assertThat(metadataWithoutInheritance.isMarkedForDeletion()).isFalse();
	}

	@Test
	public void givenMarkedForDeletionFlagSetToFalseOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setMarkedForDeletion(false);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isMarkedForDeletion()).isFalse();
	}

	@Test
	public void givenMarkedForDeletionFlagSetToTrueOnMetadataWithoutInheritanceWhenBuildingThenMarkedForDeletion()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setMarkedForDeletion(true);

		build();

		assertThat(metadataWithoutInheritance.isMarkedForDeletion()).isTrue();
	}

	@Test
	public void givenMarkedForDeletionFlagSetToTrueOnMetadataWithoutInheritanceWhenModifyingThenMarkedForDeletion()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setMarkedForDeletion(true);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isMarkedForDeletion()).isTrue();
	}

	@Test
	public void givenMutlivalueFlagOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setMarkedForDeletion(true);

		build();

		assertThat(metadataWithInheritance.isMarkedForDeletion()).isTrue();
	}

	@Test
	public void givenMarkedForDeletionFlagOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setMarkedForDeletion(true);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.isMarkedForDeletion()).isTrue();
	}

	@Test
	public void givenTrueFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setMarkedForDeletion(true);
		assertThat(metadataWithInheritanceBuilder.isMarkedForDeletion()).isTrue();

	}

	@Test
	public void givenFalseFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setMarkedForDeletion(false);
		assertThat(metadataWithInheritanceBuilder.isMarkedForDeletion()).isFalse();

	}
}
