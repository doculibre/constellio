package com.constellio.model.services.records.cache;

import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.services.records.cache.offHeapCollections.OffHeapLongList;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class OffHeapLongListAcceptanceTest extends ConstellioTest {

	@Before
	public void validateNotWritingOutsideOfReservedMemory() {
		Toggle.OFF_HEAP_ADDRESS_VALIDATOR.enable();
	}

	@Test
	public void whenSavingValuesThenRetrievable() {

		long[] insertedLongs = new long[100_000];

		OffHeapLongList longList = new OffHeapLongList();

		Random random = new Random();

		for (int i = 0; i < 100_000; i++) {
			System.out.println(i);
			int modifiedIndex = random.nextInt(100_000);
			long newValue = random.nextLong();
			insertedLongs[modifiedIndex] = newValue;
			longList.set(modifiedIndex, newValue);

			assertThat(longList.get(modifiedIndex)).isEqualTo(newValue);
			if (i % 100 == 0) {
				for (int j = 0; j < 100_000; j++) {
					if (longList.get(j) != insertedLongs[j]) {
						assertThat(longList.get(j)).isEqualTo(insertedLongs[j]);
					}


				}
			}
		}

		longList.clear();

	}
}
