package com.constellio.model.services.schemas.builders;

import org.junit.Test;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

public class MetadataBuilder_AvailableInSummaryFlagTest extends MetadataBuilderTest {

	@Test
	public void givenAvailableInSummaryFlagUndefinedOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.isAvailableInSummary()).isFalse();
	}

	@Test
	public void givenAvailableInSummaryFlagUndefinedOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isAvailableInSummary()).isFalse();
	}

	@Test
	public void givenAvailableInSummaryFlagSetToFalseOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setAvailableInSummary(false);

		build();

		assertThat(metadataWithoutInheritance.isAvailableInSummary()).isFalse();
	}

	@Test
	public void givenAvailableInSummaryFlagSetToFalseOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setAvailableInSummary(false);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isAvailableInSummary()).isFalse();
	}

	@Test
	public void givenAvailableInSummaryFlagSetToTrueOnMetadataWithoutInheritanceWhenBuildingThenAvailableInSummary()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setAvailableInSummary(true);

		build();

		assertThat(metadataWithoutInheritance.isAvailableInSummary()).isTrue();
	}

	@Test
	public void givenAvailableInSummaryFlagSetToTrueOnMetadataWithoutInheritanceWhenModifyingThenAvailableInSummary()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setAvailableInSummary(true);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isAvailableInSummary()).isTrue();
	}

	@Test
	public void givenMutlivalueFlagOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setAvailableInSummary(true);

		build();

		assertThat(metadataWithInheritance.isAvailableInSummary()).isTrue();
	}

	@Test
	public void givenAvailableInSummaryFlagOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setAvailableInSummary(true);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.isAvailableInSummary()).isTrue();
	}

	@Test
	public void givenTrueFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setAvailableInSummary(true);
		assertThat(metadataWithInheritanceBuilder.isAvailableInSummary()).isTrue();

	}

	@Test
	public void givenFalseFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setAvailableInSummary(false);
		assertThat(metadataWithInheritanceBuilder.isAvailableInSummary()).isFalse();

	}
}
