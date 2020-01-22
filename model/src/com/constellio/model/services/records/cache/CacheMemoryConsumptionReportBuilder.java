package com.constellio.model.services.records.cache;

import com.constellio.data.utils.CacheStat;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.records.reindexing.SystemReindexingConsumptionInfos;
import com.constellio.model.services.records.reindexing.SystemReindexingConsumptionInfos.SystemReindexingConsumptionHeapInfo;
import com.constellio.model.services.records.reindexing.SystemReindexingInfos;

import java.io.File;
import java.util.List;

import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.OffHeapByteArrayList_ID;
import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.OffHeapByteList_ID;
import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.OffHeapIntList_ID;
import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.OffHeapLongList_ID;
import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.OffHeapShortList_ID;
import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.SDK;
import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.SortedIntIdsList_ID;
import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.getAllocatedMemory;
import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.getFreedMemory;

public class CacheMemoryConsumptionReportBuilder {

	String SEPARATOR = "----- ----- ----- ----- ----- ----- ----- ----- -----";

	ModelLayerFactory modelLayerFactory;

	public CacheMemoryConsumptionReportBuilder(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
	}

	public String build() {

		StringBuilder builder = new StringBuilder();

		MetadataIndexCacheDataStore metadataIndexCacheDataStore =
				modelLayerFactory.getRecordsCaches().getMetadataIndexCacheDataStore();
		List<MetadataIndexCacheDataStoreStat> stats = metadataIndexCacheDataStore.compileMemoryConsumptionStats();
		List<CacheStat> cacheStats = modelLayerFactory.getRecordsCaches().getRecordsCachesDataStore()
				.compileMemoryConsumptionStats();

		long taxonomiesCacheHeap = modelLayerFactory.getTaxonomiesSearchServicesCache().getHeapConsumption();

		long totalIndexedCacheOffHeap = 0;
		long totalIndexedCacheHeap = 0;
		for (MetadataIndexCacheDataStoreStat stat : stats) {
			totalIndexedCacheHeap += stat.getKeysHeapLength() + stat.getValuesHeapLength() + stat.getEstimatedMapHeapLength();
			totalIndexedCacheOffHeap += stat.getValuesOffHeapLength();
		}

		long totalCacheOffHeap = 0;
		long totalCacheHeap = 0;
		for (CacheStat cacheStat : cacheStats) {
			if (!cacheStat.getName().startsWith("datastore.fullyCachedData.")) {
				totalCacheHeap += cacheStat.getHeapSize();
				totalCacheOffHeap += cacheStat.getOffHeapSize();
			}
		}

		long volatileCacheMemorySize = modelLayerFactory.getConfiguration().getRecordsVolatileCacheMemorySize();

		long totalHeap = totalCacheHeap + totalIndexedCacheHeap + volatileCacheMemorySize + taxonomiesCacheHeap;


		builder.append("\n" + SEPARATOR);
		builder.append("\n" + "CACHE CONSUMPTION");
		builder.append("\n" + SEPARATOR);
		builder.append("\n" + "OffHeap memory previously used : " + humanReadableByteCount(getFreedMemory(), true));
		builder.append("\n");
		builder.append("\n" + "OffHeap memory usage : " + humanReadableByteCount(getAllocatedMemory(), true));
		builder.append("\n" + "\t- Cache datastore : " + humanReadableByteCount(totalCacheOffHeap, true));
		builder.append("\n" + "\t- Cache index : " + humanReadableByteCount(totalIndexedCacheOffHeap, true));

		builder.append("\n" + "Heap memory usage : " + humanReadableByteCount(totalHeap, true));
		builder.append("\n" + "\t- Cache datastore : " + humanReadableByteCount(totalCacheHeap, true));
		builder.append("\n" + "\t- Cache index : " + humanReadableByteCount(totalIndexedCacheHeap, true));
		builder.append("\n" + "\t- Volatile cache : " + humanReadableByteCount(volatileCacheMemorySize, true));
		builder.append("\n" + "\t- Taxonomies hasChildren cache : " + humanReadableByteCount(taxonomiesCacheHeap, true));

		File mapDbFile = new File(new FoldersLocator().getWorkFolder(), "null-cache.db");
		if (mapDbFile.exists()) {
			builder.append("\n" + "File system usage : " + humanReadableByteCount(mapDbFile.length(), true));
		}

		builder.append("\n" + SEPARATOR);
		builder.append("\n" + "");

		SystemReindexingInfos reindexingInfos = ReindexingServices.getReindexingInfos();
		if (reindexingInfos != null) {
			builder.append("\n" + SEPARATOR);
			builder.append("\n" + "REINDEXING CACHE");
			builder.append("\n" + SEPARATOR);

			SystemReindexingConsumptionInfos infos = reindexingInfos.getConsumptionSupplier().get();

			for (SystemReindexingConsumptionHeapInfo info : infos.getHeapInfos()) {
				if (info.isMemory()) {
					builder.append("\n" + info.getName() + " : " + humanReadableByteCount(info.getValue(), true));
				}
			}
			builder.append("\n" + "");
			for (SystemReindexingConsumptionHeapInfo info : infos.getHeapInfos()) {
				if (!info.isMemory()) {
					builder.append("\n" + info.getName() + " : " + info.getValue());
				}
			}

			builder.append("\n" + SEPARATOR);
			builder.append("\n" + "");
		}


		builder.append("\n" + SEPARATOR);
		builder.append("\n" + "OFFHEAP MEMORY CONSUMPTION BY JAVA CLASS");
		builder.append("\n" + SEPARATOR);
		builder.append("\n" + "OffHeapByteList : " + getAllocatedMemory(OffHeapByteList_ID));
		builder.append("\n" + "OffHeapShortList : " + getAllocatedMemory(OffHeapShortList_ID));
		builder.append("\n" + "OffHeapIntList : " + getAllocatedMemory(OffHeapIntList_ID));
		builder.append("\n" + "OffHeapLongList : " + getAllocatedMemory(OffHeapLongList_ID));
		builder.append("\n" + "OffHeapByteArrayList : " + getAllocatedMemory(OffHeapByteArrayList_ID));
		builder.append("\n" + "SortedIntIdsList : " + getAllocatedMemory(SortedIntIdsList_ID));
		builder.append("\n" + "SDK : " + getAllocatedMemory(SDK));
		builder.append("\n" + SEPARATOR);
		builder.append("\n" + "");

		builder.append("\n" + SEPARATOR);
		builder.append("\n" + "DETAILED RECORD CACHE DATASTORE MEMORY CONSUMPTION");
		builder.append("\n" + SEPARATOR);


		for (CacheStat stat : cacheStats) {
			builder.append("\n" + "\t" + stat.getName() + " : " +
						   "OffHeap=" + humanReadableByteCount(stat.getOffHeapSize(), true) + ", " +
						   "Heap=" + humanReadableByteCount(stat.getHeapSize(), true));
		}

		builder.append("\n" + SEPARATOR);
		builder.append("\n" + "");

		builder.append("\n" + SEPARATOR);
		builder.append("\n" + "DETAILED CACHE INDEX METADATAS MEMORY CONSUMPTION");
		builder.append("\n" + SEPARATOR);

		int othersOffHeapTotal = 0;
		int othersHeapTotal = 0;
		int othersKeysHeap = 0;
		int othersValuesHeap = 0;
		int othersMapHeap = 0;
		int othersKeysCount = 0;
		int othersValuesCount = 0;

		for (MetadataIndexCacheDataStoreStat stat : stats) {
			//If it takes less than 10k
			if (stat.getValuesOffHeapLength() + stat.getTotalHeap() < 10_000) {
				othersOffHeapTotal += stat.getValuesOffHeapLength();
				othersHeapTotal += stat.getTotalHeap();
				othersKeysHeap += stat.getKeysHeapLength();
				othersValuesHeap += stat.getValuesHeapLength();
				othersMapHeap += stat.getEstimatedMapHeapLength();
				othersKeysCount += stat.getKeysCount();
				othersValuesCount += stat.getValuesCount();

			} else {
				builder.append("\n" + "\t" + stat.getName() + " : " +
							   "OffHeap=" + humanReadableByteCount(stat.getValuesOffHeapLength(), true) + ", " +
							   "Heap=" + humanReadableByteCount(stat.getTotalHeap(), true) + " ("
							   + "keys=" + humanReadableByteCount(stat.getKeysHeapLength(), true) + ", "
							   + "values=" + humanReadableByteCount(stat.getValuesHeapLength(), true) + ", "
							   + "map=" + humanReadableByteCount(stat.getEstimatedMapHeapLength(), true) +
							   "), keys=" + stat.getKeysCount() + ", values=" + stat.getValuesCount());
			}
		}

		builder.append("\n" + "\tOthers (entry taking less than 10ko) : " +
					   "OffHeap=" + humanReadableByteCount(othersOffHeapTotal, true) + ", " +
					   "Heap=" + humanReadableByteCount(othersHeapTotal, true) + " ("
					   + "keys=" + humanReadableByteCount(othersKeysHeap, true) + ", "
					   + "values=" + humanReadableByteCount(othersValuesHeap, true) + ", "
					   + "map=" + humanReadableByteCount(othersMapHeap, true) +
					   "), keys=" + othersKeysCount + ", values=" + othersValuesCount);

		builder.append("\n" + SEPARATOR);
		builder.append("\n" + "");

		builder.append("\n" + SEPARATOR);
		builder.append("\n" + "RECORDS COUNT");
		builder.append("\n" + SEPARATOR);

		for (String collection : modelLayerFactory.getCollectionsListManager().getCollectionsExcludingSystem()) {

			builder.append("\n" + "");
			builder.append("\n" + "");
			builder.append("\n" + "==========================================");
			builder.append("\n" + " COLLECTION '" + collection + "'");
			builder.append("\n" + "==========================================");

			RecordsCache cache = modelLayerFactory.getRecordsCaches().getCache(collection);

			int totalRecords = 0;
			long totalSize = 0;

			if (cache != null) {
				builder.append("\n" + "Total : " + totalRecords + " records, " + humanReadableByteCount(totalSize, true));
			}

		}
		return builder.toString();
	}


	//Thanks aioobe, found on https://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
	public static String humanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit) {
			return bytes + " B";
		}
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
}
