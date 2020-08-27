package com.constellio.app.services.schemas.bulkImport.authorization;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.schemas.bulkImport.BulkImportResults;
import com.constellio.app.services.schemas.bulkImport.ImportError;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.modules.rm.wrappers.Folder.CATEGORY_ENTERED;
import static com.constellio.app.services.schemas.bulkImport.authorization.AuthorizationImportServices.AUTHORIZATION_PRINCIPALS_MISSING;
import static com.constellio.app.services.schemas.bulkImport.authorization.AuthorizationImportServices.AUTHORIZATION_TARGETS_MISSING;
import static com.constellio.app.services.schemas.bulkImport.authorization.AuthorizationImportServices.INVALID_ACCESS;
import static com.constellio.app.services.schemas.bulkImport.authorization.AuthorizationImportServices.INVALID_PRINCIPAL_TYPE;
import static com.constellio.app.services.schemas.bulkImport.authorization.AuthorizationImportServices.INVALID_ROLE;
import static com.constellio.app.services.schemas.bulkImport.authorization.AuthorizationImportServices.INVALID_TARGET_TYPE;
import static com.constellio.app.services.schemas.bulkImport.authorization.AuthorizationImportServices.USE_ROLE_OR_ACCESS;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationInCollectionWithId;
import static org.assertj.core.api.Assertions.assertThat;

public class AuthorizationImportServicesAcceptanceTest extends ConstellioTest {
	RMTestRecords records = new RMTestRecords(zeCollection);

	AuthorizationImportServices importServices;
	SearchServices searchServices;
	AuthorizationsServices authorizationsServices;

	User alice;
	Group heroes;
	Folder folderHavingLegacyId;
	Document documentHavingLegacyId;
	AdministrativeUnit administrativeUnitHavingLegacyId;

