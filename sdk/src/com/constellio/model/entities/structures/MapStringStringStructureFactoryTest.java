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
package com.constellio.model.entities.structures;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;

public class MapStringStringStructureFactoryTest extends ConstellioTest {

	MapStringStringStructureFactory factory;

	@Before
	public void setUp()
			throws Exception {
		factory = new MapStringStringStructureFactory();
	}

	@Test
	public void whenSetAttributeValueThenBecomeDirty() {
		MapStringStringStructure mapStringStringStructure = new MapStringStringStructure();
		assertThat(mapStringStringStructure.isDirty()).isFalse();

		mapStringStringStructure = new MapStringStringStructure();
		mapStringStringStructure.put("key1", "value1");
		assertThat(mapStringStringStructure.isDirty()).isTrue();
	}

	@Test
	public void whenConvertingStructureWithAllValuesThenRemainsEqual()
			throws Exception {
		MapStringStringStructure mapStringStringStructure = new MapStringStringStructure();
		mapStringStringStructure.put("key1", "value1");
		mapStringStringStructure.put("key2", "value2");
		mapStringStringStructure.put("key3", "value3");
		mapStringStringStructure.put("key3", "value4");

		String stringValue = factory.toString(mapStringStringStructure);
		MapStringStringStructure builtMapStringStringStructure = (MapStringStringStructure) factory.build(stringValue);
		String stringValue2 = factory.toString(builtMapStringStringStructure);

		assertThat(builtMapStringStringStructure).isEqualTo(mapStringStringStructure);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtMapStringStringStructure.isDirty()).isFalse();
		assertThat(builtMapStringStringStructure).hasSize(3);
		assertThat(builtMapStringStringStructure.get("key1")).isEqualTo("value1");
		assertThat(builtMapStringStringStructure.get("key2")).isEqualTo("value2");
		assertThat(builtMapStringStringStructure.get("key3")).isEqualTo("value4");
	}

	@Test
	public void whenConvertingStructureWithoutSetValuesThenRemainsEqual()
			throws Exception {

		MapStringStringStructure mapStringStringStructure = new MapStringStringStructure();

		String stringValue = factory.toString(mapStringStringStructure);
		MapStringStringStructure builtMapStringStringStructure = (MapStringStringStructure) factory.build(stringValue);
		String stringValue2 = factory.toString(builtMapStringStringStructure);

		assertThat(builtMapStringStringStructure).isEqualTo(mapStringStringStructure);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtMapStringStringStructure.isDirty()).isFalse();
		assertThat(builtMapStringStringStructure).isEmpty();
	}
}