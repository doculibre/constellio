package com.constellio.model.services.schemas.builders;

import com.constellio.model.entities.schemas.AllowedReferences;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilderRuntimeException.CannotModifyAttributeOfInheritingMetadata;
import com.constellio.sdk.tests.schemas.FakeDataStoreTypeFactory;
import org.junit.Test;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE_TIME;
import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.MetadataValueType.TEXT;
import static org.assertj.core.api.Assertions.assertThat;

public class MetadataBuilder_ValueTypeTest extends MetadataBuilderTest {

	@Test(expected = MetadataBuilderRuntimeException.InvalidAttribute.class)
	public void givenValueTypeOfMetadataWithoutInheritanceNotDefinedWhenBuildingThenException()
			throws Exception {
		build();
	}

	@Test(expected = CannotModifyAttributeOfInheritingMetadata.class)
	public void givenValueTypeOfMetadataWithInheritanceWhenDefinedThenException()
			throws Exception {
		metadataWithInheritanceBuilder.setType(STRING);
	}

	@Test(expected = MetadataBuilderRuntimeException.InvalidAttribute.class)
	public void givenValueTypeOfMetadataWithoutInheritanceDefinedWhenSetTypeCalledAnotherTimeThenException()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setType(STRING);
	}

	@Test()
	public void givenValueTypeOfMetadataWithoutInheritanceDefinedAsNumberWhenBuildingThenSetToNumber()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(NUMBER);

		build();

		assertThat(metadataWithoutInheritance.getType()).isEqualTo(NUMBER);
	}

	@Test()
	public void givenValueTypeOfMetadataWithoutInheritanceDefinedAsNumberWhenModifyingThenSetToNumber()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(NUMBER);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getType()).isEqualTo(NUMBER);
	}

	@Test()
	public void givenValueTypeOfMetadataWithoutInheritanceDefinedAsTextNumberWhenBuildingThenSetToNumber()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(TEXT);

		build();

		assertThat(metadataWithoutInheritance.getType()).isEqualTo(TEXT);
	}

	@Test()
	public void givenValueTypeOfMetadataWithoutInheritanceDefinedAsTextNumberWhenModifyingThenSetToNumber()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(TEXT);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getType()).isEqualTo(TEXT);
	}

	@Test()
	public void givenValueTypeOfMetadataWithInheritanceNotDefinedWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(DATE_TIME);

		build();

		assertThat(metadataWithInheritance.getType()).isEqualTo(DATE_TIME);
	}

	@Test()
	public void givenValueTypeOfMetadataWithInheritanceNotDefinedWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(DATE_TIME);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.getType()).isEqualTo(DATE_TIME);
	}

	@Test()
	public void givenValueTypeUndefinedWhenDefiningAllowedReferencesThenTypeSetToReference()
			throws Exception {
		metadataWithoutInheritanceBuilder.defineReferences();

		assertThat(metadataWithoutInheritanceBuilder.getType()).isEqualTo(REFERENCE);
	}

	@Test()
	public void givenValueTypeDefinedToReferenceWhenDefiningAllowedReferencesThenNothingHappens()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(REFERENCE).defineReferences();
	}

	@Test(expected = MetadataBuilderRuntimeException.AllowedReferencesOnlyUsableOnReferenceTypeMetadata.class)
	public void givenValueTypeDefinedToIncorrectTypeWhenDefiningAllowedReferencesThenException()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(NUMBER).defineReferences();
	}

	@Test(expected = MetadataBuilderRuntimeException.InvalidAttribute.class)
	public void givenValueTypeDefinedToReferenceWithoutAllowedReferencesWhenBuildingThenException()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(REFERENCE);

		build();
	}

	@Test(expected = MetadataBuilderRuntimeException.InvalidAttribute.class)
	public void givenValueTypeDefinedToReferenceWithoutAddedAllowedReferencesWhenBuildingThenException()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(REFERENCE).defineReferences();

		build();
	}

	@Test(expected = CannotModifyAttributeOfInheritingMetadata.class)
	public void givenAllowedReferencesOfMetadataWithInheritanceWhenDefinedThenException()
			throws Exception {
		metadataWithInheritanceBuilder.defineReferences();
	}

	@Test
	public void givenAllowedReferencesOfMetadataWithoutInheritanceDefinedWhenBuildingThenSetToDefinedValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.defineReferences().set(anotherSchemaTypeBuilder);

		build();
		AllowedReferences metadataWithoutInheritanceReferences = metadataWithoutInheritance.getAllowedReferences();

		assertThat(metadataWithoutInheritanceReferences.getAllowedSchemaType()).isEqualTo(anotherSchemaTypeCompleteCode);
	}

	@Test
	public void givenAllowedReferencesOfMetadataWithoutInheritanceDefinedWhenModifyingThenSetToDefinedValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.defineReferences().set(anotherSchemaTypeBuilder);

		buildAndModify();
		AllowedReferences metadataWithoutInheritanceReferences = metadataWithoutInheritanceBuilder.allowedReferencesBuilder
				.build();

		assertThat(metadataWithoutInheritanceReferences.getAllowedSchemaType()).isEqualTo(anotherSchemaTypeCompleteCode);
	}

	@Test
	public void givenAllowedReferencesOfInheritedMetadataDefinedWhenBuildingThenSetToDefinedValueOnInheritingMetadata()
			throws Exception {
		inheritedMetadataBuilder.defineReferences().addCompleteSchemaCode(anotherSchemaCompleteCode);

		build();
		AllowedReferences metadataWithInheritanceReferences = metadataWithInheritance.getAllowedReferences();

		assertThat(metadataWithInheritanceReferences.getAllowedSchemas()).containsOnly(anotherSchemaCompleteCode);
	}

	@Test
	public void givenAllowedReferencesOfInheritedMetadataDefinedWhenModifyingThenSetToNull()
			throws Exception {
		inheritedMetadataBuilder.defineReferences().set(anotherSchemaTypeBuilder);

		buildAndModify();
		assertThat(metadataWithInheritanceBuilder.allowedReferencesBuilder).isNull();
	}

	@Test
	public void givenMetadataWithInheritanceWhenBuildingThenHasInheritedMetadataBehaviors()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithInheritance.getInheritedMetadataBehaviors()).isSameAs(
				inheritedMetadata.getInheritedMetadataBehaviors());
	}

	@Test(expected = MetadataSchemaBuilderRuntimeException.CannotModifyAttributeOfInheritingMetadata.class)
	public void givenMultivalueFlagOnMetadataWithInheritanceWhenDefinedThenException()
			throws Exception {
		metadataWithInheritanceBuilder.setMultivalue(true);
	}

	@Test
	public void givenTextMetadataWithoutInheritanceWhenBuildingThenHasStringDataStoreType()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.getDataStoreType()).isEqualTo(FakeDataStoreTypeFactory.STRING);
	}

	@Test
	public void givenMultipleTextMetadataWithoutInheritanceWhenBuildingThenHasStringsDataStoreType()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setMultivalue(true);

		build();

		assertThat(metadataWithoutInheritance.getDataStoreType()).isEqualTo(FakeDataStoreTypeFactory.STRINGS);
	}

	@Test
	public void givenReferenceMetadataWithoutInheritanceWhenBuildingThenHasStringDataStoreType()
			throws Exception {
		inheritedMetadataBuilder.setType(REFERENCE).defineReferences().add(anotherSchemaBuilder);

		build();

		assertThat(metadataWithoutInheritance.getDataStoreType()).isEqualTo(FakeDataStoreTypeFactory.STRING);
	}

	@Test
	public void givenMultipleReferenceTextMetadataWithoutInheritanceWhenBuildingThenHasStringsDataStoreType()
			throws Exception {
		inheritedMetadataBuilder.setType(REFERENCE).setMultivalue(true).defineReferences().add(anotherSchemaBuilder);

		build();

		assertThat(metadataWithoutInheritance.getDataStoreType()).isEqualTo(FakeDataStoreTypeFactory.STRINGS);
	}

	@Test
	public void givenNumberMetadataWithoutInheritanceWhenBuildingThenHasDoubleDataStoreType()
			throws Exception {
		inheritedMetadataBuilder.setType(NUMBER);

		build();

		assertThat(metadataWithoutInheritance.getDataStoreType()).isEqualTo(FakeDataStoreTypeFactory.DOUBLE);
	}

	@Test
	public void givenMultipleNumberTextMetadataWithoutInheritanceWhenBuildingThenHasDoublesDataStoreType()
			throws Exception {
		inheritedMetadataBuilder.setType(NUMBER).setMultivalue(true);

		build();

		assertThat(metadataWithoutInheritance.getDataStoreType()).isEqualTo(FakeDataStoreTypeFactory.DOUBLES);
	}

	@Test
	public void givenDateMetadataWithoutInheritanceWhenBuildingThenHasDoubleDataStoreType()
			throws Exception {
		inheritedMetadataBuilder.setType(DATE_TIME);

		build();

		assertThat(metadataWithoutInheritance.getDataStoreType()).isEqualTo(FakeDataStoreTypeFactory.DATE);
	}

	@Test
	public void givenMultipleDateMetadataWithoutInheritanceWhenBuildingThenHasDoublesDataStoreType()
			throws Exception {
		inheritedMetadataBuilder.setType(DATE_TIME).setMultivalue(true);

		build();

		assertThat(metadataWithoutInheritance.getDataStoreType()).isEqualTo(FakeDataStoreTypeFactory.DATES);
	}

	@Test
	public void givenBooleanMetadataWithoutInheritanceWhenBuildingThenHasBooleanDataStoreType()
			throws Exception {
		inheritedMetadataBuilder.setType(BOOLEAN);

		build();

		assertThat(metadataWithoutInheritance.getDataStoreType()).isEqualTo(FakeDataStoreTypeFactory.BOOLEAN);
	}

	@Test
	public void givenMultipleBooleanMetadataWithoutInheritanceWhenBuildingThenHasBooleansDataStoreType()
			throws Exception {
		inheritedMetadataBuilder.setType(BOOLEAN).setMultivalue(true);

		build();

		assertThat(metadataWithoutInheritance.getDataStoreType()).isEqualTo(FakeDataStoreTypeFactory.BOOLEANS);
	}

	@Test
	public void givenValueTypeModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(NUMBER);
		assertThat(metadataWithInheritanceBuilder.getType()).isSameAs(NUMBER);

	}

	@Test
	public void givenAllowedReferencesModifiedInInheritedMetadataBuilderThenAllowedReferencesModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.defineReferences().add(anotherSchemaBuilder);
		assertThat(metadataWithInheritanceBuilder.getAllowedReferencesBuider().getSchemas())
				.containsOnly(anotherSchemaBuilder.getCode());

	}
}
