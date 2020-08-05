package com.constellio.app.api.cmis.accept;

import com.constellio.app.api.cmis.accept.CmisAcceptanceTestSetup.Records;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.DriverTest;
import com.constellio.sdk.tests.setups.Users;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DriverTest
public class CmisNavigationAcceptanceTest extends ConstellioTest {
	UserServices userServices;
	TaxonomiesManager taxonomiesManager;
	MetadataSchemasManager metadataSchemasManager;
	RecordServices recordServices;
	Users users = new Users();
	CmisAcceptanceTestSetup zeCollectionSchemas = new CmisAcceptanceTestSetup(zeCollection);
	Records zeCollectionRecords;
	TaxonomiesSearchServices taxonomiesSearchServices;

	private String adminToken;

	//	Session cmisSession;
	Session session;

	@Before
	public void setUp()
			throws Exception {

		userServices = getModelLayerFactory().newUserServices();
		taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		recordServices = getModelLayerFactory().newRecordServices();

		taxonomiesSearchServices = getModelLayerFactory().newTaxonomiesSearchService();

		users.setUp(userServices);

		defineSchemasManager().using(zeCollectionSchemas);
		CmisAcceptanceTestSetup.allSchemaTypesSupported(getAppLayerFactory());
		taxonomiesManager.addTaxonomy(zeCollectionSchemas.getTaxonomy1(), metadataSchemasManager);
		taxonomiesManager.setPrincipalTaxonomy(zeCollectionSchemas.getTaxonomy1(), metadataSchemasManager);
		zeCollectionRecords = zeCollectionSchemas.givenRecords(recordServices);

		userServices.execute(users.adminAddUpdateRequest().setServiceKey("admin-key"));
		userServices.execute(users.admin().getUsername(), (req) -> req.addToCollection(zeCollection));
		recordServices.update(userServices.getUserInCollection(admin, zeCollection).setCollectionWriteAccess(true));
		getModelLayerFactory().newAuthenticationService().changePassword(admin, "1qaz2wsx");
		adminToken = userServices.generateToken(admin);

		CmisAcceptanceTestSetup.giveUseCMISPermissionToUsers(getModelLayerFactory());
	}

	@Test
	public void test()
			throws Exception {
		session = givenAdminSessionOnZeCollection();

		whenGetRootThenReturnCollection();
		whenGetTaxonomyThenReturnTaxonomy();
		whenGetFirstLevelConceptThenReturnFirstLevelConcept();
		whenGetSecondLevelConceptThenReturnSecondLevelConcept();

		givenRootWhenGetChildrenThenObtainTaxonomies();
		givenTaxo1WhenGetChildrenThenObtainRootConcepts();
		givenRootConceptWhenGetChildrenThenObtainSubConcepts();
		givenSubConceptWhenGetChildrenThenObtainSubSubConceptsAndRecords();

	}

	private void whenGetRootThenReturnCollection()
			throws Exception {

		CmisObject object = session.getObjectByPath("/");
		assertThat(object).has(property("cmis:path", "/")).has(property("cmis:objectTypeId", "collection_default"));
	}

	private void whenGetTaxonomyThenReturnTaxonomy()
			throws Exception {
		String path = "/taxo_taxo1";
		CmisObject object = session.getObjectByPath(path);
		assertThat(object).has(property("cmis:path", "/taxo_taxo1"))
				.has(property("cmis:objectTypeId", "taxonomy"));
	}

	private void whenGetFirstLevelConceptThenReturnFirstLevelConcept()
			throws Exception {
		String path = "/taxo_taxo1/" + zeCollectionRecords.taxo1_fond1;
		CmisObject object = session.getObjectByPath("/taxo1/" + zeCollectionRecords.taxo1_fond1);
		assertThat(object).has(property("cmis:path", path))
				.has(property("cmis:objectTypeId", "documentFond_default"));
	}

	private void whenGetSecondLevelConceptThenReturnSecondLevelConcept()
			throws Exception {
		String path = "/taxo_taxo1/" + zeCollectionRecords.taxo1_fond1 + "/" + zeCollectionRecords.taxo1_category2;
		CmisObject object = session.getObjectByPath(path);
		assertThat(object).has(property("cmis:path", path))
				.has(property("cmis:objectTypeId", "category_default"));
	}

