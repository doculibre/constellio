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

public class MetadataBuilder_SystemReservedFlagTest extends MetadataBuilderTest {

	@Test
	public void givenSystemReservedFlagUndefinedOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.isSystemReserved()).isFalse();
	}

	@Test
	public void givenSystemReservedFlagUndefinedOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isSystemReserved()).isFalse();
	}

	@Test
	public void givenSystemReservedFlagSetToFalseOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setSystemReserved(false);

		build();

		assertThat(metadataWithoutInheritance.isSystemReserved()).isFalse();
	}

	@Test
	public void givenSystemReservedFlagSetToFalseOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setSystemReserved(false);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isSystemReserved()).isFalse();
	}

	@Test
	public void givenSystemReservedFlagSetToTrueOnMetadataWithoutInheritanceWhenBuildingThenSystemReserved()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setSystemReserved(true);

		build();

		assertThat(metadataWithoutInheritance.isSystemReserved()).isTrue();
	}

	@Test
	public void givenSystemReservedFlagSetToTrueOnMetadataWithoutInheritanceWhenModifyingThenSystemReserved()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setSystemReserved(true);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isSystemReserved()).isTrue();
	}

	@Test
	public void givenMutlivalueFlagOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setSystemReserved(true);

		build();

		assertThat(metadataWithInheritance.isSystemReserved()).isTrue();
	}

	@Test
	public void givenSystemReservedFlagOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setSystemReserved(true);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.isSystemReserved()).isTrue();
	}

	@Test
	public void givenSystemReservedFlagThenUndeletable()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setSystemReserved(true);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.isSystemReserved()).isTrue();
		assertThat(metadataWithInheritanceBuilder.isUndeletable()).isTrue();
	}

	@Test
	public void givenTrueFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setSystemReserved(true);
		assertThat(metadataWithInheritanceBuilder.isSystemReserved()).isTrue();

	}

	@Test
	public void givenFalseFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setSystemReserved(false);
		assertThat(metadataWithInheritanceBuilder.isSystemReserved()).isFalse();

	}
}
