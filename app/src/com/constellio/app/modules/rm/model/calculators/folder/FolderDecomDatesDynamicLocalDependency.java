package com.constellio.app.modules.rm.model.calculators.folder;

import static java.util.Arrays.asList;

import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.calculators.DynamicDependencyValues;
import com.constellio.model.entities.calculators.dependencies.DynamicLocalDependency;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;

public class FolderDecomDatesDynamicLocalDependency extends DynamicLocalDependency {

	public static List<String> excludedMetadatas = asList(
			Folder.BORROW_DATE,
			Folder.BORROW_PREVIEW_RETURN_DATE,
			Folder.BORROW_RETURN_DATE,
			Folder.COPY_RULES_EXPECTED_DEPOSIT_DATES,
			Folder.COPY_RULES_EXPECTED_DESTRUCTION_DATES,
			Folder.COPY_RULES_EXPECTED_TRANSFER_DATES,
			Folder.OPENING_DATE,
			Folder.CLOSING_DATE,
			Folder.ENTERED_CLOSING_DATE,
			Folder.DECOMMISSIONING_DATE,
			Folder.ACTUAL_DEPOSIT_DATE,
			Folder.ACTUAL_DEPOSIT_DATE,
			Folder.ACTUAL_DESTRUCTION_DATE,
			Folder.ACTUAL_TRANSFER_DATE,
			Folder.EXPECTED_DEPOSIT_DATE,
			Folder.EXPECTED_DESTRUCTION_DATE,
			Folder.EXPECTED_TRANSFER_DATE

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
