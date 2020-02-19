package com.constellio.model.services.records.cache;

import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.records.cache.CacheRecordDTOUtils.CacheRecordDTOBytesArray;
import com.constellio.model.utils.EnumWithSmallCodeUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.constellio.model.services.records.RecordUtils.KEY_IS_NOT_AN_INT;
import static com.constellio.model.services.records.RecordUtils.toIntKey;

class CachedRecordDTOByteArrayBuilder {

	static final Logger LOGGER = LoggerFactory.getLogger(CacheRecordDTOUtils.class);

	short metadatasSizeToKeepInMemory;

	short metadatasSizeToPersist;

	DTOUtilsByteArrayDataOutputStream headerWriterBytesToKeepInMemory;
	DTOUtilsByteArrayDataOutputStream dataWriterBytesToKeepInMemory;

	DTOUtilsByteArrayDataOutputStream headerWriterBytesToPersist;
	DTOUtilsByteArrayDataOutputStream dataWriterBytesToPersist;

	String id;

	public CachedRecordDTOByteArrayBuilder(String id) {

		this.headerWriterBytesToKeepInMemory = new DTOUtilsByteArrayDataOutputStream(false, CacheRecordDTOUtils.compiledDTOStatsBuilder);
		this.dataWriterBytesToKeepInMemory = new DTOUtilsByteArrayDataOutputStream(false, CacheRecordDTOUtils.compiledDTOStatsBuilder);

		this.headerWriterBytesToPersist = new DTOUtilsByteArrayDataOutputStream(true, CacheRecordDTOUtils.compiledDTOStatsBuilder);
		this.dataWriterBytesToPersist = new DTOUtilsByteArrayDataOutputStream(true, CacheRecordDTOUtils.compiledDTOStatsBuilder);
		this.id = id;
	}

	/**
	 * Value is stored using 1 byte
	 */
	void addSingleValueBooleanMetadata(Metadata metadata, Object value) throws IOException {
		writeHeader(metadata);
		dataWriterBytesToKeepInMemory.writeByte(metadata, ((boolean) value ? 1 : 0));
		metadatasSizeToKeepInMemory++;
	}

	void addMultivalueBooleanMetadata(Metadata metadata, List<Boolean> metadatas) throws IOException {
		// We don't want to write in the array if all the values we have are nulls to mimic Solr
		if (metadatas.stream().allMatch(x -> x == null)) {
			return;
		}

		short listSize = (short) metadatas.size();
		writeHeaderAndMultivalueSize(metadata, listSize);

		for (Iterator i = metadatas.iterator(); i.hasNext(); ) {
			// the index gets put in a object since a boolean cant be null and we need to keep the result
			Object value = i.next();

			// if the value of the boolean is null, write -1
			// else if the value if true write 1 (true) else 0 (false)
			dataWriterBytesToKeepInMemory.writeByte(metadata, value == null ? -1 : (boolean) value ? 1 : 0);
		}

		metadatasSizeToKeepInMemory++;
	}

