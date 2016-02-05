package com.constellio.app.modules.rm.model.calculators.document;

import static java.util.Arrays.asList;

import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.calculators.DynamicDependencyValues;
import com.constellio.model.entities.calculators.dependencies.DynamicLocalDependency;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;

public class DocumentDecomDatesDynamicLocalDependency extends DynamicLocalDependency {

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
			Object dateOrDateTime = values.getValue(metadata);
			if (dateOrDateTime == null) {
				return null;

			} else if (dateOrDateTime instanceof LocalDate) {
				return (LocalDate) dateOrDateTime;

			} else if (dateOrDateTime instanceof LocalDateTime) {
				return ((LocalDateTime) dateOrDateTime).toLocalDate();

			} else {
				throw new ImpossibleRuntimeException("Unsupported type : " + metadata);
			}

		}
	}

}
