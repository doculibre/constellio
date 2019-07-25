package com.constellio.model.services.records.cache;

import com.constellio.model.services.records.cache.offHeapCollections.OffHeapShortList;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class OffHeapShortListAcceptanceTest extends ConstellioTest {

	@Test
	public void whenSavingValuesThenRetrievable() {

		short[] insertedLongs = new short[100_000];

		OffHeapShortList longList = new OffHeapShortList();

		Random random = new Random();

		for (int i = 0; i < 500_000; i++) {
			System.out.println(i);
			int modifiedIndex = random.nextInt(100_000);
			short newValue = (short) random.nextInt();
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


	}
}
