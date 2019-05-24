package com.constellio.model.services.records.cache2;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.SolrRecordDTO;
import com.constellio.data.utils.LazyIterator;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.RecordCacheType;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache2.ByteArrayRecordDTO.ByteArrayRecordDTOWithIntegerId;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.constellio.model.entities.schemas.MetadataSchemaTypes.LIMIT_OF_TYPES_IN_COLLECTION;
import static com.constellio.model.services.schemas.SchemaUtils.getSchemaTypeCode;

/**
 * There is two levels of synchronizations
 */
public class IntegerIdsMemoryEfficientRecordsCachesDataStore {

	//Memory structure of records with int id
	//Level 0 : Collection id (max 256 values)
	//Level 1 : Type id (max 1000 values)
	OffHeapIntList[][] ids = new OffHeapIntList[256][];
	OffHeapLongList[][] versions = new OffHeapLongList[256][];
	OffHeapShortList[][] schema = new OffHeapShortList[256][];
	boolean[][] isSummaryCache = new boolean[256][];
	OffHeapByteArrayList[][] summaryCachedData = new OffHeapByteArrayList[256][];
	List<RecordDTO>[][] fullyCachedData = new List[256][];

	private ModelLayerFactory modelLayerFactory;
	private CollectionsListManager collectionsListManager;
	private MetadataSchemasManager schemasManager;


