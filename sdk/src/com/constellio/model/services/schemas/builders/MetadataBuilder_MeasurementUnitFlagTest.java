package com.constellio.model.services.schemas.builders;

import org.junit.Test;

import static com.constellio.model.entities.schemas.MetadataValueType.INTEGER;
import static org.assertj.core.api.Assertions.assertThat;

public class MetadataBuilder_MeasurementUnitFlagTest extends MetadataBuilderTest {

	@Test
	public void givenMeasurementUnitUndefinedOnMetadataWithoutInheritanceWhenBuildingThenNull()
			throws Exception {
		inheritedMetadataBuilder.setType(INTEGER);

		build();

		assertThat(metadataWithoutInheritance.getMeasurementUnit()).isNull();
	}

	@Test
	public void givenMeasurementUnitUndefinedOnMetadataWithoutInheritanceWhenModifyingThenNull()
			throws Exception {
		inheritedMetadataBuilder.setType(INTEGER);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getMeasurementUnit()).isNull();
	}

	@Test
	public void givenMeasurementUnitSetToCmOnMetadataWithoutInheritanceWhenBuildingThenCm()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(INTEGER).setMeasurementUnit("cm");

		build();

		assertThat(metadataWithoutInheritance.getMeasurementUnit()).isEqualTo("cm");
	}

	@Test
	public void givenMeasurementUnitSetToCmOnMetadataWithoutInheritanceWhenModifyingThenCm()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(INTEGER).setMeasurementUnit("cm");

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getMeasurementUnit()).isEqualTo("cm");
	}

	@Test
	public void givenMeasurementUnitSetToMmOnMetadataWithoutInheritanceWhenBuildingThenMm()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(INTEGER).setMeasurementUnit("mm");

		build();

		assertThat(metadataWithoutInheritance.getMeasurementUnit()).isEqualTo("mm");
	}

	@Test
	public void givenMeasurementUnitSetToMmOnMetadataWithoutInheritanceWhenModifyingThenMm()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(INTEGER).setMeasurementUnit("mm");

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getMeasurementUnit()).isEqualTo("mm");
	}

	@Test
	public void givenMutlivalueFlagOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(INTEGER).setMeasurementUnit("nm");

		build();

		assertThat(metadataWithInheritance.getMeasurementUnit()).isEqualTo("nm");
	}

	@Test
	public void givenMutlivalueFlagOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(INTEGER).setMeasurementUnit("dm");

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.getMeasurementUnit()).isEqualTo("dm");
	}

	@Test
	public void givenMeasurementUnitModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithInheritance()
			throws Exception {
		inheritedMetadataBuilder.setType(INTEGER).setMeasurementUnit("hm");
		assertThat(metadataWithInheritanceBuilder.getMeasurementUnit()).isEqualTo("hm");

	}
}
