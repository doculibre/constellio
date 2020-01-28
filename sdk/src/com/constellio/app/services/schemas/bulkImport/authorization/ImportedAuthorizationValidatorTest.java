package com.constellio.app.services.schemas.bulkImport.authorization;

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
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.services.schemas.bulkImport.authorization.ImportedAuthorizationValidator.VALID_ROLES;
import static java.util.Arrays.asList;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ImportedAuthorizationValidatorTest extends ConstellioTest {
	ImportedAuthorizationValidator validator;

	ImportedAuthorization validAuthorization;

	@Mock MetadataSchemaType folderSchemaType;
	@Mock MetadataSchemaType documentSchemaType;
	@Mock MetadataSchemaType administrativeUnitSchemaType;
	@Mock MetadataSchemaType taskSchemaType;
	@Mock MetadataSchemaType categorySchemaType;
	@Mock MetadataSchemaTypes types;

	@Before
	public void setUp()
			throws Exception {

		when(types.getSchemaType("folder")).thenReturn(folderSchemaType);
		when(types.getSchemaType("document")).thenReturn(documentSchemaType);
		when(types.getSchemaType("userTask")).thenReturn(taskSchemaType);
		when(types.getSchemaType("category")).thenReturn(categorySchemaType);
		when(types.getSchemaType("administrativeUnit")).thenReturn(administrativeUnitSchemaType);

		when(types.hasType("folder")).thenReturn(true);
		when(types.hasType("document")).thenReturn(true);
		when(types.hasType("userTask")).thenReturn(true);
		when(types.hasType("administrativeUnit")).thenReturn(true);
		when(types.hasType("category")).thenReturn(true);

		when(types.getAllMetadatas()).thenReturn(new MetadataList());
		when(folderSchemaType.hasSecurity()).thenReturn(true);
		when(documentSchemaType.hasSecurity()).thenReturn(true);
		when(taskSchemaType.hasSecurity()).thenReturn(true);
		when(administrativeUnitSchemaType.hasSecurity()).thenReturn(false);
		when(categorySchemaType.hasSecurity()).thenReturn(false);

		Taxonomy principalTaxonomy = mock(Taxonomy.class);
		when(principalTaxonomy.getSchemaTypes()).thenReturn(asList("administrativeUnit"));

		validator = new ImportedAuthorizationValidator(types, principalTaxonomy);
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
	public void givenAuthorizationWithInexistingTargetTypeWhenValidatingThenAdequateException()
			throws Exception {
		validAuthorization.setTargets(asList(new ImportedAuthorizationTarget("folders", "legacyId")));
		validator.validate(validAuthorization);
	}

	@Test(expected = ImportedAuthorizationValidatorRuntimeException_InvalidTargetType.class)
	public void givenAuthorizationWithUnsecurableTargetTypeWhenValidatingThenAdequateException()
			throws Exception {
		validAuthorization.setTargets(asList(new ImportedAuthorizationTarget("category", "legacyId")));
		validator.validate(validAuthorization);
	}

	@Test
	public void givenAuthorizationWithUnsecurableTargetTypeProvidingSecurityWithMetadataWhenValidatingThenNoException()
			throws Exception {

		Metadata metadataProvidingSecurity = TestUtils.mockManualMetadata("folder_default_category", MetadataValueType.REFERENCE);
		when(metadataProvidingSecurity.getReferencedSchemaTypeCode()).thenReturn("category");
		when(metadataProvidingSecurity.isRelationshipProvidingSecurity()).thenReturn(true);

		MetadataList metadatas = new MetadataList(metadataProvidingSecurity);
		when(types.getAllMetadatas()).thenReturn(metadatas);

		validAuthorization.setTargets(asList(new ImportedAuthorizationTarget("category", "legacyId")));
		validator.validate(validAuthorization);
	}
}
