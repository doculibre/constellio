package com.constellio.app.api.cmis.requests.navigation;

import com.constellio.app.api.cmis.accept.CmisAcceptanceTestSetup;
import com.constellio.app.api.cmis.accept.CmisAcceptanceTestSetup.Records;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.RecordCacheType;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.DriverTest;
import com.constellio.sdk.tests.setups.Users;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationInCollection;
import static com.constellio.model.entities.security.global.AuthorizationModificationRequest.modifyAuthorizationOnRecord;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

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

		MetadataSchemasManager schemasManager = getModelLayerFactory().getMetadataSchemasManager();

		MetadataSchemaTypesBuilder metadataSchemaTypesBuilder = schemasManager.modify(zeCollection);
		metadataSchemaTypesBuilder.getSchemaType(Authorization.SCHEMA_TYPE).setRecordCacheType(RecordCacheType.FULLY_CACHED);
		metadataSchemaTypesBuilder.getSchemaType(User.SCHEMA_TYPE).setRecordCacheType(RecordCacheType.FULLY_CACHED);
		metadataSchemaTypesBuilder.getSchemaType(Group.SCHEMA_TYPE).setRecordCacheType(RecordCacheType.FULLY_CACHED);

		CmisAcceptanceTestSetup.allSchemaTypesSupported(getAppLayerFactory());
		taxonomiesManager.addTaxonomy(zeCollectionSchemas.getTaxonomy1(), metadataSchemasManager);
		taxonomiesManager.addTaxonomy(zeCollectionSchemas.getTaxonomy2(), metadataSchemasManager);
		taxonomiesManager.setPrincipalTaxonomy(zeCollectionSchemas.getTaxonomy1(), metadataSchemasManager);
		zeCollectionRecords = zeCollectionSchemas.givenRecords(recordServices);

		userServices.execute(
				userServices.addUpdate(chuckNorris).setServiceKey(chuckNorrisKey).setSystemAdminEnabled());
		chuckNorrisToken = userServices.generateToken(chuckNorris);
		userServices.execute(users.chuckNorris().getUsername(), (req) -> req.addCollection(zeCollection));
		cmisSession = givenAdminSessionOnZeCollection();
		userServices.execute(
				userServices.addUpdate(bobGratton).setServiceKey(bobKey).setSystemAdminEnabled());
		bobToken = userServices.generateToken(bobGratton);
		userServices.execute(users.bob().getUsername(), (req) -> req.addCollection(zeCollection));

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
		ItemIterable<CmisObject> obtainedChildren = getChildrenOfObject("zeCollection");
		validateThat(obtainedChildren).hasChildrenIds("taxo_taxo1", "taxo_taxo2")
				.withPaths("/taxo_taxo1", "/taxo_taxo2");
	}

	@Test
	public void whenGettingChildrenThenOnlyAllowedChildrenReturned()
			throws Exception {

		Record folder2_2 = zeCollectionRecords.folder2_2;
		recordServices.refresh(folder2_2);
		assertThat(users.bobIn(zeCollection).hasReadAccess().on(folder2_2)).isFalse();

		cmisSession = givenBobSessionOnZeCollection();
		Authorization bobAuth = addAuthorizationWithoutDetaching(asList(Role.READ),
				asList(users.bobIn(zeCollection).getId()),
				zeCollectionRecords.folder2.getId());

		waitForBatchProcess();
		recordServices.refresh(folder2_2);
		authorizationsServices.execute(modifyAuthorizationOnRecord(bobAuth, folder2_2).removingItOnRecord());
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
		if ("zeCollection".equals(objectId)) {
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
														   String grantedOnRecord) {

		String id = getModelLayerFactory().newAuthorizationsServices().add(authorizationInCollection(zeCollection)
				.forPrincipalsIds(grantedToPrincipals).on(grantedOnRecord).giving(roles));
		return getModelLayerFactory().newAuthorizationsServices().getAuthorization(zeCollection, id);
	}

}
