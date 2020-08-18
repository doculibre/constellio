package com.constellio.app.api.cmis.accept;

import com.constellio.app.api.cmis.accept.CmisAcceptanceTestSetup.Records;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordHierarchyServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.reindexing.ReindexationMode;
import com.constellio.model.services.records.reindexing.ReindexingServices;
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
import com.constellio.sdk.tests.annotations.DriverTest;
import com.constellio.sdk.tests.setups.Users;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.assertj.core.api.IterableAssert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForUsers;
import static org.apache.chemistry.opencmis.commons.enums.Action.CAN_APPLY_ACL;
import static org.apache.chemistry.opencmis.commons.enums.Action.CAN_CHECK_IN;
import static org.apache.chemistry.opencmis.commons.enums.Action.CAN_CHECK_OUT;
import static org.apache.chemistry.opencmis.commons.enums.Action.CAN_CREATE_DOCUMENT;
import static org.apache.chemistry.opencmis.commons.enums.Action.CAN_CREATE_FOLDER;
import static org.apache.chemistry.opencmis.commons.enums.Action.CAN_DELETE_CONTENT_STREAM;
import static org.apache.chemistry.opencmis.commons.enums.Action.CAN_DELETE_OBJECT;
import static org.apache.chemistry.opencmis.commons.enums.Action.CAN_DELETE_TREE;
import static org.apache.chemistry.opencmis.commons.enums.Action.CAN_GET_ACL;
import static org.apache.chemistry.opencmis.commons.enums.Action.CAN_GET_ALL_VERSIONS;
import static org.apache.chemistry.opencmis.commons.enums.Action.CAN_GET_CHILDREN;
import static org.apache.chemistry.opencmis.commons.enums.Action.CAN_GET_CONTENT_STREAM;
import static org.apache.chemistry.opencmis.commons.enums.Action.CAN_GET_FOLDER_PARENT;
import static org.apache.chemistry.opencmis.commons.enums.Action.CAN_GET_FOLDER_TREE;
import static org.apache.chemistry.opencmis.commons.enums.Action.CAN_GET_OBJECT_PARENTS;
import static org.apache.chemistry.opencmis.commons.enums.Action.CAN_GET_PROPERTIES;
import static org.apache.chemistry.opencmis.commons.enums.Action.CAN_MOVE_OBJECT;
import static org.apache.chemistry.opencmis.commons.enums.Action.CAN_SET_CONTENT_STREAM;
import static org.apache.chemistry.opencmis.commons.enums.Action.CAN_UPDATE_PROPERTIES;
import static org.assertj.core.api.Assertions.assertThat;

