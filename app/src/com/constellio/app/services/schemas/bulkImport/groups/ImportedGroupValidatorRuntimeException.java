package com.constellio.app.services.schemas.bulkImport.groups;

public class ImportedGroupValidatorRuntimeException extends RuntimeException {
	public static class ImportedGroupValidatorRuntimeException_GroupCodeIsMissing
			extends ImportedGroupValidatorRuntimeException {
	}

	public static class ImportedGroupValidatorRuntimeException_GroupTitleIsMissing
			extends ImportedGroupValidatorRuntimeException {
	}
}
