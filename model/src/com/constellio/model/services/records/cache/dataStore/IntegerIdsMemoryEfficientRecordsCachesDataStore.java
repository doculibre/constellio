package com.constellio.model.services.records.cache.dataStore;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.SolrRecordDTO;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.data.utils.CacheStat;
import com.constellio.data.utils.KeyLongMap;
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.LazyIterator;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordId;
import com.constellio.model.services.records.cache.ByteArrayRecordDTO;
import com.constellio.model.services.records.cache.ByteArrayRecordDTO.ByteArrayRecordDTOWithIntegerId;
import com.constellio.model.services.records.cache.locks.SimpleReadLockMechanism;
import com.constellio.model.services.records.cache.offHeapCollections.OffHeapByteArrayList2;
import com.constellio.model.services.records.cache.offHeapCollections.OffHeapByteList;
import com.constellio.model.services.records.cache.offHeapCollections.OffHeapIntList;
import com.constellio.model.services.records.cache.offHeapCollections.OffHeapLongList;
import com.constellio.model.services.records.cache.offHeapCollections.OffHeapShortList;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.constellio.model.entities.schemas.MetadataSchemaTypes.LIMIT_OF_TYPES_IN_COLLECTION;
import static com.constellio.model.services.schemas.SchemaUtils.getSchemaTypeCode;

public class IntegerIdsMemoryEfficientRecordsCachesDataStore {


	private static final Logger LOGGER = LoggerFactory.getLogger(IntegerIdsMemoryEfficientRecordsCachesDataStore.class);
	//Memory structure of records with int id
	//Level 0 : Collection id (max 256 values)
	//Level 1 : Type id (max 1000 values)
	OffHeapIntList ids = new OffHeapIntList();
	OffHeapLongList versions = new OffHeapLongList();
	OffHeapShortList schema = new OffHeapShortList();
	OffHeapShortList type = new OffHeapShortList();
	OffHeapByteList collection = new OffHeapByteList();
	OffHeapByteArrayList2 summaryCachedData = new OffHeapByteArrayList2();

	//TODO Replace List by another collection to save memory
	List<RecordDTO> fullyCachedData = new ArrayList<>();

	IntArrayList[][] typesIndexes = new IntArrayList[256][];

	private ModelLayerFactory modelLayerFactory;
	private CollectionsListManager collectionsListManager;
	private MetadataSchemasManager schemasManager;

	private SimpleReadLockMechanism mechanism = new SimpleReadLockMechanism();
	private short tenantId;

