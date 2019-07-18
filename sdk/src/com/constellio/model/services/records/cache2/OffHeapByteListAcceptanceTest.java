package com.constellio.model.services.records.cache2;

import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class OffHeapByteListAcceptanceTest extends ConstellioTest {

	@Test
	public void whenSavingValuesThenRetrievable() {

		byte[] insertedBytes = new byte[100_000];

		OffHeapByteList byteList = new OffHeapByteList();

		Random random = new Random();

		for (int i = 0; i < 500_000; i++) {
			System.out.println(i);
			int modifiedIndex = random.nextInt(100_000);
			byte newValue = (byte) (random.nextInt(256) + Byte.MIN_VALUE);
			insertedBytes[modifiedIndex] = newValue;
			byteList.set(modifiedIndex, newValue);

			assertThat(byteList.get(modifiedIndex)).isEqualTo(newValue);
			if (i % 100 == 0) {
				for (int j = 0; j < 100_000; j++) {
					if (byteList.get(j) != insertedBytes[j]) {
						assertThat(byteList.get(j)).isEqualTo(insertedBytes[j]);
					}
				}
			}
		}


	}
}
