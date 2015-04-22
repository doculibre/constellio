/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
