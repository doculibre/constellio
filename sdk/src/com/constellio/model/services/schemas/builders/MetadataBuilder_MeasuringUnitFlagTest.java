package com.constellio.model.services.schemas.builders;

import org.junit.Test;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

public class MetadataBuilder_MeasuringUnitFlagTest extends MetadataBuilderTest {

	@Test
	public void givenMeasurementUnitUndefinedOnMetadataWithoutInheritanceWhenBuildingThenNull()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.getMeasurementUnit()).isNull();
	}

	@Test
	public void givenMeasurementUnitUndefinedOnMetadataWithoutInheritanceWhenModifyingThenNull()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getMeasurementUnit()).isNull();
	}

	@Test
	public void givenMeasuringUnitSetToCmOnMetadataWithoutInheritanceWhenBuildingThenCm()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setMeasurementUnit("cm");

		build();

		assertThat(metadataWithoutInheritance.getMaxLength()).isEqualTo("cm");
	}

	@Test
	public void givenMeasuringUnitSetToCmOnMetadataWithoutInheritanceWhenModifyingThenCm()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setMeasurementUnit("cm");

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getMaxLength()).isEqualTo("cm");
	}

	@Test
	public void givenMeasuringUnitSetToMmOnMetadataWithoutInheritanceWhenBuildingThenMm()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setMeasurementUnit("mm");

		build();

		assertThat(metadataWithoutInheritance.getMaxLength()).isEqualTo("mm");
	}

	@Test
	public void givenMeasuringUnitSetToMmOnMetadataWithoutInheritanceWhenModifyingThenMm()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setMeasurementUnit("mm");

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getMaxLength()).isEqualTo("mm");
	}

	@Test
	public void givenMutlivalueFlagOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setMeasurementUnit("nm");

		build();

		assertThat(metadataWithInheritance.getMaxLength()).isEqualTo("nm");
	}

	@Test
	public void givenMutlivalueFlagOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setMeasurementUnit("dm");

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.getMaxLength()).isEqualTo("dm");
	}

	@Test
	public void givenMeasuringUnitModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithInheritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setMeasurementUnit("hm");
		assertThat(metadataWithInheritanceBuilder.getMaxLength()).isEqualTo("hm");

	}
}
