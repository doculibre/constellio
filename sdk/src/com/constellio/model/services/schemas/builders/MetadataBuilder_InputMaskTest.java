package com.constellio.model.services.schemas.builders;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MetadataBuilder_InputMaskTest extends MetadataBuilderTest {

	@Test
	public void givenStringMetadataWithoutInputMaskWhenBuiltThenOk() {
		metadataWithoutInheritanceBuilder.setType(STRING).setInputMask(null);

		build();

		assertThat(metadataWithoutInheritance.getInputMask()).isNull();
	}

	@Test
	public void givenStringMetadataWithoutInputMaskWhenModifyThenOk() {
		metadataWithoutInheritanceBuilder.setType(STRING).setInputMask(null);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getInputMask()).isNull();
	}

	@Test
	public void givenStringMetadataWithInputMaskWhenBuiltThenOk() {
		metadataWithoutInheritanceBuilder.setType(STRING).setInputMask("zeValue");

		build();

		assertThat(metadataWithoutInheritance.getInputMask()).isEqualTo("zeValue");
	}

	@Test
	public void givenStringMetadataWithInputMaskWhenModifyThenOk() {
		metadataWithoutInheritanceBuilder.setType(STRING).setInputMask("zeValue");

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getInputMask()).isEqualTo("zeValue");
	}

	@Test
	public void givenCustomizedInputMaskWhenBuiltThenOk() {
		inheritedMetadataBuilder.setType(STRING).setInputMask("zeValue");
		metadataWithInheritanceBuilder.setInputMask("anotherValue");

		build();

		assertThat(inheritedMetadata.getInputMask()).isEqualTo("zeValue");
		assertThat(metadataWithInheritance.getInputMask()).isEqualTo("anotherValue");
	}

	@Test
	public void givenCustomizedInputMaskWhenModifyThenOk() {
		inheritedMetadataBuilder.setType(STRING).setInputMask("zeValue");
		metadataWithInheritanceBuilder.setInputMask("anotherValue");

		buildAndModify();

		assertThat(inheritedMetadataBuilder.getInputMask()).isEqualTo("zeValue");
		assertThat(metadataWithInheritanceBuilder.getInputMask()).isEqualTo("anotherValue");
	}
}