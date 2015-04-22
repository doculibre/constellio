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

public class MetadataBuilder_TaxonomyRelationshipFlagTest extends MetadataBuilderTest {

	@Test
	public void givenTaxonomyRelationshipFlagUndefinedOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritance.isTaxonomyRelationship()).isFalse();
	}

	@Test
	public void givenTaxonomyRelationshipFlagUndefinedOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isTaxonomyRelationship()).isFalse();
	}

	@Test
	public void givenTaxonomyRelationshipFlagSetToFalseOnMetadataWithoutInheritanceWhenBuildingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setTaxonomyRelationship(false);

		build();

		assertThat(metadataWithoutInheritance.isTaxonomyRelationship()).isFalse();
	}

	@Test
	public void givenTaxonomyRelationshipFlagSetToFalseOnMetadataWithoutInheritanceWhenModifyingThenSingleValue()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setTaxonomyRelationship(false);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isTaxonomyRelationship()).isFalse();
	}

	@Test
	public void givenTaxonomyRelationshipFlagSetToTrueOnMetadataWithoutInheritanceWhenBuildingThenTaxonomyRelationship()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setTaxonomyRelationship(true);

		build();

		assertThat(metadataWithoutInheritance.isTaxonomyRelationship()).isTrue();
	}

	@Test
	public void givenTaxonomyRelationshipFlagSetToTrueOnMetadataWithoutInheritanceWhenModifyingThenTaxonomyRelationship()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING).setTaxonomyRelationship(true);

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.isTaxonomyRelationship()).isTrue();
	}

	@Test
	public void givenMutlivalueFlagOnMetadataWithInheritanceWhenBuildingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setTaxonomyRelationship(true);

		build();

		assertThat(metadataWithInheritance.isTaxonomyRelationship()).isTrue();
	}

	@Test
	public void givenTaxonomyRelationshipFlagOnMetadataWithInheritanceWhenModifyingThenSetToInheritedValue()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setTaxonomyRelationship(true);

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.isTaxonomyRelationship()).isTrue();
	}

	@Test
	public void givenTrueFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setTaxonomyRelationship(true);
		assertThat(metadataWithInheritanceBuilder.isTaxonomyRelationship()).isTrue();

	}

	@Test
	public void givenFalseFlagModifiedInInheritedMetadataBuilderThenModifiedInMetadataWithHeritance()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).setTaxonomyRelationship(false);
		assertThat(metadataWithInheritanceBuilder.isTaxonomyRelationship()).isFalse();

	}
}
