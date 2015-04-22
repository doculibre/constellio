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
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MetadataBuilder_RequirementTest extends MetadataBuilderTest {

	@Test
	public void givenDefaultRequirementFlagOfMetadataWithoutInheritanceIsNullWhenBuildingThenSetToFalse() {
		metadataWithoutInheritanceBuilder.setType(STRING).setDefaultRequirement(null);

		build();

		assertThat(metadataWithoutInheritance.isDefaultRequirement()).isFalse();
	}

	@Test
	public void givenDefaultRequirementFlagOfMetadataWithoutInheritanceIsNullWhenModifyingThenSetToFalse() {
		metadataWithoutInheritanceBuilder.setType(STRING).setDefaultRequirement(null);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getDefaultRequirement()).isFalse();
	}

	@Test
	public void givenDefaultRequirementFlagOfMetadataWithoutInheritanceIsNotDefinedWhenBuildingThenSetToFalse() {
		metadataWithoutInheritanceBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.isDefaultRequirement()).isFalse();
	}

	@Test
	public void givenDefaultRequirementFlagOfMetadataWithoutInheritanceIsTrueWhenBuildingThenSetToTrue() {
		metadataWithoutInheritanceBuilder.setType(STRING).setDefaultRequirement(true);

		build();

		assertThat(metadataWithoutInheritanceBuilder.getDefaultRequirement()).isTrue();
	}

	@Test
	public void givenDefaultRequirementFlagOfMetadataWithoutInheritanceIsTrueWhenModifyingThenSetToTrue() {
		metadataWithoutInheritanceBuilder.setType(STRING).setDefaultRequirement(true);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getDefaultRequirement()).isTrue();
	}

	@Test
	public void givenDefaultRequirementFlagOfMetadataWithInheritanceIsDifferentWhenBuildingThenSetToCustomizedValue() {
		inheritedMetadataBuilder.setType(STRING).setDefaultRequirement(false);
		metadataWithInheritanceBuilder.setDefaultRequirement(true);

		build();

		assertThat(inheritedMetadata.isDefaultRequirement()).isFalse();
		assertThat(metadataWithInheritance.isDefaultRequirement()).isTrue();
	}

	@Test
	public void givenDefaultRequirementFlagOfMetadataWithInheritanceIsDifferentWhenModifyingThenSetToCustomizedValue() {
		inheritedMetadataBuilder.setType(STRING).setDefaultRequirement(false);
		metadataWithInheritanceBuilder.setDefaultRequirement(true);

		buildAndModify();

		assertThat(inheritedMetadataBuilder.getDefaultRequirement()).isFalse();
		assertThat(metadataWithInheritanceBuilder.getDefaultRequirement()).isTrue();
	}

	@Test
	public void givenDefaultRequirementFlagOfMetadataWithInheritanceIsNullWhenBuildingThenSetToInheritedValue() {
		inheritedMetadataBuilder.setType(STRING).setDefaultRequirement(false);
		metadataWithInheritanceBuilder.setDefaultRequirement(null);

		build();

		assertThat(inheritedMetadata.isDefaultRequirement()).isFalse();
		assertThat(metadataWithInheritance.isDefaultRequirement()).isFalse();
	}

	@Test
	public void givenDefaultRequirementFlagOfMetadataWithInheritanceIsNullWhenModifyingThenSetToNull() {
		inheritedMetadataBuilder.setType(STRING).setDefaultRequirement(false);
		metadataWithInheritanceBuilder.setDefaultRequirement(null);

		buildAndModify();

		assertThat(inheritedMetadataBuilder.getDefaultRequirement()).isFalse();
		assertThat(metadataWithInheritanceBuilder.getDefaultRequirement()).isNull();
	}

	@Test
	public void givenDefaultRequirementFlagOfMetadataWithInheritanceIsSameAsInheritedWhenModifyingThenSetToNull() {
		inheritedMetadataBuilder.setType(STRING).setDefaultRequirement(true);
		metadataWithInheritanceBuilder.setDefaultRequirement(true);

		buildAndModify();

		assertThat(inheritedMetadataBuilder.getDefaultRequirement()).isTrue();
		assertThat(metadataWithInheritanceBuilder.getDefaultRequirement()).isNull();
	}

}
