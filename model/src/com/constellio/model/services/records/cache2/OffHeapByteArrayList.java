package com.constellio.model.services.records.cache2;

class OffHeapByteArrayList {
	private OffHeapShortList byteArraySizes = new OffHeapShortList();
	private OffHeapLongList byteArrayMemoryAdresses = new OffHeapLongList();

	public OffHeapByteArrayList() {
	}


	public void set(int index, byte[] value) {
		long previousAddress = byteArrayMemoryAdresses.get(index);
		long previousLength = byteArraySizes.get(index);

		try {
			if (value != null) {
				synchronized (this) {
					long address = OffHeapMemoryAllocator.allocateMemory(value.length);
					if (address == 0) {
						address = -1;
					}


					byteArrayMemoryAdresses.set(index, address);
					byteArraySizes.set(index, (short) value.length);
					if (address != -1) {
						for (int i = 0; i < value.length; i++) {
							OffHeapMemoryAllocator.putByte(address + i, value[i]);
						}
					}
				}
			} else {
				byteArrayMemoryAdresses.set(index, 0);
				byteArraySizes.set(index, (short) -1);
			}

		} finally {
			if (previousAddress != 0 && previousAddress != -1) {
				synchronized (this) {
					OffHeapMemoryAllocator.freeMemory(previousAddress, previousLength);
				}
			}
		}

	}

	public byte[] getArray(int index) {
		OffHeapBytesSupplier supplier = get(index);
		return supplier == null ? null : supplier.toArray();
	}

	/**
	 * This algo could be optimized to copy large range of bytes instead of value by value
	 *
	 * @param index
	 * @param value
	 */
	public void insertValueShiftingAllFollowingValues(int index, byte[] value) {
		byteArraySizes.insertValueShiftingAllFollowingValues(index, (short) 0);
		byteArrayMemoryAdresses.insertValueShiftingAllFollowingValues(index, 0L);
		set(index, value);
	}

	public OffHeapBytesSupplier get(int index) {

		long address = byteArrayMemoryAdresses.get(index);
		short size = byteArraySizes.get(index);

		if (address == 0) {
			return null;
		}

		return new OffHeapBytesSupplier() {

			@Override
			public byte getByte(int index) {
				return OffHeapMemoryAllocator.getByte(address + index);
			}

			@Override
			public short getShort(int index) {
				return OffHeapMemoryAllocator.getShort(address + index);
			}

			@Override
			public int getInt(int index) {
				return OffHeapMemoryAllocator.getInt(address + index);
			}

			@Override
			public byte[] toArray() {
				byte[] array = new byte[size];

				for (int i = 0; i < size; i++) {
					array[i] = getByte(i);
				}

				return array;
			}

			@Override
			public short length() {
				return size;
			}
		};


	}

	public int size() {
		return byteArrayMemoryAdresses.size();
	}

}
