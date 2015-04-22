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

public class MetadataBuilder_ChildOfRelationshipFlagTest extends MetadataBuilderTest {

	@Test
	public void givenChildOfRelationshipFlagUndefinedOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.isChildOfRelationship()).isFalse();
	}

	@Test
	public void givenChildOfRelationshipFlagUndefinedOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isChildOfRelationship()).isFalse();
	}

	@Test
	public void givenChildOfRelationshipFlagSetToFalseOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setChildOfRelationship(false);

		build();

		assertThat(metadataWithoutInheritance.isChildOfRelationship()).isFalse();
	}

	@Test
	public void givenChildOfRelationshipFlagSetToFalseOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setChildOfRelationship(false);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isChildOfRelationship()).isFalse();
	}

	@Test
	public void givenChildOfRelationshipFlagSetToTrueOnMetadataWithoutInheritanceWhenBuildingThenChildOfRelationship()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setChildOfRelationship(true);

		build();

		assertThat(metadataWithoutInheritance.isChildOfRelationship()).isTrue();
	}

	@Test
	public void givenChildOfRelationshipFlagSetToTrueOnMetadataWithoutInheritanceWhenModifyingThenChildOfRelationship()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setChildOfRelationship(true);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isChildOfRelationship()).isTrue();
	}

	@Test
	public void givenMutlivalueFlagOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setChildOfRelationship(true);

		build();

		assertThat(metadataWithInheritance.isChildOfRelationship()).isTrue();
	}

	@Test
	public void givenChildOfRelationshipFlagOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setChildOfRelationship(true);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.isChildOfRelationship()).isTrue();
	}

	@Test
	public void givenTrueFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setChildOfRelationship(true);
		assertThat(metadataWithInheritanceBuilder.isChildOfRelationship()).isTrue();

	}

	@Test
	public void givenFalseFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setChildOfRelationship(false);
		assertThat(metadataWithInheritanceBuilder.isChildOfRelationship()).isFalse();

	}
}
