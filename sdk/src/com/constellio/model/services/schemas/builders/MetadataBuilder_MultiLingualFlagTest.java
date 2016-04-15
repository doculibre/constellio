package com.constellio.model.services.schemas.builders;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MetadataBuilder_MultiLingualFlagTest extends MetadataBuilderTest {

	@Test
	public void givenMultiLingualFlagUndefinedOnMetadataWithoutInheritanceWhenBuildingThenNotNotMultiLingual()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.isMultiLingual()).isFalse();
	}

	@Test
	public void givenMultiLingualFlagUndefinedOnMetadataWithoutInheritanceWhenModifyingThenNotMultiLingual()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isMultiLingual()).isFalse();
	}

	@Test
	public void givenMultiLingualFlagSetToFalseOnMetadataWithoutInheritanceWhenBuildingThenNotMultiLingual()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setMultiLingual(false);

		build();

		assertThat(metadataWithoutInheritance.isMultiLingual()).isFalse();
	}

	@Test
	public void givenMultiLingualFlagSetToFalseOnMetadataWithoutInheritanceWhenModifyingThenNotMultiLingual()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setMultiLingual(false);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isMultiLingual()).isFalse();
	}

	@Test
	public void givenMultiLingualFlagSetToTrueOnMetadataWithoutInheritanceWhenBuildingThenMultiLingual()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setMultiLingual(true);

		build();

		assertThat(metadataWithoutInheritance.isMultiLingual()).isTrue();
	}

	@Test
	public void givenMultiLingualFlagSetToTrueOnMetadataWithoutInheritanceWhenModifyingThenMultiLingual()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setMultiLingual(true);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isMultiLingual()).isTrue();
	}

	@Test
	public void givenMutlivalueFlagOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setMultiLingual(true);

		build();

		assertThat(metadataWithInheritance.isMultiLingual()).isTrue();
	}

	@Test
	public void givenMultiLingualFlagOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setMultiLingual(true);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.isMultiLingual()).isTrue();
	}

	@Test
	public void givenTrueFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setMultiLingual(true);
		assertThat(metadataWithInheritanceBuilder.isMultiLingual()).isTrue();

	}

	@Test
	public void givenFalseFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setMultiLingual(false);
		assertThat(metadataWithInheritanceBuilder.isMultiLingual()).isFalse();

	}
}
