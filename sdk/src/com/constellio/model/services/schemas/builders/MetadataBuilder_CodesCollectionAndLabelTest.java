package com.constellio.model.services.schemas.builders;

import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

//TODO Thiago
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

		assertThat(metadataWithoutInheritance.getCollection()).isEqualTo(COLLECTION);
	}

	@Test
	public void givenCollectionOfMetadataWithoutInheritanceWhenModifyingThenHasGivenValue() {
		metadataWithoutInheritanceBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getCollection()).isEqualTo(COLLECTION);
	}

	@Test
	public void givenCollectionOfMetadataWithInheritanceWhenBuiltThenHasTheInheritedValue() {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithInheritance.getCollection()).isEqualTo(COLLECTION);
	}

	@Test
	public void givenCollectionOfMetadataWithInheritanceWhenModifyingThenHasTheInheritedValue() {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getCollection()).isEqualTo(COLLECTION);
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

	//TODO Thiago remove label tests
	//	@Test(expected = MetadataBuilderRuntimeException.InvalidAttribute.class)
	//	public void givenLabelOfMetadataWithoutInheritanceISNullLabelWhenBuildingThenException() {
	//		metadataWithoutInheritanceBuilder.setType(STRING).setLabel(null);
	//
	//		build();
	//	}
	//
	//	@Test
	//	public void givenLabelOfMetadataWithInheritanceIsNullWhenBuildingThenSetToInheritedValue() {
	//		inheritedMetadataBuilder.setType(STRING).setLabel("default label");
	//		metadataWithInheritanceBuilder.setLabel(null);
	//
	//		build();
	//
	//		assertThat(metadataWithInheritance.getLabel()).isEqualTo("default label");
	//	}
	//
	//	@Test
	//	public void givenLabelOfMetadataWithInheritanceIsNullWhenModifyingThenSetToNull() {
	//		inheritedMetadataBuilder.setType(STRING).setLabel("default label");
	//		metadataWithInheritanceBuilder.setLabel(null);
	//
	//		buildAndModify();
	//
	//		assertThat(metadataWithInheritanceBuilder.getLabel()).isNull();
	//	}
	//
	//	@Test
	//	public void givenLabelOfMetadataWithInheritanceIsSameAsItInheritanceWhenModifyingThenSetToNull() {
	//		inheritedMetadataBuilder.setType(STRING).setLabel("default label");
	//		metadataWithInheritanceBuilder.setLabel("default label");
	//
	//		buildAndModify();
	//
	//		assertThat(metadataWithInheritanceBuilder.getLabel()).isNull();
	//	}
	//
	//	@Test
	//	public void givenLabelOfMetadataWithInheritanceIsDifferentWhenBuildingThenSetToCustomizedValue() {
	//		inheritedMetadataBuilder.setType(STRING).setLabel("default label");
	//		metadataWithInheritanceBuilder.setLabel("custom label");
	//
	//		build();
	//
	//		assertThat(metadataWithInheritance.getLabel()).isEqualTo("custom label");
	//	}
	//
	//	@Test
	//	public void givenLabelOfMetadataWithInheritanceIsDifferentWhenModifyingThenSetToCustomizedValue() {
	//		inheritedMetadataBuilder.setType(STRING).setLabel("default label");
	//		metadataWithInheritanceBuilder.setLabel("custom label");
	//
	//		buildAndModify();
	//
	//		assertThat(metadataWithInheritanceBuilder.getLabel()).isEqualTo("custom label");
	//	}
	//
	//	@Test
	//	public void givenLabelOfMetadataWithInheritanceIsNotDefinedWhenBuildingThenSetToCodeValue() {
	//		inheritedMetadataBuilder.setType(STRING);
	//
	//		build();
	//
	//		assertThat(inheritedMetadataBuilder.getLabel()).isEqualTo(CODE_DEFAULT_METADATA);
	//	}

	@Test(expected = MetadataBuilderRuntimeException.InvalidAttribute.class)
	public void givenCodeFinishingWithIdThenException()
			throws Exception {
		MetadataBuilder.createMetadataWithoutInheritance(CODE_DEFAULT_METADATA + "pid",
				schemaBuilder).buildWithoutInheritance(typesFactory, modelLayerFactory);

	}

}
