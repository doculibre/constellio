package com.constellio.model.services.records.cache.locks;

import com.constellio.model.entities.schemas.MetadataSchemaType;

import java.util.concurrent.atomic.AtomicInteger;

public class SimpleReadLockMechanism {


	private AtomicInteger canRead1 = new AtomicInteger();
	private AtomicInteger canRead2 = new AtomicInteger();
	private AtomicInteger readingThreads = new AtomicInteger();

	public void obtainSchemaTypeReadingPermit(MetadataSchemaType schemaType) {
		obtainSchemaTypeReadingPermit(schemaType.getCollectionInfo().getCollectionId(), schemaType.getId());
	}

	public void obtainSchemaTypeReadingPermit(byte collectionId, short schemaTypeId) {
		obtainReadingPermit();
	}

	public void releaseSchemaTypeReadingPermit(MetadataSchemaType schemaType) {
		releaseSchemaTypeReadingPermit(schemaType.getCollectionInfo().getCollectionId(), schemaType.getId());
	}

	public void releaseSchemaTypeReadingPermit(byte collectionId, short schemaTypeId) {
		finishedReading();
	}

	public void obtainCollectionReadingPermit(byte collectionId) {
		obtainReadingPermit();
	}

	public void releaseCollectionReadingPermit(byte collectionId) {
		finishedReading();
	}

	public void obtainSystemWideReadingPermit() {
		obtainReadingPermit();
	}

	public void obtainSystemWideWritingPermit() {
		obtainWritingPermit();
	}

	public void releaseSystemWideReadingPermit() {
		finishedReading();
	}


	public void releaseSystemWideWritingPermit() {
		finishedWriting();
	}

	public void obtainSchemaTypeWritingPermit(MetadataSchemaType schemaType) {
		obtainSchemaTypeWritingPermit(schemaType.getCollectionInfo().getCollectionId(), schemaType.getId());
	}

	public void obtainSchemaTypeWritingPermit(byte collectionId, short schemaTypeId) {
		obtainWritingPermit();
	}

	public void releaseSchemaTypeWritingPermit(MetadataSchemaType schemaType) {
		releaseSchemaTypeWritingPermit(schemaType.getCollectionInfo().getCollectionId(), schemaType.getId());
	}

	public void releaseSchemaTypeWritingPermit(byte collectionId, short schemaTypeId) {
		finishedWriting();
	}

	public void obtainCollectionWritingPermit(byte collectionId) {
		obtainWritingPermit();
	}

	public void releaseCollectionWritingPermit(byte collectionId) {
		finishedWriting();
	}

	private void obtainReadingPermit() {
		boolean obtained = false;

		while (!obtained) {
			if (canRead1.get() == 0) {
				readingThreads.incrementAndGet();
				if (canRead2.get() == 0) {
					obtained = true;
				} else {
					readingThreads.decrementAndGet();
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			} else {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}


		}
	}

	private void finishedReading() {
		readingThreads.decrementAndGet();
	}

	private void obtainWritingPermit() {
		canRead1.incrementAndGet();
		canRead2.incrementAndGet();

		while (readingThreads.get() != 0) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

	}

	private void finishedWriting() {
		canRead2.decrementAndGet();
		canRead1.decrementAndGet();

	}

}
