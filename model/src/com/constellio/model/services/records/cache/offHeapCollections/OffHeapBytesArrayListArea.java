package com.constellio.model.services.records.cache.offHeapCollections;

import com.constellio.data.utils.ImpossibleRuntimeException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.getByte;
import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.putByte;

/**
 * Working with a large allocated bytes array of the specified length to add and remove smaller byte arrays, called items
 * <p>
 * A key is given in response of adding an item, that must be kept for futur access to the item.
 * The key IS NOT the actual data index in the byte array, but the position in the "keys" collection where is stored
 * the actual data index. A key returned
 * by the add method will never change until the item is removed. The key may be reused for a later insertion.
 * <p>
 * This class may automatically reorganize the byte array if total available length is sufficient,
 * but not the total available contiguous length at the end of the array.
 * This event doesn't affect the keys and requires no action. No new memory is allocated during the process, except for
 * a list of SortedKey stored in heap during the process
 * <p>
 * This collection MUST NOT be used the at the same time for writing and for writing and reading, but can be used at the same time for reading.
 * Locking must be implemented by the user outside of the class
 */
public class OffHeapBytesArrayListArea {

	private static final Logger LOGGER = LoggerFactory.getLogger(OffHeapBytesArrayListArea.class);
	private static long compactionCount = 0;

	private int reusableKeysCount;
	private OffHeapIntList reusableKeys;

	private OffHeapIntList keysIndex;

	private OffHeapIntList keysLength;

	private int length;

	private long address;

	private int nextIndex;

	private int availableLength;

	//TODO add a parameter to skip compaction on the beginning of the buffer if it is already compacted
	//TODO when compacting, tryadding the item as soon as possible inside the byte array

	public OffHeapBytesArrayListArea(int length) {
		this.length = length;
		this.availableLength = length;
		this.nextIndex = 0;
		this.address = OffHeapMemoryAllocator.allocateMemory(length, OffHeapMemoryAllocator.OffHeapByteArrayListArea_ID);
		this.keysIndex = new OffHeapIntList();
		this.keysLength = new OffHeapIntList();
		this.reusableKeys = new OffHeapIntList();

	}

	/**
	 * Try adding the byte array, returning the index or -1 if impossible
	 */
	public int tryAdd(byte[] item) {
		if (availableLength < item.length) {
			return -1;
		}

		return add(item);
	}

	public byte[] get(int key) {
		int index = keysIndex.get(key);
		int length = keysLength.get(key);
		if (index == -1) {
			throw new ImpossibleRuntimeException("No such item with key '" + key + "'");
		}
		byte[] bytes = new byte[length];
		for (int i = 0; i < length; i++) {
			bytes[i] = OffHeapMemoryAllocator.getByte(address + index + i);
		}
		return bytes;
	}

	public int getAvailableLength() {
		return availableLength;
	}

	public boolean remove(int key) {
		int index = keysIndex.get(key);
		if (index == -1) {
			return false;
		}
		int length = keysLength.get(key);
		if (nextIndex == index + length) {
			//We are removing the last item, adding more space to the end of the buffer
			nextIndex = index;
		}
		availableLength += length;
		keysIndex.set(key, -1);
		keysLength.set(key, 0);
		reusableKeys.set(reusableKeysCount, key);
		reusableKeysCount++;
		return true;
	}

	/**
	 * Return true if the existing byte array was replaced by this one, false if impossible and removed
	 */
	public boolean tryUpdateOtherwiseRemove(int key, byte[] item) {
		int index = keysIndex.get(key);
		int length = keysLength.get(key);

		if (index == -1) {
			return false;
		}

		if (item.length <= length) {
			keysLength.set(key, item.length);
			for (int i = 0; i < item.length; i++) {
				//		System.out.println("Replacing a value of '" + length + "' bytes by a value of '" + item.length + "' bytes");
				OffHeapMemoryAllocator.putByte(address + index + i, item[i]);
			}
			availableLength += (length - item.length);
			return true;

		} else {
			//	System.out.println("Removing a value of '" + length + "' bytes to add a value of '" + item.length + "' bytes");
			remove(key);
			return false;
		}

	}

	private int add(byte[] item) {
		if (length - nextIndex < item.length) {
			compacting();
		}
		if (length - nextIndex < item.length) {
			throw new ImpossibleRuntimeException("Item cannot fit in this area");
		}

		int key = findAvailableKey();
		for (int i = 0; i < item.length; i++) {
			OffHeapMemoryAllocator.putByte(address + nextIndex + i, item[i]);
		}
		keysIndex.set(key, nextIndex);
		keysLength.set(key, item.length);
		nextIndex += item.length;
		availableLength -= item.length;
		return key;

	}

	private int findAvailableKey() {
		if (reusableKeysCount > 0) {
			int key = reusableKeys.get(reusableKeysCount - 1);
			reusableKeysCount--;
			return key;
		}

		return keysIndex.size();
	}

	private void compacting() {
		compactionCount++;
		List<SortedKey> keysSortedByIndex = getKeysSortedByIndex();

		int index = 0;
		for (SortedKey key : keysSortedByIndex) {
			int length = keysLength.get(key.key);
			if (key.index != index) {
				moveValueNearerToTheStart(key.key, key.index, index, length);

			}
			index += length;
		}
		this.nextIndex = index;
	}

	private void moveValueNearerToTheStart(int key, int from, int to, int length) {

		for (int i = 0; i < length; i++) {
			putByte(address + to + i, getByte(address + from + i));
		}
		keysIndex.set(key, to);
	}

	private List<SortedKey> getKeysSortedByIndex() {
		List<SortedKey> keysSortedByIndex = new ArrayList<>();

		Stream<Integer> indexStreams = keysIndex.stream();
		final AtomicInteger key = new AtomicInteger();
		indexStreams.forEach((index) -> {
			if (index != -1) {
				keysSortedByIndex.add(new SortedKey(key.get(), index));
			}
			key.incrementAndGet();
		});

		keysSortedByIndex.sort(Comparator.comparing(SortedKey::getIndex));
		return keysSortedByIndex;
	}

	@AllArgsConstructor
	private static class SortedKey {
		int key;
		@Getter
		int index;
	}

	/**
	 * Won't be reusable
	 */
	public void clearAndClose() {
		keysLength.clear();
		keysIndex.clear();
		OffHeapMemoryAllocator.freeMemory(address, length, OffHeapMemoryAllocator.OffHeapByteArrayListArea_ID);
	}


	public int getHeapConsumption() {
		return keysIndex.getHeapConsumption() + keysLength.getHeapConsumption() + reusableKeys.getHeapConsumption() + Integer.BYTES + Integer.BYTES + Long.BYTES + Integer.BYTES + Integer.BYTES;
	}

	public long getOffHeapConsumption() {
		return keysIndex.getOffHeapConsumption() + keysLength.getOffHeapConsumption() + reusableKeys.getOffHeapConsumption() + length;
	}

	public static long getCompactionCount() {
		return compactionCount;
	}
}
