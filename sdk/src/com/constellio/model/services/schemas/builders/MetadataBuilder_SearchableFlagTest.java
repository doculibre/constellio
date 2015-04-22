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

public class MetadataBuilder_SearchableFlagTest extends MetadataBuilderTest {

	@Test
	public void givenSearchableFlagUndefinedOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.isSearchable()).isFalse();
	}

	@Test
	public void givenSearchableFlagUndefinedOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isSearchable()).isFalse();
	}

	@Test
	public void givenSearchableFlagSetToFalseOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setSearchable(false);

		build();

		assertThat(metadataWithoutInheritance.isSearchable()).isFalse();
	}

	@Test
	public void givenSearchableFlagSetToFalseOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setSearchable(false);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isSearchable()).isFalse();
	}

	@Test
	public void givenSearchableFlagSetToTrueOnMetadataWithoutInheritanceWhenBuildingThenSearchable()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setSearchable(true);

		build();

		assertThat(metadataWithoutInheritance.isSearchable()).isTrue();
	}

	@Test
	public void givenSearchableFlagSetToTrueOnMetadataWithoutInheritanceWhenModifyingThenSearchable()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setSearchable(true);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isSearchable()).isTrue();
	}

	@Test
	public void givenMutlivalueFlagOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setSearchable(true);

		build();

		assertThat(metadataWithInheritance.isSearchable()).isTrue();
	}

	@Test
	public void givenSearchableFlagOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setSearchable(true);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.isSearchable()).isTrue();
	}

	@Test
	public void givenTrueFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setSearchable(true);
		assertThat(metadataWithInheritanceBuilder.isSearchable()).isTrue();

	}

	@Test
	public void givenFalseFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setSearchable(false);
		assertThat(metadataWithInheritanceBuilder.isSearchable()).isFalse();

	}
}
