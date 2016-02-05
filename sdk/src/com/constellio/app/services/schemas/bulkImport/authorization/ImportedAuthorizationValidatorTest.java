package com.constellio.app.services.schemas.bulkImport.authorization;

import static com.constellio.app.services.schemas.bulkImport.authorization.ImportedAuthorizationValidator.VALID_ROLES;
import static java.util.Arrays.asList;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.services.schemas.bulkImport.authorization.ImportedAuthorization.ImportedAuthorizationPrincipal;
import com.constellio.app.services.schemas.bulkImport.authorization.ImportedAuthorization.ImportedAuthorizationTarget;
import com.constellio.app.services.schemas.bulkImport.authorization.ImportedAuthorizationValidatorRuntimeException.ImportedAuthorizationValidatorRuntimeException_AuthorizationIDMissing;
import com.constellio.app.services.schemas.bulkImport.authorization.ImportedAuthorizationValidatorRuntimeException.ImportedAuthorizationValidatorRuntimeException_AuthorizationPrincipalsMissing;
import com.constellio.app.services.schemas.bulkImport.authorization.ImportedAuthorizationValidatorRuntimeException.ImportedAuthorizationValidatorRuntimeException_AuthorizationTargetsMissing;
import com.constellio.app.services.schemas.bulkImport.authorization.ImportedAuthorizationValidatorRuntimeException.ImportedAuthorizationValidatorRuntimeException_EmptyLegacyId;
import com.constellio.app.services.schemas.bulkImport.authorization.ImportedAuthorizationValidatorRuntimeException.ImportedAuthorizationValidatorRuntimeException_EmptyPrincipalId;
import com.constellio.app.services.schemas.bulkImport.authorization.ImportedAuthorizationValidatorRuntimeException.ImportedAuthorizationValidatorRuntimeException_InvalidAccess;
import com.constellio.app.services.schemas.bulkImport.authorization.ImportedAuthorizationValidatorRuntimeException.ImportedAuthorizationValidatorRuntimeException_InvalidPrincipalType;
import com.constellio.app.services.schemas.bulkImport.authorization.ImportedAuthorizationValidatorRuntimeException.ImportedAuthorizationValidatorRuntimeException_InvalidRole;
import com.constellio.app.services.schemas.bulkImport.authorization.ImportedAuthorizationValidatorRuntimeException.ImportedAuthorizationValidatorRuntimeException_InvalidTargetType;
import com.constellio.app.services.schemas.bulkImport.authorization.ImportedAuthorizationValidatorRuntimeException.ImportedAuthorizationValidatorRuntimeException_UseOfAccessAndRole;

public class ImportedAuthorizationValidatorTest {
	ImportedAuthorizationValidator validator;

	ImportedAuthorization validAuthorization;

	@Before
	public void setUp()
			throws Exception {
		validator = new ImportedAuthorizationValidator();
		List<ImportedAuthorizationPrincipal> validPrincipals = asList(new ImportedAuthorizationPrincipal("user", "userLegacyId"),
				new ImportedAuthorizationPrincipal("group", "groupLegacyId"));
		List<ImportedAuthorizationTarget> validTargets = asList(new ImportedAuthorizationTarget("folder", "folderLegacyId"),
				new ImportedAuthorizationTarget("document", "documentLegacyId"),
				new ImportedAuthorizationTarget("administrativeUnit", "administrativeUnitLegacyId"),
				new ImportedAuthorizationTarget("userTask", "userTaskLegacyId"));
		validAuthorization = new ImportedAuthorization().setId("id").setPrincipals(validPrincipals)
				.setTargets(validTargets).setAccess("r");
	}

	@Test
	public void givenValidAuthorizationWhenValidatingThenOK()
			throws Exception {
		//valid authorization with access
		validator.validate(validAuthorization);
		//valid authorization with roles
		validAuthorization.setAccess(null).setRoles(VALID_ROLES);
		validator.validate(validAuthorization);
		//valid authorization without access and without roles
		validAuthorization.setAccess(null).setRoles(null);
		validator.validate(validAuthorization);
	}

