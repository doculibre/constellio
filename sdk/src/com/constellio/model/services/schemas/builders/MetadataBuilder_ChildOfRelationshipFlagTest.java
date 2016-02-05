package com.constellio.model.services.schemas.builders;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MetadataBuilder_ChildOfRelationshipFlagTest extends MetadataBuilderTest {

	@Test
	public void givenChildOfRelationshipFlagUndefinedOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.isChildOfRelationship()).isFalse();
	}

	@Test
	public void givenChildOfRelationshipFlagUndefinedOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isChildOfRelationship()).isFalse();
	}

	@Test
	public void givenChildOfRelationshipFlagSetToFalseOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setChildOfRelationship(false);

		build();

		assertThat(metadataWithoutInheritance.isChildOfRelationship()).isFalse();
	}

	@Test
	public void givenChildOfRelationshipFlagSetToFalseOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setChildOfRelationship(false);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isChildOfRelationship()).isFalse();
	}

	@Test
	public void givenChildOfRelationshipFlagSetToTrueOnMetadataWithoutInheritanceWhenBuildingThenChildOfRelationship()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setChildOfRelationship(true);

		build();

		assertThat(metadataWithoutInheritance.isChildOfRelationship()).isTrue();
	}

	@Test
	public void givenChildOfRelationshipFlagSetToTrueOnMetadataWithoutInheritanceWhenModifyingThenChildOfRelationship()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setChildOfRelationship(true);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isChildOfRelationship()).isTrue();
	}

	@Test
	public void givenMutlivalueFlagOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setChildOfRelationship(true);

		build();

		assertThat(metadataWithInheritance.isChildOfRelationship()).isTrue();
	}

	@Test
	public void givenChildOfRelationshipFlagOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setChildOfRelationship(true);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.isChildOfRelationship()).isTrue();
	}

	@Test
	public void givenTrueFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setChildOfRelationship(true);
		assertThat(metadataWithInheritanceBuilder.isChildOfRelationship()).isTrue();

	}

	@Test
	public void givenFalseFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setChildOfRelationship(false);
		assertThat(metadataWithInheritanceBuilder.isChildOfRelationship()).isFalse();

	}
}
