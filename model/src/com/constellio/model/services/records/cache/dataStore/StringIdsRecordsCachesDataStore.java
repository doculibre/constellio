package com.constellio.model.services.records.cache.dataStore;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.data.dao.dto.records.SolrRecordDTO;
import com.constellio.data.utils.CacheStat;
import com.constellio.data.utils.Holder;
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.LazyIterator;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache.ByteArrayRecordDTO;
import com.constellio.model.services.records.cache.ByteArrayRecordDTO.ByteArrayRecordDTOWithStringId;
import com.constellio.model.services.records.cache.PersistedSortValuesServices.SortValue;
import com.constellio.model.services.schemas.MetadataSchemasManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.constellio.model.entities.schemas.MetadataSchemaTypes.LIMIT_OF_TYPES_IN_COLLECTION;
import static com.constellio.model.services.schemas.SchemaUtils.getSchemaTypeCode;
import static java.util.Spliterator.DISTINCT;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.NONNULL;
import static java.util.Spliterators.spliteratorUnknownSize;

public class StringIdsRecordsCachesDataStore {

	private Map<String, Holder<RecordDTO>> allRecordsWithStringKey = new HashMap<>();
	private List<Holder<RecordDTO>>[] recordsWithStringKeyRegroupedByCollection = new List[256];
	private List<Holder<RecordDTO>>[][] recordsWithStringKeyRegroupedByCollectionAndType = new List[256][];
	private Map<Integer, Integer> mainSortValues = new HashMap<>();

	private ModelLayerFactory modelLayerFactory;
	private CollectionsListManager collectionsListManager;
	private MetadataSchemasManager schemasManager;


