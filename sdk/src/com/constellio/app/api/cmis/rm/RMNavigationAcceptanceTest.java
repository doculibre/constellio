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

import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class RMNavigationAcceptanceTest extends ConstellioTest {

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
	public void whenNavigatingInTaxonomiesThenOK()
			throws Exception {

		for (String user : asList(admin, aliceWonderland, chuckNorris, gandalf)) {
			session = newCMISSessionAsUserInZeCollection(admin);
			Folder folder = session.getRootFolder();

			assertThatChildrensOF(folder).describedAs("").containsOnly(
					tuple("todo")
			);
		}
	}

	private ListAssert<Tuple> assertThatChildrensOF(Folder folder) {
		List<Tuple> tuples = new ArrayList<>();
		for (Iterator<CmisObject> objectIterator = folder.getChildren().iterator(); objectIterator.hasNext(); ) {
			CmisObject child = objectIterator.next();
			Tuple tuple = new Tuple();
			tuples.add(tuple);

			tuple.addData(child.getId());
			tuple.addData(child.getType().getId());
			tuple.addData(child.getName());
			//tuple.addData(child.getProperty("description"));
		}
		return assertThat(tuples);
	}
}