	public IntegerIdsMemoryEfficientRecordsCachesDataStore(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.collectionsListManager = modelLayerFactory.getCollectionsListManager();
		this.schemasManager = modelLayerFactory.getMetadataSchemasManager();
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

	void remove(int id, RecordDTO dto) {

		short typeId = getTypeId(dto);
		int collectionIndex = getCollectionIndexOf(dto);


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
			synchronized (typeRecordsId) {
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

	void insert(int id, RecordDTO dto, boolean holdSpaceForPreviousIds,
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
			if (currentlySynchronized) {
				if (ids[collectionIndex] == null) {
					collectionTypesRecordsIds = ids[collectionIndex] = new OffHeapIntList[LIMIT_OF_TYPES_IN_COLLECTION];
					versions[collectionIndex] = new OffHeapLongList[LIMIT_OF_TYPES_IN_COLLECTION];
					schema[collectionIndex] = new OffHeapShortList[LIMIT_OF_TYPES_IN_COLLECTION];
					fullyCachedData[collectionIndex] = new List[LIMIT_OF_TYPES_IN_COLLECTION];
					summaryCachedData[collectionIndex] = new OffHeapByteArrayList[LIMIT_OF_TYPES_IN_COLLECTION];
					isSummaryCache[collectionIndex] = new boolean[LIMIT_OF_TYPES_IN_COLLECTION];
				}
			} else {
				synchronized (this) {
					insert(id, dto, holdSpaceForPreviousIds, true);
					return;
				}
			}
		}

		OffHeapIntList typeRecordsId = collectionTypesRecordsIds[typeId];
		if (typeRecordsId == null) {
			synchronized (this) {
				if (ids[collectionIndex][typeId] == null) {
					if (currentlySynchronized) {
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
					} else {
						synchronized (this) {
							insert(id, dto, holdSpaceForPreviousIds, true);
							return;
						}
					}
				}

			}
		}


		int index = typeRecordsId.binarySearch(id);

		boolean setAtIndex = true;
		if (index < 0) {
			index = typeRecordsId.binarySearch(id);
			synchronized (typeRecordsId) {
				if (index < 0) {

					ajustArraysForNewId(id, dto, holdSpaceForPreviousIds, typeId, schemaId, collectionIndex, typeRecordsId);
					setAtIndex = false;
				} else {

					setAtIndex = true;
				}

			}
		}

		if (setAtIndex) {
			OffHeapLongList typeRecordsVersion = versions[collectionIndex][typeId];
			OffHeapShortList typeRecordsSchema = schema[collectionIndex][typeId];

			if (dto instanceof ByteArrayRecordDTO) {
				OffHeapByteArrayList typeRecordsData = summaryCachedData[collectionIndex][typeId];
				typeRecordsData.set(index, ((ByteArrayRecordDTO) dto).data);
			} else {
				List<RecordDTO> typeRecordsData = fullyCachedData[collectionIndex][typeId];
				typeRecordsData.set(index, dto);
			}
			typeRecordsVersion.set(index, dto.getVersion());
			typeRecordsSchema.set(index, schemaId);
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

		int lastId = typeRecordsId.getLast();

		if (typeRecordsId.isEmpty() || lastId < id) {
			if (holdSpaceForPreviousIds) {

				int last = Math.max(typeRecordsId.isEmpty() ? 0 : typeRecordsId.getLast(), id - 5);
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

	RecordDTO get(int id) {

		for (int collectionIndex = 0; collectionIndex < ids.length; collectionIndex++) {
			OffHeapIntList[] typesIds = ids[collectionIndex];
			if (typesIds != null) {
				for (int typeIndex = 0; typeIndex < ids.length; typeIndex++) {
					OffHeapIntList typeIds = typesIds[typeIndex];

					if (typeIds != null) {
						int index = typeIds.binarySearch(id);

						if (index != -1) {
							return get(id, collectionIndex, typeIndex, index);
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
						return get(id, collectionIndex, typeIndex, index);
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
					return get(id, collectionIndex, typeIndex, index);
				}
			}
		}

		return null;
	}

	private RecordDTO get(int collectionIndex, int typeIndex, int listIndex) {
		int id = ids[collectionIndex][typeIndex].get(listIndex);
		return get(id, collectionIndex, typeIndex, listIndex);
	}


	private RecordDTO get(int id, int collectionIndex, int typeIndex, int listIndex) {

		short typeId = (short) typeIndex;
		short schemaId = schema[collectionIndex][typeIndex].get(listIndex);
		if (schemaId == 0) {
			return null;
		}

		if (isSummaryCache[collectionIndex][typeId]) {
			byte collectionId = (byte) (collectionIndex + Byte.MIN_VALUE);
			long version = versions[collectionIndex][typeIndex].get(listIndex);
			return getSummaryCachedRecordDTO(id, summaryCachedData[collectionIndex][typeIndex], listIndex, version, typeId, collectionId, schemaId);

		} else {
			List<RecordDTO> dtos = fullyCachedData[collectionIndex][typeIndex];
			return dtos.get(listIndex);
		}
	}

	@NotNull
	private RecordDTO getSummaryCachedRecordDTO(int id, OffHeapByteArrayList byteArrays, int listIndex, long version,
												short typeId, byte collectionId, short schemaId) {
		MetadataSchemaType type = schemasManager.get(collectionId, typeId);
		MetadataSchema schema = type.getSchema(schemaId);
		byte[] data = byteArrays.get(listIndex).toArray();
		return new ByteArrayRecordDTOWithIntegerId(id, modelLayerFactory.getMetadataSchemasManager(), version, true,
				schema.getCollection(), collectionId, type.getCode(), typeId, schema.getCode(), schemaId, data);
	}

	public Iterator<RecordDTO> iterator() {

		return new LazyIterator<RecordDTO>() {

			int collection = 0;
			int schemaType = 0;
			int index = 0;

			@Override
			protected RecordDTO getNextOrNull() {

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
							RecordDTO dto = get(collection, schemaType, index++);
							if (dto != null) {
								return dto;
							}
						}


					}

				}

				collection = 0;
				schemaType = 0;
				index = 0;

				return null;

			}
		};
	}


	public Iterator<RecordDTO> iterator(byte collectionId) {

		int collectionIndex = collectionId - Byte.MIN_VALUE;

		return new LazyIterator<RecordDTO>() {

			int schemaTypeIndex = 0;
			int index = 0;

			@Override
			protected RecordDTO getNextOrNull() {

				if (ids[collectionIndex] != null) {
					while (schemaTypeIndex < LIMIT_OF_TYPES_IN_COLLECTION) {
						if (ids[collectionIndex][schemaTypeIndex] == null || ids[collectionIndex][schemaTypeIndex].size() <= index) {
							schemaTypeIndex++;

						} else {
							RecordDTO dto = get(collectionIndex, schemaTypeIndex, index++);
							if (dto != null) {
								return dto;
							}
						}

					}
				}
				index = 0;


				return null;

			}
		};
	}


	public Iterator<RecordDTO> iterator(byte collectionId, short typeId) {

		int collectionIndex = collectionId - Byte.MIN_VALUE;
		int typeIndex = (int) typeId;

		return new LazyIterator<RecordDTO>() {

			int index = 0;

			@Override
			protected RecordDTO getNextOrNull() {

				if (ids[collectionIndex] == null || ids[collectionIndex][typeIndex] == null) {
					return null;
				}

				while (index < ids[collectionIndex][typeIndex].size()) {
					RecordDTO dto = get(collectionIndex, typeIndex, index++);
					if (dto != null) {
						return dto;
					}

				}

				index = 0;


				return null;

			}
		};
	}
}
