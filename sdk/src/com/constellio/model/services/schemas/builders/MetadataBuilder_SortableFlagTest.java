package com.constellio.model.services.schemas.builders;

import org.junit.Test;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

public class MetadataBuilder_SortableFlagTest extends MetadataBuilderTest {

	@Test
	public void givenSortableFlagUndefinedOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.isSortable()).isFalse();
	}

	@Test
	public void givenSortableFlagUndefinedOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isSortable()).isFalse();
	}

	@Test
	public void givenSortableFlagSetToFalseOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setSortable(false);

		build();

		assertThat(metadataWithoutInheritance.isSortable()).isFalse();
	}

	@Test
	public void givenSortableFlagSetToFalseOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setSortable(false);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isSortable()).isFalse();
	}

	@Test
	public void givenSortableFlagSetToTrueOnMetadataWithoutInheritanceWhenBuildingThenSortable()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setSortable(true);

		build();

		assertThat(metadataWithoutInheritance.isSortable()).isTrue();
	}

	@Test
	public void givenSortableFlagSetToTrueOnMetadataWithoutInheritanceWhenModifyingThenSortable()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setSortable(true);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isSortable()).isTrue();
	}

	@Test
	public void givenMutlivalueFlagOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setSortable(true);

		build();

		assertThat(metadataWithInheritance.isSortable()).isTrue();
	}

	@Test
	public void givenSortableFlagOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setSortable(true);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.isSortable()).isTrue();
	}

	@Test
	public void givenTrueFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setSortable(true);
		assertThat(metadataWithInheritanceBuilder.isSortable()).isTrue();

	}

	@Test
	public void givenFalseFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setSortable(false);
		assertThat(metadataWithInheritanceBuilder.isSortable()).isFalse();

	}
}
