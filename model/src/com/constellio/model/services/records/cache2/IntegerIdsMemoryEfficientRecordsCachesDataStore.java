package com.constellio.model.services.records.cache2;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.SolrRecordDTO;
import com.constellio.data.utils.LazyIterator;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache2.ByteArrayRecordDTO.ByteArrayRecordDTOWithIntegerId;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.constellio.model.entities.schemas.MetadataSchemaTypes.LIMIT_OF_TYPES_IN_COLLECTION;
import static com.constellio.model.services.schemas.SchemaUtils.getSchemaTypeCode;

/**
 * There is two levels of synchronizations
 */
public class IntegerIdsMemoryEfficientRecordsCachesDataStore {

	//Memory structure of records with int id
	//Level 0 : Collection id (max 256 values)
	//Level 1 : Type id (max 1000 values)
	OffHeapIntList ids = new OffHeapIntList();
	OffHeapLongList versions = new OffHeapLongList();
	OffHeapShortList schema = new OffHeapShortList();
	OffHeapShortList type = new OffHeapShortList();
	OffHeapByteList collection = new OffHeapByteList();
	OffHeapByteArrayList summaryCachedData = new OffHeapByteArrayList();

	//TODO Replace List by another collection to save memory
	List<RecordDTO> fullyCachedData = new ArrayList<>();

	IntArrayList[][] typesIndexes = new IntArrayList[256][];

	private ModelLayerFactory modelLayerFactory;
	private CollectionsListManager collectionsListManager;
	private MetadataSchemasManager schemasManager;


	//TODO Francis : Improve with three-level locking mecanism (level 1 for system, level 2 for collection, level 3 for type)
	//A get or a stream on an unlocked type should be allowed - but must handle the case where a full reassignIsRequired
	private boolean canRead1;
	private boolean canRead2;
	private AtomicInteger readingThreads = new AtomicInteger();

	public IntegerIdsMemoryEfficientRecordsCachesDataStore(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.collectionsListManager = modelLayerFactory.getCollectionsListManager();
		this.schemasManager = modelLayerFactory.getMetadataSchemasManager();
	}


