package com.constellio.model.services.records.cache2;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.utils.EnumWithSmallCodeUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.constellio.model.entities.schemas.MetadataValueType.CONTENT;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.MetadataValueType.STRUCTURE;
import static com.constellio.model.entities.schemas.MetadataValueType.TEXT;

/**
 * This utility class handle the reading and writing of a byte array regrouping a Record DTO metadata values
 * <p>
 * <p>
 * | metadatasSizeBytesToKeepInMemory | metadata1Id | value1IndexInByteArray | metadataNId | valueNIndexInByteArray | allValues |
 * <p>
 * Metadatas size is stocked using 2 bytes (Short)
 * Metadata id are stocked using 2 bytes (Short)
 * Value index reprensent the index of the data in the byte array. These indexes are stocked using 2 bytes (Short)
 * The size of values are not stocked in the byte array, but they can be easily computed by comparing the value index
 * with the index of the next metadata
 * <p>
 * Integer values
 * <p>
 * Header size :
 * <p>
 * 3 => 14
 * 4 => 18
 */
public class CacheRecordDTOUtils {

	private static final byte BYTES_TO_WRITE_METADATA_ID_AND_INDEX = 2;
	private static final byte BYTES_TO_WRITE_METADATA_VALUES_SIZE = 2;
	private static final byte BYTES_TO_WRITE_BOOLEAN_VALUES_SIZE = 1;
	private static final byte BYTES_TO_WRITE_BYTE_VALUES_SIZE = 1;
	private static final byte BYTES_TO_WRITE_INTEGER_VALUES_SIZE = 4;
	private static final byte BYTES_TO_WRITE_DOUBLE_VALUES_SIZE = 8;
	private static final byte BYTES_TO_WRITE_LONG_VALUES_SIZE = 8;
	private static final byte BYTES_TO_WRITE_LOCAL_DATE_VALUES_SIZE = 3;

	public static final byte KEY_IS_NOT_AN_INT = 0;
	private static final byte KEY_LENGTH = 11;

	private static final byte VALUE_IS_NOT_FOUND = -1;

	public static class CacheRecordDTOBytesArray {
		byte[] bytesToKeepInMemory;
		byte[] bytesToPersist;

		public byte[] getBytesToKeepInMemory() {
			return bytesToKeepInMemory;
		}

		public byte[] getBytesToPersist() {
			return bytesToPersist;
		}
	}

