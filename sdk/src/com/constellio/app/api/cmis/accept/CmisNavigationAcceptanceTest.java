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
package com.constellio.app.api.cmis.accept;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.assertj.core.api.Condition;
import org.junit.Before;

import com.constellio.app.api.cmis.accept.CmisAcceptanceTestSetup.Records;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.DriverTest;
import com.constellio.sdk.tests.annotations.SlowTest;
import com.constellio.sdk.tests.setups.Users;

@SlowTest
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
		taxonomiesManager.addTaxonomy(zeCollectionSchemas.getTaxonomy1(), metadataSchemasManager);
		taxonomiesManager.setPrincipalTaxonomy(zeCollectionSchemas.getTaxonomy1(), metadataSchemasManager);
		zeCollectionRecords = zeCollectionSchemas.givenRecords(recordServices);

		userServices.addUserToCollection(users.bob(), zeCollection);
		userServices.addUserToCollection(users.chuckNorris(), zeCollection);

		recordServices.update(users.chuckNorrisIn(zeCollection).setCollectionReadAccess(true));
		getModelLayerFactory().newAuthenticationService().changePassword(chuckNorris, "1qaz2wsx");

	}

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
		assertThat(object).has(property("cmis:code", "zeCollection")).has(property("cmis:path", "/"))
				.has(property("cmis:type", "collection_default"));
	}

	private void whenGetTaxonomyThenReturnTaxonomy()
			throws Exception {
		String path = "/taxo1";
		CmisObject object = session.getObjectByPath(path);
		assertThat(object).has(property("cmis:code", "zeCollection")).has(property("cmis:path", path)).has(property("cmis:type",
				"taxonomy_default"));
	}

	private void whenGetFirstLevelConceptThenReturnFirstLevelConcept()
			throws Exception {
		String path = "/taxo1/" + zeCollectionRecords.taxo1_fond1;
		CmisObject object = session.getObjectByPath("/taxo1/" + zeCollectionRecords.taxo1_fond1);
		assertThat(object).has(property("cmis:code", "zeCollection")).has(property("cmis:path", path))
				.has(property("cmis:type", "fond_default"));
	}

	private void whenGetSecondLevelConceptThenReturnSecondLevelConcept()
			throws Exception {
		String path = "/taxo1/" + zeCollectionRecords.taxo1_fond1 + "/" + zeCollectionRecords.taxo1_category2;
		CmisObject object = session.getObjectByPath(path);
		assertThat(object).has(property("cmis:code", "zeCollection")).has(property("cmis:path", path))
				.has(property("cmis:type", "category_default"));
	}

	private void whenGetARootFolderThenReturnTheFolder()
			throws Exception {
		String path = "/taxo1/" + zeCollectionRecords.taxo1_fond1 + "/" + zeCollectionRecords.taxo1_category2 + "/"
				+ zeCollectionRecords.folder4;
		CmisObject object = session.getObjectByPath(path);
		assertThat(object).has(property("cmis:code", "zeCollection")).has(property("cmis:path", "folder_default"));
	}

	private void givenRootWhenGetChildrenThenObtainTaxonomies() {
		String path = "/";
		List<CmisObject> children = getChildren((Folder) session.getObjectByPath(path));
		assertThat(children).hasSize(2);
		assertThat(children.get(0)).has(id("taxo1")).has(property("cmis:path", path)).has(property("cmis:type", "taxonomy"));
		assertThat(children.get(1)).has(id("taxo2")).has(property("cmis:path", path)).has(property("cmis:type", "taxonomy"));
	}

	private void givenTaxo1WhenGetChildrenThenObtainRootConcepts() {
		String path = "/taxo1";
		List<CmisObject> children = getChildren((Folder) session.getObjectByPath(path));
		assertThat(children).hasSize(1);
		assertThat(children.get(0)).has(id("taxo1")).has(property("cmis:path", path)).has(property("cmis:type", "taxonomy"));
	}

	private void givenRootConceptWhenGetChildrenThenObtainSubConcepts() {
		String path = "/taxo1/" + zeCollectionRecords.taxo1_fond1;
		List<CmisObject> children = getChildren((Folder) session.getObjectByPath(path));
		assertThat(children).hasSize(2);
		assertThat(children.get(0)).has(id("taxo1")).has(property("cmis:path", path)).has(property("cmis:type", "taxonomy"));
		assertThat(children.get(1)).has(id("taxo2")).has(property("cmis:path", path)).has(property("cmis:type", "taxonomy"));
	}

	private void givenSubConceptWhenGetChildrenThenObtainSubSubConceptsAndRecords() {
		String path = "/taxo1/" + zeCollectionRecords.taxo1_fond1 + "/" + zeCollectionRecords.taxo1_category2;
		List<CmisObject> children = getChildren((Folder) session.getObjectByPath(path));
		assertThat(children).hasSize(2);
		assertThat(children.get(0)).has(id("taxo1")).has(property("cmis:path", path)).has(property("cmis:type", "taxonomy"));
		assertThat(children.get(1)).has(id("taxo2")).has(property("cmis:path", path)).has(property("cmis:type", "taxonomy"));

	}

	private Session givenAdminSessionOnZeCollection() {
		return newCmisSessionBuilder().authenticatedBy(chuckNorris, "1qaz2wsx").onCollection(zeCollection).build();
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
				return LangUtils.areNullableEqual(objectData.getId(), id);
			}
		};
	}

	private Condition<? super CmisObject> property(final String key, final Object value) {
		return new Condition<CmisObject>() {
			@Override
			public boolean matches(CmisObject objectData) {
				return LangUtils.areNullableEqual(objectData.getProperty(key), value);
			}
		};
	}
}
