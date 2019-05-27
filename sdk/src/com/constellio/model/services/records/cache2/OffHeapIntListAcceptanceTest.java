package com.constellio.model.services.records.cache2;

import com.constellio.data.utils.ThreadList;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class OffHeapIntListAcceptanceTest extends ConstellioTest {

	@Test
	public void whenGettingAnUnsetIndexThenOutOfBoundException() {
		OffHeapIntList intList = new OffHeapIntList();
		intList.set(0, 42);
		intList.set(1, 46);
		intList.set(2, 48);

		intList.get(4);

		intList.get(100005);
	}

	@Test
	public void whenSavingValuesThenRetrievable() {

		int[] insertedInts = new int[100_000];

		OffHeapIntList intList = new OffHeapIntList();

		Random random = new Random();

		for (int i = 0; i < 500_000; i++) {
			System.out.println(i);
			int modifiedIndex = random.nextInt(100_000);
			int newValue = random.nextInt();
			insertedInts[modifiedIndex] = newValue;
			intList.set(modifiedIndex, newValue);

			assertThat(intList.get(modifiedIndex)).isEqualTo(newValue);
			if (i % 100 == 0) {
				for (int j = 0; j < 100_000; j++) {
					if (intList.get(j) != insertedInts[j]) {
						assertThat(intList.get(j)).isEqualTo(insertedInts[j]);
					}


				}
			}
		}

	}

	@Test
	public void whenOneThreadIsInsertingAndMultipleAreBinarySearchingThenNoProblems() throws Exception {

		OffHeapIntList list = new OffHeapIntList();

		AtomicInteger addedValues = new AtomicInteger();

		list.set(addedValues.get(), addedValues.getAndIncrement());
		list.set(addedValues.get(), addedValues.getAndIncrement());
		list.set(addedValues.get(), addedValues.getAndIncrement());

		Random random = new Random();

		AtomicBoolean finished = new AtomicBoolean();
		ThreadList threadList = new ThreadList();
		for (int i = 0; i < 200; i++) {
			threadList.add(new Thread() {
				@Override
				public void run() {

					while (!finished.get()) {
						int randomSearchedIndex = random.nextInt(addedValues.get() - 1);

						int index = list.binarySearch(randomSearchedIndex);

						if (randomSearchedIndex != index) {
							throw new RuntimeException("Binary search failed");
						}
					}
				}
			});
		}

		threadList.startAll();

		for (int i = 0; i < 1_000_000; i++) {
			if (i % 1000 == 0) {
				System.out.println("Adding " + i);
			}

			list.set(addedValues.get(), addedValues.getAndIncrement());
		}

		finished.set(true);
		threadList.joinAll();
	}

	@Test
	public void whenSavingValuesThenBinarySearchWorking() {


		OffHeapIntList intList = new OffHeapIntList();

		assertThat(intList.binarySearch(0)).isEqualTo(-1);
		assertThat(intList.binarySearch(1)).isEqualTo(-1);

		intList.set(0, 1);
		assertThat(intList.binarySearch(0)).isEqualTo(-1);
		assertThat(intList.binarySearch(1)).isEqualTo(0);
		assertThat(intList.binarySearch(2)).isEqualTo(-1);

		intList.set(1, 3);
		intList.set(2, 4);

		assertThat(intList.binarySearch(0)).isEqualTo(-1);
		assertThat(intList.binarySearch(1)).isEqualTo(0);
		assertThat(intList.binarySearch(2)).isEqualTo(-1);
		assertThat(intList.binarySearch(3)).isEqualTo(1);
		assertThat(intList.binarySearch(4)).isEqualTo(2);
		assertThat(intList.binarySearch(5)).isEqualTo(-1);

		for (int i = 3; i < 500000; i++) {
			intList.set(i, i * 2);
		}

		for (int i = 3; i < 500000; i++) {
			if (intList.binarySearch(i * 2) != i) {
				fail("binary search failed");
			}
		}

	}
}
