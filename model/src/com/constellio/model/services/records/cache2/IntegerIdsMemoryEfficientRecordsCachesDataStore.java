package com.constellio.model.services.records.cache2;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.SolrRecordDTO;
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.LazyIterator;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache2.ByteArrayRecordDTO.ByteArrayRecordDTOWithIntegerId;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

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

	List<MetadataIndex>[][] indexes = new List[256][];

	IntArrayList[][] typesIndexes = new IntArrayList[256][];

	private ModelLayerFactory modelLayerFactory;
	private CollectionsListManager collectionsListManager;
	private MetadataSchemasManager schemasManager;


	//TODO Francis : Improve with three-level locking mecanism (level 1 for system, level 2 for collection, level 3 for type)
	//A get or a stream on an unlocked type should be allowed - but must handle the case where a full reassignIsRequired
	private boolean canRead1 = true;
	private boolean canRead2 = true;
	private AtomicInteger readingThreads = new AtomicInteger();

	public IntegerIdsMemoryEfficientRecordsCachesDataStore(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.collectionsListManager = modelLayerFactory.getCollectionsListManager();
		this.schemasManager = modelLayerFactory.getMetadataSchemasManager();
	}


	private void obtainReadingPermit() {
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
	}

	private void finishedReading() {
		readingThreads.decrementAndGet();
	}

	private void obtainWritingPermit() {
		canRead1 = false;
		canRead2 = false;

		while (readingThreads.get() != 0) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

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

		int index = ids.binarySearch(id);
		boolean summary = dto instanceof ByteArrayRecordDTO;
		obtainWritingPermit();
		try {
			if (index >= 0) {

				removeAtIndex(collectionId, typeId, index, summary);
			}

		} finally {
			finishedWriting();
		}
	}

	private void removeAtIndex(byte collectionId, short typeId, int index, boolean summary) {
		//Important to set the schema first, since it is the first read
		schema.set(index, (short) 0);
		versions.set(index, 0L);
		collection.set(index, (byte) 0);
		type.set(index, (short) 0);
		int collectionIndex = ((int) collectionId) - Byte.MIN_VALUE;


		if (summary) {
			summaryCachedData.set(index, null);
		} else {
			fullyCachedData.set(index, null);
		}

		IntArrayList typeIndexes = typesIndexes[collectionIndex][typeId];
		typeIndexes.remove(index);
	}


	synchronized void insert(int id, RecordDTO dto, boolean holdSpaceForPreviousIds) {

		byte collectionId;
		short typeId;
		short schemaId;

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
		int index = ids.binarySearch(id);

		obtainWritingPermit();

		try {

			boolean newRecord = index == -1;
			if (index < 0) {
				boolean fullyCached = dto instanceof SolrRecordDTO;
				index = ajustArraysForNewId(id, fullyCached, holdSpaceForPreviousIds);
			}
			writeAtIndex(dto, collectionId, typeId, schemaId, collectionIndex, index, newRecord);

		} finally {
			finishedWriting();
		}
	}

	private void writeAtIndex(RecordDTO dto, byte collectionId, short typeId, short schemaId, int collectionIndex,
							  int index, boolean newRecord) {
		newRecord |= schema.get(index) == 0;

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

		if (newRecord) {
			IntArrayList typeIds = getTypeIdsList(typeId, collectionIndex);
			typeIds.add(index);
		}
	}

	private IntArrayList getTypeIdsList(short typeId, int collectionIndex) {
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
		return typeIds;
	}

	private int ajustArraysForNewId(int id, boolean fullyCached, boolean holdSpaceForPreviousIds) {
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
						setToNull(fullyCached, index);
					}

				}

			} else {
				indexToWrite = ids.size();
				ids.set(indexToWrite, id);
				setToNull(fullyCached, indexToWrite);
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

			ids.insertValueShiftingAllFollowingValues(insertAtIndex, id);
			versions.insertValueShiftingAllFollowingValues(insertAtIndex, 0);
			schema.insertValueShiftingAllFollowingValues(insertAtIndex, (short) 0);
			type.insertValueShiftingAllFollowingValues(insertAtIndex, (short) 0);
			collection.insertValueShiftingAllFollowingValues(insertAtIndex, (byte) 0);

			if (fullyCached) {
				fullyCachedData.add(insertAtIndex, null);
			} else {
				summaryCachedData.insertValueShiftingAllFollowingValues(insertAtIndex, new byte[0]);
			}

			for (int i = 0; i < 256; i++) {
				if (typesIndexes[i] != null) {
					for (int j = 0; j < LIMIT_OF_TYPES_IN_COLLECTION; j++) {
						if (typesIndexes[i][j] != null) {
							for (int k = 0; k < typesIndexes[i][j].size(); k++) {
								int index = typesIndexes[i][j].get(k);
								if (index >= insertAtIndex) {
									typesIndexes[i][j].set(k, index + 1);
								}
							}
						}
					}
				}
			}

			indexToWrite = insertAtIndex;
		}

		return indexToWrite;
	}

	private void setToNull(boolean fullyCached, int index) {
		schema.set(index, (short) 0);
		type.set(index, (short) 0);
		collection.set(index, (byte) 0);
		versions.set(index, (short) 0);

		if (!fullyCached) {
			summaryCachedData.set(index, null);
		} else {
			fullyCachedData.add(index, null);
		}
	}

	RecordDTO __get(int id) {
		obtainReadingPermit();
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

		obtainReadingPermit();
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

		obtainReadingPermit();
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

	private RecordDTO getUnknownIdAtIndex(int index) {
		byte collectionId = collection.get(index);
		short typeId = type.get(index);
		return getUnknownIdAtIndex(collectionId, typeId, index);
	}

	private RecordDTO getUnknownIdAtIndex(byte collectionId, short typeId, int listIndex) {
		int id = ids.get(listIndex);
		short schemaId = schema.get(listIndex);
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


	synchronized void invalidate(Predicate<RecordDTO> predicate) {
		obtainWritingPermit();
		try {

			for (int i = 0; i < ids.size(); i++) {
				RecordDTO dto = getUnknownIdAtIndex(i);
				if (dto != null && predicate.test(dto)) {
					byte collectionId = getCollectionIdOf(dto);
					short typeId = getTypeId(dto);
					boolean summary = dto instanceof ByteArrayRecordDTO;
					removeAtIndex(collectionId, typeId, i, summary);
				}
			}

		} finally {
			finishedWriting();
		}
	}

	synchronized void invalidate(byte predicateCollectionId, Predicate<RecordDTO> predicate) {
		obtainWritingPermit();
		try {

			for (int i = 0; i < ids.size(); i++) {
				RecordDTO dto = getUnknownIdAtIndex(i);
				if (dto != null && predicate.test(dto)) {
					byte collectionId = getCollectionIdOf(dto);
					if (collectionId != predicateCollectionId) {
						continue;
					}
					short typeId = getTypeId(dto);
					boolean summary = dto instanceof ByteArrayRecordDTO;
					removeAtIndex(collectionId, typeId, i, summary);
				}
			}

		} finally {
			finishedWriting();
		}
	}

	synchronized void invalidateAll(byte predicateCollectionId, short predicateTypeId) {
		invalidate(predicateCollectionId, predicateTypeId, (r) -> true);
	}

	synchronized void invalidate(byte predicateCollectionId, short predicateTypeId,
								 Predicate<RecordDTO> predicate) {
		obtainWritingPermit();
		try {

			for (int i = 0; i < ids.size(); i++) {
				RecordDTO dto = getUnknownIdAtIndex(i);
				if (dto != null && predicate.test(dto)) {
					byte collectionId = getCollectionIdOf(dto);
					if (collectionId != predicateCollectionId) {
						continue;
					}
					short typeId = getTypeId(dto);
					if (typeId != predicateTypeId) {
						continue;
					}

					boolean summary = dto instanceof ByteArrayRecordDTO;
					removeAtIndex(collectionId, typeId, i, summary);
				}
			}

		} finally {
			finishedWriting();
		}
	}


	Iterator<RecordDTO> iterator(boolean autoClosedIterator) {

		return new LazyIterator<RecordDTO>() {

			int index = 0;

			@Override
			protected RecordDTO getNextOrNull() {
				if (autoClosedIterator) {
					obtainReadingPermit();
				}
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
					if (autoClosedIterator) {
						finishedReading();
					}
				}
			}
		};
	}

	Iterator<RecordDTO> iterator(boolean autoClosedIterator, byte collectionId) {

		int collectionIndex = collectionId - Byte.MIN_VALUE;

		if (typesIndexes[collectionIndex] == null) {
			return Collections.emptyIterator();
		}


		return new LazyIterator<RecordDTO>() {

			short schemaTypeIndex = 0;
			int typeIndex = 0;

			@Override
			protected RecordDTO getNextOrNull() {

				if (autoClosedIterator) {
					obtainReadingPermit();
				}

				try {
					while (schemaTypeIndex < LIMIT_OF_TYPES_IN_COLLECTION) {
						IntArrayList typeIndexes = typesIndexes[collectionIndex][schemaTypeIndex];
						if (typeIndexes == null || typeIndexes.size() <= typeIndex) {
							schemaTypeIndex++;
							typeIndex = 0;

						} else {
							int index = typeIndexes.get(typeIndex++);
							if (index != -1) {
								RecordDTO dto = getUnknownIdAtIndex(collectionId, schemaTypeIndex, index);
								if (dto != null) {
									return dto;
								}
							}
						}

					}

				} finally {
					if (autoClosedIterator) {
						finishedReading();
					}
				}
				return null;

			}
		};
	}


	Iterator<RecordDTO> iterator(boolean autoClosedIterator, byte collectionId, short typeId) {

		int collectionIndex = collectionId - Byte.MIN_VALUE;

		if (typesIndexes[collectionIndex] == null) {
			return Collections.emptyIterator();
		}

		IntArrayList typeIndexes = typesIndexes[collectionIndex][typeId];

		if (typeIndexes == null) {
			return Collections.emptyIterator();
		}

		return new LazyIterator<RecordDTO>() {

			int typeIndex = 0;

			@Override
			protected RecordDTO getNextOrNull() {

				if (autoClosedIterator) {
					obtainReadingPermit();
				}

				try {
					while (typeIndex < typeIndexes.size()) {

						int index = typeIndexes.get(typeIndex++);
						if (index != -1) {
							RecordDTO dto = getUnknownIdAtIndex(collectionId, typeId, index);
							if (dto != null) {
								return dto;
							}
						}
					}

				} finally {
					if (autoClosedIterator) {
						finishedReading();
					}
				}


				return null;

			}
		};
	}

	Iterator<RecordDTO> iterator(boolean autoClosedIterator, byte collectionId, short typeId, short metadataId,
								 Object value) {
		//TODO : Replace streaming with proper indexes
		String metadataDataStoreCode = schemasManager.getMetadata(collectionId, typeId, metadataId).getDataStoreCode();

		List<RecordDTO> dtos = new ArrayList<>();
		iterator(false, collectionId, typeId).forEachRemaining(dto -> {
			if (LangUtils.isEqual(dto.getFields().get(metadataDataStoreCode), value)) {
				dtos.add(dto);
			}
		});

		return dtos.iterator();

		//		MetadataIndex metadataIndex = findIndex(collectionId, typeId, metadataId, false);
		//		if (metadataIndex == null) {
		//			return Collections.emptyIterator();
		//		}
		//
		//		IntArrayList positions = metadataIndex.positionsWithValue.get(value);
		//		if (positions == null) {
		//			return Collections.emptyIterator();
		//		}
		//
		//		List<RecordDTO> dtos = new ArrayList<>();
		//		positions.forEach(index -> dtos.add(getUnknownIdAtIndex(collectionId, typeId, index)));
		//		return dtos.iterator();


	}

	private MetadataIndex findIndex(byte collectionId, short typeId, short metadataId, boolean createIfInexisting) {
		int collectionIndex = ((int) collectionId) - Byte.MIN_VALUE;

		List<MetadataIndex>[] collectionIndexes = indexes[collectionIndex];
		if (collectionIndexes == null) {
			if (createIfInexisting) {
				collectionIndexes = indexes[collectionIndex] = new List[LIMIT_OF_TYPES_IN_COLLECTION];
			} else {
				return null;
			}
		}

		List<MetadataIndex> typeIndexes = collectionIndexes[typeId];
		if (typeIndexes == null) {
			if (createIfInexisting) {
				typeIndexes = collectionIndexes[typeId] = new ArrayList<>();
			} else {
				return null;
			}
		}

		MetadataIndex foundIndex = null;
		for (MetadataIndex metadataIndex : typeIndexes) {
			if (metadataIndex.metadataId == metadataId) {
				foundIndex = metadataIndex;
				break;
			}
		}

		if (foundIndex == null && createIfInexisting) {
			foundIndex = new MetadataIndex(metadataId);
			typeIndexes.add(foundIndex);
		}

		return foundIndex;
	}

	private static class MetadataIndex {

		short metadataId;

		Map<Object, IntArrayList> positionsWithValue;

		public MetadataIndex(short metadataId) {
			this.metadataId = metadataId;
			this.positionsWithValue = new HashMap<>();
		}
	}
}
