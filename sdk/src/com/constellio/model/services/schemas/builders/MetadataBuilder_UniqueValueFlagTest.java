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

public class MetadataBuilder_UniqueValueFlagTest extends MetadataBuilderTest {

	@Test
	public void givenUniqueValueFlagUndefinedOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.isUniqueValue()).isFalse();
	}

	@Test
	public void givenUniqueValueFlagUndefinedOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isUniqueValue()).isFalse();
	}

	@Test
	public void givenUniqueValueFlagSetToFalseOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setUniqueValue(false);

		build();

		assertThat(metadataWithoutInheritance.isUniqueValue()).isFalse();
	}

	@Test
	public void givenUniqueValueFlagSetToFalseOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setUniqueValue(false);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isUniqueValue()).isFalse();
	}

	@Test
	public void givenUniqueValueFlagSetToTrueOnMetadataWithoutInheritanceWhenBuildingThenUniqueValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setUniqueValue(true);

		build();

		assertThat(metadataWithoutInheritance.isUniqueValue()).isTrue();
	}

	@Test
	public void givenUniqueValueFlagSetToTrueOnMetadataWithoutInheritanceWhenModifyingThenUniqueValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setUniqueValue(true);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isUniqueValue()).isTrue();
	}

	@Test
	public void givenMutlivalueFlagOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setUniqueValue(true);

		build();

		assertThat(metadataWithInheritance.isUniqueValue()).isTrue();
	}

	@Test
	public void givenUniqueValueFlagOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setUniqueValue(true);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.isUniqueValue()).isTrue();
	}

	@Test(expected = MetadataBuilderRuntimeException.MetadataCannotBeUniqueAndMultivalue.class)
	public void givenSetMultivalueBeforeUniqueValueThenException()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setMultivalue(true).setUniqueValue(true);

		build();
	}

	@Test(expected = MetadataBuilderRuntimeException.MetadataCannotBeUniqueAndMultivalue.class)
	public void givenSetUniqueValueBeforeMultivalueThenException()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setUniqueValue(true).setMultivalue(true);

		build();
	}

	@Test
	public void givenTrueFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setUniqueValue(true);
		assertThat(metadataWithInheritanceBuilder.isUniqueValue()).isTrue();

	}

	@Test
	public void givenFalseFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setUniqueValue(false);
		assertThat(metadataWithInheritanceBuilder.isUniqueValue()).isFalse();

	}
}
