package com.constellio.app.modules.rm.model.calculators.document;

import static java.util.Arrays.asList;

import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.wrappers.Document;
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
		if (metadata.getType() == MetadataValueType.DATE || metadata.getType() == MetadataValueType.DATE_TIME
				|| metadata.getType() == MetadataValueType.NUMBER) {
			return !excludedMetadatas.contains(metadata.getLocalCode());

		} else {
			return false;
		}
	}

	/*@Deprecated
	@Override
	public LocalDate getDate(String metadata, DynamicDependencyValues values) {
		return super.getDate(metadata, values);
	}*/

}
