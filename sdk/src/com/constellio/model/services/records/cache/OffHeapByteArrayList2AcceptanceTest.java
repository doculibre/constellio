package com.constellio.model.services.records.cache;

import com.constellio.data.utils.LangUtils;
import com.constellio.model.services.records.cache.offHeapCollections.OffHeapByteArrayList2;
import com.constellio.model.services.records.cache.offHeapCollections.OffHeapBytesArrayListArea;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.constellio.data.utils.LangUtils.forEachInRandomOrder;
import static org.assertj.core.api.Assertions.assertThat;

public class OffHeapByteArrayList2AcceptanceTest extends ConstellioTest {

	Map<Integer, byte[]> expectedValues = new HashMap<>();

	@Test
	public void whenSavingValuesThenRemovingValuesThenAllRemoved() {

		OffHeapByteArrayList2 tightList = new OffHeapByteArrayList2() {
			@Override
			protected OffHeapBytesArrayListArea newArea() {
				return new OffHeapBytesArrayListArea(1_000_000, 0.0);
			}
		};
		OffHeapByteArrayList2 defaultList = new OffHeapByteArrayList2() {
			@Override
			protected OffHeapBytesArrayListArea newArea() {
				return new OffHeapBytesArrayListArea(1_000_000, 0.025);
			}
		};
		OffHeapByteArrayList2 relaxedList = new OffHeapByteArrayList2() {
			@Override
			protected OffHeapBytesArrayListArea newArea() {
				return new OffHeapBytesArrayListArea(1_000_000, 0.05);
			}
		};

		forEachInRandomOrder(100_000).forEach((index) -> {
			byte[] newValue = randomBytesOfLength(10);
			expectedValues.put(index, newValue);
			tightList.set(index, newValue);
			defaultList.set(index, newValue);
			relaxedList.set(index, newValue);
		});
		assertExpectedValues(tightList);
		assertThat(tightList.size()).isEqualTo(100_000);
		assertExpectedValues(defaultList);
		assertThat(defaultList.size()).isEqualTo(100_000);
		assertExpectedValues(relaxedList);
		assertThat(relaxedList.size()).isEqualTo(100_000);

		List<Integer> randomValues = LangUtils.getIntValuesInRandomOrder(100_000);
		for (int i = 0; i < 50_000; i += 1) {
			int index = randomValues.get(i);
			expectedValues.remove(index);
			tightList.set(index, null);
			defaultList.set(index, null);
			relaxedList.set(index, null);
		}

		assertExpectedValues(tightList);
		assertThat(tightList.size()).isEqualTo(100_000);
		assertExpectedValues(defaultList);
		assertThat(defaultList.size()).isEqualTo(100_000);
		assertExpectedValues(relaxedList);
		assertThat(relaxedList.size()).isEqualTo(100_000);

		forEachInRandomOrder(100_000).forEach((index) -> {
			byte[] newValue = randomBytesOfLength(10);
			expectedValues.put(index, newValue);
			tightList.set(index, newValue);
			defaultList.set(index, newValue);
			relaxedList.set(index, newValue);
		});
		assertExpectedValues(tightList);
		assertThat(tightList.size()).isEqualTo(100_000);
		assertExpectedValues(defaultList);
		assertThat(defaultList.size()).isEqualTo(100_000);
		assertExpectedValues(relaxedList);
		assertThat(relaxedList.size()).isEqualTo(100_000);


		long timeSpentUpdatingTight = 0;
		long timeSpentUpdatingDefault = 0;
		long timeSpentUpdatingRelaxed = 0;
		randomValues = LangUtils.getIntValuesInRandomOrder(100_000);
		for (int i = 0; i < 1_000; i += 1) {
			System.out.println("Final tight - " + i);
			if (i % 2 == 0) {
				byte[] newValue = randomBytesOfLength(9);
				expectedValues.put(i, newValue);

				long start = new Date().getTime();
				tightList.set(i, newValue);
				long end = new Date().getTime();
				timeSpentUpdatingTight += (end - start);

				start = new Date().getTime();
				defaultList.set(i, newValue);
				end = new Date().getTime();
				timeSpentUpdatingDefault += (end - start);

				start = new Date().getTime();
				relaxedList.set(i, newValue);
				end = new Date().getTime();
				timeSpentUpdatingRelaxed += (end - start);
			} else {
				byte[] newValue = randomBytesOfLength(11);
				expectedValues.put(i, newValue);

				long start = new Date().getTime();
				tightList.set(i, newValue);
				long end = new Date().getTime();
				timeSpentUpdatingTight += (end - start);

				start = new Date().getTime();
				defaultList.set(i, newValue);
				end = new Date().getTime();
				timeSpentUpdatingDefault += (end - start);

				start = new Date().getTime();
				relaxedList.set(i, newValue);
				end = new Date().getTime();
				timeSpentUpdatingRelaxed += (end - start);
			}
		}

		System.out.println("tight : " + timeSpentUpdatingTight);
		System.out.println("default : " + timeSpentUpdatingDefault);
		System.out.println("relaxed : " + timeSpentUpdatingRelaxed);

		assertExpectedValues(tightList);
		assertThat(tightList.getMappedAreasCount()).isEqualTo(1);
		assertExpectedValues(defaultList);
		assertThat(defaultList.getMappedAreasCount()).isEqualTo(2);
		assertExpectedValues(relaxedList);
		assertThat(relaxedList.getMappedAreasCount()).isEqualTo(2);

		tightList.clear();
		defaultList.clear();
		relaxedList.clear();

	}


	public void assertExpectedValues(OffHeapByteArrayList2 list) {
		expectedValues.forEach((i, value) -> {
			assertThat(list.get(i)).describedAs("element at index '" + i + "'").isEqualTo(value);
		});

		for (int i = 0; i < list.size(); i++) {
			assertThat(list.get(i)).describedAs("element at index '" + i + "'").isEqualTo(expectedValues.get(i));
		}
	}

	Random random = new Random();

	private byte[] randomBytesOfLength(int i) {
		byte[] newValue = new byte[i];
		for (int j = 0; j < newValue.length; j++) {
			newValue[j] = (byte) (random.nextInt(256) + Byte.MIN_VALUE);
		}
		return newValue;
	}

}
