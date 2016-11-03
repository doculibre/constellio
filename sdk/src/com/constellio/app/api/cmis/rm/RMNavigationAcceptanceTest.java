package com.constellio.app.api.cmis.rm;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.assertj.core.api.ListAssert;
import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.setups.Users;

@InDevelopmentTest
public class RMNavigationAcceptanceTest extends ConstellioTest {

	AuthorizationsServices authorizationsServices;
	RecordServices recordServices;
	Users users = new Users();
	Session session;

	RMTestRecords records = new RMTestRecords(zeCollection);

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users)
				.withRMTest(records).withFoldersAndContainersOfEveryStatus()
				.withDocumentsHavingContent());

		recordServices = getModelLayerFactory().newRecordServices();
		authorizationsServices = getModelLayerFactory().newAuthorizationsServices();

		users.setUp(getModelLayerFactory().newUserServices());

		recordServices.update(users.adminIn(zeCollection).setCollectionAllAccess(true));
		recordServices.update(users.aliceIn(zeCollection).setCollectionReadAccess(true));
		recordServices.update(users.chuckNorrisIn(zeCollection).setCollectionAllAccess(true));
		recordServices.update(users.gandalfIn(zeCollection).setCollectionReadAccess(true).setCollectionWriteAccess(true));

		givenConfig(ConstellioEIMConfigs.CMIS_NEVER_RETURN_ACL, false);

		//		newWebDriver();
		//		waitUntilICloseTheBrowsers();
	}

	@Test
	public void whenNavigatingInTaxonomiesThenOK()
			throws Exception {

		for (String user : asList(admin, aliceWonderland, chuckNorris, gandalf)) {
			session = newCMISSessionAsUserInZeCollection(user);
			Folder folder = session.getRootFolder();

			assertThatChildrensOf(folder).describedAs("seen by " + user).containsOnly(
					tuple("taxo_plan", "taxonomy", "Plan de classification"),
					tuple("taxo_containers", "taxonomy", "Emplacements"),
					tuple("taxo_admUnits", "taxonomy", "Unités administratives")
			);

			assertThatChildrensOf(folder("/taxo_plan")).describedAs("seen by " + user).containsOnly(
					tuple("categoryId_Z", "category_default", "Ze category"),
					tuple("categoryId_X", "category_default", "Xe category")
			);

			assertThatChildrensOf(folder("/taxo_plan/categoryId_X")).describedAs("seen by " + user).containsOnly(
					tuple("categoryId_X100", "category_default", "X100"),
					tuple("categoryId_X13", "category_default", "Agent Secreet")
			);

			assertThatChildrensOf(folder("/taxo_plan/categoryId_X/categoryId_X13")).describedAs("seen by " + user).isEmpty();

		}

		for (String user : asList(admin, aliceWonderland, chuckNorris)) {
			as(user).assertThatChildrensOf(folder("/taxo_plan/categoryId_X/categoryId_X100")).containsOnly(
					tuple("categoryId_X110", "category_default", "X110"),
					tuple("categoryId_X120", "category_default", "X120"),
					tuple("B06", "folder_default", "Framboise"),
					tuple("C06", "folder_default", "Chou-fleur"),
					tuple("A18", "folder_default", "Cheval"),
					tuple("A16", "folder_default", "Chat"),
					tuple("B32", "folder_default", "Pêche"),
					tuple("A17", "folder_default", "Chauve-souris"),
					tuple("C32", "folder_default", "Maïs")
			);
		}

		as(gandalf).assertThatChildrensOf(folder("/taxo_plan/categoryId_X/categoryId_X100")).containsOnly(
				tuple("categoryId_X110", "category_default", "X110"),
				tuple("categoryId_X120", "category_default", "X120"),
				tuple("B06", "folder_default", "Framboise"),
				tuple("C06", "folder_default", "Chou-fleur"),
				tuple("A18", "folder_default", "Cheval"),
				tuple("A16", "folder_default", "Chat"),
				tuple("B32", "folder_default", "Pêche"),
				tuple("A17", "folder_default", "Chauve-souris"),
				tuple("C32", "folder_default", "Maïs")
		);

		as(sasquatch).assertThatChildrensOf(folder("/taxo_plan/categoryId_X/categoryId_X100")).containsOnly(
				tuple("categoryId_X110", "category_default", "X110"),
				tuple("categoryId_X120", "category_default", "X120")
		);
	}

	private Folder folder(String path) {
		return (Folder) session.getObjectByPath(path);
	}

	private RMNavigationAcceptanceTest as(String user) {
		session = newCMISSessionAsUserInCollection(user, zeCollection);
		return this;
	}

	private ListAssert<Tuple> assertThatChildrensOf(Folder folder) {
		List<Tuple> tuples = new ArrayList<>();
		for (Iterator<CmisObject> objectIterator = folder.getChildren().iterator(); objectIterator.hasNext(); ) {
			CmisObject child = objectIterator.next();
			Tuple tuple = new Tuple();
			tuples.add(tuple);

			tuple.addData(child.getId());
			tuple.addData(child.getType().getId());
			tuple.addData(child.getName());
		}
		return assertThat(tuples);
	}
}
