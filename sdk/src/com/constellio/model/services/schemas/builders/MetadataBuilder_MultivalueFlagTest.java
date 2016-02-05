package com.constellio.model.services.schemas.builders;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MetadataBuilder_MultivalueFlagTest extends MetadataBuilderTest {

	@Test
	public void givenMultivalueFlagUndefinedOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.isMultivalue()).isFalse();
	}

	@Test
	public void givenMultivalueFlagUndefinedOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isMultivalue()).isFalse();
	}

	@Test
	public void givenMultivalueFlagSetToFalseOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setMultivalue(false);

		build();

		assertThat(metadataWithoutInheritance.isMultivalue()).isFalse();
	}

	@Test
	public void givenMultivalueFlagSetToFalseOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setMultivalue(false);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isMultivalue()).isFalse();
	}

	@Test
	public void givenMultivalueFlagSetToTrueOnMetadataWithoutInheritanceWhenBuildingThenMultivalue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setMultivalue(true);

		build();

		assertThat(metadataWithoutInheritance.isMultivalue()).isTrue();
	}

	@Test
	public void givenMultivalueFlagSetToTrueOnMetadataWithoutInheritanceWhenModifyingThenMultivalue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setMultivalue(true);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isMultivalue()).isTrue();
	}

	@Test
	public void givenMutlivalueFlagOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setMultivalue(true);

		build();

		assertThat(metadataWithInheritance.isMultivalue()).isTrue();
	}

	@Test
	public void givenMultivalueFlagOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setMultivalue(true);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.isMultivalue()).isTrue();
	}

	@Test
	public void givenTrueFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setMultivalue(true);
		assertThat(metadataWithInheritanceBuilder.isMultivalue()).isTrue();

	}

	@Test
	public void givenFalseFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setMultivalue(false);
		assertThat(metadataWithInheritanceBuilder.isMultivalue()).isFalse();

	}
}
