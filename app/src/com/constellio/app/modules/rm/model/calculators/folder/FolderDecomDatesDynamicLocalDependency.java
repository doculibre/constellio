package com.constellio.app.modules.rm.model.calculators.folder;

import static com.constellio.model.entities.schemas.MetadataValueType.DATE;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE_TIME;
import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static java.util.Arrays.asList;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
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

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj instanceof FolderDecomDatesDynamicLocalDependency;
	}

	@Override
	public int hashCode() {
		return FolderDecomDatesDynamicLocalDependency.class.hashCode();
	}

	/*@Deprecated
	@Override
	public LocalDate getDate(String metadata, DynamicDependencyValues values) {
		return super.getDate(metadata, values);
	}*/

}
