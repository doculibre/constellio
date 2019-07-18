package com.constellio.model.services.schemas.builders;

import org.junit.Test;

import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

public class MetadataBuilder_CodesCollectionAndLabelTest extends MetadataBuilderTest {

	@Test
	public void givenCodeOfMetadataWithoutInheritanceWhenBuiltThenHasGivenValue() {
		metadataWithoutInheritanceBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.getLocalCode()).isEqualTo(CODE_DEFAULT_METADATA);
	}

	@Test
	public void givenCodeOfMetadataWithoutInheritanceWhenModifyingThenHasGivenValue() {
		metadataWithoutInheritanceBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getLocalCode()).isEqualTo(CODE_DEFAULT_METADATA);
	}

	@Test
	public void givenCodeOfMetadataWithInheritanceWhenBuiltThenHasTheInheritedValue() {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithInheritance.getLocalCode()).isEqualTo(CODE_DEFAULT_METADATA);
	}

	@Test
	public void givenCodeOfMetadataWithInheritanceWhenModifyingThenHasTheInheritedValue() {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getLocalCode()).isEqualTo(CODE_DEFAULT_METADATA);
	}

	@Test
	public void givenCollectionOfMetadataWithoutInheritanceWhenBuiltThenHasGivenValue() {
		metadataWithoutInheritanceBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.getCollection()).isEqualTo(COLLECTION.getCode());
	}

	@Test
	public void givenCollectionOfMetadataWithoutInheritanceWhenModifyingThenHasGivenValue() {
		metadataWithoutInheritanceBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getCollection()).isEqualTo(COLLECTION.getCode());
	}

	@Test
	public void givenCollectionOfMetadataWithInheritanceWhenBuiltThenHasTheInheritedValue() {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithInheritance.getCollection()).isEqualTo(COLLECTION.getCode());
	}

	@Test
	public void givenCollectionOfMetadataWithInheritanceWhenModifyingThenHasTheInheritedValue() {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getCollection()).isEqualTo(COLLECTION.getCode());
	}

	@Test
	public void givenCompleteCodeOfMetadataWithoutInheritanceWhenBuiltThenHasGivenOnCodeAndSchemaCode() {
		metadataWithoutInheritanceBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.getCode()).isEqualTo(DEFAULT_SCHEMA_CODE + "_" + CODE_DEFAULT_METADATA);
	}

	@Test
	public void givenCompleteCodeOfMetadataWithoutInheritanceWhenModifyingThenHasGivenOnCodeAndSchemaCode() {
		metadataWithoutInheritanceBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getCode()).isEqualTo(
				DEFAULT_SCHEMA_CODE + "_" + CODE_DEFAULT_METADATA);
	}

	@Test
	public void givenCompleteCodeOfMetadataWithInheritanceWhenBuiltThenHasTheInheritedValue() {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithInheritance.getCode()).isEqualTo(CUSTOM_SCHEMA_CODE + "_" + CODE_DEFAULT_METADATA);
	}

	@Test
	public void givenCompleteCodeOfMetadataWithInheritanceWhenModifyingThenHasTheInheritedValue() {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.getCode()).isEqualTo(CUSTOM_SCHEMA_CODE + "_" + CODE_DEFAULT_METADATA);
	}

	@Test
	public void givenDataStoreCodeOfMetadataWithoutInheritanceWhenBuiltThenHasGivenCodeWithCorrectDataStoreType() {
		metadataWithoutInheritanceBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.getDataStoreCode()).isEqualTo(CODE_DEFAULT_METADATA + "_string");
	}

	@Test
	public void givenDataStoreCodeOfReferenceMetadataWithoutInheritanceWhenBuiltThenHasGivenCodeWithIdKeywordAndCorrectDataStoreType() {
		metadataWithoutInheritanceBuilder.setType(REFERENCE).defineReferencesTo(anotherSchemaTypeBuilder);

		build();

		assertThat(metadataWithoutInheritance.getDataStoreCode()).isEqualTo(CODE_DEFAULT_METADATA + "Id_string");
	}

	@Test
	public void givenDataStoreCodeOfMetadataWithInheritanceWhenBuiltThenHasGivenCodeWithCorrectDataStoreType() {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithInheritance.getDataStoreCode()).isEqualTo(CODE_DEFAULT_METADATA + "_string");
	}

	@Test
	public void givenDataStoreCodeOfReferenceMetadataWithInheritanceWhenBuiltThenHasGivenCodeWithIdKeywordAndCorrectDataStoreType() {
		inheritedMetadataBuilder.setType(REFERENCE).defineReferencesTo(anotherSchemaTypeBuilder);

		build();

		assertThat(metadataWithInheritance.getDataStoreCode()).isEqualTo(CODE_DEFAULT_METADATA + "Id_string");
	}

	@Test(expected = MetadataBuilderRuntimeException.InvalidAttribute.class)
	public void givenCodeFinishingWithIdThenException()
			throws Exception {
		MetadataBuilder.createMetadataWithoutInheritance(CODE_DEFAULT_METADATA + "pid",
				schemaBuilder).buildWithoutInheritance(typesFactory, schemaTypeBuilder, modelLayerFactory);

	}

}
