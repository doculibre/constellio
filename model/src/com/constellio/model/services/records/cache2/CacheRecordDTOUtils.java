package com.constellio.model.services.records.cache2;

import com.constellio.data.dao.dto.records.RecordDTO;
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
 * | metadatasCount | metadata1Id | value1IndexInByteArray | metadataNId | valueNIndexInByteArray | allValues |
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
	private static final int BYTES_TO_WRITE_INTEGER_ID = 4;


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


	public static <T> T readMetadata(byte[] byteArray, MetadataSchema schema, String metadataLocalCode) {
		return null;
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
		return false;
	}

	public static int metadatasSize(byte[] data, MetadataSchema schema) {
		return 0;
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
		public void addSingleValueBooleanMetadata(Metadata metadata, Object value) {
			//TODO : MANAGE NULL VALUE
			short valueBytesSize = 1;

			try {
				dataWriter.writeByte(((boolean) value ? 1 : 0));

				dataByteArrayLength += valueBytesSize;

				writeHeader(metadata);
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
		public void addSingleValueReferenceMetadata(Metadata metadata, Object value) {
			try {
				if (value instanceof Integer) {
					dataWriter.writeInt((int) value);
				} else if (value instanceof String) {
					dataWriter.writeInt(value.toString().getBytes().length * -1);
				}

				dataByteArrayLength += 4;

				writeHeader(metadata);
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
		public void addMultivalueReferenceMetadata(Metadata metadata,
												   List<Object> metadatas) { // TODO shouldn't it be List<Object> instead of List<String> ?
			//TODO
			short valueBytesSize = 0;
			short listSize = (short) metadatas.size();

			try {
				dataWriter.writeShort(listSize);
				valueBytesSize += 2;

				for (Object value : metadatas) {
					valueBytesSize += 4;

					if (value instanceof Integer) {
						dataWriter.writeInt((int) value);
					} else if (value instanceof String) {
						dataWriter.writeInt(value.toString().getBytes().length * -1);
					} else {
						dataWriter.writeInt(0);
					}

					dataByteArrayLength += valueBytesSize;
				}

				writeHeader(metadata);
			} catch (IOException e) {
				e.printStackTrace();
			}

			metadatasSize++;
		}

		private void writeHeader(Metadata metadata) {
			try {
				headerWriter.writeShort(metadata.getId());
				headerWriter.writeShort(dataByteArrayLength);
				headerByteArrayLength += 4; // 2 bytes (short) * each header write (2)
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public byte[] build() {
			// index 0 & 1 are placeholders (short) for the number of data (metadatasSize/metadatasCount) in the final array
			byte[] data = new byte[2 + headerByteArrayLength + dataByteArrayLength];
			System.arraycopy(headerOutput.toByteArray(), 0, data, 2, headerByteArrayLength);
			System.arraycopy(dataOutput.toByteArray(), 0, data, headerByteArrayLength + 2, dataByteArrayLength);

			closeStreams();

			// set the metadatasSize as 2 bytes, equivalent to DataOutputStream.writeShort() but faster because it's primitive
			data[0] = (byte) (metadatasSize & 0xff);
			data[1] = (byte) ((metadatasSize >> 8) & 0xff);

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
