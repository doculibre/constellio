package com.constellio.model.services.schemas.builders;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MetadataBuilder_DefaultValueTest extends MetadataBuilderTest {

	@Test
	public void givenStringMetadataWithoutDefaultValueWhenBuiltThenOk() {
		metadataWithoutInheritanceBuilder.setType(STRING).setDefaultValue(null);

		build();

		assertThat(metadataWithoutInheritance.getDefaultValue()).isNull();
	}

	@Test
	public void givenStringMetadataWithoutDefaultValueWhenModifyThenOk() {
		metadataWithoutInheritanceBuilder.setType(STRING).setDefaultValue(null);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getDefaultValue()).isNull();
	}

	@Test
	public void givenStringMetadataWithDefaultValueWhenBuiltThenOk() {
		metadataWithoutInheritanceBuilder.setType(STRING).setDefaultValue("zeValue");

		build();

		assertThat(metadataWithoutInheritance.getDefaultValue()).isEqualTo("zeValue");
	}

	@Test
	public void givenStringMetadataWithDefaultValueWhenModifyThenOk() {
		metadataWithoutInheritanceBuilder.setType(STRING).setDefaultValue("zeValue");

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getDefaultValue()).isEqualTo("zeValue");
	}

	@Test
	public void givenMultivaluedStringMetadataWithDefaultValueWhenBuiltThenOk() {
		metadataWithoutInheritanceBuilder.setType(STRING).setMultivalue(true).setDefaultValue(asList("zeValue", "anotherValue"));

		build();

		assertThat(metadataWithoutInheritance.getDefaultValue()).isEqualTo(asList("zeValue", "anotherValue"));
	}

	@Test
	public void givenMultivaluedStringMetadataWithDefaultValueWhenModifyThenOk() {
		metadataWithoutInheritanceBuilder.setType(STRING).setMultivalue(true).setDefaultValue(asList("zeValue", "anotherValue"));

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getDefaultValue()).isEqualTo(asList("zeValue", "anotherValue"));
	}

	@Test
	public void givenCustomizedDefaultValueWhenBuiltThenOk() {
		inheritedMetadataBuilder.setType(STRING).setDefaultValue("zeValue");
		metadataWithInheritanceBuilder.setDefaultValue("anotherValue");

		build();

		assertThat(inheritedMetadata.getDefaultValue()).isEqualTo("zeValue");
		assertThat(metadataWithInheritance.getDefaultValue()).isEqualTo("anotherValue");
	}

	@Test
	public void givenCustomizedDefaultValueWhenModifyThenOk() {
		inheritedMetadataBuilder.setType(STRING).setDefaultValue("zeValue");
		metadataWithInheritanceBuilder.setDefaultValue("anotherValue");

		buildAndModify();

		assertThat(inheritedMetadataBuilder.getDefaultValue()).isEqualTo("zeValue");
		assertThat(metadataWithInheritanceBuilder.getDefaultValue()).isEqualTo("anotherValue");
	}
}