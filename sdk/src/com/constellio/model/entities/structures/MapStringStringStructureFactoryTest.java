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