package com.constellio.model.services.search.query.logical.criteria;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.DataStoreField;

public class CriteriaUtils {

	public static String escape(String string) {
		//TODO Write tests with the criterion using escapes!
		return string.replace("\\", "\\\\").replace(" ", "\\ ").replace("/", "\\/").replace("?", "\\?").replace("*", "\\*")
				.replace("=", "\\=").replace(":", "\\:");
	}

	public static String toSolrStringValue(Object value, DataStoreField dataStoreField) {

		if (value == null) {
			return CriteriaUtils.getNullValueForDataStoreField(dataStoreField);

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
}
