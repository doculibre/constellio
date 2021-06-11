package com.constellio.model.services.schemas.builders;

import org.junit.Test;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.MetadataValueType.TEXT;
import static org.assertj.core.api.Assertions.assertThat;

public class MetadataBuilder_MarkedForTypeOrMulivalueMigrationFlagTest extends MetadataBuilderTest {

	@Test
	public void givenMarkedForTypeOrMutivalueMigrationFlagUndefinedOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.getMarkedForMigrationToType()).isNull();
		assertThat(metadataWithoutInheritance.isMarkedForMigrationToMultivalue()).isNull();
	}

	@Test
	public void givenMarkedForTypeOrMulivalueMigrationFlagUndefinedOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getMarkedForMigrationToType()).isNull();
		assertThat(metadataWithoutInheritanceBuilder.isMarkedForMigrationToMultivalue()).isNull();
	}

	@Test
	public void givenMarkedForTypeOrMultivalueMigrationFlagSetToNullOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING)
				.setMarkedForMigrationToType(null)
				.setMarkedForMigrationToMultivalue(null);

		build();

		assertThat(metadataWithoutInheritance.getMarkedForMigrationToType()).isNull();
		assertThat(metadataWithoutInheritance.isMarkedForMigrationToMultivalue()).isNull();
	}

	@Test
	public void givenMarkedForTypeOrMultivalueMigrationFlagSetToNullOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING)
				.setMarkedForMigrationToType(null)
				.setMarkedForMigrationToMultivalue(null);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getMarkedForMigrationToType()).isNull();
		assertThat(metadataWithoutInheritanceBuilder.isMarkedForMigrationToMultivalue()).isNull();
	}

	@Test
	public void givenMarkedForTypeOrMultivalueMigrationFlagSetToStringAndTrueOnMetadataWithoutInheritanceWhenBuildingThenMarkedForMigration()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(TEXT)
				.setMarkedForMigrationToType(STRING)
				.setMarkedForMigrationToMultivalue(Boolean.TRUE);

		build();

		assertThat(metadataWithoutInheritance.getMarkedForMigrationToType()).isEqualTo(STRING);
		assertThat(metadataWithoutInheritance.isMarkedForMigrationToMultivalue()).isEqualTo(Boolean.TRUE);
	}

	@Test
	public void givenMarkedForTypeOrMultivalueMigrationFlagSetToStringAndTrueOnMetadataWithoutInheritanceWhenModifyingThenMarkedForMigration()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(TEXT)
				.setMarkedForMigrationToType(STRING)
				.setMarkedForMigrationToMultivalue(Boolean.TRUE);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getMarkedForMigrationToType()).isEqualTo(STRING);
		assertThat(metadataWithoutInheritanceBuilder.isMarkedForMigrationToMultivalue()).isEqualTo(Boolean.TRUE);
	}

	@Test
	public void givenTypeOrMutlivalueMigrationFlagSetToStringAndTrueOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(TEXT)
				.setMarkedForMigrationToType(STRING)
				.setMarkedForMigrationToMultivalue(Boolean.TRUE);

		build();

		assertThat(metadataWithInheritance.getMarkedForMigrationToType()).isEqualTo(STRING);
		assertThat(metadataWithInheritance.isMarkedForMigrationToMultivalue()).isEqualTo(Boolean.TRUE);
	}

	@Test
	public void givenMarkedForTypeOrMulivalueMigrationFlagSetToStringAndTrueOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(TEXT)
				.setMarkedForMigrationToType(STRING)
				.setMarkedForMigrationToMultivalue(Boolean.TRUE);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.getMarkedForMigrationToType()).isEqualTo(STRING);
		assertThat(metadataWithInheritanceBuilder.isMarkedForMigrationToMultivalue()).isEqualTo(Boolean.TRUE);
	}

	@Test
	public void givenMarkedForTypeOrMulivalueMigrationFlagSetToStringAndTrueOnModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(TEXT)
				.setMarkedForMigrationToType(STRING)
				.setMarkedForMigrationToMultivalue(Boolean.TRUE);

		assertThat(metadataWithInheritanceBuilder.getMarkedForMigrationToType()).isEqualTo(STRING);
		assertThat(metadataWithInheritanceBuilder.isMarkedForMigrationToMultivalue()).isEqualTo(Boolean.TRUE);
	}

	@Test
	public void givenMarkedForTypeOrMulivalueMigrationFlagSetToNullAndFalseOnModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(TEXT)
				.setMarkedForMigrationToType(null)
				.setMarkedForMigrationToMultivalue(Boolean.FALSE);

		assertThat(metadataWithInheritanceBuilder.getMarkedForMigrationToType()).isNull();
		assertThat(metadataWithInheritanceBuilder.isMarkedForMigrationToMultivalue()).isEqualTo(Boolean.FALSE);
	}
}
