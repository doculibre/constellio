package com.constellio.model.services.schemas.builders;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.constellio.model.entities.schemas.MetadataVolatility;

public class MetadataBuilder_VolatileFlagTest extends MetadataBuilderTest {

	@Test
	public void givenVolatileFlagUndefinedOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING)
				.defineDataEntry().asJexlScript("title");

		build();

		assertThat(metadataWithoutInheritance.getVolatility()).isEqualTo(MetadataVolatility.PERSISTED);
	}

	@Test
	public void givenVolatileFlagUndefinedOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING)
				.defineDataEntry().asJexlScript("title");

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getVolatility()).isEqualTo(MetadataVolatility.PERSISTED);
	}

	@Test
	public void givenVolatileFlagSetToFalseOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setVolatility(MetadataVolatility.PERSISTED)
				.defineDataEntry().asJexlScript("title");

		build();

		assertThat(metadataWithoutInheritance.getVolatility()).isEqualTo(MetadataVolatility.PERSISTED);
	}

	@Test
	public void givenVolatileFlagSetToFalseOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setVolatility(MetadataVolatility.PERSISTED)
				.defineDataEntry().asJexlScript("title");

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getVolatility()).isEqualTo(MetadataVolatility.PERSISTED);
	}

	@Test
	public void givenVolatileFlagSetToVolatileLazyOnMetadataWithoutInheritanceWhenBuildingThenVolatile()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setVolatility(MetadataVolatility.VOLATILE_LAZY)
				.defineDataEntry().asJexlScript("title");

		build();

		assertThat(metadataWithoutInheritance.getVolatility()).isEqualTo(MetadataVolatility.VOLATILE_LAZY);
	}

	@Test
	public void givenVolatileFlagSetToVolatileEagerOnMetadataWithoutInheritanceWhenBuildingThenVolatile()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setVolatility(MetadataVolatility.VOLATILE_EAGER)
				.defineDataEntry().asJexlScript("title");

		build();

		assertThat(metadataWithoutInheritance.getVolatility()).isEqualTo(MetadataVolatility.VOLATILE_EAGER);
	}

	@Test
	public void givenVolatileFlagSetToVolatileLazyOnMetadataWithoutInheritanceWhenModifyingThenVolatile()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setVolatility(MetadataVolatility.VOLATILE_LAZY)
				.defineDataEntry().asJexlScript("title");

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getVolatility()).isEqualTo(MetadataVolatility.VOLATILE_LAZY);
	}

	@Test
	public void givenVolatileFlagSetToVolatileEagerOnMetadataWithoutInheritanceWhenModifyingThenVolatile()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setVolatility(MetadataVolatility.VOLATILE_EAGER)
				.defineDataEntry().asJexlScript("title");

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getVolatility()).isEqualTo(MetadataVolatility.VOLATILE_EAGER);
	}

	@Test
	public void givenVolatileFlagOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setVolatility(MetadataVolatility.VOLATILE_LAZY)
				.defineDataEntry().asJexlScript("title");

		build();

		assertThat(metadataWithInheritance.getVolatility()).isEqualTo(MetadataVolatility.VOLATILE_LAZY);
	}

	@Test
	public void givenVolatileFlagOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setVolatility(MetadataVolatility.VOLATILE_LAZY)
				.defineDataEntry().asJexlScript("title");

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.getVolatility()).isEqualTo(MetadataVolatility.VOLATILE_LAZY);
	}

	@Test
	public void givenVolatileLazyFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setVolatility(MetadataVolatility.VOLATILE_LAZY)
				.defineDataEntry().asJexlScript("title");

		assertThat(metadataWithInheritanceBuilder.getVolatility()).isEqualTo(MetadataVolatility.VOLATILE_LAZY);

	}

	@Test
	public void givenVolatileEagerFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setVolatility(MetadataVolatility.VOLATILE_EAGER)
				.defineDataEntry().asJexlScript("title");

		assertThat(metadataWithInheritanceBuilder.getVolatility()).isEqualTo(MetadataVolatility.VOLATILE_EAGER);

	}

	@Test
	public void givenFalseFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setVolatility(MetadataVolatility.PERSISTED)
				.defineDataEntry().asJexlScript("title");

		assertThat(metadataWithInheritanceBuilder.getVolatility()).isEqualTo(MetadataVolatility.PERSISTED);

	}
}
