package com.constellio.model.services.records.cache.offHeapCollections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class OffHeapByteArrayList2 {

	private static final Logger LOGGER = LoggerFactory.getLogger(OffHeapByteArrayList2.class);

	private OffHeapByteList itemsAreaId = new OffHeapByteList();
	private OffHeapIntList itemsAreaKey = new OffHeapIntList();

	private List<OffHeapBytesArrayListArea> areas = new ArrayList<>();

	public int getHeapConsumption() {
		int heapConsumption = itemsAreaId.getHeapConsumption() + itemsAreaKey.getHeapConsumption();
		for (OffHeapBytesArrayListArea area : areas) {
			heapConsumption += area.getHeapConsumption();
		}

		return heapConsumption;
	}

	public int getOffHeapConsumption() {
		int offHeapConsumption = itemsAreaId.getOffHeapConsumption() + itemsAreaKey.getOffHeapConsumption();
		for (OffHeapBytesArrayListArea area : areas) {
			offHeapConsumption += area.getOffHeapConsumption();
		}

		return offHeapConsumption;
	}

	public void set(int index, byte[] value) {

		if (value == null) {
			if (this.itemsAreaId.size() > index) {
				byte areaId = this.itemsAreaId.get(index);
				int areaKey = this.itemsAreaKey.get(index);

				if (areaId != 0) {
					this.areas.get((int) areaId - 1).remove(areaKey);
					this.itemsAreaId.set(index, (byte) 0);
					this.itemsAreaKey.set(index, -1);

				}
			}

		} else {
			boolean inserted = false;
			if (this.itemsAreaId.size() > index) {
				byte areaId = this.itemsAreaId.get(index);
				int areaKey = this.itemsAreaKey.get(index);

				if (areaId != 0) {
					inserted = this.areas.get((int) areaId - 1).tryUpdateOtherwiseRemove(areaKey, value);
				}
			}

			for (int i = 0; !inserted && i < areas.size(); i++) {
				OffHeapBytesArrayListArea area = areas.get(i);
				int key = area.tryAdd(value);
				if (key != -1) {
					inserted = true;
					this.itemsAreaId.set(index, (byte) (i + 1));
					this.itemsAreaKey.set(index, key);
				}
			}

			if (!inserted) {
				OffHeapBytesArrayListArea area = newArea();
				areas.add(area);
				int key = area.tryAdd(value);
				this.itemsAreaId.set(index, (byte) (areas.size()));
				this.itemsAreaKey.set(index, key);
			}
		}

	}

	/**
	 * This is the mapping strategy!
	 *
	 * First area are small for instances with small amount of data and become larger
	 *
	 * The required free space ratio is defined to 0.10, which means the cache may have up to 10% of unused space
	 * A lesser ratio would reduce unused space, while increasing compaction frequency and
	 *
	 * @return
	 */
	private OffHeapBytesArrayListArea newArea() {

		int length;
		if (areas.size() == 0) {
			length = 5 * 1024 * 1024;

		} else if (areas.size() < 5) {
			length = 25 * 1024 * 1024;

		} else if (areas.size() < 10) {
			length = 100 * 1024 * 1024;

		} else {
			length = 250 * 1024 * 1024;

		}

		return new OffHeapBytesArrayListArea(length, 0.10);
	}

	public byte[] get(int index) {
		if (this.itemsAreaId.size() > index) {
			byte areaId = this.itemsAreaId.get(index);
			int areaKey = this.itemsAreaKey.get(index);

			if (areaId != 0) {
				return this.areas.get((int) areaId - 1).get(areaKey);
			}

		}

		return null;
	}

	/**
	 * This algo could be optimized to copy large range of bytes instead of value by value
	 *
	 * @param index
	 * @param value
	 */
	public void insertValueShiftingAllFollowingValues(int index, byte[] value) {
		LOGGER.warn("insertValueShiftingAllFollowingValues : this should not happen and could consume a lot of memory");
		itemsAreaId.insertValueShiftingAllFollowingValues(index, (byte) 0);
		itemsAreaKey.insertValueShiftingAllFollowingValues(index, 0);
		set(index, value);
		LOGGER.warn("insertValueShiftingAllFollowingValues : finished");
	}

	public void clear() {
		itemsAreaId.clear();
		itemsAreaKey.clear();

		for (OffHeapBytesArrayListArea offHeapBytesArrayListArea : this.areas) {
			offHeapBytesArrayListArea.clearAndClose();
		}
		this.areas.clear();
	}


	public int size() {
		return itemsAreaId.size();
	}


}
