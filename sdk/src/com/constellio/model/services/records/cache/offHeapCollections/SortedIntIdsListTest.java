package com.constellio.model.services.records.cache.offHeapCollections;

import com.constellio.sdk.tests.ConstellioTest;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SortedIntIdsListTest extends ConstellioTest {

	private static int smallTestedCapacity = (int) (SortedIntIdsList.INITIAL_SIZE * Math.pow(SortedIntIdsList.RESIZE_FACTOR, 5));
	private static int largeTestedCapacity = (int) (SortedIntIdsList.INITIAL_SIZE * Math.pow(SortedIntIdsList.RESIZE_FACTOR, 7));

//	@Before
//	public void validateNotWritingOutsideOfReservedMemory() {
//		Toggle.OFF_HEAP_ADDRESS_VALIDATOR.enable();
//	}

	@Test
	public void whenAddingItemsAtFirstIndexThenAlwaysValid() {

		for (int i = 1; i < 25; i++) {

			SortedIdsList list = new SortedIntIdsList();

			List<String> expectedList = new ArrayList<>();
			for (int j = largeTestedCapacity; j > 0; j--) {
				if (j % 100 == 0) {
					System.out.println("Test #" + i + " - Adding " + id(j) + ", total of allocated memory : " + OffHeapMemoryAllocator.getAllocatedMemory());
				}
				list.add(id(j));
				expectedList.add(0, id(j));
				assertThat(list.size()).isEqualTo(largeTestedCapacity - j + 1);
			}
			assertThat(list.getValues()).isEqualTo(expectedList);
			list.clear();
		}
	}

	@Test
	public void whenAddingSameItemsTwiceThenNoDuplicates() {

		for (int i = 1; i < 25; i++) {

			SortedIdsList list = new SortedIntIdsList();

			List<String> expectedList = new ArrayList<>();

			for (int j = largeTestedCapacity; j > 0; j--) {
				if (j % 100 == 0) {
					System.out.println("Test #" + i + " - Adding " + id(j) + ", total of allocated memory : " + OffHeapMemoryAllocator.getAllocatedMemory());
				}
				list.add(id(j));
				expectedList.add(0, id(j));
				assertThat(list.size()).isEqualTo(largeTestedCapacity - j + 1);
			}

			assertThat(list.getValues()).isEqualTo(expectedList);

			for (int j = largeTestedCapacity; j > 0; j--) {
				list.add(id(j));
				assertThat(list.size()).isEqualTo(largeTestedCapacity);
			}

			assertThat(list.getValues()).isEqualTo(expectedList);
			list.clear();
		}
	}


	@Test
	public void whenAddingItemsAtLastIndexThenAlwaysValid() {

		for (int i = 1; i < 1_000_000; i++) {

			if (i % 25_000 == 0) {
				System.out.println("Test #" + i + ", total of allocated memory : " + OffHeapMemoryAllocator.getAllocatedMemory());
			}
			SortedIdsList list = new SortedIntIdsList();

			List<String> expectedList = new ArrayList<>();
			for (int j = 1; j <= 20; j++) {
				list.add(id(j));
				expectedList.add(id(j));
				assertThat(list.size()).isEqualTo(j);

			}
			assertThat(list.getValues()).isEqualTo(expectedList);

			list.clear();
		}
	}


	@Test
	public void whenAddingItemsAtMiddleIndexThenAlwaysValid() {

		for (int i = 1; i < smallTestedCapacity; i++) {
			System.out.println("Test #" + i);
			SortedIdsList list = new SortedIntIdsList();
			List<String> expectedList = new ArrayList<>();

			int added = 0;
			for (int j = smallTestedCapacity; j > 0; j--) {
				if (j % 3 == 0) {
					added++;
					list.add(id(j));
					expectedList.add(id(j));
					assertThat(list.size()).isEqualTo(added);
				}
			}

			for (int j = 1; j <= smallTestedCapacity; j++) {
				if (j % 3 == 1) {
					added++;
					list.add(id(j));
					expectedList.add(id(j));
					assertThat(list.size()).isEqualTo(added);
				}
			}

			for (int j = smallTestedCapacity; j > 0; j--) {
				if (j % 3 == 2) {
					added++;
					expectedList.add(id(j));
				}
				list.add(id(j));
				assertThat(list.size()).isEqualTo(added);
			}

			Collections.sort(expectedList);
			assertThat(list.getValues()).isEqualTo(expectedList);
			list.clear();

		}
	}


	@Test
	public void whenRemovingLastItemThenRemoved() {

		for (int i = 1; i < 100; i++) {
			System.out.println("Test #" + i);
			SortedIdsList list = new SortedIntIdsList();
			List<String> expectedList = new ArrayList<>();

			for (int j = 1; j <= smallTestedCapacity; j++) {
				list.add(id(j));
				expectedList.add(id(j));
			}


			for (int j = smallTestedCapacity; j > 0; j--) {
				list.remove(id(j));
				expectedList.remove(id(j));
				assertThat(list.size()).isEqualTo(expectedList.size());
				assertThat(list.getValues()).isEqualTo(expectedList);
			}

			assertThat(list.size()).isEqualTo(0);
			assertThat(list.getValues()).isEmpty();
			list.clear();

		}
	}

	@Test
	public void whenRemovingFirstItemThenRemoved() {

		for (int i = 1; i < 100; i++) {
			System.out.println("Test #" + i);
			SortedIdsList list = new SortedIntIdsList();
			List<String> expectedList = new ArrayList<>();

			for (int j = 1; j <= smallTestedCapacity; j++) {
				list.add(id(j));
				expectedList.add(id(j));
			}


			for (int j = 1; j <= smallTestedCapacity; j++) {
				list.remove(id(j));
				expectedList.remove(id(j));
				assertThat(list.size()).isEqualTo(expectedList.size());
				assertThat(list.getValues()).isEqualTo(expectedList);
			}

			assertThat(list.size()).isEqualTo(0);
			assertThat(list.getValues()).isEmpty();
			list.clear();

		}
	}


	@Test
	public void whenRemovingMiddleItemThenRemoved() {

		for (int i = 1; i < 100; i++) {
			System.out.println("Test #" + i);
			SortedIdsList list = new SortedIntIdsList();
			List<String> expectedList = new ArrayList<>();

			for (int j = 1; j <= smallTestedCapacity; j++) {
				list.add(id(j));
				expectedList.add(id(j));
			}


			int removed = 0;
			for (int j = smallTestedCapacity; j > 0; j--) {
				if (j % 3 == 0) {
					removed++;
					list.remove(id(j));
					expectedList.remove(id(j));
					assertThat(list.size()).isEqualTo(expectedList.size());
					assertThat(list.getValues()).isEqualTo(expectedList);
				}
			}

			for (int j = 1; j <= smallTestedCapacity; j++) {
				if (j % 3 == 1) {
					removed++;
					list.remove(id(j));
					expectedList.remove(id(j));
					assertThat(list.size()).isEqualTo(expectedList.size());
					assertThat(list.getValues()).isEqualTo(expectedList);
				}
			}

			for (int j = smallTestedCapacity; j > 0; j--) {
				if (j % 3 == 2) {
					removed++;
					expectedList.remove(id(j));
				}
				list.remove(id(j));
				assertThat(list.size()).isEqualTo(expectedList.size());
				assertThat(list.getValues()).isEqualTo(expectedList);
			}

			assertThat(list.size()).isEqualTo(0);
			assertThat(list.getValues()).isEmpty();
			list.clear();

		}
	}

	private String id(int intId) {
		return StringUtils.leftPad("" + intId, 11, "0");
	}
}
