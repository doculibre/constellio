package com.constellio.model.services.schemas.builders;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MetadataBuilder_EnableFlagTest extends MetadataBuilderTest {

	@Test
	public void givenEnabledFlagOfMetadataWithoutInheritanceIsNullWhenBuildingThenSetToTrue() {
		metadataWithoutInheritanceBuilder.setType(STRING).setEnabled(null);

		build();

		assertThat(metadataWithoutInheritance.isEnabled()).isTrue();
	}

	@Test
	public void givenEnabledFlagOfMetadataWithoutInheritanceIsNullWhenModifyingThenSetToTrue() {
		metadataWithoutInheritanceBuilder.setType(STRING).setEnabled(null);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getEnabled()).isTrue();
	}

	@Test
	public void givenEnabledFlagOfMetadataWithoutInheritanceIsNotDefinedWhenBuildingThenSetToTrue() {
		metadataWithoutInheritanceBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.isEnabled()).isTrue();
	}

	@Test
	public void givenEnabledFlagOfMetadataWithoutInheritanceIsFalseWhenBuildingThenSetToFalse() {
		metadataWithoutInheritanceBuilder.setType(STRING).setEnabled(false);

		build();

		assertThat(metadataWithInheritance.isEnabled()).isFalse();
	}

	@Test
	public void givenEnabledFlagOfMetadataWithoutInheritanceIsFalseWhenModifyingThenSetToFalse() {
		metadataWithoutInheritanceBuilder.setType(STRING).setEnabled(false);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getEnabled()).isFalse();
	}

	@Test
	public void givenEnabledFlagOfMetadataWithInheritanceIsDifferentWhenBuildingThenSetToCustomizedValue() {
		inheritedMetadataBuilder.setType(STRING).setEnabled(false);
		metadataWithInheritanceBuilder.setEnabled(true);

		build();

		assertThat(inheritedMetadata.isEnabled()).isFalse();
		assertThat(metadataWithInheritance.isEnabled()).isTrue();
	}

	@Test
	public void givenEnabledFlagOfMetadataWithInheritanceIsDifferentWhenModifyingThenSetToCustomizedValue() {
		inheritedMetadataBuilder.setType(STRING).setEnabled(false);
		metadataWithInheritanceBuilder.setEnabled(true);

		buildAndModify();

		assertThat(inheritedMetadataBuilder.getEnabled()).isFalse();
		assertThat(metadataWithInheritanceBuilder.getEnabled()).isTrue();
	}

	@Test
	public void givenEnabledFlagOfMetadataWithInheritanceIsNullWhenBuildingThenSetToInheritedValue() {
		inheritedMetadataBuilder.setType(STRING).setEnabled(false);
		metadataWithInheritanceBuilder.setEnabled(null);

		build();

		assertThat(inheritedMetadata.isEnabled()).isFalse();
		assertThat(metadataWithInheritance.isEnabled()).isFalse();
	}

	@Test
	public void givenEnabledFlagOfMetadataWithInheritanceIsNullWhenModifyingThenSetToNull() {
		inheritedMetadataBuilder.setType(STRING).setEnabled(false);
		metadataWithInheritanceBuilder.setEnabled(null);

		buildAndModify();

		assertThat(inheritedMetadataBuilder.getEnabled()).isFalse();
		assertThat(metadataWithInheritanceBuilder.getEnabled()).isNull();
	}

	@Test
	public void givenEnabledFlagOfMetadataWithInheritanceIsSameAsInheritanceWhenModifyingThenSetToNull() {
		inheritedMetadataBuilder.setType(STRING).setEnabled(false);
		metadataWithInheritanceBuilder.setEnabled(false);

		buildAndModify();

		assertThat(inheritedMetadataBuilder.getEnabled()).isFalse();
		assertThat(metadataWithInheritanceBuilder.getEnabled()).isNull();
	}

}
