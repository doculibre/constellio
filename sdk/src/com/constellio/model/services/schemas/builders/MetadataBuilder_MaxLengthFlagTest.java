package com.constellio.model.services.schemas.builders;

import org.junit.Test;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

public class MetadataBuilder_MaxLengthFlagTest extends MetadataBuilderTest {

	@Test
	public void givenMaxLengthFlagUndefinedOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		//		inheritedMetadataBuilder.setType(BOOLEAN);//%V set le mauvais type pour taxonomyRelationship, donc set mauvais type ici aussi?
		inheritedMetadataBuilder.setType(STRING);//%V set le mauv
		// ais type pour taxonomyRelationship, donc set mauvais type ici aussi?

		build();

		assertThat(metadataWithoutInheritance.getMaxLength()).isNull();
	}

	@Test
	public void givenMaxLengthFlagUndefinedOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getMaxLength()).isNull();
	}

	@Test
	public void givenMaxLengthFlagSetTo7OnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setMaxLength(8);

		build();

		assertThat(metadataWithoutInheritance.getMaxLength()).isNotEqualTo(7);
	}

	@Test
	public void givenMaxLengthFlagSetTo7OnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setMaxLength(8);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getMaxLength()).isNotEqualTo(7);
	}

	@Test
	public void givenMaxLengthFlagSetToTrueOnMetadataWithoutInheritanceWhenBuildingThenMaxLength()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setMaxLength(7);

		build();

		assertThat(metadataWithoutInheritance.getMaxLength()).isEqualTo(7);
	}

	@Test
	public void givenMaxLengthFlagSetToTrueOnMetadataWithoutInheritanceWhenModifyingThenMaxLength()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setMaxLength(7);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getMaxLength()).isEqualTo(7);
	}

	@Test
	public void givenMutlivalueFlagOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setMaxLength(7);

		build();

		assertThat(metadataWithInheritance.getMaxLength()).isEqualTo(7);
	}

	@Test
	public void givenTaxonomyRelationshipFlagOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setMaxLength(7);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.getMaxLength()).isEqualTo(7);
	}

	@Test
	public void givenTrueFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setMaxLength(7);
		assertThat(metadataWithInheritanceBuilder.getMaxLength()).isEqualTo(7);

	}

	@Test
	public void givenFalseFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setMaxLength(8);
		assertThat(metadataWithInheritanceBuilder.getMaxLength()).isNotEqualTo(7);

	}
}
