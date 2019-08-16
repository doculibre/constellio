package com.constellio.model.services.records.cache.offHeapCollections;

import com.constellio.sdk.tests.ConstellioTest;
import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.copyAdding;
import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.copyRemoving;
import static org.assertj.core.api.Assertions.assertThat;

public class OffHeapMemoryAllocatorTest extends ConstellioTest {

	long adr;
	private List<Long> allocatedAddresses = new ArrayList<>();

//	@Before
//	public void validateNotWritingOutsideOfReservedMemory() {
//		Toggle.OFF_HEAP_ADDRESS_VALIDATOR.enable();
//	}


	@Test
	public void whenMassivelyAllocatingAndFreeingMemoryThenNoProblem() throws InterruptedException {

		final Random random = new Random();

		Runnable r = () -> {

			for (int i = 0; i < 100_000; i++) {
				int size = 200;// 1000 + random.nextInt(1000);
				long address = OffHeapMemoryAllocator.allocateMemory(size);

				byte testedNumber = (byte) (i % 100);
				for (int j = 0; j < size; j++) {
					OffHeapMemoryAllocator.putByte(address + j, testedNumber);
				}

				for (int j = 0; j < size; j++) {
					byte b = OffHeapMemoryAllocator.getByte(address + j);
					assertThat(b).isEqualTo(testedNumber);
				}

				OffHeapMemoryAllocator.freeMemory(address, size);
			}
		};

		Thread t1 = new Thread(r);
		Thread t2 = new Thread(r);
		Thread t3 = new Thread(r);
		Thread t4 = new Thread(r);

		t1.start();
		t2.start();
		t3.start();
		t4.start();

		t1.join();
		t2.join();
		t3.join();
		t4.join();


	}

	@Test
	public void testingCopyAddingMethod() {
		System.out.println("Testing copy method, A JVM crash could occur if the test failed");

		long srcAddress = newAutoClosedAllocatedAddress();
		OffHeapMemoryAllocator.putByte(srcAddress, (byte) 10);
		OffHeapMemoryAllocator.putByte(srcAddress + 1, (byte) 20);
		OffHeapMemoryAllocator.putByte(srcAddress + 2, (byte) 30);
		OffHeapMemoryAllocator.putByte(srcAddress + 3, (byte) 40);
		OffHeapMemoryAllocator.putByte(srcAddress + 4, (byte) 50);

		assertAddressContainingBytes(srcAddress, 10, 20, 30, 40, 50);

		System.out.println("test 1 ");
		copyAdding(srcAddress, adr = newAutoClosedAllocatedAddress(), 4, 0, 1);
		assertAddressContainingBytes(adr, 0, 10, 20, 30, 40);

		System.out.println("test 2 ");
		copyAdding(srcAddress, adr = newAutoClosedAllocatedAddress(), 3, 0, 2);
		assertAddressContainingBytes(adr, 0, 0, 10, 20, 30);

		System.out.println("test 3 ");
		copyAdding(srcAddress, adr = newAutoClosedAllocatedAddress(), 4, 1, 1);
		assertAddressContainingBytes(adr, 10, 0, 20, 30, 40);

		System.out.println("test 4 ");
		copyAdding(srcAddress, adr = newAutoClosedAllocatedAddress(), 3, 1, 2);
		assertAddressContainingBytes(adr, 10, 0, 0, 20, 30);

		System.out.println("test 5 ");
		copyAdding(srcAddress, adr = newAutoClosedAllocatedAddress(), 4, 4, 1);
		assertAddressContainingBytes(adr, 10, 20, 30, 40, 0);

		System.out.println("test 6 ");
		copyAdding(srcAddress, adr = newAutoClosedAllocatedAddress(), 0, 4, 1);
		assertAddressContainingBytes(adr, 0, 0, 0, 0, 0);

		System.out.println("test 7 ");
		copyAdding(srcAddress, adr = newAutoClosedAllocatedAddress(), 5, 0, 0);
		assertAddressContainingBytes(adr, 10, 20, 30, 40, 50);

		System.out.println("test 8 ");
		copyAdding(srcAddress, adr = newAutoClosedAllocatedAddress(), 5, 0, 0);
		copyAdding(adr, 3, 0, 2);
		assertAddressContainingBytes(adr, 0, 0, 10, 20, 30);

		System.out.println("test 9 ");
		copyAdding(srcAddress, adr = newAutoClosedAllocatedAddress(), 5, 0, 0);
		copyAdding(adr, 5, 0, 0);
		assertAddressContainingBytes(adr, 10, 20, 30, 40, 50);

		System.out.println("test 10 ");
		copyAdding(srcAddress, adr = newAutoClosedAllocatedAddress(), 5, 0, 0);
		copyAdding(adr, 4, 2, 1);
		assertAddressContainingBytes(adr, 10, 20, 0, 30, 40);

		System.out.println("test 11 ");
		copyAdding(srcAddress, adr = newAutoClosedAllocatedAddress(), 5, 0, 0);
		copyAdding(adr, 4, 4, 1);
		assertAddressContainingBytes(adr, 10, 20, 30, 40, 0);


		System.out.println("Testing copy method, finished, JVM survived!");
	}


