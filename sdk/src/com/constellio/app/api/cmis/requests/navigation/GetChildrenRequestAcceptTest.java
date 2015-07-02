/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.api.cmis.requests.navigation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.Session;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.api.cmis.accept.CmisAcceptanceTestSetup;
import com.constellio.app.api.cmis.accept.CmisAcceptanceTestSetup.Records;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.AuthorizationDetails;
import com.constellio.model.entities.security.CustomizedAuthorizationsBehavior;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.DriverTest;
import com.constellio.sdk.tests.setups.Users;

@DriverTest
public class GetChildrenRequestAcceptTest extends ConstellioTest {

	UserServices userServices;
	TaxonomiesManager taxonomiesManager;
	MetadataSchemasManager metadataSchemasManager;
	RecordServices recordServices;
	Users users = new Users();
	CmisAcceptanceTestSetup zeCollectionSchemas = new CmisAcceptanceTestSetup(zeCollection);
	Records zeCollectionRecords;
	TaxonomiesSearchServices taxonomiesSearchServices;
	String ZE_ROLE = "zeRoleCode";
	Session cmisSession;

	AuthorizationsServices authorizationsServices;
	RolesManager roleManager;

	@Before
	public void setUp()
			throws Exception {

		userServices = getModelLayerFactory().newUserServices();
		taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		recordServices = getModelLayerFactory().newRecordServices();
		authorizationsServices = getModelLayerFactory().newAuthorizationsServices();
		roleManager = getModelLayerFactory().getRolesManager();

		taxonomiesSearchServices = getModelLayerFactory().newTaxonomiesSearchService();

		users.setUp(userServices);

		defineSchemasManager().using(zeCollectionSchemas);
		taxonomiesManager.addTaxonomy(zeCollectionSchemas.getTaxonomy1(), metadataSchemasManager);
		taxonomiesManager.setPrincipalTaxonomy(zeCollectionSchemas.getTaxonomy1(), metadataSchemasManager);
		zeCollectionRecords = zeCollectionSchemas.givenRecords(recordServices);

		userServices.addUserToCollection(users.chuckNorris(), zeCollection);
		userServices.addUserToCollection(users.bob(), zeCollection);
		cmisSession = givenAdminSessionOnZeCollection();
		recordServices.update(users.chuckNorrisIn(zeCollection).setCollectionReadAccess(true).getWrappedRecord());
	}

	@Test
	public void whenGettingChildrenOnFolderThenCorrectChildrenReturned()
			throws Exception {
		ItemIterable<CmisObject> children = getChildrenOfObject(zeCollectionRecords.taxo1_category2.getId());
		Map<String, CmisObject> childrenMap = getChildrenMap(children);
		thenCorrectChildrenReturned(childrenMap, "folder4", "taxo1_category2_1");
	}

	private Map<String, CmisObject> getChildrenMap(ItemIterable<CmisObject> children) {
		Map<String, CmisObject> childrenMap = new HashMap<>();
		for (CmisObject child : children) {
			childrenMap.put(child.getId(), child);
		}
		return childrenMap;
	}

	@Test
	public void whenGettingChildrenOnFolderThenCorrectChildrenOfOtherSchemaReturned()
			throws Exception {
		ItemIterable<CmisObject> children = getChildrenOfObject(zeCollectionRecords.folder2_2.getId());
		Map<String, CmisObject> childrenMap = getChildrenMap(children);
		thenCorrectChildrenReturned(childrenMap, "folder2_2_doc1", "folder2_2_doc2");
	}

	@Test
	public void whenGettingChildrenOnTaxoThenCorrectChildrenReturned()
			throws Exception {
		ItemIterable<CmisObject> children = getChildrenOfObject("taxo_" + zeCollectionSchemas.getTaxonomy1().getCode());
		Map<String, CmisObject> childrenMap = getChildrenMap(children);
		thenCorrectChildrenReturned(childrenMap, "taxo1_fond1");
	}

	@Test
	public void whenGettingChildrenOnRootThenCorrectChildrenReturned()
			throws Exception {
		ItemIterable<CmisObject> children = getChildrenOfObject("@root@");
		Map<String, CmisObject> childrenMap = getChildrenMap(children);
		thenCorrectChildrenReturned(childrenMap, "taxo_" + zeCollectionSchemas.getTaxonomy1().getCode());
	}

	@Test
	public void whenGettingChildrenThenOnlyAllowedChildrenReturned()
			throws Exception {
		cmisSession = givenBobSessionOnZeCollection();
		Authorization bobAuth = addAuthorizationWithoutDetaching(Arrays.asList(Role.READ),
				Arrays.asList(users.bobIn("zeCollection").getId()),
				Arrays.asList("folder2"));
		authorizationsServices.removeAuthorizationOnRecord(bobAuth, zeCollectionRecords.folder2_2,
				CustomizedAuthorizationsBehavior.KEEP_ATTACHED);
		waitForBatchProcess();

		ItemIterable<CmisObject> children = getChildrenOfObject("folder2");
		Map<String, CmisObject> childrenMap = getChildrenMap(children);
		thenCorrectChildrenReturned(childrenMap, "folder2_1");
		//thenWrongChildrenNotReturned(childrenMap, "folder2_2");
	}

	public void thenCorrectChildrenReturned(Map<String, CmisObject> childrenMap, String... childrenCodes) {
		for (String childCode : childrenCodes) {
			assertThat(childrenMap).containsKey(childCode);
		}
	}

	public void thenWrongChildrenNotReturned(Map<String, CmisObject> childrenMap, String... childrenCodes) {
		for (String childCode : childrenCodes) {
			assertThat(childrenMap).doesNotContainKey(childCode);
		}
	}

	private ItemIterable<CmisObject> getChildrenOfObject(String objectId) {
		CmisObject object;
		if ("@root@".equals(objectId)) {
			object = cmisSession.getRootFolder();
		} else {
			object = cmisSession.getObject(objectId);
		}
		return ((Folder) object).getChildren();
	}

	private Session givenAdminSessionOnZeCollection()
			throws RecordServicesException {
		getModelLayerFactory().newAuthenticationService().changePassword(chuckNorris, "1qaz2wsx");
		return newCmisSessionBuilder().authenticatedBy(chuckNorris, "1qaz2wsx").onCollection(zeCollection).build();
	}

	private Session givenBobSessionOnZeCollection()
			throws RecordServicesException {
		getModelLayerFactory().newAuthenticationService().changePassword(bobGratton, "1qaz2wsx");
		return newCmisSessionBuilder().authenticatedBy(bobGratton, "1qaz2wsx").onCollection(zeCollection).build();
	}

	private Authorization addAuthorizationWithoutDetaching(List<String> roles, List<String> grantedToPrincipals,
			List<String> grantedOnRecords) {
		AuthorizationDetails details = AuthorizationDetails.create(aString(), roles, zeCollection);

		Authorization authorization = new Authorization(details, grantedToPrincipals, grantedOnRecords);

		authorizationsServices.add(authorization, CustomizedAuthorizationsBehavior.KEEP_ATTACHED, null);
		return authorization;
	}

}
