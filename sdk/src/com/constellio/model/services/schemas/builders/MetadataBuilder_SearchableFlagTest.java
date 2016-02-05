package com.constellio.model.services.schemas.builders;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MetadataBuilder_SearchableFlagTest extends MetadataBuilderTest {

	@Test
	public void givenSearchableFlagUndefinedOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.isSearchable()).isFalse();
	}

	@Test
	public void givenSearchableFlagUndefinedOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isSearchable()).isFalse();
	}

	@Test
	public void givenSearchableFlagSetToFalseOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setSearchable(false);

		build();

		assertThat(metadataWithoutInheritance.isSearchable()).isFalse();
	}

	@Test
	public void givenSearchableFlagSetToFalseOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setSearchable(false);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isSearchable()).isFalse();
	}

	@Test
	public void givenSearchableFlagSetToTrueOnMetadataWithoutInheritanceWhenBuildingThenSearchable()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setSearchable(true);

		build();

		assertThat(metadataWithoutInheritance.isSearchable()).isTrue();
	}

	@Test
	public void givenSearchableFlagSetToTrueOnMetadataWithoutInheritanceWhenModifyingThenSearchable()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setSearchable(true);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isSearchable()).isTrue();
	}

	@Test
	public void givenMutlivalueFlagOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setSearchable(true);

		build();

		assertThat(metadataWithInheritance.isSearchable()).isTrue();
	}

	@Test
	public void givenSearchableFlagOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setSearchable(true);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.isSearchable()).isTrue();
	}

	@Test
	public void givenTrueFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setSearchable(true);
		assertThat(metadataWithInheritanceBuilder.isSearchable()).isTrue();

	}

	@Test
	public void givenFalseFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setSearchable(false);
		assertThat(metadataWithInheritanceBuilder.isSearchable()).isFalse();

	}
}