	@Test
	public void testingCopyRemovingMethod() {
		System.out.println("Testing copy method, A JVM crash could occur if the test failed");

		long srcAddress = newAutoClosedAllocatedAddress();
		OffHeapMemoryAllocator.putByte(srcAddress, (byte) 10);
		OffHeapMemoryAllocator.putByte(srcAddress + 1, (byte) 20);
		OffHeapMemoryAllocator.putByte(srcAddress + 2, (byte) 30);
		OffHeapMemoryAllocator.putByte(srcAddress + 3, (byte) 40);
		OffHeapMemoryAllocator.putByte(srcAddress + 4, (byte) 50);

		assertAddressContainingBytes(srcAddress, 10, 20, 30, 40, 50);

		System.out.println("test 1 ");
		copyRemoving(srcAddress, adr = newAutoClosedAllocatedAddress(), 5, 0, 1);
		assertAddressContainingBytes(adr, 20, 30, 40, 50, 0);

		System.out.println("test 2 ");
		copyRemoving(srcAddress, adr = newAutoClosedAllocatedAddress(), 5, 0, 2);
		assertAddressContainingBytes(adr, 30, 40, 50, 0, 0);

		System.out.println("test 3 ");
		copyRemoving(srcAddress, adr = newAutoClosedAllocatedAddress(), 5, 1, 1);
		assertAddressContainingBytes(adr, 10, 30, 40, 50, 0);

		System.out.println("test 4 ");
		copyRemoving(srcAddress, adr = newAutoClosedAllocatedAddress(), 5, 1, 2);
		assertAddressContainingBytes(adr, 10, 40, 50, 0, 0);

		System.out.println("test 5 ");
		copyRemoving(srcAddress, adr = newAutoClosedAllocatedAddress(), 5, 4, 1);
		assertAddressContainingBytes(adr, 10, 20, 30, 40, 0);

		System.out.println("test 6 ");
		copyRemoving(srcAddress, adr = newAutoClosedAllocatedAddress(), 0, 4, 1);
		assertAddressContainingBytes(adr, 0, 0, 0, 0, 0);

		System.out.println("test 7 ");
		copyRemoving(srcAddress, adr = newAutoClosedAllocatedAddress(), 5, 0, 0);
		assertAddressContainingBytes(adr, 10, 20, 30, 40, 50);

		System.out.println("test 8 ");
		copyAdding(srcAddress, adr = newAutoClosedAllocatedAddress(), 5, 0, 0);
		copyRemoving(adr, 5, 0, 2);
		assertAddressContainingBytes(adr, 30, 40, 50, 0, 0);

		System.out.println("test 9 ");
		copyAdding(srcAddress, adr = newAutoClosedAllocatedAddress(), 5, 0, 0);
		copyRemoving(adr, 5, 0, 0);
		assertAddressContainingBytes(adr, 10, 20, 30, 40, 50);

		System.out.println("test 10 ");
		copyAdding(srcAddress, adr = newAutoClosedAllocatedAddress(), 5, 0, 0);
		copyRemoving(adr, 4, 2, 1);
		assertAddressContainingBytes(adr, 10, 20, 40, 0, 50);

		System.out.println("test 11 ");
		copyAdding(srcAddress, adr = newAutoClosedAllocatedAddress(), 5, 0, 0);
		copyRemoving(adr, 5, 2, 1);
		assertAddressContainingBytes(adr, 10, 20, 40, 50, 0);


		System.out.println("Testing copy method, finished, JVM survived!");
	}

	private long newAutoClosedAllocatedAddress() {
		long address = OffHeapMemoryAllocator.allocateMemory(5);
		allocatedAddresses.add(address);
		return address;

	}

	@After
	public void tearDown() throws Exception {
		for (long allocatedAddress : allocatedAddresses) {
			OffHeapMemoryAllocator.freeMemory(allocatedAddress, 5);
		}
	}

	private void assertAddressContainingBytes(long address, int... intValuesOfBytes) {
		for (int i = 0; i < intValuesOfBytes.length; i++) {
			assertThat(OffHeapMemoryAllocator.getByte(address + i)).isEqualTo((byte) intValuesOfBytes[i]);
		}
	}
}
