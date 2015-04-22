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

public class MetadataBuilder_SortableFlagTest extends MetadataBuilderTest {

	@Test
	public void givenSortableFlagUndefinedOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.isSortable()).isFalse();
	}

	@Test
	public void givenSortableFlagUndefinedOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isSortable()).isFalse();
	}

	@Test
	public void givenSortableFlagSetToFalseOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setSortable(false);

		build();

		assertThat(metadataWithoutInheritance.isSortable()).isFalse();
	}

	@Test
	public void givenSortableFlagSetToFalseOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setSortable(false);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isSortable()).isFalse();
	}

	@Test
	public void givenSortableFlagSetToTrueOnMetadataWithoutInheritanceWhenBuildingThenSortable()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setSortable(true);

		build();

		assertThat(metadataWithoutInheritance.isSortable()).isTrue();
	}

	@Test
	public void givenSortableFlagSetToTrueOnMetadataWithoutInheritanceWhenModifyingThenSortable()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setSortable(true);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isSortable()).isTrue();
	}

	@Test
	public void givenMutlivalueFlagOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setSortable(true);

		build();

		assertThat(metadataWithInheritance.isSortable()).isTrue();
	}

	@Test
	public void givenSortableFlagOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setSortable(true);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.isSortable()).isTrue();
	}

	@Test
	public void givenTrueFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setSortable(true);
		assertThat(metadataWithInheritanceBuilder.isSortable()).isTrue();

	}

	@Test
	public void givenFalseFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setSortable(false);
		assertThat(metadataWithInheritanceBuilder.isSortable()).isFalse();

	}
}
