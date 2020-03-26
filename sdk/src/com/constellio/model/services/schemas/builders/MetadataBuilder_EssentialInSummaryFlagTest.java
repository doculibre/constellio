package com.constellio.model.services.schemas.builders;

import org.junit.Test;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

public class MetadataBuilder_EssentialInSummaryFlagTest extends MetadataBuilderTest {

	@Test
	public void givenEssentialInSummaryFlagUndefinedOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.isEssentialInSummary()).isFalse();
	}

	@Test
	public void givenEssentialInSummaryFlagUndefinedOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isEssentialInSummary()).isFalse();
	}

	@Test
	public void givenEssentialInSummaryFlagSetToFalseOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setEssentialInSummary(false);

		build();

		assertThat(metadataWithoutInheritance.isEssentialInSummary()).isFalse();
	}

	@Test
	public void givenEssentialInSummaryFlagSetToFalseOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setEssentialInSummary(false);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isEssentialInSummary()).isFalse();
	}

	@Test
	public void givenEssentialInSummaryFlagSetToTrueOnMetadataWithoutInheritanceWhenBuildingThenEssentialInSummary()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setEssentialInSummary(true);

		build();

		assertThat(metadataWithoutInheritance.isEssentialInSummary()).isTrue();
	}

	@Test
	public void givenEssentialInSummaryFlagSetToTrueOnMetadataWithoutInheritanceWhenModifyingThenEssentialInSummary()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setEssentialInSummary(true);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isEssentialInSummary()).isTrue();
	}

	@Test
	public void givenMutlivalueFlagOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setEssentialInSummary(true);

		build();

		assertThat(metadataWithInheritance.isEssentialInSummary()).isTrue();
	}

	@Test
	public void givenEssentialInSummaryFlagOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setEssentialInSummary(true);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.isEssentialInSummary()).isTrue();
	}

	@Test(expected = MetadataBuilderRuntimeException.EssentialMetadataInSummaryCannotBeDisabled.class)
	public void givenEssentialSystemInSummaryFlagAndDisabledThenException()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setEssentialInSummary(true).setEnabled(false);

		build();
	}

	public void givenUSREssentialInSummaryFlagAndDisabledThenNoException()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setEssentialInSummary(true).setEnabled(false);

		build();
	}

	@Test(expected = MetadataBuilderRuntimeException.EssentialMetadataInSummaryCannotBeDisabled.class)
	public void givenEssentialInSummaryFlagAndDisabledInCustomSchemaThenException()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setEssentialInSummary(true);
		metadataWithInheritanceBuilder.setEnabled(false);
		build();
	}

	@Test
	public void givenTrueFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setEssentialInSummary(true);
		assertThat(metadataWithInheritanceBuilder.isEssentialInSummary()).isTrue();

	}

	@Test
	public void givenFalseFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setEssentialInSummary(false);
		assertThat(metadataWithInheritanceBuilder.isEssentialInSummary()).isFalse();

	}
}
