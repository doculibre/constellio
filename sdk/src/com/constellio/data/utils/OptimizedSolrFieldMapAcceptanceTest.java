package com.constellio.data.utils;

import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Was developped to support mapping of huge records, but unused
public class OptimizedSolrFieldMapAcceptanceTest extends ConstellioTest {

	@Test
	@InDevelopmentTest
	public void test_construction_with_HashMap_25elements() {

		for (int i = 0; i < 10_000_000; i++) {
			if (i % 100000 == 0) {
				System.out.println(i);
			}
			Map<String, Object> map = new HashMap<>();

			for (int j = 0; j < 25; j++) {
				map.put("metadata" + j, j * 2 + i);
			}

		}


	}


	@Test
	@InDevelopmentTest
	public void test_construction_with_OptimizedMap_25elements() {

		for (int i = 0; i < 10_000_000; i++) {
			if (i % 100000 == 0) {
				System.out.println(i);
			}


			List<String> keys = new ArrayList<>();
			List<Object> values = new ArrayList<>();
			for (int j = 0; j < 25; j++) {
				keys.add("metadata" + j);
				values.add(j * 2 + i);
			}
			OptimizedSolrFieldMap map = new OptimizedSolrFieldMap(keys, values);
		}


	}

	@Test
	@InDevelopmentTest
	public void test_construction_with_HashMap_50elements() {

		for (int i = 0; i < 10_000_000; i++) {
			if (i % 100000 == 0) {
				System.out.println(i);
			}
			Map<String, Object> map = new HashMap<>();

			for (int j = 0; j < 50; j++) {
				map.put("metadata" + j, j * 2 + i);
			}

		}


	}


	@Test
	@InDevelopmentTest
	public void test_construction_with_OptimizedMap_50elements() {

		for (int i = 0; i < 10_000_000; i++) {
			if (i % 100000 == 0) {
				System.out.println(i);
			}


			List<String> keys = new ArrayList<>();
			List<Object> values = new ArrayList<>();
			for (int j = 0; j < 50; j++) {
				keys.add("metadata" + j);
				values.add(j * 2 + i);
			}
			OptimizedSolrFieldMap map = new OptimizedSolrFieldMap(keys, values);
		}


	}

	@Test
	@InDevelopmentTest
	public void test_construction_with_HashMap_100elements() {

		for (int i = 0; i < 10_000_000; i++) {
			if (i % 100000 == 0) {
				System.out.println(i);
			}
			Map<String, Object> map = new HashMap<>();

			for (int j = 0; j < 100; j++) {
				map.put("metadata" + j, j * 2 + i);
			}

		}


	}


	@Test
	@InDevelopmentTest
	public void test_construction_with_OptimizedMap_100elements() {

		for (int i = 0; i < 10_000_000; i++) {
			if (i % 100000 == 0) {
				System.out.println(i);
			}


			List<String> keys = new ArrayList<>();
			List<Object> values = new ArrayList<>();
			for (int j = 0; j < 100; j++) {
				keys.add("metadata" + j);
				values.add(j * 2 + i);
			}
			OptimizedSolrFieldMap map = new OptimizedSolrFieldMap(keys, values);
		}


	}

	// ---- ---- ----


	@Test
	@InDevelopmentTest
	public void test_get_with_HashMap_25elements() {

		Map<String, Object> map = new HashMap<>();

		for (int j = 0; j < 25; j++) {
			map.put("metadata" + j, j * 2 + 5);
		}

		for (int i = 0; i < 10_000_000; i++) {
			if (i % 100000 == 0) {
				System.out.println(i);
			}

			for (int j = 0; j < 25; j++) {
				Object v = map.get("metadata" + j);
			}

		}


	}


	@Test
	@InDevelopmentTest
	public void test_get_with_OptimizedMap_25elements() {

		List<String> keys = new ArrayList<>();
		List<Object> values = new ArrayList<>();
		for (int j = 0; j < 25; j++) {
			keys.add("metadata" + j);
			values.add(j * 2 + 5);
		}
		OptimizedSolrFieldMap map = new OptimizedSolrFieldMap(keys, values);

		for (int i = 0; i < 10_000_000; i++) {
			if (i % 100000 == 0) {
				System.out.println(i);
			}

			for (int j = 0; j < 25; j++) {
				Object v = map.get("metadata" + j);
			}

		}


	}

	@Test
	@InDevelopmentTest
	public void test_get_with_HashMap_50elements() {

		Map<String, Object> map = new HashMap<>();

		for (int j = 0; j < 50; j++) {
			map.put("metadata" + j, j * 2 + 5);
		}

		for (int i = 0; i < 10_000_000; i++) {
			if (i % 100000 == 0) {
				System.out.println(i);
			}

			for (int j = 0; j < 50; j++) {
				Object v = map.get("metadata" + j);
			}

		}


	}


	@Test
	@InDevelopmentTest
	public void test_get_with_OptimizedMap_50elements() {

		List<String> keys = new ArrayList<>();
		List<Object> values = new ArrayList<>();
		for (int j = 0; j < 50; j++) {
			keys.add("metadata" + j);
			values.add(j * 2 + 5);
		}
		OptimizedSolrFieldMap map = new OptimizedSolrFieldMap(keys, values);

		for (int i = 0; i < 10_000_000; i++) {
			if (i % 100000 == 0) {
				System.out.println(i);
			}

			for (int j = 0; j < 50; j++) {
				Object v = map.get("metadata" + j);
			}

		}


	}


	@Test
	@InDevelopmentTest
	public void test_get_with_HashMap_100elements() {

		Map<String, Object> map = new HashMap<>();

		for (int j = 0; j < 100; j++) {
			map.put("metadata" + j, j * 2 + 5);
		}

		for (int i = 0; i < 10_000_000; i++) {
			if (i % 100000 == 0) {
				System.out.println(i);
			}

			for (int j = 0; j < 100; j++) {
				Object v = map.get("metadata" + j);
			}

		}


	}


	@Test
	@InDevelopmentTest
	public void test_get_with_OptimizedMap_100elements() {

		List<String> keys = new ArrayList<>();
		List<Object> values = new ArrayList<>();
		for (int j = 0; j < 100; j++) {
			keys.add("metadata" + j);
			values.add(j * 2 + 5);
		}
		OptimizedSolrFieldMap map = new OptimizedSolrFieldMap(keys, values);

		for (int i = 0; i < 10_000_000; i++) {
			if (i % 100000 == 0) {
				System.out.println(i);
			}

			for (int j = 0; j < 100; j++) {
				Object v = map.get("metadata" + j);
			}

		}


	}
}