@DriverTest
public class CmisAllowableActionsAcceptanceTest extends ConstellioTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CmisAllowableActionsAcceptanceTest.class);

	UserServices userServices;
	TaxonomiesManager taxonomiesManager;
	MetadataSchemasManager metadataSchemasManager;
	RecordServices recordServices;
	Users users = new Users();
	CmisAcceptanceTestSetup zeCollectionSchemas = new CmisAcceptanceTestSetup(zeCollection);
	Records records;

	//	Session cmisSession;
	Session session;

	AuthorizationsServices authorizationsServices;

	String aliceId, bobId, charlesId, dakotaId, edouardId, chuckId, gandalfId, robinId, heroesId;

	@Before
	public void setUp()
			throws Exception {

		userServices = getModelLayerFactory().newUserServices();
		taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		recordServices = getModelLayerFactory().newRecordServices();

		users.setUp(userServices);

		defineSchemasManager().using(zeCollectionSchemas.withContentMetadata());
		zeCollectionSchemas.allSchemaTypesSupported(getAppLayerFactory());
		taxonomiesManager.addTaxonomy(zeCollectionSchemas.getTaxonomy1(), metadataSchemasManager);
		taxonomiesManager.addTaxonomy(zeCollectionSchemas.getTaxonomy2(), metadataSchemasManager);
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
		records = zeCollectionSchemas.givenRecords(recordServices);

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

		recordServices.update(users.adminIn(zeCollection).setCollectionReadAccess(true).setCollectionWriteAccess(true)
				.setCollectionDeleteAccess(true));
		recordServices.update(users.aliceIn(zeCollection).setCollectionReadAccess(true));
		recordServices.update(users.chuckNorrisIn(zeCollection).setCollectionReadAccess(true).setCollectionWriteAccess(true)
				.setCollectionDeleteAccess(true));
		recordServices.update(users.gandalfIn(zeCollection).setCollectionReadAccess(true).setCollectionWriteAccess(true));

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

		CmisAcceptanceTestSetup.giveUseCMISPermissionToUsers(getModelLayerFactory());

	}

	@Test
	public void whenGetAllowableActionsOfRootThenOK()
			throws Exception {
		givenTaxonomy2IsPrincipalWithAuthOnAConcept();
		session = newCMISSessionAsUserInZeCollection(admin);
		assertThatAllowableActionsOf("/").containsOnly(CAN_GET_PROPERTIES, CAN_GET_CHILDREN);

		session = newCMISSessionAsUserInZeCollection(aliceWonderland);
		assertThatAllowableActionsOf("/").containsOnly(CAN_GET_PROPERTIES, CAN_GET_CHILDREN);

		session = newCMISSessionAsUserInZeCollection(bobGratton);
		assertThatAllowableActionsOf("/").containsOnly(CAN_GET_PROPERTIES, CAN_GET_CHILDREN);

	}

	@Test
	public void whenGetAllowableActionsOfTaxonomyThenOK()
			throws Exception {
		givenTaxonomy2IsPrincipalWithAuthOnAConcept();
		session = newCMISSessionAsUserInZeCollection(admin);
		assertThatAllowableActionsOf("/taxo_taxo1").containsOnly(CAN_GET_PROPERTIES, CAN_GET_CHILDREN);
		assertThatAllowableActionsOf("/taxo_taxo2").containsOnly(CAN_GET_PROPERTIES, CAN_GET_CHILDREN);

		session = newCMISSessionAsUserInZeCollection(aliceWonderland);
		assertThatAllowableActionsOf("/taxo_taxo1").containsOnly(CAN_GET_PROPERTIES, CAN_GET_CHILDREN);
		assertThatAllowableActionsOf("/taxo_taxo2").containsOnly(CAN_GET_PROPERTIES, CAN_GET_CHILDREN);

		session = newCMISSessionAsUserInZeCollection(bobGratton);
		assertThatAllowableActionsOf("/taxo_taxo1").containsOnly(CAN_GET_PROPERTIES, CAN_GET_CHILDREN);
		assertThatAllowableActionsOf("/taxo_taxo2").containsOnly(CAN_GET_PROPERTIES, CAN_GET_CHILDREN);
	}

	@Test
	public void whenGetAllowableActionsOfTaxonomyConceptThenOK()
			throws Exception {
		givenTaxonomy2IsPrincipalWithAuthOnAConcept();
		Action[] secondaryTaxoExpectedActionsOfUserOrAdmin = new Action[]{CAN_GET_PROPERTIES, CAN_GET_FOLDER_PARENT,
																		  CAN_CREATE_FOLDER, CAN_GET_CHILDREN, CAN_GET_FOLDER_TREE, CAN_GET_OBJECT_PARENTS};

		Action[] principalTaxoExpectedActionsOfAdmin = new Action[]{CAN_GET_PROPERTIES, CAN_GET_FOLDER_PARENT,
																	CAN_GET_CHILDREN, CAN_CREATE_FOLDER, CAN_GET_ACL, CAN_APPLY_ACL, CAN_GET_FOLDER_TREE, CAN_GET_OBJECT_PARENTS};

		Action[] principalTaxoExpectedActionsOfUserWithWriteAccess = new Action[]{CAN_GET_PROPERTIES, CAN_GET_FOLDER_PARENT,
																				  CAN_GET_CHILDREN, CAN_CREATE_FOLDER, CAN_GET_FOLDER_TREE, CAN_GET_OBJECT_PARENTS};

		Action[] principalTaxoExpectedActionsOfUserWithReadAccess = new Action[]{CAN_GET_PROPERTIES, CAN_GET_FOLDER_PARENT,
																				 CAN_GET_CHILDREN, CAN_GET_FOLDER_TREE, CAN_GET_OBJECT_PARENTS};

		//These actions are required to allow the user to navigate to its folders
		Action[] principalTaxoExpectedActionsOfUserWithNoAccess = new Action[]{CAN_GET_FOLDER_PARENT, CAN_GET_CHILDREN,
																			   CAN_GET_FOLDER_TREE};

		session = newCMISSessionAsUserInZeCollection(admin);
		assertThatAllowableActionsOf("/taxo_taxo1/zetaxo1_fond1").containsOnly(secondaryTaxoExpectedActionsOfUserOrAdmin);
		assertThatAllowableActionsOf("/taxo_taxo2/zetaxo2_unit1").containsOnly(principalTaxoExpectedActionsOfAdmin);
		assertThatAllowableActionsOf(records.taxo2_station2).containsOnly(principalTaxoExpectedActionsOfAdmin);
		assertThatAllowableActionsOf(records.taxo2_station2_1).containsOnly(principalTaxoExpectedActionsOfAdmin);

		//Alice has read access on all the collection
		session = newCMISSessionAsUserInZeCollection(aliceWonderland);
		assertThatAllowableActionsOf("/taxo_taxo1/zetaxo1_fond1").containsOnly(secondaryTaxoExpectedActionsOfUserOrAdmin);
		assertThatAllowableActionsOf("/taxo_taxo2/zetaxo2_unit1").containsOnly(principalTaxoExpectedActionsOfUserWithReadAccess);
		assertThatAllowableActionsOf("/taxo_taxo2/zetaxo2_unit1").containsOnly(principalTaxoExpectedActionsOfUserWithReadAccess);
		assertThatAllowableActionsOf(records.taxo2_station2).containsOnly(principalTaxoExpectedActionsOfUserWithReadAccess);
		assertThatAllowableActionsOf(records.taxo2_station2_1).containsOnly(principalTaxoExpectedActionsOfUserWithReadAccess);

		//Dakota has read and write access on some administrative units
		session = newCMISSessionAsUserInZeCollection(dakota);
		assertThatAllowableActionsOf("/taxo_taxo1/zetaxo1_fond1").containsOnly(secondaryTaxoExpectedActionsOfUserOrAdmin);
		assertThatAllowableActionsOf("/taxo_taxo2/zetaxo2_unit1").containsOnly(principalTaxoExpectedActionsOfUserWithWriteAccess);
		assertThatAllowableActionsOf("/taxo_taxo2/zetaxo2_unit1").containsOnly(principalTaxoExpectedActionsOfUserWithWriteAccess);
		assertThatAllowableActionsOf(records.taxo2_station2).containsOnly(principalTaxoExpectedActionsOfUserWithWriteAccess);
		assertThatAllowableActionsOf(records.taxo2_station2_1).containsOnly(principalTaxoExpectedActionsOfUserWithWriteAccess);

		//Bob has no access
		session = newCMISSessionAsUserInZeCollection(bobGratton);
		assertThatAllowableActionsOf("/taxo_taxo1/zetaxo1_fond1").containsOnly(secondaryTaxoExpectedActionsOfUserOrAdmin);
		assertThatAllowableActionsOf("/taxo_taxo2/zetaxo2_unit1").isEmpty();
		assertThatAllowableActionsOf("/taxo_taxo2/zetaxo2_unit1").isEmpty();
		assertThatAllowableActionsOf(records.taxo2_station2).isEmpty();
		assertThatAllowableActionsOf(records.taxo2_station2_1).isEmpty();
	}

	@Test
	public void givenTaxo1IsPrincipalWhenGetAllowableActionsOfTaxonomyConceptThenOK()
			throws Exception {
		givenTaxonomy1IsPrincipalWithAuthOnAConcept();
		Action[] secondaryTaxoExpectedActionsOfUserOrAdmin = new Action[]{CAN_GET_PROPERTIES, CAN_GET_FOLDER_PARENT,
																		  CAN_CREATE_FOLDER, CAN_GET_CHILDREN, CAN_GET_FOLDER_TREE, CAN_GET_OBJECT_PARENTS};

		Action[] principalTaxoExpectedActionsOfAdmin = new Action[]{CAN_GET_PROPERTIES, CAN_GET_FOLDER_PARENT,
																	CAN_GET_CHILDREN, CAN_CREATE_FOLDER, CAN_GET_ACL, CAN_APPLY_ACL, CAN_GET_FOLDER_TREE, CAN_GET_OBJECT_PARENTS};

		Action[] principalTaxoExpectedActionsOfUserWithWriteAccess = new Action[]{CAN_GET_PROPERTIES, CAN_GET_FOLDER_PARENT,
																				  CAN_GET_CHILDREN, CAN_CREATE_FOLDER, CAN_GET_FOLDER_TREE, CAN_GET_OBJECT_PARENTS};

		Action[] principalTaxoExpectedActionsOfUserWithReadAccess = new Action[]{CAN_GET_PROPERTIES, CAN_GET_FOLDER_PARENT,
																				 CAN_GET_CHILDREN, CAN_GET_FOLDER_TREE, CAN_GET_OBJECT_PARENTS};

		//These actions are required to allow the user to navigate to its folders
		Action[] principalTaxoExpectedActionsOfUserWithNoAccess = new Action[]{CAN_GET_FOLDER_PARENT, CAN_GET_CHILDREN,
																			   CAN_GET_FOLDER_TREE};

		session = newCMISSessionAsUserInZeCollection(admin);
		assertThatAllowableActionsOf("/taxo_taxo1/zetaxo1_fond1").containsOnly(principalTaxoExpectedActionsOfAdmin);
		assertThatAllowableActionsOf("/taxo_taxo2/zetaxo2_unit1").containsOnly(secondaryTaxoExpectedActionsOfUserOrAdmin);

		assertThatAllowableActionsOf(records.taxo2_station1).containsOnly(secondaryTaxoExpectedActionsOfUserOrAdmin);
		assertThatAllowableActionsOf(records.taxo2_station2).containsOnly(secondaryTaxoExpectedActionsOfUserOrAdmin);
		assertThatAllowableActionsOf(records.taxo2_station2_1).containsOnly(secondaryTaxoExpectedActionsOfUserOrAdmin);

	}

	@Test
	public void whenGetActionsOfSecurizedRecordWithoutContentThenOK()
			throws Exception {
		givenTaxonomy2IsPrincipalWithAuthOnAConcept();
		Action[] expectedActionsOfUserWithReadAccess = new Action[]{CAN_GET_CHILDREN, CAN_GET_FOLDER_PARENT,
																	CAN_GET_FOLDER_TREE,
																	CAN_GET_PROPERTIES};

		Action[] expectedActionsOfUserWithReadWriteAccess = new Action[]{CAN_GET_CHILDREN, CAN_GET_FOLDER_PARENT,
																		 CAN_GET_FOLDER_TREE, CAN_GET_PROPERTIES, CAN_UPDATE_PROPERTIES, CAN_MOVE_OBJECT, CAN_CREATE_FOLDER};

		Action[] expectedActionsOfUserWithReadWriteDeleteAccess = new Action[]{CAN_GET_CHILDREN, CAN_GET_FOLDER_PARENT,
																			   CAN_GET_FOLDER_TREE, CAN_GET_PROPERTIES, CAN_UPDATE_PROPERTIES, CAN_MOVE_OBJECT, CAN_DELETE_OBJECT,
																			   CAN_DELETE_TREE, CAN_CREATE_FOLDER};

		Action[] expectedActionsOfAdmin = new Action[]{CAN_GET_CHILDREN, CAN_GET_FOLDER_PARENT, CAN_GET_FOLDER_TREE,
													   CAN_GET_PROPERTIES, CAN_UPDATE_PROPERTIES, CAN_MOVE_OBJECT, CAN_DELETE_OBJECT, CAN_APPLY_ACL, CAN_GET_ACL,
													   CAN_CREATE_FOLDER, CAN_DELETE_TREE};

		//These actions are required to allow the user to navigate to its folders
		Action[] expectedActionsOfUserWithNoAccess = new Action[]{CAN_GET_CHILDREN, CAN_GET_FOLDER_PARENT,
																  CAN_GET_FOLDER_TREE};

		String folder1UrlFromTaxo1 = "/taxo_taxo1/zetaxo1_fond1/zetaxo1_fond1_1/zetaxo1_category1/folder1";
		String folder1UrlFromTaxo2 = "/taxo_taxo2/zetaxo2_unit1/zetaxo2_station2/folder1";

		session = newCMISSessionAsUserInZeCollection(admin);
		assertThatAllowableActionsOf(folder1UrlFromTaxo1).containsOnly(expectedActionsOfAdmin);
		assertThatAllowableActionsOf(folder1UrlFromTaxo2).containsOnly(expectedActionsOfAdmin);

		session = newCMISSessionAsUserInZeCollection(chuckNorris);
		assertThatAllowableActionsOf(folder1UrlFromTaxo1).containsOnly(expectedActionsOfUserWithReadWriteDeleteAccess);
		assertThatAllowableActionsOf(folder1UrlFromTaxo2).containsOnly(expectedActionsOfUserWithReadWriteDeleteAccess);

		session = newCMISSessionAsUserInZeCollection(gandalf);
		assertThatAllowableActionsOf(folder1UrlFromTaxo1).containsOnly(expectedActionsOfUserWithReadWriteAccess);
		assertThatAllowableActionsOf(folder1UrlFromTaxo2).containsOnly(expectedActionsOfUserWithReadWriteAccess);

		//Alice has read access on all the collection
		session = newCMISSessionAsUserInZeCollection(aliceWonderland);
		assertThatAllowableActionsOf(folder1UrlFromTaxo1).containsOnly(expectedActionsOfUserWithReadAccess);
		assertThatAllowableActionsOf(folder1UrlFromTaxo2).containsOnly(expectedActionsOfUserWithReadAccess);

		//Dakota has read and write access on some administrative units
		session = newCMISSessionAsUserInZeCollection(dakota);
		assertThatAllowableActionsOf(folder1UrlFromTaxo1).containsOnly(expectedActionsOfUserWithReadWriteAccess);
		assertThatAllowableActionsOf(folder1UrlFromTaxo2).containsOnly(expectedActionsOfUserWithReadWriteAccess);

		//Bob has no access
		session = newCMISSessionAsUserInZeCollection(bobGratton);
		assertThatAllowableActionsOf(folder1UrlFromTaxo1).isEmpty();
		assertThatAllowableActionsOf(folder1UrlFromTaxo2).isEmpty();
	}

	@Test
	public void whenGetActionsOfSecurizedRecordWithContentThenOK()
			throws Exception {
		givenTaxonomy2IsPrincipalWithAuthOnAConcept();
		Action[] expectedActionsOfUserWithReadAccess = new Action[]{CAN_GET_CHILDREN, CAN_GET_FOLDER_PARENT,
																	CAN_GET_FOLDER_TREE, CAN_GET_PROPERTIES, CAN_GET_CONTENT_STREAM, CAN_GET_ALL_VERSIONS};

		Action[] expectedActionsOfUserWithReadWriteAccess = new Action[]{CAN_GET_CHILDREN, CAN_GET_FOLDER_PARENT,
																		 CAN_GET_FOLDER_TREE, CAN_GET_PROPERTIES, CAN_UPDATE_PROPERTIES, CAN_MOVE_OBJECT, CAN_GET_CONTENT_STREAM,
																		 CAN_GET_ALL_VERSIONS, CAN_CREATE_DOCUMENT, CAN_SET_CONTENT_STREAM, CAN_DELETE_CONTENT_STREAM, CAN_CHECK_IN,
																		 CAN_CHECK_OUT, CAN_CREATE_FOLDER};

		Action[] expectedActionsOfUserWithReadWriteDeleteAccess = new Action[]{CAN_GET_CHILDREN, CAN_GET_FOLDER_PARENT,
																			   CAN_GET_FOLDER_TREE, CAN_GET_PROPERTIES, CAN_UPDATE_PROPERTIES, CAN_MOVE_OBJECT, CAN_DELETE_OBJECT,
																			   CAN_GET_CONTENT_STREAM, CAN_GET_ALL_VERSIONS, CAN_CREATE_DOCUMENT, CAN_SET_CONTENT_STREAM,
																			   CAN_DELETE_CONTENT_STREAM, CAN_CHECK_IN, CAN_CHECK_OUT, CAN_DELETE_TREE, CAN_CREATE_FOLDER};

		Action[] expectedActionsOfAdmin = new Action[]{CAN_GET_CHILDREN, CAN_GET_FOLDER_PARENT, CAN_GET_FOLDER_TREE,
													   CAN_GET_PROPERTIES, CAN_UPDATE_PROPERTIES, CAN_MOVE_OBJECT, CAN_DELETE_OBJECT, CAN_APPLY_ACL, CAN_GET_ACL,
													   CAN_GET_CONTENT_STREAM, CAN_GET_ALL_VERSIONS, CAN_CREATE_DOCUMENT, CAN_SET_CONTENT_STREAM,
													   CAN_DELETE_CONTENT_STREAM, CAN_CHECK_IN, CAN_CHECK_OUT, CAN_CREATE_FOLDER, CAN_DELETE_TREE};

		//These actions are required to allow the user to navigate to its folders
		Action[] expectedActionsOfUserWithNoAccess = new Action[]{CAN_GET_CHILDREN, CAN_GET_FOLDER_PARENT,
																  CAN_GET_FOLDER_TREE};

		String folder1DocUrlFromTaxo1 = "/taxo_taxo1/zetaxo1_fond1/zetaxo1_fond1_1/zetaxo1_category1/folder1/folder1_doc1";
		String folder1DocUrlFromTaxo2 = "/taxo_taxo2/zetaxo2_unit1/zetaxo2_station2/folder1/folder1_doc1";

		session = newCMISSessionAsUserInZeCollection(admin);
		assertThatAllowableActionsOf(folder1DocUrlFromTaxo1).containsOnly(expectedActionsOfAdmin);
		assertThatAllowableActionsOf(folder1DocUrlFromTaxo2).containsOnly(expectedActionsOfAdmin);

		session = newCMISSessionAsUserInZeCollection(chuckNorris);
		assertThatAllowableActionsOf(folder1DocUrlFromTaxo1).containsOnly(expectedActionsOfUserWithReadWriteDeleteAccess);
		assertThatAllowableActionsOf(folder1DocUrlFromTaxo2).containsOnly(expectedActionsOfUserWithReadWriteDeleteAccess);

		session = newCMISSessionAsUserInZeCollection(gandalf);
		assertThatAllowableActionsOf(folder1DocUrlFromTaxo1).containsOnly(expectedActionsOfUserWithReadWriteAccess);
		assertThatAllowableActionsOf(folder1DocUrlFromTaxo2).containsOnly(expectedActionsOfUserWithReadWriteAccess);

		//Alice has read access on all the collection
		session = newCMISSessionAsUserInZeCollection(aliceWonderland);
		assertThatAllowableActionsOf(folder1DocUrlFromTaxo1).containsOnly(expectedActionsOfUserWithReadAccess);
		assertThatAllowableActionsOf(folder1DocUrlFromTaxo2).containsOnly(expectedActionsOfUserWithReadAccess);

		//Dakota has read and write access on some administrative units
		session = newCMISSessionAsUserInZeCollection(dakota);
		assertThatAllowableActionsOf(folder1DocUrlFromTaxo1).containsOnly(expectedActionsOfUserWithReadWriteAccess);
		assertThatAllowableActionsOf(folder1DocUrlFromTaxo2).containsOnly(expectedActionsOfUserWithReadWriteAccess);

		//Bob has no access
		session = newCMISSessionAsUserInZeCollection(bobGratton);
		assertThatAllowableActionsOf(folder1DocUrlFromTaxo1).isEmpty();
		assertThatAllowableActionsOf(folder1DocUrlFromTaxo2).isEmpty();
	}

	@Test
	public void givenACLDisabledThenNoAllowableActions()
			throws Exception {

		givenTaxonomy2IsPrincipalWithAuthOnAConcept();

		String folder1UrlFromTaxo2 = "/taxo_taxo2/zetaxo2_unit1/zetaxo2_station2/folder1";
		String folder1DocUrlFromTaxo1 = "/taxo_taxo1/zetaxo1_fond1/zetaxo1_fond1_1/zetaxo1_category1/folder1/folder1_doc1";
		String principalTaxonomyConcept = "/taxo_taxo2/zetaxo2_unit1";

		session = newCMISSessionAsUserInZeCollection(admin);
		assertThatAllowableActionsOf(folder1UrlFromTaxo2).contains(CAN_GET_ACL, CAN_APPLY_ACL);
		assertThatAllowableActionsOf(folder1DocUrlFromTaxo1).contains(CAN_GET_ACL, CAN_APPLY_ACL);
		assertThatAllowableActionsOf(principalTaxonomyConcept).contains(CAN_GET_ACL, CAN_APPLY_ACL);

		givenConfig(ConstellioEIMConfigs.CMIS_NEVER_RETURN_ACL, true);

		assertThatAllowableActionsOf(folder1UrlFromTaxo2).doesNotContain(CAN_GET_ACL, CAN_APPLY_ACL);
		assertThatAllowableActionsOf(folder1DocUrlFromTaxo1).doesNotContain(CAN_GET_ACL, CAN_APPLY_ACL);
		assertThatAllowableActionsOf(principalTaxonomyConcept).doesNotContain(CAN_GET_ACL, CAN_APPLY_ACL);

	}

	private void printTaxonomies(User user) {
		StringBuilder stringBuilder = new StringBuilder();
		for (Taxonomy taxonomy : taxonomiesManager.getEnabledTaxonomies(zeCollection)) {
			stringBuilder.append(taxonomy.getCode() + " : \n");
			for (Record record : new RecordHierarchyServices(getModelLayerFactory())
					.getRootConcept(zeCollection, taxonomy.getCode(), new TaxonomiesSearchOptions().setRows(100))) {

				printConcept(user, taxonomy.getCode(), record, 1, stringBuilder);
			}
			stringBuilder.append("\n\n");
		}
		System.out.println(stringBuilder.toString());
	}

	private void printConcept(User user, String taxonomy, Record record, int level, StringBuilder stringBuilder) {
		TaxonomiesSearchServices taxonomiesSearchServices = getModelLayerFactory().newTaxonomiesSearchService();
		for (int i = 0; i < level; i++) {
			stringBuilder.append("\t");
		}
		stringBuilder.append(record.getId() + "\n");
		for (TaxonomySearchRecord child : taxonomiesSearchServices
				.getVisibleChildConcept(user, taxonomy, record, new TaxonomiesSearchOptions().setRows(100))) {

			printConcept(user, taxonomy, child.getRecord(), level + 1, stringBuilder);
		}

	}

	private IterableAssert<Action> assertThatAllowableActionsOf(Record record) {
		return assertThatAllowableActionsOf(record.getId());
	}

	private IterableAssert<Action> assertThatAllowableActionsOf(String idOrPath) {
		try {
			if (idOrPath.startsWith("/")) {
				return assertThat(session.getObjectByPath(idOrPath).getAllowableActions().getAllowableActions());
			} else {
				return assertThat(session.getObject(idOrPath).getAllowableActions().getAllowableActions());
			}
		} catch (CmisRuntimeException e) {
			return assertThat(new HashSet<Action>());
		}
	}

	private Folder cmisFolder(Record record) {
		return (Folder) session.getObject(record.getId());
	}

	private void givenTaxonomy2IsPrincipalWithAuthOnAConcept() {
		User dakota = users.dakotaIn(zeCollection);
		User admin = users.adminIn(zeCollection);
		taxonomiesManager.setPrincipalTaxonomy(zeCollectionSchemas.getTaxonomy2(), metadataSchemasManager);

		ReindexingServices reindexingServices = getModelLayerFactory().newReindexingServices();
		reindexingServices.reindexCollection(zeCollection, ReindexationMode.RECALCULATE_AND_REWRITE);

		authorizationsServices.add(authorizationForUsers(dakota).on(records.taxo2_unit1)
				.givingReadWriteAccess().setExecutedBy(admin));
		try {
			waitForBatchProcess();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private void givenTaxonomy1IsPrincipalWithAuthOnAConcept() {
		User dakota = users.dakotaIn(zeCollection);
		User admin = users.adminIn(zeCollection);
		taxonomiesManager.setPrincipalTaxonomy(zeCollectionSchemas.getTaxonomy1(), metadataSchemasManager);
		authorizationsServices.add(authorizationForUsers(dakota).on(records.taxo1_category2).givingReadWriteAccess(), admin);
		try {
			waitForBatchProcess();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

}
