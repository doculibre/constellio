package com.constellio.model.services.records.cache2;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.SolrRecordDTO;
import com.constellio.data.utils.Holder;
import com.constellio.data.utils.LazyIterator;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.RecordCacheType;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache2.ByteArrayRecordDTO.ByteArrayRecordDTOWithIntegerId;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.constellio.model.entities.schemas.MetadataSchemaTypes.LIMIT_OF_TYPES_IN_COLLECTION;
import static com.constellio.model.services.schemas.SchemaUtils.getSchemaTypeCode;
import static java.util.Spliterator.DISTINCT;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.NONNULL;
import static java.util.Spliterators.spliteratorUnknownSize;

/**
 * Memory and read/write efficient datastore
 */
public class RecordsCachesDataStoreCopyBeforeSplitting {

	//Memory structure of records with int id
	//Level 0 : Collection id (max 256 values)
	//Level 1 : Type id (max 1000 values)
	OffHeapIntList[][] ids = new OffHeapIntList[256][];
	OffHeapLongList[][] versions = new OffHeapLongList[256][];
	OffHeapShortList[][] schema = new OffHeapShortList[256][];
	boolean[][] isSummaryCache = new boolean[256][];
	OffHeapByteArrayList[][] summaryCachedData = new OffHeapByteArrayList[256][];
	List<RecordDTO>[][] fullyCachedData = new List[256][];


	//Memory structure of records with string id
	private Map<String, Holder<RecordDTO>> allRecordsWithStringKey = new HashMap<>();
	private List<Holder<RecordDTO>>[] recordsWithStringKeyRegroupedByCollection = new List[256];
	private List<Holder<RecordDTO>>[][] recordsWithStringKeyRegroupedByCollectionAndType = new List[256][];

	private ModelLayerFactory modelLayerFactory;
	private CollectionsListManager collectionsListManager;
	private MetadataSchemasManager schemasManager;

	private DB onMemoryDatabase;

