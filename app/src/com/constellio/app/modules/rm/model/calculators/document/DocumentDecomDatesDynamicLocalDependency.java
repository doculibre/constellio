package com.constellio.app.modules.rm.model.calculators.document;

import static java.util.Arrays.asList;

import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.calculators.DynamicDependencyValues;
import com.constellio.model.entities.calculators.dependencies.DynamicLocalDependency;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CalculatorIsUsingAnForbiddenMetadata;

public class DocumentDecomDatesDynamicLocalDependency extends DynamicLocalDependency {

	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentDecomDatesDynamicLocalDependency.class);

	public static List<String> excludedMetadatas = asList(
			Document.ACTUAL_DEPOSIT_DATE_ENTERED,
			Document.ACTUAL_DESTRUCTION_DATE_ENTERED,
			Document.ACTUAL_TRANSFER_DATE_ENTERED,
			Document.FOLDER_ACTUAL_DEPOSIT_DATE,
			Document.FOLDER_ACTUAL_DESTRUCTION_DATE,
			Document.FOLDER_ACTUAL_TRANSFER_DATE,
			Document.FOLDER_EXPECTED_DEPOSIT_DATE,
			Document.FOLDER_EXPECTED_DESTRUCTION_DATE,
			Document.FOLDER_EXPECTED_TRANSFER_DATE

	);

	@Override
	public boolean isDependentOf(Metadata metadata) {
		if (metadata.getType() == MetadataValueType.DATE || metadata.getType() == MetadataValueType.DATE_TIME) {
			return !excludedMetadatas.contains(metadata.getLocalCode());

		} else {
			return false;
		}
	}

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
