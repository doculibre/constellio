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

public class MetadataBuilder_EnableFlagTest extends MetadataBuilderTest {

	@Test
	public void givenEnabledFlagOfMetadataWithoutInheritanceIsNullWhenBuildingThenSetToTrue() {
		metadataWithoutInheritanceBuilder.setType(STRING).setEnabled(null);

		build();

		assertThat(metadataWithoutInheritance.isEnabled()).isTrue();
	}

	@Test
	public void givenEnabledFlagOfMetadataWithoutInheritanceIsNullWhenModifyingThenSetToTrue() {
		metadataWithoutInheritanceBuilder.setType(STRING).setEnabled(null);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getEnabled()).isTrue();
	}

	@Test
	public void givenEnabledFlagOfMetadataWithoutInheritanceIsNotDefinedWhenBuildingThenSetToTrue() {
		metadataWithoutInheritanceBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.isEnabled()).isTrue();
	}

	@Test
	public void givenEnabledFlagOfMetadataWithoutInheritanceIsFalseWhenBuildingThenSetToFalse() {
		metadataWithoutInheritanceBuilder.setType(STRING).setEnabled(false);

		build();

		assertThat(metadataWithInheritance.isEnabled()).isFalse();
	}

	@Test
	public void givenEnabledFlagOfMetadataWithoutInheritanceIsFalseWhenModifyingThenSetToFalse() {
		metadataWithoutInheritanceBuilder.setType(STRING).setEnabled(false);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.getEnabled()).isFalse();
	}

	@Test
	public void givenEnabledFlagOfMetadataWithInheritanceIsDifferentWhenBuildingThenSetToCustomizedValue() {
		inheritedMetadataBuilder.setType(STRING).setEnabled(false);
		metadataWithInheritanceBuilder.setEnabled(true);

		build();

		assertThat(inheritedMetadata.isEnabled()).isFalse();
		assertThat(metadataWithInheritance.isEnabled()).isTrue();
	}

	@Test
	public void givenEnabledFlagOfMetadataWithInheritanceIsDifferentWhenModifyingThenSetToCustomizedValue() {
		inheritedMetadataBuilder.setType(STRING).setEnabled(false);
		metadataWithInheritanceBuilder.setEnabled(true);

		buildAndModify();

		assertThat(inheritedMetadataBuilder.getEnabled()).isFalse();
		assertThat(metadataWithInheritanceBuilder.getEnabled()).isTrue();
	}

	@Test
	public void givenEnabledFlagOfMetadataWithInheritanceIsNullWhenBuildingThenSetToInheritedValue() {
		inheritedMetadataBuilder.setType(STRING).setEnabled(false);
		metadataWithInheritanceBuilder.setEnabled(null);

		build();

		assertThat(inheritedMetadata.isEnabled()).isFalse();
		assertThat(metadataWithInheritance.isEnabled()).isFalse();
	}

	@Test
	public void givenEnabledFlagOfMetadataWithInheritanceIsNullWhenModifyingThenSetToNull() {
		inheritedMetadataBuilder.setType(STRING).setEnabled(false);
		metadataWithInheritanceBuilder.setEnabled(null);

		buildAndModify();

		assertThat(inheritedMetadataBuilder.getEnabled()).isFalse();
		assertThat(metadataWithInheritanceBuilder.getEnabled()).isNull();
	}

	@Test
	public void givenEnabledFlagOfMetadataWithInheritanceIsSameAsInheritanceWhenModifyingThenSetToNull() {
		inheritedMetadataBuilder.setType(STRING).setEnabled(false);
		metadataWithInheritanceBuilder.setEnabled(false);

		buildAndModify();

		assertThat(inheritedMetadataBuilder.getEnabled()).isFalse();
		assertThat(metadataWithInheritanceBuilder.getEnabled()).isNull();
	}

}
