package com.constellio.model.services.records.cache;

import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.services.records.cache.offHeapCollections.OffHeapByteArrayList;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class OffHeapByteArrayListAcceptanceTest extends ConstellioTest {


	//@Test
	public void whenSavingValuesThenRetrievableValidatingMemoryUsage() {

		Toggle.OFF_HEAP_ADDRESS_VALIDATOR = true;
		try {
			byte[][] insertedValues = new byte[100_000][];

			OffHeapByteArrayList list = new OffHeapByteArrayList();

			Random random = new Random();

			for (int i = 0; i < 5_000; i++) {
				System.out.println(i);
				int modifiedIndex = random.nextInt(100_000);
				byte[] newValue = new byte[random.nextInt(10)];
				for (int j = 0; j < newValue.length; j++) {
					newValue[j] = (byte) (random.nextInt(256) + Byte.MIN_VALUE);
				}
				insertedValues[modifiedIndex] = newValue;
				list.set(modifiedIndex, newValue);

				assertSameBytes(newValue, list.get(modifiedIndex));
				if (i % 100 == 0) {
					for (int j = 0; j < 100_000; j++) {
						assertSameBytes(insertedValues[j], list.get(j));


					}
				}
			}
			list.clear();

		} finally {
			Toggle.OFF_HEAP_ADDRESS_VALIDATOR = false;
		}

	}

	@Test
	public void whenSavingValuesThenRetrievableValidatingPerformance() {

		byte[][] insertedValues = new byte[100_000][];

		OffHeapByteArrayList list = new OffHeapByteArrayList();

		Random random = new Random();

		for (int i = 0; i < 100_000; i++) {
			System.out.println(i);
			int modifiedIndex = random.nextInt(100_000);
			byte[] newValue = new byte[random.nextInt(10)];
			for (int j = 0; j < newValue.length; j++) {
				newValue[j] = (byte) (random.nextInt(256) + Byte.MIN_VALUE);
			}
			insertedValues[modifiedIndex] = newValue;
			list.set(modifiedIndex, newValue);

			assertSameBytes(newValue, list.get(modifiedIndex));
			if (i % 100 == 0) {
				for (int j = 0; j < 100_000; j++) {
					assertSameBytes(insertedValues[j], list.get(j));


				}
			}
		}
		list.clear();

	}

	private void assertSameBytes(byte[] array1, byte[] array2) {

		if (array1 == null && array2 == null) {
			return;
		}

		if (array1.length != array2.length) {
			assertThat(array1).isEqualTo(array2);
		}
		for (int i = 0; i < array1.length; i++) {
			if (array1[i] != array2[i]) {
				assertThat(array1).isEqualTo(array2);
			}
		}
	}

}
