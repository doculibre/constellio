package com.constellio.app.services.schemas.bulkImport;

import java.util.HashMap;
import java.util.Map;

import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.app.services.schemas.bulkImport.data.ImportData;

public class ImportDataErrors {

	ValidationErrors errors;

	String schemaType;

	String schemaTypeLabel;

	ImportData importData;

	public ImportDataErrors(String schemaType, String schemaTypeLabel, ValidationErrors errors, ImportData importData) {
		this.errors = errors;
		this.importData = importData;
		this.schemaType = schemaType;
		this.schemaTypeLabel = schemaTypeLabel;
	}

	public void error(String code, Map<String, Object> parameters) {

		if (importData.getValue("code") != null) {
			parameters.put("prefix", schemaTypeLabel + " " + importData.getValue("code") + " : ");
		} else {
			parameters.put("prefix", schemaTypeLabel + " " + importData.getLegacyId() + " : ");
		}

		parameters.put("index", "" + (importData.getIndex() + 1));
		parameters.put("legacyId", importData.getLegacyId());
		parameters.put("schemaType", schemaType);
		errors.add(RecordsImportServices.class, code, parameters);
	}

	public void error(String code) {
		HashMap<String, Object> parameters = new HashMap<>();

		if (importData.getValue("code") != null) {
			parameters.put("prefix", schemaTypeLabel + " " + importData.getValue("code") + " : ");
		} else {
			parameters.put("prefix", schemaTypeLabel + " " + importData.getLegacyId() + " : ");
		}

		parameters.put("index", "" + (importData.getIndex() + 1));
		parameters.put("legacyId", importData.getLegacyId());
		parameters.put("schemaType", schemaType);
		errors.add(RecordsImportServices.class, code, parameters);
	}

}
