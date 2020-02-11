package com.constellio.model.services.records.cache.offHeapCollections;

import com.constellio.sdk.tests.ConstellioTest;
import org.junit.After;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class OffHeapByteArrayListAreaAcceptanceTest extends ConstellioTest {

	OffHeapBytesArrayListArea list;
	Map<Integer, byte[]> expecteKeyValues;

	public void setupWithRequiredFreeSpaceRatioForCompacting(double ratio) {
		list = spy(new OffHeapBytesArrayListArea(1000, ratio));

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
		setupWithRequiredFreeSpaceRatioForCompacting(0.001);
		assertThat(list.tryAdd(new byte[1])).isEqualTo(-1);

	}

	@Test
	public void givenFulledAreaWhenReplacingValuesWithSmallerOnesAndMoreValuesThenCompactTheBuffer() {
		setupWithRequiredFreeSpaceRatioForCompacting(0.001);
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
		verify(list, never()).compacting();

		for (byte i = 0; i < 11; i++) {
			byte[] value = new byte[]{i, (byte) (i + 1), i, i, i, i, i, i, i};
			int key = list.tryAdd(value);
			assertThat(key).isEqualTo(100 + i);
			expecteKeyValues.put(key, value);
		}
		verify(list).compacting();

		assertThat(list.tryAdd(new byte[1])).isEqualTo(111);
		assertThat(list.tryAdd(new byte[1])).isEqualTo(-1);

		expecteKeyValues.forEach((Integer key, byte[] expectedBytes) -> {
			System.out.println(key);
			assertSameBytes(list.get(key), expectedBytes);
		});

	}

	@Test
	public void givenFulledAreaWhenRemovingValuesThenEventuallyAbleToInsertNewOnes() {
		setupWithRequiredFreeSpaceRatioForCompacting(0.001);
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
		verify(list, never()).compacting();

		list.remove(2);
		expecteKeyValues.remove(2);

		assertThat(list.tryAdd(value)).isEqualTo(2);
		expecteKeyValues.put(1, value);
		verify(list).compacting();

		assertThat(list.tryAdd(new byte[1])).isEqualTo(1);
		assertThat(list.tryAdd(new byte[1])).isEqualTo(100);
		assertThat(list.tryAdd(new byte[1])).isEqualTo(101);


	}

	@Test
	public void givenFulledAreaRequiring5PercentFreeSpaceWhenRemovingValuesThenEventuallyAbleToInsertNewOnes() {

		setupWithRequiredFreeSpaceRatioForCompacting(0.05);
		assertThat(list.getAvailableLength()).isEqualTo(0);
		byte[] value = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

		assertThat(list.tryAdd(value)).isEqualTo(-1);

		list.remove(3);
		expecteKeyValues.remove(3);
		assertThat(list.getAvailableLength()).isEqualTo(10);
		assertThat(list.tryAdd(value)).isEqualTo(-1);

		list.remove(5);
		expecteKeyValues.remove(5);
		assertThat(list.tryAdd(value)).isEqualTo(-1);
		verify(list, never()).compacting();

		list.remove(7);
		expecteKeyValues.remove(7);
		assertThat(list.tryAdd(value)).isEqualTo(-1);
		verify(list, never()).compacting();

		list.remove(21);
		expecteKeyValues.remove(21);
		assertThat(list.tryAdd(value)).isEqualTo(-1);
		verify(list, never()).compacting();

		list.remove(23);
		expecteKeyValues.remove(23);

		assertThat(list.tryAdd(value)).isEqualTo(23);
		expecteKeyValues.put(23, value);
		verify(list).compacting();

		assertThat(list.tryAdd(value)).isEqualTo(21);
		expecteKeyValues.put(21, value);

		assertThat(list.tryAdd(value)).isEqualTo(7);
		expecteKeyValues.put(7, value);

		assertThat(list.tryAdd(value)).isEqualTo(5);
		expecteKeyValues.put(5, value);

		assertThat(list.tryAdd(value)).isEqualTo(3);
		expecteKeyValues.put(3, value);


		expecteKeyValues.forEach((Integer key, byte[] expectedBytes) -> {
			System.out.println(key);
			assertSameBytes(list.get(key), expectedBytes);
		});


	}

	@Test
	public void givenFulledAreaWhenTryUpdateOtherwiseRemoveValuesWithLargerVauesThenDeleteThenAndEventuallyAbleToInsertNewOnes() {
		setupWithRequiredFreeSpaceRatioForCompacting(0.001);
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
		verify(list, never()).compacting();

		list.tryUpdateOtherwiseRemove(2, value);
		expecteKeyValues.remove(2);

		assertThat(list.tryAdd(value)).isEqualTo(2);
		expecteKeyValues.put(1, value);
		verify(list).compacting();

		assertThat(list.tryAdd(new byte[1])).isEqualTo(1);
		assertThat(list.tryAdd(new byte[1])).isEqualTo(100);
		assertThat(list.tryAdd(new byte[1])).isEqualTo(101);


	}


	@Test
	public void whenRemovingLastItemAndInsertingANewOneThenNoCompaction() {
		setupWithRequiredFreeSpaceRatioForCompacting(0.001);
		assertThat(list.getAvailableLength()).isEqualTo(0);
		byte[] value = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

		assertThat(list.tryAdd(value)).isEqualTo(-1);

		list.remove(99);
		expecteKeyValues.remove(99);
		assertThat(list.getAvailableLength()).isEqualTo(10);


		assertThat(list.tryAdd(value)).isEqualTo(99);
		expecteKeyValues.put(99, value);

		verify(list, never()).compacting();

		expecteKeyValues.forEach((Integer key, byte[] expectedBytes) -> {
			assertSameBytes(list.get(key), expectedBytes);
		});
	}

	@Test
	public void whenRemovingBecauseCannotUpdateLastItemAndInsertingANewOneThenNoCompaction() {
		setupWithRequiredFreeSpaceRatioForCompacting(0.001);
		assertThat(list.getAvailableLength()).isEqualTo(0);
		byte[] value = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
		byte[] largerValue = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};

		assertThat(list.tryAdd(value)).isEqualTo(-1);

		list.tryUpdateOtherwiseRemove(99, largerValue);
		expecteKeyValues.remove(99);
		assertThat(list.getAvailableLength()).isEqualTo(10);


		assertThat(list.tryAdd(value)).isEqualTo(99);
		expecteKeyValues.put(99, value);

		verify(list, never()).compacting();

		expecteKeyValues.forEach((Integer key, byte[] expectedBytes) -> {
			assertSameBytes(list.get(key), expectedBytes);
		});
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
