package com.constellio.app.modules.rm.model.validators;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.services.records.RecordValidatorParams;

public class FolderValidator implements RecordValidator {

	@Override
	public void validate(RecordValidatorParams params) {
		Folder folder = new Folder(params.getRecord(), params.getTypes());
	}

}
