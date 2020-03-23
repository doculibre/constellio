package com.constellio.data.utils;

import com.constellio.data.utils.LangUtils.ListComparisonResults;
import com.constellio.data.utils.LangUtils.MapComparisonResults;
import com.constellio.data.utils.LangUtils.ModifiedEntry;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class LangUtilsTest extends ConstellioTest {

	@Test
	public void whenComparingListsThenCorrect()
			throws Exception {

		List<String> before = Arrays.asList("a", "a", "b", "b", "d");
		List<String> after = Arrays.asList("a", "c", "c", "d");

		ListComparisonResults<String> results = new LangUtils().compare(before, after);
		assertThat(results.getNewItems()).containsOnly("c").hasSize(1);
		assertThat(results.getRemovedItems()).containsOnly("b").hasSize(1);

		results = new LangUtils().compare(after, before);
		assertThat(results.getRemovedItems()).containsOnly("c").hasSize(1);
		assertThat(results.getNewItems()).containsOnly("b").hasSize(1);

	}


	@Test
	public void whenComparingUsingSortListsThenCorrect()
			throws Exception {

		List<String> before = Arrays.asList("a", "a", "b", "b", "d");
		List<String> after = Arrays.asList("a", "c", "c", "d");

		ListComparisonResults<String> results = new LangUtils().compareSorting(before, after);
		assertThat(results.getNewItems()).containsOnly("c").hasSize(1);
		assertThat(results.getRemovedItems()).containsOnly("b").hasSize(1);

		results = new LangUtils().compareSorting(after, before);
		assertThat(results.getRemovedItems()).containsOnly("c").hasSize(1);
		assertThat(results.getNewItems()).containsOnly("b").hasSize(1);

	}

	@Test
	public void whenComparingMapsThenCorrect()
			throws Exception {

		Map<String, String> map1 = new HashMap<>();
		map1.put("a", "1");
		map1.put("b", "1");
		map1.put("c", "1");

		Map<String, String> map2 = new HashMap<>();
		map2.put("b", "1");
		map2.put("c", "2");
		map2.put("d", "3");

		MapComparisonResults<String, String> results = LangUtils.compare(map1, map2);
		assertThat(results.getNewEntries()).containsOnly("d");
		assertThat(results.getRemovedEntries()).containsOnly("a");
		assertThat(results.getModifiedEntries()).containsOnly(new ModifiedEntry<>("c", "1", "2"));

		results = LangUtils.compare(map2, map1);
		assertThat(results.getNewEntries()).containsOnly("a");
		assertThat(results.getRemovedEntries()).containsOnly("d");
		assertThat(results.getModifiedEntries()).containsOnly(new ModifiedEntry<>("c", "2", "1"));

	}

	@Test
	public void testSupplierAndParallelExecutor() {

		List<Integer> values = IntStream.range(0, 10_000_000).boxed().collect(toList());

		Supplier<List<Integer>> supplier = LangUtils.newBatchSupplier(values, 2);
		ThreadLocal<Set<Integer>> localReceivers = new ThreadLocal<>();
		List<Set<Integer>> allReceivers = new ArrayList<>();

		LangUtils.executeInParallelUntilSupplierReturnsNull(6, supplier, (items) -> {

			Set<Integer> localReceiver = localReceivers.get();
			if (localReceiver == null) {
				localReceiver = new HashSet<>();
				localReceivers.set(localReceiver);
				allReceivers.add(localReceiver);
			}

			localReceiver.addAll(items);
		});

		Set<Integer> setReceiver = new HashSet<>();
		List<Integer> listReceiver = new ArrayList<>();
		for (Set<Integer> aReceiver : allReceivers) {
			setReceiver.addAll(aReceiver);
			listReceiver.addAll(aReceiver);
		}
		assertThat(setReceiver).hasSize(10_000_000);
		assertThat(listReceiver).hasSize(10_000_000);

	}
}