	/**
	 * Value is usually integer, but can also be String or Null.
	 * <p>
	 * Integer value is stocked using 4 bytes (Integer)
	 * String value is stocked using a 4 bytes negative value (Integer), where the value represent the size of bytes used to store the String value the characters are stored as bytes
	 */
	void addSingleValueReferenceMetadata(Metadata metadata, Object value) throws IOException {
		// the key is the result of a parsing of the string
		// 0 means the value in the string is not a number
		writeHeader(metadata);
		int key = toIntKey(value);
		short size = 0;

		if (key != KEY_IS_NOT_AN_INT) {
			dataWriterBytesToKeepInMemory.writeInt(metadata, key);
		} else {
			size = (short) value.toString().getBytes(StandardCharsets.UTF_8).length;
			writeStringReference(metadata, (String) value, size);
		}

		metadatasSizeToKeepInMemory++;
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
	void addMultivalueReferenceMetadata(Metadata metadata, List<String> metadatas)
			throws IOException {
		writeHeaderAndMultivalueSize(metadata, (short) metadatas.size());

		for (String value : metadatas) {
			short size = 0;
			if (null != value) {
				int key = toIntKey(value);

				if (key != KEY_IS_NOT_AN_INT) {
					dataWriterBytesToKeepInMemory.writeInt(metadata, key);
				} else {
					size = (short) value.getBytes(StandardCharsets.UTF_8).length;
					writeStringReference(metadata, value, size);
				}
			} else {
				dataWriterBytesToKeepInMemory.writeInt(metadata, 0);
			}
		}

		metadatasSizeToKeepInMemory++;
	}

	void addSingleValueStringMetadata(Metadata metadata, Object value) throws IOException {
		byte[] string = ((String) value).getBytes();

		// if empty string not worth writing
		if (0 < string.length) {
			// Strings are kept in the persisted cache

			//				if (isMetatadataPersisted(metadata)) {

			writeHeader(metadata);
			dataWriterBytesToPersist.write(metadata, string);

			metadatasSizeToPersist++;

			//				} else {
			//					dataWriterBytesToKeepInMemory.write(metadata, string);
			//
			//					writeHeader(metadata);
			//
			//					dataByteArrayLengthBytesToKeepInMemory += string.length;
			//					metadatasSizeToKeepInMemory++;
			//				}

			// +1 in the memory too since we need it to know the size and furthermore the contains of the byte array
			// even though there's no value associated to it in this byte array, just the id is present
			metadatasSizeToKeepInMemory++;
		}
	}

	void addMultivalueStringMetadata(Metadata metadata, List<String> metadatas) throws IOException {
		// We don't want to write in the array if all the values we have are nulls to mimic Solr

		if (metadatas.stream().allMatch(Objects::isNull)) {
			return;
		}
		// Filters out empty strings but keep nulls to mimic Solr
		List<String> strings = metadatas.stream()
				.filter(s -> (s == null || s.getBytes(StandardCharsets.UTF_8).length > 0))
				.collect(Collectors.toList());

		writeHeaderAndMultivalueSize(metadata, (short) strings.size());

		for (String string : strings) {
			int size = 0;
			if (null == string) {
				dataWriterBytesToPersist.writeInt(metadata, 0);
			} else {
				size = string.getBytes(StandardCharsets.UTF_8).length;
				writeStringWithLength(metadata, string, size);
			}

		}

		metadatasSizeToPersist++;
		// +1 in the memory too since we need it to know the size and furthermore the contains of the byte array
		// even though there's no value associated to it in this byte array, just the id is present
		metadatasSizeToKeepInMemory++;
	}

	void addSingleValueIntegerMetadata(Metadata metadata, Object value) throws IOException {
		writeHeader(metadata);

		// TODO Find a better way
		// This exist only because the value sometimes comes as a Double and other time as int
		int intValue;
		if (value instanceof Double) {
			intValue = ((Double) value).intValue();
		} else {
			intValue = (int) value;
		}

		metadatasSizeToKeepInMemory++;
		if (!CacheRecordDTOUtils.isMetatadataPersisted(metadata)) {
			dataWriterBytesToKeepInMemory.writeInt(metadata, intValue);

		} else {
			dataWriterBytesToPersist.writeInt(metadata, intValue);
			metadatasSizeToPersist++;
		}
	}

	void addMultivalueIntegerMetadata(Metadata metadata, List<Integer> metadatas) throws IOException {
		writeHeaderAndMultivalueSize(metadata, (short) metadatas.size());

		metadatasSizeToKeepInMemory++;


		if (CacheRecordDTOUtils.isMetatadataPersisted(metadata)) {
			for (int value : metadatas) {
				dataWriterBytesToPersist.writeInt(metadata, value);

			}
			metadatasSizeToPersist++;
		} else {
			for (int value : metadatas) {
				dataWriterBytesToKeepInMemory.writeInt(metadata, value);

			}
		}
	}

	void addSingleValueNumberMetadata(Metadata metadata, Object value) throws IOException {
		writeHeader(metadata);
		dataWriterBytesToKeepInMemory.writeDouble(metadata, (double) value);

		metadatasSizeToKeepInMemory++;
	}

	void addMultivalueNumberMetadata(Metadata metadata, List<Double> metadatas) throws IOException {
		writeHeaderAndMultivalueSize(metadata, (short) metadatas.size());

		for (double value : metadatas) {
			dataWriterBytesToKeepInMemory.writeDouble(metadata, value);
		}

		metadatasSizeToKeepInMemory++;
	}

	void addSingleValueLocalDateMetadata(Metadata metadata, Object value) throws IOException {
		writeHeader(metadata);
		writeLocalDate(metadata, (LocalDate) value);

		metadatasSizeToKeepInMemory++;
		if (CacheRecordDTOUtils.isMetatadataPersisted(metadata)) {
			metadatasSizeToPersist++;
		}
	}

	void addMultivalueLocalDateMetadata(Metadata metadata, List<LocalDate> metadatas) throws IOException {
		// We don't want to write in the array if all the values we have are nulls to mimic Solr
		if (metadatas.stream().allMatch(x -> x == null)) {
			return;
		}

		writeHeaderAndMultivalueSize(metadata, (short) metadatas.size());

		metadatasSizeToKeepInMemory++;

		DTOUtilsByteArrayDataOutputStream stream;
		if (CacheRecordDTOUtils.isMetatadataPersisted(metadata)) {
			stream = dataWriterBytesToPersist;
			metadatasSizeToPersist++;
		} else {
			stream = dataWriterBytesToKeepInMemory;
		}

		for (Iterator i = metadatas.iterator(); i.hasNext(); ) {
			// the index gets put in a object since it can be null and we need to keep the result
			Object date = i.next();

			// only for multivalue local date and localdate, a byte is added before the 3 bytes of the date to differentiate null values
			// 1 = a NON null value
			// -1 = a null value
			if (null != date) {
				stream.writeByte(metadata, 1);
				writeLocalDate(metadata, (LocalDate) date);

			} else {
				stream.writeByte(metadata, -1);
			}

		}


	}

	void addSingleValueLocalDateTimeMetadata(Metadata metadata, Object value) throws IOException {
		writeHeader(metadata);
		writeLocalDateTime(metadata, (LocalDateTime) value);
		metadatasSizeToKeepInMemory++;
		if (CacheRecordDTOUtils.isMetatadataPersisted(metadata)) {
			metadatasSizeToPersist++;
		}
	}

	void addMultivalueLocalDateTimeMetadata(Metadata metadata, List<LocalDateTime> metadatas)
			throws IOException {
		// We don't want to write in the array if all the values we have are nulls to mimic Solr
		if (metadatas.stream().allMatch(x -> x == null)) {
			return;
		}

		writeHeaderAndMultivalueSize(metadata, (short) metadatas.size());

		DTOUtilsByteArrayDataOutputStream stream;
		if (CacheRecordDTOUtils.isMetatadataPersisted(metadata)) {
			stream = dataWriterBytesToPersist;
			metadatasSizeToPersist++;
		} else {
			stream = dataWriterBytesToKeepInMemory;
		}

		for (Iterator i = metadatas.iterator(); i.hasNext(); ) {
			// the index gets put in a object since it can be null and we need to keep the result
			Object dateTime = i.next();

			// only for multivalue local date and localdate, a byte is added before the 3 bytes of the date to differentiate null values
			// 1 = a NON null value
			// -1 = a null value
			if (null != dateTime) {
				stream.writeByte(metadata, 1);
				writeLocalDateTime(metadata, (LocalDateTime) dateTime);

				// +8 since the datetime is stored as Epoch Time
			} else {
				stream.writeByte(metadata, -1);
			}

		}

		metadatasSizeToKeepInMemory++;
	}

	void addSingleValueEnumMetadata(Metadata metadata, Object value) throws IOException {
		writeHeader(metadata);
		writeEnum(metadata, metadata.getEnumClass(), (String) value);

		metadatasSizeToKeepInMemory++;
	}

	void addMultivalueEnumMetadata(Metadata metadata, List<Enum> metadatas) throws IOException {
		writeHeaderAndMultivalueSize(metadata, (short) metadatas.size());

		for (Object value : metadatas) {
			writeEnum(metadata, metadata.getEnumClass(), (String) value);
		}

		metadatasSizeToKeepInMemory++;
	}

	void writeHeaderAndMultivalueSize(Metadata metadata, short listSize) throws IOException {
		// the listSize tells us how many of those metadata types we should parse when reading the byte array
		// stops us from parsing a short when parsing int metadata for example
		writeHeader(metadata);
		if (CacheRecordDTOUtils.isMetatadataPersisted(metadata)) {
			dataWriterBytesToPersist.writeShort(metadata, listSize);

		} else {
			dataWriterBytesToKeepInMemory.writeShort(metadata, listSize);
		}
	}

	void writeStringReference(Metadata metadata, String value, short size) throws IOException {
		// the size (int) tells us when to stop reading the array for a string
		// the size is negative to differentiate it from a id (ex: "Juan" from "0000000008")
		dataWriterBytesToKeepInMemory.writeInt(metadata, -size);
		dataWriterBytesToKeepInMemory.writeBytes(metadata, value, false);
	}

	// TODO better way of passing an indicator of which bytes arary to write to
	void writeStringWithLength(Metadata metadata, String value, int size) throws IOException {
		dataWriterBytesToPersist.writeInt(metadata, size);
		dataWriterBytesToPersist.writeBytes(metadata, value, true);
	}

	void writeLocalDate(Metadata metadata, LocalDate date) throws IOException {
		byte[] bytes = new byte[CacheRecordDTOUtils.BYTES_TO_WRITE_LOCAL_DATE_VALUES_SIZE];

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

		CacheRecordDTOUtils.intTo3ByteArray(dateValue, bytes);
		if (CacheRecordDTOUtils.isMetatadataPersisted(metadata)) {
			dataWriterBytesToPersist.write(metadata, bytes);
		} else {
			dataWriterBytesToKeepInMemory.write(metadata, bytes);
		}
	}

	void writeLocalDateTime(Metadata metadata, LocalDateTime dateTime) throws IOException {
		// long because it's the date in epoch millis (millis since 1 jan 1970)
		if (CacheRecordDTOUtils.isMetatadataPersisted(metadata)) {
			dataWriterBytesToPersist.writeLong(metadata, dateTime.toDateTime().getMillis());
		} else {
			dataWriterBytesToKeepInMemory.writeLong(metadata, dateTime.toDateTime().getMillis());
		}
	}

	void writeEnum(Metadata metadata, Class<? extends Enum> clazz, String smallCode) throws IOException {
		// + acts as a minus since Byte.MIN_VALUE is -128
		// -128 too get place for 255 enums which should be more than enough
		Enum e = EnumWithSmallCodeUtils.toEnum((Class) clazz, smallCode);
		dataWriterBytesToKeepInMemory.writeByte(metadata, (byte) (e.ordinal() + Byte.MIN_VALUE));
	}

	void writeHeader(Metadata metadata) throws IOException {

		short id = metadata.getId();
		// if it's a string it will be stored in the persisted cache, but we want to know what is in the cache from the entrySet function
		headerWriterBytesToKeepInMemory.writeShort(metadata, id);

		if (CacheRecordDTOUtils.isMetatadataPersisted(metadata)) {
			// TODO REMOVE TO SAVE A BYTE
			headerWriterBytesToKeepInMemory.writeCompactedIntFromByteArray_2_4_8(metadata, -1);
			// +2 bytes for id of the metadata and +2 for the index in the data array


			headerWriterBytesToPersist.writeShort(metadata, id);
			headerWriterBytesToPersist.writeCompactedIntFromByteArray_2_4_8(metadata, dataWriterBytesToPersist.length);
			// +2 bytes for id of the metadata and +2 for the index in the data array
		} else {

			headerWriterBytesToKeepInMemory.writeCompactedIntFromByteArray_2_4_8(metadata, dataWriterBytesToKeepInMemory.length);
			// +2 bytes for id of the metadata and +2 for the index in the data array
		}
	}


	public CacheRecordDTOBytesArray build() throws IOException {


		// BYTES_TO_WRITE_METADATA_VALUES_SIZE in destPos because index 0 & 1 are placeholders (short)
		// for the number of metadata (metadatasSizeToKeepInMemory) in the final array
		byte[] headerOfByteArrayToKeepInMemory = headerWriterBytesToKeepInMemory.toByteArray();
		byte[] dataOfByteArrayToKeepInMemory = dataWriterBytesToKeepInMemory.toByteArray();
		byte[] dataToKeepInMemory = new byte[((int) CacheRecordDTOUtils.HEADER_OF_HEADER_BYTES) + headerOfByteArrayToKeepInMemory.length + dataOfByteArrayToKeepInMemory.length];
		short headerLengthOfByteArrayToKeepInMemory = (short) (headerOfByteArrayToKeepInMemory.length + CacheRecordDTOUtils.HEADER_OF_HEADER_BYTES);
		System.arraycopy(headerOfByteArrayToKeepInMemory, 0, dataToKeepInMemory, CacheRecordDTOUtils.HEADER_OF_HEADER_BYTES, headerOfByteArrayToKeepInMemory.length);
		System.arraycopy(dataOfByteArrayToKeepInMemory, 0, dataToKeepInMemory, headerOfByteArrayToKeepInMemory.length + CacheRecordDTOUtils.HEADER_OF_HEADER_BYTES, dataOfByteArrayToKeepInMemory.length);

		byte[] headerOfByteArrayToPersist = headerWriterBytesToPersist.toByteArray();
		byte[] dataOfByteArrayToPersist = dataWriterBytesToPersist.toByteArray();
		byte[] dataToPersist = new byte[((int) CacheRecordDTOUtils.HEADER_OF_HEADER_BYTES) + headerOfByteArrayToPersist.length + dataOfByteArrayToPersist.length];
		// BYTES_TO_WRITE_METADATA_VALUES_SIZE in destPos because index 0 & 1 are placeholders (short)
		// for the number of metadata (metadatasSizeToKeepInMemory) in the final array


		short headerLengthOfByteArrayToPersist = (short) (headerOfByteArrayToPersist.length + CacheRecordDTOUtils.HEADER_OF_HEADER_BYTES);
		System.arraycopy(headerOfByteArrayToPersist, 0, dataToPersist, CacheRecordDTOUtils.HEADER_OF_HEADER_BYTES, headerOfByteArrayToPersist.length);
		System.arraycopy(dataOfByteArrayToPersist, 0, dataToPersist, headerOfByteArrayToPersist.length + CacheRecordDTOUtils.HEADER_OF_HEADER_BYTES, dataOfByteArrayToPersist.length);


		closeStreams();
		writeMetadatasSizeToHeader(dataToKeepInMemory, metadatasSizeToKeepInMemory, headerLengthOfByteArrayToKeepInMemory);
		writeMetadatasSizeToHeader(dataToPersist, metadatasSizeToPersist, headerLengthOfByteArrayToPersist);

		if (Toggle.DEBUG_DTOS.isEnabled() && CacheRecordDTOUtils.debuggedDTOIds.isEmpty() || CacheRecordDTOUtils.debuggedDTOIds.contains(id)) {
			List<List<Object>> memoryInfos = new ArrayList<>();
			memoryInfos.add(DTOUtilsByteArrayDataOutputStream.toDebugInfos(null, 0, 2, "short", "" + metadatasSizeToKeepInMemory));
			memoryInfos.add(DTOUtilsByteArrayDataOutputStream.toDebugInfos(null, 2, 4, "short", "" + headerLengthOfByteArrayToKeepInMemory));
			memoryInfos.addAll(this.headerWriterBytesToKeepInMemory
					.getDebugInfosIncrementingOffSets(CacheRecordDTOUtils.HEADER_OF_HEADER_BYTES));
			memoryInfos.add(null);
			memoryInfos.addAll(this.dataWriterBytesToKeepInMemory
					.getDebugInfosIncrementingOffSets(CacheRecordDTOUtils.HEADER_OF_HEADER_BYTES + headerWriterBytesToKeepInMemory.length));

			List<List<Object>> persistedInfos = new ArrayList<>();
			persistedInfos.add(DTOUtilsByteArrayDataOutputStream.toDebugInfos(null, 0, 2, "short", "" + metadatasSizeToPersist));
			persistedInfos.add(DTOUtilsByteArrayDataOutputStream.toDebugInfos(null, 2, 4, "short", "" + headerLengthOfByteArrayToPersist));
			persistedInfos.addAll(this.headerWriterBytesToPersist
					.getDebugInfosIncrementingOffSets(CacheRecordDTOUtils.HEADER_OF_HEADER_BYTES));
			persistedInfos.add(null);
			persistedInfos.addAll(this.dataWriterBytesToPersist
					.getDebugInfosIncrementingOffSets(CacheRecordDTOUtils.HEADER_OF_HEADER_BYTES + headerWriterBytesToPersist.length));

			LOGGER.info(RecordsCachesUtils.logDTODebugReport(id, memoryInfos, persistedInfos));

		}

		CacheRecordDTOBytesArray bytesArray = new CacheRecordDTOBytesArray();
		bytesArray.bytesToKeepInMemory = dataToKeepInMemory;
		bytesArray.bytesToPersist = dataToPersist;

		return bytesArray;
	}

	void writeMetadatasSizeToHeader(byte[] data, short metadatasSize, short headerSize) {
		data[0] = (byte) ((metadatasSize >> 8) & 0xff);
		data[1] = (byte) (metadatasSize & 0xff);
		data[2] = (byte) ((headerSize >> 8) & 0xff);
		data[3] = (byte) (headerSize & 0xff);
	}

	void closeStreams() throws IOException {
		this.headerWriterBytesToKeepInMemory.close();
		this.dataWriterBytesToKeepInMemory.close();

		this.headerWriterBytesToPersist.close();
		this.dataWriterBytesToPersist.close();
	}
}
