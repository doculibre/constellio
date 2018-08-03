package com.constellio.model.services.schemas.builders;

import org.junit.Test;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

public class MetadataBuilder_RelationshipProvidingSecurityFlagTest extends MetadataBuilderTest {

	@Test
	public void givenRelationshipProvidingSecurityFlagUndefinedOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.isRelationshipProvidingSecurity()).isFalse();
	}

	@Test
	public void givenRelationshipProvidingSecurityFlagUndefinedOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isRelationshipProvidingSecurity()).isFalse();
	}

	@Test
	public void givenRelationshipProvidingSecurityFlagSetToFalseOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setRelationshipProvidingSecurity(false);

		build();

		assertThat(metadataWithoutInheritance.isRelationshipProvidingSecurity()).isFalse();
	}

	@Test
	public void givenRelationshipProvidingSecurityFlagSetToFalseOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setRelationshipProvidingSecurity(false);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isRelationshipProvidingSecurity()).isFalse();
	}

	@Test
	public void givenRelationshipProvidingSecurityFlagSetToTrueOnMetadataWithoutInheritanceWhenBuildingThenRelationshipProvidingSecurity()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setRelationshipProvidingSecurity(true);

		build();

		assertThat(metadataWithoutInheritance.isRelationshipProvidingSecurity()).isTrue();
	}

	@Test
	public void givenRelationshipProvidingSecurityFlagSetToTrueOnMetadataWithoutInheritanceWhenModifyingThenRelationshipProvidingSecurity()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setRelationshipProvidingSecurity(true);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isRelationshipProvidingSecurity()).isTrue();
	}

	@Test
	public void givenMutlivalueFlagOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setRelationshipProvidingSecurity(true);

		build();

		assertThat(metadataWithInheritance.isRelationshipProvidingSecurity()).isTrue();
	}

	@Test
	public void givenRelationshipProvidingSecurityFlagOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setRelationshipProvidingSecurity(true);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.isRelationshipProvidingSecurity()).isTrue();
	}

	@Test
	public void givenTrueFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setRelationshipProvidingSecurity(true);
		assertThat(metadataWithInheritanceBuilder.isRelationshipProvidingSecurity()).isTrue();

	}

	@Test
	public void givenFalseFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setRelationshipProvidingSecurity(false);
		assertThat(metadataWithInheritanceBuilder.isRelationshipProvidingSecurity()).isFalse();

	}
}
