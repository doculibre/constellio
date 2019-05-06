package com.constellio.model.services.records.cache2;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.MetadataList;
import org.antlr.v4.runtime.misc.Array2DHashSet;
import org.apache.commons.collections4.set.ListOrderedSet;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.MetadataValueType.STRUCTURE;
import static com.constellio.model.entities.schemas.MetadataValueType.TEXT;

/**
 * This utility class handle the reading and writing of a byte array regrouping a Record DTO metadata values
 * <p>
 * <p>
 * | metadatasSize | metadata1Id | value1IndexInByteArray | metadataNId | valueNIndexInByteArray | allValues |
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

	private static final int BYTES_TO_WRITE_METADATA_ID_AND_INDEX = 2;
	private static final int BYTES_TO_WRITE_METADATA_VALUES_SIZE = 2;
	private static final int BYTES_TO_WRITE_BOOLEAN_VALUES_SIZE = 1;
	private static final int BYTES_TO_WRITE_INTEGER_VALUES_SIZE = 4;

	private static final int KEY_IS_NOT_AN_INT = 0;
	private static final int KEY_LENGTH = 11;

	private static final int VALUE_IS_NOT_FOUND = -1;


	public static byte[] convertDTOToByteArray(RecordDTO dto, MetadataSchema schema) {
		CachedRecordDTOByteArrayBuilder builder = new CachedRecordDTOByteArrayBuilder();

		for (Metadata metadata : schema.getMetadatas()) {
			if (isCached(metadata)) {
				if (metadata.isMultivalue()) {
					List<Object> values = (List<Object>) dto.getFields().get(metadata.getDataStoreCode());
					if (values != null && !values.isEmpty()) {
						if (metadata.getType() == REFERENCE) {
							try {
								builder.addMultivalueReferenceMetadata(metadata, (List) values);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				} else {
					Object value = dto.getFields().get(metadata.getDataStoreCode());
					if (value != null) {
						if (metadata.getType() == REFERENCE) {
							try {
								builder.addSingleValueReferenceMetadata(metadata, value);
							} catch (IOException e) {
								e.printStackTrace();
							}
						} else if (metadata.getType() == BOOLEAN) {
							try {
								builder.addSingleValueBooleanMetadata(metadata, value);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}

		}

		return builder.build();
	}

	private static boolean isCached(Metadata metadata) {
		if (metadata.getType() == STRUCTURE || metadata.getType() == TEXT) {
			return false;
		}

		if (metadata.getType() == STRING) {
			return Schemas.TITLE.isSameLocalCode(metadata);
		}

		return true;
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

	public static <T> T readMetadata(byte[] byteArray, MetadataSchema schema, String metadataLocalCode) {
		int metadatasSize = metadatasSize(byteArray);
		Metadata metadataSearched = schema.getMetadataByDatastoreCode(metadataLocalCode);

		int metadataSearchedIndex = getMetadataSearchedIndex(byteArray, metadataSearched, metadatasSize);

		return parseValueMetadata(byteArray, metadataSearched, metadataSearchedIndex);
	}

	public static Set<String> getStoredMetadatas(byte[] byteArray, MetadataSchema schema) {
		int metadatasSize = metadatasSize(byteArray);
		Set<String> storedMetadatas = new HashSet<>();

		for(Short id : getAllMetadatasId(byteArray, metadatasSize)){
			storedMetadatas.add(schema.getMetadataById(id).getDataStoreCode());
		}

		return storedMetadatas;
	}

	public static Set<Object> getStoredValues(byte[] byteArray, MetadataSchema schema) {
		int metadatasSize = metadatasSize(byteArray);
		Set<Object> storedValues = new HashSet<>();

		int headerBytesSize = (metadatasSize * (BYTES_TO_WRITE_METADATA_ID_AND_INDEX + BYTES_TO_WRITE_METADATA_ID_AND_INDEX));

		for (short i = BYTES_TO_WRITE_METADATA_ID_AND_INDEX; i < headerBytesSize; i += BYTES_TO_WRITE_METADATA_ID_AND_INDEX) {
			// needed to know how to parse the value
			Metadata metadataSearched = schema.getMetadataById(parseShortFromBytesArray(byteArray, i));
			// needed to know where to start and stop parsing the value
			// + 2 * 2 because the index is the next to bytes in the array
			short metadataIndex = parseShortFromBytesArray(byteArray, i + (BYTES_TO_WRITE_METADATA_ID_AND_INDEX * 2));

			storedValues.add(parseValueMetadata(byteArray, metadataSearched, metadataIndex));
		}

		return storedValues;
	}

	public static Set<Entry<String, Object>> toEntrySet(byte[] byteArray, MetadataSchema schema) {


		return null;
	}

	public static boolean containsMetadata(byte[] data, MetadataSchema schema, String key) {
		int metadatasSize = metadatasSize(data);
		Metadata metadataSearched = schema.getMetadataByDatastoreCode(key);

		return VALUE_IS_NOT_FOUND != getMetadataSearchedIndex(data, metadataSearched, metadatasSize);
	}

	public static int metadatasSize(byte[] data) {
		// returns the first 2 bytes converted as a short because its the metadatasSize stored
		return parseShortFromBytesArray(data, 0);
	}

	private static int getMetadataSearchedIndex(byte[] byteArray, Metadata metadataSearched, int metadatasSize) {
		short metadataSearchedId = metadataSearched.getId();

		// *(2+2) for the bytes taken by the id and index of each metadata and +2 to skip the metadatasSize and the start of the array
		int headerBytesSize = (metadatasSize * (BYTES_TO_WRITE_METADATA_ID_AND_INDEX + BYTES_TO_WRITE_METADATA_ID_AND_INDEX))
							  + BYTES_TO_WRITE_METADATA_ID_AND_INDEX;
		int metadataSearchedIndex = headerBytesSize;

		// skipping first two byte because it's the metadatasSize
		// i+=2*2 because we are just looking for the metadataId not the metadataValue
		for (short i = BYTES_TO_WRITE_METADATA_ID_AND_INDEX; i < headerBytesSize; i += BYTES_TO_WRITE_METADATA_ID_AND_INDEX * 2) {
			short id = parseShortFromBytesArray(byteArray, i);

			if (id == metadataSearchedId) {
				// Looking for next 2 bytes to get the index in the data part of the array
				return metadataSearchedIndex += parseShortFromBytesArray(byteArray, i + BYTES_TO_WRITE_METADATA_ID_AND_INDEX);
			}
		}

		return VALUE_IS_NOT_FOUND;
	}

	private static List<Short> getAllMetadatasId(byte[] byteArray, int metadatasSize){
		List<Short> metadatasId = new ArrayList<>();

		// *(2+2) for the bytes taken by the id and index of each metadata and +2 to skip the metadatasSize and the start of the array
		int headerBytesSize = (metadatasSize * (BYTES_TO_WRITE_METADATA_ID_AND_INDEX + BYTES_TO_WRITE_METADATA_ID_AND_INDEX))
							  + BYTES_TO_WRITE_METADATA_ID_AND_INDEX;

		for (short i = BYTES_TO_WRITE_METADATA_ID_AND_INDEX; i < headerBytesSize; i += BYTES_TO_WRITE_METADATA_ID_AND_INDEX * 2) {
			metadatasId.add(parseShortFromBytesArray(byteArray, i));
		}

		return metadatasId;
	}

	private static <T> T parseValueMetadata(byte[] byteArray, Metadata metadataSearched, int metadataSearchedIndex) {
		if (isIndexValid(metadataSearchedIndex)) {
			switch (metadataSearched.getType()) {
				case BOOLEAN:
					if (!metadataSearched.isMultivalue()) {
						return (T) parseSingleValueBooleanMetadata(byteArray, metadataSearchedIndex);
					}
					break;
				case REFERENCE:
					if (metadataSearched.isMultivalue()) {
						return (T) parseMultivalueReferenceMetadata(byteArray, metadataSearchedIndex);
					} else {
						return (T) parseSingleValueReferenceMetadata(byteArray, metadataSearchedIndex);
					}
			}
		}

		return null;
	}

	private static boolean isIndexValid(int index) {
		if (VALUE_IS_NOT_FOUND == index) {
			return false;
		} else {
			return true;
		}
	}

	private static Boolean parseSingleValueBooleanMetadata(byte[] byteArray, int metadataSearchIndex) {
		return byteArray[metadataSearchIndex] == (byte) 1 ? true : byteArray[metadataSearchIndex] == (byte) 0 ? false : null;
	}

	private static String parseSingleValueReferenceMetadata(byte[] byteArray, int metadataSearchIndex) {
		int stringValue = parseIntFromBytesArray(byteArray, metadataSearchIndex);

		if (stringValue > 0) {
			return formatToId(stringValue);
		} else {
			return parseStringFromBytesArray(byteArray, metadataSearchIndex);
		}
	}

	private static List<String> parseMultivalueReferenceMetadata(byte[] byteArray, int metadataSearchIndex) {
		short numberOfMetadatas = parseShortFromBytesArray(byteArray, metadataSearchIndex);

		if (numberOfMetadatas > 0) {
			List<String> references = new ArrayList<>();

			short numberOfMetadatasFound = 0;
			// + 2 since we don't want to parse the size again
			int currentIndex = metadataSearchIndex + 2;

			while (numberOfMetadatas != numberOfMetadatasFound) {
				// + i * 4 since we want to read the 4 bytes ahead every time
				int tempValue = parseIntFromBytesArray(byteArray, currentIndex);

				if (tempValue > 0) {
					references.add(formatToId(tempValue));
					currentIndex += BYTES_TO_WRITE_INTEGER_VALUES_SIZE;
				} else {
					references.add(parseStringFromBytesArray(byteArray, currentIndex));
					// in this case the tempValue represent the size of bytes taken by the string (1 each char)
					currentIndex += (tempValue * -1) * tempValue;
				}

				numberOfMetadatasFound++;
			}

			return references;
		}

		return null;
	}

	private static String formatToId(int id) {
		return String.format("%0" + KEY_LENGTH + "d", id);
	}

	private static short parseShortFromBytesArray(byte[] byteArray, int startingIndex) {
		// + 1 for the second byte taken by the short
		return (short) (((byteArray[startingIndex] & 0xFF) << 8) | (byteArray[startingIndex + 1] & 0xFF));
	}

	private static int parseIntFromBytesArray(byte[] byteArray, int startingIndex) {
		// + 1, + 2, + 3 for to get the four bytes taken by the integer
		return byteArray[startingIndex] << 24 | (byteArray[startingIndex + 1] & 0xFF) << 16 |
			   (byteArray[startingIndex + 2] & 0xFF) << 8 | (byteArray[startingIndex + 3] & 0xFF);
	}

	private static String parseStringFromBytesArray(byte[] byteArray, int startingIndex) {
		// * -1 to get the positive value of the bytes length of the array since it's stored as a negative
		// to not confuse a string and a id when parsing
		int stringBytesLength = -1 * parseIntFromBytesArray(byteArray, startingIndex);

		int startingStringPosition = startingIndex + BYTES_TO_WRITE_INTEGER_VALUES_SIZE;
		byte[] stringValueAsByte = Arrays.copyOfRange(byteArray, startingStringPosition, startingStringPosition + stringBytesLength);

		return new String(stringValueAsByte);
	}

	private static class CachedRecordDTOByteArrayBuilder {

		private int headerByteArrayLength;
		private int dataByteArrayLength;
		private short metadatasSize;

		private ByteArrayOutputStream headerOutput;
		private ByteArrayOutputStream dataOutput;
		private DataOutputStream headerWriter;
		private DataOutputStream dataWriter;

		public CachedRecordDTOByteArrayBuilder() {
			this.headerByteArrayLength = 0;
			this.dataByteArrayLength = 0;
			this.metadatasSize = 0;

			this.headerOutput = new ByteArrayOutputStream();
			this.dataOutput = new ByteArrayOutputStream();
			this.headerWriter = new DataOutputStream(headerOutput);
			this.dataWriter = new DataOutputStream(dataOutput);
		}

		/**
		 * Value is stored using 1 byte
		 */
		private void addSingleValueBooleanMetadata(Metadata metadata, Object value) throws IOException {
			dataWriter.writeByte(((boolean) value ? 1 : (!(boolean) value) ? 0 : -1)); // TODO WHAT TODO WITH NULL

			writeHeader(metadata);

			dataByteArrayLength += BYTES_TO_WRITE_BOOLEAN_VALUES_SIZE;
			metadatasSize++;
		}

		/**
		 * Value is usually integer, but can also be String or Null.
		 * <p>
		 * Integer value is stocked using 4 bytes (Integer)
		 * String value is stocked using a 4 bytes negative value (Integer), where the value represent the size of bytes used to store the String value the characters are stored as bytes
		 */
		private void addSingleValueReferenceMetadata(Metadata metadata, Object value) throws IOException {
			int key = toIntKey(value);
			int size = 0;

			if (key != KEY_IS_NOT_AN_INT) {
				dataWriter.writeInt(key);
			} else {
				size = value.toString().getBytes(StandardCharsets.UTF_8).length;
				writeString((String) value, size);
			}

			writeHeader(metadata);

			// + size if it's a string to represent each byte taken by a char
			// else 0 to not take unused space
			dataByteArrayLength += BYTES_TO_WRITE_INTEGER_VALUES_SIZE + size;

			metadatasSize++;
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
			short listSize = (short) metadatas.size();

			dataWriter.writeShort(listSize);
			writeHeader(metadata);
			dataByteArrayLength += BYTES_TO_WRITE_METADATA_VALUES_SIZE;

			for (Object value : metadatas) {
				int key = toIntKey(value);
				int size = 0;

				if (key != KEY_IS_NOT_AN_INT) {
					dataWriter.writeInt(key);
				} else { // TODO NULL SKIPPED BECAUSE OF FOREACH ?
					size = value.toString().getBytes(StandardCharsets.UTF_8).length;
					writeString((String) value, size);
				}

				// + size if it's a string to represent each byte taken by a char
				dataByteArrayLength += BYTES_TO_WRITE_INTEGER_VALUES_SIZE + size;
			}

			metadatasSize++;
		}

		private void writeString(String value, int size) throws IOException {
			dataWriter.writeInt(-size);
			dataWriter.writeBytes(value);
		}

		private void writeHeader(Metadata metadata) throws IOException {
			headerWriter.writeShort(metadata.getId());
			headerWriter.writeShort(dataByteArrayLength);
			headerByteArrayLength += BYTES_TO_WRITE_METADATA_ID_AND_INDEX + BYTES_TO_WRITE_METADATA_ID_AND_INDEX; // 2 bytes (short) * each header write (2)
		}

		public byte[] build() {
			// index 0 & 1 are placeholders (short) for the number of data (metadatasSize/metadatasCount) in the final array
			byte[] data = new byte[BYTES_TO_WRITE_METADATA_VALUES_SIZE + headerByteArrayLength + dataByteArrayLength];
			System.arraycopy(headerOutput.toByteArray(), 0, data, BYTES_TO_WRITE_METADATA_VALUES_SIZE, headerByteArrayLength);
			System.arraycopy(dataOutput.toByteArray(), 0, data, headerByteArrayLength + BYTES_TO_WRITE_METADATA_VALUES_SIZE, dataByteArrayLength);

			try {
				closeStreams();
			} catch (IOException e) {
				e.printStackTrace();
			}
			writeMetadatasSizeToHeader(data);

			return data;
		}

		private void writeMetadatasSizeToHeader(byte[] data) {
			data[0] = (byte) ((metadatasSize >> 8) & 0xff);
			data[1] = (byte) (metadatasSize & 0xff);
		}

		private void closeStreams() throws IOException {
			this.headerOutput.close();
			this.dataOutput.close();
			this.headerWriter.close();
			this.dataWriter.close();
		}
	}
}
