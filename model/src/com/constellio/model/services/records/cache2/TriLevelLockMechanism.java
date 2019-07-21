package com.constellio.model.services.records.cache2;

import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.concurrent.atomic.AtomicInteger;

public class TriLevelLockMechanism {

	private RWLock systemWideLock = new RWLock();
	private RWLock[] collectionLocks = new RWLock[256];
	private RWLock[][] schemaTypeLocks = new RWLock[256][];

	//TODO Francis : Improve with three-level locking mecanism (level 1 for system, level 2 for collection, level 3 for type)
	//A get or a stream on an unlocked type should be allowed - but must handle the case where a full reassignIsRequired


	public void obtainSchemaTypeReadingPermit(byte collectionId, short schemaTypeId) {
		systemWideLock.obtainReadingPermit();
		collectionLock(collectionId).obtainReadingPermit();
		schemaType(collectionId, schemaTypeId).obtainReadingPermit();
	}

	public void releaseSchemaTypeReadingPermit(byte collectionId, short schemaTypeId) {
		schemaType(collectionId, schemaTypeId).finishedReading();
		collectionLock(collectionId).finishedReading();
		systemWideLock.finishedReading();
	}

	public void obtainCollectionReadingPermit(byte collectionId) {
		systemWideLock.obtainReadingPermit();
		collectionLock(collectionId).obtainReadingPermit();
	}

	public void releaseCollectionReadingPermit(byte collectionId) {
		collectionLock(collectionId).finishedReading();
		systemWideLock.finishedReading();
	}

	public void obtainSystemWideReadingPermit() {
		systemWideLock.obtainReadingPermit();
	}

	public void releaseSystemWideReadingPermit() {
		systemWideLock.finishedReading();
	}

	public void obtainSchemaTypeWritingPermit(byte collectionId, short schemaTypeId) {
		systemWideLock.obtainWritingPermit();
		collectionLock(collectionId).obtainWritingPermit();
		schemaType(collectionId, schemaTypeId).obtainWritingPermit();
	}

	public void releaseSchemaTypeWritingPermit(byte collectionId, short schemaTypeId) {
		schemaType(collectionId, schemaTypeId).finishedWriting();
		collectionLock(collectionId).finishedWriting();
		systemWideLock.finishedWriting();

	}

	public void obtainCollectionWritingPermit(byte collectionId) {
		systemWideLock.obtainWritingPermit();
		collectionLock(collectionId).obtainWritingPermit();
	}

	public void releaseCollectionWritingPermit(byte collectionId) {
		collectionLock(collectionId).finishedWriting();
		systemWideLock.finishedWriting();
	}

	private RWLock collectionLock(byte collectionId) {
		int collectionIndex = ((int) collectionId) - Byte.MIN_VALUE;
		RWLock rwLock = collectionLocks[collectionIndex];

		if (rwLock == null) {
			synchronized (this) {
				rwLock = collectionLocks[collectionIndex];
				if (rwLock == null) {
					rwLock = new RWLock();
					collectionLocks[collectionIndex] = rwLock;
				}
			}
		}

		return rwLock;
	}

	private RWLock schemaType(byte collectionId, short typeId) {
		int collectionIndex = ((int) collectionId) - Byte.MIN_VALUE;
		RWLock[] collectionLocks = schemaTypeLocks[collectionIndex];

		if (collectionLocks == null) {
			synchronized (this) {
				collectionLocks = schemaTypeLocks[collectionIndex];
				if (collectionLocks == null) {
					collectionLocks = new RWLock[MetadataSchemaTypes.LIMIT_OF_TYPES_IN_COLLECTION];
					schemaTypeLocks[collectionIndex] = collectionLocks;
				}
			}
		}

		RWLock rwLock = collectionLocks[collectionIndex];
		if (rwLock == null) {
			synchronized (this) {
				rwLock = collectionLocks[collectionIndex];
				if (rwLock == null) {
					rwLock = new RWLock();
					collectionLocks[collectionIndex] = rwLock;
				}
			}
		}

		return rwLock;
	}


	private static class RWLock {
		AtomicInteger canRead1 = new AtomicInteger();
		AtomicInteger canRead2 = new AtomicInteger();
		AtomicInteger readingThreads = new AtomicInteger();


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
			canRead2.incrementAndGet();
			canRead1.incrementAndGet();

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
}
