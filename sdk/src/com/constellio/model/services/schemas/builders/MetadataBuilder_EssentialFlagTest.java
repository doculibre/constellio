package com.constellio.model.services.schemas.builders;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MetadataBuilder_EssentialFlagTest extends MetadataBuilderTest {

	@Test
	public void givenEssentialFlagUndefinedOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.isEssential()).isFalse();
	}

	@Test
	public void givenEssentialFlagUndefinedOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isEssential()).isFalse();
	}

	@Test
	public void givenEssentialFlagSetToFalseOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setEssential(false);

		build();

		assertThat(metadataWithoutInheritance.isEssential()).isFalse();
	}

	@Test
	public void givenEssentialFlagSetToFalseOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setEssential(false);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isEssential()).isFalse();
	}

	@Test
	public void givenEssentialFlagSetToTrueOnMetadataWithoutInheritanceWhenBuildingThenEssential()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setEssential(true);

		build();

		assertThat(metadataWithoutInheritance.isEssential()).isTrue();
	}

	@Test
	public void givenEssentialFlagSetToTrueOnMetadataWithoutInheritanceWhenModifyingThenEssential()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setEssential(true);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isEssential()).isTrue();
	}

	@Test
	public void givenMutlivalueFlagOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setEssential(true);

		build();

		assertThat(metadataWithInheritance.isEssential()).isTrue();
	}

	@Test
	public void givenEssentialFlagOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setEssential(true);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.isEssential()).isTrue();
	}

	@Test
	public void givenEssentialFlagThenUndeletable()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setEssential(true);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.isEssential()).isTrue();
		assertThat(metadataWithInheritanceBuilder.isUndeletable()).isTrue();
	}

	@Test(expected = MetadataBuilderRuntimeException.EssentialMetadataCannotBeDisabled.class)
	public void givenEssentialFlagAndDisabledThenException()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setEssential(true).setEnabled(false);

		build();
	}

	@Test(expected = MetadataBuilderRuntimeException.EssentialMetadataCannotBeDisabled.class)
	public void givenEssentialFlagAndDisabledInCustomSchemaThenException()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setEssential(true);
		metadataWithInheritanceBuilder.setEnabled(false);
		build();
	}

	@Test
	public void givenTrueFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setEssential(true);
		assertThat(metadataWithInheritanceBuilder.isEssential()).isTrue();

	}

	@Test
	public void givenFalseFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setEssential(false);
		assertThat(metadataWithInheritanceBuilder.isEssential()).isFalse();

	}
}
