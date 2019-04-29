package com.constellio.model.services.records.cache2;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

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

		byte[] workHeaderByteArray = new byte[1000];
		byte[] workDataByteArray = new byte[2000];
		int headerByteArrayLength;
		int dataByteArrayLength;

		/**
		 * Value is stored using 1 byte
		 */
		public void addSingleValueBooleanMetadata(Metadata metadata, Object value) {
			//TODO
		}

		/**
		 * Value is usually integer, but can also be String or Null.
		 * <p>
		 * Integer value is stocked using 4 bytes (Integer)
		 * Null value is stocked using a 4 bytes "zero value" (Integer)
		 * String value is stocked using a 4 bytes negative value (Integer), where the value represent the size of bytes used to store the String value
		 */
		public void addSingleValueReferenceMetadata(Metadata metadata, Object value) {
			//TODO
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
		public void addMultivalueReferenceMetadata(Metadata metadata, List<String> metadatas) {
			//TODO

		}


		public byte[] build() {
			byte[] data = new byte[headerByteArrayLength + dataByteArrayLength];
			System.arraycopy(workHeaderByteArray, 0, data, 0, headerByteArrayLength);
			System.arraycopy(workDataByteArray, 0, data, headerByteArrayLength, dataByteArrayLength);
			return data;

		}

	}
}
