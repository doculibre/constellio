package com.constellio.model.services.schemas.builders;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.sdk.tests.TestUtils.getElementsClasses;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.constellio.model.services.schemas.testimpl.TestRecordMetadataValidator1;
import com.constellio.model.services.schemas.testimpl.TestRecordMetadataValidator2;

public class MetadataBuilder_ValidatorsTest extends MetadataBuilderTest {

	@Test
	public void givenRecordMetadataValidatorsDefinedInMetadataAndInheritanceWhenBuildingThenMetadataWithInheritanceHasAll()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).defineValidators().add(TestRecordMetadataValidator1.class);
		metadataWithInheritanceBuilder.defineValidators().add(TestRecordMetadataValidator2.class);

		build();

		assertThat(getElementsClasses(inheritedMetadata.getValidators())).containsOnly(TestRecordMetadataValidator1.class);
		assertThat(getElementsClasses(metadataWithInheritance.getValidators())).containsOnly(TestRecordMetadataValidator1.class,
				TestRecordMetadataValidator2.class);

	}

	@Test
	public void givenRecordMetadataValidatorsDefinedInMetadataAndInheritanceWhenModifyingThenMetadataWithInheritanceHasOnlyCustomValidators()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).defineValidators().add(TestRecordMetadataValidator1.class);
		metadataWithInheritanceBuilder.defineValidators().add(TestRecordMetadataValidator2.class);

		buildAndModify();

		assertThat(inheritedMetadataBuilder.defineValidators().implementationsClassname).containsOnly(
				TestRecordMetadataValidator1.class.getName());
		assertThat(metadataWithInheritanceBuilder.defineValidators().implementationsClassname).containsOnly(
				TestRecordMetadataValidator2.class.getName());
	}

	@Test
	public void givenRecordMetadataValidatorsDefinedDuplicatelyInMetadataAndInheritanceWhenBuildingThenNoDuplicate()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).defineValidators().add(TestRecordMetadataValidator1.class);
		metadataWithInheritanceBuilder.defineValidators().add(TestRecordMetadataValidator1.class)
				.add(TestRecordMetadataValidator2.class);

		build();

		assertThat(getElementsClasses(inheritedMetadata.getValidators())).containsOnly(TestRecordMetadataValidator1.class);
		assertThat(getElementsClasses(metadataWithInheritance.getValidators())).containsOnly(TestRecordMetadataValidator1.class,
				TestRecordMetadataValidator2.class);
	}

	@Test
	public void givenRecordMetadataValidatorsDefinedDuplicatelyInMetadataAndInheritanceWhenModifyingThenNoDuplicate()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).defineValidators().add(TestRecordMetadataValidator1.class);
		metadataWithInheritanceBuilder.defineValidators().add(TestRecordMetadataValidator1.class)
				.add(TestRecordMetadataValidator2.class);

		buildAndModify();

		assertThat(inheritedMetadataBuilder.defineValidators().implementationsClassname).containsOnly(
				TestRecordMetadataValidator1.class.getName());
		assertThat(metadataWithInheritanceBuilder.defineValidators().implementationsClassname).containsOnly(
				TestRecordMetadataValidator2.class.getName());
	}

}
