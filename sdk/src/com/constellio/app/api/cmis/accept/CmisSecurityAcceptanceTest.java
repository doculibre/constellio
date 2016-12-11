package com.constellio.app.api.cmis.accept;

import static com.constellio.model.entities.security.global.AuthorizationBuilder.authorizationForUsers;
import static java.util.Arrays.asList;
import static org.apache.chemistry.opencmis.commons.enums.AclPropagation.REPOSITORYDETERMINED;
import static org.apache.chemistry.opencmis.commons.enums.IncludeRelationships.NONE;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Condition;
import org.assertj.core.api.Fail;
import org.assertj.core.api.ListAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.global.AuthorizationBuilder;
import com.constellio.model.services.contents.ContentManagementAcceptTest;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

/**
 * Created by Constelio on 2016-10-26.
 */
public class CmisSecurityAcceptanceTest extends ConstellioTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(CmisACLAcceptanceTest.class);

	private final String PDF_MIMETYPE = "application/pdf";
	private long pdf1Length = 170039L;
	private long pdf2Length = 167347L;
	private String pdf1Hash = "KN8RjbrnBgq1EDDV2U71a6/6gd4=";
	private String pdf2Hash = "T+4zq4cGP/tXkdJp/qz1WVWYhoQ=";

	UserServices userServices;
	TaxonomiesManager taxonomiesManager;
	MetadataSchemasManager metadataSchemasManager;
	RecordServices recordServices;
	Users users = new Users();
	CmisAcceptanceTestSetup zeCollectionSchemas = new CmisAcceptanceTestSetup(zeCollection);
	CmisAcceptanceTestSetup.Records zeCollectionRecords;
	TaxonomiesSearchServices taxonomiesSearchServices;

	List<String> R = asList("cmis:read");
	List<String> RW = asList("cmis:read", "cmis:write");
	List<String> RWD = asList("cmis:read", "cmis:write", "cmis:delete");

	Session session;

	AuthorizationsServices authorizationsServices;

	String aliceId, bobId, charlesId, dakotaId, edouardId, chuckId, gandalfId, robinId, heroesId;

	@Before
	public void setup()
			throws Exception {
		userServices = getModelLayerFactory().newUserServices();
		taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		recordServices = getModelLayerFactory().newRecordServices();

		taxonomiesSearchServices = getModelLayerFactory().newTaxonomiesSearchService();

		users.setUp(userServices);

		defineSchemasManager().using(zeCollectionSchemas.withContentMetadata());
		taxonomiesManager.addTaxonomy(zeCollectionSchemas.getTaxonomy1(), metadataSchemasManager);
		taxonomiesManager.addTaxonomy(zeCollectionSchemas.getTaxonomy2(), metadataSchemasManager);
		taxonomiesManager.setPrincipalTaxonomy(zeCollectionSchemas.getTaxonomy2(), metadataSchemasManager);
		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(zeCollectionSchemas.administrativeUnit.type().getCode()).setSecurity(true);
				types.getSchemaType(zeCollectionSchemas.classificationStation.type().getCode()).setSecurity(true);
				types.getSchemaType(zeCollectionSchemas.documentFond.type().getCode()).setSecurity(false);
				types.getSchemaType(zeCollectionSchemas.category.type().getCode()).setSecurity(false);
				types.getSchemaType(zeCollectionSchemas.folderSchema.type().getCode()).setSecurity(true);
				types.getSchemaType(zeCollectionSchemas.documentSchema.type().getCode()).setSecurity(true);
			}
		});
		zeCollectionRecords = zeCollectionSchemas.givenRecords(recordServices);

		userServices.addUserToCollection(users.alice(), zeCollection);
		userServices.addUserToCollection(users.bob(), zeCollection);
		userServices.addUserToCollection(users.charles(), zeCollection);
		userServices.addUserToCollection(users.dakotaLIndien(), zeCollection);
		userServices.addUserToCollection(users.edouardLechat(), zeCollection);
		userServices.addUserToCollection(users.gandalfLeblanc(), zeCollection);
		userServices.addUserToCollection(users.chuckNorris(), zeCollection);
		userServices.addUserToCollection(users.sasquatch(), zeCollection);
		userServices.addUserToCollection(users.robin(), zeCollection);

		userServices.addUserToCollection(users.admin(), zeCollection);
		userServices.addUserToCollection(users.chuckNorris(), zeCollection);

		recordServices.update(users.adminIn(zeCollection).setCollectionAllAccess(true));
		recordServices.update(users.aliceIn(zeCollection).setCollectionReadAccess(true));
		recordServices.update(users.dakotaIn(zeCollection).setCollectionReadAccess(true).setCollectionWriteAccess(true));

		authorizationsServices = getModelLayerFactory().newAuthorizationsServices();
		aliceId = users.aliceIn(zeCollection).getId();
		bobId = users.bobIn(zeCollection).getId();
		charlesId = users.charlesIn(zeCollection).getId();
		dakotaId = users.dakotaIn(zeCollection).getId();
		edouardId = users.edouardIn(zeCollection).getId();
		gandalfId = users.gandalfIn(zeCollection).getId();
		chuckId = users.chuckNorrisIn(zeCollection).getId();
		heroesId = users.heroesIn(zeCollection).getId();
		robinId = users.robinIn(zeCollection).getId();

		givenConfig(ConstellioEIMConfigs.CMIS_NEVER_RETURN_ACL, false);
		givenFolderInheritingTaxonomyAuthorizations();
		startApplication();

		CmisAcceptanceTestSetup.giveUseCMISPermissionToUsers(getModelLayerFactory());
	}

	@After
	public void stopApplicationAfterTest() {
		stopApplication();
	}

	//TODO Gabriel @Test
	public void whenNavigatingThenEverybodyHaveAccessToRoot() {
		//Test is passing, but not on workbench
		//FAIL ONLY WHEN GETDescendants
		session = newCMISSessionAsUserInZeCollection(aliceWonderland);
		CmisObject root = session.getObjectByPath("/");
		getChildren(root);
		assertThat(false).isTrue();
	}

	@Test
	public void whenGetParentOfRecordWithoutReadAccessThenError()
			throws Exception {

		String recordId = zeCollectionRecords.folder1_doc1.getId();
		session = newCMISSessionAsUserInZeCollection(admin);
		List<ObjectParentData> bindingParents = session.getBinding().getNavigationService().getObjectParents(
				session.getRepositoryInfo().getId(), recordId, null, false, NONE, null, true, null);
		assertThat(bindingParents).extracting("object.id").containsOnly(zeCollectionRecords.folder1.getId());

		session = newCMISSessionAsUserInZeCollection(aliceWonderland);
		bindingParents = session.getBinding().getNavigationService().getObjectParents(
				session.getRepositoryInfo().getId(), recordId, null, false, NONE, null, true, null);
		assertThat(bindingParents).extracting("object.id").containsOnly(zeCollectionRecords.folder1.getId());

		session = newCMISSessionAsUserInZeCollection(dakota);
		bindingParents = session.getBinding().getNavigationService().getObjectParents(
				session.getRepositoryInfo().getId(), recordId, null, false, NONE, null, true, null);
		assertThat(bindingParents).extracting("object.id").containsOnly(zeCollectionRecords.folder1.getId());

		session = newCMISSessionAsUserInZeCollection(bobGratton);

		try {
			bindingParents = session.getBinding().getNavigationService().getObjectParents(
					session.getRepositoryInfo().getId(), recordId, null, false, NONE, null, true, null);
			fail("Exception expected");
		} catch (CmisRuntimeException e) {
			assertThat(e.getMessage())
					.isEqualTo("L'utilisateur bob n'a pas de droit en lecture sur l'enregistrement folder1_doc1 - folder1_doc1");
		}

	}

	@Test
	public void givenRecordAsAuthOnALeafRecordThenCanCallGetChildrenAndGetParentsOnNodeLeadingToIt()
			throws Exception {

		authorizationsServices.add(authorizationForUsers(users.robinIn(zeCollection))
				.on(zeCollectionRecords.folder1_doc1).givingReadAccess(), users.adminIn(zeCollection));

		session = newCMISSessionAsUserInZeCollection(admin);
		assertThatChildren(session.getRootFolder()).containsOnly("taxo_taxo1", "taxo_taxo2");
		assertThatChildren("taxo_taxo1").containsOnly("zetaxo1_fond1");
		assertThatChildren("zetaxo1_fond1").containsOnly("zetaxo1_fond1_1", "zetaxo1_category2");
		assertThatChildren("zetaxo1_fond1_1").containsOnly("zetaxo1_category1");
		assertThatChildren("zetaxo1_category1").containsOnly("folder1", "folder2");
		assertThatChildren("folder1").containsOnly("folder1_doc1");

		assertThatChildren("taxo_taxo2").containsOnly("zetaxo2_unit1");
		assertThatChildren("zetaxo2_unit1").containsOnly("zetaxo2_station2", "zetaxo2_unit1_1");
		assertThatChildren("zetaxo2_station2").containsOnly("folder1", "zetaxo2_station2_1");
		assertThatChildren("folder1").containsOnly("folder1_doc1");

		session = newCMISSessionAsUserInZeCollection(robin);
		assertThatChildren(session.getRootFolder()).containsOnly("taxo_taxo1", "taxo_taxo2");
		assertThatChildren("taxo_taxo1").containsOnly("zetaxo1_fond1");
		assertThatChildren("zetaxo1_fond1").containsOnly("zetaxo1_fond1_1", "zetaxo1_category2");
		assertThatChildren("zetaxo1_fond1_1").containsOnly("zetaxo1_category1");
		assertThatChildren("zetaxo1_category1").containsOnly("folder1");
		assertThatChildren("folder1").containsOnly("folder1_doc1");

		assertThatChildren("taxo_taxo2").containsOnly("zetaxo2_unit1");
		assertThatChildren("zetaxo2_unit1").containsOnly("zetaxo2_station2");
		assertThatChildren("zetaxo2_station2").containsOnly("folder1");
		assertThatChildren("folder1").containsOnly("folder1_doc1");

	}

	private ListAssert<Object> assertThatChildren(String id) {
		Folder folder = (Folder) session.getObject(id);
		return assertThatChildren(folder);
	}

	private ListAssert<Object> assertThatChildren(Folder folder) {

		List<CmisObject> children = new ArrayList<>();
		for (CmisObject object : folder.getChildren()) {
			List<ObjectParentData> bindingParents = session.getBinding().getNavigationService().getObjectParents(
					session.getRepositoryInfo().getId(), object.getId(), null, false, NONE, null, true, null);
			assertThat(bindingParents).extracting("object.id").contains(folder.getId());
			children.add(object);
		}
		return assertThat(children).extracting("id");
	}

	@Test
	public void whenNavigatingThenReturnOnlyReadableFolders()
			throws RecordServicesException {

		session = newCMISSessionAsUserInZeCollection(admin);
		Folder cmisFolder1_doc1 = cmisFolder(zeCollectionRecords.folder1_doc1);
		Folder cmisFolder2 = cmisFolder(zeCollectionRecords.folder2);
		cmisFolder1_doc1.addAcl(asList(ace(bobGratton, RW), ace(charlesFrancoisXavier, RW)), REPOSITORYDETERMINED);
		cmisFolder2.addAcl(asList(ace(bobGratton, RW)), REPOSITORYDETERMINED);

		List<CmisObject> descendants = getDescendants("/");
		assertThat(descendants).hasSize(35);

		session = newCMISSessionAsUserInZeCollection(aliceWonderland);
		descendants = getDescendants("/");
		assertThat(descendants).hasSize(35);

		session = newCMISSessionAsUserInZeCollection(dakota);
		descendants = getDescendants("/");
		assertThat(descendants).hasSize(35);

		session = newCMISSessionAsUserInZeCollection(bobGratton);

		//Bob can access this record, because it contains record which he has access.
		assertThat(session.getObjectByPath("/taxo_taxo2/zetaxo2_unit1/zetaxo2_station2/zetaxo2_station2_1")).isNotNull();
		assertThat(session.getObject("zetaxo2_station2_1")).isNotNull();

		session.getObjectByPath("/taxo_taxo2/zetaxo2_unit1/zetaxo2_station2/folder1/folder1_doc1");
		descendants = getDescendants("/taxo_taxo2/zetaxo2_unit1/zetaxo2_station2/zetaxo2_station2_1/folder2");
		assertThat(descendants).hasSize(4);

		session = newCMISSessionAsUserInZeCollection(charlesFrancoisXavier);
		session.getObjectByPath("/taxo_taxo2/zetaxo2_unit1/zetaxo2_station2/folder1/folder1_doc1");
		try {
			session.getObjectByPath("/taxo_taxo2/zetaxo2_unit1/zetaxo2_station2/zetaxo2_station2_1/folder2");
			Fail.fail("Charles has access to folder2 but should not");
		} catch (Exception e) {
			assertThat(e.getMessage())
					.contains("L'utilisateur charles n'a pas de droit en lecture sur l'enregistrement folder2 - folder2");
		}

		try {
			session.getObject("folder2");
			Fail.fail("Charles has access to folder2 but should not");
		} catch (Exception e) {
			assertThat(e.getMessage())
					.contains("L'utilisateur charles n'a pas de droit en lecture sur l'enregistrement folder2 - folder2");
		}
	}

	@Test
	public void whenCreatingFolderInFolderThenOnlyWorksWithParentWriteAuthorization()
			throws RecordServicesException {
		String parentId = "folder2";

		session = newCMISSessionAsUserInZeCollection(admin);
		Folder parent = cmisFolder(zeCollectionRecords.folder2);
		parent.addAcl(asList(ace(bobGratton, RW), ace(charlesFrancoisXavier, R)), REPOSITORYDETERMINED);

		Record createdRecord = createNewFolderWithTestProperties(parentId);
		assertThat(createdRecord).isNotNull();

		session = newCMISSessionAsUserInZeCollection(aliceWonderland);
		createNewFolderWithTestPropertiesExpectingPermissionError(parentId);

		session = newCMISSessionAsUserInZeCollection(dakota);
		createdRecord = createNewFolderWithTestProperties(parentId);
		assertThat(createdRecord).isNotNull();

		session = newCMISSessionAsUserInZeCollection(bobGratton);
		createdRecord = createNewFolderWithTestProperties(parentId);
		assertThat(createdRecord).isNotNull();

		session = newCMISSessionAsUserInZeCollection(charlesFrancoisXavier);
		createNewFolderWithTestPropertiesExpectingPermissionError(parentId);
	}

	@Test
	public void whenCreatingFolderInAdministrativeUnitThenOnlyWorksWithParentWriteAuthorization()
			throws RecordServicesException {
		String parentId = "zetaxo2_station1";

		session = newCMISSessionAsUserInZeCollection(admin);
		Folder parent = cmisFolder(zeCollectionRecords.taxo2_station1);
		parent.addAcl(asList(ace(bobGratton, RW), ace(charlesFrancoisXavier, R)), REPOSITORYDETERMINED);

		Record createdRecord = createNewFolderWithTestProperties(parentId);
		assertThat(createdRecord).isNotNull();

		session = newCMISSessionAsUserInZeCollection(aliceWonderland);
		createNewFolderWithTestPropertiesExpectingPermissionError(parentId);

		session = newCMISSessionAsUserInZeCollection(dakota);
		createdRecord = createNewFolderWithTestProperties(parentId);
		assertThat(createdRecord).isNotNull();

		session = newCMISSessionAsUserInZeCollection(bobGratton);
		createdRecord = createNewFolderWithTestProperties(parentId);
		assertThat(createdRecord).isNotNull();

		session = newCMISSessionAsUserInZeCollection(charlesFrancoisXavier);
		createNewFolderWithTestPropertiesExpectingPermissionError(parentId);
	}

	@Test
	public void whenCreatingDocumentInFolderThenOnlyWorksWithParentWriteAuthorization()
			throws Exception {
		String parentId = "folder2";

		session = newCMISSessionAsUserInZeCollection(admin);
		Folder parent = cmisFolder(zeCollectionRecords.folder2);
		parent.addAcl(asList(ace(bobGratton, RW), ace(charlesFrancoisXavier, R)), REPOSITORYDETERMINED);

		Record createdRecord = createNewDocumentWithTestProperties(parentId);
		assertThat(createdRecord).isNotNull();

		session = newCMISSessionAsUserInZeCollection(aliceWonderland);
		createNewDocumentWithTestPropertiesExpectingPermissionError(parentId);

		session = newCMISSessionAsUserInZeCollection(dakota);
		createdRecord = createNewDocumentWithTestProperties(parentId);
		assertThat(createdRecord).isNotNull();

		session = newCMISSessionAsUserInZeCollection(bobGratton);
		createdRecord = createNewDocumentWithTestProperties(parentId);
		assertThat(createdRecord).isNotNull();

		session = newCMISSessionAsUserInZeCollection(charlesFrancoisXavier);
		createNewDocumentWithTestPropertiesExpectingPermissionError(parentId);
	}

	@Test
	public void whenCreatingDocumentInAdministrativeUnitThenOnlyWorksWithParentWriteAuthorization()
			throws Exception {
		String parentId = zeCollectionRecords.folder1.getId();

		session = newCMISSessionAsUserInZeCollection(admin);
		Folder parent = cmisFolder(zeCollectionRecords.folder1);
		parent.addAcl(asList(ace(bobGratton, RW), ace(charlesFrancoisXavier, R)), REPOSITORYDETERMINED);

		Record createdRecord = createNewDocumentWithTestProperties(parentId);
		assertThat(createdRecord).isNotNull();

		session = newCMISSessionAsUserInZeCollection(aliceWonderland);
		createNewDocumentWithTestPropertiesExpectingPermissionError(parentId);

		session = newCMISSessionAsUserInZeCollection(dakota);
		createdRecord = createNewDocumentWithTestProperties(parentId);
		assertThat(createdRecord).isNotNull();

		assertThat(users.bobIn(zeCollection).hasWriteAccess().on(recordServices.getDocumentById(parentId))).isTrue();
		session = newCMISSessionAsUserInZeCollection(bobGratton);
		createdRecord = createNewDocumentWithTestProperties(parentId);
		assertThat(createdRecord).isNotNull();

		session = newCMISSessionAsUserInZeCollection(charlesFrancoisXavier);
		createNewDocumentWithTestPropertiesExpectingPermissionError(parentId);
	}

	@Test
	public void whenEditingFolderInAdministrativeUnitThenOnlyWorksWithParentWriteAuthorization()
			throws RecordServicesException {

		String path = "/taxo_taxo2/zetaxo2_unit1/zetaxo2_station2_1/";
		String parentId = "zetaxo2_station2_1";

		session = newCMISSessionAsUserInZeCollection(admin);
		Record newFolder = createNewFolderWithTestProperties(parentId);
		Folder parent = cmisFolder(zeCollectionRecords.taxo2_station2_1);
		parent.addAcl(asList(ace(bobGratton, RW), ace(charlesFrancoisXavier, R)), REPOSITORYDETERMINED);

		CmisObject object = session.getObjectByPath(path + newFolder.getId());
		updateFolderWithTestProperties(object, "modifiedTitle");
		assertThat(object.getName()).isEqualTo("modifiedTitle");
		updateFolderWithTestProperties(object, "folderTest");

		session = newCMISSessionAsUserInZeCollection(aliceWonderland);
		object = session.getObjectByPath(path + newFolder.getId());
		updateFolderWithTestPropertiesExpectingPermissionError(object, "modifiedTitle");
		assertThat(object.getName()).isNotEqualTo("modifiedTitle");

		session = newCMISSessionAsUserInZeCollection(dakota);
		object = session.getObjectByPath(path + newFolder.getId());
		updateFolderWithTestProperties(object, "modifiedTitle");
		assertThat(object.getName()).isEqualTo("modifiedTitle");
		updateFolderWithTestProperties(object, "folderTest");

		session = newCMISSessionAsUserInZeCollection(bobGratton);
		object = session.getObjectByPath(path + newFolder.getId());
		updateFolderWithTestProperties(object, "modifiedTitle");
		assertThat(object.getName()).isEqualTo("modifiedTitle");
		updateFolderWithTestProperties(object, "folderTest");

		session = newCMISSessionAsUserInZeCollection(charlesFrancoisXavier);
		object = session.getObjectByPath(path + newFolder.getId());
		updateFolderWithTestPropertiesExpectingPermissionError(object, "modifiedTitle");
		assertThat(object.getName()).isNotEqualTo("modifiedTitle");
	}

	@Test
	public void whenEditingFolderInFolderThenOnlyWorksWithParentWriteAuthorization()
			throws RecordServicesException {
		String path = "/taxo_taxo2/zetaxo2_unit1/zetaxo2_station2/zetaxo2_station2_1/folder2/folder2_2";

		session = newCMISSessionAsUserInZeCollection(admin);
		Folder parent = cmisFolder(zeCollectionRecords.folder2);
		parent.addAcl(asList(ace(bobGratton, RW), ace(charlesFrancoisXavier, R)), REPOSITORYDETERMINED);

		CmisObject object = session.getObjectByPath(path);
		updateFolderWithTestProperties(object, "folderTest");
		assertThat(object.getName()).isEqualTo("folderTest");
		updateFolderWithTestProperties(object, "folder2_2");

		session = newCMISSessionAsUserInZeCollection(aliceWonderland);
		object = session.getObjectByPath(path);
		updateFolderWithTestPropertiesExpectingPermissionError(object, "folderTest");
		assertThat(object.getName()).isNotEqualTo("folderTest");
		updateFolderWithTestPropertiesExpectingPermissionError(object, "folder2_2");

		session = newCMISSessionAsUserInZeCollection(dakota);
		object = session.getObjectByPath(path);
		updateFolderWithTestProperties(object, "folderTest");
		assertThat(object.getName()).isEqualTo("folderTest");
		updateFolderWithTestProperties(object, "folder2_2");

		session = newCMISSessionAsUserInZeCollection(bobGratton);
		object = session.getObjectByPath(path);
		updateFolderWithTestProperties(object, "folderTest");
		assertThat(object.getName()).isEqualTo("folderTest");
		updateFolderWithTestProperties(object, "folder2_2");

		session = newCMISSessionAsUserInZeCollection(charlesFrancoisXavier);
		object = session.getObjectByPath(path);
		updateFolderWithTestPropertiesExpectingPermissionError(object, "folderTest");
		assertThat(object.getName()).isNotEqualTo("folderTest");
		updateFolderWithTestPropertiesExpectingPermissionError(object, "folder2_2");
	}

	@Test
	public void whenEditingDocumentInAdministrativeUnitThenOnlyWorksWithParentWriteAuthorization()
			throws RecordServicesException {
		String path = "/taxo_taxo2/zetaxo2_unit1/zetaxo2_station2_1/folder2/";
		String parentId = "folder2";

		session = newCMISSessionAsUserInZeCollection(admin);
		Record newDocument = createNewDocumentWithTestProperties(parentId);
		Folder parent = cmisFolder(zeCollectionRecords.folder2);
		parent.addAcl(asList(ace(bobGratton, RW), ace(charlesFrancoisXavier, R)), REPOSITORYDETERMINED);

		CmisObject object = session.getObjectByPath(path + newDocument.getId());
		updateDocumentWithTestProperties(object, "modifiedTitle");
		assertThat(object.getName()).isEqualTo("modifiedTitle");
		updateDocumentWithTestProperties(object, "testDocument");

		session = newCMISSessionAsUserInZeCollection(aliceWonderland);
		object = session.getObjectByPath(path + newDocument.getId());
		updateDocumentWithTestPropertiesExpectingPermissionError(object, "modifiedTitle");
		assertThat(object.getName()).isNotEqualTo("modifiedTitle");

		session = newCMISSessionAsUserInZeCollection(dakota);
		object = session.getObjectByPath(path + newDocument.getId());
		updateDocumentWithTestProperties(object, "modifiedTitle");
		assertThat(object.getName()).isEqualTo("modifiedTitle");
		updateDocumentWithTestProperties(object, "testDocument");

		session = newCMISSessionAsUserInZeCollection(bobGratton);
		object = session.getObjectByPath(path + newDocument.getId());
		updateDocumentWithTestProperties(object, "modifiedTitle");
		assertThat(object.getName()).isEqualTo("modifiedTitle");
		updateDocumentWithTestProperties(object, "testDocument");

		session = newCMISSessionAsUserInZeCollection(charlesFrancoisXavier);
		object = session.getObjectByPath(path + newDocument.getId());
		updateDocumentWithTestPropertiesExpectingPermissionError(object, "modifiedTitle");
		assertThat(object.getName()).isNotEqualTo("modifiedTitle");
	}

	@Test
	public void whenEditingDocumentInFolderThenOnlyWorksWithParentWriteAuthorization()
			throws RecordServicesException {
		String path = "/taxo_taxo2/zetaxo2_unit1/zetaxo2_station2/zetaxo2_station2_1/folder2/folder2_2/folder2_2_doc1";

		session = newCMISSessionAsUserInZeCollection(admin);
		Folder parent = cmisFolder(zeCollectionRecords.folder2_2);
		parent.addAcl(asList(ace(bobGratton, RW), ace(charlesFrancoisXavier, R)), REPOSITORYDETERMINED);

		CmisObject object = session.getObjectByPath(path);
		updateDocumentWithTestProperties(object, "documentTest");
		assertThat(object.getName()).isEqualTo("documentTest");
		updateDocumentWithTestProperties(object, "folder2_2_doc1");

		session = newCMISSessionAsUserInZeCollection(aliceWonderland);
		object = session.getObjectByPath(path);
		updateDocumentWithTestPropertiesExpectingPermissionError(object, "documentTest");
		assertThat(object.getName()).isNotEqualTo("documentTest");

		session = newCMISSessionAsUserInZeCollection(dakota);
		object = session.getObjectByPath(path);
		updateDocumentWithTestProperties(object, "documentTest");
		assertThat(object.getName()).isEqualTo("documentTest");
		updateDocumentWithTestProperties(object, "folder2_2_doc1");

		session = newCMISSessionAsUserInZeCollection(bobGratton);
		object = session.getObjectByPath(path);
		updateDocumentWithTestProperties(object, "documentTest");
		assertThat(object.getName()).isEqualTo("documentTest");
		updateDocumentWithTestProperties(object, "folder2_2_doc1");

		session = newCMISSessionAsUserInZeCollection(charlesFrancoisXavier);
		object = session.getObjectByPath(path);
		updateDocumentWithTestPropertiesExpectingPermissionError(object, "documentTest");
		assertThat(object.getName()).isNotEqualTo("documentTest");
	}

	@Test
	public void whenMovingFolderInFolderThenOnlyWorksWithNewParentWriteAuthorization()
			throws RecordServicesException {

		session = newCMISSessionAsUserInZeCollection(admin);
		Folder oldParent = cmisFolder(zeCollectionRecords.folder2);
		oldParent.addAcl(asList(ace(bobGratton, RW), ace(charlesFrancoisXavier, RW)), REPOSITORYDETERMINED);
		Folder movedFolder = cmisFolder(zeCollectionRecords.folder2_2);
		movedFolder.addAcl(asList(ace(bobGratton, RW), ace(charlesFrancoisXavier, RW)), REPOSITORYDETERMINED);
		Folder newParent = cmisFolder(zeCollectionRecords.folder1);
		newParent.addAcl(asList(ace(bobGratton, RW), ace(charlesFrancoisXavier, R)), REPOSITORYDETERMINED);

		Record record = zeCollectionRecords.folder2_2;
		String newParentID = zeCollectionRecords.folder1.getId();

		assertThat(canBeMovedTo(record, newParentID)).isTrue();

		session = newCMISSessionAsUserInZeCollection(aliceWonderland);
		assertThat(canBeMovedTo(record, newParentID)).isFalse();

		session = newCMISSessionAsUserInZeCollection(dakota);
		assertThat(canBeMovedTo(record, newParentID)).isTrue();

		session = newCMISSessionAsUserInZeCollection(bobGratton);
		assertThat(canBeMovedTo(record, newParentID)).isTrue();

		session = newCMISSessionAsUserInZeCollection(charlesFrancoisXavier);
		assertThat(canBeMovedTo(record, newParentID)).isFalse();
	}

	@Test
	public void whenMovingFolderInAdministrativeUnitThenOnlyWorksWithNewParentWriteAuthorization()
			throws RecordServicesException {

		session = newCMISSessionAsUserInZeCollection(admin);
		Folder oldParent = cmisFolder(zeCollectionRecords.taxo2_station2_1);
		oldParent.addAcl(asList(ace(bobGratton, RW), ace(charlesFrancoisXavier, R)), REPOSITORYDETERMINED);
		Folder movedFolder = cmisFolder(zeCollectionRecords.folder2);
		movedFolder.addAcl(asList(ace(bobGratton, RW), ace(charlesFrancoisXavier, R)), REPOSITORYDETERMINED);
		Folder newParent = cmisFolder(zeCollectionRecords.taxo2_unit1_1);
		newParent.addAcl(asList(ace(bobGratton, RW), ace(charlesFrancoisXavier, R)), REPOSITORYDETERMINED);

		Record record = zeCollectionRecords.folder2;
		String newParentID = zeCollectionRecords.taxo2_unit1_1.getId();

		assertThat(canBeMovedTo(record, newParentID, zeCollectionRecords.taxo2_station2_1)).isTrue();

		session = newCMISSessionAsUserInZeCollection(aliceWonderland);
		assertThat(canBeMovedTo(record, newParentID, zeCollectionRecords.taxo2_station2_1)).isFalse();

		session = newCMISSessionAsUserInZeCollection(dakota);
		assertThat(canBeMovedTo(record, newParentID, zeCollectionRecords.taxo2_station2_1)).isTrue();

		session = newCMISSessionAsUserInZeCollection(bobGratton);
		assertThat(canBeMovedTo(record, newParentID, zeCollectionRecords.taxo2_station2_1)).isTrue();

		session = newCMISSessionAsUserInZeCollection(charlesFrancoisXavier);
		assertThat(canBeMovedTo(record, newParentID, zeCollectionRecords.taxo2_station2_1)).isFalse();
	}

	@Test
	public void whenMovingDocumentInFolderThenOnlyWorksWithParentWriteAuthorization()
			throws RecordServicesException {

		session = newCMISSessionAsUserInZeCollection(admin);
		Folder oldParent = cmisFolder(zeCollectionRecords.folder1);
		oldParent.addAcl(asList(ace(bobGratton, RW), ace(charlesFrancoisXavier, RW)), REPOSITORYDETERMINED);
		Folder movedFolder = cmisFolder(zeCollectionRecords.folder1_doc1);
		movedFolder.addAcl(asList(ace(bobGratton, RW), ace(charlesFrancoisXavier, RW)), REPOSITORYDETERMINED);
		Folder newParent = cmisFolder(zeCollectionRecords.folder2);
		newParent.addAcl(asList(ace(bobGratton, RW), ace(charlesFrancoisXavier, R)), REPOSITORYDETERMINED);

		Record record = zeCollectionRecords.folder1_doc1;
		String newParentID = zeCollectionRecords.folder2.getId();

		assertThat(canBeMovedTo(record, newParentID)).isTrue();

		session = newCMISSessionAsUserInZeCollection(aliceWonderland);
		assertThat(canBeMovedTo(record, newParentID)).isFalse();

		session = newCMISSessionAsUserInZeCollection(dakota);
		assertThat(canBeMovedTo(record, newParentID)).isTrue();

		session = newCMISSessionAsUserInZeCollection(bobGratton);
		assertThat(canBeMovedTo(record, newParentID)).isTrue();

		session = newCMISSessionAsUserInZeCollection(charlesFrancoisXavier);
		assertThat(canBeMovedTo(record, newParentID)).isFalse();
	}

	@Test
	public void whenDeletingFolderInFolderThenOnlyWorksWithParentDeleteAuthorization()
			throws RecordServicesException {

		session = newCMISSessionAsUserInZeCollection(admin);
		Folder parent = cmisFolder(zeCollectionRecords.folder1);
		parent.addAcl(asList(ace(bobGratton, RWD), ace(charlesFrancoisXavier, RW)), REPOSITORYDETERMINED);

		String parentID = parent.getId();

		Record deletedRecord = createAndDeleteNewFolder(admin, parentID);
		assertThat(deletedRecord).isNull();

		deletedRecord = createAndDeleteNewFolder(aliceWonderland, parentID);
		assertThat(deletedRecord).isNotNull();

		deletedRecord = createAndDeleteNewFolder(dakota, parentID);
		assertThat(deletedRecord).isNotNull();

		deletedRecord = createAndDeleteNewFolder(bobGratton, parentID);
		assertThat(deletedRecord).isNull();

		deletedRecord = createAndDeleteNewFolder(charlesFrancoisXavier, parentID);
		assertThat(deletedRecord).isNotNull();
	}

	@Test
	public void whenDeletingFolderInAdministrativeUnitThenOnlyWorksWithParentDeleteAuthorization()
			throws RecordServicesException {

		session = newCMISSessionAsUserInZeCollection(admin);
		Folder parent = cmisFolder(zeCollectionRecords.taxo2_station2_1);
		parent.addAcl(asList(ace(bobGratton, RWD), ace(charlesFrancoisXavier, RW)), REPOSITORYDETERMINED);

		String parentID = parent.getId();

		Record deletedRecord = createAndDeleteNewFolder(admin, parentID);
		assertThat(deletedRecord).isNull();

		deletedRecord = createAndDeleteNewFolder(aliceWonderland, parentID);
		assertThat(deletedRecord).isNotNull();

		deletedRecord = createAndDeleteNewFolder(dakota, parentID);
		assertThat(deletedRecord).isNotNull();

		deletedRecord = createAndDeleteNewFolder(bobGratton, parentID);
		assertThat(deletedRecord).isNull();

		deletedRecord = createAndDeleteNewFolder(charlesFrancoisXavier, parentID);
		assertThat(deletedRecord).isNotNull();
	}

	@Test
	public void whenDeletingDocumentInFolderThenOnlyWorksWithParentDeleteAuthorization()
			throws Exception {

		session = newCMISSessionAsUserInZeCollection(admin);
		Folder parent = cmisFolder(zeCollectionRecords.folder1);
		parent.addAcl(asList(ace(bobGratton, RWD), ace(charlesFrancoisXavier, RW)), REPOSITORYDETERMINED);

		String parentID = parent.getId();

		Record deletedRecord = createAndDeleteNewDocument(admin, parentID);
		assertThat(deletedRecord).isNull();

		deletedRecord = createAndDeleteNewDocument(aliceWonderland, parentID);
		assertThat(deletedRecord).isNotNull();

		deletedRecord = createAndDeleteNewDocument(dakota, parentID);
		assertThat(deletedRecord).isNotNull();

		deletedRecord = createAndDeleteNewDocument(bobGratton, parentID);
		assertThat(deletedRecord).isNull();

		deletedRecord = createAndDeleteNewDocument(charlesFrancoisXavier, parentID);
		assertThat(deletedRecord).isNotNull();
	}

	@Test
	public void whenGetContentInputStreamThenOnlyWorkIfUserHasAccess()
			throws Exception {
		session = newCMISSessionAsUserInZeCollection(admin);

		Map<String, Object> properties = new HashMap<>();
		properties.put(PropertyIds.NAME, "cmis:document");
		properties.put(PropertyIds.OBJECT_TYPE_ID, "document_default");

		Folder document = cmisFolder(zeCollectionRecords.folder1).createFolder(properties);
		document.addAcl(asList(ace(bobGratton, RW), ace(charlesFrancoisXavier, R)), REPOSITORYDETERMINED);

		properties = new HashMap<>();
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
		//properties.put("metadata", "content");
		String id = document.createDocument(properties, pdf1ContentStream(), VersioningState.MAJOR).getId();

		for (String user : asList(charlesFrancoisXavier, bobGratton, admin, aliceWonderland, dakota)) {
			session = newCMISSessionAsUserInZeCollection(charlesFrancoisXavier);
			assertThat(toByteArray(((Document) session.getObject(id)).getContentStream().getStream()).length)
					.isEqualTo((int) pdf1Length);
		}

		for (String user : asList(robin, edouard)) {
			session = newCMISSessionAsUserInZeCollection(robin);
			try {
				toByteArray(((Document) session.getObject(id)).getContentStream().getStream());
				fail("Exception expected");
			} catch (CmisRuntimeException e) {
				assertThat(e.getMessage()).contains("permission CMIS CAN_GET_CONTENT_STREAM");
			}
		}
	}

	@Test
	public void whenCheckingInThenOnlyWorksIfUserIsBorrower()
			throws Exception {
		session = newCMISSessionAsUserInZeCollection(admin);

		Map<String, Object> properties = new HashMap<>();
		properties.put(PropertyIds.NAME, "cmis:document");
		properties.put(PropertyIds.OBJECT_TYPE_ID, "document_default");

		Folder document = cmisFolder(zeCollectionRecords.folder1).createFolder(properties);
		document.addAcl(asList(ace(bobGratton, RW), ace(charlesFrancoisXavier, R)), REPOSITORYDETERMINED);

		properties = new HashMap<>();
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
		//properties.put("metadata", "content");
		Document content = document.createDocument(properties, pdf1ContentStream(), VersioningState.MAJOR);

		session = newCMISSessionAsUserInZeCollection(charlesFrancoisXavier);
		content = (Document) session.getObject(content.getId());
		try {

			content.checkOut();
			fail("Exception expected");
		} catch (CmisRuntimeException e) {
			assertThat(e.getMessage()).contains("permission CMIS CAN_CHECK_OUT");
		}

		session = newCMISSessionAsUserInZeCollection(admin);
		content = (Document) session.getObject(content.getId());
		content.checkOut();

		session = newCMISSessionAsUserInZeCollection(charlesFrancoisXavier);
		content = (Document) session.getObject(content.getId());
		try {

			content.checkIn(true, new HashMap<String, Object>(), pdf2ContentStream(), "Ze comment");
			fail("Exception expected");
		} catch (CmisRuntimeException e) {
			assertThat(e.getMessage()).contains("permission CMIS CAN_CHECK_IN");
		}

		session = newCMISSessionAsUserInZeCollection(admin);
		content = (Document) session.getObject(content.getId());
		content.checkIn(true, new HashMap<String, Object>(), pdf2ContentStream(), "Ze comment");
	}

	private ContentStream pdf1ContentStream()
			throws IOException {
		String filename = "pdf1.pdf";
		BigInteger length = BigInteger.valueOf(pdf1Length);
		InputStream stream = getTestResourceInputStream(CmisSinglevalueContentManagementAcceptTest.class, "pdf1.pdf");
		return new ContentStreamImpl(filename, length, PDF_MIMETYPE, stream);
	}

	private ContentStream pdf2ContentStream()
			throws IOException {
		String filename = "pdf2.pdf";
		BigInteger length = BigInteger.valueOf(pdf2Length);
		String mimetype = PDF_MIMETYPE;
		InputStream stream = getTestResourceInputStream(CmisSinglevalueContentManagementAcceptTest.class, "pdf2.pdf");
		return new ContentStreamImpl(filename, length, mimetype, stream);
	}

	private List<CmisObject> getChildren(CmisObject parent) {
		List<CmisObject> children = new ArrayList<>();
		if (parent.getBaseTypeId().value().equals("CMIS_FOLDER")) {
			Iterator<CmisObject> childrenIterator = ((Folder) parent).getChildren().iterator();
			while (childrenIterator.hasNext()) {
				children.add(childrenIterator.next());
			}
		}

		return children;
	}

	private List<CmisObject> getDescendants(String parentPath) {
		CmisObject parent = session.getObjectByPath(parentPath);
		return getDescendants(parent);
	}

	private List<CmisObject> getDescendants(CmisObject parent) {
		List<CmisObject> children = new ArrayList<>();
		List<CmisObject> descendants = new ArrayList<>();
		Iterator<CmisObject> childrenIterator = ((Folder) parent).getChildren().iterator();
		if (childrenIterator != null) {
			while (childrenIterator.hasNext()) {
				children.add(childrenIterator.next());
			}
		}
		for (CmisObject child : children) {
			descendants.addAll(getDescendants(child));
		}
		children.addAll(descendants);

		return children;
	}

	private void givenFolderInheritingTaxonomyAuthorizations() {
		Authorization authorization = new AuthorizationBuilder(zeCollection).forUsers(users.edouardIn(zeCollection))
				.on(zeCollectionRecords.taxo2_station2_1).givingReadWriteAccess();
		getModelLayerFactory().newAuthorizationsServices().add(authorization, users.adminIn(zeCollection));
		try {
			waitForBatchProcess();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private Ace ace(String principal, List<String> permissions) {
		return session.getObjectFactory().createAce(principal, permissions);
	}

	private Folder cmisFolder(Record record) {
		return (Folder) session.getObject(record.getId());
	}

	private String createNewFolderWithTestProperties(String parent, String objectType) {
		ObjectId parentFolderId = new ObjectIdImpl(parent);
		Map<String, Object> newFolderProperties = new HashMap<>();
		newFolderProperties.put(PropertyIds.OBJECT_TYPE_ID, objectType);
		newFolderProperties.put("title", "testFolder");
		newFolderProperties.put("numberMeta", 42.666);
		return session.createFolder(newFolderProperties, parentFolderId).getId();
	}

	private Record createNewFolderWithTestProperties(String parentId) {
		return recordServices.getDocumentById(createNewFolderWithTestProperties(parentId, "folder_default"));
	}

	private void createNewFolderWithTestPropertiesExpectingPermissionError(String parentId) {
		try {
			createNewFolderWithTestProperties(parentId);
		} catch (CmisRuntimeException e) {
			if (e.getMessage().contains("permission CMIS CAN_CREATE_FOLDER")) {
				//OK
			} else {
				throw e;
			}

		}
	}

	private void createNewDocumentWithTestPropertiesExpectingPermissionError(String parentId) {

		try {
			createNewDocumentWithTestProperties(parentId);
			fail("Exception expected");
		} catch (CmisRuntimeException e) {
			if (e.getMessage().contains("permission CMIS CAN_CREATE_FOLDER")) {
				//OK
			} else {
				throw e;
			}
		}
	}

	private Record createNewDocumentWithTestProperties(String parentId) {
		String title = "testDocument";
		String mimeType = "image/tiff";
		File documentFile = getTestResourceFile(ContentManagementAcceptTest.class, "pdf1.pdf");

		return recordServices.getDocumentById(addDocumentToFolder(documentFile, title, mimeType, parentId));
	}

	private String addDocumentToFolder(File documentFile, String title, String mimeType, String savedInFolder) {

		ObjectId savedInFolderObjectId = new ObjectIdImpl(savedInFolder);

		//
		//-- Create a document (considered as a Folder by CMIS)
		Map<String, Object> documentProperties = new HashMap<>();
		documentProperties.put(PropertyIds.OBJECT_TYPE_ID, "document_default");
		documentProperties.put("title", title);
		ObjectId newDocumentId = session.createFolder(documentProperties, savedInFolderObjectId);

		return newDocumentId.getId();
	}

	private Record createAndDeleteNewFolder(String user, String parentId) {
		session = newCMISSessionAsUserInZeCollection(admin);
		String id = createNewFolderWithTestProperties(parentId, "folder_default");

		session = newCMISSessionAsUserInZeCollection(user);
		ObjectId objectId = new ObjectIdImpl(id);
		try {
			((Folder) session.getObject(objectId)).deleteTree(true, UnfileObject.DELETE, true);
		} catch (CmisRuntimeException e) {
			//OK
		}
		try {
			return recordServices.getDocumentById(id);
		} catch (Exception e) {
			return null;
		}
	}

	private Record createAndDeleteNewDocument(String user, String parentId)
			throws Exception {
		session = newCMISSessionAsUserInZeCollection(admin);
		String id = createNewDocumentWithTestProperties(parentId).getId();

		session = newCMISSessionAsUserInZeCollection(user);
		ObjectId objectId = new ObjectIdImpl(id);
		try {
			((Folder) session.getObject(objectId)).deleteTree(true, UnfileObject.DELETE, true);
		} catch (CmisRuntimeException e) {
			//OK
		}
		try {
			return recordServices.getDocumentById(id);
		} catch (Exception e) {
			return null;
		}
	}

	private void updateFolderWithTestPropertiesExpectingPermissionError(CmisObject objectList, String title) {

		try {
			updateFolderWithTestProperties(objectList, title);
			fail("Exception expected");
		} catch (CmisRuntimeException e) {
			if (e.getMessage().contains("permission CMIS CAN_UPDATE_PROPERTIES")) {
				//OK
			} else {
				throw e;
			}
		}

	}

	private void updateFolderWithTestProperties(CmisObject objectList, String title) {
		Map<String, Object> newFolderProperties = new HashMap<>();
		newFolderProperties.put(PropertyIds.OBJECT_TYPE_ID, "folder_default");
		newFolderProperties.put("title", title);
		//        newFolderProperties.put("numberMeta", 42.666);
		((Folder) objectList).updateProperties(newFolderProperties);
	}

	private void updateDocumentWithTestPropertiesExpectingPermissionError(CmisObject objectList, String title) {
		try {
			updateDocumentWithTestProperties(objectList, title);
			fail("Exception expected");
		} catch (CmisRuntimeException e) {
			if (e.getMessage().contains("permission CMIS CAN_UPDATE_PROPERTIES")) {
				//OK
			} else {
				throw e;
			}
		}
	}

	private void updateDocumentWithTestProperties(CmisObject objectList, String title) {
		Map<String, Object> contentProperties = new HashMap<>();
		contentProperties.put(PropertyIds.OBJECT_TYPE_ID, "document_default");
		contentProperties.put("title", title);

		((Folder) objectList).updateProperties(contentProperties);
	}

	private void moveObject(Record record, String parentTargetId) {
		CmisObject object = session.getObject(record.getId());
		Holder<String> objectIdHolder = new Holder<String>(object.getId());

		session.getBinding().getObjectService()
				.moveObject(session.getRepositoryInfo().getId(), objectIdHolder, parentTargetId, record.getId(), null);

		recordServices.refresh(record);
	}

	private boolean canBeMovedTo(Record record, String parentTargetId) {
		String oldParentID = record.getParentId();

		try {
			moveObject(record, parentTargetId);
		} catch (CmisRuntimeException e) {
			if (e.getMessage().contains("permission CMIS CAN_MOVE_OBJECT") ||
					e.getMessage().contains("permission CMIS CAN_CREATE_FOLDER")) {
				return false;
			} else {
				throw e;
			}
		}
		boolean isMovable = record.getParentId().equals(parentTargetId);
		moveObject(record, oldParentID);

		return isMovable;
	}

	private boolean canBeMovedTo(Record record, String parentTargetId, Record oldParent) {

		try {
			moveObject(record, parentTargetId);
		} catch (CmisRuntimeException e) {
			if (e.getMessage().contains("permission CMIS CAN_MOVE_OBJECT")) {
				return false;
			} else {
				throw e;
			}
		}
		boolean isMovable = !record.get(Schemas.PATH).toString()
				.equals(oldParent.get(Schemas.PATH).toString() + "/" + record.getId());
		moveObject(record, oldParent.getId());

		return isMovable;
	}

	private void printTaxonomies(User user) {
		StringBuilder stringBuilder = new StringBuilder();
		for (Taxonomy taxonomy : taxonomiesManager.getEnabledTaxonomies(zeCollection)) {
			stringBuilder.append(taxonomy.getCode() + " : \n");
			for (Record record : taxonomiesSearchServices
					.getRootConcept(zeCollection, taxonomy.getCode(), new TaxonomiesSearchOptions().setRows(100))) {

				printConcept(user, taxonomy.getCode(), record, 1, stringBuilder);
			}
			stringBuilder.append("\n\n");
		}
		System.out.println(stringBuilder.toString());
	}

	private void printConcept(User user, String taxonomy, Record record, int level, StringBuilder stringBuilder) {
		for (int i = 0; i < level; i++) {
			stringBuilder.append("\t");
		}
		stringBuilder.append(record.getId() + "\n");
		for (TaxonomySearchRecord child : taxonomiesSearchServices
				.getVisibleChildConcept(user, taxonomy, record, new TaxonomiesSearchOptions().setRows(100))) {

			printConcept(user, taxonomy, child.getRecord(), level + 1, stringBuilder);
		}
	}

	private Condition<? super CmisObject> property(final String key, final Object value) {
		return new Condition<CmisObject>() {
			@Override
			public boolean matches(CmisObject objectData) {
				assertThat(objectData.getPropertyValue(key)).describedAs(key).isEqualTo(value);
				return true;
			}
		};
	}
}
