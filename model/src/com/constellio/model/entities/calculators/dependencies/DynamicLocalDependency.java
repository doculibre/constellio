package com.constellio.model.entities.calculators.dependencies;

import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.calculators.DynamicDependencyValues;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CalculatorIsUsingAnForbiddenMetadata;

public abstract class DynamicLocalDependency implements Dependency {

	private static final Logger LOGGER = LoggerFactory.getLogger(DynamicLocalDependency.class);

	@Override
	public MetadataValueType getReturnType() {
		return null;
	}

	@Override
	public boolean isMultivalue() {
		return false;
	}

	@Override
	public boolean isRequired() {
		return false;
	}

	@Override
	public String getLocalMetadataCode() {
		return null;
	}

	public boolean isIncludingGlobalMetadatas() {
		return false;
	}

	public abstract boolean isDependentOf(Metadata metadata);

	/*
	public LocalDate getDate(String metadata, DynamicDependencyValues values) {
		if (metadata == null) {
			return null;
		} else {
			Object dateOrDateTime;
			try {
				dateOrDateTime = values.getValue(metadata);
			} catch (RecordServicesRuntimeException_CalculatorIsUsingAnForbiddenMetadata e) {
				LOGGER.warn("Cannot get value of forbidden metadata '" + metadata + "'");
				dateOrDateTime = null;
			}
			return convert(metadata, dateOrDateTime);
		}
	}

	private LocalDate convert(String metadata, Object dateOrDateTime) {
		if (dateOrDateTime == null) {
			return null;

		} else if (dateOrDateTime instanceof LocalDate) {
			return (LocalDate) dateOrDateTime;

		} else if (dateOrDateTime instanceof LocalDateTime) {
			return ((LocalDateTime) dateOrDateTime).toLocalDate();

		} else if (dateOrDateTime instanceof List) {
			List<Object> list = (List) dateOrDateTime;
			for (Object item : list) {
				if (item != null) {
					return convert(metadata, item);
				}
			}
			return null;
		} else {
			throw new ImpossibleRuntimeException(
					"Unsupported type : " + metadata + " with value '" + dateOrDateTime + "'  of type  '" + dateOrDateTime
							.getClass().getName() + "'");
		}
	}*/

	public LocalDate getDate(String metadata, DynamicDependencyValues values, String yearEnd, boolean takeFirstPartOfRange) {
		if (metadata == null || values == null) {
			return null;
		} else {
			Object dateOrDateTime;
			try {
				dateOrDateTime = values.getValue(metadata);
			} catch (RecordServicesRuntimeException_CalculatorIsUsingAnForbiddenMetadata e) {
				LOGGER.warn("Cannot get value of forbidden metadata '" + metadata + "'");
				dateOrDateTime = null;
			}
			return convert(metadata, dateOrDateTime, yearEnd, takeFirstPartOfRange);
		}
	}

	private LocalDate convert(String metadata, Object date, String yearEnd, boolean takeFirstPartOfRange) {
		if (date == null) {
			return null;
		}
		if (date instanceof LocalDate) {
			return (LocalDate) date;
		} else if (date instanceof LocalDateTime) {
			return ((LocalDateTime) date).toLocalDate();
		} else if (date instanceof String) {
			return dateFromString(metadata, (String) date, yearEnd, takeFirstPartOfRange);
		}
		if (date instanceof Number) {
			return asDate(((Number) date).intValue(), yearEnd);
		} else if (date instanceof List) {
			List<Object> list = (List) date;
			for (Object item : list) {
				if (item != null) {
					return convert(metadata, item, yearEnd, takeFirstPartOfRange);
				}
			}
			return null;
		} else {
			throw new ImpossibleRuntimeException(
					"Unsupported type : " + metadata + " with value '" + date + "'  of type  '" + date
							.getClass().getName() + "'");
		}
	}

	private LocalDate dateFromString(String metadata, String dateAsString, String yearEnd, boolean takeFirstPartOfRange) {

		if (dateAsString.length() != 9) {
			throw new RuntimeException("Invalid range date format " + dateAsString + " for metadata " + metadata);
		}
		try {
			int year;
			if (takeFirstPartOfRange) {
				year = Integer.valueOf((dateAsString).substring(0, 4));
			} else {
				year = Integer.valueOf((dateAsString).substring(5, 9));
			}
			return asDate(year, yearEnd);
		} catch (NumberFormatException e) {
			throw new RuntimeException(
					"Invalid range date format " + dateAsString + " should follow pattern 9999-9999" + " for metadata "
							+ metadata);
		}
	}

	private LocalDate asDate(int year, String yearEndStr) {
		int indexOfSep = yearEndStr.indexOf("/");
		int yearEndMonth = Integer.parseInt(yearEndStr.substring(0, indexOfSep));
		int yearEndDay = Integer.parseInt(yearEndStr.substring(indexOfSep + 1));

		return new LocalDate(year, yearEndMonth, yearEndDay);
	}
}