package com.constellio.model.entities.structures;

import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UrlsStructureFactoryTest extends ConstellioTest {

	UrlsStructureFactory factory;

	@Before
	public void setUp()
			throws Exception {
		factory = new UrlsStructureFactory();
	}

	@Test
	public void whenSetAttributeValueThenBecomeDirty() {
		UrlsStructure urlsStructure = new UrlsStructure();
		assertThat(urlsStructure.isDirty()).isFalse();

		urlsStructure = new UrlsStructure();
		urlsStructure.put("key1", "value1");
		assertThat(urlsStructure.isDirty()).isTrue();
	}

	@Test
	public void whenConvertingStructureWithAllValuesThenRemainsEqual()
			throws Exception {
		UrlsStructure urlsStructure = new UrlsStructure();
		urlsStructure.put("key1", "value1");
		urlsStructure.put("key2", "value2");
		urlsStructure.put("key3", "value3");
		urlsStructure.put("key3", "value4");

		String stringValue = factory.toString(urlsStructure);
		UrlsStructure builtUrlsStructure = (UrlsStructure) factory.build(stringValue);
		String stringValue2 = factory.toString(builtUrlsStructure);

		assertThat(builtUrlsStructure).isEqualTo(urlsStructure);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtUrlsStructure.isDirty()).isFalse();
		assertThat(builtUrlsStructure).hasSize(3);
		assertThat(builtUrlsStructure.get("key1")).isEqualTo("value1");
		assertThat(builtUrlsStructure.get("key2")).isEqualTo("value2");
		assertThat(builtUrlsStructure.get("key3")).isEqualTo("value4");
	}

	@Test
	public void whenConvertingStructureWithoutSetValuesThenRemainsEqual()
			throws Exception {

		UrlsStructure urlsStructure = new UrlsStructure();

		String stringValue = factory.toString(urlsStructure);
		UrlsStructure builtUrlsStructure = (UrlsStructure) factory.build(stringValue);
		String stringValue2 = factory.toString(builtUrlsStructure);

		assertThat(builtUrlsStructure).isEqualTo(urlsStructure);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtUrlsStructure.isDirty()).isFalse();
		assertThat(builtUrlsStructure).isEmpty();
	}
}