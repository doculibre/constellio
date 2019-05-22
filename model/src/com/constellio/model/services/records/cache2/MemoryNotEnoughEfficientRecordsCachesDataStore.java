package com.constellio.model.services.records.cache2;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.utils.LazyIterator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Spliterator.DISTINCT;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.NONNULL;
import static java.util.Spliterators.spliteratorUnknownSize;

/**
 * Memory and read/write efficient datastore
 */
public class MemoryNotEnoughEfficientRecordsCachesDataStore {

	//int : taking 4bytes
	//long : taking 8bytes

	//object reference : taking 12bytes

	//Integer : taking 16bytes
	//Long : taking 24bytes

	//Zero-padded integer in a 11 character string : 64bytes
	//String : (bytes) = 8 * (int) ((((no chars) * 2) + 45) / 8)

	//Size of a 50 int in an array : (12 + 50*8) 412 bytes, ==> 412mb / million of records
	//Size of a 50 int in an array : (12 + 50*4) 212 bytes, ==> 212mb / million of records

	//Size of a 50 int in an array : (12 + 50*4) 212 bytes, ==> 212mb / million of records

	//Size of 20 value slots per record for 1 000 000 records : (12 + 12*20) 252 bytes ==> 252mb / million of records

	private static final int SIZE_OF_LEVEL2_ARRAY = 125000;
	private static final int KEY_IS_NOT_AN_INT = 0;

	/**
	 * Arrays take less bytes than map, and are faster for inserting and retrieving values (no hashCode, no synchronization),
	 * but it is only possible for int keys. Since most keys are zero-padded int values stored in a String format,
	 * they are compatible with this structure of data.
	 */
	private int nextIndex = 1;
	private int[][] zeroPaddedLongKeyIndex;
	private RecordReferences[][] zeroPaddedLongKeyReferencesTo;
	private Object[][] zeroPaddedLongKeyCacheData;

	/**
	 * Rarely, a key may be a non-padded value (from a data migration), a padded value over 2,147,483,647 (currently not supported) or string id
	 * which isn't a number. These structures are used in these cases
	 */
	private Map<Object, RecordReferences> otherKeyReferencesTo;
	private Map<Object, Object> otherKeyCacheData = new HashMap<>();


	public MemoryNotEnoughEfficientRecordsCachesDataStore() {
		this.zeroPaddedLongKeyIndex = new int[Integer.MAX_VALUE / SIZE_OF_LEVEL2_ARRAY][];
		this.zeroPaddedLongKeyReferencesTo = new RecordReferences[Integer.MAX_VALUE / SIZE_OF_LEVEL2_ARRAY][];
		this.zeroPaddedLongKeyCacheData = new Object[Integer.MAX_VALUE / SIZE_OF_LEVEL2_ARRAY][];
	}

	public void removeEntries(Function<RecordDTO, Boolean> function) {
		for (int row = 0; row < zeroPaddedLongKeyCacheData.length; row++) {
			for (int column = 0; column < zeroPaddedLongKeyCacheData[row].length; column++) {
				Object entry = zeroPaddedLongKeyCacheData[row][column];
				if (entry != null && function.apply((RecordDTO) entry)) {
					remove((RecordDTO) entry);
				}

			}
		}

		for (Object stringIdDto : otherKeyCacheData.values()) {
			if (stringIdDto != null && function.apply((RecordDTO) stringIdDto)) {
				remove((RecordDTO) stringIdDto);
			}
		}
	}

	public Stream<RecordDTO> stream() {
		return StreamSupport.stream(spliteratorUnknownSize(iterator(), DISTINCT + NONNULL + IMMUTABLE), false);
	}

	public Stream<RecordDTO> stream(short collectionCacheId, String collection, short schemaTypeId, String schemaType) {
		return StreamSupport.stream(spliteratorUnknownSize(iterator(), DISTINCT + NONNULL + IMMUTABLE), false).filter((recordDTO -> collection.equals(recordDTO.getCollection()) && recordDTO.getSchemaCode().startsWith(schemaType + "_")));
	}

	public Stream<RecordDTO> stream(short collectionCacheId, String collection) {
		return StreamSupport.stream(spliteratorUnknownSize(iterator(), DISTINCT + NONNULL + IMMUTABLE), false).filter((recordDTO -> collection.equals(recordDTO.getCollection())));
	}