	private void obtainReadingPermission() {
		//System.out.println("Asking reading permission...");
		boolean obtained = false;

		while (!obtained) {
			if (canRead1) {
				readingThreads.incrementAndGet();
				if (canRead2) {
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
		//	System.out.println("Asking reading permission... obtained");
	}

	private void finishedReading() {
		readingThreads.decrementAndGet();
		//System.out.println("Finished reading");
	}

	private void obtainWritingPermit() {
		//System.out.println("Asking writing permission...");
		canRead1 = false;
		canRead2 = false;

		while (readingThreads.get() != 0) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			//Wait
		}

		//System.out.println("Asking writing permission... obtained");
	}

	private void finishedWriting() {
		canRead2 = true;
		canRead1 = true;

	}

	private byte collectionId(String collectionCode) {
		return collectionsListManager.getCollectionInfo(collectionCode).getCollectionId();
	}

	private short typeId(String collectionCode, String typeCode) {
		return schemasManager.getSchemaTypes(collectionCode).getSchemaType(typeCode).getId();
	}

	private short schemaId(String collectionCode, String schemaCode) {
		return schemasManager.getSchemaTypes(collectionCode).getSchema(schemaCode).getId();
	}

	private byte getCollectionIdOf(RecordDTO dto) {
		if (dto instanceof ByteArrayRecordDTO) {
			return ((ByteArrayRecordDTO) dto).getCollectionId();
		} else {
			String collectionCode = dto.getCollection();
			return collectionId(collectionCode);
		}
	}

	private int getCollectionIndexOf(RecordDTO dto) {
		byte collectionId = getCollectionIdOf(dto);
		return ((int) collectionId) - Byte.MIN_VALUE;
	}

	private short getTypeId(RecordDTO dto) {
		if (dto instanceof ByteArrayRecordDTO) {
			return ((ByteArrayRecordDTO) dto).getTypeId();
		} else {
			String collectionCode = dto.getCollection();
			String schemaCode = dto.getSchemaCode();
			return typeId(collectionCode, getSchemaTypeCode(schemaCode));
		}
	}

	synchronized void remove(int id, RecordDTO dto) {


		byte collectionId = 0;
		short typeId = 0;

		if (dto instanceof ByteArrayRecordDTO) {
			collectionId = ((ByteArrayRecordDTO) dto).getCollectionId();
			typeId = ((ByteArrayRecordDTO) dto).getTypeId();
		} else {
			String collectionCode = dto.getCollection();
			String schemaCode = dto.getSchemaCode();
			collectionId = collectionId(collectionCode);
			typeId = typeId(collectionCode, getSchemaTypeCode(schemaCode));
		}
		int collectionIndex = collectionId - Byte.MIN_VALUE;
		int index = ids.binarySearch(id);

		obtainWritingPermit();
		try {
			if (index >= 0) {
				//Important to set the schema first, since it is the first read
				schema.set(index, (short) 0);
				versions.set(index, 0L);
				collection.set(index, (byte) 0);
				type.set(index, (short) 0);

				if (dto instanceof ByteArrayRecordDTO) {
					summaryCachedData.set(index, null);
				} else {
					fullyCachedData.set(index, null);
				}

				IntArrayList typeIndexes = typesIndexes[collectionIndex][typeId];
				typeIndexes.remove(index);

			}

		} finally {
			finishedWriting();
		}
	}


	synchronized void insert(int id, RecordDTO dto, boolean holdSpaceForPreviousIds) {

		byte collectionId = 0;
		short typeId = 0;
		short schemaId = 0;

		if (dto instanceof ByteArrayRecordDTO) {
			collectionId = ((ByteArrayRecordDTO) dto).getCollectionId();
			typeId = ((ByteArrayRecordDTO) dto).getTypeId();
			schemaId = ((ByteArrayRecordDTO) dto).getSchemaId();
		} else {
			String collectionCode = dto.getCollection();
			String schemaCode = dto.getSchemaCode();
			collectionId = collectionId(collectionCode);
			typeId = typeId(collectionCode, getSchemaTypeCode(schemaCode));
			schemaId = schemaId(collectionCode, schemaCode);
		}

		int collectionIndex = ((int) collectionId) - Byte.MIN_VALUE;

		IntArrayList[] collectionTypesIds = typesIndexes[collectionIndex];
		if (collectionTypesIds == null) {
			collectionTypesIds = typesIndexes[collectionIndex];
			if (collectionTypesIds == null) {
				collectionTypesIds = typesIndexes[collectionIndex] = new IntArrayList[LIMIT_OF_TYPES_IN_COLLECTION];
			}
		}


		IntArrayList typeIds = collectionTypesIds[typeId];
		if (typeIds == null) {
			typeIds = typesIndexes[collectionIndex][typeId];
			if (typeIds == null) {
				typeIds = typesIndexes[collectionIndex][typeId] = new IntArrayList();
			}
		}


		int index = ids.binarySearch(id);

		obtainWritingPermit();

		try {

			boolean setAtIndex = true;
			boolean newRecord = index == -1;
			if (index < 0) {
				index = ajustArraysForNewId(id, dto, holdSpaceForPreviousIds, typeId, schemaId, collectionId, collectionIndex);
				setAtIndex = index != -1;
			}
			newRecord |= schema.get(index) == 0;

			if (setAtIndex) {

				if (dto instanceof ByteArrayRecordDTO) {
					summaryCachedData.set(index, ((ByteArrayRecordDTO) dto).data);
				} else {
					while (index >= fullyCachedData.size()) {
						fullyCachedData.add(null);
					}
					fullyCachedData.set(index, dto);
				}
				versions.set(index, dto.getVersion());

				collection.set(index, collectionId);
				type.set(index, typeId);
				schema.set(index, schemaId);
			}

			if (newRecord) {
				typeIds.add(index);

			}

		} finally {
			finishedWriting();
		}
	}

	private int ajustArraysForNewId(int id, RecordDTO dto, boolean holdSpaceForPreviousIds, short typeId,
									short schemaId, byte collectionId, int collectionIndex) {
		boolean solrRecord = dto instanceof SolrRecordDTO;
		int lastId = ids.getLast();

		int indexToWrite = -1;
		if (ids.isEmpty() || lastId <= id) {
			if (holdSpaceForPreviousIds) {

				int last = ids.isEmpty() ? 0 : ids.getLast();
				while (last < id) {
					int index = ids.size();
					last++;
					ids.set(index, last);
					if (last == id) {
						indexToWrite = index;

					} else {
						schema.set(index, (short) 0);
						type.set(index, (short) 0);
						collection.set(index, (byte) 0);
						versions.set(index, (short) 0);

						if (!solrRecord) {
							summaryCachedData.set(index, null);
						} else {
							fullyCachedData.add(index, null);
						}
					}

				}

			} else {
				indexToWrite = ids.size();
				ids.set(indexToWrite, id);
				schema.set(indexToWrite, (short) 0);
				type.set(indexToWrite, (short) 0);
				collection.set(indexToWrite, (byte) 0);
				versions.set(indexToWrite, (short) 0);

				if (!solrRecord) {
					summaryCachedData.set(indexToWrite, null);
				} else {
					fullyCachedData.add(indexToWrite, null);
				}
			}
		} else {

			int insertAtIndex = -1;
			for (int i = 0; i < ids.size(); i++) {
				if (ids.get(i) < id) {
					insertAtIndex = i;
				} else {
					break;
				}
			}
			insertAtIndex++;

			System.out.println("Reshifting the universe");
			ids.insertValueShiftingAllFollowingValues(insertAtIndex, id);
			versions.insertValueShiftingAllFollowingValues(insertAtIndex, 0);
			schema.insertValueShiftingAllFollowingValues(insertAtIndex, (short) 0);
			type.insertValueShiftingAllFollowingValues(insertAtIndex, (short) 0);
			collection.insertValueShiftingAllFollowingValues(insertAtIndex, (byte) 0);

			if (!solrRecord) {
				summaryCachedData.insertValueShiftingAllFollowingValues(insertAtIndex, ((ByteArrayRecordDTO) dto).data);
			} else {
				fullyCachedData.add(insertAtIndex, null);
			}

			indexToWrite = insertAtIndex;
		}

		return indexToWrite;
	}

	RecordDTO __get(int id) {
		obtainReadingPermission();
		try {
			int index = ids.binarySearch(id);

			if (index == -1) {
				return null;

			} else {
				return get(id, index);
			}

		} finally {
			finishedReading();
		}

	}

	RecordDTO __get(byte collectionId, short typeId, short schemaId, int id) {

		obtainReadingPermission();
		try {
			int index = ids.binarySearch(id);

			if (index == -1) {
				return null;

			} else {
				return get(id, collectionId, typeId, schemaId, index);
			}

		} finally {
			finishedReading();
		}

	}

	RecordDTO __get(byte collectionId, int id) {

		obtainReadingPermission();
		try {
			int index = ids.binarySearch(id);

			if (index == -1) {
				return null;

			} else {
				short schemaId = schema.get(index);
				if (schemaId == 0) {
					return null;
				}
				short typeId = type.get(index);
				return get(id, collectionId, typeId, schemaId, index);
			}
		} finally {
			finishedReading();
		}

	}

	private RecordDTO getUnknownIdAtIndex(byte collectionId, short typeId, short schemaId, int listIndex) {
		int id = ids.get(listIndex);
		return get(id, collectionId, typeId, schemaId, listIndex);
	}

	private RecordDTO get(int id, int listIndex) {
		short schemaId = schema.get(listIndex);
		if (schemaId == 0) {
			return null;
		}
		byte collectionId = collection.get(listIndex);
		short typeId = type.get(listIndex);
		return get(id, collectionId, typeId, schemaId, listIndex);
	}


	private RecordDTO get(int id, byte collectionId, short typeId, short schemaId, int listIndex) {

		collectionId = collection.get(listIndex);
		typeId = type.get(listIndex);
		schemaId = schema.get(listIndex);

		if (schemaId == 0) {
			return null;
		}

		OffHeapBytesSupplier offHeapBytesSupplier = summaryCachedData.get(listIndex);
		if (offHeapBytesSupplier != null) {
			byte[] data = offHeapBytesSupplier.toArray();
			long version = versions.get(listIndex);
			MetadataSchemaType type = schemasManager.get(collectionId, typeId);
			MetadataSchema schema = type.getSchema(schemaId);

			return new ByteArrayRecordDTOWithIntegerId(id, modelLayerFactory.getMetadataSchemasManager(), version, true,
					schema.getCollection(), collectionId, type.getCode(), typeId, schema.getCode(), schemaId, data);

		} else {
			return fullyCachedData.get(listIndex);
		}

	}


	public Iterator<RecordDTO> iterator() {

		return new LazyIterator<RecordDTO>() {

			int index = 0;

			@Override
			protected RecordDTO getNextOrNull() {
				obtainReadingPermission();
				try {
					while (index < ids.size()) {
						int id = ids.get(index);
						RecordDTO dto = get(id, index);
						index++;
						if (dto != null) {
							return dto;
						}

					}

					return null;
				} finally {
					finishedReading();
				}
			}
		};
	}

	public Iterator<RecordDTO> iterator(byte collectionId) {


		return new LazyIterator<RecordDTO>() {

			int index = 0;

			@Override
			protected RecordDTO getNextOrNull() {

				obtainReadingPermission();
				try {
					while (index < ids.size()) {
						int seekedIndex = index++;
						int id = ids.get(seekedIndex);
						short schemaId = schema.get(seekedIndex);
						if (schemaId == 0) {
							continue;
						}

						byte aCollectionId = collection.get(seekedIndex);
						if (aCollectionId != collectionId) {
							continue;
						}
						short typeId = type.get(seekedIndex);
						RecordDTO dto = get(id, collectionId, typeId, schemaId, seekedIndex);
						if (dto != null) {
							return dto;
						}

					}

					return null;
				} finally {
					finishedReading();
				}
			}
		};
	}


	public Iterator<RecordDTO> iterator(byte collectionId, short typeId) {

		return new LazyIterator<RecordDTO>() {

			int index = 0;

			@Override
			protected RecordDTO getNextOrNull() {

				obtainReadingPermission();

				try {
					while (index < ids.size()) {
						int seekedIndex = index++;
						int id = ids.get(seekedIndex);

						short schemaId = schema.get(seekedIndex);
						if (schemaId == 0) {
							continue;
						}

						byte aCollectionId = collection.get(seekedIndex);
						if (aCollectionId != collectionId) {
							continue;
						}
						short aTypeId = type.get(seekedIndex);
						if (aTypeId != typeId) {
							continue;
						}
						RecordDTO dto = get(id, collectionId, typeId, schemaId, seekedIndex);
						if (dto != null) {
							return dto;
						}

					}

					return null;
				} finally {
					finishedReading();
				}
			}
		};
	}


	//	public Iterator<RecordDTO> iterator(byte collectionId) {
	//
	//		int collectionIndex = collectionId - Byte.MIN_VALUE;
	//
	//		if (typesIndexes[collectionIndex] == null) {
	//			return Collections.emptyIterator();
	//		}
	//
	//		return new LazyIterator<RecordDTO>() {
	//
	//			short schemaTypeIndex = 0;
	//			int typeIndex = 0;
	//
	//			@Override
	//			protected RecordDTO getNextOrNull() {
	//
	//				while (schemaTypeIndex < LIMIT_OF_TYPES_IN_COLLECTION) {
	//					IntArrayList typeIndexes = typesIndexes[collectionIndex][schemaTypeIndex];
	//					if (typeIndexes == null || typeIndexes.size() <= typeIndex) {
	//						schemaTypeIndex++;
	//						typeIndex = 0;
	//
	//					} else {
	//						int index = typeIndexes.get(typeIndex++);
	//						if (index != -1) {
	//							RecordDTO dto = getUnknownIdAtIndex(collectionId, schemaTypeIndex, index);
	//							if (dto != null) {
	//								return dto;
	//							}
	//						}
	//					}
	//
	//				}
	//
	//				return null;
	//
	//			}
	//		};
	//	}
	//
	//
	//	public Iterator<RecordDTO> iterator(byte collectionId, short typeId) {
	//
	//		int collectionIndex = collectionId - Byte.MIN_VALUE;
	//
	//		if (typesIndexes[collectionIndex] == null) {
	//			return Collections.emptyIterator();
	//		}
	//
	//		IntArrayList typeIndexes = typesIndexes[collectionIndex][typeId];
	//
	//		if (typeIndexes == null) {
	//			return Collections.emptyIterator();
	//		}
	//
	//		return new LazyIterator<RecordDTO>() {
	//
	//			int typeIndex = 0;
	//
	//			@Override
	//			protected RecordDTO getNextOrNull() {
	//
	//				while (typeIndex < typeIndexes.size()) {
	//
	//					int index = typeIndexes.get(typeIndex++);
	//					if (index != -1) {
	//						RecordDTO dto = getUnknownIdAtIndex(collectionId, typeId, index);
	//						if (dto != null) {
	//							return dto;
	//						}
	//					}
	//				}
	//
	//				return null;
	//
	//			}
	//		};
	//	}
}
