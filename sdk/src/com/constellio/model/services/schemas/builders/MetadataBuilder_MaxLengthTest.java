package com.constellio.model.services.schemas.builders;

import org.junit.Test;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

public class MetadataBuilder_MaxLengthTest extends MetadataBuilderTest {

	@Test
	public void givenMaxLengthUndefinedOnMetadataWithoutInheritanceWhenBuildingThenNull()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.getMaxLength()).isNull();
	}

	@Test
	public void givenMaxLengthUndefinedOnMetadataWithoutInheritanceWhenModifyingThenNull()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getMaxLength()).isNull();
	}

	@Test
	public void givenMaxLengthSetTo7OnMetadataWithoutInheritanceWhenBuildingThen7()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setMaxLength(7);

		build();

		assertThat(metadataWithoutInheritance.getMaxLength()).isEqualTo(7);
	}

	@Test
	public void givenMaxLengthSetTo7OnMetadataWithoutInheritanceWhenModifyingThen7()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setMaxLength(7);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getMaxLength()).isEqualTo(7);
	}

	@Test
	public void givenMaxLengthSetTo0OnMetadataWithoutInheritanceWhenBuildingThen0()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setMaxLength(0);

		build();

		assertThat(metadataWithoutInheritance.getMaxLength()).isEqualTo(0);
	}

	@Test
	public void givenMaxLengthSetTo0OnMetadataWithoutInheritanceWhenModifyingThen0()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setMaxLength(0);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getMaxLength()).isEqualTo(0);
	}

	@Test
	public void givenMaxLengthSetToMinus1OnMetadataWithoutInheritanceWhenBuildingThenMinus1()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setMaxLength(-1);

		build();

		assertThat(metadataWithoutInheritance.getMaxLength()).isEqualTo(-1);
	}

	@Test
	public void givenMaxLengthSetToMinus1OnMetadataWithoutInheritanceWhenModifyingThenMinus1()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setMaxLength(-1);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getMaxLength()).isEqualTo(-1);
	}

	//%Q givenMultivalueFlag... prend en compte que la md est multivalue ou lui fixe le multivalue?
	@Test
	public void givenMutlivalueFlagOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setMaxLength(7);

		build();

		assertThat(metadataWithInheritance.getMaxLength()).isEqualTo(7);
	}

	@Test
	public void givenMutlivalueFlagOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setMaxLength(7);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.getMaxLength()).isEqualTo(7);
	}

	@Test
	public void givenMaxLengthModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithInheritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setMaxLength(7);
		assertThat(metadataWithInheritanceBuilder.getMaxLength()).isEqualTo(7);

	}

}
