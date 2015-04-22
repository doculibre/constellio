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

import static com.constellio.model.entities.schemas.MetadataValueType.CONTENT;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.constellio.model.services.contents.ContentFactory;
import com.constellio.model.services.schemas.testimpl.TestStructureFactory1;

public class MetadataBuilder_StructureFactoriesTest extends MetadataBuilderTest {

	@Test
	public void givenRecordMetadataStructureFactoryDefinedInInheritedMetadataWhenBuildingThenInherited()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).defineStructureFactory(TestStructureFactory1.class);

		build();

		assertThat(inheritedMetadata.getStructureFactory().getClass()).isEqualTo(TestStructureFactory1.class);
		assertThat(metadataWithInheritance.getStructureFactory().getClass()).isEqualTo(TestStructureFactory1.class);

	}

	@Test
	public void givenRecordMetadataStructureFactoryDefinedInInheritedMetadataWhenModifyingThenInherited()
			throws Exception {
		inheritedMetadataBuilder.setType(STRING).defineStructureFactory(TestStructureFactory1.class);

		buildAndModify();

		assertThat(inheritedMetadataBuilder.getStructureFactory()).isEqualTo(TestStructureFactory1.class);
		assertThat(metadataWithInheritanceBuilder.getStructureFactory()).isEqualTo(TestStructureFactory1.class);

	}

	@Test
	public void givenContentTypeMetadataThenHasContentInfoFactory()
			throws Exception {
		inheritedMetadataBuilder.setType(CONTENT);

		buildAndModify();

		assertThat(inheritedMetadataBuilder.getStructureFactory()).isEqualTo(ContentFactory.class);
		assertThat(metadataWithInheritanceBuilder.getStructureFactory()).isEqualTo(ContentFactory.class);

	}

}
