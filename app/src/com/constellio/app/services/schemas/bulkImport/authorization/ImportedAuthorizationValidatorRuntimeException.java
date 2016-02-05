package com.constellio.app.services.schemas.bulkImport.authorization;

public class ImportedAuthorizationValidatorRuntimeException extends RuntimeException {
	public static class ImportedAuthorizationValidatorRuntimeException_InvalidRole
			extends ImportedAuthorizationValidatorRuntimeException {
	}

	public static class ImportedAuthorizationValidatorRuntimeException_InvalidAccess
			extends ImportedAuthorizationValidatorRuntimeException {
	}

	public static class ImportedAuthorizationValidatorRuntimeException_UseOfAccessAndRole
			extends ImportedAuthorizationValidatorRuntimeException {
	}

	public static class ImportedAuthorizationValidatorRuntimeException_AuthorizationIDMissing
			extends ImportedAuthorizationValidatorRuntimeException {
	}

	public static class ImportedAuthorizationValidatorRuntimeException_AuthorizationTargetsMissing
			extends ImportedAuthorizationValidatorRuntimeException {
	}

	public static class ImportedAuthorizationValidatorRuntimeException_InvalidTargetType
			extends ImportedAuthorizationValidatorRuntimeException {
	}

	public static class ImportedAuthorizationValidatorRuntimeException_AuthorizationPrincipalsMissing
			extends ImportedAuthorizationValidatorRuntimeException {
	}

	public static class ImportedAuthorizationValidatorRuntimeException_InvalidPrincipalType
			extends ImportedAuthorizationValidatorRuntimeException {
	}

	public static class ImportedAuthorizationValidatorRuntimeException_EmptyPrincipalId
			extends ImportedAuthorizationValidatorRuntimeException {
	}

	public static class ImportedAuthorizationValidatorRuntimeException_EmptyLegacyId
			extends ImportedAuthorizationValidatorRuntimeException {
	}
}
