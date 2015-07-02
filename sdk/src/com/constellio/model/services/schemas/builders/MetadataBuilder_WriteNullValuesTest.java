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

public class MetadataBuilder_WriteNullValuesTest extends MetadataBuilderTest {

	@Test
	public void givenWriteNullValuesFlagUndefinedOnMetadataWithoutInheritanceWhenBuildingThenTrue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.isWriteNullValues()).isTrue();
	}

	@Test
	public void givenWriteNullValuesFlagUndefinedOnMetadataWithoutInheritanceWhenModifyingThenTrue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isWriteNullValues()).isTrue();
	}

	@Test
	public void givenWriteNullValuesFlagSetToFalseOnMetadataWithoutInheritanceWhenBuildingThenFalse()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setWriteNullValues(false);

		build();

		assertThat(metadataWithoutInheritance.isWriteNullValues()).isFalse();
	}

	@Test
	public void givenWriteNullValuesFlagSetToFalseOnMetadataWithoutInheritanceWhenModifyingThenFalse()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setWriteNullValues(false);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isWriteNullValues()).isFalse();
	}

	@Test
	public void givenWriteNullValuesFlagSetToTrueOnMetadataWithoutInheritanceWhenBuildingThenWriteNullValues()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setWriteNullValues(true);

		build();

		assertThat(metadataWithoutInheritance.isWriteNullValues()).isTrue();
	}

	@Test
	public void givenWriteNullValuesFlagSetToTrueOnMetadataWithoutInheritanceWhenModifyingThenWriteNullValues()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setWriteNullValues(true);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isWriteNullValues()).isTrue();
	}

	@Test
	public void givenMutlivalueFlagOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setWriteNullValues(true);

		build();

		assertThat(metadataWithInheritance.isWriteNullValues()).isTrue();
	}

	@Test
	public void givenWriteNullValuesFlagOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setWriteNullValues(true);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.isWriteNullValues()).isTrue();
	}

	@Test
	public void givenTrueFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setWriteNullValues(true);
		assertThat(metadataWithInheritanceBuilder.isWriteNullValues()).isTrue();

	}

	@Test
	public void givenFalseFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setWriteNullValues(false);
		assertThat(metadataWithInheritanceBuilder.isWriteNullValues()).isFalse();

	}
}
