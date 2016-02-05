package com.constellio.model.services.schemas.builders;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MetadataBuilder_SystemReservedFlagTest extends MetadataBuilderTest {

	@Test
	public void givenSystemReservedFlagUndefinedOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.isSystemReserved()).isFalse();
	}

	@Test
	public void givenSystemReservedFlagUndefinedOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isSystemReserved()).isFalse();
	}

	@Test
	public void givenSystemReservedFlagSetToFalseOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setSystemReserved(false);

		build();

		assertThat(metadataWithoutInheritance.isSystemReserved()).isFalse();
	}

	@Test
	public void givenSystemReservedFlagSetToFalseOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setSystemReserved(false);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isSystemReserved()).isFalse();
	}

	@Test
	public void givenSystemReservedFlagSetToTrueOnMetadataWithoutInheritanceWhenBuildingThenSystemReserved()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setSystemReserved(true);

		build();

		assertThat(metadataWithoutInheritance.isSystemReserved()).isTrue();
	}

	@Test
	public void givenSystemReservedFlagSetToTrueOnMetadataWithoutInheritanceWhenModifyingThenSystemReserved()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setSystemReserved(true);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isSystemReserved()).isTrue();
	}

	@Test
	public void givenMutlivalueFlagOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setSystemReserved(true);

		build();

		assertThat(metadataWithInheritance.isSystemReserved()).isTrue();
	}

	@Test
	public void givenSystemReservedFlagOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setSystemReserved(true);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.isSystemReserved()).isTrue();
	}

	@Test
	public void givenSystemReservedFlagThenUndeletable()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setSystemReserved(true);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.isSystemReserved()).isTrue();
		assertThat(metadataWithInheritanceBuilder.isUndeletable()).isTrue();
	}

	@Test
	public void givenTrueFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setSystemReserved(true);
		assertThat(metadataWithInheritanceBuilder.isSystemReserved()).isTrue();

	}

	@Test
	public void givenFalseFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setSystemReserved(false);
		assertThat(metadataWithInheritanceBuilder.isSystemReserved()).isFalse();

	}
}