	public IntegerIdsMemoryEfficientRecordsCachesDataStore(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.collectionsListManager = modelLayerFactory.getCollectionsListManager();
		this.schemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.tenantId = modelLayerFactory.getInstanceId();
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

	void remove(int id, RecordDTO dto) {

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

		int index = -1;
		mechanism.obtainSystemWideReadingPermit();
		try {
			index = ids.binarySearch(id);
		} finally {
			mechanism.releaseSystemWideReadingPermit();
		}
		boolean summary = dto instanceof ByteArrayRecordDTO;
		mechanism.obtainSchemaTypeWritingPermit(collectionId, typeId);
		try {
			synchronized (this) {
				if (index >= 0) {

					removeFromMainListsAndTypeIndex(collectionId, typeId, index, summary);
				}
			}
		} finally {
			mechanism.releaseSchemaTypeWritingPermit(collectionId, typeId);
		}
	}

	/**
	 * Always type write locked and synchronized
	 */
	private void removeFromMainListsAndTypeIndex(byte collectionId, short typeId, int index, boolean summary) {
		int collectionIndex = removeFromMainLists(collectionId, index, summary);

		IntArrayList typeIndexes = typesIndexes[collectionIndex][typeId];
		typeIndexes.remove(index);
	}

	/**
	 * Always write locked and synchronized
	 */
	private int removeFromMainLists(int collectionId, int index, boolean summary) {
		//Important to set the schema first, since it is the first read
		schema.set(index, (short) 0);
		versions.set(index, 0L);
		collection.set(index, (byte) 0);
		type.set(index, (short) 0);
		int collectionIndex = collectionId - Byte.MIN_VALUE;


		if (summary) {
			summaryCachedData.set(index, null);
		} else {
			fullyCachedData.set(index, null);
		}
		return collectionIndex;
	}

	/**
	 * Not locked or synchronized
	 */
	void insert(int id, RecordDTO dto, boolean holdSpaceForPreviousIds) {

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
		int index = -1;
		mechanism.obtainSystemWideReadingPermit();
		try {
			index = ids.binarySearch(id);
		} finally {
			mechanism.releaseSystemWideReadingPermit();
		}

		mechanism.obtainSchemaTypeWritingPermit(collectionId, typeId);

		try {
			synchronized (this) {
				index = ids.binarySearch(id);
				boolean newRecord = index == -1;
				if (index < 0) {
					boolean fullyCached = dto instanceof SolrRecordDTO;
					index = ajustArraysForNewId(id, fullyCached, holdSpaceForPreviousIds);
				}
				writeAtIndex(dto, collectionId, typeId, schemaId, collectionIndex, index, newRecord);
			}
		} finally {
			mechanism.releaseSchemaTypeWritingPermit(collectionId, typeId);
		}
	}

	/**
	 * Always write locked and synchronized
	 */
	private void writeAtIndex(RecordDTO dto, byte collectionId, short typeId, short schemaId,
							  int collectionIndex,
							  int index, boolean newRecord) {
		newRecord |= schema.get(index) == 0;

		if (dto instanceof ByteArrayRecordDTO) {
			summaryCachedData.set(index, ((ByteArrayRecordDTO) dto).getData());
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

	/**
	 * Always write locked and synchronized
	 */
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

	/**
	 * Always write locked and synchronized
	 */
	private synchronized int ajustArraysForNewId(int id, boolean fullyCached, boolean holdSpaceForPreviousIds) {
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

			LOGGER.info("last id : " + lastId);
			LOGGER.info("new id : " + id);
			LOGGER.info("insert at index : " + insertAtIndex);

			ids.insertValueShiftingAllFollowingValues(insertAtIndex, id);
			versions.insertValueShiftingAllFollowingValues(insertAtIndex, 0);
			schema.insertValueShiftingAllFollowingValues(insertAtIndex, (short) 0);
			type.insertValueShiftingAllFollowingValues(insertAtIndex, (short) 0);
			collection.insertValueShiftingAllFollowingValues(insertAtIndex, (byte) 0);

			if (fullyCached) {
				while (fullyCachedData.size() < insertAtIndex) {
					fullyCachedData.add(null);
				}
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

	/**
	 * Always write locked and synchronized
	 */
	private void setToNull(boolean fullyCached, int index) {
		schema.set(index, (short) 0);
		type.set(index, (short) 0);
		collection.set(index, (byte) 0);
		versions.set(index, (short) 0);

		if (!fullyCached) {
			summaryCachedData.set(index, null);
		} else {
			while (fullyCachedData.size() < index) {
				fullyCachedData.add(null);
			}
			fullyCachedData.add(index, null);
		}
	}

	RecordDTO __get(int id) {
		mechanism.obtainSystemWideReadingPermit();
		try {
			int index = ids.binarySearch(id);

			if (index == -1) {
				return null;

			} else {
				return get(id, index);
			}

		} finally {
			mechanism.releaseSystemWideReadingPermit();
		}

	}

	RecordDTO __get(byte collectionId, short typeId, short schemaId, int id) {

		mechanism.obtainSystemWideReadingPermit();
		try {
			int index = ids.binarySearch(id);

			if (index == -1) {
				return null;

			} else {
				return get(id, collectionId, typeId, schemaId, index);
			}

		} finally {
			mechanism.releaseSystemWideReadingPermit();
		}

	}

	RecordDTO __get(byte collectionId, int id) {

		mechanism.obtainSystemWideReadingPermit();
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
			mechanism.releaseSystemWideReadingPermit();
		}

	}

	/**
	 * Always read locked or write locked and synchronized
	 */
	private RecordDTO getUnknownIdAtIndex(int index) {
		byte collectionId = collection.get(index);
		short typeId = type.get(index);
		return getUnknownIdAtIndex(collectionId, typeId, index);
	}

	/**
	 * Always read locked or write locked and synchronized
	 */
	private RecordDTO getUnknownIdAtIndex(byte collectionId, short typeId, int listIndex) {
		int id = ids.get(listIndex);
		short schemaId = schema.get(listIndex);
		return get(id, collectionId, typeId, schemaId, listIndex);
	}

	/**
	 * Always systemwide read locked
	 */
	private RecordDTO get(int id, int listIndex) {
		short schemaId = schema.get(listIndex);
		if (schemaId == 0) {
			return null;
		}
		byte collectionId = collection.get(listIndex);
		short typeId = type.get(listIndex);
		return get(id, collectionId, typeId, schemaId, listIndex);
	}


	/**
	 * Always read locked or write locked and synchronized
	 */
	private RecordDTO get(int id, byte collectionId, short typeId, short schemaId, int listIndex) {

		//System.out.println("get " + id + "," + collectionId + "," + typeId + "," + schemaId + "," + listIndex);
		if (schemaId == 0) {
			return null;
		}

		byte[] data = summaryCachedData.get(listIndex);
		if (data != null) {
			long version = versions.get(listIndex);
			MetadataSchemaType type = schemasManager.get(collectionId, typeId);
			MetadataSchema schema = type.getSchema(schemaId);

			return new ByteArrayRecordDTOWithIntegerId(id, modelLayerFactory.getMetadataSchemasManager(), version, true,
					tenantId, schema.getCollection(), collectionId, type.getCode(), typeId, schema.getCode(), schemaId, data);

		} else {
			return fullyCachedData.get(listIndex);
		}

	}


	private void invalidate(byte predicateCollectionId, Predicate<RecordDTO> predicate) {
		mechanism.obtainCollectionWritingPermit(predicateCollectionId);
		try {
			synchronized (this) {
				for (int i = 0; i < ids.size(); i++) {
					RecordDTO dto = getUnknownIdAtIndex(i);
					if (dto != null && predicate.test(dto)) {
						byte collectionId = getCollectionIdOf(dto);
						if (collectionId != predicateCollectionId) {
							continue;
						}
						boolean summary = dto instanceof ByteArrayRecordDTO;
						removeFromMainLists(collectionId, i, summary);
					}
				}

				int collectionIndex = predicateCollectionId - Byte.MIN_VALUE;
				IntArrayList[] typeIndexes = typesIndexes[collectionIndex];
				if (typeIndexes != null) {
					for (IntArrayList typeIndex : typeIndexes) {
						if (typeIndex != null) {
							typeIndex.clear();
						}
					}

				}
			}
		} finally {
			mechanism.releaseCollectionWritingPermit(predicateCollectionId);
		}
	}

	void invalidateAll(byte predicateCollectionId, short predicateTypeId) {
		invalidate(predicateCollectionId, predicateTypeId, (r) -> true);
	}

	void invalidateAll(byte predicateCollectionId) {
		invalidate(predicateCollectionId, (r) -> true);
	}

	void invalidate(byte predicateCollectionId, short predicateTypeId,
					Predicate<RecordDTO> predicate) {
		mechanism.obtainSchemaTypeWritingPermit(predicateCollectionId, predicateTypeId);

		try {
			synchronized (this) {
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
						removeFromMainListsAndTypeIndex(collectionId, typeId, i, summary);
					}
				}
			}
		} finally {
			mechanism.releaseSchemaTypeWritingPermit(predicateCollectionId, predicateTypeId);
		}
	}


	Iterator<RecordDTO> iterator(boolean autoClosedIterator) {

		return new LazyIterator<RecordDTO>() {

			int index = 0;

			@Override
			protected RecordDTO getNextOrNull() {
				if (autoClosedIterator) {
					mechanism.obtainSystemWideReadingPermit();
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
						mechanism.releaseSystemWideReadingPermit();
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
					mechanism.obtainCollectionReadingPermit(collectionId);
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
						mechanism.releaseCollectionReadingPermit(collectionId);
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
					mechanism.obtainSchemaTypeReadingPermit(collectionId, typeId);
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
						mechanism.releaseSchemaTypeReadingPermit(collectionId, typeId);
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
		iterator(autoClosedIterator, collectionId, typeId).forEachRemaining(dto -> {
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

	public void close() {

		mechanism.obtainSystemWideWritingPermit();

		try {
			this.ids.clear();
			this.ids = null;

			this.versions.clear();
			this.versions = null;

			this.schema.clear();
			this.schema = null;

			this.type.clear();
			this.type = null;

			this.collection.clear();
			this.collection = null;

			this.summaryCachedData.clear();
			this.summaryCachedData = null;

		} finally {
			mechanism.releaseSystemWideWritingPermit();
		}
	}

	public List<CacheStat> compileMemoryConsumptionStats() {
		mechanism.obtainSystemWideReadingPermit();
		try {
			List<CacheStat> stats = new ArrayList<>();

			//OffHeapIntList ids = new OffHeapIntList();
			stats.add(new CacheStat("datastore.ids", ids.getHeapConsumption(), ids.getOffHeapConsumption()));
			stats.add(new CacheStat("datastore.versions", versions.getHeapConsumption(), versions.getOffHeapConsumption()));
			stats.add(new CacheStat("datastore.schemas", schema.getHeapConsumption(), schema.getOffHeapConsumption()));
			stats.add(new CacheStat("datastore.types", type.getHeapConsumption(), type.getOffHeapConsumption()));
			stats.add(new CacheStat("datastore.collection", collection.getHeapConsumption(), collection.getOffHeapConsumption()));
			stats.add(new CacheStat("datastore.summaryCachedData", summaryCachedData.getHeapConsumption(), summaryCachedData.getOffHeapConsumption()));
			//OffHeapLongList versions = new OffHeapLongList();
			//OffHeapShortList schema = new OffHeapShortList();
			//OffHeapShortList type = new OffHeapShortList();
			//OffHeapByteList collection = new OffHeapByteList();
			//OffHeapByteArrayList summaryCachedData = new OffHeapByteArrayList();

			//List<RecordDTO> fullyCachedData = new ArrayList<>();

			//Not accurate

			KeyLongMap<String> schemaTypesConsumptions = new KeyLongMap<>();
			long fullyCachedDataSize = 12 + 4 + 12 * fullyCachedData.size();
			for (RecordDTO recordDTO : fullyCachedData) {
				if (recordDTO instanceof SolrRecordDTO) {
					long estimatedSize = recordDTO.heapMemoryConsumption();
					fullyCachedDataSize += estimatedSize;
					String typeCode = SchemaUtils.getSchemaTypeCode(recordDTO.getSchemaCode());
					schemaTypesConsumptions.increment(typeCode, estimatedSize);
				}

			}
			stats.add(new CacheStat("datastore.fullyCachedData", fullyCachedDataSize, 0));
			for (Map.Entry<String, Long> entry : schemaTypesConsumptions.entriesSortedByDescValue()) {
				stats.add(new CacheStat("datastore.fullyCachedData." + entry.getKey(), entry.getValue(), 0));
			}

			long typeIndexesSize = 12 + 12 * typesIndexes.length;
			for (int i = 0; i < typesIndexes.length; i++) {
				if (typesIndexes[i] != null) {
					typeIndexesSize += 12 + 12 * typesIndexes[i].length;
					for (int j = 0; j < typesIndexes[i].length; j++) {
						if (typesIndexes[i][j] != null) {
							//This is not totally accurate, but maybe not so bad either
							typeIndexesSize += 12 + 4 + typesIndexes[i][j].size() * Integer.BYTES;
						}
					}
				}
			}
			stats.add(new CacheStat("datastore.typeIndexes", typeIndexesSize, 0));

			return stats;
		} finally {
			mechanism.releaseSystemWideReadingPermit();
		}
	}

	public void structureCacheUsingExistingIds(Iterator<RecordId> sortedExistingIdsIterator) {

		BatchBuilderIterator<RecordId> batchIterator = new BatchBuilderIterator<>(sortedExistingIdsIterator, 10000);

		if (ids.size() != 0) {
			throw new IllegalStateException("Can only be called before cache loading");
		}
		LOGGER.info("structuring cache using id iterator");

		mechanism.obtainSystemWideWritingPermit();
		try {

			int index = 0;
			while (batchIterator.hasNext()) {
				List<RecordId> ids = batchIterator.next().stream().filter(RecordId::isInteger).collect(Collectors.toList());
				for (RecordId recordId : ids) {
					this.ids.set(index, recordId.intValue());
					this.versions.set(index, 0);
					this.collection.set(index, (byte) 0);
					this.type.set(index, (short) 0);
					this.schema.set(index, (short) 0);
					index++;
				}

			}

		} finally {
			mechanism.releaseSystemWideWritingPermit();
		}
		LOGGER.info("finished structuring cache using id iterator");
	}
}
