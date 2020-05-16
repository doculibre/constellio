package com.constellio.model.services.records.cache;

import com.constellio.data.utils.ThreadList;
import com.constellio.model.services.records.cache.locks.TriLevelLockMechanism;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.SlowTest;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class TriLevelLockMechanismTest extends ConstellioTest {

	AtomicInteger errorCount = new AtomicInteger();
	AtomicLong readingOperations = new AtomicLong();
	AtomicLong writingOperations = new AtomicLong();
	boolean running = true;

	TriLevelLockMechanism mechanism;

	@Before
	public void setUp() throws Exception {
		mechanism = new TriLevelLockMechanism();
		//doThrow(InterruptedException.class).when(mechanism).sleep1Ms();
	}

	@Test
	// Confirm @SlowTest
	public void givenSystemWideWriting() throws InterruptedException {
		for (int i = 0; i < 5; i++) {
			doTest();
		}
		System.out.println("Reading operations : " + readingOperations.get());
		System.out.println("Writing operations : " + writingOperations.get());
	}

	private void doTest() throws InterruptedException {
		running = true;
		AtomicInteger threadId = new AtomicInteger();
		ThreadList<Thread> readers = new ThreadList();
		IntStream.range(0, 60).forEach((i) -> readers.add(new Thread(this::readingASchemaType, "Reader " + threadId.incrementAndGet())));
		IntStream.range(0, 20).forEach((i) -> readers.add(new Thread(this::readingACollectionRecord, "Reader " + threadId.incrementAndGet())));
		IntStream.range(0, 20).forEach((i) -> readers.add(new Thread(this::readingASystemRecord, "Reader " + threadId.incrementAndGet())));

		ThreadList<Thread> writers = new ThreadList();
		IntStream.range(0, 10).forEach((i) -> writers.add(new Thread(this::writing, "Writer " + threadId.incrementAndGet())));

		readers.startAll();
		writers.startAll();

		Thread.sleep(2_000);
		running = false;

		readers.joinAll();
		writers.joinAll();

		assertThat(errorCount.get()).isEqualTo(0);
	}

	private AtomicInteger writers = new AtomicInteger();
	private AtomicInteger readers = new AtomicInteger();

	private void readingASchemaType() {
		Random random = new Random();
		while (running) {
			byte collectionId = (byte) random.nextInt(2);
			byte schemaTypeId = (byte) random.nextInt(2);
			mechanism.obtainSystemWideReadingPermit();
			if (writers.get() > 0) {
				System.out.println("writer at the same moment");
				errorCount.incrementAndGet();
			}
			readers.incrementAndGet();
			readingOperations.incrementAndGet();
			readers.decrementAndGet();
			mechanism.releaseSystemWideReadingPermit();

		}
	}


	private void readingACollectionRecord() {
		Random random = new Random();
		while (running) {
			byte collectionId = (byte) random.nextInt(2);
			mechanism.obtainCollectionReadingPermit(collectionId);
			if (writers.get() > 0) {
				System.out.println("writer at the same moment");
				errorCount.incrementAndGet();
			}
			readers.incrementAndGet();
			readingOperations.incrementAndGet();
			readers.decrementAndGet();
			mechanism.releaseCollectionReadingPermit(collectionId);

		}
	}


	private void readingASystemRecord() {
		Random random = new Random();
		while (running) {
			byte collectionId = (byte) random.nextInt(2);
			byte schemaTypeId = (byte) random.nextInt(2);
			mechanism.obtainSystemWideReadingPermit();
			if (writers.get() > 0) {
				System.out.println("writer at the same moment");
				errorCount.incrementAndGet();
			}
			readers.incrementAndGet();
			readingOperations.incrementAndGet();
			readers.decrementAndGet();
			mechanism.releaseSystemWideReadingPermit();

		}
	}


	private void writing() {
		Random random = new Random();
		while (running) {
			byte collectionId = (byte) random.nextInt(2);
			byte schemaTypeId = (byte) random.nextInt(2);
			mechanism.obtainSchemaTypeWritingPermit(collectionId, schemaTypeId);
			//			if (currentlyWriting) {
			//				System.out.println("2 writers at the same moment");
			//				errorCount.incrementAndGet();
			//			}
			writers.incrementAndGet();
			if (readers.get() > 0) {
				System.out.println("reader at the same moment of writing");
				errorCount.incrementAndGet();
			}

			writingOperations.incrementAndGet();
			writers.decrementAndGet();
			mechanism.releaseSchemaTypeWritingPermit(collectionId, schemaTypeId);
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}


}