	private void whenGetARootFolderThenReturnTheFolder()
			throws Exception {
		String path = "/taxo_taxo1/" + zeCollectionRecords.taxo1_fond1 + "/" + zeCollectionRecords.taxo1_category2 + "/"
					  + zeCollectionRecords.folder4;
		CmisObject object = session.getObjectByPath(path);
		assertThat(object).has(property("cmis:path", "folder_default"));
	}

	private void givenRootWhenGetChildrenThenObtainTaxonomies() {
		String path = "/";
		List<CmisObject> children = getChildren((Folder) session.getObjectByPath(path));
		assertThat(children).hasSize(1);
		assertThat(children.get(0)).has(id("taxo_taxo1")).has(property("cmis:path", "/taxo_taxo1"))
				.has(property("cmis:objectTypeId", "taxonomy"));
		//assertThat(children.get(1)).has(id("taxo2")).has(property("cmis:path", path)).has(property("cmis:type", "taxonomy"));
	}

	private void givenTaxo1WhenGetChildrenThenObtainRootConcepts() {
		String path = "/taxo_taxo1";
		List<CmisObject> children = getChildren((Folder) session.getObjectByPath(path));
		assertThat(children).hasSize(1);
		assertThat(children.get(0)).has(id("zetaxo1_fond1")).has(property("cmis:path", "/taxo_taxo1/zetaxo1_fond1"))
				.has(property("cmis:objectTypeId", "documentFond_default"));
	}

	private void givenRootConceptWhenGetChildrenThenObtainSubConcepts() {
		String path = "/taxo_taxo1/" + zeCollectionRecords.taxo1_fond1;
		List<CmisObject> children = getChildren((Folder) session.getObjectByPath(path));
		assertThat(children).hasSize(2);
		assertThat(children.get(0)).has(id("zetaxo1_category2"))
				.has(property("cmis:path", "/taxo_taxo1/zetaxo1_fond1/zetaxo1_category2"))
				.has(property("cmis:objectTypeId", "category_default"));
		assertThat(children.get(1)).has(id("zetaxo1_fond1_1"))
				.has(property("cmis:path", "/taxo_taxo1/zetaxo1_fond1/zetaxo1_fond1_1"))
				.has(property("cmis:objectTypeId", "documentFond_default"));
	}

	private void givenSubConceptWhenGetChildrenThenObtainSubSubConceptsAndRecords() {
		String path = "/taxo_taxo1/" + zeCollectionRecords.taxo1_fond1 + "/" + zeCollectionRecords.taxo1_category2;
		List<CmisObject> children = getChildren((Folder) session.getObjectByPath(path));
		assertThat(children).hasSize(2);
		assertThat(children.get(0)).has(id("zetaxo1_category2_1"))
				.has(property("cmis:path", "/taxo_taxo1/zetaxo1_fond1/zetaxo1_category2/zetaxo1_category2_1"))
				.has(property("cmis:objectTypeId", "category_default"));
		assertThat(children.get(1)).has(id("folder4"))
				.has(property("cmis:path", "/taxo_taxo1/zetaxo1_fond1/zetaxo1_category2/folder4"))
				.has(property("cmis:objectTypeId", "folder_default"));
	}

	private Session givenAdminSessionOnZeCollection() {
		return newCmisSessionBuilder().authenticatedBy("admin-key", adminToken).onCollection(zeCollection).build();
	}

	private List<CmisObject> getChildren(Folder folder) {
		Iterator<CmisObject> childrenIterator = folder.getChildren().iterator();
		List<CmisObject> children = new ArrayList<>();
		while (childrenIterator.hasNext()) {
			children.add(childrenIterator.next());
		}
		return children;
	}

	private Condition<? super CmisObject> id(final String id) {
		return new Condition<CmisObject>() {
			@Override
			public boolean matches(CmisObject objectData) {
				assertThat(objectData.getId()).describedAs("id").isEqualTo(id);
				return true;
			}
		};
	}

	private Condition<? super CmisObject> property(final String key, final Object value) {
		return new Condition<CmisObject>() {
			@Override
			public boolean matches(CmisObject objectData) {
				assertThat(objectData.<Object>getPropertyValue(key)).describedAs(key).isEqualTo(value);
				return true;
			}
		};
	}
}
