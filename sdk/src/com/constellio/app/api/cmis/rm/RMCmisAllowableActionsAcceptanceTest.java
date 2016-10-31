package com.constellio.app.api.cmis.accept;

import static com.constellio.model.entities.security.Role.DELETE;
import static com.constellio.model.entities.security.Role.READ;
import static com.constellio.model.entities.security.Role.WRITE;
import static com.constellio.model.entities.security.global.AuthorizationBuilder.authorizationForUsers;
import static com.constellio.sdk.tests.TestUtils.asSet;
import static java.util.Arrays.asList;
import static org.apache.chemistry.opencmis.commons.enums.Action.CAN_GET_CHILDREN;
import static org.apache.chemistry.opencmis.commons.enums.Action.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.assertj.core.api.IterableAssert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.accept.CmisAcceptanceTestSetup.Records;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
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
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.setups.Users;

@DriverTest
public class RMCmisAllowableActionsAcceptanceTest extends ConstellioTest {

	AuthorizationsServices authorizationsServices;
	RecordServices recordServices;
	Users users = new Users();
	Session session;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users)
				.withFoldersAndContainersOfEveryStatus()
				.withDocumentsHavingContent());

		recordServices = getModelLayerFactory().newRecordServices();
		authorizationsServices = getModelLayerFactory().newAuthorizationsServices();

		users.setUp(getModelLayerFactory().newUserServices());

		recordServices.update(users.adminIn(zeCollection).setCollectionAllAccess(true));
		recordServices.update(users.aliceIn(zeCollection).setCollectionReadAccess(true));
		recordServices.update(users.chuckNorrisIn(zeCollection).setCollectionAllAccess(true));
		recordServices.update(users.gandalfIn(zeCollection).setCollectionReadAccess(true).setCollectionWriteAccess(true));

		givenConfig(ConstellioEIMConfigs.CMIS_NEVER_RETURN_ACL, false);
	}

	@Test
	public void whenGetAllowableActionsOfRootThenOK()
			throws Exception {

		session = newCMISSessionAsUserInZeCollection(admin);
		assertThatAllowableActionsOf("/").containsOnly(CAN_GET_PROPERTIES, CAN_GET_CHILDREN);

		session = newCMISSessionAsUserInZeCollection(aliceWonderland);
		assertThatAllowableActionsOf("/").containsOnly(CAN_GET_PROPERTIES, CAN_GET_CHILDREN);

		session = newCMISSessionAsUserInZeCollection(bobGratton);
		assertThatAllowableActionsOf("/").containsOnly(CAN_GET_PROPERTIES, CAN_GET_CHILDREN);

		while (true) {
			Thread.sleep(1000);
		}
	}

	@Test
	public void whenGetAllowableActionsOfTaxonomyThenOK()
			throws Exception {

		session = newCMISSessionAsUserInZeCollection(admin);
		assertThatAllowableActionsOf("/taxo_plan/a").containsOnly(CAN_GET_PROPERTIES, CAN_GET_CHILDREN);
		assertThatAllowableActionsOf("/taxo_unit/").containsOnly(CAN_GET_PROPERTIES, CAN_GET_CHILDREN);

		session = newCMISSessionAsUserInZeCollection(aliceWonderland);
		assertThatAllowableActionsOf("/taxo_taxo1").containsOnly(CAN_GET_PROPERTIES, CAN_GET_CHILDREN);
		assertThatAllowableActionsOf("/taxo_taxo2").containsOnly(CAN_GET_PROPERTIES, CAN_GET_CHILDREN);

		session = newCMISSessionAsUserInZeCollection(bobGratton);
		assertThatAllowableActionsOf("/taxo_taxo1").containsOnly(CAN_GET_PROPERTIES, CAN_GET_CHILDREN);
		assertThatAllowableActionsOf("/taxo_taxo2").containsOnly(CAN_GET_PROPERTIES, CAN_GET_CHILDREN);
	}

	//@Test
	public void whenGetAllowableActionsOfSecondaryTaxonomyThenOK()
			throws Exception {

		Action[] secondaryTaxoExpectedActionsOfUserOrAdmin = new Action[] { CAN_GET_PROPERTIES, CAN_GET_FOLDER_PARENT,
				CAN_CREATE_FOLDER, CAN_GET_CHILDREN, CAN_GET_FOLDER_TREE, CAN_GET_OBJECT_PARENTS };

		Action[] principalTaxoExpectedActionsOfAdmin = new Action[] { CAN_GET_PROPERTIES, CAN_GET_FOLDER_PARENT,
				CAN_GET_CHILDREN, CAN_CREATE_FOLDER, CAN_GET_ACL, CAN_APPLY_ACL, CAN_GET_FOLDER_TREE, CAN_GET_OBJECT_PARENTS };

		Action[] principalTaxoExpectedActionsOfUserWithWriteAccess = new Action[] { CAN_GET_PROPERTIES, CAN_GET_FOLDER_PARENT,
				CAN_GET_CHILDREN, CAN_CREATE_FOLDER, CAN_GET_FOLDER_TREE, CAN_GET_OBJECT_PARENTS };

		Action[] principalTaxoExpectedActionsOfUserWithReadAccess = new Action[] { CAN_GET_PROPERTIES, CAN_GET_FOLDER_PARENT,
				CAN_GET_CHILDREN, CAN_GET_FOLDER_TREE, CAN_GET_OBJECT_PARENTS };

		//These actions are required to allow the user to navigate to its folders
		Action[] principalTaxoExpectedActionsOfUserWithNoAccess = new Action[] { CAN_GET_FOLDER_PARENT, CAN_GET_CHILDREN,
				CAN_GET_FOLDER_TREE };

		session = newCMISSessionAsUserInZeCollection(admin);
		assertThatAllowableActionsOf("/taxo_taxo1/zetaxo1_fond1").containsOnly(secondaryTaxoExpectedActionsOfUserOrAdmin);
		assertThatAllowableActionsOf("/taxo_taxo2/zetaxo2_unit1").containsOnly(principalTaxoExpectedActionsOfAdmin);

		//Alice has read access on all the collection
		session = newCMISSessionAsUserInZeCollection(aliceWonderland);
		assertThatAllowableActionsOf("/taxo_taxo1/zetaxo1_fond1").containsOnly(secondaryTaxoExpectedActionsOfUserOrAdmin);
		assertThatAllowableActionsOf("/taxo_taxo2/zetaxo2_unit1").containsOnly(principalTaxoExpectedActionsOfUserWithReadAccess);

		//Dakota has read and write access on some administrative units
		session = newCMISSessionAsUserInZeCollection(dakota);
		assertThatAllowableActionsOf("/taxo_taxo1/zetaxo1_fond1").containsOnly(secondaryTaxoExpectedActionsOfUserOrAdmin);
		assertThatAllowableActionsOf("/taxo_taxo2/zetaxo2_unit1").containsOnly(principalTaxoExpectedActionsOfUserWithWriteAccess);

		//Bob has no access
		session = newCMISSessionAsUserInZeCollection(bobGratton);
		assertThatAllowableActionsOf("/taxo_taxo1/zetaxo1_fond1").containsOnly(secondaryTaxoExpectedActionsOfUserOrAdmin);
		assertThatAllowableActionsOf("/taxo_taxo2/zetaxo2_unit1").containsOnly(principalTaxoExpectedActionsOfUserWithNoAccess);
	}

	//@Test
	public void whenGetActionsOfSecurizedRecordWithoutContentThenOK()
			throws Exception {

		Action[] expectedActionsOfUserWithReadAccess = new Action[] { CAN_GET_CHILDREN, CAN_GET_FOLDER_PARENT,
				CAN_GET_FOLDER_TREE,
				CAN_GET_PROPERTIES };

		Action[] expectedActionsOfUserWithReadWriteAccess = new Action[] { CAN_GET_CHILDREN, CAN_GET_FOLDER_PARENT,
				CAN_GET_FOLDER_TREE, CAN_GET_PROPERTIES, CAN_UPDATE_PROPERTIES, CAN_MOVE_OBJECT };

		Action[] expectedActionsOfUserWithReadWriteDeleteAccess = new Action[] { CAN_GET_CHILDREN, CAN_GET_FOLDER_PARENT,
				CAN_GET_FOLDER_TREE, CAN_GET_PROPERTIES, CAN_UPDATE_PROPERTIES, CAN_MOVE_OBJECT, CAN_DELETE_OBJECT };

		Action[] expectedActionsOfAdmin = new Action[] { CAN_GET_CHILDREN, CAN_GET_FOLDER_PARENT, CAN_GET_FOLDER_TREE,
				CAN_GET_PROPERTIES, CAN_UPDATE_PROPERTIES, CAN_MOVE_OBJECT, CAN_DELETE_OBJECT, CAN_APPLY_ACL, CAN_GET_ACL };

		//These actions are required to allow the user to navigate to its folders
		Action[] expectedActionsOfUserWithNoAccess = new Action[] { CAN_GET_CHILDREN, CAN_GET_FOLDER_PARENT,
				CAN_GET_FOLDER_TREE };

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
		assertThatAllowableActionsOf(folder1UrlFromTaxo1).containsOnly(expectedActionsOfUserWithNoAccess);
		assertThatAllowableActionsOf(folder1UrlFromTaxo2).containsOnly(expectedActionsOfUserWithNoAccess);
	}

	//@Test
	public void whenGetActionsOfSecurizedRecordWithContentThenOK()
			throws Exception {

		Action[] expectedActionsOfUserWithReadAccess = new Action[] { CAN_GET_CHILDREN, CAN_GET_FOLDER_PARENT,
				CAN_GET_FOLDER_TREE, CAN_GET_PROPERTIES, CAN_GET_CONTENT_STREAM, CAN_GET_ALL_VERSIONS };

		Action[] expectedActionsOfUserWithReadWriteAccess = new Action[] { CAN_GET_CHILDREN, CAN_GET_FOLDER_PARENT,
				CAN_GET_FOLDER_TREE, CAN_GET_PROPERTIES, CAN_UPDATE_PROPERTIES, CAN_MOVE_OBJECT, CAN_GET_CONTENT_STREAM,
				CAN_GET_ALL_VERSIONS, CAN_CREATE_DOCUMENT, CAN_SET_CONTENT_STREAM, CAN_DELETE_CONTENT_STREAM, CAN_CHECK_IN,
				CAN_CHECK_OUT };

		Action[] expectedActionsOfUserWithReadWriteDeleteAccess = new Action[] { CAN_GET_CHILDREN, CAN_GET_FOLDER_PARENT,
				CAN_GET_FOLDER_TREE, CAN_GET_PROPERTIES, CAN_UPDATE_PROPERTIES, CAN_MOVE_OBJECT, CAN_DELETE_OBJECT,
				CAN_GET_CONTENT_STREAM, CAN_GET_ALL_VERSIONS, CAN_CREATE_DOCUMENT, CAN_SET_CONTENT_STREAM,
				CAN_DELETE_CONTENT_STREAM, CAN_CHECK_IN, CAN_CHECK_OUT };

		Action[] expectedActionsOfAdmin = new Action[] { CAN_GET_CHILDREN, CAN_GET_FOLDER_PARENT, CAN_GET_FOLDER_TREE,
				CAN_GET_PROPERTIES, CAN_UPDATE_PROPERTIES, CAN_MOVE_OBJECT, CAN_DELETE_OBJECT, CAN_APPLY_ACL, CAN_GET_ACL,
				CAN_GET_CONTENT_STREAM, CAN_GET_ALL_VERSIONS, CAN_CREATE_DOCUMENT, CAN_SET_CONTENT_STREAM,
				CAN_DELETE_CONTENT_STREAM, CAN_CHECK_IN, CAN_CHECK_OUT };

		//These actions are required to allow the user to navigate to its folders
		Action[] expectedActionsOfUserWithNoAccess = new Action[] { CAN_GET_CHILDREN, CAN_GET_FOLDER_PARENT,
				CAN_GET_FOLDER_TREE };

		String folder1UrlFromTaxo1 = "/taxo_taxo1/zetaxo1_fond1/zetaxo1_fond1_1/zetaxo1_category1/folder1/folder1_doc1";
		String folder1UrlFromTaxo2 = "/taxo_taxo2/zetaxo2_unit1/zetaxo2_station2/folder1/folder1_doc1";

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
		assertThatAllowableActionsOf(folder1UrlFromTaxo1).containsOnly(expectedActionsOfUserWithNoAccess);
		assertThatAllowableActionsOf(folder1UrlFromTaxo2).containsOnly(expectedActionsOfUserWithNoAccess);
	}

	private void printTaxonomies(User user) {
		TaxonomiesSearchServices taxonomiesSearchServices = getModelLayerFactory().newTaxonomiesSearchService();
		TaxonomiesManager taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
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

	private IterableAssert<Action> assertThatAllowableActionsOf(String path) {
		return assertThat(session.getObjectByPath(path).getAllowableActions().getAllowableActions());
	}

	private Folder cmisFolder(Record record) {
		return (Folder) session.getObject(record.getId());
	}

}
