package com.constellio.model.services.schemas.builders;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Ignore;
import org.junit.Test;

import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderTestCalculatorUtils.CalculatorUsingM1;
import com.constellio.model.services.schemas.testimpl.TestMetadataValueCalculator;
import com.constellio.model.services.schemas.testimpl.TestRecordMetadataValidator2;
import com.constellio.model.services.schemas.testimpl.problems.AbstractTestMetadataValueCalculator;
import com.constellio.model.services.schemas.testimpl.problems.TestMetadataValueCalculatorWithoutDefaultConstructor;

public class MetadataBuilder_DataEntryTest extends MetadataBuilderTest {

	@Test
	public void givenDataEntryUndefinedWhenBuildingThenSetDataEntryToManual() {
		metadataWithoutInheritanceBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.getDataEntry().getType()).isEqualTo(DataEntryType.MANUAL);
	}

	@Test
	public void givenDataEntryUndefinedWhenModifyingThenSetDataEntryToManual() {
		metadataWithoutInheritanceBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getDataEntry().getType()).isEqualTo(DataEntryType.MANUAL);
	}

	@Test
	public void givenDataEntryDefinedToManualWhenBuildingThenSetDataEntryToManual() {
		metadataWithoutInheritanceBuilder.setType(STRING).defineDataEntry().asManual();

		build();

		assertThat(metadataWithoutInheritance.getDataEntry().getType()).isEqualTo(DataEntryType.MANUAL);
	}

	@Test
	public void givenDataEntryDefinedToManualWhenModifyingThenSetDataEntryToManual() {
		metadataWithoutInheritanceBuilder.setType(STRING).defineDataEntry().asManual();

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getDataEntry().getType()).isEqualTo(DataEntryType.MANUAL);
	}

	@Test
	public void givenDataEntryOfMetadataWithInheritanceWhenBuildingThenSetToSameInstance() {
		inheritedMetadataBuilder.setType(STRING).defineDataEntry().asManual();

		build();

		assertThat(metadataWithInheritance.getDataEntry()).isSameAs(inheritedMetadata.getDataEntry());
	}

	@Test
	public void givenDataEntryOfMetadataWithInheritanceWhenModifyingThenSetToManual() {
		inheritedMetadataBuilder.setType(STRING).defineDataEntry().asManual();

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.getDataEntry().getType()).isSameAs(DataEntryType.MANUAL);
	}

	@Test(expected = MetadataSchemaBuilderRuntimeException.CannotModifyAttributeOfInheritingMetadata.class)
	public void givenDataEntryOfMetadataWithInheritanceWhenDefinedThenException() {
		metadataWithInheritanceBuilder.defineDataEntry();
	}

	@Test(expected = MetadataBuilderRuntimeException.InvalidAttribute.class)
	@Ignore
	public void givenDataEntryOfMetadataWithoutInheritanceDefinedWhenDefinedAnotherTimeThenException() {
		metadataWithoutInheritanceBuilder.defineDataEntry().asManual().defineDataEntry();
	}

	@Test
	public void givenDataEntryDefinedAsCopiedWhenBuildingThenHasCorrectAttributes() {
		metadataWithoutInheritanceBuilder.setType(STRING).defineDataEntry()
				.asCopied(referenceOtherSchemaMetadataBuilder, anotherSchemaMetadataBuilder);

		build();
		DataEntry dataEntry = metadataWithoutInheritance.getDataEntry();

		assertThat(dataEntry.getType()).isEqualTo(DataEntryType.COPIED);
		assertThat(dataEntry).isInstanceOf(CopiedDataEntry.class);
		assertThat(((CopiedDataEntry) dataEntry).getReferenceMetadata()).isEqualTo(
				referenceOtherSchemaMetadataBuilder.getCode());
		assertThat(((CopiedDataEntry) dataEntry).getCopiedMetadata()).isEqualTo(anotherSchemaMetadataCompleteCode);
	}

	@Test
	public void givenDataEntryDefinedAsCopiedWhenModifyingThenHasCorrectAttributes() {
		metadataWithoutInheritanceBuilder.setType(STRING).defineDataEntry()
				.asCopied(referenceOtherSchemaMetadataBuilder, anotherSchemaMetadataBuilder);

		buildAndModify();
		DataEntry dataEntry = metadataWithoutInheritanceBuilder.getDataEntry();

		assertThat(dataEntry.getType()).isEqualTo(DataEntryType.COPIED);
		assertThat(dataEntry).isInstanceOf(CopiedDataEntry.class);
		assertThat(((CopiedDataEntry) dataEntry).getReferenceMetadata()).isEqualTo(
				referenceOtherSchemaMetadataBuilder.getCode());
		assertThat(((CopiedDataEntry) dataEntry).getCopiedMetadata()).isEqualTo(anotherSchemaMetadataCompleteCode);

	}

	@Test
	public void givenDataEntryDefinedAsCalculatedWhenBuildingThenHasCorrectAttributes() {
		metadataWithoutInheritanceBuilder.setType(STRING).defineDataEntry().asCalculated(TestMetadataValueCalculator.class);

		build();
		DataEntry dataEntry = metadataWithoutInheritance.getDataEntry();

		assertThat(dataEntry.getType()).isEqualTo(DataEntryType.CALCULATED);
		assertThat(dataEntry).isInstanceOf(CalculatedDataEntry.class);
		assertThat(((CalculatedDataEntry) dataEntry).getCalculator().getClass()).isEqualTo(TestMetadataValueCalculator.class);
	}

	@Test
	public void givenDataEntryDefinedAsCalculatedWhenModifyingThenHasCorrectAttributes() {
		metadataWithoutInheritanceBuilder.setType(STRING).defineDataEntry().asCalculated(TestMetadataValueCalculator.class);

		buildAndModify();
		DataEntry dataEntry = metadataWithoutInheritanceBuilder.getDataEntry();

		assertThat(dataEntry.getType()).isEqualTo(DataEntryType.CALCULATED);
		assertThat(dataEntry).isInstanceOf(CalculatedDataEntry.class);
		assertThat(((CalculatedDataEntry) dataEntry).getCalculator().getClass()).isEqualTo(TestMetadataValueCalculator.class);
	}

	@Test(expected = MetadataBuilderRuntimeException.InvalidAttribute.class)
	public void whenDefineAbstractCalculatorThenException() {
		metadataWithoutInheritanceBuilder.defineDataEntry().asCalculated(AbstractTestMetadataValueCalculator.class);
	}

	@Test(expected = MetadataBuilderRuntimeException.InvalidAttribute.class)
	public void whenDefineCalculatorWithoutDefaultConstructorThenException() {
		metadataWithoutInheritanceBuilder.defineDataEntry().asCalculated(
				TestMetadataValueCalculatorWithoutDefaultConstructor.class);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test(expected = MetadataBuilderRuntimeException.InvalidAttribute.class)
	public void whenDefineCalculatorNotImplementingInterfaceThenException() {
		metadataWithoutInheritanceBuilder.defineDataEntry().asCalculated((Class) TestRecordMetadataValidator2.class);
	}

	@Test
	public void givenFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).defineDataEntry().asCalculated(CalculatorUsingM1.class);
		assertThat(metadataWithInheritanceBuilder.getDataEntry().getType()).isSameAs(DataEntryType.CALCULATED);

	}
}
