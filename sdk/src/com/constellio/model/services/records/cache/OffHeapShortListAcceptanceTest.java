package com.constellio.model.services.records.cache;

import com.constellio.model.services.records.cache.offHeapCollections.OffHeapShortList;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class OffHeapShortListAcceptanceTest extends ConstellioTest {

//	@Before
//	public void validateNotWritingOutsideOfReservedMemory() {
//		Toggle.OFF_HEAP_ADDRESS_VALIDATOR.enable();
//	}

	@Test
	public void whenSavingValuesThenRetrievable() {

		short[] insertedLongs = new short[100_000];

		OffHeapShortList shortList = new OffHeapShortList();

		Random random = new Random();

		for (int i = 0; i < 100_000; i++) {
			System.out.println(i);
			int modifiedIndex = random.nextInt(100_000);
			short newValue = (short) random.nextInt();
			insertedLongs[modifiedIndex] = newValue;
			shortList.set(modifiedIndex, newValue);

			assertThat(shortList.get(modifiedIndex)).isEqualTo(newValue);
			if (i % 100 == 0) {
				for (int j = 0; j < 100_000; j++) {
					if (shortList.get(j) != insertedLongs[j]) {
						assertThat(shortList.get(j)).isEqualTo(insertedLongs[j]);
					}


				}
			}
		}

		shortList.clear();


	}
}
