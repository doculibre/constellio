package com.constellio.app.modules.rm.model.calculators.document;

import static com.constellio.model.entities.schemas.MetadataValueType.DATE;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE_TIME;
import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
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
		return isMetadataUsableByCopyRetentionRules(metadata);
	}

	public static boolean isMetadataUsableByCopyRetentionRules(Metadata metadata) {
		if (metadata.getType() == DATE || metadata.getType() == DATE_TIME || metadata.getType() == NUMBER
				|| isTimeRangeMetadata(metadata)) {
			return !excludedMetadatas.contains(metadata.getLocalCode());

		} else {
			return false;
		}
	}

	private static boolean isTimeRangeMetadata(Metadata metadata) {
		return "9999-9999".equals(metadata.getInputMask());
	}


	/*@Deprecated
	@Override
	public LocalDate getDate(String metadata, DynamicDependencyValues values) {
		return super.getDate(metadata, values);
	}*/

}
