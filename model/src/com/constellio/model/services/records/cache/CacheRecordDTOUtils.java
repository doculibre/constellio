package com.constellio.model.services.records.cache;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.utils.systemLogger.SystemLogger;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.records.RecordId;
import com.constellio.model.services.records.cache.CompiledDTOStats.CompiledDTOStatsBuilder;
import lombok.AllArgsConstructor;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

import static com.constellio.model.entities.schemas.MetadataValueType.CONTENT;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.MetadataValueType.STRUCTURE;
import static com.constellio.model.entities.schemas.MetadataValueType.TEXT;

/**
 * This utility class handle the reading and writing of a byte array regrouping a Record DTO metadata values
 * <p>
 * <p>
 * | metadatasSizeToKeepInMemory | metadata1Id | value1IndexInByteArray | metadataNId | valueNIndexInByteArray | allValues |
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

	private static final Logger LOGGER = LoggerFactory.getLogger(CacheRecordDTOUtils.class);

	static final byte METADATAS_WITH_VALUE_COUNT_BYTES = 2;
	static final byte HEADER_SIZE_BYTES = 2;
	static final byte HEADER_OF_HEADER_BYTES = METADATAS_WITH_VALUE_COUNT_BYTES + HEADER_SIZE_BYTES;

	static final byte BYTES_TO_WRITE_METADATA_ID = 2;
	static final byte BYTES_TO_WRITE_METADATA_INDEX = 4;


	static final byte BYTES_TO_WRITE_BOOLEAN_VALUES_SIZE = 1;
	static final byte BYTES_TO_WRITE_BYTE_VALUES_SIZE = 1;
	static final byte BYTES_TO_WRITE_METADATA_VALUES_SIZE = 2;
	static final byte BYTES_TO_WRITE_INTEGER_VALUES_SIZE = 4;
	static final byte BYTES_TO_WRITE_DOUBLE_VALUES_SIZE = 8;
	static final byte BYTES_TO_WRITE_LONG_VALUES_SIZE = 8;
	static final byte BYTES_TO_WRITE_LOCAL_DATE_VALUES_SIZE = 3;

	static final byte VALUE_IS_NOT_FOUND = -1;
	public static List<String> debuggedDTOIds = new ArrayList<>();

	static CompiledDTOStatsBuilder compiledDTOStatsBuilder;
	static CompiledDTOStats lastCompiledDTOStats;

	public static void startCompilingDTOsStats() {
		compiledDTOStatsBuilder = new CompiledDTOStatsBuilder();
	}

	public static CompiledDTOStats stopCompilingDTOsStats() {
		lastCompiledDTOStats = compiledDTOStatsBuilder.build();
		compiledDTOStatsBuilder = null;
		return lastCompiledDTOStats;
	}

	public static CompiledDTOStats getLastCompiledDTOStats() {
		return lastCompiledDTOStats;
	}

	public static class CacheRecordDTOBytesArray {
		byte[] bytesToKeepInMemory;
		byte[] bytesToPersist;
	}

	public static CacheRecordDTOBytesArray convertDTOToByteArrays(RecordDTO dto, MetadataSchema schema) {

		CachedRecordDTOByteArrayBuilder builder = new CachedRecordDTOByteArrayBuilder(dto.getId());

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

	static boolean isMetatadataPersisted(Metadata metadata) {
		if (metadata.getType() == STRUCTURE || metadata.getType() == TEXT ||
			metadata.getType() == CONTENT || metadata.getType() == STRING) {
			return true;
		}

		return false;
	}

	private static int threeByteArrayToInt(byte[] byteArray, int startingIndex) {
		// used to convert 3 bytes to a int that represent a LocalDate
		int i = (short) (128 * ((byte) (byteArray[startingIndex + 1] & (byte) 0x7f)) + byteArray[startingIndex + 2]);
		if (byteArray[startingIndex] >= 0) {
			return i * 127 + byteArray[startingIndex];
		} else {
			return -1 * (i * 127) + (byteArray[startingIndex] + 1);
		}
	}

	static void intTo3ByteArray(int dateValue, byte[] bytes) {
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

	public static <T> T readMetadata(String recordId, byte[] byteArray, MetadataSchema schema, String metadataLocalCode,
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

		MetadataValuePositionInByteArray metadataValuePositionInByteArray = getMetadataValuePosition(byteArrayToSearchIn, metadataSearched.getId());
		return parseValueMetadata(recordId, byteArrayToSearchIn, metadataSearched, metadataValuePositionInByteArray);
	}

	public static Set<String> getStoredMetadatas(byte[] byteArrayToKeepInMemory, MetadataSchema schema) {
		Set<String> storedMetadatas = new HashSet<>();

		// skipping first two byte because it's the metadatasSizeToKeepInMemory
		// i+=2*2 because we are just looking for the metadataId not the metadataValue
		short headerBytesSize = headerSizeOf(byteArrayToKeepInMemory);

		for (int i = HEADER_OF_HEADER_BYTES; i < headerBytesSize; i += (BYTES_TO_WRITE_METADATA_ID + BYTES_TO_WRITE_METADATA_INDEX)) {
			short id = parseMetadataIdFromByteArray(byteArrayToKeepInMemory, i);
			Metadata storedMetadata = schema.getMetadataById(id);
			storedMetadatas.add(storedMetadata.getDataStoreCode());
		}

		return storedMetadatas;
	}

	public static Set<Object> getStoredValues(String recordId, byte[] byteArray, MetadataSchema schema,
											  Supplier<byte[]> persistedByteArraySupplier) {
		Set<Object> storedValues = new HashSet<>();

		// *(2+2) for the bytes taken by the id and in dex of each metadata and +2 to skip the metadatasSizeToKeepInMemory and the start of the array
		short headerBytesSize = headerSizeOf(byteArray);

		for (int i = HEADER_OF_HEADER_BYTES; i < headerBytesSize; i += (BYTES_TO_WRITE_METADATA_ID + BYTES_TO_WRITE_METADATA_INDEX)) {
			short id = parseMetadataIdFromByteArray(byteArray, i);
			// needed to know how to parse the value
			Metadata metadataSearched = schema.getMetadataById(id);

			byte[] byteArrayToUse;
			MetadataValuePositionInByteArray metadataValuePositionInByteArray;
			if (isMetatadataPersisted(metadataSearched)) {
				byteArrayToUse = persistedByteArraySupplier.get();
				metadataValuePositionInByteArray = getMetadataValuePosition(byteArrayToUse, id);
			} else {
				byteArrayToUse = byteArray;
				metadataValuePositionInByteArray = getMetadataValuePosition(byteArrayToUse, id);
			}

			// inclusiveStartIndex & exclusiveEndIndex are needed to know where to start and stop parsing the value
			storedValues.add(parseValueMetadata(recordId, byteArrayToUse, metadataSearched, metadataValuePositionInByteArray));
		}

		return storedValues;
	}

	public static Set<Entry<String, Object>> toEntrySet(String recordId, byte[] byteArray, MetadataSchema schema,
														Supplier<byte[]> persistedByteArraySupplier) {
		Set<Entry<String, Object>> metadatasEntrySet = new HashSet<>();

		short headerBytesSize = headerSizeOf(byteArray);

		for (int i = HEADER_OF_HEADER_BYTES; i < headerBytesSize; i += (BYTES_TO_WRITE_METADATA_ID + BYTES_TO_WRITE_METADATA_INDEX)) {
			short id = parseMetadataIdFromByteArray(byteArray, i);
			// needed to know how to parse the value
			Metadata metadataSearched = schema.getMetadataById(id);

			byte[] byteArrayToUse;
			MetadataValuePositionInByteArray metadataValuePositionInByteArray;
			if (isMetatadataPersisted(metadataSearched)) {
				byteArrayToUse = persistedByteArraySupplier.get();
				metadataValuePositionInByteArray = getMetadataValuePosition(byteArrayToUse, id);
			} else {
				byteArrayToUse = byteArray;
				metadataValuePositionInByteArray = getMetadataValuePosition(byteArrayToUse, id);
			}

			metadatasEntrySet.add(new SimpleEntry(metadataSearched.getDataStoreCode(),
					parseValueMetadata(recordId, byteArrayToUse, metadataSearched, metadataValuePositionInByteArray)));
		}

		return metadatasEntrySet;
	}

	public static boolean containsMetadata(byte[] data, MetadataSchema schema, String key) {
		Metadata metadataSearched = schema.getMetadataByDatastoreCode(key);

		short metadataSearchedId = metadataSearched.getId();
		short headerBytesSize = headerSizeOf(data);

		// if the value is persisted go up 2 bytes instead of 4 since we don<t have a index value in this byteArrayToKeepInMemory
		for (int i = HEADER_OF_HEADER_BYTES; i < headerBytesSize; i += (BYTES_TO_WRITE_METADATA_ID + BYTES_TO_WRITE_METADATA_INDEX)) {
			short id = parseMetadataIdFromByteArray(data, i);

			if (id == metadataSearchedId) {
				return true;
			}
		}

		return false;
	}

	public static short headerSizeOf(byte[] byteArray) {
		return parseShortFromByteArray(byteArray, (short) METADATAS_WITH_VALUE_COUNT_BYTES);
		//return (short) (metadatasWithValueCount(byteArray) * (BYTES_TO_WRITE_METADATA_ID + BYTES_TO_WRITE_METADATA_INDEX) + METADATAS_COUNT_BYTES);
	}

	public static short metadatasWithValueCount(byte[] data) {
		// returns the first 2 bytes converted as a short because its where the metadatasSizeToKeepInMemory is stored
		return parseShortFromByteArray(data, (short) 0);
	}

	private static MetadataValuePositionInByteArray getMetadataValuePosition(byte[] byteArray, short metadataId) {
		MetadataValuePositionInByteArray metadataValuePositionInByteArray = new MetadataValuePositionInByteArray();
		int headerBytesSize = headerSizeOf(byteArray);

		// skipping first two byte because it's the metadatasSizeToKeepInMemory
		for (int i = HEADER_OF_HEADER_BYTES; i < headerBytesSize; i += (BYTES_TO_WRITE_METADATA_ID + BYTES_TO_WRITE_METADATA_INDEX)) {
			short id = parseMetadataIdFromByteArray(byteArray, i);

			if (id == metadataId) {
				// Looking for next 2 bytes to get the index in the data part of the array
				metadataValuePositionInByteArray.inclusiveStartIndex = ((int) headerBytesSize) + parseMetadataValueIndexFromByteArray(byteArray, (i + BYTES_TO_WRITE_METADATA_ID));
				int indexOfNextMetadataValue = (i + (BYTES_TO_WRITE_METADATA_ID + BYTES_TO_WRITE_METADATA_INDEX + BYTES_TO_WRITE_METADATA_ID));

				// if it's NOT greater than the size of the header then the next index is the result of the parsing
				// else it's the metadata of the byte array keep reading until the end of the array
				if (!(indexOfNextMetadataValue + 1 > headerBytesSize)) {
					metadataValuePositionInByteArray.exclusiveEndIndex = ((int) headerBytesSize) + parseMetadataValueIndexFromByteArray(byteArray, indexOfNextMetadataValue);

				} else {
					// No next value, using the byte array length as the end index
					metadataValuePositionInByteArray.exclusiveEndIndex = byteArray.length;
				}

				return metadataValuePositionInByteArray;
			}
		}

		return metadataValuePositionInByteArray;
	}


	private static <T> T parseValueMetadata(String recordId, byte[] byteArray, Metadata metadataSearched,
											MetadataValuePositionInByteArray positionInByteArray) {

		int metadataSearchedIndex = positionInByteArray.inclusiveStartIndex;
		int nextMetadataIndex = positionInByteArray.exclusiveEndIndex;

		if (metadataSearched.isMultivalue()) {

			try {
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

			} catch (Throwable t) {
				SystemLogger.error("Could not parse value of metadata '" + metadataSearched.getLocalCode() + "' of record '" + recordId + "'", t);
				return (T) new ArrayList<>();
			}

			// returns a empty array even if no value is stored in the cache to mimic Solr
			return (T) new ArrayList<>();
		} else {
			try {
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
			} catch (Throwable t) {
				SystemLogger.error("Could not parse value of metadata '" + metadataSearched.getLocalCode() + "' of record '" + recordId + "'", t);
				return null;
			}
			return null;
		}
	}

	private static boolean isIndexValid(int index) {
		return VALUE_IS_NOT_FOUND != index;
	}

	private static Boolean getSingleValueBooleanMetadata(byte[] byteArray, int metadataSearchedIndex) {
		return parseBooleanFromByteArray(byteArray, metadataSearchedIndex);
	}

	private static List<Boolean> getMultivalueBooleanMetadata(byte[] byteArray, int metadataSearchedIndex) {
		short valuesCount = parseValuesCountFromByteArray(byteArray, metadataSearchedIndex);

		if (valuesCount > 0) {
			List<Boolean> booleans = new ArrayList<>();

			short numberOfMetadatasFound = 0;
			// + 2 since we don't want to parse the size again
			int currentIndex = (metadataSearchedIndex + BYTES_TO_WRITE_METADATA_VALUES_SIZE);

			while (valuesCount != numberOfMetadatasFound) {
				booleans.add(parseBooleanFromByteArray(byteArray, currentIndex));
				currentIndex += BYTES_TO_WRITE_BOOLEAN_VALUES_SIZE;
				numberOfMetadatasFound++;
			}

			return booleans;
		}

		return null;
	}

	private static String getSingleValueReferenceMetadata(byte[] byteArray, int metadataSearchedIndex) {
		int stringValue = parseIntFromByteArray(byteArray, metadataSearchedIndex);

		// if the value is higher than 0 it's positive so it is a regular id
		// else if it's negative it's a string containing letters most likely
		if (stringValue > 0) {
			return formatToId(stringValue);
		} else {
			return parseStringReferenceFromByteArray(byteArray, metadataSearchedIndex);
		}
	}

	private static List<String> getMultivalueReferenceMetadata(byte[] byteArray, int metadataSearchedIndex) {
		short valuesCount = parseValuesCountFromByteArray(byteArray, metadataSearchedIndex);

		if (valuesCount > 0) {
			List<String> references = new ArrayList<>();

			short numberOfMetadatasFound = 0;
			// + 2 since we don't want to parse the size again
			int currentIndex = (metadataSearchedIndex + BYTES_TO_WRITE_METADATA_VALUES_SIZE);

			while (valuesCount != numberOfMetadatasFound) {
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

	private static Integer getSingleValueIntegerMetadata(byte[] byteArray, int metadataSearchedIndex) {
		return parseIntFromByteArray(byteArray, metadataSearchedIndex);
	}

	private static List<Integer> getMultivalueIntegerMetadata(byte[] byteArray, int metadataSearchedIndex) {
		short valuesCount = parseValuesCountFromByteArray(byteArray, metadataSearchedIndex);

		if (valuesCount > 0) {
			List<Integer> integers = new ArrayList<>();

			short numberOfMetadatasFound = 0;
			// + 2 since we don't want to parse the size again
			int currentIndex = metadataSearchedIndex + BYTES_TO_WRITE_METADATA_VALUES_SIZE;

			while (valuesCount != numberOfMetadatasFound) {
				integers.add(parseIntFromByteArray(byteArray, currentIndex));
				currentIndex += BYTES_TO_WRITE_INTEGER_VALUES_SIZE;

				numberOfMetadatasFound++;
			}

			return integers;
		}

		return null;
	}

	private static Double getSingleValueNumberMetadata(byte[] byteArray, int metadataSearchedIndex) {
		return parseDoubleFromByteArray(byteArray, metadataSearchedIndex);
	}

	private static List<Double> getMultivalueNumberMetadata(byte[] byteArray, int metadataSearchedIndex) {
		short valuesCount = parseValuesCountFromByteArray(byteArray, metadataSearchedIndex);

		if (valuesCount > 0) {
			List<Double> doubles = new ArrayList<>();

			short numberOfMetadatasFound = 0;
			// + 2 since we don't want to parse the size again
			int currentIndex = (metadataSearchedIndex + BYTES_TO_WRITE_METADATA_VALUES_SIZE);

			while (valuesCount != numberOfMetadatasFound) {
				doubles.add(parseDoubleFromByteArray(byteArray, currentIndex));
				currentIndex += BYTES_TO_WRITE_DOUBLE_VALUES_SIZE;

				numberOfMetadatasFound++;
			}

			return doubles;
		}

		return null;
	}

	private static String getSingleValueStringMetadata(byte[] byteArray, int metadataSearchedIndex,
													   int nextMetadataIndex) {
		byte[] stringValueAsByte = Arrays.copyOfRange(byteArray, metadataSearchedIndex, nextMetadataIndex);

		return new String(stringValueAsByte);
	}

	private static List<String> getMultivalueStringMetadata(byte[] byteArray, int metadataSearchedIndex) {
		short valuesCount = parseValuesCountFromByteArray(byteArray, metadataSearchedIndex);

		if (valuesCount > 0) {
			List<String> strings = new ArrayList<>();

			short numberOfMetadatasFound = 0;
			// + 2 since we don't want to parse the size again
			int currentIndex = metadataSearchedIndex + BYTES_TO_WRITE_METADATA_VALUES_SIZE;

			while (valuesCount != numberOfMetadatasFound) {
				// in this case the stringValue represent the size of bytes taken by the string (1 each char) or the bytes length if you prefer
				int stringValue = parseIntFromByteArray(byteArray, currentIndex);

				// if the stringValue is higher then 0 it's a valid string
				// else if the stringValue lower or equal to zero the string is null
				if (stringValue > 0) {
					strings.add(parseStringWithLengthFromByteArray(byteArray, currentIndex));

					// + 4 for the bytes taken by the size value
					currentIndex += stringValue + BYTES_TO_WRITE_INTEGER_VALUES_SIZE;

					// negative string length is a null
				} else {
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

	private static LocalDate getSingleValueLocalDateMetadata(byte[] byteArray, int metadataSearchedIndex) {
		return parseLocalDateFromByteArray(byteArray, metadataSearchedIndex);
	}

	private static List<LocalDate> getMultivalueLocalDateMetadata(byte[] byteArray, int metadataSearchedIndex) {
		short valuesCount = parseValuesCountFromByteArray(byteArray, metadataSearchedIndex);

		if (valuesCount > 0) {
			List<LocalDate> dates = new ArrayList<>();

			short numberOfMetadatasFound = 0;
			// + 2 since we don't want to parse the size again
			int currentIndex = metadataSearchedIndex + BYTES_TO_WRITE_METADATA_VALUES_SIZE;

			while (valuesCount != numberOfMetadatasFound) {
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

	private static LocalDateTime getSingleValueLocalDateTimeMetadata(byte[] byteArray, int metadataSearchedIndex) {
		return parseLocalDateTimeFromByteArray(byteArray, metadataSearchedIndex);
	}

	private static List<LocalDateTime> getMultivalueLocalDateTimeMetadata(byte[] byteArray, int metadataSearchedIndex) {
		short valuesCount = parseValuesCountFromByteArray(byteArray, metadataSearchedIndex);

		if (valuesCount > 0) {
			List<LocalDateTime> dates = new ArrayList<>();

			short numberOfMetadatasFound = 0;
			// + 2 since we don't want to parse the size again
			int currentIndex = metadataSearchedIndex + BYTES_TO_WRITE_METADATA_VALUES_SIZE;

			while (valuesCount != numberOfMetadatasFound) {
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

	private static String getSingleValueEnumMetadata(byte[] byteArray, int metadataSearchedIndex,
													 Metadata metadataSearched) {
		return parseEnumFromByteArray(metadataSearched.getEnumClass(), byteArray[metadataSearchedIndex]);
	}

	private static List<String> getMultivalueEnumMetadata(byte[] byteArray, int metadataSearchedIndex,
														  Metadata metadataSearched) {
		short valuesCount = parseValuesCountFromByteArray(byteArray, metadataSearchedIndex);

		if (valuesCount > 0) {
			List<String> enums = new ArrayList<>();

			short numberOfMetadatasFound = 0;
			// + 2 since we don't want to parse the size again
			int currentIndex = metadataSearchedIndex + BYTES_TO_WRITE_METADATA_VALUES_SIZE;

			while (valuesCount != numberOfMetadatasFound) {
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
		return RecordId.toId(id).toString();
	}

	private static Boolean parseBooleanFromByteArray(byte[] byteArray, int startingIndex) {
		return byteArray[startingIndex] == (byte) -1 ? null : byteArray[startingIndex] == (byte) 1 ? true : false;
	}

	@Deprecated
	private static short parseShortFromByteArray(byte[] byteArray, int startingIndex) {
		return (short) (((byteArray[startingIndex] & 0xFF) << 8) | (byteArray[startingIndex + 1] & 0xFF));
	}

	/**
	 * Only for better code readability
	 */
	private static short parseValuesCountFromByteArray(byte[] byteArray, int startingIndex) {
		return parseShortFromByteArray(byteArray, startingIndex);
	}

	/**
	 * Only for better code readability
	 */
	private static short parseMetadataIdFromByteArray(byte[] byteArray, int startingIndex) {
		return parseShortFromByteArray(byteArray, startingIndex);
	}

	/**
	 * Only for better code readability
	 */
	private static int parseMetadataValueIndexFromByteArray(byte[] byteArray, int startingIndex) {
		return parseIntFromByteArray(byteArray, startingIndex);
	}

	/**
	 * Value ranging from 0 to 65535 are written on 2 bytes
	 * Value ranging from 65536 to 131071 are written on 4 byte
	 * Value ranging from 131072 to Max int are written on 8 byte
	 * etc
	 */
	public static CompactedInt parseCompactedPositiveIntFromByteArray_2_4_8(byte[] byteArray, int startingIndex) {
		int twoByteValue = parseShortFromByteArray(byteArray, startingIndex) + Short.MAX_VALUE;
		if (twoByteValue == -1) {
			int nextTwoByteValue = parseShortFromByteArray(byteArray, startingIndex + 2) + Short.MAX_VALUE;
			if (nextTwoByteValue == -1) {
				return new CompactedInt(parseIntFromByteArray(byteArray, startingIndex + 4), 8);
			} else {
				return new CompactedInt(nextTwoByteValue + 65535, 4);
			}
		} else {
			return new CompactedInt(twoByteValue, 2);
		}
	}

	/**
	 * Value ranging from -127 to 127 are written on 1 bytes
	 * Value ranging from -254 to 254 are written on 2 bytes
	 * Value ranging from -32768 to 32767 are written on 4 bytes
	 * etc
	 */
	public static CompactedShort parseCompactedShortFromByteArray_1_2_4(byte[] byteArray, int startingIndex) {
		int byteValue = byteArray[startingIndex];
		if (byteValue == Byte.MIN_VALUE) {
			int nextByteValue = byteArray[startingIndex + 1];
			if (nextByteValue == Byte.MIN_VALUE) {
				return new CompactedShort(parseShortFromByteArray(byteArray, startingIndex + 2), 4);
			} else {
				short value = (short) (nextByteValue < 0 ? (nextByteValue - Byte.MAX_VALUE) : (nextByteValue + Byte.MAX_VALUE));
				return new CompactedShort(value, 2);
			}
		} else {
			return new CompactedShort((short) byteValue, 1);
		}
	}

	@AllArgsConstructor
	static class CompactedInt {
		int value;
		int length;
	}

	@AllArgsConstructor
	static class CompactedShort {
		short value;
		int length;
	}


	private static int parseIntFromByteArray(byte[] byteArray, int startingIndex) {
		return ((int) (byteArray[startingIndex] & 0xFF)) << 24 | ((int) (byteArray[startingIndex + 1] & 0xFF)) << 16 |
			   ((int) (byteArray[startingIndex + 2] & 0xFF)) << 8 | ((int) (byteArray[startingIndex + 3] & 0xFF));
	}

	private static String parseStringReferenceFromByteArray(byte[] byteArray, int startingIndex) {
		// * -1 to get the positive value of the bytes length of the array since it's stored as a negative
		// to not confuse a string and a id when parsing
		int stringBytesLength = -1 * parseIntFromByteArray(byteArray, startingIndex);

		// + 4 to skip the string length 4 bytes
		int startingStringPosition = (startingIndex + BYTES_TO_WRITE_INTEGER_VALUES_SIZE);
		byte[] stringValueAsByte = Arrays.copyOfRange(byteArray, startingStringPosition, startingStringPosition + stringBytesLength);

		return new String(stringValueAsByte);
	}

	private static String parseStringWithLengthFromByteArray(byte[] byteArray, int startingIndex) {
		// 2 * 3 to get the next index in the header
		int stringBytesLength = parseIntFromByteArray(byteArray, startingIndex);

		if (stringBytesLength > 25_000_000) {
			throw new IllegalStateException("Cannot get a string value of '" + stringBytesLength + "' length");
		}

		// + 4 to skip the string length 4 bytes
		int startingStringPosition = (startingIndex + BYTES_TO_WRITE_INTEGER_VALUES_SIZE);
		byte[] stringValueAsByte = Arrays.copyOfRange(byteArray, startingStringPosition, startingStringPosition + stringBytesLength);

		try {
			System.out.println(stringValueAsByte.length);
			return new String(stringValueAsByte, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private static double parseDoubleFromByteArray(byte[] byteArray, int startingIndex) {
		return Double.longBitsToDouble(parseLongFromByteArray(byteArray, startingIndex));
	}

	private static long parseLongFromByteArray(byte[] byteArray, int startingIndex) {
		return ((long) (byteArray[startingIndex] & 0xFF)) << 56 | ((long) (byteArray[startingIndex + 1] & 0xFF)) << 48 |
			   ((long) (byteArray[startingIndex + 2] & 0xFF)) << 40 | ((long) (byteArray[startingIndex + 3] & 0xFF)) << 32 |
			   ((long) (byteArray[startingIndex + 4] & 0xFF)) << 24 | ((long) (byteArray[startingIndex + 5] & 0xFF)) << 16 |
			   ((long) (byteArray[startingIndex + 6] & 0xFF)) << 8 | ((long) (byteArray[startingIndex + 7] & 0xFF));
	}

	private static LocalDate parseLocalDateFromByteArray(byte[] byteArray, int startingIndex) {
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

	private static LocalDateTime parseLocalDateTimeFromByteArray(byte[] byteArray, int startingIndex) {
		long epochTimeInMillis = parseLongFromByteArray(byteArray, startingIndex);

		return new LocalDateTime(epochTimeInMillis);
	}

	private static Map<Class, Map<Byte, String>> enumCache = new HashMap<>();

	private static String parseEnumFromByteArray(Class<? extends Enum> clazz, byte value) {
		Map<Byte, String> map = enumCache.get(clazz);
		if (map == null) {
			try {
				map = new HashMap<>();
				EnumWithSmallCode[] values = ((EnumWithSmallCode[]) clazz.getMethod("values").invoke(null));
				for (int i = 0; i < values.length; i++) {
					EnumWithSmallCode aValue = values[i];
					map.put((byte) (i + Byte.MIN_VALUE), aValue.getCode());
				}

				synchronized (enumCache) {
					enumCache.put(clazz, map);
				}
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		}

		return map.get(value);
	}

	private static class MetadataValuePositionInByteArray {
		private int inclusiveStartIndex = VALUE_IS_NOT_FOUND;
		private int exclusiveEndIndex;
	}

}
