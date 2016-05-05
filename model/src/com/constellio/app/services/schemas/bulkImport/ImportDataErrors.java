package com.constellio.app.services.schemas.bulkImport;

import java.util.HashMap;
import java.util.Map;

import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.app.services.schemas.bulkImport.data.ImportData;

public class ImportDataErrors {

	ValidationErrors errors;

	String schemaType;

	ImportData importData;

	public ImportDataErrors(String schemaType, ValidationErrors errors, ImportData importData) {
		this.errors = errors;
		this.importData = importData;
		this.schemaType = schemaType;
	}

	public void error(String code, Map<String, Object> parameters) {
		parameters.put("index", "" + (importData.getIndex() + 1));
		parameters.put("legacyId", importData.getLegacyId());
		parameters.put("schemaType", schemaType);
		errors.add(RecordsImportServices.class, code, parameters);
	}

	public void error(String code) {
		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("index", "" + (importData.getIndex() + 1));
		parameters.put("legacyId", importData.getLegacyId());
		parameters.put("schemaType", schemaType);
		errors.add(RecordsImportServices.class, code, parameters);
	}
}
