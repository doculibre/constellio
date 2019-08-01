package com.constellio.model.services.records.cache;

import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class CompiledDTOStats {

	private LocalDateTime compilationTime;

	private CompiledCollectionDTOStats[] collectionsStats = new CompiledCollectionDTOStats[256];

	private CompiledDTOStats(LocalDateTime time) {
		this.compilationTime = time;
	}

	public Map<Byte, CompiledCollectionDTOStats> getCollectionStats() {
		Map<Byte, CompiledCollectionDTOStats> statsMap = new HashMap<>();

		for (short i = 0; i < 256; i++) {
			if (collectionsStats[i] != null) {
				byte collectionId = (byte) (i + Byte.MIN_VALUE);
				statsMap.put(collectionId, collectionsStats[i]);
			}
		}
		return statsMap;
	}

	public LocalDateTime getCompilationTime() {
		return compilationTime;
	}

	public static class CompiledDTOStatsBuilder {

		CompiledDTOStats stats = new CompiledDTOStats(LocalDateTime.now());

		public void log(byte collectionId, short typeId, short metadataId, long length, boolean persisted) {
			int collectionIndex = collectionId - Byte.MIN_VALUE;
			CompiledCollectionDTOStats collectionStats = stats.collectionsStats[collectionIndex];
			if (collectionStats == null) {
				synchronized (this) {
					collectionStats = stats.collectionsStats[collectionIndex];
					if (collectionStats == null) {
						stats.collectionsStats[collectionIndex] = collectionStats
								= new CompiledCollectionDTOStats(collectionId);
					}
				}
			}

			if (persisted) {
				collectionStats.persistedDataSize += length;
			} else {
				collectionStats.memoryDataSize += length;
			}

			CompiledSchemaTypeDTOStats schemaTypeStats = collectionStats.schemaTypeStats[typeId];
			if (schemaTypeStats == null) {
				synchronized (this) {
					schemaTypeStats = collectionStats.schemaTypeStats[typeId];
					if (schemaTypeStats == null) {
						collectionStats.schemaTypeStats[typeId] = schemaTypeStats = new CompiledSchemaTypeDTOStats(typeId);
					}
				}
			}

			schemaTypeStats.log(metadataId, length, persisted);

		}

		public CompiledDTOStats build() {
			return stats;
		}
	}


	public static class CompiledCollectionDTOStats {

		private short typeId;
		private long persistedDataSize;
		private long memoryDataSize;

		private CompiledSchemaTypeDTOStats[] schemaTypeStats
				= new CompiledSchemaTypeDTOStats[MetadataSchemaTypes.LIMIT_OF_TYPES_IN_COLLECTION];

		public CompiledCollectionDTOStats(short typeId) {
			this.typeId = typeId;
		}


		public short getTypeId() {
			return typeId;
		}

		public long getPersistedDataSize() {
			return persistedDataSize;
		}

		public long getMemoryDataSize() {
			return memoryDataSize;
		}

		public Map<Short, CompiledSchemaTypeDTOStats> getSchemaTypeStats() {
			Map<Short, CompiledSchemaTypeDTOStats> statsMap = new HashMap<>();

			for (short i = 0; i < MetadataSchemaTypes.LIMIT_OF_TYPES_IN_COLLECTION; i++) {
				if (schemaTypeStats[i] != null) {
					statsMap.put(i, schemaTypeStats[i]);
				}
			}
			return statsMap;
		}
	}

	public static class CompiledSchemaTypeDTOStats {

		private short typeId;

		public CompiledSchemaTypeDTOStats(short typeId) {
			this.typeId = typeId;
		}


		private long persistedDataSize;
		private long memoryDataSize;

		long[] persistedDataSizePerMetadata = new long[Short.MAX_VALUE * 2 + 1];
		long[] memoryDataSizePerMetadata = new long[Short.MAX_VALUE * 2 + 1];


		private void log(short metadataId, long length, boolean persisted) {
			int metadataIndex = metadataId - Short.MIN_VALUE;
			if (persisted) {
				persistedDataSize += length;
				persistedDataSizePerMetadata[metadataIndex] += length;

			} else {
				memoryDataSize += length;
				memoryDataSizePerMetadata[metadataIndex] += length;
			}
		}

		public short getTypeId() {
			return typeId;
		}

		public long getPersistedDataSize() {
			return persistedDataSize;
		}

		public long getMemoryDataSize() {
			return memoryDataSize;
		}

		public long getPersistedDataSize(short metadataId) {
			int metadataIndex = metadataId - Short.MIN_VALUE;
			return persistedDataSizePerMetadata[metadataIndex];
		}

		public long getMemoryDataSize(short metadataId) {
			int metadataIndex = metadataId - Short.MIN_VALUE;
			return memoryDataSizePerMetadata[metadataIndex];
		}
	}

}
