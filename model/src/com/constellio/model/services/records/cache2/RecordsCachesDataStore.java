package com.constellio.model.services.records.cache2;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.utils.LazyMergingIterator;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache2.ByteArrayRecordDTO.ByteArrayRecordDTOWithIntegerId;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Spliterator.DISTINCT;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.NONNULL;
import static java.util.Spliterators.spliteratorUnknownSize;

public class RecordsCachesDataStore {

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
	}

	public void insertWithoutReservingSpaceForPreviousIds(RecordDTO dto) {
		int intId;
		if (dto instanceof ByteArrayRecordDTO.ByteArrayRecordDTOWithIntegerId) {
			intId = ((ByteArrayRecordDTOWithIntegerId) dto).getIntId();
		} else {
			intId = CacheRecordDTOUtils.toIntKey(dto.getId());
		}

		if (intId == CacheRecordDTOUtils.KEY_IS_NOT_AN_INT) {
			stringIdsDataStore.insert(dto);
		} else {
			intIdsDataStore.insert(intId, dto, false, false);
		}
	}

	public void insert(RecordDTO dto) {

		int intId;
		if (dto instanceof ByteArrayRecordDTO.ByteArrayRecordDTOWithIntegerId) {
			intId = ((ByteArrayRecordDTOWithIntegerId) dto).getIntId();
		} else {
			intId = CacheRecordDTOUtils.toIntKey(dto.getId());
		}

		if (intId == CacheRecordDTOUtils.KEY_IS_NOT_AN_INT) {
			stringIdsDataStore.insert(dto);
		} else {
			intIdsDataStore.insert(intId, dto, true, false);
		}
	}


	public void remove(RecordDTO dto) {
		int intId = CacheRecordDTOUtils.toIntKey(dto.getId());

		if (intId == CacheRecordDTOUtils.KEY_IS_NOT_AN_INT) {
			stringIdsDataStore.remove(dto);

		} else {
			intIdsDataStore.remove(intId, dto);
		}

	}

	public RecordDTO get(String id) {
		int intId = CacheRecordDTOUtils.toIntKey(id);

		if (intId == CacheRecordDTOUtils.KEY_IS_NOT_AN_INT) {
			return stringIdsDataStore.get(id);

		} else {
			return intIdsDataStore.get(intId);
		}
	}

	public RecordDTO get(byte collectionId, String id) {
		int intId = CacheRecordDTOUtils.toIntKey(id);

		if (intId == CacheRecordDTOUtils.KEY_IS_NOT_AN_INT) {
			return stringIdsDataStore.get(id);

		} else {
			return intIdsDataStore.get(collectionId, intId);
		}
	}

	public RecordDTO get(byte collectionId, short typeId, String id) {
		int intId = CacheRecordDTOUtils.toIntKey(id);

		if (intId == CacheRecordDTOUtils.KEY_IS_NOT_AN_INT) {
			return stringIdsDataStore.get(id);

		} else {
			return intIdsDataStore.get(collectionId, typeId, intId);
		}
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

	public Iterator<RecordDTO> iterator() {
		return new LazyMergingIterator<>(
				intIdsDataStore.iterator(),
				stringIdsDataStore.iterator());

	}


	public Iterator<RecordDTO> iterator(byte collectionId) {
		return new LazyMergingIterator<>(
				intIdsDataStore.iterator(collectionId),
				stringIdsDataStore.iterator(collectionId));
	}


	public Iterator<RecordDTO> iterator(byte collectionId, short typeId) {
		return new LazyMergingIterator<>(
				intIdsDataStore.iterator(collectionId, typeId),
				stringIdsDataStore.iterator(collectionId, typeId));
	}

}
