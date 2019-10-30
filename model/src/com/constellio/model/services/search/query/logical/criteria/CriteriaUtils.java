package com.constellio.model.services.search.query.logical.criteria;

import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataTransiency;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.search.query.logical.condition.TestedQueryRecord;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.constellio.data.utils.LangUtils.isEmptyList;
import static com.constellio.model.entities.records.LocalisedRecordMetadataRetrieval.PREFERRING;

public class CriteriaUtils {

	public static String escape(String string) {
		return ClientUtils.escapeQueryChars(string);
	}

	public static List<Object> convertToMemoryQueryValues(List<Object> values) {
		List<Object> memoryQueryValues = new ArrayList<>();
		for (Object item : values) {
			memoryQueryValues.add(convertToMemoryQueryValue(item));

		}
		return memoryQueryValues;
	}

	public static Object convertToMemoryQueryValue(Object item) {

		if (item instanceof List) {
			return convertToMemoryQueryValues((List) item);

		} else if (item instanceof Record) {
			return ((Record) item).getId();

		} else if (item instanceof RecordWrapper) {
			return ((RecordWrapper) item).getId();

		} else if (item instanceof EnumWithSmallCode) {
			return ((EnumWithSmallCode) item).getCode();

		} else if (item instanceof Number) {
			return ((Number) item).doubleValue();

		} else {
			return item;
		}
	}


	public static Object convertMetadataValue(Metadata metadata, TestedQueryRecord testedRecord) {
		Object recordValue;

		//Same behavior than solr
		if (metadata.getTransiency() != MetadataTransiency.PERSISTED) {
			return metadata.isMultivalue() ? Collections.emptyList() : null;
		}

		Record record = testedRecord.getRecord();
		if (metadata.isEncrypted()) {
			recordValue = ((RecordImpl) record).getRecordDTO().getFields().get(metadata.getDataStoreCode());

		} else if (metadata.getType() == MetadataValueType.NUMBER && metadata.isSameLocalCode(Schemas.VERSION)) {

			//No conversion, since it is losing precision
			return record.getVersion();
		} else {
			recordValue = record.get(metadata, testedRecord.getLocale());
			if ((recordValue == null || isEmptyList(recordValue)) && testedRecord.getMetadataRetrieval() == PREFERRING) {
				recordValue = record.get(metadata);
			}

		}

		return convertMetadataValue(metadata, recordValue);
	}


	private static Object convertMetadataValue(Metadata metadata, Object recordValue) {

		if (recordValue == null) {
			return null;
		}

		if (metadata.getType() == MetadataValueType.NUMBER || metadata.getType() == MetadataValueType.INTEGER
			|| metadata.getType() == MetadataValueType.ENUM) {

			if (recordValue instanceof List) {
				List<Object> converted = new ArrayList<>();

				for (Object item : ((List) recordValue)) {
					converted.add(convertMetadataValue(metadata, item));
				}

				return converted;
			}

			if (metadata.getType() == MetadataValueType.ENUM) {
				recordValue = ((EnumWithSmallCode) recordValue).getCode();
			}

			if (metadata.getType() == MetadataValueType.INTEGER || metadata.getType() == MetadataValueType.NUMBER) {
				recordValue = ((Number) recordValue).doubleValue();
			}

		}


		return recordValue;
	}

	public static <T> List<T> getValues(Object value) {
		if (value == null) {
			return Collections.emptyList();
		} else {
			if (value instanceof List) {
				return (List<T>) value;
			} else {
				List<T> values = Collections.singletonList((T) value);
				return values;
			}
		}
	}

	private static String getCodeFromEnumWithSmallCode(Object enumWithSmallCode) {
		if (enumWithSmallCode == null) {
			return null;
		} else {
			return ((EnumWithSmallCode) enumWithSmallCode).getCode();
		}
	}

	public static String toSolrStringValue(Object value, DataStoreField dataStoreField) {

		if (value == null) {
			return dataStoreField == null ? null : CriteriaUtils.getNullValueForDataStoreField(dataStoreField);

		} else if (value instanceof LocalDateTime) {
			return correctLocalDateTime((LocalDateTime) value);

		} else if (value instanceof LocalDate) {
			LocalDateTime localDateTime = ((LocalDate) value).toLocalDateTime(LocalTime.MIDNIGHT);
			return localDateTime + "Z";

		} else if (value instanceof Record) {
			return ((Record) value).getId();

		} else if (value instanceof RecordWrapper) {
			return ((RecordWrapper) value).getId();

		} else if (value instanceof Boolean) {
			return getBooleanStringValue((Boolean) value);

		} else if (value instanceof EnumWithSmallCode) {
			return ((EnumWithSmallCode) value).getCode();

		} else {
			return escape(value.toString());
		}
	}

	private static String correctLocalDateTime(LocalDateTime value) {

		if (value.isEqual(new LocalDateTime(Integer.MIN_VALUE))) {
			return getNullDateValue();
		} else {
			return value + "Z";
		}

	}

	public static int getOffSetMillis(LocalDateTime ldt) {
		return -1 * (DateTimeZone.getDefault().getOffset(ldt.toDate().getTime()));
	}

	public static String getBooleanStringValue(boolean value) {
		return value ? "__TRUE__" : "__FALSE__";
	}

	public static String getNullStringValue() {
		return "__NULL__";
	}

	public static String getNullDateValue() {
		return "4242-06-06T06:42:42.666Z";
	}

	public static String getNullNumberValue() {
		return "" + Integer.MIN_VALUE;
	}

	public static String getNullValueForDataStoreField(DataStoreField dataStoreField) {
		switch (dataStoreField.getType()) {
			case DATE:
			case DATE_TIME:
				return getNullDateValue();
			case NUMBER:
				return getNullNumberValue();
			default:
				return getNullStringValue();
		}
	}

	public static boolean useConvertedValues(Metadata metadata) {
		return !(metadata.getType() == MetadataValueType.NUMBER && metadata.isSameLocalCode(Schemas.VERSION));
	}
}
