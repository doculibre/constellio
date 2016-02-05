package com.constellio.model.services.schemas.builders;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MetadataBuilder_SchemaAutocompleteFlagTest extends MetadataBuilderTest {

	@Test
	public void givenSchemaAutocompleteFlagUndefinedOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.isSchemaAutocomplete()).isFalse();
	}

	@Test
	public void givenSchemaAutocompleteFlagUndefinedOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isSchemaAutocomplete()).isFalse();
	}

	@Test
	public void givenSchemaAutocompleteFlagSetToFalseOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setSchemaAutocomplete(false);

		build();

		assertThat(metadataWithoutInheritance.isSchemaAutocomplete()).isFalse();
	}

	@Test
	public void givenSchemaAutocompleteFlagSetToFalseOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setSchemaAutocomplete(false);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isSchemaAutocomplete()).isFalse();
	}

	@Test
	public void givenSchemaAutocompleteFlagSetToTrueOnMetadataWithoutInheritanceWhenBuildingThenSchemaAutocomplete()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setSchemaAutocomplete(true);

		build();

		assertThat(metadataWithoutInheritance.isSchemaAutocomplete()).isTrue();
	}

	@Test
	public void givenSchemaAutocompleteFlagSetToTrueOnMetadataWithoutInheritanceWhenModifyingThenSchemaAutocomplete()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setSchemaAutocomplete(true);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isSchemaAutocomplete()).isTrue();
	}

	@Test
	public void givenMutlivalueFlagOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setSchemaAutocomplete(true);

		build();

		assertThat(metadataWithInheritance.isSchemaAutocomplete()).isTrue();
	}

	@Test
	public void givenSchemaAutocompleteFlagOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setSchemaAutocomplete(true);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.isSchemaAutocomplete()).isTrue();
	}

	@Test
	public void givenTrueFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setSchemaAutocomplete(true);
		assertThat(metadataWithInheritanceBuilder.isSchemaAutocomplete()).isTrue();

	}

	@Test
	public void givenFalseFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setSchemaAutocomplete(false);
		assertThat(metadataWithInheritanceBuilder.isSchemaAutocomplete()).isFalse();

	}
}
