package com.constellio.model.services.records.cache.dataStore;

import com.constellio.data.conf.FoldersLocator;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.data.utils.CacheStat;
import com.constellio.data.utils.LazyMergingIterator;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.records.cache.ByteArrayRecordDTO.ByteArrayRecordDTOWithIntegerId;
import com.constellio.model.services.records.cache.PersistedIdsServices;
import com.constellio.model.services.records.cache.PersistedIdsServices.RecordIdsIterator;
import com.constellio.model.services.records.cache.PersistedSortValuesServices.SortValueList;
import com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.constellio.data.utils.LangUtils.humanReadableByteCount;
import static java.util.Spliterator.DISTINCT;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.NONNULL;
import static java.util.Spliterators.spliteratorUnknownSize;

public class RecordsCachesDataStore implements Closeable {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecordsCachesDataStore.class);

	/**
	 * Most ids are zero-padded incrementd integers, so the values are efficiently saved in this structure
	 */
	IntegerIdsMemoryEfficientRecordsCachesDataStore intIdsDataStore;

	/**
	 * For others ids, they are saved in java collections (less efficient, but enough)
	 */
	StringIdsRecordsCachesDataStore stringIdsDataStore;

	public RecordsCachesDataStore(ModelLayerFactory modelLayerFactory) {
		this.intIdsDataStore = new IntegerIdsMemoryEfficientRecordsCachesDataStore(modelLayerFactory);
		this.stringIdsDataStore = new StringIdsRecordsCachesDataStore(modelLayerFactory);


		if (Toggle.STRUCTURE_CACHE_BASED_ON_EXISTING_IDS.isEnabled()) {
			if (modelLayerFactory.getConfiguration().isLoadingIdsFromVaultWhenPossible()) {
				PersistedIdsServices persistedIdsServices = new PersistedIdsServices(modelLayerFactory);
				RecordIdsIterator recordIdsIterator = persistedIdsServices.getRecordIds();
				String message = "Structuring cache based on ids obtained from " + (recordIdsIterator.isObtainedFromSolr() ? "solr" : "vault");

				LOGGER.info(message + "...       - Current memory : " + humanReadableByteCount(OffHeapMemoryAllocator.getAllocatedMemory(), true));
				intIdsDataStore.structureCacheUsingExistingIds(recordIdsIterator.getIterator());
				LOGGER.info(message + " finished - Current memory : " + humanReadableByteCount(OffHeapMemoryAllocator.getAllocatedMemory(), true));

			} else {
				List<RecordId> recordIds = null;
				File idsList = new File(new FoldersLocator().getWorkFolder(), "integer-ids.txt");
				if (FoldersLocator.usingAppWrapper() || Toggle.STRUCTURE_CACHE_BASED_ON_EXISTING_IDS_ON_DEV_STATION.isEnabled()) {
					if (idsList.exists() && new DateTime(idsList.lastModified()).isAfter(new DateTime().minusDays(1))) {
						try {
							recordIds = FileUtils.readLines(idsList, "UTF-8").stream().map((line) -> RecordId.toId(line))
									.filter((id) -> id.isInteger()).collect(Collectors.toList());
						} catch (IOException e) {
							e.printStackTrace();
							recordIds = null;
						}
					}
				}
				if (recordIds == null) {
					LOGGER.info("Loading ids from solr... could take up to 30 minutes, please wait...");
					Iterator<RecordId> recordIdIterator = modelLayerFactory.newSearchServices().recordsIdIteratorExceptEvents();
					recordIds = IteratorUtils.toList(recordIdIterator);
					List<String> lines = recordIds.stream().filter((id) -> id.isInteger()).map(RecordId::stringValue).collect(Collectors.toList());

					if (FoldersLocator.usingAppWrapper() || Toggle.STRUCTURE_CACHE_BASED_ON_EXISTING_IDS_ON_DEV_STATION.isEnabled()) {
						try {

							FileUtils.writeLines(idsList, lines);
							if (!lines.isEmpty()) {
								LOGGER.info("Last line is : " + lines.get(lines.size() - 1));
							}
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				}

				LOGGER.info("Structuring cache based on ids...       - Current memory : " + humanReadableByteCount(OffHeapMemoryAllocator.getAllocatedMemory(), true));
				intIdsDataStore.structureCacheUsingExistingIds(recordIds.iterator());
				LOGGER.info("Structuring cache based on ids finished - Current memory : " + humanReadableByteCount(OffHeapMemoryAllocator.getAllocatedMemory(), true));

			}
		}
	}

	public void insertWithoutReservingSpaceForPreviousIds(RecordDTO dto) {
		int intId;
		if (dto instanceof ByteArrayRecordDTOWithIntegerId) {
			intId = ((ByteArrayRecordDTOWithIntegerId) dto).getIntId();
		} else {
			intId = RecordUtils.toIntKey(dto.getId());
		}

		if (intId == RecordUtils.KEY_IS_NOT_AN_INT) {
			stringIdsDataStore.insert(dto);
		} else {
			intIdsDataStore.insert(intId, dto, false);
		}
	}

	public void insert(RecordDTO dto) {

		int intId;
		if (dto instanceof ByteArrayRecordDTOWithIntegerId) {
			intId = ((ByteArrayRecordDTOWithIntegerId) dto).getIntId();
		} else {
			intId = RecordUtils.toIntKey(dto.getId());
		}

		if (intId == RecordUtils.KEY_IS_NOT_AN_INT) {
			stringIdsDataStore.insert(dto);
		} else {
			intIdsDataStore.insert(intId, dto, true);
		}
	}


	public void remove(RecordDTO dto) {
		int intId = RecordUtils.toIntKey(dto.getId());

		if (intId == RecordUtils.KEY_IS_NOT_AN_INT) {
			stringIdsDataStore.remove(dto.getId());

		} else {
			intIdsDataStore.remove(intId, dto);
		}

	}


	public RecordDTO get(RecordId id) {
		if (id.isInteger()) {
			return intIdsDataStore.__get(id.intValue());

		} else {
			return stringIdsDataStore.get(id.stringValue());
		}
	}

	public RecordDTO get(String id) {
		int intId = RecordUtils.toIntKey(id);

		if (intId == RecordUtils.KEY_IS_NOT_AN_INT) {
			return stringIdsDataStore.get(id);

		} else {
			return intIdsDataStore.__get(intId);
		}
	}

	public RecordDTO get(byte collectionId, String id) {
		int intId = RecordUtils.toIntKey(id);

		if (intId == RecordUtils.KEY_IS_NOT_AN_INT) {
			return stringIdsDataStore.get(id);

		} else {
			return intIdsDataStore.__get(collectionId, intId);
		}
	}

	public RecordDTO get(byte collectionId, RecordId id) {
		if (id.isInteger()) {
			return intIdsDataStore.__get(collectionId, id.intValue());

		} else {
			return stringIdsDataStore.get(id.stringValue());
		}
	}


	public Stream<RecordDTO> stream() {
		return stream(true);
	}

	public Stream<RecordDTO> stream(byte collection) {
		return stream(true, collection);
	}

	public Stream<RecordDTO> stream(byte collection, short schemaType) {
		return stream(true, collection, schemaType, false);
	}

	public Stream<RecordDTO> streamSortedById(byte collection, short schemaType) {
		return stream(true, collection, schemaType, true);
	}

	public List<RecordDTO> list(byte collectionId, short typeId) {
		List<RecordDTO> values = intIdsDataStore.list(collectionId, typeId);
		List<RecordDTO> values2 = stringIdsDataStore.list(collectionId, typeId);
		if (values2 != null && !values2.isEmpty()) {
			values = new ArrayList<>(values);
			values.addAll(values2);
		}
		return values;
	}

	public Stream<RecordDTO> stream(byte collection, short schemaType, short metadataId, Object value) {
		return StreamSupport.stream(spliteratorUnknownSize(iterator(true, collection, schemaType, metadataId, value), DISTINCT + NONNULL + IMMUTABLE), false);
	}


	public Stream<RecordDTO> stream(boolean autoClosedStream) {
		return StreamSupport.stream(spliteratorUnknownSize(iterator(autoClosedStream), DISTINCT + NONNULL + IMMUTABLE), false);
	}

	public Stream<RecordDTO> stream(boolean autoClosedStream, byte collection) {
		return StreamSupport.stream(spliteratorUnknownSize(iterator(autoClosedStream, collection), DISTINCT + NONNULL + IMMUTABLE), false);
	}

	public Stream<RecordDTO> stream(boolean autoClosedStream, byte collection, short schemaType, boolean sortedById) {
		return StreamSupport.stream(spliteratorUnknownSize(iterator(autoClosedStream, collection, schemaType, sortedById), DISTINCT + NONNULL + IMMUTABLE), false);
	}

	public Stream<RecordDTO> stream(byte collectionId, List<String> ids) {
		return ids.stream().map((id) -> get(collectionId, id)).filter(Objects::nonNull);
	}


	public synchronized void invalidate(byte collection, short schemaType, Predicate<RecordDTO> predicate) {
		stringIdsDataStore.invalidate(collection, schemaType, predicate);
		intIdsDataStore.invalidate(collection, schemaType, predicate);
	}

	public synchronized void invalidateAll(byte collection, short schemaType) {
		stringIdsDataStore.invalidateAll(collection, schemaType);
		intIdsDataStore.invalidateAll(collection, schemaType);
	}

	public synchronized void invalidateAll(byte collection) {
		stringIdsDataStore.invalidateAll(collection);
		intIdsDataStore.invalidateAll(collection);
	}


	public Iterator<RecordDTO> iterator(boolean autoClosedIterator) {
		return new LazyMergingIterator<>(
				intIdsDataStore.iterator(autoClosedIterator),
				stringIdsDataStore.iterator());

	}


	public Iterator<RecordDTO> iterator(boolean autoClosedIterator, byte collectionId) {
		return new LazyMergingIterator<>(
				intIdsDataStore.iterator(autoClosedIterator, collectionId),
				stringIdsDataStore.iterator(collectionId));
	}


	public Iterator<RecordDTO> iterator(boolean autoClosedIterator, byte collectionId, short typeId, boolean sortedById) {
		return new LazyMergingIterator<>(
				intIdsDataStore.iterator(autoClosedIterator, collectionId, typeId, sortedById),
				stringIdsDataStore.iterator(collectionId, typeId));
	}

	public Iterator<RecordDTO> iterator(boolean autoClosedIterator, byte collectionId, short typeId, short metadataId,
										Object value) {
		return new LazyMergingIterator<>(
				intIdsDataStore.iterator(autoClosedIterator, collectionId, typeId, metadataId, value),
				stringIdsDataStore.iterator(collectionId, typeId, metadataId, value));
	}

	public IntegerIdsMemoryEfficientRecordsCachesDataStore getIntIdsDataStore() {
		return intIdsDataStore;
	}

	public StringIdsRecordsCachesDataStore getStringIdsDataStore() {
		return stringIdsDataStore;
	}

	public void close() {
		if (intIdsDataStore != null) {
			intIdsDataStore.close();
		}
		intIdsDataStore = null;
		stringIdsDataStore = null;
	}

	public List<CacheStat> compileMemoryConsumptionStats() {
		List<CacheStat> stats = new ArrayList<>();

		stats.addAll(intIdsDataStore.compileMemoryConsumptionStats());
		stats.addAll(stringIdsDataStore.compileMemoryConsumptionStats());

		return stats;

	}

	public void setRecordsMainSortValue(List<?> existingIds) {
		intIdsDataStore.setRecordsMainSortValue(existingIds);
		stringIdsDataStore.setRecordsMainSortValue(existingIds);

	}

	public void setRecordsMainSortValue(SortValueList sortValueList, ToIntFunction<RecordDTO> valueHascodeFunction) {
		if (sortValueList.isObtainedFromSolr()) {
			intIdsDataStore.setRecordsMainSortValue(sortValueList.getSortValues());
			stringIdsDataStore.setRecordsMainSortValue(sortValueList.getSortValues());

		} else {
			intIdsDataStore.setRecordsMainSortValueComparingValues(sortValueList.getSortValues(), valueHascodeFunction);
			stringIdsDataStore.setRecordsMainSortValueComparingValues(sortValueList.getSortValues(), valueHascodeFunction);

		}


	}

	public int getMainSortValue(RecordId recordId) {
		if (recordId.isInteger()) {
			return intIdsDataStore.getMainSortValue(recordId);

		} else {
			return -1;
		}
	}
}
