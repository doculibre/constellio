package com.constellio.app.modules.rm.model.calculators.folder;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.dependencies.DynamicLocalDependency;
import com.constellio.model.entities.schemas.Metadata;

import java.util.List;

import static com.constellio.model.entities.schemas.MetadataValueType.*;
import static java.util.Arrays.asList;

public class FolderDecomDatesDynamicLocalDependency extends DynamicLocalDependency {

	public static List<String> excludedMetadatas = asList(
			Folder.BORROW_DATE,
			Folder.BORROW_PREVIEW_RETURN_DATE,
			Folder.BORROW_RETURN_DATE,
			Folder.COPY_RULES_EXPECTED_DEPOSIT_DATES,
			Folder.COPY_RULES_EXPECTED_DESTRUCTION_DATES,
			Folder.COPY_RULES_EXPECTED_TRANSFER_DATES,
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
