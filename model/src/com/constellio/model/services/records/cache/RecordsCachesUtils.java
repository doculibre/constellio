package com.constellio.model.services.records.cache;

import com.constellio.data.utils.KeyLongMap;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache.CompiledDTOStats.CompiledCollectionDTOStats;
import com.constellio.model.services.records.cache.CompiledDTOStats.CompiledSchemaTypeDTOStats;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

import static com.constellio.data.utils.LangUtils.humanReadableByteCount;

public class RecordsCachesUtils {

	public static CacheInsertionStatus evaluateCacheInsert(Record insertedRecord) {

		if (insertedRecord.isDirty()) {
			return CacheInsertionStatus.REFUSED_DIRTY;
		}

		if (!insertedRecord.isSaved()) {
			return CacheInsertionStatus.REFUSED_UNSAVED;
		}

		return CacheInsertionStatus.ACCEPTED;
	}

	public static String buildCacheDTOStatsReport(ModelLayerFactory modelLayerFactory) {

		CompiledDTOStats stats = CacheRecordDTOUtils.getLastCompiledDTOStats();
		if (stats == null /*|| !SummaryCacheSingletons.getDataStore().isRecreated()*/) {
			return "No compiled stats available - they are only accessible after a full cache rebuild from solr";

		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("---------------------------------------------------------\n");
			sb.append("Size of cache data by collections\n");
			sb.append("---------------------------------------------------------\n");
			Map<Byte, CompiledCollectionDTOStats> collectionStats = stats.getCollectionStats();
			for (String collection : modelLayerFactory.getCollectionsListManager().getCollections()) {
				CollectionInfo collectionInfo = modelLayerFactory.getCollectionsListManager().getCollectionInfo(collection);
				CompiledCollectionDTOStats collectionDTOStats = collectionStats.get(collectionInfo.getCollectionId());
				if (collectionDTOStats != null) {
					sb.append(collection);
					sb.append(" : ");
					sb.append(humanReadableByteCount(collectionDTOStats.getMemoryDataSize(), false) + " in memory,");
					sb.append(humanReadableByteCount(collectionDTOStats.getPersistedDataSize(), false) + " persisted");
					sb.append("\n");
				}
			}
			sb.append("\n");

			KeyLongMap<String> persistedLengthPerSchemaTypes = new KeyLongMap<>();
			KeyLongMap<String> memoryLengthPerSchemaTypes = new KeyLongMap<>();

			KeyLongMap<String> persistedLengthPerMetadatas = new KeyLongMap<>();
			KeyLongMap<String> memoryLengthPerMetadatas = new KeyLongMap<>();

			for (String collection : modelLayerFactory.getCollectionsListManager().getCollections()) {
				CollectionInfo collectionInfo = modelLayerFactory.getCollectionsListManager().getCollectionInfo(collection);
				CompiledCollectionDTOStats collectionDTOStats = collectionStats.get(collectionInfo.getCollectionId());

				if (collectionDTOStats != null) {
					for (Map.Entry<Short, CompiledSchemaTypeDTOStats> entry : collectionDTOStats.getSchemaTypeStats().entrySet()) {
						MetadataSchemaType schemaType = modelLayerFactory.getMetadataSchemasManager()
								.getSchemaTypes(collection).getSchemaType(entry.getKey());

						CompiledSchemaTypeDTOStats schemaTypeDTOStats = entry.getValue();
						persistedLengthPerSchemaTypes.increment(schemaType.getCode(), schemaTypeDTOStats.getPersistedDataSize());
						memoryLengthPerSchemaTypes.increment(schemaType.getCode(), schemaTypeDTOStats.getMemoryDataSize());

						for (Metadata metadata : schemaType.getAllMetadatas()) {

							long persisted = schemaTypeDTOStats.getPersistedDataSize(metadata.getId());
							long memory = schemaTypeDTOStats.getMemoryDataSize(metadata.getId());

							if (persisted != 0 || memory != 0) {
								persistedLengthPerMetadatas.increment(metadata.getCode(), persisted);
								memoryLengthPerMetadatas.increment(metadata.getCode(), memory);
							}
						}

					}
				}

			}

			sb.append("---------------------------------------------------------\n");
			sb.append("Size of cache data by schema types (all collections) \n");
			sb.append("---------------------------------------------------------\n");

			for (Map.Entry<String, Long> entry : memoryLengthPerSchemaTypes.entriesSortedByDescValue()) {
				long persisted = persistedLengthPerSchemaTypes.get(entry.getKey());
				sb.append(entry.getKey());
				sb.append(" : ");
				sb.append(humanReadableByteCount(entry.getValue(), false) + " in memory");
				if (persisted > 0) {
					sb.append(" / " + humanReadableByteCount(persisted, true) + " persisted");
				}
				sb.append("\n");
			}


			sb.append("---------------------------------------------------------\n");
			sb.append("Size of cache data by metadatas (all collections) \n");
			sb.append("---------------------------------------------------------\n");

			for (Map.Entry<String, Long> entry : memoryLengthPerMetadatas.entriesSortedByDescValue()) {
				long persisted = persistedLengthPerMetadatas.get(entry.getKey());
				sb.append(entry.getKey());
				sb.append(" : ");
				sb.append(humanReadableByteCount(entry.getValue(), false) + " in memory, ");
				if (persisted > 0) {
					sb.append(humanReadableByteCount(persisted, true) + " persisted");
				}
				sb.append("\n");
			}
			return sb.toString();
		}

	}


	public static String logDTODebugReport(String id, List<List<Object>> memoryInfos,
										   List<List<Object>> persistedInfos) {

		StringBuilder sb = new StringBuilder("\n");
		sb.append("------------------------------------------------------------------------------------------------------\n");
		sb.append("Memory infos for byte array DTO of " + id + "\n");
		sb.append("+--------+--------+----------------------+-----------------+------------------------------------------\n");

		for (List<Object> info : memoryInfos) {
			if (info == null) {
				sb.append("+--------+--------+----------------------+-----------------+------------------------------------------\n");
			} else {
				sb.append("| ");
				sb.append(StringUtils.rightPad("" + info.get(0), 6));
				sb.append(" | ");
				sb.append(StringUtils.rightPad("" + info.get(1), 6));
				sb.append(" | ");
				sb.append(info.get(2));
				sb.append(" | ");
				sb.append(info.get(3));
				sb.append(" | ");
				sb.append(info.get(4));
				sb.append("\n");
			}
		}
		sb.append("+--------+--------+----------------------+-----------------+------------------------------------------\n");
		sb.append("\n");
		sb.append("\n");

		sb.append("------------------------------------------------------------------------------------------------------\n");
		sb.append("Persisted infos for byte array DTO of " + id + "\n");
		sb.append("+--------+--------+----------------------+-----------------+------------------------------------------\n");

		for (List<Object> info : persistedInfos) {
			if (info == null) {
				sb.append("+--------+--------+----------------------+-----------------+------------------------------------------\n");
			} else {
				sb.append("| ");
				sb.append(StringUtils.rightPad("" + info.get(0), 6));
				sb.append(" | ");
				sb.append(StringUtils.rightPad("" + info.get(1), 6));
				sb.append(" | ");
				sb.append(info.get(2));
				sb.append(" | ");
				sb.append(info.get(3));
				sb.append(" | ");
				sb.append(info.get(4));
				sb.append("\n");
			}
		}
		sb.append("+--------+--------+----------------------+-----------------+------------------------------------------\n");

		return sb.toString();
	}
}
