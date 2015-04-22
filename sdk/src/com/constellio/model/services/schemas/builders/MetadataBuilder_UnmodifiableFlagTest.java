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

public class MetadataBuilder_UnmodifiableFlagTest extends MetadataBuilderTest {

	@Test
	public void givenUnmodifiableFlagUndefinedOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.isUnmodifiable()).isFalse();
	}

	@Test
	public void givenUnmodifiableFlagUndefinedOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isUnmodifiable()).isFalse();
	}

	@Test
	public void givenUnmodifiableFlagSetToFalseOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setUnmodifiable(false);

		build();

		assertThat(metadataWithoutInheritance.isUnmodifiable()).isFalse();
	}

	@Test
	public void givenUnmodifiableFlagSetToFalseOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setUnmodifiable(false);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isUnmodifiable()).isFalse();
	}

	@Test
	public void givenUnmodifiableFlagSetToTrueOnMetadataWithoutInheritanceWhenBuildingThenUnmodifiable()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setUnmodifiable(true);

		build();

		assertThat(metadataWithoutInheritance.isUnmodifiable()).isTrue();
	}

	@Test
	public void givenUnmodifiableFlagSetToTrueOnMetadataWithoutInheritanceWhenModifyingThenUnmodifiable()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setUnmodifiable(true);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isUnmodifiable()).isTrue();
	}

	@Test
	public void givenMutlivalueFlagOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setUnmodifiable(true);

		build();

		assertThat(metadataWithInheritance.isUnmodifiable()).isTrue();
	}

	@Test
	public void givenUnmodifiableFlagOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setUnmodifiable(true);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.isUnmodifiable()).isTrue();
	}

	@Test
	public void givenTrueFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setUnmodifiable(true);
		assertThat(metadataWithInheritanceBuilder.isUnmodifiable()).isTrue();

	}

	@Test
	public void givenFalseFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setUnmodifiable(false);
		assertThat(metadataWithInheritanceBuilder.isUnmodifiable()).isFalse();

	}
}