	public StringIdsRecordsCachesDataStore(ModelLayerFactory modelLayerFactory) {
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

	private List<Holder<RecordDTO>> getStringKeyListForType(byte collectionId, short typeId, boolean createIfAbsent,
															boolean isSynchronized) {
		int collectionIndex = collectionId - Byte.MIN_VALUE;

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
		int collectionIndex = collectionId - Byte.MIN_VALUE;

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

	void insert(RecordDTO dto) {
		Holder<RecordDTO> recordDTOHolder = allRecordsWithStringKey.get(dto.getId());

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

		if (dto.getMainSortValue() != RecordDTO.MAIN_SORT_UNCHANGED) {
			synchronized (mainSortValues) {
				mainSortValues.put(RecordId.id(dto.getId()).intValue(), dto.getMainSortValue());
			}
		}
	}


	void remove(String id) {
		Holder<RecordDTO> recordDTOHolder = allRecordsWithStringKey.get(id);
		if (recordDTOHolder != null) {
			recordDTOHolder.set(null);
			synchronized (this) {
				allRecordsWithStringKey.remove(id);
			}
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

	public RecordDTO get(String id) {
		Holder<RecordDTO> recordDTOHolder = allRecordsWithStringKey.get(id);
		RecordDTO dto = recordDTOHolder == null ? null : recordDTOHolder.get();
		return transform(dto);
	}

	private RecordDTO transform(RecordDTO dto) {
		if (dto == null) {
			return null;
		} else if (dto instanceof ByteArrayRecordDTOWithStringId) {
			int mainSortValue = mainSortValues.get(RecordId.id(dto.getId()).intValue());
			if (mainSortValue > 0) {
				((ByteArrayRecordDTOWithStringId) dto).setMainSortValue(mainSortValue);
			}

		} else if (dto instanceof SolrRecordDTO) {
			int mainSortValue = mainSortValues.get(RecordId.id(dto.getId()).intValue());
			if (mainSortValue > 0) {
				return ((SolrRecordDTO) dto).withMainSortValue(mainSortValue);
			}
		}
		return dto;
	}

	public Iterator<RecordDTO> iterator() {

		return new LazyIterator<RecordDTO>() {

			int collection = 0;
			int schemaType = 0;
			int index = 0;

			@Override
			protected RecordDTO getNextOrNull() {

				while (collection < 256) {
					if (recordsWithStringKeyRegroupedByCollectionAndType[collection] == null) {
						collection++;
					} else {
						if (recordsWithStringKeyRegroupedByCollectionAndType[collection][schemaType] == null
							|| recordsWithStringKeyRegroupedByCollectionAndType[collection][schemaType].size() <= index) {
							schemaType++;
							index = 0;
							if (schemaType >= LIMIT_OF_TYPES_IN_COLLECTION) {
								schemaType = 0;
								collection++;
							}
						} else {
							Holder<RecordDTO> recordDTOHolder = recordsWithStringKeyRegroupedByCollectionAndType[collection][schemaType].get(index++);
							if (recordDTOHolder != null) {
								RecordDTO recordDTO = recordDTOHolder.get();
								if (recordDTO != null) {
									return transform(recordDTO);
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

			int index = 0;

			@Override
			protected RecordDTO getNextOrNull() {

				while (index < collectionHoldersOfRecordsWithStringKey.size()) {
					Holder<RecordDTO> recordDTOHolder = collectionHoldersOfRecordsWithStringKey.get(index++);
					if (recordDTOHolder != null) {
						RecordDTO recordDTO = recordDTOHolder.get();
						if (recordDTO != null) {
							return transform(recordDTO);
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

			@Override
			protected RecordDTO getNextOrNull() {

				while (index < collectionHoldersOfRecordsWithStringKey.size()) {
					Holder<RecordDTO> recordDTOHolder = collectionHoldersOfRecordsWithStringKey.get(index++);
					if (recordDTOHolder != null) {
						RecordDTO recordDTO = recordDTOHolder.get();
						if (recordDTO != null) {
							return transform(recordDTO);
						}
					}
				}

				return null;

			}
		};
	}


	public Iterator<RecordDTO> iterator(byte collectionId, short typeId, short metadataId, Object value) {

		List<Holder<RecordDTO>> collectionHoldersOfRecordsWithStringKey
				= getStringKeyListForType(collectionId, typeId, false, false);

		int collectionIndex = collectionId - Byte.MIN_VALUE;
		int typeIndex = (int) typeId;

		String metadataDataStoreCode = schemasManager.getMetadata(collectionId, typeId, metadataId).getDataStoreCode();

		return new LazyIterator<RecordDTO>() {

			int index = 0;

			@Override
			protected RecordDTO getNextOrNull() {

				while (index < collectionHoldersOfRecordsWithStringKey.size()) {
					Holder<RecordDTO> recordDTOHolder = collectionHoldersOfRecordsWithStringKey.get(index++);
					if (recordDTOHolder != null) {
						RecordDTO recordDTO = recordDTOHolder.get();
						if (recordDTO != null && LangUtils.isEqual(recordDTO.getFields().get(metadataDataStoreCode), value)) {
							return transform(recordDTO);
						}
					}
				}

				return null;

			}
		};
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

	public synchronized void invalidate(byte collection, Predicate<RecordDTO> predicate) {
		List<String> idsToDelete = new ArrayList<>();
		stream(collection).filter(predicate).forEach((dto) -> idsToDelete.add(dto.getId()));
		idsToDelete.stream().forEach((id -> {
			this.allRecordsWithStringKey.remove(id).set(null);
		}));
	}

	public synchronized void invalidate(byte collection, short schemaType, Predicate<RecordDTO> predicate) {
		List<String> idsToDelete = new ArrayList<>();
		stream(collection, schemaType).filter(predicate).forEach((dto) -> idsToDelete.add(dto.getId()));
		idsToDelete.stream().forEach((id -> this.allRecordsWithStringKey.remove(id).set(null)));
	}

	public synchronized void invalidateAll(byte collectionId, short schemaTypeId) {
		MetadataSchemaType schemaType = schemasManager.getSchemaTypes(collectionId).getSchemaType(schemaTypeId);

		int collectionIndex = collectionId - Byte.MIN_VALUE;
		List<Holder<RecordDTO>> holders = recordsWithStringKeyRegroupedByCollection[collectionIndex];

		if (holders != null) {

			Iterator<Holder<RecordDTO>> collectionHoldersDTO = holders.iterator();

			while (collectionHoldersDTO.hasNext()) {
				Holder<RecordDTO> recordDTOHolder = collectionHoldersDTO.next();

				if (recordDTOHolder.get() == null) {
					collectionHoldersDTO.remove();

				} else if (recordDTOHolder.get().getSchemaCode().startsWith(schemaType.getCode() + "_")) {
					this.allRecordsWithStringKey.remove(recordDTOHolder.get().getId());
					collectionHoldersDTO.remove();
				}
			}
		}

		List<Holder<RecordDTO>>[] typesHolders = recordsWithStringKeyRegroupedByCollectionAndType[collectionIndex];
		if (typesHolders != null) {
			typesHolders[schemaTypeId] = new ArrayList<>();
		}

	}


	public synchronized void invalidateAll(byte collectionId) {

		int collectionIndex = collectionId - Byte.MIN_VALUE;

		List<Holder<RecordDTO>> holders = recordsWithStringKeyRegroupedByCollection[collectionIndex];

		if (holders != null) {
			for (Holder<RecordDTO> recordDTOHolder : holders) {
				if (recordDTOHolder.get() != null) {
					this.allRecordsWithStringKey.remove(recordDTOHolder.get().getId());
				}
			}
		}

		if (recordsWithStringKeyRegroupedByCollection[collectionIndex] != null) {
			recordsWithStringKeyRegroupedByCollection[collectionIndex].clear();
		}

		List<Holder<RecordDTO>>[] typesHolders = recordsWithStringKeyRegroupedByCollectionAndType[collectionIndex];
		if (typesHolders != null) {
			for (int i = 0; i < typesHolders.length; i++) {
				List<Holder<RecordDTO>> typeHolders = typesHolders[i];

				if (typeHolders != null) {
					typesHolders[i] = new ArrayList<>();
				}
			}
		}

	}

	public List<CacheStat> compileMemoryConsumptionStats() {
		List<CacheStat> stats = new ArrayList<>();

		if (!allRecordsWithStringKey.isEmpty()) {
			stats.add(new CacheStat("!!! Records with string ids are not calculated : " + allRecordsWithStringKey.size(), 1, 0));
		}
		return stats;
	}

	public void setRecordsMainSortValue(List<?> recordIds) {
		synchronized (mainSortValues) {
			for (int i = 0; i < recordIds.size(); i++) {
				Object o = recordIds.get(i);
				RecordId recordId = (o instanceof RecordId) ? (RecordId) o : ((Supplier<RecordId>) o).get();
				if (!recordId.isInteger()) {
					mainSortValues.put(recordId.intValue(), i * 2 + 1);
				}
			}
		}
	}

	public List<RecordDTO> list(byte collectionId, short typeId) {
		List<RecordDTO> list = null;

		List<Holder<RecordDTO>> collectionHoldersOfRecordsWithStringKey
				= getStringKeyListForType(collectionId, typeId, false, false);

		for (int index = 0; index < collectionHoldersOfRecordsWithStringKey.size(); index++) {
			Holder<RecordDTO> recordDTOHolder = collectionHoldersOfRecordsWithStringKey.get(index++);
			if (recordDTOHolder != null) {
				RecordDTO recordDTO = recordDTOHolder.get();
				if (recordDTO != null) {
					if (list == null) {
						list = new ArrayList<>();
					}
					list.add(transform(recordDTO));
				}
			}
		}


		return list == null ? Collections.emptyList() : list;
	}

	public void setRecordsMainSortValueComparingValues(List<SortValue> sortValues,
													   ToIntFunction<RecordDTO> valueHascodeFunction) {
		//Records with non-standard ids has no sorts
	}
}