	@Test(expected = ImportedAuthorizationValidatorRuntimeException_InvalidAccess.class)
	public void givenAuthorizationWithInvalidAccessWhenValidatingThenAdequateException()
			throws Exception {
		validAuthorization.setAccess("les");
		validator.validate(validAuthorization);
	}

	@Test(expected = ImportedAuthorizationValidatorRuntimeException_InvalidRole.class)
	public void givenAuthorizationWithInvalidRoleWhenValidatingThenAdequateException()
			throws Exception {
		validAuthorization.setRoles(asList("g"));
		validator.validate(validAuthorization);
	}

	@Test(expected = ImportedAuthorizationValidatorRuntimeException_UseOfAccessAndRole.class)
	public void givenAuthorizationWithRoleAndAccessWhenValidatingThenAdequateException()
			throws Exception {
		validAuthorization.setAccess("r").setRoles(VALID_ROLES);
		validator.validate(validAuthorization);
	}

	@Test(expected = ImportedAuthorizationValidatorRuntimeException_AuthorizationIDMissing.class)
	public void givenAuthorizationWithMissingIDWhenValidatingThenAdequateException()
			throws Exception {
		validAuthorization.setId(" ");
		validator.validate(validAuthorization);
	}

	@Test(expected = ImportedAuthorizationValidatorRuntimeException_AuthorizationTargetsMissing.class)
	public void givenAuthorizationWithMissingTargetsWhenValidatingThenAdequateException()
			throws Exception {
		validAuthorization.setTargets(null);
		try {
			validator.validate(validAuthorization);
			fail("");
		} catch (ImportedAuthorizationValidatorRuntimeException_AuthorizationTargetsMissing e) {
			//ok
		}
		validAuthorization.setTargets(new ArrayList<ImportedAuthorizationTarget>());
		validator.validate(validAuthorization);
	}

	@Test(expected = ImportedAuthorizationValidatorRuntimeException_AuthorizationPrincipalsMissing.class)
	public void givenAuthorizationWithMissingPrincipalsWhenValidatingThenAdequateException()
			throws Exception {
		validAuthorization.setPrincipals(null);
		try {
			validator.validate(validAuthorization);
			fail("");
		} catch (ImportedAuthorizationValidatorRuntimeException_AuthorizationPrincipalsMissing e) {
			//ok
		}
		validAuthorization.setPrincipals(new ArrayList<ImportedAuthorizationPrincipal>());
		validator.validate(validAuthorization);
	}

	@Test(expected = ImportedAuthorizationValidatorRuntimeException_EmptyPrincipalId.class)
	public void givenAuthorizationWithEmptyPrincipalIdWhenValidatingThenAdequateException()
			throws Exception {
		validAuthorization.setPrincipals(asList(new ImportedAuthorizationPrincipal("user", null)));
		validator.validate(validAuthorization);
	}

	@Test(expected = ImportedAuthorizationValidatorRuntimeException_EmptyLegacyId.class)
	public void givenAuthorizationWithEmptyTargetLegacyIdWhenValidatingThenAdequateException()
			throws Exception {
		validAuthorization.setTargets(asList(new ImportedAuthorizationTarget("folder", " ")));
		validator.validate(validAuthorization);
	}

	@Test(expected = ImportedAuthorizationValidatorRuntimeException_InvalidPrincipalType.class)
	public void givenAuthorizationWithInvalidPrincipalTypeWhenValidatingThenAdequateException()
			throws Exception {
		validAuthorization.setPrincipals(asList(new ImportedAuthorizationPrincipal("users", "legacyId")));
		validator.validate(validAuthorization);
	}

	@Test(expected = ImportedAuthorizationValidatorRuntimeException_InvalidTargetType.class)
	public void givenAuthorizationWithInvalidTargetTypeWhenValidatingThenAdequateException()
			throws Exception {
		validAuthorization.setTargets(asList(new ImportedAuthorizationTarget("folders", "legacyId")));
		validator.validate(validAuthorization);
	}

}