	public Iterator<RecordDTO> iterator() {

		Iterator<Object> otherIdsDtoIterator = otherKeyCacheData.values().iterator();

		return new LazyIterator<RecordDTO>() {

			int row = 0;
			int column = 0;

			@Override
			protected RecordDTO getNextOrNull() {

				while (row < zeroPaddedLongKeyCacheData.length) {
					Object entry = zeroPaddedLongKeyCacheData[row][column];
					column++;
					if (column >= zeroPaddedLongKeyCacheData[row].length) {
						column = 0;
						row++;
					}

					if (entry != null) {
						return (RecordDTO) entry;
					}

				}

				while (otherIdsDtoIterator.hasNext()) {
					RecordDTO next = (RecordDTO) otherIdsDtoIterator.next();
					if (next != null) {
						return next;
					}

				}

				return null;

			}
		};
	}

	public void put(Object key, RecordDTO data) {
		int intKey = CacheRecordDTOUtils.toIntKey(key);
		if (intKey == KEY_IS_NOT_AN_INT) {
			synchronized (otherKeyCacheData) {
				otherKeyCacheData.put(key, data);
			}
		} else {
			setData(intKey, data);
		}
	}

	private int indexOfIntId(int intKey) {
		int indexArrayLevel1 = intKey / SIZE_OF_LEVEL2_ARRAY;
		int indexArrayLevel2 = intKey % SIZE_OF_LEVEL2_ARRAY;

		int[] level2Array = zeroPaddedLongKeyIndex[indexArrayLevel1];
		if (level2Array == null) {
			synchronized (this) {
				level2Array = zeroPaddedLongKeyIndex[indexArrayLevel1];
				if (level2Array == null) {
					level2Array = new int[SIZE_OF_LEVEL2_ARRAY];
					zeroPaddedLongKeyIndex[indexArrayLevel1] = level2Array;
				}
			}
		}

		int index = level2Array[indexArrayLevel2];

		if (index == 0) {
			synchronized (this) {
				index = level2Array[indexArrayLevel2];
				if (index == 0) {
					index = nextIndex++;
					level2Array[indexArrayLevel2] = index;
				}
			}
		}

		return index - 1;
	}

	private RecordReferences getReferencesTo(int intKey) {
		int index = indexOfIntId(intKey);
		int arrayLevel1 = index / SIZE_OF_LEVEL2_ARRAY;
		int arrayLevel2 = index % SIZE_OF_LEVEL2_ARRAY;

		RecordReferences[] level2Array = zeroPaddedLongKeyReferencesTo[arrayLevel1];
		if (level2Array == null) {
			synchronized (this) {
				level2Array = zeroPaddedLongKeyReferencesTo[arrayLevel1];
				if (level2Array == null) {
					level2Array = new RecordReferences[SIZE_OF_LEVEL2_ARRAY];
					zeroPaddedLongKeyReferencesTo[arrayLevel1] = level2Array;
				}
			}
		}

		RecordReferences references = level2Array[arrayLevel2];

		if (references == null) {
			synchronized (this) {
				references = level2Array[arrayLevel2];
				if (references == null) {
					references = new RecordReferences();
					level2Array[arrayLevel2] = references;
				}
			}
		}

		return references;
	}

	private RecordReferences getReferences(Object key) {
		int intKey = CacheRecordDTOUtils.toIntKey(key);
		if (intKey == KEY_IS_NOT_AN_INT) {
			RecordReferences references = otherKeyReferencesTo.get(key);
			if (references == null) {
				synchronized (this) {
					references = otherKeyReferencesTo.get(key);
					if (references == null) {
						references = new RecordReferences();
						otherKeyReferencesTo.put(key, references);
					}
				}
			}
			return references;
		} else {
			return getReferences(intKey);
		}
	}

	private RecordDTO getData(int intKey) {
		int index = indexOfIntId(intKey);
		int arrayLevel1 = index / SIZE_OF_LEVEL2_ARRAY;
		int arrayLevel2 = index % SIZE_OF_LEVEL2_ARRAY;

		Object[] level2Array = zeroPaddedLongKeyCacheData[arrayLevel1];
		return level2Array == null ? null : (RecordDTO) level2Array[arrayLevel2];
	}

	private void setData(int intKey, Object data) {
		int index = indexOfIntId(intKey);
		int arrayLevel1 = index / SIZE_OF_LEVEL2_ARRAY;
		int arrayLevel2 = index % SIZE_OF_LEVEL2_ARRAY;

		Object[] level2Array = zeroPaddedLongKeyCacheData[arrayLevel1];
		if (level2Array == null) {
			synchronized (this) {
				level2Array = zeroPaddedLongKeyCacheData[arrayLevel1];
				if (level2Array == null) {
					level2Array = new Object[SIZE_OF_LEVEL2_ARRAY];
					zeroPaddedLongKeyCacheData[arrayLevel1] = level2Array;
				}
			}
		}

		level2Array[arrayLevel2] = data;
	}


