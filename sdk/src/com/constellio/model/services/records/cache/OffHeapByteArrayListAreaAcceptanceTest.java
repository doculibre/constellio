package com.constellio.model.services.records.cache;

import com.constellio.model.services.records.cache.offHeapCollections.OffHeapBytesArrayListArea;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class OffHeapByteArrayListAreaAcceptanceTest extends ConstellioTest {

	OffHeapBytesArrayListArea list;
	Map<Integer, byte[]> expecteKeyValues;

	@Before
	public void setUp() throws Exception {
		list = new OffHeapBytesArrayListArea(1000);

		expecteKeyValues = new HashMap<>();
		for (byte i = 0; i < 100; i++) {
			byte[] arrayOf10Bytes = new byte[]{i, i, i, i, i, i, i, i, i, i};
			int key = list.tryAdd(arrayOf10Bytes);
			expecteKeyValues.put(key, arrayOf10Bytes);
		}

		expecteKeyValues.forEach((Integer key, byte[] expectedBytes) -> {
			assertSameBytes(list.get(key), expectedBytes);
		});
	}

	@After
	public void tearDown() throws Exception {
		list.clearAndClose();
	}

	@Test
	public void givenFulledAreaThenCannotAcceptAnymoreBytes() {
		assertThat(list.tryAdd(new byte[1])).isEqualTo(-1);

	}

	@Test
	public void givenFulledAreaWhenReplacingValuesWithSmallerOnesAndMoreValuesThenCompactTheBuffer() {

		//This will free 100 separated bytes, enough for 11 new records of 9 digits
		expecteKeyValues.keySet().forEach((Integer key) -> {
			byte[] data = list.get(key);
			byte[] newValue = new byte[]{data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8]};
			expecteKeyValues.put(key, newValue);
			assertThat(list.tryUpdateOtherwiseRemove(key, newValue)).isTrue();
		});

		assertThat(list.getAvailableLength()).isEqualTo(100);

		expecteKeyValues.forEach((Integer key, byte[] expectedBytes) -> {
			assertSameBytes(list.get(key), expectedBytes);
		});

		for (byte i = 0; i < 11; i++) {
			byte[] value = new byte[]{i, (byte) (i + 1), i, i, i, i, i, i, i};
			int key = list.tryAdd(value);
			assertThat(key).isEqualTo(100 + i);
			expecteKeyValues.put(key, value);
		}
		assertThat(list.tryAdd(new byte[1])).isEqualTo(111);
		assertThat(list.tryAdd(new byte[1])).isEqualTo(-1);

		expecteKeyValues.forEach((Integer key, byte[] expectedBytes) -> {
			System.out.println(key);
			assertSameBytes(list.get(key), expectedBytes);
		});

	}

	@Test
	public void givenFulledAreaWhenRemovingValuesThenEventuallyAbleToInsertNewOnes() {
		assertThat(list.getAvailableLength()).isEqualTo(0);
		byte[] value = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};

		assertThat(list.tryAdd(value)).isEqualTo(-1);

		list.remove(1);
		expecteKeyValues.remove(1);
		assertThat(list.getAvailableLength()).isEqualTo(10);
		assertThat(list.tryAdd(value)).isEqualTo(-1);


		list.remove(1);
		assertThat(list.getAvailableLength()).isEqualTo(10);
		assertThat(list.tryAdd(value)).isEqualTo(-1);

		list.remove(2);
		expecteKeyValues.remove(2);

		assertThat(list.tryAdd(value)).isEqualTo(2);
		expecteKeyValues.put(1, value);

		assertThat(list.tryAdd(new byte[1])).isEqualTo(1);
		assertThat(list.tryAdd(new byte[1])).isEqualTo(100);
		assertThat(list.tryAdd(new byte[1])).isEqualTo(101);


	}

	@Test
	public void givenFulledAreaWhenTryUpdateOtherwiseRemoveValuesWithLargerVauesThenDeleteThenAndEventuallyAbleToInsertNewOnes() {
		assertThat(list.getAvailableLength()).isEqualTo(0);
		byte[] value = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};

		assertThat(list.tryAdd(value)).isEqualTo(-1);

		list.tryUpdateOtherwiseRemove(1, value);
		expecteKeyValues.remove(1);
		assertThat(list.getAvailableLength()).isEqualTo(10);
		assertThat(list.tryAdd(value)).isEqualTo(-1);


		list.tryUpdateOtherwiseRemove(1, value);
		assertThat(list.getAvailableLength()).isEqualTo(10);
		assertThat(list.tryAdd(value)).isEqualTo(-1);

		list.tryUpdateOtherwiseRemove(2, value);
		expecteKeyValues.remove(2);

		assertThat(list.tryAdd(value)).isEqualTo(2);
		expecteKeyValues.put(1, value);

		assertThat(list.tryAdd(new byte[1])).isEqualTo(1);
		assertThat(list.tryAdd(new byte[1])).isEqualTo(100);
		assertThat(list.tryAdd(new byte[1])).isEqualTo(101);


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
