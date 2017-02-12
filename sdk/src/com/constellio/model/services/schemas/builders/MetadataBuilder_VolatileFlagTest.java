package com.constellio.model.services.schemas.builders;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.constellio.model.entities.schemas.MetadataVolatility;

public class MetadataBuilder_VolatileFlagTest extends MetadataBuilderTest {

	@Test
	public void givenVolatileFlagUndefinedOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.getVolatility()).isEqualTo(MetadataVolatility.PERSISTED);
	}

	@Test
	public void givenVolatileFlagUndefinedOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getVolatility()).isEqualTo(MetadataVolatility.PERSISTED);
	}

	@Test
	public void givenVolatileFlagSetToFalseOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setVolatility(MetadataVolatility.PERSISTED);

		build();

		assertThat(metadataWithoutInheritance.getVolatility()).isEqualTo(MetadataVolatility.PERSISTED);
	}

	@Test
	public void givenVolatileFlagSetToFalseOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setVolatility(MetadataVolatility.PERSISTED);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getVolatility()).isEqualTo(MetadataVolatility.PERSISTED);
	}

	@Test
	public void givenVolatileFlagSetToVolatileLazyOnMetadataWithoutInheritanceWhenBuildingThenVolatile()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setVolatility(MetadataVolatility.VOLATILE_LAZY);

		build();

		assertThat(metadataWithoutInheritance.getVolatility()).isEqualTo(MetadataVolatility.VOLATILE_LAZY);
	}

	@Test
	public void givenVolatileFlagSetToVolatileEagerOnMetadataWithoutInheritanceWhenBuildingThenVolatile()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setVolatility(MetadataVolatility.VOLATILE_EAGER);

		build();

		assertThat(metadataWithoutInheritance.getVolatility()).isEqualTo(MetadataVolatility.VOLATILE_EAGER);
	}

	@Test
	public void givenVolatileFlagSetToVolatileLazyOnMetadataWithoutInheritanceWhenModifyingThenVolatile()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setVolatility(MetadataVolatility.VOLATILE_LAZY);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getVolatility()).isEqualTo(MetadataVolatility.VOLATILE_LAZY);
	}

	@Test
	public void givenVolatileFlagSetToVolatileEagerOnMetadataWithoutInheritanceWhenModifyingThenVolatile()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setVolatility(MetadataVolatility.VOLATILE_EAGER);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getVolatility()).isEqualTo(MetadataVolatility.VOLATILE_EAGER);
	}

	@Test
	public void givenVolatileFlagOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setVolatility(MetadataVolatility.VOLATILE_LAZY);

		build();

		assertThat(metadataWithInheritance.getVolatility()).isEqualTo(MetadataVolatility.VOLATILE_LAZY);
	}

	@Test
	public void givenVolatileFlagOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setVolatility(MetadataVolatility.VOLATILE_LAZY);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.getVolatility()).isEqualTo(MetadataVolatility.VOLATILE_LAZY);
	}

	@Test
	public void givenVolatileLazyFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setVolatility(MetadataVolatility.VOLATILE_LAZY);
		assertThat(metadataWithInheritanceBuilder.getVolatility()).isEqualTo(MetadataVolatility.VOLATILE_LAZY);

	}

	@Test
	public void givenVolatileEagerFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setVolatility(MetadataVolatility.VOLATILE_EAGER);
		assertThat(metadataWithInheritanceBuilder.getVolatility()).isEqualTo(MetadataVolatility.VOLATILE_EAGER);

	}

	@Test
	public void givenFalseFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setVolatility(MetadataVolatility.PERSISTED);
		assertThat(metadataWithInheritanceBuilder.getVolatility()).isEqualTo(MetadataVolatility.PERSISTED);

	}
}
