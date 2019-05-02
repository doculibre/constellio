package com.constellio.model.services.records.cache2;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

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


	public static byte[] convertDTOToByteArray(RecordDTO dto, MetadataSchema schema) {
		CachedRecordDTOByteArrayBuilder builder = new CachedRecordDTOByteArrayBuilder();

		for (Metadata metadata : schema.getMetadatas()) {
			if (isCached(metadata)) {
				if (metadata.isMultivalue()) {
					List<Object> values = (List<Object>) dto.getFields().get(metadata.getDataStoreCode());
					if (values != null && !values.isEmpty()) {
						if (metadata.getType() == REFERENCE) {
							builder.addMultivalueReferenceMetadata(metadata, (List) values);
						}
					}
				} else {
					Object value = dto.getFields().get(metadata.getDataStoreCode());
					if (value != null) {
						if (metadata.getType() == REFERENCE) {
							builder.addSingleValueReferenceMetadata(metadata, value);
						} else if (metadata.getType() == BOOLEAN) {
							builder.addSingleValueBooleanMetadata(metadata, value);
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

			if (value < Integer.MAX_VALUE) {
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
		return null;
	}

	public static Set<Object> getStoredValues(byte[] byteArray, MetadataSchema schema) {
		return null;
	}

	public static Set<Entry<String, Object>> toEntrySet(byte[] byteArray, MetadataSchema schema) {
		return null;
	}

	public static boolean containsMetadata(byte[] data, MetadataSchema schema, String key) {
		int metadatasSize = metadatasSize(data);
		//		Metadata metadataSearched = schema.get(key);
		Metadata metadataSearched = schema.getMetadataByDatastoreCode(key);

		return -1 != getMetadataSearchedIndex(data, metadataSearched, metadatasSize);
	}

	public static int metadatasSize(byte[] data) {
		// returns the first 2 bytes converted as a short because its the metadatasSize stored
		return ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
	}

	private static int getMetadataSearchedIndex(byte[] byteArray, Metadata metadataSearched, int metadatasSize) {
		short metadataSearchedId = metadataSearched.getId();
		// not starting at 0 because we want to skip the header because the index found is the one in the data part not the whole array
		// *(2+2) for the bytes taken by the id and index of each metadata and +2 to skip the metadatasSize and the start of the array
		int headerBytesSize = (metadatasSize * (BYTES_TO_WRITE_METADATA_ID_AND_INDEX + BYTES_TO_WRITE_METADATA_ID_AND_INDEX))
							  + BYTES_TO_WRITE_METADATA_ID_AND_INDEX;
		int metadataSearchedIndex = headerBytesSize;

		// skipping first two byte because it's the metadatasSize
		// i+=2*2 because we are just looking for the metadataId not the metadataValue
		for (short i = BYTES_TO_WRITE_METADATA_ID_AND_INDEX; i < headerBytesSize; i += BYTES_TO_WRITE_METADATA_ID_AND_INDEX * 2) {
			short id = (short) (((byteArray[i] & 0xFF) << 8) | (byteArray[i + 1] & 0xFF));

			if (id == metadataSearchedId) {
				// Looking for next 2 bytes to get the index in the data part of the array
				return metadataSearchedIndex += ((byteArray[i + BYTES_TO_WRITE_METADATA_ID_AND_INDEX] & 0xFF) << 8) |
												(byteArray[i + BYTES_TO_WRITE_METADATA_ID_AND_INDEX + 1] & 0xFF);
			}
		}

		return -1; // TODO THROW INSTEAD ?
	}

	private static <T> T parseValueMetadata(byte[] byteArray, Metadata metadataSearched, int metadataSearchedIndex) {
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

		return null;
	}

	private static Boolean parseSingleValueBooleanMetadata(byte[] byteArray, int metadataSearchIndex) {
		return byteArray[metadataSearchIndex] == (byte) 1;
	}

	private static Integer parseSingleValueReferenceMetadata(byte[] byteArray, int metadataSearchIndex) {


		return null; // TODO RETURN A STRING ?
	}

	private static Set<Integer> parseMultivalueReferenceMetadata(byte[] byteArray, int metadataSearchIndex) {


		return null; // TODO RETURN AS LIST STRING ?
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
		private void addSingleValueBooleanMetadata(Metadata metadata, Object value) {
			//TODO : MANAGE NULL VALUE
			try {
				dataWriter.writeByte(((boolean) value ? 1 : 0));

				writeHeader(metadata);

				dataByteArrayLength += BYTES_TO_WRITE_BOOLEAN_VALUES_SIZE;
			} catch (IOException e) {
				e.printStackTrace();
			}

			metadatasSize++;
		}

		/**
		 * Value is usually integer, but can also be String or Null.
		 * <p>
		 * Integer value is stocked using 4 bytes (Integer)
		 * String value is stocked using a 4 bytes negative value (Integer), where the value represent the size of bytes used to store the String value
		 */
		private void addSingleValueReferenceMetadata(Metadata metadata, Object value) {
			try {
				if (value instanceof Integer) {
					dataWriter.writeInt((int) value);
				} else if (value instanceof String) {
					dataWriter.writeInt(value.toString().getBytes().length * -1);
				}

				writeHeader(metadata);

				dataByteArrayLength += BYTES_TO_WRITE_INTEGER_VALUES_SIZE;
			} catch (IOException e) {
				e.printStackTrace();
			}

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
		private void addMultivalueReferenceMetadata(Metadata metadata,
													List<Object> metadatas) { // TODO PUT STRING INSTEAD OF OBJECT
			short valueBytesSize = 0;
			short listSize = (short) metadatas.size();

			try {
				dataWriter.writeShort(listSize);
				writeHeader(metadata);
				valueBytesSize += BYTES_TO_WRITE_METADATA_VALUES_SIZE;

				for (Object value : metadatas) {
					valueBytesSize += BYTES_TO_WRITE_INTEGER_VALUES_SIZE;
					//					toIntKey(value); TODO USE THIS

					if (value instanceof Integer) {
						dataWriter.writeInt((int) value);
					} else if (value instanceof String) {
						dataWriter.writeInt(value.toString().getBytes().length * -1);
					} else {
						dataWriter.writeInt(0);
					}

					dataByteArrayLength += valueBytesSize;
				}
			} catch (IOException e) {
				e.printStackTrace();
				//				throw new runtime TODO
			}

			metadatasSize++;
		}

		private void writeHeader(Metadata metadata) {
			try {
				headerWriter.writeShort(metadata.getId());
				headerWriter.writeShort(dataByteArrayLength);
				headerByteArrayLength += BYTES_TO_WRITE_METADATA_ID_AND_INDEX + BYTES_TO_WRITE_METADATA_ID_AND_INDEX; // 2 bytes (short) * each header write (2)
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public byte[] build() {
			// index 0 & 1 are placeholders (short) for the number of data (metadatasSize/metadatasCount) in the final array
			byte[] data = new byte[BYTES_TO_WRITE_METADATA_VALUES_SIZE + headerByteArrayLength + dataByteArrayLength];
			System.arraycopy(headerOutput.toByteArray(), 0, data, BYTES_TO_WRITE_METADATA_VALUES_SIZE, headerByteArrayLength);
			System.arraycopy(dataOutput.toByteArray(), 0, data, headerByteArrayLength + BYTES_TO_WRITE_METADATA_VALUES_SIZE, dataByteArrayLength);

			closeStreams();

			// set the metadatasSize as 2 bytes, equivalent to DataOutputStream.writeShort()
			// but faster because it's a primitive operation
			/*ByteArrayOutputStream ba = new ByteArrayOutputStream();
			DataOutputStream da = new DataOutputStream(ba);
			byte[] temp = null;
			try {
				da.writeShort(100);
				temp = ba.toByteArray();
			} catch (IOException e) {
				e.printStackTrace();
			}

			data[0] = temp[0];
			data[1] = temp[1];*/

			data[0] = (byte) ((metadatasSize >> 8) & 0xff);
			data[1] = (byte) (metadatasSize & 0xff);

			return data;
		}

		private void closeStreams() {
			try {
				this.headerOutput.close();
				this.dataOutput.close();
				this.headerWriter.close();
				this.dataWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