	String existingAuthorizationToUpdateId = "existingAuthorizationToUpdate";
	String existingAuthorizationToDeleteId = "existingAuthorizationToDelete";
	Authorization existingAuthorizationToUpdate;
	Authorization existingAuthorizationToDelete;
	Users users = new Users();

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withAllTestUsers().withDocumentsHavingContent()
		);

		//rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		users.setUp(getModelLayerFactory().newUserServices(), zeCollection);
		searchServices = getModelLayerFactory().newSearchServices();
		authorizationsServices = getModelLayerFactory().newAuthorizationsServices();

		importServices = new AuthorizationImportServices();

		initTestData();
		existingAuthorizationToUpdate = grantReadForAliceOnFolderA01(existingAuthorizationToUpdateId);
		existingAuthorizationToDelete = grantReadForAliceOnFolderA01(existingAuthorizationToDeleteId);

	}

	private void initTestData()
			throws RecordServicesException {
		alice = users.aliceIn(zeCollection);
		heroes = users.heroesIn(zeCollection);
		folderHavingLegacyId = records.getFolder_A01();
		documentHavingLegacyId = records.getDocumentWithContent_A19();
		administrativeUnitHavingLegacyId = records.getUnit10();
		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		Transaction transaction = new Transaction();
		transaction.add(folderHavingLegacyId.setLegacyId("folderLegacyId"));
		transaction.add(documentHavingLegacyId.setLegacyId("documentLegacyId"));
		transaction.add(administrativeUnitHavingLegacyId.setLegacyId("administrativeUnitLegacyId"));
		recordServices.execute(transaction);
	}

	private Authorization grantReadForAliceOnFolderA01(String authorizationId) {

		authorizationId = authorizationsServices.add(authorizationInCollectionWithId(zeCollection, authorizationId)
				.forUsers(users.aliceIn(zeCollection)).on(records.getFolder_A01()).givingReadAccess());

		return authorizationsServices.getAuthorization(zeCollection, authorizationId);
	}

	@Test
	public void whenImportingAuthorizationsThenImportedCorrectly()
			throws Exception {
		File authorizationFile = getTestResourceFile("authorizations.xml");
		BulkImportResults results = importServices
				.bulkImport(authorizationFile, zeCollection, getModelLayerFactory());
		System.out.println(results.getCorrectlyImportedCount() + ", " + results.getInvalidIds().size());
		Map<String, ImportError> errorsMappedByAuthorizationId = getErrorsMap(results.getImportErrors());
		assertThat(errorsMappedByAuthorizationId)
				.containsKeys("newInvalidAuthorizationWithInvalidRole", "newInvalidAuthorizationWithInvalidRole",
						"newInvalidAuthorizationWithMissingTargets", "newInvalidAuthorizationWithMissingPrincipals",
						"newInvalidAuthorizationWithInvalidTargetType", "newInvalidAuthorizationWithInvalidPrincipalType",
						"newInvalidAuthorizationWithAccessAndRoles");
		ImportError newInvalidAuthorizationWithInvalidAccess = errorsMappedByAuthorizationId
				.get("newInvalidAuthorizationWithInvalidAccess");
		assertThat(newInvalidAuthorizationWithInvalidAccess.getErrorMessage()).isEqualTo($(INVALID_ACCESS));

		ImportError newInvalidAuthorizationWithMissingTargets = errorsMappedByAuthorizationId
				.get("newInvalidAuthorizationWithMissingTargets");
		assertThat(newInvalidAuthorizationWithMissingTargets.getErrorMessage()).isEqualTo($(AUTHORIZATION_TARGETS_MISSING));

		ImportError newInvalidAuthorizationWithMissingPrincipals = errorsMappedByAuthorizationId
				.get("newInvalidAuthorizationWithMissingPrincipals");
		assertThat(newInvalidAuthorizationWithMissingPrincipals.getErrorMessage())
				.isEqualTo($(AUTHORIZATION_PRINCIPALS_MISSING));

		ImportError newInvalidAuthorizationWithInvalidTargetType = errorsMappedByAuthorizationId
				.get("newInvalidAuthorizationWithInvalidTargetType");
		assertThat(newInvalidAuthorizationWithInvalidTargetType.getErrorMessage()).isEqualTo($(INVALID_TARGET_TYPE));

		ImportError newInvalidAuthorizationWithInvalidPrincipalType = errorsMappedByAuthorizationId
				.get("newInvalidAuthorizationWithInvalidPrincipalType");
		assertThat(newInvalidAuthorizationWithInvalidPrincipalType.getErrorMessage()).isEqualTo($(INVALID_PRINCIPAL_TYPE));

		ImportError newInvalidAuthorizationWithAccessAndRoles = errorsMappedByAuthorizationId
				.get("newInvalidAuthorizationWithAccessAndRoles");
		assertThat(newInvalidAuthorizationWithAccessAndRoles.getErrorMessage()).isEqualTo($(USE_ROLE_OR_ACCESS));

		ImportError newInvalidAuthorizationWithInvalidRole = errorsMappedByAuthorizationId
				.get("newInvalidAuthorizationWithInvalidRole");
		assertThat(newInvalidAuthorizationWithInvalidRole.getErrorMessage()).isEqualTo($(INVALID_ROLE));

	}

	@Test
	public void whenImportingAuthorizationsOnTypeProvidingSecurityThenOK()
			throws Exception {

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType("folder").getDefaultSchema().get(CATEGORY_ENTERED).setRelationshipProvidingSecurity(true);
			}
		});

		File authorizationFile = getTestResourceFile("categoryAuthorizations.xml");
		BulkImportResults results = importServices
				.bulkImport(authorizationFile, zeCollection, getModelLayerFactory());
		assertThat(results.getImportErrors()).isEmpty();

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		Authorization auth1 = rm.getSolrAuthorizationDetailsWithLegacyId("1");
		Authorization auth2 = rm.getSolrAuthorizationDetailsWithLegacyId("2");
		Authorization auth3 = rm.getSolrAuthorizationDetailsWithLegacyId("3");

		assertThat(auth1).isNotNull();
		assertThat(auth1.getTarget()).isEqualTo(rm.getCategoryWithCode("Z100").getId());
		assertThat(auth1.isOverrideInherited()).isFalse();

		assertThat(auth2.getTarget()).isEqualTo(rm.getCategoryWithCode("Z200").getId());
		assertThat(auth2).isNotNull();
		assertThat(auth2.isOverrideInherited()).isTrue();

		assertThat(auth3.getTarget()).isEqualTo(rm.getCategoryWithCode("Z120").getId());
		assertThat(auth3).isNotNull();
		assertThat(auth3.isOverrideInherited()).isFalse();

	}

	private Map<String, ImportError> getErrorsMap(List<ImportError> importErrors) {
		Map<String, ImportError> returnMap = new HashMap<>();
		for (ImportError importError : importErrors) {
			returnMap.put(importError.getInvalidElementId(), importError);
		}
		return returnMap;
	}

}
