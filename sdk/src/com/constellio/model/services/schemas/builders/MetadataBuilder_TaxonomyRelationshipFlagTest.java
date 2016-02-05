package com.constellio.model.services.schemas.builders;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MetadataBuilder_TaxonomyRelationshipFlagTest extends MetadataBuilderTest {

	@Test
	public void givenTaxonomyRelationshipFlagUndefinedOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.isTaxonomyRelationship()).isFalse();
	}

	@Test
	public void givenTaxonomyRelationshipFlagUndefinedOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isTaxonomyRelationship()).isFalse();
	}

	@Test
	public void givenTaxonomyRelationshipFlagSetToFalseOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setTaxonomyRelationship(false);

		build();

		assertThat(metadataWithoutInheritance.isTaxonomyRelationship()).isFalse();
	}

	@Test
	public void givenTaxonomyRelationshipFlagSetToFalseOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setTaxonomyRelationship(false);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isTaxonomyRelationship()).isFalse();
	}

	@Test
	public void givenTaxonomyRelationshipFlagSetToTrueOnMetadataWithoutInheritanceWhenBuildingThenTaxonomyRelationship()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setTaxonomyRelationship(true);

		build();

		assertThat(metadataWithoutInheritance.isTaxonomyRelationship()).isTrue();
	}

	@Test
	public void givenTaxonomyRelationshipFlagSetToTrueOnMetadataWithoutInheritanceWhenModifyingThenTaxonomyRelationship()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setTaxonomyRelationship(true);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isTaxonomyRelationship()).isTrue();
	}

	@Test
	public void givenMutlivalueFlagOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setTaxonomyRelationship(true);

		build();

		assertThat(metadataWithInheritance.isTaxonomyRelationship()).isTrue();
	}

	@Test
	public void givenTaxonomyRelationshipFlagOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setTaxonomyRelationship(true);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.isTaxonomyRelationship()).isTrue();
	}

	@Test
	public void givenTrueFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setTaxonomyRelationship(true);
		assertThat(metadataWithInheritanceBuilder.isTaxonomyRelationship()).isTrue();

	}

	@Test
	public void givenFalseFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setTaxonomyRelationship(false);
		assertThat(metadataWithInheritanceBuilder.isTaxonomyRelationship()).isFalse();

	}
}
