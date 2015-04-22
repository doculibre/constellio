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
package com.constellio.model.services.records;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.records.Record;
import com.constellio.sdk.tests.ConstellioTest;

public class RecordUtilsUnitTest extends ConstellioTest {

	RecordUtils recordUtils;

	@Mock Record recordOfCustomSchemaOfType1;
	@Mock Record recordOfDefaultSchemaOfType1;
	@Mock Record recordOfCustomSchemaOfType2;
	@Mock Record recordOfDefaultSchemaOfType2;

	@Before
	public void setUp()
			throws Exception {
		recordUtils = new RecordUtils();

		when(recordOfCustomSchemaOfType1.getSchemaCode()).thenReturn("type1_custom");
		when(recordOfDefaultSchemaOfType1.getSchemaCode()).thenReturn("type1_default");
		when(recordOfCustomSchemaOfType2.getSchemaCode()).thenReturn("type2_custom");
		when(recordOfDefaultSchemaOfType2.getSchemaCode()).thenReturn("type2_default");

	}

	@Test
	public void whenSplitRecordBySchemaTypeThenPutDefaultAndCustomSchemaOfSameTypesTogether()
			throws Exception {

		List<Record> records = Arrays.asList(recordOfCustomSchemaOfType1, recordOfDefaultSchemaOfType1,
				recordOfCustomSchemaOfType2, recordOfDefaultSchemaOfType2);

		Map<String, List<Record>> results = recordUtils.splitRecordsBySchemaTypes(records);
		assertThat(results).hasSize(2);
		assertThat(results)
				.containsEntry("type1", Arrays.asList(recordOfCustomSchemaOfType1, recordOfDefaultSchemaOfType1));
		assertThat(results)
				.containsEntry("type2", Arrays.asList(recordOfCustomSchemaOfType2, recordOfDefaultSchemaOfType2));
	}
}
