package com.constellio.app.services.schemas.bulkImport.authorization;

import com.constellio.app.services.schemas.bulkImport.authorization.ImportedAuthorization.ImportedAuthorizationPrincipal;
import com.constellio.app.services.schemas.bulkImport.authorization.ImportedAuthorization.ImportedAuthorizationTarget;
import com.constellio.app.services.schemas.bulkImport.authorization.ImportedAuthorizationValidatorRuntimeException.*;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static java.util.Arrays.asList;

public class ImportedAuthorizationValidator {

	public static final List<String> VALID_ACCESS_LIST = asList("r", "w", "d", "rw", "rd", "rwd");
	public static final List<String> VALID_ROLES = asList("u", "m", "rgd");
	public static final List<String> VALID_PRINCIPAL_TYPES = asList("user", "group");

	Taxonomy principalTaxonomy;
	MetadataSchemaTypes types;

	public ImportedAuthorizationValidator(MetadataSchemaTypes types, Taxonomy principalTaxonomy) {
		this.types = types;
		this.principalTaxonomy = principalTaxonomy;
	}

	public void validate(ImportedAuthorization importedAuthorization) {
		validateId(importedAuthorization);
		validateAccess(importedAuthorization);
		validateRoles(importedAuthorization);
		validateNotUseOfAccessAndRoles(importedAuthorization);
		if (!emptyAccessAndRoles(importedAuthorization)) {
			validateTargets(importedAuthorization);
			validatePrincipals(importedAuthorization);
		}
	}

	private boolean emptyAccessAndRoles(ImportedAuthorization importedAuthorization) {
		String access = importedAuthorization.getAccess();
		List<String> roles = importedAuthorization.getRoles();
		if (StringUtils.isBlank(access) && (roles == null || roles.isEmpty())) {
			return true;
		}
		return false;
	}

	private void validatePrincipals(ImportedAuthorization importedAuthorization) {
		List<ImportedAuthorizationPrincipal> principals = importedAuthorization.getPrincipals();
		if (principals == null || principals.isEmpty()) {
			throw new ImportedAuthorizationValidatorRuntimeException_AuthorizationPrincipalsMissing();
		}
		for (ImportedAuthorizationPrincipal principal : principals) {
			validatePrincipal(principal);
		}
	}

	private void validatePrincipal(ImportedAuthorizationPrincipal target) {
		String targetType = target.getType();
		if (targetType == null || !VALID_PRINCIPAL_TYPES.contains(targetType.trim())) {
			throw new ImportedAuthorizationValidatorRuntimeException_InvalidPrincipalType();
		}
		String principalId = target.getPrincipalId();
		if (StringUtils.isBlank(principalId)) {
			throw new ImportedAuthorizationValidatorRuntimeException_EmptyPrincipalId();
		}
	}

	private void validateTargets(ImportedAuthorization importedAuthorization) {
		List<ImportedAuthorizationTarget> targets = importedAuthorization.getTargets();
		if (targets == null || targets.isEmpty()) {
			throw new ImportedAuthorizationValidatorRuntimeException_AuthorizationTargetsMissing();
		}
		for (ImportedAuthorizationTarget target : targets) {
			validateTarget(target);
		}
	}

	private void validateTarget(ImportedAuthorizationTarget target) {
		String targetType = target.getType();
		if (targetType == null || !types.hasType(targetType)) {
			throw new ImportedAuthorizationValidatorRuntimeException_InvalidTargetType();
		}

		MetadataSchemaType schemaType = types.getSchemaType(targetType);
		if (!schemaType.hasSecurity()) {

			boolean principalTaxonomySchemaType =
					principalTaxonomy != null && principalTaxonomy.getSchemaTypes().contains(targetType);

			if (!principalTaxonomySchemaType) {
				boolean hasMetadataProvidingSecurityFromThisType = false;
				for (Metadata metadata : types.getAllMetadatas().onlyReferencesToType(targetType)) {
					hasMetadataProvidingSecurityFromThisType |= metadata.isRelationshipProvidingSecurity();
				}

				if (!hasMetadataProvidingSecurityFromThisType) {
					throw new ImportedAuthorizationValidatorRuntimeException_InvalidTargetType();
				}
			}

		}

		String legacyId = target.getLegacyId();
		if (StringUtils.isBlank(legacyId)) {
			throw new ImportedAuthorizationValidatorRuntimeException_EmptyLegacyId();
		}
	}

	private void validateNotUseOfAccessAndRoles(ImportedAuthorization importedAuthorization) {
		List<String> roles = importedAuthorization.getRoles();
		String access = importedAuthorization.getAccess();
		if (roles != null && !roles.isEmpty() && StringUtils.isNotBlank(access)) {
			throw new ImportedAuthorizationValidatorRuntimeException_UseOfAccessAndRole();
		}
	}

	private void validateRoles(ImportedAuthorization importedAuthorization) {
		List<String> roles = importedAuthorization.getRoles();
		if (roles != null) {
			for (String role : roles) {
				validateRole(role);
			}
		}
	}

	private void validateRole(String role) {
		role = role.toLowerCase();
		if (!VALID_ROLES.contains(role)) {
			throw new ImportedAuthorizationValidatorRuntimeException_InvalidRole();
		}
	}

	private void validateAccess(ImportedAuthorization importedAuthorization) {
		String access = importedAuthorization.getAccess();
		if (StringUtils.isNotBlank(access)) {
			access = access.toLowerCase();
			if (!VALID_ACCESS_LIST.contains(access)) {
				throw new ImportedAuthorizationValidatorRuntimeException_InvalidAccess();
			}
		}
	}

	private void validateId(ImportedAuthorization importedAuthorization) {
		if (StringUtils.isBlank(importedAuthorization.getId())) {
			throw new ImportedAuthorizationValidatorRuntimeException_AuthorizationIDMissing();
		}
	}
}
