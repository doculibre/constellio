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
package com.constellio.model.services.records.populators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.sdk.tests.ConstellioTest;

public class PathsFieldPopulatorUnitTest extends ConstellioTest {

	@Mock Metadata pathMetadata;
	@Mock Metadata otherMetadata;
	@Mock MetadataSchemaTypes types;

	PathsFieldPopulator populator;

	@Before
	public void setUp()
			throws Exception {

		populator = new PathsFieldPopulator(types);

		when(pathMetadata.getLocalCode()).thenReturn(Schemas.PATH.getLocalCode());
		when(otherMetadata.getLocalCode()).thenReturn(Schemas.PRINCIPAL_PATH.getLocalCode());
	}

	@Test
	public void whenPopulatingForOtherMetadataThenReturnEmptyMap()
			throws Exception {

		assertThat(populator.populateCopyfields(otherMetadata, Arrays.asList("test"))).isEmpty();

	}

	@Test
	public void whenPopulatingForPathMetadataWithEmptyListThenReturnEmptyPartList()
			throws Exception {

		assertThat(populator.populateCopyfields(pathMetadata, new ArrayList<>())).containsEntry("pathParts_ss", Arrays.asList())
				.hasSize(1);

	}

	@Test
	public void whenPopulatingForPathMetadataWithMultiplePathsOfMultipleLevelsThenReturnAllIdsPerLevel()
			throws Exception {

		String path1 = "/taxo1/root/t1SubConcept";
		String path2 = "/taxo1/root";
		String path3 = "/taxo2/root/t2SubConcept/t2SubSubConcept";
		String path4 = "/taxo3/";

		Map<String, Object> results = populator.populateCopyfields(pathMetadata, Arrays.asList(path1, path2, path3, path4));
		List<String> pathsParts = (List) results.get("pathParts_ss");
		assertThat(results).hasSize(1).containsKey("pathParts_ss");
		assertThat(pathsParts).containsOnlyOnce("taxo1_0_root", "taxo2_0_root", "taxo1_1_t1SubConcept", "taxo2_1_t2SubConcept",
				"taxo2_2_t2SubSubConcept");

	}
}
