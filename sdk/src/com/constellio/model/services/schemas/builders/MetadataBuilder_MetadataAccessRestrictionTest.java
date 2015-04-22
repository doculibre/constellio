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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.constellio.model.entities.schemas.MetadataValueType;

public class MetadataBuilder_MetadataAccessRestrictionTest extends MetadataBuilderTest {

	@Test
	public void whenBuildingMetadataWithoutInheritanceThenSetAccessRestrictions()
			throws Exception {

		metadataWithoutInheritanceBuilder.setType(MetadataValueType.STRING).defineAccessRestrictions()
				.withRequiredReadRole("zeRole");

		build();

		assertThat(metadataWithoutInheritance.getAccessRestrictions().getRequiredReadRoles()).containsOnly("zeRole");
	}

	@Test
	public void whenModifyingMetadataWithoutInheritanceThenSetAccessRestrictions()
			throws Exception {

		metadataWithoutInheritanceBuilder.setType(MetadataValueType.STRING).defineAccessRestrictions()
				.withRequiredReadRole("zeRole");

		buildAndModify();

		assertThat(metadataWithoutInheritanceBuilder.defineAccessRestrictions().getRequiredReadRoles()).containsOnly("zeRole");
	}

	@Test
	public void whenBuildingMetadataWithInheritanceThenSetAccessRestrictionsConfiguredInInheritedMetadata()
			throws Exception {

		inheritedMetadataBuilder.setType(MetadataValueType.STRING).defineAccessRestrictions()
				.withRequiredReadRole("zeRole");

		build();

		assertThat(metadataWithInheritance.getAccessRestrictions().getRequiredReadRoles()).containsOnly("zeRole");
	}

	@Test
	public void whenModifyingMetadataWithInheritanceThenSetNull()
			throws Exception {

		metadataWithoutInheritanceBuilder.setType(MetadataValueType.STRING).defineAccessRestrictions()
				.withRequiredReadRole("zeRole");

		buildAndModify();

		assertThat(metadataWithInheritanceBuilder.defineAccessRestrictions()).isNull();
	}
}
