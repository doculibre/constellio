package com.constellio.model.entities.structures;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;

public class MapStringListStringStructureFactoryTest extends ConstellioTest {

	MapStringListStringStructureFactory factory;

	@Before
	public void setUp()
			throws Exception {
		factory = new MapStringListStringStructureFactory();
	}

	@Test
	public void whenSetAttributeValueThenBecomeDirty() {
		MapStringListStringStructure mapStringListStringStructure = new MapStringListStringStructure();
		assertThat(mapStringListStringStructure.isDirty()).isFalse();

		mapStringListStringStructure = new MapStringListStringStructure();
		mapStringListStringStructure.put("key1", Arrays.asList("value1", "value2"));
		assertThat(mapStringListStringStructure.isDirty()).isTrue();
	}

	@Test
	public void whenConvertingStructureWithAllValuesThenRemainsEqual()
			throws Exception {
		MapStringListStringStructure mapStringListStringStructure = new MapStringListStringStructure();
		mapStringListStringStructure.put("key1", Arrays.asList("value1", "value2"));
		mapStringListStringStructure.put("key2", Arrays.asList("value3", "value4"));
		mapStringListStringStructure.put("key3", Arrays.asList("value5", "value6"));
		mapStringListStringStructure.put("key3", Arrays.asList("value7", "value8"));

		String stringValue = factory.toString(mapStringListStringStructure);
		MapStringListStringStructure builtMapStringListStringStructure = (MapStringListStringStructure) factory.build(stringValue);
		String stringValue2 = factory.toString(builtMapStringListStringStructure);

		assertThat(builtMapStringListStringStructure).isEqualTo(mapStringListStringStructure);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtMapStringListStringStructure.isDirty()).isFalse();
		assertThat(builtMapStringListStringStructure).hasSize(3);
		assertThat(builtMapStringListStringStructure.get("key1")).isEqualTo(Arrays.asList("value1", "value2"));
		assertThat(builtMapStringListStringStructure.get("key2")).isEqualTo(Arrays.asList("value3", "value4"));
		assertThat(builtMapStringListStringStructure.get("key3")).isEqualTo(Arrays.asList("value7", "value8"));
	}

	@Test
	public void whenConvertingStructureWithoutSetValuesThenRemainsEqual()
			throws Exception {

		MapStringListStringStructure mapStringListStringStructure = new MapStringListStringStructure();

		String stringValue = factory.toString(mapStringListStringStructure);
		MapStringListStringStructure builtMapStringListStringStructure = (MapStringListStringStructure) factory.build(stringValue);
		String stringValue2 = factory.toString(builtMapStringListStringStructure);

		assertThat(builtMapStringListStringStructure).isEqualTo(mapStringListStringStructure);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtMapStringListStringStructure.isDirty()).isFalse();
		assertThat(builtMapStringListStringStructure).isEmpty();
	}
}