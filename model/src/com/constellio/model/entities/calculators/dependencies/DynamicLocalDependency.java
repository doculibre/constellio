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

	public abstract boolean isDependentOf(Metadata metadata);

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
	}

}