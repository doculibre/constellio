package com.constellio.model.services.schemas.builders;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MetadataBuilder_WriteEncryptedTest extends MetadataBuilderTest {

	@Test
	public void givenWriteEncryptedFlagUndefinedOnMetadataWithoutInheritanceWhenBuildingThenFalse()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.isEncrypted()).isFalse();
	}

	@Test
	public void givenWriteEncryptedFlagUndefinedOnMetadataWithoutInheritanceWhenModifyingThenFalse()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isEncrypted()).isFalse();
	}

	@Test
	public void givenWriteEncryptedFlagSetToFalseOnMetadataWithoutInheritanceWhenBuildingThenFalse()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setEncrypted(false);

		build();

		assertThat(metadataWithoutInheritance.isEncrypted()).isFalse();
	}

	@Test
	public void givenWriteEncryptedFlagSetToFalseOnMetadataWithoutInheritanceWhenModifyingThenFalse()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setEncrypted(false);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isEncrypted()).isFalse();
	}

	@Test
	public void givenWriteEncryptedFlagSetToTrueOnMetadataWithoutInheritanceWhenBuildingThenWriteEncrypted()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setEncrypted(true);

		build();

		assertThat(metadataWithoutInheritance.isEncrypted()).isTrue();
	}

	@Test
	public void givenWriteEncryptedFlagSetToTrueOnMetadataWithoutInheritanceWhenModifyingThenWriteEncrypted()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setEncrypted(true);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isEncrypted()).isTrue();
	}

	@Test
	public void givenMutlivalueFlagOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setEncrypted(true);

		build();

		assertThat(metadataWithInheritance.isEncrypted()).isTrue();
	}

	@Test
	public void givenWriteEncryptedFlagOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setEncrypted(true);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.isEncrypted()).isTrue();
	}

	@Test
	public void givenTrueFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setEncrypted(true);
		assertThat(metadataWithInheritanceBuilder.isEncrypted()).isTrue();

	}

	@Test
	public void givenFalseFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setEncrypted(false);
		assertThat(metadataWithInheritanceBuilder.isEncrypted()).isFalse();

	}
}
