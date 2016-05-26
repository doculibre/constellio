package com.constellio.model.services.schemas.builders;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MetadataBuilder_DuplicatableFlagTest extends MetadataBuilderTest {

	@Test
	public void givenDuplicatableFlagNotDefinedOnMetadataWithoutInheritanceWhenBuildingThenSetToFalse()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.isDuplicatable()).isFalse();
	}

	@Test
	public void givenDuplicatableFlagSetToFalseOnMetadataWithoutInheritanceWhenBuildingThenSetToFalse()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setDuplicatable(false);

		build();

		assertThat(metadataWithoutInheritance.isDuplicatable()).isFalse();
	}

	@Test
	public void givenDuplicatableFlagNotDefinedOnMetadataWithoutInheritanceWhenModifyingThenSetToFalse()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isDuplicatable()).isFalse();
	}

	@Test
	public void givenDuplicatableFlagSetToFalseOnMetadataWithoutInheritanceWhenModifyingThenSetToFalse()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setDuplicatable(false);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isDuplicatable()).isFalse();
	}

	@Test
	public void givenDuplicatableFlagSetToTrueOnMetadataWithoutInheritanceWhenBuildingThenSetToTrue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setDuplicatable(true);

		build();

		assertThat(metadataWithoutInheritance.isDuplicatable()).isTrue();
	}

	@Test
	public void givenDuplicatableFlagSetToTrueOnMetadataWithoutInheritanceWhenModifyingThenSetToTrue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setDuplicatable(true);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isDuplicatable()).isTrue();
	}

	@Test
	public void givenDuplicatableFlagOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setDuplicatable(true);

		build();

		assertThat(metadataWithInheritance.isDuplicatable()).isTrue();
	}

	@Test
	public void givenDuplicatableFlagOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setDuplicatable(true);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.isDuplicatable()).isTrue();
	}

	@Test
	public void givenTrueFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setDuplicatable(true);

		assertThat(metadataWithInheritanceBuilder.isDuplicatable()).isTrue();
	}

	@Test
	public void givenFalseFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setDuplicatable(false);

		assertThat(metadataWithInheritanceBuilder.isDuplicatable()).isFalse();
	}
}
