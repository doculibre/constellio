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
package com.constellio.model.entities.schemas;

import static com.constellio.sdk.tests.TestUtils.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.sdk.tests.ConstellioTest;

public class MetadataSchemaTypeTest extends ConstellioTest {

	@Mock MetadataSchema defaultSchema;
	@Mock MetadataSchema customSchema1;
	@Mock MetadataSchema customSchema2;

	@Mock Metadata defaultSchemaAutomaticMetadata;
	@Mock Metadata customSchema1AutomaticMetadataWithInheritance;
	@Mock Metadata customSchema2AutomaticMetadataWithInheritance;
	@Mock Metadata customSchema2AutomaticMetadataWithoutInheritance;

	MetadataSchemaType type;

	@Before
	public void setUp()
			throws Exception {
		when(customSchema1AutomaticMetadataWithInheritance.getInheritance()).thenReturn(defaultSchemaAutomaticMetadata);
		when(customSchema2AutomaticMetadataWithInheritance.getInheritance()).thenReturn(defaultSchemaAutomaticMetadata);

		type = new MetadataSchemaType(aString(), aString(), aString(), asList(customSchema1, customSchema2), defaultSchema,
				false, true);
	}

	@Test
	public void whenGetAutomaticMetadataThenReturnAllDefaultMetadataAndAllCustomSchemasMetadatasWithoutInheritance()
			throws Exception {

		when(defaultSchema.getAutomaticMetadatas()).thenReturn(asList(defaultSchemaAutomaticMetadata));
		when(customSchema1.getAutomaticMetadatas()).thenReturn(asList(customSchema1AutomaticMetadataWithInheritance));
		when(customSchema2.getAutomaticMetadatas()).thenReturn(
				asList(customSchema2AutomaticMetadataWithInheritance, customSchema2AutomaticMetadataWithoutInheritance));

		List<Metadata> automaticMetadatas = type.getAutomaticMetadatas();

		assertThat(automaticMetadatas).containsOnly(defaultSchemaAutomaticMetadata,
				customSchema2AutomaticMetadataWithoutInheritance);

	}

	@Test
	public void whenGetAllSchemasThenReturnDefaultAndAllCustomSchemas()
			throws Exception {

		assertThat(type.getAllSchemas()).containsOnly(customSchema1, customSchema2, defaultSchema);
	}
}
