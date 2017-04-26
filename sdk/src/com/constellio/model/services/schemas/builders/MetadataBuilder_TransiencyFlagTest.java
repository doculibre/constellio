package com.constellio.model.services.schemas.builders;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.constellio.model.entities.schemas.MetadataTransiency;

public class MetadataBuilder_TransiencyFlagTest extends MetadataBuilderTest {

	@Test
	public void givenTransiencyFlagUndefinedOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING)
				.defineDataEntry().asJexlScript("title");

		build();

		assertThat(metadataWithoutInheritance.getTransiency()).isEqualTo(MetadataTransiency.PERSISTED);
	}

	@Test
	public void givenTransiencyFlagUndefinedOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING)
				.defineDataEntry().asJexlScript("title");

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getTransiency()).isEqualTo(MetadataTransiency.PERSISTED);
	}

	@Test
	public void givenTransiencyFlagSetToFalseOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setTransiency(MetadataTransiency.PERSISTED)
				.defineDataEntry().asJexlScript("title");

		build();

		assertThat(metadataWithoutInheritance.getTransiency()).isEqualTo(MetadataTransiency.PERSISTED);
	}

	@Test
	public void givenTransiencyFlagSetToFalseOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setTransiency(MetadataTransiency.PERSISTED)
				.defineDataEntry().asJexlScript("title");

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getTransiency()).isEqualTo(MetadataTransiency.PERSISTED);
	}

	@Test
	public void givenTransiencyFlagSetToTransiencyLazyOnMetadataWithoutInheritanceWhenBuildingThenTransiency()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setTransiency(MetadataTransiency.TRANSIENT_LAZY)
				.defineDataEntry().asJexlScript("title");

		build();

		assertThat(metadataWithoutInheritance.getTransiency()).isEqualTo(MetadataTransiency.TRANSIENT_LAZY);
	}

	@Test
	public void givenTransiencyFlagSetToTransiencyEagerOnMetadataWithoutInheritanceWhenBuildingThenTransiency()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setTransiency(MetadataTransiency.TRANSIENT_EAGER)
				.defineDataEntry().asJexlScript("title");

		build();

		assertThat(metadataWithoutInheritance.getTransiency()).isEqualTo(MetadataTransiency.TRANSIENT_EAGER);
	}

	@Test
	public void givenTransiencyFlagSetToTransiencyLazyOnMetadataWithoutInheritanceWhenModifyingThenTransiency()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setTransiency(MetadataTransiency.TRANSIENT_LAZY)
				.defineDataEntry().asJexlScript("title");

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getTransiency()).isEqualTo(MetadataTransiency.TRANSIENT_LAZY);
	}

	@Test
	public void givenTransiencyFlagSetToTransiencyEagerOnMetadataWithoutInheritanceWhenModifyingThenTransiency()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setTransiency(MetadataTransiency.TRANSIENT_EAGER)
				.defineDataEntry().asJexlScript("title");

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getTransiency()).isEqualTo(MetadataTransiency.TRANSIENT_EAGER);
	}

	@Test
	public void givenTransiencyFlagOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setTransiency(MetadataTransiency.TRANSIENT_LAZY)
				.defineDataEntry().asJexlScript("title");

		build();

		assertThat(metadataWithInheritance.getTransiency()).isEqualTo(MetadataTransiency.TRANSIENT_LAZY);
	}

	@Test
	public void givenTransiencyFlagOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setTransiency(MetadataTransiency.TRANSIENT_LAZY)
				.defineDataEntry().asJexlScript("title");

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.getTransiency()).isEqualTo(MetadataTransiency.TRANSIENT_LAZY);
	}

	@Test
	public void givenTransiencyLazyFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setTransiency(MetadataTransiency.TRANSIENT_LAZY)
				.defineDataEntry().asJexlScript("title");

		assertThat(metadataWithInheritanceBuilder.getTransiency()).isEqualTo(MetadataTransiency.TRANSIENT_LAZY);

	}

	@Test
	public void givenTransiencyEagerFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setTransiency(MetadataTransiency.TRANSIENT_EAGER)
				.defineDataEntry().asJexlScript("title");

		assertThat(metadataWithInheritanceBuilder.getTransiency()).isEqualTo(MetadataTransiency.TRANSIENT_EAGER);

	}

	@Test
	public void givenFalseFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setTransiency(MetadataTransiency.PERSISTED)
				.defineDataEntry().asJexlScript("title");

		assertThat(metadataWithInheritanceBuilder.getTransiency()).isEqualTo(MetadataTransiency.PERSISTED);

	}
}