	public void remove(RecordDTO key) {
		int intKey = CacheRecordDTOUtils.toIntKey(key);
		if (intKey == KEY_IS_NOT_AN_INT) {
			synchronized (otherKeyCacheData) {
				otherKeyCacheData.remove(key);
			}
		} else {
			setData(intKey, null);
		}
	}


	public RecordDTO get(Object key) {
		int intKey = CacheRecordDTOUtils.toIntKey(key);
		if (intKey == KEY_IS_NOT_AN_INT) {
			return (RecordDTO) otherKeyCacheData.get(key);
		} else {
			return getData(intKey);
		}
	}

	public void addReference(Object fromKey, Object toKey) {
		getReferences(toKey).add(fromKey);
	}

	public void removeReference(Object fromKey, Object toKey) {
		getReferences(toKey).remove(fromKey);
	}

	public static class RecordReferences {

		private static final int ARRAYS_SIZE = 20;

		int intKeysArray = 0;
		int longKeysArray = 0;

		/**
		 * This list of keys can contain :
		 * - arrays of int keys
		 * - arrays of long keys
		 * String keys (not in an array)
		 */
		List<Object> keys = null;

		boolean contains(Object reference) {
			if ((reference instanceof Integer)) {
				return contains(((Integer) reference).intValue());
			}

			if ((reference instanceof Long)) {
				return contains(((Long) reference).longValue());
			}

			return keys.contains(reference);

		}

		boolean contains(int intReference) {
			for (Object entry : keys) {
				if (entry instanceof int[]) {
					for (int item : ((int[]) entry)) {
						if (intReference == item) {
							return true;
						}
					}
				}
			}

			return false;
		}

		boolean contains(long longReference) {
			for (Object entry : keys) {
				if (entry instanceof long[]) {
					for (long item : ((long[]) entry)) {
						if (longReference == item) {
							return true;
						}
					}
				}
			}

			return false;
		}

		boolean add(Object reference) {

			if (contains(reference)) {
				return false;
			}

			if (reference instanceof Integer) {
				addIntReference((Integer) reference);

			} else if (reference instanceof Long) {
				addLongReference((Long) reference);

			} else {
				keys.add(reference);
			}
			return true;
		}

		private void addIntReference(int reference) {
			boolean added = false;

			iteratingKeys:
			for (int i = intKeysArray; i < keys.size(); i++) {
				Object entry = keys.get(i);
				if (entry instanceof int[]) {
					int[] array = (int[]) entry;
					intKeysArray = i;
					for (int j = 0; j < array.length; j++) {
						if (array[j] == 0) {
							array[j] = reference;
							added = true;
							break iteratingKeys;
						}
					}
				}
			}

			if (!added) {
				int[] newArray = new int[ARRAYS_SIZE];
				newArray[0] = reference;
				intKeysArray = keys.size();
				keys.add(newArray);
			}
		}

		private void addLongReference(long reference) {
			boolean added = false;
			iteratingKeys:
			for (int i = longKeysArray; i < keys.size(); i++) {
				Object entry = keys.get(i);
				if (entry instanceof long[]) {
					long[] array = (long[]) entry;
					longKeysArray = i;
					for (int j = 0; j < array.length; j++) {
						if (array[j] == 0) {
							array[j] = reference;
							added = true;
							break iteratingKeys;
						}
					}
				}
			}

			if (!added) {
				long[] newArray = new long[ARRAYS_SIZE];
				newArray[0] = reference;
				longKeysArray = keys.size();
				keys.add(newArray);
			}
		}

		void remove(Object reference) {

		}

		Iterator<Object> iterateReferences() {

			AtomicInteger index = new AtomicInteger();
			AtomicInteger arrayIndex = new AtomicInteger();
			return new LazyIterator<Object>() {
				@Override
				protected Object getNextOrNull() {

					while (true) {
						if (index.get() < keys.size()) {
							Object key = keys.get(index.get());

							if (key instanceof int[]) {
								int[] intKeys = ((int[]) key);
								int ref = intKeys[arrayIndex.get()];


								if (arrayIndex.incrementAndGet() >= intKeys.length) {
									arrayIndex.set(0);
									index.incrementAndGet();
								}

								if (ref != 0) {
									return ref;
								}

							} else if (key instanceof long[]) {
								long[] longKeys = ((long[]) key);
								long ref = longKeys[arrayIndex.get()];

								if (arrayIndex.incrementAndGet() >= longKeys.length) {
									arrayIndex.set(0);
									index.incrementAndGet();
								}

								if (ref != 0L) {
									return ref;
								}

							} else {
								index.incrementAndGet();
								return key;
							}

						} else {
							return null;
						}
					}
				}
			};
		}

	}
}
