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

public class MetadataBuilder_MultivalueFlagTest extends MetadataBuilderTest {

	@Test
	public void givenMultivalueFlagUndefinedOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.isMultivalue()).isFalse();
	}

	@Test
	public void givenMultivalueFlagUndefinedOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isMultivalue()).isFalse();
	}

	@Test
	public void givenMultivalueFlagSetToFalseOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setMultivalue(false);

		build();

		assertThat(metadataWithoutInheritance.isMultivalue()).isFalse();
	}

	@Test
	public void givenMultivalueFlagSetToFalseOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setMultivalue(false);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isMultivalue()).isFalse();
	}

	@Test
	public void givenMultivalueFlagSetToTrueOnMetadataWithoutInheritanceWhenBuildingThenMultivalue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setMultivalue(true);

		build();

		assertThat(metadataWithoutInheritance.isMultivalue()).isTrue();
	}

	@Test
	public void givenMultivalueFlagSetToTrueOnMetadataWithoutInheritanceWhenModifyingThenMultivalue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setMultivalue(true);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isMultivalue()).isTrue();
	}

	@Test
	public void givenMutlivalueFlagOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setMultivalue(true);

		build();

		assertThat(metadataWithInheritance.isMultivalue()).isTrue();
	}

	@Test
	public void givenMultivalueFlagOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setMultivalue(true);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.isMultivalue()).isTrue();
	}

	@Test
	public void givenTrueFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setMultivalue(true);
		assertThat(metadataWithInheritanceBuilder.isMultivalue()).isTrue();

	}

	@Test
	public void givenFalseFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setMultivalue(false);
		assertThat(metadataWithInheritanceBuilder.isMultivalue()).isFalse();

	}
}
