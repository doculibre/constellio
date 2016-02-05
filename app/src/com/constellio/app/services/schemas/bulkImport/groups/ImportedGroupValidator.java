package com.constellio.app.services.schemas.bulkImport.groups;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.services.schemas.bulkImport.groups.ImportedGroupValidatorRuntimeException.ImportedGroupValidatorRuntimeException_GroupCodeIsMissing;
import com.constellio.app.services.schemas.bulkImport.groups.ImportedGroupValidatorRuntimeException.ImportedGroupValidatorRuntimeException_GroupTitleIsMissing;

public class ImportedGroupValidator {

	public void validate(ImportedGroup importedGroup) {
		validateCode(importedGroup);
		validateTitle(importedGroup);
	}

	private void validateCode(ImportedGroup importedGroup) {
		if (StringUtils.isBlank(importedGroup.getCode())) {
			throw new ImportedGroupValidatorRuntimeException_GroupCodeIsMissing();
		}
	}

	private void validateTitle(ImportedGroup importedGroup) {
		if (StringUtils.isBlank(importedGroup.getTitle())) {
			throw new ImportedGroupValidatorRuntimeException_GroupTitleIsMissing();
		}
	}
}