	public RecordsCachesDataStoreCopyBeforeSplitting(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.collectionsListManager = modelLayerFactory.getCollectionsListManager();
		this.schemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.onMemoryDatabase = DBMaker.memoryDirectDB().make();
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


	public void insertWithoutReservingSpaceForPreviousIds(RecordDTO dto) {
		int intId;
		if (dto instanceof ByteArrayRecordDTOWithIntegerId) {
			intId = ((ByteArrayRecordDTOWithIntegerId) dto).getIntId();
		} else {
			intId = CacheRecordDTOUtils.toIntKey(dto.getId());
		}

		if (intId == CacheRecordDTOUtils.KEY_IS_NOT_AN_INT) {
			insertWithStringKey(dto);
		} else {
			insertWithIntKey(intId, dto, false, false);
		}
	}

	public void insert(RecordDTO dto) {

		int intId;
		if (dto instanceof ByteArrayRecordDTOWithIntegerId) {
			intId = ((ByteArrayRecordDTOWithIntegerId) dto).getIntId();
		} else {
			intId = CacheRecordDTOUtils.toIntKey(dto.getId());
		}

		if (intId == CacheRecordDTOUtils.KEY_IS_NOT_AN_INT) {
			insertWithStringKey(dto);
		} else {
			insertWithIntKey(intId, dto, true, false);
		}
	}

	private List<Holder<RecordDTO>> getStringKeyListForType(byte collectionId, short typeId, boolean createIfAbsent,
															boolean isSynchronized) {
		int collectionIndex = collectionId + Byte.MIN_VALUE;

		List<Holder<RecordDTO>>[] collectionTypesRecords = recordsWithStringKeyRegroupedByCollectionAndType[collectionIndex];

		if (collectionTypesRecords == null) {
			if (createIfAbsent) {
				if (isSynchronized) {
					collectionTypesRecords = recordsWithStringKeyRegroupedByCollectionAndType[collectionIndex] = new List[LIMIT_OF_TYPES_IN_COLLECTION];
				} else {
					synchronized (this) {
						getStringKeyListForType(collectionId, typeId, createIfAbsent, true);
					}
				}


			} else {
				return Collections.emptyList();
			}
		}

		List<Holder<RecordDTO>> list = collectionTypesRecords[typeId];

		if (list == null) {
			if (createIfAbsent) {
				if (isSynchronized) {
					list = collectionTypesRecords[typeId] = new ArrayList<>();
				} else {
					synchronized (this) {
						getStringKeyListForType(collectionId, typeId, createIfAbsent, true);
					}
				}


			} else {
				return Collections.emptyList();
			}
		}

		return list;

	}

	private List<Holder<RecordDTO>> getStringKeyListForCollection(byte collectionId, boolean createIfAbsent,
																  boolean isSynchronized) {
		int collectionIndex = collectionId + Byte.MIN_VALUE;

		List<Holder<RecordDTO>> list = recordsWithStringKeyRegroupedByCollection[collectionIndex];

		if (list == null) {
			if (createIfAbsent) {
				if (isSynchronized) {
					list = recordsWithStringKeyRegroupedByCollection[collectionIndex] = new ArrayList<>();
				} else {
					synchronized (this) {
						getStringKeyListForCollection(collectionId, createIfAbsent, true);
					}
				}


			} else {
				return Collections.emptyList();
			}
		}

		return list;

	}

	private void insertWithStringKey(RecordDTO dto) {
		Holder<RecordDTO> recordDTOHolder = allRecordsWithStringKey.get(dto);

		byte collectionId = getCollectionIdOf(dto);
		short typeId = getTypeId(dto);

		if (recordDTOHolder == null) {
			synchronized (this) {
				Holder holder = new Holder(dto);
				allRecordsWithStringKey.put(dto.getId(), holder);
				getStringKeyListForType(collectionId, typeId, true, true).add(holder);
				getStringKeyListForCollection(collectionId, true, true).add(holder);
			}
		} else {
			recordDTOHolder.set(dto);
		}
	}


	public void remove(RecordDTO dto) {
		int intId = CacheRecordDTOUtils.toIntKey(dto.getId());

		if (intId == CacheRecordDTOUtils.KEY_IS_NOT_AN_INT) {
			Holder<RecordDTO> recordDTOHolder = allRecordsWithStringKey.get(dto.getId());
			if (recordDTOHolder != null) {
				recordDTOHolder.set(null);
				synchronized (this) {
					allRecordsWithStringKey.remove(dto.getId());
				}
			}

		} else {
			remove(intId, dto);
		}

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

	public void remove(int id, RecordDTO dto) {

		short typeId = getTypeId(dto);
		int collectionIndex = getCollectionIndexOf(dto);

		synchronized (this) {
			OffHeapIntList[] collectionTypesRecordsIds = ids[collectionIndex];
			if (collectionTypesRecordsIds == null) {
				return;
			}

			OffHeapIntList typeRecordsId = collectionTypesRecordsIds[typeId];
			if (typeRecordsId == null) {
				return;
			}

			int index = typeRecordsId.binarySearch(id);
			if (index >= 0) {
				OffHeapLongList typeRecordsVersion = versions[collectionIndex][typeId];
				OffHeapShortList typeRecordsSchema = schema[collectionIndex][typeId];


				typeRecordsVersion.set(index, 0L);
				typeRecordsSchema.set(index, (short) 0);
				if (dto instanceof ByteArrayRecordDTO) {
					OffHeapByteArrayList typeRecordsData = summaryCachedData[collectionIndex][typeId];
					typeRecordsData.set(index, null);
				} else {
					List<RecordDTO> typeRecordsData = fullyCachedData[collectionIndex][typeId];
					typeRecordsData.set(index, null);
				}

			}
		}

	}

	private void insertWithIntKey(int id, RecordDTO dto, boolean holdSpaceForPreviousIds,
								  boolean currentlySynchronized) {
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


		OffHeapIntList[] collectionTypesRecordsIds = ids[collectionIndex];
		if (collectionTypesRecordsIds == null) {
			synchronized (this) {
				if (ids[collectionIndex] == null) {
					collectionTypesRecordsIds = ids[collectionIndex] = new OffHeapIntList[LIMIT_OF_TYPES_IN_COLLECTION];
					versions[collectionIndex] = new OffHeapLongList[LIMIT_OF_TYPES_IN_COLLECTION];
					schema[collectionIndex] = new OffHeapShortList[LIMIT_OF_TYPES_IN_COLLECTION];
					fullyCachedData[collectionIndex] = new List[LIMIT_OF_TYPES_IN_COLLECTION];
					summaryCachedData[collectionIndex] = new OffHeapByteArrayList[LIMIT_OF_TYPES_IN_COLLECTION];
					isSummaryCache[collectionIndex] = new boolean[LIMIT_OF_TYPES_IN_COLLECTION];
				}
			}
		}

		OffHeapIntList typeRecordsId = collectionTypesRecordsIds[typeId];
		if (typeRecordsId == null) {
			synchronized (this) {
				if (ids[collectionIndex][typeId] == null) {
					typeRecordsId = ids[collectionIndex][typeId] = new OffHeapIntList();
					versions[collectionIndex][typeId] = new OffHeapLongList();
					schema[collectionIndex][typeId] = new OffHeapShortList();


					MetadataSchemaType schemaType = schemasManager.get(collectionId, typeId);
					if (schemaType.getCacheType().isSummaryCache()) {
						isSummaryCache[collectionIndex][typeId] = true;
						summaryCachedData[collectionIndex][typeId] = new OffHeapByteArrayList();
					}

					if (schemaType.getCacheType() == RecordCacheType.FULLY_CACHED) {
						isSummaryCache[collectionIndex][typeId] = false;
						fullyCachedData[collectionIndex][typeId] = new ArrayList<>();
					}

				}

			}
		}

		int index = typeRecordsId.binarySearch(id);
		if (index < 0) {
			if (currentlySynchronized) {
				ajustArraysForNewId(id, dto, holdSpaceForPreviousIds, typeId, schemaId, collectionIndex, typeRecordsId);

			} else {
				synchronized (this) {
					insertWithIntKey(id, dto, holdSpaceForPreviousIds, true);
				}

			}
		} else {
			OffHeapLongList typeRecordsVersion = versions[collectionIndex][typeId];
			OffHeapShortList typeRecordsSchema = schema[collectionIndex][typeId];


			typeRecordsVersion.set(index, dto.getVersion());
			typeRecordsSchema.set(index, schemaId);
			if (dto instanceof ByteArrayRecordDTO) {
				OffHeapByteArrayList typeRecordsData = summaryCachedData[collectionIndex][typeId];
				typeRecordsData.set(index, ((ByteArrayRecordDTO) dto).data);
			} else {
				List<RecordDTO> typeRecordsData = fullyCachedData[collectionIndex][typeId];
				typeRecordsData.set(index, dto);
			}

		}
	}

	private void ajustArraysForNewId(int id, RecordDTO dto, boolean holdSpaceForPreviousIds, short typeId,
									 short schemaId, int collectionIndex, OffHeapIntList typeRecordsId) {
		OffHeapLongList typeRecordsVersion = versions[collectionIndex][typeId];
		OffHeapShortList typeRecordsSchema = schema[collectionIndex][typeId];
		OffHeapByteArrayList typeRecordsSummaryData = null;
		List<RecordDTO> typeRecordsData = null;

		if (dto instanceof SolrRecordDTO) {
			typeRecordsData = fullyCachedData[collectionIndex][typeId];
		} else {
			typeRecordsSummaryData = summaryCachedData[collectionIndex][typeId];
		}


		if (typeRecordsId.isEmpty() || typeRecordsId.getLast() < id) {
			if (holdSpaceForPreviousIds) {

				int last = typeRecordsId.isEmpty() ? 0 : typeRecordsId.getLast();
				while (last < id) {
					int index = typeRecordsId.size();
					last++;
					typeRecordsId.set(index, last);
					if (last == id) {
						typeRecordsVersion.set(index, dto.getVersion());
						typeRecordsSchema.set(index, schemaId);
						if (typeRecordsSummaryData != null) {
							typeRecordsSummaryData.set(index, ((ByteArrayRecordDTO) dto).data);
						} else {
							typeRecordsData.add(index, dto);
						}


					} else {
						typeRecordsVersion.set(index, (short) 0);
						typeRecordsSchema.set(index, (short) 0);
						if (typeRecordsSummaryData != null) {
							typeRecordsSummaryData.set(index, null);
						} else {
							typeRecordsData.add(index, null);
						}

					}

				}

			} else {
				int index = typeRecordsId.size();
				typeRecordsId.set(index, id);
				typeRecordsVersion.set(index, dto.getVersion());
				typeRecordsSchema.set(index, schemaId);
				if (typeRecordsSummaryData != null) {
					typeRecordsSummaryData.set(index, ((ByteArrayRecordDTO) dto).data);
				} else {
					typeRecordsData.add(index, dto);
				}
			}
		} else {

			int insertAtIndex = -1;
			for (int i = 0; i < typeRecordsId.size(); i++) {
				if (typeRecordsId.get(i) < id) {
					insertAtIndex = i;
				} else {
					break;
				}
			}
			insertAtIndex++;

			typeRecordsId.insertValueShiftingAllFollowingValues(insertAtIndex, id);
			typeRecordsVersion.insertValueShiftingAllFollowingValues(insertAtIndex, dto.getVersion());
			typeRecordsSchema.insertValueShiftingAllFollowingValues(insertAtIndex, schemaId);

			if (typeRecordsSummaryData != null) {
				typeRecordsSummaryData.insertValueShiftingAllFollowingValues(insertAtIndex, ((ByteArrayRecordDTO) dto).data);
			} else {
				typeRecordsData.add(insertAtIndex, dto);
			}

		}
	}


	public RecordDTO get(String id) {
		int intId = CacheRecordDTOUtils.toIntKey(id);

		if (intId == CacheRecordDTOUtils.KEY_IS_NOT_AN_INT) {
			Holder<RecordDTO> recordDTOHolder = allRecordsWithStringKey.get(id);
			return recordDTOHolder == null ? null : recordDTOHolder.get();

		} else {
			return get(intId);
		}
	}

	public RecordDTO get(byte collectionId, String id) {
		int intId = CacheRecordDTOUtils.toIntKey(id);

		if (intId == CacheRecordDTOUtils.KEY_IS_NOT_AN_INT) {
			Holder<RecordDTO> recordDTOHolder = allRecordsWithStringKey.get(id);
			return recordDTOHolder == null ? null : recordDTOHolder.get();

		} else {
			return get(collectionId, intId);
		}
	}

	public RecordDTO get(byte collectionId, short typeId, String id) {
		int intId = CacheRecordDTOUtils.toIntKey(id);

		if (intId == CacheRecordDTOUtils.KEY_IS_NOT_AN_INT) {
			Holder<RecordDTO> recordDTOHolder = allRecordsWithStringKey.get(id);
			return recordDTOHolder == null ? null : recordDTOHolder.get();

		} else {
			return get(collectionId, typeId, intId);
		}
	}

	public RecordDTO get(int id) {

		for (int collectionIndex = 0; collectionIndex < ids.length; collectionIndex++) {
			OffHeapIntList[] typesIds = ids[collectionIndex];
			if (typesIds != null) {
				for (int typeIndex = 0; typeIndex < ids.length; typeIndex++) {
					OffHeapIntList typeIds = typesIds[typeIndex];

					if (typeIds != null) {
						int index = typeIds.binarySearch(id);

						if (index != -1) {
							return getIntIdAtLocation(id, collectionIndex, typeIndex, index);
						}
					}
				}
			}
		}

		return null;
	}

	public RecordDTO get(byte collectionId, int id) {

		int collectionIndex = ((int) collectionId) - Byte.MIN_VALUE;
		OffHeapIntList[] typesIds = ids[collectionIndex];
		if (typesIds != null) {
			for (int typeIndex = 0; typeIndex < ids.length; typeIndex++) {
				OffHeapIntList typeIds = typesIds[typeIndex];

				if (typeIds != null) {
					int index = typeIds.binarySearch(id);

					if (index != -1) {
						return getIntIdAtLocation(id, collectionIndex, typeIndex, index);
					}
				}
			}
		}

		return null;
	}

	public RecordDTO get(byte collectionId, short typeId, int id) {

		int collectionIndex = ((int) collectionId) - Byte.MIN_VALUE;
		int typeIndex = (int) typeId;
		OffHeapIntList[] typesIds = ids[collectionIndex];
		if (typesIds != null) {
			OffHeapIntList typeIds = typesIds[typeIndex];

			if (typeIds != null) {
				int index = typeIds.binarySearch(id);

				if (index != -1) {
					return getIntIdAtLocation(id, collectionIndex, typeIndex, index);
				}
			}
		}

		return null;
	}

	public Stream<RecordDTO> stream() {
		return StreamSupport.stream(spliteratorUnknownSize(iterator(), DISTINCT + NONNULL + IMMUTABLE), false);
	}

	public Stream<RecordDTO> stream(byte collection) {
		return StreamSupport.stream(spliteratorUnknownSize(iterator(collection), DISTINCT + NONNULL + IMMUTABLE), false);
	}

	public Stream<RecordDTO> stream(byte collection, short schemaType) {
		return StreamSupport.stream(spliteratorUnknownSize(iterator(collection, schemaType), DISTINCT + NONNULL + IMMUTABLE), false);
	}

	public Stream<RecordDTO> stream(byte collectionId, List<String> ids) {
		return ids.stream().map((id) -> get(collectionId, id)).filter(Objects::nonNull);
	}

	public synchronized void invalidate(Predicate<RecordDTO> predicate) {
		stream().filter(predicate).forEachOrdered(this::remove);
	}

	public synchronized void invalidate(byte collection, Predicate<RecordDTO> predicate) {
		stream(collection).filter(predicate).forEachOrdered(this::remove);
	}

	public synchronized void invalidate(byte collection, short schemaType, Predicate<RecordDTO> predicate) {
		stream(collection, schemaType).filter(predicate).forEachOrdered(this::remove);
	}


	private RecordDTO getIntIdAtLocation(int collectionIndex, int typeIndex, int listIndex) {
		int id = ids[collectionIndex][typeIndex].get(listIndex);
		return getIntIdAtLocation(id, collectionIndex, typeIndex, listIndex);
	}


	private RecordDTO getIntIdAtLocation(int id, int collectionIndex, int typeIndex, int listIndex) {


		long version = versions[collectionIndex][typeIndex].get(listIndex);
		short typeId = (short) typeIndex;
		byte collectionId = (byte) (collectionIndex + Byte.MIN_VALUE);
		short schemaId = schema[collectionIndex][typeIndex].get(listIndex);
		if (schemaId == 0) {
			return null;
		}
		MetadataSchemaType type = schemasManager.getSchemaTypes(collectionId).getSchemaType(typeId);
		MetadataSchema schema = type.getSchema(schemaId);

		if (isSummaryCache[collectionIndex][typeId]) {
			OffHeapByteArrayList byteArrays = summaryCachedData[collectionIndex][typeIndex];//.get(listIndex);
			return new ByteArrayRecordDTOWithIntegerId(id, modelLayerFactory.getMetadataSchemasManager(), version, true,
					schema.getCollection(), collectionId, type.getCode(), typeId, schema.getCode(), schemaId,
					byteArrays.get(listIndex).toArray());

		} else {
			List<RecordDTO> dtos = fullyCachedData[collectionIndex][typeIndex];
			return dtos.get(listIndex);
		}
	}

	public Iterator<RecordDTO> iterator() {

		return new LazyIterator<RecordDTO>() {

			int collection = 0;
			int schemaType = 0;
			int index = 0;
			boolean intIdsFinished;

			@Override
			protected RecordDTO getNextOrNull() {

				if (!intIdsFinished) {
					while (collection < ids.length) {
						if (ids[collection] == null) {
							collection++;
						} else {
							if (ids[collection][schemaType] == null || ids[collection][schemaType].size() <= index) {
								schemaType++;
								if (schemaType >= LIMIT_OF_TYPES_IN_COLLECTION) {
									schemaType = 0;
									index = 0;
									collection++;
								}
							} else {
								RecordDTO dto = getIntIdAtLocation(collection, schemaType, index++);
								if (dto != null) {
									return dto;
								}
							}


						}

					}

					intIdsFinished = true;
					collection = 0;
					schemaType = 0;
					index = 0;
				}

				while (collection < ids.length) {
					if (recordsWithStringKeyRegroupedByCollectionAndType[collection] == null) {
						collection++;
					} else {
						if (recordsWithStringKeyRegroupedByCollectionAndType[collection][schemaType] == null
							|| recordsWithStringKeyRegroupedByCollectionAndType[collection][schemaType].size() <= index) {
							schemaType++;
							if (schemaType >= LIMIT_OF_TYPES_IN_COLLECTION) {
								schemaType = 0;
								index = 0;
								collection++;
							}
						} else {
							Holder<RecordDTO> recordDTOHolder = recordsWithStringKeyRegroupedByCollectionAndType[collection][schemaType].get(index++);
							if (recordDTOHolder != null) {
								RecordDTO recordDTO = recordDTOHolder.get();
								if (recordDTO != null) {
									return recordDTO;
								}
							}
						}


					}

				}


				return null;

			}
		};
	}


	public Iterator<RecordDTO> iterator(byte collectionId) {

		List<Holder<RecordDTO>> collectionHoldersOfRecordsWithStringKey
				= getStringKeyListForCollection(collectionId, false, false);

		int collectionIndex = collectionId - Byte.MIN_VALUE;

		return new LazyIterator<RecordDTO>() {

			int schemaTypeIndex = 0;
			int index = 0;
			boolean intIdsFinished;

			@Override
			protected RecordDTO getNextOrNull() {

				if (!intIdsFinished) {
					if (ids[collectionIndex] != null) {
						while (schemaTypeIndex < LIMIT_OF_TYPES_IN_COLLECTION) {
							if (ids[collectionIndex][schemaTypeIndex] == null || ids[collectionIndex][schemaTypeIndex].size() <= index) {
								schemaTypeIndex++;

							} else {
								RecordDTO dto = getIntIdAtLocation(collectionIndex, schemaTypeIndex, index++);
								if (dto != null) {
									return dto;
								}
							}

						}
					}
					intIdsFinished = true;
					index = 0;
				}

				while (index < collectionHoldersOfRecordsWithStringKey.size()) {
					Holder<RecordDTO> recordDTOHolder = collectionHoldersOfRecordsWithStringKey.get(index++);
					if (recordDTOHolder != null) {
						RecordDTO recordDTO = recordDTOHolder.get();
						if (recordDTO != null) {
							return recordDTO;
						}
					}
				}

				return null;

			}
		};
	}


	public Iterator<RecordDTO> iterator(byte collectionId, short typeId) {

		List<Holder<RecordDTO>> collectionHoldersOfRecordsWithStringKey
				= getStringKeyListForType(collectionId, typeId, false, false);

		int collectionIndex = collectionId - Byte.MIN_VALUE;
		int typeIndex = (int) typeId;

		return new LazyIterator<RecordDTO>() {

			int index = 0;
			boolean intIdsFinished;

			@Override
			protected RecordDTO getNextOrNull() {

				if (!intIdsFinished) {
					if (ids[collectionIndex] == null || ids[collectionIndex][typeIndex] == null) {
						return null;
					}

					while (index < ids[collectionIndex][typeIndex].size()) {
						RecordDTO dto = getIntIdAtLocation(collectionIndex, typeIndex, index++);
						if (dto != null) {
							return dto;
						}

					}

					intIdsFinished = true;
					index = 0;
				}

				while (index < collectionHoldersOfRecordsWithStringKey.size()) {
					Holder<RecordDTO> recordDTOHolder = collectionHoldersOfRecordsWithStringKey.get(index++);
					if (recordDTOHolder != null) {
						RecordDTO recordDTO = recordDTOHolder.get();
						if (recordDTO != null) {
							return recordDTO;
						}
					}
				}

				return null;

			}
		};
	}

}
