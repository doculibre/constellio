package com.constellio.app.api.cmis.requests.navigation;

import static com.constellio.model.entities.security.CustomizedAuthorizationsBehavior.KEEP_ATTACHED;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.api.cmis.accept.CmisAcceptanceTestSetup;
import com.constellio.app.api.cmis.accept.CmisAcceptanceTestSetup.Records;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.AuthorizationDetails;
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
	Session cmisSession;

	AuthorizationsServices authorizationsServices;
	RolesManager roleManager;

	String bobKey = "bob-key";
	String chuckNorrisKey = "chuckNorris-key";
	String bobToken, chuckNorrisToken;

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
		CmisAcceptanceTestSetup.allSchemaTypesSupported(getAppLayerFactory());
		taxonomiesManager.addTaxonomy(zeCollectionSchemas.getTaxonomy1(), metadataSchemasManager);
		taxonomiesManager.addTaxonomy(zeCollectionSchemas.getTaxonomy2(), metadataSchemasManager);
		taxonomiesManager.setPrincipalTaxonomy(zeCollectionSchemas.getTaxonomy1(), metadataSchemasManager);
		zeCollectionRecords = zeCollectionSchemas.givenRecords(recordServices);

		userServices.addUpdateUserCredential(
				userServices.getUserCredential(chuckNorris).withServiceKey(chuckNorrisKey).withSystemAdminPermission());
		chuckNorrisToken = userServices.generateToken(chuckNorris);
		userServices.addUserToCollection(users.chuckNorris(), zeCollection);
		cmisSession = givenAdminSessionOnZeCollection();
		userServices.addUpdateUserCredential(
				userServices.getUserCredential(bobGratton).withServiceKey(bobKey).withSystemAdminPermission());
		bobToken = userServices.generateToken(bobGratton);
		userServices.addUserToCollection(users.bob(), zeCollection);

		recordServices.update(users.chuckNorrisIn(zeCollection).setCollectionReadAccess(true).getWrappedRecord());
		CmisAcceptanceTestSetup.giveUseCMISPermissionToUsers(getModelLayerFactory());
	}

	@Test
	public void whenGettingChildrenOnFolderThenCorrectChildrenReturned()
			throws Exception {
		ItemIterable<CmisObject> obtainedChildren = getChildrenOfObject(zeCollectionRecords.taxo1_category2.getId());
		validateThat(obtainedChildren).hasChildrenIds("folder4", "zetaxo1_category2_1");
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
		ItemIterable<CmisObject> obtainedChildren = getChildrenOfObject(zeCollectionRecords.folder2_2.getId());
		validateThat(obtainedChildren).hasChildrenIds("folder2_2_doc1", "folder2_2_doc2")
				.withPaths("/taxo_taxo1/zetaxo1_fond1/zetaxo1_fond1_1/zetaxo1_category1/folder2/folder2_2/folder2_2_doc2",
						"/taxo_taxo1/zetaxo1_fond1/zetaxo1_fond1_1/zetaxo1_category1/folder2/folder2_2/folder2_2_doc1");
	}

	@Test
	public void whenGettingChildrenOnPrincipalTaxoThenCorrectChildrenReturned()
			throws Exception {
		ItemIterable<CmisObject> obtainedChildren = getChildrenOfObject("taxo_" + zeCollectionSchemas.getTaxonomy1().getCode());
		validateThat(obtainedChildren).hasChildrenIds("zetaxo1_fond1").withPaths("/taxo_taxo1/zetaxo1_fond1");

		obtainedChildren = getChildrenOfObject(zeCollectionRecords.taxo1_fond1.getId());
		validateThat(obtainedChildren).hasChildrenIds("zetaxo1_fond1_1", "zetaxo1_category2")
				.withPaths("/taxo_taxo1/zetaxo1_fond1/zetaxo1_fond1_1", "/taxo_taxo1/zetaxo1_fond1/zetaxo1_category2");

		obtainedChildren = getChildrenOfObject(zeCollectionRecords.taxo1_category2.getId());
		validateThat(obtainedChildren).hasChildrenIds("zetaxo1_category2_1", "folder4")
				.withPaths("/taxo_taxo1/zetaxo1_fond1/zetaxo1_category2/folder4",
						"/taxo_taxo1/zetaxo1_fond1/zetaxo1_category2/zetaxo1_category2_1");

	}

	@Test
	public void whenGettingChildrenOnSecondaryTaxoThenCorrectChildrenReturned()
			throws Exception {
		ItemIterable<CmisObject> obtainedChildren = getChildrenOfObject("taxo_" + zeCollectionSchemas.getTaxonomy2().getCode());
		validateThat(obtainedChildren).hasChildrenIds("zetaxo2_unit1").withPaths("/taxo_taxo2/zetaxo2_unit1");

		obtainedChildren = getChildrenOfObject(zeCollectionRecords.taxo2_unit1.getId());
		validateThat(obtainedChildren).hasChildrenIds("zetaxo2_unit1_1", "zetaxo2_station2")
				.withPaths("/taxo_taxo2/zetaxo2_unit1/zetaxo2_unit1_1", "/taxo_taxo2/zetaxo2_unit1/zetaxo2_station2");

		obtainedChildren = getChildrenOfObject(zeCollectionRecords.taxo2_station2.getId());
		validateThat(obtainedChildren).hasChildrenIds("zetaxo2_station2_1", "folder1")
				.withPaths("/taxo_taxo2/zetaxo2_unit1/zetaxo2_station2/zetaxo2_station2_1",
						"/taxo_taxo1/zetaxo1_fond1/zetaxo1_fond1_1/zetaxo1_category1/folder1");
	}

	@Test
	public void whenGettingChildrenOnRootThenCorrectChildrenReturned()
			throws Exception {
		ItemIterable<CmisObject> obtainedChildren = getChildrenOfObject("@root@");
		validateThat(obtainedChildren).hasChildrenIds("taxo_taxo1", "taxo_taxo2")
				.withPaths("/taxo_taxo1", "/taxo_taxo2");
	}

	//Security is broken, so CMIS services are restricted
	//@Test
	public void whenGettingChildrenThenOnlyAllowedChildrenReturned()
			throws Exception {

		Record folder2_2 = zeCollectionRecords.folder2_2;
		recordServices.refresh(folder2_2);
		assertThat(users.bobIn(zeCollection).hasReadAccess().on(folder2_2)).isFalse();

		cmisSession = givenBobSessionOnZeCollection();
		Authorization bobAuth = addAuthorizationWithoutDetaching(asList(Role.READ),
				asList(users.bobIn(zeCollection).getId()),
				asList(zeCollectionRecords.folder2.getId()));

		waitForBatchProcess();
		recordServices.refresh(folder2_2);
		authorizationsServices.removeAuthorizationOnRecord(bobAuth, folder2_2, KEEP_ATTACHED);
		waitForBatchProcess();

		recordServices.refresh(folder2_2);
		assertThat(users.bobIn(zeCollection).hasReadAccess().on(folder2_2)).isFalse();
		ItemIterable<CmisObject> obtainedChildren = getChildrenOfObject("folder2");
		validateThat(obtainedChildren).hasChildrenIds("folder2_1");

	}

	private GetChildrenRequestAcceptTestValidator validateThat(ItemIterable<CmisObject> children) {
		return new GetChildrenRequestAcceptTestValidator(getChildrenMap(children));
	}

	private class GetChildrenRequestAcceptTestValidator {

		private Map<String, CmisObject> childrenMap;

		public GetChildrenRequestAcceptTestValidator(Map<String, CmisObject> childrenMap) {
			this.childrenMap = childrenMap;
		}

		public GetChildrenRequestAcceptTestValidator hasChildrenIds(String... childrenCodes) {
			assertThat(childrenMap.keySet()).containsOnly(childrenCodes);

			return this;
		}

		public GetChildrenRequestAcceptTestValidator withPaths(String... paths) {
			Set<String> wasPaths = new HashSet<>();
			for (CmisObject child : childrenMap.values()) {
				wasPaths.add(child.getProperty(PropertyIds.PATH).getValueAsString());
			}
			assertThat(wasPaths).containsOnly(paths);
			return this;
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
		return newCmisSessionBuilder().authenticatedBy(chuckNorrisKey, chuckNorrisToken).onCollection(zeCollection)
				.build();
	}

	private Session givenBobSessionOnZeCollection()
			throws RecordServicesException {
		return newCmisSessionBuilder().authenticatedBy(bobKey, bobToken).onCollection(zeCollection)
				.build();
	}

	private Authorization addAuthorizationWithoutDetaching(List<String> roles, List<String> grantedToPrincipals,
			List<String> grantedOnRecords) {
		AuthorizationDetails details = AuthorizationDetails.create(aString(), roles, zeCollection);

		Authorization authorization = new Authorization(details, grantedToPrincipals, grantedOnRecords);

		authorizationsServices.add(authorization, KEEP_ATTACHED, null);
		return authorization;
	}

}
