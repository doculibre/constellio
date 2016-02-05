package com.constellio.data.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class KeyListMapTest {

	KeyListMap<String, String> keyListMap;

	@Before
	public void setUp()
			throws Exception {

		keyListMap = new KeyListMap<String, String>();
	}

	@Test
	public void whenAddingItemsThenObtainableInSameOrder()
			throws Exception {

		keyListMap.add("key1", "value1");
		keyListMap.add("key1", "value2");

		assertThat(keyListMap.get("key1")).containsExactly("value1", "value2");
		assertThat(keyListMap.get("key2")).isEmpty();
	}
}