	public static CacheRecordDTOBytesArray convertDTOToByteArrays(RecordDTO dto, MetadataSchema schema) {
		CachedRecordDTOByteArrayBuilder builder = new CachedRecordDTOByteArrayBuilder();

		for (Metadata metadata : schema.getSummaryMetadatas()) {
			if (metadata.isMultivalue()) {
				List<Object> values = (List<Object>) dto.getFields().get(metadata.getDataStoreCode());
				if (values != null && !values.isEmpty()) {
					switch (metadata.getType()) {
						case STRING:
						case STRUCTURE:
						case TEXT:
						case CONTENT:
							try {
								builder.addMultivalueStringMetadata(metadata, (List) values);
							} catch (IOException e) {
								e.printStackTrace();
							}
							break;
						case REFERENCE:
							try {
								builder.addMultivalueReferenceMetadata(metadata, (List) values);
							} catch (IOException e) {
								e.printStackTrace();
							}
							break;
						case BOOLEAN:
							try {
								builder.addMultivalueBooleanMetadata(metadata, (List) values);
							} catch (IOException e) {
								e.printStackTrace();
							}
							break;
						case INTEGER:
							try {
								builder.addMultivalueIntegerMetadata(metadata, (List) values);
							} catch (IOException e) {
								e.printStackTrace();
							}
							break;
						case NUMBER:
							try {
								builder.addMultivalueNumberMetadata(metadata, (List) values);
							} catch (IOException e) {
								e.printStackTrace();
							}
							break;
						case DATE:
							try {
								builder.addMultivalueLocalDateMetadata(metadata, (List) values);
							} catch (IOException e) {
								e.printStackTrace();
							}
							break;
						case DATE_TIME:
							try {
								builder.addMultivalueLocalDateTimeMetadata(metadata, (List) values);
							} catch (IOException e) {
								e.printStackTrace();
							}
							break;
						case ENUM:
							try {
								builder.addMultivalueEnumMetadata(metadata, (List) values);
							} catch (IOException e) {
								e.printStackTrace();
							}
							break;
					}
				}
			} else {
				Object value = dto.getFields().get(metadata.getDataStoreCode());
				if (value != null) {
					switch (metadata.getType()) {
						case REFERENCE:
							try {
								builder.addSingleValueReferenceMetadata(metadata, value);
							} catch (IOException e) {
								e.printStackTrace();
							}
							break;
						case BOOLEAN:
							try {
								builder.addSingleValueBooleanMetadata(metadata, value);
							} catch (IOException e) {
								e.printStackTrace();
							}
							break;
						case STRING:
						case STRUCTURE:
						case TEXT:
						case CONTENT:
							try {
								builder.addSingleValueStringMetadata(metadata, value);
							} catch (IOException e) {
								e.printStackTrace();
							}
							break;
						case INTEGER:
							try {
								builder.addSingleValueIntegerMetadata(metadata, value);
							} catch (IOException e) {
								e.printStackTrace();
							}
							break;
						case NUMBER:
							try {
								builder.addSingleValueNumberMetadata(metadata, value);
							} catch (IOException e) {
								e.printStackTrace();
							}
							break;
						case DATE:
							try {
								builder.addSingleValueLocalDateMetadata(metadata, value);
							} catch (IOException e) {
								e.printStackTrace();
							}
							break;
						case DATE_TIME:
							try {
								builder.addSingleValueLocalDateTimeMetadata(metadata, value);
							} catch (IOException e) {
								e.printStackTrace();
							}
							break;
						case ENUM:
							try {
								builder.addSingleValueEnumMetadata(metadata, value);
							} catch (IOException e) {
								e.printStackTrace();
							}
							break;
					}
				}
			}
		}

		try {
			return builder.build();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static boolean isMetatadataPersisted(Metadata metadata) {
		if (metadata.getType() == STRUCTURE || metadata.getType() == TEXT ||
			metadata.getType() == CONTENT || metadata.getType() == STRING) {
			return true;
		}

		return false;
	}

	public static int toIntKey(Object key) {
		if (key instanceof Integer) {
			return ((Integer) key);
		}

		if (key instanceof Long) {
			return KEY_IS_NOT_AN_INT;
		}

		if (key instanceof String) {
			long value = LangUtils.tryParseLong((String) key, 0);

			if (((String) key).length() == KEY_LENGTH && value < Integer.MAX_VALUE) {
				return (int) value;
			} else {
				return KEY_IS_NOT_AN_INT;
			}
		}

		throw new ImpossibleRuntimeException("Invalid key : " + key);
	}

	private static int threeByteArrayToInt(byte[] byteArray, short startingIndex) {
		// used to convert 3 bytes to a int that represent a LocalDate
		int i = (short) (128 * ((byte) (byteArray[startingIndex + 1] & (byte) 0x7f)) + byteArray[startingIndex + 2]);
		if (byteArray[startingIndex] >= 0) {
			return i * 127 + byteArray[startingIndex];
		} else {
			return -1 * (i * 127) + (byteArray[startingIndex] + 1);
		}
	}

	private static void intTo3ByteArray(int dateValue, byte[] bytes) {
		// used to convert LocalDate from int to 3 bytes
		if (dateValue >= 0) {
			bytes[0] = (byte) ((dateValue % 127));
			dateValue = dateValue / 127;
		} else {
			bytes[0] = (byte) (-1 * (1 + ((-1 * dateValue) % 127)));
			dateValue = ((dateValue * -1) / 127);
		}

		bytes[1] = (byte) (((short) (dateValue >> 7)) & ((short) 0x7f) | 0x80);
		bytes[2] = (byte) ((dateValue & ((short) 0x7f)));
	}

	public static <T> T readMetadata(byte[] byteArray, MetadataSchema schema, String metadataLocalCode,
									 Supplier<byte[]> persistedByteArraySupplier) {
		Metadata metadataSearched = schema.getMetadataByDatastoreCode(metadataLocalCode);

		byte[] byteArrayToSearchIn;
		if (metadataSearched == null) {
			return null;

		} else if (isMetatadataPersisted(metadataSearched)) {
			byteArrayToSearchIn = persistedByteArraySupplier.get();

		} else {
			byteArrayToSearchIn = byteArray;
		}

		NeighboringMetadata neighboringMetadata = getNeighboringMetadataSearchedIndexesInMemoryCache(byteArrayToSearchIn, metadataSearched.getId());
		short searchedMetadataIndex = neighboringMetadata.searchedMetadataIndex;
		short nextMetadataIndex = neighboringMetadata.nextMetadataIndex;

		return parseValueMetadata(byteArrayToSearchIn, metadataSearched, searchedMetadataIndex, nextMetadataIndex);
	}

	public static Set<String> getStoredMetadatas(byte[] byteArrayToKeepInMemory, MetadataSchema schema) {
		short metadatasSize = metadatasSize(byteArrayToKeepInMemory);
		Set<String> storedMetadatas = new HashSet<>();

		// skipping first two byte because it's the metadatasSizeBytesToKeepInMemory
		// i+=2*2 because we are just looking for the metadataId not the metadataValue
		short headerBytesSize = (short) ((metadatasSize * (BYTES_TO_WRITE_METADATA_ID_AND_INDEX + BYTES_TO_WRITE_METADATA_ID_AND_INDEX))
										 + BYTES_TO_WRITE_METADATA_ID_AND_INDEX);

		// if the value is persisted go up 2 bytes instead of 4 since we don<t have a index value in this byteArrayToKeepInMemory
		for (short i = BYTES_TO_WRITE_METADATA_ID_AND_INDEX; i < headerBytesSize; i += BYTES_TO_WRITE_METADATA_ID_AND_INDEX * 2) {
			short id = parseShortFromByteArray(byteArrayToKeepInMemory, i);
			Metadata storedMetadata = schema.getMetadataById(id);
			storedMetadatas.add(storedMetadata.getDataStoreCode());
		}

		return storedMetadatas;
	}

	public static Set<Object> getStoredValues(byte[] byteArray, MetadataSchema schema,
											  Supplier<byte[]> persistedByteArraySupplier) {
		short metadatasSize = metadatasSize(byteArray);
		Set<Object> storedValues = new HashSet<>();

		// *(2+2) for the bytes taken by the id and index of each metadata and +2 to skip the metadatasSizeBytesToKeepInMemory and the start of the array
		short headerBytesSize = (short) (metadatasSize * (BYTES_TO_WRITE_METADATA_ID_AND_INDEX + BYTES_TO_WRITE_METADATA_ID_AND_INDEX) + BYTES_TO_WRITE_METADATA_ID_AND_INDEX);

		for (short i = BYTES_TO_WRITE_METADATA_ID_AND_INDEX; i < headerBytesSize; i += (BYTES_TO_WRITE_METADATA_ID_AND_INDEX + BYTES_TO_WRITE_METADATA_ID_AND_INDEX)) {
			short id = parseShortFromByteArray(byteArray, i);
			// needed to know how to parse the value
			Metadata metadataSearched = schema.getMetadataById(id);

			byte[] byteArrayToUse;
			NeighboringMetadata neighboringMetadata;
			if (isMetatadataPersisted(metadataSearched)) {
				byteArrayToUse = persistedByteArraySupplier.get();
				neighboringMetadata = getNeighboringMetadataSearchedIndexesInPersistedCache(byteArrayToUse, id);
			} else {
				byteArrayToUse = byteArray;
				neighboringMetadata = getNeighboringMetadataSearchedIndexesInMemoryCache(byteArrayToUse, id);
			}

			// searchedMetadataIndex & nextMetadataIndex are needed to know where to start and stop parsing the value
			storedValues.add(parseValueMetadata(byteArrayToUse, metadataSearched, neighboringMetadata.searchedMetadataIndex, neighboringMetadata.nextMetadataIndex));
		}

		return storedValues;
	}

	public static Set<Entry<String, Object>> toEntrySet(byte[] byteArray, MetadataSchema schema,
														Supplier<byte[]> persistedByteArraySupplier) {
		Set<Entry<String, Object>> metadatasEntrySet = new HashSet<>();

		short metadatasSize = metadatasSize(byteArray);

		// *(2+2) for the bytes taken by the id and index of each metadata and +2 to skip the metadatasSizeBytesToKeepInMemory and the start of the array
		short headerBytesSize = (short) (metadatasSize * (BYTES_TO_WRITE_METADATA_ID_AND_INDEX + BYTES_TO_WRITE_METADATA_ID_AND_INDEX) + BYTES_TO_WRITE_METADATA_ID_AND_INDEX);

		for (short i = BYTES_TO_WRITE_METADATA_ID_AND_INDEX; i < headerBytesSize; i += (BYTES_TO_WRITE_METADATA_ID_AND_INDEX + BYTES_TO_WRITE_METADATA_ID_AND_INDEX)) {
			short id = parseShortFromByteArray(byteArray, i);
			// needed to know how to parse the value
			Metadata metadataSearched = schema.getMetadataById(id);

			byte[] byteArrayToUse;
			NeighboringMetadata neighboringMetadata;
			if (isMetatadataPersisted(metadataSearched)) {
				byteArrayToUse = persistedByteArraySupplier.get();
				neighboringMetadata = getNeighboringMetadataSearchedIndexesInPersistedCache(byteArrayToUse, id);
			} else {
				byteArrayToUse = byteArray;
				neighboringMetadata = getNeighboringMetadataSearchedIndexesInMemoryCache(byteArrayToUse, id);
			}

			// searchedMetadataIndex & nextMetadataIndex are needed to know where to start and stop parsing the value
			metadatasEntrySet.add(new SimpleEntry(metadataSearched.getDataStoreCode(),
					parseValueMetadata(byteArrayToUse, metadataSearched, neighboringMetadata.searchedMetadataIndex, neighboringMetadata.nextMetadataIndex)));
		}

		return metadatasEntrySet;
	}

	public static boolean containsMetadata(byte[] data, MetadataSchema schema, String key) {
		short metadatasSize = metadatasSize(data);
		Metadata metadataSearched = schema.getMetadataByDatastoreCode(key);

		short metadataSearchedId = metadataSearched.getId();

		// skipping first two byte because it's the metadatasSizeBytesToKeepInMemory
		// i+=2*2 because we are just looking for the metadataId not the metadataValue
		short headerBytesSize = (short) ((metadatasSize * (BYTES_TO_WRITE_METADATA_ID_AND_INDEX + BYTES_TO_WRITE_METADATA_ID_AND_INDEX))
										 + BYTES_TO_WRITE_METADATA_ID_AND_INDEX);

		// if the value is persisted go up 2 bytes instead of 4 since we don<t have a index value in this byteArrayToKeepInMemory
		for (short i = BYTES_TO_WRITE_METADATA_ID_AND_INDEX; i < headerBytesSize; i += BYTES_TO_WRITE_METADATA_ID_AND_INDEX * 2) {
			short id = parseShortFromByteArray(data, i);

			if (id == metadataSearchedId) {
				return true;
			}
		}

		return false;
	}

	public static short metadatasSize(byte[] data) {
		// returns the first 2 bytes converted as a short because its where the metadatasSizeBytesToKeepInMemory is stored
		return parseShortFromByteArray(data, (short) 0);
	}

	private static NeighboringMetadata getNeighboringMetadataSearchedIndexesInMemoryCache(byte[] byteArray,
																						  short metadataSearchedId) {
		short metadatasSize = metadatasSize(byteArray);

		NeighboringMetadata neighboringMetadata = new NeighboringMetadata();

		// *(2+2) for the bytes taken by the id and index of each metadata and +2 to skip the metadatasSizeBytesToKeepInMemory and the start of the array
		short headerBytesSize = (short) ((metadatasSize * (BYTES_TO_WRITE_METADATA_ID_AND_INDEX + BYTES_TO_WRITE_METADATA_ID_AND_INDEX))
										 + BYTES_TO_WRITE_METADATA_ID_AND_INDEX);

		// skipping first two byte because it's the metadatasSizeBytesToKeepInMemory
		// i+=2*2 because we are just looking for the metadataId not the metadataValue
		for (short i = BYTES_TO_WRITE_METADATA_ID_AND_INDEX; i < headerBytesSize; i += BYTES_TO_WRITE_METADATA_ID_AND_INDEX * 2) {
			short id = parseShortFromByteArray(byteArray, i);

			if (id == metadataSearchedId) {
				// Looking for next 2 bytes to get the index in the data part of the array
				neighboringMetadata.searchedMetadataIndex = (short) (headerBytesSize + parseShortFromByteArray(byteArray, (short) (i + BYTES_TO_WRITE_METADATA_ID_AND_INDEX)));
				neighboringMetadata.nextMetadataIndex = calculateNextMetadataIndex(byteArray, headerBytesSize, i);

				return neighboringMetadata;
			}
		}

		return neighboringMetadata;
	}

	private static NeighboringMetadata getNeighboringMetadataSearchedIndexesInPersistedCache(byte[] persistedByteArray,
																							 short metadataSearchedId) {
		NeighboringMetadata neighboringMetadata = new NeighboringMetadata();
		short metadatasSize = metadatasSize(persistedByteArray);

		short headerBytesSize = (short) (metadatasSize * (BYTES_TO_WRITE_METADATA_ID_AND_INDEX + BYTES_TO_WRITE_METADATA_ID_AND_INDEX) + BYTES_TO_WRITE_METADATA_ID_AND_INDEX);

		for (short i = BYTES_TO_WRITE_METADATA_ID_AND_INDEX; i < headerBytesSize; i += (BYTES_TO_WRITE_METADATA_ID_AND_INDEX + BYTES_TO_WRITE_METADATA_ID_AND_INDEX)) {
			short id = parseShortFromByteArray(persistedByteArray, i);

			if (metadataSearchedId == id) {
				neighboringMetadata.searchedMetadataIndex = (short) (headerBytesSize + parseShortFromByteArray(persistedByteArray, (short) (i + BYTES_TO_WRITE_METADATA_ID_AND_INDEX)));
				neighboringMetadata.nextMetadataIndex = calculateNextMetadataIndex(persistedByteArray, headerBytesSize, i);
			}
		}

		return neighboringMetadata;
	}

	private static short calculateNextMetadataIndex(byte[] byteArray, short headerBytesSize,
													short currentPositionInArray) {
		// 2 * 3 to get the next index in the header
		short possibleNextMetadataIndex = (short) (currentPositionInArray + (BYTES_TO_WRITE_METADATA_ID_AND_INDEX * 3));
		// +1 to complete the 2 bytes taken by the short
		// if it's NOT greater than the size of the header then the next index is the result of the parsing
		// else it's the metadata of the byte array keep reading until the end of the array
		if (!(possibleNextMetadataIndex + 1 > headerBytesSize)) {
			return (short) (headerBytesSize + parseShortFromByteArray(byteArray, possibleNextMetadataIndex));
		} else {
			// Not doing -1 even though we want the last index because when we use
			// Arrays.copyRange the param "to" is exclusive and instead of doing a minus here
			// and a plus further down we just keep the value as is.
			return (short) byteArray.length;
		}
	}

	private static <T> T parseValueMetadata(byte[] byteArray, Metadata metadataSearched, short metadataSearchedIndex,
											short nextMetadataIndex) {
		if (metadataSearched.isMultivalue()) {
			if (isIndexValid(metadataSearchedIndex)) {
				switch (metadataSearched.getType()) {
					case BOOLEAN:
						return (T) getMultivalueBooleanMetadata(byteArray, metadataSearchedIndex);
					case REFERENCE:
						return (T) getMultivalueReferenceMetadata(byteArray, metadataSearchedIndex);
					case STRING:
					case STRUCTURE:
					case TEXT:
					case CONTENT:
						return (T) getMultivalueStringMetadata(byteArray, metadataSearchedIndex);
					case INTEGER:
						return (T) getMultivalueIntegerMetadata(byteArray, metadataSearchedIndex);
					case NUMBER:
						return (T) getMultivalueNumberMetadata(byteArray, metadataSearchedIndex);
					case DATE:
						return (T) getMultivalueLocalDateMetadata(byteArray, metadataSearchedIndex);
					case DATE_TIME:
						return (T) getMultivalueLocalDateTimeMetadata(byteArray, metadataSearchedIndex);
					case ENUM:
						return (T) getMultivalueEnumMetadata(byteArray, metadataSearchedIndex, metadataSearched);
				}
			}

			// returns a empty array even if no value is stored in the cache to mimic Solr
			return (T) new ArrayList<>();
		} else {
			if (isIndexValid(metadataSearchedIndex)) {
				switch (metadataSearched.getType()) {
					case BOOLEAN:
						return (T) getSingleValueBooleanMetadata(byteArray, metadataSearchedIndex);
					case REFERENCE:
						return (T) getSingleValueReferenceMetadata(byteArray, metadataSearchedIndex);
					case STRING:
					case STRUCTURE:
					case TEXT:
					case CONTENT:
						return (T) getSingleValueStringMetadata(byteArray, metadataSearchedIndex, nextMetadataIndex);
					case INTEGER:
						return (T) getSingleValueIntegerMetadata(byteArray, metadataSearchedIndex);
					case NUMBER:
						return (T) getSingleValueNumberMetadata(byteArray, metadataSearchedIndex);
					case DATE:
						return (T) getSingleValueLocalDateMetadata(byteArray, metadataSearchedIndex);
					case DATE_TIME:
						return (T) getSingleValueLocalDateTimeMetadata(byteArray, metadataSearchedIndex);
					case ENUM:
						return (T) getSingleValueEnumMetadata(byteArray, metadataSearchedIndex, metadataSearched);
				}
			}

			return null;
		}
	}

	private static boolean isIndexValid(short index) {
		if (VALUE_IS_NOT_FOUND == index) {
			return false;
		} else {
			return true;
		}
	}

	private static Boolean getSingleValueBooleanMetadata(byte[] byteArray, short metadataSearchedIndex) {
		return parseBooleanFromByteArray(byteArray, metadataSearchedIndex);
	}

	private static List<Boolean> getMultivalueBooleanMetadata(byte[] byteArray, short metadataSearchedIndex) {
		short numberOfMetadatas = parseShortFromByteArray(byteArray, metadataSearchedIndex);

		if (numberOfMetadatas > 0) {
			List<Boolean> booleans = new ArrayList<>();

			short numberOfMetadatasFound = 0;
			// + 2 since we don't want to parse the size again
			short currentIndex = (short) (metadataSearchedIndex + BYTES_TO_WRITE_METADATA_VALUES_SIZE);

			while (numberOfMetadatas != numberOfMetadatasFound) {
				booleans.add(parseBooleanFromByteArray(byteArray, currentIndex));
				currentIndex += BYTES_TO_WRITE_BOOLEAN_VALUES_SIZE;
				numberOfMetadatasFound++;
			}

			return booleans;
		}

		return null;
	}

	private static String getSingleValueReferenceMetadata(byte[] byteArray, short metadataSearchedIndex) {
		int stringValue = parseIntFromByteArray(byteArray, metadataSearchedIndex);

		// if the value is higher than 0 it's positive so it is a regular id
		// else if it's negative it's a string containing letters most likely
		if (stringValue > 0) {
			return formatToId(stringValue);
		} else {
			return parseStringReferenceFromByteArray(byteArray, metadataSearchedIndex);
		}
	}

	private static List<String> getMultivalueReferenceMetadata(byte[] byteArray, short metadataSearchedIndex) {
		short numberOfMetadatas = parseShortFromByteArray(byteArray, metadataSearchedIndex);

		if (numberOfMetadatas > 0) {
			List<String> references = new ArrayList<>();

			short numberOfMetadatasFound = 0;
			// + 2 since we don't want to parse the size again
			short currentIndex = (short) (metadataSearchedIndex + BYTES_TO_WRITE_METADATA_VALUES_SIZE);

			while (numberOfMetadatas != numberOfMetadatasFound) {
				int stringValue = parseIntFromByteArray(byteArray, currentIndex);

				// if the value is higher than 0 it's positive so it is a regular id
				// if it's equal to 0 the value is null
				// else it's negative and it's a string containing letters most likely
				if (stringValue > 0) {
					references.add(formatToId(stringValue));
					currentIndex += BYTES_TO_WRITE_INTEGER_VALUES_SIZE;
				} else if (stringValue == 0) {
					references.add(null);
					currentIndex += BYTES_TO_WRITE_INTEGER_VALUES_SIZE;
				} else {
					references.add(parseStringReferenceFromByteArray(byteArray, currentIndex));
					// in this case the stringValue represent the size of bytes taken by the string (1 each char) or the bytes length if you prefer
					// * -1 to get a positive value since the length of strings is stored as a negative
					// + 4 for the bytes taken by the size value
					currentIndex += stringValue * -1 + BYTES_TO_WRITE_INTEGER_VALUES_SIZE;
				}

				numberOfMetadatasFound++;
			}

			return references;
		}

		return null;
	}

	private static Integer getSingleValueIntegerMetadata(byte[] byteArray, short metadataSearchedIndex) {
		return parseIntFromByteArray(byteArray, metadataSearchedIndex);
	}

	private static List<Integer> getMultivalueIntegerMetadata(byte[] byteArray, short metadataSearchedIndex) {
		short numberOfMetadatas = parseShortFromByteArray(byteArray, metadataSearchedIndex);

		if (numberOfMetadatas > 0) {
			List<Integer> integers = new ArrayList<>();

			short numberOfMetadatasFound = 0;
			// + 2 since we don't want to parse the size again
			short currentIndex = (short) (metadataSearchedIndex + BYTES_TO_WRITE_METADATA_VALUES_SIZE);

			while (numberOfMetadatas != numberOfMetadatasFound) {
				integers.add(parseIntFromByteArray(byteArray, currentIndex));
				currentIndex += BYTES_TO_WRITE_INTEGER_VALUES_SIZE;

				numberOfMetadatasFound++;
			}

			return integers;
		}

		return null;
	}

	private static Double getSingleValueNumberMetadata(byte[] byteArray, short metadataSearchedIndex) {
		return parseDoubleFromByteArray(byteArray, metadataSearchedIndex);
	}

	private static List<Double> getMultivalueNumberMetadata(byte[] byteArray, short metadataSearchedIndex) {
		short numberOfMetadatas = parseShortFromByteArray(byteArray, metadataSearchedIndex);

		if (numberOfMetadatas > 0) {
			List<Double> doubles = new ArrayList<>();

			short numberOfMetadatasFound = 0;
			// + 2 since we don't want to parse the size again
			short currentIndex = (short) (metadataSearchedIndex + BYTES_TO_WRITE_METADATA_VALUES_SIZE);

			while (numberOfMetadatas != numberOfMetadatasFound) {
				doubles.add(parseDoubleFromByteArray(byteArray, currentIndex));
				currentIndex += BYTES_TO_WRITE_DOUBLE_VALUES_SIZE;

				numberOfMetadatasFound++;
			}

			return doubles;
		}

		return null;
	}

	private static String getSingleValueStringMetadata(byte[] byteArray, short metadataSearchedIndex,
													   short nextMetadataIndex) {
		byte[] stringValueAsByte = Arrays.copyOfRange(byteArray, metadataSearchedIndex, nextMetadataIndex);

		return new String(stringValueAsByte);
	}

	private static List<String> getMultivalueStringMetadata(byte[] byteArray, short metadataSearchedIndex) {
		short numberOfMetadatas = parseShortFromByteArray(byteArray, metadataSearchedIndex);

		if (numberOfMetadatas > 0) {
			List<String> strings = new ArrayList<>();

			short numberOfMetadatasFound = 0;
			// + 2 since we don't want to parse the size again
			short currentIndex = (short) (metadataSearchedIndex + BYTES_TO_WRITE_METADATA_VALUES_SIZE);

			while (numberOfMetadatas != numberOfMetadatasFound) {
				// in this case the stringValue represent the size of bytes taken by the string (1 each char) or the bytes length if you prefer
				int stringValue = parseIntFromByteArray(byteArray, currentIndex);

				// if the stringValue is higher then 0 it's a valid string
				// else if the stringValue lower or equal to zero the string is null
				if (stringValue > 0) {
					strings.add(parseStringWithLengthFromByteArray(byteArray, currentIndex));

					// + 4 for the bytes taken by the size value
					currentIndex += stringValue + BYTES_TO_WRITE_INTEGER_VALUES_SIZE;

					// negative string length is a null
				} else if (stringValue <= 0) {
					strings.add(null);
					// + 2 only because we only need the size to know if it's null
					currentIndex += BYTES_TO_WRITE_INTEGER_VALUES_SIZE;
				}

				numberOfMetadatasFound++;
			}

			return strings;
		}

		return null;
	}

	private static LocalDate getSingleValueLocalDateMetadata(byte[] byteArray, short metadataSearchedIndex) {
		return parseLocalDateFromByteArray(byteArray, metadataSearchedIndex);
	}

	private static List<LocalDate> getMultivalueLocalDateMetadata(byte[] byteArray, short metadataSearchedIndex) {
		short numberOfMetadatas = parseShortFromByteArray(byteArray, metadataSearchedIndex);

		if (numberOfMetadatas > 0) {
			List<LocalDate> dates = new ArrayList<>();

			short numberOfMetadatasFound = 0;
			// + 2 since we don't want to parse the size again
			short currentIndex = (short) (metadataSearchedIndex + BYTES_TO_WRITE_METADATA_VALUES_SIZE);

			while (numberOfMetadatas != numberOfMetadatasFound) {
				Object isValueNull = parseBooleanFromByteArray(byteArray, currentIndex);
				currentIndex += BYTES_TO_WRITE_BYTE_VALUES_SIZE;

				if (null == isValueNull) {
					dates.add(null);
				} else {
					dates.add(parseLocalDateFromByteArray(byteArray, currentIndex));
					currentIndex += BYTES_TO_WRITE_LOCAL_DATE_VALUES_SIZE;
				}

				numberOfMetadatasFound++;
			}

			return dates;
		}

		return null;
	}

	private static LocalDateTime getSingleValueLocalDateTimeMetadata(byte[] byteArray, short metadataSearchedIndex) {
		return parseLocalDateTimeFromByteArray(byteArray, metadataSearchedIndex);
	}

	private static List<LocalDateTime> getMultivalueLocalDateTimeMetadata(byte[] byteArray,
																		  short metadataSearchedIndex) {
		short numberOfMetadatas = parseShortFromByteArray(byteArray, metadataSearchedIndex);

		if (numberOfMetadatas > 0) {
			List<LocalDateTime> dates = new ArrayList<>();

			short numberOfMetadatasFound = 0;
			// + 2 since we don't want to parse the size again
			short currentIndex = (short) (metadataSearchedIndex + BYTES_TO_WRITE_METADATA_VALUES_SIZE);

			while (numberOfMetadatas != numberOfMetadatasFound) {
				Object isValueNull = parseBooleanFromByteArray(byteArray, currentIndex);
				currentIndex += BYTES_TO_WRITE_BYTE_VALUES_SIZE;

				if (null == isValueNull) {
					dates.add(null);
				} else {
					dates.add(parseLocalDateTimeFromByteArray(byteArray, currentIndex));
					// + 8 for the bytes taken by the epoch time of the LocalDateTime
					currentIndex += BYTES_TO_WRITE_LONG_VALUES_SIZE;
				}

				numberOfMetadatasFound++;
			}

			return dates;
		}

		return null;
	}

	private static String getSingleValueEnumMetadata(byte[] byteArray, short metadataSearchedIndex,
													 Metadata metadataSearched) {
		return parseEnumFromByteArray(metadataSearched.getEnumClass(), byteArray[metadataSearchedIndex]);
	}

	private static List<String> getMultivalueEnumMetadata(byte[] byteArray, short metadataSearchedIndex,
														  Metadata metadataSearched) {
		short numberOfMetadatas = parseShortFromByteArray(byteArray, metadataSearchedIndex);

		if (numberOfMetadatas > 0) {
			List<String> enums = new ArrayList<>();

			short numberOfMetadatasFound = 0;
			// + 2 since we don't want to parse the size again
			short currentIndex = (short) (metadataSearchedIndex + BYTES_TO_WRITE_METADATA_VALUES_SIZE);

			while (numberOfMetadatas != numberOfMetadatasFound) {
				enums.add(parseEnumFromByteArray(metadataSearched.getEnumClass(), byteArray[currentIndex]));
				currentIndex += BYTES_TO_WRITE_BYTE_VALUES_SIZE;

				numberOfMetadatasFound++;
			}

			return enums;
		}

		return null;
	}

	private static String formatToId(int id) {
		// rebuild the id to have have the right length (ex: 8 -> "00000000008")
		return String.format("%0" + KEY_LENGTH + "d", id);
	}

	private static Boolean parseBooleanFromByteArray(byte[] byteArray, short startingIndex) {
		return byteArray[startingIndex] == (byte) -1 ? null : byteArray[startingIndex] == (byte) 1 ? true : false;
	}

	private static short parseShortFromByteArray(byte[] byteArray, short startingIndex) {
		return (short) (((byteArray[startingIndex] & 0xFF) << 8) | (byteArray[startingIndex + 1] & 0xFF));
	}

	private static int parseIntFromByteArray(byte[] byteArray, short startingIndex) {
		return ((int) (byteArray[startingIndex] & 0xFF)) << 24 | ((int) (byteArray[startingIndex + 1] & 0xFF)) << 16 |
			   ((int) (byteArray[startingIndex + 2] & 0xFF)) << 8 | ((int) (byteArray[startingIndex + 3] & 0xFF));
	}

	private static String parseStringReferenceFromByteArray(byte[] byteArray, short startingIndex) {
		// * -1 to get the positive value of the bytes length of the array since it's stored as a negative
		// to not confuse a string and a id when parsing
		int stringBytesLength = -1 * parseIntFromByteArray(byteArray, startingIndex);

		// + 4 to skip the string length 4 bytes
		short startingStringPosition = (short) (startingIndex + BYTES_TO_WRITE_INTEGER_VALUES_SIZE);
		byte[] stringValueAsByte = Arrays.copyOfRange(byteArray, startingStringPosition, startingStringPosition + stringBytesLength);

		return new String(stringValueAsByte);
	}

	private static String parseStringWithLengthFromByteArray(byte[] byteArray, short startingIndex) {
		// 2 * 3 to get the next index in the header
		int stringBytesLength = parseIntFromByteArray(byteArray, startingIndex);

		// + 4 to skip the string length 4 bytes
		short startingStringPosition = (short) (startingIndex + BYTES_TO_WRITE_INTEGER_VALUES_SIZE);
		byte[] stringValueAsByte = Arrays.copyOfRange(byteArray, startingStringPosition, startingStringPosition + stringBytesLength);

		return new String(stringValueAsByte);
	}

	private static double parseDoubleFromByteArray(byte[] byteArray, short startingIndex) {
		return Double.longBitsToDouble(parseLongFromByteArray(byteArray, startingIndex));
	}

	private static long parseLongFromByteArray(byte[] byteArray, short startingIndex) {
		return ((long) (byteArray[startingIndex] & 0xFF)) << 56 | ((long) (byteArray[startingIndex + 1] & 0xFF)) << 48 |
			   ((long) (byteArray[startingIndex + 2] & 0xFF)) << 40 | ((long) (byteArray[startingIndex + 3] & 0xFF)) << 32 |
			   ((long) (byteArray[startingIndex + 4] & 0xFF)) << 24 | ((long) (byteArray[startingIndex + 5] & 0xFF)) << 16 |
			   ((long) (byteArray[startingIndex + 6] & 0xFF)) << 8 | ((long) (byteArray[startingIndex + 7] & 0xFF));
	}

	private static LocalDate parseLocalDateFromByteArray(byte[] byteArray, short startingIndex) {
		// all the operations on the date makes it possible to have it stored in 3 bytes instead of 8 in epoch time
		// the value can range from the first of january of the year -1000 to 7000
		int value = threeByteArrayToInt(byteArray, startingIndex);
		short dayMonth = (short) ((value < 0 ? value * -1 : value) % 400);
		int deltaYear = value / 400;
		int year = deltaYear + 1900;
		short month = (short) (1 + dayMonth / 31);
		short day = (short) (1 + dayMonth % 31);

		return new LocalDate(year, month, day);
	}

	private static LocalDateTime parseLocalDateTimeFromByteArray(byte[] byteArray, short startingIndex) {
		long epochTimeInMillis = parseLongFromByteArray(byteArray, startingIndex);

		return new LocalDateTime(epochTimeInMillis);
	}

	private static String parseEnumFromByteArray(Class<? extends Enum> clazz, byte value) {
		try {
			// - acts as a + since Byte.MIN_VALUE is -128
			return ((EnumWithSmallCode) ((Object[]) clazz.getMethod("values").invoke(null))[((int) value - Byte.MIN_VALUE)]).getCode();
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	private static class NeighboringMetadata {
		private short searchedMetadataIndex = VALUE_IS_NOT_FOUND;
		private short nextMetadataIndex;

		public short getSearchedMetadataIndex() {
			return searchedMetadataIndex;
		}

		public void setSearchedMetadataIndex(short searchedMetadataIndex) {
			this.searchedMetadataIndex = searchedMetadataIndex;
		}

		public short getNextMetadataIndex() {
			return nextMetadataIndex;
		}

		public void setNextMetadataIndex(short nextMetadataIndex) {
			this.nextMetadataIndex = nextMetadataIndex;
		}
	}

	private static class CachedRecordDTOByteArrayBuilder {

		private int headerByteArrayLengthBytesToKeepInMemory;
		private int dataByteArrayLengthBytesToKeepInMemory;
		private short metadatasSizeBytesToKeepInMemory;

		private int headerByteArrayLengthBytesToPersist;
		private int dataByteArrayLengthBytesToPersist;
		private short metadatasSizeBytesToPersist;

		private ByteArrayOutputStream headerOutputBytesToKeepInMemory;
		private ByteArrayOutputStream dataOutputBytesToKeepInMemory;
		private DataOutputStream headerWriterBytesToKeepInMemory;
		private DataOutputStream dataWriterBytesToKeepInMemory;

		private ByteArrayOutputStream headerOutputBytesToPersist;
		private ByteArrayOutputStream dataOutputBytesToPersist;
		private DataOutputStream headerWriterBytesToPersist;
		private DataOutputStream dataWriterBytesToPersist;

		public CachedRecordDTOByteArrayBuilder() {
			this.headerByteArrayLengthBytesToKeepInMemory = 0;
			this.dataByteArrayLengthBytesToKeepInMemory = 0;
			this.metadatasSizeBytesToKeepInMemory = 0;

			this.headerByteArrayLengthBytesToPersist = 0;
			this.dataByteArrayLengthBytesToPersist = 0;
			this.metadatasSizeBytesToPersist = 0;

			this.headerOutputBytesToKeepInMemory = new ByteArrayOutputStream();
			this.dataOutputBytesToKeepInMemory = new ByteArrayOutputStream();
			this.headerWriterBytesToKeepInMemory = new DataOutputStream(headerOutputBytesToKeepInMemory);
			this.dataWriterBytesToKeepInMemory = new DataOutputStream(dataOutputBytesToKeepInMemory);

			this.headerOutputBytesToPersist = new ByteArrayOutputStream();
			this.dataOutputBytesToPersist = new ByteArrayOutputStream();
			this.headerWriterBytesToPersist = new DataOutputStream(headerOutputBytesToPersist);
			this.dataWriterBytesToPersist = new DataOutputStream(dataOutputBytesToPersist);
		}

		/**
		 * Value is stored using 1 byte
		 */
		private void addSingleValueBooleanMetadata(Metadata metadata, Object value) throws IOException {
			dataWriterBytesToKeepInMemory.writeByte(((boolean) value ? 1 : 0));

			writeHeader(metadata);

			dataByteArrayLengthBytesToKeepInMemory += BYTES_TO_WRITE_BOOLEAN_VALUES_SIZE;
			metadatasSizeBytesToKeepInMemory++;
		}

		private void addMultivalueBooleanMetadata(Metadata metadata, List<Boolean> metadatas) throws IOException {
			// We don't want to write in the array if all the values we have are nulls to mimic Solr
			if (metadatas.stream().allMatch(x -> x == null)) {
				return;
			}

			short listSize = (short) metadatas.size();
			writeMultivalueSize(metadata, listSize);

			for (Iterator i = metadatas.iterator(); i.hasNext(); ) {
				// the index gets put in a object since a boolean cant be null and we need to keep the result
				Object value = i.next();

				// if the value of the boolean is null, write -1
				// else if the value if true write 1 (true) else 0 (false)
				dataWriterBytesToKeepInMemory.writeByte(value == null ? -1 : (boolean) value ? 1 : 0);
				dataByteArrayLengthBytesToKeepInMemory += BYTES_TO_WRITE_BOOLEAN_VALUES_SIZE;
			}

			metadatasSizeBytesToKeepInMemory++;
		}

		/**
		 * Value is usually integer, but can also be String or Null.
		 * <p>
		 * Integer value is stocked using 4 bytes (Integer)
		 * String value is stocked using a 4 bytes negative value (Integer), where the value represent the size of bytes used to store the String value the characters are stored as bytes
		 */
		private void addSingleValueReferenceMetadata(Metadata metadata, Object value) throws IOException {
			// the key is the result of a parsing of the string
			// 0 means the value in the string is not a number
			int key = toIntKey(value);
			short size = 0;

			if (key != KEY_IS_NOT_AN_INT) {
				dataWriterBytesToKeepInMemory.writeInt(key);
			} else {
				size = (short) value.toString().getBytes(StandardCharsets.UTF_8).length;
				writeStringReference((String) value, size);
			}

			writeHeader(metadata);

			// + size if it's a string to represent each byte taken by a char
			// else 0 to not take unused space
			dataByteArrayLengthBytesToKeepInMemory += BYTES_TO_WRITE_INTEGER_VALUES_SIZE + size;

			metadatasSizeBytesToKeepInMemory++;
		}

		/**
		 * Data is structured like this :
		 * | listSize | value1 | value 2 | value N |
		 * <p>
		 * listSize is stocked using 2 bytes (Short)
		 * <p>
		 * Values are usually integer, but can also be String or Null.
		 * <p>
		 * Integer value is stocked using 4 bytes (Integer)
		 * Null value is stocked using a 4 bytes "zero value" (Integer)
		 * String value is stocked using a 4 bytes negative value (Integer), where the value represent the size of bytes used to store the String value
		 */
		private void addMultivalueReferenceMetadata(Metadata metadata, List<String> metadatas)
				throws IOException {
			writeMultivalueSize(metadata, (short) metadatas.size());

			for (String value : metadatas) {
				short size = 0;
				if (null != value) {
					int key = toIntKey(value);

					if (key != KEY_IS_NOT_AN_INT) {
						dataWriterBytesToKeepInMemory.writeInt(key);
					} else {
						size = (short) value.getBytes(StandardCharsets.UTF_8).length;
						writeStringReference(value, size);
					}
				} else {
					dataWriterBytesToKeepInMemory.writeInt(0);
				}

				// + size if it's a string to represent each byte taken by a char
				dataByteArrayLengthBytesToKeepInMemory += BYTES_TO_WRITE_INTEGER_VALUES_SIZE + size;
			}

			metadatasSizeBytesToKeepInMemory++;
		}

		private void addSingleValueStringMetadata(Metadata metadata, Object value) throws IOException {
			byte[] string = ((String) value).getBytes();

			// if empty string not worth writing
			if (0 < string.length) {
				// Strings are kept in the persisted cache
				dataWriterBytesToPersist.write(string);

				writeHeader(metadata);

				dataByteArrayLengthBytesToPersist += string.length;
				metadatasSizeBytesToPersist++;
				// +1 in the memory too since we need it to know the size and furthermore the contains of the byte array
				// even though there's no value associated to it in this byte array, just the id is present
				metadatasSizeBytesToKeepInMemory++;
			}
		}

		private void addMultivalueStringMetadata(Metadata metadata, List<String> metadatas) throws IOException {
			// We don't want to write in the array if all the values we have are nulls to mimic Solr
			if (metadatas.stream().allMatch(x -> x == null)) {
				return;
			}

			// Filters out empty strings but keep nulls to mimic Solr
			List<String> strings = metadatas.stream()
					.filter(s -> (s == null || s.getBytes(StandardCharsets.UTF_8).length > 0))
					.collect(Collectors.toList());

			writeMultivalueSize(metadata, (short) strings.size());

			for (String string : strings) {
				short size = 0;
				if (null == string) {
					dataWriterBytesToPersist.writeInt(0);
				} else {
					size = (short) string.getBytes(StandardCharsets.UTF_8).length;
					writeStringWithLength(string, size);
				}

				// + size if it's a string to represent each byte taken by a char
				dataByteArrayLengthBytesToPersist += BYTES_TO_WRITE_INTEGER_VALUES_SIZE + size;
			}

			metadatasSizeBytesToPersist++;
			// +1 in the memory too since we need it to know the size and furthermore the contains of the byte array
			// even though there's no value associated to it in this byte array, just the id is present
			metadatasSizeBytesToKeepInMemory++;
		}

		private void addSingleValueIntegerMetadata(Metadata metadata, Object value) throws IOException {
			// TODO Find a better way
			// This exist only because the value sometimes comes as a Double and other time as int
			if (value instanceof Double) {
				dataWriterBytesToKeepInMemory.writeInt(((Double) value).intValue());
			} else {
				dataWriterBytesToKeepInMemory.writeInt((int) value);
			}

			writeHeader(metadata);

			dataByteArrayLengthBytesToKeepInMemory += BYTES_TO_WRITE_INTEGER_VALUES_SIZE;
			metadatasSizeBytesToKeepInMemory++;
		}

		private void addMultivalueIntegerMetadata(Metadata metadata, List<Integer> metadatas) throws IOException {
			writeMultivalueSize(metadata, (short) metadatas.size());

			for (int value : metadatas) {
				dataWriterBytesToKeepInMemory.writeInt(value);

				dataByteArrayLengthBytesToKeepInMemory += BYTES_TO_WRITE_INTEGER_VALUES_SIZE;
			}

			metadatasSizeBytesToKeepInMemory++;
		}

		private void addSingleValueNumberMetadata(Metadata metadata, Object value) throws IOException {
			dataWriterBytesToKeepInMemory.writeDouble((double) value);

			writeHeader(metadata);

			dataByteArrayLengthBytesToKeepInMemory += BYTES_TO_WRITE_DOUBLE_VALUES_SIZE;
			metadatasSizeBytesToKeepInMemory++;
		}

		private void addMultivalueNumberMetadata(Metadata metadata, List<Double> metadatas) throws IOException {
			writeMultivalueSize(metadata, (short) metadatas.size());

			for (double value : metadatas) {
				dataWriterBytesToKeepInMemory.writeDouble(value);

				dataByteArrayLengthBytesToKeepInMemory += BYTES_TO_WRITE_DOUBLE_VALUES_SIZE;
			}

			metadatasSizeBytesToKeepInMemory++;
		}

		private void addSingleValueLocalDateMetadata(Metadata metadata, Object value) throws IOException {
			writeLocalDate((LocalDate) value);

			writeHeader(metadata);

			dataByteArrayLengthBytesToKeepInMemory += BYTES_TO_WRITE_LOCAL_DATE_VALUES_SIZE;
			metadatasSizeBytesToKeepInMemory++;
		}

		private void addMultivalueLocalDateMetadata(Metadata metadata, List<LocalDate> metadatas) throws IOException {
			// We don't want to write in the array if all the values we have are nulls to mimic Solr
			if (metadatas.stream().allMatch(x -> x == null)) {
				return;
			}

			writeMultivalueSize(metadata, (short) metadatas.size());

			for (Iterator i = metadatas.iterator(); i.hasNext(); ) {
				// the index gets put in a object since it can be null and we need to keep the result
				Object date = i.next();

				// only for multivalue local date and localdate, a byte is added before the 3 bytes of the date to differentiate null values
				// 1 = a NON null value
				// -1 = a null value
				if (null != date) {
					dataWriterBytesToKeepInMemory.writeByte(1);
					writeLocalDate((LocalDate) date);

					dataByteArrayLengthBytesToKeepInMemory += BYTES_TO_WRITE_LOCAL_DATE_VALUES_SIZE;
				} else {
					dataWriterBytesToKeepInMemory.writeByte(-1);
				}

				dataByteArrayLengthBytesToKeepInMemory += BYTES_TO_WRITE_BYTE_VALUES_SIZE;
			}

			metadatasSizeBytesToKeepInMemory++;
		}

		private void addSingleValueLocalDateTimeMetadata(Metadata metadata, Object value) throws IOException {
			writeLocalDateTime((LocalDateTime) value);

			writeHeader(metadata);

			dataByteArrayLengthBytesToKeepInMemory += BYTES_TO_WRITE_LONG_VALUES_SIZE;
			metadatasSizeBytesToKeepInMemory++;
		}

		private void addMultivalueLocalDateTimeMetadata(Metadata metadata, List<LocalDateTime> metadatas)
				throws IOException {
			// We don't want to write in the array if all the values we have are nulls to mimic Solr
			if (metadatas.stream().allMatch(x -> x == null)) {
				return;
			}

			writeMultivalueSize(metadata, (short) metadatas.size());

			for (Iterator i = metadatas.iterator(); i.hasNext(); ) {
				// the index gets put in a object since it can be null and we need to keep the result
				Object dateTime = i.next();

				// only for multivalue local date and localdate, a byte is added before the 3 bytes of the date to differentiate null values
				// 1 = a NON null value
				// -1 = a null value
				if (null != dateTime) {
					dataWriterBytesToKeepInMemory.writeByte(1);
					writeLocalDateTime((LocalDateTime) dateTime);

					// +8 since the datetime is stored as Epoch Time
					dataByteArrayLengthBytesToKeepInMemory += BYTES_TO_WRITE_LONG_VALUES_SIZE;
				} else {
					dataWriterBytesToKeepInMemory.writeByte(-1);
				}

				dataByteArrayLengthBytesToKeepInMemory += BYTES_TO_WRITE_BYTE_VALUES_SIZE;
			}

			metadatasSizeBytesToKeepInMemory++;
		}

		private void addSingleValueEnumMetadata(Metadata metadata, Object value) throws IOException {
			writeEnum(metadata.getEnumClass(), (String) value);

			writeHeader(metadata);

			dataByteArrayLengthBytesToKeepInMemory += BYTES_TO_WRITE_BYTE_VALUES_SIZE;
			metadatasSizeBytesToKeepInMemory++;
		}

		private void addMultivalueEnumMetadata(Metadata metadata, List<Enum> metadatas) throws IOException {
			writeMultivalueSize(metadata, (short) metadatas.size());

			for (Object value : metadatas) {
				writeEnum(metadata.getEnumClass(), (String) value);

				dataByteArrayLengthBytesToKeepInMemory += BYTES_TO_WRITE_BYTE_VALUES_SIZE;
			}

			metadatasSizeBytesToKeepInMemory++;
		}

		private void writeMultivalueSize(Metadata metadata, short listSize) throws IOException {
			// the listSize tells us how many of those metadata types we should parse when reading the byte array
			// stops us from parsing a short when parsing int metadata for example
			if (isMetatadataPersisted(metadata)) {
				dataWriterBytesToPersist.writeShort(listSize);
				writeHeader(metadata);

				dataByteArrayLengthBytesToPersist += BYTES_TO_WRITE_METADATA_VALUES_SIZE;
			} else {
				dataWriterBytesToKeepInMemory.writeShort(listSize);
				writeHeader(metadata);

				dataByteArrayLengthBytesToKeepInMemory += BYTES_TO_WRITE_METADATA_VALUES_SIZE;
			}
		}

		private void writeStringReference(String value, short size) throws IOException {
			// the size (int) tells us when to stop reading the array for a string
			// the size is negative to differentiate it from a id (ex: "Juan" from "0000000008")
			dataWriterBytesToKeepInMemory.writeInt(-size);
			dataWriterBytesToKeepInMemory.writeBytes(value);
		}

		// TODO better way of passing an indicator of which bytes arary to write to
		private void writeStringWithLength(String value, short size) throws IOException {
			dataWriterBytesToPersist.writeInt(size);
			dataWriterBytesToPersist.writeBytes(value);
		}

		private void writeLocalDate(LocalDate date) throws IOException {
			byte[] bytes = new byte[BYTES_TO_WRITE_LOCAL_DATE_VALUES_SIZE];

			int year = date.getYear();
			short month = (short) date.getMonthOfYear();
			short day = (short) date.getDayOfMonth();

			int dayMonth = ((month - 1) * 31 + (day - 1));
			int deltaYear = (year - 1900);

			int dateValue;
			if (deltaYear < 0) {
				dateValue = deltaYear * 400 + dayMonth * -1;
			} else {
				dateValue = deltaYear * 400 + dayMonth;
			}

			intTo3ByteArray(dateValue, bytes);
			dataWriterBytesToKeepInMemory.write(bytes);
		}

		private void writeLocalDateTime(LocalDateTime dateTime) throws IOException {
			// long because it's the date in epoch millis (millis since 1 jan 1970)
			dataWriterBytesToKeepInMemory.writeLong(dateTime.toDateTime().getMillis());
		}

		private void writeEnum(Class<? extends Enum> clazz, String smallCode) throws IOException {
			// + acts as a minus since Byte.MIN_VALUE is -128
			// -128 too get place for 255 enums which should be more than enough
			Enum e = EnumWithSmallCodeUtils.toEnum((Class) clazz, smallCode);
			dataWriterBytesToKeepInMemory.writeByte((byte) (e.ordinal() + Byte.MIN_VALUE));
		}

		private void writeHeader(Metadata metadata) throws IOException {
			short id = metadata.getId();
			// if it's a string it will be stored in the persisted cache, but we want to know what is in the cache from the entrySet function
			headerWriterBytesToKeepInMemory.writeShort(id);

			if (isMetatadataPersisted(metadata)) {
				// TODO REMOVE TO SAVE A BYTE
				headerWriterBytesToKeepInMemory.writeShort(-1);
				// +2 bytes for id of the metadata and +2 for the index in the data array
				headerByteArrayLengthBytesToKeepInMemory += BYTES_TO_WRITE_METADATA_ID_AND_INDEX + BYTES_TO_WRITE_METADATA_ID_AND_INDEX;

				/*// only the id is kept not the index since the value is in the persisted cache
				headerByteArrayLengthBytesToKeepInMemory += BYTES_TO_WRITE_METADATA_ID_AND_INDEX;*/

				headerWriterBytesToPersist.writeShort(id);
				headerWriterBytesToPersist.writeShort(dataByteArrayLengthBytesToPersist);
				// +2 bytes for id of the metadata and +2 for the index in the data array
				headerByteArrayLengthBytesToPersist += BYTES_TO_WRITE_METADATA_ID_AND_INDEX + BYTES_TO_WRITE_METADATA_ID_AND_INDEX;
			} else {
				headerWriterBytesToKeepInMemory.writeShort(dataByteArrayLengthBytesToKeepInMemory);
				// +2 bytes for id of the metadata and +2 for the index in the data array
				headerByteArrayLengthBytesToKeepInMemory += BYTES_TO_WRITE_METADATA_ID_AND_INDEX + BYTES_TO_WRITE_METADATA_ID_AND_INDEX;
			}
		}

		public CacheRecordDTOBytesArray build() throws IOException {
			byte[] dataToKeepInMemory = new byte[BYTES_TO_WRITE_METADATA_VALUES_SIZE + headerByteArrayLengthBytesToKeepInMemory + dataByteArrayLengthBytesToKeepInMemory];
			// BYTES_TO_WRITE_METADATA_VALUES_SIZE in destPos because index 0 & 1 are placeholders (short)
			// for the number of metadata (metadatasSizeBytesToKeepInMemory) in the final array
			System.arraycopy(headerOutputBytesToKeepInMemory.toByteArray(), 0, dataToKeepInMemory, BYTES_TO_WRITE_METADATA_VALUES_SIZE, headerByteArrayLengthBytesToKeepInMemory);
			System.arraycopy(dataOutputBytesToKeepInMemory.toByteArray(), 0, dataToKeepInMemory, headerByteArrayLengthBytesToKeepInMemory + BYTES_TO_WRITE_METADATA_VALUES_SIZE, dataByteArrayLengthBytesToKeepInMemory);

			byte[] dataToPersist = new byte[BYTES_TO_WRITE_METADATA_VALUES_SIZE + headerByteArrayLengthBytesToPersist + dataByteArrayLengthBytesToPersist];
			// BYTES_TO_WRITE_METADATA_VALUES_SIZE in destPos because index 0 & 1 are placeholders (short)
			// for the number of metadata (metadatasSizeBytesToKeepInMemory) in the final array
			System.arraycopy(headerOutputBytesToPersist.toByteArray(), 0, dataToPersist, BYTES_TO_WRITE_METADATA_VALUES_SIZE, headerByteArrayLengthBytesToPersist);
			System.arraycopy(dataOutputBytesToPersist.toByteArray(), 0, dataToPersist, headerByteArrayLengthBytesToPersist + BYTES_TO_WRITE_METADATA_VALUES_SIZE, dataByteArrayLengthBytesToPersist);

			closeStreams();
			writeMetadatasSizeToHeader(dataToKeepInMemory, metadatasSizeBytesToKeepInMemory);
			writeMetadatasSizeToHeader(dataToPersist, metadatasSizeBytesToPersist);

			CacheRecordDTOBytesArray bytesArray = new CacheRecordDTOBytesArray();
			bytesArray.bytesToKeepInMemory = dataToKeepInMemory;
			bytesArray.bytesToPersist = dataToPersist;

			return bytesArray;
		}

		private void writeMetadatasSizeToHeader(byte[] data, short metadatasSize) {
			data[0] = (byte) ((metadatasSize >> 8) & 0xff);
			data[1] = (byte) (metadatasSize & 0xff);
		}

		private void closeStreams() throws IOException {
			this.headerOutputBytesToKeepInMemory.close();
			this.dataOutputBytesToKeepInMemory.close();
			this.headerWriterBytesToKeepInMemory.close();
			this.dataWriterBytesToKeepInMemory.close();

			this.headerOutputBytesToPersist.close();
			this.dataOutputBytesToPersist.close();
			this.headerWriterBytesToPersist.close();
			this.dataWriterBytesToPersist.close();
		}
	}
}
